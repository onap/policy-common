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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotNull;

public class TestFieldValidator extends ValidatorUtil {
    private static final String NO_METHOD = "noMethod";
    private static final String UNANNOTATED_FIELD = "unannotated";
    private static final String INT_FIELD = "intValue";
    private static final int VALID_INT = 10;
    private static final int INVALID_INT = -10;

    @Getter
    private int unannotated;

    @Min(0)
    @Getter
    private int intValue;

    @NotNull
    @Getter
    private boolean boolValue;

    @NotNull
    @Getter
    private String notNullValue;

    @Min(0)
    @Getter
    private static int staticField;

    /**
     * Has no accessor.
     */
    @Min(0)
    private int noMethod;

    /**
     * Accessor is {@link #getStaticMethod()}, which is static.
     */
    @Min(0)
    private int staticMethod;

    /**
     * Accessor is {@link #getVoidMethod()}, which returns a void.
     */
    @Min(0)
    private int voidMethod;

    /**
     * Accessor is {@link #getParameterizedMethod()}, which requires a parameter.
     */
    @Min(0)
    private int parameterizedMethod;


    @Before
    public void setUp() {
        bean = new BeanValidator();
    }

    @Test
    public void testFieldValidator() {

        // unannotated
        assertThat(new FieldValidator(bean, TestFieldValidator.class, getField(UNANNOTATED_FIELD)).isEmpty()).isTrue();

        // these are invalid for various reasons

        Field staticField2 = getField("staticField");
        assertThatThrownBy(() -> new FieldValidator(bean, TestFieldValidator.class, staticField2))
                        .isInstanceOf(IllegalArgumentException.class);

        Field noMethodField = getField(NO_METHOD);
        assertThatThrownBy(() -> new FieldValidator(bean, TestFieldValidator.class, noMethodField))
                        .isInstanceOf(IllegalArgumentException.class);

        // annotated
        assertThat(new FieldValidator(bean, TestFieldValidator.class, getField(INT_FIELD)).isEmpty()).isFalse();

        // class-level annotation on a field without a method
        assertThat(new FieldValidator(bean, ClassAnnot.class, getField(ClassAnnot.class, NO_METHOD)).isEmpty())
                        .isTrue();

        // class-level annotation on a static field
        assertThat(new FieldValidator(bean, ClassAnnot.class, getField(ClassAnnot.class, "staticValue")).isEmpty())
                        .isTrue();
    }

    @Test
    public void testValidateComponent() {

        // valid
        intValue = VALID_INT;
        BeanValidationResult result = new BeanValidationResult(MY_NAME, this);
        new FieldValidator(bean, getClass(), getField(INT_FIELD)).validateComponent(result, this);
        assertThat(result.getResult()).isNull();

        // invalid
        intValue = INVALID_INT;
        result = new BeanValidationResult(MY_NAME, this);
        new FieldValidator(bean, getClass(), getField(INT_FIELD)).validateComponent(result, this);
        assertThat(result.getResult()).contains(INT_FIELD);
    }

    @Test
    public void testGetAccessor() {
        // uses "getXxx"
        assertThat(new FieldValidator(bean, TestFieldValidator.class, getField(INT_FIELD)).isEmpty()).isFalse();

        // uses "isXxx"
        assertThat(new FieldValidator(bean, TestFieldValidator.class, getField("boolValue")).isEmpty()).isFalse();
    }

    @Test
    public void testGetMethod() {
        assertThat(new FieldValidator(bean, TestFieldValidator.class, getField(INT_FIELD)).isEmpty()).isFalse();

        // these are invalid for various reasons

        Field noMethodField = getField(NO_METHOD);
        assertThatThrownBy(() -> new FieldValidator(bean, TestFieldValidator.class, noMethodField))
                        .isInstanceOf(IllegalArgumentException.class);

        Field staticMethodField = getField("staticMethod");
        assertThatThrownBy(() -> new FieldValidator(bean, TestFieldValidator.class, staticMethodField))
                        .isInstanceOf(IllegalArgumentException.class);
    }

    @NotNull
    public static class ClassAnnot {
        @Getter
        private String text;

        // no "get" method
        @SuppressWarnings("unused")
        private String noMethod;

        @Getter
        private static int staticValue;
    }
}
