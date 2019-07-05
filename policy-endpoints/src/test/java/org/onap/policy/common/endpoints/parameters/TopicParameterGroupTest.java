/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.common.endpoints.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Class to perform unit test of {@link TopicParameterGroup}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class TopicParameterGroupTest {
    private static CommonTestData testData = new CommonTestData();
    private static final Coder coder = new StandardCoder();

    @Test
    public void test() {
        final TopicParameterGroup topicParameterGroup =
                testData.toObject(testData.getTopicParameterGroupMap(false), TopicParameterGroup.class);
        final GroupValidationResult validationResult = topicParameterGroup.validate();
        assertTrue(validationResult.isValid());
        assertEquals(CommonTestData.TOPIC_PARAMS, topicParameterGroup.getTopicSinks());
        assertEquals(CommonTestData.TOPIC_PARAMS, topicParameterGroup.getTopicSources());
    }

    @Test
    public void testValidate() {
        final TopicParameterGroup topicParameterGroup =
            testData.toObject(testData.getTopicParameterGroupMap(false), TopicParameterGroup.class);
        final GroupValidationResult result = topicParameterGroup.validate();
        assertNull(result.getResult());
        assertTrue(result.isValid());
    }

    @Test
    public void test_valid() throws Exception {
        String json = testData.getParameterGroupAsString(
            "src/test/resources/org/onap/policy/common/endpoints/parameters/TopicParameters_valid.json");
        TopicParameterGroup topicParameterGroup = coder.decode(json, TopicParameterGroup.class);
        final GroupValidationResult result = topicParameterGroup.validate();
        assertNull(result.getResult());
        assertTrue(result.isValid());
    }

    @Test
    public void test_invalid() throws Exception {
        String json = testData.getParameterGroupAsString(
            "src/test/resources/org/onap/policy/common/endpoints/parameters/TopicParameters_invalid.json");
        TopicParameterGroup topicParameterGroup = coder.decode(json, TopicParameterGroup.class);
        final GroupValidationResult result = topicParameterGroup.validate();
        assertFalse(result.isValid());
        assertTrue(result.getResult().contains("parameter group has status INVALID"));
    }
}
