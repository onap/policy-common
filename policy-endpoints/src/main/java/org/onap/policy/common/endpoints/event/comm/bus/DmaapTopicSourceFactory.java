/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modified Copyright (C) 2018 Samsung Electronics Co., Ltd.
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
import org.onap.policy.common.endpoints.event.comm.bus.internal.SingleThreadedDmaapTopicSource;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DMAAP Topic Source Factory.
 */
public interface DmaapTopicSourceFactory {
    String DME2_READ_TIMEOUT_PROPERTY = "AFT_DME2_EP_READ_TIMEOUT_MS";
    String DME2_EP_CONN_TIMEOUT_PROPERTY = "AFT_DME2_EP_CONN_TIMEOUT";
    String DME2_ROUNDTRIP_TIMEOUT_PROPERTY = "AFT_DME2_ROUNDTRIP_TIMEOUT_MS";
    String DME2_VERSION_PROPERTY = "Version";
    String DME2_ROUTE_OFFER_PROPERTY = "routeOffer";
    String DME2_SERVICE_NAME_PROPERTY = "ServiceName";
    String DME2_SUBCONTEXT_PATH_PROPERTY = "SubContextPath";
    String DME2_SESSION_STICKINESS_REQUIRED_PROPERTY = "sessionstickinessrequired";

    /**
     * Creates an DMAAP Topic Source based on properties files.
     * 
     * @param properties Properties containing initialization values
     * 
     * @return an DMAAP Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    List<DmaapTopicSource> build(Properties properties);

    /**
     * Instantiates a new DMAAP Topic Source.
     * 
     * @param busTopicParams parameters object
     * @return a DMAAP Topic Source
     */
    DmaapTopicSource build(BusTopicParams busTopicParams);

    /**
     * Instantiates a new DMAAP Topic Source.
     * 
     * @param servers list of servers
     * @param topic topic name
     * @param apiKey API Key
     * @param apiSecret API Secret
     * 
     * @return an DMAAP Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    DmaapTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret);

    /**
     * Instantiates a new DMAAP Topic Source.
     * 
     * @param servers list of servers
     * @param topic topic name
     * 
     * @return an DMAAP Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    DmaapTopicSource build(List<String> servers, String topic);

    /**
     * Destroys an DMAAP Topic Source based on a topic.
     * 
     * @param topic topic name
     * @throws IllegalArgumentException if invalid parameters are present
     */
    void destroy(String topic);

    /**
     * Destroys all DMAAP Topic Sources.
     */
    void destroy();

    /**
     * Gets an DMAAP Topic Source based on topic name.
     * 
     * @param topic the topic name
     * @return an DMAAP Topic Source with topic name
     * @throws IllegalArgumentException if an invalid topic is provided
     * @throws IllegalStateException if the DMAAP Topic Source is an incorrect state
     */
    DmaapTopicSource get(String topic);

    /**
     * Provides a snapshot of the DMAAP Topic Sources.
     * 
     * @return a list of the DMAAP Topic Sources
     */
    List<DmaapTopicSource> inventory();
}


/* ------------- implementation ----------------- */

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

            DmaapTopicSource dmaapTopicSource =
                    new SingleThreadedDmaapTopicSource(busTopicParams);

            if (busTopicParams.isManaged()) {
                dmaapTopicSources.put(busTopicParams.getTopic(), dmaapTopicSource);
            }
            return dmaapTopicSource;
        }
    }

    @Override
    public List<DmaapTopicSource> build(Properties properties) {

        String readTopics = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS);
        if (readTopics == null || readTopics.isEmpty()) {
            logger.info("{}: no topic for DMaaP Source", this);
            return new ArrayList<>();
        }
        List<String> readTopicList = new ArrayList<>(Arrays.asList(readTopics.split("\\s*,\\s*")));

        List<DmaapTopicSource> dmaapTopicSourceLst = new ArrayList<>();
        synchronized (this) {
            for (String topic : readTopicList) {
                if (this.dmaapTopicSources.containsKey(topic)) {
                    dmaapTopicSourceLst.add(this.dmaapTopicSources.get(topic));
                    continue;
                }

                String servers = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);

                List<String> serverList;
                if (servers != null && !servers.isEmpty()) {
                    serverList = new ArrayList<>(Arrays.asList(servers.split("\\s*,\\s*")));
                } else {
                    serverList = new ArrayList<>();
                }

                final String apiKey = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX);

                final String apiSecret = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX);

                final String aafMechId = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_AAF_MECHID_SUFFIX);

                final String aafPassword = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_AAF_PASSWORD_SUFFIX);

                final String consumerGroup = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX);

                final String consumerInstance = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX);

                final String fetchTimeoutString = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX);

                /* DME2 Properties */

                final String dme2Environment = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX);

                final String dme2AftEnvironment = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX);

                final String dme2Partner = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_PARTNER_SUFFIX);

                final String dme2RouteOffer = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX);

                final String dme2Latitude = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX);

                final String dme2Longitude = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX);

                final String dme2EpReadTimeoutMs =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + topic
                                + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX);

                final String dme2EpConnTimeout = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX);

                final String dme2RoundtripTimeoutMs =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + topic
                                + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX);

                final String dme2Version = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_VERSION_SUFFIX);

                final String dme2SubContextPath = properties.getProperty(
                                PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX);

                final String dme2SessionStickinessRequired =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + topic
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

                int fetchTimeout = DmaapTopicSource.DEFAULT_TIMEOUT_MS_FETCH;
                if (fetchTimeoutString != null && !fetchTimeoutString.isEmpty()) {
                    try {
                        fetchTimeout = Integer.parseInt(fetchTimeoutString);
                    } catch (NumberFormatException nfe) {
                        logger.warn("{}: fetch timeout {} is in invalid format for topic {} ", this, fetchTimeoutString,
                                topic);
                    }
                }

                String fetchLimitString = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX);
                int fetchLimit = DmaapTopicSource.DEFAULT_LIMIT_FETCH;
                if (fetchLimitString != null && !fetchLimitString.isEmpty()) {
                    try {
                        fetchLimit = Integer.parseInt(fetchLimitString);
                    } catch (NumberFormatException nfe) {
                        logger.warn("{}: fetch limit {} is in invalid format for topic {} ", this, fetchLimitString,
                                topic);
                    }
                }

                String managedString = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX);
                boolean managed = true;
                if (managedString != null && !managedString.isEmpty()) {
                    managed = Boolean.parseBoolean(managedString);
                }

                String useHttpsString = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX);

                // default is to use HTTP if no https property exists
                boolean useHttps = false;
                if (useHttpsString != null && !useHttpsString.isEmpty()) {
                    useHttps = Boolean.parseBoolean(useHttpsString);
                }

                String allowSelfSignedCertsString =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + topic
                                + PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX);

                // default is to disallow self-signed certs
                boolean allowSelfSignedCerts = false;
                if (allowSelfSignedCertsString != null && !allowSelfSignedCertsString.isEmpty()) {
                    allowSelfSignedCerts = Boolean.parseBoolean(allowSelfSignedCertsString);
                }


                DmaapTopicSource uebTopicSource = this.build(BusTopicParams.builder()
                        .servers(serverList)
                        .topic(topic)
                        .apiKey(apiKey)
                        .apiSecret(apiSecret)
                        .userName(aafMechId)
                        .password(aafPassword)
                        .consumerGroup(consumerGroup)
                        .consumerInstance(consumerInstance)
                        .fetchTimeout(fetchTimeout)
                        .fetchLimit(fetchLimit)
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

                dmaapTopicSourceLst.add(uebTopicSource);
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
                .fetchTimeout(DmaapTopicSource.DEFAULT_TIMEOUT_MS_FETCH)
                .fetchLimit(DmaapTopicSource.DEFAULT_LIMIT_FETCH)
                .managed(true)
                .useHttps(false)
                .allowSelfSignedCerts(false)
                .build());
    }

    @Override
    public DmaapTopicSource build(List<String> servers, String topic) {
        return this.build(servers, topic, null, null);
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
                throw new IllegalArgumentException("DmaapTopiceSource for " + topic + " not found");
            }
        }
    }

    @Override
    public synchronized List<DmaapTopicSource> inventory() {
        return new ArrayList<>(this.dmaapTopicSources.values());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IndexedDmaapTopicSourceFactory []");
        return builder.toString();
    }

}

