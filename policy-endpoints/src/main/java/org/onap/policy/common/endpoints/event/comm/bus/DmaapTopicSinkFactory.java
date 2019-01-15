/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;
import java.util.Properties;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;

/**
 * DMAAP Topic Sink Factory.
 */
public interface DmaapTopicSinkFactory {

    String DME2_READ_TIMEOUT_PROPERTY = "AFT_DME2_EP_READ_TIMEOUT_MS";
    String DME2_EP_CONN_TIMEOUT_PROPERTY = "AFT_DME2_EP_CONN_TIMEOUT";
    String DME2_ROUNDTRIP_TIMEOUT_PROPERTY = "AFT_DME2_ROUNDTRIP_TIMEOUT_MS";
    String DME2_VERSION_PROPERTY = "Version";
    String DME2_ROUTE_OFFER_PROPERTY = "routeOffer";
    String DME2_SERVICE_NAME_PROPERTY = "ServiceName";
    String DME2_SUBCONTEXT_PATH_PROPERTY = "SubContextPath";
    String DME2_SESSION_STICKINESS_REQUIRED_PROPERTY = "sessionstickinessrequired";

    /**
     * <pre>
     * Instantiate a new DMAAP Topic Sink, with following params.
     * servers         list of servers
     * topic           topic name
     * apiKey          API Key
     * apiSecret       API Secret
     * userName        AAF user name
     * password        AAF password
     * partitionKey    Consumer Group
     * environment     DME2 environment
     * aftEnvironment  DME2 AFT environment
     * partner         DME2 Partner
     * latitude        DME2 latitude
     * longitude       DME2 longitude
     * additionalProps additional properties to pass to DME2
     * managed         is this sink endpoint managed?
     * </pre>
     * @param busTopicParams parameter object
     * @return DmaapTopicSink object
     * @throws IllegalArgumentException if invalid parameters are present
     */
    DmaapTopicSink build(BusTopicParams busTopicParams);

    /**
     * Creates an DMAAP Topic Sink based on properties files.
     *
     * @param properties Properties containing initialization values
     * @return an DMAAP Topic Sink
     * @throws IllegalArgumentException if invalid parameters are present
     */
    List<DmaapTopicSink> build(Properties properties);

    /**
     * Instantiates a new DMAAP Topic Sink.
     *
     * @param servers list of servers
     * @param topic topic name
     * @return an DMAAP Topic Sink
     * @throws IllegalArgumentException if invalid parameters are present
     */
    DmaapTopicSink build(List<String> servers, String topic);

    /**
     * Destroys an DMAAP Topic Sink based on a topic.
     *
     * @param topic topic name
     * @throws IllegalArgumentException if invalid parameters are present
     */
    void destroy(String topic);

    /**
     * Destroys all DMAAP Topic Sinks.
     */
    void destroy();

    /**
     * Gets an DMAAP Topic Sink based on topic name.
     *
     * @param topic the topic name
     * @return an DMAAP Topic Sink with topic name
     * @throws IllegalArgumentException if an invalid topic is provided
     * @throws IllegalStateException if the DMAAP Topic Reader is an incorrect state
     */
    DmaapTopicSink get(String topic);

    /**
     * Provides a snapshot of the DMAAP Topic Sinks.
     *
     * @return a list of the DMAAP Topic Sinks
     */
    List<DmaapTopicSink> inventory();
}
