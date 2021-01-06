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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;

public class TestItem2Validator extends ValidatorUtil {

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
    @Simple
    private int mismatch;

    /**
     * No annotations.
     */
    @SuppressWarnings("unused")
    private int noAnnotations;

    /**
     * One matching annotation.
     */
    @NotNull
    private int match;

    /**
     * Multiple matching annotations.
     */
    @NotNull
    @NotBlank
    private String multiMatch;


    @Before
    public void setUp() {
        bean = new BeanValidator();
    }

    @Test
    public void testGetAnnotation() {
        // no matches
        assertThat(new Item2Validator(bean, getAnnotType("noAnnotations"), true).isEmpty()).isTrue();

        // had a match
        assertThat(new Item2Validator(bean, getAnnotType("match"), true).checkers).hasSize(1);

        // multiple matches
        Item2Validator validator = new Item2Validator(bean, getAnnotType("multiMatch"), true);
        assertThat(validator.checkers).hasSize(2);

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
    public void testItem2ValidatorBeanValidatorAnnotation() {
        assertThat(new Item2Validator(bean, getAnnotType("match")).isEmpty()).isFalse();
    }

    @Test
    public void testItem2ValidatorBeanValidatorAnnotationBoolean() {
        assertThat(new Item2Validator(bean, getAnnotType("match"), true).isEmpty()).isFalse();

        assertThat(new Item2Validator(bean, getAnnotType("match"), false).isEmpty()).isTrue();
    }

    // these annotations are not recognized by the BeanValidator

    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface Simple {

    }
}
