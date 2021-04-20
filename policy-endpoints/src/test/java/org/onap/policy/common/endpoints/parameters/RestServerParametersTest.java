/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.common.endpoints.parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.parameters.BeanValidator2;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Class to perform unit test of {@link RestServerParameters}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class RestServerParametersTest {

    private static CommonTestData testData = new CommonTestData();
    private static final Coder coder = new StandardCoder();

    @Test
    public void test() {
        final RestServerParameters restServerParameters =
                testData.toObject(testData.getRestServerParametersMap(false), RestServerParameters.class);
        final GroupValidationResult validationResult = restServerParameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(CommonTestData.REST_SERVER_HOST, restServerParameters.getHost());
        assertEquals(CommonTestData.REST_SERVER_PORT, restServerParameters.getPort());
        assertEquals(CommonTestData.REST_SERVER_USER, restServerParameters.getUserName());
        assertEquals(CommonTestData.REST_SERVER_PASS, restServerParameters.getPassword());
        assertEquals(CommonTestData.REST_SERVER_HTTPS, restServerParameters.isHttps());
        assertEquals(CommonTestData.REST_SERVER_AAF, restServerParameters.isAaf());

        assertThat(new BeanValidator2().validate(restServerParameters)).isNull();
    }

    @Test
    public void testValidate() {
        final RestServerParameters restServerParameters =
            testData.toObject(testData.getRestServerParametersMap(false), RestServerParameters.class);
        final GroupValidationResult result = restServerParameters.validate();
        assertNull(result.getResult());
        assertTrue(result.isValid());
    }

    @Test
    public void test_valid() throws Exception {
        String json = testData.getParameterGroupAsString(
            "src/test/resources/org/onap/policy/common/endpoints/parameters/RestServerParameters_valid.json");
        RestServerParameters restServerParameters = coder.decode(json, RestServerParameters.class);
        final GroupValidationResult result = restServerParameters.validate();
        assertNull(result.getResult());
        assertTrue(result.isValid());

        assertThat(new BeanValidator2().validate(restServerParameters)).isNull();
    }

    @Test
    public void test_invalid() throws Exception {
        String json = testData.getParameterGroupAsString(
            "src/test/resources/org/onap/policy/common/endpoints/parameters/RestServerParameters_invalid.json");
        RestServerParameters restServerParameters = coder.decode(json, RestServerParameters.class);
        final GroupValidationResult result = restServerParameters.validate();
        assertFalse(result.isValid());
        assertTrue(result.getResult().contains("parameter group has status INVALID"));

        assertThat(new BeanValidator2().validate(restServerParameters)).contains("host", "null", "must not be blank");
    }
}
