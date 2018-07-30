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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class holds the result of the validation of a map of parameter groups.
 */
public class GroupMapValidationResult implements ValidationResult {
    // The name of the parameter group map
    final String mapParameterName;
    
    // Validation status for the entire parameter class
    private ValidationStatus status = ValidationStatus.CLEAN;
    private String message = ParameterConstants.PARAMETER_GROUP_MAP_HAS_STATUS_MESSAGE + status.toString();

    // Validation results for each parameter in the group
    private final Map<String, ValidationResult> validationResultMap = new LinkedHashMap<>();

    /**
     * Constructor, create the group map validation result.
     *
     * @param field the map parameter field
     * @param mapObject the value of the map parameter field
     */
    protected GroupMapValidationResult(final Field field, final Object mapObject) {
        this.mapParameterName = field.getName();

        // Cast the map object to a map of parameter groups keyed by string, we can't type check maps
        // due to restrictions on generics so we have to check each entry key is a stgring and each entry
        // value is a parameter group
        @SuppressWarnings("unchecked")
        Map<String, ParameterGroup> parameterGroupMap = (Map<String, ParameterGroup>) mapObject;

        // Add a validation result per map entry
        for (Entry<String, ParameterGroup> parameterGroupMapEntry : parameterGroupMap.entrySet()) {
            // Create a validation status entry for the map
            validationResultMap.put(parameterGroupMapEntry.getKey(),
                            new GroupValidationResult(parameterGroupMapEntry.getValue()));
        }
    }

    /**
     * Gets the parameter group for this validation result.
     *
     * @return the parameter class
     */
    public ParameterGroup getParameterGroup() {
        return null;
    }

    /**
     * Gets the name of the parameter being validated.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return mapParameterName;
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
     * Set the validation result on a parameter map entry.
     * 
     * @param entryName The name of the parameter map entry
     * @param mapEntryValidationResult The validation result for the entry
     */
    public void setResult(String entryName, ValidationResult mapEntryValidationResult) {
        ValidationResult validationResult = validationResultMap.get(entryName);
        if (validationResult == null) {
            throw new ParameterRuntimeException("no entry with name \"" + entryName + "\" exists");
        }

        // Set the status of the parameter group and replace the field result
        validationResultMap.put(entryName, mapEntryValidationResult);
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
        validationResultBuilder.append("parameter group map \"");
        validationResultBuilder.append(mapParameterName);
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