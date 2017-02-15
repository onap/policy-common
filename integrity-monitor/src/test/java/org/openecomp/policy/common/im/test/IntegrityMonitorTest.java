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

package org.openecomp.policy.common.im.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;



import org.openecomp.policy.common.im.IntegrityMonitor;
import org.openecomp.policy.common.im.IntegrityMonitorProperties;
import org.openecomp.policy.common.im.StateManagement;
import org.openecomp.policy.common.im.jpa.ForwardProgressEntity;
import org.openecomp.policy.common.im.jpa.ImTestEntity;
import org.openecomp.policy.common.im.jpa.ResourceRegistrationEntity;
import org.openecomp.policy.common.im.jpa.StateManagementEntity;
import org.openecomp.policy.common.logging.flexlogger.FlexLogger; 
import org.openecomp.policy.common.logging.flexlogger.Logger;

public class IntegrityMonitorTest {
	private static Logger logger = FlexLogger.getLogger(IntegrityMonitorTest.class);
	private static Properties myProp;
	private static EntityManagerFactory emf;
	private static EntityManager em;
	private static EntityTransaction et;
	private static String resourceName;
	private static Properties systemProps;
	
	@BeforeClass
	public static void setUpClass() throws Exception {

	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		IntegrityMonitor.isUnitTesting = true;
		
		myProp = new Properties();
		myProp.put(IntegrityMonitorProperties.DB_DRIVER, IntegrityMonitorProperties.DEFAULT_DB_DRIVER);
		myProp.put(IntegrityMonitorProperties.DB_URL, IntegrityMonitorProperties.DEFAULT_DB_URL);
		myProp.put(IntegrityMonitorProperties.DB_USER, IntegrityMonitorProperties.DEFAULT_DB_USER);
		myProp.put(IntegrityMonitorProperties.DB_PWD, IntegrityMonitorProperties.DEFAULT_DB_PWD);
		myProp.put(IntegrityMonitorProperties.SITE_NAME, "SiteA");
		myProp.put(IntegrityMonitorProperties.NODE_TYPE, "pap");
		
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
	@Ignore // Test passed 10/18/16 
	@Test
	public void testSanityJmx() throws Exception {
		System.out.println("\nIntegrityMonitorTest: Entering testSanityJmx\n\n");
		
		String dependent = "group1_logparser";
		
		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, dependent);
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "true");
		IntegrityMonitor.updateProperties(myProp);
		
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);
		System.out.println("\n\ntestSanityJmx starting im state"
				+ "\nAdminState = " + im.getStateManager().getAdminState()
				+ "\nOpState() = " + im.getStateManager().getOpState()
				+ "\nAvailStatus = " + im.getStateManager().getAvailStatus()
				+ "\nStandbyStatus = " + im.getStateManager().getStandbyStatus()
				+ "\n");
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
    		System.out.println("Adding resource " + dependent + " to ResourceRegistration table");  	
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
    	
    	Thread.sleep(15000); //sleep 15 sec so the FPManager has time to call evaluateSanty()
		
		boolean sanityPass = true;
		try {
			im.evaluateSanity();
		} catch (Exception e) {
			System.out.println("evaluateSanity exception: " + e);
			sanityPass = false;
		}
		assertFalse(sanityPass);  // expect sanity test to fail

		// undo dependency groups and jmx test properties settings
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		IntegrityMonitor.updateProperties(myProp);

		System.out.println("\ntestSantityJmx ending properties: " + myProp);
		
		//We know at this point that the IM is disable-dependency.  We want to be
		//sure it will recover from this condition since the properties were
		//updated.
		
		
		System.out.println("\n\ntestSanityJmx ending im state"
				+ "\nAdminState = " + im.getStateManager().getAdminState()
				+ "\nOpState() = " + im.getStateManager().getOpState()
				+ "\nAvailStatus = " + im.getStateManager().getAvailStatus()
				+ "\nStandbyStatus = " + im.getStateManager().getStandbyStatus()
				+ "\n");
		
		//Destroy the instance
		System.out.println("\ntestSanityJmx restarting the IntegrityMonitor");
		IntegrityMonitor.deleteInstance();
		//Create a new instance.  It should recover from the disabled-dependency condition
		im = IntegrityMonitor.getInstance(resourceName, myProp);
		
		System.out.println("\n\ntestSanityJmx state after creating new im"
				+ "\nAdminState = " + im.getStateManager().getAdminState()
				+ "\nOpState() = " + im.getStateManager().getOpState()
				+ "\nAvailStatus = " + im.getStateManager().getAvailStatus()
				+ "\nStandbyStatus = " + im.getStateManager().getStandbyStatus()
				+ "\n");

		//Verify the state
		assertEquals(im.getStateManager().getAdminState(), StateManagement.UNLOCKED);
		assertEquals(im.getStateManager().getOpState(), StateManagement.ENABLED);
		assertEquals(im.getStateManager().getAvailStatus(), StateManagement.NULL_VALUE);
		assertEquals(im.getStateManager().getStandbyStatus(), StateManagement.NULL_VALUE);
		
		//Test state manager via the IntegrityMonitor
		StateManagement sm = im.getStateManager();
		
		// Verify lock state
		sm.lock();
		System.out.println("\n\nsm.lock()"
			+ "\nAdminState = " + sm.getAdminState()
			+ "\nOpState() = " + sm.getOpState()
			+ "\nAvailStatus = " + sm.getAvailStatus()
			+ "\nStandbyStatus = " + sm.getStandbyStatus()
			+ "\n");
		assert(sm.getAdminState().equals(StateManagement.LOCKED));
		
		//Verify lock persists across a restart
		//Destroy the instance
		System.out.println("\ntestSanityJmx restarting the IntegrityMonitor");
		IntegrityMonitor.deleteInstance();
		//Create a new instance.  It should come up with the admin state locked
		im = IntegrityMonitor.getInstance(resourceName, myProp);
		sm = im.getStateManager();
		System.out.println("\n\ntestSanityJmx restart with AdminState=locked"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getAdminState().equals(StateManagement.LOCKED));
		
		// Verify unlock
		sm.unlock();
		System.out.println("\n\ntestSanityJmx sm.unlock"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getAdminState().equals(StateManagement.UNLOCKED));		
		
		// Verify unlock restart
		//Destroy the instance
		System.out.println("\ntestSanityJmx restarting the IntegrityMonitor");
		IntegrityMonitor.deleteInstance();
		//Create a new instance.  It should come up with the admin state locked
		im = IntegrityMonitor.getInstance(resourceName, myProp);
		sm = im.getStateManager();
		System.out.println("\n\ntestSanityJmx restart with AdminState=unlocked"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getAdminState().equals(StateManagement.UNLOCKED));
		
		System.out.println("\n\ntestSanityJmx: Exit\n\n");
	}
	

	@Ignore  // Test passed 10/18/16 
	@Test
	public void testIM() throws Exception {
		System.out.println("\nIntegrityMonitorTest: Entering testIM\n\n");
		
		// parameters are passed via a properties file
		
		/*
		 * Create an IntegrityMonitor
		 * NOTE: This uses the database that was created above.  So, this MUST follow the creation
		 * of the DB
		 */
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);
		
		System.out.println("\n\nim before sleep"
				+ "\nAdminState = " + im.getStateManager().getAdminState()
				+ "\nOpState() = " + im.getStateManager().getOpState()
				+ "\nAvailStatus = " + im.getStateManager().getAvailStatus()
				+ "\nStandbyStatus = " + im.getStateManager().getStandbyStatus()
				+ "\n");
		
		// wait for test transactions to fire and increment fpc
		Thread.sleep(20000);
		
		System.out.println("\n\nim after sleep"
				+ "\nAdminState = " + im.getStateManager().getAdminState()
				+ "\nOpState() = " + im.getStateManager().getOpState()
				+ "\nAvailStatus = " + im.getStateManager().getAvailStatus()
				+ "\nStandbyStatus = " + im.getStateManager().getStandbyStatus()
				+ "\n");
		
		// test evaluate sanity
		boolean sanityPass = true;
		try {
			im.evaluateSanity();
		} catch (Exception e) {
			System.out.println("evaluateSanity exception: " + e);
			sanityPass = false;
		}
		assertTrue(sanityPass);  // expect sanity test to pass
		
		//Test startTransaction - should works since it is unlocked
		boolean transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(transPass);
		
		//Test state manager via the IntegrityMonitor
		StateManagement sm = im.getStateManager();
		
		sm.lock();
		System.out.println("\n\nsm.lock()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getAdminState().equals(StateManagement.LOCKED));
		
		//test startTransaction.  It should fail since it is locked
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
		
		sm.unlock();
		System.out.println("\n\nsm.unlock()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getAdminState().equals(StateManagement.UNLOCKED));
		
		//test startTransaction.  It should succeed
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(transPass); //expect it to succeed
		
		sm.disableDependency();
		System.out.println("\n\nsm.disableDependency()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getOpState().equals(StateManagement.DISABLED));
		assert(sm.getAvailStatus().equals(StateManagement.DEPENDENCY));
		
		//test startTransaction.  It should succeed since standby status is null and unlocked
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(transPass); //expect it to succeed
	
		sm.enableNoDependency();
		System.out.println("\n\nsm.enableNoDependency()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
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
		System.out.println("\n\nsm.disableFailed()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
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
		System.out.println("\n\nsm.enabledNotFailed()"
							+ "\nAdminState = " + sm.getAdminState()
							+ "\nOpState() = " + sm.getOpState()
							+ "\nAvailStatus = " + sm.getAvailStatus()
							+ "\nStandbyStatus = " + sm.getStandbyStatus()
							+ "\n");
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
	
		sm.demote();
		System.out.println("\n\nsm.demote()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getStandbyStatus().equals(StateManagement.HOT_STANDBY));

		//test startTransaction.  It should fail since it is standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
		
		sm.promote();
		System.out.println("\n\nsm.promote()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getStandbyStatus().equals(StateManagement.PROVIDING_SERVICE));

		//test startTransaction.  It should succeed since it is providing service
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(transPass); //expect it to succeed
		
		
		//Test the multi-valued availability status
		sm.disableDependency();
		sm.disableFailed();
		System.out.println("\n\nsm.disableDependency(), sm.disableFailed"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getAvailStatus().equals(StateManagement.DEPENDENCY_FAILED));
		
		//Test startTransaction.  Should fail since standby status is cold standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
		
		sm.enableNoDependency();
		System.out.println("\n\nsm.enableNoDependency()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getAvailStatus().equals(StateManagement.FAILED));
		//Test startTransaction.  Should fail since standby status is cold standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
	
		sm.disableDependency();
		sm.enableNotFailed();
		System.out.println("\n\nsm.disableDependency(),sm.enableNotFailed()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getAvailStatus().equals(StateManagement.DEPENDENCY));
		//Test startTransaction.  Should fail since standby status is cold standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
	
		sm.enableNoDependency();
		System.out.println("\n\nsm.enableNoDependency()"
				+ "\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
		assert(sm.getOpState().equals(StateManagement.ENABLED));
		//test startTransaction.  It should fail since standby status is hot standby
		transPass = true;
		try{
			im.startTransaction();
		} catch (Exception e){
			System.out.println("startTransaction exception: " + e);
			transPass = false;
		}
		assertTrue(!transPass); //expect it to fail
	
		System.out.println("\n\ntestIM: Exit\n\n");
	}
	

	@Ignore  // Test passed 10/18/16 
	@Test
	public void testSanityState() throws Exception {
		System.out.println("\nIntegrityMonitorTest: Entering testSanityState\n\n");
		
		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "group1_dep1,group1_dep2; group2_dep1");
		IntegrityMonitor.updateProperties(myProp);
		
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
		
		//Now add new group1 stateManager instances
		StateManagement sm2 = new StateManagement(emf, "group1_dep1");
		StateManagement sm3 = new StateManagement(emf, "group1_dep2");
		
		boolean sanityPass = true;
		Thread.sleep(15000);
		try {
			im.evaluateSanity();
		} catch (Exception e) {
			System.out.println("evaluateSanity exception: " + e);
			sanityPass = false;
		}
		assertFalse(sanityPass);  // expect sanity test to fail
		
		// undo dependency groups and jmx test properties settings
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		IntegrityMonitor.updateProperties(myProp);

		et = em.getTransaction();
		
		et.begin();
		// Make sure we leave the DB clean
		em.createQuery("DELETE FROM StateManagementEntity").executeUpdate();
		em.createQuery("DELETE FROM ResourceRegistrationEntity").executeUpdate();
		em.createQuery("DELETE FROM ForwardProgressEntity").executeUpdate();

		em.flush();
		et.commit();
		
		System.out.println("\n\ntestSanityState: Exit\n\n");
	}
	
	@Ignore  // Test passed 10/18/16 
	@Test
	public void testRefreshStateAudit() throws Exception {
		logger.debug("\nIntegrityMonitorTest: testRefreshStateAudit Enter\n\n");

		// parameters are passed via a properties file
		myProp.put(IntegrityMonitorProperties.DEPENDENCY_GROUPS, "");
		myProp.put(IntegrityMonitorProperties.TEST_VIA_JMX, "false");
		IntegrityMonitor.updateProperties(myProp);
		
		et = em.getTransaction();
		et.begin();

		// Make sure we leave the DB clean
		em.createQuery("DELETE FROM StateManagementEntity").executeUpdate();
		em.createQuery("DELETE FROM ResourceRegistrationEntity").executeUpdate();
		em.createQuery("DELETE FROM ForwardProgressEntity").executeUpdate();

		em.flush();
		et.commit();

		IntegrityMonitor.deleteInstance();

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

			logger.debug("??? -- Retrieve StateManagementEntity from database --"
					+ "\nsme.getResourceName() = " + sme.getResourceName() 
					+ "\nsme.getAdminState() = " + sme.getAdminState()
					+ "\nsme.getOpState() = " + sme.getOpState()
					+ "\nsme.getAvailStatus() = " + sme.getAvailStatus()
					+ "\nsme.getStandbyStatus() = " + sme.getStandbyStatus());

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

		Thread.sleep(65000);

		//The refreshStateAudit should run and change the state to unlocked,enabled,null,hotstandby
		StateManagementEntity sme1 = null;

		Query query1 = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");

		query1.setParameter("resource", resourceName);

		//Just test that we are retrieving the right object
		@SuppressWarnings("rawtypes")
		List resourceList1 = query1.getResultList();
		if (!resourceList1.isEmpty()) {
			// exist 
			sme1 = (StateManagementEntity) resourceList1.get(0);
			em.refresh(sme1);
			logger.debug("??? -- Retrieve StateManagementEntity from database --"
					+ "\nsme1.getResourceName() = " + sme1.getResourceName() 
					+ "\nsme1.getAdminState() = " + sme1.getAdminState()
					+ "\nsme1.getOpState() = " + sme1.getOpState()
					+ "\nsme1.getAvailStatus() = " + sme1.getAvailStatus()
					+ "\nsme1.getStandbyStatus() = " + sme1.getStandbyStatus());

			assertTrue(sme1.getAdminState().equals(StateManagement.UNLOCKED)); 
			assertTrue(sme1.getOpState().equals(StateManagement.ENABLED)); 
			assertTrue(sme1.getAvailStatus().equals(StateManagement.NULL_VALUE)); 
			assertTrue(sme1.getStandbyStatus().equals(StateManagement.HOT_STANDBY)); 
			logger.debug("--");
		} else {
			logger.debug("Record not found, resourceName: " + resourceName);
			assertTrue(false);
		}

		et = em.getTransaction();
		et.begin();

		// Make sure we leave the DB clean
		em.createQuery("DELETE FROM StateManagementEntity").executeUpdate();
		em.createQuery("DELETE FROM ResourceRegistrationEntity").executeUpdate();
		em.createQuery("DELETE FROM ForwardProgressEntity").executeUpdate();

		em.flush();
		et.commit();

		IntegrityMonitor.deleteInstance();

		logger.debug("\nIntegrityMonitorTest: testRefreshStateAudit Exit\n\n");
	}
}
