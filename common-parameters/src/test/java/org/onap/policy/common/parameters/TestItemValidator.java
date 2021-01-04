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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.Items;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;

public class TestItemValidator extends ValidatorUtil {

    // annotated fields - each field must have exactly one annotation

    /**
     * This annotation does not contain a method returning an array.
     */
    @Min(value = 0)
    private int notArray;

    /**
     * This annotation doesn't contain any annotations that the {@link BeanValidator}
     * recognizes.
     */
    @SimpleItems(simple = {@Simple})
    private int mismatch;

    /**
     * Annotation with no sub-annotations.
     */
    @Items()
    private int noAnnotations;

    /**
     * One matching sub-annotation.
     */
    @Items(notNull = {@NotNull})
    private int match;

    /**
     * Excess matching sub-annotations of a single type.
     */
    @Items(notNull = {@NotNull, @NotNull})
    private int excess;

    /**
     * Multiple matching annotations.
     */
    @Items(notNull = {@NotNull}, notBlank = {@NotBlank})
    private String multiMatch;


    @Before
    public void setUp() {
        bean = new BeanValidator();
    }

    @Test
    public void testGetAnnotation() {
        // no matches
        assertThat(new ItemValidator(bean, getAnnot("noAnnotations"), true).isEmpty()).isTrue();

        // had a match
        assertThat(new ItemValidator(bean, getAnnot("match"), true).isEmpty()).isFalse();

        // with an exception
        IllegalAccessException ex = new IllegalAccessException("expected exception");

        assertThatThrownBy(() -> new ItemValidator(bean, getAnnot("match"), true) {
            @Override
            protected <T extends Annotation> T getAnnotation2(Class<T> annotClass, Method method)
                            throws IllegalAccessException {
                throw ex;
            }
        }).hasCause(ex);

        // multiple matches
        ItemValidator validator = new ItemValidator(bean, getAnnot("multiMatch"), true);

        BeanValidationResult result = new BeanValidationResult(MY_NAME, this);
        validator.validateValue(result, MY_FIELD, HELLO);
        assertThat(result.getResult()).isNull();

        result = new BeanValidationResult(MY_NAME, this);
        validator.validateValue(result, MY_FIELD, null);
        assertThat(result.getResult()).isNotNull();

        result = new BeanValidationResult(MY_NAME, this);
        validator.validateValue(result, MY_FIELD, "");
        assertThat(result.getResult()).isNotNull();
    }

    @Test
    public void testItemValidatorBeanValidatorAnnotation() {
        assertThat(new ItemValidator(bean, getAnnot("match")).isEmpty()).isFalse();
    }

    @Test
    public void testItemValidatorBeanValidatorAnnotationBoolean() {
        assertThat(new ItemValidator(bean, getAnnot("match"), true).isEmpty()).isFalse();

        assertThat(new ItemValidator(bean, getAnnot("match"), false).isEmpty()).isTrue();
    }

    @Test
    public void testGetAnnotation2() {
        assertThat(new ItemValidator(bean, getAnnot("notArray"), true).isEmpty()).isTrue();
        assertThat(new ItemValidator(bean, getAnnot("mismatch"), true).isEmpty()).isTrue();
        assertThat(new ItemValidator(bean, getAnnot("noAnnotations"), true).isEmpty()).isTrue();

        assertThat(new ItemValidator(bean, getAnnot("match"), true).isEmpty()).isFalse();

        Annotation excess = getAnnot("excess");
        assertThatThrownBy(() -> new ItemValidator(bean, excess, true)).isInstanceOf(IllegalArgumentException.class);
    }

    // these annotations are not recognized by the BeanValidator

    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface Simple {

    }

    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface SimpleItems {
        /**
         * Validates that it's simple.
         */
        Simple[] simple() default {};
    }
}
