/*
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

package org.onap.policy.common.parameters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import org.onap.policy.common.parameters.annotations.Max;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;

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

        if (parameterValue == null) {
            if (getAnyAnnotation(field, NotNull.class) != null) {
                setResult(ValidationStatus.INVALID, "is null");
            }

        } else if (parameterValue instanceof String) {
            if (getAnyAnnotation(field, NotBlank.class) != null && parameterValue.toString().trim().isEmpty()) {
                setResult(ValidationStatus.INVALID, "must be a non-blank string");
            }

        } else if (parameterValue instanceof Number) {
            checkMinValue(field, parameterValue);
            checkMaxValue(field, parameterValue);
        }
    }

    /**
     * Checks the minimum value of a field, if it has the "@Min" annotation.
     *
     * @param field field whose value is being validated
     * @param parameterValue field's value
     */
    private void checkMinValue(final Field field, final Object parameterValue) {
        var minAnnot = field.getAnnotation(Min.class);
        if (minAnnot != null && ((Number) parameterValue).longValue() < minAnnot.value()) {
            setResult(ValidationStatus.INVALID, "must be >= " + minAnnot.value());
        }
    }

    /**
     * Checks the maximum value of a field, if it has the "@Max" annotation.
     *
     * @param field field whose value is being validated
     * @param parameterValue field's value
     */
    private void checkMaxValue(final Field field, final Object parameterValue) {
        var maxAnnot = field.getAnnotation(Max.class);
        if (maxAnnot != null && ((Number) parameterValue).longValue() > maxAnnot.value()) {
            setResult(ValidationStatus.INVALID, "must be <= " + maxAnnot.value());
        }
    }

    /**
     * Gets an annotation for a field, first checking the field, itself, and then checking
     * at the class level. Does not check super classes as class-level annotations should
     * only apply to the fields within the class.
     *
     * @param field field of interest
     * @param annotClass class of annotation that is desired
     * @return the field's annotation, or {@code null} if it does not exist
     */
    private static <T extends Annotation> T getAnyAnnotation(final Field field, Class<T> annotClass) {
        var annot = field.getAnnotation(annotClass);
        if (annot != null) {
            return annot;
        }

        return field.getDeclaringClass().getAnnotation(annotClass);
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
        if (this.status == ValidationStatus.CLEAN) {
            this.status = status;
            this.message = message;
        }
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

        var validationResultBuilder = new StringBuilder();

        validationResultBuilder.append(initialIndentation);
        validationResultBuilder.append("field \"");
        validationResultBuilder.append(getName());
        validationResultBuilder.append("\" type \"");
        validationResultBuilder.append(field.getType().getName());
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
