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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.Entries;
import org.onap.policy.common.parameters.annotations.Items;
import org.onap.policy.common.parameters.annotations.Max;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Pattern;
import org.onap.policy.common.parameters.annotations.Valid;

public class TestBeanValidator {
    private static final String TOP = "top";
    private static final String STR_FIELD = "strValue";
    private static final String INT_FIELD = "intValue";
    private static final String NUM_FIELD = "numValue";
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
    public void testValidateTop_testValidateMethods() {
        // validate null
        assertTrue(validator.validateTop(TOP, null).isValid());

        // validate something that has no annotations
        assertTrue(validator.validateTop(TOP, validator).isValid());

        final AtomicReference<String> strValue = new AtomicReference<>();
        final AtomicReference<Integer> intValue = new AtomicReference<>();

        @NotNull
        class Data {
            @SuppressWarnings("unused")
            public String strValue() {
                return strValue.get();
            }
        }

        // one failure case
        Data data = new Data();
        strValue.set(null);
        BeanValidationResult result = validator.validateTop(TOP, data);
        assertInvalid("testValidateMethods", result, STR_FIELD, "null");
        assertTrue(result.getResult().contains(TOP));

        // one success case
        strValue.set(STRING_VALUE);
        assertTrue(validator.validateTop(TOP, data).isValid());

        /**
         * Repeat with a subclass.
         */
        @Getter
        class Derived extends Data {
            @Min(10)
            public int intValue() {
                return intValue.get();
            }
        }

        Derived derived = new Derived();
        strValue.set(STRING_VALUE);
        intValue.set(INT_VALUE);

        // success case
        assertTrue(validator.validateTop(TOP, derived).isValid());

        // failure cases
        strValue.set(null);
        assertInvalid("testValidateMethods", validator.validateTop(TOP, derived), STR_FIELD, "null");
        strValue.set(STRING_VALUE);

        intValue.set(1);
        assertInvalid("testValidateMethods", validator.validateTop(TOP, derived), INT_FIELD, "minimum");
        intValue.set(INT_VALUE);

        // both invalid
        strValue.set(null);
        intValue.set(1);
        result = validator.validateTop(TOP, derived);
        assertInvalid("testValidateMethods", result, STR_FIELD, "null");
        assertInvalid("testValidateMethods", result, INT_FIELD, "minimum");
        strValue.set(STRING_VALUE);
        intValue.set(INT_VALUE);
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
    public void testVerRegex() {
        class RegexCheck {
            @Getter
            @Pattern(regexp = "[a-f]*")
            String strValue;
        }

        RegexCheck regexCheck = new RegexCheck();

        // does not match
        regexCheck.strValue = "xyz";
        assertInvalid("testVerRegex", validator.validateTop(TOP, regexCheck), STR_FIELD,
                        "does not match regular expression [a-f]");

        // matches
        regexCheck.strValue = "abcabc";
        assertTrue(validator.validateTop(TOP, regexCheck).isValid());

        // invalid regex
        class InvalidRegexCheck {
            @Getter
            @Pattern(regexp = "[a-f")
            String strValue;
        }

        InvalidRegexCheck invalidRegexCheck = new InvalidRegexCheck();

        // does not match
        invalidRegexCheck.strValue = "abc";
        assertInvalid("testVerRegex", validator.validateTop(TOP, invalidRegexCheck), STR_FIELD,
                        "does not match regular expression [a-f");

        // matches
        regexCheck.strValue = "abcabc";
        assertTrue(validator.validateTop(TOP, regexCheck).isValid());

        /*
         * Class with "regex" annotation on an integer.
         */
        class RegexInt {
            @Getter
            @Pattern(regexp = "[a-f]*")
            int intValue;
        }

        RegexInt regexInt = new RegexInt();
        regexInt.intValue = 0;
        assertInvalid("testVerRegex", validator.validateTop(TOP, regexInt), INT_FIELD,
                        "does not match regular expression [a-f]");
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

    @Test
    public void testVerCascade() {
        class Item {
            @Getter
            @NotNull
            Integer intValue;
        }

        @Getter
        class Container {
            @Valid
            Item checked;

            Item unchecked;

            @Valid
            List<Item> items;

            @Valid
            Map<String, Item> itemMap;
        }

        Container cont = new Container();
        cont.unchecked = new Item();
        cont.items = List.of(new Item());
        cont.itemMap = Map.of(STRING_VALUE, new Item());

        cont.checked = null;
        assertTrue(validator.validateTop(TOP, cont).isValid());

        cont.checked = new Item();

        assertInvalid("testVerCascade", validator.validateTop(TOP, cont), INT_FIELD, "null");

        cont.checked.intValue = INT_VALUE;
        assertTrue(validator.validateTop(TOP, cont).isValid());
    }

    @Test
    public void testVerCollection() {
        @Getter
        class Container {
            @Items(min = @Min(5))
            List<Integer> items;

            // not a collection - should not be checked
            @Items(valid = {@Valid})
            String strValue;

            String noAnnotations;
        }

        Container cont = new Container();
        cont.strValue = STRING_VALUE;
        cont.noAnnotations = STRING_VALUE;

        // null collection - always valid
        assertTrue(validator.validateTop(TOP, cont).isValid());

        // empty collection - always valid
        cont.items = List.of();
        assertTrue(validator.validateTop(TOP, cont).isValid());

        cont.items = List.of(-10, -20);
        assertThat(validator.validateTop(TOP, cont).getResult()).contains("\"0\"", "-10", "\"1\"", "-20", "minimum");

        cont.items = List.of(10, -30);
        assertThat(validator.validateTop(TOP, cont).getResult()).contains("\"1\"", "-30", "minimum")
                        .doesNotContain("\"0\"");

        cont.items = List.of(10, 20);
        assertTrue(validator.validateTop(TOP, cont).isValid());
    }

    @Test
    public void testVerMap() {
        @Getter
        class Container {
            @Entries(key = @Items(), value = @Items(min = {@Min(5)}))
            Map<String, Integer> items;

            // not a map - should not be checked
            @Entries(key = @Items(), value = @Items(min = {@Min(5)}))
            String strValue;

            String noAnnotations;
        }

        Container cont = new Container();
        cont.strValue = STRING_VALUE;
        cont.noAnnotations = STRING_VALUE;

        // null map - always valid
        assertTrue(validator.validateTop(TOP, cont).isValid());

        // empty map - always valid
        cont.items = Map.of();
        assertTrue(validator.validateTop(TOP, cont).isValid());

        cont.items = Map.of("abc", -10, "def", -20);
        assertThat(validator.validateTop(TOP, cont).getResult()).contains("abc", "-10", "def", "-20", "minimum");

        cont.items = Map.of("abc", 10, "def", -30);
        assertThat(validator.validateTop(TOP, cont).getResult()).contains("def", "-30", "minimum")
                        .doesNotContain("abc");

        cont.items = Map.of("abc", 10, "def", 20);
        assertTrue(validator.validateTop(TOP, cont).isValid());
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


    private void assertInvalid(String testName, BeanValidationResult result, String... text) {
        assertThat(result.getResult()).describedAs(testName).contains(text);
    }
}
