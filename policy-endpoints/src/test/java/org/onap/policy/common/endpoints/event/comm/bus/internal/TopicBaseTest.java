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

package org.onap.policy.common.endpoints.event.comm.bus.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicListener;

public class TopicBaseTest {
    private static String TOPIC = "my-topic";
    private static String EXPECTED = "expected";

    private List<String> servers;
    private TopicBaseImpl base;

    @Before
    public void setUp() {
        servers = Arrays.asList("server-A", "server-B");
        base = new TopicBaseImpl(servers, TOPIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTopicBase_NullServers() {
        new TopicBaseImpl(null, TOPIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTopicBase_EmptyServers() {
        new TopicBaseImpl(Collections.emptyList(), TOPIC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTopicBase_NullTopic() {
        new TopicBaseImpl(servers, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTopicBase_EmptyTopic() {
        new TopicBaseImpl(servers, "");
    }

    @Test
    public void testRegister() {
        TopicListener listener = mock(TopicListener.class);
        base.register(listener);
        assertEquals(Arrays.asList(listener), base.snapshotTopicListeners());

        // re-register - list should be unchanged
        base.register(listener);
        assertEquals(Arrays.asList(listener), base.snapshotTopicListeners());

        // register a new listener
        TopicListener listener2 = mock(TopicListener.class);
        base.register(listener2);
        assertEquals(Arrays.asList(listener, listener2), base.snapshotTopicListeners());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegister_NullListener() {
        base.register(null);
    }

    @Test
    public void testUnregister() {
        // register two listeners
        TopicListener listener = mock(TopicListener.class);
        TopicListener listener2 = mock(TopicListener.class);
        base.register(listener);
        base.register(listener2);

        // unregister one
        base.unregister(listener);
        assertEquals(Arrays.asList(listener2), base.snapshotTopicListeners());

        // unregister the other
        base.unregister(listener2);
        assertTrue(base.snapshotTopicListeners().isEmpty());

        // unregister again
        base.unregister(listener2);
        assertTrue(base.snapshotTopicListeners().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnregister_NullListener() {
        base.register(mock(TopicListener.class));
        base.unregister(null);
    }

    @Test
    public void testBroadcast() {
        // register two listeners
        TopicListener listener = mock(TopicListener.class);
        TopicListener listener2 = mock(TopicListener.class);
        base.register(listener);
        base.register(listener2);

        // broadcast a message
        final String msg1 = "message-A";
        base.broadcast(msg1);
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, TOPIC, msg1);
        verify(listener2).onTopicEvent(CommInfrastructure.NOOP, TOPIC, msg1);

        // broadcast another message, with an exception
        final String msg2 = "message-B";
        doThrow(new RuntimeException(EXPECTED)).when(listener).onTopicEvent(any(), any(), any());
        base.broadcast(msg2);
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, TOPIC, msg2);
        verify(listener2).onTopicEvent(CommInfrastructure.NOOP, TOPIC, msg2);
    }

    @Test
    public void testLock_testUnlock() {
        assertFalse(base.isLocked());
        assertTrue(base.lock());
        assertEquals(0, base.startCount);
        assertEquals(1, base.stopCount);

        // lock again - should not stop again
        assertTrue(base.isLocked());
        assertTrue(base.lock());
        assertEquals(0, base.startCount);
        assertEquals(1, base.stopCount);

        assertTrue(base.isLocked());
        assertTrue(base.unlock());
        assertEquals(1, base.startCount);
        assertEquals(1, base.stopCount);

        // unlock again - should not start again
        assertFalse(base.isLocked());
        assertTrue(base.unlock());
        assertEquals(1, base.startCount);
        assertEquals(1, base.stopCount);

        // lock, but stop returns false
        base = new TopicBaseImpl(servers, TOPIC);
        base.stopReturn = false;
        assertFalse(base.lock());
        assertTrue(base.isLocked());
        assertTrue(base.lock());

        // unlock, but start returns false
        base.startReturn = false;
        assertFalse(base.unlock());
        assertFalse(base.isLocked());
        assertTrue(base.unlock());

        // lock & re-lock, but start throws an exception
        base = new TopicBaseImpl(servers, TOPIC);
        base.startEx = true;
        assertTrue(base.lock());
        assertFalse(base.unlock());
        assertFalse(base.isLocked());
        assertTrue(base.unlock());
    }

    @Test
    public void testIsLocked() {
        assertFalse(base.isLocked());
        base.lock();
        assertTrue(base.isLocked());
        base.unlock();
        assertFalse(base.isLocked());
    }

    @Test
    public void testGetTopic() {
        assertEquals(TOPIC, base.getTopic());
    }

    @Test
    public void testIsAlive() {
        assertFalse(base.isAlive());
        base.start();
        assertTrue(base.isAlive());
        base.stop();
        assertFalse(base.isAlive());
    }

    @Test
    public void testGetServers() {
        assertEquals(servers, base.getServers());
    }

    @Test
    public void testGetRecentEvents() {
        assertEquals(0, base.getRecentEvents().length);

        base.addEvent("recent-A");
        base.addEvent("recent-B");

        String[] recent = base.getRecentEvents();
        assertEquals(2, recent.length);
        assertEquals("recent-A", recent[0]);
        assertEquals("recent-B", recent[1]);
    }

    @Test
    public void testToString() {
        assertNotNull(base.toString());
    }

    /**
     * Implementation of TopicBase.
     */
    private static class TopicBaseImpl extends TopicBase {
        private int startCount = 0;
        private int stopCount = 0;
        private boolean startReturn = true;
        private boolean stopReturn = true;
        private boolean startEx = false;

        /**
         * Constructor.
         * 
         * @param servers list of servers
         * @param topic topic name
         */
        public TopicBaseImpl(List<String> servers, String topic) {
            super(servers, topic);
        }

        @Override
        public CommInfrastructure getTopicCommInfrastructure() {
            return CommInfrastructure.NOOP;
        }

        @Override
        public boolean start() {
            ++startCount;

            if (startEx) {
                throw new RuntimeException(EXPECTED);
            }

            alive = true;
            return startReturn;
        }

        @Override
        public boolean stop() {
            ++stopCount;
            alive = false;
            return stopReturn;
        }

        @Override
        public void shutdown() {
            // do nothing
        }

        /**
         * Adds an event to the list of recent events.
         * 
         * @param event event to be added
         */
        public void addEvent(String event) {
            recentEvents.add(event);
        }
    }
}
