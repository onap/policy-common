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
import java.util.Properties;

import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.SingleThreadedUebTopicSource;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UEB Topic Source Factory.
 */
public interface UebTopicSourceFactory {

    /**
     * Creates an UEB Topic Source based on properties files.
     * 
     * @param properties Properties containing initialization values
     * 
     * @return an UEB Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    List<UebTopicSource> build(Properties properties);

    /**
     * Instantiates a new UEB Topic Source.
     * 
     * @param busTopicParams parameters object
     * @return an UEB Topic Source
     */
    UebTopicSource build(BusTopicParams busTopicParams);

    /**
     * Instantiates a new UEB Topic Source.
     * 
     * @param servers list of servers
     * @param topic topic name
     * @param apiKey API Key
     * @param apiSecret API Secret
     * 
     * @return an UEB Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    UebTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret);

    /**
     * Instantiates a new UEB Topic Source.
     * 
     * @param servers list of servers
     * @param topic topic name
     * 
     * @return an UEB Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    UebTopicSource build(List<String> servers, String topic);

    /**
     * Destroys an UEB Topic Source based on a topic.
     * 
     * @param topic topic name
     * @throws IllegalArgumentException if invalid parameters are present
     */
    void destroy(String topic);

    /**
     * Destroys all UEB Topic Sources.
     */
    void destroy();

    /**
     * Gets an UEB Topic Source based on topic name.
     * 
     * @param topic the topic name
     * @return an UEB Topic Source with topic name
     * @throws IllegalArgumentException if an invalid topic is provided
     * @throws IllegalStateException if the UEB Topic Source is an incorrect state
     */
    UebTopicSource get(String topic);

    /**
     * Provides a snapshot of the UEB Topic Sources.
     * 
     * @return a list of the UEB Topic Sources
     */
    List<UebTopicSource> inventory();
}


/* ------------- implementation ----------------- */

/**
 * Factory of UEB Source Topics indexed by topic name.
 */
class IndexedUebTopicSourceFactory implements UebTopicSourceFactory {
    private static final String MISSING_TOPIC = "A topic must be provided";

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedUebTopicSourceFactory.class);

    /**
     * UEB Topic Name Index.
     */
    protected HashMap<String, UebTopicSource> uebTopicSources = new HashMap<>();

    /**
     * {@inheritDoc}
     */
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

            UebTopicSource uebTopicSource = new SingleThreadedUebTopicSource(busTopicParams);

            if (busTopicParams.isManaged()) {
                uebTopicSources.put(busTopicParams.getTopic(), uebTopicSource);
            }

            return uebTopicSource;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UebTopicSource> build(Properties properties) {

        String readTopics = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS);
        if (readTopics == null || readTopics.isEmpty()) {
            logger.info("{}: no topic for UEB Source", this);
            return new ArrayList<>();
        }
        List<String> readTopicList = new ArrayList<>(Arrays.asList(readTopics.split("\\s*,\\s*")));

        List<UebTopicSource> newUebTopicSources = new ArrayList<>();
        synchronized (this) {
            for (String topic : readTopicList) {
                if (this.uebTopicSources.containsKey(topic)) {
                    newUebTopicSources.add(this.uebTopicSources.get(topic));
                    continue;
                }

                String servers = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);

                if (servers == null || servers.isEmpty()) {
                    logger.error("{}: no UEB servers configured for sink {}", this, topic);
                    continue;
                }

                List<String> serverList = new ArrayList<>(Arrays.asList(servers.split("\\s*,\\s*")));

                String apiKey = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "." + topic
                        + PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX);

                String apiSecret = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX);

                String consumerGroup = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX);

                String consumerInstance = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX);

                String fetchTimeoutString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX);
                int fetchTimeout = UebTopicSource.DEFAULT_TIMEOUT_MS_FETCH;
                if (fetchTimeoutString != null && !fetchTimeoutString.isEmpty()) {
                    try {
                        fetchTimeout = Integer.parseInt(fetchTimeoutString);
                    } catch (NumberFormatException nfe) {
                        logger.warn("{}: fetch timeout {} is in invalid format for topic {} ", this, fetchTimeoutString,
                                topic);
                    }
                }

                String fetchLimitString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX);
                int fetchLimit = UebTopicSource.DEFAULT_LIMIT_FETCH;
                if (fetchLimitString != null && !fetchLimitString.isEmpty()) {
                    try {
                        fetchLimit = Integer.parseInt(fetchLimitString);
                    } catch (NumberFormatException nfe) {
                        logger.warn("{}: fetch limit {} is in invalid format for topic {} ", this, fetchLimitString,
                                topic);
                    }
                }

                String managedString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX);
                boolean managed = true;
                if (managedString != null && !managedString.isEmpty()) {
                    managed = Boolean.parseBoolean(managedString);
                }

                String useHttpsString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX);

                // default is to use HTTP if no https property exists
                boolean useHttps = false;
                if (useHttpsString != null && !useHttpsString.isEmpty()) {
                    useHttps = Boolean.parseBoolean(useHttpsString);
                }

                String allowSelfSignedCertsString =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "." + topic
                                + PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX);

                // default is to disallow self-signed certs
                boolean allowSelfSignedCerts = false;
                if (allowSelfSignedCertsString != null && !allowSelfSignedCertsString.isEmpty()) {
                    allowSelfSignedCerts = Boolean.parseBoolean(allowSelfSignedCertsString);
                }

                UebTopicSource uebTopicSource = this.build(BusTopicParams.builder()
                        .servers(serverList)
                        .topic(topic)
                        .apiKey(apiKey)
                        .apiSecret(apiSecret)
                        .consumerGroup(consumerGroup)
                        .consumerInstance(consumerInstance)
                        .fetchTimeout(fetchTimeout)
                        .fetchLimit(fetchLimit)
                        .managed(managed)
                        .useHttps(useHttps)
                        .allowSelfSignedCerts(allowSelfSignedCerts).build());
                newUebTopicSources.add(uebTopicSource);
            }
        }
        return newUebTopicSources;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UebTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret) {

        return this.build(BusTopicParams.builder()
                .servers(servers)
                .topic(topic)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .fetchTimeout(UebTopicSource.DEFAULT_TIMEOUT_MS_FETCH)
                .fetchLimit(UebTopicSource.DEFAULT_LIMIT_FETCH)
                .managed(true)
                .useHttps(false)
                .allowSelfSignedCerts(true).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UebTopicSource build(List<String> servers, String topic) {
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

    /**
     * {@inheritDoc}
     */
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
        StringBuilder builder = new StringBuilder();
        builder.append("IndexedUebTopicSourceFactory []");
        return builder.toString();
    }
}
