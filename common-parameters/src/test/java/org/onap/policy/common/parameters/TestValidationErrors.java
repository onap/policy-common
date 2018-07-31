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

import org.junit.Test;
import org.onap.policy.common.parameters.testclasses.ParameterGroupWithArray;
import org.onap.policy.common.parameters.testclasses.ParameterGroupWithCollection;
import org.onap.policy.common.parameters.testclasses.ParameterGroupWithIllegalMapKey;
import org.onap.policy.common.parameters.testclasses.ParameterGroupWithIllegalMapValue;
import org.onap.policy.common.parameters.testclasses.ParameterGroupWithNullCollection;
import org.onap.policy.common.parameters.testclasses.ParameterGroupWithNullMapValue;
import org.onap.policy.common.parameters.testclasses.ParameterGroupWithParameterGroupCollection;

public class TestValidationErrors {
    @Test
    public void testBadArrayValidation() {
        ParameterGroupWithArray groupWithArray = new ParameterGroupWithArray("Illegal Array Group");
        assertTrue(groupWithArray.isValid());
    }

    @Test
    public void testCollectionValidation() {
        ParameterGroupWithCollection legalCollection = new ParameterGroupWithCollection("Legal Collection");
        assertTrue(legalCollection.isValid());

        ParameterGroupWithParameterGroupCollection illegalCollection = new ParameterGroupWithParameterGroupCollection(
                        "Illegal Collection");
        try {
            illegalCollection.isValid();
            fail("test should throw an exception");
        } catch (ParameterRuntimeException e) {
            assertEquals("collection parameter \"parameterGroupArrayList\" is illegal,"
                            + " parameter groups are not allowed as collection members", e.getMessage());
        }
    }

    @Test
    public void testNullCollection() {
        ParameterGroupWithNullCollection nullCollection = new ParameterGroupWithNullCollection("Null Collection");

        try {
            nullCollection.isValid();
            fail("test should throw an exception");
        } catch (ParameterRuntimeException e) {
            assertEquals("collection parameter \"nullList\" is null", e.getMessage());
        }
    }

    @Test
    public void testMapNullValueValidation() {
        ParameterGroupWithNullMapValue nullMap = new ParameterGroupWithNullMapValue("Null Map value");
        try {
            nullMap.isValid();
            fail("test should throw an exception");
        } catch (ParameterRuntimeException e) {
            assertEquals("map parameter \"nullMap\" is null", e.getMessage());
        }
    }

    @Test
    public void testBadMapKeyValidation() {
        ParameterGroupWithIllegalMapKey illegalMap = new ParameterGroupWithIllegalMapKey("Illegal Map");
        try {
            illegalMap.isValid();
            fail("test should throw an exception");
        } catch (ParameterRuntimeException e) {
            assertEquals("map entry is not a parameter group keyed by a string, key \"1\" "
                            + "in map \"badMap\" is not a string", e.getMessage());
        }
    }

    @Test
    public void testBadMapValueValidation() {
        ParameterGroupWithIllegalMapValue illegalMap = new ParameterGroupWithIllegalMapValue("Illegal Map");
        try {
            illegalMap.isValid();
            fail("test should throw an exception");
        } catch (ParameterRuntimeException e) {
            assertEquals("map entry is not a parameter group keyed by a string, value \"1\" in "
                            + "map \"intMap\" is not a parameter group", e.getMessage());
        }
    }
}
