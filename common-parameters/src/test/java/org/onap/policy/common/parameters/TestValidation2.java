/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.Max;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.testclasses.TestParametersL00;
import org.onap.policy.common.parameters.testclasses.TestParametersL10;

/**
 * Tests validation using annotations from policy-common.
 */
public class TestValidation2 {
    private static final String L0_PARAMETERS = "l0Parameters";

    private static final String NOT_BLANK_STRING_MESSAGE =
                    "field 'notBlankString' type 'java.lang.String' value '' INVALID, must be a non-blank string\n"
                                    .replace('\'', '"');

    private static final String NULL_STRING_MESSAGE =
                    "field 'notNullString' type 'java.lang.String' value 'null' INVALID, is null\n".replace('\'', '"');


    private static final String NOT_BLANK_OBJECT_NAME = "notBlankObject";
    private static final String NOT_BLANK_STRING_NAME = "notBlankString";
    private static final String NOT_NULL_OBJECT_NAME = "notNullObject";
    private static final String NOT_NULL_STRING_NAME = "notNullString";
    private static final String MIN_LONG_NAME = "minLong";
    private static final String MAX_LONG_NAME = "maxLong";

    @NotNull
    private String notNullString;

    @NotNull
    private Object notNullObject;

    @NotBlank
    private String notBlankString;

    @NotBlank
    private Object notBlankObject;

    @Min(value = 10)
    private long minLong;

    @Max(value = 10)
    private long maxLong;

    @Test
    public void testValidationOk() throws IOException {
        TestParametersL00 l0Parameters = new TestParametersL00(L0_PARAMETERS);

        GroupValidationResult validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertTrue(validationResult.isClean());
        assertNull(validationResult.getResult());
        assertEquals(l0Parameters, validationResult.getParameterGroup());
        assertEquals(l0Parameters.getName(), validationResult.getName());

        String expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_0_OK.txt")))
                                        .replaceAll("\\s+", "");
        assertEquals(expectedResult, validationResult.getResult("", "  ", true).replaceAll("\\s+", ""));
    }

    @Test
    public void testValidationObservation() throws IOException {
        TestParametersL00 l0Parameters = new TestParametersL00(L0_PARAMETERS);

        l0Parameters.triggerValidationStatus(ValidationStatus.OBSERVATION, 3);

        String expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_3_Observation.txt")))
                                        .replaceAll("\\s+", "");

        GroupValidationResult validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertFalse(validationResult.isClean());
        assertEquals(expectedResult, validationResult.getResult().replaceAll("\\s+", ""));

        l0Parameters.triggerValidationStatus(ValidationStatus.CLEAN, 3);
        l0Parameters.triggerValidationStatus(ValidationStatus.OBSERVATION, 2);

        expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_2_Observation.txt")))
                                        .replaceAll("\\s+", "");

        validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(expectedResult, validationResult.getResult().replaceAll("\\s+", ""));

        l0Parameters.triggerValidationStatus(ValidationStatus.CLEAN, 3);
        l0Parameters.triggerValidationStatus(ValidationStatus.OBSERVATION, 1);

        expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_1_Observation.txt")))
                                        .replaceAll("\\s+", "");

        validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(expectedResult, validationResult.getResult().replaceAll("\\s+", ""));

        l0Parameters.triggerValidationStatus(ValidationStatus.CLEAN, 3);
        l0Parameters.triggerValidationStatus(ValidationStatus.OBSERVATION, 0);

        validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(null, validationResult.getResult());
    }

    @Test
    public void testValidationWarning() throws IOException {
        TestParametersL00 l0Parameters = new TestParametersL00(L0_PARAMETERS);

        l0Parameters.triggerValidationStatus(ValidationStatus.WARNING, 3);

        String expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_3_Warning.txt")))
                                        .replaceAll("\\s+", "");

        GroupValidationResult validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(expectedResult, validationResult.getResult().replaceAll("\\s+", ""));

        l0Parameters.triggerValidationStatus(ValidationStatus.CLEAN, 3);
        l0Parameters.triggerValidationStatus(ValidationStatus.WARNING, 2);

        expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_2_Warning.txt")))
                                        .replaceAll("\\s+", "");

        validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(expectedResult, validationResult.getResult().replaceAll("\\s+", ""));

        l0Parameters.triggerValidationStatus(ValidationStatus.CLEAN, 3);
        l0Parameters.triggerValidationStatus(ValidationStatus.WARNING, 1);

        expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_1_Warning.txt")))
                                        .replaceAll("\\s+", "");

        validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(expectedResult, validationResult.getResult().replaceAll("\\s+", ""));

        l0Parameters.triggerValidationStatus(ValidationStatus.CLEAN, 3);
        l0Parameters.triggerValidationStatus(ValidationStatus.WARNING, 0);

        validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(null, validationResult.getResult());
    }

    @Test
    public void testValidationInvalid() throws IOException {
        TestParametersL00 l0Parameters = new TestParametersL00(L0_PARAMETERS);

        l0Parameters.triggerValidationStatus(ValidationStatus.INVALID, 3);

        String expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_3_Invalid.txt")))
                                        .replaceAll("\\s+", "");

        GroupValidationResult validationResult = l0Parameters.validate();
        assertFalse(validationResult.isValid());
        assertEquals(expectedResult, validationResult.getResult().replaceAll("\\s+", ""));

        l0Parameters.triggerValidationStatus(ValidationStatus.CLEAN, 3);
        l0Parameters.triggerValidationStatus(ValidationStatus.INVALID, 2);

        expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_2_Invalid.txt")))
                                        .replaceAll("\\s+", "");

        validationResult = l0Parameters.validate();
        assertFalse(validationResult.isValid());
        assertEquals(expectedResult, validationResult.getResult().replaceAll("\\s+", ""));

        l0Parameters.triggerValidationStatus(ValidationStatus.CLEAN, 3);
        l0Parameters.triggerValidationStatus(ValidationStatus.INVALID, 1);

        expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_1_Invalid.txt")))
                                        .replaceAll("\\s+", "");

        validationResult = l0Parameters.validate();
        assertFalse(validationResult.isValid());
        assertEquals(expectedResult, validationResult.getResult().replaceAll("\\s+", ""));

        l0Parameters.triggerValidationStatus(ValidationStatus.CLEAN, 3);
        l0Parameters.triggerValidationStatus(ValidationStatus.INVALID, 0);

        validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(null, validationResult.getResult());
    }

    @Test
    public void testValidationEmptySubGroup() {
        TestParametersL10 l10Parameters = new TestParametersL10("l10Parameters");

        l10Parameters.setL10LGenericNested0(null);

        GroupValidationResult validationResult = l10Parameters.validate();
        assertTrue(validationResult.isValid());

        assertTrue(validationResult.getResult("", "", true).contains("UNDEFINED"));
    }

    @Test
    public void testGetValidationResult() {
        Contained item = new Contained();
        item.setName("item");

        Container cont = new Container();
        cont.item = item;
        GroupValidationResult result = cont.validate();
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertTrue(result.getResult().contains(">= 1"));

        item.minInt = 1000;
        result = cont.validate();
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        cont.item = null;
        result = cont.validate();
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertTrue(result.getResult().contains("is null"));
    }

    @Test
    public void testParameterValidationResult_NotNull() throws Exception {
        ParameterValidationResult result = new ParameterValidationResult(
                        TestValidation2.class.getDeclaredField(NOT_NULL_STRING_NAME), null);
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals(NULL_STRING_MESSAGE, result.getResult());

        // don't allow overwrite - values should remain unchanged
        result.setResult(ValidationStatus.WARNING, "unknown");
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals(NULL_STRING_MESSAGE, result.getResult());

        // non-null should be OK
        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_NULL_STRING_NAME), "");
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        // non-null should be OK
        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_NULL_STRING_NAME), "abc");
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        /*
         * Check plain object fields, too.
         */
        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_NULL_OBJECT_NAME), null);
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals("field 'notNullObject' type 'java.lang.Object' value 'null' INVALID, is null\n".replace('\'', '"'),
                        result.getResult());

        // non-null should be OK
        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_NULL_OBJECT_NAME),
                        new Object());
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        /*
         * Class-level annotation.
         */

        result = new ParameterValidationResult(NotNullSub.class.getDeclaredField(NOT_NULL_STRING_NAME), null);
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals(NULL_STRING_MESSAGE, result.getResult());

        // non-null should be OK
        result = new ParameterValidationResult(NotNullSub.class.getDeclaredField(NOT_NULL_STRING_NAME), "");
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        /*
         * Super class annotation - null should be OK
         */
        result = new ParameterValidationResult(NotNullSub2.class.getDeclaredField("anotherString"), null);
        assertEquals(ValidationStatus.CLEAN, result.getStatus());
    }

    @Test
    public void testParameterValidationResult_NotBlank() throws Exception {
        ParameterValidationResult result =
            new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_BLANK_STRING_NAME), "");
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals(NOT_BLANK_STRING_MESSAGE, result.getResult());

        // spaces only
        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_BLANK_STRING_NAME), " \t");
        assertEquals(ValidationStatus.INVALID, result.getStatus());

        // null should be OK
        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_BLANK_STRING_NAME), null);
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        // null should be OK
        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_BLANK_STRING_NAME), "abc");
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        /*
         * Check plain object fields, too.
         */
        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_BLANK_OBJECT_NAME), null);
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(NOT_BLANK_OBJECT_NAME),
                        new Object());
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        /*
         * Class-level annotation.
         */
        result = new ParameterValidationResult(NotBlankSub.class.getDeclaredField(NOT_BLANK_STRING_NAME), "");
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals(NOT_BLANK_STRING_MESSAGE, result.getResult());

        // non-null should be OK
        result = new ParameterValidationResult(NotBlankSub.class.getDeclaredField(NOT_BLANK_STRING_NAME), "abc");
        assertEquals(ValidationStatus.CLEAN, result.getStatus());
    }

    @Test
    public void testParameterValidationResult_Min() throws Exception {
        ParameterValidationResult result =
                        new ParameterValidationResult(TestValidation2.class.getDeclaredField(MIN_LONG_NAME), 9L);
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals("field 'minLong' type 'long' value '9' INVALID, must be >= 10\n".replace('\'', '"'),
                        result.getResult());

        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(MIN_LONG_NAME), -2);
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals("field 'minLong' type 'long' value '-2' INVALID, must be >= 10\n".replace('\'', '"'),
                        result.getResult());

        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(MIN_LONG_NAME), 10L);
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(MIN_LONG_NAME), 11);
        assertEquals(ValidationStatus.CLEAN, result.getStatus());
    }

    @Test
    public void testParameterValidationResult_Max() throws Exception {
        ParameterValidationResult result =
                        new ParameterValidationResult(TestValidation2.class.getDeclaredField(MAX_LONG_NAME), 11L);
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals("field 'maxLong' type 'long' value '11' INVALID, must be <= 10\n".replace('\'', '"'),
                        result.getResult());

        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(MAX_LONG_NAME), 20);
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertEquals("field 'maxLong' type 'long' value '20' INVALID, must be <= 10\n".replace('\'', '"'),
                        result.getResult());

        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(MAX_LONG_NAME), 10L);
        assertEquals(ValidationStatus.CLEAN, result.getStatus());

        result = new ParameterValidationResult(TestValidation2.class.getDeclaredField(MAX_LONG_NAME), 9);
        assertEquals(ValidationStatus.CLEAN, result.getStatus());
    }

    // these classes are used to test class-level annotations


    private static class EmptyBase {

    }

    private static class NotNullSub extends EmptyBase {
        @NotNull
        @SuppressWarnings("unused")
        private String notNullString;
    }

    private static class NotNullSub2 extends NotNullSub {
        @SuppressWarnings("unused")
        private String anotherString;
    }

    private static class NotBlankSub extends EmptyBase {
        @NotBlank
        @SuppressWarnings("unused")
        private String notBlankString;
    }

    private static class Contained extends ParameterGroupImpl {
        @Min(value = 1)
        private int minInt;

        public Contained() {
            super("Contained");
        }

        @SuppressWarnings("unused")
        public int getMinInt() {
            return minInt;
        }
    }

    private static class Container extends ParameterGroupImpl {
        @NotNull
        private Contained item;

        public Container() {
            super("Container");
        }

        @SuppressWarnings("unused")
        public Contained getItem() {
            return item;
        }
    }
}
