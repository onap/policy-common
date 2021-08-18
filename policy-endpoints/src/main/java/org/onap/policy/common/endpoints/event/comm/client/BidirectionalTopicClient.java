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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "bidirectional" topic, which is a pair of topics, one of which is used to publish
 * requests and the other to receive responses.
 */
@Getter
public class BidirectionalTopicClient {
    private static final Logger logger = LoggerFactory.getLogger(BidirectionalTopicClient.class);
    private static final Coder coder = new StandardCoder();

    public static final long WAIT_MS = 4000L;

    private final String sinkTopic;
    private final String sourceTopic;
    private final TopicSink sink;
    private final TopicSource source;
    private final CommInfrastructure sinkTopicCommInfrastructure;
    private final CommInfrastructure sourceTopicCommInfrastructure;

    /**
     * Used when checking whether or not a message sent on the sink topic can be received
     * on the source topic. When a matching message is received on the incoming topic,
     * {@code true} is placed on the queue. If {@link #stop()} is called or the waiting
     * thread is interrupted, then {@code false} is placed on the queue. Whenever a value
     * is pulled from the queue, it is immediately placed back on the queue.
     */
    private final BlockingDeque<Boolean> checkerQueue = new LinkedBlockingDeque<>();


    /**
     * Constructs the object.
     *
     * @param sinkTopic sink topic name
     * @param sourceTopic source topic name
     * @throws BidirectionalTopicClientException if either topic does not exist
     */
    public BidirectionalTopicClient(String sinkTopic, String sourceTopic) throws BidirectionalTopicClientException {
        this.sinkTopic = sinkTopic;
        this.sourceTopic = sourceTopic;

        // init sinkClient
        List<TopicSink> sinks = getTopicEndpointManager().getTopicSinks(sinkTopic);
        if (sinks.isEmpty()) {
            throw new BidirectionalTopicClientException("no sinks for topic: " + sinkTopic);
        } else if (sinks.size() > 1) {
            throw new BidirectionalTopicClientException("too many sinks for topic: " + sinkTopic);
        }

        this.sink = sinks.get(0);

        // init source
        List<TopicSource> sources = getTopicEndpointManager().getTopicSources(Arrays.asList(sourceTopic));
        if (sources.isEmpty()) {
            throw new BidirectionalTopicClientException("no sources for topic: " + sourceTopic);
        } else if (sources.size() > 1) {
            throw new BidirectionalTopicClientException("too many sources for topic: " + sourceTopic);
        }

        this.source = sources.get(0);

        this.sinkTopicCommInfrastructure = sink.getTopicCommInfrastructure();
        this.sourceTopicCommInfrastructure = source.getTopicCommInfrastructure();
    }

    public boolean send(String message) {
        return sink.send(message);
    }

    public void register(TopicListener topicListener) {
        source.register(topicListener);
    }

    public boolean offer(String event) {
        return source.offer(event);
    }

    public void unregister(TopicListener topicListener) {
        source.unregister(topicListener);
    }

    /**
     * Determines whether or not the topic is ready (i.e., {@link #awaitReady(Object)} has
     * previously returned {@code true}).
     *
     * @return {@code true}, if the topic is ready to send and receive
     */
    public boolean isReady() {
        return Boolean.TRUE.equals(checkerQueue.peek());
    }

    /**
     * Waits for the bidirectional topic to become "ready" by publishing a message on the
     * sink topic and awaiting receipt of the message on the source topic. If the message
     * is not received within a few seconds, then it tries again. This process is
     * continued until the message is received, {@link #stop()} is called, or this thread
     * is interrupted. Once this returns, subsequent calls will return immediately, always
     * with the same value.
     *
     * @param message message to be sent to the sink topic. Note: the equals() method must
     *        return {@code true} if and only if two messages are the same
     * @return {@code true} if the message was received from the source topic,
     *         {@code false} if this method was stopped or interrupted before receipt of
     *         the message
     * @throws CoderException if the message cannot be encoded
     */
    public synchronized <T> boolean awaitReady(T message) throws CoderException {
        // see if we already know the answer
        if (!checkerQueue.isEmpty()) {
            return checkerQueue.peek();
        }

        final String messageText = coder.encode(message);

        // class of message to be decoded
        @SuppressWarnings("unchecked")
        final Class<? extends T> clazz = (Class<? extends T>) message.getClass();

        // create a listener to detect when a matching message is received
        final TopicListener listener = (infra, topic, msg) -> {
            try {
                T incoming = decode(msg, clazz);

                if (message.equals(incoming)) {
                    logger.info("topic {} is ready; found matching message {}", topic, incoming);
                    checkerQueue.add(Boolean.TRUE);
                }

            } catch (CoderException e) {
                logger.warn("cannot decode message from topic {}", topic, e);
                decodeFailed();
            }
        };

        source.register(listener);

        // loop until the message is received
        try {
            Boolean result;
            do {
                send(messageText);
            } while ((result = checkerQueue.poll(getWaitMs(), TimeUnit.MILLISECONDS)) == null);

            // put it back on the queue
            checkerQueue.add(result);

        } catch (InterruptedException e) {
            logger.error("interrupted waiting for topic sink {} source {}", sink.getTopic(), source.getTopic(), e);
            Thread.currentThread().interrupt();
            checkerQueue.add(Boolean.FALSE);

        } finally {
            source.unregister(listener);
        }

        return checkerQueue.peek();
    }

    /**
     * Stops any listeners that are currently stuck in {@link #awaitReady(Object)} by
     * adding {@code false} to the queue.
     */
    public void stopWaiting() {
        checkerQueue.add(Boolean.FALSE);
    }

    // these may be overridden by junit tests

    protected TopicEndpoint getTopicEndpointManager() {
        return TopicEndpointManager.getManager();
    }

    protected <T> T decode(String msg, Class<? extends T> clazz) throws CoderException {
        return coder.decode(msg, clazz);
    }

    protected void decodeFailed() {
        // already logged - nothing else to do
    }

    /**
     * Determines how long to wait for the message before re-sending it.
     *
     * @return time, in milliseconds, to wait
     */
    protected long getWaitMs() {
        return WAIT_MS;
    }
}
