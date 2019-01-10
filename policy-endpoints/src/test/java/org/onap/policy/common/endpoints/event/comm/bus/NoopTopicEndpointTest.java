/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicListener;

public abstract class NoopTopicEndpointTest<F extends NoopTopicFactory<T>, T extends NoopTopicEndpoint>
    extends TopicTestBase {

    protected final F factory;
    protected T endpoint;

    protected abstract boolean io(String message);

    public NoopTopicEndpointTest(F factory) {
        this.factory = factory;
    }

    @Before
    public void setUp() {
        super.setUp();
        this.endpoint = this.factory.build(servers, MY_TOPIC);
    }

    @Test
    public void tesIo() {
        TopicListener listener = mock(TopicListener.class);
        this.endpoint.register(listener);
        this.endpoint.start();

        assertTrue(io(MY_MESSAGE));

        assertEquals(Collections.singletonList(MY_MESSAGE), Arrays.asList(this.endpoint.getRecentEvents()));
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIoNullMessage() {
        io(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIoEmptyMessage() {
        io("");
    }

    @Test(expected = IllegalStateException.class)
    public void testOfferNotStarted() {
        io(MY_MESSAGE);
    }

    @Test
    public void testGetTopicCommInfrastructure() {
        assertEquals(CommInfrastructure.NOOP, this.endpoint.getTopicCommInfrastructure());
    }

    @Test
    public void testStart_testStop_testShutdown() {
        this.endpoint.start();
        assertTrue(this.endpoint.isAlive());

        // start again
        this.endpoint.start();
        assertTrue(this.endpoint.isAlive());

        // stop
        this.endpoint.stop();
        assertFalse(this.endpoint.isAlive());

        // re-start again
        this.endpoint.start();
        assertTrue(this.endpoint.isAlive());

        // shutdown
        this.endpoint.shutdown();
        assertFalse(this.endpoint.isAlive());
    }

    @Test(expected = IllegalStateException.class)
    public void testStart_Locked() {
        this.endpoint.lock();
        this.endpoint.start();
    }

}
