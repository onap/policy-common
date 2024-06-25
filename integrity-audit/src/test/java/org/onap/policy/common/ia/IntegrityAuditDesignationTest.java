/*
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;

/**
 * All JUnits are designed to run in the local development environment where they have write
 * privileges and can execute time-sensitive tasks.
 * 
 * <p>Many of the test verification steps are performed by scanning for items written to the log
 * file. Rather than actually scan the log file, an {@link ExtractAppender} is used to monitor
 * events that are logged and extract relevant items. In order to attach the appender to the debug
 * log, it assumes that the debug log is a <i>logback</i> Logger configured per EELF.
 * 
 * <p>These tests use a temporary, in-memory DB, which is dropped once the tests complete.
 */
class IntegrityAuditDesignationTest extends IntegrityAuditTestBase {

    private static Logger logger = FlexLogger.getLogger(IntegrityAuditDesignationTest.class);

    /**
     * Matches specified PDPs in the debug log. A regular expression matching the desired PDPs
     * should be appended, followed by a right parenthesis. For example:
     * 
     * <pre>
     * <code>new ExtractAppender(START_AUDIT_RE_PREFIX + "pdp[124])")
     * </code>
     * </pre>
     */
    private static final String START_AUDIT_RE_PREFIX = "Running audit for persistenceUnit=\\w+ on resourceName=(";

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        IntegrityAuditTestBase
                .setUpBeforeClass(DEFAULT_DB_URL_PREFIX + IntegrityAuditDesignationTest.class.getSimpleName());
    }

    @AfterAll
    public static void tearDownAfterClass() {
        IntegrityAuditTestBase.tearDownAfterClass();
    }

    /**
     * Set up before test cases.
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
     * Tests designation logic when only one functioning resource is in play. Designation should
     * stay with single resource.
     * 
     * Note: console.log must be examined to ascertain whether or not this test was successful.
     */
    @Test
    void testOneResource() throws Exception {

        logger.info("testOneResource: Entering");

        final ExtractAppender logA = watch(debugLogger, START_AUDIT_RE);

        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Wait for
         * 
         * 1) pdp1 to run audit
         * 
         * 2) Logic to detect that no other node is available for designation
         * 
         * 3) pdp1 to run audit again
         */
        runAudit(integrityAudit);
        waitStaleAndRun(integrityAudit);

        logger.info("testOneResource: Stopping audit thread");
        integrityAudit.stopAuditThread();

        verifyItemsInLog(logA, "pdp1");

        /*
         * Test fix for ONAPD2TD-783: Audit fails to run when application is restarted.
         */
        integrityAudit.startAuditThread();

        /*
         * Sleep long enough to allow
         * 
         * 1) pdp1 to run audit
         * 
         * 2) Logic to detect that no other node is available for designation
         * 
         * 3) pdp1 to run audit again
         */
        runAudit(integrityAudit);
        waitStaleAndRun(integrityAudit);

        verifyItemsInLog(logA, "pdp1");

        logger.info("testOneResource: Exiting");

    }

    /*
     * Tests designation logic when two functioning resources are in play. Designation should
     * alternate between resources.
     * 
     * Note: console.log must be examined to ascertain whether or not this test was successful.
     */
    @Test
    void testTwoResources() throws Exception {

        logger.info("testTwoResources: Entering");

        final ExtractAppender logA = watch(debugLogger, START_AUDIT_RE);

        /*
         * Start audit for pdp1.
         */
        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Start audit for pdp2.
         */
        MyIntegrityAudit integrityAudit2 = makeAuditor("pdp2", A_SEQ_PU);

        /*
         * Sleep long enough to allow
         * 
         * 1) pdp1 to run audit
         * 
         * 2) Logic to detect that pdp1 is stale and designate pdp2
         * 
         * 3) pdp2 to run audit
         * 
         * 4) Logic to detect that pdp2 is stale and designate pdp1
         * 
         * 5) pdp1 to run audit
         */
        runAudit(integrityAudit);
        waitStaleAndRun(integrityAudit2);
        waitStaleAndRun(integrityAudit);

        verifyItemsInLog(logA, "pdp1", "pdp2", "pdp1");

        logger.info("testTwoResources: Exiting");

    }

    /*
     * Tests designation logic when two functioning resources are in play, each with different PUs.
     * Audits for persistenceUnit and PU_B should run simultaneously. Designation should not
     * alternate.
     * 
     * Note: console.log must be examined to ascertain whether or not this test was successful.
     */
    @Test
    void testTwoResourcesDifferentPus() throws Exception {

        logger.info("testTwoResourcesDifferentPus: Entering");

        final ExtractAppender logA = watch(debugLogger, START_AUDIT_RE_PREFIX + "pdp1)");
        final ExtractAppender logB = watch(debugLogger, START_AUDIT_RE_PREFIX + "pdp2)");

        /*
         * Start audit for pdp1.
         */
        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Start audit for pdp2.
         */
        MyIntegrityAudit integrityAudit2 = makeAuditor("pdp2", B_SEQ_PU);

        /*
         * Sleep long enough to allow
         * 
         * 1) pdp1 and pdp2 to run audit simultaneously
         * 
         * 2) Logic to detect that no other node is available for designation for either pdp1 or
         * pdp2
         * 
         * 3) pdp1 and pdp2 to again run audit simultaneously
         */
        runAudit(integrityAudit, integrityAudit2);
        waitStaleAndRun(integrityAudit, integrityAudit2);

        verifyItemsInLog(logA, "pdp1", "pdp1");
        verifyItemsInLog(logB, "pdp2", "pdp2");

        logger.info("testTwoResourcesDifferentPus: Exiting");

    }

    /*
     * Tests designation logic when two resources are in play but one of them is dead/hung.
     * Designation should move to second resource but then get restored back to original resource
     * when it's discovered that second resource is dead.
     * 
     * Note: console.log must be examined to ascertain whether or not this test was successful.
     */
    @Test
    void testTwoResourcesOneDead() throws Exception {

        logger.info("testTwoResourcesOneDead: Entering");

        final ExtractAppender logA = watch(debugLogger, START_AUDIT_RE);

        /*
         * Start audit for pdp1.
         */
        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Populate DB for pdp2, which will simulate it having registered but then having died.
         */
        new DbDao("pdp2", A_SEQ_PU, makeProperties()).destroy();

        /*
         * Sleep long enough to allow
         * 
         * 1) pdp1 to run audit
         * 
         * 2) Logic to detect that other node, pdp2, is not available for designation
         * 
         * 3) pdp1 to run audit again
         */
        runAudit(integrityAudit);
        waitStaleAndRun(integrityAudit);

        verifyItemsInLog(logA, "pdp1", "pdp1");

        logger.info("testTwoResourcesOneDead: Exiting");

    }

    /*
     * Tests designation logic when three functioning resources are in play. Designation should
     * round robin among resources.
     * 
     * Note: console.log must be examined to ascertain whether or not this test was successful.
     */
    @Test
    void testThreeResources() throws Exception {

        logger.info("testThreeResources: Entering");

        final ExtractAppender logA = watch(debugLogger, START_AUDIT_RE);

        /*
         * Start audit for pdp1.
         */
        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Start audit for pdp2.
         */
        MyIntegrityAudit integrityAudit2 = makeAuditor("pdp2", A_SEQ_PU);

        /*
         * Start audit for pdp3.
         */
        MyIntegrityAudit integrityAudit3 = makeAuditor("pdp3", A_SEQ_PU);

        /*
         * Sleep long enough to allow
         * 
         * 1) pdp1 to run audit
         * 
         * 2) Logic to detect that pdp1 is stale and designate pdp2
         * 
         * 3) pdp2 to run audit
         * 
         * 4) Logic to detect that pdp2 is stale and designate pdp3
         * 
         * 5) pdp3 to run audit
         * 
         * 6) Logic to detect that pdp3 is stale and designate pdp1
         * 
         * 7) pdp1 to run audit
         */
        runAudit(integrityAudit);
        waitStaleAndRun(integrityAudit2);
        waitStaleAndRun(integrityAudit3);
        waitStaleAndRun(integrityAudit);

        verifyItemsInLog(logA, "pdp1", "pdp2", "pdp3", "pdp1");

        logger.info("testThreeResources: Exiting");

    }

    /*
     * Tests designation logic when four functioning resources are in play, two with one PU, two
     * with another. Audits for persistenceUnit and PU_B should run simultaneously. Designation
     * should alternate between resources for each of the two persistence units.
     * 
     * Note: console.log must be examined to ascertain whether or not this test was successful.
     */
    @Test
    void testFourResourcesDifferentPus() throws Exception {

        logger.info("testFourResourcesDifferentPus: Entering");

        final ExtractAppender logA = watch(debugLogger, START_AUDIT_RE_PREFIX + "pdp1|pdp3)");
        final ExtractAppender logB = watch(debugLogger, START_AUDIT_RE_PREFIX + "pdp2|pdp4)");

        /*
         * Start audit for "pdp1", testPU.
         */
        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Start audit for pdp2, integrityAuditPU.
         */
        MyIntegrityAudit integrityAudit2 = makeAuditor("pdp2", B_SEQ_PU);

        /*
         * Start audit for pdp3, testPU.
         */
        MyIntegrityAudit integrityAudit3 = makeAuditor("pdp3", A_SEQ_PU);

        /*
         * Start audit for pdp4, integrityAuditPU.
         */
        MyIntegrityAudit integrityAudit4 = makeAuditor("pdp4", B_SEQ_PU);

        /*
         * Sleep long enough to allow
         * 
         * 1) pdp1 and pdp2 to run audit simultaneously
         * 
         * 2) Logic to detect that pdp1 and pdp2 are stale and designate pdp3 (one's counterpart)
         * and pdp4 (two's counterpart)
         * 
         * 3) pdp3 and pdp4 to run audit simultaneously
         * 
         * 4) Logic to detect that pdp3 and pdp4 are stale and designate pdp1 (three's counterpart)
         * and pdp2 (four's counterpart)
         * 
         * 5) pdp1 and pdp2 to run audit simultaneously
         */
        runAudit(integrityAudit, integrityAudit2);
        waitStaleAndRun(integrityAudit3, integrityAudit4);
        waitStaleAndRun(integrityAudit, integrityAudit2);

        /*
         * These sequences may be intermingled, so we extract and compare one sequence at a time.
         */

        // only care about pdp1 & pdp3 in this sequence
        verifyItemsInLog(logA, "pdp1", "pdp3", "pdp1");

        // only care about pdp2 & pdp4 in this sequence
        verifyItemsInLog(logB, "pdp2", "pdp4", "pdp2");

        logger.info("testFourResourcesDifferentPus: Exiting");

    }

    /*
     * Tests designation logic when four resources are in play but one is not functioning.
     * Designation should round robin among functioning resources only.
     * 
     * Note: console.log must be examined to ascertain whether or not this test was successful.
     */
    @Test
    void testFourResourcesOneDead() throws Exception {

        logger.info("testFourResourcesOneDead: Entering");

        final ExtractAppender logA = watch(debugLogger, START_AUDIT_RE);

        /*
         * Start audit for pdp1.
         */
        MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Start audit for pdp2.
         */
        MyIntegrityAudit integrityAudit2 = makeAuditor("pdp2", A_SEQ_PU);

        /*
         * Populate DB for pdp3, which will simulate it having registered but then having died.
         */
        new DbDao("pdp3", A_SEQ_PU, makeProperties()).destroy();

        /*
         * Start audit for pdp4.
         */
        MyIntegrityAudit integrityAudit4 = makeAuditor("pdp4", A_SEQ_PU);

        /*
         * Sleep long enough to allow
         * 
         * 1) pdp1 to run audit
         * 
         * 2) Logic to detect that pdp1 is stale and designate pdp2
         * 
         * 3) pdp2 to run audit
         * 
         * 4) Logic to detect that pdp2 is stale and designate pdp4
         * 
         * 5) pdp4 to run audit
         * 
         * 6) Logic to detect that pdp4 is stale and designate pdp1
         * 
         * 7) pdp1 to run audit
         * 
         * 8) Logic to detect that pdp1 is stale and designate pdp2
         * 
         * 7) pdp2 to run audit
         */
        runAudit(integrityAudit);
        waitStaleAndRun(integrityAudit2);
        waitStaleAndRun(integrityAudit4);
        waitStaleAndRun(integrityAudit);
        waitStaleAndRun(integrityAudit2);
        waitStaleAndRun(integrityAudit4);

        verifyItemsInLog(logA, "pdp1", "pdp2", "pdp4", "pdp1", "pdp2", "pdp4");

        logger.info("testFourResourcesOneDead: Exiting");

    }

    /*
     * Tests designation logic when four resources are in play but only one is functioning.
     * Designation should remain with sole functioning resource.
     * 
     * Note: console.log must be examined to ascertain whether or not this test was successful.
     */
    @Test
    void testFourResourcesThreeDead() throws Exception {

        logger.info("testFourResourcesThreeDead: Entering");

        final ExtractAppender logA = watch(debugLogger, START_AUDIT_RE);

        /*
         * Populate DB for "pdp1", which will simulate it having registered but then having died.
         */
        new DbDao("pdp1", A_SEQ_PU, makeProperties()).destroy();

        /*
         * Populate DB for pdp2, which will simulate it having registered but then having died.
         */
        new DbDao("pdp2", A_SEQ_PU, makeProperties()).destroy();

        /*
         * Start audit for pdp3.
         */
        MyIntegrityAudit integrityAudit3 = makeAuditor("pdp3", A_SEQ_PU);

        /*
         * Populate DB for pdp4, which will simulate it having registered but then having died.
         */
        new DbDao("pdp4", A_SEQ_PU, makeProperties()).destroy();

        /*
         * Sleep long enough to allow
         * 
         * 1) pdp3 to discover that all other designation candidates are stale
         * 
         * 1) pdp3 to run audit
         * 
         * 2) Logic to detect that no other nodes are available for designation
         * 
         * 3) pdp3 to run audit again
         */
        runAudit(integrityAudit3);
        waitStaleAndRun(integrityAudit3);

        verifyItemsInLog(logA, "pdp3", "pdp3");

        logger.info("testFourResourcesThreeDead: Exiting");

    }

    /*
     * Tests designation logic when the designated node dies and is no longer current
     * 
     * Note: console.log must be examined to ascertain whether or not this test was successful.
     */
    @Test
    void testDesignatedNodeDead() throws Exception {
        logger.info("testDesignatedNodeDead: Entering");

        final ExtractAppender logA = watch(debugLogger, START_AUDIT_RE);

        /*
         * Instantiate audit object for pdp1.
         */
        final MyIntegrityAudit integrityAudit = makeAuditor("pdp1", A_SEQ_PU);

        /*
         * Start audit for pdp2.
         */
        final MyIntegrityAudit integrityAudit2 = makeAuditor("pdp2", A_SEQ_PU);

        /*
         * Instantiate audit object for pdp3.
         */
        final MyIntegrityAudit integrityAudit3 = makeAuditor("pdp3", A_SEQ_PU);

        // Start audit on pdp1
        logger.info("testDesignatedNodeDead: Start audit on pdp1");
        runAudit(integrityAudit);

        // Start the auditing threads on other nodes.
        logger.info("testDesignatedNodeDead: Start audit on pdp2");
        runAudit(integrityAudit2);

        // Kill audit on pdp1
        logger.info("testDesignatedNodeDead: Kill audit on pdp1");
        integrityAudit.stopAuditThread();

        // Wait long enough for pdp1 to get stale and pdp2 to take over
        waitStaleAndRun(integrityAudit2);

        // Start audit thread on pdp1 again.
        logger.info("testDesignatedNodeDead: Start audit thread on pdp1 again.");
        integrityAudit.startAuditThread();

        // Wait long enough for pdp2 to complete its audit and get stale, at
        // which point pdp3 should take over
        logger.info(
                "testDesignatedNodeDead: Wait long enough for pdp2 to complete its audit and get stale, at which point"
                        + " pdp3 should take over");
        waitStaleAndRun(integrityAudit3);

        // Kill audit on pdp3
        logger.info("testDesignatedNodeDead: Killing audit on pdp3");
        integrityAudit3.stopAuditThread();

        // Wait long enough for pdp3 to get stale and pdp1 to take over
        logger.info("testDesignatedNodeDead: Wait long enough for pdp3 to get stale and pdp1 to take over");
        waitStaleAndRun(integrityAudit);

        verifyItemsInLog(logA, "pdp1", "pdp2", "pdp3", "pdp1");

        logger.info("testDesignatedNodeDead: Exiting");
    }
}
