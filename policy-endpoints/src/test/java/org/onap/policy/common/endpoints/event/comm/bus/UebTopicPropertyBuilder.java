/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_FETCH_LIMIT;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_FETCH_TIMEOUT;
import static org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase.MY_PARTITION;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_AAF_MECHID_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_AAF_PASSWORD_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX;

public class UebTopicPropertyBuilder extends TopicPropertyBuilder {

    public static final String SERVER = "my-server";
    public static final String TOPIC2 = "my-topic-2";

    public static final String MY_AAF_MECHID = "my-aaf-mechid";
    public static final String MY_AAF_PASSWD = "my-aaf-passwd";

    /**
     * Constructs the object.
     *
     * @param prefix the prefix for the properties to be built
     */
    public UebTopicPropertyBuilder(String prefix) {
        super(prefix);
    }

    /**
     * Adds a topic and configures it's properties with default values.
     *
     * @param topic the topic to be added
     * @return this builder
     */
    public UebTopicPropertyBuilder makeTopic(String topic) {
        addTopic(topic);

        setTopicProperty(PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX, MY_CONS_GROUP);
        setTopicProperty(PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX, MY_CONS_INST);
        setTopicProperty(PROPERTY_MANAGED_SUFFIX, "true");
        setTopicProperty(PROPERTY_HTTP_HTTPS_SUFFIX, "true");
        setTopicProperty(PROPERTY_TOPIC_AAF_MECHID_SUFFIX, MY_AAF_MECHID);
        setTopicProperty(PROPERTY_TOPIC_AAF_PASSWORD_SUFFIX, MY_AAF_PASSWD);
        setTopicProperty(PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX, MY_AFT_ENV);
        setTopicProperty(PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX, "true");
        setTopicProperty(PROPERTY_TOPIC_API_KEY_SUFFIX, MY_API_KEY);
        setTopicProperty(PROPERTY_TOPIC_API_SECRET_SUFFIX, MY_API_SECRET);
        setTopicProperty(PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX, MY_FETCH_LIMIT);
        setTopicProperty(PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX, MY_FETCH_TIMEOUT);
        setTopicProperty(PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX, MY_PARTITION);
        setTopicProperty(PROPERTY_TOPIC_SERVERS_SUFFIX, SERVER);

        return this;
    }
}
