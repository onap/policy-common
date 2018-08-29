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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.onap.policy.common.parameters.ParameterRuntimeException;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.common.parameters.testclasses.EmptyParameterGroup;

public class TestParameterService {

    @Test
    public void testParameterService() {
        ParameterService.clear();

        assertFalse(ParameterService.contains("EmptyGroup"));
        try {
            ParameterService.get("EmptyGroup");
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("\"EmptyGroup\" not found in parameter service", e.getMessage());
        }

        ParameterService.register(new EmptyParameterGroup("Empty Group"));
        assertTrue(ParameterService.contains("Empty Group"));
        assertNotNull(ParameterService.get("Empty Group"));
        
        try {
            ParameterService.register(new EmptyParameterGroup("Empty Group"));
            fail("this test should throw an exception");
        }
        catch (ParameterRuntimeException e) {
            assertEquals("\"Empty Group\" already registered in parameter service", e.getMessage());
        }

        try {
            ParameterService.register(new EmptyParameterGroup("Empty Group"), false);
            fail("this test should throw an exception");
        }
        catch (ParameterRuntimeException e) {
            assertEquals("\"Empty Group\" already registered in parameter service", e.getMessage());
        }

        ParameterService.register(new EmptyParameterGroup("Empty Group"), true);
        assertTrue(ParameterService.contains("Empty Group"));

        ParameterService.deregister("Empty Group");
        assertFalse(ParameterService.contains("Empty Group"));

        ParameterService.register(new EmptyParameterGroup("Empty Group"), true);
        assertTrue(ParameterService.contains("Empty Group"));

        ParameterService.deregister("Empty Group");
        assertFalse(ParameterService.contains("Empty Group"));

        EmptyParameterGroup epg = new EmptyParameterGroup("Empty Group");
        ParameterService.register(epg);
        assertTrue(ParameterService.contains("Empty Group"));
        assertNotNull(ParameterService.get("Empty Group"));

        ParameterService.deregister(epg);
        assertFalse(ParameterService.contains("Empty Group"));

        try {
            ParameterService.deregister("Empty Group");
            fail("this test should throw an exception");
        }
        catch (ParameterRuntimeException e) {
            assertEquals("\"Empty Group\" not registered in parameter service", e.getMessage());
        }

        try {
            ParameterService.get("Empty Group");
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("\"Empty Group\" not found in parameter service", e.getMessage());
        }

        ParameterService.register(new EmptyParameterGroup("Empty Group"));
        assertTrue(ParameterService.contains("Empty Group"));
        assertNotNull(ParameterService.get("Empty Group"));

        assertEquals(1, ParameterService.getAll().size());
        ParameterService.clear();
        assertEquals(0, ParameterService.getAll().size());
        assertFalse(ParameterService.contains("Empty Group"));
        try {
            ParameterService.get("Empty Group");
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("\"Empty Group\" not found in parameter service", e.getMessage());
        }
    }
}
