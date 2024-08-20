/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2024 Nordix Foundation.
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

package org.onap.policy.common.im.jmx;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.management.MBeanServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.im.IntegrityMonitor;
import org.onap.policy.common.im.StateManagement;

class ComponentAdminTest {

    @Mock
    private IntegrityMonitor mockIntegrityMonitor;

    @Mock
    private StateManagement mockStateManagement;

    private ComponentAdmin componentAdmin;
    private MBeanServer mbeanServer;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create an MBeanServer and register it to be found by findMBeanServer
        mbeanServer = ComponentAdmin.createMBeanServer();
        componentAdmin = new ComponentAdmin("TestComponent", mockIntegrityMonitor, mockStateManagement);
    }

    @AfterEach
    void tearDown() throws Exception {
        componentAdmin.unregister();
    }

    @Test
    void testRegisterAndUnregister() throws Exception {
        assertTrue(componentAdmin.isRegistered(), "MBean should be registered");

        componentAdmin.unregister();
        assertFalse(componentAdmin.isRegistered(), "MBean should be unregistered");
    }

    @Test
    void testRegisterWithExistingMBean() throws Exception {
        // Register the MBean again to test re-registration
        componentAdmin.register();
        assertTrue(componentAdmin.isRegistered(), "MBean should still be registered after re-registering");
    }

    @Test
    void testTestMethod() throws Exception {
        doNothing().when(mockIntegrityMonitor).evaluateSanity();

        componentAdmin.test();

        verify(mockIntegrityMonitor, times(1)).evaluateSanity();
    }

    @Test
    void testLockMethod() throws Exception {
        doNothing().when(mockStateManagement).lock();

        componentAdmin.lock();

        verify(mockStateManagement, times(1)).lock();
    }

    @Test
    void testUnlockMethod() throws Exception {
        doNothing().when(mockStateManagement).unlock();

        componentAdmin.unlock();

        verify(mockStateManagement, times(1)).unlock();
    }

    @Test
    void testTestMethodThrowsExceptionWhenIntegrityMonitorIsNull() {
        assertThatThrownBy(() -> {
            new ComponentAdmin("TestComponent", null, mockStateManagement);
        }).hasMessageContaining("null input");
    }

    @Test
    void testLockMethodThrowsExceptionWhenStateManagerIsNull() {
        assertThatThrownBy(() -> {
            new ComponentAdmin("TestComponent", mockIntegrityMonitor, null);
        }).hasMessageContaining("null input");
    }
}
