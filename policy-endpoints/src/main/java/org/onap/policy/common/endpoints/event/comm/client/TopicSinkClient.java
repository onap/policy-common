/*-
 * ============LICENSE_START=======================================================
 * ONAP PAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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


import java.util.List;

import lombok.Getter;

import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for sending messages to a Topic using TopicSink.
 */
public class TopicSinkClient {
    private static final Logger logger = LoggerFactory.getLogger(TopicSinkClient.class);

    /**
     * Coder used to encode messages being sent to the topic.
     */
    private static final Coder CODER = new StandardCoder();

    /**
     * Topic to which messages are published.
     */
    @Getter
    private final String topic;

    /**
     * Where messages are published.
     */
    private final TopicSink sink;

    /**
     * Constructs the object.
     *
     * @param topic topic to which messages should be published
     * @throws TopicSinkClientException if the topic does not exist
     */
    public TopicSinkClient(final String topic) throws TopicSinkClientException {
        this.topic = topic;

        final List<TopicSink> lst = getTopicSinks(topic);
        if (lst.isEmpty()) {
            throw new TopicSinkClientException("no sinks for topic: " + topic);
        }

        this.sink = lst.get(0);
    }

    /**
     * Sends a message to the topic, after encoding the message as json.
     *
     * @param message message to be encoded and sent
     * @return {@code true} if the message was successfully sent/enqueued, {@code false} otherwise
     */
    public boolean send(final Object message) {
        try {
            final String json = CODER.encode(message);
            return sink.send(json);

        } catch (RuntimeException | CoderException e) {
            logger.warn("send to {} failed because of {}", topic, e.getMessage(), e);
            return false;
        }
    }

    // the remaining methods are wrappers that can be overridden by junit tests

    /**
     * Gets the sinks for a given topic.
     *
     * @param topic the topic of interest
     * @return the sinks for the topic
     */
    protected List<TopicSink> getTopicSinks(final String topic) {
        return TopicEndpoint.manager.getTopicSinks(topic);
    }
}
