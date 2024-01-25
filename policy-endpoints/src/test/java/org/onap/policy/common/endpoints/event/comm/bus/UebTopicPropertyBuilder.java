/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation.
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

import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_AFT_ENV;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_API_KEY;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_API_SECRET;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_CONS_GROUP;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_CONS_INST;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_EFFECTIVE_TOPIC;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_FETCH_LIMIT;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_FETCH_TIMEOUT;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_PARTITION;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX;

import java.util.List;
import lombok.Getter;
import org.onap.policy.common.endpoints.parameters.TopicParameters;

@Getter
public class UebTopicPropertyBuilder extends TopicPropertyBuilder {

    public static final String SERVER = "my-server";
    public static final String TOPIC2 = "my-topic-2";

    private final TopicParameters params = new TopicParameters();

    /**
     * Constructs the object.
     *
     * @param prefix the prefix for the properties to be built
     */
    public UebTopicPropertyBuilder(String prefix) {
        super(prefix);
    }

    /**
     * Adds a topic and configures its properties with default values.
     *
     * @param topic the topic to be added
     * @return this builder
     */
    public UebTopicPropertyBuilder makeTopic(String topic) {
        addTopic(topic);

        setTopicProperty(PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX, MY_EFFECTIVE_TOPIC);
        setTopicProperty(PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX, MY_CONS_GROUP);
        setTopicProperty(PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX, MY_CONS_INST);
        setTopicProperty(PROPERTY_MANAGED_SUFFIX, "true");
        setTopicProperty(PROPERTY_HTTP_HTTPS_SUFFIX, "true");
        setTopicProperty(PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX, "true");
        setTopicProperty(PROPERTY_TOPIC_API_KEY_SUFFIX, MY_API_KEY);
        setTopicProperty(PROPERTY_TOPIC_API_SECRET_SUFFIX, MY_API_SECRET);
        setTopicProperty(PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX, MY_FETCH_LIMIT);
        setTopicProperty(PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX, MY_FETCH_TIMEOUT);
        setTopicProperty(PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX, MY_PARTITION);
        setTopicProperty(PROPERTY_TOPIC_SERVERS_SUFFIX, SERVER);

        params.setTopicCommInfrastructure("ueb");
        params.setTopic(topic);
        params.setEffectiveTopic(MY_EFFECTIVE_TOPIC);
        params.setConsumerGroup(MY_CONS_GROUP);
        params.setConsumerInstance(MY_CONS_INST);
        params.setManaged(true);
        params.setUseHttps(true);
        params.setAftEnvironment(MY_AFT_ENV);
        params.setAllowSelfSignedCerts(true);
        params.setApiKey(MY_API_KEY);
        params.setApiSecret(MY_API_SECRET);
        params.setFetchLimit(MY_FETCH_LIMIT);
        params.setFetchTimeout(MY_FETCH_TIMEOUT);
        params.setPartitionId(MY_PARTITION);
        params.setServers(List.of(SERVER));

        return this;
    }
}
