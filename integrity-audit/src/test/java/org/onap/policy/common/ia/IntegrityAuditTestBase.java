/*
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.onap.policy.common.utils.jpa.EntityMgrCloser;
import org.onap.policy.common.utils.jpa.EntityMgrFactoryCloser;
import org.onap.policy.common.utils.jpa.EntityTransCloser;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;
import org.slf4j.LoggerFactory;

/**
 * All JUnits are designed to run in the local development environment where they have write
 * privileges and can execute time-sensitive tasks. <p/> Many of the test verification steps are
 * performed by scanning for items written to the log file. Rather than actually scan the log file,
 * an {@link ExtractAppender} is used to monitor events that are logged and extract relevant items.
 * In order to attach the appender to the debug log, it assumes that the debug log is a
 * <i>logback</i> Logger configured per EELF. <p/> These tests use a temporary, in-memory DB, which
 * is dropped once the tests complete.
 */
public class IntegrityAuditTestBase {

    /**
     * Root of the debug logger, as defined in the logback-test.xml.
     */
    protected static final Logger debugLogger = (Logger) LoggerFactory.getLogger("com.att.eelf.debug");

    /**
     * Root of the error logger, as defined in the logback-test.xml.
     */
    protected static final Logger errorLogger = (Logger) LoggerFactory.getLogger("com.att.eelf.error");

    /**
     * Directory containing the log files.
     */
    private static final String LOG_DIR = "testingLogs/common-modules/integrity-audit";

    /**
     * Max time, in milliseconds, to wait for a latch to be triggered.
     */
    protected static final long WAIT_MS = 5000L;

    /**
     * Milliseconds that auditor should sleep between audit steps.
     */
    protected static final long SLEEP_INTERVAL_MS = 2L;

    /**
     * Milliseconds that auditor should sleep when an audit completes.
     */
    protected static final long COMPLETION_INTERVAL_MS = 5L;

    /**
     * Milliseconds that an entire audit-simulation cycles takes.
     */
    protected static final long AUDIT_SIMULATION_MS = SLEEP_INTERVAL_MS * AuditThread.AUDIT_SIMULATION_ITERATIONS;

    /**
     * Milliseconds that it takes for an auditor's last update to become stale. Includes a 1ms fudge
     * factor.
     */
    protected static final long STALE_MS = 1 + 2 * Math.max(COMPLETION_INTERVAL_MS, AUDIT_SIMULATION_MS);

    /**
     * Milliseconds that the db-audit should wait between makings updates.
     */
    private static final long DB_AUDIT_UPDATE_MS = 10L;

    /**
     * Milliseconds that the db-audit should sleep between cycles.
     */
    private static final long DB_AUDIT_SLEEP_MS = 3L;

    public static final String DEFAULT_DB_URL_PREFIX = "jdbc:h2:mem:";

    protected static final String dbDriver = "org.h2.Driver";
    protected static final String dbUser = "testu";
    protected static final String dbPwd = "testp";
    protected static final String siteName = "SiteA";
    protected static final String nodeType = "pdp_xacml";

    // will be defined by the test *Classes*
    protected static String dbUrl;

    /**
     * Persistence unit for PDP sequence A.
     */
    protected static final String A_SEQ_PU = "testPU";

    /**
     * Persistence unit for PDP sequence B.
     */
    protected static final String B_SEQ_PU = "integrityAuditPU";

    /**
     * Matches the start of an audit for arbitrary PDPs in the debug log.
     */
    protected static final String START_AUDIT_RE = "Starting audit simulation for resourceName=([^,]*)";

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
     * Saved debug logger level, to be restored once all tests complete.
     */
    private static Level savedDebugLevel;

    /**
     * Saved error logger level, to be restored once all tests complete.
     */
    private static Level savedErrorLevel;

    /**
     * Saved audit sleep interval, to be restored once all tests complete.
     */
    private static long savedSleepIntervalMs;

    /**
     * Saved audit completion interval, to be restored once all tests complete.
     */
    private static long savedCompletionIntervalMs;

    /**
     * Saved db audit update time, to be restored once all tests complete.
     */
    private static long savedDbAuditUpdateMs;

    /**
     * Saved db audit sleep time, to be restored once all tests complete.
     */
    private static long savedDbAuditSleepMs;

    /**
     * List of auditors whose threads must be stopped when a given test case ends.
     */
    private List<MyIntegrityAudit> auditors;

    /**
     * List of appenders that must be removed from loggers when a given test case ends.
     */
    private List<LogApp> appenders;

    /**
     * Saves current configuration information and then sets new values.
     * 
     * @param dbDriver the name of the DB Driver class
     * @param dbUrl the URL to the DB
     * @throws IOException if an IO error occurs
     */
    protected static void setUpBeforeClass(String dbUrl) throws IOException {

        // truncate the logs
        new FileOutputStream(LOG_DIR + "/audit.log").close();
        new FileOutputStream(LOG_DIR + "/debug.log").close();
        new FileOutputStream(LOG_DIR + "/error.log").close();
        new FileOutputStream(LOG_DIR + "/metrics.log").close();

        IntegrityAuditTestBase.dbUrl = dbUrl;

        // save data that we have to restore at the end of the test
        savedDebugLevel = debugLogger.getLevel();
        savedErrorLevel = errorLogger.getLevel();
        savedSleepIntervalMs = AuditThread.getAuditThreadSleepIntervalMillis();
        savedCompletionIntervalMs = AuditThread.getAuditCompletionIntervalMillis();
        savedDbAuditUpdateMs = DbAudit.getDbAuditUpdateMillis();
        savedDbAuditSleepMs = DbAudit.getDbAuditSleepMillis();

        AuditThread.setAuditThreadSleepIntervalMillis(SLEEP_INTERVAL_MS);
        AuditThread.setAuditCompletionIntervalMillis(COMPLETION_INTERVAL_MS);

        DbAudit.setDbAuditUpdateMillis(DB_AUDIT_UPDATE_MS);
        DbAudit.setDbAuditSleepMillis(DB_AUDIT_SLEEP_MS);

        IntegrityAudit.setUnitTesting(true);

        properties = new Properties();
        properties.put(IntegrityAuditProperties.DB_DRIVER, dbDriver);
        properties.put(IntegrityAuditProperties.DB_URL, dbUrl);
        properties.put(IntegrityAuditProperties.DB_USER, dbUser);
        properties.put(IntegrityAuditProperties.DB_PWD, dbPwd);
        properties.put(IntegrityAuditProperties.SITE_NAME, siteName);
        properties.put(IntegrityAuditProperties.NODE_TYPE, nodeType);

        emf = Persistence.createEntityManagerFactory(A_SEQ_PU, makeProperties());

        // keep this open so the in-memory DB stays around until all tests are
        // done
        em = emf.createEntityManager();

        debugLogger.setLevel(Level.DEBUG);
        errorLogger.setLevel(Level.ERROR);
    }

    /**
     * Restores the configuration to what it was before the test.
     */
    protected static void tearDownAfterClass() {
        AuditThread.setAuditThreadSleepIntervalMillis(savedSleepIntervalMs);
        AuditThread.setAuditCompletionIntervalMillis(savedCompletionIntervalMs);

        DbAudit.setDbAuditUpdateMillis(savedDbAuditUpdateMs);
        DbAudit.setDbAuditSleepMillis(savedDbAuditSleepMs);

        IntegrityAudit.setUnitTesting(false);

        debugLogger.setLevel(savedDebugLevel);
        errorLogger.setLevel(savedErrorLevel);

        // this should result in the in-memory DB being deleted
        em.close();
        emf.close();
    }

    /**
     * Sets up for a test, which includes deleting all records from the IntegrityAuditEntity table.
     */
    protected void setUp() {
        auditors = new LinkedList<>();
        appenders = new LinkedList<>();

        properties.put(IntegrityAuditProperties.AUDIT_PERIOD_MILLISECONDS, String.valueOf(SLEEP_INTERVAL_MS));

        // Clean up the DB
        try (EntityTransCloser etc = new EntityTransCloser(em.getTransaction())) {
            EntityTransaction et = etc.getTransation();

            em.createQuery("Delete from IntegrityAuditEntity").executeUpdate();

            // commit transaction
            et.commit();
        }
    }

    /**
     * Cleans up after a test, removing any ExtractAppenders from the logger and stopping any
     * AuditThreads.
     */
    protected void tearDown() {
        for (LogApp p : appenders) {
            p.detach();
        }

        for (MyIntegrityAudit p : auditors) {
            p.stopAuditThread();
        }
    }

    /**
     * Truncate the table.
     * 
     * @param properties the properties
     * @param persistenceUnit the persistence unit
     * @param tableName the name of the table
     */
    public void truncateTable(Properties properties, String persistenceUnit, String tableName) {

        try (EntityMgrFactoryCloser emfc =
                new EntityMgrFactoryCloser(Persistence.createEntityManagerFactory(persistenceUnit, properties));
                EntityMgrCloser emc = new EntityMgrCloser(emfc.getFactory().createEntityManager());
                EntityTransCloser etc = new EntityTransCloser(emc.getManager().getTransaction())) {

            EntityManager em = emc.getManager();
            EntityTransaction et = etc.getTransation();

            // Clean up the DB
            em.createQuery("Delete from " + tableName).executeUpdate();

            // commit transaction
            et.commit();
        }
    }

    /**
     * Verifies that items appear within the log, in order. A given item may appear more than once.
     * In addition, the log may contain extra items; those are ignored.
     * 
     * @param textre regular expression used to extract an item from a line in the log. The first
     *        "capture" group of the regular expression is assumed to contain the extracted item
     * @param items items that should be matched by the items extracted from the log, in order
     * @throws IOException if an IO error occurs
     * @throws AssertionError if the desired items were not all found
     */
    protected void verifyItemsInLog(ExtractAppender app, String... items) throws IOException {

        Iterator<String> it = new ArrayList<>(Arrays.asList(items)).iterator();
        if (!it.hasNext()) {
            return;
        }

        String expected = it.next();
        String last = null;

        for (String extractedText : app.getExtracted()) {
            if (extractedText.equals(expected)) {
                if (!it.hasNext()) {
                    // matched all of the items
                    return;
                }

                last = expected;
                expected = it.next();

            } else if (!extractedText.equals(last)) {
                List<String> remaining = getRemaining(expected, it);
                fail("missing items " + remaining + ", but was: " + extractedText);
            }
        }

        List<String> remaining = getRemaining(expected, it);
        assertTrue("missing items " + remaining, remaining.isEmpty());
    }

    /**
     * Gets the remaining items from an iterator.
     * 
     * @param current the current item, to be included within the list
     * @param it iterator from which to get the remaining items
     * @return a list of the remaining items
     */
    private LinkedList<String> getRemaining(String current, Iterator<String> it) {
        LinkedList<String> remaining = new LinkedList<>();
        remaining.add(current);

        while (it.hasNext()) {
            remaining.add(it.next());
        }
        return remaining;
    }

    /**
     * Waits for a thread to stop. If the thread doesn't complete in the allotted time, then it
     * interrupts it and waits again.
     * 
     * @param auditor the thread for which to wait
     * @return {@code true} if the thread stopped, {@code false} otherwise
     */
    public boolean waitThread(MyIntegrityAudit auditor) {
        if (auditor != null) {
            try {
                auditor.interrupt();

                if (!auditor.joinAuditThread(WAIT_MS)) {
                    System.out.println("failed to stop audit thread");
                    return false;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return true;
    }

    /**
     * Makes a new auditor.
     * 
     * @param resourceName2 the name of the resource
     * @param persistenceUnit2 the persistence unit
     * @return a new auditor
     * @throws Exception if an error occurs
     */
    protected MyIntegrityAudit makeAuditor(String resourceName2, String persistenceUnit2) throws Exception {
        return new MyIntegrityAudit(resourceName2, persistenceUnit2, makeProperties());
    }

    /**
     * Watches for patterns in a logger by attaching a ExtractAppender to it.
     * 
     * @param logger the logger to watch
     * @param regex regular expression used to extract relevant text
     * @return a new appender
     */
    protected ExtractAppender watch(Logger logger, String regex) {
        ExtractAppender app = new ExtractAppender(regex);
        appenders.add(new LogApp(logger, app));

        return app;
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
     * Waits for data to become stale and then runs an audit on several auditors in parallel.
     * 
     * @param auditors the auditors
     * @throws InterruptedException if a thread is interrupted
     */
    protected void waitStaleAndRun(MyIntegrityAudit... auditors) throws InterruptedException {
        waitStale();
        runAudit(auditors);
    }

    /**
     * Runs an audit on several auditors in parallel.
     * 
     * @param auditors the auditors
     * @throws InterruptedException if a thread is interrupted
     */
    protected void runAudit(MyIntegrityAudit... auditors) throws InterruptedException {

        // start an audit cycle on each auditor
        List<CountDownLatch> latches = new ArrayList<>(auditors.length);
        for (MyIntegrityAudit p : auditors) {
            latches.add(p.startAudit());
        }

        // wait for each auditor to complete its cycle
        for (CountDownLatch latch : latches) {
            waitLatch(latch);
        }
    }

    /**
     * Waits for a latch to reach zero.
     * 
     * @param latch the latch to wait for
     * @throws InterruptedException if the thread is interrupted
     * @throws AssertionError if the latch did not reach zero in the allotted time
     */
    protected void waitLatch(CountDownLatch latch) throws InterruptedException {
        assertTrue(latch.await(WAIT_MS, TimeUnit.SECONDS));
    }

    /**
     * Sleep a bit so that the currently designated pdp becomes stale.
     * 
     * @throws InterruptedException if the thread is interrupted
     */
    protected void waitStale() throws InterruptedException {
        Thread.sleep(STALE_MS);
    }

    /**
     * Tracks which appender has been added to a logger.
     */
    private static class LogApp {
        private final Logger logger;
        private final ExtractAppender appender;

        public LogApp(Logger logger, ExtractAppender appender) {
            this.logger = logger;
            this.appender = appender;

            logger.addAppender(appender);

            appender.start();
        }

        public void detach() {
            logger.detachAppender(appender);
        }
    }

    /**
     * Manages audits by inserting latches into a queue for the AuditThread to count.
     */
    protected class MyIntegrityAudit extends IntegrityAudit {

        /**
         * Queue from which the AuditThread will take latches.
         */
        private BlockingQueue<CountDownLatch> queue = null;

        /**
         * Constructs an auditor and starts the AuditThread.
         * 
         * @param resourceName the resource name
         * @param persistenceUnit the persistence unit
         * @param properties the properties
         * @throws Exception if an error occurs
         */
        public MyIntegrityAudit(String resourceName, String persistenceUnit, Properties properties) throws Exception {
            super(resourceName, persistenceUnit, properties);

            auditors.add(this);

            startAuditThread();
        }

        /**
         * Interrupts the AuditThread.
         */
        public void interrupt() {
            super.stopAuditThread();
        }

        /**
         * Triggers an audit by adding a latch to the queue.
         * 
         * @return the latch that was added
         * @throws InterruptedException if the thread is interrupted
         */
        public CountDownLatch startAudit() throws InterruptedException {
            CountDownLatch latch = new CountDownLatch(1);
            queue.add(latch);

            return latch;
        }

        /**
         * Starts a new AuditThread. Creates a new latch queue and associates it with the thread.
         */
        @Override
        public final void startAuditThread() throws IntegrityAuditException {
            if (queue != null) {
                // queue up a bogus latch, in case a thread is still running
                queue.add(new CountDownLatch(1) {
                    @Override
                    public void countDown() {
                        throw new RuntimeException("auditor has multiple threads");
                    }
                });
            }

            queue = new LinkedBlockingQueue<>();

            if (super.startAuditThread(queue)) {
                // wait for the thread to start
                CountDownLatch latch = new CountDownLatch(1);
                queue.add(latch);

                try {
                    waitLatch(latch);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IntegrityAuditException(e);
                }
            }
        }

        /**
         * Stops the AuditThread and waits for it to stop.
         * 
         * @throws AssertionError if the thread is still running
         */
        @Override
        public void stopAuditThread() {
            super.stopAuditThread();

            assertTrue(waitThread(this));
        }
    }
}
