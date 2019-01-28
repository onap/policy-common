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

package org.onap.policy.common.endpoints.event.comm;

import java.util.List;

import org.onap.policy.common.capabilities.Lockable;
import org.onap.policy.common.capabilities.Startable;


/**
 * Essential Topic Data.
 */
public interface Topic extends TopicRegisterable, Startable, Lockable {

    /**
     * Underlying Communication infrastructure Types.
     */
    public enum CommInfrastructure {
        /**
         * UEB Communication Infrastructure.
         */
        UEB,
        /**
         * DMAAP Communication Infrastructure.
         */
        DMAAP,
        /**
         * NOOP for internal use only.
         */
        NOOP,
        /**
         * REST Communication Infrastructure.
         */
        REST
    }

    /**
     * Gets the topic name.
     *
     * @return topic name
     */
    public String getTopic();

    /**
     * Gets the communication infrastructure type.
     *
     * @return CommInfrastructure object
     */
    public CommInfrastructure getTopicCommInfrastructure();

    /**
     * Return list of servers.
     *
     * @return bus servers
     */
    public List<String> getServers();

    /**
     * Get the more recent events in this topic entity.
     *
     * @return list of most recent events
     */
    public String[] getRecentEvents();

}
