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

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.common.parameters.annotations.NotNull;

/**
 * Validator of a component (i.e., field or method) of a class, supporting the parameter
 * annotations.
 */
public abstract class ComponentValidator extends ValueValidator {

    /**
     * {@code True} if there is a component-level annotation, {@code false} otherwise.
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private boolean componentAnnotated = false;

    /**
     * Class containing the component of interest.
     */
    private final Class<?> clazz;

    /**
     * Component to be validated.
     */
    private final AccessibleObject component;

    @Getter
    private final String componentName;


    /**
     * Constructs the object.
     *
     * @param clazz class containing the component
     * @param component component to be validated
     * @param componentName component's name
     */
    public ComponentValidator(Class<?> clazz, AccessibleObject component, String componentName) {
        this.clazz = clazz;
        this.component = component;
        this.componentName = componentName;

        // determine if null is allowed
        if (component.getAnnotation(NotNull.class) != null || clazz.getAnnotation(NotNull.class) != null) {
            setNullAllowed(false);
        }
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotClass) {

        // component annotation takes precedence over class annotation
        T annot = component.getAnnotation(annotClass);
        if (annot != null) {
            setComponentAnnotated(true);
            return annot;
        }

        return clazz.getAnnotation(annotClass);
    }

    /**
     * Performs validation of a single component.
     *
     * @param result validation results are added here
     * @param object object whose component is to be validated
     */
    public void validateComponent(BeanValidationResult result, Object object) {
        if (isEmpty()) {
            // has no annotations - nothing to check
            return;
        }

        // get the value
        Object value = getValue(object, getAccessor());

        validateValue(result, componentName, value);
    }

    /**
     * Gets the value from the object using the accessor function.
     *
     * @param object object whose value is to be retrieved
     * @param accessor "getter" method
     * @return the object's value
     */
    private Object getValue(Object object, Method accessor) {
        try {
            return accessor.invoke(object);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalArgumentException(
                            clazz.getName() + "." + accessor.getName() + " threw an exception", e);
        }
    }

    /**
     * Throws an exception if there are component-level annotations.
     *
     * @param exceptionMessage exception message
     */
    protected void classOnly(String exceptionMessage) {
        if (isComponentAnnotated()) {
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    /**
     * Determines if a method is a valid "getter".
     *
     * @param method method to be checked
     * @return {@code true} if the method is a valid "getter", {@code false} otherwise
     */
    protected boolean validMethod(Method method) {
        int mod = method.getModifiers();
        return !(Modifier.isStatic(mod) || method.getReturnType() == void.class || method.getParameterCount() != 0);
    }

    /**
     * Gets a method to access the component's value.
     *
     * @return a method to access the component's value
     */
    protected abstract Method getAccessor();
}
