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

package org.onap.policy.common.im;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;

import org.onap.policy.common.im.jpa.ForwardProgressEntity;
import org.onap.policy.common.im.jpa.ResourceRegistrationEntity;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.onap.policy.common.im.jmx.ComponentAdmin;
import org.onap.policy.common.im.jmx.ComponentAdminMBean;
import org.onap.policy.common.im.jmx.JmxAgentConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntegrityMonitor Main class for monitoring the integrity of a resource and
 * managing its state. State management follows the X.731 ITU standard.
 */
public class IntegrityMonitor {
	private static final Logger logger = LoggerFactory
			.getLogger(IntegrityMonitor.class.getName());

	// only allow one instance of IntegrityMonitor
	private static IntegrityMonitor instance = null;

	private static String resourceName = null;
	boolean alarmExists = false;

	/*
	 * Error message that is written by the dependencyCheck() method. It is made
	 * available externally through the evaluateSanity() method.
	 */
	private String dependencyCheckErrorMsg = "";

	// The entity manager factory for JPA access
	private EntityManagerFactory emf;
	private EntityManager em;

	// Persistence Unit for JPA
	private static final String PERSISTENCE_UNIT = "operationalPU";

	private StateManagement stateManager = null;

	private static final int CYCLE_INTERVAL_MILLIS = 1000;

	private FPManager fpManager = null;

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
	private long lastDependencyCheckTime = System.currentTimeMillis();

	// Time of the last state audit. It is initialized at the time of the IM
	// construction
	private Date lastStateAuditTime = new Date();

	// Interval between state audits in ms. We leave it turned off by default so
	// that it will only
	// be run on the nodes which we want doing the audit. In particular, we only
	// want it to run
	// on the droolspdps
	private static long stateAuditIntervalMs = 0L;

	// the number of cycles since 'fpCounter' was last changed
	private int missedCycles = 0;

	// forward progress monitoring interval
	private static int monitorInterval = IntegrityMonitorProperties.DEFAULT_MONITOR_INTERVAL;
	// The number of periods the counter fails to increment before an alarm is
	// raised.
	private static int failedCounterThreshold = IntegrityMonitorProperties.DEFAULT_FAILED_COUNTER_THRESHOLD;
	// test transaction interval
	private static int testTransInterval = IntegrityMonitorProperties.DEFAULT_TEST_INTERVAL;
	// write Fpc to DB interval
	private static int writeFpcInterval = IntegrityMonitorProperties.DEFAULT_WRITE_FPC_INTERVAL;
	// check the health of dependencies
	private static int checkDependencyInterval = IntegrityMonitorProperties.DEFAULT_CHECK_DEPENDENCY_INTERVAL;
	

	// A lead subsystem will have dependency groups with resource names in the
	// properties file.
	// For non-lead subsystems, the dependency_group property will be absent.
	private static String[] dep_groups = null;

	private static boolean isUnitTesting = false;
	
	// can turn on health checking of dependents via jmx test() call by setting
	// this property to true
	private static boolean testViaJmx = false;

	private static String jmxFqdn = null;

	// this is the max interval seconds allowed without any forward progress
	// counter updates
	private static int maxFpcUpdateInterval = IntegrityMonitorProperties.DEFAULT_MAX_FPC_UPDATE_INTERVAL;

	// Node types
	private enum NodeType {
		pdp_xacml, pdp_drools, pap, pap_admin, logparser, brms_gateway, astra_gateway, elk_server, pypdp

	}

	private static String site_name;
	private static String node_type;
	private Date refreshStateAuditLastRunDate;
	private static long refreshStateAuditIntervalMs = 600000; // run it once per
																// 10 minutes

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
	private final Object IMFLUSHLOCK = new Object();
	
	private Map<String, String> allSeemsWellMap;
	private Map<String, String> allNotWellMap;

	/**
	 * Get an instance of IntegrityMonitor for a given resource name. It creates
	 * one if it does not exist. Only one instance is allowed to be created per
	 * resource name.
	 * 
	 * @param resourceName
	 *            The resource name of the resource
	 * @param properties
	 *            a set of properties passed in from the resource
	 * @return The new instance of IntegrityMonitor
	 * @throws Exception
	 *             if unable to create jmx url or the constructor returns an
	 *             exception
	 */
	public static IntegrityMonitor getInstance(String resourceName,
			Properties properties) throws Exception {
		synchronized (getInstanceLock) {
			if (logger.isDebugEnabled()) {
				logger.debug("getInstance() called - resourceName= {}",
						resourceName);
			}
			if (resourceName == null || resourceName.isEmpty()
					|| properties == null) {
				logger.error("Error: getIntegrityMonitorInstance() called with invalid input");
				return null;
			}

			if (instance == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Creating new instance of IntegrityMonitor");
				}
				instance = new IntegrityMonitor(resourceName, properties);
			}
			return instance;
		}
	}

	public static IntegrityMonitor getInstance() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("getInstance() called");
		}
		if (instance == null) {
			String msg = "No IntegrityMonitor instance exists."
					+ " Please use the method IntegrityMonitor.getInstance(String resourceName, Properties properties)";
			throw new IntegrityMonitorPropertiesException(msg);
		} else {
			return instance;
		}
	}

	/*
	 * This is a facility used by JUnit testing to destroy the IntegrityMonitor
	 * instance before creating a new one. It includes a delay of 2 seconds to
	 * allow the FPManager to fully exit.
	 */
	public static void deleteInstance() throws IntegrityMonitorException {
		if (logger.isDebugEnabled()) {
			logger.debug("deleteInstance() called");
		}
		if (isUnitTesting()) {
			// Stop the FPManager thread

			if (instance != null && instance.getFPManager() != null) {
				instance.getFPManager().stopAndExit();

				try {
					// Make sure it has exited
					Thread.sleep(2 * CYCLE_INTERVAL_MILLIS);
				} catch (InterruptedException e) {
					logger.error("deleteInstance: Interrupted while waiting for FPManaager to fully exit");
					e.printStackTrace();
				}
				if (instance.getFPManager().isAlive()) {
					logger.error("IntegrityMonitor.deleteInstance() Failed to kill FPManager thread");
					throw new IntegrityMonitorException(
							"IntegrityMonitor.deleteInstance() Failed to kill FPManager thread");
				}
				instance = null;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("deleteInstance() exit");
		}
	}

	private FPManager getFPManager() {
		return fpManager;
	}

	/**
	 * IntegrityMonitor constructor. It is invoked from the getInstance() method
	 * in this class or from the constructor of a child or sub-class. A class
	 * can extend the IntegrityMonitor class if there is a need to override any
	 * of the base methods (ex. subsystemTest()). Only one instance is allowed
	 * to be created per resource name.
	 * 
	 * @param resourceName
	 *            The resource name of the resource
	 * @param properties
	 *            a set of properties passed in from the resource
	 * @throws Exception
	 *             if any errors are encountered in the consructor
	 */
	protected IntegrityMonitor(String resourceName, Properties properties)
			throws Exception {

		// singleton check since this constructor can be called from a child or
		// sub-class
		if (instance != null) {
			String msg = "IM object exists and only one instance allowed";
			logger.error("{}", msg);
			throw new IntegrityMonitorException(
					"IntegrityMonitor constructor exception: " + msg);
		}
		instance = this;

		IntegrityMonitor.resourceName = resourceName;

		/*
		 * Validate that the properties file contains all the needed properties.
		 * Throws an IntegrityMonitorPropertiesException
		 */
		validateProperties(properties);

		// construct jmx url
		String jmxUrl = getJmxUrl();

		//
		// Create the entity manager factory
		//
		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT,
				properties);
		//
		// Did it get created?
		//
		if (emf == null) {
			logger.error(
					"Error creating IM entity manager factory with persistence unit: {}",
					PERSISTENCE_UNIT);
			throw new IntegrityMonitorException(
					"Unable to create IM Entity Manager Factory");
		}

		// add entry to forward progress and resource registration tables in DB

		// Start a transaction
		em = emf.createEntityManager();
		EntityTransaction et = em.getTransaction();

		et.begin();

		try {
			// if ForwardProgress entry exists for resourceName, update it. If
			// not found, create a new entry
			Query fquery = em
					.createQuery("Select f from ForwardProgressEntity f where f.resourceName=:rn");
			fquery.setParameter("rn", resourceName);

			@SuppressWarnings("rawtypes")
			List fpList = fquery.setLockMode(LockModeType.NONE)
					.setFlushMode(FlushModeType.COMMIT).getResultList();
			ForwardProgressEntity fpx = null;
			if (!fpList.isEmpty()) {
				// ignores multiple results
				fpx = (ForwardProgressEntity) fpList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(fpx);
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Resource {} exists and will be updated - old fpc= {}, lastUpdated= {}",
							resourceName, fpx.getFpcCount(),
							fpx.getLastUpdated());
				}
				fpx.setFpcCount(fpCounter);
			} else {
				// Create a forward progress object
				if (logger.isDebugEnabled()) {
					logger.debug("Adding resource {} to ForwardProgress table",
							resourceName);
				}
				fpx = new ForwardProgressEntity();
			}
			// update/set columns in entry
			fpx.setResourceName(resourceName);
			em.persist(fpx);
			// flush to the DB
			synchronized (IMFLUSHLOCK) {
				em.flush();
			}

			// if ResourceRegistration entry exists for resourceName, update it.
			// If not found, create a new entry
			Query rquery = em
					.createQuery("Select r from ResourceRegistrationEntity r where r.resourceName=:rn");
			rquery.setParameter("rn", resourceName);

			@SuppressWarnings("rawtypes")
			List rrList = rquery.setLockMode(LockModeType.NONE)
					.setFlushMode(FlushModeType.COMMIT).getResultList();
			ResourceRegistrationEntity rrx = null;
			if (!rrList.isEmpty()) {
				// ignores multiple results
				rrx = (ResourceRegistrationEntity) rrList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(rrx);
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Resource {} exists and will be updated - old url= {}, createdDate={}",
							resourceName, rrx.getResourceUrl(),
							rrx.getCreatedDate());
				}
				rrx.setLastUpdated(new Date());
			} else {
				// register resource by adding entry to table in DB
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Adding resource {} to ResourceRegistration table",
							resourceName);
				}
				rrx = new ResourceRegistrationEntity();
			}
			// update/set columns in entry
			rrx.setResourceName(resourceName);
			rrx.setResourceUrl(jmxUrl);
			rrx.setNodeType(node_type);
			rrx.setSite(site_name);
			em.persist(rrx);
			// flush to the DB
			synchronized (IMFLUSHLOCK) {
				et.commit();
			}

		} catch (Exception e) {
			logger.error(
					"IntegrityMonitor constructor DB table update failed with exception: ",
					e);
			try {
				if (et.isActive()) {
					synchronized (IMFLUSHLOCK) {
						et.rollback();
					}
				}
			} catch (Exception e1) {
				logger.error("IntegrityMonitor constructor threw exception: ",
						e1);
			}
			throw e;
		}

		// create instance of StateMangement class and pass emf to it
		stateManager = new StateManagement(emf, resourceName);

		/**
		 * Initialize the state and status attributes. This will maintain any
		 * Administrative state value but will set the operational state =
		 * enabled, availability status = null, standby status = null. The
		 * integrity monitor will set the operational state via the FPManager
		 * and the owning application must set the standby status by calling
		 * promote/demote on the StateManager.
		 */
		stateManager.initializeState();

		// create management bean
		try {
			new ComponentAdmin(resourceName, this, stateManager);
		} catch (Exception e) {
			logger.error("ComponentAdmin constructor exception: {}",
					e.toString(), e);
		}

		fpManager = new FPManager();

	}

	private static String getJmxUrl() throws IntegrityMonitorException {

		// get the jmx remote port and construct the JMX URL
		Properties systemProps = System.getProperties();
		String jmx_port = systemProps
				.getProperty("com.sun.management.jmxremote.port");
		String jmx_err_msg;
		if (jmx_port == null) {
			jmx_err_msg = "System property com.sun.management.jmxremote.port for JMX remote port is not set";
			logger.error("{}", jmx_err_msg);
			throw new IntegrityMonitorException("getJmxUrl exception: "
					+ jmx_err_msg);
		}

		int port = 0;
		try {
			port = Integer.parseInt(jmx_port);
		} catch (NumberFormatException e) {
			jmx_err_msg = "JMX remote port is not a valid integer value - "
					+ jmx_port;
			logger.error("{}", jmx_err_msg);
			throw new IntegrityMonitorException("getJmxUrl exception: "
					+ jmx_err_msg);
		}

		try {
			if (jmxFqdn == null) {
				jmxFqdn = InetAddress.getLocalHost().getCanonicalHostName(); // get
																				// FQDN
																				// of
																				// this
																				// host
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
		String jmx_url = "service:jmx:rmi:///jndi/rmi://" + jmxFqdn + ":"
				+ port + "/jmxrmi";

		if (logger.isDebugEnabled()) {
			logger.debug("IntegerityMonitor - jmx url={}", jmx_url);
		}

		return jmx_url;
	}

	/**
	 * evaluateSanity() is designed to be called by an external entity to
	 * evealuate the sanity of the node. It checks the operational and
	 * administrative states and the standby status. If the operational state is
	 * disabled, it will include the dependencyCheckErrorMsg which includes
	 * information about any dependency (node) which has failed.
	 */
	public void evaluateSanity() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("evaluateSanity called ....");
		}
		synchronized (evaluateSanityLock) {

			String error_msg = dependencyCheckErrorMsg;
			if (logger.isDebugEnabled()) {
				logger.debug("evaluateSanity dependencyCheckErrorMsg = {}",
						error_msg);
			}
			// check op state and throw exception if disabled
			if ((stateManager.getOpState() != null)
					&& stateManager.getOpState().equals(
							StateManagement.DISABLED)) {
				String msg = "Resource " + resourceName
						+ " operation state is disabled. " + error_msg;
				if (logger.isDebugEnabled()) {
					logger.debug("{}", msg);
				}
				throw new IntegrityMonitorException(msg);
			}

			// check admin state and throw exception if locked
			if ((stateManager.getAdminState() != null)
					&& stateManager.getAdminState().equals(
							StateManagement.LOCKED)) {
				String msg = "Resource " + resourceName
						+ " is administratively locked";
				if (logger.isDebugEnabled()) {
					logger.debug("{}", msg);
				}
				throw new AdministrativeStateException(
						"IntegrityMonitor Admin State Exception: " + msg);
			}
			// check standby state and throw exception if cold standby
			if ((stateManager.getStandbyStatus() != null)
					&& stateManager.getStandbyStatus().equals(
							StateManagement.COLD_STANDBY)) {
				String msg = "Resource " + resourceName + " is cold standby";
				if (logger.isDebugEnabled()) {
					logger.debug("{}", msg);
				}
				throw new StandbyStatusException(
						"IntegrityMonitor Standby Status Exception: " + msg);
			}

		}

	}

	/*
	 * This method checks the forward progress counter and the state of a
	 * dependency. If the dependency is unavailable or failed, an error message
	 * is created which is checked when evaluateSanity interface is called. If
	 * the error message is set then the evaluateSanity will return an error.
	 */
	public String stateCheck(String dep) {
		if (logger.isDebugEnabled()) {
			logger.debug("checking state of dependent resource: {}", dep);
		}
		String error_msg = null;
		ForwardProgressEntity forwardProgressEntity = null;
		StateManagementEntity stateManagementEntity = null;

		// Start a transaction
		EntityTransaction et = em.getTransaction();
		et.begin();

		try {
			Query query = em
					.createQuery("Select p from ForwardProgressEntity p where p.resourceName=:resource");
			query.setParameter("resource", dep);

			@SuppressWarnings("rawtypes")
			List fpList = query.setLockMode(LockModeType.NONE)
					.setFlushMode(FlushModeType.COMMIT).getResultList();

			if (!fpList.isEmpty()) {
				// exists
				forwardProgressEntity = (ForwardProgressEntity) fpList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(forwardProgressEntity);
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Found entry in ForwardProgressEntity table for dependent Resource={}",
							dep);
				}
			} else {
				error_msg = dep
						+ ": resource not found in ForwardProgressEntity database table";
				logger.error("{}", error_msg);
			}
			synchronized (IMFLUSHLOCK) {
				et.commit();
			}
		} catch (Exception ex) {
			// log an error
			error_msg = dep
					+ ": ForwardProgressEntity DB operation failed with exception: ";
			logger.error("{}", error_msg, ex);
			synchronized (IMFLUSHLOCK) {
				if (et.isActive()) {
					et.rollback();
				}
			}
		}

		if (error_msg == null) {
			// Start a transaction
			et = em.getTransaction();
			et.begin();
			try {
				// query if StateManagement entry exists for dependent resource
				Query query = em
						.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
				query.setParameter("resource", dep);

				@SuppressWarnings("rawtypes")
				List smList = query.setLockMode(LockModeType.NONE)
						.setFlushMode(FlushModeType.COMMIT).getResultList();
				if (!smList.isEmpty()) {
					// exist
					stateManagementEntity = (StateManagementEntity) smList
							.get(0);
					// refresh the object from DB in case cached data was
					// returned
					em.refresh(stateManagementEntity);
					if (logger.isDebugEnabled()) {
						logger.debug(
								"Found entry in StateManagementEntity table for dependent Resource={}",
								dep);
					}
				} else {
					error_msg = dep
							+ ": resource not found in state management entity database table";
					logger.error("{}", error_msg);
				}

				synchronized (IMFLUSHLOCK) {
					et.commit();
				}
			} catch (Exception e) {
				// log an error
				error_msg = dep
						+ ": StateManagementEntity DB read failed with exception: ";
				logger.error("{}", error_msg, e);
				synchronized (IMFLUSHLOCK) {
					if (et.isActive()) {
						et.rollback();
					}
				}
			}
		}

		// verify that the ForwardProgress is current (check last_updated)
		if (error_msg == null) {
			if (forwardProgressEntity != null && stateManagementEntity != null) {
				Date date = new Date();
				long diffMs = date.getTime()
						- forwardProgressEntity.getLastUpdated().getTime();
				if (logger.isDebugEnabled()) {
					logger.debug("IntegrityMonitor.stateCheck(): diffMs = {}",
							diffMs);
				}

				// Threshold for a stale entry
				long staleMs = 1000L * maxFpcUpdateInterval;
				if (logger.isDebugEnabled()) {
					logger.debug("IntegrityMonitor.stateCheck(): staleMs = {}",
							staleMs);
				}

				if (diffMs > staleMs) {
					// ForwardProgress is stale. Disable it
					try {
						if (!stateManagementEntity.getOpState().equals(
								StateManagement.DISABLED)) {
							if (logger.isDebugEnabled()) {
								logger.debug(
										"IntegrityMonitor.stateCheck(): Changing OpStat = disabled for {}",
										dep);
							}
							stateManager.disableFailed(dep);
						}
					} catch (Exception e) {
						String msg = "IntegrityMonitor.stateCheck(): Failed to diableFail dependent resource = "
								+ dep + "; " + e.getMessage();
						logger.error("{}", msg, e);
					}
				}
			} else {

				if (forwardProgressEntity == null) {
					String msg = "IntegrityMonitor.stateCheck(): Failed to diableFail dependent resource = "
							+ dep + "; " + " forwardProgressEntity == null.";
					logger.error("{}", msg);
				}

				else {
					String msg = "IntegrityMonitor.stateCheck(): Failed to diableFail dependent resource = "
							+ dep + "; " + " stateManagementEntity == null.";
					logger.error("{}", msg);
				}
			}
		}

		// check operation, admin and standby states of dependent resource
		if (error_msg == null) {
			if (stateManagementEntity != null) {
				if ((stateManager.getAdminState() != null)
						&& stateManagementEntity.getAdminState().equals(
								StateManagement.LOCKED)) {
					error_msg = dep + ": resource is administratively locked";
					logger.error("{}", error_msg);
				} else if ((stateManager.getOpState() != null)
						&& stateManagementEntity.getOpState().equals(
								StateManagement.DISABLED)) {
					error_msg = dep + ": resource is operationally disabled";
					logger.error("{}", error_msg);
				} else if ((stateManager.getStandbyStatus() != null)
						&& stateManagementEntity.getStandbyStatus().equals(
								StateManagement.COLD_STANDBY)) {
					error_msg = dep + ": resource is cold standby";
					logger.error("{}", error_msg);
				}
			} else {
				error_msg = dep
						+ ": could not check standy state of resource. stateManagementEntity == null.";
				logger.error("{}", error_msg);
			}
		}

		String returnMsg = "IntegrityMonitor.stateCheck(): returned error_msg: "
				+ error_msg;
		if (logger.isDebugEnabled()) {
			logger.debug("{}", returnMsg);
		}
		return error_msg;
	}

	private String fpCheck(String dep) {
		if (logger.isDebugEnabled()) {
			logger.debug(
					"checking forward progress count of dependent resource: {}",
					dep);
		}

		String error_msg = null;

		// check FPC count - a changing FPC count indicates the resource JVM is
		// running

		// Start a transaction
		EntityTransaction et = em.getTransaction();
		et.begin();
		try {
			Query fquery = em
					.createQuery("Select f from ForwardProgressEntity f where f.resourceName=:rn");
			fquery.setParameter("rn", dep);

			@SuppressWarnings("rawtypes")
			List fpList = fquery.setLockMode(LockModeType.NONE)
					.setFlushMode(FlushModeType.COMMIT).getResultList();
			ForwardProgressEntity fpx;
			if (!fpList.isEmpty()) {
				// ignores multiple results
				fpx = (ForwardProgressEntity) fpList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(fpx);
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Dependent resource {} - fpc= {}, lastUpdated={}",
							dep, fpx.getFpcCount(), fpx.getLastUpdated());
				}
				long currTime = System.currentTimeMillis();
				// if dependent resource FPC has not been updated, consider it
				// an error
				if ((currTime - fpx.getLastUpdated().getTime()) > (1000 * maxFpcUpdateInterval)) {
					error_msg = dep
							+ ": FP count has not been updated in the last "
							+ maxFpcUpdateInterval + " seconds";
					logger.error("{}", error_msg);
					try {
						// create instance of StateMangement class for dependent
						StateManagement depStateManager = new StateManagement(
								emf, dep);
						if (!depStateManager.getOpState().equals(
								StateManagement.DISABLED)) {
							if (logger.isDebugEnabled()) {
								logger.debug(
										"Forward progress not detected for dependent resource {}. Setting dependent's state to disable failed.",
										dep);
							}
							depStateManager.disableFailed();
						}
					} catch (Exception e) {
						// ignore errors
						logger.error(
								"Update dependent state failed with exception: ",
								e);
					}
				}
			} else {
				// resource entry not found in FPC table
				error_msg = dep
						+ ": resource not found in ForwardProgressEntity table in the DB";
				logger.error("{}", error_msg);
			}
			synchronized (IMFLUSHLOCK) {
				et.commit();
			}
		} catch (Exception e) {
			// log an error and continue
			error_msg = dep
					+ ": ForwardProgressEntity DB read failed with exception: ";
			logger.error("{}", error_msg, e);
			synchronized (IMFLUSHLOCK) {
				if (et.isActive()) {
					et.rollback();
				}
			}
		}

		return error_msg;
	}

	public ArrayList<ForwardProgressEntity> getAllForwardProgressEntity() {
		if (logger.isDebugEnabled()) {
			logger.debug("getAllForwardProgressEntity: entry");
		}
		ArrayList<ForwardProgressEntity> fpList = new ArrayList<>();
		// Start a transaction
		EntityTransaction et = em.getTransaction();
		et.begin();
		try {
			Query fquery = em
					.createQuery("Select e from ForwardProgressEntity e");
			@SuppressWarnings("rawtypes")
			List myList = fquery.setLockMode(LockModeType.NONE)
					.setFlushMode(FlushModeType.COMMIT).getResultList();
			synchronized (IMFLUSHLOCK) {
				et.commit();
			}
			if (logger.isDebugEnabled()) {
				logger.debug("getAllForwardProgressEntity: myList.size(): {}",
						myList.size());
			}
			if (!myList.isEmpty()) {
				for (int i = 0; i < myList.size(); i++) {
					if (logger.isDebugEnabled()) {
						logger.debug(
								"getAllForwardProgressEntity: myList.get({}).getResourceName(): {}",
								i, ((ForwardProgressEntity) myList.get(i))
										.getResourceName());
					}
					fpList.add((ForwardProgressEntity) myList.get(i));
				}
			}
			synchronized (IMFLUSHLOCK) {
				if (et.isActive()) {
					et.commit();
				}
			}
		} catch (Exception e) {
			// log an error and continue
			String msg = "getAllForwardProgessEntity DB read failed with exception: ";
			logger.error("{}", msg, e);
			synchronized (IMFLUSHLOCK) {
				if (et.isActive()) {
					et.rollback();
				}
			}
		}
		return fpList;
	}

	private String jmxCheck(String dep) {
		if (logger.isDebugEnabled()) {
			logger.debug(
					"checking health of dependent by calling test() via JMX on resource: {}",
					dep);
		}

		String error_msg = null;

		// get the JMX URL from the database
		String jmxUrl = null;
		// Start a transaction
		EntityTransaction et = em.getTransaction();
		et.begin();
		try {
			// query if ResourceRegistration entry exists for resourceName
			Query rquery = em
					.createQuery("Select r from ResourceRegistrationEntity r where r.resourceName=:rn");
			rquery.setParameter("rn", dep);

			@SuppressWarnings("rawtypes")
			List rrList = rquery.setLockMode(LockModeType.NONE)
					.setFlushMode(FlushModeType.COMMIT).getResultList();
			ResourceRegistrationEntity rrx = null;

			if (!rrList.isEmpty()) {
				// ignores multiple results
				rrx = (ResourceRegistrationEntity) rrList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(rrx);
				jmxUrl = rrx.getResourceUrl();
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Dependent Resource={}, url={}, createdDate={}",
							dep, jmxUrl, rrx.getCreatedDate());
				}
			} else {
				error_msg = dep
						+ ": resource not found in ResourceRegistrationEntity table in the DB";
				logger.error("{}", error_msg);
			}

			synchronized (IMFLUSHLOCK) {
				et.commit();
			}
		} catch (Exception e) {
			error_msg = dep
					+ ": ResourceRegistrationEntity DB read failed with exception: ";
			logger.error("{}", error_msg, e);
			synchronized (IMFLUSHLOCK) {
				if (et.isActive()) {
					et.rollback();
				}
			}
		}

		if (jmxUrl != null) {
			JmxAgentConnection jmxAgentConnection = null;
			try {
				jmxAgentConnection = new JmxAgentConnection(jmxUrl);
				MBeanServerConnection mbeanServer = jmxAgentConnection
						.getMBeanConnection();
				ComponentAdminMBean admin = JMX.newMXBeanProxy(mbeanServer,
						ComponentAdmin.getObjectName(dep),
						ComponentAdminMBean.class);

				// invoke the test method via the jmx proxy
				admin.test();
				if (logger.isDebugEnabled()) {
					logger.debug("Dependent resource {} sanity test passed",
							dep);
				}
			} catch (Exception e) {
				error_msg = dep
						+ ": resource sanity test failed with exception: ";
				logger.error("{}", error_msg, e);
			} finally {
				// close the JMX connector
				if (jmxAgentConnection != null) {
					jmxAgentConnection.disconnect();
				}
			}
		}

		return error_msg;
	}

	public String dependencyCheck() {
		if (logger.isDebugEnabled()) {
			logger.debug("dependencyCheck: entry");
		}
		synchronized (dependencyCheckLock) {

			// Start with the error message empty
			String error_msg = "";
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
								"{}: There has been a subsystemTest failure with error:{} Updating this resource's state to disableDependency",
								resourceName, e.getMessage());
					}
					// Capture the subsystemTest failure info
					if (!error_msg.isEmpty()) {
						error_msg = error_msg.concat(",");
					}
					error_msg = error_msg.concat(resourceName + ": "
							+ e.getMessage());
					this.stateManager.disableDependency();
				} catch (Exception ex) {
					logger.error("IntegrityMonitor threw exception.", ex);
					if (!error_msg.isEmpty()) {
						error_msg = error_msg.concat(",");
					}
					error_msg = error_msg
							.concat("\n"
									+ resourceName
									+ ": Failed to disable dependency after subsystemTest failure due to: "
									+ ex.getMessage());
				}
			}

			// Check the sanity of dependents for lead subcomponents
			if (dep_groups != null && dep_groups.length > 0) {
				// check state of resources in dependency groups
				for (String group : dep_groups) {
					group = group.trim();
					if (group.isEmpty()) {
						// ignore empty group
						continue;
					}
					String[] dependencies = group.split(",");
					if (logger.isDebugEnabled()) {
						logger.debug("group dependencies = {}",
								Arrays.toString(dependencies));
					}
					int real_dep_count = 0;
					int fail_dep_count = 0;
					for (String dep : dependencies) {
						dep = dep.trim();
						if (dep.isEmpty()) {
							// ignore empty dependency
							continue;
						}
						real_dep_count++; // this is a valid dependency whose
											// state is tracked
						String fail_msg = fpCheck(dep); // if a resource is
														// down, its FP count
														// will not be
														// incremented
						if (fail_msg == null) {
							if (testViaJmx) {
								fail_msg = jmxCheck(dep);
							} else {
								fail_msg = stateCheck(dep);
							}
						}
						if (fail_msg != null) {
							fail_dep_count++;
							if (!error_msg.isEmpty()) {
								error_msg = error_msg.concat(", ");
							}
							error_msg = error_msg.concat(fail_msg);
						}
					}// end for (String dep : dependencies)

					// if all dependencies in a group are failed, set this
					// resource's state to disable dependency
					if ((real_dep_count > 0)
							&& (fail_dep_count == real_dep_count)) {
						dependencyFailure = true;
						try {
							if(logger.isDebugEnabled()){
								logger.debug("All dependents in group {} have failed their health check. Updating this resource's state to disableDependency", group);
							}					
							if(stateManager.getAvailStatus()== null || !( (stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY) ||
									(stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY_FAILED) ) ){
								// Note: redundant calls are made by refreshStateAudit
								this.stateManager.disableDependency();
							}
						} catch (Exception e) {
							logger.error("IntegrityMonitor threw exception.", e);
							if (!error_msg.isEmpty()) {
								error_msg = error_msg.concat(",");
							}
							error_msg = error_msg.concat(resourceName
									+ ": Failed to disable dependency");
							break; // break out on failure and skip checking
									// other groups
						}
					}
					// check the next group

				}// end for (String group : dep_groups)

				/*
				 * We have checked all the dependency groups. If all are ok and
				 * subsystemTest passed, dependencyFailure == false
				 */
				if (!dependencyFailure) {
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("All dependency groups have at least one viable member. Updating this resource's state to enableNoDependency");
						}
						if(stateManager.getAvailStatus() != null && ((stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY) ||
								(stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY_FAILED)) ){
							// Note: redundant calls are made by refreshStateAudit
							this.stateManager.enableNoDependency();	
						} // The refreshStateAudit will catch the case where it is disabled but availStatus != failed
					} catch (Exception e) {
						logger.error("IntegrityMonitor threw exception.", e);
						if (!error_msg.isEmpty()) {
							error_msg = error_msg.concat(",");
						}
						error_msg = error_msg.concat(resourceName
								+ ": Failed to enable no dependency");
					}
				}
			} else if (!dependencyFailure) {
				/*
				 * This is put here to clean up when no dependency group should
				 * exist, but one was erroneously added which caused the state
				 * to be disabled/dependency/coldstandby and later removed. We
				 * saw this happen in the lab, but is not very likely in a
				 * production environment...but you never know.
				 */
				try {
					if (logger.isDebugEnabled()) {
						logger.debug("There are no dependents. Updating this resource's state to enableNoDependency");
					}
					if(stateManager.getAvailStatus() != null && ((stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY) ||
							(stateManager.getAvailStatus()).equals(StateManagement.DEPENDENCY_FAILED)) ){
						// Note: redundant calls are made by refreshStateAudit
						this.stateManager.enableNoDependency();
					}// The refreshStateAudit will catch the case where it is
						// disabled but availStatus != failed
				} catch (Exception e) {
					logger.error("IntegrityMonitor threw exception.", e);
					if (!error_msg.isEmpty()) {
						error_msg = error_msg.concat(",");
					}
					error_msg = error_msg.concat(resourceName
							+ ": Failed to enable no dependency");
				}
			}

			if (!error_msg.isEmpty()) {
				logger.error(
						"Sanity failure detected in a dependent resource: {}",
						error_msg);

			}

			dependencyCheckErrorMsg = error_msg;
			lastDependencyCheckTime = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("dependencyCheck: exit");
			}
			return error_msg;
		}
	}

	/**
	 * Execute a test transaction. It is called when the test transaction timer fires.
	 * It could be overridden to provide additional test functionality. If overridden,
	 * the overriding method must invoke startTransaction() and endTransaction() and 
	 * check if the allNotWellMap is empty.
	 */
	public void testTransaction() {
		synchronized (testTransactionLock){
			if(logger.isDebugEnabled()){
				logger.debug("testTransaction: entry");
			}
			//
			// startTransaction() not required for testTransaction
			//

			// end transaction - increments local FP counter
			endTransaction();
		}
	}

	/**
	 * Additional testing for subsystems that do not have a /test interface (for
	 * ex. 3rd party processes like elk). This method would be overridden by the
	 * subsystem.
	 */
	public void subsystemTest() throws IntegrityMonitorException {
		// Testing provided by subsystem
		if (logger.isDebugEnabled()) {
			logger.debug("IntegrityMonitor subsystemTest() OK");
		}
	}

	/**
	 * Checks admin state and resets transaction timer. Called by application at
	 * the start of a transaction.
	 * 
	 * @throws AdministrativeStateException
	 *             throws admin state exception if resource is locked
	 * @throws StandbyStatusException
	 */
	public void startTransaction() throws AdministrativeStateException,
			StandbyStatusException {

		synchronized (startTransactionLock) {
			// check admin state and throw exception if locked
			if ((stateManager.getAdminState() != null)
					&& stateManager.getAdminState().equals(
							StateManagement.LOCKED)) {
				String msg = "Resource " + resourceName
						+ " is administratively locked";

				throw new AdministrativeStateException(
						"IntegrityMonitor Admin State Exception: " + msg);
			}
			// check standby state and throw exception if locked

			if ((stateManager.getStandbyStatus() != null)
					&& (stateManager.getStandbyStatus().equals(
							StateManagement.HOT_STANDBY) || stateManager
							.getStandbyStatus().equals(
									StateManagement.COLD_STANDBY))) {
				String msg = "Resource " + resourceName + " is standby";

				throw new StandbyStatusException(
						"IntegrityMonitor Standby Status Exception: " + msg);
			}

			// reset transactionTimer so it will not fire
			elapsedTestTransTime = 0;
		}
	}

	/**
	 * Increment the local forward progress counter. Called by application at
	 * the end of each transaction (successful or not).
	 */
	public void endTransaction() {
		synchronized(endTransactionLock){
			if(getAllNotWellMap() != null){
				if(!(getAllNotWellMap().isEmpty())){
					/*
					 * An entity has reported that it is not well.  We must not
					 * allow the the forward progress counter to advance.
					 */
					String msg = "allNotWellMap:";
					for(String key: allNotWellMap.keySet()){
						msg = msg.concat("\nkey = " + key + " msg = " + allNotWellMap.get(key));
					}
					logger.error("endTransaction: allNotWellMap is NOT EMPTY.  Not advancing forward"
							+ "progress counter. \n{}\n", msg);
					return;
				}else{
					if(logger.isDebugEnabled()){
						if(getAllSeemsWellMap() != null){
							if(!(getAllSeemsWellMap().isEmpty())){
								String msg = "allSeemsWellMap:";
								for(String key: allSeemsWellMap.keySet()){
									msg = msg.concat("\nkey = " + key + " msg = " + allSeemsWellMap.get(key));
								}
								logger.debug("endTransaction: allNotWellMap IS EMPTY and allSeemsWellMap is NOT EMPTY.  Advancing forward"
										+ "progress counter. \n{}\n", msg);
							}
						}
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
			Query fquery = em
					.createQuery("Select f from ForwardProgressEntity f where f.resourceName=:rn");
			fquery.setParameter("rn", resourceName);

			@SuppressWarnings("rawtypes")
			List fpList = fquery.setLockMode(LockModeType.NONE)
					.setFlushMode(FlushModeType.COMMIT).getResultList();
			ForwardProgressEntity fpx;
			if (!fpList.isEmpty()) {
				// ignores multiple results
				fpx = (ForwardProgressEntity) fpList.get(0);
				// refresh the object from DB in case cached data was returned
				em.refresh(fpx);
				if (logger.isDebugEnabled()) {
					logger.debug(
							"Updating FP entry: Resource={}, fpcCount={}, lastUpdated={}, new fpcCount={}",
							resourceName, fpx.getFpcCount(),
							fpx.getLastUpdated(), fpCounter);
				}
				fpx.setFpcCount(fpCounter);
				em.persist(fpx);
				// flush to the DB and commit
				synchronized (IMFLUSHLOCK) {
					et.commit();
				}
			} else {
				// Error - FP entry does not exist
				String msg = "FP entry not found in database for resource "
						+ resourceName;
				throw new IntegrityMonitorException(msg);
			}
		} catch (Exception e) {
			try {
				synchronized (IMFLUSHLOCK) {
					if (et.isActive()) {
						et.rollback();
					}
				}
			} catch (Exception e1) {
				logger.error("IntegrityMonitor threw exception.", e1);
			}
			logger.error("writeFpc DB table commit failed with exception: {}",
					e);
			throw e;
		}
	}

	// retrieve state manager reference
	public final StateManagement getStateManager() {
		return this.stateManager;
	}

	/**
	 * Read and validate properties
	 * 
	 * @throws Exception
	 */
	private static void validateProperties(Properties prop)
			throws IntegrityMonitorPropertiesException {

		if (prop.getProperty(IntegrityMonitorProperties.DB_DRIVER) == null) {
			String msg = IntegrityMonitorProperties.DB_DRIVER
					+ " property is null";
			logger.error("{}", msg);
			throw new IntegrityMonitorPropertiesException(
					"IntegrityMonitor Property Exception: " + msg);
		}

		if (prop.getProperty(IntegrityMonitorProperties.DB_URL) == null) {
			String msg = IntegrityMonitorProperties.DB_URL
					+ " property is null";
			logger.error("{}", msg);
			throw new IntegrityMonitorPropertiesException(
					"IntegrityMonitor Property Exception: " + msg);
		}

		if (prop.getProperty(IntegrityMonitorProperties.DB_USER) == null) {
			String msg = IntegrityMonitorProperties.DB_USER
					+ " property is null";
			logger.error("{}", msg);
			throw new IntegrityMonitorPropertiesException(
					"IntegrityMonitor Property Exception: " + msg);
		}

		if (prop.getProperty(IntegrityMonitorProperties.DB_PWD) == null) {
			String msg = IntegrityMonitorProperties.DB_PWD
					+ " property is null";
			logger.error("{}", msg);
			throw new IntegrityMonitorPropertiesException(
					"IntegrityMonitor Property Exception: " + msg);
		}

		if (prop.getProperty(IntegrityMonitorProperties.FP_MONITOR_INTERVAL) != null) {
			try {
				monitorInterval = Integer.parseInt(prop.getProperty(
						IntegrityMonitorProperties.FP_MONITOR_INTERVAL).trim());
			} catch (NumberFormatException e) {
				logger.warn("Ignored invalid property: {}",
						IntegrityMonitorProperties.FP_MONITOR_INTERVAL, e);
			}
		}

		if (prop.getProperty(IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD) != null) {
			try {
				failedCounterThreshold = Integer.parseInt(prop.getProperty(
						IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD)
						.trim());
			} catch (NumberFormatException e) {
				logger.warn("Ignored invalid property: {}",
						IntegrityMonitorProperties.FAILED_COUNTER_THRESHOLD, e);
			}
		}

		if (prop.getProperty(IntegrityMonitorProperties.TEST_TRANS_INTERVAL) != null) {
			try {
				testTransInterval = Integer.parseInt(prop.getProperty(
						IntegrityMonitorProperties.TEST_TRANS_INTERVAL).trim());
			} catch (NumberFormatException e) {
				logger.warn("Ignored invalid property: {}",
						IntegrityMonitorProperties.TEST_TRANS_INTERVAL, e);
			}
		}

		if (prop.getProperty(IntegrityMonitorProperties.WRITE_FPC_INTERVAL) != null) {
			try {
				writeFpcInterval = Integer.parseInt(prop.getProperty(
						IntegrityMonitorProperties.WRITE_FPC_INTERVAL).trim());
			} catch (NumberFormatException e) {
				logger.warn("Ignored invalid property: {}",
						IntegrityMonitorProperties.WRITE_FPC_INTERVAL, e);
			}
		}

		if (prop.getProperty(IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL) != null) {
			try {
				checkDependencyInterval = Integer.parseInt(prop.getProperty(
						IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL).trim());
			} catch (NumberFormatException e) {
				logger.warn("Ignored invalid property: {}",
						IntegrityMonitorProperties.CHECK_DEPENDENCY_INTERVAL, e);
			}
		}

		// dependency_groups are a semi-colon separated list of groups
		// each group is a comma separated list of resource names
		// For ex. dependency_groups = site_1.pap_1,site_1.pap_2 ; site_1.pdp_1,
		// site_1.pdp_2
		if (prop.getProperty(IntegrityMonitorProperties.DEPENDENCY_GROUPS) != null) {
			try {
				dep_groups = prop.getProperty(
						IntegrityMonitorProperties.DEPENDENCY_GROUPS)
						.split(";");
				if (logger.isDebugEnabled()) {
					logger.debug("dependency groups property = {}",
							Arrays.toString(dep_groups));
				}
			} catch (Exception e) {
				logger.warn("Ignored invalid property: {}",
						IntegrityMonitorProperties.DEPENDENCY_GROUPS, e);
			}
		}

		site_name = prop.getProperty(IntegrityMonitorProperties.SITE_NAME);
		if (site_name == null) {
			String msg = IntegrityMonitorProperties.SITE_NAME
					+ " property is null";
			logger.error("{}", msg);
			throw new IntegrityMonitorPropertiesException(
					"IntegrityMonitor Property Exception: " + msg);
		} else {
			site_name = site_name.trim();
		}

		node_type = prop.getProperty(IntegrityMonitorProperties.NODE_TYPE);
		if (node_type == null) {
			String msg = IntegrityMonitorProperties.NODE_TYPE
					+ " property is null";
			logger.error("{}", msg);
			throw new IntegrityMonitorPropertiesException(
					"IntegrityMonitor Property Exception: " + msg);
		} else {
			node_type = node_type.trim();
			if (!isNodeTypeEnum(node_type)) {
				String msg = IntegrityMonitorProperties.NODE_TYPE
						+ " property " + node_type + " is invalid";
				logger.error("{}", msg);
				throw new IntegrityMonitorPropertiesException(
						"IntegrityMonitor Property Exception: " + msg);
			}
		}

		if (prop.getProperty(IntegrityMonitorProperties.TEST_VIA_JMX) != null) {
			String jmx_test = prop.getProperty(
					IntegrityMonitorProperties.TEST_VIA_JMX).trim();
			testViaJmx = Boolean.parseBoolean(jmx_test);
		}

		if (prop.getProperty(IntegrityMonitorProperties.JMX_FQDN) != null) {
			jmxFqdn = prop.getProperty(IntegrityMonitorProperties.JMX_FQDN)
					.trim();
			if (jmxFqdn.isEmpty()) {
				jmxFqdn = null;
			}
		}

		if (prop.getProperty(IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL) != null) {
			try {
				maxFpcUpdateInterval = Integer.parseInt(prop.getProperty(
						IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL)
						.trim());
			} catch (NumberFormatException e) {
				logger.warn("Ignored invalid property: {}",
						IntegrityMonitorProperties.MAX_FPC_UPDATE_INTERVAL, e);
			}
		}

		if (prop.getProperty(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS) != null) {
			try {
				stateAuditIntervalMs = Long
						.parseLong(prop
								.getProperty(IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS));
			} catch (NumberFormatException e) {
				logger.warn("Ignored invalid property: {}",
						IntegrityMonitorProperties.STATE_AUDIT_INTERVAL_MS, e);
			}
		}

		if (prop.getProperty(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS) != null) {
			try {
				refreshStateAuditIntervalMs = Long
						.parseLong(prop
								.getProperty(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS));
			} catch (NumberFormatException e) {
				logger.warn(
						"Ignored invalid property: {}",
						IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS,
						e);
			}
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("IntegrityMonitor.validateProperties(): Property values \n"
					+ "maxFpcUpdateInterval = {}\n", maxFpcUpdateInterval);
		}

		return;
	}

	public static void updateProperties(Properties newprop) {
		if (isUnitTesting()) {
			try {
				validateProperties(newprop);
			} catch (IntegrityMonitorPropertiesException e) {
				logger.error("IntegrityMonitor threw exception.", e);
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Update integrity monitor properties not allowed");
			}
		}
	}

	private static boolean isNodeTypeEnum(String nodeType) {
		for (NodeType n : NodeType.values()) {
			if (n.toString().equals(nodeType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Look for "Forward Progress" -- if the 'FPMonitor' is stalled for too
	 * long, the operational state is changed to 'Disabled', and an alarm is
	 * set. The state is restored when forward progress continues.
	 */
	private void fpMonitorCycle() {
		if(logger.isDebugEnabled()){
			logger.debug("fpMonitorCycle(): entry");
		}
		synchronized (fpMonitorCycleLock) {
			// monitoring interval checks
			if (monitorInterval <= 0) {
				if(logger.isDebugEnabled()){
					logger.debug("fpMonitorCycle(): disabled");
				}
				elapsedTime = 0;
				return; // monitoring is disabled
			}

			elapsedTime = elapsedTime
					+ TimeUnit.MILLISECONDS.toSeconds(CYCLE_INTERVAL_MILLIS);
			if (elapsedTime < monitorInterval) {
				return; // monitoring interval not reached
			}

			elapsedTime = 0; // reset elapsed time

			try {
				if (fpCounter == lastFpCounter) {
					// no forward progress
					missedCycles += 1;
					if (missedCycles >= failedCounterThreshold && !alarmExists) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"Forward progress not detected for resource {}. Setting state to disable failed.",
									resourceName);
						}
						if (!(stateManager.getOpState())
								.equals(StateManagement.DISABLED)) {
							// Note: The refreshStateAudit will make redundant
							// calls
							stateManager.disableFailed();
						}// The refreshStateAudit will catch the case where
							// opStat = disabled and availState !
							// failed/dependency.failed
						alarmExists = true;
					}
				} else {
					// forward progress has occurred
					lastFpCounter = fpCounter;
					missedCycles = 0;
					// set op state to enabled
					if (logger.isDebugEnabled()) {
						logger.debug(
								"Forward progress detected for resource {}. Setting state to enable not failed.",
								resourceName);
					}
					if (!(stateManager.getOpState())
							.equals(StateManagement.ENABLED)) {
						// Note: The refreshStateAudit will make redundant calls
						stateManager.enableNotFailed();
					}// The refreshStateAudit will catch the case where
						// opState=enabled and availStatus != null
					alarmExists = false;
				}
			} catch (Exception e) {
				// log error
				logger.error("FP Monitor encountered error. ", e);
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("fpMonitorCycle(): exit");
		}
	}

	/**
	 * Look for "Forward Progress" on other nodes. If they are not making
	 * forward progress, check their operational state. If it is not disabled,
	 * then disable them.
	 */
	private void stateAudit() {
		if(logger.isDebugEnabled()){
			logger.debug("IntegrityMonitor.stateAudit(): entry"); 
		}
		if (stateAuditIntervalMs <= 0) {
			if(logger.isDebugEnabled()){
				logger.debug("IntegrityMonitor.stateAudit(): disabled"); 
			}
			return; // stateAudit is disabled
		}
		
		//Only run from nodes that are operational
		if(stateManager.getOpState().equals(StateManagement.DISABLED)){
			if(logger.isDebugEnabled()){
				logger.debug("IntegrityMonitor.stateAudit(): DISABLED. returning"); 
			}
			return;
		}
		if(stateManager.getAdminState().equals(StateManagement.LOCKED)){
			if(logger.isDebugEnabled()){
				logger.debug("IntegrityMonitor.stateAudit(): LOCKED. returning"); 
			}
			return;
		}
		if(!stateManager.getStandbyStatus().equals(StateManagement.NULL_VALUE) &&
				stateManager.getStandbyStatus()!= null){
			if(!stateManager.getStandbyStatus().equals(StateManagement.PROVIDING_SERVICE)){
				if(logger.isDebugEnabled()){
					logger.debug("IntegrityMonitor.stateAudit(): NOT PROVIDING_SERVICE. returning"); 
				}
				return;
			}
		}

		Date date = new Date();		
		long timeSinceLastStateAudit = date.getTime() - lastStateAuditTime.getTime(); 
		if (timeSinceLastStateAudit < stateAuditIntervalMs){
			if(logger.isDebugEnabled()){
				logger.debug("IntegrityMonitor.stateAudit(): Not time to run. returning"); 
			}
			return;
		}
		
		executeStateAudit();

		lastStateAuditTime = date;
		
		if(logger.isDebugEnabled()){
			logger.debug("IntegrityMonitor.stateAudit(): exit"); 
		}
	}// end stateAudit()
	
	public void executeStateAudit(){
		if(logger.isDebugEnabled()){
			logger.debug("IntegrityMonitor.executeStateAudit(): entry"); 
		}
		Date date = new Date();

		// Get all entries in the forwardprogressentity table
		ArrayList<ForwardProgressEntity> fpList = getAllForwardProgressEntity();

		// Check if each forwardprogressentity entry is current
		for(ForwardProgressEntity fpe : fpList){
			//If the this is my ForwardProgressEntity, continue
			if(fpe.getResourceName().equals(IntegrityMonitor.resourceName)){
				continue;
			}
			//Make sure you are not getting a cached version
			em.refresh(fpe);
			long diffMs = date.getTime() - fpe.getLastUpdated().getTime();
			if(logger.isDebugEnabled()){
				logger.debug("IntegrityMonitor.executeStateAudit(): resource = {}, diffMs = {}", fpe.getResourceName(), diffMs);
			}

			//Threshold for a stale entry
			long staleMs = 1000L * maxFpcUpdateInterval;
			if(logger.isDebugEnabled()){
				logger.debug("IntegrityMonitor.executeStateAudit(): resource = {}, staleMs = {}", fpe.getResourceName(), staleMs);
			}

			if(diffMs > staleMs){
				//ForwardProgress is stale.  Disable it
				// Start a transaction
				if(logger.isDebugEnabled()){
					logger.debug("IntegrityMonitor.executeStateAudit(): resource = {}, FPC is stale. Disabling it" );
				}
				EntityTransaction et = em.getTransaction();
				et.begin();
				StateManagementEntity sme = null;
				try {
					// query if StateManagement entry exists for fpe resource
					Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
					query.setParameter("resource", fpe.getResourceName());

					@SuppressWarnings("rawtypes")
					List smList = query.setLockMode(
							LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
					if (!smList.isEmpty()) {
						// exists
						sme = (StateManagementEntity) smList.get(0);
						// refresh the object from DB in case cached data was returned
						em.refresh(sme);
						if(logger.isDebugEnabled()){
							logger.debug("IntegrityMonitor.executeStateAudit(): Found entry in StateManagementEntity table for Resource={}", sme.getResourceName());
						}
					} else {
						String msg = "IntegrityMonitor.executeStateAudit(): " + fpe.getResourceName() + ": resource not found in state management entity database table";
							logger.error("{}", msg);
					}
					synchronized(IMFLUSHLOCK){
						et.commit();
					}
				} catch (Exception e) {
					// log an error
					logger.error("IntegrityMonitor.executeStateAudit(): {}: StateManagementEntity DB read failed with exception: ", fpe.getResourceName(), e);
					synchronized(IMFLUSHLOCK){
						if(et.isActive()){
							et.rollback();
						}
					}
				}

				if(sme != null && !sme.getOpState().equals(StateManagement.DISABLED)){
					if(logger.isDebugEnabled()){	
						logger.debug("IntegrityMonitor.executeStateAudit(): Changing OpStat = disabled for {}", sme.getResourceName());
					}
						try {
							stateManager.disableFailed(sme.getResourceName());
						} catch (Exception e) {
							String msg = "IntegrityMonitor.executeStateAudit(): Failed to disable " + sme.getResourceName();
							logger.error("{}", msg, e);
						}
				}
			}// end if(diffMs > staleMs)
		}// end for(ForwardProgressEntity fpe : fpList)
		if(logger.isDebugEnabled()){
			logger.debug("IntegrityMonitor.executeStateAudit(): exit"); 
		}
	}

	/**
	 * Execute a test transaction when test transaction interval has elapsed.
	 */
	private void checkTestTransaction() {
		if(logger.isDebugEnabled()){
			logger.debug("checkTestTransaction(): entry");
		}
		synchronized (checkTestTransactionLock) {

			// test transaction timer checks
			if (testTransInterval <= 0) {
				if(logger.isDebugEnabled()){
					logger.debug("checkTestTransaction(): disabled");
				}
				elapsedTestTransTime = 0;
				return; // test transaction is disabled
			}

			elapsedTestTransTime = elapsedTestTransTime
					+ TimeUnit.MILLISECONDS.toSeconds(CYCLE_INTERVAL_MILLIS);
			if (elapsedTestTransTime < testTransInterval) {
				return; // test transaction interval not reached
			}

			elapsedTestTransTime = 0; // reset elapsed time

			// execute test transaction
			testTransaction();
		}
		if(logger.isDebugEnabled()){
			logger.debug("checkTestTransaction(): exit");
		}
	}

	/**
	 * Updates Fpc counter in database when write Fpc interval has elapsed.
	 */
	private void checkWriteFpc() {
		if(logger.isDebugEnabled()){
			logger.debug("checkWriteFpc(): entry");
		}
		synchronized (checkWriteFpcLock) {

			// test transaction timer checks
			if (writeFpcInterval <= 0) {
				if(logger.isDebugEnabled()){
					logger.debug("checkWriteFpc(): disabled");
				}
				elapsedWriteFpcTime = 0;
				return; // write Fpc is disabled
			}

			elapsedWriteFpcTime = elapsedWriteFpcTime
					+ TimeUnit.MILLISECONDS.toSeconds(CYCLE_INTERVAL_MILLIS);
			if (elapsedWriteFpcTime < writeFpcInterval) {
				return; // write Fpc interval not reached
			}

			elapsedWriteFpcTime = 0; // reset elapsed time

			// write Fpc to database
			try {
				writeFpc();
			} catch (Exception e) {
				logger.error("IntegrityMonitor threw exception.", e);
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("checkWriteFpc(): exit");
		}
	}

	/**
	 * Execute a dependency health check periodically which also updates this
	 * resource's state.
	 */
	private void checkDependentHealth() {
		if (logger.isDebugEnabled()) {
			logger.debug("checkDependentHealth: entry");
		}
		if (checkDependencyInterval <=0) {
			if (logger.isDebugEnabled()) {
				logger.debug("checkDependentHealth: disabled");
			}
			return; // dependency monitoring is disabled
		}

		long currTime = System.currentTimeMillis();
		if (logger.isDebugEnabled()) {
			logger.debug(
					"checkDependentHealth currTime - lastDependencyCheckTime = {}",
					(currTime - lastDependencyCheckTime));
		}
		if ((currTime - lastDependencyCheckTime) > (1000 * checkDependencyInterval)) {
			// execute dependency check and update this resource's state

			dependencyCheck();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("checkDependentHealth: exit");
		}
	}

	/*
	 * This is a simple refresh audit which is periodically run to assure that
	 * the states and status attributes are aligned and notifications are sent
	 * to any listeners. It is possible for state/status to get out of synch and
	 * notified systems to be out of synch due to database corruption (manual or
	 * otherwise) or because a node became isolated.
	 * 
	 * When the operation (lock/unlock) is called, it will cause a re-evaluation
	 * of the state and send a notification to all registered observers.
	 */
	private void refreshStateAudit() {
		if(logger.isDebugEnabled()){
			logger.debug("refreshStateAudit(): entry");
		}
		if (refreshStateAuditIntervalMs <= 0) {
			// The audit is disabled
			if(logger.isDebugEnabled()){
				logger.debug("refreshStateAudit(): disabled");
			}
			return;
		}
		executeRefreshStateAudit();
		if(logger.isDebugEnabled()){
			logger.debug("refreshStateAudit(): exit");
		}
	}
	
	public void executeRefreshStateAudit(){
		if(logger.isDebugEnabled()){
			logger.debug("executeRefreshStateAudit(): entry");
		}
		synchronized (refreshStateAuditLock) {
			if (logger.isDebugEnabled()) {
				logger.debug("refreshStateAudit: entry");
			}
			Date now = new Date();
			long nowMs = now.getTime();
			long lastTimeMs = refreshStateAuditLastRunDate.getTime();
			if (logger.isDebugEnabled()) {
				logger.debug("refreshStateAudit: ms since last run = {}",
						(nowMs - lastTimeMs));
			}

			if ((nowMs - lastTimeMs) > refreshStateAuditIntervalMs) {
				String adminState = stateManager.getAdminState();
				if (logger.isDebugEnabled()) {
					logger.debug("refreshStateAudit: adminState = {}",
							adminState);
				}
				if (adminState.equals(StateManagement.LOCKED)) {
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("refreshStateAudit: calling lock()");
						}
						stateManager.lock();
					} catch (Exception e) {
						logger.error(
								"refreshStateAudit: caught unexpected exception from stateManager.lock(): ",
								e);
					}
				} else {// unlocked
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("refreshStateAudit: calling unlock()");
						}
						stateManager.unlock();
						;
					} catch (Exception e) {
						logger.error(
								"refreshStateAudit: caught unexpected exception from stateManager.unlock(): ",
								e);
					}
				}
				refreshStateAuditLastRunDate = new Date();
				if (logger.isDebugEnabled()) {
					logger.debug("refreshStateAudit: exit");
				}
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("executeRefreshStateAudit(): exit");
		}
	}

	public static boolean isUnitTesting() {
		return isUnitTesting;
	}

	public static void setUnitTesting(boolean isUnitTesting) {
		IntegrityMonitor.isUnitTesting = isUnitTesting;
	}

	/**
	 * The following nested class periodically performs the forward progress
	 * check, checks dependencies, does a refresh state audit and runs the
	 * stateAudit.
	 */
	class FPManager extends Thread {
		private volatile boolean exit = false;

		// Constructor - start FP manager thread
		FPManager() {
			// set now as the last time the refreshStateAudit ran
			IntegrityMonitor.this.refreshStateAuditLastRunDate = new Date();
			// start thread
			this.start();
		}

		@Override
		public void run() {
			if (logger.isDebugEnabled()) {
				logger.debug("FPManager thread running");
			}
			while (!exit) {
				try {
					Thread.sleep(CYCLE_INTERVAL_MILLIS);
				} catch (InterruptedException e) {
					// The 'sleep' call was interrupted
					if (logger.isDebugEnabled()) {
						logger.debug("IntegrityMonitor threw exception.", e);
					}
					Thread.currentThread().interrupt();
					continue;
				}

				try {
					if (logger.isDebugEnabled()) {
						logger.debug("FPManager calling fpMonitorCycle()");
					}
					// check forward progress timer
					IntegrityMonitor.this.fpMonitorCycle();

					if (logger.isDebugEnabled()) {
						logger.debug("FPManager calling checkTestTransaction()");
					}
					// check test transaction timer
					IntegrityMonitor.this.checkTestTransaction();

					if (logger.isDebugEnabled()) {
						logger.debug("FPManager calling checkWriteFpc()");
					}
					// check write Fpc timer
					IntegrityMonitor.this.checkWriteFpc();

					if (logger.isDebugEnabled()) {
						logger.debug("FPManager calling checkDependentHealth()");
					}
					// check dependency health
					IntegrityMonitor.this.checkDependentHealth();

					if (logger.isDebugEnabled()) {
						logger.debug("FPManager calling refreshStateAudit()");
					}
					// check if it is time to run the refreshStateAudit
					IntegrityMonitor.this.refreshStateAudit();

					if (logger.isDebugEnabled()) {
						logger.debug("FPManager calling stateAudit()");
					}
					// check if it is time to run the stateAudit
					IntegrityMonitor.this.stateAudit();

				} catch (Exception e) {
					logger.error(
							"Ignore FPManager thread processing timer(s) exception: ",
							e);
				}
			}
		}

		public void stopAndExit() {
			exit = true;
			// Referring to the FPManager instance
			FPManager.this.interrupt();
		}

	}

	public void allSeemsWell(@NotNull String key, @NotNull Boolean asw, @NotNull String msg)
			throws IllegalArgumentException, AllSeemsWellException {

		if(logger.isDebugEnabled()){
			logger.debug("allSeemsWell entry: key = {}, asw = {}, msg = {}", key, asw, msg);
		}
		if(key == null || key.isEmpty()){
			logger.error("allSeemsWell: 'key' has no visible content");
			throw new IllegalArgumentException("allSeemsWell: 'key' has no visible content");
		}
		if(asw == null){
			logger.error("allSeemsWell: 'asw' is null");
			throw new IllegalArgumentException("allSeemsWell: 'asw' is null");
		}
		if(msg == null || msg.isEmpty()){
			logger.error("allSeemsWell: 'msg' has no visible content");
			throw new IllegalArgumentException("allSeemsWell: 'msg' has no visible content");
		}
		
		if(allSeemsWellMap == null){
			allSeemsWellMap = new HashMap<>();
		}
		
		if(allNotWellMap == null){
			allNotWellMap = new HashMap<>();
		}

		if(asw){
			logger.info("allSeemsWell: ALL SEEMS WELL: key = {}, msg = {}", key, msg);
			try{
				allSeemsWellMap.put(key, msg);
			}catch(Exception e){
				String exceptMsg ="allSeemsWell: encountered an exception with allSeemsWellMap.put("
						+ key + "," + msg + ")";
				logger.error(exceptMsg);
				throw new AllSeemsWellException(exceptMsg, e);
			}

			try{
				allNotWellMap.remove(key);
			}catch(Exception e){
				String exceptMsg ="allSeemsWell: encountered an exception with allNotWellMap.delete("
						+ key + ")";
				logger.error(exceptMsg);
				throw new AllSeemsWellException(exceptMsg, e);
			}

		}else{
			logger.error("allSeemsWell: ALL NOT WELL: key = {}, msg = {}", key, msg);
			try{
				allSeemsWellMap.remove(key);
			}catch(Exception e){
				String exceptMsg ="allSeemsWell: encountered an exception with allSeemsWellMap.remove("
						+ key + ")";
				logger.error(exceptMsg);
				throw new AllSeemsWellException(exceptMsg, e);
			}

			try{
				allNotWellMap.put(key, msg);
			}catch(Exception e){
				String exceptMsg ="allSeemsWell: encountered an exception with allNotWellMap.put("
						+ key + msg + ")";
				logger.error(exceptMsg);
				throw new AllSeemsWellException(exceptMsg, e);
			}
		}

		if(logger.isDebugEnabled()){
			for(String mykey: allSeemsWellMap.keySet()){
				logger.debug("allSeemsWellMap: key = {}  msg = {}", mykey, allSeemsWellMap.get(mykey));							
			}
			for(String mykey: allNotWellMap.keySet()){
				logger.debug("allNotWellMap: key = {}  msg = {}", mykey, allNotWellMap.get(mykey));							
			}
			logger.debug("allSeemsWell exit");
		}
	}

	public Map<String, String> getAllSeemsWellMap(){
		return allSeemsWellMap;
	}

	public Map<String, String> getAllNotWellMap(){
		return allNotWellMap;
	}
}
