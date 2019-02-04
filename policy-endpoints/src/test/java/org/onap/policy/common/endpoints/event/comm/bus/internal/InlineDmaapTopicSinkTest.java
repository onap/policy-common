/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
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

package org.onap.policy.common.endpoints.event.comm.bus.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.utils.gson.GsonTestUtils;

public class InlineDmaapTopicSinkTest extends TopicTestBase {
    private InlineDmaapTopicSink sink;

    /**
     * Creates the object to be tested.
     */
    @Before
    public void setUp() {
        super.setUp();

        sink = new InlineDmaapTopicSink(makeBuilder().build());
    }

    @After
    public void tearDown() {
        sink.shutdown();
    }
    
    @Test
    public void testSerialize() {
        new GsonTestUtils().compareJackson2Gson(sink);
    }

    @Test
    public void testToString() {
        assertTrue(sink.toString().startsWith("InlineDmaapTopicSink ["));
    }

    @Test
    public void testInit() {
        // nothing null
        sink = new InlineDmaapTopicSink(makeBuilder().build());
        sink.init();
        sink.shutdown();

        // no DME2 info
        sink = new InlineDmaapTopicSink(makeBuilder().environment(null).aftEnvironment(null).latitude(null)
                        .longitude(null).partner(null).build());
        sink.init();
        sink.shutdown();
    }

    @Test
    public void testGetTopicCommInfrastructure() {
        assertEquals(CommInfrastructure.DMAAP, sink.getTopicCommInfrastructure());
    }

}
