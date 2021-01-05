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

import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotNull;

public class TestMethodValidator extends ValidatorUtil {


    @Before
    public void setUp() {
        bean = new BeanValidator();
    }

    @Test
    public void testGetAccessor() {
        Method method = getMethod("getInt");
        assertThat(new MethodValidator(bean, getClass(), method).getAccessor()).isSameAs(method);
    }

    @Test
    public void testMethodValidator() {

        // unannotated
        assertThat(new MethodValidator(bean, TestMethodValidator.class, getMethod("unannotatedMethod")).isEmpty())
                        .isTrue();

        // annotated
        assertThat(new MethodValidator(bean, TestMethodValidator.class, getMethod("getInt")).isEmpty()).isFalse();

        // class-level annotation, but invalid method
        assertThat(new MethodValidator(bean, ClassAnnot.class, getMethod(ClassAnnot.class, "staticMethod")).isEmpty())
                        .isTrue();
    }

    @Test
    public void testDetmComponentName() {
        assertThat(MethodValidator.detmComponentName(getMethod("getInt"))).isEqualTo("int");
        assertThat(MethodValidator.detmComponentName(getMethod("isolate"))).isEqualTo("isolate");
        assertThat(MethodValidator.detmComponentName(getMethod("isolate"))).isEqualTo("isolate");
        assertThat(MethodValidator.detmComponentName(getMethod("isInt"))).isEqualTo("int");
        assertThat(MethodValidator.detmComponentName(getMethod("isAnotherInt"))).isEqualTo("anotherInt");
        assertThat(MethodValidator.detmComponentName(getMethod("get"))).isEqualTo("get");
        assertThat(MethodValidator.detmComponentName(getMethod("is"))).isEqualTo("is");
    }

    @Test
    public void testValidateComponent() {

        // valid
        BeanValidationResult result = new BeanValidationResult(MY_NAME, this);
        new MethodValidator(bean, getClass(), getMethod("getInt")).validateComponent(result, this);
        assertThat(result.getResult()).isNull();

        // invalid
        result = new BeanValidationResult(MY_NAME, this);
        new MethodValidator(bean, getClass(), getMethod("getInvalidInt")).validateComponent(result, this);
        assertThat(result.getResult()).contains("invalidInt");
    }

    @Min(1)
    public int getInt() {
        return 10;
    }

    @Min(1)
    public int getInvalidInt() {
        return -10;
    }

    public void unannotatedMethod() {
        // do nothing
    }

    public void isolate() {
        // do nothing
    }

    public boolean isInt() {
        return true;
    }

    public Boolean isAnotherInt() {
        return true;
    }

    // these names are short

    public int get() {
        return 0;
    }

    public boolean is() {
        return true;
    }

    @NotNull
    public static class ClassAnnot {

        public static int staticMethod() {
            return 0;
        }
    }
}
