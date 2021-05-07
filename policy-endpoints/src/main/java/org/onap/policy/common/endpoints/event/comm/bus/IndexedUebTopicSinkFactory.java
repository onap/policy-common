/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
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
import org.onap.policy.common.endpoints.event.comm.bus.internal.InlineUebTopicSink;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.endpoints.utils.PropertyUtils;
import org.onap.policy.common.endpoints.utils.UebPropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of UEB Reader Topics indexed by topic name.
 */
class IndexedUebTopicSinkFactory implements UebTopicSinkFactory {
    private static final Pattern COMMA_SPACE_PAT = Pattern.compile("\\s*,\\s*");
    private static final String MISSING_TOPIC = "A topic must be provided";

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedUebTopicSinkFactory.class);

    /**
     * UEB Topic Name Index.
     */
    protected HashMap<String, UebTopicSink> uebTopicSinks = new HashMap<>();

    @Override
    public UebTopicSink build(BusTopicParams busTopicParams) {

        if (busTopicParams.getServers() == null || busTopicParams.getServers().isEmpty()) {
            throw new IllegalArgumentException("UEB Server(s) must be provided");
        }

        if (StringUtils.isBlank(busTopicParams.getTopic())) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (uebTopicSinks.containsKey(busTopicParams.getTopic())) {
                return uebTopicSinks.get(busTopicParams.getTopic());
            }

            UebTopicSink uebTopicWriter = makeSink(busTopicParams);

            if (busTopicParams.isManaged()) {
                uebTopicSinks.put(busTopicParams.getTopic(), uebTopicWriter);
            }

            return uebTopicWriter;
        }
    }


    @Override
    public UebTopicSink build(List<String> servers, String topic) {
        return this.build(BusTopicParams.builder()
                .servers(servers)
                .topic(topic)
                .managed(true)
                .useHttps(false)
                .allowSelfSignedCerts(false)
                .build());
    }


    @Override
    public List<UebTopicSink> build(Properties properties) {

        String writeTopics = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS);
        if (StringUtils.isBlank(writeTopics)) {
            logger.info("{}: no topic for UEB Sink", this);
            return new ArrayList<>();
        }

        List<UebTopicSink> newUebTopicSinks = new ArrayList<>();
        synchronized (this) {
            for (String topic : COMMA_SPACE_PAT.split(writeTopics)) {
                addTopic(newUebTopicSinks, topic, properties);
            }
            return newUebTopicSinks;
        }
    }

    private void addTopic(List<UebTopicSink> newUebTopicSinks, String topic, Properties properties) {
        if (this.uebTopicSinks.containsKey(topic)) {
            newUebTopicSinks.add(this.uebTopicSinks.get(topic));
            return;
        }

        String topicPrefix = PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS + "." + topic;

        var props = new PropertyUtils(properties, topicPrefix,
            (name, value, ex) -> logger.warn("{}: {} {} is in invalid format for topic {} ", this, name, value, topic));

        String servers = properties.getProperty(topicPrefix + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);
        if (StringUtils.isBlank(servers)) {
            logger.error("{}: no UEB servers configured for sink {}", this, topic);
            return;
        }

        UebTopicSink uebTopicWriter = this.build(UebPropertyUtils.makeBuilder(props, topic, servers)
                .partitionId(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX, null))
                .build());
        newUebTopicSinks.add(uebTopicWriter);
    }

    @Override
    public void destroy(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        UebTopicSink uebTopicWriter;
        synchronized (this) {
            if (!uebTopicSinks.containsKey(topic)) {
                return;
            }

            uebTopicWriter = uebTopicSinks.remove(topic);
        }

        uebTopicWriter.shutdown();
    }

    @Override
    public void destroy() {
        List<UebTopicSink> writers = this.inventory();
        for (UebTopicSink writer : writers) {
            writer.shutdown();
        }

        synchronized (this) {
            this.uebTopicSinks.clear();
        }
    }

    @Override
    public UebTopicSink get(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (uebTopicSinks.containsKey(topic)) {
                return uebTopicSinks.get(topic);
            } else {
                throw new IllegalStateException("UebTopicSink for " + topic + " not found");
            }
        }
    }

    @Override
    public synchronized List<UebTopicSink> inventory() {
        return new ArrayList<>(this.uebTopicSinks.values());
    }

    /**
     * Makes a new sink.
     *
     * @param busTopicParams parameters to use to configure the sink
     * @return a new sink
     */
    protected UebTopicSink makeSink(BusTopicParams busTopicParams) {
        return new InlineUebTopicSink(busTopicParams);
    }


    @Override
    public String toString() {
        return "IndexedUebTopicSinkFactory []";
    }

}
