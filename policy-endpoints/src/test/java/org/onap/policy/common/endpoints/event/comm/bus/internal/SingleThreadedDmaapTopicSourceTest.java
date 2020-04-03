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

import java.net.MalformedURLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.utils.gson.GsonTestUtils;

public class SingleThreadedDmaapTopicSourceTest extends TopicTestBase {
    private static final String SOURCE_NAME = "SingleThreadedDmaapTopicSource [";
    private SingleThreadedDmaapTopicSource source;

    /**
     * Creates the object to be tested.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();

        source = new SingleThreadedDmaapTopicSource(makeBuilder().build());
    }

    @After
    public void tearDown() {
        source.shutdown();
    }

    @Test
    public void testSerialize() {
        assertThatCode(() -> new GsonTestUtils().compareGson(source, SingleThreadedDmaapTopicSourceTest.class))
                        .doesNotThrowAnyException();
    }

    @Test
    public void testToString() {
        assertTrue(source.toString().startsWith(SOURCE_NAME));
        source.shutdown();

        // try with null password
        source = new SingleThreadedDmaapTopicSource(makeBuilder().password(null).build());
        assertTrue(source.toString().startsWith(SOURCE_NAME));
        source.shutdown();

        // try with empty password
        source = new SingleThreadedDmaapTopicSource(makeBuilder().password("").build());
        assertTrue(source.toString().startsWith(SOURCE_NAME));
        source.shutdown();
    }

    @Test
    public void testInit() {
        // verify with different parameters
        new SingleThreadedDmaapTopicSource(makeBuilder().environment(null).aftEnvironment(null).latitude(null)
                        .longitude(null).partner(null).build()).shutdown();
        assertThatCode(() -> new SingleThreadedDmaapTopicSource(makeBuilder().userName(null).build()).shutdown())
                        .doesNotThrowAnyException();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingleThreadedDmaapTopicSource_Ex() {
        new SingleThreadedDmaapTopicSource(makeBuilder().build()) {
            @Override
            public void init() throws MalformedURLException {
                throw new MalformedURLException(EXPECTED);
            }
        }.shutdown();
    }

    @Test
    public void testGetTopicCommInfrastructure() {
        assertEquals(CommInfrastructure.DMAAP, source.getTopicCommInfrastructure());
    }

}
