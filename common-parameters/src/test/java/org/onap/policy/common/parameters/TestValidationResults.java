/*-
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.testclasses.TestParametersL10;
import org.onap.policy.common.parameters.testclasses.TestParametersLGeneric;

public class TestValidationResults {
    private static final String NON_EXISTANT_PARAMETER = "nonExistantParameter";
    private static final String L10L_GENERIC_NESTED_MAP_VAL0 = "l10LGenericNestedMapVal0";
    private static final String L10_INT_FIELD = "l10IntField";
    private static final String ENTRY0 = "entry0";
    private static final String THIS_VALUE_IS_INVALID = "This value is invalid";
    private static final String SOMETHING_WAS_OBSERVED = "Something was observed";
    private static final String PG_MAP = "pgMap";

    private Map<String, ParameterGroup> pgMap = new LinkedHashMap<>();
    private ParameterGroup pg = new TestParametersL10("pg");

    @Before
    public void initMap() {
        pgMap.put(ENTRY0, new TestParametersLGeneric(ENTRY0));
    }

    @Test
    public void testGroupMapValidationResult() throws NoSuchFieldException {
        GroupMapValidationResult result = new GroupMapValidationResult(this.getClass().getDeclaredField(PG_MAP),
                        pgMap);

        assertTrue(result.isValid());
        assertEquals(PG_MAP, result.getName());

        result.setResult(ValidationStatus.OBSERVATION);
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());

        // Once the status is stepped, it can't be reset back because it is the status of map members
        result.setResult(ValidationStatus.CLEAN);
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());

        result.setResult(ValidationStatus.OBSERVATION, SOMETHING_WAS_OBSERVED);
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());
        assertEquals("parameter group map \"pgMap\" OBSERVATION, Something was observed", result.getResult().trim());

        result.setResult(ENTRY0, new GroupValidationResult(pgMap.get(ENTRY0)));
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());
        assertEquals("parameter group map \"pgMap\" OBSERVATION, Something was observed", result.getResult().trim());

        assertThatThrownBy(() -> result.setResult("nonExistantEntry", new GroupValidationResult(pgMap.get(ENTRY0))))
                        .hasMessage("no entry with name \"nonExistantEntry\" exists");
    }

    @Test
    public void testGroupValidationResult() throws NoSuchFieldException {
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

        result.setResult(ValidationStatus.OBSERVATION, SOMETHING_WAS_OBSERVED);
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.OBSERVATION, result.getStatus());
        assertEquals("parameter group \"pg\" type \"org.onap.policy.common.parameters.testclasses.TestParametersL10\""
                        + " OBSERVATION, Something was observed", result.getResult().trim());

        assertThatThrownBy(() -> result.setResult(NON_EXISTANT_PARAMETER, ValidationStatus.OBSERVATION,
                        SOMETHING_WAS_OBSERVED))
                                        .hasMessage("no parameter field exists for parameter: nonExistantParameter");

        result.setResult(L10_INT_FIELD, ValidationStatus.OBSERVATION, SOMETHING_WAS_OBSERVED);
        assertTrue(result.isValid());

        assertThatThrownBy(() -> result.setResult(NON_EXISTANT_PARAMETER, new GroupValidationResult(pg)))
                        .hasMessage("no nested parameter field exists for parameter: nonExistantParameter");

        assertThatThrownBy(() -> result.setResult(L10_INT_FIELD, new GroupValidationResult(pg)))
                        .hasMessage("parameter is not a nested group parameter: l10IntField");

        GroupMapValidationResult groupMapResult = new GroupMapValidationResult(
                        this.getClass().getDeclaredField(PG_MAP), pgMap);

        assertThatThrownBy(() -> result.setResult(NON_EXISTANT_PARAMETER, ENTRY0, groupMapResult))
                        .hasMessage("no group map parameter field exists for parameter: nonExistantParameter");

        assertThatThrownBy(() -> result.setResult(L10_INT_FIELD, ENTRY0, groupMapResult))
                        .hasMessage("parameter is not a nested group map parameter: l10IntField");

        result.setResult("l10LGenericNestedMap", L10L_GENERIC_NESTED_MAP_VAL0, ValidationStatus.INVALID,
                        THIS_VALUE_IS_INVALID);
        assertEquals(ValidationStatus.INVALID, result.getStatus());

        assertThatThrownBy(() -> result.setResult(L10_INT_FIELD, L10L_GENERIC_NESTED_MAP_VAL0, ValidationStatus.INVALID,
                        THIS_VALUE_IS_INVALID))
                                        .hasMessage("parameter is not a nested group map parameter: l10IntField");

        assertThatThrownBy(() -> result.setResult(NON_EXISTANT_PARAMETER, L10L_GENERIC_NESTED_MAP_VAL0,
                        ValidationStatus.INVALID, THIS_VALUE_IS_INVALID)).hasMessage(
                                        "no group map parameter field exists for parameter: nonExistantParameter");

        assertThatThrownBy(() -> result.setResult("l10LGenericNestedMap", "NonExistantKey", ValidationStatus.INVALID,
                        THIS_VALUE_IS_INVALID)).hasMessage("no entry with name \"NonExistantKey\" exists");
    }
}
