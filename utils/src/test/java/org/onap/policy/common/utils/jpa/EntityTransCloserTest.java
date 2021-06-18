/*
 * ============LICENSE_START=======================================================
 * Common Utils
 * ================================================================================
 * Copyright (C) 2018-2021 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityTransaction;
import org.junit.Before;
import org.junit.Test;

public class EntityTransCloserTest {

    private EntityTransaction trans;

    /**
     * Set up EntityTransaction mock.
     */
    @Before
    public void setUp() {
        trans = mock(EntityTransaction.class);

        when(trans.isActive()).thenReturn(true);
    }



    /**
     * Verifies that the constructor starts a transaction, but does not do anything extra before
     * being closed.
     */
    @Test
    public void testEntityTransCloser() {
        EntityTransCloser entityTransCloser = new EntityTransCloser(trans);

        assertEquals(trans, entityTransCloser.getTransaction());

        // verify that transaction was started
        verify(trans).begin();

        // verify not closed, committed, or rolled back yet
        verify(trans, never()).commit();
        verify(trans, never()).rollback();

        entityTransCloser.close();

        verify(trans).rollback();
    }

    @Test
    public void testGetTransation() {
        try (EntityTransCloser t = new EntityTransCloser(trans)) {
            assertEquals(trans, t.getTransaction());
        }
    }

    /**
     * Verifies that the transaction is rolled back and the manager is closed when and a transaction
     * is active.
     */
    @Test
    public void testClose_Active() {
        EntityTransCloser entityTransCloser = new EntityTransCloser(trans);

        when(trans.isActive()).thenReturn(true);

        entityTransCloser.close();

        // closed and rolled back, but not committed
        verify(trans, never()).commit();
        verify(trans).rollback();
    }

    /**
     * Verifies that the manager is closed, but that the transaction is <i>not</i> rolled back and
     * when and no transaction is active.
     */
    @Test
    public void testClose_Inactive() {
        EntityTransCloser entityTransCloser = new EntityTransCloser(trans);

        when(trans.isActive()).thenReturn(false);

        entityTransCloser.close();

        // closed, but not committed or rolled back
        verify(trans, never()).commit();
        verify(trans, never()).rollback();
    }

    /**
     * Verifies that the manager is closed and the transaction rolled back when "try" block exits
     * normally and a transaction is active.
     */
    @Test
    public void testClose_TryWithoutExcept_Active() {
        when(trans.isActive()).thenReturn(true);

        try (EntityTransCloser entityTransCloser = new EntityTransCloser(trans)) {
            // No need to do anything in the try block
        }

        // closed and rolled back, but not committed
        verify(trans, never()).commit();
        verify(trans).rollback();
    }

    /**
     * Verifies that the manager is closed, but that the transaction is <i>not</i> rolled back when
     * "try" block exits normally and no transaction is active.
     */
    @Test
    public void testClose_TryWithoutExcept_Inactive() {
        when(trans.isActive()).thenReturn(false);

        try (EntityTransCloser entityTransCloser = new EntityTransCloser(trans)) {
            // No need to do anything in the try block
        }

        // closed, but not rolled back or committed
        verify(trans, never()).commit();
        verify(trans, never()).rollback();
    }

    /**
     * Verifies that the manager is closed and the transaction rolled back when "try" block throws
     * an exception and a transaction is active.
     */
    @Test
    public void testClose_TryWithExcept_Active() {
        when(trans.isActive()).thenReturn(true);

        try {
            try (EntityTransCloser t = new EntityTransCloser(trans)) {
                throw new Exception("expected exception");
            }

        } catch (Exception e) {
            // Ignore the exception
        }

        // closed and rolled back, but not committed
        verify(trans, never()).commit();
        verify(trans).rollback();
    }

    /**
     * Verifies that the manager is closed, but that the transaction is <i>not</i> rolled back when
     * "try" block throws an exception and no transaction is active.
     */
    @Test
    public void testClose_TryWithExcept_Inactive() {
        when(trans.isActive()).thenReturn(false);

        try {
            try (EntityTransCloser t = new EntityTransCloser(trans)) {
                throw new Exception("expected exception");
            }

        } catch (Exception e) {
            // Ignore the exception
        }

        // closed, but not rolled back or committed
        verify(trans, never()).commit();
        verify(trans, never()).rollback();
    }

    /**
     * Verifies that commit() only commits, and that the subsequent close() does not re-commit.
     */
    @Test
    public void testCommit() {
        EntityTransCloser entityTransCloser = new EntityTransCloser(trans);

        entityTransCloser.commit();

        // committed, but not closed or rolled back
        verify(trans).commit();
        verify(trans, never()).rollback();

        // closed, but not re-committed
        entityTransCloser.close();

        verify(trans, times(1)).commit();
    }

    /**
     * Verifies that rollback() only rolls back, and that the subsequent close() does not re-roll
     * back.
     */
    @Test
    public void testRollback() {
        EntityTransCloser entityTransCloser = new EntityTransCloser(trans);

        entityTransCloser.rollback();

        // rolled back, but not closed or committed
        verify(trans, never()).commit();
        verify(trans).rollback();

        // closed, but not re-rolled back
        when(trans.isActive()).thenReturn(false);
        entityTransCloser.close();

        verify(trans, times(1)).rollback();
    }

}
