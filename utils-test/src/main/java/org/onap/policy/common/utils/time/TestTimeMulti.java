/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.common.utils.time;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * "Current" time, when running junit tests in multiple threads. This is intended to be
 * injected into classes under test, to replace their {@link CurrentTime} objects. The
 * {@link #sleep(long)} method blocks until the "time" has reached the specified sleep
 * time. A queue of work items is maintained, sorted by the time for which the items are
 * scheduled to execute. Tasks are executed by the test/controlling thread when one of the
 * waitXxx() methods is invoked. {@link PseudoTimer} and
 * {@link PseudoScheduledExecutorService} add work items to the queue.
 *
 * <p/>
 * This only handles relatively simple situations, though it does support multi-threaded
 * testing.
 */
public class TestTimeMulti extends TestTime {
    private static final Logger logger = LoggerFactory.getLogger(TestTimeMulti.class);

    public static final String NEVER_SATISFIED = "condition was never satisfied";

    /**
     * Default value, in milliseconds, to wait for an item to be added to the queue.
     */
    public static final long DEFAULT_MAX_WAIT_MS = 5000L;

    /**
     * Maximum time that the test thread should wait for something to be added to its work
     * queue.
     */
    @Getter
    private final long maxWaitMs;

    /**
     * Queue of timer tasks to be executed, sorted by {@link WorkItem#nextMs}.
     */
    private final PriorityQueue<WorkItem> queue =
                    new PriorityQueue<>((item1, item2) -> Long.compare(item1.getNextMs(), item2.getNextMs()));

    /**
     * Lock used when modifying the queue.
     */
    private final Object updateLock = new Object();

    /**
     * Constructs the object using the default maximum wait time.
     */
    public TestTimeMulti() {
        this(DEFAULT_MAX_WAIT_MS);
    }

    /**
     * Constructs the object.
     *
     * @param maxWaitMs maximum time that the test thread should wait for something to be
     *        added to its work queue
     */
    public TestTimeMulti(long maxWaitMs) {
        this.maxWaitMs = maxWaitMs;
    }

    /**
     * Determines if the task queue is empty.
     *
     * @return {@code true} if the task queue is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        synchronized (updateLock) {
            purgeItems();
            return queue.isEmpty();
        }
    }

    /**
     * Gets the number of tasks in the work queue.
     *
     * @return the number of tasks in the work queue
     */
    public int queueLength() {
        synchronized (updateLock) {
            purgeItems();
            return queue.size();
        }
    }

    /**
     * Indicates that this will no longer be used. Interrupts any threads that are waiting
     * for their "sleep()" to complete.
     */
    public void destroy() {
        synchronized (updateLock) {
            queue.forEach(WorkItem::interrupt);
            queue.clear();
        }
    }

    /**
     * Runs a single task from the queue.
     *
     * @param waitMs time, in milliseconds, for which to wait. This is "real" time rather
     *        than pseudo time
     *
     * @return {@code true} if a task was run, {@code false} if the queue was empty
     * @throws InterruptedException if the current thread is interrupted
     */
    public boolean runOneTask(long waitMs) throws InterruptedException {
        WorkItem item = pollQueue(waitMs);
        if (item == null) {
            return false;
        }

        runItem(item);
        return true;
    }

    /**
     * Waits for the pseudo time to reach a certain point. Executes work items until the
     * time is reached.
     *
     * @param waitMs pseudo time, in milliseconds, for which to wait
     * @throws InterruptedException if the current thread is interrupted
     */
    public void waitFor(long waitMs) throws InterruptedException {
        // pseudo time for which we're waiting
        long tend = getMillis() + waitMs;

        while (getMillis() < tend) {
            if (!runOneTask(maxWaitMs)) {
                /*
                 * Waited the maximum poll time and nothing has happened, so we'll just
                 * bump the time directly.
                 */
                super.sleep(tend - getMillis());
                break;
            }
        }
    }

    /**
     * Waits for a condition to become true. Executes work items until the given condition
     * is true.
     *
     * @param condition condition to be checked
     */
    public void waitUntil(Callable<Boolean> condition) {
        try {
            // real time for which we're waiting
            long realEnd = System.currentTimeMillis() + maxWaitMs;

            while (System.currentTimeMillis() < realEnd) {
                if (Boolean.TRUE.equals(condition.call())) {
                    return;
                }

                runOneTask(100);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("interrupted while waiting for condition", e);
            // disabling sonar, as this is only used by junit tests
            fail("interrupted while waiting for condition: " + e.getMessage()); // NOSONAR

        } catch (Exception e) {
            logger.error("condition evaluator threw an exception", e);
            // disabling sonar, as this is only used by junit tests
            fail("condition evaluator threw an exception: " + e.getMessage());  // NOSONAR
        }

        fail(NEVER_SATISFIED);
    }

    /**
     * Waits for a condition to become true. Executes work items until the given condition
     * is true or the maximum wait time is reached.
     *
     * @param twait maximum, pseudo time to wait
     * @param units time units represented by "twait"
     * @param condition condition to be checked
     */
    public void waitUntil(long twait, TimeUnit units, Callable<Boolean> condition) {
        // pseudo time for which we're waiting
        long tend = getMillis() + units.toMillis(twait);

        waitUntil(() -> {
            if (getMillis() >= tend) {
                fail(NEVER_SATISFIED);
            }

            return condition.call();
        });
    }

    /**
     * Gets one item from the work queue.
     *
     * @param waitMs time, in milliseconds, for which to wait. This is "real" time rather
     *        than pseudo time
     * @return the first item in the queue, or {@code null} if no item was added to the
     *         queue before the wait time expired
     * @throws InterruptedException if the current thread was interrupted
     */
    private WorkItem pollQueue(long waitMs) throws InterruptedException {
        long realEnd = System.currentTimeMillis() + waitMs;
        WorkItem work;

        synchronized (updateLock) {
            while ((work = queue.poll()) == null) {
                updateLock.wait(Math.max(1, realEnd - System.currentTimeMillis()));

                if (queue.isEmpty() && System.currentTimeMillis() >= realEnd) {
                    return null;
                }
            }
        }

        return work;
    }

    /**
     * Runs a work item.
     *
     * @param work work item to be run
     * @throws InterruptedException if the current thread was interrupted
     */
    private void runItem(WorkItem work) throws InterruptedException {
        if (work.wasCancelled()) {
            logger.info("work item was canceled {}", work);
            return;
        }

        // update the pseudo time
        super.sleep(work.getNextMs() - getMillis());

        /*
         * Add it back into the queue if appropriate, in case cancel() is called while
         * it's executing.
         */
        if (work.bumpNextTime()) {
            logger.info("re-enqueuing work item");
            enqueue(work);
        }

        logger.info("fire work item {}", work);
        work.fire();
    }

    @Override
    public void sleep(long sleepMs) throws InterruptedException {
        if (sleepMs <= 0) {
            return;
        }

        var item = new SleepItem(this, sleepMs, Thread.currentThread());
        enqueue(item);

        // wait for the item to fire
        logger.info("sleeping {}", item);
        item.await();
        logger.info("done sleeping {}", Thread.currentThread());
    }

    /**
     * Adds an item to the {@link #queue}.
     *
     * @param item item to be added
     */
    protected void enqueue(WorkItem item) {
        logger.info("enqueue work item {}", item);
        synchronized (updateLock) {
            queue.add(item);
            updateLock.notifyAll();
        }
    }

    /**
     * Cancels work items by removing them from the queue if they're associated with the
     * specified object.
     *
     * @param associate object whose associated items are to be cancelled
     * @return list of items that were canceled
     */
    protected List<WorkItem> cancelItems(Object associate) {
        List<WorkItem> items = new LinkedList<>();

        synchronized (updateLock) {
            Iterator<WorkItem> iter = queue.iterator();
            while (iter.hasNext()) {
                WorkItem item = iter.next();
                if (item.isAssociatedWith(associate)) {
                    iter.remove();
                    items.add(item);
                }
            }
        }

        return items;
    }

    /**
     * Purges work items that are known to have been canceled. (Does not remove canceled
     * TimerTasks, as there is no way via the public API to determine if the task has been
     * canceled.)
     */
    public void purgeItems() {
        synchronized (updateLock) {
            Iterator<WorkItem> iter = queue.iterator();
            while (iter.hasNext()) {
                if (iter.next().wasCancelled()) {
                    iter.remove();
                }
            }
        }
    }
}
