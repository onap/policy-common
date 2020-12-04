/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

package org.onap.policy.common.parameters;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class holds the result of the validation of a parameter group.
 */
public abstract class CommonGroupValidationResult implements ValidationResult {


    private final String messagePrefix;

    /**
     * Validation status for the entire class.
     */
    protected ValidationStatus status = ValidationStatus.CLEAN;

    /**
     * Status message.
     */
    protected String message;

    /**
     * Validation results for each parameter in the group.
     */
    protected final Map<String, ValidationResult> validationResultMap = new LinkedHashMap<>();


    /**
     * Constructs the object.
     *
     * @param messagePrefix status message prefix
     */
    protected CommonGroupValidationResult(String messagePrefix) {
        this.messagePrefix = messagePrefix;
        this.message = messagePrefix + status.toString();
    }

    /**
     * Gets the status of validation.
     *
     * @return the status
     */
    @Override
    public ValidationStatus getStatus() {
        return status;
    }

    /**
     * Set the validation result on a parameter group.
     *
     * @param status The validation status the parameter group is receiving
     * @param message The validation message explaining the validation status
     */
    @Override
    public void setResult(ValidationStatus status, String message) {
        setResult(status);
        this.message = message;
    }

    /**
     * Set the validation result on a parameter group. On a sequence of calls, the most
     * serious validation status is recorded, assuming the status enum ordinal increase in
     * order of severity
     *
     * @param status The validation status the parameter group is receiving
     */
    public void setResult(final ValidationStatus status) {
        if (this.status.ordinal() < status.ordinal()) {
            this.status = status;
            this.message = messagePrefix + status;
        }
    }

    /**
     * Gets the validation result.
     *
     * @param initialIndentation the indentation to use on the main result output
     * @param subIndentation the indentation to use on sub parts of the result output
     * @param showClean output information on clean fields
     * @return the result
     */
    @Override
    public String getResult(final String initialIndentation, final String subIndentation, final boolean showClean) {
        if (status == ValidationStatus.CLEAN && !showClean) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        result.append(initialIndentation);

        addGroupTypeName(result);

        result.append(status);
        result.append(", ");
        result.append(message);
        result.append('\n');

        for (ValidationResult fieldResult : validationResultMap.values()) {
            String msg = fieldResult.getResult(initialIndentation + subIndentation, subIndentation, showClean);
            if (msg != null) {
                result.append(msg);
            }
        }

        return result.toString();
    }

    /**
     * Adds the group type and name to the result string.
     *
     * @param result result string
     */
    protected abstract void addGroupTypeName(StringBuilder result);
}
