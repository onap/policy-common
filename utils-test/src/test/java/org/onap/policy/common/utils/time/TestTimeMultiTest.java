/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestTimeMultiTest {
    private static final long SHORT_WAIT_MS = 100L;
    private static final long DELAY_MS = 500L;
    private static final long MAX_WAIT_MS = 5000L;

    private TestTimeMulti multi;

    @BeforeEach
    public void setUp() {
        multi = new TestTimeMulti();
    }

    @Test
    void testSleep() throws InterruptedException {
        // negative sleep time
        final long tbegin = multi.getMillis();
        MyThread thread = new MyThread(-5);
        thread.start();

        // should complete without creating a work item
        assertTrue(thread.await());
        assertNull(thread.ex);

        // time should not have changed
        assertEquals(tbegin, multi.getMillis());


        // positive sleep time
        thread = new MyThread(DELAY_MS);
        thread.start();

        // must execute the SleepItem
        multi.runOneTask(MAX_WAIT_MS);

        assertTrue(multi.isEmpty());
        assertTrue(thread.await());
        assertNull(thread.ex);

        // time SHOULD HAVE changed
        assertEquals(tbegin + DELAY_MS, multi.getMillis());
    }

    @Test
    void testTestTimeMulti() {
        assertTrue(multi.getMaxWaitMs() > 0);
    }

    @Test
    void testTestTimeMultiLong() {
        assertEquals(200, new TestTimeMulti(200).getMaxWaitMs());
    }

    @Test
    void testIsEmpty_testQueueLength() throws InterruptedException {
        assertTrue(multi.isEmpty());

        // queue up two items
        multi.enqueue(new WorkItem(multi, DELAY_MS));
        assertFalse(multi.isEmpty());
        assertEquals(1, multi.queueLength());

        multi.enqueue(new WorkItem(multi, DELAY_MS));
        assertEquals(2, multi.queueLength());

        // run one - should not be empty yet
        multi.runOneTask(0);
        assertFalse(multi.isEmpty());
        assertEquals(1, multi.queueLength());

        // run the other - should be empty now
        multi.runOneTask(0);
        assertTrue(multi.isEmpty());
        assertEquals(0, multi.queueLength());
    }

    @Test
    void testDestroy() throws InterruptedException {
        // this won't interrupt
        multi.enqueue(new WorkItem(multi, DELAY_MS));

        // these will interrupt
        AtomicBoolean interrupted1 = new AtomicBoolean(false);
        multi.enqueue(new WorkItem(multi, DELAY_MS) {
            @Override
            public void interrupt() {
                interrupted1.set(true);
            }
        });

        AtomicBoolean interrupted2 = new AtomicBoolean(false);
        multi.enqueue(new WorkItem(multi, DELAY_MS) {
            @Override
            public void interrupt() {
                interrupted2.set(true);
            }
        });

        multi.destroy();
        assertTrue(multi.isEmpty());

        assertTrue(interrupted1.get());
        assertTrue(interrupted2.get());
    }

    @Test
    void testRunOneTask() throws InterruptedException {
        // nothing in the queue yet
        assertFalse(multi.runOneTask(0));

        // put something in the queue
        multi.enqueue(new WorkItem(multi, DELAY_MS));

        final long tbegin = multi.getMillis();
        assertTrue(multi.runOneTask(MAX_WAIT_MS));

        assertEquals(tbegin + DELAY_MS, multi.getMillis());

        // nothing in the queue now
        assertFalse(multi.runOneTask(0));

        // time doesn't change
        assertEquals(tbegin + DELAY_MS, multi.getMillis());
    }

    @Test
    void testWaitFor() throws InterruptedException {
        // queue up a couple of items
        multi.enqueue(new WorkItem(multi, DELAY_MS));
        multi.enqueue(new WorkItem(multi, DELAY_MS * 2));
        multi.enqueue(new WorkItem(multi, DELAY_MS * 3));

        final long realBegin = System.currentTimeMillis();
        final long tbegin = multi.getMillis();
        multi.waitFor(DELAY_MS * 2 - 1);
        assertEquals(tbegin + DELAY_MS * 2, multi.getMillis());

        // minimal real time should have elapsed
        assertTrue(System.currentTimeMillis() < realBegin + TestTimeMulti.DEFAULT_MAX_WAIT_MS);
    }

    @Test
    void testWaitFor_EmptyQueue() throws InterruptedException {
        multi = new TestTimeMulti(SHORT_WAIT_MS);

        final long realBegin = System.currentTimeMillis();
        final long tbegin = multi.getMillis();

        multi.waitFor(2);

        assertEquals(tbegin + 2, multi.getMillis());
        assertTrue(System.currentTimeMillis() >= realBegin + SHORT_WAIT_MS);
    }

    @Test
    void testWaitUntilCallable() throws InterruptedException {
        multi.enqueue(new WorkItem(multi, DELAY_MS));
        multi.enqueue(new WorkItem(multi, DELAY_MS * 2));
        multi.enqueue(new WorkItem(multi, DELAY_MS * 3));

        final long tbegin = multi.getMillis();
        AtomicInteger count = new AtomicInteger(0);
        multi.waitUntil(() -> count.incrementAndGet() == 3);

        assertEquals(tbegin + DELAY_MS * 2, multi.getMillis());

        // should still be one item left in the queue
        assertEquals(1, multi.queueLength());
        assertEquals(3, count.get());
    }

    @Test
    void testWaitUntilCallable_InterruptEx() throws InterruptedException {
        multi = new TestTimeMulti();

        Callable<Boolean> callable = () -> {
            throw new InterruptedException("expected exception");
        };

        LinkedBlockingQueue<Error> errors = new LinkedBlockingQueue<>();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    multi.waitUntil(callable);
                } catch (Error ex) {
                    errors.add(ex);
                }
            }
        };

        thread.start();

        Error ex = errors.poll(MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        assertNotNull(ex);
        assertEquals("interrupted while waiting for condition: expected exception", ex.getMessage());
    }

    @Test
    void testWaitUntilCallable_ConditionThrowsEx() throws InterruptedException {
        multi = new TestTimeMulti();

        Callable<Boolean> callable = () -> {
            throw new IllegalStateException("expected exception");
        };

        final long realBegin = System.currentTimeMillis();
        assertThatThrownBy(() -> multi.waitUntil(callable))
                        .hasMessage("condition evaluator threw an exception: expected exception");

        assertTrue(System.currentTimeMillis() < realBegin + TestTimeMulti.DEFAULT_MAX_WAIT_MS);
    }

    @Test
    void testWaitUntilCallable_NeverSatisfied() throws InterruptedException {
        multi = new TestTimeMulti(SHORT_WAIT_MS);

        final long realBegin = System.currentTimeMillis();
        assertThatThrownBy(() -> multi.waitUntil(() -> false))
                        .hasMessage(TestTimeMulti.NEVER_SATISFIED);
        assertTrue(System.currentTimeMillis() >= realBegin + SHORT_WAIT_MS);
    }

    @Test
    void testWaitUntilLongTimeUnitCallable() throws InterruptedException {
        multi.enqueue(new WorkItem(multi, DELAY_MS));
        multi.enqueue(new WorkItem(multi, DELAY_MS * 2));
        multi.enqueue(new WorkItem(multi, DELAY_MS * 3));

        final long tbegin = multi.getMillis();
        AtomicInteger count = new AtomicInteger(0);
        multi.waitUntil(DELAY_MS * 4, TimeUnit.MILLISECONDS, () -> count.incrementAndGet() == 3);

        assertEquals(tbegin + DELAY_MS * 2, multi.getMillis());

        // should still be one item left in the queue
        assertEquals(1, multi.queueLength());
        assertEquals(3, count.get());
    }

    @Test
    void testWaitUntilLongTimeUnitCallable_PseudoTimeExpires() throws InterruptedException {
        multi.enqueue(new WorkItem(multi, DELAY_MS));
        multi.enqueue(new WorkItem(multi, DELAY_MS * 2));
        multi.enqueue(new WorkItem(multi, DELAY_MS * 3));

        final long tbegin = multi.getMillis();
        assertThatThrownBy(() -> multi.waitUntil(DELAY_MS * 2 - 1, TimeUnit.MILLISECONDS, () -> false))
                        .hasMessage(TestTimeMulti.NEVER_SATISFIED);
        assertEquals(tbegin + DELAY_MS * 2, multi.getMillis());
    }

    @Test
    void testRunItem() throws InterruptedException {
        AtomicBoolean fired = new AtomicBoolean(false);
        multi.enqueue(new MyWorkItem(fired));

        assertTrue(multi.runOneTask(1));

        // should no longer be in the queue
        assertTrue(multi.isEmpty());

        // should have been fired
        assertTrue(fired.get());
    }

    @Test
    void testRunItem_Rescheduled() throws InterruptedException {
        AtomicBoolean fired = new AtomicBoolean(false);

        multi.enqueue(new MyWorkItem(fired) {
            @Override
            public boolean bumpNextTime() {
                bumpNextTime(DELAY_MS);
                return true;
            }
        });

        assertTrue(multi.runOneTask(1));

        // should still be in the queue
        assertEquals(1, multi.queueLength());

        // should have been fired
        assertTrue(fired.get());
    }

    @Test
    void testRunItem_Canceled() throws InterruptedException {
        AtomicBoolean fired = new AtomicBoolean(false);

        multi.enqueue(new MyWorkItem(fired) {
            @Override
            public boolean wasCancelled() {
                return true;
            }

            @Override
            public boolean bumpNextTime() {
                return true;
            }
        });

        final long tbegin = multi.getMillis();
        assertTrue(multi.runOneTask(1));

        // time should be unchanged
        assertEquals(tbegin, multi.getMillis());

        assertTrue(multi.isEmpty());

        // should not have been fired
        assertFalse(fired.get());
    }

    @Test
    void testEnqueue() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<InterruptedException> ex = new AtomicReference<>();

        Thread thread = new Thread() {
            @Override
            public void run() {
                started.countDown();

                try {
                    multi.runOneTask(DELAY_MS * 3);
                } catch (InterruptedException e) {
                    ex.set(e);
                }

                finished.countDown();
            }
        };

        thread.start();

        // wait for thread to start
        started.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS);

        // wait for it to block on the lock
        await().atMost(MAX_WAIT_MS, TimeUnit.MILLISECONDS).until(() -> thread.getState() == Thread.State.TIMED_WAITING);

        // add an item to the queue - should trigger the thread to continue
        multi.enqueue(new WorkItem(multi, DELAY_MS));

        assertTrue(finished.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS));
        assertNull(ex.get());
    }

    @Test
    void testCancelItems() throws InterruptedException {
        AtomicBoolean fired1 = new AtomicBoolean();
        multi.enqueue(new MyWorkItem(fired1));

        AtomicBoolean fired2 = new AtomicBoolean();
        multi.enqueue(new MyWorkItem(fired2));
        multi.enqueue(new MyWorkItem(fired2));

        AtomicBoolean fired3 = new AtomicBoolean();
        multi.enqueue(new MyWorkItem(fired3));

        // cancel some
        multi.cancelItems(fired2);

        // should have only canceled two of them
        assertEquals(2, multi.queueLength());

        // fire both
        multi.runOneTask(0);
        multi.runOneTask(0);

        // these should have fired
        assertTrue(fired1.get());
        assertTrue(fired3.get());

        // these should NOT have fired
        assertFalse(fired2.get());
    }

    @Test
    void testPurgeItems() throws InterruptedException {
        AtomicBoolean fired = new AtomicBoolean();

        // queue up two that are canceled, one that is not
        multi.enqueue(new MyWorkItem(true));
        multi.enqueue(new MyWorkItem(fired));
        multi.enqueue(new MyWorkItem(true));

        multi.purgeItems();

        assertEquals(1, multi.queueLength());

        multi.runOneTask(0);
        assertTrue(fired.get());
    }

    private class MyWorkItem extends WorkItem {
        private final AtomicBoolean fired;
        private final boolean canceled;

        public MyWorkItem(AtomicBoolean fired) {
            super(multi, DELAY_MS);
            this.fired = fired;
            this.canceled = false;
        }

        public MyWorkItem(boolean canceled) {
            super(multi, DELAY_MS);
            this.fired = new AtomicBoolean();
            this.canceled = canceled;
        }

        @Override
        public void fire() {
            fired.set(true);
        }

        @Override
        public boolean isAssociatedWith(Object associate) {
            return (fired == associate);
        }

        @Override
        public boolean wasCancelled() {
            return canceled;
        }
    }

    private class MyThread extends Thread {
        private final long sleepMs;
        private final CountDownLatch finished = new CountDownLatch(1);
        private InterruptedException ex = null;

        public MyThread(long sleepMs) {
            this.sleepMs = sleepMs;
            this.setDaemon(true);
        }

        public boolean await() throws InterruptedException {
            return finished.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        }

        @Override
        public void run() {
            try {
                multi.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                ex = e;
            }

            finished.countDown();
        }
    }
}
