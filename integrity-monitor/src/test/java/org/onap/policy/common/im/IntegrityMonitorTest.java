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
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.im.jpa.ForwardProgressEntity;
import org.onap.policy.common.im.jpa.ResourceRegistrationEntity;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class IntegrityMonitorTest extends IntegrityMonitorTestBase {
	private static Logger logger = LoggerFactory.getLogger(IntegrityMonitorTest.class);

	private static Properties myProp;
	private static EntityTransaction et;
	private static String resourceName;

	private BlockingQueue<CountDownLatch> queue;

	@BeforeClass
	public static void setUpClass() throws Exception {
		IntegrityMonitorTestBase.setUpBeforeClass(DEFAULT_DB_URL_PREFIX + IntegrityMonitorTest.class.getSimpleName());

		resourceName = IntegrityMonitorTestBase.siteName + "." + IntegrityMonitorTestBase.nodeType;
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		IntegrityMonitorTestBase.tearDownAfterClass();
	}

	@Before
	public void setUp() throws Exception {
		super.setUpTest();

		myProp = makeProperties();
		et = null;
	}

	@After
	public void tearDown() throws Exception {
		if (et != null && et.isActive()) {
			try {
				et.rollback();

			} catch (RuntimeException e) {
				logger.error("cannot rollback transaction", e);
			}
		}

		super.tearDownTest();
	}

	/*
	 * The following test verifies the following test cases: New Install New
	 * Install - Bad Dependency data Recovery from bad dependency data Lock Lock
	 * restart Unlock Unlock restart
	 */
	@Test
	public void testSanityJmx() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testSanityJmx\n\n");

		String dependent = "group1_logparser";

		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, dependent);
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "true");
		// Disable the integrity monitor so it will not interfere
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, "-1");
		// Disable the refresh state audit
		myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable the state audit
		myProp.put(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable the test transaction
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, "-1");
		// Disable the write FPC
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, "-1");
		// Speed up the check
		myProp.put(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, "1");
		// Fail dependencies after three seconds
		myProp.put(IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL, "3");

		IntegrityMonitor im = makeMonitor(resourceName, myProp);
		logger.debug(
				"\n\ntestSanityJmx starting im state \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				im.getStateManager().getAdminState(), im.getStateManager().getOpState(),
				im.getStateManager().getAvailStatus(), im.getStateManager().getStandbyStatus());
		// add an entry to Resource registration table in the DB for the
		// dependent resource

		et = em.getTransaction();
		et.begin();
		Query rquery = em.createQuery("Select r from ResourceRegistrationEntity r where r.resourceName=:rn");
		rquery.setParameter("rn", dependent);

		@SuppressWarnings("rawtypes")
		List rrList = rquery.getResultList();
		ResourceRegistrationEntity rrx = null;
		if (rrList.isEmpty()) {
			// register resource by adding entry to table in DB
			logger.debug("Adding resource {}  to ResourceRegistration table", dependent);
			rrx = new ResourceRegistrationEntity();
			// set columns in entry
			rrx.setResourceName(dependent);
			rrx.setResourceUrl("service:jmx:somewhere:9999");
			rrx.setNodeType("logparser");
			rrx.setSite("siteA");
		}
		em.persist(rrx);
		// flush to the DB
		em.flush();

		// commit transaction
		et.commit();

		// wait for the FPManager to check dependency health
		waitStep();

		assertException(im, imx -> {
			imx.evaluateSanity();
		});

		// undo dependency groups and jmx test properties settings
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		IntegrityMonitor.updateProperties(myProp);

		logger.debug("\ntestSantityJmx ending properties: {}", myProp);

		// We know at this point that the IM is disable-dependency. We want to
		// be
		// sure it will recover from this condition since the properties were
		// updated.

		logger.debug(
				"\n\ntestSanityJmx ending im state\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				im.getStateManager().getAdminState(), im.getStateManager().getOpState(),
				im.getStateManager().getAvailStatus(), im.getStateManager().getStandbyStatus());

		logger.debug("\ntestSanityJmx restarting the IntegrityMonitor");
		// Create a new instance. It should recover from the disabled-dependency
		// condition
		im = makeMonitor(resourceName, myProp);

		logger.debug(
				"\n\ntestSanityJmx state after creating new im\n"
						+ "AdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				im.getStateManager().getAdminState(), im.getStateManager().getOpState(),
				im.getStateManager().getAvailStatus(), im.getStateManager().getStandbyStatus());

		// Verify the state
		assertEquals(StateManagement.UNLOCKED, im.getStateManager().getAdminState());
		assertEquals(StateManagement.ENABLED, im.getStateManager().getOpState());
		assertEquals(StateManagement.NULL_VALUE, im.getStateManager().getAvailStatus());
		assertEquals(StateManagement.NULL_VALUE, im.getStateManager().getStandbyStatus());

		// Test state manager via the IntegrityMonitor
		StateManagement sm = im.getStateManager();

		// Verify lock state
		sm.lock();
		logger.debug("\n\nsm.lock()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());
		assertEquals(StateManagement.LOCKED, sm.getAdminState());

		// Verify lock persists across a restart
		logger.debug("\ntestSanityJmx restarting the IntegrityMonitor");
		// Create a new instance. It should come up with the admin state locked
		im = makeMonitor(resourceName, myProp);
		sm = im.getStateManager();
		logger.debug(
				"\n\ntestSanityJmx restart with AdminState=locked"
						+ "\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());
		assertEquals(StateManagement.LOCKED, sm.getAdminState());

		// Verify unlock
		sm.unlock();
		logger.debug(
				"\n\ntestSanityJmx sm.unlock\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());
		assertEquals(StateManagement.UNLOCKED, sm.getAdminState());

		// Verify unlock restart
		logger.debug("\ntestSanityJmx restarting the IntegrityMonitor");
		// Create a new instance. It should come up with the admin state locked
		im = makeMonitor(resourceName, myProp);
		sm = im.getStateManager();
		logger.debug(
				"\n\ntestSanityJmx restart with AdminState=unlocked\n"
						+ "AdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.UNLOCKED, sm.getAdminState());

		logger.debug("\n\ntestSanityJmx: Exit\n\n");
	}

	@Test
	public void testIM() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testIM\n\n");

		// Disable the integrity monitor so it will not interfere
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, "-1");
		// Disable dependency checking
		myProp.put(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, "-1");
		// Disable the refresh state audit
		myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable the state audit
		myProp.put(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable the test transaction
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, "-1");
		// Disable writing the FPC
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, "-1");

		IntegrityMonitor im = makeMonitor(resourceName, myProp);

		logger.debug("\n\nim initial state: \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				im.getStateManager().getAdminState(), im.getStateManager().getOpState(),
				im.getStateManager().getAvailStatus(), im.getStateManager().getStandbyStatus());

		waitStep();

		// test evaluate sanity
		assertNoException(im, imx -> {
			imx.evaluateSanity();
		});

		// Test startTransaction - should work since it is unlocked
		assertNoException(im, imx -> {
			imx.startTransaction();
		});

		// Test state manager via the IntegrityMonitor
		StateManagement sm = im.getStateManager();

		sm.lock();

		logger.debug("\n\nsm.lock()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.LOCKED, sm.getAdminState());

		// test startTransaction. It should fail since it is locked
		assertException(im, imx -> {
			imx.startTransaction();
		});

		sm.unlock();
		logger.debug("\n\nsm.unlock()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());
		assertEquals(StateManagement.UNLOCKED, sm.getAdminState());

		// test startTransaction. It should succeed
		assertNoException(im, imx -> {
			imx.startTransaction();
		});

		sm.disableDependency();
		logger.debug(
				"\n\nsm.disableDependency()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.DISABLED, sm.getOpState());
		assertEquals(StateManagement.DEPENDENCY, sm.getAvailStatus());

		// test startTransaction. It should succeed since standby status is null
		// and unlocked
		assertNoException(im, imx -> {
			imx.startTransaction();
		});

		sm.enableNoDependency();

		logger.debug(
				"\n\nsm.enableNoDependency()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());
		assertEquals(StateManagement.ENABLED, sm.getOpState());
		// test startTransaction. It should succeed since standby status is null
		// and unlocked
		assertNoException(im, imx -> {
			imx.startTransaction();
		});

		sm.disableFailed();
		logger.debug("\n\nsm.disableFailed()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.DISABLED, sm.getOpState());
		assertEquals(StateManagement.FAILED, sm.getAvailStatus());
		// test startTransaction. It should succeed since standby status is null
		// and unlocked
		assertNoException(im, imx -> {
			imx.startTransaction();
		});

		sm.enableNotFailed();

		logger.debug(
				"\n\nsm.enabledNotFailed()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.ENABLED, sm.getOpState());
		// test startTransaction. It should succeed since standby status is null
		// and unlocked
		assertNoException(im, imx -> {
			imx.startTransaction();
		});

		sm.demote();

		logger.debug("\n\nsm.demote()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.HOT_STANDBY, sm.getStandbyStatus());

		// test startTransaction. It should fail since it is standby
		assertException(im, imx -> {
			imx.startTransaction();
		});

		sm.promote();

		logger.debug("\n\nsm.promote()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.PROVIDING_SERVICE, sm.getStandbyStatus());

		// test startTransaction. It should succeed since it is providing
		// service
		assertNoException(im, imx -> {
			imx.startTransaction();
		});

		// Test the multi-valued availability status
		sm.disableDependency();
		sm.disableFailed();

		logger.debug(
				"\n\nsm.disableDependency(), sm.disableFailed\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.DEPENDENCY_FAILED, sm.getAvailStatus());

		// Test startTransaction. Should fail since standby status is cold
		// standby
		assertException(im, imx -> {
			imx.startTransaction();
		});

		sm.enableNoDependency();

		logger.debug(
				"\n\nsm.enableNoDependency()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());
		assertEquals(StateManagement.FAILED, sm.getAvailStatus());
		// Test startTransaction. Should fail since standby status is cold
		// standby
		assertException(im, imx -> {
			imx.startTransaction();
		});

		sm.disableDependency();
		sm.enableNotFailed();

		logger.debug(
				"\n\nsm.disableDependency(),sm.enableNotFailed()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());

		assertEquals(StateManagement.DEPENDENCY, sm.getAvailStatus());
		// Test startTransaction. Should fail since standby status is cold
		// standby
		assertException(im, imx -> {
			imx.startTransaction();
		});

		sm.enableNoDependency();
		logger.debug(
				"\n\nsm.enableNoDependency()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(), sm.getOpState(), sm.getAvailStatus(), sm.getStandbyStatus());
		assertEquals(StateManagement.ENABLED, sm.getOpState());
		// test startTransaction. It should fail since standby status is hot
		// standby
		assertException(im, imx -> {
			imx.startTransaction();
		});

		logger.debug("\n\ntestIM: Exit\n\n");
	}

	@Test
	public void testSanityState() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testSanityState\n\n");

		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "group1_dep1,group1_dep2; group2_dep1");
		// Disable the integrity monitor so it will not interfere
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, "-1");
		// Disable the refresh state audit
		myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable dependency checking so it does not interfere
		myProp.put(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, "-1");
		// Disable the state audit
		myProp.put(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable the test transaction
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, "-1");
		// Disable writing the FPC
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, "-1");
		// Max interval for use in deciding if a FPC entry is stale in seconds
		myProp.put(IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL, "120");

		IntegrityMonitor im = makeMonitor(resourceName, myProp);

		waitStep();

		// Add a group1 dependent resources to put an entry in the forward
		// progress table
		ForwardProgressEntity fpe = new ForwardProgressEntity();
		ForwardProgressEntity fpe2 = new ForwardProgressEntity();
		fpe.setFpcCount(0);
		fpe.setResourceName("group1_dep1");
		fpe2.setFpcCount(0);
		fpe2.setResourceName("group1_dep2");
		et = em.getTransaction();
		et.begin();
		em.persist(fpe);
		em.persist(fpe2);
		em.flush();
		et.commit();

		// Add a group2 dependent resource to the StateManagementEntity DB table
		// and set its admin state to locked
		// Expect sanity test to fail.
		StateManagement stateManager = new StateManagement(emf, "group2_dep1");
		stateManager.lock();

		new StateManagement(emf, "group1_dep1");
		new StateManagement(emf, "group1_dep2");

		// Call the dependency check directly instead of waiting for FPManager
		// to do it.
		logger.debug("\n\nIntegrityMonitor.testSanityState: calling im.dependencyCheck()\n\n");
		im.dependencyCheck();
		assertException(im, imx -> {
			imx.evaluateSanity();
		});

		logger.debug("\n\ntestSanityState: Exit\n\n");
	}

	@Test
	public void testRefreshStateAudit() throws Exception {
		logger.debug("\nIntegrityMonitorTest: testRefreshStateAudit Enter\n\n");

		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		// Disable the integrity monitor so it will not interfere
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, "-1");
		// Disable the refresh state audit
		myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable dependency checking so it does not interfere
		myProp.put(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, "-1");
		// Disable the state audit
		myProp.put(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable the test transaction
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, "-1");
		// Disable writing the FPC
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, "-1");

		IntegrityMonitor im = makeMonitor(resourceName, myProp);

		waitStep();

		// the state here is unlocked, enabled, null, null
		StateManagementEntity sme = null;

		Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");

		query.setParameter("resource", resourceName);

		// Just test that we are retrieving the right object
		@SuppressWarnings("rawtypes")
		List resourceList = query.getResultList();
		if (!resourceList.isEmpty()) {
			// exist
			sme = (StateManagementEntity) resourceList.get(0);
			em.refresh(sme);

			logger.debug(
					"??? -- Retrieve StateManagementEntity from database --\nsme.getResourceName() = {}\n"
							+ "sme.getAdminState() = {}\nsme.getOpState() = {}\nsme.getAvailStatus() = {}\nsme.getStandbyStatus() = {}",
					sme.getResourceName(), sme.getAdminState(), sme.getOpState(), sme.getAvailStatus(),
					sme.getStandbyStatus());

			assertEquals(StateManagement.UNLOCKED, sme.getAdminState());
			assertEquals(StateManagement.ENABLED, sme.getOpState());
			assertEquals(StateManagement.NULL_VALUE, sme.getAvailStatus());
			assertEquals(StateManagement.NULL_VALUE, sme.getStandbyStatus());
			logger.debug("--");
		} else {
			logger.debug("Record not found, resourceName: " + resourceName);
			fail("missing record");
		}

		et = em.getTransaction();
		et.begin();

		sme.setStandbyStatus(StateManagement.COLD_STANDBY);
		em.persist(sme);
		em.flush();
		et.commit();

		// Run the refreshStateAudit
		im.executeRefreshStateAudit();

		// The refreshStateAudit should run and change the state to
		// unlocked,enabled,null,hotstandby
		StateManagementEntity sme1 = null;

		Query query1 = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");

		query1.setParameter("resource", resourceName);

		@SuppressWarnings("rawtypes")
		List resourceList1 = query1.getResultList();
		if (!resourceList1.isEmpty()) {
			// exist
			sme1 = (StateManagementEntity) resourceList1.get(0);
			em.refresh(sme1);
			logger.debug(
					"??? -- Retrieve StateManagementEntity from database --\nsme1.getResourceName() = {}\n"
							+ "sme1.getAdminState() = {}\nsme1.getOpState() = {}\nsme1.getAvailStatus() = {}\nsme1.getStandbyStatus() = {}",
					sme1.getResourceName(), sme1.getAdminState(), sme1.getOpState(), sme1.getAvailStatus(),
					sme1.getStandbyStatus());

			assertEquals(StateManagement.UNLOCKED, sme1.getAdminState());
			assertEquals(StateManagement.ENABLED, sme1.getOpState());
			assertEquals(StateManagement.NULL_VALUE, sme1.getAvailStatus());
			assertEquals(StateManagement.HOT_STANDBY, sme1.getStandbyStatus());
			logger.debug("--");
		} else {
			logger.debug("Record not found, resourceName: " + resourceName);
			fail("record not found");
		}

		logger.debug("\nIntegrityMonitorTest: testRefreshStateAudit Exit\n\n");
	}

	@Test
	public void testStateCheck() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testStateCheck\n\n");

		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "group1_dep1");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		myProp.put(IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD, "1");
		/*
		 * The monitorInterval is set to 10 and the failedCounterThreshold is 1
		 * because stateCheck() uses the faileCounterThreshold * monitorInterval
		 * to determine if an entry is stale, it will be stale after 10 seconds.
		 */
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, "5");
		/*
		 * We accelerate the test transaction and write FPC intervals because we
		 * don't want there to be any chance of a FPC failure because of the
		 * short monitor interval
		 */
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, "1");
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, "2");
		// Disable the refresh state audit
		myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
		// The maximum time in seconds to determine that a FPC entry is stale
		myProp.put(IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL, "5");
		myProp.put(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, "5");

		IntegrityMonitor im = makeMonitor(resourceName, myProp);

		// Note: do ***NOT*** do waitStep() here

		// Add a group1 dependent resources to put an entry in the forward
		// progress table
		// This sets lastUpdated to the current time
		ForwardProgressEntity fpe = new ForwardProgressEntity();
		fpe.setFpcCount(0);
		fpe.setResourceName("group1_dep1");
		et = em.getTransaction();
		et.begin();
		em.persist(fpe);
		em.flush();
		et.commit();

		new StateManagement(emf, "group1_dep1");

		assertNoException(im, imx -> {
			imx.evaluateSanity();
		});

		// wait for FPManager to perform dependency health check. Once that's
		// done,
		// it should now be stale and the sanity check should fail
		waitStep();

		assertException(im, imx -> {
			imx.evaluateSanity();
		});

		logger.debug("\n\ntestStateCheck: Exit\n\n");
	}

	@Test
	public void testGetAllForwardProgressEntity() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testGetAllForwardProgressEntity\n\n");
		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		// Disable the integrity monitor so it will not interfere
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, "-1");
		// Disable the refresh state audit
		myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable dependency checking so it does not interfere
		myProp.put(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, "-1");
		// Disable the state audit
		myProp.put(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable the test transaction
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, "-1");
		// Disable writing the FPC
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, "-1");

		IntegrityMonitor im = makeMonitor(resourceName, myProp);
		waitStep();

		logger.debug("\nIntegrityMonitorTest: Creating ForwardProgressEntity entries\n\n");
		// Add resource entries in the forward progress table
		ForwardProgressEntity fpe = new ForwardProgressEntity();
		ForwardProgressEntity fpe2 = new ForwardProgressEntity();
		ForwardProgressEntity fpe3 = new ForwardProgressEntity();
		fpe.setFpcCount(0);
		fpe.setResourceName("siteA_pap2");
		fpe2.setFpcCount(0);
		fpe2.setResourceName("siteB_pap1");
		fpe3.setFpcCount(0);
		fpe3.setResourceName("siteB_pap2");
		et = em.getTransaction();
		et.begin();
		em.persist(fpe);
		em.persist(fpe2);
		em.persist(fpe3);
		em.flush();
		et.commit();

		logger.debug(
				"\nIntegrityMonitorTest:testGetAllForwardProgressEntity Calling im.getAllForwardProgressEntity()\n\n");
		List<ForwardProgressEntity> fpeList = im.getAllForwardProgressEntity();

		assertEquals(4, fpeList.size());

		logger.debug("\nIntegrityMonitorTest: Exit testGetAllForwardProgressEntity\n\n");
	}

	@Test
	public void testStateAudit() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testStateAudit\n\n");

		// parameters are passed via a properties file

		// No Dependency Groups
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
		// Don't use JMX
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		// Disable the internal sanity monitoring.
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, "-1");
		// Disable the dependency monitoring.
		myProp.put(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, "-1");
		// Disable the refresh state audit
		myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
		// Disable the test transaction
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, "-1");
		// Disable the write FPC
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, "-1");
		// Disable the State Audit we will call it directly
		myProp.put(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, "-1");
		// Max interval for use in deciding if a FPC entry is stale in seconds
		myProp.put(IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL, "120");

		IntegrityMonitor im = makeMonitor(resourceName, myProp);
		waitStep();

		logger.debug("\nIntegrityMonitorTest: Creating ForwardProgressEntity entries\n\n");
		// Add resources to put an entry in the forward progress table
		Date staleDate = new Date(0);
		ForwardProgressEntity fpe1 = new ForwardProgressEntity();
		ForwardProgressEntity fpe2 = new ForwardProgressEntity();
		ForwardProgressEntity fpe3 = new ForwardProgressEntity();
		fpe1.setFpcCount(0);
		fpe1.setResourceName("siteA_pap2");
		fpe2.setFpcCount(0);
		fpe2.setResourceName("siteB_pap1");
		fpe3.setFpcCount(0);
		fpe3.setResourceName("siteB_pap2");
		logger.debug("\nIntegrityMonitorTest: Creating StateManagementEntity entries\n\n");
		StateManagementEntity sme1 = new StateManagementEntity();
		StateManagementEntity sme2 = new StateManagementEntity();
		StateManagementEntity sme3 = new StateManagementEntity();
		sme1.setResourceName("siteA_pap2");
		sme1.setAdminState(StateManagement.UNLOCKED);
		sme1.setOpState(StateManagement.ENABLED);
		sme1.setAvailStatus(StateManagement.NULL_VALUE);
		sme1.setStandbyStatus(StateManagement.NULL_VALUE);
		sme2.setResourceName("siteB_pap1");
		sme2.setAdminState(StateManagement.UNLOCKED);
		sme2.setOpState(StateManagement.ENABLED);
		sme2.setAvailStatus(StateManagement.NULL_VALUE);
		sme2.setStandbyStatus(StateManagement.NULL_VALUE);
		sme3.setResourceName("siteB_pap2");
		sme3.setAdminState(StateManagement.UNLOCKED);
		sme3.setOpState(StateManagement.ENABLED);
		sme3.setAvailStatus(StateManagement.NULL_VALUE);
		sme3.setStandbyStatus(StateManagement.NULL_VALUE);
		et = em.getTransaction();
		et.begin();
		em.persist(fpe1);
		em.persist(fpe2);
		em.persist(fpe3);
		em.persist(sme1);
		em.persist(sme2);
		em.persist(sme3);
		em.flush();
		et.commit();

		Query updateQuery = em.createQuery(
				"UPDATE ForwardProgressEntity f " + "SET f.lastUpdated = :newDate " + "WHERE f.resourceName=:resource");
		updateQuery.setParameter("newDate", staleDate, TemporalType.TIMESTAMP);
		updateQuery.setParameter("resource", fpe1.getResourceName());

		et = em.getTransaction();
		et.begin();
		updateQuery.executeUpdate();
		et.commit();

		logger.debug("\nIntegrityMonitorTest:testStateAudit Calling im.getAllForwardProgressEntity()\n\n");
		List<ForwardProgressEntity> fpeList = im.getAllForwardProgressEntity();

		logger.debug("\n\n");
		logger.debug("IntegrityMonitorTest:testStateAudit:ForwardProgressEntity entries");
		for (ForwardProgressEntity myFpe : fpeList) {
			logger.debug("\n    ResourceName: {}" + "\n        LastUpdated: {}", myFpe.getResourceName(),
					myFpe.getLastUpdated());
		}
		logger.debug("\n\n");

		logger.debug("\nIntegrityMonitorTest:testStateAudit getting list of StateManagementEntity entries\n\n");
		Query query = em.createQuery("SELECT s FROM StateManagementEntity s");
		List<?> smeList = query.getResultList();

		logger.debug("\n\n");
		logger.debug("IntegrityMonitorTest:testStateAudit:StateManagementEntity entries");
		for (Object mySme : smeList) {
			StateManagementEntity tmpSme = (StateManagementEntity) mySme;
			em.refresh(tmpSme);
			logger.debug(
					"\n    ResourceName: {}" + "\n        AdminState: {}" + "\n        OpState: {}"
							+ "\n        AvailStatus: {}" + "\n        StandbyStatus: {}",
					tmpSme.getResourceName(), tmpSme.getAdminState(), tmpSme.getOpState(), tmpSme.getAvailStatus(),
					tmpSme.getStandbyStatus());
		}
		logger.debug("\n\n");

		em.refresh(sme1);
		assertEquals(StateManagement.ENABLED, sme1.getOpState());

		logger.debug("IntegrityMonitorTest:testStateAudit: calling stateAudit()");
		im.executeStateAudit();
		logger.debug("IntegrityMonitorTest:testStateAudit: call to stateAudit() complete");

		logger.debug("\nIntegrityMonitorTest:testStateAudit getting list of StateManagementEntity entries\n\n");
		smeList = query.getResultList();

		logger.debug("\n\n");
		logger.debug("IntegrityMonitorTest:testStateAudit:StateManagementEntity entries");
		for (Object mySme : smeList) {
			StateManagementEntity tmpSme = (StateManagementEntity) mySme;
			em.refresh(tmpSme);
			logger.debug(
					"\n    ResourceName: {}" + "\n        AdminState: {}" + "\n        OpState: {}"
							+ "\n        AvailStatus: {}" + "\n        StandbyStatus: {}",
					tmpSme.getResourceName(), tmpSme.getAdminState(), tmpSme.getOpState(), tmpSme.getAvailStatus(),
					tmpSme.getStandbyStatus());
		}
		logger.debug("\n\n");

		em.refresh(sme1);
		assertEquals(StateManagement.DISABLED, sme1.getOpState());

		// Now let's add sme2 to the mix
		updateQuery = em.createQuery(
				"UPDATE ForwardProgressEntity f " + "SET f.lastUpdated = :newDate " + "WHERE f.resourceName=:resource");
		updateQuery.setParameter("newDate", staleDate, TemporalType.TIMESTAMP);
		updateQuery.setParameter("resource", fpe2.getResourceName());

		et = em.getTransaction();
		et.begin();
		updateQuery.executeUpdate();
		et.commit();

		// Give it a chance to write the DB and run the audit
		logger.debug("IntegrityMonitorTest:testStateAudit: (restart4) Running State Audit");
		waitStep();
		im.executeStateAudit();
		waitStep();
		logger.debug("IntegrityMonitorTest:testStateAudit: (restart4) State Audit complete");

		// Now check its state
		logger.debug(
				"\nIntegrityMonitorTest:testStateAudit (restart4) getting list of StateManagementEntity entries\n\n");
		smeList = query.getResultList();

		logger.debug("\n\n");
		logger.debug("IntegrityMonitorTest:testStateAudit:StateManagementEntity (restart4) entries");
		for (Object mySme : smeList) {
			StateManagementEntity tmpSme = (StateManagementEntity) mySme;
			em.refresh(tmpSme);

			logger.debug(
					"\n    (restart4) ResourceName: {}" + "\n        AdminState: {}" + "\n        OpState: {}"
							+ "\n        AvailStatus: {}" + "\n        StandbyStatus: {}",
					tmpSme.getResourceName(), tmpSme.getAdminState(), tmpSme.getOpState(), tmpSme.getAvailStatus(),
					tmpSme.getStandbyStatus());
		}
		logger.debug("\n\n");

		em.refresh(sme1);
		assertEquals(StateManagement.DISABLED, sme1.getOpState());

		em.refresh(sme2);
		assertEquals(StateManagement.DISABLED, sme2.getOpState());

		logger.debug("\nIntegrityMonitorTest: Exit testStateAudit\n\n");
		System.out.println("\n\ntestStateAudit: Exit\n\n");
	}

	private IntegrityMonitor makeMonitor(String resourceName, Properties myProp) throws Exception {
		IntegrityMonitor.deleteInstance();

		queue = new LinkedBlockingQueue<>();

		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp, queue);

		// wait for the monitor thread to start
		waitStep();

		return im;
	}

	/**
	 * Waits for the FPManager to complete another cycle.
	 * 
	 * @throws InterruptedException
	 */
	private void waitStep() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		queue.offer(latch);
		waitLatch(latch);
	}
}
