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
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.SingleThreadedDmaapTopicSource;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.endpoints.utils.DmaapPropertyUtils;
import org.onap.policy.common.endpoints.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of DMAAP Source Topics indexed by topic name.
 */

class IndexedDmaapTopicSourceFactory implements DmaapTopicSourceFactory {
    private static final String MISSING_TOPIC = "A topic must be provided";

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedDmaapTopicSourceFactory.class);

    /**
     * DMaaP Topic Name Index.
     */
    protected HashMap<String, DmaapTopicSource> dmaapTopicSources = new HashMap<>();

    @Override
    public DmaapTopicSource build(BusTopicParams busTopicParams) {

        if (busTopicParams.getTopic() == null || busTopicParams.getTopic().isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (dmaapTopicSources.containsKey(busTopicParams.getTopic())) {
                return dmaapTopicSources.get(busTopicParams.getTopic());
            }

            var dmaapTopicSource = makeSource(busTopicParams);

            if (busTopicParams.isManaged()) {
                dmaapTopicSources.put(busTopicParams.getTopic(), dmaapTopicSource);
            }
            return dmaapTopicSource;
        }
    }

    @Override
    public List<DmaapTopicSource> build(Properties properties) {

        String readTopics = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS);
        if (StringUtils.isBlank(readTopics)) {
            logger.info("{}: no topic for DMaaP Source", this);
            return new ArrayList<>();
        }

        List<DmaapTopicSource> dmaapTopicSourceLst = new ArrayList<>();
        synchronized (this) {
            for (String topic : readTopics.split("\\s*,\\s*")) {
                addTopic(dmaapTopicSourceLst, properties, topic);
            }
        }
        return dmaapTopicSourceLst;
    }

    @Override
    public DmaapTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret) {
        return this.build(BusTopicParams.builder()
                .servers(servers)
                .topic(topic)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .fetchTimeout(PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH)
                .fetchLimit(PolicyEndPointProperties.DEFAULT_LIMIT_FETCH)
                .managed(true)
                .useHttps(false)
                .allowSelfSignedCerts(false)
                .build());
    }

    @Override
    public DmaapTopicSource build(List<String> servers, String topic) {
        return this.build(servers, topic, null, null);
    }

    private void addTopic(List<DmaapTopicSource> dmaapTopicSourceLst, Properties properties, String topic) {
        if (this.dmaapTopicSources.containsKey(topic)) {
            dmaapTopicSourceLst.add(this.dmaapTopicSources.get(topic));
            return;
        }

        String topicPrefix = PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + topic;

        var props = new PropertyUtils(properties, topicPrefix,
            (name, value, ex) -> logger.warn("{}: {} {} is in invalid format for topic {} ", this, name, value, topic));

        String servers = properties.getProperty(topicPrefix + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);
        if (StringUtils.isBlank(servers)) {
            logger.error("{}: no DMaaP servers or DME2 ServiceName provided", this);
            return;
        }

        DmaapTopicSource uebTopicSource = this.build(DmaapPropertyUtils.makeBuilder(props, topic, servers)
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

        dmaapTopicSourceLst.add(uebTopicSource);
    }

    /**
     * Makes a new source.
     *
     * @param busTopicParams parameters to use to configure the source
     * @return a new source
     */
    protected DmaapTopicSource makeSource(BusTopicParams busTopicParams) {
        return new SingleThreadedDmaapTopicSource(busTopicParams);
    }

    @Override
    public void destroy(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        DmaapTopicSource uebTopicSource;

        synchronized (this) {
            if (!dmaapTopicSources.containsKey(topic)) {
                return;
            }

            uebTopicSource = dmaapTopicSources.remove(topic);
        }

        uebTopicSource.shutdown();
    }

    @Override
    public void destroy() {
        List<DmaapTopicSource> readers = this.inventory();
        for (DmaapTopicSource reader : readers) {
            reader.shutdown();
        }

        synchronized (this) {
            this.dmaapTopicSources.clear();
        }
    }

    @Override
    public DmaapTopicSource get(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (dmaapTopicSources.containsKey(topic)) {
                return dmaapTopicSources.get(topic);
            } else {
                throw new IllegalStateException("DmaapTopiceSource for " + topic + " not found");
            }
        }
    }

    @Override
    public synchronized List<DmaapTopicSource> inventory() {
        return new ArrayList<>(this.dmaapTopicSources.values());
    }

    @Override
    public String toString() {
        return "IndexedDmaapTopicSourceFactory []";
    }

}
