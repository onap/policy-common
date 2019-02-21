/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.InlineDmaapTopicSink;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of DMAAP Reader Topics indexed by topic name.
 */
class IndexedDmaapTopicSinkFactory implements DmaapTopicSinkFactory {

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

        if (busTopicParams.getTopic() == null || busTopicParams.getTopic().isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (dmaapTopicWriters.containsKey(busTopicParams.getTopic())) {
                return dmaapTopicWriters.get(busTopicParams.getTopic());
            }

            DmaapTopicSink dmaapTopicSink = makeSink(busTopicParams);

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
        if (writeTopics == null || writeTopics.isEmpty()) {
            logger.info("{}: no topic for DMaaP Sink", this);
            return new ArrayList<>();
        }

        List<String> writeTopicList = new ArrayList<>(Arrays.asList(writeTopics.split("\\s*,\\s*")));
        List<DmaapTopicSink> newDmaapTopicSinks = new ArrayList<>();
        synchronized (this) {
            for (String topic : writeTopicList) {
                if (this.dmaapTopicWriters.containsKey(topic)) {
                    newDmaapTopicSinks.add(this.dmaapTopicWriters.get(topic));
                    continue;
                }
                String servers = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);

                List<String> serverList;
                if (servers != null && !servers.isEmpty()) {
                    serverList = new ArrayList<>(Arrays.asList(servers.split("\\s*,\\s*")));
                } else {
                    serverList = new ArrayList<>();
                }

                final String effectiveTopic = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                    + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX, topic);

                final String apiKey = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX);
                final String apiSecret = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX);

                final String aafMechId = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_AAF_MECHID_SUFFIX);
                final String aafPassword = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_AAF_PASSWORD_SUFFIX);

                final String partitionKey = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX);

                final String managedString = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX);

                /* DME2 Properties */

                final String dme2Environment = properties.getProperty(
                        PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                                + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX);

                final String dme2AftEnvironment = properties.getProperty(
                        PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                                + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX);

                final String dme2Partner = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_PARTNER_SUFFIX);

                final String dme2RouteOffer = properties.getProperty(
                        PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                                + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX);

                final String dme2Latitude = properties.getProperty(
                        PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "."
                                + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX);

                final String dme2Longitude = properties.getProperty(
                        PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                                + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX);

                final String dme2EpReadTimeoutMs = properties.getProperty(
                        PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                                + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX);

                final String dme2EpConnTimeout = properties.getProperty(
                        PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                                + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX);

                final String dme2RoundtripTimeoutMs =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                                + "." + topic
                                + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX);

                final String dme2Version = properties.getProperty(
                        PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "."
                                + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_VERSION_SUFFIX);

                final String dme2SubContextPath = properties.getProperty(
                        PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                                + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX);

                final String dme2SessionStickinessRequired =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS
                                + "." + topic
                                + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX);

                Map<String, String> dme2AdditionalProps = new HashMap<>();

                if (dme2EpReadTimeoutMs != null && !dme2EpReadTimeoutMs.isEmpty()) {
                    dme2AdditionalProps.put(DME2_READ_TIMEOUT_PROPERTY, dme2EpReadTimeoutMs);
                }
                if (dme2EpConnTimeout != null && !dme2EpConnTimeout.isEmpty()) {
                    dme2AdditionalProps.put(DME2_EP_CONN_TIMEOUT_PROPERTY, dme2EpConnTimeout);
                }
                if (dme2RoundtripTimeoutMs != null && !dme2RoundtripTimeoutMs.isEmpty()) {
                    dme2AdditionalProps.put(DME2_ROUNDTRIP_TIMEOUT_PROPERTY, dme2RoundtripTimeoutMs);
                }
                if (dme2Version != null && !dme2Version.isEmpty()) {
                    dme2AdditionalProps.put(DME2_VERSION_PROPERTY, dme2Version);
                }
                if (dme2RouteOffer != null && !dme2RouteOffer.isEmpty()) {
                    dme2AdditionalProps.put(DME2_ROUTE_OFFER_PROPERTY, dme2RouteOffer);
                }
                if (dme2SubContextPath != null && !dme2SubContextPath.isEmpty()) {
                    dme2AdditionalProps.put(DME2_SUBCONTEXT_PATH_PROPERTY, dme2SubContextPath);
                }
                if (dme2SessionStickinessRequired != null && !dme2SessionStickinessRequired.isEmpty()) {
                    dme2AdditionalProps.put(DME2_SESSION_STICKINESS_REQUIRED_PROPERTY, dme2SessionStickinessRequired);
                }

                if (servers == null || servers.isEmpty()) {
                    logger.error("{}: no DMaaP servers or DME2 ServiceName provided", this);
                    continue;
                }

                boolean managed = true;
                if (managedString != null && !managedString.isEmpty()) {
                    managed = Boolean.parseBoolean(managedString);
                }

                String useHttpsString = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX);

                // default is to use HTTP if no https property exists
                boolean useHttps = false;
                if (useHttpsString != null && !useHttpsString.isEmpty()) {
                    useHttps = Boolean.parseBoolean(useHttpsString);
                }

                String allowSelfSignedCertsString =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "." + topic
                                + PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX);

                // default is to disallow self-signed certs
                boolean allowSelfSignedCerts = false;
                if (allowSelfSignedCertsString != null && !allowSelfSignedCertsString.isEmpty()) {
                    allowSelfSignedCerts = Boolean.parseBoolean(allowSelfSignedCertsString);
                }

                DmaapTopicSink dmaapTopicSink = this.build(BusTopicParams.builder()
                        .servers(serverList)
                        .topic(topic)
                        .effectiveTopic(effectiveTopic)
                        .apiKey(apiKey)
                        .apiSecret(apiSecret)
                        .userName(aafMechId)
                        .password(aafPassword)
                        .partitionId(partitionKey)
                        .environment(dme2Environment)
                        .aftEnvironment(dme2AftEnvironment)
                        .partner(dme2Partner)
                        .latitude(dme2Latitude)
                        .longitude(dme2Longitude)
                        .additionalProps(dme2AdditionalProps)
                        .managed(managed)
                        .useHttps(useHttps)
                        .allowSelfSignedCerts(allowSelfSignedCerts)
                        .build());

                newDmaapTopicSinks.add(dmaapTopicSink);
            }
            return newDmaapTopicSinks;
        }
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
        return "IndexedDmaapTopicSinkFactory []";
    }

}
