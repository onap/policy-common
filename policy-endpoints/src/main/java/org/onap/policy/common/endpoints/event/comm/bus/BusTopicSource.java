/*-
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm.bus;

import org.onap.policy.common.endpoints.event.comm.TopicSource;

/**
 * Generic Topic Source for UEB/DMAAP Communication Infrastructure.
 *
 */
public interface BusTopicSource extends ApiKeyEnabled, TopicSource {

    /**
     * Gets the consumer group.
     *
     * @return consumer group
     */
    public String getConsumerGroup();

    /**
     * Gets the consumer instance.
     *
     * @return consumer instance
     */
    public String getConsumerInstance();

    /**
     * Gets the fetch timeout.
     *
     * @return fetch timeout
     */
    public int getFetchTimeout();

    /**
     * Gets the fetch limit.
     *
     * @return fetch limit
     */
    public int getFetchLimit();
}
