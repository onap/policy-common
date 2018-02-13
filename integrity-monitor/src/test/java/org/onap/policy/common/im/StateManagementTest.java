/*-
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
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class StateManagementTest extends IntegrityMonitorTestBase {
	private static Logger logger = LoggerFactory.getLogger(StateManagementTest.class);
	//

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrityMonitorTestBase.setUpBeforeClass(DEFAULT_DB_URL_PREFIX + StateManagementTest.class.getSimpleName());

	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		IntegrityMonitorTestBase.tearDownAfterClass();
	}

	@Before
	public void setUp() {
		super.setUpTest();
	}

	@After
	public void tearDown() {
		super.tearDownTest();
	}

	@Test
	public void test() throws Exception {
		logger.info("\n\nlogger.infor StateManagementTest: Entering\n\n");
		String resourceName = "test_resource1";

		// These parameters are in a properties file
		try {
			StateManagement sm = new StateManagement(emf, resourceName);

			logger.info("\n??? initial state");
			assertEquals("unlocked,enabled,null,null", makeString(sm));

			logger.info("\n??? test lock()");
			sm.lock();
			assertEquals("locked,enabled,null,null", makeString(sm));

			logger.info("\n??? test unlock()");
			sm.unlock();
			assertEquals("unlocked,enabled,null,null", makeString(sm));

			logger.info("\n??? test enableNotFailed()");
			sm.enableNotFailed();
			assertEquals("unlocked,enabled,null,null", makeString(sm));

			logger.info("\n??? test disableFailed()");
			sm.disableFailed();
			assertEquals("unlocked,disabled,failed,null", makeString(sm));

			// P4 If promote() is called while either the opState is disabled or
			// the adminState is locked,
			// the standbystatus shall transition to coldstandby and a
			// StandbyStatusException shall be thrown
			logger.info("\n??? promote() test case P4");
			try {
				sm.disableFailed();
				sm.lock();

				sm.promote();
				fail("missing exception");
			} catch (StandbyStatusException ex) {
				logger.info("StandbyStatusException thrown and caught");
				assertEquals("locked,disabled,failed,coldstandby", makeString(sm));
			}

			// P3 If promote() is called while standbyStatus is coldstandby, the
			// state shall not transition
			// and a StandbyStatusException shall be thrown
			logger.info("\n??? promote() test case P3");
			try {
				logger.info(resourceName + " standbyStatus= " + sm.getStandbyStatus());
				sm.promote();
				fail("missing exception");
			} catch (StandbyStatusException ex) {
				assertEquals("locked,disabled,failed,coldstandby", makeString(sm));
			}

			// P2 If promote() is called while the standbyStatus is null and the
			// opState is enabled and adminState is unlocked,
			// the state shall transition to providingservice
			logger.info("\n??? promote() test case P2");
			resourceName = "test_resource2";
			StateManagement sm2 = new StateManagement(emf, resourceName);
			sm2.enableNotFailed();
			sm2.unlock();
			assertEquals("unlocked,enabled,null,null", makeString(sm2));
			sm2.promote();
			assertEquals("unlocked,enabled,null,providingservice", makeString(sm2));

			// P5 If promote() is called while standbyStatus is
			// providingservice, no action is taken
			logger.info("\n??? promote() test case P5");
			sm2.promote();
			assertEquals("unlocked,enabled,null,providingservice", makeString(sm2));

			// D1 If demote() is called while standbyStatus is providingservice,
			// the state shall transition to hotstandby
			logger.info("\n??? demote() test case D1");
			sm2.demote();
			assertEquals("unlocked,enabled,null,hotstandby", makeString(sm2));

			// D4 If demote() is called while standbyStatus is hotstandby, no
			// action is taken
			logger.info("\n??? demote() test case D4");
			sm2.demote();
			assertEquals("unlocked,enabled,null,hotstandby", makeString(sm2));

			// D3 If demote() is called while standbyStatus is null and
			// adminState is locked or opState is disabled,
			// the state shall transition to coldstandby
			logger.info("\n??? demote() test case D3");
			resourceName = "test_resource3";
			StateManagement sm3 = new StateManagement(emf, resourceName);
			sm3.lock();
			sm3.disableFailed();
			sm3.demote();
			assertEquals("locked,disabled,failed,coldstandby", makeString(sm3));

			// D5 If demote() is called while standbyStatus is coldstandby, no
			// action is taken
			logger.info("\n??? demote() test case D5");
			sm3.demote();
			assertEquals("locked,disabled,failed,coldstandby", makeString(sm3));

			// D2 If demote() is called while standbyStatus is null and
			// adminState is unlocked and opState is enabled,
			// the state shall transition to hotstandby
			logger.info("\n??? demote() test case D2");
			resourceName = "test_resource4";
			StateManagement sm4 = new StateManagement(emf, resourceName);
			sm4.unlock();
			sm4.enableNotFailed();
			assertEquals("unlocked,enabled,null,null", makeString(sm4));
			sm4.demote();
			assertEquals("unlocked,enabled,null,hotstandby", makeString(sm4));

			// P1 If promote() is called while standbyStatus is hotstandby, the
			// state shall transition to providingservice.
			logger.info("\n??? promote() test case P1");
			sm4.promote();
			assertEquals("unlocked,enabled,null,providingservice", makeString(sm4));

			// State change notification
			logger.info("\n??? State change notification test case 1 - lock()");
			StateChangeNotifier stateChangeNotifier = new StateChangeNotifier();
			sm.addObserver(stateChangeNotifier);
			sm.lock();
			assertEquals("locked,disabled,failed,coldstandby", makeString(stateChangeNotifier.getStateManagement()));

			logger.info("\n??? State change notification test case 2 - unlock()");
			sm.unlock();
			assertEquals("unlocked,disabled,failed,coldstandby", makeString(stateChangeNotifier.getStateManagement()));

			logger.info("\n??? State change notification test case 3 - enabled()");
			sm.enableNotFailed();
			assertEquals("unlocked,enabled,null,hotstandby", makeString(stateChangeNotifier.getStateManagement()));

			logger.info("\n??? State change notification test case 4 - disableFailed()");
			sm.disableFailed();
			assertEquals("unlocked,disabled,failed,coldstandby", makeString(stateChangeNotifier.getStateManagement()));

			logger.info("\n??? State change notification test case 5 - demote()");
			sm.demote();
			assertEquals("unlocked,disabled,failed,coldstandby", makeString(stateChangeNotifier.getStateManagement()));

			logger.info("\n??? State change notification test case 6 - promote()");
			try {
				sm.promote();
				fail("missing exception");
			} catch (StandbyStatusException ex) {
				assertEquals("unlocked,disabled,failed,coldstandby", makeString(sm));
			}

		} catch (Exception ex) {
			logger.error("Exception: {}", ex.toString());
			throw ex;
		}

		logger.info("\n\nStateManagementTest: Exit\n\n");
	}

	/**
	 * Converts a state element to a comma-separated string.
	 * 
	 * @param se
	 *            element to be converted
	 * @return a string representing the element
	 */
	private String makeString(StateManagement sm) {
		if (sm == null) {
			return null;
		}

		StringBuilder b = new StringBuilder();

		b.append(sm.getAdminState());
		b.append(',');
		b.append(sm.getOpState());
		b.append(',');
		b.append(sm.getAvailStatus());
		b.append(',');
		b.append(sm.getStandbyStatus());

		return b.toString();
	}
}
