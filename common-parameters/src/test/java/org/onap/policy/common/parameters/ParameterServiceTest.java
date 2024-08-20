/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2024 Nordix Foundation
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParameterServiceTest {

    // Sample implementation of the ParameterGroup interface for testing
    private static class TestParameterGroup implements ParameterGroup {
        private final String name;

        TestParameterGroup(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public void setName(String name) {
            // do nothing
        }

        @Override
        public BeanValidationResult validate() {
            return null;
        }

        @Override
        public boolean isValid() {
            return ParameterGroup.super.isValid();
        }
    }

    @BeforeEach
    void setUp() {
        // Clear the ParameterService before each test
        ParameterService.clear();
    }

    @Test
    void testRegisterAndRetrieveParameterGroup() {
        TestParameterGroup group = new TestParameterGroup("testGroup");

        ParameterService.register(group);
        ParameterGroup retrievedGroup = ParameterService.get("testGroup");

        assertEquals(group, retrievedGroup, "The retrieved group should be the same as the registered group.");
    }

    @Test
    void testRegisterDuplicateParameterGroupThrowsException() {
        TestParameterGroup group = new TestParameterGroup("testGroup");

        ParameterService.register(group);

        TestParameterGroup testGroup = new TestParameterGroup("testGroup");
        assertThrows(ParameterRuntimeException.class, () -> {
            ParameterService.register(testGroup);
        }, "Registering a duplicate parameter group should throw an exception.");
    }

    @Test
    void testRegisterWithOverwrite() {
        TestParameterGroup group1 = new TestParameterGroup("testGroup");
        TestParameterGroup group2 = new TestParameterGroup("testGroup");

        ParameterService.register(group1);
        ParameterService.register(group2, true); // Overwrite the existing group

        ParameterGroup retrievedGroup = ParameterService.get("testGroup");
        assertEquals(group2, retrievedGroup,
            "The retrieved group should be the newly registered group after overwrite.");
    }

    @Test
    void testDeregisterParameterGroupByName() {
        TestParameterGroup group = new TestParameterGroup("testGroup");

        ParameterService.register(group);
        ParameterService.deregister("testGroup");

        assertThrows(ParameterRuntimeException.class, () -> {
            ParameterService.get("testGroup");
        }, "Deregistering a parameter group should remove it from the service.");
    }

    @Test
    void testDeregisterParameterGroupByInstance() {
        TestParameterGroup group = new TestParameterGroup("testGroup");

        ParameterService.register(group);
        ParameterService.deregister(group);

        assertThrows(ParameterRuntimeException.class, () -> {
            ParameterService.get("testGroup");
        }, "Deregistering a parameter group by instance should remove it from the service.");
    }

    @Test
    void testContainsParameterGroup() {
        TestParameterGroup group = new TestParameterGroup("testGroup");

        ParameterService.register(group);

        assertTrue(ParameterService.contains("testGroup"), "The parameter group should be contained in the service.");
        assertFalse(ParameterService.contains("nonExistentGroup"),
            "A non-existent parameter group should not be contained in the service.");
    }

    @Test
    void testGetAllParameterGroups() {
        TestParameterGroup group1 = new TestParameterGroup("group1");
        TestParameterGroup group2 = new TestParameterGroup("group2");

        ParameterService.register(group1);
        ParameterService.register(group2);

        Set<Map.Entry<String, ParameterGroup>> allGroups = ParameterService.getAll();
        assertEquals(2, allGroups.size(), "There should be exactly 2 parameter groups in the service.");
        assertTrue(allGroups.stream().anyMatch(entry -> entry.getKey().equals("group1")),
            "The service should contain group1.");
        assertTrue(allGroups.stream().anyMatch(entry -> entry.getKey().equals("group2")),
            "The service should contain group2.");
    }

    @Test
    void testClearParameterGroups() {
        TestParameterGroup group = new TestParameterGroup("testGroup");

        ParameterService.register(group);
        ParameterService.clear();

        assertFalse(ParameterService.contains("testGroup"), "All parameter groups should be cleared from the service.");
    }
}
