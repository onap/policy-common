/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.parameters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.parameters.annotations.Max;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;

/**
 * Bean validator, supporting the parameter annotations.
 * <p/>
 * Note: this currently does not support Min/Max validation of "short" or "byte"; these
 * annotations are simply ignored for these types.
 */
public class BeanValidator {

    /**
     * {@code True} if there is a field-level annotation, {@code false} otherwise.
     */
    private boolean fieldIsAnnotated;

    /**
     * Validates top level fields within an object. For each annotated field, it retrieves
     * the value using the public "getter" method for the field. If there is no public
     * "getter" method, then it throws an exception. Otherwise, it validates the retrieved
     * value based on the annotations. This recurses through super classes looking for
     * fields to be verified, but it does not examine any interfaces.
     *
     * @param name name of the object being validated
     * @param object object to be validated. If {@code null}, then an empty result is
     *        returned
     * @return the validation result
     */
    public BeanValidationResult validateTop(String name, Object object) {
        BeanValidationResult result = new BeanValidationResult(name, object);
        if (object == null) {
            return result;
        }

        // check class hierarchy - don't need to check interfaces
        for (Class<?> clazz = object.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            validateFields(result, object, clazz);
        }

        return result;
    }

    /**
     * Performs validation of all annotated fields found within the class.
     *
     * @param result validation results are added here
     * @param object object whose fields are to be validated
     * @param clazz class, within the object's hierarchy, to be examined for fields to be
     *        verified
     */
    private void validateFields(BeanValidationResult result, Object object, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            validateField(result, object, clazz, field);
        }
    }

    /**
     * Performs validation of a single field.
     *
     * @param result validation results are added here
     * @param object object whose fields are to be validated
     * @param clazz class, within the object's hierarchy, containing the field
     * @param field field whose value is to be validated
     */
    private void validateField(BeanValidationResult result, Object object, Class<?> clazz, Field field) {
        final String fieldName = field.getName();
        if (fieldName.contains("$")) {
            return;
        }

        /*
         * Identify the annotations. NotNull MUST be first so the check is run before the
         * others.
         */
        fieldIsAnnotated = false;
        List<Predicate<Object>> checkers = new ArrayList<>(10);
        addAnnotation(clazz, field, checkers, NotNull.class, (annot, value) -> verNotNull(result, fieldName, value));
        addAnnotation(clazz, field, checkers, NotBlank.class, (annot, value) -> verNotBlank(result, fieldName, value));
        addAnnotation(clazz, field, checkers, Max.class, (annot, value) -> verMax(result, fieldName, annot, value));
        addAnnotation(clazz, field, checkers, Min.class, (annot, value) -> verMin(result, fieldName, annot, value));

        if (checkers.isEmpty()) {
            // has no annotations - nothing to check
            return;
        }

        // verify the field type is of interest
        int mod = field.getModifiers();
        if (Modifier.isStatic(mod)) {
            classOnly(clazz.getName() + "." + fieldName + " is annotated but the field is static");
            return;
        }

        // get the field's "getter" method
        Method accessor = getAccessor(object.getClass(), fieldName);
        if (accessor == null) {
            classOnly(clazz.getName() + "." + fieldName + " is annotated but has no \"get\" method");
            return;
        }

        // get the value
        Object value = getValue(object, clazz, fieldName, accessor);

        // perform the checks
        if (value == null && field.getAnnotation(NotNull.class) == null && clazz.getAnnotation(NotNull.class) == null) {
            // value is null and there's no null check - just return
            return;
        }

        for (Predicate<Object> checker : checkers) {
            if (!checker.test(value)) {
                // invalid - don't bother with additional checks
                return;
            }
        }
    }

    /**
     * Gets the value from the object using the accessor function.
     *
     * @param object object whose value is to be retrieved
     * @param clazz class containing the field
     * @param fieldName name of the field
     * @param accessor "getter" method
     * @return the object's value
     */
    private Object getValue(Object object, Class<?> clazz, final String fieldName, Method accessor) {
        try {
            return accessor.invoke(object);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalArgumentException(clazz.getName() + "." + fieldName + " accessor threw an exception", e);
        }
    }

    /**
     * Throws an exception if there are field-level annotations.
     *
     * @param exceptionMessage exception message
     */
    private void classOnly(String exceptionMessage) {
        if (fieldIsAnnotated) {
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    /**
     * Looks for an annotation at the class or field level. If an annotation is found at
     * either the field or class level, then it adds a verifier to the list of checkers.
     *
     * @param clazz class to be searched
     * @param field field to be searched
     * @param checkers where to place the new field verifier
     * @param annotClass class of annotation to find
     * @param check verification function to be added to the list, if the annotation is
     *        found
     */
    private <T extends Annotation> void addAnnotation(Class<?> clazz, Field field, List<Predicate<Object>> checkers,
                    Class<T> annotClass, BiPredicate<T, Object> check) {

        // field annotation takes precedence over class annotation
        T annot = field.getAnnotation(annotClass);
        if (annot != null) {
            fieldIsAnnotated = true;

        } else if ((annot = clazz.getAnnotation(annotClass)) == null) {
            return;
        }

        T annot2 = annot;
        checkers.add(value -> check.test(annot2, value));
    }

    /**
     * Verifies that the value is not null.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param value value to be verified, assumed to be non-null
     * @return {@code true} if the value is valid, {@code false} otherwise
     */
    private boolean verNotNull(BeanValidationResult result, String fieldName, Object value) {
        return result.validateNotNull(fieldName, value);
    }

    /**
     * Verifies that the value is not blank.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param value value to be verified, assumed to be non-null
     * @return {@code true} if the value is valid, {@code false} otherwise
     */
    private boolean verNotBlank(BeanValidationResult result, String fieldName, Object value) {
        if (value instanceof String && StringUtils.isBlank(value.toString())) {
            ObjectValidationResult fieldResult =
                            new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID, "is blank");
            result.addResult(fieldResult);
            return false;
        }

        return true;
    }

    /**
     * Verifies that the value is <= the minimum value.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param annot annotation against which the value is being verified
     * @param value value to be verified, assumed to be non-null
     * @return {@code true} if the value is valid, {@code false} otherwise
     */
    private boolean verMax(BeanValidationResult result, String fieldName, Max annot, Object value) {
        if (!(value instanceof Number)) {
            return true;
        }

        Number num = (Number) value;
        if (num instanceof Integer || num instanceof Long) {
            if (num.longValue() <= annot.value()) {
                return true;
            }

        } else if (num instanceof Float || num instanceof Double) {
            if (num.doubleValue() <= annot.value()) {
                return true;
            }

        } else {
            return true;
        }

        ObjectValidationResult fieldResult = new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID,
                        "exceeds the maximum value: " + annot.value());
        result.addResult(fieldResult);
        return false;
    }

    /**
     * Verifies that the value is >= the minimum value.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param annot annotation against which the value is being verified
     * @param value value to be verified, assumed to be non-null
     * @return {@code true} if the value is valid, {@code false} otherwise
     */
    private boolean verMin(BeanValidationResult result, String fieldName, Min annot, Object value) {
        if (!(value instanceof Number)) {
            return true;
        }

        Number num = (Number) value;
        if (num instanceof Integer || num instanceof Long) {
            if (num.longValue() >= annot.value()) {
                return true;
            }

        } else if (num instanceof Float || num instanceof Double) {
            if (num.doubleValue() >= annot.value()) {
                return true;
            }

        } else {
            return true;
        }

        ObjectValidationResult fieldResult = new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID,
                        "is below the minimum value: " + annot.value());
        result.addResult(fieldResult);
        return false;
    }

    /**
     * Gets an accessor method for the given field.
     *
     * @param clazz class whose methods are to be searched
     * @param fieldName field whose "getter" is to be identified
     * @return the field's "getter" method, or {@code null} if it is not found
     */
    private Method getAccessor(Class<?> clazz, String fieldName) {
        String capname = StringUtils.capitalize(fieldName);
        Method accessor = getMethod(clazz, "get" + capname);
        if (accessor != null) {
            return accessor;
        }

        return getMethod(clazz, "is" + capname);
    }

    /**
     * Gets the "getter" method having the specified name.
     *
     * @param clazz class whose methods are to be searched
     * @param methodName name of the method of interest
     * @return the method, or {@code null} if it is not found
     */
    private Method getMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getMethods()) {
            if (methodName.equals(method.getName()) && validMethod(method)) {
                return method;
            }
        }

        return null;
    }

    /**
     * Determines if a method is a valid "getter".
     *
     * @param method method to be checked
     * @return {@code true} if the method is a valid "getter", {@code false} otherwise
     */
    private boolean validMethod(Method method) {
        int mod = method.getModifiers();
        return !(Modifier.isStatic(mod) || method.getReturnType() == void.class || method.getParameterCount() != 0);
    }
}
