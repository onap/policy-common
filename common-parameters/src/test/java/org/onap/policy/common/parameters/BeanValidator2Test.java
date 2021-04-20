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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;

public class BeanValidator2Test {
    private static final String MY_NAME = "John";
    private static final int MY_AGE = 15;
    private static final String MY_STATE = "NY";

    private static final String OTHER_NAME = "Tom";
    private static final int OTHER_AGE = 200;

    private BeanValidator2 validator;

    @Before
    public void setUp() {
        validator = new BeanValidator2();
    }

    @Test
    public void testValidate() {
        // valid
        assertThat(validator.validate(makeUser(MY_NAME, MY_AGE, true, MY_STATE, true))).isNull();

        // several invalid, including nested data
        // @formatter:off
        assertThat(validator.validate(makeUser(OTHER_NAME, MY_AGE, false, null, false))).isEqualTo(
                        String.join("\n",
                            "address.state (null): must not be null",
                            "checks[0]: 'isOk' must return true",
                            "name (Tom): must match \"J.*\"",
                            "sleeping (false): must be true"));
        // @formatter:on

        // null list item
        ArrayList<Check> checks = new ArrayList<>(1);
        checks.add(null);

        // null map item
        HashMap<String, Check> map = new HashMap<>();
        map.put(null, null);

        User user = makeUser(MY_NAME, MY_AGE, true, MY_STATE, true);
        user.setChecks(checks);
        user.setType2check(map);
        // @formatter:off
        assertThat(validator.validate(user)).isEqualTo(
                        String.join("\n",
                            "checks[0] (null): must not be null",
                            "type2check.<map key> (null): must not be null",
                            "type2check.<map value> (null): must not be null"));
        // @formatter:on
    }

    @Test
    public void testAddValue() {
        // null value
        assertThat(validator.validate(new User(MY_NAME, MY_AGE, true, null, null, null)))
                        .isEqualTo("address (null): must not be null");

        // String value
        assertThat(validator.validate(makeUser(OTHER_NAME, MY_AGE, true, MY_STATE, true)))
                        .isEqualTo("name (Tom): must match \"J.*\"");

        // primitive int
        assertThat(validator.validate(makeUser(MY_NAME, OTHER_AGE, true, MY_STATE, true)))
                        .isEqualTo("age (200): must be less than or equal to 20");

        // boxed primitive
        assertThat(validator.validate(makeUser(MY_NAME, MY_AGE, false, MY_STATE, true)))
                        .isEqualTo("sleeping (false): must be true");

        // none of the above - value should not be included
        assertThat(validator.validate(makeUser(MY_NAME, MY_AGE, true, MY_STATE, false)))
                        .isEqualTo("checks[0]: 'isOk' must return true");
    }

    private User makeUser(String name, int age, boolean sleeping, String state, boolean checkOk) {
        return new User(name, age, sleeping, new Address(state), List.of(new Check(checkOk)), Map.of());
    }

    @Data
    @AllArgsConstructor
    private static class User {
        @NotNull
        @Pattern(regexp = "J.*")
        private String name;

        @Min(10)
        @Max(20)
        private int age;

        @AssertTrue
        private boolean sleeping;

        @NotNull
        @Valid
        private Address address;

        private List<@Valid @NotNull Check> checks;

        private Map<@NotNull String, @Valid @NotNull Check> type2check;
    }

    @Data
    @AllArgsConstructor
    private static class Address {
        @NotNull
        private String state;
    }

    @Data
    @AllArgsConstructor
    @Checker
    private static class Check {
        private boolean ok;
    }

    @Retention(RUNTIME)
    @Target(TYPE)
    @Constraint(validatedBy = {CheckValidator.class})
    // @ReportAsSingleViolation // not relevant
    public static @interface Checker {

        /**
         * The error message.
         */
        String message() default "'isOk' must return true";

        /**
         * The groups.
         */
        Class<?>[] groups() default {};

        /**
         * The payload.
         */
        Class<? extends Payload>[] payload() default {};
    }

    public static class CheckValidator implements ConstraintValidator<Checker, Check> {

        @Override
        public boolean isValid(Check check, ConstraintValidatorContext context) {
            return check.isOk();
        }
    }
}
