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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.commons.lang3.ClassUtils;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

/**
 * Bean validator, based on javax.validation.
 */
public class BeanValidator2 {
    private static final ValidatorFactory DEFAULT_FACTORY = Validation.byDefaultProvider().configure()
                    .messageInterpolator(new ParameterMessageInterpolator()).buildValidatorFactory();

    private final ValidatorFactory factory;

    /**
     * Constructs the object, using the default validator factory.
     */
    public BeanValidator2() {
        this(DEFAULT_FACTORY);
    }

    /**
     * Constructs the object, using the given validator factory.
     *
     * @param factory validator factory to use
     */
    public BeanValidator2(ValidatorFactory factory) {
        this.factory = factory;
    }

    /**
     * Validates a bean.
     *
     * @param <T> type of bean to validate
     * @param bean bean to be validated
     * @return the violations, separated by newlines, or {@code null}, if the bean is
     *         valid
     */
    public <T> String validate(T bean) {
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<T>> result = validator.validate(bean);
        if (result.isEmpty()) {
            return null;
        }

        // order the violations by property name
        Map<String, ConstraintViolation<T>> propName2violation = new TreeMap<>();
        for (ConstraintViolation<T> violation : result) {
            propName2violation.put(getPropName(violation.getPropertyPath()), violation);
        }

        // convert violations to strings
        StringBuilder builder = new StringBuilder();
        for (Entry<String, ConstraintViolation<T>> entry : propName2violation.entrySet()) {
            if (builder.length() > 0) {
                builder.append('\n');
            }

            builder.append(entry.getKey());

            ConstraintViolation<T> violation = entry.getValue();
            addValue(violation.getInvalidValue(), builder);
            builder.append(": ");
            builder.append(violation.getMessage());
        }

        return builder.toString();
    }

    /**
     * Gets the property name from the path.
     *
     * @param propertyPath the path
     * @return the property name
     */
    private String getPropName(Path propertyPath) {
        StringBuilder builder = new StringBuilder();

        for (Node node : propertyPath) {
            Integer index = node.getIndex();
            if (index != null) {
                builder.append('[');
                builder.append(index);
                builder.append(']');
            }

            String name = node.getName();
            if (name != null && !"<list element>".equals(name)) {
                if (builder.length() > 0) {
                    builder.append('.');
                }

                builder.append(node.getName());
            }
        }

        return builder.toString();
    }

    /**
     * Adds a value to the builder, if it's {@code null}, a String, or a primitive.
     *
     * @param value value to be added
     * @param builder builder to which the value should be added
     */
    protected void addValue(Object value, StringBuilder builder) {
        if (value == null) {
            builder.append(" (null)");
            return;
        }

        Class<?> clazz = value.getClass();

        if (clazz == String.class || ClassUtils.isPrimitiveOrWrapper(clazz)) {
            builder.append(" (");
            builder.append(value.toString());
            builder.append(")");
        }
    }
}
