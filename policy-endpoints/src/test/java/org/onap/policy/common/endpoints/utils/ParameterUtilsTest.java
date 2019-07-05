/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.

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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.endpoints.utils;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Properties;
import org.junit.Test;
import org.onap.policy.common.endpoints.parameters.CommonTestData;
import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;

/**
 * Class to perform unit test of {@link ParameterUtils}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class ParameterUtilsTest {
    private static final String SERVERS = ".servers";

    /**
     * Test getTopicProperties from TopicParameterGroup.
     */
    @Test
    public void testGetTopicProperties() {
        CommonTestData testData = new CommonTestData();
        final TopicParameterGroup topicParameterGroup =
            testData.toObject(testData.getTopicParameterGroupMap(false), TopicParameterGroup.class);
        Properties topicProperties = ParameterUtils.getTopicProperties(topicParameterGroup);
        assertEquals(CommonTestData.TOPIC_NAME,
            topicProperties.getProperty(CommonTestData.TOPIC_INFRA + ".source.topics"));
        assertEquals(CommonTestData.TOPIC_NAME,
            topicProperties.getProperty(CommonTestData.TOPIC_INFRA + ".sink.topics"));
        assertEquals(CommonTestData.TOPIC_SERVER, topicProperties
            .getProperty(CommonTestData.TOPIC_INFRA + ".source.topics." + CommonTestData.TOPIC_NAME + SERVERS));
        assertEquals(CommonTestData.TOPIC_SERVER, topicProperties
            .getProperty(CommonTestData.TOPIC_INFRA + ".sink.topics." + CommonTestData.TOPIC_NAME + SERVERS));
    }

    @Test
    public void testUpdateTopicProperties() {
        Properties topicProperties = new Properties();
        ParameterUtils.updateTopicProperties(topicProperties, "source", CommonTestData.TOPIC_INFRA,
            CommonTestData.TOPIC_NAME, Arrays.asList(CommonTestData.TOPIC_SERVER));
        assertEquals(CommonTestData.TOPIC_NAME,
            topicProperties.getProperty(CommonTestData.TOPIC_INFRA + ".source.topics"));
        assertEquals(CommonTestData.TOPIC_SERVER, topicProperties
            .getProperty(CommonTestData.TOPIC_INFRA + ".source.topics." + CommonTestData.TOPIC_NAME + SERVERS));
        ParameterUtils.updateTopicProperties(topicProperties, "sink", CommonTestData.TOPIC_INFRA,
            CommonTestData.TOPIC_NAME, Arrays.asList(CommonTestData.TOPIC_SERVER));
        assertEquals(CommonTestData.TOPIC_NAME,
            topicProperties.getProperty(CommonTestData.TOPIC_INFRA + ".sink.topics"));
        assertEquals(CommonTestData.TOPIC_SERVER, topicProperties
            .getProperty(CommonTestData.TOPIC_INFRA + ".sink.topics." + CommonTestData.TOPIC_NAME + SERVERS));
    }
}
