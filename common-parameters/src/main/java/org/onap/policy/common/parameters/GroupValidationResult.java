/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;

/**
 * This class holds the result of the validation of a parameter group.
 */
public class GroupValidationResult extends CommonGroupValidationResult {
    // The parameter group which the validation result applies
    private final ParameterGroup parameterGroup;

    /**
     * Constructor, create the field validation result with default arguments.
     *
     * @param parameterGroup the parameter group being validated
     */
    public GroupValidationResult(final ParameterGroup parameterGroup) {
        super(ParameterConstants.PARAMETER_GROUP_HAS_STATUS_MESSAGE);

        this.parameterGroup = parameterGroup;

        // Parameter group definitions may be optional
        if (parameterGroup == null) {
            return;
        }

        // Add a validation result for all fields in the declared class
        for (Field field : parameterGroup.getClass().getDeclaredFields()) {
            // Check if a validation result should be added for this declared field
            if (isIncludedField(field)) {
                // Set a validation result for the field
                validationResultMap.put(field.getName(), getSetValidationResult(field, parameterGroup));
            }
        }

        // Add a validation result for protected and public fields in super classes
        for (Field field : getSuperclassFields(parameterGroup.getClass().getSuperclass())) {
            // Check if a validation result should be added for this declared field
            if (isIncludedField(field)) {
                // Set a validation result for the field
                validationResultMap.putIfAbsent(field.getName(), getSetValidationResult(field, parameterGroup));
            }
        }
    }

    /**
     * Construct a validation result for a field, updating "this" status.
     *
     * @param field The parameter field
     * @param parameterGroup The parameter group containing the field
     * @return the validation result
     * @throws Exception on accessing private fields
     */
    private ValidationResult getSetValidationResult(Field field, ParameterGroup parameterGroup) {
        ValidationResult result = getValidationResult(field, parameterGroup);
        setResult(result.getStatus());

        return result;
    }

    /**
     * Construct a validation result for a field.
     *
     * @param field The parameter field
     * @param parameterGroup The parameter group containing the field
     * @return the validation result
     * @throws Exception on accessing private fields
     */
    private ValidationResult getValidationResult(final Field field, final ParameterGroup parameterGroup) {
        final String fieldName = field.getName();
        final Class<?> fieldType = field.getType();
        final Object fieldObject = getObjectField(parameterGroup, field);

        // perform null checks
        ParameterValidationResult result = new ParameterValidationResult(field, fieldObject);
        if (!result.isValid()) {
            return result;
        }

        // Nested parameter groups are allowed
        if (ParameterGroup.class.isAssignableFrom(fieldType)) {
            if (null != fieldObject) {
                return ((ParameterGroup) fieldObject).validate();
            } else {
                return new GroupValidationResult((ParameterGroup) fieldObject);
            }
        }

        // Nested maps of parameter groups are allowed
        if (Map.class.isAssignableFrom(field.getType())) {
            checkMapIsParameterGroupMap(fieldName, fieldObject);
            return new GroupMapValidationResult(field, fieldObject);
        }

        // Collections of parameter groups are not allowed
        if (Collection.class.isAssignableFrom(field.getType())) {
            checkCollection4ParameterGroups(fieldName, fieldObject);
            return result;
        }

        // It's a regular parameter
        return result;
    }

    /**
     * Get the value of a field in an object using a getter found with reflection.
     *
     * @param targetObject The object on which to read the field value
     * @param field The name of the field
     * @return The field value
     */
    private Object getObjectField(final Object targetObject, final Field field) {
        String getterMethodName;

        // Check for Boolean fields, the convention for boolean getters is that they start with "is"
        // If the field name already starts with "is" then the getter has the field name otherwise
        // the field name is prepended with "is"
        if (boolean.class.equals(field.getType())) {
            if (field.getName().startsWith("is")) {
                getterMethodName = field.getName();
            } else {
                getterMethodName = "is" + StringUtils.capitalize(field.getName());
            }
        } else {
            getterMethodName = "get" + StringUtils.capitalize(field.getName());
        }

        // Look up the getter method for the field
        Method getterMethod;
        try {
            getterMethod = targetObject.getClass().getMethod(getterMethodName, (Class<?>[]) null);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ParameterRuntimeException("could not get getter method for parameter \"" + field.getName() + "\"",
                e);
        }

        // Invoke the getter
        try {
            return getterMethod.invoke(targetObject, (Object[]) null);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ParameterRuntimeException("error calling getter method for parameter \"" + field.getName() + "\"",
                e);
        }
    }

    /**
     * Check if this field is a map of parameter groups indexed by string keys.
     *
     * @param fieldName the name of the collection field.
     * @param mapObject the map object to check
     */
    private void checkMapIsParameterGroupMap(String fieldName, Object mapObject) {
        if (mapObject == null) {
            throw new ParameterRuntimeException("map parameter \"" + fieldName + "\" is null");
        }

        Map<?, ?> incomingMap = (Map<?, ?>) mapObject;

        for (Entry<?, ?> mapEntry : incomingMap.entrySet()) {
            // Check the key is a string
            if (!String.class.isAssignableFrom(mapEntry.getKey().getClass())) {
                throw new ParameterRuntimeException("map entry is not a parameter group keyed by a string, key \""
                    + mapEntry.getKey() + "\" in map \"" + fieldName + "\" is not a string");
            }

            // Check the value is a parameter group
            if (!ParameterGroup.class.isAssignableFrom(mapEntry.getValue().getClass())) {
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
        if (collectionObject == null) {
            throw new ParameterRuntimeException("collection parameter \"" + fieldName + "\" is null");
        }

        Collection<?> collection2Check = (Collection<?>) collectionObject;

        for (Object collectionMember : collection2Check) {
            if (ParameterGroup.class.isAssignableFrom(collectionMember.getClass())) {
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
    public ParameterGroup getParameterGroup() {
        return parameterGroup;
    }

    /**
     * Gets the name of the parameter group being validated.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return parameterGroup.getName();
    }

    /**
     * Set the validation result on a parameter in a parameter group.
     *
     * @param parameterName The name of the parameter
     * @param status The validation status the field is receiving
     * @param message The validation message explaining the validation status
     */
    public void setResult(final String parameterName, final ValidationStatus status, final String message) {
        ValidationResult validationResult = validationResultMap.get(parameterName);

        if (validationResult == null) {
            throw new ParameterRuntimeException("no parameter field exists for parameter: " + parameterName);
        }

        // Set the status and the message on the result irrespective of validation result type
        validationResult.setResult(status, message);

        // Set the status of this result
        this.setResult(status);
    }

    /**
     * Set the validation result on a nested parameter group.
     *
     * @param parameterName The name of the parameter field
     * @param nestedValidationResult The validation result from a nested field
     */
    public void setResult(final String parameterName, final ValidationResult nestedValidationResult) {
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
        this.setResult(nestedValidationResult.getStatus());
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
        this.setResult(nestedMapValidationResult.getStatus());
    }

    /**
     * Set the validation status on a group map entry.
     *
     * @param parameterName The name of the parameter field
     * @param key The key of the map entry
     * @param status The validation status of the entry
     * @param message The message for the parameter group
     */
    public void setResult(final String parameterName, final String key, final ValidationStatus status,
        final String message) {
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
        groupMapValidationResult.setResult(key, status, message);
        this.setResult(status);
    }

    @Override
    protected void addGroupTypeName(StringBuilder result) {
        result.append("parameter group \"");

        if (parameterGroup != null) {
            result.append(parameterGroup.getName());
            result.append("\" type \"");
            result.append(parameterGroup.getClass().getName());
        } else {
            result.append("UNDEFINED");
        }

        result.append("\" ");
    }


    /**
     * Check if a field should be included for validation.
     *
     * @param field the field to check for inclusion
     * @return true of the field should be included
     */
    private boolean isIncludedField(final Field field) {
        return !field.getName().startsWith("$") && !field.getName().startsWith("_")
            && !Modifier.isStatic(field.getModifiers());
    }

    /**
     * Get the public and protected fields of super classes.
     * @param firstSuperClass the first superclass to check
     *
     * @return a set of the superclass fields
     */
    private List<Field> getSuperclassFields(final Class<?> firstSuperClass) {
        List<Field> superclassFields = new ArrayList<>();

        Class<?> currentClass = firstSuperClass;
        while (currentClass.getSuperclass() != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                // Check if this field is public or protected
                if (Modifier.isPublic(field.getModifiers()) || Modifier.isProtected(field.getModifiers())) {
                    superclassFields.add(field);
                }
            }

            // Check the next super class down
            currentClass = currentClass.getSuperclass();
        }

        return superclassFields;
    }
}
