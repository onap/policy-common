/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.listeners;

import com.google.common.base.Strings;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.onap.policy.common.message.bus.event.Topic.CommInfrastructure;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dispatches messages to listeners based on the request id extracted from the message. A
 * listener may be registered for a specific request id or for messages that have no
 * request id (i.e., autonomous messages). Note: only one listener may be registered for a
 * specific request id.
 *
 * @param <T> type of message/POJO this handles
 */
public class RequestIdDispatcher<T> extends ScoListener<T> {

    private static final Logger logger = LoggerFactory.getLogger(RequestIdDispatcher.class);

    /**
     * Name of the request id field, which may be hierarchical.
     */
    private final Object[] requestIdFieldNames;

    /**
     * Listeners for autonomous messages.
     */
    private final ConcurrentLinkedQueue<TypedMessageListener<T>> listeners = new ConcurrentLinkedQueue<>();

    /**
     * Listeners for specific request ids.
     */
    private final ConcurrentHashMap<String, TypedMessageListener<T>> req2listener = new ConcurrentHashMap<>();

    /**
     * Constructs the object.
     *
     * @param clazz class of message this handles
     * @param requestIdFieldNames name of the request id field, which may be hierarchical
     */
    public RequestIdDispatcher(Class<T> clazz, String... requestIdFieldNames) {
        super(clazz);
        this.requestIdFieldNames = requestIdFieldNames;
    }

    /**
     * Registers a listener for autonomous messages.
     *
     * @param listener listener to be registered
     */
    public void register(TypedMessageListener<T> listener) {
        listeners.add(listener);
    }

    /**
     * Registers a listener for a particular request id.
     *
     * @param reqid request id of interest
     * @param listener listener to be registered
     */
    public void register(String reqid, TypedMessageListener<T> listener) {
        if (Strings.isNullOrEmpty(reqid)) {
            throw new IllegalArgumentException("attempt to register a listener with an empty request id");
        }

        req2listener.put(reqid, listener);
    }

    /**
     * Unregisters a listener for autonomous messages.
     *
     * @param listener listener to be unregistered
     */
    public void unregister(TypedMessageListener<T> listener) {
        listeners.remove(listener);
    }

    /**
     * Unregisters the listener associated with a particular request id.
     *
     * @param reqid request id whose listener is to be unregistered
     */
    public void unregister(String reqid) {
        req2listener.remove(reqid);
    }

    @Override
    public void onTopicEvent(CommInfrastructure infra, String topic, StandardCoderObject sco, T message) {

        // extract the request id
        var reqid = sco.getString(requestIdFieldNames);

        // dispatch the message
        if (Strings.isNullOrEmpty(reqid)) {
            // it's an autonomous message - offer it to all autonomous listeners
            if (listeners.isEmpty()) {
                logger.info("no listeners for autonomous message of type {}", message.getClass().getSimpleName());
            }

            for (TypedMessageListener<T> listener : listeners) {
                offerToListener(infra, topic, message, reqid, listener);
            }

        } else {
            // it's a response to a particular request
            offerToListener(infra, topic, message, reqid, req2listener.get(reqid));
        }
    }

    /**
     * Offers a message to a listener.
     *
     * @param infra infrastructure on which the message was received
     * @param topic topic on which the message was received
     * @param msg message that was received
     * @param reqid request id extracted from the message
     * @param listener listener to which the message should be offered, or {@code null}
     */
    private void offerToListener(CommInfrastructure infra, String topic, T msg, String reqid,
                    TypedMessageListener<T> listener) {

        if (listener == null) {
            logger.info("no listener for request id {}", reqid);
            return;
        }

        try {
            listener.onTopicEvent(infra, topic, msg);

        } catch (RuntimeException e) {
            logger.warn("listener {} failed to process message: {}", listener, msg, e);
        }
    }
}
