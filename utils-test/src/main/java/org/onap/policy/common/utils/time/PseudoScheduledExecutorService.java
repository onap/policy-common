/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * Scheduled executor service that uses {@link TestTimeMulti} to execute its tasks. Note:
 * the invokeXxx() methods are not currently supported.
 */
public class PseudoScheduledExecutorService implements ScheduledExecutorService {
    private static final String NOT_IMPLEMENTED_YET = "not implemented yet";

    /**
     * Object to be used to execute timer tasks.
     */
    private final TestTimeMulti currentTime;

    /**
     * {@code True} if {@link #shutdown()} or {@link #shutdownNow()} has been called,
     * {@code false} otherwise.
     */
    @Getter
    private boolean shutdown = false;

    /**
     * Constructs the object.
     *
     * @param currentTime object to be used to execute timer tasks
     */
    public PseudoScheduledExecutorService(TestTimeMulti currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * Cancels <i>all</i> tasks that have not yet been executed.
     */
    @Override
    public void shutdown() {
        shutdown = true;
        currentTime.cancelItems(this);
    }

    /**
     * Cancels <i>all</i> tasks that have not yet been executed. Does <i>not</i> interrupt
     * any currently executing task.
     */
    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return currentTime.cancelItems(this).stream().map(item -> ((RunnableItem) item).getAction())
                        .collect(Collectors.toList());
    }

    @Override
    public boolean isTerminated() {
        return isShutdown();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return shutdown;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return enqueueRunOnce(0, new FutureTask<>(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return enqueueRunOnce(0, new FutureTask<>(task, result));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return enqueueRunOnce(0, new FutureTask<>(task, null));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                    throws InterruptedException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public void execute(Runnable command) {
        currentTime.enqueue(new RunnableItem(currentTime, this, 0, command));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return enqueueRunOnce(unit.toMillis(delay), new PseudoScheduledFuture<>(command, null, false));
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return enqueueRunOnce(unit.toMillis(delay), new PseudoScheduledFuture<>(callable, false));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return enqueuePeriodic(unit.toMillis(initialDelay), unit.toMillis(period),
                        new PseudoScheduledFuture<>(command, null, true));
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return enqueuePeriodic(unit.toMillis(initialDelay), unit.toMillis(delay),
                        new PseudoScheduledFuture<>(command, null, true));
    }

    /**
     * Enqueues a future to be executed one time.
     *
     * @param delay delay until the future should be executed
     * @param future future to be enqueued
     * @return the future
     */
    private <F extends FutureTask<T>, T> F enqueueRunOnce(long delay, F future) {
        currentTime.enqueue(new RunnableItem(currentTime, this, delay, future));
        return future;
    }

    /**
     * Enqueues a future to be executed periodically.
     *
     * @param initialDelayMs delay until the future should be executed the first time
     * @param periodMs delay between executions of the future
     * @param future future to be enqueued
     * @return the future
     */
    private <T> ScheduledFuture<T> enqueuePeriodic(long initialDelayMs, long periodMs,
                    PseudoScheduledFuture<T> future) {
        currentTime.enqueue(new PeriodicItem(currentTime, this, initialDelayMs, periodMs, future));
        return future;
    }
}
