/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.ia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.ia.jpa.IntegrityAuditEntity;
import org.onap.policy.common.utils.jpa.EntityTransCloser;
import org.onap.policy.common.utils.time.TestTime;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class DbDAOTest extends IntegrityAuditTestBase {
    private static String resourceName = "pdp0";

    private DbDAO dbDao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        IntegrityAuditTestBase.setUpBeforeClass(DEFAULT_DB_URL_PREFIX + DbDAOTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDownAfterClass() {
        IntegrityAuditTestBase.tearDownAfterClass();
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        dbDao = null;
    }

    /**
     * Tear down after test cases.
     */
    @Override
    @After
    public void tearDown() {
        if (dbDao != null) {
            dbDao.destroy();
        }

        super.tearDown();
    }

    /* Tests registering a new IntegrityAuditEntity object in the DB */
    @Test
    public void testNewRegistration() throws Exception {
        Properties properties = makeProperties();

        try (EntityTransCloser et = new EntityTransCloser(em.getTransaction())) {
            dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);

            // Find the proper entry in the database
            Query iaequery = em.createQuery(
                    "Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
            iaequery.setParameter("rn", DbDAOTest.resourceName);
            iaequery.setParameter("pu", DbDAOTest.A_SEQ_PU);

            @SuppressWarnings("rawtypes")
            List iaeList = iaequery.getResultList();

            // Assert that the IntegrityAuditEntity object was found
            assertNotNull(iaeList);

            // flush to the DB
            em.flush();
            et.commit();
        }
    }

    /*
     * Tests updating an IntegrityAuditEntity if it has already been registered
     */
    @Test
    public void testUpdateRegistration() throws Exception {
        Properties properties = makeProperties();

        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);

        // Change site_name in properties to test that an update was made to
        // an existing entry in the table
        properties.put(IntegrityAuditProperties.SITE_NAME, "SiteB");
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);

        try (EntityTransCloser et = new EntityTransCloser(em.getTransaction())) {
            // Find the proper entry in the database
            Query iaequery = em.createQuery(
                    "Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
            iaequery.setParameter("rn", DbDAOTest.resourceName);
            iaequery.setParameter("pu", DbDAOTest.A_SEQ_PU);

            @SuppressWarnings("rawtypes")
            List iaeList = iaequery.getResultList();
            IntegrityAuditEntity iae = null;
            if (!iaeList.isEmpty()) {
                // ignores multiple results
                iae = (IntegrityAuditEntity) iaeList.get(0);

                em.refresh(iae);
                em.persist(iae);

                // flush to the DB
                em.flush();

                // commit transaction
                et.commit();

                // Assert that the site_name for the existing entry was updated
                assertEquals("SiteB", iae.getSite());
            }
        }
    }

    /* Tests obtaining all Integrity Audit Entities from a table */
    @Test
    public void testGetIntegrityAuditEntities() throws Exception {
        Properties properties = makeProperties();

        // Add some entries to the DB
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
        new DbDAO("pdp1", A_SEQ_PU, properties).destroy();
        properties.put(IntegrityAuditProperties.NODE_TYPE, "pdp_drools");
        new DbDAO("pdp2", A_SEQ_PU, properties).destroy();

        List<IntegrityAuditEntity> entities;
        // Obtain entries based on persistenceUnit and nodeType
        entities = dbDao.getIntegrityAuditEntities(A_SEQ_PU, "pdp_xacml");
        assertEquals(2, entities.size());
    }

    /* Tests retrieving a DbDAO instance's IntegrityAuditEntity */
    @Test
    public void testGetMyIntegrityAuditEntity() throws Exception {
        Properties properties = makeProperties();

        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
        IntegrityAuditEntity iae = dbDao.getMyIntegrityAuditEntity();
        // assertEquals("integrityAuditPU", iae.getPersistenceUnit());
        assertEquals(A_SEQ_PU, iae.getPersistenceUnit());
    }

    /* Tests obtaining an IntegrityAuditEntity by ID */
    @Test
    public void testGetIntegrityAuditEntity() throws Exception {
        Properties properties = makeProperties();

        // Obtain an entry from the database based on ID
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);

        // Find the proper database entry
        Query iaequery = em
                .createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
        iaequery.setParameter("rn", DbDAOTest.resourceName);
        iaequery.setParameter("pu", DbDAOTest.A_SEQ_PU);

        @SuppressWarnings("rawtypes")
        List iaeList = iaequery.getResultList();
        IntegrityAuditEntity iae = null;
        if (!iaeList.isEmpty()) {
            // ignores multiple results
            iae = (IntegrityAuditEntity) iaeList.get(0);

            // refresh the object from DB in case cached data was returned
            em.refresh(iae);

            // Obtain ID for an IntegrityAuditEntity
            PersistenceUnitUtil util = emf.getPersistenceUnitUtil();
            Object iaeId = util.getIdentifier(iae);

            // Obtain the same IntegrityAuditEntity based on ID
            IntegrityAuditEntity iaeDuplicate = dbDao.getIntegrityAuditEntity((long) iaeId);
            Object duplicateId = util.getIdentifier(iaeDuplicate);

            // Assert that the proper entry was retrieved based on ID
            assertEquals((long) iaeId, (long) duplicateId);
        }
    }

    /* Tests setting an IntegrityAuditEntity as the designated node */
    @Test
    public void testSetDesignated() throws Exception {
        Properties properties = makeProperties();

        try (EntityTransCloser et = new EntityTransCloser(em.getTransaction())) {
            // Create an entry and set it's designated field to true
            dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
            dbDao.setDesignated(resourceName, A_SEQ_PU, true);

            // Find the proper entry in the database
            Query iaequery = em.createQuery(
                    "Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
            iaequery.setParameter("rn", resourceName);
            iaequery.setParameter("pu", A_SEQ_PU);

            @SuppressWarnings("rawtypes")
            List iaeList = iaequery.getResultList();
            IntegrityAuditEntity iae = null;

            if (!iaeList.isEmpty()) {
                // ignores multiple results
                iae = (IntegrityAuditEntity) iaeList.get(0);
                em.refresh(iae);

                // Check if the node is designated
                boolean result = iae.isDesignated();

                // Assert that it is designated
                assertTrue(result);
            }

            // flush to the DB
            em.flush();

            // close the transaction
            et.commit();
        }
    }

    /* Tests that the lastUpdated column in the database is updated properly */
    @Test
    public void testSetLastUpdated() throws Exception {
        Properties properties = makeProperties();

        try (EntityTransCloser et = new EntityTransCloser(em.getTransaction())) {
            TestTime testTime = new TestTime();
            
            // Create an entry
            dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);

            // Find the proper entry in the database
            Query iaequery = em.createQuery(
                    "Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
            iaequery.setParameter("rn", resourceName);
            iaequery.setParameter("pu", A_SEQ_PU);

            @SuppressWarnings("rawtypes")
            List iaeList = iaequery.getResultList();
            IntegrityAuditEntity iae = null;

            if (!iaeList.isEmpty()) {
                // ignores multiple results
                iae = (IntegrityAuditEntity) iaeList.get(0);
                // refresh the object from DB in case cached data was returned
                em.refresh(iae);

                // Obtain old update value and set new update value
                final Date oldDate = iae.getLastUpdated();

                // ensure dates are different by sleeping for a bit
                testTime.sleep(1);

                iae.setSite("SiteB");
                iae.setLastUpdated(testTime.getDate());
                final Date newDate = iae.getLastUpdated();

                em.persist(iae);
                // flush to the DB
                em.flush();
                // close the transaction
                et.commit();

                // Assert that the old and new update times are different
                assertNotEquals(oldDate, newDate);
            }
        }
    }

    /* Tests that all the entries from a class can be retrieved */
    @Test
    public void testGetAllMyEntriesString() throws Exception {
        Properties properties = makeProperties();

        // create entries for the IntegrityAuditEntity table
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
        new DbDAO("pdp1", A_SEQ_PU, properties).destroy();
        new DbDAO("pdp2", A_SEQ_PU, properties).destroy();

        // Obtain a hash with the persisted objects
        Map<Object, Object> entries = dbDao.getAllMyEntries("org.onap.policy.common.ia.jpa.IntegrityAuditEntity");

        // Assert there were 3 entries for that class
        assertEquals(3, entries.size());
    }

    /*
     * Tests retrieving all entities in a Persistence Unit using the class name and a hashset of IDs
     */
    @Test
    public void testGetAllMyEntriesStringHashSet() throws Exception {
        Properties properties = makeProperties();

        // create entries for the IntegrityAuditEntity table
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
        new DbDAO("pdp1", A_SEQ_PU, properties).destroy();
        new DbDAO("pdp2", A_SEQ_PU, properties).destroy();

        // Obtain all entity keys
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object> cq = cb.createQuery();
        Root<?> rootEntry = cq.from(Class.forName("org.onap.policy.common.ia.jpa.IntegrityAuditEntity"));
        CriteriaQuery<Object> all = cq.select(rootEntry);
        TypedQuery<Object> allQuery = em.createQuery(all);
        List<Object> objectList = allQuery.getResultList();
        HashSet<Object> resultSet = new HashSet<Object>();
        PersistenceUnitUtil util = emf.getPersistenceUnitUtil();
        for (Object o : objectList) {
            Object key = util.getIdentifier(o);
            resultSet.add(key);
        }

        // Obtain a hash with the persisted objects
        Map<Object, Object> entries =
                dbDao.getAllMyEntries("org.onap.policy.common.ia.jpa.IntegrityAuditEntity", resultSet);

        // Assert there were 3 entries for that class
        assertEquals(3, entries.size());
    }

    /*
     * Tests retrieving all entities in a Persistence Unit using the persistence unit, properties,
     * and class name
     */
    @Test
    public void testGetAllEntriesStringPropertiesString() throws Exception {
        Properties properties = makeProperties();

        // create entries for the IntegrityAuditEntity table
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
        new DbDAO("pdp1", A_SEQ_PU, properties).destroy();
        new DbDAO("pdp2", A_SEQ_PU, properties).destroy();

        // Obtain a hash with the persisted objects
        Map<Object, Object> entries = dbDao.getAllEntries("integrityAuditPU", properties,
                "org.onap.policy.common.ia.jpa.IntegrityAuditEntity");

        // Assert there were 3 entries for that class
        assertEquals(3, entries.size());
    }

    /*
     * Tests retrieving all entities in a Persistence Unit using the persistence unit, properties,
     * class name, and a hashset of IDs
     */
    @Test
    public void testGetAllEntriesStringPropertiesStringHashSet() throws Exception {
        Properties properties = makeProperties();

        // create entries for the IntegrityAuditEntity table
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
        new DbDAO("pdp1", A_SEQ_PU, properties).destroy();
        new DbDAO("pdp2", A_SEQ_PU, properties).destroy();

        // Obtain all entity keys
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object> cq = cb.createQuery();
        Root<?> rootEntry = cq.from(Class.forName("org.onap.policy.common.ia.jpa.IntegrityAuditEntity"));
        CriteriaQuery<Object> all = cq.select(rootEntry);
        TypedQuery<Object> allQuery = em.createQuery(all);
        List<Object> objectList = allQuery.getResultList();
        HashSet<Object> resultSet = new HashSet<Object>();
        PersistenceUnitUtil util = emf.getPersistenceUnitUtil();
        for (Object o : objectList) {
            Object key = util.getIdentifier(o);
            resultSet.add(key);
        }

        // Obtain a hash with the persisted objects
        Map<Object, Object> entries = dbDao.getAllEntries("integrityAuditPU", properties,
                "org.onap.policy.common.ia.jpa.IntegrityAuditEntity", resultSet);

        // Assert there were 3 entries for that class
        assertEquals(3, entries.size());
    }

    /*
     * Tests getting all the entries from a class based on persistenceUnit, properties, and
     * className
     */
    @Test
    public void testGetAllEntries() throws Exception {
        Properties properties = makeProperties();

        // create entries for the IntegrityAuditEntity table
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
        new DbDAO("pdp1", A_SEQ_PU, properties).destroy();
        new DbDAO("pdp2", A_SEQ_PU, properties).destroy();

        // Obtain a hash with the persisted objects
        Map<Object, Object> entries =
                dbDao.getAllEntries(A_SEQ_PU, properties, "org.onap.policy.common.ia.jpa.IntegrityAuditEntity");

        // Assert there were 3 entries for that class
        assertEquals(3, entries.size());
    }

    /**
     * Tests obtaining all class names of persisted classes.
     */
    public void testGetPersistenceClassNames() throws Exception {
        Properties properties = makeProperties();

        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);

        // Retrieve persistence class names
        Set<String> result = dbDao.getPersistenceClassNames();
        assertEquals(1, result.size());
    }
}
