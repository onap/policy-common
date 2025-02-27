/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017, 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

package org.onap.policy.common.message.bus.event.base;

import org.onap.policy.common.message.bus.event.TopicSink;

/**
 * Topic Sink over Bus Infrastructure (KAFKA).
 */
public interface BusTopicSink extends ApiKeyEnabled, TopicSink {

    /**
     * Sets the partition key for published messages.
     *
     * @param partitionKey the partition key
     */
    void setPartitionKey(String partitionKey);

    /**
     * Return the partition key in used by the system to publish messages.
     *
     * @return the partition key
     */
    String getPartitionKey();
}
