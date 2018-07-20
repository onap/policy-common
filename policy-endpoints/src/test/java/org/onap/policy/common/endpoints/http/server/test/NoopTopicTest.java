/*-
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.http.server.test;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.NoopTopicSinkFactory;
import org.onap.policy.common.endpoints.event.comm.bus.impl.IndexedNoopTopicSinkFactory;
import org.onap.policy.common.endpoints.event.comm.impl.ProxyTopicEndpointManager;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NOOP Endpoint Tests
 */
public class NoopTopicTest implements TopicListener {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(NoopTopicTest.class);

    private final String topicName = "junit-noop";
    private final String outMessage = "blah";
    private String inMessage = null;

    @Test
    public void testNoopEndpoint() {
        logger.info("-- testNoopEndpoint() --");

        Properties noopSinkProperties = new Properties();
        noopSinkProperties.put(PolicyEndPointProperties.PROPERTY_NOOP_SINK_TOPICS, topicName);

        List<? extends TopicSink> noopTopics =
                ProxyTopicEndpointManager.getInstance().addTopicSinks(noopSinkProperties);

        NoopTopicSinkFactory noopTopicSinkFactory = IndexedNoopTopicSinkFactory.getInstance();
        TopicSink sink = noopTopicSinkFactory.get(topicName);


        assertTrue(noopTopics.size() == 1);
        assertTrue(noopTopics.size() == noopTopicSinkFactory.inventory().size());
        assertTrue(noopTopics.get(0) == sink);
        assertTrue(sink == noopTopicSinkFactory.inventory().get(0));

        assertTrue(!sink.isAlive());

        boolean badState = false;
        try {
            sink.send(outMessage);
        } catch (IllegalStateException e) {
            badState = true;
        }
        assertTrue(badState);

        sink.start();
        assertTrue(sink.isAlive());

        sink.send(outMessage);
        assertTrue(sink.getRecentEvents().length == 1);
        assertTrue(sink.getRecentEvents()[0].equals(outMessage));
        assertTrue(this.inMessage == null);

        sink.register(this);
        sink.send(this.outMessage);
        assertTrue(outMessage.equals(this.inMessage));
        this.inMessage = null;

        sink.unregister(this);
        sink.send(this.outMessage);
        assertTrue(!outMessage.equals(this.inMessage));

        sink.stop();
        try {
            sink.send(outMessage);
        } catch (IllegalStateException e) {
            badState = true;
        }
        assertTrue(badState);

        noopTopicSinkFactory.destroy(topicName);
        assertTrue(noopTopicSinkFactory.inventory().size() == 0);
    }

    @Override
    public void onTopicEvent(CommInfrastructure commType, String topic, String event) {
        if (commType != CommInfrastructure.NOOP) {
            return;
        }

        if (topic == null || !topic.equals(topicName)) {
            return;
        }

        this.inMessage = event;
    }
}
