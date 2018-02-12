/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
	 * This is the audit period in milliseconds. For example, if it had a value of 3600000, the audit
	 * can only run once per hour.  If it has a value of 6000, it can run once per minute.
	 * 
	 * Values: 
	 *    integrityAuditPeriodMillis < 0 (negative number) indicates the audit is off
	 *    integrityAuditPeriodMillis == 0 indicates the audit is to run continuously
	 *    integrityAuditPeriodMillis > 0 indicates the audit is to run at most once during the indicated period
	 * 
	 */
	private int integrityAuditPeriodMillis;
	
	/**
	 * IntegrityAudit constructor
	 * @param resourceName
	 * @param persistenceUnit
	 * @param properties
	 * @throws Exception
	 */
	public IntegrityAudit(String resourceName, String persistenceUnit, Properties properties) throws IntegrityAuditException {
		
		logger.info("Constructor: Entering and checking for nulls");
		StringBuilder parmList = new StringBuilder();
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
			this.integrityAuditPeriodMillis= 1000 * Integer.parseInt(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS).trim());
		} else if(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_MILLISECONDS) != null){ //It is allowed to be null
				this.integrityAuditPeriodMillis= Integer.parseInt(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_MILLISECONDS).trim());
		} else{
			//If it is null, set it to the default value
			this.integrityAuditPeriodMillis = 1000 * IntegrityAuditProperties.DEFAULT_AUDIT_PERIOD_SECONDS;
		}
		logger.info("Constructor: Exiting");
		
	}
	
	/**
	 * Used during JUnit testing by AuditPeriodTest.java
	 */
	public int getIntegrityAuditPeriodSeconds() {
		return (integrityAuditPeriodMillis / 1000);
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
			Properties properties, StringBuilder badparams) {

		boolean parmsAreBad = false;
		
		if(resourceName == null || resourceName.isEmpty()){
			badparams.append("resourceName ");
			parmsAreBad = true;
		}
		
		if(persistenceUnit == null || persistenceUnit.isEmpty()){
			badparams.append("persistenceUnit ");
			parmsAreBad = true;
		}
		
		if(properties == null || properties.isEmpty()){
			badparams.append("properties ");
			parmsAreBad = true;
		}
		else{
			String dbDriver = properties.getProperty(IntegrityAuditProperties.DB_DRIVER);
			if(dbDriver == null || dbDriver.isEmpty()){
				badparams.append("dbDriver ");
				parmsAreBad = true;
			}
	
			String dbUrl = properties.getProperty(IntegrityAuditProperties.DB_URL);
			if(dbUrl == null || dbUrl.isEmpty()){
				badparams.append("dbUrl ");
				parmsAreBad = true;
			}
			
			String dbUser = properties.getProperty(IntegrityAuditProperties.DB_USER);
			if(dbUser == null || dbUser.isEmpty()){
				badparams.append("dbUser ");
				parmsAreBad = true;
			}
			
			String dbPwd = properties.getProperty(IntegrityAuditProperties.DB_PWD);
			if(dbPwd == null){ //may be empty
				badparams.append("dbPwd ");
				parmsAreBad = true;
			}
			
			String siteName = properties.getProperty(IntegrityAuditProperties.SITE_NAME);
			if(siteName == null || siteName.isEmpty()){
				badparams.append("siteName ");
				parmsAreBad = true;
			}
			
			String nodeType = properties.getProperty(IntegrityAuditProperties.NODE_TYPE);
			if(nodeType == null || nodeType.isEmpty()){
				badparams.append("nodeType ");
				parmsAreBad = true;
			} else {
				nodeType = nodeType.trim();
				if (!isNodeTypeEnum(nodeType)) {
					String nodetypes = "nodeType must be one of[";
					for (NodeTypeEnum n : NodeTypeEnum.values()) {
						nodetypes = nodetypes.concat(n.toString() + " ");
					}
					badparams.append(nodetypes + "] ");
					parmsAreBad = true;
				}
			}
			if(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS) != null){ //It is allowed to be null
				try{
					Integer.parseInt(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS).trim());
				}catch(NumberFormatException nfe){
					badparams.append(", auditPeriodSeconds=" 
							+ properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS).trim());
					parmsAreBad = true;
				}
			}
			else if(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_MILLISECONDS) != null){ //It is allowed to be null
				try{
					Integer.parseInt(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_MILLISECONDS).trim());
				}catch(NumberFormatException nfe){
					badparams.append(", auditPeriodMilliSeconds=" 
							+ properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_MILLISECONDS).trim());
					parmsAreBad = true;
				}
			}
		} // End else
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
		startAuditThread(null);
	}
	/**
	 * Starts the audit thread
	 * @param queue 
	 * @return {@code true} if the thread was started, {@code false} otherwise
	 * @throws Exception
	 */
	protected boolean startAuditThread(BlockingQueue<CountDownLatch> queue) throws Exception {

		logger.info("startAuditThread: Entering");
		
		boolean success = false;
		
		if (integrityAuditPeriodMillis >= 0) {
			this.auditThread = new AuditThread(this.resourceName,
					this.persistenceUnit, this.properties,
					integrityAuditPeriodMillis, TimeUnit.MILLISECONDS, this, queue);
			logger.info("startAuditThread: Audit started and will run every "
					+ integrityAuditPeriodMillis/1000 + " seconds");
			this.auditThread.start();
			success = true;
		} else {
			logger.info("startAuditThread: Suppressing integrity audit, integrityAuditPeriodSeconds="
					+ integrityAuditPeriodMillis/1000);
		}

		logger.info("startAuditThread: Exiting");
		
		return success;
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

	/**
	 * Waits a bit for the AuditThread to complete. Used by JUnit tests.
	 * 
	 * @param twaitms
	 *            wait time, in milliseconds
	 * @return {@code true} if the thread stopped within the given time,
	 *         {@code false} otherwise
	 * @throws InterruptedException
	 */
	protected boolean joinAuditThread(long twaitms) throws InterruptedException {
		if(this.auditThread == null) {
			return true;
			
		} else {
			this.auditThread.join(twaitms);
			return ! this.auditThread.isAlive();
		}
	}
}
