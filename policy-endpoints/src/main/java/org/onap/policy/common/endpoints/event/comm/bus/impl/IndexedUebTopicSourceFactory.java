/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.endpoints.event.comm.bus.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.onap.policy.common.endpoints.event.comm.bus.UebTopicSource;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicSourceFactory;
import org.onap.policy.common.endpoints.event.comm.bus.internal.SingleThreadedUebTopicSource;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of UEB Source Topics indexed by topic name
 */
public class IndexedUebTopicSourceFactory implements UebTopicSourceFactory {
    private static final String MISSING_TOPIC = "A topic must be provided";

    private static final IndexedUebTopicSourceFactory instance = new IndexedUebTopicSourceFactory();

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedUebTopicSourceFactory.class);

    /**
     * UEB Topic Name Index
     */
    protected HashMap<String, UebTopicSource> uebTopicSources = new HashMap<>();

    /**
     * Get the singleton instance.
     * 
     * @return the instance
     */
    public static IndexedUebTopicSourceFactory getInstance() {
        return instance;
    }

    private IndexedUebTopicSourceFactory() {}

    /**
     * {@inheritDoc}
     */
    @Override
    public UebTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret,
            String consumerGroup, String consumerInstance, int fetchTimeout, int fetchLimit, boolean managed,
            boolean useHttps, boolean allowSelfSignedCerts) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("UEB Server(s) must be provided");
        }

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException(MISSING_TOPIC);
        }

        synchronized (this) {
            if (uebTopicSources.containsKey(topic)) {
                return uebTopicSources.get(topic);
            }

            UebTopicSource uebTopicSource = new SingleThreadedUebTopicSource(servers, topic, apiKey, apiSecret,
                    consumerGroup, consumerInstance, fetchTimeout, fetchLimit, useHttps, allowSelfSignedCerts);

            if (managed) {
                uebTopicSources.put(topic, uebTopicSource);
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

                String servers = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "." + topic
                        + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);

                if (servers == null || servers.isEmpty()) {
                    logger.error("{}: no UEB servers configured for sink {}", this, topic);
                    continue;
                }

                List<String> serverList = new ArrayList<>(Arrays.asList(servers.split("\\s*,\\s*")));

                String apiKey = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "." + topic
                        + PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX);

                String apiSecret = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "." + topic
                        + PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX);

                String consumerGroup = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "." + topic
                        + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX);

                String consumerInstance = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX);

                String fetchTimeoutString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX);
                int fetchTimeout = UebTopicSource.DEFAULT_TIMEOUT_MS_FETCH;
                if (fetchTimeoutString != null && !fetchTimeoutString.isEmpty()) {
                    try {
                        fetchTimeout = Integer.parseInt(fetchTimeoutString);
                    } catch (NumberFormatException nfe) {
                        logger.warn("{}: fetch timeout {} is in invalid format for topic {} ", this, fetchTimeoutString,
                                topic);
                    }
                }

                String fetchLimitString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX);
                int fetchLimit = UebTopicSource.DEFAULT_LIMIT_FETCH;
                if (fetchLimitString != null && !fetchLimitString.isEmpty()) {
                    try {
                        fetchLimit = Integer.parseInt(fetchLimitString);
                    } catch (NumberFormatException nfe) {
                        logger.warn("{}: fetch limit {} is in invalid format for topic {} ", this, fetchLimitString,
                                topic);
                    }
                }

                String managedString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "." + topic
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX);
                boolean managed = true;
                if (managedString != null && !managedString.isEmpty()) {
                    managed = Boolean.parseBoolean(managedString);
                }

                String useHttpsString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS + "." + topic
                        + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX);

                // default is to use HTTP if no https property exists
                boolean useHttps = false;
                if (useHttpsString != null && !useHttpsString.isEmpty()) {
                    useHttps = Boolean.parseBoolean(useHttpsString);
                }

                String allowSelfSignedCertsString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS
                        + "." + topic + PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX);

                // default is to disallow self-signed certs
                boolean allowSelfSignedCerts = false;
                if (allowSelfSignedCertsString != null && !allowSelfSignedCertsString.isEmpty()) {
                    allowSelfSignedCerts = Boolean.parseBoolean(allowSelfSignedCertsString);
                }

                UebTopicSource uebTopicSource = this.build(serverList, topic, apiKey, apiSecret, consumerGroup,
                        consumerInstance, fetchTimeout, fetchLimit, managed, useHttps, allowSelfSignedCerts);
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

        return this.build(servers, topic, apiKey, apiSecret, null, null, UebTopicSource.DEFAULT_TIMEOUT_MS_FETCH,
                UebTopicSource.DEFAULT_LIMIT_FETCH, true, false, true);
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IndexedUebTopicSourceFactory []");
        return builder.toString();
    }

}
