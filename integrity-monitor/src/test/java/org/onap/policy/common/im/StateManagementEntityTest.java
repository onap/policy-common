/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;

import jakarta.persistence.Query;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.onap.policy.common.utils.jpa.EntityTransCloser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateManagementEntityTest extends IntegrityMonitorTestBase {
    private static final Logger logger = LoggerFactory.getLogger(StateManagementEntityTest.class);

    /**
     * Set up for the test class.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        IntegrityMonitorTestBase
                .setUpBeforeClass(DEFAULT_DB_URL_PREFIX + StateManagementEntityTest.class.getSimpleName());

    }

    /**
     * Tear down after the test class.
     */
    @AfterClass
    public static void tearDownClass() {
        IntegrityMonitorTestBase.tearDownAfterClass();
    }

    /**
     * Set up for the test cases.
     */
    @Before
    public void setUp() {
        super.setUpTest();
    }

    /**
     * Tear down after the test cases.
     */
    @After
    public void tearDown() {
        super.tearDownTest();
    }

    @Test
    public void testJpa() throws Exception {
        logger.debug("\n??? logger.infor StateManagementEntityTest: Entering\n\n");

        // Define the resourceName for the StateManagement constructor
        String resourceName = "test_resource1";

        //
        logger.debug("Create StateManagementEntity, resourceName: {}", resourceName);
        logger.debug("??? instantiate StateManagementEntity object");
        StateManagementEntity sme = new StateManagementEntity();

        logger.debug("??? setResourceName : {}", resourceName);
        sme.setResourceName(resourceName);
        logger.debug("??? getResourceName : {}", sme.getResourceName());

        sme.setAdminState(StateManagement.UNLOCKED);
        assertEquals(StateManagement.UNLOCKED, sme.getAdminState());

        sme.setOpState(StateManagement.ENABLED);
        assertEquals(StateManagement.ENABLED, sme.getOpState());

        sme.setAvailStatus(StateManagement.NULL_VALUE);
        assertEquals(StateManagement.NULL_VALUE, sme.getAvailStatus());

        sme.setStandbyStatus(StateManagement.COLD_STANDBY);
        assertEquals(StateManagement.COLD_STANDBY, sme.getStandbyStatus());

        try (EntityTransCloser et = new EntityTransCloser(em.getTransaction())) {
            logger.debug("??? before persist");
            em.persist(sme);
            logger.debug("??? after  persist");

            em.flush();
            logger.debug("??? after flush");

            et.commit();
            logger.debug("??? after commit");
        }

        Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");

        query.setParameter("resource", resourceName);

        // Just test that we are retrieving the right object
        @SuppressWarnings("rawtypes")
        List resourceList = query.getResultList();
        if (!resourceList.isEmpty()) {
            // exist
            StateManagementEntity sme2 = (StateManagementEntity) resourceList.get(0);

            assertEquals(sme.getResourceName(), sme2.getResourceName());
            assertEquals(sme.getAdminState(), sme2.getAdminState());
            assertEquals(sme.getOpState(), sme2.getOpState());
            assertEquals(sme.getAvailStatus(), sme2.getAvailStatus());
            assertEquals(sme.getStandbyStatus(), sme2.getStandbyStatus());
            logger.debug("--");
        } else {
            logger.debug("Record not found, resourceName: {}", resourceName);
        }

        logger.debug("\n\nJpaTest: Exit\n\n");
    }
}
