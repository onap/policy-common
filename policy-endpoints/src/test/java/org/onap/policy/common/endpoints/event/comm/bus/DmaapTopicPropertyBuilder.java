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

import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_AFT_ENV;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_API_KEY;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_API_SECRET;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_ENV;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_FETCH_LIMIT;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_FETCH_TIMEOUT;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_LAT;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_LONG;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_PARTITION;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_PARTNER;
import static org.onap.policy.common.endpoints.event.comm.bus.BusTopicTestBase.MY_ROUTE;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_PARTNER_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_VERSION_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_AAF_MECHID_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_AAF_PASSWORD_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_API_KEY_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_API_SECRET_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX;

public class DmaapTopicPropertyBuilder extends TopicPropertyBuilder {

    public static final String SERVER = "my-server";
    public static final String TOPIC2 = "my-topic-2";

    public static final String MY_CONN_TIMEOUT = "200";
    public static final String MY_READ_TIMEOUT = "201";
    public static final String MY_ROUNDTRIP_TIMEOUT = "202";
    public static final String MY_STICKINESS = "true";
    public static final String MY_SUBCONTEXT = "my-subcontext";
    public static final String MY_DME_VERSION = "my-version";
    public static final String MY_AAF_MECHID = "my-aaf-mechid";
    public static final String MY_AAF_PASSWD = "my-aaf-passwd";

    /**
     * Constructs the object.
     *
     * @param prefix the prefix for the properties to be built
     */
    public DmaapTopicPropertyBuilder(String prefix) {
        super(prefix);
    }

    /**
     * Adds a topic and configures it's properties with default values.
     *
     * @param topic the topic to be added
     * @return this builder
     */
    public DmaapTopicPropertyBuilder makeTopic(String topic) {
        addTopic(topic);

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
        setTopicProperty(PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX, MY_CONN_TIMEOUT);
        setTopicProperty(PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX, MY_ENV);
        setTopicProperty(PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX, MY_LAT);
        setTopicProperty(PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX, MY_LONG);
        setTopicProperty(PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX, MY_PARTITION);
        setTopicProperty(PROPERTY_DMAAP_DME2_PARTNER_SUFFIX, MY_PARTNER);
        setTopicProperty(PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX, MY_READ_TIMEOUT);
        setTopicProperty(PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX, MY_ROUNDTRIP_TIMEOUT);
        setTopicProperty(PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX, MY_ROUTE);
        setTopicProperty(PROPERTY_TOPIC_SERVERS_SUFFIX, SERVER);
        setTopicProperty(PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX, MY_STICKINESS);
        setTopicProperty(PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX, MY_SUBCONTEXT);
        setTopicProperty(PROPERTY_DMAAP_DME2_VERSION_SUFFIX, MY_DME_VERSION);

        return this;
    }
}
