/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
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

import static org.junit.Assert.assertEquals;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_VERSION_SUFFIX;

import java.util.Map;
import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;

/**
 * Base class for DmaapTopicXxxFactory tests.
 *
 * @param <T> type of topic managed by the factory
 */
public abstract class DmaapTopicFactoryTestBase<T extends Topic> extends BusTopicFactoryTestBase<T> {

    public static final String MY_CONN_TIMEOUT = "200";
    public static final String MY_READ_TIMEOUT = "201";
    public static final String MY_ROUNDTRIP_TIMEOUT = "202";
    public static final String MY_STICKINESS = "true";
    public static final String MY_SUBCONTEXT = "my-subcontext";
    public static final String MY_DME_VERSION = "my-version";

    @Override
    public void testBuildProperties() {
        
        super.testBuildProperties();
        
        initFactory();

        assertEquals(1, buildTopics(makePropBuilder().makeTopic(MY_TOPIC).build()).size());

        BusTopicParams params = getLastParams();
        assertEquals(MY_ENV, params.getEnvironment());
        assertEquals(MY_LAT, params.getLatitude());
        assertEquals(MY_LONG, params.getLongitude());
        assertEquals(MY_PARTNER, params.getPartner());

        Map<String, String> add = params.getAdditionalProps();
        assertEquals(MY_CONN_TIMEOUT, add.get(DmaapTopicSinkFactory.DME2_EP_CONN_TIMEOUT_PROPERTY));
        assertEquals(MY_READ_TIMEOUT, add.get(DmaapTopicSinkFactory.DME2_READ_TIMEOUT_PROPERTY));
        assertEquals(MY_ROUNDTRIP_TIMEOUT, add.get(DmaapTopicSinkFactory.DME2_ROUNDTRIP_TIMEOUT_PROPERTY));
        assertEquals(MY_ROUTE, add.get(DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY));
        assertEquals(MY_STICKINESS, add.get(DmaapTopicSinkFactory.DME2_SESSION_STICKINESS_REQUIRED_PROPERTY));
        assertEquals(MY_SUBCONTEXT, add.get(DmaapTopicSinkFactory.DME2_SUBCONTEXT_PATH_PROPERTY));
        assertEquals(MY_DME_VERSION, add.get(DmaapTopicSinkFactory.DME2_VERSION_PROPERTY));
    }

    @Override
    public void testBuildProperties_Variations() {
        super.testBuildProperties_Variations();

        // check "additional" properties
        expectNullAddProp(PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX,
                        DmaapTopicSinkFactory.DME2_EP_CONN_TIMEOUT_PROPERTY);

        expectNullAddProp(PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX,
                        DmaapTopicSinkFactory.DME2_READ_TIMEOUT_PROPERTY);

        expectNullAddProp(PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX,
                        DmaapTopicSinkFactory.DME2_ROUNDTRIP_TIMEOUT_PROPERTY);

        expectNullAddProp(PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX, DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY);

        expectNullAddProp(PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX,
                        DmaapTopicSinkFactory.DME2_SESSION_STICKINESS_REQUIRED_PROPERTY);

        expectNullAddProp(PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX,
                        DmaapTopicSinkFactory.DME2_SUBCONTEXT_PATH_PROPERTY);

        expectNullAddProp(PROPERTY_DMAAP_DME2_VERSION_SUFFIX, DmaapTopicSinkFactory.DME2_VERSION_PROPERTY);
    }

    @Override
    public void testBuildListOfStringString() {
        super.testBuildListOfStringString();

        // check parameters that were used
        BusTopicParams params = getLastParams();
        assertEquals(false, params.isAllowSelfSignedCerts());
    }
}
