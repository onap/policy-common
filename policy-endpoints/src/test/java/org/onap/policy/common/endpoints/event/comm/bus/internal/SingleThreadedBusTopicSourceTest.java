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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer.FilterableBusConsumer;
import org.onap.policy.common.utils.gson.GsonTestUtils;

public class SingleThreadedBusTopicSourceTest extends TopicTestBase {
    private Thread thread;
    private BusConsumer cons;
    private TopicListener listener;
    private SingleThreadedBusTopicSourceImpl source;

    /**
     * Creates the object to be tested, as well as various mocks.
     */
    @Before
    public void setUp() {
        super.setUp();

        thread = mock(Thread.class);
        cons = mock(BusConsumer.class);
        listener = mock(TopicListener.class);
        source = new SingleThreadedBusTopicSourceImpl(makeBuilder().build());
    }

    @After
    public void tearDown() {
        source.shutdown();
    }
    
    @Test
    public void testSerialize() {
        new GsonTestUtils().compareGson(source, new File("SingleThreadedBusTopicSourceTest.json"));
    }

    @Test
    public void testRegister() {
        source.register(listener);
        assertEquals(1, source.initCount);
        source.offer(MY_MESSAGE);
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE);

        // register another - should not re-init
        TopicListener listener2 = mock(TopicListener.class);
        source.register(listener2);
        assertEquals(1, source.initCount);
        source.offer(MY_MESSAGE + "z");
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE + "z");
        verify(listener2).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE + "z");

        // re-register - should not re-init
        source.register(listener);
        assertEquals(1, source.initCount);
        source.offer(MY_MESSAGE2);
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE2);

        // lock & register - should not init
        source = new SingleThreadedBusTopicSourceImpl(makeBuilder().build());
        source.lock();
        source.register(listener);
        assertEquals(0, source.initCount);

        // exception during init
        source = new SingleThreadedBusTopicSourceImpl(makeBuilder().build());
        source.initEx = true;
        source.register(listener);
    }

    @Test
    public void testUnregister() {
        TopicListener listener2 = mock(TopicListener.class);
        source.register(listener);
        source.register(listener2);

        // unregister first listener - should NOT invoke close
        source.unregister(listener);
        verify(cons, never()).close();
        assertEquals(Arrays.asList(listener2), source.snapshotTopicListeners());

        // unregister same listener - should not invoke close
        source.unregister(listener);
        verify(cons, never()).close();
        assertEquals(Arrays.asList(listener2), source.snapshotTopicListeners());

        // unregister second listener - SHOULD invoke close
        source.unregister(listener2);
        verify(cons).close();
        assertTrue(source.snapshotTopicListeners().isEmpty());

        // unregister same listener - should not invoke close again
        source.unregister(listener2);
        verify(cons).close();
        assertTrue(source.snapshotTopicListeners().isEmpty());
    }

    @Test
    public void testToString() {
        assertTrue(source.toString().startsWith("SingleThreadedBusTopicSource ["));
    }

    @Test
    public void testMakePollerThread() {
        SingleThreadedBusTopicSource source2 = new SingleThreadedBusTopicSource(makeBuilder().build()) {
            @Override
            public CommInfrastructure getTopicCommInfrastructure() {
                return CommInfrastructure.NOOP;
            }

            @Override
            public void init() throws MalformedURLException {
                // do nothing
            }
        };
        
        assertNotNull(source2.makePollerThread());
    }

    @Test
    public void testSingleThreadedBusTopicSource() {
        // verify that different wrappers can be built
        new SingleThreadedBusTopicSourceImpl(makeBuilder().consumerGroup(null).build());
        new SingleThreadedBusTopicSourceImpl(makeBuilder().consumerInstance(null).build());
        new SingleThreadedBusTopicSourceImpl(makeBuilder().fetchTimeout(-1).build());
        new SingleThreadedBusTopicSourceImpl(makeBuilder().fetchLimit(-1).build());
    }

    @Test
    public void testStart() {
        source.start();
        assertTrue(source.isAlive());
        assertEquals(1, source.initCount);
        verify(thread).start();

        // attempt to start again - nothing should be invoked again
        source.start();
        assertTrue(source.isAlive());
        assertEquals(1, source.initCount);
        verify(thread).start();

        // stop & re-start
        source.stop();
        source.start();
        assertTrue(source.isAlive());
        assertEquals(2, source.initCount);
        verify(thread, times(2)).start();
    }

    @Test(expected = IllegalStateException.class)
    public void testStart_Locked() {
        source.lock();
        source.start();
    }

    @Test(expected = IllegalStateException.class)
    public void testStart_InitEx() {
        source.initEx = true;
        source.start();
    }

    @Test
    public void testStop() {
        source.start();
        source.stop();
        verify(cons).close();

        // stop it again - not re-closed
        source.stop();
        verify(cons).close();

        // start & stop again, but with an exception
        doThrow(new RuntimeException(EXPECTED)).when(cons).close();
        source.start();
        source.stop();
    }

    @Test
    public void testRun() throws Exception {
        source.register(listener);

        /*
         * Die in the middle of fetching messages. Also, throw an exception during the
         * first fetch attempt.
         */
        when(cons.fetch()).thenAnswer(new Answer<Iterable<String>>() {
            int count = 0;

            @Override
            public Iterable<String> answer(InvocationOnMock invocation) throws Throwable {
                if (++count > 1) {
                    source.alive = false;
                    return Arrays.asList(MY_MESSAGE, MY_MESSAGE2);

                } else {
                    throw new IOException(EXPECTED);
                }
            }
        });
        source.alive = true;
        source.run();
        assertEquals(Arrays.asList(MY_MESSAGE), Arrays.asList(source.getRecentEvents()));
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE);
        verify(listener, never()).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE2);

        /*
         * Die AFTER fetching messages.
         */
        final String msga = "message-A";
        final String msgb = "message-B";
        when(cons.fetch()).thenAnswer(new Answer<Iterable<String>>() {
            int count = 0;

            @Override
            public Iterable<String> answer(InvocationOnMock invocation) throws Throwable {
                if (++count > 1) {
                    source.alive = false;
                    return Collections.emptyList();

                } else {
                    return Arrays.asList(msga, msgb);
                }
            }
        });
        source.alive = true;
        source.run();
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, msga);
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, msgb);

        assertEquals(Arrays.asList(MY_MESSAGE, msga, msgb), Arrays.asList(source.getRecentEvents()));
    }

    @Test
    public void testOffer() {
        source.register(listener);
        source.offer(MY_MESSAGE);
        verify(listener).onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, MY_MESSAGE);
        assertEquals(Arrays.asList(MY_MESSAGE), Arrays.asList(source.getRecentEvents()));
    }

    @Test(expected = IllegalStateException.class)
    public void testOffer_NotStarted() {
        source.offer(MY_MESSAGE);
    }

    @Test
    public void testSetFilter() {
        FilterableBusConsumer filt = mock(FilterableBusConsumer.class);
        cons = filt;

        source.start();
        source.setFilter("my-filter");
        verify(filt).setFilter("my-filter");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetFilter_Unsupported() {
        source.start();
        source.setFilter("unsupported-filter");
    }

    @Test
    public void testGetConsumerGroup() {
        assertEquals(MY_CONS_GROUP, source.getConsumerGroup());
    }

    @Test
    public void testGetConsumerInstance() {
        assertEquals(MY_CONS_INST, source.getConsumerInstance());
    }

    @Test
    public void testShutdown() {
        source.register(listener);

        source.shutdown();
        verify(cons).close();
        assertTrue(source.snapshotTopicListeners().isEmpty());
    }

    @Test
    public void testGetFetchTimeout() {
        assertEquals(MY_FETCH_TIMEOUT, source.getFetchTimeout());
    }

    @Test
    public void testGetFetchLimit() {
        assertEquals(MY_FETCH_LIMIT, source.getFetchLimit());
    }

    /**
     * Implementation of SingleThreadedBusTopicSource that counts the number of times
     * init() is invoked.
     */
    private class SingleThreadedBusTopicSourceImpl extends SingleThreadedBusTopicSource {

        private int initCount = 0;
        private boolean initEx = false;

        public SingleThreadedBusTopicSourceImpl(BusTopicParams busTopicParams) {
            super(busTopicParams);
        }

        @Override
        public CommInfrastructure getTopicCommInfrastructure() {
            return CommInfrastructure.NOOP;
        }

        @Override
        public void init() throws MalformedURLException {
            ++initCount;

            if (initEx) {
                throw new MalformedURLException(EXPECTED);
            }

            consumer = cons;
        }

        @Override
        protected Thread makePollerThread() {
            return thread;
        }

    }
}
