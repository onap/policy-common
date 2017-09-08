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

import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.onap.policy.common.im.StateTransition;
import org.onap.policy.common.im.StateElement;
import org.onap.policy.common.logging.flexlogger.FlexLogger; 
import org.onap.policy.common.logging.flexlogger.Logger;
/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class StateTransitionTest {
	private static Logger logger = FlexLogger.getLogger(StateTransitionTest.class);
	
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

	//@Ignore
	@Test
	public void testJPA() throws Exception {
		logger.info("\n\nlogger.infor StateTransitionTest: Entering\n\n");
		//These parameters are in a properties file
		EntityManagerFactory emf = null; 
		try {
		    Properties myProp = new Properties();
		    myProp.put(DB_DRIVER, DEFAULT_DB_DRIVER);
		    myProp.put(DB_URL,    DEFAULT_DB_URL);
		    myProp.put(DB_USER,   DEFAULT_DB_USER);
		    myProp.put(DB_PWD,    DEFAULT_DB_PWD);
		
		    logger.info("??? create a new StateTransition"); 
		    StateTransition st = new StateTransition();
 
		    StateElement se = null; 
		    try {
		    	// bad test case 
		    	se = st.getEndingState("unlocked", "enabled", "null",  "coldstandby", "lock");
		    	//
		        logger.info("??? StateTransition testcase 1");
		        se = st.getEndingState("unlocked", "enabled", "null",  "null", "lock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 2");
		        se = st.getEndingState("unlocked", "enabled", "null",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 3");
		        se = st.getEndingState("unlocked", "enabled", "null",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 4");
		        se = st.getEndingState("unlocked", "enabled", "null",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 5");
		        se = st.getEndingState("unlocked", "enabled", "null",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);
                
		        logger.info("??? StateTransition testcase 6");
		        se = st.getEndingState("unlocked", "enabled", "null",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 7");
		        se = st.getEndingState("unlocked", "enabled", "null",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 8");
		        se = st.getEndingState("unlocked", "enabled", "null",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 9");
		        se = st.getEndingState("unlocked", "enabled", "null",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 10");
		        se = st.getEndingState("unlocked", "enabled", "null",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 11");
		        se = st.getEndingState("unlocked", "enabled", "null",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 12");
		        se = st.getEndingState("unlocked", "enabled", "null",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 13");
		        se = st.getEndingState("unlocked", "enabled", "null",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 14");
		        se = st.getEndingState("unlocked", "enabled", "null",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 15");
		        se = st.getEndingState("unlocked", "enabled", "null",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 16");
		        se = st.getEndingState("unlocked", "enabled", "null",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 17");
		        se = st.getEndingState("unlocked", "enabled", "null",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 18");
		        se = st.getEndingState("unlocked", "enabled", "null",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 19");
		        se = st.getEndingState("unlocked", "enabled", "null",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 20");
		        se = st.getEndingState("unlocked", "enabled", "null",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 21");
		        se = st.getEndingState("unlocked", "enabled", "null",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 22");
		        se = st.getEndingState("unlocked", "enabled", "null",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 23");
		        se = st.getEndingState("unlocked", "enabled", "null",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 24");
		        se = st.getEndingState("unlocked", "enabled", "null",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 25");
		        se = st.getEndingState("unlocked", "enabled", "null",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 26");
		        se = st.getEndingState("unlocked", "enabled", "null",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 27");
		        se = st.getEndingState("unlocked", "enabled", "null",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 28");
		        se = st.getEndingState("unlocked", "enabled", "null",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 29");
		        se = st.getEndingState("unlocked", "enabled", "null",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 30");
		        se = st.getEndingState("unlocked", "enabled", "null",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 31");
		        se = st.getEndingState("unlocked", "enabled", "null",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 32");
		        se = st.getEndingState("unlocked", "enabled", "null",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 33");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 34");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 35");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 36");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 37");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 38");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 39");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 40");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 41");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 42");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 43");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 44");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 45");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 46");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 47");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 48");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 49");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 50");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 51");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 52");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 53");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 54");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 55");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 56");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 57");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 58");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 59");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 60");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 61");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 62");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 63");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 64");
		        se = st.getEndingState("unlocked", "enabled", "failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 65");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 66");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 67");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 68");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 69");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 70");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 71");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 72");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 73");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 74");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 75");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 76");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 77");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 78");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 79");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 80");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 81");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 82");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 83");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 84");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 85");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 86");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 87");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 88");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 89");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 90");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 91");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 92");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 93");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 94");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 95");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 96");
		        se = st.getEndingState("unlocked", "enabled", "dependency",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 97");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 98");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 99");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 100");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 101");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 102");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 103");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 104");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 105");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 106");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 107");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 108");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 109");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 110");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 111");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 112");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 113");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 114");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 115");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 116");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 117");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 118");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 119");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 120");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 121");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 122");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 123");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 124");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 125");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 126");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 127");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 128");
		        se = st.getEndingState("unlocked", "enabled", "dependency,failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 129");
		        se = st.getEndingState("unlocked", "disabled", "null",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 130");
		        se = st.getEndingState("unlocked", "disabled", "null",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 131");
		        se = st.getEndingState("unlocked", "disabled", "null",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 132");
		        se = st.getEndingState("unlocked", "disabled", "null",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 133");
		        se = st.getEndingState("unlocked", "disabled", "null",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 134");
		        se = st.getEndingState("unlocked", "disabled", "null",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 135");
		        se = st.getEndingState("unlocked", "disabled", "null",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 136");
		        se = st.getEndingState("unlocked", "disabled", "null",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 137");
		        se = st.getEndingState("unlocked", "disabled", "null",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 138");
		        se = st.getEndingState("unlocked", "disabled", "null",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 139");
		        se = st.getEndingState("unlocked", "disabled", "null",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 140");
		        se = st.getEndingState("unlocked", "disabled", "null",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 141");
		        se = st.getEndingState("unlocked", "disabled", "null",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 142");
		        se = st.getEndingState("unlocked", "disabled", "null",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 143");
		        se = st.getEndingState("unlocked", "disabled", "null",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 144");
		        se = st.getEndingState("unlocked", "disabled", "null",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 145");
		        se = st.getEndingState("unlocked", "disabled", "null",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 146");
		        se = st.getEndingState("unlocked", "disabled", "null",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 147");
		        se = st.getEndingState("unlocked", "disabled", "null",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 148");
		        se = st.getEndingState("unlocked", "disabled", "null",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 149");
		        se = st.getEndingState("unlocked", "disabled", "null",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 150");
		        se = st.getEndingState("unlocked", "disabled", "null",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 151");
		        se = st.getEndingState("unlocked", "disabled", "null",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 152");
		        se = st.getEndingState("unlocked", "disabled", "null",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 153");
		        se = st.getEndingState("unlocked", "disabled", "null",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 154");
		        se = st.getEndingState("unlocked", "disabled", "null",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 155");
		        se = st.getEndingState("unlocked", "disabled", "null",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 156");
		        se = st.getEndingState("unlocked", "disabled", "null",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 157");
		        se = st.getEndingState("unlocked", "disabled", "null",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 158");
		        se = st.getEndingState("unlocked", "disabled", "null",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 159");
		        se = st.getEndingState("unlocked", "disabled", "null",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 160");
		        se = st.getEndingState("unlocked", "disabled", "null",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 161");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 162");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 163");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 164");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 165");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 166");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 167");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 168");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 169");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 170");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 171");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 172");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 173");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 174");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 175");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 176");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 177");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 178");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 179");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 180");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 181");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 182");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 183");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 184");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 185");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 186");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 187");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 188");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 189");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 190");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 191");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 192");
		        se = st.getEndingState("unlocked", "disabled", "failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 193");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 194");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 195");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 196");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 197");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 198");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 199");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 200");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 201");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 202");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 203");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 204");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 205");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 206");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 207");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 208");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 209");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 210");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 211");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 212");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 213");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 214");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 215");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 216");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 217");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 218");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 219");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 220");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 221");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 222");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 223");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 224");
		        se = st.getEndingState("unlocked", "disabled", "dependency",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 225");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 226");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 227");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 228");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 229");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 230");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 231");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 232");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 233");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 234");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 235");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 236");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 237");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 238");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 239");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 240");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 241");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 242");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 243");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 244");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 245");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 246");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 247");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 248");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 249");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 250");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 251");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 252");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 253");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 254");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 255");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 256");
		        se = st.getEndingState("unlocked", "disabled", "dependency,failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 257");
		        se = st.getEndingState("locked", "enabled", "null",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 258");
		        se = st.getEndingState("locked", "enabled", "null",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 259");
		        se = st.getEndingState("locked", "enabled", "null",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 260");
		        se = st.getEndingState("locked", "enabled", "null",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 261");
		        se = st.getEndingState("locked", "enabled", "null",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 262");
		        se = st.getEndingState("locked", "enabled", "null",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 263");
		        se = st.getEndingState("locked", "enabled", "null",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 264");
		        se = st.getEndingState("locked", "enabled", "null",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 265");
		        se = st.getEndingState("locked", "enabled", "null",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 266");
		        se = st.getEndingState("locked", "enabled", "null",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 267");
		        se = st.getEndingState("locked", "enabled", "null",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 268");
		        se = st.getEndingState("locked", "enabled", "null",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 269");
		        se = st.getEndingState("locked", "enabled", "null",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 270");
		        se = st.getEndingState("locked", "enabled", "null",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 271");
		        se = st.getEndingState("locked", "enabled", "null",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 272");
		        se = st.getEndingState("locked", "enabled", "null",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 273");
		        se = st.getEndingState("locked", "enabled", "null",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 274");
		        se = st.getEndingState("locked", "enabled", "null",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 275");
		        se = st.getEndingState("locked", "enabled", "null",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 276");
		        se = st.getEndingState("locked", "enabled", "null",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 277");
		        se = st.getEndingState("locked", "enabled", "null",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 278");
		        se = st.getEndingState("locked", "enabled", "null",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 279");
		        se = st.getEndingState("locked", "enabled", "null",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 280");
		        se = st.getEndingState("locked", "enabled", "null",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 281");
		        se = st.getEndingState("locked", "enabled", "null",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 282");
		        se = st.getEndingState("locked", "enabled", "null",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 283");
		        se = st.getEndingState("locked", "enabled", "null",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 284");
		        se = st.getEndingState("locked", "enabled", "null",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 285");
		        se = st.getEndingState("locked", "enabled", "null",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 286");
		        se = st.getEndingState("locked", "enabled", "null",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 287");
		        se = st.getEndingState("locked", "enabled", "null",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 288");
		        se = st.getEndingState("locked", "enabled", "null",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 289");
		        se = st.getEndingState("locked", "enabled", "failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 290");
		        se = st.getEndingState("locked", "enabled", "failed",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 291");
		        se = st.getEndingState("locked", "enabled", "failed",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 292");
		        se = st.getEndingState("locked", "enabled", "failed",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 293");
		        se = st.getEndingState("locked", "enabled", "failed",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 294");
		        se = st.getEndingState("locked", "enabled", "failed",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 295");
		        se = st.getEndingState("locked", "enabled", "failed",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 296");
		        se = st.getEndingState("locked", "enabled", "failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 297");
		        se = st.getEndingState("locked", "enabled", "failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 298");
		        se = st.getEndingState("locked", "enabled", "failed",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 299");
		        se = st.getEndingState("locked", "enabled", "failed",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 300");
		        se = st.getEndingState("locked", "enabled", "failed",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 301");
		        se = st.getEndingState("locked", "enabled", "failed",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 302");
		        se = st.getEndingState("locked", "enabled", "failed",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 303");
		        se = st.getEndingState("locked", "enabled", "failed",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 304");
		        se = st.getEndingState("locked", "enabled", "failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 305");
		        se = st.getEndingState("locked", "enabled", "failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 306");
		        se = st.getEndingState("locked", "enabled", "failed",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 307");
		        se = st.getEndingState("locked", "enabled", "failed",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 308");
		        se = st.getEndingState("locked", "enabled", "failed",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 309");
		        se = st.getEndingState("locked", "enabled", "failed",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 310");
		        se = st.getEndingState("locked", "enabled", "failed",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 311");
		        se = st.getEndingState("locked", "enabled", "failed",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 312");
		        se = st.getEndingState("locked", "enabled", "failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 313");
		        se = st.getEndingState("locked", "enabled", "failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 314");
		        se = st.getEndingState("locked", "enabled", "failed",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 315");
		        se = st.getEndingState("locked", "enabled", "failed",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 316");
		        se = st.getEndingState("locked", "enabled", "failed",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 317");
		        se = st.getEndingState("locked", "enabled", "failed",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 318");
		        se = st.getEndingState("locked", "enabled", "failed",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 319");
		        se = st.getEndingState("locked", "enabled", "failed",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 320");
		        se = st.getEndingState("locked", "enabled", "failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 321");
		        se = st.getEndingState("locked", "enabled", "dependency",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 322");
		        se = st.getEndingState("locked", "enabled", "dependency",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 323");
		        se = st.getEndingState("locked", "enabled", "dependency",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 324");
		        se = st.getEndingState("locked", "enabled", "dependency",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 325");
		        se = st.getEndingState("locked", "enabled", "dependency",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 326");
		        se = st.getEndingState("locked", "enabled", "dependency",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 327");
		        se = st.getEndingState("locked", "enabled", "dependency",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 328");
		        se = st.getEndingState("locked", "enabled", "dependency",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 329");
		        se = st.getEndingState("locked", "enabled", "dependency",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 330");
		        se = st.getEndingState("locked", "enabled", "dependency",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 331");
		        se = st.getEndingState("locked", "enabled", "dependency",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 332");
		        se = st.getEndingState("locked", "enabled", "dependency",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 333");
		        se = st.getEndingState("locked", "enabled", "dependency",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 334");
		        se = st.getEndingState("locked", "enabled", "dependency",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 335");
		        se = st.getEndingState("locked", "enabled", "dependency",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 336");
		        se = st.getEndingState("locked", "enabled", "dependency",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 337");
		        se = st.getEndingState("locked", "enabled", "dependency",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 338");
		        se = st.getEndingState("locked", "enabled", "dependency",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 339");
		        se = st.getEndingState("locked", "enabled", "dependency",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 340");
		        se = st.getEndingState("locked", "enabled", "dependency",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 341");
		        se = st.getEndingState("locked", "enabled", "dependency",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 342");
		        se = st.getEndingState("locked", "enabled", "dependency",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 343");
		        se = st.getEndingState("locked", "enabled", "dependency",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 344");
		        se = st.getEndingState("locked", "enabled", "dependency",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 345");
		        se = st.getEndingState("locked", "enabled", "dependency",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 346");
		        se = st.getEndingState("locked", "enabled", "dependency",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 347");
		        se = st.getEndingState("locked", "enabled", "dependency",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 348");
		        se = st.getEndingState("locked", "enabled", "dependency",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 349");
		        se = st.getEndingState("locked", "enabled", "dependency",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 350");
		        se = st.getEndingState("locked", "enabled", "dependency",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 351");
		        se = st.getEndingState("locked", "enabled", "dependency",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 352");
		        se = st.getEndingState("locked", "enabled", "dependency",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 353");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 354");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 355");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 356");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 357");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 358");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 359");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 360");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 361");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 362");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 363");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 364");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 365");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 366");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 367");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 368");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 369");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 370");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 371");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 372");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 373");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 374");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 375");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 376");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 377");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 378");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 379");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 380");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 381");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 382");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 383");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 384");
		        se = st.getEndingState("locked", "enabled", "dependency,failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 385");
		        se = st.getEndingState("locked", "disabled", "null",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 386");
		        se = st.getEndingState("locked", "disabled", "null",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 387");
		        se = st.getEndingState("locked", "disabled", "null",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 388");
		        se = st.getEndingState("locked", "disabled", "null",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 389");
		        se = st.getEndingState("locked", "disabled", "null",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 390");
		        se = st.getEndingState("locked", "disabled", "null",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 391");
		        se = st.getEndingState("locked", "disabled", "null",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 392");
		        se = st.getEndingState("locked", "disabled", "null",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 393");
		        se = st.getEndingState("locked", "disabled", "null",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 394");
		        se = st.getEndingState("locked", "disabled", "null",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 395");
		        se = st.getEndingState("locked", "disabled", "null",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 396");
		        se = st.getEndingState("locked", "disabled", "null",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 397");
		        se = st.getEndingState("locked", "disabled", "null",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 398");
		        se = st.getEndingState("locked", "disabled", "null",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 399");
		        se = st.getEndingState("locked", "disabled", "null",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 400");
		        se = st.getEndingState("locked", "disabled", "null",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 401");
		        se = st.getEndingState("locked", "disabled", "null",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 402");
		        se = st.getEndingState("locked", "disabled", "null",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 403");
		        se = st.getEndingState("locked", "disabled", "null",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 404");
		        se = st.getEndingState("locked", "disabled", "null",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 405");
		        se = st.getEndingState("locked", "disabled", "null",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 406");
		        se = st.getEndingState("locked", "disabled", "null",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 407");
		        se = st.getEndingState("locked", "disabled", "null",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 408");
		        se = st.getEndingState("locked", "disabled", "null",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 409");
		        se = st.getEndingState("locked", "disabled", "null",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 410");
		        se = st.getEndingState("locked", "disabled", "null",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 411");
		        se = st.getEndingState("locked", "disabled", "null",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 412");
		        se = st.getEndingState("locked", "disabled", "null",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 413");
		        se = st.getEndingState("locked", "disabled", "null",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 414");
		        se = st.getEndingState("locked", "disabled", "null",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 415");
		        se = st.getEndingState("locked", "disabled", "null",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 416");
		        se = st.getEndingState("locked", "disabled", "null",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 417");
		        se = st.getEndingState("locked", "disabled", "failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 418");
		        se = st.getEndingState("locked", "disabled", "failed",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 419");
		        se = st.getEndingState("locked", "disabled", "failed",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 420");
		        se = st.getEndingState("locked", "disabled", "failed",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 421");
		        se = st.getEndingState("locked", "disabled", "failed",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 422");
		        se = st.getEndingState("locked", "disabled", "failed",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 423");
		        se = st.getEndingState("locked", "disabled", "failed",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 424");
		        se = st.getEndingState("locked", "disabled", "failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 425");
		        se = st.getEndingState("locked", "disabled", "failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 426");
		        se = st.getEndingState("locked", "disabled", "failed",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 427");
		        se = st.getEndingState("locked", "disabled", "failed",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 428");
		        se = st.getEndingState("locked", "disabled", "failed",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 429");
		        se = st.getEndingState("locked", "disabled", "failed",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 430");
		        se = st.getEndingState("locked", "disabled", "failed",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 431");
		        se = st.getEndingState("locked", "disabled", "failed",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 432");
		        se = st.getEndingState("locked", "disabled", "failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 433");
		        se = st.getEndingState("locked", "disabled", "failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 434");
		        se = st.getEndingState("locked", "disabled", "failed",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 435");
		        se = st.getEndingState("locked", "disabled", "failed",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 436");
		        se = st.getEndingState("locked", "disabled", "failed",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 437");
		        se = st.getEndingState("locked", "disabled", "failed",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 438");
		        se = st.getEndingState("locked", "disabled", "failed",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 439");
		        se = st.getEndingState("locked", "disabled", "failed",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 440");
		        se = st.getEndingState("locked", "disabled", "failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 441");
		        se = st.getEndingState("locked", "disabled", "failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 442");
		        se = st.getEndingState("locked", "disabled", "failed",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 443");
		        se = st.getEndingState("locked", "disabled", "failed",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 444");
		        se = st.getEndingState("locked", "disabled", "failed",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 445");
		        se = st.getEndingState("locked", "disabled", "failed",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 446");
		        se = st.getEndingState("locked", "disabled", "failed",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 447");
		        se = st.getEndingState("locked", "disabled", "failed",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 448");
		        se = st.getEndingState("locked", "disabled", "failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 449");
		        se = st.getEndingState("locked", "disabled", "dependency",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 450");
		        se = st.getEndingState("locked", "disabled", "dependency",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 451");
		        se = st.getEndingState("locked", "disabled", "dependency",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 452");
		        se = st.getEndingState("locked", "disabled", "dependency",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 453");
		        se = st.getEndingState("locked", "disabled", "dependency",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 454");
		        se = st.getEndingState("locked", "disabled", "dependency",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 455");
		        se = st.getEndingState("locked", "disabled", "dependency",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 456");
		        se = st.getEndingState("locked", "disabled", "dependency",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 457");
		        se = st.getEndingState("locked", "disabled", "dependency",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 458");
		        se = st.getEndingState("locked", "disabled", "dependency",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 459");
		        se = st.getEndingState("locked", "disabled", "dependency",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 460");
		        se = st.getEndingState("locked", "disabled", "dependency",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 461");
		        se = st.getEndingState("locked", "disabled", "dependency",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 462");
		        se = st.getEndingState("locked", "disabled", "dependency",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 463");
		        se = st.getEndingState("locked", "disabled", "dependency",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 464");
		        se = st.getEndingState("locked", "disabled", "dependency",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 465");
		        se = st.getEndingState("locked", "disabled", "dependency",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 466");
		        se = st.getEndingState("locked", "disabled", "dependency",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 467");
		        se = st.getEndingState("locked", "disabled", "dependency",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 468");
		        se = st.getEndingState("locked", "disabled", "dependency",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 469");
		        se = st.getEndingState("locked", "disabled", "dependency",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 470");
		        se = st.getEndingState("locked", "disabled", "dependency",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 471");
		        se = st.getEndingState("locked", "disabled", "dependency",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 472");
		        se = st.getEndingState("locked", "disabled", "dependency",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 473");
		        se = st.getEndingState("locked", "disabled", "dependency",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 474");
		        se = st.getEndingState("locked", "disabled", "dependency",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 475");
		        se = st.getEndingState("locked", "disabled", "dependency",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 476");
		        se = st.getEndingState("locked", "disabled", "dependency",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 477");
		        se = st.getEndingState("locked", "disabled", "dependency",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 478");
		        se = st.getEndingState("locked", "disabled", "dependency",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 479");
		        se = st.getEndingState("locked", "disabled", "dependency",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 480");
		        se = st.getEndingState("locked", "disabled", "dependency",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 481");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 482");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "null", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 483");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "null", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 484");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "null", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 485");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "null", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 486");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "null", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 487");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "null", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 488");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "null", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 489");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 490");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "coldstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 491");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "coldstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 492");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "coldstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 493");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "coldstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 494");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "coldstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 495");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "coldstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 496");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "coldstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 497");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 498");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "hotstandby", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 499");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "hotstandby", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 500");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "hotstandby", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 501");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "hotstandby", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 502");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "hotstandby", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 503");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "hotstandby", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 504");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "hotstandby", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 505");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 506");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "providingservice", "unlock");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 507");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "providingservice", "disableFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 508");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "providingservice", "enableNotFailed");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 509");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "providingservice", "disableDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 510");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "providingservice", "enableNoDependency");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 511");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "providingservice", "promote");
		        if (se != null) displayEndingState(se);

		        logger.info("??? StateTransition testcase 512");
		        se = st.getEndingState("locked", "disabled", "dependency,failed",  "providingservice", "demote");
		        if (se != null) displayEndingState(se);
		        
		    } catch (Exception ex) {
		    	logger.error("EndingState NOT found");
		    	throw new Exception("EndingState NOT found. " + ex);
		    }

		   	//if (emf.isOpen()) {
		   		//emf.close(); 
		   	//}
		} catch(Exception ex) {
			logger.error("Exception: " + ex.toString());
			throw new Exception("Failure getting ending state. " + ex );
		} finally {
			if (emf != null && emf.isOpen()) {
			    emf.close(); 
			}
		}

	    logger.info("\n\nStateTransitionTest: Exit\n\n");
	}
	
	private void displayEndingState(StateElement se) 
	{
		String endingStandbyStatus = se.getEndingStandbyStatus(); 
		if (endingStandbyStatus != null) {
			endingStandbyStatus.replace(".",  ",");
		}
	    logger.info("EndingAdminState   = [" + se.getEndingAdminState() +"]"); 
	    logger.info("EndingOpState      = [" + se.getEndingOpState() +"]"); 
	   	logger.info("EndingAvailStatus  = [" + se.getEndingAvailStatus() +"]"); 
	   	logger.info("EndingStandbyStatus= [" + endingStandbyStatus +"]");
	   	logger.info("Exception          = [" + se.getException() +"]"); 	   	
	} 
}