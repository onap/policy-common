/*
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modificaitons Copyright (C) 2023 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

public class AllSeemsWellTest extends IntegrityMonitorTestBase {
    private static final String ALL_SEEMS_WELL_MSG = "'AllSeemsWellTest - ALLSEEMSWELL'";

    private static Logger logger = LoggerFactory.getLogger(AllSeemsWellTest.class);

    private static String resourceName;

    private Properties myProp;
    private Semaphore monitorSem;
    private Semaphore junitSem;

    /**
     * Set up for test class.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        IntegrityMonitorTestBase.setUpBeforeClass(DEFAULT_DB_URL_PREFIX + AllSeemsWellTest.class.getSimpleName());

        resourceName = IntegrityMonitorTestBase.SITE_NAME + "." + IntegrityMonitorTestBase.NODE_TYPE;
    }

    @AfterClass
    public static void tearDownClass() {
        IntegrityMonitorTestBase.tearDownAfterClass();
    }

    /**
     * Set up for test cases.
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        super.setUpTest();

        myProp = makeProperties();

        monitorSem = new Semaphore(0);
        junitSem = new Semaphore(0);

        IntegrityMonitor im = new IntegrityMonitor(resourceName, myProp) {

            @Override
            protected void runStarted() throws InterruptedException {
                monitorSem.acquire();

                junitSem.release();
                monitorSem.acquire();
            }

            @Override
            protected void monitorCompleted() throws InterruptedException {
                junitSem.release();
                monitorSem.acquire();
            }
        };

        ReflectionTestUtils.setField(IntegrityMonitor.class, IM_INSTANCE_FIELD, im);
    }

    @After
    public void tearDown() {
        super.tearDownTest();
    }

    @Test
    public void testAllSeemsWell() throws Exception {
        logger.debug("\nIntegrityMonitorTest: Entering testAllSeemsWell\n\n");

        // parameters are passed via a properties file
        myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
        myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
        myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
        myProp.put(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, "-1");
        myProp.put(IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD, "1");

        IntegrityMonitor.updateProperties(myProp);

        IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);

        StateManagement sm = im.getStateManager();

        // Give it time to set the states in the DB
        waitStateChange();

        // Check the state
        logger.debug(
                "\n\ntestAllSeemsWell starting im state \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\n"
                        + "StandbyStatus = {}\n",
                sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

        assertEquals(StateManagement.ENABLED, sm.getOpState());

        // Indicate a failure
        im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLNOTWELL,
                "'AllSeemsWellTest - ALLNOTWELL'");

        // Wait for the state to change due to ALLNOTWELL
        waitStateChange();
        // Check the state
        logger.debug(
                "\n\ntestAllSeemsWell after ALLNOTWELL: im state \nAdminState = {}\nOpState() = {}\nAvailStatus = "
                        + "{}\nStandbyStatus = {}\n",
                sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

        Map<String, String> allNotWellMap = im.getAllNotWellMap();
        if (logger.isDebugEnabled()) {
            for (Entry<String, String> ent : allNotWellMap.entrySet()) {
                logger.debug("AllSeemsWellTest: allNotWellMap: key = {}  msg = {}", ent.getKey(), ent.getValue());
            }
        }
        assertEquals(1, allNotWellMap.size());

        assertTrue(im.getAllSeemsWellMap().isEmpty());

        // Return to normal
        im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLSEEMSWELL,
                ALL_SEEMS_WELL_MSG);

        // Wait for the state to change due to ALLNOTWELL
        waitStateChange();
        // Check the state
        logger.debug(
                "\n\ntestAllSeemsWell after ALLSEEMSWELL: im state \nAdminState = {}\nOpState() = {}\nAvailStatus = "
                        + "{}\nStandbyStatus = {}\n",
                sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

        assertEquals(StateManagement.ENABLED, sm.getOpState());

        allNotWellMap = im.getAllNotWellMap();
        assertTrue(allNotWellMap.isEmpty());

        Map<String, String> allSeemsWellMap = im.getAllSeemsWellMap();
        assertEquals(1, allSeemsWellMap.size());
        if (logger.isDebugEnabled()) {
            for (Entry<String, String> ent : allSeemsWellMap.entrySet()) {
                logger.debug("AllSeemsWellTest: allSeemsWellMap: key = {}  msg = {}", ent.getKey(), ent.getValue());
            }
        }

        // Check for null parameters
        assertThatIllegalArgumentException().isThrownBy(
            () -> im.allSeemsWell(null, IntegrityMonitorProperties.ALLSEEMSWELL, ALL_SEEMS_WELL_MSG));

        assertThatIllegalArgumentException().isThrownBy(
            () -> im.allSeemsWell("", IntegrityMonitorProperties.ALLSEEMSWELL, ALL_SEEMS_WELL_MSG));

        assertThatIllegalArgumentException().isThrownBy(
            () -> im.allSeemsWell(this.getClass().getName(), null, ALL_SEEMS_WELL_MSG));

        assertThatIllegalArgumentException().isThrownBy(
            () -> im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLSEEMSWELL, null));

        assertThatIllegalArgumentException().isThrownBy(
            () -> im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLSEEMSWELL, ""));

        logger.debug("\n\ntestAllSeemsWell: Exit\n\n");
    }

    /**
     * Waits for the state to change.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    private void waitStateChange() throws InterruptedException {
        monitorSem.release();
        waitSem(junitSem);
    }

}
