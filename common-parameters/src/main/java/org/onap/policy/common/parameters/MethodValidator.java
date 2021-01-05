/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import java.lang.reflect.Method;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator of the value returned by a method, supporting the parameter annotations.
 */
public class MethodValidator extends ComponentValidator {

    /**
     * Method whose value is to be validated.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Method accessor;


    /**
     * Constructs the object.
     *
     * @param validator provider of validation methods
     * @param clazz class containing the method
     * @param accessor method whose value is to be validated
     */
    public MethodValidator(BeanValidator validator, Class<?> clazz, Method accessor) {
        super(clazz, accessor, detmComponentName(accessor));

        this.accessor = accessor;

        validator.addValidators(this);

        if (checkers.isEmpty()) {
            // has no annotations - nothing to check
            return;
        }

        // verify the method type is of interest
        if (!validMethod(accessor)) {
            classOnly(clazz.getName() + "." + accessor.getName() + " is annotated but is not a valid 'getter' method");
            checkers.clear();
        }
    }

    /**
     * Determines the component's name.
     *
     * @param accessor method whose value is to be validated
     * @return the component's name
     */
    protected static String detmComponentName(Method accessor) {
        String name = accessor.getName();
        if (name.length() > 3 && name.startsWith("get")) {
            return StringUtils.uncapitalize(name.substring(3));
        }

        if (name.length() > 2 && name.startsWith("is")) {
            Class<?> ret = accessor.getReturnType();
            if (ret == boolean.class || ret == Boolean.class) {
                return StringUtils.uncapitalize(name.substring(2));
            }
        }

        return name;
    }
}
