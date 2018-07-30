/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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

public class TestValidation {
    @Test
    public void testValidationOk() throws IOException {
        TestParametersL00 l0Parameters = new TestParametersL00("l0Parameters");

        GroupValidationResult validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
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
        TestParametersL00 l0Parameters = new TestParametersL00("l0Parameters");
        
        l0Parameters.triggerValidationStatus(ValidationStatus.OBSERVATION, 3);

        String expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestParametersL0_3_Observation.txt")))
                                        .replaceAll("\\s+", "");

        GroupValidationResult validationResult = l0Parameters.validate();
        assertTrue(validationResult.isValid());
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
        TestParametersL00 l0Parameters = new TestParametersL00("l0Parameters");
        
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
        TestParametersL00 l0Parameters = new TestParametersL00("l0Parameters");
        
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
}
