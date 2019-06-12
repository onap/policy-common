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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.parameters.testclasses.EmptyParameterGroup;

public class TestParameterService {
    private static final String EMPTY_GROUP = "Empty Group";

    @Test
    public void testParameterService() {
        ParameterService.clear();

        assertFalse(ParameterService.contains("EmptyGroup"));

        assertThatThrownBy(() -> ParameterService.get("EmptyGroup"))
                        .hasMessage("\"EmptyGroup\" not found in parameter service");

        ParameterService.register(new EmptyParameterGroup(EMPTY_GROUP));
        assertTrue(ParameterService.contains(EMPTY_GROUP));
        assertNotNull(ParameterService.get(EMPTY_GROUP));

        assertThatThrownBy(() -> ParameterService.register(new EmptyParameterGroup(EMPTY_GROUP)))
                        .hasMessage("\"Empty Group\" already registered in parameter service");

        assertThatThrownBy(() -> ParameterService.register(new EmptyParameterGroup(EMPTY_GROUP), false))
                        .hasMessage("\"Empty Group\" already registered in parameter service");

        ParameterService.register(new EmptyParameterGroup(EMPTY_GROUP), true);
        assertTrue(ParameterService.contains(EMPTY_GROUP));

        ParameterService.deregister(EMPTY_GROUP);
        assertFalse(ParameterService.contains(EMPTY_GROUP));

        ParameterService.register(new EmptyParameterGroup(EMPTY_GROUP), true);
        assertTrue(ParameterService.contains(EMPTY_GROUP));

        ParameterService.deregister(EMPTY_GROUP);
        assertFalse(ParameterService.contains(EMPTY_GROUP));

        EmptyParameterGroup epg = new EmptyParameterGroup(EMPTY_GROUP);
        ParameterService.register(epg);
        assertTrue(ParameterService.contains(EMPTY_GROUP));
        assertNotNull(ParameterService.get(EMPTY_GROUP));

        ParameterService.deregister(epg);
        assertFalse(ParameterService.contains(EMPTY_GROUP));

        assertThatThrownBy(() -> ParameterService.deregister(EMPTY_GROUP))
                        .hasMessage("\"Empty Group\" not registered in parameter service");

        assertThatThrownBy(() -> ParameterService.get(EMPTY_GROUP))
                        .hasMessage("\"Empty Group\" not found in parameter service");

        ParameterService.register(new EmptyParameterGroup(EMPTY_GROUP));
        assertTrue(ParameterService.contains(EMPTY_GROUP));
        assertNotNull(ParameterService.get(EMPTY_GROUP));

        assertEquals(1, ParameterService.getAll().size());
        ParameterService.clear();
        assertEquals(0, ParameterService.getAll().size());
        assertFalse(ParameterService.contains(EMPTY_GROUP));

        assertThatThrownBy(() -> ParameterService.get(EMPTY_GROUP))
                        .hasMessage("\"Empty Group\" not found in parameter service");
    }
}
