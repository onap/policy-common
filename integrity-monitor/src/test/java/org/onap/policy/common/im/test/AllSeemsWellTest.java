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

import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

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
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllSeemsWellTest {
	private static Logger logger = LoggerFactory.getLogger(AllSeemsWellTest.class);
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
		
		myProp = new Properties();
		myProp.put(IntegrityMonitorProperties.DB_DRIVER, AllSeemsWellTest.DEFAULT_DB_DRIVER);
		myProp.put(IntegrityMonitorProperties.DB_URL, AllSeemsWellTest.DEFAULT_DB_URL);
		myProp.put(IntegrityMonitorProperties.DB_USER, AllSeemsWellTest.DEFAULT_DB_USER);
		myProp.put(IntegrityMonitorProperties.DB_PWD, AllSeemsWellTest.DEFAULT_DB_PWD);
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

	//Ignore
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
		 *  The monitorInterval is 5 and the failedCounterThreshold is 1
		 *  A forward progress will be stale after 5 seconds.
		 */
		
		et = em.getTransaction();
		et.begin();

		// Make sure we start with the DB clean
		em.createQuery("DELETE FROM StateManagementEntity").executeUpdate();
		em.createQuery("DELETE FROM ResourceRegistrationEntity").executeUpdate();
		em.createQuery("DELETE FROM ForwardProgressEntity").executeUpdate();

		em.flush();
		et.commit();

		IntegrityMonitor.deleteInstance();
		
		IntegrityMonitor im = IntegrityMonitor.getInstance(resourceName, myProp);

		StateManagement sm = im.getStateManager();
		
		//Give it time to set the states in the DB
		Thread.sleep(15000);

		//Check the state
		logger.debug("\n\testAllSeemsWell starting im state \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		
		assertTrue(sm.getOpState().equals(StateManagement.ENABLED));
		
		//Indicate a failure
		im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLNOTWELL, 
				"'AllSeemsWellTest - ALLNOTWELL'");
		
		//Wait for the state to change due to ALLNOTWELL
		Thread.sleep(15000);
		//Check the state
		logger.debug("\n\ntestAllSeemsWell after ALLNOTWELL: im state \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		
		//assertTrue(sm.getOpState().equals(StateManagement.DISABLED));
		
		Map<String, String> allNotWellMap = im.getAllNotWellMap();
		for(String key: allNotWellMap.keySet()){
			logger.debug("AllSeemsWellTest: allNotWellMap: key = {}  msg = {}", key, allNotWellMap.get(key));							
		}
		//assertTrue(allNotWellMap.size() == 1);

		Map<String,String> allSeemsWellMap = im.getAllSeemsWellMap();
		//assertTrue(allSeemsWellMap.isEmpty());
		
		//Return to normal
		im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLSEEMSWELL, 
				"'AllSeemsWellTest - ALLSEEMSWELL'");
		
		//Wait for the state to change due to ALLNOTWELL
		Thread.sleep(15000);
		//Check the state
		logger.debug("\n\ntestAllSeemsWell after ALLSEEMSWELL: im state \nAdminState = {}\nOpState() = {}\nAvailStatus = {}\nStandbyStatus = {}\n",
				sm.getAdminState(),
				sm.getOpState(),
				sm.getAvailStatus(),
				sm.getStandbyStatus());
		
		//assertTrue(sm.getOpState().equals(StateManagement.ENABLED));
		
		allNotWellMap = im.getAllNotWellMap();
		assertTrue(allNotWellMap.isEmpty());
		
		allSeemsWellMap = im.getAllSeemsWellMap();
		assertTrue(allSeemsWellMap.size() == 1);
		for(String key: allSeemsWellMap.keySet()){
			logger.debug("AllSeemsWellTest: allSeemsWellMap: key = {}  msg = {}", key, allSeemsWellMap.get(key));							
		}
		
		//Check for null parameters
		try{
			im.allSeemsWell(null, IntegrityMonitorProperties.ALLSEEMSWELL, 
					"'AllSeemsWellTest - ALLSEEMSWELL'");
			assertTrue(false);
		}catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try{
			im.allSeemsWell("", IntegrityMonitorProperties.ALLSEEMSWELL, 
					"'AllSeemsWellTest - ALLSEEMSWELL'");
			assertTrue(false);
		}catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try{
			im.allSeemsWell(this.getClass().getName(), null, 
					"'AllSeemsWellTest - ALLSEEMSWELL'");
			assertTrue(false);
		}catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try{
			im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLSEEMSWELL, 
					null);
			assertTrue(false);
		}catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try{
			im.allSeemsWell(this.getClass().getName(), IntegrityMonitorProperties.ALLSEEMSWELL, 
					"");
			assertTrue(false);
		}catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// undo settings
		myProp.put(IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD, Integer.toString(IntegrityMonitorProperties.DEFAULT_FAILED_COUNTER_THRESHOLD));
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, Integer.toString(IntegrityMonitorProperties.DEFAULT_MONITOR_INTERVAL));
		myProp.put(IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD, Integer.toString(IntegrityMonitorProperties.DEFAULT_FAILED_COUNTER_THRESHOLD));
		myProp.put(IntegrityMonitorProperties.FP_MONITOR_INTERVAL, Integer.toString(IntegrityMonitorProperties.DEFAULT_MONITOR_INTERVAL));
		myProp.put(IntegrityMonitorProperties.TEST_TRANS_INTERVAL, Integer.toString(IntegrityMonitorProperties.DEFAULT_TEST_INTERVAL));
		myProp.put(IntegrityMonitorProperties.WRITE_FPC_INTERVAL, Integer.toString(IntegrityMonitorProperties.DEFAULT_WRITE_FPC_INTERVAL));
		IntegrityMonitor.updateProperties(myProp);

		et = em.getTransaction();
		
		et.begin();
		// Make sure we leave the DB clean
		em.createQuery("DELETE FROM StateManagementEntity").executeUpdate();
		em.createQuery("DELETE FROM ResourceRegistrationEntity").executeUpdate();
		em.createQuery("DELETE FROM ForwardProgressEntity").executeUpdate();

		em.flush();
		et.commit();
		
		logger.debug("\n\ntestAllSeemsWell: Exit\n\n");
	}

}
