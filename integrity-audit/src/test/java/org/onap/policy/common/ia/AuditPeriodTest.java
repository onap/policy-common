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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class AuditPeriodTest extends IntegrityAuditTestBase {

    private static Logger logger = FlexLogger.getLogger(AuditPeriodTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        IntegrityAuditTestBase.setUpBeforeClass(DEFAULT_DB_URL_PREFIX + AuditPeriodTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDownAfterClass() {
        IntegrityAuditTestBase.tearDownAfterClass();
    }

    /**
     * Set up for test case.
     */
    @Override
    @Before
    public void setUp() {
        logger.info("setUp: Entering");

        super.setUp();

        logger.info("setUp: Exiting");

    }

    /**
     * Tear down after test cases.
     */
    @Override
    @After
    public void tearDown() {
        logger.info("tearDown: Entering");

        super.tearDown();

        logger.info("tearDown: Exiting");
    }

    /*
     * Verifies (via log parsing) that when a negative audit period is specified, the audit is
     * suppressed.
     */
    @Test
    public void testNegativeAuditPeriod() throws Exception {

        logger.info("testNegativeAuditPeriod: Entering");

        properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "-1");

        ExtractAppender logA = watch(debugLogger, "Suppressing integrity audit, integrityAuditPeriodSeconds=([^,]*)");

        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Sleep long enough to allow
         * 
         * 1) audit to immediately terminate.
         */
        waitThread(integrityAudit);

        verifyItemsInLog(logA, "-1");

        logger.info("testNegativeAuditPeriod: Exiting");

    }

    /*
     * Verifies (via log parsing) that when an audit period of zero is specified, the audit runs
     * continuously, generating a number of sleep/wake sequences in a short period of time (e.g.
     * 100ms).
     */
    @Test
    public void testZeroAuditPeriod() throws Exception {

        logger.info("testZeroAuditPeriod: Entering");

        properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "0");

        final ExtractAppender logA = watch(debugLogger, "[Aa]waking from (0ms) sleep");

        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Wait for
         * 
         * 1) audit to generate a bunch of sleep wake sequences.
         */
        String[] awakings = new String[10];
        for (int x = 0; x < awakings.length; ++x) {
            awakings[x] = "0ms";
            runAudit(integrityAudit);
        }

        // run a couple more audits
        runAudit(integrityAudit);
        runAudit(integrityAudit);

        /*
         * We should get at least 10 sleep/wake sequences.
         */

        verifyItemsInLog(logA, awakings);

        logger.info("testZeroAuditPeriod: Exiting");

    }

    /**
     * Verifies that when different audit periods are specified, there is an appropriate interval
     * between the audits.
     */
    @Test
    public void testLongAuditPeriod() throws Exception {

        logger.info("testLongAuditPeriod: Entering");

        testAuditPeriod(100);
        testAuditPeriod(200);

        logger.info("testLongAuditPeriod: Exiting");
    }

    /**
     * Verifies that audits actually take as long as expected, even with multiple auditors running
     * simultaneously.
     * 
     * @param periodms audit period, in milliseconds
     * @throws Exception if an error occurs
     * @throws InterruptedException if the thread is interrupted
     */
    private void testAuditPeriod(long periodms) throws Exception, InterruptedException {

        properties.put(IntegrityAuditProperties.AUDIT_PERIOD_MILLISECONDS, String.valueOf(periodms));

        /*
         * Start several auditors.
         */
        MyIntegrityAudit[] ia = new MyIntegrityAudit[3];
        for (int x = 0; x < ia.length; ++x) {
            ia[x] = makeAuditor("pdp" + x, A_SEQ_PU);
        }

        /*
         * Run an audit on all of them.
         */
        runAudit(ia);

        /*
         * Now run again and ensure it waited long enough between runs.
         */
        long tmin = minAuditTime(ia);
        assertTrue(tmin >= periodms + AUDIT_SIMULATION_MS);

        /*
         * Now run again and ensure it waited long enough between runs.
         */
        tmin = minAuditTime(ia);
        assertTrue(tmin >= periodms + AUDIT_SIMULATION_MS);
    }

    /**
     * Runs simultaneous audits on several auditors.
     * 
     * @param auditors the auditors
     * @return the minimum time, in milliseconds, elapsed for any given auditor
     * @throws InterruptedException if the thread is interrupted
     */
    private long minAuditTime(MyIntegrityAudit... auditors) throws InterruptedException {
        List<Thread> threads = new ArrayList<>(auditors.length);
        AtomicLong tfirst = new AtomicLong(Long.MAX_VALUE);
        final long tbeg = System.currentTimeMillis();

        // create the threads
        for (MyIntegrityAudit p : auditors) {
            Thread auditThread = new Thread() {

                @Override
                public void run() {
                    try {
                        runAudit(p);
                        setMinTime(tfirst);

                    } catch (InterruptedException e) {
                        ;
                    }
                }
            };

            auditThread.setDaemon(true);
            threads.add(auditThread);
        }

        // start the threads
        for (Thread t : threads) {
            t.start();
        }

        // wait for them to complete
        for (Thread t : threads) {
            t.join();
        }

        return (tfirst.get() - tbeg);
    }

    /**
     * Sets a value to the minimum between the current value and the current time.
     * 
     * @param tmin current minimum value/value to be set
     */
    private static void setMinTime(AtomicLong tmin) {
        long tcur = System.currentTimeMillis();
        long time;
        while ((time = tmin.get()) > tcur) {
            tmin.compareAndSet(time, tcur);
        }
    }
}
