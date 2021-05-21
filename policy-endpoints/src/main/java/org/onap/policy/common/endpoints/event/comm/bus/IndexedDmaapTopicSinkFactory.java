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
import org.onap.policy.common.endpoints.event.comm.bus.internal.InlineDmaapTopicSink;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.endpoints.utils.DmaapPropertyUtils;
import org.onap.policy.common.endpoints.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of DMAAP Reader Topics indexed by topic name.
 */
class IndexedDmaapTopicSinkFactory implements DmaapTopicSinkFactory {

    private static final Pattern COMMA_SPACE_PAT = Pattern.compile("\\s*,\\s*");
    private static final String MISSING_TOPIC = "A topic must be provided";

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedDmaapTopicSinkFactory.class);

    /**
     * DMAAP Topic Name Index.
     */
    protected HashMap<String, DmaapTopicSink> dmaapTopicWriters = new HashMap<>();

    @Override
    public DmaapTopicSink build(BusTopicParams busTopicParams) {

        if (StringUtils.isBlank(busTopicParams.getTopic())) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (dmaapTopicWriters.containsKey(busTopicParams.getTopic())) {
                return dmaapTopicWriters.get(busTopicParams.getTopic());
            }

            var dmaapTopicSink = makeSink(busTopicParams);

            if (busTopicParams.isManaged()) {
                dmaapTopicWriters.put(busTopicParams.getTopic(), dmaapTopicSink);
            }
            return dmaapTopicSink;
        }
    }

    @Override
    public DmaapTopicSink build(List<String> servers, String topic) {
        return this.build(BusTopicParams.builder()
                .servers(servers)
                .topic(topic)
                .managed(true)
                .useHttps(false)
                .allowSelfSignedCerts(false)
                .build());
    }

    @Override
    public List<DmaapTopicSink> build(Properties properties) {

        String writeTopics = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS);
        if (StringUtils.isBlank(writeTopics)) {
            logger.info("{}: no topic for DMaaP Sink", this);
            return new ArrayList<>();
        }

        List<DmaapTopicSink> newDmaapTopicSinks = new ArrayList<>();
        synchronized (this) {
            for (String topic : COMMA_SPACE_PAT.split(writeTopics)) {
                addTopic(newDmaapTopicSinks, properties, topic);
            }
            return newDmaapTopicSinks;
        }
    }

    private void addTopic(List<DmaapTopicSink> newDmaapTopicSinks, Properties properties, String topic) {
        if (this.dmaapTopicWriters.containsKey(topic)) {
            newDmaapTopicSinks.add(this.dmaapTopicWriters.get(topic));
            return;
        }

        String topicPrefix = PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "." + topic;

        var props = new PropertyUtils(properties, topicPrefix,
            (name, value, ex) -> logger.warn("{}: {} {} is in invalid format for topic {} ", this, name, value, topic));

        String servers = properties.getProperty(topicPrefix + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);
        if (StringUtils.isBlank(servers)) {
            logger.error("{}: no DMaaP servers or DME2 ServiceName provided", this);
            return;
        }

        var dmaapTopicSink = this.build(DmaapPropertyUtils.makeBuilder(props, topic, servers)
                .partitionId(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX, null))
                .build());

        newDmaapTopicSinks.add(dmaapTopicSink);
    }

    /**
     * Makes a new sink.
     *
     * @param busTopicParams parameters to use to configure the sink
     * @return a new sink
     */
    protected DmaapTopicSink makeSink(BusTopicParams busTopicParams) {
        return new InlineDmaapTopicSink(busTopicParams);
    }

    @Override
    public void destroy(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        DmaapTopicSink dmaapTopicWriter;
        synchronized (this) {
            if (!dmaapTopicWriters.containsKey(topic)) {
                return;
            }

            dmaapTopicWriter = dmaapTopicWriters.remove(topic);
        }

        dmaapTopicWriter.shutdown();
    }

    @Override
    public void destroy() {
        List<DmaapTopicSink> writers = this.inventory();
        for (DmaapTopicSink writer : writers) {
            writer.shutdown();
        }

        synchronized (this) {
            this.dmaapTopicWriters.clear();
        }
    }

    @Override
    public DmaapTopicSink get(String topic) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (dmaapTopicWriters.containsKey(topic)) {
                return dmaapTopicWriters.get(topic);
            } else {
                throw new IllegalStateException("DmaapTopicSink for " + topic + " not found");
            }
        }
    }

    @Override
    public synchronized List<DmaapTopicSink> inventory() {
        return new ArrayList<>(this.dmaapTopicWriters.values());
    }

    @Override
    public String toString() {
        return "IndexedDmaapTopicSinkFactory " + dmaapTopicWriters.keySet();
    }

}
