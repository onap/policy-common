/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ServiceManagerExceptionTest {
    private ServiceManagerException sme;

    @Test
    public void testServiceManagerException() {
        sme = new ServiceManagerException();
        assertNull(sme.getMessage());
        assertNull(sme.getCause());
    }

    @Test
    public void testServiceManagerExceptionString() {
        sme = new ServiceManagerException("hello");
        assertEquals("hello", sme.getMessage());
        assertNull(sme.getCause());
    }

    @Test
    public void testServiceManagerExceptionThrowable() {
        Throwable thrown = new Throwable("expected exception");
        sme = new ServiceManagerException(thrown);
        assertNotNull(sme.getMessage());
        assertSame(thrown, sme.getCause());
    }

    @Test
    public void testServiceManagerExceptionStringThrowable() {
        Throwable thrown = new Throwable("another expected exception");
        sme = new ServiceManagerException("world", thrown);
        assertEquals("world", sme.getMessage());
        assertSame(thrown, sme.getCause());
    }

}
