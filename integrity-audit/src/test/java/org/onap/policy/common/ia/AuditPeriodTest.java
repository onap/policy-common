/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
class AuditPeriodTest extends IntegrityAuditTestBase {

    private static Logger logger = FlexLogger.getLogger(AuditPeriodTest.class);

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        IntegrityAuditTestBase.setUpBeforeClass(DEFAULT_DB_URL_PREFIX + AuditPeriodTest.class.getSimpleName());
    }

    @AfterAll
    public static void tearDownAfterClass() {
        IntegrityAuditTestBase.tearDownAfterClass();
    }

    /**
     * Set up for test case.
     */
    @Override
    @BeforeEach
    public void setUp() {
        logger.info("setUp: Entering");

        super.setUp();

        logger.info("setUp: Exiting");

    }

    /**
     * Tear down after test cases.
     */
    @Override
    @AfterEach
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
    void testNegativeAuditPeriod() throws Exception {

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
    void testZeroAuditPeriod() throws Exception {

        logger.info("testZeroAuditPeriod: Entering");

        properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "0");

        final ExtractAppender logA = watch(debugLogger, "[Aa]waking from (0s) sleep");

        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Wait for
         *
         * 1) audit to generate a bunch of sleep wake sequences.
         */
        String[] awakings = new String[10];
        for (int x = 0; x < awakings.length; ++x) {
            awakings[x] = "0s";
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
    void testLongAuditPeriod() throws Exception {

        logger.info("testLongAuditPeriod: Entering");

        testAuditPeriod(100);
        testAuditPeriod(200);

        logger.info("testLongAuditPeriod: Exiting");
    }

    /**
     * Verifies that audits actually take as long as expected, even with multiple auditors running
     * simultaneously.
     *
     * @param periodSec audit period, in seconds
     * @throws Exception if an error occurs
     * @throws InterruptedException if the thread is interrupted
     */
    private void testAuditPeriod(long periodSec) throws Exception {

        properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, String.valueOf(periodSec));

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
        assertTrue(tmin >= periodSec + AuditThread.AUDIT_COMPLETION_INTERVAL * AuditThread.AUDIT_RESET_CYCLES);

        /*
         * Now run again and ensure it waited long enough between runs.
         */
        tmin = minAuditTime(ia);
        assertTrue(tmin >= periodSec + AuditThread.AUDIT_COMPLETION_INTERVAL * AuditThread.AUDIT_RESET_CYCLES);
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
        AtomicLong tmin = new AtomicLong(Long.MAX_VALUE);

        // create the threads
        for (MyIntegrityAudit p : auditors) {
            Thread auditThread = new Thread() {

                @Override
                public void run() {
                    try {
                        long tbegin = p.getTimeInMillis();
                        runAudit(p);
                        long elapsed = p.getTimeInMillis() - tbegin;

                        synchronized (tmin) {
                            tmin.set(Math.min(tmin.get(), elapsed));
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
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

        return tmin.get();
    }
}
