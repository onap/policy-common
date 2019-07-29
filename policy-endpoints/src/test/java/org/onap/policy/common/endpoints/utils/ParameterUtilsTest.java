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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import org.junit.Test;
import org.onap.policy.common.endpoints.parameters.CommonTestData;
import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Class to perform unit test of {@link ParameterUtils}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class ParameterUtilsTest {
    private static final String SOURCE_TOPICS_POLICY_PDP_PAP1 = ".source.topics.POLICY-PDP-PAP1.";
    private static final String SINK_TOPICS_POLICY_PDP_PAP1 = ".sink.topics.POLICY-PDP-PAP1.";
    private static final String SERVERS = ".servers";
    private static CommonTestData testData = new CommonTestData();
    private static final Coder coder = new StandardCoder();

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
        ParameterUtils.updateTopicProperties(topicProperties, "source", CommonTestData.TOPIC_PARAMS.get(0));
        assertEquals(CommonTestData.TOPIC_NAME,
            topicProperties.getProperty(CommonTestData.TOPIC_INFRA + ".source.topics"));
        assertEquals(CommonTestData.TOPIC_SERVER, topicProperties
            .getProperty(CommonTestData.TOPIC_INFRA + ".source.topics." + CommonTestData.TOPIC_NAME + SERVERS));
        ParameterUtils.updateTopicProperties(topicProperties, "sink", CommonTestData.TOPIC_PARAMS.get(0));
        assertEquals(CommonTestData.TOPIC_NAME,
            topicProperties.getProperty(CommonTestData.TOPIC_INFRA + ".sink.topics"));
        assertEquals(CommonTestData.TOPIC_SERVER, topicProperties
            .getProperty(CommonTestData.TOPIC_INFRA + ".sink.topics." + CommonTestData.TOPIC_NAME + SERVERS));
    }

    @Test
    public void testGetTopicProperties_all_props() throws Exception {
        String json = testData.getParameterGroupAsString(
            "src/test/resources/org/onap/policy/common/endpoints/parameters/TopicParameters_all_params.json");
        TopicParameterGroup topicParameterGroup = coder.decode(json, TopicParameterGroup.class);
        final GroupValidationResult result = topicParameterGroup.validate();
        assertNull(result.getResult());
        assertTrue(result.isValid());
        Properties topicProperties = ParameterUtils.getTopicProperties(topicParameterGroup);
        assertEquals("true", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SOURCE_TOPICS_POLICY_PDP_PAP1 + "managed"));
        assertEquals("true", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SINK_TOPICS_POLICY_PDP_PAP1 + "managed"));
        assertEquals("123", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SOURCE_TOPICS_POLICY_PDP_PAP1 + "port"));
        assertEquals("123", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SINK_TOPICS_POLICY_PDP_PAP1 + "port"));
        assertEquals("my-api-key", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SOURCE_TOPICS_POLICY_PDP_PAP1 + "apiKey"));
        assertEquals("my-effective-topic", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SOURCE_TOPICS_POLICY_PDP_PAP1 + "effectiveTopic"));
        assertEquals("true", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SOURCE_TOPICS_POLICY_PDP_PAP1 + "useHttps"));
        assertEquals("username", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SOURCE_TOPICS_POLICY_PDP_PAP1 + "userName"));
        assertEquals("password", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SOURCE_TOPICS_POLICY_PDP_PAP1 + "password"));
        assertEquals("true", topicProperties.getProperty(
            CommonTestData.TOPIC_INFRA + SOURCE_TOPICS_POLICY_PDP_PAP1 + "allowSelfSignedCerts"));
    }
}
