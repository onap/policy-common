/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.onap.policy.common.utils.time.TestTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * "Time" provider for junit tests. Like its super class, this does not use the real system
 * time, but simulates it instead.  A queue of work items is maintained, sorted by the time for
 * which the items are scheduled to execute.  Tasks are executed by the test/controlling thread
 * when one of the waitXxx() methods is invoked.
 */
public class PseudoTime extends TestTime {
    private static final Logger logger = LoggerFactory.getLogger(PseudoTime.class);

    /**
     * Maximum time that the test thread should wait for something to be added to its work queue.
     */
    private static final long MAX_WAIT_MS = 5000L;

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
     * Indicates that this will no longer be used. Interrupts any threads that are waiting
     * for their "sleep()" to complete.
     */
    public void destroy() {
        synchronized (updateLock) {
            for (WorkItem item : queue) {
                item.interrupt();
            }

            queue.clear();
        }
    }

    /**
     * Waits for the pseudo time to reach a certain point.  Executes work items until the time
     * is reached.
     *
     * @param waitMs time, in milliseconds, for which to wait
     * @throws InterruptedException if the current thread is interrupted
     */
    public void waitFor(long waitMs) throws InterruptedException {
        // pseudo time for which we're waiting
        long tend = getMillis() + waitMs;

        while (getMillis() < tend) {
            WorkItem item = pollQueue(MAX_WAIT_MS);
            if (item != null) {
                runItem(item);

            } else {
                /*
                 * Waited the maximum poll time and nothing has happened, so we'll queue
                 * up our own work item to bump the time.
                 */
                enqueue(new WorkItem(this, tend - getMillis()));
            }
        }
    }

    /**
     * Waits for a condition to become true.   Executes work items until the given
     * condition is true or the maximum wait time
     * is reached.
     *
     * @param twait maximum time to wait
     * @param units time units represented by "twait"
     * @param condition condition to be checked
     */
    public void waitUntil(long twait, TimeUnit units, Callable<Boolean> condition) {
        try {
            // pseudo time for which we're waiting
            long tend = getMillis() + units.toMillis(twait);

            // real time for which we're waiting
            long realEnd = System.currentTimeMillis() + MAX_WAIT_MS;

            while (getMillis() < tend && System.currentTimeMillis() < realEnd) {
                if (condition.call()) {
                    return;
                }

                WorkItem item = pollQueue(100);
                if (item != null) {
                    runItem(item);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("interrupted while waiting for condition", e);
            fail("interrupted while waiting for condition: " + e.getMessage());

        } catch (Exception e) {
            logger.error("condition evaluator threw an exception", e);
            fail("condition evaluator threw an exception: " + e.getMessage());
        }

        fail("condition was never satisfied");
    }

    /**
     * Gets one item from the work queue.
     *
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

        work.fire();
    }

    @Override
    public void sleep(long sleepMs) throws InterruptedException {
        if (sleepMs <= 0) {
            return;
        }

        SleepItem item = new SleepItem(this, sleepMs, Thread.currentThread());
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
            updateLock.notify();
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
}
