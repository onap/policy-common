/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.im.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.policy.common.im.IntegrityMonitor;
import org.onap.policy.common.im.IntegrityMonitorProperties;
import org.onap.policy.common.im.StateManagement;
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
public class IntegrityMonitorTest {
	private static Logger logger = LoggerFactory.getLogger(IntegrityMonitorTest.class);
	private static Properties myProp;
	private static EntityManagerFactory emf;
	private static EntityManager em;
	private static EntityTransaction et;
	private static String resourceName;
	private static Properties systemProps;
	
	private static final String DEFAULT_DB_DRIVER = "org.h2.Driver";
	private static final String DEFAULT_DB_URL = "jdbc:h2:file:./sql/imTest";
	private static final String DEFAULT_DB_USER = "sa";
	private static final String DEFAULT_DB_PWD = "";
	
	@BeforeClass
	public static void setUpClass() throws Exception {

	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		IntegrityMonitor.setUnitTesting(true);
		
		cleanMyProp();
		
		// set JMX remote port in system properties
		systemProps = System.getProperties();
		systemProps.put("com.sun.management.jmxremote.port", "9797");
		
		resourceName = "siteA.pap1";
		
		//Create the data schema and entity manager factory
		emf = Persistence.createEntityManagerFactory("schemaPU", myProp);

		// Create an entity manager to use the DB
		em = emf.createEntityManager();

	}
	

	@After
	public void tearDown() throws Exception {
		// clear jmx remote port setting
		systemProps.remove("com.sun.management.jmxremote.port");
	}
	
	private void cleanMyProp(){
		myProp = new Properties();
		myProp.put(IntegrityMonitorProperties.DB_DRIVER, IntegrityMonitorTest.DEFAULT_DB_DRIVER);
		myProp.put(IntegrityMonitorProperties.DB_URL, IntegrityMonitorTest.DEFAULT_DB_URL);
		myProp.put(IntegrityMonitorProperties.DB_USER, IntegrityMonitorTest.DEFAULT_DB_USER);
		myProp.put(IntegrityMonitorProperties.DB_PWD, IntegrityMonitorTest.DEFAULT_DB_PWD);
		myProp.put(IntegrityMonitorProperties.SITE_NAME, "SiteA");
		myProp.put(IntegrityMonitorProperties.NODE_TYPE, "pap");
	}
	
	private void cleanDb(){
		et = em.getTransaction();
		
		et.begin();
		// Make sure we leave the DB clean
		em.createQuery("DELETE FROM StateManagementEntity").executeUpdate();
		em.createQuery("DELETE FROM ResourceRegistrationEntity").executeUpdate();
		em.createQuery("DELETE FROM ForwardProgressEntity").executeUpdate();
		em.flush();
		et.commit();
	}
	
	
	/*
	 * The following runs all tests and controls the order of execution. If you allow
	 * the tests to execute individually, you cannot predict the order and some
	 * conflicts may occur.
	 */
	//@Ignore
	@Test
	public void runAllTests() throws Exception{
		testSanityJmx();
		testIM(); 
		testSanityState();
		testRefreshStateAudit();
		testStateCheck();
		testGetAllForwardProgressEntity();
		testStateAudit(); 
	}

	/*
	 * The following test verifies the following test cases:
	 * New Install
	 * New Install - Bad Dependency data
	 * Recovery from bad dependency data
	 * Lock
	 * Lock restart
	 * Unlock
	 * Unlock restart
	 */
	public void testSanityJmx() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testSanityJmx\n\n");
		cleanDb();
		cleanMyProp();
		IntegrityMonitor.deleteInstance();
		
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
		
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);
		logger.debug("\n\ntestSanityJmx starting im state \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				im.getStateManager().getAdminState(),
				im.getStateManager().getOpState(),
				im.getStateManager().getAvailStatus(),
				im.getStateManager().getStandbyStatus());
		// add an entry to Resource registration table in the DB for the dependent resource
		
		
		et = em.getTransaction();
		et.begin();
    	Query rquery = em.createQuery("Select r from ResourceRegistrationEntity r where r.resourceName=:rn");
    	rquery.setParameter("rn", dependent);

    	@SuppressWarnings("rawtypes")
    	List rrList = rquery.getResultList();
    	ResourceRegistrationEntity rrx = null;
    	if(rrList.isEmpty()){
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
    	
    	Thread.sleep(5000); //sleep 5 sec so the FPManager has time to check dependency health
		
		boolean sanityPass = true;
		try {
			im.evaluateSanity();
		} catch (Exception e) {
			logger.error("evaluateSanity exception: ", e);
			sanityPass = false;
		}
		assertFalse(sanityPass);  // expect sanity test to fail

		// undo dependency groups and jmx test properties settings
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		IntegrityMonitor.updateProperties(myProp);

		logger.debug("\ntestSantityJmx ending properties: {}", myProp);
		
		//We know at this point that the IM is disable-dependency.  We want to be
		//sure it will recover from this condition since the properties were
		//updated.
		
		
		logger.debug("\n\ntestSanityJmx ending im state\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				im.getStateManager().getAdminState(),
				im.getStateManager().getOpState(),
				im.getStateManager().getAvailStatus(),
				im.getStateManager().getStandbyStatus());
		
		//Destroy the instance
		logger.debug("\ntestSanityJmx restarting the IntegrityMonitor");
		IntegrityMonitor.deleteInstance();
		//Create a new instance.  It should recover from the disabled-dependency condition
		im = IntegrityMonitor.getInstance(resourceName, myProp);
		
		logger.debug("\n\ntestSanityJmx state after creating new im\n"+
					"AdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
						im.getStateManager().getAdminState(),
						im.getStateManager().getOpState(),
						im.getStateManager().getAvailStatus(),
						im.getStateManager().getStandbyStatus());

		//Verify the state
		assertEquals(im.getStateManager().getAdminState(), StateManagement.UNLOCKED);
		assertEquals(im.getStateManager().getOpState(), StateManagement.ENABLED);
		assertEquals(im.getStateManager().getAvailStatus(), StateManagement.NULL_VALUE);
		assertEquals(im.getStateManager().getStandbyStatus(), StateManagement.NULL_VALUE);
		
		//Test state manager via the IntegrityMonitor
		StateManagement sm = im.getStateManager();
		
		// Verify lock state
		sm.lock();
		logger.debug("\n\nsm.lock()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
			sm.getAdminState(),
			sm.getOpState(),
			sm.getAvailStatus(),
			sm.getStandbyStatus());
		assert(sm.getAdminState().equals(StateManagement.LOCKED));
		
		//Verify lock persists across a restart
		//Destroy the instance
		logger.debug("\ntestSanityJmx restarting the IntegrityMonitor");
		IntegrityMonitor.deleteInstance();
		//Create a new instance.  It should come up with the admin state locked
		im = IntegrityMonitor.getInstance(resourceName, myProp);
		sm = im.getStateManager();
		logger.debug("\n\ntestSanityJmx restart with AdminState=locked"+
					 "\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
					 	sm.getAdminState(),
					 	sm.getOpState(),
					 	sm.getAvailStatus(),
					 	sm.getStandbyStatus());
		assert(sm.getAdminState().equals(StateManagement.LOCKED));
		
		// Verify unlock
		sm.unlock();
		logger.debug("\n\ntestSanityJmx sm.unlock\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		assert(sm.getAdminState().equals(StateManagement.UNLOCKED));		
		
		// Verify unlock restart
		//Destroy the instance
		logger.debug("\ntestSanityJmx restarting the IntegrityMonitor");
		IntegrityMonitor.deleteInstance();
		//Create a new instance.  It should come up with the admin state locked
		im = IntegrityMonitor.getInstance(resourceName, myProp);
		sm = im.getStateManager();
		logger.debug("\n\ntestSanityJmx restart with AdminState=unlocked\n" + 
					"AdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
						sm.getAdminState(),
						sm.getOpState(),
						sm.getAvailStatus(),
						sm.getStandbyStatus());
		
		assert(sm.getAdminState().equals(StateManagement.UNLOCKED));
		
		logger.debug("\n\ntestSanityJmx: Exit\n\n");
	}
	

	public void testIM() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testIM\n\n");
		cleanDb();
		cleanMyProp();
		IntegrityMonitor.deleteInstance();
		
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
		
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);
		
		logger.debug("\n\nim initial state: \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
						im.getStateManager().getAdminState(),
						im.getStateManager().getOpState(),
						im.getStateManager().getAvailStatus(),
						im.getStateManager().getStandbyStatus());
		
		// test evaluate sanity
		boolean sanityPass = true;
		try {
			im.evaluateSanity();
		} catch (Exception e) {
			logger.error("evaluateSanity exception: ", e);
			sanityPass = false;
		}
		assertTrue(sanityPass);  // expect sanity test to pass
		
		//Test startTransaction - should works since it is unlocked
		boolean transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(transPass);
		
		//Test state manager via the IntegrityMonitor
		StateManagement sm = im.getStateManager();
		
		sm.lock();
		
		logger.debug("\n\nsm.lock()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
					sm.getAdminState(),
					sm.getOpState(),
					sm.getAvailStatus(),
					sm.getStandbyStatus());
		
		assert(sm.getAdminState().equals(StateManagement.LOCKED));
		
		//test startTransaction.  It should fail since it is locked
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
		
		sm.unlock();
		logger.debug("\n\nsm.unlock()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
						sm.getAdminState(),
						sm.getOpState(),
						sm.getAvailStatus(),
						sm.getStandbyStatus());
		assert(sm.getAdminState().equals(StateManagement.UNLOCKED));
		
		//test startTransaction.  It should succeed
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(transPass); //expect it to succeed
		
		sm.disableDependency();
		logger.debug("\n\nsm.disableDependency()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		
		assert(sm.getOpState().equals(StateManagement.DISABLED));
		assert(sm.getAvailStatus().equals(StateManagement.DEPENDENCY));
		
		//test startTransaction.  It should succeed since standby status is null and unlocked
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(transPass); //expect it to succeed
	
		sm.enableNoDependency();

		logger.debug("\n\nsm.enableNoDependency()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		assert(sm.getOpState().equals(StateManagement.ENABLED));
		//test startTransaction.  It should succeed since standby status is null and unlocked
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(transPass); //expect it to succeed
	
		
		sm.disableFailed();
		logger.debug("\n\nsm.disableFailed()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());

		assert(sm.getOpState().equals(StateManagement.DISABLED));
		assert(sm.getAvailStatus().equals(StateManagement.FAILED));
		//test startTransaction.  It should succeed since standby status is null and unlocked
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(transPass); //expect it to succeed
	
		sm.enableNotFailed();
		
		logger.debug("\n\nsm.enabledNotFailed()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		
		assert(sm.getOpState().equals(StateManagement.ENABLED));
		//test startTransaction.  It should succeed since standby status is null and unlocked
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(transPass); //expect it to succeed
	
		sm.demote();

		logger.debug("\n\nsm.demote()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		
		assert(sm.getStandbyStatus().equals(StateManagement.HOT_STANDBY));

		//test startTransaction.  It should fail since it is standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
		
		sm.promote();

		logger.debug("\n\nsm.promote()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		
		assert(sm.getStandbyStatus().equals(StateManagement.PROVIDING_SERVICE));

		//test startTransaction.  It should succeed since it is providing service
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(transPass); //expect it to succeed
		
		
		//Test the multi-valued availability status
		sm.disableDependency();
		sm.disableFailed();

		logger.debug("\n\nsm.disableDependency(), sm.disableFailed\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		
		assert(sm.getAvailStatus().equals(StateManagement.DEPENDENCY_FAILED));
		
		//Test startTransaction.  Should fail since standby status is cold standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
		
		sm.enableNoDependency();

		logger.debug("\n\nsm.enableNoDependency()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		assert(sm.getAvailStatus().equals(StateManagement.FAILED));
		//Test startTransaction.  Should fail since standby status is cold standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
	
		sm.disableDependency();
		sm.enableNotFailed();
		
		logger.debug("\n\nsm.disableDependency(),sm.enableNotFailed()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());		

		assert(sm.getAvailStatus().equals(StateManagement.DEPENDENCY));
		//Test startTransaction.  Should fail since standby status is cold standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
	
		sm.enableNoDependency();
		logger.debug("\n\nsm.enableNoDependency()\nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		assert(sm.getOpState().equals(StateManagement.ENABLED));
		//test startTransaction.  It should fail since standby status is hot standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			logger.error("startTransaction exception: ", e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
	
		logger.debug("\n\ntestIM: Exit\n\n");
	}
	

	public void testSanityState() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testSanityState\n\n");
		cleanDb();
		cleanMyProp();
		IntegrityMonitor.deleteInstance();
		
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
		
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);
		
		// Add a group1 dependent resources to put an entry in the forward progress table
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

		
		// Add a group2 dependent resource to the StateManagementEntity DB table and set its admin state to locked
		// Expect sanity test to fail.
		StateManagement stateManager = new StateManagement(emf, "group2_dep1");
		stateManager.lock();
		
		new StateManagement(emf, "group1_dep1");
		new StateManagement(emf, "group1_dep2");
		
		boolean sanityPass = true;
		// Call the dependency check directly instead of waiting for FPManager to do it.
		logger.debug("\n\nIntegrityMonitor.testSanityState: calling im.dependencyCheck()\n\n");
		im.dependencyCheck();
		try {
			im.evaluateSanity();
		} catch (Exception e) {
			logger.error("evaluateSanity exception: ", e);
			sanityPass = false;
		}
		assertFalse(sanityPass);  // expect sanity test to fail
		
		logger.debug("\n\ntestSanityState: Exit\n\n");
	}
	
	public void testRefreshStateAudit() throws Exception {
		logger.debug("\nIntegrityMonitorTest: testRefreshStateAudit Enter\n\n");
		cleanDb();
		cleanMyProp();
		IntegrityMonitor.deleteInstance();

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
		
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);

		//the state here is unlocked, enabled, null, null
		StateManagementEntity sme = null;

		Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");

		query.setParameter("resource", resourceName);

		//Just test that we are retrieving the right object
		@SuppressWarnings("rawtypes")
		List resourceList = query.getResultList();
		if (!resourceList.isEmpty()) {
			// exist 
			sme = (StateManagementEntity) resourceList.get(0);
			em.refresh(sme);

			logger.debug("??? -- Retrieve StateManagementEntity from database --\nsme.getResourceName() = {}\n"+
						"sme.getAdminState() = {}\nsme.getOpState() = {}\nsme.getAvailStatus() = {}\nsme.getStandbyStatus() = {}",
							sme.getResourceName(), 
							sme.getAdminState(),
							sme.getOpState(),
							sme.getAvailStatus(),
							sme.getStandbyStatus());

			assertTrue(sme.getAdminState().equals(StateManagement.UNLOCKED)); 
			assertTrue(sme.getOpState().equals(StateManagement.ENABLED)); 
			assertTrue(sme.getAvailStatus().equals(StateManagement.NULL_VALUE)); 
			assertTrue(sme.getStandbyStatus().equals(StateManagement.NULL_VALUE));
			logger.debug("--");
		} else {
			logger.debug("Record not found, resourceName: " + resourceName);
			assertTrue(false);
		}

		et = em.getTransaction();
		et.begin();

		sme.setStandbyStatus(StateManagement.COLD_STANDBY);
		em.persist(sme);
		em.flush();
		et.commit();

		// Run the refreshStateAudit
		im.executeRefreshStateAudit();

		//The refreshStateAudit should run and change the state to unlocked,enabled,null,hotstandby
		StateManagementEntity sme1 = null;

		Query query1 = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");

		query1.setParameter("resource", resourceName);

		@SuppressWarnings("rawtypes")
		List resourceList1 = query1.getResultList();
		if (!resourceList1.isEmpty()) {
			// exist 
			sme1 = (StateManagementEntity) resourceList1.get(0);
			em.refresh(sme1);
			logger.debug("??? -- Retrieve StateManagementEntity from database --\nsme1.getResourceName() = {}\n" +
							"sme1.getAdminState() = {}\nsme1.getOpState() = {}\nsme1.getAvailStatus() = {}\nsme1.getStandbyStatus() = {}",
							sme1.getResourceName(), 
							sme1.getAdminState(),
							sme1.getOpState(),
							sme1.getAvailStatus(),
							sme1.getStandbyStatus());

			assertTrue(sme1.getAdminState().equals(StateManagement.UNLOCKED)); 
			assertTrue(sme1.getOpState().equals(StateManagement.ENABLED)); 
			assertTrue(sme1.getAvailStatus().equals(StateManagement.NULL_VALUE)); 
			assertTrue(sme1.getStandbyStatus().equals(StateManagement.HOT_STANDBY)); 
			logger.debug("--");
		} else {
			logger.debug("Record not found, resourceName: " + resourceName);
			assertTrue(false);
		}

		logger.debug("\nIntegrityMonitorTest: testRefreshStateAudit Exit\n\n");
	}
	
	public void testStateCheck() throws Exception {
		logger.debug("\nIntegrityMonitorTest: Entering testStateCheck\n\n");
		cleanDb();
		cleanMyProp();
		IntegrityMonitor.deleteInstance();
		
		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "group1_dep1");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		myProp.put(IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD, "1");
		/*
		 *  The monitorInterval is set to 10 and the failedCounterThreshold is 1
		 *  because stateCheck() uses the faileCounterThreshold * monitorInterval to determine
		 *  if an entry is stale, it will be stale after 10 seconds.
		 */
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, "5");
		/* 
		 * We accelerate the test transaction and write FPC intervals because we don't want
		 * there to be any chance of a FPC failure because of the short monitor interval
		 */
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, "1");
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, "2");
		// Disable the refresh state audit
		myProp.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS, "-1");
		// The maximum time in seconds to determine that a FPC entry is stale
		myProp.put(IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL, "5");
		myProp.put(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, "5");
		
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);
		
		// Add a group1 dependent resources to put an entry in the forward progress table
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
		
		boolean sanityPass = true;
		//Thread.sleep(15000);
		//Thread.sleep(5000);
		try {
			im.evaluateSanity();
		} catch (Exception e) {
			logger.error("testStateCheck: After 5 sec sleep - evaluateSanity exception: ", e);
			sanityPass = false;
		}
		assertTrue(sanityPass);  // expect sanity test to pass
		
		//now wait 10 seconds.  The dependency entry  is checked every 10 sec.  So, even in the worst case
		//it should now be stale and the sanity check should fail
		
		sanityPass = true;
		Thread.sleep(10000);
		try {
			im.evaluateSanity();
		} catch (Exception e) {
			logger.error("testStateCheck: After 15 sec sleep - evaluateSanity exception: ", e);
			sanityPass = false;
		}
		assertFalse(sanityPass);  // expect sanity test to fail
		
		logger.debug("\n\ntestStateCheck: Exit\n\n");
	}
	
	public void testGetAllForwardProgressEntity() throws Exception{
		logger.debug("\nIntegrityMonitorTest: Entering testGetAllForwardProgressEntity\n\n");
		cleanDb();
		cleanMyProp();
		IntegrityMonitor.deleteInstance();
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
		
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);
		
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

		logger.debug("\nIntegrityMonitorTest:testGetAllForwardProgressEntity Calling im.getAllForwardProgressEntity()\n\n");
		ArrayList<ForwardProgressEntity> fpeList = im.getAllForwardProgressEntity();
		
		assertTrue(fpeList.size()==4);
		
		logger.debug("\nIntegrityMonitorTest: Exit testGetAllForwardProgressEntity\n\n");
	}
	
	public void testStateAudit() throws Exception{
		logger.debug("\nIntegrityMonitorTest: Entering testStateAudit\n\n");
		cleanDb();
		cleanMyProp();
		IntegrityMonitor.deleteInstance();
		
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

		
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);
		StateManagement sm = im.getStateManager();
		
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
		StateManagementEntity sme3= new StateManagementEntity();
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
		
		Query updateQuery = em.createQuery("UPDATE ForwardProgressEntity f "
				+ "SET f.lastUpdated = :newDate "
				+ "WHERE f.resourceName=:resource");
		updateQuery.setParameter("newDate", staleDate, TemporalType.TIMESTAMP);
		updateQuery.setParameter("resource", fpe1.getResourceName());
		
		et = em.getTransaction();
		et.begin();
		updateQuery.executeUpdate();
		et.commit();
		
		logger.debug("\nIntegrityMonitorTest:testStateAudit Calling im.getAllForwardProgressEntity()\n\n");
		ArrayList<ForwardProgressEntity> fpeList = im.getAllForwardProgressEntity();
		
		logger.debug("\n\n");
		logger.debug("IntegrityMonitorTest:testStateAudit:ForwardProgressEntity entries");
		for(ForwardProgressEntity myFpe : fpeList){
			logger.debug("\n    ResourceName: {}" + 
						 "\n        LastUpdated: {}",
						 myFpe.getResourceName(),
						 myFpe.getLastUpdated());
		}
		logger.debug("\n\n");
		
		logger.debug("\nIntegrityMonitorTest:testStateAudit getting list of StateManagementEntity entries\n\n");
		Query query = em.createQuery("SELECT s FROM StateManagementEntity s");
		List<?> smeList = query.getResultList();
		
		logger.debug("\n\n");
		logger.debug("IntegrityMonitorTest:testStateAudit:StateManagementEntity entries");
		for(Object mySme : smeList){
			StateManagementEntity tmpSme = (StateManagementEntity) mySme;
			em.refresh(tmpSme);
			logger.debug("\n    ResourceName: {}" + 
					 "\n        AdminState: {}" + 
					 "\n        OpState: {}" + 
					 "\n        AvailStatus: {}" + 
					 "\n        StandbyStatus: {}",
					 tmpSme.getResourceName(),
					 tmpSme.getAdminState(),
					 tmpSme.getOpState(),
					 tmpSme.getAvailStatus(),
					 tmpSme.getStandbyStatus()
					);
		}
		logger.debug("\n\n");
		
		em.refresh(sme1);
		assertTrue(sme1.getOpState().equals(StateManagement.ENABLED));
		
		logger.debug("IntegrityMonitorTest:testStateAudit: calling stateAudit()");
		im.executeStateAudit();
		logger.debug("IntegrityMonitorTest:testStateAudit: call to stateAudit() complete");
		
		logger.debug("\nIntegrityMonitorTest:testStateAudit getting list of StateManagementEntity entries\n\n");
		smeList = query.getResultList();
		
		logger.debug("\n\n");
		logger.debug("IntegrityMonitorTest:testStateAudit:StateManagementEntity entries");
		for(Object mySme : smeList){
			StateManagementEntity tmpSme = (StateManagementEntity) mySme;
			em.refresh(tmpSme);
			logger.debug("\n    ResourceName: {}" + 
					 "\n        AdminState: {}" + 
					 "\n        OpState: {}" + 
					 "\n        AvailStatus: {}" + 
					 "\n        StandbyStatus: {}",
					 tmpSme.getResourceName(),
					 tmpSme.getAdminState(),
					 tmpSme.getOpState(),
					 tmpSme.getAvailStatus(),
					 tmpSme.getStandbyStatus()
					);
		}
		logger.debug("\n\n");
		
		em.refresh(sme1);
		assertTrue(sme1.getOpState().equals(StateManagement.DISABLED));
		
		//Now let's add sme2 to the mix
		updateQuery = em.createQuery("UPDATE ForwardProgressEntity f "
				+ "SET f.lastUpdated = :newDate "
				+ "WHERE f.resourceName=:resource");
		updateQuery.setParameter("newDate", staleDate, TemporalType.TIMESTAMP);
		updateQuery.setParameter("resource", fpe2.getResourceName());
		
		et = em.getTransaction();
		et.begin();
		updateQuery.executeUpdate();
		et.commit();
		
		//Give it a chance to write the DB and run the audit
		logger.debug("IntegrityMonitorTest:testStateAudit: (restart4) Running State Audit");
		Thread.sleep(2000);
		im.executeStateAudit();
		Thread.sleep(1000);
		logger.debug("IntegrityMonitorTest:testStateAudit: (restart4) State Audit complete");
		
		//Now check its state
		logger.debug("\nIntegrityMonitorTest:testStateAudit (restart4) getting list of StateManagementEntity entries\n\n");
		smeList = query.getResultList();
		
		logger.debug("\n\n");
		logger.debug("IntegrityMonitorTest:testStateAudit:StateManagementEntity (restart4) entries");
		for(Object mySme : smeList){
			StateManagementEntity tmpSme = (StateManagementEntity) mySme;
			em.refresh(tmpSme);

			logger.debug("\n    (restart4) ResourceName: {}" + 
					 "\n        AdminState: {}" + 
					 "\n        OpState: {}" + 
					 "\n        AvailStatus: {}" + 
					 "\n        StandbyStatus: {}",
					 tmpSme.getResourceName(),
					 tmpSme.getAdminState(),
					 tmpSme.getOpState(),
					 tmpSme.getAvailStatus(),
					 tmpSme.getStandbyStatus()
					);
		}
		logger.debug("\n\n");
		
		em.refresh(sme1);
		assertTrue(sme1.getOpState().equals(StateManagement.DISABLED));
		
		em.refresh(sme2);
		assertTrue(sme2.getOpState().equals(StateManagement.DISABLED));
		
		logger.debug("\nIntegrityMonitorTest: Exit testStateAudit\n\n");
		System.out.println("\n\ntestStateAudit: Exit\n\n");
	}
}
