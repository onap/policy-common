/*
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.onap.policy.common.utils.jpa.EntityMgrCloser;
import org.onap.policy.common.utils.jpa.EntityMgrFactoryCloser;
import org.onap.policy.common.utils.jpa.EntityTransCloser;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;
import org.onap.policy.common.utils.time.CurrentTime;
import org.onap.policy.common.utils.time.TestTime;
import org.powermock.reflect.Whitebox;
import org.slf4j.LoggerFactory;

/**
 * All JUnits are designed to run in the local development environment where they have write
 * privileges and can execute time-sensitive tasks.
 *
 * <p>Many of the test verification steps are performed by scanning for items written to the log
 * file. Rather than actually scan the log file, an {@link ExtractAppender} is used to monitor
 * events that are logged and extract relevant items. In order to attach the appender to the debug
 * log, it assumes that the debug log is a <i>logback</i> Logger configured per EELF.
 *
 * <p>These tests use a temporary, in-memory DB, which is dropped once the tests complete.
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
     * Name of the field within the AuditorTime class that supplies the time.
     */
    public static final String TIME_SUPPLY_FIELD = "supplier";


    /**
     * Max time, in milliseconds, to wait for a semaphore.
     */
    protected static final long WAIT_MS = 5000L;

    /**
     * Number of seconds in an audit period.
     */
    public static final int AUDIT_PERIOD_SEC = 5;

    public static final String DEFAULT_DB_URL_PREFIX = "jdbc:h2:mem:";

    protected static final String DB_DRIVER = "org.h2.Driver";
    protected static final String DB_USER = "testu";
    protected static final String DB_PASS = "testp";
    protected static final String SITE_NAME = "SiteA";
    protected static final String NODE_TYPE = "pdp_xacml";

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
    protected static final String START_AUDIT_RE = "Running audit for persistenceUnit=\\w+ on resourceName=([^,]*)";

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
     * Current time used by given test.
     */
    private static ThreadLocal<TestTime> testTime = ThreadLocal.withInitial(() -> null);

    /**
     * Supplies the test time so that each thread maintains its own notion of "current"
     * time.
     */
    private static Supplier<TestTime> timeSupplier = () -> testTime.get();

    /**
     * Saved debug logger level, to be restored once all tests complete.
     */
    private static Level savedDebugLevel;

    /**
     * Saved error logger level, to be restored once all tests complete.
     */
    private static Level savedErrorLevel;

    /**
     * Saved time, to be restored once all tests complete.
     */
    private static Supplier<CurrentTime> savedTime;

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
        savedTime = Whitebox.getInternalState(AuditorTime.class, TIME_SUPPLY_FIELD);
        savedDebugLevel = debugLogger.getLevel();
        savedErrorLevel = errorLogger.getLevel();

        IntegrityAudit.setUnitTesting(true);

        properties = new Properties();
        properties.put(IntegrityAuditProperties.DB_DRIVER, DB_DRIVER);
        properties.put(IntegrityAuditProperties.DB_URL, dbUrl);
        properties.put(IntegrityAuditProperties.DB_USER, DB_USER);
        properties.put(IntegrityAuditProperties.DB_PWD, DB_PASS);
        properties.put(IntegrityAuditProperties.SITE_NAME, SITE_NAME);
        properties.put(IntegrityAuditProperties.NODE_TYPE, NODE_TYPE);

        emf = Persistence.createEntityManagerFactory(A_SEQ_PU, makeProperties());

        // keep this open so the in-memory DB stays around until all tests are
        // done
        em = emf.createEntityManager();

        Whitebox.setInternalState(AuditorTime.class, TIME_SUPPLY_FIELD, timeSupplier);
        debugLogger.setLevel(Level.DEBUG);
        errorLogger.setLevel(Level.ERROR);
    }

    /**
     * Restores the configuration to what it was before the test.
     */
    protected static void tearDownAfterClass() {

        IntegrityAudit.setUnitTesting(false);

        Whitebox.setInternalState(AuditorTime.class, TIME_SUPPLY_FIELD, savedTime);
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

        properties.put(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS, String.valueOf(AUDIT_PERIOD_SEC));

        TestTime time = new TestTime();
        testTime.set(time);

        // Clean up the DB
        try (EntityTransCloser etc = new EntityTransCloser(em.getTransaction())) {
            EntityTransaction et = etc.getTransaction();

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
     * Get the test time.
     *
     * @return the {@link TestTime} in use by this thread
     */
    public static TestTime getTestTime() {
        return testTime.get();
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

            EntityManager entmgr = emc.getManager();
            EntityTransaction entrans = etc.getTransaction();

            // Clean up the DB
            entmgr.createQuery("Delete from " + tableName).executeUpdate();

            // commit transaction
            entrans.commit();
        }
    }

    /**
     * Verifies that items appear within the log, in order. A given item may appear more than once.
     * In addition, the log may contain extra items; those are ignored.
     *
     * @param app where data has been logged
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
                    errorLogger.error("failed to stop audit thread");
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
        // each auditor gets its own notion of time
        TestTime time = new TestTime();

        // use the auditor-specific time while this thread constructs things
        testTime.set(time);

        return new MyIntegrityAudit(resourceName2, persistenceUnit2, makeProperties(), time);
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
        List<Semaphore> semaphores = new ArrayList<>(auditors.length);
        for (MyIntegrityAudit p : auditors) {
            semaphores.add(p.startAudit());
        }

        // wait for each auditor to complete its cycle
        for (Semaphore sem : semaphores) {
            waitSem(sem);
        }
    }

    /**
     * Waits for a semaphore to be released.
     *
     * @param sem the semaphore for which to wait
     * @throws InterruptedException if the thread is interrupted
     * @throws AssertionError if the semaphore did not reach zero in the allotted time
     */
    protected void waitSem(Semaphore sem) throws InterruptedException {
        assertTrue(sem.tryAcquire(WAIT_MS, TimeUnit.MILLISECONDS));
    }

    /**
     * Sleep a bit so that the currently designated pdp becomes stale.
     *
     * @throws InterruptedException if the thread is interrupted
     */
    protected void waitStale() throws InterruptedException {
        // waits for ALL auditors to become stale, as each has its own timer
        for (MyIntegrityAudit auditor : auditors) {
            auditor.sleep(AuditThread.AUDIT_COMPLETION_INTERVAL * AuditThread.AUDIT_RESET_CYCLES + 1);
        }
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
     * Manages audits by inserting semaphores into a queue for the AuditThread to count.
     */
    protected class MyIntegrityAudit extends IntegrityAudit {

        private final TestTime myTime;

        /**
         * Semaphore on which the audit thread should wait.
         */
        private Semaphore auditSem = null;

        /**
         * Semaphore on which the junit management thread should wait.
         */
        private Semaphore junitSem = null;

        /**
         * Constructs an auditor and starts the AuditThread.
         *
         * @param resourceName the resource name
         * @param persistenceUnit the persistence unit
         * @param properties the properties
         * @param time the time
         * @throws Exception if an error occurs
         */
        public MyIntegrityAudit(String resourceName, String persistenceUnit,
                        Properties properties, TestTime time) throws Exception {
            super(resourceName, persistenceUnit, properties);

            myTime = time;
            testTime.set(myTime);

            auditors.add(this);

            startAuditThread();
        }

        /**
         * Get time in milliseconds.
         *
         * @return the "current" time for the auditor
         */
        public long getTimeInMillis() {
            return myTime.getMillis();
        }

        /**
         * Sleeps for a period of time.
         *
         * @param sleepMs time to sleep
         * @throws InterruptedException can be interrupted
         */
        public void sleep(long sleepMs) throws InterruptedException {
            myTime.sleep(sleepMs);
        }

        /**
         * Interrupts the AuditThread.
         */
        public void interrupt() {
            super.stopAuditThread();
        }

        /**
         * Triggers an audit by releasing the audit thread's semaphore.
         *
         * @return the semaphore on which to wait
         * @throws InterruptedException if the thread is interrupted
         */
        public Semaphore startAudit() throws InterruptedException {
            auditSem.release();
            return junitSem;
        }

        /**
         * Starts a new AuditThread. Creates a new pair of semaphores and associates them
         * with the thread.
         */
        @Override
        public final void startAuditThread() throws IntegrityAuditException {
            if (auditSem != null) {
                // release a bunch of semaphores, in case a thread is still running
                auditSem.release(1000);
            }

            auditSem = new Semaphore(0);
            junitSem = new Semaphore(0);

            super.startAuditThread();

            if (haveAuditThread()) {
                // tell the thread it can run
                auditSem.release();

                // wait for the thread to start
                try {
                    waitSem(junitSem);

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

        @Override
        protected AuditThread makeAuditThread(String resourceName2, String persistenceUnit2, Properties properties2,
                        int integrityAuditPeriodSeconds2) throws IntegrityAuditException {

            // make sure we're still using the auditor's time while we construct things
            testTime.set(myTime);

            return new AuditThread(resourceName2, persistenceUnit2, properties2, integrityAuditPeriodSeconds2, this) {

                private Semaphore auditSem = MyIntegrityAudit.this.auditSem;
                private Semaphore junitSem = MyIntegrityAudit.this.junitSem;

                @Override
                public void run() {
                    // make sure our thread uses this auditor's time
                    testTime.set(myTime);
                    super.run();
                }

                @Override
                public void runStarted() throws InterruptedException {
                    auditSem.acquire();

                    junitSem.release();
                    auditSem.acquire();
                }

                @Override
                public void auditCompleted() throws InterruptedException {
                    junitSem.release();
                    auditSem.acquire();
                }

            };
        }
    }
}
