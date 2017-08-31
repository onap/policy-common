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

import org.onap.policy.common.im.StateManagement;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateManagementEntityTest {
	private static Logger logger = LoggerFactory.getLogger(StateManagementEntityTest.class);
	
	private static final String DEFAULT_DB_DRIVER = "org.h2.Driver";
	private static final String DEFAULT_DB_URL = "jdbc:h2:file:./sql/smTest";
	//private static final String DEFAULT_DB_URL = "jdbc:h2:file:./sql/xacml";
	private static final String DEFAULT_DB_USER = "sa";
	private static final String DEFAULT_DB_PWD = "";

	/*
	private static final String DEFAULT_DB_DRIVER = "org.mariadb.jdbc.Driver";
	private static final String DEFAULT_DB_URL    = "jdbc:mariadb://localhost:3306/xacml";
	private static final String DEFAULT_DB_USER   = "policy_user";
	private static final String DEFAULT_DB_PWD    = "policy_user";
	*/

	private static final String DB_DRIVER         = "javax.persistence.jdbc.driver";
	private static final String DB_URL            = "javax.persistence.jdbc.url";
	private static final String DB_USER           = "javax.persistence.jdbc.user";
	private static final String DB_PWD            = "javax.persistence.jdbc.password";
	  
	@BeforeClass
	public static void setUpClass() throws Exception {

	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	//@Ignore
	@Test
	public void testJPA() throws Exception {
		logger.debug("\n??? logger.infor StateManagementEntityTest: Entering\n\n");
		
		Properties myProp = new Properties();
		myProp.put(DB_DRIVER, DEFAULT_DB_DRIVER);
		myProp.put(DB_URL,    DEFAULT_DB_URL);
		myProp.put(DB_USER,   DEFAULT_DB_USER);
		myProp.put(DB_PWD,    DEFAULT_DB_PWD);
		
		logger.debug("??? {} = {}", DB_DRIVER, DEFAULT_DB_DRIVER); 
		logger.debug("??? {} = {}",  DB_URL, DEFAULT_DB_URL); 
		logger.debug("??? {} = {}",  DB_USER, DEFAULT_DB_USER); 
		logger.debug("??? {} = {}",  DB_PWD, DEFAULT_DB_PWD); 
		
		//Create the data schema and entity manager factory
		logger.debug("??? createEntityManagerFactory for schemaPU"); 
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("schemaPU", myProp);

		// Create an entity manager to use the DB
		logger.debug("??? createEntityManager");
		EntityManager em = emf.createEntityManager();
		logger.debug("??? getTransaction");
		EntityTransaction et = em.getTransaction();
		et.begin();
		// Make sure the DB is clean
		logger.debug("??? clean StateManagementEntity");
		em.createQuery("DELETE FROM StateManagementEntity").executeUpdate();

		//Define the resourceName for the StateManagement constructor
		String resourceName = "test_resource1";
		
		//
		logger.debug("Create StateManagementEntity, resourceName: {}", resourceName);
		logger.debug("??? instantiate StateManagementEntity object");
		StateManagementEntity sme = new StateManagementEntity(); 
		
		logger.debug("??? setResourceName : {}", resourceName);
		sme.setResourceName(resourceName);
		logger.debug("??? getResourceName : {}", sme.getResourceName());

		logger.debug("??? setAdminState   : {}", StateManagement.UNLOCKED);
		sme.setAdminState(StateManagement.UNLOCKED); 
		logger.debug("??? getAdminState   : {}", sme.getAdminState());
		
		logger.debug("??? setOpState      : {}", StateManagement.ENABLED);
		sme.setOpState(StateManagement.ENABLED);
		logger.debug("??? getOpState      : {}", sme.getOpState());
		
		logger.debug("??? setAvailStatus   : {}", StateManagement.NULL_VALUE);
		sme.setAvailStatus(StateManagement.NULL_VALUE);
		logger.debug("??? getAvailStatus   : {}", sme.getAvailStatus());
		
		logger.debug("??? setStandbyStatus: {}", StateManagement.COLD_STANDBY);
		sme.setStandbyStatus(StateManagement.COLD_STANDBY);
		logger.debug("??? getStandbyStatus: {}", sme.getStandbyStatus());
		
		logger.debug("??? before persist");
		em.persist(sme); 
		logger.debug("??? after  persist");
		
		em.flush(); 
		logger.debug("??? after flush");

		et.commit(); 
		logger.debug("??? after commit");
		
		try {
	        Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
	       
	        query.setParameter("resource", resourceName);
	       
	        //Just test that we are retrieving the right object
	        @SuppressWarnings("rawtypes")
	        List resourceList = query.getResultList();
	        if (!resourceList.isEmpty()) {
	           // exist 
	           StateManagementEntity sme2 = (StateManagementEntity) resourceList.get(0);
	       	   logger.debug("??? -- Retrieve StateManagementEntity from database --\n\nsme.getResourceName() = {}\n" + 
	       			   			"sme2getResourceName() = {}\n\nsme.getAdminState() = {}\nsme2.getAdminState() = {}\n\n" + 
	       			   			"sme.getOpState() = {}\nsme2.getOpState() = {}\n\nsme.getAvailStatus() = {}\n" +
	       			   			"sme2.getAvailStatus() = {}\n\nsme.getStandbyStatus() = {}\nsme2.getStandbyStatus() = {}",
	       			   			sme.getResourceName(), 
	       			   			sme2.getResourceName(), 
	       			   			sme.getAdminState(),
	       			   			sme2.getAdminState(),
	       			   			sme.getOpState(),
	       			   			sme2.getOpState(),
	       			   			sme.getAvailStatus(),
	       			   			sme.getAvailStatus(),
	       			   			sme.getStandbyStatus(),
	       			   			sme2.getStandbyStatus());
	       	   		
	       	   
	       	   assert(sme2.getResourceName().equals(sme.getResourceName())); 
	       	   assert(sme2.getAdminState().equals(sme.getAdminState())); 
	       	   assert(sme2.getOpState().equals(sme.getOpState())); 
	       	   assert(sme2.getAvailStatus().equals(sme.getAvailStatus())); 
	       	   assert(sme2.getStandbyStatus().equals(sme.getStandbyStatus())); 
			   logger.debug("--");
	        } else {
	           logger.debug("Record not found, resourceName: {}", resourceName);
	        }
		  } catch(Exception ex) {
			logger.error("Exception on select query: " + ex.toString());
	    }
		
		em.close(); 
		logger.debug("\n??? after close");
		logger.debug("\n\nJpaTest: Exit\n\n");
	}
}
