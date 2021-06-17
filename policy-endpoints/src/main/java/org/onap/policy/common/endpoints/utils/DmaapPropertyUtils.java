/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.utils;

import com.google.re2j.Pattern;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams.TopicParamsBuilder;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DmaapPropertyUtils {
    private static final Pattern COMMA_SPACE_PAT = Pattern.compile("\\s*,\\s*");

    /**
     * Maps a topic property to a DME property.
     */
    private static final Map<String, String> PROP_TO_DME;

    static {
        Map<String, String> map = new HashMap<>();

        map.put(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX,
                        PolicyEndPointProperties.DME2_ROUTE_OFFER_PROPERTY);

        map.put(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX,
                        PolicyEndPointProperties.DME2_READ_TIMEOUT_PROPERTY);

        map.put(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX,
                        PolicyEndPointProperties.DME2_EP_CONN_TIMEOUT_PROPERTY);

        map.put(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX,
                        PolicyEndPointProperties.DME2_ROUNDTRIP_TIMEOUT_PROPERTY);

        map.put(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_VERSION_SUFFIX,
                        PolicyEndPointProperties.DME2_VERSION_PROPERTY);

        map.put(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX,
                        PolicyEndPointProperties.DME2_SUBCONTEXT_PATH_PROPERTY);

        map.put(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX,
                        PolicyEndPointProperties.DME2_SESSION_STICKINESS_REQUIRED_PROPERTY);

        PROP_TO_DME = Collections.unmodifiableMap(map);
    }

    /**
     * Makes a topic builder, configuring it with properties that are common to both
     * sources and sinks.
     *
     * @param props properties to be used to configure the builder
     * @param topic topic being configured
     * @param servers target servers
     * @return a topic builder
     */
    public static TopicParamsBuilder makeBuilder(PropertyUtils props, String topic, String servers) {

        /* Additional DME2 Properties */

        Map<String, String> dme2AdditionalProps = new HashMap<>();

        for (Map.Entry<String, String> ent : PROP_TO_DME.entrySet()) {
            String propName = ent.getKey();
            var value = props.getString(propName, null);

            if (!StringUtils.isBlank(value)) {
                String dmeName = ent.getValue();
                dme2AdditionalProps.put(dmeName, value);
            }
        }

        final List<String> serverList = new ArrayList<>(Arrays.asList(COMMA_SPACE_PAT.split(servers)));

        return BusTopicParams.builder()
                    .servers(serverList)
                    .topic(topic)
                    .effectiveTopic(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX,
                                    topic))
                    .apiKey(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX, null))
                    .apiSecret(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX, null))
                    .userName(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_AAF_MECHID_SUFFIX, null))
                    .password(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_AAF_PASSWORD_SUFFIX, null))
                    .environment(props.getString(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX,
                                    null))
                    .aftEnvironment(props.getString(
                                    PolicyEndPointProperties.PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX, null))
                    .partner(props.getString(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_PARTNER_SUFFIX, null))
                    .latitude(props.getString(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX, null))
                    .longitude(props.getString(PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX, null))
                    .additionalProps(dme2AdditionalProps)
                    .managed(props.getBoolean(PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, true))
                    .useHttps(props.getBoolean(PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, false))
                    .allowSelfSignedCerts(props.getBoolean(
                                    PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX, false));
    }
}
