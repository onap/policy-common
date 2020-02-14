/*-
 * ============LICENSE_START=======================================================
 * ONAP PAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.TopicSink;

public class TopicSinkClientTest {
    private static final String TOPIC = "my-topic";

    @Mock
    private TopicSink sink;
    @Mock
    private TopicEndpoint endpoint;

    private TopicSinkClient client;
    private List<TopicSink> sinks;

    /**
     * Creates mocks and an initial client object.
     *
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(sink.send(anyString())).thenReturn(true);

        sinks = Arrays.asList(sink, null);
        when(endpoint.getTopicSinks(TOPIC)).thenReturn(sinks);

        client = new TopicSinkClient2(TOPIC);

        Properties props = new Properties();
        props.setProperty("noop.sink.topics", TOPIC);

        // clear all topics and then configure one topic
        TopicEndpointManager.getManager().shutdown();
        TopicEndpointManager.getManager().addTopicSinks(props);
    }

    @AfterClass
    public static void tearDown() {
        // clear all topics after the tests
        TopicEndpointManager.getManager().shutdown();
    }

    /**
     * Uses a real NO-OP topic sink.
     */
    @Test
    public void testGetTopicSinks() throws Exception {

        sink = TopicEndpointManager.getManager().getNoopTopicSink(TOPIC);
        assertNotNull(sink);

        final AtomicReference<String> evref = new AtomicReference<>(null);

        sink.register((infra, topic, event) -> evref.set(event));
        sink.start();

        client = new TopicSinkClient(TOPIC);
        client.send(100);

        assertEquals("100", evref.get());
    }

    @Test
    public void testTopicSinkClient() {
        // unknown topic -> should throw exception
        when(endpoint.getTopicSinks(TOPIC)).thenReturn(Collections.emptyList());
        assertThatThrownBy(() -> new TopicSinkClient2(TOPIC)).isInstanceOf(TopicSinkClientException.class)
                        .hasMessage("no sinks for topic: my-topic");
    }

    @Test
    public void testTopicSinkClient_GetTopic() throws TopicSinkClientException {
        assertEquals(TOPIC, new TopicSinkClient(TopicEndpointManager.getManager().getNoopTopicSink(TOPIC)).getTopic());
        assertEquals(TOPIC, new TopicSinkClient(TOPIC).getTopic());

        assertThatThrownBy(() -> new TopicSinkClient((TopicSink) null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TopicSinkClient("blah")).isInstanceOf(TopicSinkClientException.class)
                        .hasMessage("no sinks for topic: blah");
    }

    @Test
    public void testSend() {
        client.send(Arrays.asList("abc", "def"));
        verify(sink).send("['abc','def']".replace('\'', '"'));

        // sink send fails
        when(sink.send(anyString())).thenReturn(false);
        assertFalse(client.send("ghi"));

        // sink send throws an exception
        final RuntimeException ex = new RuntimeException("expected exception");
        when(sink.send(anyString())).thenThrow(ex);
        assertFalse(client.send("jkl"));
    }

    /**
     * TopicSinkClient with some overrides.
     */
    private class TopicSinkClient2 extends TopicSinkClient {

        public TopicSinkClient2(final String topic) throws TopicSinkClientException {
            super(topic);
        }

        @Override
        protected TopicEndpoint getTopicEndpointManager() {
            return endpoint;
        }
    }
}
