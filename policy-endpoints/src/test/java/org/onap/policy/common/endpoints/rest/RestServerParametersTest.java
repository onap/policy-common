/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.rest.RestServerParameters;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Class to perform unit test of {@link RestServerParameters}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
class RestServerParametersTest {

    private static final CommonTestData testData = new CommonTestData();
    private static final Coder coder = new StandardCoder();

    @Test
    void test() {
        final RestServerParameters restServerParameters =
                testData.toObject(testData.getRestServerParametersMap(false), RestServerParameters.class);
        final ValidationResult validationResult = restServerParameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(CommonTestData.REST_SERVER_HOST, restServerParameters.getHost());
        assertEquals(CommonTestData.REST_SERVER_PORT, restServerParameters.getPort());
        assertEquals(CommonTestData.REST_SERVER_USER, restServerParameters.getUserName());
        assertEquals(CommonTestData.REST_SERVER_PASS, restServerParameters.getPassword());
        assertEquals(CommonTestData.REST_SERVER_HTTPS, restServerParameters.isHttps());
        assertEquals(CommonTestData.REST_SERVER_AAF, restServerParameters.isAaf());
    }

    @Test
    void testValidate() {
        final RestServerParameters restServerParameters =
            testData.toObject(testData.getRestServerParametersMap(false), RestServerParameters.class);
        final ValidationResult result = restServerParameters.validate();
        assertNull(result.getResult());
        assertTrue(result.isValid());
    }

    @Test
    void test_valid() throws Exception {
        String json = testData.getParameterGroupAsString(
            "src/test/resources/org/onap/policy/common/endpoints/rest/RestServerParameters_valid.json");
        RestServerParameters restServerParameters = coder.decode(json, RestServerParameters.class);
        final ValidationResult result = restServerParameters.validate();
        assertNull(result.getResult());
        assertTrue(result.isValid());
    }

    @Test
    void test_invalid() throws Exception {
        String json = testData.getParameterGroupAsString(
            "src/test/resources/org/onap/policy/common/endpoints/rest/RestServerParameters_invalid.json");
        RestServerParameters restServerParameters = coder.decode(json, RestServerParameters.class);
        final ValidationResult result = restServerParameters.validate();
        assertFalse(result.isValid());
        assertThat(result.getResult()).contains("item has status INVALID");
    }
}
