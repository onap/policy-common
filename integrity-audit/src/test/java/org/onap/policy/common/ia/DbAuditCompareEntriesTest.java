/*
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
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
public class DbAuditCompareEntriesTest extends IntegrityAuditTestBase {

    private static Logger logger = FlexLogger.getLogger(DbAuditCompareEntriesTest.class);

    private DbDao dbDao;
    private static String resourceName = "pdp1";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        IntegrityAuditTestBase
                .setUpBeforeClass(DEFAULT_DB_URL_PREFIX + DbAuditCompareEntriesTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDownAfterClass() {
        IntegrityAuditTestBase.tearDownAfterClass();
    }

    /**
     * Set up for test cases.
     */
    @Override
    @Before
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
    @After
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
    public void testSuccessfulComparison() throws Exception {
        logger.info("testSuccessfulComparison: Entering");

        dbDao = new DbDao(resourceName, A_SEQ_PU, makeProperties());
        final DbAudit dbAudit = new DbAudit(dbDao);

        String className = null;
        // There is only one entry IntegrityAuditEntity, but we will check
        // anyway
        Set<String> classNameSet = dbDao.getPersistenceClassNames();
        for (String c : classNameSet) {
            if (c.equals("org.onap.policy.common.ia.jpa.IntegrityAuditEntity")) {
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
        entry1.setJdbcDriver(dbDriver);
        entry1.setJdbcPassword(dbPwd);
        entry1.setJdbcUrl(dbUrl);
        entry1.setJdbcUser(dbUser);
        entry1.setLastUpdated(date);
        entry1.setNodeType(nodeType);
        entry1.setPersistenceUnit(A_SEQ_PU);
        entry1.setResourceName(resourceName1);
        entry1.setSite(siteName);

        entry2.setDesignated(false);
        entry2.setJdbcDriver(dbDriver);
        entry2.setJdbcPassword(dbPwd);
        entry2.setJdbcUrl(dbUrl);
        entry2.setJdbcUser(dbUser);
        entry2.setLastUpdated(date);
        entry2.setNodeType(nodeType);
        entry2.setPersistenceUnit(A_SEQ_PU);
        entry2.setResourceName(resourceName2);
        entry2.setSite(siteName);

        dbAudit.writeAuditDebugLog(className, resourceName1, resourceName2, entry1, entry2);

        HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
        HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();

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
    public void testComparisonError() throws Exception {
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
        entry1.setJdbcDriver(dbDriver);
        entry1.setJdbcPassword(dbPwd);
        entry1.setJdbcUrl(dbUrl);
        entry1.setJdbcUser(dbUser);
        entry1.setLastUpdated(date);
        entry1.setNodeType(nodeType);
        entry1.setPersistenceUnit(A_SEQ_PU);
        entry1.setResourceName(resourceName1);
        entry1.setSite(siteName);

        entry2.setDesignated(true);
        entry2.setJdbcDriver(dbDriver);
        entry2.setJdbcPassword(dbPwd);
        entry2.setJdbcUrl(dbUrl);
        entry2.setJdbcUser(dbUser);
        entry2.setLastUpdated(date);
        entry2.setNodeType(nodeType);
        entry2.setPersistenceUnit(A_SEQ_PU);
        entry2.setResourceName(resourceName2);
        entry2.setSite(siteName);

        HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
        HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();

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
    public void testCompareMissingEntries() throws Exception {
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
        entry1.setJdbcDriver(dbDriver);
        entry1.setJdbcPassword(dbPwd);
        entry1.setJdbcUrl(dbUrl);
        entry1.setJdbcUser(dbUser);
        entry1.setLastUpdated(date);
        entry1.setNodeType(nodeType);
        entry1.setPersistenceUnit(A_SEQ_PU);
        entry1.setResourceName(resourceName1);
        entry1.setSite(siteName);

        entry2.setDesignated(true);
        entry2.setJdbcDriver(dbDriver);
        entry2.setJdbcPassword(dbPwd);
        entry2.setJdbcUrl(dbUrl);
        entry2.setJdbcUser(dbUser);
        entry2.setLastUpdated(date);
        entry2.setNodeType(nodeType);
        entry2.setPersistenceUnit(A_SEQ_PU);
        entry2.setResourceName(resourceName2);
        entry2.setSite(siteName);

        entry3.setDesignated(false);
        entry3.setJdbcDriver(dbDriver);
        entry3.setJdbcPassword(dbPwd);
        entry3.setJdbcUrl(dbUrl);
        entry3.setJdbcUser(dbUser);
        entry3.setLastUpdated(date);
        entry3.setNodeType(nodeType);
        entry3.setPersistenceUnit(A_SEQ_PU);
        entry3.setResourceName(resourceName2);
        entry3.setSite("SiteB");

        entry4.setDesignated(false);
        entry4.setJdbcDriver(dbDriver);
        entry4.setJdbcPassword(dbPwd);
        entry4.setJdbcUrl(dbUrl);
        entry4.setJdbcUser(dbUser);
        entry4.setLastUpdated(date);
        entry4.setNodeType(nodeType);
        entry4.setPersistenceUnit(A_SEQ_PU);
        entry4.setResourceName(resourceName2);
        entry4.setSite("SiteB");

        HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
        HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();

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
    public void testCompareAllHashEntities() throws Exception {
        logger.info("testCompareAllHashEntities: Entering");

        dbDao = new DbDao(resourceName, A_SEQ_PU, makeProperties());
        DbAudit dbAudit = new DbAudit(dbDao);

        Set<String> classNameSet = dbDao.getPersistenceClassNames();
        Set<Object> mismatchResult = new HashSet<Object>();
        for (String className : classNameSet) {
            if (className.equals("org.onap.policy.common.ia.jpa.IntegrityAuditEntity")) {
                final String resourceName1 = resourceName;
                final String resourceName2 = resourceName;

                final IntegrityAuditEntity entry1 = new IntegrityAuditEntity();
                final IntegrityAuditEntity entry2 = new IntegrityAuditEntity();
                Date date = new Date();

                /*
                 * Two entries with the same field values
                 */
                entry1.setDesignated(false);
                entry1.setJdbcDriver(dbDriver);
                entry1.setJdbcPassword(dbPwd);
                entry1.setJdbcUrl(dbUrl);
                entry1.setJdbcUser(dbUser);
                entry1.setLastUpdated(date);
                entry1.setNodeType(nodeType);
                entry1.setPersistenceUnit(A_SEQ_PU);
                entry1.setResourceName(resourceName1);
                entry1.setSite(siteName);

                entry2.setDesignated(false);
                entry2.setJdbcDriver(dbDriver);
                entry2.setJdbcPassword(dbPwd);
                entry2.setJdbcUrl(dbUrl);
                entry2.setJdbcUser(dbUser);
                entry2.setLastUpdated(date);
                entry2.setNodeType(nodeType);
                entry2.setPersistenceUnit(A_SEQ_PU);
                entry2.setResourceName(resourceName2);
                entry2.setSite(siteName);

                HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
                HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();

                myEntries.put("pdp1", entry1);
                theirEntries.put("pdp1", entry2);

                mismatchResult = dbAudit.compareEntries(myEntries, theirEntries);

                /*
                 * Assert there was no mismatches
                 */
                assertTrue(mismatchResult.isEmpty());
            } else if (className.equals("org.onap.policy.common.ia.jpa.IaTestEntity")) {
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
                iate2.setModifiedBy("Zaphod");
                iate2.setModifiedDate(date);

                iate3.setCreatedBy("Zaphod");
                iate3.setModifiedBy("Ford");
                iate3.setModifiedDate(date);

                iate4.setCreatedBy("Ford");
                iate4.setModifiedBy("Ford");
                iate4.setModifiedDate(date);

                HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
                HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();

                myEntries.put("0", iate);
                myEntries.put("1", iate2);
                theirEntries.put("0", iate3);
                theirEntries.put("1", iate4);

                mismatchResult = dbAudit.compareEntries(myEntries, theirEntries);

                /*
                 * Assert that there is 2 mismatches
                 */
                assertEquals(2, mismatchResult.size());
            }
        }

        logger.info("testCompareAllHashEntities: Exit");
    }

    /*
     * Tests that comparison algorithm works for each entity in the database
     */
    @Ignore
    @Test
    public void testCompareAllDbEntities() throws Exception {
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
        Set<Object> mismatchResult = new HashSet<Object>();
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
    public void testEmbeddedClass() throws Exception {
        logger.info("testEmbeddedClasses: Entering");

        dbDao = new DbDao(resourceName, A_SEQ_PU, properties);
        final DbAudit dbAudit = new DbAudit(dbDao);

        String className = null;
        // There is only one entry IntegrityAuditEntity, but we will check
        // anyway
        Set<String> classNameSet = dbDao.getPersistenceClassNames();
        for (String classNameInClassNameSet : classNameSet) {
            if (classNameInClassNameSet.equals("org.onap.policy.common.ia.jpa.IaTestEntity")) {
                className = classNameInClassNameSet;
            }
        }

        final IaTestEntity iate = new IaTestEntity();
        final IaTestEntity iate2 = new IaTestEntity();

        final Date date = new Date();

        PersonSample person = new PersonSample("Ford", "Prefect", 21);
        PersonSample person2 = new PersonSample("Zaphod", "Beeblebrox", 25);

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
        iate.setModifiedBy("Zaphod");
        iate.setModifiedDate(date);
        iate.setPersonTest(person);

        iate2.setCreatedBy("Ford");
        iate2.setModifiedBy("Zaphod");
        iate2.setModifiedDate(date);
        iate2.setPersonTest(person2);

        dbAudit.writeAuditDebugLog(className, "resource1", "resource2", iate, iate2);

        HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
        HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();

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
