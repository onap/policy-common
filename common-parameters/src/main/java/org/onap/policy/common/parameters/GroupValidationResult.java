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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class holds the result of the validation of a parameter group.
 */
public class GroupValidationResult implements ValidationResult {
    // The parameter group which the validation result applies
    private final AbstractParameterGroup parameterGroup;

    // Validation status for the entire parameter class
    private ValidationStatus status = ValidationStatus.CLEAN;
    private String message = ParameterConstants.PARAMETER_GROUP_HAS_STATUS_MESSAGE + status.toString();

    // Validation results for each parameter in the group
    private final Map<String, ValidationResult> validationResultMap = new LinkedHashMap<>();

    /**
     * Constructor, create the field validation result with default arguments.
     *
     * @param parameterGroup the parameter group being validated
     */
    public GroupValidationResult(final AbstractParameterGroup parameterGroup) {
        this.parameterGroup = parameterGroup;

        // Add a validation result per field
        for (Field field : parameterGroup.getClass().getDeclaredFields()) {
            // Exclude system fields
            if (field.getName().startsWith("$") || field.getName().startsWith("_")) {
                continue;
            }

            try {
                boolean savedAccessibilityValue = field.isAccessible();
                field.setAccessible(true);
                // Nested maps of parameter groups are allowed
                if (AbstractParameterGroup.class.isAssignableFrom((field.getType()))) {
                    validationResultMap.put(field.getName(),
                                    new GroupValidationResult((AbstractParameterGroup) field.get(parameterGroup)));
                }
                // Nested maps of parameter groups are allowed
                else if (field.get(parameterGroup) != null && Map.class.isAssignableFrom((field.getType()))) {
                    checkMapIsParameterGroupMap(field.getName(), field.get(parameterGroup));
                    validationResultMap.put(field.getName(),
                                    new GroupMapValidationResult(field, field.get(parameterGroup)));
                }
                // Collections of parameter groups are not allowed
                else if (field.get(parameterGroup) != null && Collection.class.isAssignableFrom((field.getType()))) {
                    checkCollection4ParameterGroups(field.getName(), field.get(parameterGroup));
                    validationResultMap.put(field.getName(),
                                    new ParameterValidationResult(field, field.get(parameterGroup)));
                }
                // It's a regular parameter
                else {
                    validationResultMap.put(field.getName(),
                                    new ParameterValidationResult(field, field.get(parameterGroup)));
                }
                field.setAccessible(savedAccessibilityValue);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new ParameterRuntimeException("could not get value of parameter \"" + field.getName() + "\"", e);
            }

        }
    }

    /**
     * Check if this field is a map of parameter groups indexed by string keys.
     * 
     * @param fieldName the name of the collection field.
     * @param mapObject the map object to check
     */
    private void checkMapIsParameterGroupMap(String fieldName, Object mapObject) {
        Map<?, ?> incomingMap = (Map<?, ?>) mapObject;
        
        for (Entry<?, ?> mapEntry : incomingMap.entrySet()) {
            // Check the key is a string
            if (!String.class.isAssignableFrom(mapEntry.getKey().getClass())) {
                throw new ParameterRuntimeException("map entry is not a parameter group keyed by a string, key \""
                                + mapEntry.getKey() + "\" in map \"" + fieldName + "\" is not a string");
            }

            // Check the value is a parameter group
            if (!AbstractParameterGroup.class.isAssignableFrom(mapEntry.getValue().getClass())) {
                throw new ParameterRuntimeException("map entry is not a parameter group keyed by a string, value \""
                                + mapEntry.getValue() + "\" in map \"" + fieldName + "\" is not a parameter group");
            }
        }
    }

    /**
     * Check if this field contains parameter groups.
     * 
     * @param fieldName the name of the collection field.
     * @param collectionObject the collection object to check
     */
    private void checkCollection4ParameterGroups(final String fieldName, final Object collectionObject) {
        Collection<?> collection2Check = (Collection<?>) collectionObject;

        for (Object collectionMember : collection2Check) {
            if (AbstractParameterGroup.class.isAssignableFrom(collectionMember.getClass())) {
                throw new ParameterRuntimeException("collection parameter \"" + fieldName + "\" is illegal,"
                                + " parameter groups are not allowed as collection members");
            }
        }
    }

    /**
     * Gets the parameter group for this validation result.
     *
     * @return the parameter class
     */
    public AbstractParameterGroup getParameterGroup() {
        return parameterGroup;
    }

    /**
     * Gets the name of the parameter being validated.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return parameterGroup.getName();
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
     * Set the validation result on on a parameter group.
     * 
     * @param status The validation status the field is receiving
     * @param message The validation message explaining the validation status
     */
    @Override
    public void setResult(ValidationStatus status, String message) {
        setResult(status);
        this.message = message;
    }

    /**
     * Set the validation result on on a parameter group.
     * 
     * @param status The validation status the field is receiving
     */
    public void setResult(final ValidationStatus status) {
        // We record the most serious validation status, assuming the status enum ordinals
        // increase in order of severity
        if (this.status.ordinal() < status.ordinal()) {
            this.status = status;
            this.message = ParameterConstants.PARAMETER_GROUP_HAS_STATUS_MESSAGE + status.toString();
        }
    }

    /**
     * Set the validation result on a parameter field.
     * 
     * @param parameterName The name of the parameter field
     * @param status The validation status the field is receiving
     * @param message The validation message explaining the validation status
     */
    public void setResult(final String parameterName, final ValidationStatus status, final String message) {
        ParameterValidationResult parameterValidationResult;
        try {
            parameterValidationResult = (ParameterValidationResult) validationResultMap.get(parameterName);
        } catch (ClassCastException e) {
            throw new ParameterRuntimeException("parameter not a regular parameter: " + parameterName, e);
        }

        if (parameterValidationResult == null) {
            throw new ParameterRuntimeException("no regular parameter field exists for parameter: " + parameterName);
        }

        // Set the status of the parameter group and the field
        parameterValidationResult.setResult(status, message);
        this.setResult(status);
    }

    /**
     * Set the validation result on a nested parameter group.
     * 
     * @param parameterName The name of the parameter field
     * @param nestedValidationResult The validation result from a nested field
     */
    public void setResult(String parameterName, ValidationResult nestedValidationResult) {
        GroupValidationResult groupValidationResult;
        try {
            groupValidationResult = (GroupValidationResult) validationResultMap.get(parameterName);
        } catch (ClassCastException e) {
            throw new ParameterRuntimeException("parameter is not a nested group parameter: " + parameterName, e);
        }

        if (groupValidationResult == null) {
            throw new ParameterRuntimeException("no nested parameter field exists for parameter: " + parameterName);
        }

        // Set the status of the parameter group and replace the field result
        validationResultMap.put(parameterName, nestedValidationResult);
        this.setResult(status);
    }

    /**
     * Set the validation result on a nested parameter group map entry.
     * 
     * @param parameterName The name of the parameter field
     * @param key The key of the map entry
     * @param nestedMapValidationResult The validation result from a nested map entry
     */
    public void setResult(final String parameterName, final String key,
                    final ValidationResult nestedMapValidationResult) {
        GroupMapValidationResult groupMapValidationResult;
        try {
            groupMapValidationResult = (GroupMapValidationResult) validationResultMap.get(parameterName);
        } catch (ClassCastException e) {
            throw new ParameterRuntimeException("parameter is not a nested group map parameter: " + parameterName, e);
        }

        if (groupMapValidationResult == null) {
            throw new ParameterRuntimeException("no group map parameter field exists for parameter: " + parameterName);
        }

        // Set the status of the parameter group and the field
        groupMapValidationResult.setResult(key, nestedMapValidationResult);
        this.setResult(status);
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

        StringBuilder validationResultBuilder = new StringBuilder();

        validationResultBuilder.append(initialIndentation);
        validationResultBuilder.append("parameter group \"");
        validationResultBuilder.append(parameterGroup.getName());
        validationResultBuilder.append("\" type \"");
        validationResultBuilder.append(parameterGroup.getClass().getCanonicalName());
        validationResultBuilder.append("\" ");
        validationResultBuilder.append(status);
        validationResultBuilder.append(", ");
        validationResultBuilder.append(message);
        validationResultBuilder.append('\n');

        for (ValidationResult fieldResult : validationResultMap.values()) {
            String fieldResultMessage = fieldResult.getResult(initialIndentation + subIndentation, subIndentation,
                            showClean);
            if (fieldResultMessage != null) {
                validationResultBuilder.append(fieldResultMessage);
            }
        }

        return validationResultBuilder.toString();
    }
}