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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotNull;

public class TestComponentValidator extends ValidatorUtil {
    private static final String NOT_NULL_METHOD = "notNullMethod";
    private static final String UNANNOTATED_METHOD = "unannotatedMethod";
    private static final String VALID_INT_METHOD = "validIntMethod";
    private static final String INVALID_INT_METHOD = "invalidIntMethod";


    @Before
    public void setUp() {
        bean = new BeanValidator();
    }

    @Test
    public void testSetNullAllowed() {
        // default - null is allowed
        assertThat(new MyValidator(bean, TestComponentValidator.class, getMethod(VALID_INT_METHOD)).isNullAllowed())
                        .isTrue();

        // component-level NotNull
        assertThat(new MyValidator(bean, TestComponentValidator.class, getMethod(NOT_NULL_METHOD)).isNullAllowed())
                        .isFalse();

        // class-level NotNull
        assertThat(new MyValidator(bean, ClassAnnot.class, getMethod(ClassAnnot.class, NOT_NULL_METHOD))
                        .isNullAllowed()).isFalse();
    }

    @Test
    public void testGetAnnotation() {
        // component-level annotation
        assertThat(new MyValidator(bean, TestComponentValidator.class, getMethod(VALID_INT_METHOD)).isEmpty())
                        .isFalse();

        // class-level annotation
        assertThat(new MyValidator(bean, ClassAnnot.class, getMethod(ClassAnnot.class, NOT_NULL_METHOD)).isEmpty())
                        .isFalse();
    }

    @Test
    public void testValidateComponent_testGetValue() {
        // unannotated
        BeanValidationResult result = new BeanValidationResult(MY_NAME, this);
        new MyValidator(bean, getClass(), getMethod(UNANNOTATED_METHOD)).validateComponent(result, this);
        assertThat(result.getResult()).isNull();

        // valid
        result = new BeanValidationResult(MY_NAME, this);
        new MyValidator(bean, getClass(), getMethod(VALID_INT_METHOD)).validateComponent(result, this);
        assertThat(result.getResult()).isNull();

        // invalid
        result = new BeanValidationResult(MY_NAME, this);
        new MyValidator(bean, getClass(), getMethod(INVALID_INT_METHOD)).validateComponent(result, this);
        assertThat(result.getResult()).contains(INVALID_INT_METHOD);

        // throws an exception
        MyValidator validator = new MyValidator(bean, TestComponentValidator.class, getMethod("exMethod"));
        BeanValidationResult result2 = new BeanValidationResult(MY_NAME, this);
        assertThatThrownBy(() -> validator.validateComponent(result2, this))
                        .isInstanceOf(IllegalArgumentException.class).getCause()
                        .isInstanceOf(InvocationTargetException.class).getCause().hasMessage("expected exception");
    }

    @Test
    public void testClassOnly() {
        MyValidator validator = new MyValidator(bean, getClass(), getMethod(VALID_INT_METHOD));

        // class-level annotation is allowed
        validator.setComponentAnnotated(false);
        assertThatCode(() -> validator.classOnly("some message")).doesNotThrowAnyException();

        // component-level annotation is not allowed
        validator.setComponentAnnotated(true);
        assertThatThrownBy(() -> validator.classOnly("expected exception")).isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("expected exception");
    }

    @Test
    public void testValidMethod() throws Exception {
        MyValidator validator = new MyValidator(bean, MyValidator.class, getMethod(VALID_INT_METHOD));

        assertThat(validator.validMethod(getMethod(VALID_INT_METHOD))).isTrue();

        // these are invalid for various reasons

        assertThat(validator.validMethod(getMethod("staticMethod"))).isFalse();
        assertThat(validator.validMethod(getMethod("voidMethod"))).isFalse();
        assertThat(validator.validMethod(getClass().getDeclaredMethod("parameterizedMethod", boolean.class))).isFalse();
    }

    @Test
    public void testIsComponentAnnotated_testSetComponentAnnotated() {
        // annotated at the component level
        assertThat(new MyValidator(bean, getClass(), getMethod(VALID_INT_METHOD)).isComponentAnnotated()).isTrue();

        // unannotated
        assertThat(new MyValidator(bean, getClass(), getMethod(UNANNOTATED_METHOD)).isComponentAnnotated()).isFalse();
    }

    public int unannotatedMethod() {
        return 10;
    }

    @Min(0)
    public int validIntMethod() {
        return 10;
    }

    @Min(0)
    public int invalidIntMethod() {
        return -10;
    }

    @NotNull
    public String notNullMethod() {
        return "";
    }

    @Min(0)
    public int exMethod() {
        throw new RuntimeException("expected exception");
    }

    @Min(0)
    public static int staticMethod() {
        return -1000;
    }

    @Min(0)
    public void voidMethod() {
        // do nothing
    }

    @Min(0)
    public int parameterizedMethod(boolean flag) {
        return 0;
    }

    @NotNull
    public static class ClassAnnot {

        public String notNullMethod() {
            return "";
        }

        public static int staticMethod() {
            return 0;
        }
    }

    private static class MyValidator extends ComponentValidator {

        @Getter(AccessLevel.PROTECTED)
        private final Method accessor;

        public MyValidator(BeanValidator bean, Class<?> clazz, Method component) {
            super(clazz, component, component.getName());

            this.accessor = component;

            bean.addValidators(this);
        }
    }
}
