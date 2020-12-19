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
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.parameters.annotations.Entries;
import org.onap.policy.common.parameters.annotations.Items;
import org.onap.policy.common.parameters.annotations.Max;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Pattern;
import org.onap.policy.common.parameters.annotations.Valid;

/**
 * Bean validator, supporting the parameter annotations.
 */
public class BeanValidator {

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
     * Adds validators based on the annotations that are available.
     *
     * @param validator where to add the validators
     */
    protected void addValidators(ValueValidator validator) {
        validator.addAnnotation(NotNull.class, this::verNotNull);
        validator.addAnnotation(NotBlank.class, this::verNotBlank);
        validator.addAnnotation(Max.class, this::verMax);
        validator.addAnnotation(Min.class, this::verMin);
        validator.addAnnotation(Pattern.class, this::verRegex);
        validator.addAnnotation(Valid.class, this::verCascade);
        validator.addAnnotation(Items.class, this::verCollection);
        validator.addAnnotation(Entries.class, this::verMap);
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
            FieldValidator validator = makeFieldValidator(clazz, field);
            validator.validateField(result, object);
        }
    }

    /**
     * Verifies that the value is not null.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verNotNull(BeanValidationResult result, String fieldName, Object value) {
        if (value == null) {
            ObjectValidationResult result2 =
                            new ObjectValidationResult(fieldName, xlate(value), ValidationStatus.INVALID, "is null");
            result.addResult(result2);
            return false;
        }

        return true;
    }

    /**
     * Verifies that the value is not blank. Note: this does <i>not</i> verify that the
     * value is not {@code null}.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verNotBlank(BeanValidationResult result, String fieldName, Object value) {
        if (value instanceof String && StringUtils.isBlank(value.toString())) {
            ObjectValidationResult result2 =
                            new ObjectValidationResult(fieldName, xlate(value), ValidationStatus.INVALID, "is blank");
            result.addResult(result2);
            return false;
        }

        return true;
    }

    /**
     * Verifies that the value matches a regular expression.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param annot annotation against which the value is being verified
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verRegex(BeanValidationResult result, String fieldName, Pattern annot, Object value) {
        try {
            if (value instanceof String && !com.google.re2j.Pattern.matches(annot.regexp(), value.toString())) {
                ObjectValidationResult result2 = new ObjectValidationResult(fieldName, xlate(value),
                                ValidationStatus.INVALID, "does not match regular expression " + annot.regexp());
                result.addResult(result2);
                return false;
            }
        } catch (RuntimeException e) {
            // TODO log at trace level
            return true;
        }

        return true;
    }

    /**
     * Verifies that the value is <= the minimum value.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param annot annotation against which the value is being verified
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verMax(BeanValidationResult result, String fieldName, Max annot, Object value) {
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

        ObjectValidationResult result2 = new ObjectValidationResult(fieldName, xlate(value), ValidationStatus.INVALID,
                        "exceeds the maximum value: " + annot.value());
        result.addResult(result2);
        return false;
    }

    /**
     * Verifies that the value is >= the minimum value.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param annot annotation against which the value is being verified
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verMin(BeanValidationResult result, String fieldName, Min annot, Object value) {
        return verMin(result, fieldName, annot.value(), value);
    }

    /**
     * Verifies that the value is >= the minimum value.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param min minimum against which the value is being verified
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verMin(BeanValidationResult result, String fieldName, long min, Object value) {
        if (!(value instanceof Number)) {
            return true;
        }

        Number num = (Number) value;
        if (num instanceof Integer || num instanceof Long) {
            if (num.longValue() >= min) {
                return true;
            }

        } else if (num instanceof Float || num instanceof Double) {
            if (num.doubleValue() >= min) {
                return true;
            }

        } else {
            return true;
        }

        ObjectValidationResult result2 = new ObjectValidationResult(fieldName, xlate(value), ValidationStatus.INVALID,
                        "is below the minimum value: " + min);
        result.addResult(result2);
        return false;
    }

    /**
     * Verifies that the value is valid by recursively invoking
     * {@link #validateTop(String, Object)}.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verCascade(BeanValidationResult result, String fieldName, Object value) {
        if (value == null || value instanceof Collection || value instanceof Map) {
            return true;
        }

        BeanValidationResult result2 = validateTop(fieldName, value);

        if (result2.isClean()) {
            return true;
        }

        result.addResult(result2);

        return result2.isValid();
    }

    /**
     * Validates the items in a collection.
     *
     * @param result where to add the validation result
     * @param fieldName name of the field containing the collection
     * @param annot validation annotations for individual items
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verCollection(BeanValidationResult result, String fieldName, Annotation annot, Object value) {

        if (!(value instanceof Collection)) {
            return true;
        }

        ItemValidator itemValidator = makeItemValidator(annot);

        return verCollection(result, fieldName, itemValidator, value);
    }

    /**
     * Validates the items in a collection.
     *
     * @param result where to add the validation result
     * @param fieldName name of the field containing the collection
     * @param itemValidator validator for individual items within the list
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verCollection(BeanValidationResult result, String fieldName, ValueValidator itemValidator,
                    Object value) {

        if (!(value instanceof Collection) || itemValidator.isEmpty()) {
            return true;
        }

        Collection<?> list = (Collection<?>) value;

        BeanValidationResult result2 = new BeanValidationResult(fieldName, value);
        int count = 0;
        for (Object item : list) {
            itemValidator.validateValue(result2, String.valueOf(count++), item);
        }

        if (result2.isClean()) {
            return true;
        }

        result.addResult(result2);
        return false;
    }

    /**
     * Validates the items in a Map.
     *
     * @param result where to add the validation result
     * @param fieldName name of the field containing the map
     * @param annot validation annotations for individual entries
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verMap(BeanValidationResult result, String fieldName, Entries annot, Object value) {

        if (!(value instanceof Map)) {
            return true;
        }

        EntryValidator entryValidator = makeEntryValidator(annot.key(), annot.value());

        return verMap(result, fieldName, entryValidator, value);
    }

    /**
     * Validates the items in a Map.
     *
     * @param result where to add the validation result
     * @param fieldName name of the field containing the map
     * @param entryValidator validator for individual entries within the Map
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verMap(BeanValidationResult result, String fieldName, EntryValidator entryValidator, Object value) {

        if (!(value instanceof Map)) {
            return true;
        }

        Map<?, ?> map = (Map<?, ?>) value;

        BeanValidationResult result2 = new BeanValidationResult(fieldName, value);

        for (Entry<?, ?> entry : map.entrySet()) {
            entryValidator.validateEntry(result2, entry);
        }

        if (result2.isClean()) {
            return true;
        }

        result.addResult(result2);
        return false;
    }

    /**
     * Makes a field validator.
     *
     * @param clazz class containing the field
     * @param field field of interest
     * @return a validator for the given field
     */
    protected FieldValidator makeFieldValidator(Class<?> clazz, Field field) {
        return new FieldValidator(this, clazz, field);
    }

    /**
     * Makes an item validator.
     *
     * @param annot container for the item annotations
     * @return a new item validator
     */
    protected ItemValidator makeItemValidator(Annotation annot) {
        return new ItemValidator(this, annot);
    }

    /**
     * Makes an entry validator.
     *
     * @param keyAnnot container for the annotations associated with the entry key
     * @param valueAnnot container for the annotations associated with the entry value
     * @return a new entry validator
     */
    protected EntryValidator makeEntryValidator(Annotation keyAnnot, Annotation valueAnnot) {
        return new EntryValidator(this, keyAnnot, valueAnnot);
    }

    /**
     * Translates a value to something printable, for use by
     * {@link ObjectValidationResult}. This default method simply returns the original
     * value.
     *
     * @param value value to be translated
     * @return the translated value
     */
    public Object xlate(Object value) {
        return value;
    }
}
