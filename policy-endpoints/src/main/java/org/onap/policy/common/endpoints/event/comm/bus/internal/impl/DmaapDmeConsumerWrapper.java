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

package org.onap.policy.common.endpoints.event.comm.bus.internal.impl;

import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.test.clients.ProtocolTypeConstants;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSinkFactory;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmaapDmeConsumerWrapper extends DmaapConsumerWrapper {

    private static Logger logger = LoggerFactory.getLogger(DmaapDmeConsumerWrapper.class);

    private final Properties props;

    public DmaapDmeConsumerWrapper(List<String> servers, String topic, String apiKey, String apiSecret,
            String dme2Login, String dme2Password, String consumerGroup, String consumerInstance, int fetchTimeout,
            int fetchLimit, String environment, String aftEnvironment, String dme2Partner, String latitude,
            String longitude, Map<String, String> additionalProps, boolean useHttps) throws MalformedURLException {



        super(servers, topic, apiKey, apiSecret, dme2Login, dme2Password, consumerGroup, consumerInstance, fetchTimeout,
                fetchLimit);


        final String dme2RouteOffer = additionalProps.get(DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY);

        if (environment == null || environment.isEmpty()) {
            throw parmException(topic, PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX);
        }
        if (aftEnvironment == null || aftEnvironment.isEmpty()) {
            throw parmException(topic, PolicyEndPointProperties.PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX);
        }
        if (latitude == null || latitude.isEmpty()) {
            throw parmException(topic, PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX);
        }
        if (longitude == null || longitude.isEmpty()) {
            throw parmException(topic, PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX);
        }

        if ((dme2Partner == null || dme2Partner.isEmpty()) && (dme2RouteOffer == null || dme2RouteOffer.isEmpty())) {
            throw new IllegalArgumentException(
                    "Must provide at least " + PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + topic
                            + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_PARTNER_SUFFIX + " or "
                            + PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + topic
                            + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX + " for DME2");
        }

        final String serviceName = servers.get(0);

        this.consumer.setProtocolFlag(ProtocolTypeConstants.DME2.getValue());

        this.consumer.setUsername(dme2Login);
        this.consumer.setPassword(dme2Password);

        props = new Properties();

        props.setProperty(DmaapTopicSinkFactory.DME2_SERVICE_NAME_PROPERTY, serviceName);

        props.setProperty("username", dme2Login);
        props.setProperty("password", dme2Password);

        /* These are required, no defaults */
        props.setProperty("topic", topic);

        props.setProperty("Environment", environment);
        props.setProperty("AFT_ENVIRONMENT", aftEnvironment);

        if (dme2Partner != null) {
            props.setProperty("Partner", dme2Partner);
        }
        if (dme2RouteOffer != null) {
            props.setProperty(DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY, dme2RouteOffer);
        }

        props.setProperty("Latitude", latitude);
        props.setProperty("Longitude", longitude);

        /* These are optional, will default to these values if not set in additionalProps */
        props.setProperty("AFT_DME2_EP_READ_TIMEOUT_MS", "50000");
        props.setProperty("AFT_DME2_ROUNDTRIP_TIMEOUT_MS", "240000");
        props.setProperty("AFT_DME2_EP_CONN_TIMEOUT", "15000");
        props.setProperty("Version", "1.0");
        props.setProperty("SubContextPath", "/");
        props.setProperty("sessionstickinessrequired", "no");

        /* These should not change */
        props.setProperty("TransportType", "DME2");
        props.setProperty("MethodType", "GET");

        if (useHttps) {
            props.setProperty(PROTOCOL_PROP, "https");

        } else {
            props.setProperty(PROTOCOL_PROP, "http");
        }

        props.setProperty("contenttype", "application/json");

        if (additionalProps != null) {
            for (Map.Entry<String, String> entry : additionalProps.entrySet()) {
                props.put(entry.getKey(), entry.getValue());
            }
        }

        MRClientFactory.prop = props;
        this.consumer.setProps(props);

        logger.info("{}: CREATION", this);
    }

    private IllegalArgumentException parmException(String topic, String propnm) {
        return new IllegalArgumentException("Missing " + PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                + topic + propnm + " property for DME2 in DMaaP");

    }
}
