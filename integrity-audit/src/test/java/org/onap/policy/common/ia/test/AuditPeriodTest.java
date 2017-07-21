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
import org.onap.policy.common.ia.IntegrityAudit;
import org.onap.policy.common.ia.IntegrityAuditProperties;
import org.onap.policy.common.logging.flexlogger.FlexLogger; 
import org.onap.policy.common.logging.flexlogger.Logger;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class AuditPeriodTest {
	
	private static Logger logger = FlexLogger.getLogger(AuditPeriodTest.class);
	
	private static final String AUDIT_PERIOD_TEST_LOG = "./testingLogs/common-modules/integrity-audit/debug.log";
		
	private static String persistenceUnit;
	private static Properties properties;
	private static String resourceName;
	
	@Before
	public void setUp() throws Exception {

		
		System.out.println("setUp: Clearing " + AUDIT_PERIOD_TEST_LOG);
		FileOutputStream fstream = new FileOutputStream(AUDIT_PERIOD_TEST_LOG);
		fstream.close();

		logger.info("setUp: Entering");

		IntegrityAudit.isUnitTesting = true;
		
		properties = new Properties();
		properties.put(IntegrityAuditProperties.DB_DRIVER, IntegrityAuditProperties.DEFAULT_DB_DRIVER);
		properties.put(IntegrityAuditProperties.DB_URL, IntegrityAuditProperties.DEFAULT_DB_URL);
		properties.put(IntegrityAuditProperties.DB_USER, IntegrityAuditProperties.DEFAULT_DB_USER);
		properties.put(IntegrityAuditProperties.DB_PWD, IntegrityAuditProperties.DEFAULT_DB_PWD);
		properties.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
				
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
	 * Verifies (via log parsing) that when a negative audit period is
	 * specified, the audit is suppressed.
	 */
	@Ignore
	@Test
	public void testNegativeAuditPeriod() throws Exception {
		
		logger.info("testNegativeAuditPeriod: Entering");
		
		properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "-1");

		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName, persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Sleep long enough to allow
		 * 
		 * 1) audit to immediately terminate.
		 */
		Thread.sleep(1000);
		
		logger.info("testNegativeAuditPeriod: Stopping audit thread (should be a no-op!)");
		integrityAudit.stopAuditThread();

		FileInputStream fstream = new FileInputStream(AUDIT_PERIOD_TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("-1"));
		ArrayList<String> delegates = new ArrayList<String>();
		while ((strLine = br.readLine()) != null)   {
			/* parse strLine to obtain what you want */
			if (strLine.contains("Suppressing integrity audit, integrityAuditPeriodSeconds=")) {
				startIndex = strLine.indexOf("integrityAuditPeriodSeconds=") + 28;
				
				String integrityAuditPeriodSeconds = strLine.substring(startIndex);
				
				delegates.add(integrityAuditPeriodSeconds);
			}
		}
				
		for (String delegate: delegates) {
			logger.info("testNegativeAuditPeriod: delegate: " + delegate);
		}
		
		fstream.close();
				
		assertTrue(expectedResult.equals(delegates));
		
		logger.info("testNegativeAuditPeriod: Exiting");

	}
	
	/*
	 * Verifies (via log parsing) that when an audit period of zero is
	 * specified, the audit runs continuously, generating a number of
	 * sleep/wake sequences in a short period of time (e.g. 100ms).
	 */
	@Ignore
	@Test
	public void testZeroAuditPeriod() throws Exception {

		logger.info("testZeroAuditPeriod: Entering");

		properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "0");

		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName,
				persistenceUnit, properties);
		integrityAudit.startAuditThread();

		/*
		 * Sleep long enough to allow
		 * 
		 * 1) audit to generate a bunch of sleep wake sequences.
		 * 
		 * Note:
		 * 
		 * (AuditThread.AUDIT_SIMULATION_SLEEP_INTERVAL *
		 * AuditThread.AUDIT_SIMULATION_ITERATIONS) is the time it takes for the
		 * audit simulation to run.
		 * 
		 * (integrityAudit.getIntegrityAuditPeriodSeconds() should return a
		 * value of zero; i.e. audit should not sleep at all between iterations
		 * 
		 * "100"ms is the time we allow the audit to cycle continuously
		 */
		long sleepMillis = (AuditThread.AUDIT_SIMULATION_SLEEP_INTERVAL * AuditThread.AUDIT_SIMULATION_ITERATIONS)
				+ (integrityAudit.getIntegrityAuditPeriodSeconds() * 1000)
				+ 100;
		logger.info("testZeroAuditPeriod: Sleeping " + sleepMillis + "ms before stopping auditThread");
		Thread.sleep(sleepMillis);
		
		logger.info("testZeroAuditPeriod: Stopping audit thread");
		integrityAudit.stopAuditThread();

		/*
		 * Before audit completion message upon awaking from sleep is upper case "Awaking".  After audit
		 * completion, all awakings are lower case "awaking".
		 */
		logger.info("testZeroAuditPeriod: Parsing " + AUDIT_PERIOD_TEST_LOG + " for 'awaking'");
		FileInputStream fstream = new FileInputStream(AUDIT_PERIOD_TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine = "";
		int awakings = 0;
		int lines = 0;
		while ((strLine = br.readLine()) != null) {
			if (strLine.contains("Awaking from 0ms sleep")) {
				fail("Audit appears not to have run!?  Got '" + strLine + "'");
			} else {
				if (strLine.contains("awaking from 0ms sleep")) {
					awakings++;
				}
			}
			lines++;
		}
		logger.info("testZeroAuditPeriod: Done parsing "
				+ AUDIT_PERIOD_TEST_LOG + " for 'awaking'; lines parsed="
				+ lines + ", closing stream");
		fstream.close();

		/*
		 * We should get at least 10 sleep/wake sequences.
		 */
		assertTrue("Only " + awakings + " awakings", awakings > 10);
		assertTrue(integrityAudit.getIntegrityAuditPeriodSeconds() == 0);

		logger.info("testZeroAuditPeriod: Exiting, awakings="
				+ awakings + ", integrityAuditPeriodSeconds="
				+ integrityAudit.getIntegrityAuditPeriodSeconds());

	}
	
	/*
	 * Verifies (via log parsing) that when an audit period of five minutes is
	 * specified, there is a five minute interval between the audits run
	 * on each of three different entities.
	 */
	@Ignore
	@Test
	public void testFiveMinuteAuditPeriod() throws Exception {

		logger.info("testFiveMinuteAuditPeriod: Entering");

		properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "300");

		/*
		 * Start audit for pdp1.
		 */
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName,
				persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Start audit for pdp2.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, IntegrityAuditProperties.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, IntegrityAuditProperties.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, IntegrityAuditProperties.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, IntegrityAuditProperties.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "300");
		String persistenceUnit2 = "testPU";
		String resourceName2 = "pdp2";
		IntegrityAudit integrityAudit2 = new IntegrityAudit(resourceName2, persistenceUnit2, properties2);
		integrityAudit2.startAuditThread();
		
		/*
		 * Start audit for pdp3.
		 */
		Properties properties3 = new Properties();
		properties3.put(IntegrityAuditProperties.DB_DRIVER, IntegrityAuditProperties.DEFAULT_DB_DRIVER);
		properties3.put(IntegrityAuditProperties.DB_URL, IntegrityAuditProperties.DEFAULT_DB_URL);
		properties3.put(IntegrityAuditProperties.DB_USER, IntegrityAuditProperties.DEFAULT_DB_USER);
		properties3.put(IntegrityAuditProperties.DB_PWD, IntegrityAuditProperties.DEFAULT_DB_PWD);
		properties3.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties3.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties3.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "300");
		String persistenceUnit3 = "testPU";
		String resourceName3 = "pdp3";
		IntegrityAudit integrityAudit3 = new IntegrityAudit(resourceName3, persistenceUnit3, properties3);
		integrityAudit3.startAuditThread();


		/*
		 * 1) All three audit run once.  This should take approximately 105 seconds, as follows:
		 * 
		 * T0: pdp1 runs audit (15 seconds), then sleeps for five minutes (300 seconds)
		 * pdp2 recognizes that pdp1 is stale (30 seconds) and runs its audit (15 seconds)
		 * pdp3 recognizes that pdp2 is stale (30 seconds) and runs its audit (15 seconds)
		 * 
		 * 2) Five minutes after T0, at T1, pdp1 wakes up and the above sequence begins again,
		 * which should take another 115 seconds:
		 * 
		 * T1: pdp1 runs audit (15 seconds), then sleeps for two minutes (300 seconds)
		 * pdp2 wakes up, resets auditCompleted and sleeps (5 seconds), recognizes that pdp1 is stale (30 seconds) and runs its audit (15 seconds)
		 * pdp3 wakes up, resets auditCompleted and sleeps (5 seconds), recognizes that pdp2 is stale (30 seconds) and runs its audit (15 seconds)
		 * 
		 * So, the entire sequence should take 15 + 300 + 115 = 430 seconds
		 * Adding a fudge factor, we sleep for 450 seconds
		 */
		Thread.sleep(450000);
		
		
		logger.info("testFiveMinuteAuditPeriod: Stopping all three audit threads");
		integrityAudit.stopAuditThread();

		integrityAudit.stopAuditThread();
		integrityAudit2.stopAuditThread();
		integrityAudit3.stopAuditThread();

		FileInputStream fstream = new FileInputStream(AUDIT_PERIOD_TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp1", "pdp2", "pdp3", "pdp1", "pdp2", "pdp3"));
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
			logger.info("testFiveMinuteAuditPeriod: delegate: " + delegate);
		}
		
		fstream.close();
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 6);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
		assertTrue("delegate 2 is " + expectedResult.get(2), expectedResult.get(2).equals(delegates.get(2)));
		assertTrue("delegate 3 is " + expectedResult.get(3), expectedResult.get(3).equals(delegates.get(3)));
		assertTrue("delegate 4 is " + expectedResult.get(4), expectedResult.get(4).equals(delegates.get(4)));
		assertTrue("delegate 5 is " + expectedResult.get(5), expectedResult.get(5).equals(delegates.get(5)));
				
		logger.info("testFiveMinuteAuditPeriod: Exiting");
	}
	
	/*
	 * Verifies (via log parsing) that when an audit period of 20 seconds is
	 * specified, there is a 20 second interval between the audits run
	 * on each of three different entities.
	 */
	@Ignore
	@Test
	public void testTwentySecondAuditPeriod() throws Exception {

		logger.info("testTwentySecondAuditPeriod: Entering");

		properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "20");

		/*
		 * Start audit for pdp1.
		 */
		IntegrityAudit integrityAudit = new IntegrityAudit(resourceName,
				persistenceUnit, properties);
		integrityAudit.startAuditThread();
		
		/*
		 * Start audit for pdp2.
		 */
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, IntegrityAuditProperties.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, IntegrityAuditProperties.DEFAULT_DB_URL);
		properties2.put(IntegrityAuditProperties.DB_USER, IntegrityAuditProperties.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, IntegrityAuditProperties.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties2.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "20");
		String persistenceUnit2 = "testPU";
		String resourceName2 = "pdp2";
		IntegrityAudit integrityAudit2 = new IntegrityAudit(resourceName2, persistenceUnit2, properties2);
		integrityAudit2.startAuditThread();
		
		/*
		 * Start audit for pdp3.
		 */
		Properties properties3 = new Properties();
		properties3.put(IntegrityAuditProperties.DB_DRIVER, IntegrityAuditProperties.DEFAULT_DB_DRIVER);
		properties3.put(IntegrityAuditProperties.DB_URL, IntegrityAuditProperties.DEFAULT_DB_URL);
		properties3.put(IntegrityAuditProperties.DB_USER, IntegrityAuditProperties.DEFAULT_DB_USER);
		properties3.put(IntegrityAuditProperties.DB_PWD, IntegrityAuditProperties.DEFAULT_DB_PWD);
		properties3.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties3.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		properties3.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, "20");
		String persistenceUnit3 = "testPU";
		String resourceName3 = "pdp3";
		IntegrityAudit integrityAudit3 = new IntegrityAudit(resourceName3, persistenceUnit3, properties3);
		integrityAudit3.startAuditThread();


		/*
		 * 1) All three audit run once.
		 * 
		 * pdp1 runs audit (15 seconds), then goes into 20 second sleep cycles
		 * pdp2 recognizes that pdp1 is stale (30 seconds), runs its audit (15 seconds), then goes into 20 second sleep cycles
		 * pdp3 recognizes that pdp2 is stale (30 seconds), runs its audit (15 seconds), then goes into 20 second sleep cycles 
		 * 
		 * 2) Eventually pdp2 gets stale, pdp1 recognizes this and cycle begins again. 
		 * 
		 * So, we allow 15 + (5 * 45) = 240 seconds plus a fudge factor.
		 * 
		 */
		Thread.sleep(250000);
		
		
		logger.info("testTwentySecondAuditPeriod: Stopping all three audit threads");
		integrityAudit.stopAuditThread();

		integrityAudit.stopAuditThread();
		integrityAudit2.stopAuditThread();
		integrityAudit3.stopAuditThread();

		FileInputStream fstream = new FileInputStream(AUDIT_PERIOD_TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		int endIndex;
		ArrayList<String> expectedResult = new ArrayList<String>(Arrays.asList("pdp1", "pdp2", "pdp3", "pdp1", "pdp2", "pdp3"));
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
			logger.info("testTwentySecondAuditPeriod: delegate: " + delegate);
		}
		
		fstream.close();
		
		assertTrue("delegate count only " + delegates.size(), delegates.size() >= 6);
		assertTrue("delegate 0 is " + expectedResult.get(0), expectedResult.get(0).equals(delegates.get(0)));
		assertTrue("delegate 1 is " + expectedResult.get(1), expectedResult.get(1).equals(delegates.get(1)));
		assertTrue("delegate 2 is " + expectedResult.get(2), expectedResult.get(2).equals(delegates.get(2)));
		assertTrue("delegate 3 is " + expectedResult.get(3), expectedResult.get(3).equals(delegates.get(3)));
		assertTrue("delegate 4 is " + expectedResult.get(4), expectedResult.get(4).equals(delegates.get(4)));
		assertTrue("delegate 5 is " + expectedResult.get(5), expectedResult.get(5).equals(delegates.get(5)));
				
		logger.info("testTwentySecondAuditPeriod: Exiting");
	}
	
}
