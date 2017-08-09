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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.persistence.Table;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;





import org.onap.policy.common.ia.jpa.IntegrityAuditEntity;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.flexlogger.FlexLogger; 
import org.onap.policy.common.logging.flexlogger.Logger;

/**
 * class DbAudit does actual auditing of DB tables.
 */
public class DbAudit {
	
	private static final Logger logger = FlexLogger.getLogger(DbAudit.class);
	
	DbDAO dbDAO = null;
	
	public DbAudit(DbDAO dbDAO) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Constructor: Entering");
		}

		this.dbDAO = dbDAO;
		
		if (logger.isDebugEnabled()) {
			logger.debug("Constructor: Exiting");
		}

	}
	
	/**
	 * dbAudit actually does the audit
	 * @param resourceName
	 * @param persistenceUnit
	 * @param nodeType
	 * @throws Exception
	 */
	public void dbAudit(String resourceName, String persistenceUnit, String nodeType) throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug("dbAudit: Entering, resourceName=" + resourceName
					+ ", persistenceUnit=" + persistenceUnit + ", nodeType="
					+ nodeType);
		}
	
		// Get all IntegrityAudit entries so we can get the DB access info
		List<IntegrityAuditEntity> iaeList = dbDAO.getIntegrityAuditEntities(persistenceUnit, nodeType);
		if(iaeList == null || iaeList.isEmpty()){
			
			String msg = "DbAudit: for node " + resourceName + " Found no IntegrityAuditEntity entries";
			logger.error(MessageCodes.ERROR_AUDIT,  msg);
			throw new DbAuditException(msg);
			
		}else if(iaeList.size() == 1){
			
			Long iaeId = null;
			String iaeRN = null;
			String iaeNT = null;
			String iaeS = null;
			for (IntegrityAuditEntity iae : iaeList){
				iaeId = iae.getId();
				iaeRN = iae.getResourceName();
				iaeNT = iae.getNodeType();
				iaeS = iae.getSite();
			}
			String msg = "DbAudit: Found only one IntegrityAuditEntity entry:"
					+ " ID = " + iaeId
					+ " ResourceName = " + iaeRN
					+ " NodeType = " + iaeNT
					+ " Site = " + iaeS;
			logger.warn(msg);
			return;
		}
		
		// Obtain all persistence class names for the PU we are auditing
		HashSet<String> classNameSet = dbDAO.getPersistenceClassNames();
		if(classNameSet == null || classNameSet.isEmpty()){
			
			String msg = "DbAudit: For node " + resourceName + " Found no persistence class names";
			logger.error(MessageCodes.ERROR_AUDIT,  msg);
			throw new DbAuditException(msg);
			
		}
		
		/*
		 * Retrieve myIae.  We are going to compare the local class entries against
		 * all other DB nodes. Since the audit is run in a round-robin, every instance
		 * will be compared against every other instance.
		 */
		IntegrityAuditEntity myIae = dbDAO.getMyIntegrityAuditEntity();

		if(myIae == null){
			
			String msg = "DbAudit: Found no IntegrityAuditEntity entry for resourceName: " + resourceName
					+ " persistenceUnit: " + persistenceUnit;
			logger.error(MessageCodes.ERROR_AUDIT,  msg);
			throw new DbAuditException(msg);
			
		}
		/*
		 * This is the map of mismatched entries indexed by className. For
		 * each class name there is a list of mismatched entries
		 */
		HashMap<String,HashSet<Object>> misMatchedMap = new HashMap<>();
		
		// We need to keep track of how long the audit is taking
		long startTime = System.currentTimeMillis();
		
		// Retrieve all instances of the class for each node			
		if (logger.isDebugEnabled()) {
			logger.debug("dbAudit: Traversing classNameSet, size=" + classNameSet.size());
		}
		for(String clazzName: classNameSet){
			
			if (logger.isDebugEnabled()) {
				logger.debug("dbAudit: clazzName=" + clazzName);
			}
	
			// all instances of the class for myIae
			HashMap<Object,Object> myEntries = dbDAO.getAllMyEntries(clazzName);
			//get a map of the objects indexed by id. Does not necessarily have any entries
			
			if (logger.isDebugEnabled()) {
				logger.debug("dbAudit: Traversing iaeList, size=" + iaeList.size());
			}
			for (IntegrityAuditEntity iae : iaeList){
				if(iae.getId() == myIae.getId()){
					if (logger.isDebugEnabled()) {
						logger.debug("dbAudit: My Id=" + iae.getId()
								+ ", resourceName=" + iae.getResourceName());
					}
					continue; //no need to compare with self
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("dbAudit: Id=" + iae.getId()
								+ ", resourceName=" + iae.getResourceName());
					}
				}
				// Create properties for the other db node
				Properties theirProperties = new Properties();
				theirProperties.put(IntegrityAuditProperties.DB_DRIVER, iae.getJdbcDriver());
				theirProperties.put(IntegrityAuditProperties.DB_URL, iae.getJdbcUrl());
				theirProperties.put(IntegrityAuditProperties.DB_USER, iae.getJdbcUser());
				theirProperties.put(IntegrityAuditProperties.DB_PWD, iae.getJdbcPassword());
				theirProperties.put(IntegrityAuditProperties.SITE_NAME, iae.getSite());
				theirProperties.put(IntegrityAuditProperties.NODE_TYPE, iae.getNodeType());
				
				//get a map of the instances for their iae indexed by id
				HashMap<Object,Object> theirEntries = dbDAO.getAllEntries(persistenceUnit, theirProperties, clazzName);
				if (logger.isDebugEnabled()) {
					logger.debug("dbAudit: For persistenceUnit="
							+ persistenceUnit + ", clazzName=" + clazzName
							+ ", theirEntries.size()="
							+ theirEntries.size());
				}
				
				/*
				 * Compare myEntries with theirEntries and get back a set of mismatched IDs.
				 * Collect the IDs for the class where a mismatch occurred.  We will check
				 * them again for all nodes later.
				 */
				HashSet<Object> misMatchedKeySet = compareEntries(myEntries, theirEntries);
				if(!misMatchedKeySet.isEmpty()){
					HashSet<Object> misMatchedEntry = misMatchedMap.get(clazzName);
					if(misMatchedEntry == null){
						misMatchedMap.put(clazzName, misMatchedKeySet);
					}else{
						misMatchedEntry.addAll(misMatchedKeySet);
						misMatchedMap.put(clazzName, misMatchedEntry);
					}
				}
			} //end for (IntegrityAuditEntity iae : iaeList)
			//Time check
			if((System.currentTimeMillis() - startTime) >= 5000){ //5 seconds
				//update the timestamp
				dbDAO.setLastUpdated();
				//reset the startTime
				startTime=System.currentTimeMillis();
			}else{
				//sleep a couple seconds to break up the activity
				if (logger.isDebugEnabled()) {
					logger.debug("dbAudit: Sleeping 2 seconds");
				}
				Thread.sleep(2000);
				if (logger.isDebugEnabled()) {
					logger.debug("dbAudit: Waking from sleep");
				}
			}
		}//end: for(String clazzName: classNameList)
		
		//check if misMatchedMap is empty
		if(misMatchedMap.isEmpty()){
			
			if (logger.isDebugEnabled()) {
				logger.debug("dbAudit: Exiting, misMatchedMap is empty");
			}
			//we are done
			return;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("dbAudit: Doing another comparison; misMatchedMap.size()=" + misMatchedMap.size());
			}
		}
		
		// If misMatchedMap is not empty, retrieve the entries in each misMatched list and compare again
		classNameSet = new HashSet<String>(misMatchedMap.keySet());
		// We need to keep track of how long the audit is taking
		startTime = System.currentTimeMillis();
		
		// Retrieve all instances of the class for each node			
		if (logger.isDebugEnabled()) {
			logger.debug("dbAudit: Second comparison; traversing classNameSet, size=" + classNameSet.size());
		}
		
		int errorCount = 0;
		
		for(String clazzName: classNameSet){
			
			if (logger.isDebugEnabled()) {
				logger.debug("dbAudit: Second comparison; clazzName=" + clazzName);
			}
	
			// all instances of the class for myIae
			HashSet<Object> keySet = misMatchedMap.get(clazzName);
			HashMap<Object,Object> myEntries = dbDAO.getAllMyEntries(clazzName, keySet);
			//get a map of the objects indexed by id
			
			if (logger.isDebugEnabled()) {
				logger.debug("dbAudit: Second comparison; traversing iaeList, size=" + iaeList.size());
			}
			for (IntegrityAuditEntity iae : iaeList){
				if(iae.getId() == myIae.getId()){
					if (logger.isDebugEnabled()) {
						logger.debug("dbAudit: Second comparison; My Id=" + iae.getId()
								+ ", resourceName=" + iae.getResourceName());
					}
					continue; //no need to compare with self
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("dbAudit: Second comparison; Id=" + iae.getId()
								+ ", resourceName=" + iae.getResourceName());
					}
				}
				// Create properties for the other db node
				Properties theirProperties = new Properties();
				theirProperties.put(IntegrityAuditProperties.DB_DRIVER, iae.getJdbcDriver());
				theirProperties.put(IntegrityAuditProperties.DB_URL, iae.getJdbcUrl());
				theirProperties.put(IntegrityAuditProperties.DB_USER, iae.getJdbcUser());
				theirProperties.put(IntegrityAuditProperties.DB_PWD, iae.getJdbcPassword());
				theirProperties.put(IntegrityAuditProperties.SITE_NAME, iae.getSite());
				theirProperties.put(IntegrityAuditProperties.NODE_TYPE, iae.getNodeType());
				
				//get a map of the instances for their iae indexed by id
				HashMap<Object,Object> theirEntries = dbDAO.getAllEntries(persistenceUnit, theirProperties, clazzName, keySet);
				
				/*
				 * Compare myEntries with theirEntries and get back a set of mismatched IDs.
				 * Collect the IDs for the class where a mismatch occurred.  We will now
				 * write an error log for each.
				 */
				HashSet<Object> misMatchedKeySet = compareEntries(myEntries, theirEntries);
				if(!misMatchedKeySet.isEmpty()){
					String keysString = "";
					for(Object key: misMatchedKeySet){
						keysString = keysString.concat(key.toString() + ", ");
						errorCount ++;
					}
					writeAuditSummaryLog(clazzName, resourceName, iae.getResourceName(), keysString);
					if(logger.isDebugEnabled()){
						for(Object key : misMatchedKeySet){
							writeAuditDebugLog(clazzName, resourceName, iae.getResourceName(), myEntries.get(key), theirEntries.get(key));
						}
					}
				}
			}
			//Time check
			if((System.currentTimeMillis() - startTime) >= 5000){ //5 seconds
				//update the timestamp
				dbDAO.setLastUpdated();
				//reset the startTime
				startTime=System.currentTimeMillis();
			}else{
				//sleep a couple seconds to break up the activity
				if (logger.isDebugEnabled()) {
					logger.debug("dbAudit: Second comparison; sleeping 2 seconds");
				}
				Thread.sleep(2000);
				if (logger.isDebugEnabled()) {
					logger.debug("dbAudit: Second comparison; waking from sleep");
				}
			}
		}//end: for(String clazzName: classNameList)
		
		if(errorCount != 0){
			String msg = " DB Audit: " + errorCount + " errors found. A large number of errors may indicate DB replication has stopped";
			logger.error(MessageCodes.ERROR_AUDIT,  msg);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("dbAudit: Exiting");
		}
	
		return; //all done
	}

	/**
	 * dbAuditSimulate simulates the DB audit
	 * @param resourceName
	 * @param persistenceUnit
	 * @param nodeType
	 * @throws InterruptedException
	 * @throws DbDaoTransactionException
	 */
	public void dbAuditSimulate(String resourceName, String persistenceUnit) throws InterruptedException,
			DbDaoTransactionException {

		logger.info("dbAuditSimulate: Starting audit simulation for resourceName="
				+ resourceName + ", persistenceUnit=" + persistenceUnit);

		for (int i = 0; i < AuditThread.AUDIT_SIMULATION_ITERATIONS; i++) {
			dbDAO.setLastUpdated();
			logger.info("dbAuditSimulate: i=" + i + ", sleeping "
					+ AuditThread.AUDIT_SIMULATION_SLEEP_INTERVAL + "ms");
			Thread.sleep(AuditThread.AUDIT_SIMULATION_SLEEP_INTERVAL);
		}

		logger.info("dbAuditSimulate: Finished audit simulation for resourceName="
				+ resourceName + ", persistenceUnit=" + persistenceUnit);

	}
	
	/**
	 * compareEntries() will compare the lists of entries from the DB
	 * @param myEntries
	 * @param theirEntries
	 * @return
	 */
	public HashSet<Object> compareEntries(HashMap<Object,Object> myEntries, HashMap<Object,Object> theirEntries){
		/*
		 * Compare the entries for the same key in each of the hashmaps.  The comparison will be done by serializing the objects 
		 * (create a byte array) and then do a byte array comparison.  The audit will walk the local repository hash map comparing 
		 * to the remote cluster hashmap and then turn it around and walk the remote hashmap and look for any entries that are not 
		 * present in the local cluster hashmap. 
		 * 
		 * If the objects are not identical, the audit will put the object IDs on a list to try after completing the audit of the table 
		 * it is currently working on.
		 * 
		 */
		HashSet<Object> misMatchedKeySet = new HashSet<>();
		for(Object key: myEntries.keySet()){
			byte[] mySerializedEntry = SerializationUtils.serialize((Serializable) myEntries.get(key));
			byte[] theirSerializedEntry = SerializationUtils.serialize((Serializable) theirEntries.get(key));
			if(!Arrays.equals(mySerializedEntry, theirSerializedEntry)){
				logger.debug("compareEntries: For myEntries.key=" + key + ", entries do not match");
				misMatchedKeySet.add(key);
			} else {
				logger.debug("compareEntries: For myEntries.key=" + key + ", entries match");
			}
		}
		//now compare it in the other direction to catch entries in their set that is not in my set
		for(Object key: theirEntries.keySet()){
			byte[] mySerializedEntry = SerializationUtils.serialize((Serializable) myEntries.get(key));
			byte[] theirSerializedEntry = SerializationUtils.serialize((Serializable) theirEntries.get(key));
			if(!Arrays.equals(mySerializedEntry, theirSerializedEntry)){
				logger.debug("compareEntries: For theirEntries.key=" + key + ", entries do not match");
				misMatchedKeySet.add(key);
			} else {
				logger.debug("compareEntries: For theirEntries.key=" + key + ", entries match");
			}
		}
		
		//return a Set of the object IDs
		logger.debug("compareEntries: misMatchedKeySet.size()=" + misMatchedKeySet.size());
		return misMatchedKeySet;
	}
	
	/**
	 * writeAuditDebugLog() writes the mismatched entry details to the debug log
	 * @param clazzName
	 * @param resourceName1
	 * @param resourceName2
	 * @param entry1
	 * @param entry2
	 * @throws ClassNotFoundException
	 */
	public void writeAuditDebugLog(String clazzName, String resourceName1,
			String resourceName2, Object entry1, Object entry2) throws ClassNotFoundException{
		Class<?> entityClass = Class.forName(clazzName);
		String tableName = entityClass.getAnnotation(Table.class).name();
		String msg = "\nDB Audit Error: "
				+ "\n    Table Name: " + tableName
				+ "\n    Entry 1 (short prefix style): " + resourceName1 + ": " + new ReflectionToStringBuilder(entry1,ToStringStyle.SHORT_PREFIX_STYLE).toString()
				+ "\n    Entry 2 (short prefix style): " + resourceName2 + ": " + new ReflectionToStringBuilder(entry2,ToStringStyle.SHORT_PREFIX_STYLE).toString()
				+ "\n    Entry 1 (recursive style): " + resourceName1 + ": " + new ReflectionToStringBuilder(entry1, new RecursiveToStringStyle()).toString()
				+ "\n    Entry 2 (recursive style): " + resourceName2 + ": " + new ReflectionToStringBuilder(entry2, new RecursiveToStringStyle()).toString();
		logger.debug(msg);
		
	}
	
	/**
	 * writeAuditSummaryLog() writes a summary of the DB mismatches to the error log
	 * @param clazzName
	 * @param resourceName1
	 * @param resourceName2
	 * @param keys
	 * @throws ClassNotFoundException
	 */
	public void writeAuditSummaryLog(String clazzName, String resourceName1, 
			String resourceName2, String keys) throws ClassNotFoundException{
		Class<?> entityClass = Class.forName(clazzName);
		String tableName = entityClass.getAnnotation(Table.class).name();
		String msg = " DB Audit Error: Table Name: " + tableName
				+ ";  Mismatch between nodes: " + resourceName1 +" and " + resourceName2
				+ ";  Mismatched entries (keys): " + keys;
		logger.info(msg);
	}



	

}
