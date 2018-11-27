/*
 * ============LICENSE_START=======================================================
 * Integrity Monitor
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

package org.onap.policy.common.im;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.onap.policy.common.im.jmx.ComponentAdmin;
import org.onap.policy.common.im.jmx.ComponentAdminMBean;
import org.onap.policy.common.im.jmx.JmxAgentConnection;
import org.onap.policy.common.im.jpa.ForwardProgressEntity;
import org.onap.policy.common.im.jpa.ResourceRegistrationEntity;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntegrityMonitor Main class for monitoring the integrity of a resource and managing its state. State management
 * follows the X.731 ITU standard.
 */
public class IntegrityMonitor {

    private static final Logger logger = LoggerFactory.getLogger(IntegrityMonitor.class.getName());

    // only allow one instance of IntegrityMonitor
    private static IntegrityMonitor instance = null;

    /*
     * Common strings
     */
    private static final String NULL_PROPERTY_STRING = " property is null";
    private static final String IGNORE_INVALID_PROPERTY_STRING = "Ignored invalid property: {}";
    private static final String PROPERTY_EXCEPTION_STRING = "IntegrityMonitor Property Exception: ";
    private static final String EXCEPTION_STRING = "IntegrityMonitor threw exception.";
    private static final String STATE_CHECK_STRING = "IntegrityMonitor.stateCheck(): "
            + "Failed to disableFail dependent resource = ";
    private static final String RESOURCE_STRING = "Resource ";
    private static final String LC_RESOURCE_STRING = "resource";

    /*
     * Query String
     */
    private static final String QUERY_STRING = "Select f from ForwardProgressEntity f where f.resourceName=:rn";

    private static String resourceName = null;
    boolean alarmExists = false;

    /*
     * Error message that is written by the dependencyCheck() method. It is made available
     * externally through the evaluateSanity() method.
     */
    private String dependencyCheckErrorMsg = "";

    // The entity manager factory for JPA access
    private EntityManagerFactory emf;
    private EntityManager em;

    // Persistence Unit for JPA
    public static final String PERSISTENCE_UNIT = "operationalPU";

    public static final long CYCLE_INTERVAL_MILLIS = 1000L;

    private StateManagement stateManager = null;

    private FpManager fpManager = null;

    // The forward progress counter is incremented as the
    // process being monitored makes forward progress
    private int fpCounter = 0;
    private int lastFpCounter = 0;

    // elapsed time since last FP counter check
    private long elapsedTime = 0;

    // elapsed time since last test transaction check
    private long elapsedTestTransTime = 0;

    // elapsed time since last write Fpc check
    private long elapsedWriteFpcTime = 0;

    // last dependency health check time. Initialize so that the periodic check
    // starts after 60 seconds.
    // This allows time for dependents to come up.
    private long lastDependencyCheckTime = MonitorTime.getInstance().getMillis();

    // Time of the last state audit. It is initialized at the time of the IM
    // construction
    private Date lastStateAuditTime = MonitorTime.getInstance().getDate();

    // Interval between state audits in ms. We leave it turned off by default so
    // that it will only
    // be run on the nodes which we want doing the audit. In particular, we only
    // want it to run
    // on the droolspdps
    private static long stateAuditIntervalMs = 0L;

    // the number of cycles since 'fpCounter' was last changed
    private int missedCycles = 0;

    // forward progress monitoring interval
    private static long monitorIntervalMs = toMillis(IntegrityMonitorProperties.DEFAULT_MONITOR_INTERVAL);
    // The number of periods the counter fails to increment before an alarm is
    // raised.
    private static int failedCounterThreshold = IntegrityMonitorProperties.DEFAULT_FAILED_COUNTER_THRESHOLD;
    // test transaction interval
    private static long testTransIntervalMs = toMillis(IntegrityMonitorProperties.DEFAULT_TEST_INTERVAL);
    // write Fpc to DB interval
    private static long writeFpcIntervalMs = toMillis(IntegrityMonitorProperties.DEFAULT_WRITE_FPC_INTERVAL);
    // check the health of dependencies
    private static long checkDependencyIntervalMs =
            toMillis(IntegrityMonitorProperties.DEFAULT_CHECK_DEPENDENCY_INTERVAL);

    // A lead subsystem will have dependency groups with resource names in the
    // properties file.
    // For non-lead subsystems, the dependency_group property will be absent.
    private static String[] depGroups = null;

    private static boolean isUnitTesting = false;

    // can turn on health checking of dependents via jmx test() call by setting
    // this property to true
    private static boolean testViaJmx = false;

    private static String jmxFqdn = null;

    // this is the max interval allowed without any forward progress
    // counter updates
    private static long maxFpcUpdateIntervalMs = toMillis(IntegrityMonitorProperties.DEFAULT_MAX_FPC_UPDATE_INTERVAL);

    // Node types
    private enum NodeType {
        PDP_XACML, PDP_DROOLS, PAP, PAP_ADMIN, LOGPARSER, BRMS_GATEWAY, ASTRA_GATEWAY, ELK_SERVER, PYPDP

    }

    private static String siteName;
    private static String nodeType;
    private Date refreshStateAuditLastRunDate;
    private static long refreshStateAuditIntervalMs = 600000; // run it once per 10 minutes

    // lock objects
    private final Object evaluateSanityLock = new Object();
    private final Object fpMonitorCycleLock = new Object();
    private final Object dependencyCheckLock = new Object();
    private final Object testTransactionLock = new Object();
    private final Object startTransactionLock = new Object();
    private final Object endTransactionLock = new Object();
    private final Object checkTestTransactionLock = new Object();
    private final Object checkWriteFpcLock = new Object();
    private static final Object getInstanceLock = new Object();
    private final Object refreshStateAuditLock = new Object();
    private final Object imFlushLock = new Object();

    private Map<String, String> allSeemsWellMap;
    private Map<String, String> allNotWellMap;

    /**
     * IntegrityMonitor constructor. It is invoked from the getInstance() method in this class or from the constructor
     * of a child or sub-class. A class can extend the IntegrityMonitor class if there is a need to override any of the
     * base methods (ex. subsystemTest()). Only one instance is allowed to be created per resource name.
     *
     * @param resourceName The resource name of the resource
     * @param properties a set of properties passed in from the resource
     * @throws IntegrityMonitorException if any errors are encountered in the constructor
     */
    protected IntegrityMonitor(String resourceName, Properties properties) throws IntegrityMonitorException {

        // singleton check since this constructor can be called from a child or
        // sub-class
        if (instance != null) {
            String msg = "IM object exists and only one instance allowed";
            logger.error("{}", msg);
            throw new IntegrityMonitorException("IntegrityMonitor constructor exception: " + msg);
        }
        instance = this;

        IntegrityMonitor.resourceName = resourceName;

        /*
         * Validate that the properties file contains all the needed properties. Throws an
         * IntegrityMonitorPropertiesException
         */
        validateProperties(properties);

        // construct jmx url
        String jmxUrl = getJmxUrl();

        //
        // Create the entity manager factory
        //
        emf = Persistence.createEntityManagerFactory(getPersistenceUnit(), properties);
        //
        // Did it get created?
        //
        if (emf == null) {
            logger.error("Error creating IM entity manager factory with persistence unit: {}", 
                            getPersistenceUnit());
            throw new IntegrityMonitorException("Unable to create IM Entity Manager Factory");
        }

        // add entry to forward progress and resource registration tables in DB

        // Start a transaction
        em = emf.createEntityManager();
        EntityTransaction et = em.getTransaction();

        et.begin();

        try {
            // if ForwardProgress entry exists for resourceName, update it. If
            // not found, create a new entry
            Query fquery = em.createQuery(QUERY_STRING);
            fquery.setParameter("rn", resourceName);

            @SuppressWarnings("rawtypes")
            List fpList = fquery.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
            ForwardProgressEntity fpx = null;
            if (!fpList.isEmpty()) {
                // ignores multiple results
                fpx = (ForwardProgressEntity) fpList.get(0);
                // refresh the object from DB in case cached data was returned
                em.refresh(fpx);
                if (logger.isDebugEnabled()) {
                    logger.debug("Resource {} exists and will be updated - old fpc= {}, lastUpdated= {}", resourceName,
                            fpx.getFpcCount(), fpx.getLastUpdated());
                }
                fpx.setFpcCount(fpCounter);
            } else {
                // Create a forward progress object
                logger.debug("Adding resource {} to ForwardProgress table", resourceName);
                fpx = new ForwardProgressEntity();
            }
            // update/set columns in entry
            fpx.setResourceName(resourceName);
            em.persist(fpx);
            // flush to the DB
            synchronized (imFlushLock) {
                em.flush();
            }

            // if ResourceRegistration entry exists for resourceName, update it.
            // If not found, create a new entry
            Query rquery = em.createQuery("Select r from ResourceRegistrationEntity r where r.resourceName=:rn");
            rquery.setParameter("rn", resourceName);

            @SuppressWarnings("rawtypes")
            List rrList = rquery.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
            ResourceRegistrationEntity rrx = null;
            if (!rrList.isEmpty()) {
                // ignores multiple results
                rrx = (ResourceRegistrationEntity) rrList.get(0);
                // refresh the object from DB in case cached data was returned
                em.refresh(rrx);
                if (logger.isDebugEnabled()) {
                    logger.debug("Resource {} exists and will be updated - old url= {}, createdDate={}", resourceName,
                            rrx.getResourceUrl(), rrx.getCreatedDate());
                }
                rrx.setLastUpdated(MonitorTime.getInstance().getDate());
            } else {
                // register resource by adding entry to table in DB
                logger.debug("Adding resource {} to ResourceRegistration table", resourceName);
                rrx = new ResourceRegistrationEntity();
            }
            // update/set columns in entry
            rrx.setResourceName(resourceName);
            rrx.setResourceUrl(jmxUrl);
            rrx.setNodeType(nodeType);
            rrx.setSite(siteName);
            em.persist(rrx);
            // flush to the DB
            synchronized (imFlushLock) {
                et.commit();
            }

        } catch (Exception e) {
            logger.error("IntegrityMonitor constructor DB table update failed with exception: ", e);
            try {
                if (et.isActive()) {
                    synchronized (imFlushLock) {
                        et.rollback();
                    }
                }
            } catch (Exception e1) {
                logger.error("IntegrityMonitor constructor threw exception: ", e1);
            }
            throw e;
        }

        try {
            // create instance of StateManagement class and pass emf to it
            stateManager = new StateManagement(emf, resourceName);

            /**
             * Initialize the state and status attributes. This will maintain any Administrative
             * state value but will set the operational state = enabled, availability status = null,
             * standby status = null. The integrity monitor will set the operational state via the
             * FPManager and the owning application must set the standby status by calling
             * promote/demote on the StateManager.
             */
            stateManager.initializeState();

        } catch (StateManagementException e) {
            throw new IntegrityMonitorException(e);
        }

        // create management bean
        try {
            new ComponentAdmin(resourceName, this, stateManager);
        } catch (Exception e) {
            logger.error("ComponentAdmin constructor exception: {}", e.toString(), e);
        }

        fpManager = new FpManager();
        fpManager.start();

    }

    /**
     * Get an instance of IntegrityMonitor for a given resource name. It creates one if it does not exist. Only one
     * instance is allowed to be created per resource name.
     *
     * @param resourceName The resource name of the resource
     * @param properties a set of properties passed in from the resource
     * @return The new instance of IntegrityMonitor
     * @throws IntegrityMonitorException if unable to create jmx url or the constructor returns an exception
     */
    public static IntegrityMonitor getInstance(String resourceName, Properties properties)
            throws IntegrityMonitorException {

        synchronized (getInstanceLock) {
            logger.debug("getInstance() called - resourceName= {}", resourceName);
            if (resourceName == null || resourceName.isEmpty() || properties == null) {
                logger.error("Error: getIntegrityMonitorInstance() called with invalid input");
                return null;
            }

            if (instance == null) {
                logger.debug("Creating new instance of IntegrityMonitor");
                instance = new IntegrityMonitor(resourceName, properties);
            }
            return instance;
        }
    }

    /**
     * Get the single instance.
     *
     * @return the instance
     * @throws IntegrityMonitorException if no instance exists
     */
    public static IntegrityMonitor getInstance() throws IntegrityMonitorException {
        logger.debug("getInstance() called");
        if (instance == null) {
            String msg = "No IntegrityMonitor instance exists."
                    + " Please use the method IntegrityMonitor.getInstance(String resourceName, Properties properties)";
            throw new IntegrityMonitorPropertiesException(msg);
        } else {
            return instance;
        }
    }

    /**
     * This is a facility used by JUnit testing to destroy the IntegrityMonitor instance before creating a new one. It
     * waits a bit to allow the FPManager to fully exit.
     */
    public static void deleteInstance() throws IntegrityMonitorException {
        logger.debug("deleteInstance() called");
        synchronized (getInstanceLock) {
            if (isUnitTesting() && instance != null && instance.getFpManager() != null) {
                FpManager fpm = instance.getFpManager();

                // Stop the FPManager thread
                fpm.stopAndExit();

                try {
                    // Make sure it has exited
                    fpm.join(2000L);
                } catch (InterruptedException e) {
                    logger.error("deleteInstance: Interrupted while waiting for FPManager to fully exit", e);
                    Thread.currentThread().interrupt();
                }

                if (fpm.isAlive()) {
                    logger.error("IntegrityMonitor.deleteInstance() Failed to kill FPManager thread");
                    throw new IntegrityMonitorException(
                            "IntegrityMonitor.deleteInstance() Failed to kill FPManager thread");
                }

                instance = null;
            }
        }
        logger.debug("deleteInstance() exit");
    }

    private FpManager getFpManager() {
        return fpManager;
    }

    private static String getJmxUrl() throws IntegrityMonitorException {

        // get the jmx remote port and construct the JMX URL
        Properties systemProps = System.getProperties();
        String jmxPort = systemProps.getProperty("com.sun.management.jmxremote.port");
        String jmxErrMsg;
        if (jmxPort == null) {
            jmxErrMsg = "System property com.sun.management.jmxremote.port for JMX remote port is not set";
            logger.error("{}", jmxErrMsg);
            throw new IntegrityMonitorException("getJmxUrl exception: " + jmxErrMsg);
        }

        int port = 0;
        try {
            port = Integer.parseInt(jmxPort);
        } catch (NumberFormatException e) {
            jmxErrMsg = "JMX remote port is not a valid integer value - " + jmxPort;
            logger.error("{}", jmxErrMsg);
            throw new IntegrityMonitorException("getJmxUrl exception: " + jmxErrMsg);
        }

        try {
            if (jmxFqdn == null) {
                // get FQDN of this host
                jmxFqdn = InetAddress.getLocalHost().getCanonicalHostName();
            }
        } catch (Exception e) {
            String msg = "getJmxUrl could not get hostname";
            logger.error("{}", msg, e);
            throw new IntegrityMonitorException("getJmxUrl Exception: " + msg);
        }
        if (jmxFqdn == null) {
            String msg = "getJmxUrl encountered null hostname";
            logger.error("{}", msg);
            throw new IntegrityMonitorException("getJmxUrl error: " + msg);
        }

        // assemble the jmx url
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://" + jmxFqdn + ":" + port + "/jmxrmi";

        logger.debug("IntegrityMonitor - jmx url={}", jmxUrl);

        return jmxUrl;
    }

    /**
     * evaluateSanity() is designed to be called by an external entity to evaluate the sanity of the node. It checks the
     * operational and administrative states and the standby status. If the operational state is disabled, it will
     * include the dependencyCheckErrorMsg which includes information about any dependency (node) which has failed.
     */
    public void evaluateSanity() throws IntegrityMonitorException {
        logger.debug("evaluateSanity called ....");
        synchronized (evaluateSanityLock) {

            String errorMsg = dependencyCheckErrorMsg;
            logger.debug("evaluateSanity dependencyCheckErrorMsg = {}", errorMsg);
            // check op state and throw exception if disabled
            if ((stateManager.getOpState() != null) && stateManager.getOpState().equals(StateManagement.DISABLED)) {
                String msg = RESOURCE_STRING + resourceName + " operation state is disabled. " + errorMsg;
                logger.debug("{}", msg);
                throw new IntegrityMonitorException(msg);
            }

            // check admin state and throw exception if locked
            if ((stateManager.getAdminState() != null) && stateManager.getAdminState().equals(StateManagement.LOCKED)) {
                String msg = RESOURCE_STRING + resourceName + " is administratively locked";
                logger.debug("{}", msg);
                throw new AdministrativeStateException("IntegrityMonitor Admin State Exception: " + msg);
            }
            // check standby state and throw exception if cold standby
            if ((stateManager.getStandbyStatus() != null)
                    && stateManager.getStandbyStatus().equals(StateManagement.COLD_STANDBY)) {
                String msg = RESOURCE_STRING + resourceName + " is cold standby";
                logger.debug("{}", msg);
                throw new StandbyStatusException("IntegrityMonitor Standby Status Exception: " + msg);
            }

        }

    }

    /**
     * This method checks the forward progress counter and the state of a dependency. If the dependency is unavailable
     * or failed, an error message is created which is checked when evaluateSanity interface is called. If the error
     * message is set then the evaluateSanity will return an error.
     *
     * @param dep the dependency
     */
    public String stateCheck(String dep) {
        logger.debug("checking state of dependent resource: {}", dep);
        String errorMsg = null;
        ForwardProgressEntity forwardProgressEntity = null;
        StateManagementEntity stateManagementEntity = null;

        // Start a transaction
        EntityTransaction et = em.getTransaction();
        et.begin();

        try {
            Query query = em.createQuery("Select p from ForwardProgressEntity p where p.resourceName=:resource");
            query.setParameter(LC_RESOURCE_STRING, dep);

            @SuppressWarnings("rawtypes")
            List fpList = query.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();

            if (!fpList.isEmpty()) {
                // exists
                forwardProgressEntity = (ForwardProgressEntity) fpList.get(0);
                // refresh the object from DB in case cached data was returned
                em.refresh(forwardProgressEntity);
                logger.debug("Found entry in ForwardProgressEntity table for dependent Resource={}", dep);
            } else {
                errorMsg = dep + ": resource not found in ForwardProgressEntity database table";
                logger.error("{}", errorMsg);
            }
            synchronized (imFlushLock) {
                et.commit();
            }
        } catch (Exception ex) {
            // log an error
            errorMsg = dep + ": ForwardProgressEntity DB operation failed with exception: ";
            logger.error("{}", errorMsg, ex);
            synchronized (imFlushLock) {
                if (et.isActive()) {
                    et.rollback();
                }
            }
        }

        if (errorMsg == null) {
            // Start a transaction
            et = em.getTransaction();
            et.begin();
            try {
                // query if StateManagement entry exists for dependent resource
                Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
                query.setParameter(LC_RESOURCE_STRING, dep);

                @SuppressWarnings("rawtypes")
                List smList = query.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
                if (!smList.isEmpty()) {
                    // exist
                    stateManagementEntity = (StateManagementEntity) smList.get(0);
                    // refresh the object from DB in case cached data was
                    // returned
                    em.refresh(stateManagementEntity);
                    logger.debug("Found entry in StateManagementEntity table for dependent Resource={}", dep);
                } else {
                    errorMsg = dep + ": resource not found in state management entity database table";
                    logger.error("{}", errorMsg);
                }

                synchronized (imFlushLock) {
                    et.commit();
                }
            } catch (Exception e) {
                // log an error
                errorMsg = dep + ": StateManagementEntity DB read failed with exception: ";
                logger.error("{}", errorMsg, e);
                synchronized (imFlushLock) {
                    if (et.isActive()) {
                        et.rollback();
                    }
                }
            }
        }

        // verify that the ForwardProgress is current (check last_updated)
        if (errorMsg == null) {
            if (forwardProgressEntity != null && stateManagementEntity != null) {
                Date date = MonitorTime.getInstance().getDate();
                long diffMs = date.getTime() - forwardProgressEntity.getLastUpdated().getTime();
                logger.debug("IntegrityMonitor.stateCheck(): diffMs = {}", diffMs);

                // Threshold for a stale entry
                long staleMs = maxFpcUpdateIntervalMs;
                logger.debug("IntegrityMonitor.stateCheck(): staleMs = {}", staleMs);

                if (diffMs > staleMs) {
                    // ForwardProgress is stale. Disable it
                    try {
                        if (!stateManagementEntity.getOpState().equals(StateManagement.DISABLED)) {
                            logger.debug("IntegrityMonitor.stateCheck(): Changing OpStat = disabled for {}", dep);
                            stateManager.disableFailed(dep);
                        }
                    } catch (Exception e) {
                        String msg = STATE_CHECK_STRING + dep
                                + "; " + e.getMessage();
                        logger.error("{}", msg, e);
                    }
                }
            } else {

                if (forwardProgressEntity == null) {
                    String msg = STATE_CHECK_STRING + dep
                            + "; " + " forwardProgressEntity == null.";
                    logger.error("{}", msg);
                } else {
                    String msg = STATE_CHECK_STRING + dep
                            + "; " + " stateManagementEntity == null.";
                    logger.error("{}", msg);
                }
            }
        }

        // check operation, admin and standby states of dependent resource
        if (errorMsg == null) {
            if (stateManagementEntity != null) {
                if ((stateManager.getAdminState() != null)
                        && stateManagementEntity.getAdminState().equals(StateManagement.LOCKED)) {
                    errorMsg = dep + ": resource is administratively locked";
                    logger.error("{}", errorMsg);
                } else if ((stateManager.getOpState() != null)
                        && stateManagementEntity.getOpState().equals(StateManagement.DISABLED)) {
                    errorMsg = dep + ": resource is operationally disabled";
                    logger.error("{}", errorMsg);
                } else if ((stateManager.getStandbyStatus() != null)
                        && stateManagementEntity.getStandbyStatus().equals(StateManagement.COLD_STANDBY)) {
                    errorMsg = dep + ": resource is cold standby";
                    logger.error("{}", errorMsg);
                }
            } else {
                errorMsg = dep + ": could not check standy state of resource. stateManagementEntity == null.";
                logger.error("{}", errorMsg);
            }
        }

        String returnMsg = "IntegrityMonitor.stateCheck(): returned error_msg: " + errorMsg;
        logger.debug("{}", returnMsg);
        return errorMsg;
    }

    private String fpCheck(String dep) {
        logger.debug("checking forward progress count of dependent resource: {}", dep);

        String errorMsg = null;

        // check FPC count - a changing FPC count indicates the resource JVM is
        // running

        // Start a transaction
        EntityTransaction et = em.getTransaction();
        et.begin();
        try {
            Query fquery = em.createQuery(QUERY_STRING);
            fquery.setParameter("rn", dep);

            @SuppressWarnings("rawtypes")
            List fpList = fquery.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
            ForwardProgressEntity fpx;
            if (!fpList.isEmpty()) {
                // ignores multiple results
                fpx = (ForwardProgressEntity) fpList.get(0);
                // refresh the object from DB in case cached data was returned
                em.refresh(fpx);
                if (logger.isDebugEnabled()) {
                    logger.debug("Dependent resource {} - fpc= {}, lastUpdated={}", dep, fpx.getFpcCount(),
                            fpx.getLastUpdated());
                }
                long currTime = MonitorTime.getInstance().getMillis();
                // if dependent resource FPC has not been updated, consider it
                // an error
                if ((currTime - fpx.getLastUpdated().getTime()) > maxFpcUpdateIntervalMs) {
                    errorMsg = dep + ": FP count has not been updated in the last " + maxFpcUpdateIntervalMs + "ms";
                    logger.error("{}", errorMsg);
                    try {
                        // create instance of StateMangement class for dependent
                        StateManagement depStateManager = new StateManagement(emf, dep);
                        if (!depStateManager.getOpState().equals(StateManagement.DISABLED)) {
                            logger.debug("Forward progress not detected for dependent resource {}. Setting dependent's "
                                    + "state to disable failed.", dep);
                            depStateManager.disableFailed();
                        }
                    } catch (Exception e) {
                        // ignore errors
                        logger.error("Update dependent state failed with exception: ", e);
                    }
                }
            } else {
                // resource entry not found in FPC table
                errorMsg = dep + ": resource not found in ForwardProgressEntity table in the DB";
                logger.error("{}", errorMsg);
            }
            synchronized (imFlushLock) {
                et.commit();
            }
        } catch (Exception e) {
            // log an error and continue
            errorMsg = dep + ": ForwardProgressEntity DB read failed with exception: ";
            logger.error("{}", errorMsg, e);
            synchronized (imFlushLock) {
                if (et.isActive()) {
                    et.rollback();
                }
            }
        }

        return errorMsg;
    }

    /**
     * Get all forward progress entities.
     *
     * @return list of all forward progress entities
     */
    public List<ForwardProgressEntity> getAllForwardProgressEntity() {
        logger.debug("getAllForwardProgressEntity: entry");
        ArrayList<ForwardProgressEntity> fpList = new ArrayList<>();
        // Start a transaction
        EntityTransaction et = em.getTransaction();
        et.begin();
        try {
            Query fquery = em.createQuery("Select e from ForwardProgressEntity e");
            @SuppressWarnings("rawtypes")
            List myList = fquery.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
            synchronized (imFlushLock) {
                et.commit();
            }
            logger.debug("getAllForwardProgressEntity: myList.size(): {}", myList.size());
            if (!myList.isEmpty()) {
                for (int i = 0; i < myList.size(); i++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("getAllForwardProgressEntity: myList.get({}).getResourceName(): {}", i,
                                ((ForwardProgressEntity) myList.get(i)).getResourceName());
                    }
                    fpList.add((ForwardProgressEntity) myList.get(i));
                }
            }
            synchronized (imFlushLock) {
                if (et.isActive()) {
                    et.commit();
                }
            }
        } catch (Exception e) {
            // log an error and continue
            String msg = "getAllForwardProgessEntity DB read failed with exception: ";
            logger.error("{}", msg, e);
            synchronized (imFlushLock) {
                if (et.isActive()) {
                    et.rollback();
                }
            }
        }
        return fpList;
    }

    private String jmxCheck(String dep) {
        logger.debug("checking health of dependent by calling test() via JMX on resource: {}", dep);

        String errorMsg = null;

        // get the JMX URL from the database
        String jmxUrl = null;
        // Start a transaction
        EntityTransaction et = em.getTransaction();
        et.begin();
        try {
            // query if ResourceRegistration entry exists for resourceName
            Query rquery = em.createQuery("Select r from ResourceRegistrationEntity r where r.resourceName=:rn");
            rquery.setParameter("rn", dep);

            @SuppressWarnings("rawtypes")
            List rrList = rquery.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
            ResourceRegistrationEntity rrx = null;

            if (!rrList.isEmpty()) {
                // ignores multiple results
                rrx = (ResourceRegistrationEntity) rrList.get(0);
                // refresh the object from DB in case cached data was returned
                em.refresh(rrx);
                jmxUrl = rrx.getResourceUrl();
                if (logger.isDebugEnabled()) {
                    logger.debug("Dependent Resource={}, url={}, createdDate={}", dep, jmxUrl, rrx.getCreatedDate());
                }
            } else {
                errorMsg = dep + ": resource not found in ResourceRegistrationEntity table in the DB";
                logger.error("{}", errorMsg);
            }

            synchronized (imFlushLock) {
                et.commit();
            }
        } catch (Exception e) {
            errorMsg = dep + ": ResourceRegistrationEntity DB read failed with exception: ";
            logger.error("{}", errorMsg, e);
            synchronized (imFlushLock) {
                if (et.isActive()) {
                    et.rollback();
                }
            }
        }

        if (jmxUrl != null) {
            JmxAgentConnection jmxAgentConnection = null;
            try {
                jmxAgentConnection = new JmxAgentConnection(jmxUrl);
                MBeanServerConnection mbeanServer = jmxAgentConnection.getMBeanConnection();
                ComponentAdminMBean admin =
                        JMX.newMXBeanProxy(mbeanServer, ComponentAdmin.getObjectName(dep), ComponentAdminMBean.class);

                // invoke the test method via the jmx proxy
                admin.test();
                logger.debug("Dependent resource {} sanity test passed", dep);
            } catch (Exception e) {
                errorMsg = dep + ": resource sanity test failed with exception: ";
                logger.error("{}", errorMsg, e);
            } finally {
                // close the JMX connector
                if (jmxAgentConnection != null) {
                    jmxAgentConnection.disconnect();
                }
            }
        }

        return errorMsg;
    }

    /**
     * Perform a dependency check.
     *
     * @return an error message detailing any issues found
     */
    public String dependencyCheck() {
        logger.debug("dependencyCheck: entry");
        synchronized (dependencyCheckLock) {

            // Start with the error message empty
            String errorMsg = "";
            boolean dependencyFailure = false;

            /*
             * Before we check dependency groups we need to check subsystemTest.
             */
            try {
                // Test any subsystems that are not covered under the dependency
                // relationship
                subsystemTest();
            } catch (Exception e) {
                logger.error("IntegrityMonitor threw exception", e);
                dependencyFailure = true;
                // This indicates a subsystemTest failure
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "{}: There has been a subsystemTest failure with error:{} Updating this resource's "
                                        + "state to disableDependency",
                                resourceName, e.getMessage());
                    }
                    // Capture the subsystemTest failure info
                    if (!errorMsg.isEmpty()) {
                        errorMsg = errorMsg.concat(",");
                    }
                    errorMsg = errorMsg.concat(resourceName + ": " + e.getMessage());
                    this.stateManager.disableDependency();
                } catch (Exception ex) {
                    logger.error(EXCEPTION_STRING, ex);
                    if (!errorMsg.isEmpty()) {
                        errorMsg = errorMsg.concat(",");
                    }
                    errorMsg = errorMsg.concat("\n" + resourceName
                            + ": Failed to disable dependency after subsystemTest failure due to: " + ex.getMessage());
                }
            }

            // Check the sanity of dependents for lead subcomponents
            if (depGroups != null && depGroups.length > 0) {
                // check state of resources in dependency groups
                for (String group : depGroups) {
                    group = group.trim();
                    if (group.isEmpty()) {
                        // ignore empty group
                        continue;
                    }
                    String[] dependencies = group.split(",");
                    if (logger.isDebugEnabled()) {
                        logger.debug("group dependencies = {}", Arrays.toString(dependencies));
                    }
                    int realDepCount = 0;
                    int failDepCount = 0;
                    for (String dep : dependencies) {
                        dep = dep.trim();
                        if (dep.isEmpty()) {
                            // ignore empty dependency
                            continue;
                        }
                        realDepCount++; // this is a valid dependency whose state is tracked
                        // if a resource is down, its FP count will not be incremented
                        String failMsg = fpCheck(dep);
                        if (failMsg == null) {
                            if (testViaJmx) {
                                failMsg = jmxCheck(dep);
                            } else {
                                failMsg = stateCheck(dep);
                            }
                        }
                        if (failMsg != null) {
                            failDepCount++;
                            if (!errorMsg.isEmpty()) {
                                errorMsg = errorMsg.concat(", ");
                            }
                            errorMsg = errorMsg.concat(failMsg);
                        }
                    } // end for (String dep : dependencies)

                    // if all dependencies in a group are failed, set this
                    // resource's state to disable dependency
                    if ((realDepCount > 0) && (failDepCount == realDepCount)) {
                        dependencyFailure = true;
                        try {
                            logger.debug("All dependents in group {} have failed their health check. Updating this "
                                    + "resource's state to disableDependency", group);
                            if (stateManager.getAvailStatus() == null || !((stateManager.getAvailStatus())
                                    .equals(StateManagement.DEPENDENCY)
                                    || (stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY_FAILED))) {
                                // Note: redundant calls are made by
                                // refreshStateAudit
                                this.stateManager.disableDependency();
                            }
                        } catch (Exception e) {
                            logger.error(EXCEPTION_STRING, e);
                            if (!errorMsg.isEmpty()) {
                                errorMsg = errorMsg.concat(",");
                            }
                            errorMsg = errorMsg.concat(resourceName + ": Failed to disable dependency");
                            break; // break out on failure and skip checking other groups
                        }
                    }
                    // check the next group

                } // end for (String group : depGroups)

                /*
                 * We have checked all the dependency groups. If all are ok and subsystemTest
                 * passed, dependencyFailure == false
                 */
                if (!dependencyFailure) {
                    try {
                        logger.debug(
                                "All dependency groups have at least one viable member. Updating this resource's state"
                                        + " to enableNoDependency");
                        if (stateManager.getAvailStatus() != null
                                && ((stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY)
                                || (stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY_FAILED))) {
                            // Note: redundant calls are made by
                            // refreshStateAudit
                            this.stateManager.enableNoDependency();
                        }
                        // The refreshStateAudit will catch the case where it is disabled but
                        // availStatus != failed
                    } catch (Exception e) {
                        logger.error(EXCEPTION_STRING, e);
                        if (!errorMsg.isEmpty()) {
                            errorMsg = errorMsg.concat(",");
                        }
                        errorMsg = errorMsg.concat(resourceName + ": Failed to enable no dependency");
                    }
                }
            } else if (!dependencyFailure) {
                /*
                 * This is put here to clean up when no dependency group should exist, but one was
                 * erroneously added which caused the state to be disabled/dependency/coldstandby
                 * and later removed. We saw this happen in the lab, but is not very likely in a
                 * production environment...but you never know.
                 */
                try {
                    logger.debug("There are no dependents. Updating this resource's state to enableNoDependency");
                    if (stateManager.getAvailStatus() != null
                            && ((stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY)
                            || (stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY_FAILED))) {
                        // Note: redundant calls are made by refreshStateAudit
                        this.stateManager.enableNoDependency();
                    }
                    // The refreshStateAudit will catch the case where it is disabled but
                    // availStatus != failed
                } catch (Exception e) {
                    logger.error(EXCEPTION_STRING, e);
                    if (!errorMsg.isEmpty()) {
                        errorMsg = errorMsg.concat(",");
                    }
                    errorMsg = errorMsg.concat(resourceName + ": Failed to enable no dependency");
                }
            }

            if (!errorMsg.isEmpty()) {
                logger.error("Sanity failure detected in a dependent resource: {}", errorMsg);

            }

            dependencyCheckErrorMsg = errorMsg;
            lastDependencyCheckTime = MonitorTime.getInstance().getMillis();
            logger.debug("dependencyCheck: exit");
            return errorMsg;
        }
    }

    /**
     * Execute a test transaction. It is called when the test transaction timer fires. It could be overridden to provide
     * additional test functionality. If overridden, the overriding method must invoke startTransaction() and
     * endTransaction() and check if the allNotWellMap is empty.
     */
    public void testTransaction() {
        synchronized (testTransactionLock) {
            logger.debug("testTransaction: entry");
            //
            // startTransaction() not required for testTransaction
            //

            // end transaction - increments local FP counter
            endTransaction();
        }
    }

    /**
     * Additional testing for subsystems that do not have a /test interface (for ex. 3rd party processes like elk). This
     * method would be overridden by the subsystem.
     */
    public void subsystemTest() throws IntegrityMonitorException {
        // Testing provided by subsystem
        logger.debug("IntegrityMonitor subsystemTest() OK");
    }

    /**
     * Checks admin state and resets transaction timer. Called by application at the start of a transaction.
     *
     * @throws AdministrativeStateException throws admin state exception if resource is locked
     * @throws StandbyStatusException if resource is in standby
     */
    public void startTransaction() throws AdministrativeStateException, StandbyStatusException {

        synchronized (startTransactionLock) {
            // check admin state and throw exception if locked
            if ((stateManager.getAdminState() != null) && stateManager.getAdminState().equals(StateManagement.LOCKED)) {
                String msg = RESOURCE_STRING + resourceName + " is administratively locked";

                throw new AdministrativeStateException("IntegrityMonitor Admin State Exception: " + msg);
            }
            // check standby state and throw exception if locked

            if ((stateManager.getStandbyStatus() != null)
                    && (stateManager.getStandbyStatus().equals(StateManagement.HOT_STANDBY)
                    || stateManager.getStandbyStatus().equals(StateManagement.COLD_STANDBY))) {
                String msg = RESOURCE_STRING + resourceName + " is standby";

                throw new StandbyStatusException("IntegrityMonitor Standby Status Exception: " + msg);
            }

            // reset transactionTimer so it will not fire
            elapsedTestTransTime = 0;
        }
    }

    /**
     * Increment the local forward progress counter. Called by application at the end of each transaction (successful or
     * not).
     */
    public void endTransaction() {
        synchronized (endTransactionLock) {
            if (getAllNotWellMap() != null) {
                if (!(getAllNotWellMap().isEmpty())) {
                    /*
                     * An entity has reported that it is not well. We must not allow the the forward
                     * progress counter to advance.
                     */
                    String msg = "allNotWellMap:";
                    for (Entry<String, String> entry : allNotWellMap.entrySet()) {
                        msg = msg.concat("\nkey = " + entry.getKey() + " msg = " + entry.getValue());
                    }
                    logger.error("endTransaction: allNotWellMap is NOT EMPTY.  Not advancing forward"
                            + "progress counter. \n{}\n", msg);
                    return;
                } else {
                    if (logger.isDebugEnabled() && getAllSeemsWellMap() != null && !(getAllSeemsWellMap().isEmpty())) {
                        String msg = "allSeemsWellMap:";
                        for (Entry<String, String> entry : allSeemsWellMap.entrySet()) {
                            msg = msg.concat("\nkey = " + entry.getKey() + " msg = " + entry.getValue());
                        }
                        logger.debug(
                                "endTransaction: allNotWellMap IS EMPTY and allSeemsWellMap is NOT EMPTY.  "
                                        + "Advancing forward progress counter. \n{}\n", msg);
                    }
                }
            }
            // increment local FPC
            fpCounter++;
        }
    }

    // update FP count in DB with local FP count
    private void writeFpc() throws IntegrityMonitorException {

        // Start a transaction
        EntityTransaction et = em.getTransaction();

        if (!et.isActive()) {
            et.begin();
        }

        try {
            // query if ForwardProgress entry exists for resourceName
            Query fquery = em.createQuery(QUERY_STRING);
            fquery.setParameter("rn", resourceName);

            @SuppressWarnings("rawtypes")
            List fpList = fquery.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
            ForwardProgressEntity fpx;
            if (!fpList.isEmpty()) {
                // ignores multiple results
                fpx = (ForwardProgressEntity) fpList.get(0);
                // refresh the object from DB in case cached data was returned
                em.refresh(fpx);
                if (logger.isDebugEnabled()) {
                    logger.debug("Updating FP entry: Resource={}, fpcCount={}, lastUpdated={}, new fpcCount={}",
                            resourceName, fpx.getFpcCount(), fpx.getLastUpdated(), fpCounter);
                }
                fpx.setFpcCount(fpCounter);
                em.persist(fpx);
                // flush to the DB and commit
                synchronized (imFlushLock) {
                    et.commit();
                }
            } else {
                // Error - FP entry does not exist
                String msg = "FP entry not found in database for resource " + resourceName;
                throw new IntegrityMonitorException(msg);
            }
        } catch (Exception e) {
            try {
                synchronized (imFlushLock) {
                    if (et.isActive()) {
                        et.rollback();
                    }
                }
            } catch (Exception e1) {
                logger.error(EXCEPTION_STRING, e1);
            }
            logger.error("writeFpc DB table commit failed with exception: {}", e);
            throw e;
        }
    }

    // retrieve state manager reference
    public final StateManagement getStateManager() {
        return this.stateManager;
    }

    /**
     * Read and validate properties.
     *
     * @throws IntegrityMonitorPropertiesException if a property is invalid
     */
    private static void validateProperties(Properties prop) throws IntegrityMonitorPropertiesException {

        if (prop.getProperty(IntegrityMonitorProperties.DB_DRIVER) == null) {
            String msg = IntegrityMonitorProperties.DB_DRIVER + NULL_PROPERTY_STRING;
            logger.error("{}", msg);
            throw new IntegrityMonitorPropertiesException(PROPERTY_EXCEPTION_STRING + msg);
        }

        if (prop.getProperty(IntegrityMonitorProperties.DB_URL) == null) {
            String msg = IntegrityMonitorProperties.DB_URL + NULL_PROPERTY_STRING;
            logger.error("{}", msg);
            throw new IntegrityMonitorPropertiesException(PROPERTY_EXCEPTION_STRING + msg);
        }

        if (prop.getProperty(IntegrityMonitorProperties.DB_USER) == null) {
            String msg = IntegrityMonitorProperties.DB_USER + NULL_PROPERTY_STRING;
            logger.error("{}", msg);
            throw new IntegrityMonitorPropertiesException(PROPERTY_EXCEPTION_STRING + msg);
        }

        if (prop.getProperty(IntegrityMonitorProperties.DB_PWD) == null) {
            String msg = IntegrityMonitorProperties.DB_PWD + NULL_PROPERTY_STRING;
            logger.error("{}", msg);
            throw new IntegrityMonitorPropertiesException(PROPERTY_EXCEPTION_STRING + msg);
        }

        if (prop.getProperty(IntegrityMonitorProperties.FP_MONITOR_INTERVAL) != null) {
            try {
                monitorIntervalMs = toMillis(
                        Integer.parseInt(prop.getProperty(IntegrityMonitorProperties.FP_MONITOR_INTERVAL).trim()));
            } catch (NumberFormatException e) {
                logger.warn(IGNORE_INVALID_PROPERTY_STRING, IntegrityMonitorProperties.FP_MONITOR_INTERVAL, e);
            }
        }

        if (prop.getProperty(IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD) != null) {
            try {
                failedCounterThreshold =
                        Integer.parseInt(prop.getProperty(IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD).trim());
            } catch (NumberFormatException e) {
                logger.warn(IGNORE_INVALID_PROPERTY_STRING, IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD, e);
            }
        }

        if (prop.getProperty(IntegrityMonitorProperties.TEST_TRANS_INTERVAL) != null) {
            try {
                testTransIntervalMs = toMillis(
                        Integer.parseInt(prop.getProperty(IntegrityMonitorProperties.TEST_TRANS_INTERVAL).trim()));
            } catch (NumberFormatException e) {
                logger.warn(IGNORE_INVALID_PROPERTY_STRING, IntegrityMonitorProperties.TEST_TRANS_INTERVAL, e);
            }
        }

        if (prop.getProperty(IntegrityMonitorProperties.WRITE_FPC_INTERVAL) != null) {
            try {
                writeFpcIntervalMs = toMillis(
                        Integer.parseInt(prop.getProperty(IntegrityMonitorProperties.WRITE_FPC_INTERVAL).trim()));
            } catch (NumberFormatException e) {
                logger.warn(IGNORE_INVALID_PROPERTY_STRING, IntegrityMonitorProperties.WRITE_FPC_INTERVAL, e);
            }
        }

        if (prop.getProperty(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL) != null) {
            try {
                checkDependencyIntervalMs = toMillis(Integer
                        .parseInt(prop.getProperty(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL).trim()));
            } catch (NumberFormatException e) {
                logger.warn(IGNORE_INVALID_PROPERTY_STRING, IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, e);
            }
        }

        // dependency_groups are a semi-colon separated list of groups
        // each group is a comma separated list of resource names
        // For ex. dependency_groups = site_1.pap_1,site_1.pap_2 ; site_1.pdp_1,
        // site_1.pdp_2
        if (prop.getProperty(IntegrityMonitorProperties.DEPENDENCY_GROUPS) != null) {
            try {
                depGroups = prop.getProperty(IntegrityMonitorProperties.DEPENDENCY_GROUPS).split(";");
                if (logger.isDebugEnabled()) {
                    logger.debug("dependency groups property = {}", Arrays.toString(depGroups));
                }
            } catch (Exception e) {
                logger.warn(IGNORE_INVALID_PROPERTY_STRING, IntegrityMonitorProperties.DEPENDENCY_GROUPS, e);
            }
        }

        siteName = prop.getProperty(IntegrityMonitorProperties.SITE_NAME);
        if (siteName == null) {
            String msg = IntegrityMonitorProperties.SITE_NAME + NULL_PROPERTY_STRING;
            logger.error("{}", msg);
            throw new IntegrityMonitorPropertiesException(PROPERTY_EXCEPTION_STRING + msg);
        } else {
            siteName = siteName.trim();
        }

        nodeType = prop.getProperty(IntegrityMonitorProperties.NODE_TYPE);
        if (nodeType == null) {
            String msg = IntegrityMonitorProperties.NODE_TYPE + NULL_PROPERTY_STRING;
            logger.error("{}", msg);
            throw new IntegrityMonitorPropertiesException(PROPERTY_EXCEPTION_STRING + msg);
        } else {
            nodeType = nodeType.trim();
            if (!isNodeTypeEnum(nodeType)) {
                String msg = IntegrityMonitorProperties.NODE_TYPE + " property " + nodeType + " is invalid";
                logger.error("{}", msg);
                throw new IntegrityMonitorPropertiesException(PROPERTY_EXCEPTION_STRING + msg);
            }
        }

        if (prop.getProperty(IntegrityMonitorProperties.TEST_VIA_JMX) != null) {
            String jmxTest = prop.getProperty(IntegrityMonitorProperties.TEST_VIA_JMX).trim();
            testViaJmx = Boolean.parseBoolean(jmxTest);
        }

        if (prop.getProperty(IntegrityMonitorProperties.JMX_FQDN) != null) {
            jmxFqdn = prop.getProperty(IntegrityMonitorProperties.JMX_FQDN).trim();
            if (jmxFqdn.isEmpty()) {
                jmxFqdn = null;
            }
        }

        if (prop.getProperty(IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL) != null) {
            try {
                maxFpcUpdateIntervalMs = toMillis(
                        Integer.parseInt(prop.getProperty(IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL).trim()));
            } catch (NumberFormatException e) {
                logger.warn(IGNORE_INVALID_PROPERTY_STRING, IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL, e);
            }
        }

        if (prop.getProperty(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS) != null) {
            try {
                stateAuditIntervalMs =
                        Long.parseLong(prop.getProperty(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS));
            } catch (NumberFormatException e) {
                logger.warn(IGNORE_INVALID_PROPERTY_STRING, IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, e);
            }
        }

        if (prop.getProperty(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS) != null) {
            try {
                refreshStateAuditIntervalMs =
                        Long.parseLong(prop.getProperty(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS));
            } catch (NumberFormatException e) {
                logger.warn(IGNORE_INVALID_PROPERTY_STRING, IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS,
                        e);
            }
        }

        logger.debug("IntegrityMonitor.validateProperties(): Property values \nmaxFpcUpdateIntervalMs = {}\n",
                maxFpcUpdateIntervalMs);
    }

    /**
     * Update properties.
     *
     * @param newprop the new properties
     */
    public static void updateProperties(Properties newprop) {
        if (isUnitTesting()) {
            try {
                validateProperties(newprop);
            } catch (IntegrityMonitorPropertiesException e) {
                logger.error(EXCEPTION_STRING, e);
            }
        } else {
            logger.debug("Update integrity monitor properties not allowed");
        }
    }

    private static boolean isNodeTypeEnum(String nodeType) {
        String upper = nodeType.toUpperCase();
        for (NodeType n : NodeType.values()) {
            if (n.toString().equals(upper)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Look for "Forward Progress" -- if the 'FPMonitor' is stalled for too long, the operational state is changed to
     * 'Disabled', and an alarm is set. The state is restored when forward progress continues.
     */
    private void fpMonitorCycle() {
        logger.debug("fpMonitorCycle(): entry");
        synchronized (fpMonitorCycleLock) {
            // monitoring interval checks
            if (monitorIntervalMs <= 0) {
                logger.debug("fpMonitorCycle(): disabled");
                elapsedTime = 0;
                return; // monitoring is disabled
            }

            elapsedTime = elapsedTime + CYCLE_INTERVAL_MILLIS;
            if (elapsedTime < monitorIntervalMs) {
                return; // monitoring interval not reached
            }

            elapsedTime = 0; // reset elapsed time

            try {
                if (fpCounter == lastFpCounter) {
                    // no forward progress
                    missedCycles += 1;
                    if (missedCycles >= failedCounterThreshold && !alarmExists) {
                        logger.debug("Forward progress not detected for resource {}. Setting state to disable failed.",
                                resourceName);
                        if (!(stateManager.getOpState()).equals(StateManagement.DISABLED)) {
                            // Note: The refreshStateAudit will make redundant
                            // calls
                            stateManager.disableFailed();
                        }
                        // The refreshStateAudit will catch the case where opStat = disabled and
                        // availState ! failed/dependency.failed
                        alarmExists = true;
                    }
                } else {
                    // forward progress has occurred
                    lastFpCounter = fpCounter;
                    missedCycles = 0;
                    // set op state to enabled
                    logger.debug("Forward progress detected for resource {}. Setting state to enable not failed.",
                            resourceName);
                    if (!(stateManager.getOpState()).equals(StateManagement.ENABLED)) {
                        // Note: The refreshStateAudit will make redundant calls
                        stateManager.enableNotFailed();
                    }
                    // The refreshStateAudit will catch the case where opState=enabled and
                    // availStatus != null
                    alarmExists = false;
                }
            } catch (Exception e) {
                // log error
                logger.error("FP Monitor encountered error. ", e);
            }
        }
        logger.debug("fpMonitorCycle(): exit");
    }

    /**
     * Look for "Forward Progress" on other nodes. If they are not making forward progress, check their operational
     * state. If it is not disabled, then disable them.
     */
    private void stateAudit() {
        logger.debug("IntegrityMonitor.stateAudit(): entry");
        if (stateAuditIntervalMs <= 0) {
            logger.debug("IntegrityMonitor.stateAudit(): disabled");
            return; // stateAudit is disabled
        }

        // Only run from nodes that are operational
        if (stateManager.getOpState().equals(StateManagement.DISABLED)) {
            logger.debug("IntegrityMonitor.stateAudit(): DISABLED. returning");
            return;
        }
        if (stateManager.getAdminState().equals(StateManagement.LOCKED)) {
            logger.debug("IntegrityMonitor.stateAudit(): LOCKED. returning");
            return;
        }
        if (!stateManager.getStandbyStatus().equals(StateManagement.NULL_VALUE)
                && stateManager.getStandbyStatus() != null
                && !stateManager.getStandbyStatus().equals(StateManagement.PROVIDING_SERVICE)) {
            logger.debug("IntegrityMonitor.stateAudit(): NOT PROVIDING_SERVICE. returning");
            return;
        }

        Date date = MonitorTime.getInstance().getDate();
        long timeSinceLastStateAudit = date.getTime() - lastStateAuditTime.getTime();
        if (timeSinceLastStateAudit < stateAuditIntervalMs) {
            logger.debug("IntegrityMonitor.stateAudit(): Not time to run. returning");
            return;
        }

        executeStateAudit();

        lastStateAuditTime = date;

        logger.debug("IntegrityMonitor.stateAudit(): exit");
    }

    /**
     * Execute state audit.
     */
    public void executeStateAudit() {
        logger.debug("IntegrityMonitor.executeStateAudit(): entry");
        Date date = MonitorTime.getInstance().getDate();

        // Get all entries in the forwardprogressentity table
        List<ForwardProgressEntity> fpList = getAllForwardProgressEntity();

        // Check if each forwardprogressentity entry is current
        for (ForwardProgressEntity fpe : fpList) {
            // If the this is my ForwardProgressEntity, continue
            if (fpe.getResourceName().equals(IntegrityMonitor.resourceName)) {
                continue;
            }
            // Make sure you are not getting a cached version
            em.refresh(fpe);
            long diffMs = date.getTime() - fpe.getLastUpdated().getTime();
            if (logger.isDebugEnabled()) {
                logger.debug("IntegrityMonitor.executeStateAudit(): resource = {}, diffMs = {}", fpe.getResourceName(),
                        diffMs);
            }

            // Threshold for a stale entry
            long staleMs = maxFpcUpdateIntervalMs;
            if (logger.isDebugEnabled()) {
                logger.debug("IntegrityMonitor.executeStateAudit(): resource = {}, staleMs = {}", fpe.getResourceName(),
                        staleMs);
            }

            if (diffMs > staleMs) {
                // ForwardProgress is stale. Disable it
                // Start a transaction
                logger.debug("IntegrityMonitor.executeStateAudit(): resource = {}, FPC is stale. Disabling it",
                        fpe.getResourceName());
                EntityTransaction et = em.getTransaction();
                et.begin();
                StateManagementEntity sme = null;
                try {
                    // query if StateManagement entry exists for fpe resource
                    Query query =
                            em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
                    query.setParameter(LC_RESOURCE_STRING, fpe.getResourceName());

                    @SuppressWarnings("rawtypes")
                    List smList =
                            query.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
                    if (!smList.isEmpty()) {
                        // exists
                        sme = (StateManagementEntity) smList.get(0);
                        // refresh the object from DB in case cached data was
                        // returned
                        em.refresh(sme);
                        if (logger.isDebugEnabled()) {
                            logger.debug(
                                    "IntegrityMonitor.executeStateAudit(): Found entry in StateManagementEntity table "
                                            + "for Resource={}",
                                    sme.getResourceName());
                        }
                    } else {
                        String msg = "IntegrityMonitor.executeStateAudit(): " + fpe.getResourceName()
                                + ": resource not found in state management entity database table";
                        logger.error("{}", msg);
                    }
                    synchronized (imFlushLock) {
                        et.commit();
                    }
                } catch (Exception e) {
                    // log an error
                    logger.error("IntegrityMonitor.executeStateAudit(): {}: StateManagementEntity DB read failed with "
                            + "exception: ", fpe.getResourceName(), e);
                    synchronized (imFlushLock) {
                        if (et.isActive()) {
                            et.rollback();
                        }
                    }
                }

                if (sme != null && !sme.getOpState().equals(StateManagement.DISABLED)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("IntegrityMonitor.executeStateAudit(): Changing OpStat = disabled for {}",
                                sme.getResourceName());
                    }
                    try {
                        stateManager.disableFailed(sme.getResourceName());
                    } catch (Exception e) {
                        String msg = "IntegrityMonitor.executeStateAudit(): Failed to disable " + sme.getResourceName();
                        logger.error("{}", msg, e);
                    }
                }
            } // end if(diffMs > staleMs)
        } // end for(ForwardProgressEntity fpe : fpList)
        logger.debug("IntegrityMonitor.executeStateAudit(): exit");
    }

    /**
     * Execute a test transaction when test transaction interval has elapsed.
     */
    private void checkTestTransaction() {
        logger.debug("checkTestTransaction(): entry");
        synchronized (checkTestTransactionLock) {

            // test transaction timer checks
            if (testTransIntervalMs <= 0) {
                logger.debug("checkTestTransaction(): disabled");
                elapsedTestTransTime = 0;
                return; // test transaction is disabled
            }

            elapsedTestTransTime = elapsedTestTransTime + CYCLE_INTERVAL_MILLIS;
            if (elapsedTestTransTime < testTransIntervalMs) {
                return; // test transaction interval not reached
            }

            elapsedTestTransTime = 0; // reset elapsed time

            // execute test transaction
            testTransaction();
        }
        logger.debug("checkTestTransaction(): exit");
    }

    /**
     * Updates Fpc counter in database when write Fpc interval has elapsed.
     */
    private void checkWriteFpc() {
        logger.debug("checkWriteFpc(): entry");
        synchronized (checkWriteFpcLock) {

            // test transaction timer checks
            if (writeFpcIntervalMs <= 0) {
                logger.debug("checkWriteFpc(): disabled");
                elapsedWriteFpcTime = 0;
                return; // write Fpc is disabled
            }

            elapsedWriteFpcTime = elapsedWriteFpcTime + CYCLE_INTERVAL_MILLIS;
            if (elapsedWriteFpcTime < writeFpcIntervalMs) {
                return; // write Fpc interval not reached
            }

            elapsedWriteFpcTime = 0; // reset elapsed time

            // write Fpc to database
            try {
                writeFpc();
            } catch (Exception e) {
                logger.error(EXCEPTION_STRING, e);
            }
        }
        logger.debug("checkWriteFpc(): exit");
    }

    /**
     * Execute a dependency health check periodically which also updates this resource's state.
     */
    private void checkDependentHealth() {
        logger.debug("checkDependentHealth: entry");
        if (checkDependencyIntervalMs <= 0) {
            logger.debug("checkDependentHealth: disabled");
            return; // dependency monitoring is disabled
        }

        long currTime = MonitorTime.getInstance().getMillis();
        logger.debug("checkDependentHealth currTime - lastDependencyCheckTime = {}",
                currTime - lastDependencyCheckTime);
        if ((currTime - lastDependencyCheckTime) > checkDependencyIntervalMs) {
            // execute dependency check and update this resource's state

            dependencyCheck();
        }
        logger.debug("checkDependentHealth: exit");
    }

    /*
     * This is a simple refresh audit which is periodically run to assure that the states and status
     * attributes are aligned and notifications are sent to any listeners. It is possible for
     * state/status to get out of sync and notified systems to be out of synch due to database
     * corruption (manual or otherwise) or because a node became isolated.
     *
     * When the operation (lock/unlock) is called, it will cause a re-evaluation of the state and
     * send a notification to all registered observers.
     */
    private void refreshStateAudit() {
        logger.debug("refreshStateAudit(): entry");
        if (refreshStateAuditIntervalMs <= 0) {
            // The audit is disabled
            logger.debug("refreshStateAudit(): disabled");
            return;
        }
        executeRefreshStateAudit();
        logger.debug("refreshStateAudit(): exit");
    }

    /**
     * Execute refresh state audit.
     */
    public void executeRefreshStateAudit() {
        logger.debug("executeRefreshStateAudit(): entry");
        synchronized (refreshStateAuditLock) {
            logger.debug("refreshStateAudit: entry");
            Date now = MonitorTime.getInstance().getDate();
            long nowMs = now.getTime();
            long lastTimeMs = refreshStateAuditLastRunDate.getTime();
            logger.debug("refreshStateAudit: ms since last run = {}", nowMs - lastTimeMs);

            if ((nowMs - lastTimeMs) > refreshStateAuditIntervalMs) {
                String adminState = stateManager.getAdminState();
                logger.debug("refreshStateAudit: adminState = {}", adminState);
                if (adminState.equals(StateManagement.LOCKED)) {
                    try {
                        logger.debug("refreshStateAudit: calling lock()");
                        stateManager.lock();
                    } catch (Exception e) {
                        logger.error("refreshStateAudit: caught unexpected exception from stateManager.lock(): ", e);
                    }
                } else { // unlocked
                    try {
                        logger.debug("refreshStateAudit: calling unlock()");
                        stateManager.unlock();
                    } catch (Exception e) {
                        logger.error("refreshStateAudit: caught unexpected exception from stateManager.unlock(): ", e);
                    }
                }
                refreshStateAuditLastRunDate = MonitorTime.getInstance().getDate();
                logger.debug("refreshStateAudit: exit");
            }
        }
        logger.debug("executeRefreshStateAudit(): exit");
    }

    /**
     * The following nested class periodically performs the forward progress check, checks dependencies, does a refresh
     * state audit and runs the stateAudit.
     */
    class FpManager extends Thread {

        private volatile boolean stopRequested = false;

        // Constructor - start FP manager thread
        FpManager() {
            // set now as the last time the refreshStateAudit ran
            IntegrityMonitor.this.refreshStateAuditLastRunDate = MonitorTime.getInstance().getDate();
        }

        @Override
        public void run() {
            logger.debug("FPManager thread running");

            try {
                runStarted();

                while (!stopRequested) {
                    MonitorTime.getInstance().sleep(CYCLE_INTERVAL_MILLIS);
                    
                    runOnce();
                    monitorCompleted();
                }

            } catch (InterruptedException e) {
                logger.debug(EXCEPTION_STRING, e);
                Thread.currentThread().interrupt();
            }
        }

        void stopAndExit() {
            stopRequested = true;
            this.interrupt();
        }

        private void runOnce() {
            try {
                logger.debug("FPManager calling fpMonitorCycle()");
                // check forward progress timer
                fpMonitorCycle();

                logger.debug("FPManager calling checkTestTransaction()");
                // check test transaction timer
                checkTestTransaction();

                logger.debug("FPManager calling checkWriteFpc()");
                // check write Fpc timer
                checkWriteFpc();

                logger.debug("FPManager calling checkDependentHealth()");
                // check dependency health
                checkDependentHealth();

                logger.debug("FPManager calling refreshStateAudit()");
                // check if it is time to run the refreshStateAudit
                refreshStateAudit();

                logger.debug("FPManager calling stateAudit()");
                // check if it is time to run the stateAudit
                stateAudit();

            } catch (Exception e) {
                logger.error("Ignore FPManager thread processing timer(s) exception: ", e);
            }
        }
    }

    /**
     * Set all seems well or not well for the specified key.
     *
     * @param key the key
     * @param asw <code>true</code> if all seems well for the key, <code>false</code> if all seems not well for the key
     * @param msg message to add for the key
     * @throws AllSeemsWellException if an error occurs
     */
    public void allSeemsWell(String key, Boolean asw, String msg)
            throws AllSeemsWellException {

        logger.debug("allSeemsWell entry: key = {}, asw = {}, msg = {}", key, asw, msg);
        if (key == null || key.isEmpty()) {
            logger.error("allSeemsWell: 'key' has no visible content");
            throw new IllegalArgumentException("allSeemsWell: 'key' has no visible content");
        }
        if (asw == null) {
            logger.error("allSeemsWell: 'asw' is null");
            throw new IllegalArgumentException("allSeemsWell: 'asw' is null");
        }
        if (msg == null || msg.isEmpty()) {
            logger.error("allSeemsWell: 'msg' has no visible content");
            throw new IllegalArgumentException("allSeemsWell: 'msg' has no visible content");
        }

        if (allSeemsWellMap == null) {
            allSeemsWellMap = new HashMap<>();
        }

        if (allNotWellMap == null) {
            allNotWellMap = new HashMap<>();
        }

        if (asw) {
            logger.info("allSeemsWell: ALL SEEMS WELL: key = {}, msg = {}", key, msg);
            try {
                allSeemsWellMap.put(key, msg);
            } catch (Exception e) {
                String exceptMsg =
                        "allSeemsWell: encountered an exception with allSeemsWellMap.put(" + key + "," + msg + ")";
                logger.error(exceptMsg);
                throw new AllSeemsWellException(exceptMsg, e);
            }

            try {
                allNotWellMap.remove(key);
            } catch (Exception e) {
                String exceptMsg = "allSeemsWell: encountered an exception with allNotWellMap.delete(" + key + ")";
                logger.error(exceptMsg);
                throw new AllSeemsWellException(exceptMsg, e);
            }

        } else {
            logger.error("allSeemsWell: ALL NOT WELL: key = {}, msg = {}", key, msg);
            try {
                allSeemsWellMap.remove(key);
            } catch (Exception e) {
                String exceptMsg = "allSeemsWell: encountered an exception with allSeemsWellMap.remove(" + key + ")";
                logger.error(exceptMsg);
                throw new AllSeemsWellException(exceptMsg, e);
            }

            try {
                allNotWellMap.put(key, msg);
            } catch (Exception e) {
                String exceptMsg = "allSeemsWell: encountered an exception with allNotWellMap.put(" + key + msg + ")";
                logger.error(exceptMsg);
                throw new AllSeemsWellException(exceptMsg, e);
            }
        }

        if (logger.isDebugEnabled()) {
            for (Entry<String, String> entry : allSeemsWellMap.entrySet()) {
                logger.debug("allSeemsWellMap: key = {}  msg = {}", entry.getKey(), entry.getValue());
            }
            for (Entry<String, String> entry : allNotWellMap.entrySet()) {
                logger.debug("allNotWellMap: key = {}  msg = {}", entry.getKey(), entry.getValue());
            }
            logger.debug("allSeemsWell exit");
        }
    }

    /**
     * Converts the given value to milliseconds using the current {@link #propertyUnits}.
     *
     * @param value value to be converted, or -1
     * @return the value, in milliseconds, or -1
     */
    private static long toMillis(long value) {
        return (value < 0 ? -1 : value * 1000L);
    }

    public Map<String, String> getAllSeemsWellMap() {
        return allSeemsWellMap;
    }

    public Map<String, String> getAllNotWellMap() {
        return allNotWellMap;
    }
    
    // these methods may be overridden by junit tests

    /**
     * Indicates that the {@link FpManager#run()} method has started. This method
     * simply returns.
     * 
     * @throws InterruptedException can be interrupted
     */
    protected void runStarted() throws InterruptedException {
        // does nothing
    }

    /**
     * Indicates that a monitor activity has completed. This method simply returns.
     * 
     * @throws InterruptedException can be interrupted
     */
    protected void monitorCompleted() throws InterruptedException {
        // does nothing
    }

    /**
     * Get persistence unit.
     * 
     * @return the persistence unit to be used
     */
    protected String getPersistenceUnit() {
        return PERSISTENCE_UNIT;
    }

    /*
     * The remaining methods are used by JUnit tests.
     */

    public static boolean isUnitTesting() {
        return isUnitTesting;
    }

    public static void setUnitTesting(boolean isUnitTesting) {
        IntegrityMonitor.isUnitTesting = isUnitTesting;
    }
}
