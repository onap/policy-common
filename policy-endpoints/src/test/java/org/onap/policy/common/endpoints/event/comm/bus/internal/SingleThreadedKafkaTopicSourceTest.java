/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.utils.gson.GsonTestUtils;

public class SingleThreadedKafkaTopicSourceTest extends TopicTestBase {
    private SingleThreadedKafkaTopicSource source;

    /**
     * Creates the object to be tested.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();

        source = new SingleThreadedKafkaTopicSource(makeBuilder().build());
    }

    @After
    public void tearDown() {
        source.shutdown();
    }

    @Test
    public void testToString() {
        assertTrue(source.toString().startsWith("SingleThreadedKafkaTopicSource ["));
    }

    @Test
    public void testGetTopicCommInfrastructure() {
        assertEquals(CommInfrastructure.KAFKA, source.getTopicCommInfrastructure());
    }

}
