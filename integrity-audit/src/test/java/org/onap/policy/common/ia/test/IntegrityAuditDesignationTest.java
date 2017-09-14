/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
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

package org.onap.policy.common.ia.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;



//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.onap.policy.common.ia.AuditThread;
import org.onap.policy.common.ia.DbDAO;
import org.onap.policy.common.ia.IntegrityAudit;
import org.onap.policy.common.ia.IntegrityAuditProperties;
import org.onap.policy.common.logging.flexlogger.FlexLogger; 
import org.onap.policy.common.logging.flexlogger.Logger;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 * 
 * If any have been ignored (@Ignore) they will not run at the same time
 * as others. You should run them as JUnits by themselves.
 */
public class IntegrityAuditDesignationTest {
	
	private static Logger logger = FlexLogger.getLogger(IntegrityAuditDesignationTest.class);
	
	/*
	 * Provides a little cushion for timing events.
	 */
	private static int FUDGE_FACTOR = 15000;
	
	private static String persistenceUnit;
	private static Properties properties;
	private static String resourceName;
	private static final String TEST_LOG = "./testingLogs/common-modules/integrity-audit/debug.log";
	@Before
	public void setUp() throws Exception {

		
		System.out.println("setUp: Clearing debug.log");
		FileOutputStream fstream = new FileOutputStream(TEST_LOG);
		fstream.close();

		logger.info("setUp: Entering");

		IntegrityAudit.setUnitTesting(true);
		
		properties = new Properties();
		properties.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		
		/*
		 * AuditThread.AUDIT_THREAD_SLEEP_INTERVAL is also five seconds, so
		 * setting AUDIT_PERIOD_SECONDS to 5 ensures that whether or not audit
		 * has already been run on a node, it will sleep the same amount of
		 * time.
		 */
		properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
				
		persistenceUnit = "testPU";
		resourceName = "pdp1";
		
		
		//Clean up the DB		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		
		EntityManager em = emf.createEntityManager();
		// Start a transaction
		EntityTransaction et = em.getTransaction();

		et.begin();

		// if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not found, create a new entry
		em.createQuery("Delete from IntegrityAuditEntity").executeUpdate();

		// commit transaction
		et.commit();
		em.close();
		logger.info("setUp: Exiting");
		
	}
	

	@After
	public void tearDown() throws Exception {
		
		logger.info("tearDown: Entering");
				
		logger.info("tearDown: Exiting");

	}

	/*
	 * Tests designation logic when only one functioning resource is in play.  Designation
	 * should stay with single resource.
	 * 
	 * Note: console.log must be examined to ascertain whether or not this test was successful.
	 */
	//@Ignore
	@Test
	public void testOneResource() throws Exception {
		
		logger.info("testOneResource: Entering");
		
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName, persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) pdp1 to run audit (15 seconds)
		 * 
		 * 2) Logic to detect that no other node is available for designation (60 seconds)
		 * 
		 * 3) pdp1 to run audit again (15 seconds)
		 */
		logger.info("testOneResource: Sleeping 100 seconds");
		Thread.sleep(100000);
		
		logger.info("testOneResource: Stopping audit thread");
		integrityAudit.stopAuditThread();
		
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		
		String rName = "";
		while ((strLine = br.readLine()) != null)   {
			// parse strLine to obtain what you want 
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				rName = strLine.substring(startIndex, endIndex);
				logger.info("testOneResource: rName: " + rName);
				assertEquals("pdp1", rName);
			}
		}	
		fstream.close();
		
		/*
		 * Test fix for ONAPD2TD-783: Audit fails to run when application is restarted.
		 */
		integrityAudit.startAuditThread();
		
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) pdp1 to run audit (15 seconds)
		 * 
		 * 2) Logic to detect that no other node is available for designation (60 seconds)
		 * 
		 * 3) pdp1 to run audit again (15 seconds)
		 */
		logger.info("testOneResource: Sleeping 100 seconds for second time");
		Thread.sleep(100000);
		
		logger.info("testOneResource: Stopping audit thread for second time");
		integrityAudit.stopAuditThread();
		
		fstream = new FileInputStream(TEST_LOG);
		br = new BufferedReader(new InputStreamReader(fstream));
		
		rName = "";
		while ((strLine = br.readLine()) != null)   {
			// parse strLine to obtain what you want 
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				rName = strLine.substring(startIndex, endIndex);
				logger.info("testOneResource: rName: " + rName);
				assertEquals("pdp1", rName);
			}
		}	
		fstream.close();
		
		logger.info("testOneResource: Exiting");

	}
	
	/*
	 * Tests designation logic when two functioning resources are in play.
	 * Designation should alternate between resources.
	 * 
	 * Note: console.log must be examined to ascertain whether or not this test
	 * was successful. A quick way of examining the log is to search for the
	 * string "audit simulation":
	 * 
	 * As you can see from the "dbAuditSimulate" method, when it executes, it
	 * logs the "Starting audit simulation..." message and when it finishes, it
	 * logs the "Finished audit simulation..." message. By looking for these
	 * messages, you can verify that the audits are run by the proper resource.
	 * For example, when testFourResourcesOneDead is run, you should see a
	 * Starting.../Finished... sequence for pdp1, followed by a
	 * Starting.../Finished... sequence for pdp2, followed by a
	 * Starting.../Finished... sequence for pdp4 (pdp3 is skipped as it's
	 * dead/hung), followed by a Starting.../Finished... sequence for pdp1, etc.
	 */
	@Ignore
	@Test
	public void testTwoResources() throws Exception {
		
		logger.info("testTwoResources: Entering");
		
		/*
		 * Start audit for pdp1.
		 */
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName, persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Start audit for pdp2.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit2 = "testPU";
		String resourceName2 = "pdp2";
		IntegrityAudit integrityAudit2 = new IntegrityAudit(resourceName2, persistenceUnit2, properties2);
		integrityAudit2.startAuditThread();
		
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) pdp1 to run audit (15 seconds)
		 * 
		 * 2) Logic to detect that pdp1 is stale and designate pdp2 (30 seconds)
		 * 
		 * 3) pdp2 to run audit (15 seconds)
		 * 
		 * 4) Logic to detect that pdp2 is stale and designate pdp1 (30 seconds)
		 * 
		 * 5) pdp1 to run audit (15 seconds)
		 */
		Thread.sleep(120000);
		
		logger.info("testTwoResources: Stopping audit threads");
		integrityAudit.stopAuditThread();
		integrityAudit2.stopAuditThread();
		
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp1", "pdp2", "pdp1"));
		ArrayList<String> delegates = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			/* parse strLine to obtain what you want */
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				
				String rName = strLine.substring(startIndex, endIndex);
				
				delegates.add(rName);
			}
		}
				
		for (String delegate: delegates) {
			logger.info("testTwoResources: delegate: " + delegate);
		}
		
		fstream.close();
				
		assertTrue(expectedResult.equals(delegates));
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 3);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
		assertTrue("delegate 2 is " + expectedResult.get(2), expectedResult.get(2).equals(delegates.get(2)));
	
		logger.info("testTwoResources: Exiting");

	}
	
	/*
	 * Tests designation logic when two functioning resources are in play, each
	 * with different PUs. Audits for "testPU" and "integrityAuditPU" should run
	 * simultaneously. Designation should not alternate.
	 * 
	 * Note: console.log must be examined to ascertain whether or not this test
	 * was successful.
	 */
	@Ignore
	@Test
	public void testTwoResourcesDifferentPus() throws Exception {
		
		logger.info("testTwoResourcesDifferentPus: Entering");
		
		/*
		 * Start audit for pdp1.
		 */
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName, persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Start audit for pdp2.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit2 = "integrityAuditPU";
		String resourceName2 = "pdp2";
		IntegrityAudit integrityAudit2 = new IntegrityAudit(resourceName2, persistenceUnit2, properties2);
		integrityAudit2.startAuditThread();
		
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) pdp1 and pdp2 to run audit simultaneously (15 seconds)
		 * 
		 * 2) Logic to detect that no other node is available for designation for either pdp1 or pdp2 (60 seconds)
		 * 
		 * 3) pdp1 and pdp2 to again run audit simultaneously (15 seconds)
		 * 
		 * NOTE: Based on the above, you would think a 100000ms sleep would be appropriate,
		 * but for some reason, when all tests are run this test errors.   
		 */
		logger.info("testTwoResourcesDifferentPus: Sleeping 80 seconds");
		Thread.sleep(100000);
		
		logger.info("testTwoResourcesDifferentPus: Stopping audit threads");
		integrityAudit.stopAuditThread();
		integrityAudit2.stopAuditThread();
		
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp1", "pdp2", "pdp1", "pdp2"));
		ArrayList<String> delegates = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			/* parse strLine to obtain what you want */
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				
				String rName = strLine.substring(startIndex, endIndex);
				
				delegates.add(rName);
			}
		}
				
		for (String delegate: delegates) {
			logger.info("testTwoResourcesDifferentPus: delegate: " + delegate);
		}
		
		fstream.close();
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 4);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
		assertTrue("delegate 2 is " + expectedResult.get(2), expectedResult.get(2).equals(delegates.get(2)));
		assertTrue("delegate 3 is " + expectedResult.get(3), expectedResult.get(3).equals(delegates.get(3)));
		
		assertTrue(expectedResult.equals(delegates));
				
		logger.info("testTwoResourcesDifferentPus: Exiting");

	}

	
	/*
	 * Tests designation logic when two resources are in play but one of them is
	 * dead/hung. Designation should move to second resource but then get
	 * restored back to original resource when it's discovered that second
	 * resource is dead.
	 * 
	 * Note: console.log must be examined to ascertain whether or not this test
	 * was successful.
	 */
	@Ignore
	@Test
	public void testTwoResourcesOneDead() throws Exception {
		
		logger.info("testTwoResourcesOneDead: Entering");
		
		/*
		 * Start audit for pdp1.
		 */
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName, persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Populate DB for pdp2, which will simulate it having registered but then having died.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit2 = "testPU";
		String resourceName2 = "pdp2";
		new DbDAO(resourceName2, persistenceUnit2, properties2);
		
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) pdp1 to run audit (15 seconds)
		 * 
		 * 2) Logic to detect that other node, pdp2, is not available for designation (60 seconds)
		 * 
		 * 3) pdp1 to run audit again (15 seconds)
		 */
		logger.info("testTwoResourcesOneDead: Sleeping 100 seconds");
		Thread.sleep(100000);
		
		logger.info("testTwoResourcesOneDead: Stopping audit thread");
		integrityAudit.stopAuditThread();
		
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp1", "pdp1"));
		ArrayList<String> delegates = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			/* parse strLine to obtain what you want */
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				
				String rName = strLine.substring(startIndex, endIndex);
				
				delegates.add(rName);
			}
		}
				
		for (String delegate: delegates) {
			logger.info("testTwoResourcesOneDead: delegate: " + delegate);
		}
		
		fstream.close();
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 2);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
				
		logger.info("testTwoResourcesOneDead: Exiting");

	}
	
	
	/*
	 * Tests designation logic when three functioning resources are in play.  Designation should
	 * round robin among resources.
	 * 
	 * Note: console.log must be examined to ascertain whether or not this test was successful.
	 */
	@Ignore
	@Test
	public void testThreeResources() throws Exception {
		
		logger.info("testThreeResources: Entering");
		
		/*
		 * Start audit for pdp1.
		 */
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName, persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Start audit for pdp2.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit2 = "testPU";
		String resourceName2 = "pdp2";
		IntegrityAudit integrityAudit2 = new IntegrityAudit(resourceName2, persistenceUnit2, properties2);
		integrityAudit2.startAuditThread();
		
		/*
		 * Start audit for pdp3.
		 */
		Properties properties3 = new Properties();
		properties3.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties3.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties3.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties3.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties3.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties3.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties3.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit3 = "testPU";
		String resourceName3 = "pdp3";
		IntegrityAudit integrityAudit3 = new IntegrityAudit(resourceName3, persistenceUnit3, properties3);
		integrityAudit3.startAuditThread();
		
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) pdp1 to run audit (15 seconds)
		 * 
		 * 2) Logic to detect that pdp1 is stale and designate pdp2 (30 seconds)
		 * 
		 * 3) pdp2 to run audit (15 seconds)
		 * 
		 * 4) Logic to detect that pdp2 is stale and designate pdp3 (30 seconds)
		 * 
		 * 5) pdp3 to run audit (15 seconds)
		 * 
		 * 6) Logic to detect that pdp3 is stale and designate pdp1 (30 seconds)
		 * 
		 * 7) pdp1 to run audit (15 seconds)
		 */
		logger.info("testThreeResources: Sleeping 160 seconds");
		Thread.sleep(160000);
		
		logger.info("testThreeResources: Stopping threads");
		integrityAudit.stopAuditThread();
		integrityAudit2.stopAuditThread();
		integrityAudit3.stopAuditThread();
		
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp1", "pdp2", "pdp3", "pdp1"));
		ArrayList<String> delegates = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			/* parse strLine to obtain what you want */
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				
				String rName = strLine.substring(startIndex, endIndex);
				
				delegates.add(rName);
			}
		}
		
		for (String delegate: delegates) {
			logger.info("testThreeResources: delegate: " + delegate);
		}
		
		fstream.close();
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 3);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
		assertTrue("delegate 2 is " + expectedResult.get(2), expectedResult.get(2).equals(delegates.get(2)));
		assertTrue("delegate 3 is " + expectedResult.get(3), expectedResult.get(3).equals(delegates.get(3)));
				
		logger.info("testThreeResources: Exiting");

	}
	
	/*
	 * Tests designation logic when four functioning resources are in play, two
	 * with one PU, two with another. Audits for "testPU" and "integrityAuditPU" should run
	 * simultaneously. Designation should alternate between resources for each of the two
	 * persistence units.
	 * 
	 * Note: console.log must be examined to ascertain whether or not this test
	 * was successful.
	 */
	@Ignore
	@Test
	public void testFourResourcesDifferentPus() throws Exception {
		
		logger.info("testFourResourcesDifferentPus: Entering");
		
		/*
		 * Start audit for pdp1, testPU.
		 */
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName, persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Start audit for pdp2, integrityAuditPU.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit2 = "integrityAuditPU";
		String resourceName2 = "pdp2";
		IntegrityAudit integrityAudit2 = new IntegrityAudit(resourceName2, persistenceUnit2, properties2);
		integrityAudit2.startAuditThread();
		
		/*
		 * Start audit for pdp3, testPU.
		 */
		Properties properties3 = new Properties();
		properties3.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties3.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties3.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties3.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties3.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties3.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties3.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit3 = "testPU";
		String resourceName3 = "pdp3";
		IntegrityAudit integrityAudit3 = new IntegrityAudit(resourceName3, persistenceUnit3, properties3);
		integrityAudit3.startAuditThread();
		
		/*
		 * Start audit for pdp4, integrityAuditPU.
		 */
		Properties properties4 = new Properties();
		properties4.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties4.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties4.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties4.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties4.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties4.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties4.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit4 = "integrityAuditPU";
		String resourceName4 = "pdp4";
		IntegrityAudit integrityAudit4 = new IntegrityAudit(resourceName4, persistenceUnit4, properties4);
		integrityAudit4.startAuditThread();
		
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) pdp1 and pdp2 to run audit simultaneously (15 seconds)
		 * 
		 * 2) Logic to detect that pdp1 and pdp2 are stale and designate pdp3 (one's counterpart) and pdp4 (two's counterpart) (30 seconds)
		 * 
		 * 3) pdp3 and pdp4 to run audit simultaneously (15 seconds)
		 * 
		 * 4) Logic to detect that pdp3 and pdp4 are stale and designate pdp1 (three's counterpart) and pdp2 (four's counterpart) (30 seconds)
		 * 
		 * 5) pdp1 and pdp2 to run audit simultaneously (15 seconds)
		 */
		logger.info("testFourResourcesDifferentPus: Sleeping 120 seconds");
		Thread.sleep(120000);
		
		logger.info("testFourResourcesDifferentPus: Stopping threads");
		integrityAudit.stopAuditThread();
		integrityAudit2.stopAuditThread();
		integrityAudit3.stopAuditThread();
		integrityAudit4.stopAuditThread();
		
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp1", "pdp2", "pdp3", "pdp4", "pdp1", "pdp2"));
		ArrayList<String> delegates = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			/* parse strLine to obtain what you want */
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				
				String rName = strLine.substring(startIndex, endIndex);
				
				delegates.add(rName);
			}
		}
				
		for (String delegate: delegates) {
			logger.info("testFourResourcesDifferentPus: delegate: " + delegate);
		}
		
		fstream.close();
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 6);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
		assertTrue("delegate 2 is " + expectedResult.get(2), expectedResult.get(2).equals(delegates.get(2)));
		assertTrue("delegate 3 is " + expectedResult.get(3), expectedResult.get(3).equals(delegates.get(3)));
		assertTrue("delegate 4 is " + expectedResult.get(4), expectedResult.get(4).equals(delegates.get(4)));
		assertTrue("delegate 5 is " + expectedResult.get(5), expectedResult.get(5).equals(delegates.get(5)));
				
		logger.info("testFourResourcesDifferentPus: Exiting");

	}
	
	/*
	 * Tests designation logic when four resources are in play but one is not
	 * functioning. Designation should round robin among functioning resources
	 * only.
	 * 
	 * Note: console.log must be examined to ascertain whether or not this test
	 * was successful.
	 */
	@Ignore
	@Test
	public void testFourResourcesOneDead() throws Exception {
		
		logger.info("testFourResourcesOneDead: Entering");
		
		/*
		 * Start audit for pdp1.
		 */
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName, persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Start audit for pdp2.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit2 = "testPU";
		String resourceName2 = "pdp2";
		IntegrityAudit integrityAudit2 = new IntegrityAudit(resourceName2, persistenceUnit2, properties2);
		integrityAudit2.startAuditThread();
		
		/*
		 * Populate DB for pdp3, which will simulate it having registered but then having died.
		 */
		Properties properties3 = new Properties();
		properties3.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties3.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties3.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties3.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties3.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties3.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties3.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit3 = "testPU";
		String resourceName3 = "pdp3";
		new DbDAO(resourceName3, persistenceUnit3, properties3);
		
		/*
		 * Start audit for pdp4.
		 */
		Properties properties4 = new Properties();
		properties4.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties4.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties4.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties4.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties4.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties4.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties4.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit4 = "testPU";
		String resourceName4 = "pdp4";
		IntegrityAudit integrityAudit4 = new IntegrityAudit(resourceName4, persistenceUnit4, properties4);
		integrityAudit4.startAuditThread();
		
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) pdp1 to run audit (15 seconds)
		 * 
		 * 2) Logic to detect that pdp1 is stale and designate pdp2 (30 seconds)
		 * 
		 * 3) pdp2 to run audit (15 seconds)
		 * 
		 * 4) Logic to detect that pdp2 is stale and designate pdp4 (30 seconds)
		 * 
		 * 5) pdp4 to run audit (15 seconds)
		 * 
		 * 6) Logic to detect that pdp4 is stale and designate pdp1 (30 seconds)
		 * 
		 * 7) pdp1 to run audit (15 seconds)
		 * 
		 * 8) Logic to detect that pdp1 is stale and designate pdp2 (30 seconds)
		 * 
		 * 7) pdp2 to run audit (15 seconds)
		 */
		logger.info("testFourResourcesOneDead: Sleeping 210 seconds");
		Thread.sleep(210000);
		
		logger.info("testFourResourcesOneDead: Stopping threads");		
		integrityAudit.stopAuditThread();
		integrityAudit2.stopAuditThread();
		integrityAudit4.stopAuditThread();
		
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp1", "pdp2", "pdp4", "pdp1", "pdp2", "pdp4"));
		ArrayList<String> delegates = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			/* parse strLine to obtain what you want */
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				
				String rName = strLine.substring(startIndex, endIndex);

				delegates.add(rName);
			}
		}
		
		for (String delegate : delegates) {
			logger.info("testFourResourcesOneDead: delegate: " + delegate);
		}

		fstream.close();
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 6);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
		assertTrue("delegate 2 is " + expectedResult.get(2), expectedResult.get(2).equals(delegates.get(2)));
		assertTrue("delegate 3 is " + expectedResult.get(3), expectedResult.get(3).equals(delegates.get(3)));
		assertTrue("delegate 4 is " + expectedResult.get(4), expectedResult.get(4).equals(delegates.get(4)));
		assertTrue("delegate 5 is " + expectedResult.get(5), expectedResult.get(5).equals(delegates.get(5)));
				
		logger.info("testFourResourcesOneDead: Exiting");

	}
	
	/*
	 * Tests designation logic when four resources are in play but only one is
	 * functioning. Designation should remain with sole functioning resource.
	 * 
	 * Note: console.log must be examined to ascertain whether or not this test
	 * was successful.
	 */
	@Ignore
	@Test
	public void testFourResourcesThreeDead() throws Exception {
				
		logger.info("testFourResourcesThreeDead: Entering");
		
		/*
		 * Populate DB for pdp1, which will simulate it having registered but then having died.
		 */
		new DbDAO(resourceName, persistenceUnit, properties);

		
		/*
		 * Populate DB for pdp2, which will simulate it having registered but then having died.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit2 = "testPU";
		String resourceName2 = "pdp2";
		new DbDAO(resourceName2, persistenceUnit2, properties2);

		/*
		 * Start audit for pdp3.
		 */
		Properties properties3 = new Properties();
		properties3.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties3.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties3.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties3.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties3.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties3.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties3.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit3 = "testPU";
		String resourceName3 = "pdp3";
		IntegrityAudit integrityAudit3 = new IntegrityAudit(resourceName3, persistenceUnit3, properties3);
		integrityAudit3.startAuditThread();
		
		/*
		 * Populate DB for pdp4, which will simulate it having registered but then having died.
		 */
		Properties properties4 = new Properties();
		properties4.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties4.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties4.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties4.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties4.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties4.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties4.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit4 = "testPU";
		String resourceName4 = "pdp4";
		new DbDAO(resourceName4, persistenceUnit4, properties4);
				
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) pdp3 to discover that all other designation candidates are stale (30 seconds)
		 * 
		 * 1) pdp3 to run audit (15 seconds)
		 * 
		 * 2) Logic to detect that no other nodes are available for designation (60 seconds)
		 * 
		 * 3) pdp3 to run audit again (15 seconds)
		 */
		logger.info("testFourResourcesThreeDead: Sleeping 130 seconds");
		Thread.sleep(130000);
		
		logger.info("testFourResourcesThreeDead: Stopping thread");
		integrityAudit3.stopAuditThread();
		
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp3", "pdp3"));
		ArrayList<String> delegates = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			/* parse strLine to obtain what you want */
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				
				String rName = strLine.substring(startIndex, endIndex);

				delegates.add(rName);
			}
		}
		
		for (String delegate : delegates) {
			logger.info("testFourResourcesThreeDead: delegate: " + delegate);
		}

		fstream.close();
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 2);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
		
		logger.info("testFourResourcesThreeDead: Exiting");

	}


	/*
	 * Tests designation logic when the designated node dies and is no longer
	 * current
	 * 
	 * Note: console.log must be examined to ascertain whether or not this test
	 * was successful.
	 */
	@Ignore
	@Test
	public void testDesignatedNodeDead() throws Exception {
		logger.info("testDesignatedNodeDead: Entering");
		
		/*
		 * Instantiate audit object for pdp1.
		 */
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName, persistenceUnit, properties);
		
		/*
		 * Start audit for pdp2.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit2 = "testPU";
		String resourceName2 = "pdp2";
		IntegrityAudit integrityAudit2 = new IntegrityAudit(resourceName2, persistenceUnit2, properties2);

		/*
		 * Instantiate audit object for pdp3.
		 */
		Properties properties3 = new Properties();
		properties3.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties3.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties3.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties3.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties3.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties3.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties3.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "5");
		String persistenceUnit3 = "testPU";
		String resourceName3 = "pdp3";
		IntegrityAudit integrityAudit3 = new IntegrityAudit(resourceName3, persistenceUnit3, properties3);
				
		// Start audit on pdp1
		logger.info("testDesignatedNodeDead: Start audit on pdp1");
		integrityAudit.startAuditThread();			

		// Start the auditing threads on other nodes.
		logger.info("testDesignatedNodeDead: Start audit on pdp2");
		integrityAudit2.startAuditThread();	
		logger.info("testDesignatedNodeDead: Start audit on pdp3");
		integrityAudit3.startAuditThread();		
		

		// Kill audit on pdp1
		logger.info("testDesignatedNodeDead: Kill audit on pdp1");
		integrityAudit.stopAuditThread();
		
		// Sleep long enough for pdp1 to get stale and pdp2 to take over
		logger.info("testDesignatedNodeDead: Sleep long enough for pdp1 to get stale and pdp2 to take over");
		Thread.sleep(AuditThread.AUDIT_COMPLETION_INTERVAL + FUDGE_FACTOR); 
		
		// Start audit thread on pdp1 again.
		logger.info("testDesignatedNodeDead: Start audit thread on pdp1 again.");
		integrityAudit.startAuditThread(); 
		
		// Sleep long enough for pdp2 to complete its audit and get stale, at
		// which point pdp3 should take over
		logger.info("testDesignatedNodeDead: Sleep long enough for pdp2 to complete its audit and get stale, at which point pdp3 should take over");
		Thread.sleep((AuditThread.AUDIT_SIMULATION_SLEEP_INTERVAL * AuditThread.AUDIT_SIMULATION_ITERATIONS)
				+ AuditThread.AUDIT_COMPLETION_INTERVAL + FUDGE_FACTOR);
		
		// Kill audit on pdp3
		logger.info("testDesignatedNodeDead: Killing audit on pdp3");
		integrityAudit3.stopAuditThread();
		
		// Sleep long enough for pdp3 to get stale and pdp1 to take over
		logger.info("testDesignatedNodeDead: Sleep long enough for pdp3 to get stale and pdp1 to take over");
		Thread.sleep(AuditThread.AUDIT_COMPLETION_INTERVAL + FUDGE_FACTOR); 
				
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp1", "pdp2", "pdp3", "pdp1"));
		ArrayList<String> delegates = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			/* parse strLine to obtain what you want */
			if (strLine.contains("Starting audit simulation for resourceName=")) {
				startIndex = strLine.indexOf("resourceName=") + 13;
				endIndex = strLine.indexOf(",");
				
				String rName = strLine.substring(startIndex, endIndex);
				
				delegates.add(rName);
			}
		}
		fstream.close();
		
		// Stop remaining threads.
		logger.info("testDesignatedNodeDead: Stopping remaining threads");
		integrityAudit.stopAuditThread();
		integrityAudit2.stopAuditThread();
		
		for (String delegate: delegates) {
			logger.info("testDesignatedNodeDead: delegate: " + delegate);
		}
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 4);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
		assertTrue("delegate 2 is " + expectedResult.get(2), expectedResult.get(2).equals(delegates.get(2)));
		assertTrue("delegate 3 is " + expectedResult.get(3), expectedResult.get(3).equals(delegates.get(3)));
		
		logger.info("testDesignatedNodeDead: Exiting");
	}
}
