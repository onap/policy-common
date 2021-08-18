/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

@RunWith(MockitoJUnitRunner.class)
public class BidirectionalTopicClientTest {
    private static final Coder coder = new StandardCoder();
    private static final long MAX_WAIT_MS = 5000;
    private static final long SHORT_WAIT_MS = 1;
    private static final String SINK_TOPIC = "my-sink-topic";
    private static final String SOURCE_TOPIC = "my-source-topic";
    private static final String MY_TEXT = "my-text";

    private static final CommInfrastructure SINK_INFRA = CommInfrastructure.UEB;
    private static final CommInfrastructure SOURCE_INFRA = CommInfrastructure.NOOP;

    @Mock
    private TopicSink sink;
    @Mock
    private TopicSource source;
    @Mock
    private TopicEndpoint endpoint;
    @Mock
    private TopicListener listener;

    private MyMessage theMessage;

    private BidirectionalTopicClient client;
    private Context context;

    /**
     * Configures the endpoints.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        Properties props = new Properties();
        props.setProperty("noop.sink.topics", SINK_TOPIC);
        props.setProperty("noop.source.topics", SOURCE_TOPIC);

        // clear all topics and then configure one sink and one source
        TopicEndpointManager.getManager().shutdown();
        TopicEndpointManager.getManager().addTopicSinks(props);
        TopicEndpointManager.getManager().addTopicSources(props);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        // clear all topics after the tests
        TopicEndpointManager.getManager().shutdown();
    }

    /**
     * Creates mocks and an initial client object.
     */
    @Before
    public void setUp() throws Exception {
        when(sink.send(anyString())).thenReturn(true);
        when(sink.getTopicCommInfrastructure()).thenReturn(SINK_INFRA);

        when(source.offer(anyString())).thenReturn(true);
        when(source.getTopicCommInfrastructure()).thenReturn(SOURCE_INFRA);

        when(endpoint.getTopicSinks(anyString())).thenReturn(Arrays.asList());
        when(endpoint.getTopicSinks(SINK_TOPIC)).thenReturn(Arrays.asList(sink));

        when(endpoint.getTopicSources(any())).thenReturn(Arrays.asList());
        when(endpoint.getTopicSources(Arrays.asList(SOURCE_TOPIC))).thenReturn(Arrays.asList(source));

        theMessage = new MyMessage(MY_TEXT);

        client = new BidirectionalTopicClient2(SINK_TOPIC, SOURCE_TOPIC);

        context = new Context();
    }

    @After
    public void tearDown() {
        context.stop();
    }

    @Test
    public void testBidirectionalTopicClient_testGetters() {
        assertSame(sink, client.getSink());
        assertSame(source, client.getSource());
        assertEquals(SINK_TOPIC, client.getSinkTopic());
        assertEquals(SOURCE_TOPIC, client.getSourceTopic());
        assertEquals(SINK_INFRA, client.getSinkTopicCommInfrastructure());
        assertEquals(SOURCE_INFRA, client.getSourceTopicCommInfrastructure());
    }

    /**
     * Tests the constructor when the sink or source cannot be found.
     */
    @Test
    public void testBidirectionalTopicClientExceptions() {
        assertThatThrownBy(() -> new BidirectionalTopicClient2("unknown-sink", SOURCE_TOPIC))
                        .isInstanceOf(BidirectionalTopicClientException.class)
                        .hasMessage("no sinks for topic: unknown-sink");

        assertThatThrownBy(() -> new BidirectionalTopicClient2(SINK_TOPIC, "unknown-source"))
                        .isInstanceOf(BidirectionalTopicClientException.class)
                        .hasMessage("no sources for topic: unknown-source");

        // too many sources
        when(endpoint.getTopicSources(Arrays.asList(SOURCE_TOPIC))).thenReturn(Arrays.asList(source, source));

        assertThatThrownBy(() -> new BidirectionalTopicClient2(SINK_TOPIC, SOURCE_TOPIC))
                        .isInstanceOf(BidirectionalTopicClientException.class)
                        .hasMessage("too many sources for topic: my-source-topic");
    }

    /**
     * Tests the "delegate" methods.
     */
    @Test
    public void testDelegates() {
        assertTrue(client.send("hello"));
        verify(sink).send("hello");

        assertTrue(client.offer("incoming"));
        verify(source).offer("incoming");

        client.register(listener);
        verify(source).register(listener);

        client.unregister(listener);
        verify(source).unregister(listener);
    }

    @Test
    public void testGetTopicEndpointManager() throws BidirectionalTopicClientException {
        // use a real manager
        client = new BidirectionalTopicClient(SINK_TOPIC, SOURCE_TOPIC);
        assertNotNull(client.getTopicEndpointManager());

        assertNotNull(client.getSink());
        assertNotNull(client.getSource());

        assertNotSame(sink, client.getSink());
        assertNotSame(source, client.getSource());
    }

    @Test
    public void testAwaitReceipt() throws Exception {
        context.start(theMessage);
        assertThat(context.awaitSend(1)).isTrue();

        verify(source).register(any());
        verify(sink, atLeast(1)).send(any());

        inject(theMessage);

        verifyReceipt();
    }

    @Test
    public void testAwaitReceipt_AlreadyDone() throws Exception {
        context.start(theMessage);
        assertThat(context.awaitSend(1)).isTrue();

        inject(theMessage);

        verifyReceipt();

        // calling again should result in "true" again, without injecting message
        context.start(theMessage);
        verifyReceipt();
    }

    @Test
    public void testAwaitReceipt_MessageDoesNotMatch() throws Exception {
        context.start(theMessage);
        assertThat(context.awaitSend(1)).isTrue();

        // non-matching message
        inject("{}");

        // wait for a few more calls to "send" and then inject a matching message
        assertThat(context.awaitSend(3)).isTrue();
        inject(theMessage);

        verifyReceipt();
    }

    @Test
    public void testAwaitReceipt_DecodeFails() throws Exception {
        context.start(theMessage);
        assertThat(context.awaitSend(1)).isTrue();

        // force a failure and inject the message
        context.forceDecodeFailure = true;
        inject(theMessage);

        assertThat(context.awaitDecodeFailure()).isTrue();

        // no more failures
        context.forceDecodeFailure = false;
        inject(theMessage);

        verifyReceipt();
    }

    @Test
    public void testAwaitReceipt_Interrupted() throws InterruptedException {
        context.start(theMessage);
        assertThat(context.awaitSend(1)).isTrue();

        context.interrupt();

        verifyNoReceipt();
    }

    @Test
    public void testAwaitReceipt_MultipleLoops() throws Exception {
        context.start(theMessage);

        // wait for multiple "send" calls
        assertThat(context.awaitSend(3)).isTrue();

        inject(theMessage);

        verifyReceipt();
    }

    @Test
    public void testStop() throws InterruptedException {
        context.start(theMessage);
        assertThat(context.awaitSend(1)).isTrue();

        context.stop();

        verifyNoReceipt();
    }

    @Test
    public void testGetWaitMs() throws BidirectionalTopicClientException {
        // don't override getWaitMs()
        var checker = new BidirectionalTopicClient2(SINK_TOPIC, SOURCE_TOPIC);
        assertThat(checker.getWaitMs()).isEqualTo(BidirectionalTopicClient.WAIT_MS);
    }

    /**
     * Verifies that awaitReceipt() returns {@code true}.
     *
     * @throws InterruptedException if interrupted while waiting for the thread to
     *         terminate
     */
    private void verifyReceipt() throws InterruptedException {
        assertThat(context.join()).isTrue();
        assertThat(context.result).isTrue();
        assertThat(context.exception).isNull();

        verify(source).unregister(any());
    }

    /**
     * Verifies that awaitReceipt() returns {@code false}.
     *
     * @throws InterruptedException if interrupted while waiting for the thread to
     *         terminate
     */
    private void verifyNoReceipt() throws InterruptedException {
        assertThat(context.join()).isTrue();
        assertThat(context.result).isFalse();
        assertThat(context.exception).isNull();

        verify(source).unregister(any());
    }

    /**
     * Injects a message into the source topic.
     *
     * @param message message to be injected
     * @throws CoderException if the message cannot be encoded
     */
    private void inject(MyMessage message) throws CoderException {
        inject(coder.encode(message));
    }

    /**
     * Injects a message into the source topic.
     *
     * @param message message to be injected
     */
    private void inject(String message) {
        ArgumentCaptor<TopicListener> cap = ArgumentCaptor.forClass(TopicListener.class);
        verify(source).register(cap.capture());

        cap.getValue().onTopicEvent(SOURCE_INFRA, SOURCE_TOPIC, message);
    }


    /**
     * BidirectionalTopicClient with some overrides.
     */
    private class BidirectionalTopicClient2 extends BidirectionalTopicClient {

        public BidirectionalTopicClient2(String sinkTopic, String sourceTopic)
                        throws BidirectionalTopicClientException {
            super(sinkTopic, sourceTopic);
        }

        @Override
        protected TopicEndpoint getTopicEndpointManager() {
            return endpoint;
        }
    }

    private class Context {
        private Thread thread;
        private boolean result;
        private Exception exception;
        private boolean forceDecodeFailure;

        // released every time the checker publishes a message
        private final Semaphore sendSem = new Semaphore(0);

        // released every time a message-decode fails
        private final Semaphore decodeFailedSem = new Semaphore(0);

        private final BidirectionalTopicClient2 checker;

        public Context() throws BidirectionalTopicClientException {

            checker = new BidirectionalTopicClient2(SINK_TOPIC, SOURCE_TOPIC) {

                @Override
                protected long getWaitMs() {
                    return SHORT_WAIT_MS;
                }

                @Override
                public boolean send(String messageText) {
                    boolean result = super.send(messageText);
                    sendSem.release();
                    return result;
                }

                @Override
                protected <T> T decode(String msg, Class<? extends T> clazz) throws CoderException {
                    if (forceDecodeFailure) {
                        throw new CoderException("expected exception");
                    }

                    return super.decode(msg, clazz);
                }

                @Override
                protected void decodeFailed() {
                    super.decodeFailed();
                    decodeFailedSem.release();
                }
            };
        }

        /**
         * Starts the thread.
         *
         * @param message message to be sent to the sink topic
         */
        public void start(MyMessage message) {
            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        result = checker.awaitReady(message);
                    } catch (Exception e) {
                        exception = e;
                    }
                }
            };
            thread.setDaemon(true);
            thread.start();
        }

        public void stop() {
            checker.stopWaiting();
        }

        public boolean join() throws InterruptedException {
            thread.join(MAX_WAIT_MS);
            return !thread.isAlive();
        }

        public void interrupt() {
            thread.interrupt();
        }

        public boolean awaitSend(int npermits) throws InterruptedException {
            return sendSem.tryAcquire(npermits, MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        }

        public boolean awaitDecodeFailure() throws InterruptedException {
            return decodeFailedSem.tryAcquire(MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyMessage {
        private String text;
    }
}
