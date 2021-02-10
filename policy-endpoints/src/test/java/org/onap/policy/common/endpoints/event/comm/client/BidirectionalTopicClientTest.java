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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Properties;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;

public class BidirectionalTopicClientTest {
    private static final String SINK_TOPIC = "my-sink-topic";
    private static final String SOURCE_TOPIC = "my-source-topic";

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

    private BidirectionalTopicClient client;

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
        MockitoAnnotations.initMocks(this);

        when(sink.send(anyString())).thenReturn(true);
        when(sink.getTopicCommInfrastructure()).thenReturn(SINK_INFRA);

        when(source.offer(anyString())).thenReturn(true);
        when(source.getTopicCommInfrastructure()).thenReturn(SOURCE_INFRA);

        when(endpoint.getTopicSinks(anyString())).thenReturn(Arrays.asList());
        when(endpoint.getTopicSinks(SINK_TOPIC)).thenReturn(Arrays.asList(sink));

        when(endpoint.getTopicSources(any())).thenReturn(Arrays.asList());
        when(endpoint.getTopicSources(Arrays.asList(SOURCE_TOPIC))).thenReturn(Arrays.asList(source));

        client = new BidirectionalTopicClient2(SINK_TOPIC, SOURCE_TOPIC);
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
}
