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

package org.openecomp.policy.common.ia.test;

import static org.junit.Assert.*;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

import org.openecomp.policy.common.ia.DbAudit;
import org.openecomp.policy.common.ia.DbDAO;
import org.openecomp.policy.common.ia.IntegrityAudit;
import org.openecomp.policy.common.ia.IntegrityAuditProperties;
import org.openecomp.policy.common.ia.jpa.IntegrityAuditEntity;
import org.openecomp.policy.common.ia.test.jpa.IaTestEntity;
import org.openecomp.policy.common.ia.test.jpa.PersonTest;
import org.openecomp.policy.common.logging.flexlogger.FlexLogger; 
import org.openecomp.policy.common.logging.flexlogger.Logger;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class DbAuditCompareEntriesTest {

	private static Logger logger = FlexLogger.getLogger(DbAuditCompareEntriesTest.class);
	private DbDAO dbDAO;
	private static String persistenceUnit;
	private static Properties properties;
	private static String resourceName;
	private String dbDriver;
	private String dbUrl;
	private String dbUser;
	private String dbPwd;
	private String siteName;
	private String nodeType;
	private static final String TEST_LOG = "./testingLogs/common-modules/integrity-audit/debug.log";
	
	@Before
	public void setUp() throws Exception {
		System.out.println("setUp: Clearing IntegrityAudit.log");
		//FileOutputStream fstream = new FileOutputStream("IntegrityAudit.log");
		FileOutputStream fstream = new FileOutputStream(TEST_LOG);
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

		dbDriver = IntegrityAuditProperties.DEFAULT_DB_DRIVER;
		dbUrl = IntegrityAuditProperties.DEFAULT_DB_URL;
		dbUser = IntegrityAuditProperties.DEFAULT_DB_USER;
		dbPwd = IntegrityAuditProperties.DEFAULT_DB_PWD;
		siteName = "SiteA";
		nodeType = "pdp_xacml";
		persistenceUnit = "testPU";
		resourceName = "pdp1";
				
		logger.info("setUp: Exiting");
	}

	/*
	 * Clean up DB after each test.
	 */
	@After
	public void tearDown() throws Exception {
		logger.info("tearDown: Entering");
	
		logger.info("tearDown: Exiting");
	}

	/*
	 * Tests that a comparison between hashsets is successful if
	 * the entries match
	 */
	@Ignore
	@Test
	public void testSuccessfulComparison() throws Exception {
		logger.info("testSuccessfulComparison: Entering");
		
		dbDAO = new DbDAO(resourceName, persistenceUnit, properties);
		DbAudit dbAudit = new DbAudit(dbDAO);
		
		String className = null;
		//There is only one entry IntegrityAuditEntity, but we will check anyway
		HashSet<String> classNameSet = dbDAO.getPersistenceClassNames();
		for(String c : classNameSet){
			if (c.equals("org.openecomp.policy.common.ia.jpa.IntegrityAuditEntity")){
				className = c;
			}
		}
		String resourceName1 = resourceName;
		String resourceName2 = resourceName;
		
		IntegrityAuditEntity entry1 = new IntegrityAuditEntity();
		IntegrityAuditEntity entry2 = new IntegrityAuditEntity();
		Date date = new Date();
		
		/*
		 * Two entries with the same field values
		 */
		entry1.setDesignated(false);
		entry1.setJdbcDriver(dbDriver);
		entry1.setJdbcPassword(dbPwd);
		entry1.setJdbcUrl(dbUrl);
		entry1.setJdbcUser(dbUser);
		entry1.setLastUpdated(date);
		entry1.setNodeType(nodeType);
		entry1.setPersistenceUnit(persistenceUnit);
		entry1.setResourceName(resourceName1);
		entry1.setSite(siteName);
		
		entry2.setDesignated(false);
		entry2.setJdbcDriver(dbDriver);
		entry2.setJdbcPassword(dbPwd);
		entry2.setJdbcUrl(dbUrl);
		entry2.setJdbcUser(dbUser);
		entry2.setLastUpdated(date);
		entry2.setNodeType(nodeType);
		entry2.setPersistenceUnit(persistenceUnit);
		entry2.setResourceName(resourceName2);
		entry2.setSite(siteName);
		
		dbAudit.writeAuditDebugLog(className, resourceName1, resourceName2, entry1, entry2);
		
		HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
		HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();
		
		myEntries.put("pdp1", entry1);
		theirEntries.put("pdp1", entry2);
				
		HashSet<Object> result = dbAudit.compareEntries(myEntries, theirEntries);
		
		/*
		 * Assert that there are no mismatches returned
		 */
		assertTrue(result.isEmpty());
		
		logger.info("testSuccessfulComparison: Exit");
	}

	/*
	 * Tests that an error is detected if an entry in one hashset doesn't
	 * match the other
	 */
	@Ignore
	@Test
	public void testComparisonError() throws Exception {
		logger.info("testComparisonError: Entering");
		
		dbDAO = new DbDAO(resourceName, persistenceUnit, properties);
		DbAudit dbAudit = new DbAudit(dbDAO);
		
		String resourceName1 = resourceName;
		String resourceName2 = resourceName;
		
		IntegrityAuditEntity entry1 = new IntegrityAuditEntity();
		IntegrityAuditEntity entry2 = new IntegrityAuditEntity();
		Date date = new Date();
		
		/*
		 * Create two entries with different designated values
		 */
		entry1.setDesignated(false);
		entry1.setJdbcDriver(dbDriver);
		entry1.setJdbcPassword(dbPwd);
		entry1.setJdbcUrl(dbUrl);
		entry1.setJdbcUser(dbUser);
		entry1.setLastUpdated(date);
		entry1.setNodeType(nodeType);
		entry1.setPersistenceUnit(persistenceUnit);
		entry1.setResourceName(resourceName1);
		entry1.setSite(siteName);
		
		entry2.setDesignated(true);
		entry2.setJdbcDriver(dbDriver);
		entry2.setJdbcPassword(dbPwd);
		entry2.setJdbcUrl(dbUrl);
		entry2.setJdbcUser(dbUser);
		entry2.setLastUpdated(date);
		entry2.setNodeType(nodeType);
		entry2.setPersistenceUnit(persistenceUnit);
		entry2.setResourceName(resourceName2);
		entry2.setSite(siteName);
				
		HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
		HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();
		
		myEntries.put("pdp1", entry1);
		theirEntries.put("pdp1", entry2);
		
		HashSet<Object> result = dbAudit.compareEntries(myEntries, theirEntries);
		
		/*
		 * Assert that there was one mismatch
		 */
		assertEquals(1, result.size());
		
		logger.info("testComparisonError: Exit");
	}
	
	/*
	 * Tests that a mismatch/miss entry is detected if there are missing entries in 
	 * one or both of the hashsets
	 */
	@Ignore
	@Test
	public void testCompareMissingEntries() throws Exception {
		logger.info("testCompareMissingEntries: Entering");
	
		dbDAO = new DbDAO(resourceName, persistenceUnit, properties);
		DbAudit dbAudit = new DbAudit(dbDAO);
		
		String resourceName1 = resourceName;
		String resourceName2 = resourceName;
		
		IntegrityAuditEntity entry1 = new IntegrityAuditEntity();
		IntegrityAuditEntity entry2 = new IntegrityAuditEntity();
		IntegrityAuditEntity entry3 = new IntegrityAuditEntity();
		IntegrityAuditEntity entry4 = new IntegrityAuditEntity();
		
		Date date = new Date();
		
		/*
		 * 4 entries, one mismatch, two miss entries
		 */
		entry1.setDesignated(false);
		entry1.setJdbcDriver(dbDriver);
		entry1.setJdbcPassword(dbPwd);
		entry1.setJdbcUrl(dbUrl);
		entry1.setJdbcUser(dbUser);
		entry1.setLastUpdated(date);
		entry1.setNodeType(nodeType);
		entry1.setPersistenceUnit(persistenceUnit);
		entry1.setResourceName(resourceName1);
		entry1.setSite(siteName);
		
		entry2.setDesignated(true);
		entry2.setJdbcDriver(dbDriver);
		entry2.setJdbcPassword(dbPwd);
		entry2.setJdbcUrl(dbUrl);
		entry2.setJdbcUser(dbUser);
		entry2.setLastUpdated(date);
		entry2.setNodeType(nodeType);
		entry2.setPersistenceUnit(persistenceUnit);
		entry2.setResourceName(resourceName2);
		entry2.setSite(siteName);
		
		entry3.setDesignated(false);
		entry3.setJdbcDriver(dbDriver);
		entry3.setJdbcPassword(dbPwd);
		entry3.setJdbcUrl(dbUrl);
		entry3.setJdbcUser(dbUser);
		entry3.setLastUpdated(date);
		entry3.setNodeType(nodeType);
		entry3.setPersistenceUnit(persistenceUnit);
		entry3.setResourceName(resourceName2);
		entry3.setSite("SiteB");
		
		entry4.setDesignated(false);
		entry4.setJdbcDriver(dbDriver);
		entry4.setJdbcPassword(dbPwd);
		entry4.setJdbcUrl(dbUrl);
		entry4.setJdbcUser(dbUser);
		entry4.setLastUpdated(date);
		entry4.setNodeType(nodeType);
		entry4.setPersistenceUnit(persistenceUnit);
		entry4.setResourceName(resourceName2);
		entry4.setSite("SiteB");

		HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
		HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();
		
		myEntries.put("0", entry1);
		myEntries.put("1", entry3);
		theirEntries.put("0", entry2);
		theirEntries.put("2", entry4);
		
		HashSet<Object> mismatchResult = dbAudit.compareEntries(myEntries, theirEntries);
		
		/*
		 * Assert 3 mismatches/missing entries were found
		 */
		assertEquals(3, mismatchResult.size());
		
		logger.info("testCompareMissingEntries: Exit");
	}
	
	/*
	 * Tests that comparison algorithm works for each entity in the hashsets 
	 */
	@Ignore
	@Test
	public void testCompareAllHashEntities() throws Exception {
		logger.info("testCompareAllHashEntities: Entering");
		
		dbDAO = new DbDAO(resourceName, persistenceUnit, properties);
		DbAudit dbAudit = new DbAudit(dbDAO);
		
		HashSet<String> classNameSet = dbDAO.getPersistenceClassNames();
		HashSet<Object> mismatchResult = new HashSet<Object>();
		for(String c : classNameSet) {
			if (c.equals("org.openecomp.policy.common.ia.jpa.IntegrityAuditEntity")){
				String resourceName1 = resourceName;
				String resourceName2 = resourceName;
				
				IntegrityAuditEntity entry1 = new IntegrityAuditEntity();
				IntegrityAuditEntity entry2 = new IntegrityAuditEntity();
				Date date = new Date();
				
				/*
				 * Two entries with the same field values
				 */
				entry1.setDesignated(false);
				entry1.setJdbcDriver(dbDriver);
				entry1.setJdbcPassword(dbPwd);
				entry1.setJdbcUrl(dbUrl);
				entry1.setJdbcUser(dbUser);
				entry1.setLastUpdated(date);
				entry1.setNodeType(nodeType);
				entry1.setPersistenceUnit(persistenceUnit);
				entry1.setResourceName(resourceName1);
				entry1.setSite(siteName);
				
				entry2.setDesignated(false);
				entry2.setJdbcDriver(dbDriver);
				entry2.setJdbcPassword(dbPwd);
				entry2.setJdbcUrl(dbUrl);
				entry2.setJdbcUser(dbUser);
				entry2.setLastUpdated(date);
				entry2.setNodeType(nodeType);
				entry2.setPersistenceUnit(persistenceUnit);
				entry2.setResourceName(resourceName2);
				entry2.setSite(siteName);
				
				HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
				HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();
				
				myEntries.put("pdp1", entry1);
				theirEntries.put("pdp1", entry2);
						
				mismatchResult = dbAudit.compareEntries(myEntries, theirEntries);
				
				/*
				 * Assert there was no mismatches
				 */
				assertTrue(mismatchResult.isEmpty());
			}
			else if (c.equals("org.openecomp.policy.common.ia.test.jpa.IaTestEntity")) {
				IaTestEntity iate = new IaTestEntity();
				IaTestEntity iate2 = new IaTestEntity();
				IaTestEntity iate3 = new IaTestEntity();
				IaTestEntity iate4 = new IaTestEntity();
				
				Date date = new Date();
				
				/*
				 * Four entries, 2 mismatches
				 */
				iate.setCreatedBy("Ford");
				iate.setModifiedBy("Ford");
				iate.setModifiedDate(date);
				
				iate2.setCreatedBy("Ford");
				iate2.setModifiedBy("Zaphod");
				iate2.setModifiedDate(date);
				
				iate3.setCreatedBy("Zaphod");
				iate3.setModifiedBy("Ford");
				iate3.setModifiedDate(date);
				
				iate4.setCreatedBy("Ford");
				iate4.setModifiedBy("Ford");
				iate4.setModifiedDate(date);
				
				HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
				HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();
				
				myEntries.put("0", iate);
				myEntries.put("1", iate2);
				theirEntries.put("0", iate3);
				theirEntries.put("1", iate4);
				
				mismatchResult = dbAudit.compareEntries(myEntries, theirEntries);
				
				/*
				 * Assert that there is 2 mismatches
				 */
				assertEquals(2, mismatchResult.size());
			}
		}
		
		logger.info("testCompareAllHashEntities: Exit");
	}
	
	/*
	 * Tests that comparison algorithm works for each entity in the database 
	 */
	@Ignore
	@Test
	public void testCompareAllDbEntities() throws Exception {
		logger.info("testCompareAllDbEntities: Entering");

		logger.info("Setting up DB");

		IntegrityAudit.isUnitTesting = true;
		
		properties = new Properties();
		properties.put(IntegrityAuditProperties.DB_DRIVER, IntegrityAuditProperties.DEFAULT_DB_DRIVER);
		properties.put(IntegrityAuditProperties.DB_URL, IntegrityAuditProperties.DEFAULT_DB_URL);
		properties.put(IntegrityAuditProperties.DB_USER, IntegrityAuditProperties.DEFAULT_DB_USER);
		properties.put(IntegrityAuditProperties.DB_PWD, IntegrityAuditProperties.DEFAULT_DB_PWD);
		properties.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");

		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, IntegrityAuditProperties.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, "jdbc:h2:file:./sql/iaTest2");
		properties2.put(IntegrityAuditProperties.DB_USER, IntegrityAuditProperties.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, IntegrityAuditProperties.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
		
		dbDriver = IntegrityAuditProperties.DEFAULT_DB_DRIVER;
		dbUrl = IntegrityAuditProperties.DEFAULT_DB_URL;
		dbUser = IntegrityAuditProperties.DEFAULT_DB_USER;
		dbPwd = IntegrityAuditProperties.DEFAULT_DB_PWD;
		siteName = "SiteA";
		nodeType = "pdp_xacml";
		persistenceUnit = "testPU";
		resourceName = "pdp1";
		
		//Clean up the two DBs
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManagerFactory emf2 = Persistence.createEntityManagerFactory(persistenceUnit, properties2);
		
		EntityManager em = emf.createEntityManager();
		EntityManager em2 = emf2.createEntityManager();
		// Start a transaction
		EntityTransaction et = em.getTransaction();
		EntityTransaction et2 = em2.getTransaction();

		/*
		 * Delete entries in first DB
		 */
		et.begin();

		// Clean the IntegrityAuditEntity table
		em.createQuery("Delete from IntegrityAuditEntity").executeUpdate();

		// commit transaction
		et.commit();
		
		et.begin();

		// Clean the IaTestEntity table
		em.createQuery("Delete from IaTestEntity").executeUpdate();

		// commit transaction
		et.commit();
		em.close();
		
		/*
		 * Delete entries in second DB
		 */
		et2.begin();

		// Clean the IntegrityAuditEntity table
		em2.createQuery("Delete from IntegrityAuditEntity").executeUpdate();

		// commit transaction
		et2.commit();
		
		et2.begin();

		// Clean the IaTestEntity table
		em2.createQuery("Delete from IaTestEntity").executeUpdate();

		// commit transaction
		et2.commit();
		em2.close();
		logger.info("Exiting set up");
		
		// Add entries into DB1
		dbDAO = new DbDAO(resourceName, persistenceUnit, properties);
		new DbDAO("pdp2", persistenceUnit, properties);
		DbAudit dbAudit = new DbAudit(dbDAO);
		
		// Add entries into DB2
		DbDAO dbDAO3 = new DbDAO(resourceName, persistenceUnit, properties2);
		new DbDAO("pdp2", persistenceUnit, properties2);
		
		// Pull all entries and compare
		HashSet<String> classNameSet = dbDAO.getPersistenceClassNames();
		HashMap<Object, Object> myEntries;
		HashMap<Object, Object> theirEntries;
		HashSet<Object> mismatchResult = new HashSet<Object>();
		String className;
		for(String c : classNameSet) {
			className = c;
			logger.info("classNameSet entry = " + c);
			myEntries = dbDAO.getAllEntries(persistenceUnit, properties, className);
			theirEntries = dbDAO3.getAllEntries(persistenceUnit, properties2, className);
			mismatchResult = dbAudit.compareEntries(myEntries, theirEntries);
			if(className.contains("IntegrityAuditEntity")){
				break;
			}
		}
		
		// Assert that there is 2 mismatches between IntegrityAuditEntity tables
		assertEquals(2, mismatchResult.size());
		
		logger.info("testCompareAllDbEntities: Exit");
	}
	
	/*
	 * Tests that differences in embedded classes are still caught  
	 */
	@Ignore
	@Test
	public void testEmbeddedClass() throws Exception {
		logger.info("testEmbeddedClasses: Entering");
		
		dbDAO = new DbDAO(resourceName, persistenceUnit, properties);
		DbAudit dbAudit = new DbAudit(dbDAO);
		
		String className = null;
		//There is only one entry IntegrityAuditEntity, but we will check anyway
		HashSet<String> classNameSet = dbDAO.getPersistenceClassNames();
		for(String c : classNameSet){
			if (c.equals("org.openecomp.policy.common.ia.test.jpa.IaTestEntity")){
				className = c;
			}
		}
		
		IaTestEntity iate = new IaTestEntity();
		IaTestEntity iate2 = new IaTestEntity();
		
		Date date = new Date();
		
		PersonTest person = new PersonTest("Ford", "Prefect", 21);
		PersonTest person2 = new PersonTest("Zaphod", "Beeblebrox", 25);
		
		/*
		 * Two entries, 1 mismatch
		 */
		iate.setCreatedBy("Ford");
		iate.setModifiedBy("Zaphod");
		iate.setModifiedDate(date);
		iate.setPersonTest(person);
		
		iate2.setCreatedBy("Ford");
		iate2.setModifiedBy("Zaphod");
		iate2.setModifiedDate(date);
		iate2.setPersonTest(person2);
		
		dbAudit.writeAuditDebugLog(className, "resource1", "resource2", iate, iate2);
		
		HashMap<Object, Object> myEntries = new HashMap<Object, Object>();
		HashMap<Object, Object> theirEntries = new HashMap<Object, Object>();
		
		myEntries.put("0", iate);
		theirEntries.put("0", iate2);
				
		HashSet<Object> result = dbAudit.compareEntries(myEntries, theirEntries);
		
		/*
		 * Assert that there are no mismatches returned
		 */
		assertTrue(!result.isEmpty());
		
		logger.info("testEmbeddedClasses: Exit");
	}
}
