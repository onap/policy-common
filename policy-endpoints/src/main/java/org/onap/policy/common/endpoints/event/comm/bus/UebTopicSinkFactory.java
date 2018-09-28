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
import org.onap.policy.common.endpoints.event.comm.bus.internal.InlineUebTopicSink;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UEB Topic Sink Factory.
 */
public interface UebTopicSinkFactory {

    /**
     * Instantiates a new UEB Topic Writer.
     * 
     * @param busTopicParams parameters object
     * @return an UEB Topic Sink
     */
    UebTopicSink build(BusTopicParams busTopicParams);

    /**
     * Creates an UEB Topic Writer based on properties files.
     * 
     * @param properties Properties containing initialization values
     * 
     * @return an UEB Topic Writer
     * @throws IllegalArgumentException if invalid parameters are present
     */
    List<UebTopicSink> build(Properties properties);

    /**
     * Instantiates a new UEB Topic Writer.
     * 
     * @param servers list of servers
     * @param topic topic name
     * 
     * @return an UEB Topic Writer
     * @throws IllegalArgumentException if invalid parameters are present
     */
    UebTopicSink build(List<String> servers, String topic);

    /**
     * Destroys an UEB Topic Writer based on a topic.
     * 
     * @param topic topic name
     * @throws IllegalArgumentException if invalid parameters are present
     */
    void destroy(String topic);

    /**
     * Destroys all UEB Topic Writers.
     */
    void destroy();

    /**
     * gets an UEB Topic Writer based on topic name.
     * 
     * @param topic the topic name
     * 
     * @return an UEB Topic Writer with topic name
     * @throws IllegalArgumentException if an invalid topic is provided
     * @throws IllegalStateException if the UEB Topic Reader is an incorrect state
     */
    UebTopicSink get(String topic);

    /**
     * Provides a snapshot of the UEB Topic Writers.
     * 
     * @return a list of the UEB Topic Writers
     */
    List<UebTopicSink> inventory();
}


/* ------------- implementation ----------------- */

/**
 * Factory of UEB Reader Topics indexed by topic name.
 */
class IndexedUebTopicSinkFactory implements UebTopicSinkFactory {
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

        if (busTopicParams.getTopic() == null || busTopicParams.getTopic().isEmpty()) {
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
        if (writeTopics == null || writeTopics.isEmpty()) {
            logger.info("{}: no topic for UEB Sink", this);
            return new ArrayList<>();
        }

        List<String> writeTopicList = new ArrayList<>(Arrays.asList(writeTopics.split("\\s*,\\s*")));
        List<UebTopicSink> newUebTopicSinks = new ArrayList<>();
        synchronized (this) {
            for (String topic : writeTopicList) {
                if (this.uebTopicSinks.containsKey(topic)) {
                    newUebTopicSinks.add(this.uebTopicSinks.get(topic));
                    continue;
                }

                String servers = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS + "." + topic
                        + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);
                if (servers == null || servers.isEmpty()) {
                    logger.error("{}: no UEB servers configured for sink {}", this, topic);
                    continue;
                }

                final List<String> serverList = new ArrayList<>(Arrays.asList(servers.split("\\s*,\\s*")));

                final String apiKey = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS 
                                + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX);
                final String apiSecret = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS 
                                + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX);
                final String partitionKey = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS 
                                + "." + topic + PolicyEndPointProperties.PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX);

                String managedString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX);
                boolean managed = true;
                if (managedString != null && !managedString.isEmpty()) {
                    managed = Boolean.parseBoolean(managedString);
                }

                String useHttpsString = properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS + "."
                        + topic + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX);

                // default is to use HTTP if no https property exists
                boolean useHttps = false;
                if (useHttpsString != null && !useHttpsString.isEmpty()) {
                    useHttps = Boolean.parseBoolean(useHttpsString);
                }


                String allowSelfSignedCertsString =
                        properties.getProperty(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS + "." + topic
                                + PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX);

                // default is to disallow self-signed certs
                boolean allowSelfSignedCerts = false;
                if (allowSelfSignedCertsString != null && !allowSelfSignedCertsString.isEmpty()) {
                    allowSelfSignedCerts = Boolean.parseBoolean(allowSelfSignedCertsString);
                }

                UebTopicSink uebTopicWriter = this.build(BusTopicParams.builder()
                        .servers(serverList)
                        .topic(topic)
                        .apiKey(apiKey)
                        .apiSecret(apiSecret)
                        .partitionId(partitionKey)
                        .managed(managed)
                        .useHttps(useHttps)
                        .allowSelfSignedCerts(allowSelfSignedCerts)
                        .build());
                newUebTopicSinks.add(uebTopicWriter);
            }
            return newUebTopicSinks;
        }
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
        StringBuilder builder = new StringBuilder();
        builder.append("IndexedUebTopicSinkFactory []");
        return builder.toString();
    }

}
