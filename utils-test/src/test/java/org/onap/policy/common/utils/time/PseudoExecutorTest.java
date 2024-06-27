/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.common.utils.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PseudoExecutorTest {
    private int invoked;
    private PseudoExecutor executor;

    @BeforeEach
    public void setUp() {
        invoked = 0;
        executor = new PseudoExecutor();
    }

    @Test
    void test() {
        assertEquals(0, executor.getQueueLength());
        assertEquals(0, executor.getTasks().size());
        assertTrue(executor.runAll(0));

        executor.execute(() -> invoked++);
        executor.execute(() -> invoked++);
        executor.execute(() -> invoked++);
        assertEquals(3, executor.getTasks().size());
        assertEquals(3, executor.getQueueLength());

        assertFalse(executor.runAll(2));
        assertEquals(2, invoked);
        assertEquals(1, executor.getQueueLength());

        assertTrue(executor.runAll(2));
        assertEquals(3, invoked);
        assertEquals(0, executor.getQueueLength());
    }
}
