/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_EFFECTIVE_TOPIC;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX;

import java.util.Arrays;
import lombok.Getter;
import org.onap.policy.common.endpoints.parameters.TopicParameters;

public class NoopTopicPropertyBuilder extends TopicPropertyBuilder {

    public static final String SERVER = "my-server";
    public static final String TOPIC2 = "my-topic-2";

    @Getter
    private TopicParameters params = new TopicParameters();

    /**
     * Constructs the object.
     *
     * @param prefix the prefix for the properties to be built
     */
    public NoopTopicPropertyBuilder(String prefix) {
        super(prefix);
    }

    /**
     * Adds a topic and configures it's properties with default values.
     *
     * @param topic the topic to be added
     * @return this builder
     */
    public NoopTopicPropertyBuilder makeTopic(String topic) {
        addTopic(topic);

        setTopicProperty(PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX, MY_EFFECTIVE_TOPIC);
        setTopicProperty(PROPERTY_MANAGED_SUFFIX, "true");
        setTopicProperty(PROPERTY_HTTP_HTTPS_SUFFIX, "true");
        setTopicProperty(PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX, "true");
        setTopicProperty(PROPERTY_TOPIC_SERVERS_SUFFIX, SERVER);

        params.setTopicCommInfrastructure("noop");
        params.setTopic(topic);
        params.setEffectiveTopic(MY_EFFECTIVE_TOPIC);
        params.setManaged(true);
        params.setUseHttps(true);
        params.setAllowSelfSignedCerts(true);
        params.setServers(Arrays.asList(SERVER));

        return this;
    }
}
