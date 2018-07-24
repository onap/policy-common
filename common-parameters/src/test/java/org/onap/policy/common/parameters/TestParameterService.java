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

public class TestParameterService {

    @Test
    public void testParameterService() {
        ParameterService.clear();

        assertFalse(ParameterService.existsParameters(LegalParameters.class));
        try {
            ParameterService.getParameters(LegalParameters.class);
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals(
                    "Parameters for org.onap.policy.common.parameters.LegalParameters not found in parameter service",
                    e.getMessage());
        }

        ParameterService.registerParameters(new LegalParameters());
        assertTrue(ParameterService.existsParameters(LegalParameters.class));
        assertNotNull(ParameterService.getParameters(LegalParameters.class));

        ParameterService.deregisterParameters(LegalParameters.class);

        assertFalse(ParameterService.existsParameters(LegalParameters.class));
        try {
            ParameterService.getParameters(LegalParameters.class);
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals(
                    "Parameters for org.onap.policy.common.parameters.LegalParameters not found in parameter service",
                    e.getMessage());
        }

        ParameterService.registerParameters(new LegalParameters());
        assertTrue(ParameterService.existsParameters(LegalParameters.class));
        assertNotNull(ParameterService.getParameters(LegalParameters.class));

        assertEquals(1, ParameterService.getAll().size());
        ParameterService.clear();
        assertEquals(0, ParameterService.getAll().size());
        assertFalse(ParameterService.existsParameters(LegalParameters.class));
        try {
            ParameterService.getParameters(LegalParameters.class);
            fail("Test should throw an exception here");
        } catch (final Exception e) {
            assertEquals(
                    "Parameters for org.onap.policy.common.parameters.LegalParameters not found in parameter service",
                    e.getMessage());
        }
    }
}
