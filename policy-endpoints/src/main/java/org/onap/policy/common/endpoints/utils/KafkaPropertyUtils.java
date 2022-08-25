/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2022 Nordix Foundation.
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
public class KafkaPropertyUtils {
    private static final Pattern COMMA_SPACE_PAT = Pattern.compile("\\s*,\\s*");

    /**
     * Maps a topic property to a DME property.
     */
    private static final Map<String, String> PROP_TO_SASL;

    static {
        Map<String, String> map = new HashMap<>();

        map.put(PolicyEndPointProperties.PROPERTY_KAFKA_SECURITY_PROTOCOL,
                        PolicyEndPointProperties.DEFAULT_KAFKA_SECURITY_PROTOCOL);

        map.put(PolicyEndPointProperties.PROPERTY_KAFKA_SASL_MECHANISM,
                        PolicyEndPointProperties.DEFAULT_KAFKA_SASL_MECHANISM);

        map.put(PolicyEndPointProperties.PROPERTY_KAFKA_JAAS_CONFIG,
                        PolicyEndPointProperties.DEFAULT_KAFKA_JAAS_CONFIG);

        PROP_TO_SASL = Collections.unmodifiableMap(map);
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

        /* Additional Kafka security Properties */
        Map<String, String> kafkaAdditionalProps = new HashMap<>();

        for (Map.Entry<String, String> ent : PROP_TO_SASL.entrySet()) {
            String propName = ent.getKey();
            var value = props.getString(propName, null);

            if (!StringUtils.isBlank(value)) {
                String dmeName = ent.getValue();
                kafkaAdditionalProps.put(dmeName, value);
            }
        }

        final List<String> serverList = new ArrayList<>(Arrays.asList(COMMA_SPACE_PAT.split(servers)));

        return BusTopicParams.builder()
                    .servers(serverList)
                    .topic(topic)
                    .effectiveTopic(props.getString(PolicyEndPointProperties.PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX,
                                    topic))
                    .additionalProps(kafkaAdditionalProps)
                    .managed(props.getBoolean(PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, true));
    }
}
