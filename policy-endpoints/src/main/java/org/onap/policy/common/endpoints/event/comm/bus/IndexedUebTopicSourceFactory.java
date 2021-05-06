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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.SingleThreadedUebTopicSource;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.endpoints.utils.PropertyUtils;
import org.onap.policy.common.endpoints.utils.UebPropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of UEB Source Topics indexed by topic name.
 */
class IndexedUebTopicSourceFactory implements UebTopicSourceFactory {
    private static final Pattern COMMA_SPACE_PAT = Pattern.compile("\\s*,\\s*");
    private static final String MISSING_TOPIC = "A topic must be provided";

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedUebTopicSourceFactory.class);

    /**
     * UEB Topic Name Index.
     */
    protected HashMap<String, UebTopicSource> uebTopicSources = new HashMap<>();

    @Override
    public UebTopicSource build(BusTopicParams busTopicParams) {
        if (busTopicParams.getServers() == null || busTopicParams.getServers().isEmpty()) {
            throw new IllegalArgumentException("UEB Server(s) must be provided");
        }

        if (busTopicParams.getTopic() == null || busTopicParams.getTopic().isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (uebTopicSources.containsKey(busTopicParams.getTopic())) {
                return uebTopicSources.get(busTopicParams.getTopic());
            }

            var uebTopicSource = makeSource(busTopicParams);

            if (busTopicParams.isManaged()) {
                uebTopicSources.put(busTopicParams.getTopic(), uebTopicSource);
            }

            return uebTopicSource;
        }
    }

    @Override
    public List<UebTopicSource> build(Properties properties) {

        String readTopics = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS);
        if (StringUtils.isBlank(readTopics)) {
            logger.info("{}: no topic for UEB Source", this);
            return new ArrayList<>();
        }

        List<UebTopicSource> newUebTopicSources = new ArrayList<>();
        synchronized (this) {
            for (String topic : COMMA_SPACE_PAT.split(readTopics)) {
                addTopic(newUebTopicSources, topic, properties);
            }
        }
        return newUebTopicSources;
    }

    @Override
    public UebTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret) {

        return this.build(BusTopicParams.builder()
                .servers(servers)
                .topic(topic)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .fetchTimeout(PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH)
                .fetchLimit(PolicyEndPointProperties.DEFAULT_LIMIT_FETCH)
                .managed(true)
                .useHttps(false)
                .allowSelfSignedCerts(true).build());
    }

    @Override
    public UebTopicSource build(List<String> servers, String topic) {
        return this.build(servers, topic, null, null);
    }

    private void addTopic(List<UebTopicSource> newUebTopicSources, String topic, Properties properties) {
        if (this.uebTopicSources.containsKey(topic)) {
            newUebTopicSources.add(this.uebTopicSources.get(topic));
            return;
        }

        String topicPrefix = PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "." + topic;

        var props = new PropertyUtils(properties, topicPrefix,
            (name, value, ex) -> logger.warn("{}: {} {} is in invalid format for topic {} ", this, name, value, topic));

        String servers = properties.getProperty(topicPrefix + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);
        if (StringUtils.isBlank(servers)) {
            logger.error("{}: no UEB servers configured for sink {}", this, topic);
            return;
        }

        var uebTopicSource = this.build(UebPropertyUtils.makeBuilder(props, topic, servers)
                .consumerGroup(props.getString(
                                PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX, null))
                .consumerInstance(props.getString(
                                PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX, null))
                .fetchTimeout(props.getInteger(
                                PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX,
                                PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH))
                .fetchLimit(props.getInteger(PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX,
                                PolicyEndPointProperties.DEFAULT_LIMIT_FETCH))
                .build());

        newUebTopicSources.add(uebTopicSource);
    }

    /**
     * Makes a new source.
     *
     * @param busTopicParams parameters to use to configure the source
     * @return a new source
     */
    protected UebTopicSource makeSource(BusTopicParams busTopicParams) {
        return new SingleThreadedUebTopicSource(busTopicParams);
    }

    @Override
    public void destroy(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        UebTopicSource uebTopicSource;

        synchronized (this) {
            if (!uebTopicSources.containsKey(topic)) {
                return;
            }

            uebTopicSource = uebTopicSources.remove(topic);
        }

        uebTopicSource.shutdown();
    }

    @Override
    public void destroy() {
        List<UebTopicSource> readers = this.inventory();
        for (UebTopicSource reader : readers) {
            reader.shutdown();
        }

        synchronized (this) {
            this.uebTopicSources.clear();
        }
    }

    @Override
    public UebTopicSource get(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (uebTopicSources.containsKey(topic)) {
                return uebTopicSources.get(topic);
            } else {
                throw new IllegalStateException("UebTopiceSource for " + topic + " not found");
            }
        }
    }

    @Override
    public synchronized List<UebTopicSource> inventory() {
        return new ArrayList<>(this.uebTopicSources.values());
    }

    @Override
    public String toString() {
        return "IndexedUebTopicSourceFactory []";
    }
}
