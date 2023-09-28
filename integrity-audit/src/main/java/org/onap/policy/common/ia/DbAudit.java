/*--
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
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

    private static final String COMMA_RESOURCE_NAME = ", resourceName=";

    private static final long DB_AUDIT_UPDATE_MS = 5000L;
    private static final long DB_AUDIT_SLEEP_MS = 2000L;

    DbDao dbDao = null;

    /**
     * Construct an instance with the given DbDao.
     *
     * @param dbDao the DbDao
     */
    public DbAudit(DbDao dbDao) {

        logger.debug("Constructor: Entering");

        this.dbDao = dbDao;

        logger.debug("Constructor: Exiting");
    }

    /**
     * dbAudit actually does the audit.
     *
     * @param resourceName the resource name
     * @param persistenceUnit the persistence unit
     * @param nodeType the node type
     * @throws IntegrityAuditException if an error occurs
     */
    public void dbAudit(String resourceName, String persistenceUnit, String nodeType) throws IntegrityAuditException {

        if (logger.isDebugEnabled()) {
            logger.debug("dbAudit: Entering, resourceName=" + resourceName + ", persistenceUnit=" + persistenceUnit
                    + ", nodeType=" + nodeType);
        }

        // Get all IntegrityAudit entries so we can get the DB access info
        List<IntegrityAuditEntity> iaeList = dbDao.getIntegrityAuditEntities(persistenceUnit, nodeType);
        if (iaeList == null || iaeList.isEmpty()) {

            String msg = "DbAudit: for node " + resourceName + " Found no IntegrityAuditEntity entries";
            logger.error(MessageCodes.ERROR_AUDIT, msg);
            throw new DbAuditException(msg);

        } else if (iaeList.size() == 1) {

            Long iaeId = null;
            String iaeRn = null;
            String iaeNt = null;
            String iaeS = null;
            for (IntegrityAuditEntity iae : iaeList) {
                iaeId = iae.getId();
                iaeRn = iae.getResourceName();
                iaeNt = iae.getNodeType();
                iaeS = iae.getSite();
            }
            String msg = "DbAudit: Found only one IntegrityAuditEntity entry:" + " ID = " + iaeId + " ResourceName = "
                    + iaeRn + " NodeType = " + iaeNt + " Site = " + iaeS;
            logger.warn(msg);
            return;
        }

        // Obtain all persistence class names for the PU we are auditing
        Set<String> classNameSet = dbDao.getPersistenceClassNames();
        if (classNameSet == null || classNameSet.isEmpty()) {

            String msg = "DbAudit: For node " + resourceName + " Found no persistence class names";
            logger.error(MessageCodes.ERROR_AUDIT, msg);
            throw new DbAuditException(msg);

        }

        /*
         * Retrieve myIae. We are going to compare the local class entries against all other DB
         * nodes. Since the audit is run in a round-robin, every instance will be compared against
         * every other instance.
         */
        var myIae = dbDao.getMyIntegrityAuditEntity();

        if (myIae == null) {

            String msg = "DbAudit: Found no IntegrityAuditEntity entry for resourceName: " + resourceName
                    + " persistenceUnit: " + persistenceUnit;
            logger.error(MessageCodes.ERROR_AUDIT, msg);
            throw new DbAuditException(msg);

        }
        /*
         * This is the map of mismatched entries indexed by className. For each class name there is
         * a list of mismatched entries
         */
        Map<String, Set<Object>> misMatchedMap = new HashMap<>();

        compareList(persistenceUnit, iaeList, myIae, classNameSet, misMatchedMap);

        // If misMatchedMap is not empty, retrieve the entries in each misMatched list and compare
        // again
        recompareList(resourceName, persistenceUnit, iaeList, myIae, misMatchedMap);

        if (logger.isDebugEnabled()) {
            logger.debug("dbAudit: Exiting");
        }
    }

    private void compareList(String persistenceUnit, List<IntegrityAuditEntity> iaeList, IntegrityAuditEntity myIae,
                    Set<String> classNameSet, Map<String, Set<Object>> misMatchedMap)
                    throws IntegrityAuditException {

        // We need to keep track of how long the audit is taking
        long startTime = AuditorTime.getInstance().getMillis();

        // Retrieve all instances of the class for each node
        if (logger.isDebugEnabled()) {
            logger.debug("dbAudit: Traversing classNameSet, size=" + classNameSet.size());
        }
        for (String clazzName : classNameSet) {

            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: clazzName=" + clazzName);
            }

            // all instances of the class for myIae
            Map<Object, Object> myEntries = dbDao.getAllMyEntries(clazzName);
            // get a map of the objects indexed by id. Does not necessarily have any entries

            compareMineWithTheirs(persistenceUnit, iaeList, myIae, misMatchedMap, clazzName, myEntries);

            // Time check
            startTime = timeCheck("First", startTime);
        }

        // check if misMatchedMap is empty
        if (misMatchedMap.isEmpty()) {

            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: Exiting, misMatchedMap is empty");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: Doing another comparison; misMatchedMap.size()=" + misMatchedMap.size());
            }
        }
    }

    private void compareMineWithTheirs(String persistenceUnit, List<IntegrityAuditEntity> iaeList,
                    IntegrityAuditEntity myIae, Map<String, Set<Object>> misMatchedMap, String clazzName,
                    Map<Object, Object> myEntries) {

        if (logger.isDebugEnabled()) {
            logger.debug("dbAudit: Traversing iaeList, size=" + iaeList.size());
        }
        for (IntegrityAuditEntity iae : iaeList) {
            if (iae.getId() == myIae.getId()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("dbAudit: My Id=" + iae.getId() + COMMA_RESOURCE_NAME + iae.getResourceName());
                }
                continue; // no need to compare with self
            }

            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: Id=" + iae.getId() + COMMA_RESOURCE_NAME + iae.getResourceName());
            }

            // get a map of the instances for their iae indexed by id
            Map<Object, Object> theirEntries =
                            dbDao.getAllEntries(persistenceUnit, getTheirDaoProperties(iae), clazzName);
            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: For persistenceUnit=" + persistenceUnit + ", clazzName=" + clazzName
                                + ", theirEntries.size()=" + theirEntries.size());
            }

            /*
             * Compare myEntries with theirEntries and get back a set of mismatched IDs.
             * Collect the IDs for the class where a mismatch occurred. We will check them
             * again for all nodes later.
             */
            compareMineWithTheirs(myEntries, theirEntries, clazzName, misMatchedMap);
        }
    }

    private void compareMineWithTheirs(Map<Object, Object> myEntries, Map<Object, Object> theirEntries,
                    String clazzName, Map<String, Set<Object>> misMatchedMap) {

        Set<Object> misMatchedKeySet = compareEntries(myEntries, theirEntries);
        if (misMatchedKeySet.isEmpty()) {
            return;
        }

        Set<Object> misMatchedEntry = misMatchedMap.get(clazzName);
        if (misMatchedEntry == null) {
            misMatchedMap.put(clazzName, misMatchedKeySet);
        } else {
            misMatchedEntry.addAll(misMatchedKeySet);
        }
    }

    private long timeCheck(String type, long startTime) throws IntegrityAuditException {
        if ((AuditorTime.getInstance().getMillis() - startTime) >= DB_AUDIT_UPDATE_MS) {
            // update the timestamp
            dbDao.setLastUpdated();
            // reset the startTime
            return AuditorTime.getInstance().getMillis();
        } else {
            // sleep a couple seconds to break up the activity
            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: " + type + " comparison; sleeping " + DB_AUDIT_SLEEP_MS + "ms");
            }
            sleep();
            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: " + type + " comparison; waking from sleep");
            }
            return startTime;
        }
    }

    /**
     * Creates properties for the other db node.
     * @param iae target DB node
     * @return DAO properties for the given DB node
     */
    private Properties getTheirDaoProperties(IntegrityAuditEntity iae) {
        var theirProperties = new Properties();

        theirProperties.put(IntegrityAuditProperties.DB_DRIVER, iae.getJdbcDriver());
        theirProperties.put(IntegrityAuditProperties.DB_URL, iae.getJdbcUrl());
        theirProperties.put(IntegrityAuditProperties.DB_USER, iae.getJdbcUser());
        theirProperties.put(IntegrityAuditProperties.DB_PWD, iae.getJdbcPassword());
        theirProperties.put(IntegrityAuditProperties.SITE_NAME, iae.getSite());
        theirProperties.put(IntegrityAuditProperties.NODE_TYPE, iae.getNodeType());

        return theirProperties;
    }

    private void recompareList(String resourceName, String persistenceUnit, List<IntegrityAuditEntity> iaeList,
                    IntegrityAuditEntity myIae, Map<String, Set<Object>> misMatchedMap)
                    throws IntegrityAuditException {

        Set<String> classNameSet;
        long startTime;
        classNameSet = new HashSet<>(misMatchedMap.keySet());
        // We need to keep track of how long the audit is taking
        startTime = AuditorTime.getInstance().getMillis();

        // Retrieve all instances of the class for each node
        if (logger.isDebugEnabled()) {
            logger.debug("dbAudit: Second comparison; traversing classNameSet, size=" + classNameSet.size());
        }

        var errorCount = 0;

        for (String clazzName : classNameSet) {

            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: Second comparison; clazzName=" + clazzName);
            }

            // all instances of the class for myIae
            Set<Object> keySet = misMatchedMap.get(clazzName);
            Map<Object, Object> myEntries = dbDao.getAllMyEntries(clazzName, keySet);
            // get a map of the objects indexed by id

            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: Second comparison; traversing iaeList, size=" + iaeList.size());
            }
            errorCount += recompareMineWithTheirs(resourceName, persistenceUnit, iaeList, myIae, clazzName,
                            keySet, myEntries);
            // Time check
            startTime = timeCheck("Second", startTime);
        }

        if (errorCount > 0) {
            String msg = " DB Audit: " + errorCount
                    + " errors found. A large number of errors may indicate DB replication has stopped";
            logger.error(MessageCodes.ERROR_AUDIT, msg);
        }
    }

    private int recompareMineWithTheirs(String resourceName, String persistenceUnit, List<IntegrityAuditEntity> iaeList,
                    IntegrityAuditEntity myIae, String clazzName, Set<Object> keySet, Map<Object, Object> myEntries)
                    throws IntegrityAuditException {

        var errorCount = 0;
        for (IntegrityAuditEntity iae : iaeList) {
            if (iae.getId() == myIae.getId()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("dbAudit: Second comparison; My Id=" + iae.getId() + COMMA_RESOURCE_NAME
                                    + iae.getResourceName());
                }
                continue; // no need to compare with self
            }

            if (logger.isDebugEnabled()) {
                logger.debug("dbAudit: Second comparison; Id=" + iae.getId() + COMMA_RESOURCE_NAME
                                + iae.getResourceName());
            }

            // get a map of the instances for their iae indexed by id
            Map<Object, Object> theirEntries =
                            dbDao.getAllEntries(persistenceUnit, getTheirDaoProperties(iae), clazzName, keySet);

            /*
             * Compare myEntries with theirEntries and get back a set of mismatched IDs.
             * Collect the IDs for the class where a mismatch occurred. We will now write
             * an error log for each.
             */
            errorCount += recompareMineWithTheirs(resourceName, clazzName, myEntries, iae, theirEntries);
        }
        return errorCount;
    }

    private int recompareMineWithTheirs(String resourceName, String clazzName, Map<Object, Object> myEntries,
                    IntegrityAuditEntity iae, Map<Object, Object> theirEntries) throws IntegrityAuditException {
        Set<Object> misMatchedKeySet = compareEntries(myEntries, theirEntries);
        if (misMatchedKeySet.isEmpty()) {
            return 0;
        }

        var keyBuilder = new StringBuilder();
        for (Object key : misMatchedKeySet) {
            keyBuilder.append(key.toString());
            keyBuilder.append(", ");
        }
        writeAuditSummaryLog(clazzName, resourceName, iae.getResourceName(), keyBuilder.toString());
        if (logger.isDebugEnabled()) {
            for (Object key : misMatchedKeySet) {
                writeAuditDebugLog(clazzName, resourceName, iae.getResourceName(), myEntries.get(key),
                                theirEntries.get(key));
            }
        }
        return misMatchedKeySet.size();
    }

    /**
     * Sleeps a bit.
     *
     * @throws IntegrityAuditException if interrupted
     */
    private void sleep() throws IntegrityAuditException {
        try {
            AuditorTime.getInstance().sleep(DB_AUDIT_SLEEP_MS);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IntegrityAuditException(e);
        }
    }

    /**
     * compareEntries() will compare the lists of entries from the DB.
     *
     * @param myEntries the entries
     * @param theirEntries the entries to compare against myEntries
     * @return the set of differences
     */
    public Set<Object> compareEntries(Map<Object, Object> myEntries, Map<Object, Object> theirEntries) {
        /*
         * Compare the entries for the same key in each of the hashmaps. The comparison will be done
         * by serializing the objects (create a byte array) and then do a byte array comparison. The
         * audit will walk the local repository hash map comparing to the remote cluster hashmap and
         * then turn it around and walk the remote hashmap and look for any entries that are not
         * present in the local cluster hashmap.
         *
         * If the objects are not identical, the audit will put the object IDs on a list to try
         * after completing the audit of the table it is currently working on.
         *
         */
        HashSet<Object> misMatchedKeySet = new HashSet<>();
        for (Entry<Object, Object> ent : myEntries.entrySet()) {
            Object key = ent.getKey();
            byte[] mySerializedEntry = SerializationUtils.serialize((Serializable) ent.getValue());
            byte[] theirSerializedEntry = SerializationUtils.serialize((Serializable) theirEntries.get(key));
            if (!Arrays.equals(mySerializedEntry, theirSerializedEntry)) {
                logger.debug("compareEntries: For myEntries.key=" + key + ", entries do not match");
                misMatchedKeySet.add(key);
            } else {
                logger.debug("compareEntries: For myEntries.key=" + key + ", entries match");
            }
        }
        // now compare it in the other direction to catch entries in their set that is not in my set
        for (Entry<Object, Object> ent : theirEntries.entrySet()) {
            Object key = ent.getKey();
            byte[] mySerializedEntry = SerializationUtils.serialize((Serializable) myEntries.get(key));
            byte[] theirSerializedEntry = SerializationUtils.serialize((Serializable) ent.getValue());
            if (!Arrays.equals(mySerializedEntry, theirSerializedEntry)) {
                logger.debug("compareEntries: For theirEntries.key=" + key + ", entries do not match");
                misMatchedKeySet.add(key);
            } else {
                logger.debug("compareEntries: For theirEntries.key=" + key + ", entries match");
            }
        }

        // return a Set of the object IDs
        logger.debug("compareEntries: misMatchedKeySet.size()=" + misMatchedKeySet.size());
        return misMatchedKeySet;
    }

    /**
     * writeAuditDebugLog() writes the mismatched entry details to the debug log.
     *
     * @param clazzName the class name
     * @param resourceName1 resource name 1
     * @param resourceName2 resource name 2
     * @param entry1 entry 1
     * @param entry2 entry 2
     * @throws IntegrityAuditException if the given class cannot be found
     */
    public void writeAuditDebugLog(String clazzName, String resourceName1, String resourceName2, Object entry1,
            Object entry2) throws IntegrityAuditException {
        try {
            Class<?> entityClass = Class.forName(clazzName);
            String tableName = entityClass.getAnnotation(Table.class).name();
            String msg = "\nDB Audit Error: " + "\n    Table Name: " + tableName
                    + "\n    Entry 1 (short prefix style): " + resourceName1 + ": "
                    + new ReflectionToStringBuilder(entry1, ToStringStyle.SHORT_PREFIX_STYLE)
                    + "\n    Entry 2 (short prefix style): " + resourceName2 + ": "
                    + (entry2 != null
                        ? new ReflectionToStringBuilder(entry2, ToStringStyle.SHORT_PREFIX_STYLE).toString()
                        : "null")
                    + "\n    Entry 1 (recursive style): " + resourceName1 + ": "
                    + new ReflectionToStringBuilder(entry1, new RecursiveToStringStyle())
                    + "\n    Entry 2 (recursive style): " + resourceName2 + ": "
                    + (entry2 != null
                        ? new ReflectionToStringBuilder(entry2, new RecursiveToStringStyle()).toString()
                        : "null");
            logger.debug(msg);

        } catch (ClassNotFoundException e) {
            throw new IntegrityAuditException(e);
        }

    }

    /**
     * writeAuditSummaryLog() writes a summary of the DB mismatches to the error log.
     *
     * @param clazzName the name of the class
     * @param resourceName1 resource name 1
     * @param resourceName2 resource name 2
     * @param keys the mismatched entry keys
     * @throws IntegrityAuditException if the given class cannot be found
     */
    public void writeAuditSummaryLog(String clazzName, String resourceName1, String resourceName2, String keys)
            throws IntegrityAuditException {
        try {
            Class<?> entityClass = Class.forName(clazzName);
            String tableName = entityClass.getAnnotation(Table.class).name();
            String msg = " DB Audit Error: Table Name: " + tableName + ";  Mismatch between nodes: " + resourceName1
                    + " and " + resourceName2 + ";  Mismatched entries (keys): " + keys;
            logger.info(msg);
        } catch (ClassNotFoundException e) {
            throw new IntegrityAuditException(e);
        }
    }
}
