/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.utils.gson.GsonTestUtils;

public class InlineBusTopicSinkTest extends TopicTestBase {

    private InlineBusTopicSinkImpl sink;

    /**
     * Creates the object to be tested.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();

        sink = new InlineBusTopicSinkImpl(makeBuilder().build());
    }

    @After
    public void tearDown() {
        sink.shutdown();
    }

    @Test
    public void testSerialize() {
        assertThatCode(() -> new GsonTestUtils().compareGson(sink, InlineBusTopicSinkTest.class))
                        .doesNotThrowAnyException();
    }

    @Test
    public void testInlineBusTopicSinkImpl() {
        // verify that different wrappers can be built
        sink = new InlineBusTopicSinkImpl(makeBuilder().build());
        assertEquals(MY_PARTITION, sink.getPartitionKey());

        sink = new InlineBusTopicSinkImpl(makeBuilder().partitionId(null).build());
        assertNotNull(sink.getPartitionKey());
    }

    @Test
    public void testStart() {
        assertTrue(sink.start());
        assertEquals(1, sink.initCount);

        // re-start, init() should not be invoked again
        assertTrue(sink.start());
        assertEquals(1, sink.initCount);
    }

    @Test(expected = IllegalStateException.class)
    public void testStart_Locked() {
        sink.lock();
        sink.start();
    }

    @Test
    public void testStop() {
        BusPublisher pub = mock(BusPublisher.class);
        sink.publisher = pub;

        assertTrue(sink.stop());
        verify(pub).close();

        // stop again, shouldn't not invoke close() again
        assertFalse(sink.stop());
        verify(pub).close();

        // publisher throws exception
        sink = new InlineBusTopicSinkImpl(makeBuilder().build());
        sink.publisher = pub;
        doThrow(new RuntimeException(EXPECTED)).when(pub).close();
        assertTrue(sink.stop());
    }

    @Test
    public void testSend() {
        sink.start();
        BusPublisher pub = mock(BusPublisher.class);
        sink.publisher = pub;

        TopicListener listener = mock(TopicListener.class);
        sink.register(listener);

        assertTrue(sink.send(MY_MESSAGE));

        verify(pub).send(MY_PARTITION, MY_MESSAGE);
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE);
        assertEquals(List.of(MY_MESSAGE), Arrays.asList(sink.getRecentEvents()));

        // arrange for send to throw an exception
        when(pub.send(anyString(), anyString())).thenThrow(new RuntimeException(EXPECTED));

        assertFalse(sink.send(MY_MESSAGE));

        // no more event deliveries
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSend_NullMessage() {
        sink.start();
        sink.publisher = mock(BusPublisher.class);

        sink.send(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSend_EmptyMessage() {
        sink.start();
        sink.publisher = mock(BusPublisher.class);

        sink.send("");
    }

    @Test(expected = IllegalStateException.class)
    public void testSend_NotStarted() {
        sink.publisher = mock(BusPublisher.class);

        sink.send(MY_MESSAGE);
    }

    @Test
    public void testSetPartitionKey_getPartitionKey() {
        assertEquals(MY_PARTITION, sink.getPartitionKey());

        sink.setPartitionKey("part-B");
        assertEquals("part-B", sink.getPartitionKey());
    }

    @Test
    public void testShutdown() {
        BusPublisher pub = mock(BusPublisher.class);
        sink.publisher = pub;

        sink.shutdown();
        verify(pub).close();
    }

    @Test
    public void testAnyNullOrEmpty() {
        assertFalse(sink.anyNullOrEmpty());
        assertFalse(sink.anyNullOrEmpty("any-none-null", "any-none-null-B"));

        assertTrue(sink.anyNullOrEmpty(null, "any-first-null"));
        assertTrue(sink.anyNullOrEmpty("any-middle-null", null, "any-middle-null-B"));
        assertTrue(sink.anyNullOrEmpty("any-last-null", null));
        assertTrue(sink.anyNullOrEmpty("any-empty", ""));
    }

    @Test
    public void testAllNullOrEmpty() {
        assertTrue(sink.allNullOrEmpty());
        assertTrue(sink.allNullOrEmpty(""));
        assertTrue(sink.allNullOrEmpty(null, ""));

        assertFalse(sink.allNullOrEmpty("all-ok-only-one"));
        assertFalse(sink.allNullOrEmpty("all-ok-one", "all-ok-two"));
        assertFalse(sink.allNullOrEmpty("all-ok-null", null));
        assertFalse(sink.allNullOrEmpty("", "all-ok-empty"));
        assertFalse(sink.allNullOrEmpty("", "all-one-ok", null));
    }

    @Test
    public void testToString() {
        assertTrue(sink.toString().startsWith("InlineBusTopicSink ["));
    }

    /**
     * Implementation of InlineBusTopicSink that tracks the number of times that init() is
     * invoked.
     */
    private static class InlineBusTopicSinkImpl extends InlineBusTopicSink {

        private int initCount = 0;

        public InlineBusTopicSinkImpl(BusTopicParams busTopicParams) {
            super(busTopicParams);
        }

        @Override
        public CommInfrastructure getTopicCommInfrastructure() {
            return CommInfrastructure.NOOP;
        }

        @Override
        public void init() {
            ++initCount;
        }

    }
}
