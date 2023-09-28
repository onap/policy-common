/*-
 * ============LICENSE_START=======================================================
 * ONAP POLICY
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 *
 */

package org.onap.policy.common.endpoints.event.comm.bus.internal;

import java.util.Properties;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;

public class BusHelper {

    private BusHelper() {
        /* no constructor */
    }

    /**
     * Complete the properties param with common fields for both BusConsumer and BusPublisher.
     * @param busTopicParams topics
     * @param dme2RouteOffer route
     * @param props properties
     */
    public static void setCommonProperties(BusTopicParams busTopicParams, String dme2RouteOffer, Properties props) {
        props.setProperty("Environment", busTopicParams.getEnvironment());
        props.setProperty("AFT_ENVIRONMENT", busTopicParams.getAftEnvironment());

        if (busTopicParams.getPartner() != null) {
            props.setProperty("Partner", busTopicParams.getPartner());
        }
        if (dme2RouteOffer != null) {
            props.setProperty(PolicyEndPointProperties.DME2_ROUTE_OFFER_PROPERTY, dme2RouteOffer);
        }

        props.setProperty("Latitude", busTopicParams.getLatitude());
        props.setProperty("Longitude", busTopicParams.getLongitude());

        /* These are optional, will default to these values if not set in additionalProps */
        props.setProperty("AFT_DME2_EP_READ_TIMEOUT_MS", "50000");
        props.setProperty("AFT_DME2_ROUNDTRIP_TIMEOUT_MS", "240000");
        props.setProperty("AFT_DME2_EP_CONN_TIMEOUT", "15000");
        props.setProperty("Version", "1.0");
        props.setProperty("SubContextPath", "/");
        props.setProperty("sessionstickinessrequired", "no");

        /* These should not change */
        props.setProperty("TransportType", "DME2");
    }

    /**
     * Throws exception when any of the checks are invalid.
     * @param busTopicParams topics
     * @param topicType topic type (sink or source)
     */
    public static void validateBusTopicParams(BusTopicParams busTopicParams, String topicType) {
        if (busTopicParams.isEnvironmentInvalid()) {
            throw paramException(busTopicParams.getTopic(), topicType,
                PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX);
        }
        if (busTopicParams.isAftEnvironmentInvalid()) {
            throw paramException(busTopicParams.getTopic(), topicType,
                PolicyEndPointProperties.PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX);
        }
        if (busTopicParams.isLatitudeInvalid()) {
            throw paramException(busTopicParams.getTopic(), topicType,
                PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX);
        }
        if (busTopicParams.isLongitudeInvalid()) {
            throw paramException(busTopicParams.getTopic(), topicType,
                PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX);
        }
    }

    private static IllegalArgumentException paramException(String topic, String topicType, String propertyName) {
        return new IllegalArgumentException("Missing " + topicType + "."
            + topic + propertyName + " property for DME2 in DMaaP");

    }
}
