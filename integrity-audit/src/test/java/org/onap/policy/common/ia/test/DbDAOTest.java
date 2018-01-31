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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.ia.DbDAO;
import org.onap.policy.common.ia.DbDaoTransactionException;
import org.onap.policy.common.ia.IntegrityAuditProperties;
import org.onap.policy.common.ia.jpa.IntegrityAuditEntity;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 * 
 * If any have been ignored (@Ignore) they will not run at the same time
 * as others. You should run them as JUnits by themselves.
 */
public class DbDAOTest {
	private static String persistenceUnit;
	private static Properties properties;
	private static String resourceName;
	
	DbDAO d;
	
	@Before
	public void setUp() throws Exception {
		properties = new Properties();
		properties.put(IntegrityAuditProperties.DB_DRIVER, TestUtils.DEFAULT_DB_DRIVER);
		properties.put(IntegrityAuditProperties.DB_URL, TestUtils.DEFAULT_DB_URL);
		properties.put(IntegrityAuditProperties.DB_USER, TestUtils.DEFAULT_DB_USER);
		properties.put(IntegrityAuditProperties.DB_PWD, TestUtils.DEFAULT_DB_PWD);
		properties.put(IntegrityAuditProperties.SITE_NAME, "SiteA");
		properties.put(IntegrityAuditProperties.NODE_TYPE, "pdp_xacml");
				
		//persistenceUnit = "integrityAuditPU";
		persistenceUnit = "testPU";
		resourceName = "pdp0";		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/* Tests registering a new IntegrityAuditEntity object in the DB */
	//@Ignore
	@Test
	public void testNewRegistration() {
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
			EntityManager em = emf.createEntityManager();
			
			// Start a transaction
			EntityTransaction et = em.getTransaction();
			
			// Begin Transaction
			et.begin();
			
			// Clean the DB
			em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
			
			// flush to the DB
			em.flush();
			et.commit();
	        
			et.begin();
			d = new DbDAO(resourceName, persistenceUnit, properties);
			
	       	// Find the proper entry in the database
	    	Query iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
	    	iaequery.setParameter("rn", DbDAOTest.resourceName);
	    	iaequery.setParameter("pu", DbDAOTest.persistenceUnit);
	    	
	    	@SuppressWarnings("rawtypes")
	    	List iaeList = iaequery.getResultList();
	    	
	    	// Assert that the IntegrityAuditEntity object was found
	    	assertNotNull(iaeList);
	    	
			// flush to the DB
			em.flush();
			et.commit();
			em.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Tests updating an IntegrityAuditEntity if it has already been registered */
	//@Ignore
	@Test
	public void testUpdateRegistration() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManager em = emf.createEntityManager();

		// Start a transaction
		EntityTransaction et = em.getTransaction();

		// Begin transaction
		et.begin();
		
		// Clean the DB
		em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
		
		// flush to the DB
		em.flush();
		et.commit();
					
		// close the EntityManager
		em.close();
		
		try {
			d = new DbDAO(resourceName, persistenceUnit, properties);
			
			// Change site_name in properties to test that an update was made to an existing entry in the table
			properties.put(IntegrityAuditProperties.SITE_NAME, "SiteB");
			d = new DbDAO(resourceName, persistenceUnit, properties);
			
			em = emf.createEntityManager();
			
			// Start a transaction
			et = em.getTransaction();
			
			// Begin Transaction
			et.begin();
	        
	       	// Find the proper entry in the database
	    	Query iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
	    	iaequery.setParameter("rn", DbDAOTest.resourceName);
	    	iaequery.setParameter("pu", DbDAOTest.persistenceUnit);
	    	
	    	@SuppressWarnings("rawtypes")
	    	List iaeList = iaequery.getResultList();
	    	IntegrityAuditEntity iae = null;
	    	if(!iaeList.isEmpty()) {
	    		//ignores multiple results
	    		iae = (IntegrityAuditEntity) iaeList.get(0);
	    		
	    		em.refresh(iae);
	    		em.persist(iae);
	        	
	    		// flush to the DB
	        	em.flush();
	        	
	        	// commit transaction
	        	et.commit();
	        	
	        	// close the EntityManager
	        	em.close();
	        	
	        	// Assert that the site_name for the existing entry was updated
	        	assertEquals("SiteB", iae.getSite());
	    	}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Tests obtaining all Integrity Audit Entities from a table */
	//@Ignore
	@Test
	public void testGetIntegrityAuditEntities() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManager em = emf.createEntityManager();
		
		// Start a transaction
		EntityTransaction et = em.getTransaction();
		
		et.begin();
		
		// Clean the DB
		em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
		
		// flush to the DB
		em.flush();
		
		// close the transaction
		et.commit();
		
		// close the EntityManager
		em.close();
		
		try {
			// Add some entries to the DB
			d = new DbDAO(resourceName, persistenceUnit, properties);
			new DbDAO("pdp1", persistenceUnit, properties);
			properties.put(IntegrityAuditProperties.NODE_TYPE, "pdp_drools");
			new DbDAO("pdp2", persistenceUnit, properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<IntegrityAuditEntity> entities;
		try {
			// Obtain entries based on persistenceUnit and nodeType
			entities = d.getIntegrityAuditEntities(persistenceUnit, "pdp_xacml");
			assertEquals(2, entities.size());
		} catch (DbDaoTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	/* Tests retrieving a DbDAO instance's IntegrityAuditEntity */
	//@Ignore
	@Test
	public void testGetMyIntegrityAuditEntity() {
		try {
			d = new DbDAO(resourceName, persistenceUnit, properties);
			IntegrityAuditEntity iae = d.getMyIntegrityAuditEntity();
			//assertEquals("integrityAuditPU", iae.getPersistenceUnit());
			assertEquals("testPU", iae.getPersistenceUnit());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Tests obtaining an IntegrityAuditEntity by ID */
	//@Ignore
	@Test
	public void testGetIntegrityAuditEntity() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManager em = emf.createEntityManager();
		
		// Start a transaction
		EntityTransaction et = em.getTransaction();
		
		// Begin transaction
		et.begin();
		
		// Clean the DB
		em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
		
		// flush to the DB
		em.flush();
		
		// close the transaction
		et.commit();
		
		try {
			// Obtain an entry from the database based on ID
			d = new DbDAO(resourceName, persistenceUnit, properties);
			
			et.begin();

			// Find the proper database entry
			Query iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", DbDAOTest.resourceName);
			iaequery.setParameter("pu", DbDAOTest.persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList = iaequery.getResultList();
			IntegrityAuditEntity iae = null;
			if(!iaeList.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList.get(0);
				
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
			
				// Obtain ID for an IntegrityAuditEntity
				PersistenceUnitUtil util = emf.getPersistenceUnitUtil(); 
				Object iaeId = util.getIdentifier(iae);

				// Obtain the same IntegrityAuditEntity based on ID
				IntegrityAuditEntity iaeDuplicate = d.getIntegrityAuditEntity((long) iaeId);
				Object duplicateId = util.getIdentifier(iaeDuplicate);
				
				// Assert that the proper entry was retrieved based on ID
				assertEquals((long) iaeId, (long) duplicateId);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// close the EntityManager
		em.close();
	}

	/* Tests setting an IntegrityAuditEntity as the designated node */
	//@Ignore
	@Test
	public void testSetDesignated() {
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
			EntityManager em = emf.createEntityManager();

			// Start a transaction
			EntityTransaction et = em.getTransaction();

			// Begin transaction
			et.begin();

			// Clean the DB
			em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
			
			// flush to the DB
			em.flush();
			et.commit();
			
			et.begin();
			
			// Create an entry and set it's designated field to true
			d = new DbDAO(resourceName, persistenceUnit, properties);
			d.setDesignated(resourceName, persistenceUnit, true);
			
			// Find the proper entry in the database
			Query iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", resourceName);
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList = iaequery.getResultList();
			IntegrityAuditEntity iae = null;

			if(!iaeList.isEmpty()){
				//ignores multiple results
				iae = (IntegrityAuditEntity) iaeList.get(0);
				em.refresh(iae);
				
				// Check if the node is designated
				boolean result = iae.isDesignated();

				// Assert that it is designated
				assertTrue(result);
			}
			
			// flush to the DB
			em.flush();
			
			// close the transaction
			et.commit();
			
			// close the EntityManager
			em.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* Tests that the lastUpdated column in the database is updated properly */
	//@Ignore
	@Test
	public void testSetLastUpdated() {
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
			EntityManager em = emf.createEntityManager();

			// Start a transaction
			EntityTransaction et = em.getTransaction();

			// Begin transaction
			et.begin();
			
			// Clean the DB
			em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
			
			// flush to the DB
			em.flush();
			et.commit();
			
			et.begin();

			// Create an entry
			d = new DbDAO(resourceName, persistenceUnit, properties);
			
			// Find the proper entry in the database
			Query iaequery = em.createQuery("Select i from IntegrityAuditEntity i where i.resourceName=:rn and i.persistenceUnit=:pu");
			iaequery.setParameter("rn", resourceName);
			iaequery.setParameter("pu", persistenceUnit);

			@SuppressWarnings("rawtypes")
			List iaeList = iaequery.getResultList();
			IntegrityAuditEntity iae = null;

			if(!iaeList.isEmpty()){
				// ignores multiple results
				iae = (IntegrityAuditEntity) iaeList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(iae);
				
				// Obtain old update value and set new update value
				Date oldDate = iae.getLastUpdated();
				iae.setSite("SiteB");
				iae.setLastUpdated(new Date());
				Date newDate = iae.getLastUpdated();
				
				em.persist(iae);
				// flush to the DB
				em.flush();
				// close the transaction
				et.commit();
				// close the EntityManager
				em.close();
				
				// Assert that the old and new update times are different
				assertNotEquals(oldDate, newDate);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Tests that all the entries from a class can be retrieved */
	//@Ignore
	@Test
	public void testGetAllMyEntriesString() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManager em = emf.createEntityManager();

		// Start a transaction
		EntityTransaction et = em.getTransaction();

		// Begin transaction
		et.begin();
		
		// Clean the DB
		em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
		
		// flush to the DB
		em.flush();
		et.commit();
					
		// close the EntityManager
		em.close();
		
		try {
			// create entries for the IntegrityAuditEntity table
			d = new DbDAO(resourceName, persistenceUnit, properties);
			new DbDAO("pdp1", persistenceUnit, properties);
			new DbDAO("pdp2", persistenceUnit, properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			// Obtain a hash with the persisted objects
			Map<Object, Object> entries = d.getAllMyEntries("org.onap.policy.common.ia.jpa.IntegrityAuditEntity");
			
			// Assert there were 3 entries for that class
			assertEquals(3, entries.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Tests retrieving all entities in a Persistence Unit using the class name and a hashset of IDs */
	//@Ignore
	@Test
	public void testGetAllMyEntriesStringHashSet() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManager em = emf.createEntityManager();

		// Start a transaction
		EntityTransaction et = em.getTransaction();

		// Begin transaction
		et.begin();
		
		// Clean the DB
		em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
		
		// flush to the DB
		em.flush();
		et.commit();
		
		try {
			// create entries for the IntegrityAuditEntity table
			d = new DbDAO(resourceName, persistenceUnit, properties);
			new DbDAO("pdp1", persistenceUnit, properties);
			new DbDAO("pdp2", persistenceUnit, properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			// Obtain all entity keys
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Object> cq = cb.createQuery();
			Root<?> rootEntry = cq.from(Class.forName("org.onap.policy.common.ia.jpa.IntegrityAuditEntity"));
			CriteriaQuery<Object> all = cq.select(rootEntry);
			TypedQuery<Object> allQuery = em.createQuery(all);
			List<Object> objectList = allQuery.getResultList();
			HashSet<Object> resultSet = new HashSet<Object>();
			PersistenceUnitUtil util = emf.getPersistenceUnitUtil();
			for (Object o: objectList){
				Object key = util.getIdentifier(o);
				resultSet.add(key);
			}
			
			// Obtain a hash with the persisted objects
			Map<Object, Object> entries = d.getAllMyEntries("org.onap.policy.common.ia.jpa.IntegrityAuditEntity", resultSet);
			
			// Assert there were 3 entries for that class
			assertEquals(3, entries.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// close the EntityManager
		em.close();
	}
	
	/* Tests retrieving all entities in a Persistence Unit using the persistence unit, properties, and class name */
	//@Ignore
	@Test
	public void testGetAllEntriesStringPropertiesString() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManager em = emf.createEntityManager();

		// Start a transaction
		EntityTransaction et = em.getTransaction();

		// Begin transaction
		et.begin();
		
		// Clean the DB
		em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
		
		// flush to the DB
		em.flush();
		et.commit();
					
		// close the EntityManager
		em.close();
		
		try {
			// create entries for the IntegrityAuditEntity table
			d = new DbDAO(resourceName, persistenceUnit, properties);
			new DbDAO("pdp1", persistenceUnit, properties);
			new DbDAO("pdp2", persistenceUnit, properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			// Obtain a hash with the persisted objects
			Map<Object, Object> entries = d.getAllEntries("integrityAuditPU", properties, "org.onap.policy.common.ia.jpa.IntegrityAuditEntity");
			
			// Assert there were 3 entries for that class
			assertEquals(3, entries.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Tests retrieving all entities in a Persistence Unit using the persistence unit, properties, class name, and a hashset of IDs */
	//@Ignore
	@Test
	public void testGetAllEntriesStringPropertiesStringHashSet() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManager em = emf.createEntityManager();

		// Start a transaction
		EntityTransaction et = em.getTransaction();

		// Begin transaction
		et.begin();
		
		// Clean the DB
		em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
		
		// flush to the DB
		em.flush();
		et.commit();
		
		try {
			// create entries for the IntegrityAuditEntity table
			d = new DbDAO(resourceName, persistenceUnit, properties);
			new DbDAO("pdp1", persistenceUnit, properties);
			new DbDAO("pdp2", persistenceUnit, properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			// Obtain all entity keys
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Object> cq = cb.createQuery();
			Root<?> rootEntry = cq.from(Class.forName("org.onap.policy.common.ia.jpa.IntegrityAuditEntity"));
			CriteriaQuery<Object> all = cq.select(rootEntry);
			TypedQuery<Object> allQuery = em.createQuery(all);
			List<Object> objectList = allQuery.getResultList();
			HashSet<Object> resultSet = new HashSet<Object>();
			PersistenceUnitUtil util = emf.getPersistenceUnitUtil();
			for (Object o: objectList){
				Object key = util.getIdentifier(o);
				resultSet.add(key);
			}
			
			// Obtain a hash with the persisted objects
			Map<Object, Object> entries = d.getAllEntries("integrityAuditPU", properties, "org.onap.policy.common.ia.jpa.IntegrityAuditEntity", resultSet);
			
			// Assert there were 3 entries for that class
			assertEquals(3, entries.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// close the EntityManager
		em.close();
	}
	
	/* Tests getting all the entries from a class based on persistenceUnit, properties, and className */
	//@Ignore
	@Test
	public void testGetAllEntries() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManager em = emf.createEntityManager();

		// Start a transaction
		EntityTransaction et = em.getTransaction();

		// Begin transaction
		et.begin();
		
		// Clean the DB
		em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
		
		// flush to the DB
		em.flush();
		et.commit();
					
		// close the EntityManager
		em.close();
		
		try {
			// create entries for the IntegrityAuditEntity table
			d = new DbDAO(resourceName, persistenceUnit, properties);
			new DbDAO("pdp1", persistenceUnit, properties);
			new DbDAO("pdp2", persistenceUnit, properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			// Obtain a hash with the persisted objects
			Map<Object, Object> entries = d.getAllEntries(persistenceUnit, properties, "org.onap.policy.common.ia.jpa.IntegrityAuditEntity");
			
			// Assert there were 3 entries for that class
			assertEquals(3, entries.size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Tests obtaining all class names of persisted classes */
	public void testGetPersistenceClassNames() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
		EntityManager em = emf.createEntityManager();

		// Start a transaction
		EntityTransaction et = em.getTransaction();

		// Begin transaction
		et.begin();
		
		// Clean the DB
		em.createQuery("DELETE FROM IntegrityAuditEntity").executeUpdate();
		
		// flush to the DB
		em.flush();
		et.commit();
					
		// close the EntityManager
		em.close();
		
		try {
			d = new DbDAO(resourceName, persistenceUnit, properties);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Retrieve persistence class names
		Set<String> result = d.getPersistenceClassNames();
		assertEquals(1, result.size());
	}
}
