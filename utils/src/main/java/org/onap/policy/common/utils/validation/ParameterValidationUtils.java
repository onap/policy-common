/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.common.utils.validation;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class to provide utility methods for common parameter validations.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@ericsson.com)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParameterValidationUtils {

    /**
     * Validates the given string input.
     *
     * @param inputString the string to validate
     * @return the boolean validation result
     */
    public static boolean validateStringParameter(final String inputString) {
        return (inputString != null && !inputString.trim().isEmpty());
    }

    /**
     * Validates the given integer input.
     *
     * @param input the integer to validate
     * @return the boolean validation result
     */
    public static boolean validateIntParameter(final int input) {
        return (input > 0);
    }

    /**
     * Validates the given long input.
     *
     * @param input the long to validate
     * @return the boolean validation result
     */
    public static boolean validateLongParameter(final long input) {
        return (input > 0);
    }
}
