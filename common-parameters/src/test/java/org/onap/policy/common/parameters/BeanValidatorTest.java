/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.Max;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;

public class BeanValidatorTest {
    private static final String GET_MSG = "\"get\"";
    private static final IllegalStateException EXPECTED_EXCEPTION = new IllegalStateException("expected exception");
    private static final String TOP = "top";
    private static final String STR_FIELD = "strValue";
    private static final String INT_FIELD = "intValue";
    private static final String NUM_FIELD = "numValue";
    private static final String BOOL_FIELD = "boolValue";
    private static final String STRING_VALUE = "string value";
    private static final int INT_VALUE = 20;

    private BeanValidator validator;

    @Before
    public void setUp() {
        validator = new BeanValidator();
    }

    @Test
    public void testValidateTop_testValidateFields() {
        // validate null
        assertTrue(validator.validateTop(TOP, null).isValid());

        // validate something that has no annotations
        assertTrue(validator.validateTop(TOP, validator).isValid());

        @NotNull
        @Getter
        class Data {
            String strValue;
        }

        // one failure case
        Data data = new Data();
        BeanValidationResult result = validator.validateTop(TOP, data);
        assertInvalid("testValidateFields", result, STR_FIELD, "null");
        assertTrue(result.getResult().contains(TOP));

        // one success case
        data.strValue = STRING_VALUE;
        assertTrue(validator.validateTop(TOP, data).isValid());

        /**
         * Repeat with a subclass.
         */
        @Getter
        class Derived extends Data {
            @Min(10)
            int intValue;
        }

        Derived derived = new Derived();
        derived.strValue = STRING_VALUE;
        derived.intValue = INT_VALUE;

        // success case
        assertTrue(validator.validateTop(TOP, derived).isValid());

        // failure cases
        derived.strValue = null;
        assertInvalid("testValidateFields", validator.validateTop(TOP, derived), STR_FIELD, "null");
        derived.strValue = STRING_VALUE;

        derived.intValue = 1;
        assertInvalid("testValidateFields", validator.validateTop(TOP, derived), INT_FIELD, "minimum");
        derived.intValue = INT_VALUE;

        // both invalid
        derived.strValue = null;
        derived.intValue = 1;
        result = validator.validateTop(TOP, derived);
        assertInvalid("testValidateFields", result, STR_FIELD, "null");
        assertInvalid("testValidateFields", result, INT_FIELD, "minimum");
        derived.strValue = STRING_VALUE;
        derived.intValue = INT_VALUE;
    }

    @Test
    public void testValidateField() {
        /*
         * Note: nested classes contain fields like "$this", thus the check for "$" in the
         * variable name is already covered by the other tests.
         */

        /*
         * Class with no annotations.
         */
        class NoAnnotations {
            @SuppressWarnings("unused")
            String strValue;
        }

        NoAnnotations noAnnot = new NoAnnotations();
        noAnnot.strValue = null;
        assertTrue(validator.validateTop(TOP, noAnnot).isValid());

        /*
         * Class containing a static field with an annotation.
         */
        AnnotFieldStatic annotFieldStatic = new AnnotFieldStatic();
        assertThatIllegalArgumentException().isThrownBy(() -> validator.validateTop(TOP, annotFieldStatic))
                        .withMessageContaining(STR_FIELD).withMessageContaining("static");

        /*
         * Class containing a static field, with an annotation at the class level.
         */
        AnnotClassStatic annotClassStatic = new AnnotClassStatic();
        assertTrue(validator.validateTop(TOP, annotClassStatic).isValid());

        /*
         * Class with no getter method, with field-level annotation.
         */
        class NoGetter {
            @NotNull
            String strValue;
        }

        NoGetter noGetter = new NoGetter();
        assertThatIllegalArgumentException().isThrownBy(() -> validator.validateTop(TOP, noGetter))
                        .withMessageContaining(STR_FIELD).withMessageContaining(GET_MSG);

        /*
         * Class with no getter method, with class-level annotation.
         */
        @NotNull
        class ClassNoGetter {
            @SuppressWarnings("unused")
            String strValue;
        }

        ClassNoGetter classNoGetter = new ClassNoGetter();
        assertTrue(validator.validateTop(TOP, classNoGetter).isValid());

        /*
         * Class where the getter throws an exception.
         */
        class GetExcept {
            @NotNull
            String strValue;

            @SuppressWarnings("unused")
            public String getStrValue() {
                throw EXPECTED_EXCEPTION;
            }
        }

        GetExcept getExcept = new GetExcept();
        assertThatIllegalArgumentException().isThrownBy(() -> validator.validateTop(TOP, getExcept))
                        .withMessageContaining(STR_FIELD).withMessageContaining("accessor threw");

        /*
         * Class with "blank", but no "null" check. Value is null.
         */
        class NoNullCheck {
            @NotBlank
            @Getter
            String strValue;
        }

        NoNullCheck noNullCheck = new NoNullCheck();
        assertTrue(validator.validateTop(TOP, noNullCheck).isValid());

        /*
         * Class with conflicting minimum and maximum, where the value doesn't satisfy
         * either of them. This should only generate one result, rather than one for each
         * check. Note: the "max" check occurs before the "min" check, so that's the one
         * we expect in the result.
         */
        class MinAndMax {
            @Getter
            @Min(200)
            @Max(100)
            Integer intValue;
        }

        MinAndMax minAndMax = new MinAndMax();
        minAndMax.intValue = 150;
        BeanValidationResult result = validator.validateTop(INT_FIELD, minAndMax);
        assertFalse(result.isValid());
        assertInvalid("testValidateField", result, INT_FIELD, "maximum");
        assertFalse(result.getResult().contains("minimum"));
    }

    @Test
    public void testVerNotNull() {
        class NotNullCheck {
            @Getter
            @Min(1)
            @NotNull
            Integer intValue;
        }

        NotNullCheck notNullCheck = new NotNullCheck();
        assertInvalid("testVerNotNull", validator.validateTop(TOP, notNullCheck), INT_FIELD, "null");

        notNullCheck.intValue = INT_VALUE;
        assertTrue(validator.validateTop(TOP, notNullCheck).isValid());

        notNullCheck.intValue = 0;
        assertInvalid("testVerNotNull", validator.validateTop(TOP, notNullCheck), INT_FIELD, "minimum");
    }

    @Test
    public void testVerNotBlank() {
        class NotBlankCheck {
            @Getter
            @NotBlank
            String strValue;
        }

        NotBlankCheck notBlankCheck = new NotBlankCheck();

        // null
        assertTrue(validator.validateTop(TOP, notBlankCheck).isValid());

        // empty
        notBlankCheck.strValue = "";
        assertInvalid("testVerNotNull", validator.validateTop(TOP, notBlankCheck), STR_FIELD, "blank");

        // spaces
        notBlankCheck.strValue = "  ";
        assertInvalid("testVerNotNull", validator.validateTop(TOP, notBlankCheck), STR_FIELD, "blank");

        // not blank
        notBlankCheck.strValue = STRING_VALUE;
        assertTrue(validator.validateTop(TOP, notBlankCheck).isValid());

        /*
         * Class with "blank" annotation on an integer.
         */
        class NotBlankInt {
            @Getter
            @NotBlank
            int intValue;
        }

        NotBlankInt notBlankInt = new NotBlankInt();
        notBlankInt.intValue = 0;
        assertTrue(validator.validateTop(TOP, notBlankInt).isValid());
    }

    @Test
    public void testVerMax() {
        /*
         * Field is not a number.
         */
        class NonNumeric {
            @Getter
            @Max(100)
            String strValue;
        }

        NonNumeric nonNumeric = new NonNumeric();
        nonNumeric.strValue = STRING_VALUE;
        assertTrue(validator.validateTop(TOP, nonNumeric).isValid());

        /*
         * Integer field.
         */
        class IntField {
            @Getter
            @Max(100)
            Integer intValue;
        }

        // ok value
        IntField intField = new IntField();
        assertNumeric("testVerMax-integer", intField, value -> {
            intField.intValue = value;
        }, INT_FIELD, "maximum", INT_VALUE, 100, 101);

        /*
         * Long field.
         */
        class LongField {
            @Getter
            @Max(100)
            Long numValue;
        }

        // ok value
        LongField longField = new LongField();
        assertNumeric("testVerMax-long", longField, value -> {
            longField.numValue = (long) value;
        }, NUM_FIELD, "maximum", INT_VALUE, 100, 101);

        /*
         * Float field.
         */
        class FloatField {
            @Getter
            @Max(100)
            Float numValue;
        }

        // ok value
        FloatField floatField = new FloatField();
        assertNumeric("testVerMax-float", floatField, value -> {
            floatField.numValue = (float) value;
        }, NUM_FIELD, "maximum", INT_VALUE, 100, 101);

        /*
         * Double field.
         */
        class DoubleField {
            @Getter
            @Max(100)
            Double numValue;
        }

        // ok value
        DoubleField doubleField = new DoubleField();
        assertNumeric("testVerMax-double", doubleField, value -> {
            doubleField.numValue = (double) value;
        }, NUM_FIELD, "maximum", INT_VALUE, 100, 101);

        /*
         * Atomic Integer field (which is a subclass of Number).
         */
        class AtomIntValue {
            @Getter
            @Max(100)
            AtomicInteger numValue;
        }

        // ok value
        AtomIntValue atomIntField = new AtomIntValue();
        atomIntField.numValue = new AtomicInteger(INT_VALUE);
        assertTrue(validator.validateTop(TOP, atomIntField).isValid());

        // invalid value - should be OK, because it isn't an Integer
        atomIntField.numValue.set(101);
        assertTrue(validator.validateTop(TOP, atomIntField).isValid());
    }

    @Test
    public void testVerMin() {
        /*
         * Field is not a number.
         */
        class NonNumeric {
            @Getter
            @Min(10)
            String strValue;
        }

        NonNumeric nonNumeric = new NonNumeric();
        nonNumeric.strValue = STRING_VALUE;
        assertTrue(validator.validateTop(TOP, nonNumeric).isValid());

        /*
         * Integer field.
         */
        class IntField {
            @Getter
            @Min(10)
            Integer intValue;
        }

        // ok value
        IntField intField = new IntField();
        assertNumeric("testVerMin-integer", intField, value -> {
            intField.intValue = value;
        }, INT_FIELD, "minimum", INT_VALUE, 10, 1);

        /*
         * Long field.
         */
        class LongField {
            @Getter
            @Min(10)
            Long numValue;
        }

        // ok value
        LongField longField = new LongField();
        assertNumeric("testVerMin-long", longField, value -> {
            longField.numValue = (long) value;
        }, NUM_FIELD, "minimum", INT_VALUE, 10, 1);

        /*
         * Float field.
         */
        class FloatField {
            @Getter
            @Min(10)
            Float numValue;
        }

        // ok value
        FloatField floatField = new FloatField();
        assertNumeric("testVerMin-float", floatField, value -> {
            floatField.numValue = (float) value;
        }, NUM_FIELD, "minimum", INT_VALUE, 10, 1);

        /*
         * Double field.
         */
        class DoubleField {
            @Getter
            @Min(10)
            Double numValue;
        }

        // ok value
        DoubleField doubleField = new DoubleField();
        assertNumeric("testVerMin-double", doubleField, value -> {
            doubleField.numValue = (double) value;
        }, NUM_FIELD, "minimum", INT_VALUE, 10, 1);

        /*
         * Atomic Integer field (which is a subclass of Number).
         */
        class AtomIntValue {
            @Getter
            @Min(10)
            AtomicInteger numValue;
        }

        // ok value
        AtomIntValue atomIntField = new AtomIntValue();
        atomIntField.numValue = new AtomicInteger(INT_VALUE);
        assertTrue(validator.validateTop(TOP, atomIntField).isValid());

        // invalid value - should be OK, because it isn't an Integer
        atomIntField.numValue.set(101);
        assertTrue(validator.validateTop(TOP, atomIntField).isValid());
    }

    private <T> void assertNumeric(String testName, T object, Consumer<Integer> setter, String fieldName,
                    String expectedText, int inside, int edge, int outside) {
        setter.accept(inside);
        assertTrue(validator.validateTop(TOP, object).isValid());

        // on the edge
        setter.accept(edge);
        assertTrue(validator.validateTop(TOP, object).isValid());

        // invalid
        setter.accept(outside);
        assertInvalid("testVerNotNull", validator.validateTop(TOP, object), fieldName, expectedText);
    }

    @Test
    public void testGetAccessor() {
        /*
         * Class with "get" method has been tested through-out this junit, so no need to
         * do more.
         */

        /*
         * Class with "is" method.
         */
        class IsField {
            @NotNull
            Boolean boolValue;

            @SuppressWarnings("unused")
            public Boolean isBoolValue() {
                return boolValue;
            }
        }

        // ok value
        IsField isField = new IsField();
        isField.boolValue = true;
        assertTrue(validator.validateTop(TOP, isField).isValid());

        // invalid value
        isField.boolValue = null;
        assertInvalid("testGetAccessor", validator.validateTop(TOP, isField), BOOL_FIELD, "null");
    }

    @Test
    public void testGetMethod() {
        /*
         * Class with some fields annotated and some not.
         */
        @Getter
        class Mixed {
            Integer intValue;

            @NotNull
            String strValue;
        }

        // invalid
        Mixed mixed = new Mixed();
        BeanValidationResult result = validator.validateTop(TOP, mixed);
        assertInvalid("testGetMethod", result, STR_FIELD, "null");
        assertFalse(result.getResult().contains(INT_FIELD));

        // intValue is null, but it isn't annotated so this should be valid
        mixed.strValue = STRING_VALUE;
        assertTrue(validator.validateTop(TOP, mixed).isValid());
    }

    @Test
    public void testValidMethod() {

        /*
         * Plain getter.
         */
        class PlainGetter {
            @NotNull
            @Getter
            String strValue;
        }

        // invalid
        PlainGetter plainGetter = new PlainGetter();
        assertInvalid("testValidMethod", validator.validateTop(TOP, plainGetter), STR_FIELD, "null");

        // valid
        plainGetter.strValue = STRING_VALUE;
        assertTrue(validator.validateTop(TOP, plainGetter).isValid());

        /*
         * Static getter - should throw an exception.
         */
        StaticGetter staticGetter = new StaticGetter();
        assertThatIllegalArgumentException().isThrownBy(() -> validator.validateTop(TOP, staticGetter))
                        .withMessageContaining(STR_FIELD).withMessageContaining(GET_MSG);

        /*
         * Protected getter - should throw an exception.
         */
        class ProtectedGetter {
            @NotNull
            @Getter(AccessLevel.PROTECTED)
            String strValue;
        }

        ProtectedGetter protectedGetter = new ProtectedGetter();
        assertThatIllegalArgumentException().isThrownBy(() -> validator.validateTop(TOP, protectedGetter))
                        .withMessageContaining(STR_FIELD).withMessageContaining(GET_MSG);

        /*
         * getter is a "void" function - should throw an exception.
         */
        class VoidGetter {
            @NotNull
            String strValue;

            @SuppressWarnings("unused")
            public void getStrValue() {
                // do nothing
            }
        }

        VoidGetter voidGetter = new VoidGetter();
        assertThatIllegalArgumentException().isThrownBy(() -> validator.validateTop(TOP, voidGetter))
                        .withMessageContaining(STR_FIELD).withMessageContaining(GET_MSG);

        /*
         * getter takes an argument - should throw an exception.
         */
        class ArgGetter {
            @NotNull
            String strValue;

            @SuppressWarnings("unused")
            public String getStrValue(String echo) {
                return echo;
            }
        }

        ArgGetter argGetter = new ArgGetter();
        assertThatIllegalArgumentException().isThrownBy(() -> validator.validateTop(TOP, argGetter))
                        .withMessageContaining(STR_FIELD).withMessageContaining(GET_MSG);
    }


    private void assertInvalid(String testName, BeanValidationResult result, String fieldName, String message) {
        String text = result.getResult();
        assertNotNull(testName, text);
        assertTrue(testName, text.contains(fieldName));
        assertTrue(testName, text.contains(message));
    }

    /**
     * Annotated static field.
     */
    private static class AnnotFieldStatic {
        @NotNull
        static String strValue;
    }

    /**
     * Annotated class with a static field.
     */
    @NotNull
    private static class AnnotClassStatic {
        @SuppressWarnings("unused")
        static String strValue;
    }

    private static class StaticGetter {
        @NotNull
        String strValue;

        @SuppressWarnings("unused")
        public static String getStrValue() {
            return STRING_VALUE;
        }
    }
}
