/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2025 Nordix Foundation.
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

package org.onap.policy.common.message.bus.check;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.onap.policy.common.message.bus.event.Topic;
import org.onap.policy.common.parameters.topic.TopicParameters;

class TopicHealthCheckFactoryTest {

    @Test
    void testGetTopicHealthCheck() {
        var topicHealthCheckFactory = new TopicHealthCheckFactory();
        var param = new TopicParameters();
        param.setTopicCommInfrastructure(Topic.CommInfrastructure.NOOP.name());
        var topicHealthCheck = topicHealthCheckFactory.getTopicHealthCheck(param);
        assertNotNull(topicHealthCheck);
        param.setTopicCommInfrastructure(Topic.CommInfrastructure.KAFKA.name());
        topicHealthCheck = topicHealthCheckFactory.getTopicHealthCheck(param);
        assertNotNull(topicHealthCheck);
        param.setTopicCommInfrastructure(Topic.CommInfrastructure.REST.name());
        topicHealthCheck = topicHealthCheckFactory.getTopicHealthCheck(param);
        assertNull(topicHealthCheck);
    }
}
