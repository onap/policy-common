/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class holds the result of the validation of a map of parameter groups.
 */
public class GroupMapValidationResult extends CommonGroupValidationResult {
    // The name of the parameter group map
    final String mapParameterName;

    /**
     * Constructor, create the group map validation result.
     *
     * @param field the map parameter field
     * @param mapObject the value of the map parameter field
     */
    protected GroupMapValidationResult(final Field field, final Object mapObject) {
        super(ParameterConstants.PARAMETER_GROUP_MAP_HAS_STATUS_MESSAGE);

        this.mapParameterName = field.getName();

        // Cast the map object to a map of parameter groups keyed by string, we can't type check maps
        // due to restrictions on generics so we have to check each entry key is a string and each entry
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
     * Gets the name of the parameter being validated.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return mapParameterName;
    }

    /**
     * Set the validation result on a parameter map entry.
     *
     * @param entryName The name of the parameter map entry
     * @param status The validation status for the entry
     * @param message The validation message for the entry
     */
    public void setResult(final String entryName, final ValidationStatus status, final String message) {
        ValidationResult validationResult = validationResultMap.get(entryName);
        if (validationResult == null) {
            throw new ParameterRuntimeException("no entry with name \"" + entryName + "\" exists");
        }

        // Set the status of the parameter group and replace the field result
        validationResult.setResult(status, message);
        this.setResult(status);
    }


    /**
     * Set the validation result on a parameter map entry.
     *
     * @param entryName The name of the parameter map entry
     * @param mapEntryValidationResult The validation result for the entry
     */
    public void setResult(final String entryName, final ValidationResult mapEntryValidationResult) {
        ValidationResult validationResult = validationResultMap.get(entryName);
        if (validationResult == null) {
            throw new ParameterRuntimeException("no entry with name \"" + entryName + "\" exists");
        }

        // Set the status of the parameter group and replace the field result
        validationResultMap.put(entryName, mapEntryValidationResult);
        this.setResult(mapEntryValidationResult.getStatus());
    }

    @Override
    protected void addGroupTypeName(StringBuilder result) {
        result.append("parameter group map \"");
        result.append(mapParameterName);
        result.append("\" ");
    }
}