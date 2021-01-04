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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Validator of an "item", which is typically found in a collection, or the key or value
 * components of an entry in a Map.
 */
public class ItemValidator extends ValueValidator {
    private final Annotation annotationContainer;

    /**
     * Constructs the object.
     *
     * @param validator provider of validation methods
     * @param annotationContainer an annotation containing validation annotations to be
     *        applied to the item
     */
    public ItemValidator(BeanValidator validator, Annotation annotationContainer) {
        this(validator, annotationContainer, true);
    }

    /**
     * Constructs the object.
     *
     * @param validator provider of validation methods
     * @param annotationContainer an annotation containing validation annotations to be
     *        applied to the item
     * @param addValidators {@code true} if to add validators
     */
    public ItemValidator(BeanValidator validator, Annotation annotationContainer, boolean addValidators) {
        this.annotationContainer = annotationContainer;

        if (addValidators) {
            validator.addValidators(this);
        }
    }

    /**
     * Gets an annotation from the field or the class.
     *
     * @param annotClass annotation class of interest
     * @return the annotation, or {@code null} if the {@link #annotationContainer} does
     *         not contain the desired annotation
     */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotClass) {
        try {
            for (Method meth : annotationContainer.getClass().getDeclaredMethods()) {
                T annot = getAnnotation2(annotClass, meth);
                if (annot != null) {
                    return annot;
                }
            }
        } catch (RuntimeException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("cannot determine " + annotClass.getName(), e);
        }

        return null;
    }

    /**
     * Note: this is only marked "private" so it can be overridden for junit testing.
     */
    protected <T extends Annotation> T getAnnotation2(Class<T> annotClass, Method method)
                    throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Class<?> ret = method.getReturnType();
        if (!ret.isArray()) {
            return null;
        }

        Class<?> comp = ret.getComponentType();
        if (comp != annotClass) {
            return null;
        }

        // get the array for this type of annotation
        @SuppressWarnings("unchecked")
        T[] arrobj = (T[]) method.invoke(annotationContainer);

        if (arrobj.length == 0) {
            return null;
        }

        if (arrobj.length > 1) {
            throw new IllegalArgumentException("extra item annotations of type: " + annotClass.getName());
        }

        return arrobj[0];
    }
}
