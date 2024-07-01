/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PseudoScheduledExecutorServiceTest {
    private static final long DELAY_MS = 100L;
    private static final long PERIOD_MS = 200L;
    private static final List<Callable<Object>> EMPTY_CALLABLES = Collections.emptyList();

    private int ran;
    private int called;
    private TestTimeMulti currentTime;
    private PseudoScheduledExecutorService svc;

    /**
     * Sets up objects, including {@link #svc}.
     */
    @BeforeEach
    public void setUp() {
        ran = 0;
        called = 0;
        currentTime = new TestTimeMulti();
        svc = new PseudoScheduledExecutorService(currentTime);
    }

    @Test
    void testShutdown() {
        // submit some tasks
        svc.submit(new MyRun());
        svc.schedule(new MyRun(), 1L, TimeUnit.SECONDS);

        svc.shutdown();
        assertTrue(svc.isShutdown());

        // task should have been removed
        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testShutdownNow() {
        // submit some tasks
        svc.submit(new MyRun());
        svc.schedule(new MyRun(), 1L, TimeUnit.SECONDS);

        svc.shutdownNow();
        assertTrue(svc.isShutdown());

        // task should have been removed
        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testIsShutdown_testIsTerminated() {
        assertFalse(svc.isShutdown());
        assertFalse(svc.isTerminated());

        svc.shutdown();
        assertTrue(svc.isShutdown());
        assertTrue(svc.isTerminated());
    }

    @Test
    void testAwaitTermination() throws InterruptedException {
        assertFalse(svc.awaitTermination(1L, TimeUnit.SECONDS));

        svc.shutdown();
        assertTrue(svc.awaitTermination(1L, TimeUnit.SECONDS));
    }

    @Test
    void testSubmitCallableOfT() throws Exception {
        Future<Integer> future = svc.submit(new MyCallable());
        currentTime.runOneTask(0);

        assertEquals(1, called);
        assertEquals(1, future.get().intValue());

        // nothing re-queued
        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testSubmitRunnableT() throws Exception {
        Future<Integer> future = svc.submit(new MyRun(), 2);
        currentTime.runOneTask(0);

        assertEquals(1, ran);
        assertEquals(2, future.get().intValue());

        // nothing re-queued
        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testSubmitRunnable() throws Exception {
        assertNotNull(svc.submit(new MyRun()));
        currentTime.runOneTask(0);

        assertEquals(1, ran);

        // nothing re-queued
        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testInvokeAllCollectionOfQextendsCallableOfT() {
        assertThatThrownBy(() -> svc.invokeAll(EMPTY_CALLABLES))
                        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testInvokeAllCollectionOfQextendsCallableOfTLongTimeUnit() {
        assertThatThrownBy(() -> svc.invokeAll(EMPTY_CALLABLES, 1, TimeUnit.MILLISECONDS))
                        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testInvokeAnyCollectionOfQextendsCallableOfT() {
        assertThatThrownBy(() -> svc.invokeAny(EMPTY_CALLABLES))
                        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testInvokeAnyCollectionOfQextendsCallableOfTLongTimeUnit() {
        assertThatThrownBy(() -> svc.invokeAny(EMPTY_CALLABLES, 1, TimeUnit.MILLISECONDS))
                        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testExecute() throws InterruptedException {
        svc.execute(new MyRun());
        currentTime.runOneTask(0);

        assertEquals(1, ran);

        // nothing re-queued
        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testScheduleRunnableLongTimeUnit() throws InterruptedException {
        assertNotNull(svc.schedule(new MyRun(), DELAY_MS, TimeUnit.MILLISECONDS));

        assertEquals(DELAY_MS, oneTaskElapsedTime());
        assertEquals(1, ran);

        // verify nothing re-scheduled
        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testScheduleCallableOfVLongTimeUnit() throws Exception {
        ScheduledFuture<Integer> future = svc.schedule(new MyCallable(), DELAY_MS, TimeUnit.MILLISECONDS);

        assertEquals(DELAY_MS, oneTaskElapsedTime());
        assertEquals(1, called);
        assertEquals(1, future.get().intValue());

        // verify nothing re-scheduled
        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testScheduleAtFixedRate() throws InterruptedException {
        final ScheduledFuture<?> future =
                        svc.scheduleAtFixedRate(new MyRun(), DELAY_MS, PERIOD_MS, TimeUnit.MILLISECONDS);

        assertEquals(DELAY_MS, oneTaskElapsedTime());
        assertEquals(1, ran);

        assertEquals(PERIOD_MS, oneTaskElapsedTime());
        assertEquals(2, ran);

        assertEquals(PERIOD_MS, oneTaskElapsedTime());
        assertEquals(3, ran);

        future.cancel(false);

        // should not actually execute
        assertEquals(0, oneTaskElapsedTime());
        assertEquals(3, ran);

        // verify nothing re-scheduled
        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testScheduleWithFixedDelay() throws InterruptedException {
        final ScheduledFuture<?> future =
                        svc.scheduleWithFixedDelay(new MyRun(), DELAY_MS, PERIOD_MS, TimeUnit.MILLISECONDS);

        assertEquals(DELAY_MS, oneTaskElapsedTime());
        assertEquals(1, ran);

        assertEquals(PERIOD_MS, oneTaskElapsedTime());
        assertEquals(2, ran);

        assertEquals(PERIOD_MS, oneTaskElapsedTime());
        assertEquals(3, ran);

        future.cancel(false);

        // should not actually execute
        assertEquals(0, oneTaskElapsedTime());
        assertEquals(3, ran);

        // verify nothing re-scheduled
        assertTrue(currentTime.isEmpty());
    }

    /**
     * Runs a single task and returns its elapsed (pseudo) time.
     *
     * @return the elapsed time taken to run the task
     * @throws InterruptedException if the thread is interrupted
     */
    private long oneTaskElapsedTime() throws InterruptedException {
        final long tbegin = currentTime.getMillis();
        currentTime.runOneTask(0);
        return (currentTime.getMillis() - tbegin);
    }

    private class MyRun implements Runnable {
        @Override
        public void run() {
            ++ran;
        }
    }

    private class MyCallable implements Callable<Integer> {
        @Override
        public Integer call() {
            return ++called;
        }
    }
}
