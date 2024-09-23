/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.onap.policy.common.message.bus.event.Topic.CommInfrastructure;

/**
 * Listener for messages of a certain type.
 *
 * @param <T> type of message/POJO this handles
 */
@FunctionalInterface
public interface TypedMessageListener<T> {

    /**
     * Handles receipt of a message.
     *
     * @param infra infrastructure with which the message was received
     * @param topic topic on which the message was received
     * @param message message that was received
     */
    void onTopicEvent(CommInfrastructure infra, String topic, T message);
}
