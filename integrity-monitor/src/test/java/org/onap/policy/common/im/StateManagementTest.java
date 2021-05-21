/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.QueryTimeoutException;
import javax.persistence.TypedQuery;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * All JUnits are designed to run in the local development environment where they have write
 * privileges and can execute time-sensitive tasks.
 */
public class StateManagementTest extends IntegrityMonitorTestBase {
    private static final String LOCKED_DISABLED_FAILED_COLDSTANDBY = "locked,disabled,failed,coldstandby";
    private static final String UNLOCKED_DISABLED_FAILED_COLDSTANDBY = "unlocked,disabled,failed,coldstandby";
    private static final String UNLOCKED_ENABLED_NULL_HOTSTANDBY = "unlocked,enabled,null,hotstandby";
    private static final String UNLOCKED_ENABLED_NULL_NULL = "unlocked,enabled,null,null";
    private static final String UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE = "unlocked,enabled,null,providingservice";
    private static final String TEST_RESOURCE_NAME = "test_resource1";

    private static Logger logger = LoggerFactory.getLogger(StateManagementTest.class);
    //

    @BeforeClass
    public static void setUpClass() throws Exception {
        IntegrityMonitorTestBase.setUpBeforeClass(DEFAULT_DB_URL_PREFIX + StateManagementTest.class.getSimpleName());

    }

    @AfterClass
    public static void tearDownClass() {
        IntegrityMonitorTestBase.tearDownAfterClass();
    }

    @Before
    public void setUp() {
        super.setUpTest();
    }

    @After
    public void tearDown() {
        super.tearDownTest();
    }

    @Test
    public void test() throws Exception {
        logger.info("\n\nlogger.infor StateManagementTest: Entering\n\n");

        // These parameters are in a properties file
        try {
            final StateManagement sm = new StateManagement(emf, TEST_RESOURCE_NAME);
            test_1(sm);
            test_2(sm);
        } catch (final Exception ex) {
            logger.error("Exception: {}", ex.toString());
            throw ex;
        }

        logger.info("\n\nStateManagementTest: Exit\n\n");
    }

    private void test_1(final StateManagement sm) throws StateManagementException, IntegrityMonitorException {
        logger.info("\n??? initial state");
        assertEquals(UNLOCKED_ENABLED_NULL_NULL, makeString(sm));

        logger.info("\n??? test lock()");
        sm.lock();
        assertEquals("locked,enabled,null,null", makeString(sm));

        logger.info("\n??? test unlock()");
        sm.unlock();
        assertEquals(UNLOCKED_ENABLED_NULL_NULL, makeString(sm));

        logger.info("\n??? test enableNotFailed()");
        sm.enableNotFailed();
        assertEquals(UNLOCKED_ENABLED_NULL_NULL, makeString(sm));

        logger.info("\n??? test disableFailed()");
        sm.disableFailed();
        assertEquals("unlocked,disabled,failed,null", makeString(sm));

        // P4 If promote() is called while either the opState is disabled or
        // the adminState is locked,
        // the standbystatus shall transition to coldstandby and a
        // StandbyStatusException shall be thrown
        logger.info("\n??? promote() test case P4");
        sm.disableFailed();
        sm.lock();
        assertThatThrownBy(sm::promote).isInstanceOf(IntegrityMonitorException.class);

        assertEquals(LOCKED_DISABLED_FAILED_COLDSTANDBY, makeString(sm));

        // P3 If promote() is called while standbyStatus is coldstandby, the
        // state shall not transition
        // and a StandbyStatusException shall be thrown
        logger.info("\n??? promote() test case P3");
        assertThatThrownBy(sm::promote).isInstanceOf(IntegrityMonitorException.class);
        assertEquals(LOCKED_DISABLED_FAILED_COLDSTANDBY, makeString(sm));

        // P2 If promote() is called while the standbyStatus is null and the
        // opState is enabled and adminState is unlocked,
        // the state shall transition to providingservice
        logger.info("\n??? promote() test case P2");
        final StateManagement sm2 = new StateManagement(emf, "test_resource2");
        sm2.enableNotFailed();
        sm2.unlock();
        assertEquals(UNLOCKED_ENABLED_NULL_NULL, makeString(sm2));
        sm2.promote();
        assertEquals(UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE, makeString(sm2));

        // P5 If promote() is called while standbyStatus is
        // providingservice, no action is taken
        logger.info("\n??? promote() test case P5");
        sm2.promote();
        assertEquals(UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE, makeString(sm2));

        // D1 If demote() is called while standbyStatus is providingservice,
        // the state shall transition to hotstandby
        logger.info("\n??? demote() test case D1");
        sm2.demote();
        assertEquals(UNLOCKED_ENABLED_NULL_HOTSTANDBY, makeString(sm2));

        // D4 If demote() is called while standbyStatus is hotstandby, no
        // action is taken
        logger.info("\n??? demote() test case D4");
        sm2.demote();
        assertEquals(UNLOCKED_ENABLED_NULL_HOTSTANDBY, makeString(sm2));
    }

    private void test_2(final StateManagement sm) throws StateManagementException, IntegrityMonitorException {
        // D3 If demote() is called while standbyStatus is null and
        // adminState is locked or opState is disabled,
        // the state shall transition to coldstandby
        logger.info("\n??? demote() test case D3");
        final StateManagement sm3 = new StateManagement(emf, "test_resource3");
        sm3.lock();
        sm3.disableFailed();
        sm3.demote();
        assertEquals(LOCKED_DISABLED_FAILED_COLDSTANDBY, makeString(sm3));

        // D5 If demote() is called while standbyStatus is coldstandby, no
        // action is taken
        logger.info("\n??? demote() test case D5");
        sm3.demote();
        assertEquals(LOCKED_DISABLED_FAILED_COLDSTANDBY, makeString(sm3));

        // D2 If demote() is called while standbyStatus is null and
        // adminState is unlocked and opState is enabled,
        // the state shall transition to hotstandby
        logger.info("\n??? demote() test case D2");
        final StateManagement sm4 = new StateManagement(emf, "test_resource4");
        sm4.unlock();
        sm4.enableNotFailed();
        assertEquals(UNLOCKED_ENABLED_NULL_NULL, makeString(sm4));
        sm4.demote();
        assertEquals(UNLOCKED_ENABLED_NULL_HOTSTANDBY, makeString(sm4));

        // P1 If promote() is called while standbyStatus is hotstandby, the
        // state shall transition to providingservice.
        logger.info("\n??? promote() test case P1");
        sm4.promote();
        assertEquals(UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE, makeString(sm4));

        // State change notification
        logger.info("\n??? State change notification test case 1 - lock()");
        final StateChangeNotifier stateChangeNotifier = new StateChangeNotifier();
        sm.addObserver(stateChangeNotifier);
        sm.lock();
        assertEquals(LOCKED_DISABLED_FAILED_COLDSTANDBY, makeString(stateChangeNotifier.getStateManagement()));

        logger.info("\n??? State change notification test case 2 - unlock()");
        sm.unlock();
        assertEquals(UNLOCKED_DISABLED_FAILED_COLDSTANDBY, makeString(stateChangeNotifier.getStateManagement()));

        logger.info("\n??? State change notification test case 3 - enabled()");
        sm.enableNotFailed();
        assertEquals(UNLOCKED_ENABLED_NULL_HOTSTANDBY, makeString(stateChangeNotifier.getStateManagement()));

        logger.info("\n??? State change notification test case 4 - disableFailed()");
        sm.disableFailed();
        assertEquals(UNLOCKED_DISABLED_FAILED_COLDSTANDBY, makeString(stateChangeNotifier.getStateManagement()));

        logger.info("\n??? State change notification test case 5 - demote()");
        sm.demote();
        assertEquals(UNLOCKED_DISABLED_FAILED_COLDSTANDBY, makeString(stateChangeNotifier.getStateManagement()));

        logger.info("\n??? State change notification test case 6 - promote()");
        assertThatThrownBy(sm::promote).isInstanceOf(IntegrityMonitorException.class);
        assertEquals(UNLOCKED_DISABLED_FAILED_COLDSTANDBY, makeString(sm));
    }

    @Test(expected = StateManagementException.class)
    @SuppressWarnings("unchecked")
    public void test_StateManagementInitialization_ThrowException_ifEntityManagerCreateQuerythrowsAnyException()
            throws Exception {
        final EntityManager mockedEm = getMockedEntityManager();
        final EntityManagerFactory mockedEmf = getMockedEntityManagerFactory(mockedEm);

        doThrow(PersistenceException.class).when(mockedEm).createQuery(anyString(),
                any(StateManagementEntity.class.getClass()));

        new StateManagement(mockedEmf, TEST_RESOURCE_NAME);

    }

    @Test(expected = StateManagementException.class)
    @SuppressWarnings("unchecked")
    public void test_StateManagementInitialization_ThrowStateManagementException_ifEntityManagerthrowsAnyException()
            throws Exception {
        final EntityManager mockedEm = getMockedEntityManager();
        final EntityManagerFactory mockedEmf = getMockedEntityManagerFactory(mockedEm);
        final TypedQuery<StateManagementEntity> mockedQuery = mock(TypedQuery.class);

        when(mockedQuery.setFlushMode(Mockito.any())).thenReturn(mockedQuery);
        when(mockedQuery.setLockMode(Mockito.any())).thenReturn(mockedQuery);
        when(mockedEm.createQuery(anyString(), any(StateManagementEntity.class.getClass()))).thenReturn(mockedQuery);

        doThrow(QueryTimeoutException.class).when(mockedQuery).getResultList();

        new StateManagement(mockedEmf, TEST_RESOURCE_NAME);

    }

    private EntityManager getMockedEntityManager() {
        final EntityManager mockedEm = mock(EntityManager.class);
        final EntityTransaction mockedTransaction = mock(EntityTransaction.class);

        when(mockedEm.getTransaction()).thenReturn(mockedTransaction);
        return mockedEm;
    }

    private EntityManagerFactory getMockedEntityManagerFactory(final EntityManager entityManager) {
        final EntityManagerFactory mockedEmf = mock(EntityManagerFactory.class);
        when(mockedEmf.createEntityManager()).thenReturn(entityManager);

        return mockedEmf;

    }

    /**
     * Converts a state element to a comma-separated string.
     *
     * @param sm element to be converted
     * @return a string representing the element
     */
    private String makeString(final StateManagement sm) {
        if (sm == null) {
            return null;
        }

        return sm.getAdminState()
            + ',' + sm.getOpState()
            + ',' + sm.getAvailStatus()
            + ',' + sm.getStandbyStatus();
    }
}
