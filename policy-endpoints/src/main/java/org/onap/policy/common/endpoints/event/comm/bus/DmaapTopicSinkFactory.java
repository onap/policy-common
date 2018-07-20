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

package org.onap.policy.common.endpoints.event.comm.bus;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * DMAAP Topic Sink Factory
 */
public interface DmaapTopicSinkFactory {
    public final String DME2_READ_TIMEOUT_PROPERTY = "AFT_DME2_EP_READ_TIMEOUT_MS";
    public final String DME2_EP_CONN_TIMEOUT_PROPERTY = "AFT_DME2_EP_CONN_TIMEOUT";
    public final String DME2_ROUNDTRIP_TIMEOUT_PROPERTY = "AFT_DME2_ROUNDTRIP_TIMEOUT_MS";
    public final String DME2_VERSION_PROPERTY = "Version";
    public final String DME2_ROUTE_OFFER_PROPERTY = "routeOffer";
    public final String DME2_SERVICE_NAME_PROPERTY = "ServiceName";
    public final String DME2_SUBCONTEXT_PATH_PROPERTY = "SubContextPath";
    public final String DME2_SESSION_STICKINESS_REQUIRED_PROPERTY = "sessionstickinessrequired";

    /**
     * Instantiates a new DMAAP Topic Sink
     * 
     * @param servers list of servers
     * @param topic topic name
     * @param apiKey API Key
     * @param apiSecret API Secret
     * @param userName AAF user name
     * @param password AAF password
     * @param partitionKey Consumer Group
     * @param environment DME2 environment
     * @param aftEnvironment DME2 AFT environment
     * @param partner DME2 Partner
     * @param latitude DME2 latitude
     * @param longitude DME2 longitude
     * @param additionalProps additional properties to pass to DME2
     * @param managed is this sink endpoint managed?
     * 
     * @return an DMAAP Topic Sink
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public DmaapTopicSink build(List<String> servers, String topic, String apiKey, String apiSecret, String userName,
            String password, String partitionKey, String environment, String aftEnvironment, String partner,
            String latitude, String longitude, Map<String, String> additionalProps, boolean managed, boolean useHttps,
            boolean allowSelfSignedCerts);

    /**
     * Instantiates a new DMAAP Topic Sink
     * 
     * @param servers list of servers
     * @param topic topic name
     * @param apiKey API Key
     * @param apiSecret API Secret
     * @param userName AAF user name
     * @param password AAF password
     * @param partitionKey Consumer Group
     * @param managed is this sink endpoint managed?
     * 
     * @return an DMAAP Topic Sink
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public DmaapTopicSink build(List<String> servers, String topic, String apiKey, String apiSecret, String userName,
            String password, String partitionKey, boolean managed, boolean useHttps, boolean allowSelfSignedCerts);

    /**
     * Creates an DMAAP Topic Sink based on properties files
     * 
     * @param properties Properties containing initialization values
     * 
     * @return an DMAAP Topic Sink
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public List<DmaapTopicSink> build(Properties properties);

    /**
     * Instantiates a new DMAAP Topic Sink
     * 
     * @param servers list of servers
     * @param topic topic name
     * 
     * @return an DMAAP Topic Sink
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public DmaapTopicSink build(List<String> servers, String topic);

    /**
     * Destroys an DMAAP Topic Sink based on a topic
     * 
     * @param topic topic name
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public void destroy(String topic);

    /**
     * gets an DMAAP Topic Sink based on topic name
     * 
     * @param topic the topic name
     * 
     * @return an DMAAP Topic Sink with topic name
     * @throws IllegalArgumentException if an invalid topic is provided
     * @throws IllegalStateException if the DMAAP Topic Reader is an incorrect state
     */
    public DmaapTopicSink get(String topic);

    /**
     * Provides a snapshot of the DMAAP Topic Sinks
     * 
     * @return a list of the DMAAP Topic Sinks
     */
    public List<DmaapTopicSink> inventory();

    /**
     * Destroys all DMAAP Topic Sinks
     */
    public void destroy();
}
