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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicListener;

public class NoopTopicSinkTest extends TopicTestBase {

    private NoopTopicSink sink;

    /**
     * Creates the object to be tested.
     */
    @Before
    public void setUp() {
        super.setUp();

        sink = new NoopTopicSink(servers, MY_TOPIC);
    }

    @Test
    public void testToString() {
        assertTrue(sink.toString().startsWith("NoopTopicSink ["));
    }

    @Test
    public void testSend() {
        TopicListener listener = mock(TopicListener.class);
        sink.register(listener);
        sink.start();

        assertTrue(sink.send(MY_MESSAGE));

        assertEquals(Arrays.asList(MY_MESSAGE), Arrays.asList(sink.getRecentEvents()));
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE);

        // generate exception during broadcast
        sink = new NoopTopicSink(servers, MY_TOPIC) {
            @Override
            protected boolean broadcast(String message) {
                throw new RuntimeException(EXPECTED);
            }

        };

        sink.start();
        assertFalse(sink.send(MY_MESSAGE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSend_NullMessage() {
        sink.send(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSend_EmptyMessage() {
        sink.send("");
    }

    @Test(expected = IllegalStateException.class)
    public void testSend_NotStarted() {
        sink.send(MY_MESSAGE);
    }

    @Test
    public void testGetTopicCommInfrastructure() {
        assertEquals(CommInfrastructure.NOOP, sink.getTopicCommInfrastructure());
    }

    @Test
    public void testStart_testStop_testShutdown() {
        sink.start();
        assertTrue(sink.isAlive());

        // start again
        sink.start();
        assertTrue(sink.isAlive());

        // stop
        sink.stop();
        assertFalse(sink.isAlive());

        // re-start again
        sink.start();
        assertTrue(sink.isAlive());

        // shutdown
        sink.shutdown();
        assertFalse(sink.isAlive());
    }

    @Test(expected = IllegalStateException.class)
    public void testStart_Locked() {
        sink.lock();
        sink.start();
    }

}
