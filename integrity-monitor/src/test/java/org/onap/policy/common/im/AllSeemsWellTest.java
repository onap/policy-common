/*
 * ============LICENSE_START=======================================================
 * Integrity Monitor
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

package org.onap.policy.common.im;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllSeemsWellTest extends IntegrityMonitorTestBase {
	private static Logger logger = LoggerFactory.getLogger(AllSeemsWellTest.class);

	private static final long STATE_CYCLE_MS = 3 * CYCLE_INTERVAL_MS;

	private static Properties myProp;
	private static String resourceName;

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrityMonitorTestBase.setUpBeforeClass(DEFAULT_DB_URL_PREFIX + AllSeemsWellTest.class.getSimpleName());

		resourceName = IntegrityMonitorTestBase.siteName + "." + IntegrityMonitorTestBase.nodeType;
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		IntegrityMonitorTestBase.tearDownAfterClass();
	}

	@Before
	public void setUp() {
		super.setUpTest();

		myProp = makeProperties();

	}

	@After
	public void tearDown() {
		super.tearDownTest();
	}

	// Ignore
	@Test
	public void testAllSeemsWell() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testAllSeemsWell\n\n");

		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
		myProp.put(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, "-1");
		myProp.put(IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD, "1");
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, "5");
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, "1");
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, "1");

		IntegrityMonitor.updateProperties(myProp);
		/*
		 * The monitorInterval is 5 and the failedCounterThreshold is 1 A
		 * forward progress will be stale after 5 seconds.
		 */

		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);

		StateManagement sm = im.getStateManager();

		// Give it time to set the states in the DB
		Thread.sleep(STATE_CYCLE_MS);

		// Check the state
		logger.debug(
				"\n\ntestAllSeemsWell starting im state \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.ENABLED, sm.getOpState());

		// Indicate a failure
		im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLNOTWELL,
				"'AllSeemsWellTest - ALLNOTWELL'");

		// Wait for the state to change due to ALLNOTWELL
		Thread.sleep(STATE_CYCLE_MS);
		// Check the state
		logger.debug(
				"\n\ntestAllSeemsWell after ALLNOTWELL: im state \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		// assertEquals(StateManagement.DISABLED, sm.getOpState());

		Map<String, String> allNotWellMap = im.getAllNotWellMap();
		for (String key : allNotWellMap.keySet()) {
			logger.debug("AllSeemsWellTest: allNotWellMap: key = {}  msg = {}", key, allNotWellMap.get(key));
		}
		// assertEquals(1, allNotWellMap.size());

		Map<String, String> allSeemsWellMap = im.getAllSeemsWellMap();
		// assertTrue(allSeemsWellMap.isEmpty());

		// Return to normal
		im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLSEEMSWELL,
				"'AllSeemsWellTest - ALLSEEMSWELL'");

		// Wait for the state to change due to ALLNOTWELL
		Thread.sleep(STATE_CYCLE_MS);
		// Check the state
		logger.debug(
				"\n\ntestAllSeemsWell after ALLSEEMSWELL: im state \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		// assertEquals(StateManagement.ENABLED, sm.getOpState());

		allNotWellMap = im.getAllNotWellMap();
		assertTrue(allNotWellMap.isEmpty());

		allSeemsWellMap = im.getAllSeemsWellMap();
		assertEquals(1, allSeemsWellMap.size());
		for (String key : allSeemsWellMap.keySet()) {
			logger.debug("AllSeemsWellTest: allSeemsWellMap: key = {}  msg = {}", key, allSeemsWellMap.get(key));
		}

		// Check for null parameters
		assertException(im, imx -> {
			imx.allSeemsWell(null, IntegrityMonitorProperties.ALLSEEMSWELL, "'AllSeemsWellTest - ALLSEEMSWELL'");
		});

		assertException(im, imx -> {
			im.allSeemsWell("", IntegrityMonitorProperties.ALLSEEMSWELL, "'AllSeemsWellTest - ALLSEEMSWELL'");
		});

		assertException(im, imx -> {
			im.allSeemsWell(this.getClass().getName(), null, "'AllSeemsWellTest - ALLSEEMSWELL'");
		});

		assertException(im, imx -> {
			im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLSEEMSWELL, null);
		});

		assertException(im, imx -> {
			im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLSEEMSWELL, "");
		});

		logger.debug("\n\ntestAllSeemsWell: Exit\n\n");
	}

}
