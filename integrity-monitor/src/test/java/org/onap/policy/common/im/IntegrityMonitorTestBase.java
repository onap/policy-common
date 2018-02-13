/*
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.onap.policy.common.utils.jpa.EntityTransCloser;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All JUnits are designed to run in the local development environment where
 * they have write privileges and can execute time-sensitive tasks.
 * <p/>
 * Many of the test verification steps are performed by scanning for items
 * written to the log file. Rather than actually scan the log file, an
 * {@link ExtractAppender} is used to monitor events that are logged and extract
 * relevant items. In order to attach the appender to the debug log, it assumes
 * that the debug log is a <i>logback</i> Logger configured per EELF.
 * <p/>
 * These tests use a temporary, in-memory DB, which is dropped once the tests
 * complete.
 */
public class IntegrityMonitorTestBase {
	private static Logger logger = LoggerFactory.getLogger(IntegrityMonitorTestBase.class);

	/**
	 * Directory containing the slf4j log files.
	 */
	private static final String SLF4J_LOG_DIR = "logs";

	private static final String JMX_PORT_PROP = "com.sun.management.jmxremote.port";

	/**
	 * Max time, in milliseconds, to wait for a latch to be triggered.
	 */
	protected static final long WAIT_MS = 5000l;

	/**
	 * Milliseconds that monitor should sleep between cycles.
	 */
	protected static final long CYCLE_INTERVAL_MS = 2l;

	public static final String DEFAULT_DB_URL_PREFIX = "jdbc:h2:mem:";

	protected static final String dbDriver = "org.h2.Driver";
	protected static final String dbUser = "testu";
	protected static final String dbPwd = "testp";
	protected static final String siteName = "SiteA";
	protected static final String nodeType = "pap";

	// will be defined by the test *Classes*
	protected static String dbUrl;

	/**
	 * Persistence unit.
	 */
	protected static final String PERSISTENCE_UNIT = "schemaPU";

	/**
	 * Properties to be used in all tests.
	 */
	protected static Properties properties;

	/**
	 * Entity manager factory pointing to the in-memory DB for A_SEQ_PU.
	 */
	protected static EntityManagerFactory emf;

	/**
	 * Entity manager factory pointing to the in-memory DB associated with emf.
	 */
	protected static EntityManager em;

	/**
	 * Saved JMX port from system properties, to be restored once all tests
	 * complete.
	 */
	private static Object savedJmxPort;

	/**
	 * Saved IM persistence unit, to be restored once all tests complete.
	 */
	private static String savedPU;

	/**
	 * Saved monitor cycle interval, to be restored once all tests complete.
	 */
	private static long savedCycleIntervalMillis;

	/**
	 * Saved property time units, to be restored once all tests complete.
	 */
	private static TimeUnit savedPropertyUnits;

	/**
	 * Saves current configuration information and then sets new values.
	 * 
	 * @param dbDriver
	 *            the name of the DB Driver class
	 * @param dbUrl
	 *            the URL to the DB
	 * @throws IOException
	 * @throws Exception
	 */
	protected static void setUpBeforeClass(String dbUrl) throws IOException {
		logger.info("setup");

		Properties systemProps = System.getProperties();

		// truncate the logs
		new FileOutputStream(SLF4J_LOG_DIR + "/audit.log").close();
		new FileOutputStream(SLF4J_LOG_DIR + "/debug.log").close();
		new FileOutputStream(SLF4J_LOG_DIR + "/error.log").close();
		new FileOutputStream(SLF4J_LOG_DIR + "/metrics.log").close();

		IntegrityMonitorTestBase.dbUrl = dbUrl;

		// save data that we have to restore at the end of the test
		savedJmxPort = systemProps.get(JMX_PORT_PROP);
		savedPU = IntegrityMonitor.getPersistenceUnit();
		savedCycleIntervalMillis = IntegrityMonitor.getCycleIntervalMillis();
		savedPropertyUnits = IntegrityMonitor.getPropertyUnits();

		systemProps.put(JMX_PORT_PROP, "9797");

		IntegrityMonitor.setPersistenceUnit(PERSISTENCE_UNIT);
		IntegrityMonitor.setCycleIntervalMillis(CYCLE_INTERVAL_MS);
		IntegrityMonitor.setPropertyUnits(TimeUnit.MILLISECONDS);

		IntegrityMonitor.setUnitTesting(true);

		properties = new Properties();
		properties.put(IntegrityMonitorProperties.DB_DRIVER, dbDriver);
		properties.put(IntegrityMonitorProperties.DB_URL, dbUrl);
		properties.put(IntegrityMonitorProperties.DB_USER, dbUser);
		properties.put(IntegrityMonitorProperties.DB_PWD, dbPwd);
		properties.put(IntegrityMonitorProperties.SITE_NAME, siteName);
		properties.put(IntegrityMonitorProperties.NODE_TYPE, nodeType);
		properties.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS,
				String.valueOf(100L * CYCLE_INTERVAL_MS));

		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, makeProperties());

		// keep this open so the in-memory DB stays around until all tests are
		// done
		em = emf.createEntityManager();

		stopMonitor();
	}

	/**
	 * Restores the configuration to what it was before the test.
	 */
	protected static void tearDownAfterClass() {
		Properties systemProps = System.getProperties();
		if (savedJmxPort == null) {
			systemProps.remove(JMX_PORT_PROP);

		} else {
			systemProps.put(JMX_PORT_PROP, savedJmxPort);
		}

		IntegrityMonitor.setPersistenceUnit(savedPU);
		IntegrityMonitor.setCycleIntervalMillis(savedCycleIntervalMillis);
		IntegrityMonitor.setPropertyUnits(savedPropertyUnits);

		IntegrityMonitor.setUnitTesting(false);

		// this should result in the in-memory DB being deleted
		em.close();
		emf.close();
	}

	/**
	 * Sets up for a test, which includes deleting all records from the
	 * IntegrityAuditEntity table.
	 */
	protected void setUpTest() {

		// Clean up the DB
		try (EntityTransCloser etc = new EntityTransCloser(em.getTransaction())) {
			EntityTransaction et = etc.getTransation();

			em.createQuery("Delete from StateManagementEntity").executeUpdate();
			em.createQuery("Delete from ForwardProgressEntity").executeUpdate();
			em.createQuery("Delete from ResourceRegistrationEntity").executeUpdate();

			// commit transaction
			et.commit();
		}
	}

	/**
	 * Cleans up after a test, removing any ExtractAppenders from the logger and
	 * stopping any AuditThreads.
	 */
	protected void tearDownTest() {
		stopMonitor();
	}

	/**
	 * Stops the IntegrityMonitor instance.
	 */
	private static void stopMonitor() {
		try {
			IntegrityMonitor.deleteInstance();

		} catch (IntegrityMonitorException e) {
			// no need to log, as exception was already logged
		}
	}

	/**
	 * Makes a new Property set that's a clone of {@link #properties}.
	 * 
	 * @return a new Property set containing all of a copy of all of the
	 *         {@link #properties}
	 */
	protected static Properties makeProperties() {
		Properties props = new Properties();
		props.putAll(properties);
		return props;
	}

	/**
	 * Waits for a latch to reach zero.
	 * 
	 * @param latch
	 * @throws InterruptedException
	 * @throws AssertionError
	 *             if the latch did not reach zero in the allotted time
	 */
	protected void waitLatch(CountDownLatch latch) throws InterruptedException {
		assertTrue(latch.await(WAIT_MS, TimeUnit.SECONDS));
	}

	/**
	 * Applies a function on a monitor, expecting it to succeed. Catches any
	 * exceptions thrown by the function.
	 * 
	 * @param arg
	 * @param func
	 * @throws AssertionError
	 */
	protected <T> void assertNoException(T arg, VoidFunction<T> func) {
		try {
			func.apply(arg);

		} catch (Exception e) {
			System.out.println("startTransaction exception: " + e);
			fail("action failed");
		}
	}

	/**
	 * Applies a function on a monitor, expecting it to fail. Catches any
	 * exceptions thrown by the function.
	 * 
	 * @param arg
	 * @param func
	 * @throws AssertionError
	 */
	protected <T> void assertException(T arg, VoidFunction<T> func) {
		try {
			func.apply(arg);
			fail("missing exception");
		} catch (Exception e) {
			System.out.println("action found expected exception: " + e);
		}
	}

	@FunctionalInterface
	protected static interface VoidFunction<T> {
		public void apply(T arg) throws Exception;
	}
}
