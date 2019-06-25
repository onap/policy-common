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

package org.onap.policy.common.utils.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.parameters.GroupValidationResult;

/**
 * Class to perform unit test of {@link RestServerParameters}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class RestServerParametersTest {

    private static CommonTestData testData = new CommonTestData();

    @Test
    public void test() throws Exception {
        final RestServerParameters restServerParameters =
                testData.toObject(testData.getRestServerParametersMap(false), RestServerParameters.class);
        final GroupValidationResult validationResult = restServerParameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(CommonTestData.REST_SERVER_HOST, restServerParameters.getHost());
        assertEquals(CommonTestData.REST_SERVER_PORT, restServerParameters.getPort());
        assertEquals(CommonTestData.REST_SERVER_USER, restServerParameters.getUserName());
        assertEquals(CommonTestData.REST_SERVER_PASSWORD, restServerParameters.getPassword());
        assertEquals(CommonTestData.REST_SERVER_HTTPS, restServerParameters.isHttps());
        assertEquals(CommonTestData.REST_SERVER_AAF, restServerParameters.isAaf());
    }

    @Test
    public void testValidate() throws Exception {
        final TopicParameterGroup topicParameterGroup =
            testData.toObject(testData.getTopicParameterGroupMap(false), TopicParameterGroup.class);
        final GroupValidationResult result = topicParameterGroup.validate();
        assertNull(result.getResult());
        assertTrue(result.isValid());
    }
}
