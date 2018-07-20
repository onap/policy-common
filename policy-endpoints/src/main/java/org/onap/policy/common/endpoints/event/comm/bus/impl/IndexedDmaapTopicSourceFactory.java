/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm.bus.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSource;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSourceFactory;
import org.onap.policy.common.endpoints.event.comm.bus.internal.SingleThreadedDmaapTopicSource;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of DMAAP Source Topics indexed by topic name
 */

public class IndexedDmaapTopicSourceFactory implements DmaapTopicSourceFactory {
    private static final String MISSING_TOPIC = "A topic must be provided";

    private static final IndexedDmaapTopicSourceFactory instance = new IndexedDmaapTopicSourceFactory();

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedDmaapTopicSourceFactory.class);

    /**
     * DMaaP Topic Name Index
     */
    protected HashMap<String, DmaapTopicSource> dmaapTopicSources = new HashMap<>();

    /**
     * Get the singleton instance.
     * 
     * @return the instance
     */
    public static IndexedDmaapTopicSourceFactory getInstance() {
        return instance;
    }

    private IndexedDmaapTopicSourceFactory() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public DmaapTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret, String userName,
            String password, String consumerGroup, String consumerInstance, int fetchTimeout, int fetchLimit,
            String environment, String aftEnvironment, String partner, String latitude, String longitude,
            Map<String, String> additionalProps, boolean managed, boolean useHttps, boolean allowSelfSignedCerts) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (dmaapTopicSources.containsKey(topic)) {
                return dmaapTopicSources.get(topic);
            }

            DmaapTopicSource dmaapTopicSource = new SingleThreadedDmaapTopicSource(servers, topic, apiKey, apiSecret,
                    userName, password, consumerGroup, consumerInstance, fetchTimeout, fetchLimit, environment,
                    aftEnvironment, partner, latitude, longitude, additionalProps, useHttps, allowSelfSignedCerts);

            if (managed) {
                dmaapTopicSources.put(topic, dmaapTopicSource);
            }

            return dmaapTopicSource;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DmaapTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret, String userName,
            String password, String consumerGroup, String consumerInstance, int fetchTimeout, int fetchLimit,
            boolean managed, boolean useHttps, boolean allowSelfSignedCerts) {

        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("DMaaP Server(s) must be provided");
        }

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (dmaapTopicSources.containsKey(topic)) {
                return dmaapTopicSources.get(topic);
            }

            DmaapTopicSource dmaapTopicSource =
                    new SingleThreadedDmaapTopicSource(servers, topic, apiKey, apiSecret, userName, password,
                            consumerGroup, consumerInstance, fetchTimeout, fetchLimit, useHttps, allowSelfSignedCerts);

            if (managed) {
                dmaapTopicSources.put(topic, dmaapTopicSource);
            }

            return dmaapTopicSource;
        }
    }

    /**
     * {@inheritDoc}
     */
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

                String apiKey = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX);

                String apiSecret = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX);

                String aafMechId = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_AAF_MECHID_SUFFIX);

                String aafPassword = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_AAF_PASSWORD_SUFFIX);

                String consumerGroup = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX);

                String consumerInstance = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX);

                String fetchTimeoutString = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX);

                /* DME2 Properties */

                String dme2Environment = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX);

                String dme2AftEnvironment = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX);

                String dme2Partner = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_PARTNER_SUFFIX);

                String dme2RouteOffer = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX);

                String dme2Latitude = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX);

                String dme2Longitude = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX);

                String dme2EpReadTimeoutMs =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + topic
                                + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX);

                String dme2EpConnTimeout = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX);

                String dme2RoundtripTimeoutMs =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + topic
                                + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX);

                String dme2Version = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_VERSION_SUFFIX);

                String dme2SubContextPath = properties.getProperty(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX);

                String dme2SessionStickinessRequired =
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


                DmaapTopicSource uebTopicSource = this.build(serverList, topic, apiKey, apiSecret, aafMechId,
                        aafPassword, consumerGroup, consumerInstance, fetchTimeout, fetchLimit, dme2Environment,
                        dme2AftEnvironment, dme2Partner, dme2Latitude, dme2Longitude, dme2AdditionalProps, managed,
                        useHttps, allowSelfSignedCerts);

                dmaapTopicSourceLst.add(uebTopicSource);
            }
        }
        return dmaapTopicSourceLst;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     */
    @Override
    public DmaapTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret) {
        return this.build(servers, topic, apiKey, apiSecret, null, null, null, null,
                DmaapTopicSource.DEFAULT_TIMEOUT_MS_FETCH, DmaapTopicSource.DEFAULT_LIMIT_FETCH, true, false, false);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     */
    @Override
    public DmaapTopicSource build(List<String> servers, String topic) {
        return this.build(servers, topic, null, null);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IndexedDmaapTopicSourceFactory []");
        return builder.toString();
    }

}
