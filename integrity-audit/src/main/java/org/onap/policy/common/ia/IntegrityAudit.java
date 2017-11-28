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

package org.onap.policy.common.ia;

import java.util.Properties;


import org.onap.policy.common.ia.IntegrityAuditProperties.NodeTypeEnum;
import org.onap.policy.common.logging.flexlogger.FlexLogger; 
import org.onap.policy.common.logging.flexlogger.Logger; 

/**
 * class IntegrityAudit
 * Audits all persisted entities for all resource clusters for all sites and logs any anomalies.
 */
public class IntegrityAudit {
	
	private static final Logger logger = FlexLogger.getLogger(IntegrityAudit.class);

	private static boolean isUnitTesting;
	private boolean isThreadInitialized = false; 
	
	AuditThread auditThread = null;
		
	private String persistenceUnit;
	private Properties properties;
	private String resourceName;
	
	
	/*
	 * This is the audit period in seconds. For example, if it had a value of 3600, the audit
	 * can only run once per hour.  If it has a value of 60, it can run once per minute.
	 * 
	 * Values: 
	 *    integrityAuditPeriodSeconds < 0 (negative number) indicates the audit is off
	 *    integrityAuditPeriodSecconds == 0 indicates the audit is to run continuously
	 *    integrityAuditPeriodSeconds > 0 indicates the audit is to run at most once during the indicated period
	 * 
	 */
	private int integrityAuditPeriodSeconds;
	
	/**
	 * IntegrityAudit constructor
	 * @param resourceName
	 * @param persistenceUnit
	 * @param properties
	 * @throws Exception
	 */
	public IntegrityAudit(String resourceName, String persistenceUnit, Properties properties) throws IntegrityAuditException {
		
		logger.info("Constructor: Entering and checking for nulls");
		String parmList = "";
		if (parmsAreBad(resourceName, persistenceUnit, properties, parmList)) {
			logger.error("Constructor: Parms contain nulls; cannot run audit for resourceName="
					+ resourceName + ", persistenceUnit=" + persistenceUnit
					+ ", bad parameters: " + parmList);
			throw new IntegrityAuditException(
					"Constructor: Parms contain nulls; cannot run audit for resourceName="
							+ resourceName + ", persistenceUnit="
							+ persistenceUnit
							+ ", bad parameters: " + parmList);
		}
		
		this.persistenceUnit = persistenceUnit;
		this.properties = properties;
		this.resourceName = resourceName;
		
		if(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS) != null){ //It is allowed to be null
			this.integrityAuditPeriodSeconds= Integer.parseInt(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS).trim());
		} else{
			//If it is null, set it to the default value
			this.integrityAuditPeriodSeconds = IntegrityAuditProperties.DEFAULT_AUDIT_PERIOD_SECONDS;
		}
		logger.info("Constructor: Exiting");
		
	}
	
	/**
	 * Used during JUnit testing by AuditPeriodTest.java
	 */
	public int getIntegrityAuditPeriodSeconds() {
		return integrityAuditPeriodSeconds;
	}

	/**
	 * Determine if the nodeType conforms to the required node types
	 */
	public static boolean isNodeTypeEnum(String nt) {
		for (NodeTypeEnum n : NodeTypeEnum.values()) {
			if (n.toString().equals(nt)) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Makes sure we don't try to run the audit with bad parameters.
	 */
	public static boolean parmsAreBad(String resourceName, String persistenceUnit,
			Properties properties, String badparams) {
		String localBadParams = badparams;
		boolean parmsAreBad = false;
		
		if(resourceName == null || resourceName.isEmpty()){
			localBadParams = localBadParams.concat("resourceName ");
			parmsAreBad = true;
		}
		
		if(persistenceUnit == null || persistenceUnit.isEmpty()){
			localBadParams = localBadParams.concat("persistenceUnit ");
			parmsAreBad = true;
		}
		
		String dbDriver = properties.getProperty(IntegrityAuditProperties.DB_DRIVER).trim();
		if(dbDriver == null || dbDriver.isEmpty()){
			localBadParams = localBadParams.concat("dbDriver ");
			parmsAreBad = true;
		}

		String dbUrl = properties.getProperty(IntegrityAuditProperties.DB_URL).trim();
		if(dbUrl == null || dbUrl.isEmpty()){
			localBadParams = localBadParams.concat("dbUrl ");
			parmsAreBad = true;
		}
		
		String dbUser = properties.getProperty(IntegrityAuditProperties.DB_USER).trim();
		if(dbUser == null || dbUser.isEmpty()){
			localBadParams = localBadParams.concat("dbUser ");
			parmsAreBad = true;
		}
		
		String dbPwd = properties.getProperty(IntegrityAuditProperties.DB_PWD).trim();
		if(dbPwd == null){ //may be empty
			localBadParams = localBadParams.concat("dbPwd ");
			parmsAreBad = true;
		}
		
		String siteName = properties.getProperty(IntegrityAuditProperties.SITE_NAME).trim();
		if(siteName == null || siteName.isEmpty()){
			localBadParams = localBadParams.concat("siteName ");
			parmsAreBad = true;
		}
		
		String nodeType = properties.getProperty(IntegrityAuditProperties.NODE_TYPE).trim();
		if(nodeType == null || nodeType.isEmpty()){
			localBadParams = localBadParams.concat("nodeType ");
			parmsAreBad = true;
		} else {
			if (!isNodeTypeEnum(nodeType)) {
				String nodetypes = "nodeType must be one of[";
				for (NodeTypeEnum n : NodeTypeEnum.values()) {
					nodetypes = nodetypes.concat(n.toString() + " ");
				}
				localBadParams = localBadParams.concat(nodetypes + "] ");
				parmsAreBad = true;
			}
		}
		if(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS) != null){ //It is allowed to be null
			try{
				Integer.parseInt(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS).trim());
			}catch(NumberFormatException nfe){
				localBadParams = localBadParams.concat(", auditPeriodSeconds=" 
						+ properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS).trim());
				parmsAreBad = true;
			}
		}
		logger.debug("parmsAreBad: exit:" 
				+ "\nresourceName: " + resourceName
				+ "\npersistenceUnit: " + persistenceUnit
				+ "\nproperties: " + properties);
		
		return parmsAreBad;
	}	
	/**
	 * Starts the audit thread
	 * @throws Exception
	 */
	public void startAuditThread() throws Exception {

		logger.info("startAuditThread: Entering");
		
		if (integrityAuditPeriodSeconds >= 0) {
			this.auditThread = new AuditThread(this.resourceName,
					this.persistenceUnit, this.properties,
					integrityAuditPeriodSeconds, this);
			logger.info("startAuditThread: Audit started and will run every "
					+ integrityAuditPeriodSeconds + " seconds");
			this.auditThread.start();
		} else {
			logger.info("startAuditThread: Suppressing integrity audit, integrityAuditPeriodSeconds="
					+ integrityAuditPeriodSeconds);
		}

		logger.info("startAuditThread: Exiting");
	}
	/**
	 * Stops the audit thread
	 */
	public void stopAuditThread() {

		logger.info("stopAuditThread: Entering");
		
		if (this.auditThread != null) {
			this.auditThread.interrupt();
		} else {
			logger.info("stopAuditThread: auditThread never instantiated; no need to interrupt");
		}
		
		logger.info("stopAuditThread: Exiting");
	}

	public boolean isThreadInitialized() {
		return isThreadInitialized;
	}

	public void setThreadInitialized(boolean isThreadInitialized) {
		logger.info("setThreadInitialized: Setting isThreadInitialized=" + isThreadInitialized);
		this.isThreadInitialized = isThreadInitialized;
	}

	public static boolean isUnitTesting() {
		return isUnitTesting;
	}

	public static void setUnitTesting(boolean isUnitTesting) {
		IntegrityAudit.isUnitTesting = isUnitTesting;
	}
}
