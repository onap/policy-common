/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024-2025 Nordix Foundation
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

package org.onap.policy.common.utils.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Class to perform unit test of ParameterValidationUtils.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@ericsson.com)
 */
class TestParameterValidationUtils {

    @Test
    void testValidateStringParameter() {
        assertTrue(ParameterValidationUtils.validateStringParameter("Policy"));
        assertFalse(ParameterValidationUtils.validateStringParameter(null));
        assertFalse(ParameterValidationUtils.validateStringParameter(""));
    }

    @Test
    void testValidateIntParameter() {
        assertTrue(ParameterValidationUtils.validateIntParameter(5555));
        assertFalse(ParameterValidationUtils.validateIntParameter(0));
        assertFalse(ParameterValidationUtils.validateIntParameter(-1));
    }

    @Test
    void testValidateLongParameter() {
        assertTrue(ParameterValidationUtils.validateLongParameter(5555L));
        assertFalse(ParameterValidationUtils.validateLongParameter(0L));
        assertFalse(ParameterValidationUtils.validateLongParameter(-1L));
    }
}
