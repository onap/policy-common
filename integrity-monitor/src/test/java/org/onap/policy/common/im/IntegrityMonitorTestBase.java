/*
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * Modificaitons Copyright (C) 2023-2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.onap.policy.common.utils.jpa.EntityTransCloser;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;
import org.onap.policy.common.utils.time.CurrentTime;
import org.onap.policy.common.utils.time.TestTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * All JUnits are designed to run in the local development environment where they have write
 * privileges and can execute time-sensitive tasks. <p/> Many of the test verification steps are
 * performed by scanning for items written to the log file. Rather than actually scan the log file,
 * an {@link ExtractAppender} is used to monitor events that are logged and extract relevant items.
 * In order to attach the appender to the debug log, it assumes that the debug log is a
 * <i>logback</i> Logger configured per EELF. <p/> These tests use a temporary, in-memory DB, which
 * is dropped once the tests complete.
 */
public class IntegrityMonitorTestBase {
    private static Logger logger = LoggerFactory.getLogger(IntegrityMonitorTestBase.class);

    /**
     * Name of the instance field within the IntegrityMonitor class.
     */
    public static final String IM_INSTANCE_FIELD = "instance";

    /**
     * Name of the instance field within the MonitorTime class.
     */
    public static final String TIME_INSTANCE_FIELD = "instance";

    /**
     * Directory containing the slf4j log files.
     */
    private static final String SLF4J_LOG_DIR = "logs";

    private static final String JMX_PORT_PROP = "com.sun.management.jmxremote.port";

    /**
     * Max time, in milliseconds, to wait for a latch to be triggered.
     */
    protected static final long WAIT_MS = 5000L;

    /**
     * Milliseconds between state refreshes.
     */
    protected static final long REFRESH_INTERVAL_MS = 3L * IntegrityMonitor.CYCLE_INTERVAL_MILLIS;

    public static final String DEFAULT_DB_URL_PREFIX = "jdbc:h2:mem:";

    protected static final String DB_DRIVER = "org.h2.Driver";
    protected static final String DB_USER = "testu";
    protected static final String DB_PASS = "testp";
    protected static final String SITE_NAME = "SiteA";
    protected static final String NODE_TYPE = "pap";

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
     * Test time used by tests in lieu of CurrentTime.
     */
    private static TestTime testTime;

    /**
     * Saved JMX port from system properties, to be restored once all tests complete.
     */
    private static Object savedJmxPort;

    /**
     * Saved time accessor, to be restored once all tests complete.
     */
    private static CurrentTime savedTime;


    /**
     * Saves current configuration information and then sets new values.
     *
     * @param dbUrl the URL to the DB
     * @throws IOException if an IO error occurs
     */
    protected static void setUpBeforeClass(String dbUrl) throws IOException {
        logger.info("setup");

        final Properties systemProps = System.getProperties();

        // truncate the logs
        new FileOutputStream(SLF4J_LOG_DIR + "/audit.log").close();
        new FileOutputStream(SLF4J_LOG_DIR + "/debug.log").close();
        new FileOutputStream(SLF4J_LOG_DIR + "/error.log").close();
        new FileOutputStream(SLF4J_LOG_DIR + "/metrics.log").close();

        IntegrityMonitorTestBase.dbUrl = dbUrl;

        // save data that we have to restore at the end of the test
        savedJmxPort = systemProps.get(JMX_PORT_PROP);
        savedTime = MonitorTime.getInstance();

        systemProps.put(JMX_PORT_PROP, "9797");

        IntegrityMonitor.setUnitTesting(true);

        testTime = new TestTime();
        ReflectionTestUtils.setField(MonitorTime.class, TIME_INSTANCE_FIELD, testTime);

        properties = new Properties();
        properties.put(IntegrityMonitorProperties.DB_DRIVER, DB_DRIVER);
        properties.put(IntegrityMonitorProperties.DB_URL, dbUrl);
        properties.put(IntegrityMonitorProperties.DB_USER, DB_USER);
        properties.put(IntegrityMonitorProperties.DB_PWD, DB_PASS);
        properties.put(IntegrityMonitorProperties.SITE_NAME, SITE_NAME);
        properties.put(IntegrityMonitorProperties.NODE_TYPE, NODE_TYPE);
        properties.put(IntegrityMonitorProperties.REFRESH_STATE_AUDIT_INTERVAL_MS,
                String.valueOf(REFRESH_INTERVAL_MS));

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

        ReflectionTestUtils.setField(MonitorTime.class, TIME_INSTANCE_FIELD, savedTime);

        IntegrityMonitor.setUnitTesting(false);

        stopMonitor();

        // this should result in the in-memory DB being deleted
        em.close();
        emf.close();
    }

    /**
     * Sets up for a test, which includes deleting all records from the IntegrityAuditEntity table.
     */
    protected void setUpTest() {

        // Clean up the DB
        try (EntityTransCloser et = new EntityTransCloser(em.getTransaction())) {

            em.createQuery("Delete from StateManagementEntity").executeUpdate();
            em.createQuery("Delete from ForwardProgressEntity").executeUpdate();
            em.createQuery("Delete from ResourceRegistrationEntity").executeUpdate();

            // commit transaction
            et.commit();
        }
    }

    /**
     * Cleans up after a test, removing any ExtractAppenders from the logger and stopping any
     * AuditThreads.
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
            logger.debug("stopMonitor failed", e);
        }
    }

    /**
     * Get current test time.
     *
     * @return the "current" time, in milliseconds
     */
    protected static long getCurrentTestTime() {
        return testTime.getMillis();
    }

    /**
     * Makes a new Property set that's a clone of {@link #properties}.
     *
     * @return a new Property set containing all of a copy of all of the {@link #properties}
     */
    protected static Properties makeProperties() {
        Properties props = new Properties();
        props.putAll(properties);
        return props;
    }

    /**
     * Waits for a semaphore to be acquired.
     *
     * @param sem semaphore to wait on
     * @throws InterruptedException if the thread is interrupted
     * @throws AssertionError if the semaphore was not acquired within the allotted time
     */
    protected void waitSem(Semaphore sem) throws InterruptedException {
        assertTrue(sem.tryAcquire(WAIT_MS, TimeUnit.MILLISECONDS));
    }
}
