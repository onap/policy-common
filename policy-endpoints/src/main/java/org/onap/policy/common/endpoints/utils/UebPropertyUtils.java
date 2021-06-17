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
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams.TopicParamsBuilder;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UebPropertyUtils {
    private static final Pattern COMMA_SPACE_PAT = Pattern.compile("\\s*,\\s*");

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

        final List<String> serverList = new ArrayList<>(Arrays.asList(COMMA_SPACE_PAT.split(servers)));

        return BusTopicParams.builder()
                    .servers(serverList)
                    .topic(topic)
                    .effectiveTopic(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX,
                                    topic))
                    .apiKey(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX, null))
                    .apiSecret(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX, null))
                    .consumerGroup(props.getString(
                                    PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX, null))
                    .consumerInstance(props.getString(
                                    PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX, null))
                    .managed(props.getBoolean(PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, true))
                    .useHttps(props.getBoolean(PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, false))
                    .allowSelfSignedCerts(props.getBoolean(
                                    PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX,
                                    false));
    }
}
