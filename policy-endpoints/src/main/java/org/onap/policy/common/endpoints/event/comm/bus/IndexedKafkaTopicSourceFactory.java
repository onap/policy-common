/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.endpoints.event.comm.bus;

import com.google.re2j.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.SingleThreadedKafkaTopicSource;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.endpoints.utils.KafkaPropertyUtils;
import org.onap.policy.common.endpoints.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of KAFKA Source Topics indexed by topic name.
 */
class IndexedKafkaTopicSourceFactory implements KafkaTopicSourceFactory {
    private static final Pattern COMMA_SPACE_PAT = Pattern.compile("\\s*,\\s*");
    private static final String MISSING_TOPIC = "A topic must be provided";

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedKafkaTopicSourceFactory.class);

    /**
     * KAFKA Topic Name Index.
     */
    protected HashMap<String, KafkaTopicSource> kafkaTopicSources = new HashMap<>();

    @Override
    public KafkaTopicSource build(BusTopicParams busTopicParams) {
        if (busTopicParams.getServers() == null || busTopicParams.getServers().isEmpty()) {
            throw new IllegalArgumentException("KAFKA Server(s) must be provided");
        }

        if (busTopicParams.getTopic() == null || busTopicParams.getTopic().isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (kafkaTopicSources.containsKey(busTopicParams.getTopic())) {
                return kafkaTopicSources.get(busTopicParams.getTopic());
            }

            var kafkaTopicSource = makeSource(busTopicParams);
            kafkaTopicSources.put(busTopicParams.getTopic(), kafkaTopicSource);

            return kafkaTopicSource;
        }
    }

    @Override
    public List<KafkaTopicSource> build(Properties properties) {

        String readTopics = properties.getProperty(PolicyEndPointProperties.PROPERTY_KAFKA_SOURCE_TOPICS);
        if (StringUtils.isBlank(readTopics)) {
            logger.info("{}: no topic for KAFKA Source", this);
            return new ArrayList<>();
        }

        List<KafkaTopicSource> newKafkaTopicSources = new ArrayList<>();
        synchronized (this) {
            for (String topic : COMMA_SPACE_PAT.split(readTopics)) {
                addTopic(newKafkaTopicSources, topic, properties);
            }
        }
        return newKafkaTopicSources;
    }

    @Override
    public KafkaTopicSource build(List<String> servers, String topic) {
        return this.build(BusTopicParams.builder()
                .servers(servers)
                .topic(topic)
                .managed(true)
                .useHttps(false).build());
    }

    private void addTopic(List<KafkaTopicSource> newKafkaTopicSources, String topic, Properties properties) {
        if (this.kafkaTopicSources.containsKey(topic)) {
            newKafkaTopicSources.add(this.kafkaTopicSources.get(topic));
            return;
        }

        String topicPrefix = PolicyEndPointProperties.PROPERTY_KAFKA_SOURCE_TOPICS + "." + topic;

        var props = new PropertyUtils(properties, topicPrefix,
            (name, value, ex) -> logger.warn("{}: {} {} is in invalid format for topic {} ", this, name, value, topic));

        String servers = properties.getProperty(topicPrefix + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);
        if (StringUtils.isBlank(servers)) {
            logger.error("{}: no KAFKA servers configured for sink {}", this, topic);
            return;
        }

        var kafkaTopicSource = this.build(KafkaPropertyUtils.makeBuilder(props, topic, servers)
                .build());

        newKafkaTopicSources.add(kafkaTopicSource);
    }

    /**
     * Makes a new source.
     *
     * @param busTopicParams parameters to use to configure the source
     * @return a new source
     */
    protected KafkaTopicSource makeSource(BusTopicParams busTopicParams) {
        return new SingleThreadedKafkaTopicSource(busTopicParams);
    }

    @Override
    public void destroy(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        KafkaTopicSource kafkaTopicSource;

        synchronized (this) {
            if (!kafkaTopicSources.containsKey(topic)) {
                return;
            }

            kafkaTopicSource = kafkaTopicSources.remove(topic);
        }

        kafkaTopicSource.shutdown();
    }

    @Override
    public void destroy() {
        List<KafkaTopicSource> readers = this.inventory();
        for (KafkaTopicSource reader : readers) {
            reader.shutdown();
        }

        synchronized (this) {
            this.kafkaTopicSources.clear();
        }
    }

    @Override
    public KafkaTopicSource get(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (kafkaTopicSources.containsKey(topic)) {
                return kafkaTopicSources.get(topic);
            } else {
                throw new IllegalStateException("KafkaTopiceSource for " + topic + " not found");
            }
        }
    }

    @Override
    public synchronized List<KafkaTopicSource> inventory() {
        return new ArrayList<>(this.kafkaTopicSources.values());
    }

    @Override
    public String toString() {
        return "IndexedKafkaTopicSourceFactory " + kafkaTopicSources.keySet();
    }
}
