/*
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.ia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.ia.jpa.IaTestEntity;
import org.onap.policy.common.ia.jpa.IntegrityAuditEntity;
import org.onap.policy.common.ia.jpa.PersonSample;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
class DbAuditCompareEntriesTest extends IntegrityAuditTestBase {
    private static final String ZAPHOD = "Zaphod";

    private static Logger logger = FlexLogger.getLogger(DbAuditCompareEntriesTest.class);

    private DbDao dbDao;
    private static String resourceName = "pdp1";

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        IntegrityAuditTestBase
                .setUpBeforeClass(DEFAULT_DB_URL_PREFIX + DbAuditCompareEntriesTest.class.getSimpleName());
    }

    @AfterAll
    public static void tearDownAfterClass() {
        IntegrityAuditTestBase.tearDownAfterClass();
    }

    /**
     * Set up for test cases.
     */
    @Override
    @BeforeEach
    public void setUp() {

        logger.info("setUp: Entering");

        super.setUp();

        truncateTables(makeProperties());

        logger.info("setUp: Exiting");
    }

    /**
     * Clean up DB after each test.
     */
    @Override
    @AfterEach
    public void tearDown() {
        logger.info("tearDown: Entering");

        dbDao.destroy();

        super.tearDown();

        logger.info("tearDown: Exiting");
    }

    /*
     * Tests that a comparison between hashsets is successful if the entries match
     */
    // @Ignore
    @Test
    void testSuccessfulComparison() throws Exception {
        logger.info("testSuccessfulComparison: Entering");

        dbDao = new DbDao(resourceName, A_SEQ_PU, makeProperties());
        final DbAudit dbAudit = new DbAudit(dbDao);

        String className = null;
        // There is only one entry IntegrityAuditEntity, but we will check
        // anyway
        Set<String> classNameSet = dbDao.getPersistenceClassNames();
        for (String c : classNameSet) {
            if ("org.onap.policy.common.ia.jpa.IntegrityAuditEntity".equals(c)) {
                className = c;
            }
        }
        final String resourceName1 = resourceName;
        final String resourceName2 = resourceName;

        final IntegrityAuditEntity entry1 = new IntegrityAuditEntity();
        final IntegrityAuditEntity entry2 = new IntegrityAuditEntity();
        Date date = new Date();

        /*
         * Two entries with the same field values
         */
        entry1.setDesignated(false);
        entry1.setJdbcDriver(DB_DRIVER);
        entry1.setJdbcPassword(DB_PASS);
        entry1.setJdbcUrl(dbUrl);
        entry1.setJdbcUser(DB_USER);
        entry1.setLastUpdated(date);
        entry1.setNodeType(NODE_TYPE);
        entry1.setPersistenceUnit(A_SEQ_PU);
        entry1.setResourceName(resourceName1);
        entry1.setSite(SITE_NAME);

        entry2.setDesignated(false);
        entry2.setJdbcDriver(DB_DRIVER);
        entry2.setJdbcPassword(DB_PASS);
        entry2.setJdbcUrl(dbUrl);
        entry2.setJdbcUser(DB_USER);
        entry2.setLastUpdated(date);
        entry2.setNodeType(NODE_TYPE);
        entry2.setPersistenceUnit(A_SEQ_PU);
        entry2.setResourceName(resourceName2);
        entry2.setSite(SITE_NAME);

        dbAudit.writeAuditDebugLog(className, resourceName1, resourceName2, entry1, entry2);

        HashMap<Object, Object> myEntries = new HashMap<>();
        HashMap<Object, Object> theirEntries = new HashMap<>();

        myEntries.put("pdp1", entry1);
        theirEntries.put("pdp1", entry2);

        Set<Object> result = dbAudit.compareEntries(myEntries, theirEntries);

        /*
         * Assert that there are no mismatches returned
         */
        assertTrue(result.isEmpty());

        logger.info("testSuccessfulComparison: Exit");
    }

    /*
     * Tests that an error is detected if an entry in one hashset doesn't match the other
     */
    // @Ignore
    @Test
    void testComparisonError() throws Exception {
        logger.info("testComparisonError: Entering");

        dbDao = new DbDao(resourceName, A_SEQ_PU, makeProperties());
        final DbAudit dbAudit = new DbAudit(dbDao);

        final String resourceName1 = resourceName;
        final String resourceName2 = resourceName;

        final IntegrityAuditEntity entry1 = new IntegrityAuditEntity();
        final IntegrityAuditEntity entry2 = new IntegrityAuditEntity();
        Date date = new Date();

        /*
         * Create two entries with different designated values
         */
        entry1.setDesignated(false);
        entry1.setJdbcDriver(DB_DRIVER);
        entry1.setJdbcPassword(DB_PASS);
        entry1.setJdbcUrl(dbUrl);
        entry1.setJdbcUser(DB_USER);
        entry1.setLastUpdated(date);
        entry1.setNodeType(NODE_TYPE);
        entry1.setPersistenceUnit(A_SEQ_PU);
        entry1.setResourceName(resourceName1);
        entry1.setSite(SITE_NAME);

        entry2.setDesignated(true);
        entry2.setJdbcDriver(DB_DRIVER);
        entry2.setJdbcPassword(DB_PASS);
        entry2.setJdbcUrl(dbUrl);
        entry2.setJdbcUser(DB_USER);
        entry2.setLastUpdated(date);
        entry2.setNodeType(NODE_TYPE);
        entry2.setPersistenceUnit(A_SEQ_PU);
        entry2.setResourceName(resourceName2);
        entry2.setSite(SITE_NAME);

        HashMap<Object, Object> myEntries = new HashMap<>();
        HashMap<Object, Object> theirEntries = new HashMap<>();

        myEntries.put("pdp1", entry1);
        theirEntries.put("pdp1", entry2);

        Set<Object> result = dbAudit.compareEntries(myEntries, theirEntries);

        /*
         * Assert that there was one mismatch
         */
        assertEquals(1, result.size());

        logger.info("testComparisonError: Exit");
    }

    /*
     * Tests that a mismatch/miss entry is detected if there are missing entries in one or both of
     * the hashsets
     */
    // @Ignore
    @Test
    void testCompareMissingEntries() throws Exception {
        logger.info("testCompareMissingEntries: Entering");

        dbDao = new DbDao(resourceName, A_SEQ_PU, makeProperties());
        final DbAudit dbAudit = new DbAudit(dbDao);

        final String resourceName1 = resourceName;
        final String resourceName2 = resourceName;

        final IntegrityAuditEntity entry1 = new IntegrityAuditEntity();
        final IntegrityAuditEntity entry2 = new IntegrityAuditEntity();
        final IntegrityAuditEntity entry3 = new IntegrityAuditEntity();
        final IntegrityAuditEntity entry4 = new IntegrityAuditEntity();

        Date date = new Date();

        /*
         * 4 entries, one mismatch, two miss entries
         */
        entry1.setDesignated(false);
        entry1.setJdbcDriver(DB_DRIVER);
        entry1.setJdbcPassword(DB_PASS);
        entry1.setJdbcUrl(dbUrl);
        entry1.setJdbcUser(DB_USER);
        entry1.setLastUpdated(date);
        entry1.setNodeType(NODE_TYPE);
        entry1.setPersistenceUnit(A_SEQ_PU);
        entry1.setResourceName(resourceName1);
        entry1.setSite(SITE_NAME);

        entry2.setDesignated(true);
        entry2.setJdbcDriver(DB_DRIVER);
        entry2.setJdbcPassword(DB_PASS);
        entry2.setJdbcUrl(dbUrl);
        entry2.setJdbcUser(DB_USER);
        entry2.setLastUpdated(date);
        entry2.setNodeType(NODE_TYPE);
        entry2.setPersistenceUnit(A_SEQ_PU);
        entry2.setResourceName(resourceName2);
        entry2.setSite(SITE_NAME);

        entry3.setDesignated(false);
        entry3.setJdbcDriver(DB_DRIVER);
        entry3.setJdbcPassword(DB_PASS);
        entry3.setJdbcUrl(dbUrl);
        entry3.setJdbcUser(DB_USER);
        entry3.setLastUpdated(date);
        entry3.setNodeType(NODE_TYPE);
        entry3.setPersistenceUnit(A_SEQ_PU);
        entry3.setResourceName(resourceName2);
        entry3.setSite("SiteB");

        entry4.setDesignated(false);
        entry4.setJdbcDriver(DB_DRIVER);
        entry4.setJdbcPassword(DB_PASS);
        entry4.setJdbcUrl(dbUrl);
        entry4.setJdbcUser(DB_USER);
        entry4.setLastUpdated(date);
        entry4.setNodeType(NODE_TYPE);
        entry4.setPersistenceUnit(A_SEQ_PU);
        entry4.setResourceName(resourceName2);
        entry4.setSite("SiteB");

        HashMap<Object, Object> myEntries = new HashMap<>();
        HashMap<Object, Object> theirEntries = new HashMap<>();

        myEntries.put("0", entry1);
        myEntries.put("1", entry3);
        theirEntries.put("0", entry2);
        theirEntries.put("2", entry4);

        Set<Object> mismatchResult = dbAudit.compareEntries(myEntries, theirEntries);

        /*
         * Assert 3 mismatches/missing entries were found
         */
        assertEquals(3, mismatchResult.size());

        logger.info("testCompareMissingEntries: Exit");
    }

    /*
     * Tests that comparison algorithm works for each entity in the hashsets
     */
    // @Ignore
    @Test
    void testCompareAllHashEntities() throws Exception {
        logger.info("testCompareAllHashEntities: Entering");

        dbDao = new DbDao(resourceName, A_SEQ_PU, makeProperties());
        DbAudit dbAudit = new DbAudit(dbDao);

        Set<String> classNameSet = dbDao.getPersistenceClassNames();
        for (String className : classNameSet) {
            if ("org.onap.policy.common.ia.jpa.IntegrityAuditEntity".equals(className)) {
                final String resourceName1 = resourceName;
                final String resourceName2 = resourceName;

                final IntegrityAuditEntity entry1 = new IntegrityAuditEntity();
                final IntegrityAuditEntity entry2 = new IntegrityAuditEntity();
                Date date = new Date();

                /*
                 * Two entries with the same field values
                 */
                entry1.setDesignated(false);
                entry1.setJdbcDriver(DB_DRIVER);
                entry1.setJdbcPassword(DB_PASS);
                entry1.setJdbcUrl(dbUrl);
                entry1.setJdbcUser(DB_USER);
                entry1.setLastUpdated(date);
                entry1.setNodeType(NODE_TYPE);
                entry1.setPersistenceUnit(A_SEQ_PU);
                entry1.setResourceName(resourceName1);
                entry1.setSite(SITE_NAME);

                entry2.setDesignated(false);
                entry2.setJdbcDriver(DB_DRIVER);
                entry2.setJdbcPassword(DB_PASS);
                entry2.setJdbcUrl(dbUrl);
                entry2.setJdbcUser(DB_USER);
                entry2.setLastUpdated(date);
                entry2.setNodeType(NODE_TYPE);
                entry2.setPersistenceUnit(A_SEQ_PU);
                entry2.setResourceName(resourceName2);
                entry2.setSite(SITE_NAME);

                HashMap<Object, Object> myEntries = new HashMap<>();
                HashMap<Object, Object> theirEntries = new HashMap<>();

                myEntries.put("pdp1", entry1);
                theirEntries.put("pdp1", entry2);

                /*
                 * Assert there was no mismatches
                 */
                assertTrue(dbAudit.compareEntries(myEntries, theirEntries).isEmpty());
            } else if ("org.onap.policy.common.ia.jpa.IaTestEntity".equals(className)) {
                final IaTestEntity iate = new IaTestEntity();
                final IaTestEntity iate2 = new IaTestEntity();
                final IaTestEntity iate3 = new IaTestEntity();
                final IaTestEntity iate4 = new IaTestEntity();

                Date date = new Date();

                /*
                 * Four entries, 2 mismatches
                 */
                iate.setCreatedBy("Ford");
                iate.setModifiedBy("Ford");
                iate.setModifiedDate(date);

                iate2.setCreatedBy("Ford");
                iate2.setModifiedBy(ZAPHOD);
                iate2.setModifiedDate(date);

                iate3.setCreatedBy(ZAPHOD);
                iate3.setModifiedBy("Ford");
                iate3.setModifiedDate(date);

                iate4.setCreatedBy("Ford");
                iate4.setModifiedBy("Ford");
                iate4.setModifiedDate(date);

                HashMap<Object, Object> myEntries = new HashMap<>();
                HashMap<Object, Object> theirEntries = new HashMap<>();

                myEntries.put("0", iate);
                myEntries.put("1", iate2);
                theirEntries.put("0", iate3);
                theirEntries.put("1", iate4);


                /*
                 * Assert that there is 2 mismatches
                 */
                assertEquals(2, dbAudit.compareEntries(myEntries, theirEntries).size());
            }
        }

        logger.info("testCompareAllHashEntities: Exit");
    }

    /*
     * Tests that comparison algorithm works for each entity in the database
     */
    @Test
    void testCompareAllDbEntities() throws Exception {
        logger.info("testCompareAllDbEntities: Entering");

        logger.info("Setting up DB");

        IntegrityAudit.setUnitTesting(true);

        Properties properties = makeProperties();

        Properties properties2 = makeProperties();
        properties2.put(IntegrityAuditProperties.DB_URL,
                "jdbc:h2:mem:" + DbAuditCompareEntriesTest.class.getSimpleName() + "2");

        // Clean up the two DBs
        truncateTables(properties);
        truncateTables(properties2);

        // Add entries into DB1
        dbDao = new DbDao(resourceName, A_SEQ_PU, properties);
        new DbDao("pdp2", A_SEQ_PU, properties).destroy();
        DbAudit dbAudit = new DbAudit(dbDao);

        // Add entries into DB2
        DbDao dbDao3 = new DbDao(resourceName, A_SEQ_PU, properties2);
        new DbDao("pdp2", A_SEQ_PU, properties2).destroy();

        // Pull all entries and compare
        Set<String> classNameSet = dbDao.getPersistenceClassNames();
        Map<Object, Object> myEntries;
        Map<Object, Object> theirEntries;
        Set<Object> mismatchResult = new HashSet<>();
        for (String className : classNameSet) {
            logger.info("classNameSet entry = " + className);
            myEntries = dbDao.getAllEntries(A_SEQ_PU, properties, className);
            theirEntries = dbDao3.getAllEntries(A_SEQ_PU, properties2, className);
            mismatchResult = dbAudit.compareEntries(myEntries, theirEntries);
            if (className.contains("IntegrityAuditEntity")) {
                break;
            }
        }

        dbDao3.destroy();

        // Assert that there is 2 mismatches between IntegrityAuditEntity tables
        assertEquals(2, mismatchResult.size());

        logger.info("testCompareAllDbEntities: Exit");
    }

    /**
     * Truncate the tables.
     *
     * @param properties the properties
     */
    private void truncateTables(Properties properties) {
        truncateTable(properties, A_SEQ_PU, "IntegrityAuditEntity");
        truncateTable(properties, A_SEQ_PU, "IaTestEntity");
    }

    /*
     * Tests that differences in embedded classes are still caught
     */
    // @Ignore
    @Test
    void testEmbeddedClass() throws Exception {
        logger.info("testEmbeddedClasses: Entering");

        dbDao = new DbDao(resourceName, A_SEQ_PU, properties);
        final DbAudit dbAudit = new DbAudit(dbDao);

        String className = null;
        // There is only one entry IntegrityAuditEntity, but we will check
        // anyway
        Set<String> classNameSet = dbDao.getPersistenceClassNames();
        for (String classNameInClassNameSet : classNameSet) {
            if ("org.onap.policy.common.ia.jpa.IaTestEntity".equals(classNameInClassNameSet)) {
                className = classNameInClassNameSet;
            }
        }

        final IaTestEntity iate = new IaTestEntity();
        final IaTestEntity iate2 = new IaTestEntity();

        final Date date = new Date();

        PersonSample person = new PersonSample("Ford", "Prefect", 21);
        PersonSample person2 = new PersonSample(ZAPHOD, "Beeblebrox", 25);

        /*
         * Silly tests to bump coverage stats, not sure why they are counting PersonSample to begin
         * with. Will have to look into that at some point.
         */
        assertNotEquals(person.getAge(), person2.getAge());
        assertNotEquals(person.getFirstName(), person2.getFirstName());
        assertNotEquals(person.getLasttName(), person2.getLasttName());
        PersonSample personTest = new PersonSample(null, null, 0);
        personTest.setAge(person.getAge());
        personTest.setFirstName(person.getFirstName());
        personTest.setLastName(person.getLasttName());
        /*
         * Two entries, 1 mismatch
         */
        iate.setCreatedBy("Ford");
        iate.setModifiedBy(ZAPHOD);
        iate.setModifiedDate(date);
        iate.setPersonTest(person);

        iate2.setCreatedBy("Ford");
        iate2.setModifiedBy(ZAPHOD);
        iate2.setModifiedDate(date);
        iate2.setPersonTest(person2);

        dbAudit.writeAuditDebugLog(className, "resource1", "resource2", iate, iate2);

        HashMap<Object, Object> myEntries = new HashMap<>();
        HashMap<Object, Object> theirEntries = new HashMap<>();

        myEntries.put("0", iate);
        theirEntries.put("0", iate2);

        Set<Object> result = dbAudit.compareEntries(myEntries, theirEntries);

        /*
         * Assert that there are no mismatches returned
         */
        assertTrue(!result.isEmpty());

        logger.info("testEmbeddedClasses: Exit");
    }
}
