/*-
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.testclasses.TestParametersL10;
import org.onap.policy.common.parameters.testclasses.TestParametersLGeneric;

public class TestValidationResults {
    private Map<String, ParameterGroup> pgMap = new LinkedHashMap<>();
    private ParameterGroup pg = new TestParametersL10("pg");

    @Before
    public void initMap() {
        pgMap.put("entry0", new TestParametersLGeneric("entry0"));
    }

    @Test
    public void testGroupMapValidationResult() throws NoSuchFieldException, SecurityException {
        GroupMapValidationResult result = new GroupMapValidationResult(this.getClass().getDeclaredField("pgMap"),
                        pgMap);

        assertTrue(result.isValid());
        assertEquals("pgMap", result.getName());

        result.setResult(ValidationStatus.OBSERVATION);
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());

        // Once the status is stepped, it can't be reset back because it is the status of map members
        result.setResult(ValidationStatus.CLEAN);
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());

        result.setResult(ValidationStatus.OBSERVATION, "Something was observed");
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());
        assertEquals("parameter group map \"pgMap\" OBSERVATION, Something was observed", result.getResult().trim());

        result.setResult("entry0", new GroupValidationResult(pgMap.get("entry0")));
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());
        assertEquals("parameter group map \"pgMap\" OBSERVATION, Something was observed", result.getResult().trim());

        try {
            result.setResult("nonExistantEntry", new GroupValidationResult(pgMap.get("entry0")));
            fail("test shold throw an exception here");
        } catch (Exception e) {
            assertEquals("no entry with name \"nonExistantEntry\" exists", e.getMessage());
        }
    }

    @Test
    public void testGroupValidationResult() throws NoSuchFieldException, SecurityException {
        GroupValidationResult result = new GroupValidationResult(pg);

        assertTrue(result.isValid());
        assertEquals(pg, result.getParameterGroup());
        assertEquals("pg", result.getName());

        result.setResult(ValidationStatus.OBSERVATION);
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());

        // Once the status is stepped, it can't be reset back because it is the status of map members
        result.setResult(ValidationStatus.CLEAN);
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());

        result.setResult(ValidationStatus.OBSERVATION, "Something was observed");
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());
        assertEquals("parameter group \"pg\" type \"org.onap.policy.common.parameters.testclasses.TestParametersL10\""
                        + " OBSERVATION, Something was observed", result.getResult().trim());

        try {
            result.setResult("nonExistantParameter", ValidationStatus.OBSERVATION, "Something was observed");
            fail("test shold throw an exception here");
        } catch (Exception e) {
            assertEquals("no regular parameter field exists for parameter: nonExistantParameter", e.getMessage());
        }

        try {
            result.setResult("l10LGenericNestedMap", ValidationStatus.OBSERVATION, "Something was observed");
            fail("test shold throw an exception here");
        } catch (Exception e) {
            assertEquals("parameter not a regular parameter: l10LGenericNestedMap", e.getMessage());
        }

        try {
            result.setResult("nonExistantParameter", new GroupValidationResult(pg));
            fail("test shold throw an exception here");
        } catch (Exception e) {
            assertEquals("no nested parameter field exists for parameter: nonExistantParameter", e.getMessage());
        }

        try {
            result.setResult("l10IntField", new GroupValidationResult(pg));
            fail("test shold throw an exception here");
        } catch (Exception e) {
            assertEquals("parameter is not a nested group parameter: l10IntField", e.getMessage());
        }

        GroupMapValidationResult groupMapResult = new GroupMapValidationResult(
                        this.getClass().getDeclaredField("pgMap"), pgMap);

        try {
            result.setResult("nonExistantParameter", "entry0", groupMapResult);
            fail("test shold throw an exception here");
        } catch (Exception e) {
            assertEquals("no group map parameter field exists for parameter: nonExistantParameter", e.getMessage());
        }

        try {
            result.setResult("l10IntField", "entry0", groupMapResult);
            fail("test shold throw an exception here");
        } catch (Exception e) {
            assertEquals("parameter is not a nested group map parameter: l10IntField", e.getMessage());
        }
    }
}
