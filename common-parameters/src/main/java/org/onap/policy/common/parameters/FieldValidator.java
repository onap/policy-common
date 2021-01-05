/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator of the contents of a field, supporting the parameter annotations.
 */
public class FieldValidator extends ComponentValidator {

    /**
     * Method to retrieve the field's value.
     */
    @Getter(AccessLevel.PROTECTED)
    private Method accessor;


    /**
     * Constructs the object.
     *
     * @param validator provider of validation methods
     * @param clazz class containing the field
     * @param field field whose value is to be validated
     */
    public FieldValidator(BeanValidator validator, Class<?> clazz, Field field) {
        super(clazz, field, field.getName());

        String fieldName = field.getName();

        validator.addValidators(this);

        if (checkers.isEmpty()) {
            // has no annotations - nothing to check
            return;
        }

        // verify the field type is of interest
        int mod = field.getModifiers();
        if (Modifier.isStatic(mod)) {
            classOnly(clazz.getName() + "." + fieldName + " is annotated but the field is static");
            checkers.clear();
            return;
        }

        // get the field's "getter" method
        accessor = getAccessor(clazz, fieldName);
        if (accessor == null) {
            classOnly(clazz.getName() + "." + fieldName + " is annotated but has no \"get\" method");
            checkers.clear();
            return;
        }
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
        Method accessor2 = getMethod(clazz, "get" + capname);
        if (accessor2 != null) {
            return accessor2;
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
}
