/*
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.ia.jpa.IntegrityAuditEntity;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 * 
 * If any have been ignored (@Ignore) they will not run at the same time
 * as others. You should run them as JUnits by themselves.
 */
public class DbAuditTest extends IntegrityAuditTestBase {

    private static Logger logger = FlexLogger.getLogger(DbAuditTest.class);

    private static final String resourceName = "pdp1";

    private EntityManagerFactory emf2;
    private EntityManager em2;
    private DbDAO dbDao;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        IntegrityAuditTestBase.setUpBeforeClass(DEFAULT_DB_URL_PREFIX + DbAuditTest.class.getSimpleName());
        IntegrityAuditEntity.setUnitTesting(true);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        IntegrityAuditTestBase.tearDownAfterClass();
        IntegrityAuditEntity.setUnitTesting(false);
    }

    /**
     * Set up for test cases.
     */
    @Override
    @Before
    public void setUp() {
        logger.info("setUp: Entering");

        super.setUp();

        dbDao = null;
        emf2 = null;
        em2 = null;

        logger.info("setUp: Exiting");
    }

    /**
     * Tear down after test cases.
     */
    @Override
    @After
    public void tearDown() {
        logger.info("tearDown: Entering");

        if (dbDao != null) {
            dbDao.destroy();
        }

        if (em2 != null) {
            em2.close();
        }

        if (emf2 != null) {
            emf2.close();
        }

        super.tearDown();

        logger.info("tearDown: Exiting");
    }

    private void createDb(Properties properties) {
        if (emf2 != null) {
            throw new IllegalStateException("DB2 has already been created");
        }

        // open the DB and ensure it stays open until the test completes
        emf2 = Persistence.createEntityManagerFactory(A_SEQ_PU, properties);
        em2 = emf2.createEntityManager();

        truncateTable(properties, A_SEQ_PU, "IntegrityAuditEntity");
    }

    /*
     * Tests printing an error to the log in the event where there are no entities saved in the
     * database
     */
    @Test
    public void noEntitiesTest() throws Exception {
        Properties properties = makeProperties();

        logger.info("noEntitiesTest: Entering");

        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
        dbDao.deleteAllIntegrityAuditEntities();
        try {
            DbAudit dbAudit = new DbAudit(dbDao);
            dbAudit.dbAudit(resourceName, A_SEQ_PU, nodeType);
            fail("found unexpected entities");

        } catch (DbAuditException e) {
            // Ignore expected exception
        }

        logger.info("noEntitiesTest: Exit");
    }

    /*
     * Tests the detection of only one entry in the database
     */
    @Test
    public void oneEntityTest() throws Exception {
        Properties properties = makeProperties();

        logger.info("oneEntityTest: Entering");

        final ExtractAppender log = watch(debugLogger, "DbAudit: Found only (one) IntegrityAuditEntity entry:");

        // Add one entry in the database
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);
        DbAudit dbAudit = new DbAudit(dbDao);
        dbAudit.dbAudit(resourceName, A_SEQ_PU, nodeType);

        List<IntegrityAuditEntity> iaeList = dbDao.getIntegrityAuditEntities(A_SEQ_PU, nodeType);
        logger.info("List size: " + iaeList.size());

        verifyItemsInLog(log, "one");

        logger.info("oneEntityTest: Exit");
    }

    /*
     * Tests reporting mismatches and missing entries using the error log
     */
    @Test
    public void mismatchTest() throws Exception {
        logger.info("mismatchTest: Entering");

        // use new URLs so we get a completely new DB
        String dbUrl = DbAuditTest.dbUrl + "_mismatchTest";
        String dbUrl2 = dbUrl + "2";

        Properties properties = makeProperties();
        properties.put(IntegrityAuditProperties.DB_URL, dbUrl);

        // Properties for DB2
        Properties properties2 = makeProperties();
        properties2.put(IntegrityAuditProperties.DB_URL, dbUrl2);

        /*
         * We must drop and re-create DB1 so that it's sequence generator is in step with the
         * sequence generator for DB2.
         */
        recreateDb1(properties);

        // create/open DB2
        createDb(properties2);

        final ExtractAppender dbglog = watch(debugLogger, "Mismatched entries [(]keys[)]:(.*)");
        final ExtractAppender errlog = watch(errorLogger, "DB Audit: ([0-9])");

        /*
         * Create entries in DB1 & DB2 for the resource of interest
         */
        dbDao = new DbDAO(resourceName, A_SEQ_PU, properties);

        new DbDAO(resourceName, A_SEQ_PU, properties2).destroy();

        /*
         * Entries in DB1, pointing to DB2, except for pdp3
         */
        new DbDAO("pdp2", A_SEQ_PU, properties, dbUrl2).destroy();
        new DbDAO("pdp1", A_SEQ_PU, properties, dbUrl2).destroy();
        new DbDAO("pdp3", A_SEQ_PU, properties).destroy(); // mismatched URL
        new DbDAO("pdp4", A_SEQ_PU, properties, dbUrl2).destroy();

        /*
         * Identical entries in DB2, all pointing to DB2, including pdp3, but leaving out pdp4
         */
        new DbDAO("pdp2", A_SEQ_PU, properties2).destroy();
        new DbDAO("pdp1", A_SEQ_PU, properties2).destroy();
        new DbDAO("pdp3", A_SEQ_PU, properties2).destroy();

        /*
         * Run the DB Audit, once it finds a mismatch and sleeps, update DB1 to have the same entry
         * as DB2 it can be confirmed that the mismatch is resolved
         */
        DbAudit dbAudit = new DbAudit(dbDao);
        dbAudit.dbAudit(resourceName, A_SEQ_PU, nodeType);

        // update pdp3 entry in DB1 to point to DB2
        new DbDAO("pdp3", A_SEQ_PU, properties, dbUrl2).destroy();

        /*
         * Run the audit again and correct the mismatch, the result should be one entry in the
         * mismatchKeySet because of the missing entry from the beginning of the test
         */
        dbAudit.dbAudit(resourceName, A_SEQ_PU, nodeType);

        assertFalse(dbglog.getExtracted().isEmpty());

        String mismatchIndex = dbglog.getExtracted().get(dbglog.getExtracted().size() - 1);
        int mismatchEntries = mismatchIndex.trim().split(",").length;
        logger.info("mismatchTest: mismatchIndex found: '" + mismatchIndex + "'" + " mismatachEntries = "
                + mismatchEntries);

        // Assert there is only one entry index
        assertEquals(1, mismatchEntries);

        // Now check the entry in the error.log
        assertFalse(errlog.getExtracted().isEmpty());

        String mismatchNum = errlog.getExtracted().get(errlog.getExtracted().size() - 1);

        logger.info("mismatchTest: mismatchNum found: '" + mismatchNum + "'");

        // Assert that there are a total of 3 mismatches - 1 between each
        // comparison node.
        assertEquals("3", mismatchNum);

        logger.info("mismatchTest: Exit");
    }

    /**
     * Re-creates DB1, using the specified properties.
     * 
     * @param properties the properties
     */
    private void recreateDb1(Properties properties) {
        em.close();
        emf.close();

        createDb(properties);

        em = em2;
        emf = emf2;

        em2 = null;
        emf2 = null;
    }

}
