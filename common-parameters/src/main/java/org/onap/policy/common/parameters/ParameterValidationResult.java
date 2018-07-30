/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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

import java.lang.reflect.Field;

/**
 * This class holds the result of the validation of a parameter.
 */
public class ParameterValidationResult implements ValidationResult {
    // The field and value of the parameter to which the validation result applies
    private final Field field;
    private final Object parameterValue;

    // Validation status and message
    private ValidationStatus status = ValidationStatus.CLEAN;
    private String message = ParameterConstants.PARAMETER_HAS_STATUS_MESSAGE + status.toString();

    /**
     * Constructor, create validation result for a parameter with default arguments.
     *
     * @param field the parameter field
     * @param parameterValue the value of the parameter field
     */
    protected ParameterValidationResult(final Field field, final Object parameterValue) {
        this.field = field;
        this.parameterValue = parameterValue;
    }

    /**
     * Gets the name of the parameter being validated.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return field.getName();
    }

    /**
     * Gets the validation status.
     *
     * @return the validation status
     */
    @Override
    public ValidationStatus getStatus() {
        return status;
    }

    /**
     * Set the validation result on on a parameter field. 
     * @param status The validation status the field is receiving
     * @param message The validation message explaining the validation status
     */
    @Override
    public void setResult(final ValidationStatus status, final String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Gets the validation result.
     *
     * @param initialIndentation the result indentation
     * @param subIndentation the indentation to use on sub parts of the result output
     * @param showClean output information on clean fields
     * @return the result
     */
    @Override
    public String getResult(final String initialIndentation, final String subIndentation, final boolean showClean) {
        if (status == ValidationStatus.CLEAN && !showClean) {
            return null;
        }

        StringBuilder validationResultBuilder = new StringBuilder();

        validationResultBuilder.append(initialIndentation);
        validationResultBuilder.append("field \"");
        validationResultBuilder.append(getName());
        validationResultBuilder.append("\" type \"");
        validationResultBuilder.append(field.getType().getCanonicalName());
        validationResultBuilder.append("\" value \"");
        validationResultBuilder.append(parameterValue);
        validationResultBuilder.append("\" ");
        validationResultBuilder.append(getStatus());
        validationResultBuilder.append(", ");
        validationResultBuilder.append(message);
        validationResultBuilder.append('\n');

        return validationResultBuilder.toString();
    }
}
