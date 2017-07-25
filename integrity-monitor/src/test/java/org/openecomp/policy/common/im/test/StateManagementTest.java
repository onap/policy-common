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

import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.policy.common.im.StateManagement;
import org.openecomp.policy.common.im.StandbyStatusException; 
import org.openecomp.policy.common.im.StateChangeNotifier; 
import org.openecomp.policy.common.logging.flexlogger.FlexLogger; 
import org.openecomp.policy.common.logging.flexlogger.Logger;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class StateManagementTest {
	private static Logger logger = FlexLogger.getLogger(StateManagementTest.class);
	
	private static final String DEFAULT_DB_DRIVER = "org.h2.Driver";
	private static final String DEFAULT_DB_URL    = "jdbc:h2:file:./sql/smTest";
	private static final String DEFAULT_DB_USER   = "sa";
	private static final String DEFAULT_DB_PWD    = "";

	private static final String DB_DRIVER         = "javax.persistence.jdbc.driver";
	private static final String DB_URL            = "javax.persistence.jdbc.url";
	private static final String DB_USER           = "javax.persistence.jdbc.user";
	private static final String DB_PWD            = "javax.persistence.jdbc.password";
	// 
	  
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
		logger.info("\n\nlogger.infor StateManagementTest: Entering\n\n");
		String resourceName = "test_resource1"; 
		boolean standbyExceptionThrown = false; 
		
		//These parameters are in a properties file
		EntityManagerFactory emf = null; 
		try {
		    Properties myProp = new Properties();
		    myProp.put(DB_DRIVER, DEFAULT_DB_DRIVER);
		    myProp.put(DB_URL,    DEFAULT_DB_URL);
		    myProp.put(DB_USER,   DEFAULT_DB_USER);
		    myProp.put(DB_PWD,    DEFAULT_DB_PWD);
		
		    //Create the data schema and entity manager factory
		    emf = Persistence.createEntityManagerFactory("schemaPU", myProp);

		    StateManagement sm = new StateManagement(emf, resourceName);
		    System.out.println("\n\ntest lock()");
		    displayState(resourceName, sm);
		    logger.info("\n??? test lock()");
		    logger.info(resourceName + " before adminState   = " + sm.getAdminState()); 
		    logger.info(resourceName + " before opState      = " + sm.getOpState()); 
		   	logger.info(resourceName + " before availStatus  = " + sm.getAvailStatus()); 
		   	logger.info(resourceName + " before standbyStatus= " + sm.getStandbyStatus()); 
		    sm.lock(); 
		    System.out.println("\n\nafter lock()");
		    displayState(resourceName, sm);		    
		    logger.info(resourceName + " after  adminState   = " + sm.getAdminState()); 
		    logger.info(resourceName + " after  opState      = " + sm.getOpState()); 
		   	logger.info(resourceName + " after  availStatus  = " + sm.getAvailStatus()); 
		   	logger.info(resourceName + " after  standbyStatus= " + sm.getStandbyStatus()); 
		   	 
		    logger.info("\n??? test unlock()");
		    sm.unlock(); 
		    System.out.println("\n\nafter unlock()");
		    displayState(resourceName, sm);		
		    logger.info(resourceName + " adminState   = " + sm.getAdminState()); 
		    logger.info(resourceName + " opState      = " + sm.getOpState()); 
		   	logger.info(resourceName + " availStatus  = " + sm.getAvailStatus()); 
		   	logger.info(resourceName + " standbyStatus= " + sm.getStandbyStatus()); 
		   	
		    logger.info("\n??? test enableNotFailed()");
		    sm.enableNotFailed(); 
		    System.out.println("\n\nafter enableNotFailed()");
		    displayState(resourceName, sm);		
		    logger.info(resourceName + " adminState   = " + sm.getAdminState()); 
		    logger.info(resourceName + " opState      = " + sm.getOpState()); 
		   	logger.info(resourceName + " availStatus  = " + sm.getAvailStatus()); 
		   	logger.info(resourceName + " standbyStatus= " + sm.getStandbyStatus()); 
		    
		    logger.info("\n??? test disableFailed()");
		    sm.disableFailed();
		    System.out.println("\n\nafter disableFailed()");
		    displayState(resourceName, sm);		
		    logger.info(resourceName + " adminState   = " + sm.getAdminState()); 
		    logger.info(resourceName + " opState      = " + sm.getOpState()); 
		   	logger.info(resourceName + " availStatus  = " + sm.getAvailStatus()); 
		   	logger.info(resourceName + " standbyStatus= " + sm.getStandbyStatus()); 
		   	
		    // P4 If promote() is called while either the opState is disabled or the adminState is locked,  
		    // the standbystatus shall transition to coldstandby and a StandbyStatusException shall be thrown
		    logger.info("\n??? promote() test case P4");
		   	try {
		    	sm.disableFailed(); 
		    	sm.lock();
			    System.out.println("\n\nafter lock() and disableFailed");
			    displayState(resourceName, sm);		
			    logger.info(resourceName + " adminState   = " + sm.getAdminState()); 
			    logger.info(resourceName + " opState      = " + sm.getOpState()); 
			   	logger.info(resourceName + " standbyStatus= " + sm.getStandbyStatus());
			   	sm.promote(); 
			    System.out.println("\n\nafter promote");
			    displayState(resourceName, sm);		
		    } catch(StandbyStatusException ex) {
		    	standbyExceptionThrown = true; 
		    	logger.info("StandbyStatusException thrown and catched");
		    } catch(Exception ex) {
		    	logger.info("??? Exception: " + ex.toString());
		    }
		   	assert(standbyExceptionThrown); 
	    	assert(sm.getStandbyStatus().equals(StateManagement.COLD_STANDBY)); 
	    	standbyExceptionThrown = false; 
		    
		   	// P3 If promote() is called while standbyStatus is coldstandby, the state shall not transition 
		    //    and a StandbyStatusException shall be thrown
		    logger.info("\n??? promote() test case P3");
		    try {
			   	logger.info(resourceName + " standbyStatus= " + sm.getStandbyStatus()); 		    	
		    	sm.promote(); 
		    } catch(StandbyStatusException ex) {
		    	standbyExceptionThrown = true; 
		    	logger.info("StandbyStatusException thrown and catched");
		    } catch(Exception ex) {
		    	logger.info("??? Exception: " + ex.toString());
		    }	
		   	assert(standbyExceptionThrown); 
	    	assert(sm.getStandbyStatus().equals(StateManagement.COLD_STANDBY)); 
		    System.out.println("\n\nP3 after promote()");
		    displayState(resourceName, sm);	
	    	standbyExceptionThrown = false; 		    
		    
		    // P2 If promote() is called while the standbyStatus is null and the opState is enabled and adminState is unlocked, 
		    //    the state shall transition to providingservice
		    logger.info("\n??? promote() test case P2");
		    resourceName = "test_resource2"; 
		    StateManagement sm2 = new StateManagement(emf, resourceName);
		    sm2.enableNotFailed();
		    sm2.unlock(); 
		    System.out.println("\n\nafter sm2.enableNotFailed() and sm2.unlock()");
		    displayState(resourceName, sm2);	
		    logger.info(resourceName + " adminState   = " + sm2.getAdminState()); 
		    logger.info(resourceName + " opState      = " + sm2.getOpState()); 
		   	logger.info(resourceName + " standbyStatus= " + sm2.getStandbyStatus()); 		    
		    sm2.promote(); 
		    System.out.println("\n\nP2 after sm2.promote");
		    displayState(resourceName, sm2);	
		    assert(sm2.getAdminState().equals(StateManagement.UNLOCKED)); 
		    assert(sm2.getOpState().equals(StateManagement.ENABLED)); 
		    assert(sm2.getStandbyStatus().equals(StateManagement.PROVIDING_SERVICE));
		    
		    // P5 If promote() is called while standbyStatus is providingservice, no action is taken
		    logger.info("\n??? promote() test case P5");
		   	logger.info(resourceName + " standbyStatus= " + sm2.getStandbyStatus()); 
		    sm2.promote();  
		    System.out.println("\n\nP5 after sm2.promote()");
		    displayState(resourceName, sm2);	
		    assert(sm2.getStandbyStatus().equals(StateManagement.PROVIDING_SERVICE));
		    
		    // D1 If demote() is called while standbyStatus is providingservice, the state shall transition to hotstandby
		    logger.info("\n??? demote() test case D1");
		    logger.info(resourceName + " standbyStatus= " + sm2.getStandbyStatus()); 
		    sm2.demote(); 
		    System.out.println("\n\nD1 after sm2.demote()");
		    displayState(resourceName, sm2);	
		    assert(sm2.getStandbyStatus().equals(StateManagement.HOT_STANDBY));
		    
		    // D4 If demote() is called while standbyStatus is hotstandby, no action is taken
		    logger.info("\n??? demote() test case D4");
		    logger.info(resourceName + " standbyStatus= " + sm2.getStandbyStatus()); 
		    sm2.demote(); 
		    System.out.println("\n\nD4 after sm2.demote()");
		    displayState(resourceName, sm2);	
		    assert(sm2.getStandbyStatus().equals(StateManagement.HOT_STANDBY));
		    
		    // D3 If demote() is called while standbyStatus is null and adminState is locked or opState is disabled, 
		    //    the state shall transition to coldstandby
		    logger.info("\n??? demote() test case D3"); 
		    resourceName = "test_resource3"; 
		    StateManagement sm3 = new StateManagement(emf, resourceName);
		    sm3.lock(); 
		    sm3.disableFailed(); 
		    System.out.println("\n\nD3 after sm3.lock() and sm3.disableFailed()");
		    displayState(resourceName, sm3);	
		    logger.info(resourceName + " adminState   = " + sm3.getAdminState()); 
		    logger.info(resourceName + " opState      = " + sm3.getOpState()); 
		   	logger.info(resourceName + " standbyStatus= " + sm3.getStandbyStatus()); 		    
		    sm3.demote(); 
		    System.out.println("\n\nD3 after sm3.demote()");
		    displayState(resourceName, sm3);	
		    assert(sm3.getStandbyStatus().equals(StateManagement.COLD_STANDBY));
		    
		    // D5 If demote() is called while standbyStatus is coldstandby, no action is taken
		    logger.info("\n??? demote() test case D5");	
		    logger.info(resourceName + " standbyStatus= " + sm3.getStandbyStatus()); 
		    sm3.demote(); 
		    System.out.println("\n\nD5 after sm3.demote()");
		    displayState(resourceName, sm3);	
		    assert(sm3.getStandbyStatus().equals(StateManagement.COLD_STANDBY));		    
		    
		    // D2 If demote() is called while standbyStatus is null and adminState is unlocked and opState is enabled, 
		    //    the state shall transition to hotstandby
		    logger.info("\n??? demote() test case D2");
		    resourceName = "test_resource4"; 
		    StateManagement sm4 = new StateManagement(emf, resourceName);
		    sm4.unlock(); 
		    sm4.enableNotFailed(); 
		    System.out.println("\n\nD2 after sm4.unlock() and sm4.enableNotFailed()");
		    displayState(resourceName, sm4);	
		    logger.info(resourceName + " adminState   = " + sm4.getAdminState()); 
		    logger.info(resourceName + " opState      = " + sm4.getOpState()); 
		   	logger.info(resourceName + " standbyStatus= " + sm4.getStandbyStatus()); 
		    sm4.demote(); 
		    assert(sm4.getStandbyStatus().equals(StateManagement.HOT_STANDBY));
		    
		    // P1 If promote() is called while standbyStatus is hotstandby, the state shall transition to providingservice.
		    logger.info("\n??? promote() test case P1");
		    logger.info(resourceName + " standbyStatus= " + sm4.getStandbyStatus()); 
		    sm4.promote(); 
		    System.out.println("\n\nP1 after sm4.promote()");
		    displayState(resourceName, sm4);	
		    assert(sm4.getStandbyStatus().equals(StateManagement.PROVIDING_SERVICE));
		    
		    // State change notification
		    logger.info("\n??? State change notification test case 1 - lock()");
		    StateChangeNotifier stateChangeNotifier = new StateChangeNotifier(); 
		    sm.addObserver(stateChangeNotifier); 
		    sm.lock(); 
		    
		    logger.info("\n??? State change notification test case 2 - unlock()");
		    sm.unlock(); 
		    
		    logger.info("\n??? State change notification test case 3 - enabled()");
		    sm.enableNotFailed(); 
		    
		    logger.info("\n??? State change notification test case 4 - disableFailed()");
		    sm.disableFailed(); 

		    logger.info("\n??? State change notification test case 5 - demote()");
		    sm.demote(); 

	        logger.info("\n??? State change notification test case 6 - promote()");
		    try {
		      sm.promote(); 
		    } catch(Exception ex) {
		    	logger.info("Exception from promote(): " + ex.toString());
		    }
 
		   	if (emf.isOpen()) {
		   		emf.close(); 
		   	}
		} catch(Exception ex) {
			logger.error("Exception: " + ex.toString());
		} finally {
			if (emf.isOpen()) {
			    emf.close(); 
			}
		}

	    logger.info("\n\nStateManagementTest: Exit\n\n");
	}
	
	private void displayState(String resourceName, StateManagement sm) 
	{
		System.out.println("\nAdminState = " + sm.getAdminState()
				+ "\nOpState() = " + sm.getOpState()
				+ "\nAvailStatus = " + sm.getAvailStatus()
				+ "\nStandbyStatus = " + sm.getStandbyStatus()
				+ "\n");
	    logger.info(resourceName + " adminState   = " + sm.getAdminState()); 
	    logger.info(resourceName + " opState      = " + sm.getOpState()); 
	   	logger.info(resourceName + " availStatus  = " + sm.getAvailStatus()); 
	   	logger.info(resourceName + " standbyStatus= " + sm.getStandbyStatus()); 
	}
}

