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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;


//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.onap.policy.common.ia.DbAudit;
import org.onap.policy.common.ia.DbAuditException;
import org.onap.policy.common.ia.DbDAO;
import org.onap.policy.common.ia.DbDaoTransactionException;
import org.onap.policy.common.ia.IntegrityAudit;
import org.onap.policy.common.ia.IntegrityAuditProperties;
import org.onap.policy.common.ia.jpa.IntegrityAuditEntity;
import org.onap.policy.common.logging.flexlogger.FlexLogger; 
import org.onap.policy.common.logging.flexlogger.Logger;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class DbAuditTest {
	
	private static Logger logger = FlexLogger.getLogger(DbAuditTest.class);
	
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
	private static final String ERROR_LOG = "./testingLogs/common-modules/integrity-audit/error.log";
	
	public void cleanLog() throws Exception{
		
		logger.debug("cleanLog: enter");
		//FileOutputStream fstream = new FileOutputStream("IntegrityAudit.log");
		FileOutputStream fstream = new FileOutputStream(TEST_LOG);
		fstream.close();
		fstream = new FileOutputStream(ERROR_LOG);
		fstream.close();
		logger.debug("cleanLog: exit");
	}
	
	public void cleanDb(String persistenceUnit, Properties properties){
		logger.debug("cleanDb: enter");

		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		
		EntityManager em = emf.createEntityManager();
		// Start a transaction
		EntityTransaction et = em.getTransaction();

		et.begin();

		// Clean up the DB
		em.createQuery("Delete from IntegrityAuditEntity").executeUpdate();

		// commit transaction
		et.commit();
		em.close();
		logger.debug("cleanDb: exit");
	}
	

	@Before
	public void setUp() throws Exception {

		logger.info("setUp: Entering");

		IntegrityAudit.isUnitTesting = true;
		IntegrityAuditEntity.isUnitTesting = true;
		
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
	
	@After
	public void tearDown() throws Exception {
		
		logger.info("tearDown: Entering");
		
		//cleanDb(persistenceUnit, properties);
		
		logger.info("tearDown: Exiting");
	}
	
	@Ignore
	@Test
	public void runAllTests() throws Exception{
		//The order is important - I haven't figured out why, but it is.
		mismatchTest();
		noEntitiesTest();
		oneEntityTest();
	}


	/*
	 * Tests printing an error to the log in the event where
	 * there are no entities saved in the database
	 */
	public void noEntitiesTest() throws Exception {
		cleanLog();
		cleanDb(persistenceUnit, properties);
		
		logger.info("noEntitiesTest: Entering");
		
		// Boolean to assert there are no entries found
		Boolean noEntities = false;
		
		dbDAO = new DbDAO(resourceName, persistenceUnit, properties);
		dbDAO.deleteAllIntegrityAuditEntities();
		try {
			DbAudit dbAudit = new DbAudit(dbDAO);
			dbAudit.dbAudit(resourceName, persistenceUnit, nodeType);
		}
		catch (DbAuditException e) {
			noEntities = true;
		}
		
		dbDAO.deleteAllIntegrityAuditEntities();
		
		logger.info("noEntitiesTest: No entities are persisted in the database");
		
		// Assert there are no entities retrieved
		assertTrue(noEntities);
		
		logger.info("noEntitiesTest: Exit");
	}
	
	/*
	 * Tests the detection of only one entry in the database
	 */
	public void oneEntityTest() throws Exception{
		cleanLog();
		cleanDb(persistenceUnit, properties);
		
		logger.info("oneEntityTest: Entering");
	
		// Add one entry in the database
		dbDAO = new DbDAO(resourceName, persistenceUnit, properties);
		DbAudit dbAudit = new DbAudit(dbDAO);
		dbAudit.dbAudit(resourceName, persistenceUnit, nodeType);
		
		List<IntegrityAuditEntity> iaeList = dbDAO.getIntegrityAuditEntities(persistenceUnit, nodeType);
		logger.info("List size: " + iaeList.size());
		
		//FileInputStream fstream = new FileInputStream("IntegrityAudit.log");
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		Boolean oneEntity = false;
		while ((strLine = br.readLine()) != null)   {
			 //parse strLine to obtain what you want 

			if (strLine.contains("DbAudit: Found only one IntegrityAuditEntity entry:")) {
				oneEntity = true;
			}

		}
		if(oneEntity){
			logger.info("oneEntityTest: One entity is persisted in the database");
		}else{
			logger.info("oneEntityTest: No entities are persisted in the database");
		}
		
		
		// Assert there is only one entry
		assertTrue(oneEntity);
		
		br.close();
		
		logger.info("oneEntityTest: Exit");
	}
	
	/*
	 * Tests reporting mismatches and misentries using the error log
	 */
	@SuppressWarnings("unused")
	public void mismatchTest() throws Exception{
		cleanLog();
		logger.info("mismatchTest: Entering");

		// Properties for DB2
		Properties properties2 = new Properties();
		properties2.put(IntegrityAuditProperties.DB_DRIVER, IntegrityAuditProperties.DEFAULT_DB_DRIVER);
		properties2.put(IntegrityAuditProperties.DB_URL, "jdbc:h2:file:./sql/iaTest2");
		properties2.put(IntegrityAuditProperties.DB_USER, IntegrityAuditProperties.DEFAULT_DB_USER);
		properties2.put(IntegrityAuditProperties.DB_PWD, IntegrityAuditProperties.DEFAULT_DB_PWD);
		properties2.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties2.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
	
		//Clean the DBs before we begin
		cleanDb(persistenceUnit, properties);
		cleanDb(persistenceUnit, properties2);
		
		// Entries in DB1
		dbDAO = new DbDAO(resourceName, persistenceUnit, properties);
		DbDAO dbDAO2 = new DbDAO("pdp2", persistenceUnit, properties);
		
		/*
		 * dbDAO3 is a mismatch entry, dbDAO7 is a misentry
		 */
		DbDAO dbDAO3 = new DbDAO("pdp3", persistenceUnit, properties);
		DbDAO dbDAO7 = new DbDAO("pdp4", persistenceUnit, properties);
		Date date = new Date();
		
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		
		/*
		 * Update DB url's in DB1 to point to DB2
		 */
		try{
			EntityManager em = emf.createEntityManager();
			// Start a transaction
			EntityTransaction et = em.getTransaction();

			et.begin();

			// if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not found, create a new entry
			Query iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", "pdp2");
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList = iaequery.getResultList();
			IntegrityAuditEntity iae = null;

			//If it already exists, we just want to update the properties and lastUpdated date
			if(!iaeList.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
				logger.info("Resource: " + "pdp2" + " with PersistenceUnit: " + persistenceUnit 
						+ " exists and entry be updated");
			}else{
				// If it does not exist, we also must add teh resourceName, persistenceUnit and designated values
				logger.info("Adding resource " + "pdp2" + " with PersistenceUnit: " + persistenceUnit
						+ " to IntegrityAuditEntity table");  	
				iae = new IntegrityAuditEntity();
				iae.setResourceName("pdp2");
				iae.setPersistenceUnit(persistenceUnit);
				iae.setDesignated(false);
			}
			
			//update/set properties in entry
			iae.setSite(siteName);
			iae.setNodeType(nodeType);
			iae.setLastUpdated(date);
			iae.setCreatedDate(date);
			iae.setJdbcDriver(dbDriver);
			iae.setJdbcPassword(dbPwd);
			iae.setJdbcUrl("jdbc:h2:file:./sql/iaTest2");
			iae.setJdbcUser(dbUser);

			em.persist(iae);
			// flush to the DB
			em.flush();

			// commit transaction
			et.commit();
			
			et.begin();

			// if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not found, create a new entry
			iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", "pdp1");
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList2 = iaequery.getResultList();
			iae = null;

			//If it already exists, we just want to update the properties and lastUpdated date
			if(!iaeList2.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList2.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
				logger.info("Resource: " + "pdp1" + " with PersistenceUnit: " + persistenceUnit 
						+ " exists and entry be updated");
			}else{
				// If it does not exist, we also must add teh resourceName, persistenceUnit and designated values
				logger.info("Adding resource " + "pdp1" + " with PersistenceUnit: " + persistenceUnit
						+ " to IntegrityAuditEntity table");  	
				iae = new IntegrityAuditEntity();
				iae.setResourceName("pdp1");
				iae.setPersistenceUnit(persistenceUnit);
				iae.setDesignated(false);
			}
			
			//update/set properties in entry
			iae.setSite(siteName);
			iae.setNodeType(nodeType);
			iae.setLastUpdated(date);
			iae.setCreatedDate(date);
			iae.setJdbcDriver(dbDriver);
			iae.setJdbcPassword(dbPwd);
			iae.setJdbcUrl("jdbc:h2:file:./sql/iaTest2");
			iae.setJdbcUser(dbUser);
			
			em.persist(iae);
			// flush to the DB
			em.flush();

			// commit transaction
			et.commit();
			
			et.begin();

			// if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not found, create a new entry
			iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", "pdp3");
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList3 = iaequery.getResultList();
			iae = null;

			//If it already exists, we just want to update the properties and lastUpdated date
			if(!iaeList3.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList3.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
				logger.info("Resource: " + "pdp3" + " with PersistenceUnit: " + persistenceUnit 
						+ " exists and entry be updated");
			}else{
				// If it does not exist, we also must add the resourceName, persistenceUnit and designated values
				logger.info("Adding resource " + "pdp3" + " with PersistenceUnit: " + persistenceUnit
						+ " to IntegrityAuditEntity table");  	
				iae = new IntegrityAuditEntity();
				iae.setResourceName("pdp3");
				iae.setPersistenceUnit(persistenceUnit);
				iae.setDesignated(false);
			}
			
			//update/set properties in entry
			iae.setSite(siteName);
			iae.setNodeType(nodeType);
			iae.setLastUpdated(date);
			iae.setCreatedDate(date);
			iae.setJdbcDriver(dbDriver);
			iae.setJdbcPassword(dbPwd);
			iae.setJdbcUrl(dbUrl);
			iae.setJdbcUser(dbUser);
			
			em.persist(iae);
			// flush to the DB
			em.flush();

			// commit transaction
			et.commit();
			
			et.begin();

			// if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not found, create a new entry
			iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", "pdp4");
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList4 = iaequery.getResultList();
			iae = null;

			//If it already exists, we just want to update the properties and lastUpdated date
			if(!iaeList4.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList4.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
				logger.info("Resource: " + "pdp4" + " with PersistenceUnit: " + persistenceUnit 
						+ " exists and entry be updated");
			}else{
				// If it does not exist, we also must add the resourceName, persistenceUnit and designated values
				logger.info("Adding resource " + "pdp4" + " with PersistenceUnit: " + persistenceUnit
						+ " to IntegrityAuditEntity table");  	
				iae = new IntegrityAuditEntity();
				iae.setResourceName("pdp4");
				iae.setPersistenceUnit(persistenceUnit);
				iae.setDesignated(false);
			}
			
			//update/set properties in entry
			iae.setSite(siteName);
			iae.setNodeType(nodeType);
			iae.setLastUpdated(date);
			iae.setCreatedDate(date);
			iae.setJdbcDriver(dbDriver);
			iae.setJdbcPassword(dbPwd);
			iae.setJdbcUrl("jdbc:h2:file:./sql/iaTest2");
			iae.setJdbcUser(dbUser);
			
			em.persist(iae);
			// flush to the DB
			em.flush();

			// commit transaction
			et.commit();
			
			em.close();
		}catch (Exception e){
			String msg = "DbDAO: " + "register() " + "ecountered a problem in execution: ";
			logger.error(msg + e);
			throw new DbDaoTransactionException(e);
		}
		
		/* 
		 * Identical entries in from DB1 in DB2 except for dbDAO6
		 */
		emf = Persistence.createEntityManagerFactory(persistenceUnit, properties2);
		DbDAO dbDAO4 = new DbDAO(resourceName, persistenceUnit, properties2);
		
		DbDAO dbDAO5 = new DbDAO("pdp2", persistenceUnit, properties2);
		
		/*
		 * This is the mismatch entry
		 */
		DbDAO dbDAO6 = new DbDAO("pdp3", persistenceUnit, properties2);
		try{
			EntityManager em = emf.createEntityManager();
			// Start a transaction
			EntityTransaction et = em.getTransaction();

			et.begin();

			// if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not found, create a new entry
			Query iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", "pdp2");
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList = iaequery.getResultList();
			IntegrityAuditEntity iae = null;

			//If it already exists, we just want to update the properties and lastUpdated date
			if(!iaeList.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
				logger.info("Resource: " + "pdp2" + " with PersistenceUnit: " + persistenceUnit 
						+ " exists and entry be updated");
			}else{
				// If it does not exist, we also must add teh resourceName, persistenceUnit and designated values
				logger.info("Adding resource " + "pdp2" + " with PersistenceUnit: " + persistenceUnit
						+ " to IntegrityAuditEntity table");  	
				iae = new IntegrityAuditEntity();
				iae.setResourceName("pdp2");
				iae.setPersistenceUnit(persistenceUnit);
				iae.setDesignated(false);
			}
			
			//update/set properties in entry
			iae.setSite(siteName);
			iae.setNodeType(nodeType);
			iae.setLastUpdated(date);
			iae.setCreatedDate(date);
			iae.setJdbcDriver(dbDriver);
			iae.setJdbcPassword(dbPwd);
			iae.setJdbcUrl("jdbc:h2:file:./sql/iaTest2");
			iae.setJdbcUser(dbUser);

			em.persist(iae);
			// flush to the DB
			em.flush();

			// commit transaction
			et.commit();
			
			et.begin();

			// if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not found, create a new entry
			iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", "pdp1");
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList2 = iaequery.getResultList();
			iae = null;

			//If it already exists, we just want to update the properties and lastUpdated date
			if(!iaeList2.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList2.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
				logger.info("Resource: " + "pdp1" + " with PersistenceUnit: " + persistenceUnit 
						+ " exists and entry be updated");
			}else{
				// If it does not exist, we also must add teh resourceName, persistenceUnit and designated values
				logger.info("Adding resource " + "pdp1" + " with PersistenceUnit: " + persistenceUnit
						+ " to IntegrityAuditEntity table");  	
				iae = new IntegrityAuditEntity();
				iae.setResourceName("pdp1");
				iae.setPersistenceUnit(persistenceUnit);
				iae.setDesignated(false);
			}
			
			//update/set properties in entry
			iae.setSite(siteName);
			iae.setNodeType(nodeType);
			iae.setLastUpdated(date);
			iae.setCreatedDate(date);
			iae.setJdbcDriver(dbDriver);
			iae.setJdbcPassword(dbPwd);
			iae.setJdbcUrl("jdbc:h2:file:./sql/iaTest2");
			iae.setJdbcUser(dbUser);
			
			em.persist(iae);
			// flush to the DB
			em.flush();

			// commit transaction
			et.commit();
			
			et.begin();

			// if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not found, create a new entry
			iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", "pdp3");
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList3 = iaequery.getResultList();
			iae = null;

			//If it already exists, we just want to update the properties and lastUpdated date
			if(!iaeList3.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList3.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
				logger.info("Resource: " + "pdp3" + " with PersistenceUnit: " + persistenceUnit 
						+ " exists and entry be updated");
			}else{
				// If it does not exist, we also must add teh resourceName, persistenceUnit and designated values
				logger.info("Adding resource " + "pdp3" + " with PersistenceUnit: " + persistenceUnit
						+ " to IntegrityAuditEntity table");  	
				iae = new IntegrityAuditEntity();
				iae.setResourceName("pdp3");
				iae.setPersistenceUnit(persistenceUnit);
				iae.setDesignated(false);
			}
			
			//update/set properties in entry
			iae.setSite(siteName);
			iae.setNodeType(nodeType);
			iae.setLastUpdated(date);
			iae.setCreatedDate(date);
			iae.setJdbcDriver(dbDriver);
			iae.setJdbcPassword(dbPwd);
			iae.setJdbcUrl("jdbc:h2:file:./sql/iaTest2");
			iae.setJdbcUser(dbUser);
			
			em.persist(iae);
			// flush to the DB
			em.flush();

			// commit transaction
			et.commit();
			
			em.close();
		}catch (Exception e){
			String msg = "DbDAO: " + "register() " + "ecountered a problem in execution: ";
			logger.error(msg + e);
			throw new DbDaoTransactionException(e);

		}
		
		/*
		 * Run the DB Audit, once it finds a mismatch and sleeps, update DB1
		 * to have the same entry as DB2 it can be confirmed that the mismatch
		 * is resolved
		 */
		DbAudit dbAudit = new DbAudit(dbDAO);
		dbAudit.dbAudit(resourceName, persistenceUnit, nodeType);
		emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		try{
			EntityManager em = emf.createEntityManager();
			// Start a transaction
			EntityTransaction et = em.getTransaction();

			et.begin();

			// if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not found, create a new entry
			Query iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", "pdp3");
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList = iaequery.getResultList();
			IntegrityAuditEntity iae = null;

			//If it already exists, we just want to update the properties and lastUpdated date
			if(!iaeList.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
				logger.info("Resource: " + "pdp3" + " with PersistenceUnit: " + persistenceUnit 
						+ " exists and entry be updated");
			}else{
				// If it does not exist, we also must add the resourceName, persistenceUnit and designated values
				logger.info("Adding resource " + "pdp3" + " with PersistenceUnit: " + persistenceUnit
						+ " to IntegrityAuditEntity table");  	
				iae = new IntegrityAuditEntity();
				iae.setResourceName("pdp3");
				iae.setPersistenceUnit(persistenceUnit);
				iae.setDesignated(false);
			}
			
			//update/set properties in entry
			iae.setSite(siteName);
			iae.setNodeType(nodeType);
			iae.setLastUpdated(date);
			iae.setCreatedDate(date);
			iae.setJdbcDriver(dbDriver);
			iae.setJdbcPassword(dbPwd);
			iae.setJdbcUrl("jdbc:h2:file:./sql/iaTest2");
			iae.setJdbcUser(dbUser);

			em.persist(iae);
			// flush to the DB
			em.flush();

			// commit transaction
			et.commit();
			em.close();
		}catch (Exception e){
			String msg = "DbDAO: " + "register() " + "ecountered a problem in execution: ";
			logger.error(msg + e);
			throw new DbDaoTransactionException(e);
		}
		
		/*
		 * Run the audit again and correct the mismatch, the result should be one
		 * entry in the mismatchKeySet because of the misentry from the beginning
		 * of the test
		 */
		dbAudit.dbAudit(resourceName, persistenceUnit, nodeType);
		
		//Cleanup DB2
		cleanDb(persistenceUnit, properties2);
		
		FileInputStream fstream = new FileInputStream(TEST_LOG);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int startIndex;
		String mismatchIndex = "";
		while ((strLine = br.readLine()) != null)   {
			 //parse strLine to obtain what you want...retrieve the last entry

			if (strLine.contains("Mismatched entries (keys):")) {
				startIndex = strLine.indexOf("(keys):") + 8;
				mismatchIndex = strLine.substring(startIndex);
			}
		}
		int mismatchEntries = mismatchIndex.trim().split(",").length;
		logger.info("mismatchTest: mismatchIndex found: '" + mismatchIndex + "'" 
				+ " mismatachEntries = " + mismatchEntries);
		
		// Assert there is only one entry index
		assertEquals(1, mismatchEntries);
		
		br.close();
		
		//Now check the entry in the error.log
		fstream = new FileInputStream(ERROR_LOG);
		br = new BufferedReader(new InputStreamReader(fstream));
		String mismatchNum = "";
		while ((strLine = br.readLine()) != null)   {
			 //parse strLine to obtain what you want...retrieve the last entry

			if (strLine.contains("DB Audit:")) {
				startIndex = strLine.indexOf("DB Audit:") + 10;
				mismatchNum = strLine.substring(startIndex, startIndex+1);
			}
		}
		logger.info("mismatchTest: mismatchNum found: '" + mismatchNum + "'");
		
		// Assert that there are a total of 3 mismatches - 1 between each comparison node.
		assertEquals("3", mismatchNum);
		
		br.close();
		
		logger.info("mismatchTest: Exit");
	}
	
}
