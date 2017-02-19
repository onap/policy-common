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

import org.openecomp.policy.common.im.StateManagement;
import org.openecomp.policy.common.im.jpa.StateManagementEntity;
import org.openecomp.policy.common.logging.flexlogger.FlexLogger; 
import org.openecomp.policy.common.logging.flexlogger.Logger;

public class StateManagementEntityTest {
	private static Logger logger = FlexLogger.getLogger(StateManagementEntityTest.class);
	
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

	@Ignore
	@Test
	public void testJPA() throws Exception {
		System.out.println("\n??? logger.infor StateManagementEntityTest: Entering\n\n");
		
		Properties myProp = new Properties();
		myProp.put(DB_DRIVER, DEFAULT_DB_DRIVER);
		myProp.put(DB_URL,    DEFAULT_DB_URL);
		myProp.put(DB_USER,   DEFAULT_DB_USER);
		myProp.put(DB_PWD,    DEFAULT_DB_PWD);
		
		System.out.println("??? " + DB_DRIVER + "=" + DEFAULT_DB_DRIVER); 
		System.out.println("??? " + DB_URL    + "=" + DEFAULT_DB_URL); 
		System.out.println("??? " + DB_USER   + "=" + DEFAULT_DB_USER); 
		System.out.println("??? " + DB_PWD    + "=" + DEFAULT_DB_PWD); 
		
		//Create the data schema and entity manager factory
		System.out.println("??? createEntityManagerFactory for schemaPU"); 
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("schemaPU", myProp);

		// Create an entity manager to use the DB
		System.out.println("??? createEntityManager");
		EntityManager em = emf.createEntityManager();
		System.out.println("??? getTransaction");
		EntityTransaction et = em.getTransaction();
		et.begin();
		// Make sure the DB is clean
		System.out.println("??? clean StateManagementEntity");
		em.createQuery("DELETE FROM StateManagementEntity").executeUpdate();

		//Define the resourceName for the StateManagement constructor
		String resourceName = "test_resource1";
		
		//
		System.out.println("Create StateManagementEntity, resourceName: " + resourceName);
		System.out.println("??? instantiate StateManagementEntity object");
		StateManagementEntity sme = new StateManagementEntity(); 
		
		System.out.println("??? setResourceName : " + resourceName);
		sme.setResourceName(resourceName);
		System.out.println("??? getResourceName : " + sme.getResourceName());

		System.out.println("??? setAdminState   : " + StateManagement.UNLOCKED);
		sme.setAdminState(StateManagement.UNLOCKED); 
		System.out.println("??? getAdminState   : " + sme.getAdminState());
		
		System.out.println("??? setOpState      : " + StateManagement.ENABLED);
		sme.setOpState(StateManagement.ENABLED);
		System.out.println("??? getOpState      : " + sme.getOpState());
		
		System.out.println("??? setAvailStatus   : " + StateManagement.NULL_VALUE);
		sme.setAvailStatus(StateManagement.NULL_VALUE);
		System.out.println("??? getAvailStatus   : " + sme.getAvailStatus());
		
		System.out.println("??? setStandbyStatus: " + StateManagement.COLD_STANDBY);
		sme.setStandbyStatus(StateManagement.COLD_STANDBY);
		System.out.println("??? getStandbyStatus: " + sme.getStandbyStatus());
		
		System.out.println("??? before persist");
		em.persist(sme); 
		System.out.println("??? after  persist");
		
		em.flush(); 
		System.out.println("??? after flush");

		et.commit(); 
		System.out.println("??? after commit");
		
		try {
	        Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
	       
	        query.setParameter("resource", resourceName);
	       
	        //Just test that we are retrieving the right object
	        @SuppressWarnings("rawtypes")
	        List resourceList = query.getResultList();
	        String resource = null; 
	        if (!resourceList.isEmpty()) {
	           // exist 
	           StateManagementEntity sme2 = (StateManagementEntity) resourceList.get(0);
	       	   System.out.println("??? -- Retrieve StateManagementEntity from database --"
	       	   		+ "\n\nsme.getResourceName() = " + sme.getResourceName() 
	       	   		+ "\nsme2getResourceName() = " + sme2.getResourceName() 
	       	   		+ "\n\nsme.getAdminState() = " + sme.getAdminState()
	       	   		+ "\nsme2.getAdminState() = " + sme2.getAdminState()
	       	   		+ "\n\nsme.getOpState() = " + sme.getOpState()
	       	   		+ "\nsme2.getOpState() = " + sme2.getOpState()
	       			+ "\n\nsme.getAvailStatus() = " + sme.getAvailStatus()
	       			+ "\nsme2.getAvailStatus() = " + sme.getAvailStatus()
	       	   		+ "\n\nsme.getStandbyStatus() = " + sme.getStandbyStatus()
	       	   		+ "\nsme2.getStandbyStatus() = " + sme2.getStandbyStatus());
	       	   		
	       	   
	       	   assert(sme2.getResourceName().equals(sme.getResourceName())); 
	       	   assert(sme2.getAdminState().equals(sme.getAdminState())); 
	       	   assert(sme2.getOpState().equals(sme.getOpState())); 
	       	   assert(sme2.getAvailStatus().equals(sme.getAvailStatus())); 
	       	   assert(sme2.getStandbyStatus().equals(sme.getStandbyStatus())); 
			   System.out.println("--");
	        } else {
	           System.out.println("Record not found, resourceName: " + resourceName);
	        }
		  } catch(Exception ex) {
			logger.error("Exception on select query: " + ex.toString());
	    }
		
		em.close(); 
		System.out.println("\n??? after close");
		System.out.println("\n\nJpaTest: Exit\n\n");
	}
}
