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

package org.onap.policy.common.endpoints.event.comm.bus.internal.impl;

import com.att.nsa.mr.test.clients.ProtocolTypeConstants;

import java.util.List;
import java.util.Map;

import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSinkFactory;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;

public class DmaapDmePublisherWrapper extends DmaapPublisherWrapper {
    public DmaapDmePublisherWrapper(List<String> servers, String topic, String username, String password,
            String environment, String aftEnvironment, String dme2Partner, String latitude, String longitude,
            Map<String, String> additionalProps, boolean useHttps) {

        super(ProtocolTypeConstants.DME2, servers, topic, username, password, useHttps);



        String dme2RouteOffer = additionalProps.get(DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY);

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
                            + PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "." + topic
                            + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX + " for DME2");
        }

        String serviceName = servers.get(0);

        /* These are required, no defaults */
        props.setProperty("Environment", environment);
        props.setProperty("AFT_ENVIRONMENT", aftEnvironment);

        props.setProperty(DmaapTopicSinkFactory.DME2_SERVICE_NAME_PROPERTY, serviceName);

        if (dme2Partner != null) {
            props.setProperty("Partner", dme2Partner);
        }
        if (dme2RouteOffer != null) {
            props.setProperty(DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY, dme2RouteOffer);
        }

        props.setProperty("Latitude", latitude);
        props.setProperty("Longitude", longitude);

        // ServiceName also a default, found in additionalProps

        /* These are optional, will default to these values if not set in optionalProps */
        props.setProperty("AFT_DME2_EP_READ_TIMEOUT_MS", "50000");
        props.setProperty("AFT_DME2_ROUNDTRIP_TIMEOUT_MS", "240000");
        props.setProperty("AFT_DME2_EP_CONN_TIMEOUT", "15000");
        props.setProperty("Version", "1.0");
        props.setProperty("SubContextPath", "/");
        props.setProperty("sessionstickinessrequired", "no");

        /* These should not change */
        props.setProperty("TransportType", "DME2");
        props.setProperty("MethodType", "POST");

        for (Map.Entry<String, String> entry : additionalProps.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value != null) {
                props.setProperty(key, value);
            }
        }

        this.publisher.setProps(props);
    }

    private IllegalArgumentException parmException(String topic, String propnm) {
        return new IllegalArgumentException("Missing " + PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "."
                + topic + propnm + " property for DME2 in DMaaP");

    }
}
