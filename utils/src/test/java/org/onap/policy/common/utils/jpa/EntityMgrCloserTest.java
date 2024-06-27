/*
 * ============LICENSE_START=======================================================
 * Common Utils
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

package org.onap.policy.common.utils.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EntityMgrCloserTest {

    private EntityManager mgr;


    @BeforeEach
    public void setUp() {
        mgr = mock(EntityManager.class);
    }


    /**
     * Verifies that the constructor does not do anything extra before being closed.
     */
    @Test
    void testEntityMgrCloser() {
        EntityMgrCloser entityMgrCloser = new EntityMgrCloser(mgr);

        assertEquals(mgr, entityMgrCloser.getManager());

        // verify not closed yet
        verify(mgr, never()).close();

        entityMgrCloser.close();

        verify(mgr).close();
    }

    @Test
    void testGetManager() {
        try (EntityMgrCloser c = new EntityMgrCloser(mgr)) {
            assertEquals(mgr, c.getManager());
        }
    }

    /**
     * Verifies that the manager gets closed when close() is invoked.
     */
    @Test
    void testClose() {
        EntityMgrCloser entityMgrCloser = new EntityMgrCloser(mgr);

        entityMgrCloser.close();

        // should be closed
        verify(mgr).close();
    }

    /**
     * Ensures that the manager gets closed when "try" block exits normally.
     */
    @Test
    void testClose_TryWithoutExcept() {
        try (EntityMgrCloser entityMgrCloser = new EntityMgrCloser(mgr)) {
            // No need to do anything in the try block
        }

        verify(mgr).close();
    }

    /**
     * Ensures that the manager gets closed when "try" block throws an exception.
     */
    @Test
    void testClose_TryWithExcept() {
        try {
            try (EntityMgrCloser c = new EntityMgrCloser(mgr)) {
                throw new Exception("expected exception");
            }

        } catch (Exception exception) {
            // Ignore the exception
        }

        verify(mgr).close();
    }

}
