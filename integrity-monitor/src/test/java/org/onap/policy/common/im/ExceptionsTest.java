/*
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.im;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.onap.policy.common.im.jmx.ComponentAdminException;
import org.onap.policy.common.utils.test.ExceptionsTester;

/**
 * Tests various Exception subclasses.
 */
class ExceptionsTest extends ExceptionsTester {

    @Test
    void testStateTransitionException() {
        assertEquals(4, test(StateTransitionException.class));
    }

    @Test
    void testStateManagementException() {
        assertEquals(4, test(StateManagementException.class));
    }

    @Test
    void testStandbyStatusException() {
        assertEquals(5, test(StandbyStatusException.class));
    }

    @Test
    void testIntegrityMonitorPropertiesException() {
        assertEquals(4, test(IntegrityMonitorPropertiesException.class));
    }

    @Test
    void testIntegrityMonitorException() {
        assertEquals(5, test(IntegrityMonitorException.class));
    }

    @Test
    void testForwardProgressException() {
        assertEquals(4, test(ForwardProgressException.class));
    }

    @Test
    void testAllSeemsWellException() {
        assertEquals(4, test(AllSeemsWellException.class));
    }

    @Test
    void testAdministrativeStateException() {
        assertEquals(4, test(AdministrativeStateException.class));
    }

    @Test
    void testComponentAdminException() {
        assertEquals(4, test(ComponentAdminException.class));
    }
}
