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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
    public static final String INDENT = "    ";

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
        List<ConstraintViolation<T>> violations = new ArrayList<>(result);
        violations.sort((left, right) -> getPropComponents(left.getPropertyPath()).toString()
                        .compareTo(getPropComponents(right.getPropertyPath()).toString()));

        // convert violations to strings
        return convertToString(violations);
    }

    /**
     * Gets the property components from the path.
     *
     * @param propertyPath the path
     * @return the property components
     */
    protected List<String> getPropComponents(Path propertyPath) {
        List<String> components = new ArrayList<>();

        for (Node node : propertyPath) {
            Integer index = node.getIndex();
            if (index != null) {
                components.add("item " + index);
            }

            String name = node.getName();
            if (name != null && !"<list element>".equals(name)) {
                components.add(node.getName());
            }
        }

        return components;
    }

    /**
     * Converts violations to a string.
     *
     * @param <T> type of bean being validated
     * @param violations violations to be added
     * @return the violations, separated by newlines
     */
    private <T> String convertToString(List<ConstraintViolation<T>> violations) {
        StringBuilder builder = new StringBuilder();
        List<String> oldHierarchy = List.of();
        for (ConstraintViolation<T> violation : violations) {
            List<String> newHierarchy = getPropComponents(violation.getPropertyPath());

            if (addName(newHierarchy, oldHierarchy, builder)) {
                addValue(violation.getInvalidValue(), builder);
            }

            oldHierarchy = newHierarchy;

            String prefix = "\n" + (INDENT.repeat(oldHierarchy.size()));
            for (String line : violation.getMessage().split("\n")) {
                builder.append(prefix);
                builder.append(line);
            }
        }

        return builder.toString();
    }

    /**
     * Adds the component names to the builder, with each name on a separate line.
     *
     * @param newHierarchy new list of components
     * @param oldHierarchy old list of components, empty if nothing has been added yet
     * @param builder builder to which the names should be added
     * @return {@code true} if a new name was added, {@code false} otherwise
     */
    private boolean addName(List<String> newHierarchy, List<String> oldHierarchy, StringBuilder builder) {
        Iterator<String> newIter = newHierarchy.iterator();
        Iterator<String> curIter = oldHierarchy.iterator();

        int nitems = 0;
        String newName = null;
        String curName = null;
        while (newIter.hasNext() && curIter.hasNext()) {
            ++nitems;
            newName = newIter.next();
            curName = curIter.next();

            if (!newName.equals(curName)) {
                append(nitems, newName, newIter, builder);
                return true;
            }
        }

        if (!newIter.hasNext()) {
            return false;
        }

        append(nitems, newIter, curName != null, builder);
        return true;
    }

    /**
     * Appends the names on successive lines, with increasing indentations.
     *
     * @param nitems number of items so far
     * @param newName first name to be added
     * @param iter remaining names to be added
     * @param builder where to add the names
     */
    private void append(int nitems, String newName, Iterator<String> iter, StringBuilder builder) {
        builder.append('\n');
        builder.append(INDENT.repeat(nitems - 1));
        builder.append(newName);
        builder.append(", INVALID:");

        append(nitems, iter, iter.hasNext(), builder);
    }

    /**
     * Appends the names on successive lines, with increasing indentations.
     *
     * @param nitems number of items so far
     * @param iter remaining names to be added
     * @param needNewline {@code true} if to add a newline before adding the next name
     * @param builder where to add the names
     */
    private void append(int nitems, Iterator<String> iter, boolean needNewline, StringBuilder builder) {
        String indent = INDENT.repeat(nitems);
        if (needNewline) {
            indent = "\n" + indent;
        }

        while (iter.hasNext()) {
            String name = iter.next();
            builder.append(indent);
            builder.append(name);
            builder.append(", INVALID:");

            if (indent.startsWith("\n")) {
                indent += INDENT;
            } else {
                indent = "\n" + indent + INDENT;
            }
        }
    }

    /**
     * Adds a value to the builder, if it's a String, a primitive, or {@code null}.
     *
     * @param value value to be added
     * @param builder builder to which the value should be added
     */
    private void addValue(Object value, StringBuilder builder) {
        if (value == null) {
            builder.append(" null");
            return;
        }

        Class<?> clazz = value.getClass();

        if (clazz == String.class) {
            builder.append(" '");
            builder.append(value.toString());
            builder.append('\'');
            return;
        }

        if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
            builder.append(' ');
            builder.append(value.toString());
        }
    }
}
