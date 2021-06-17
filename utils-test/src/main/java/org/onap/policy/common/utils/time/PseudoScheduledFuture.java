/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Scheduled future that gets its time from an associated work item.
 *
 * @param <T> type of result returned by the future
 */
class PseudoScheduledFuture<T> extends FutureTask<T> implements RunnableScheduledFuture<T> {

    /**
     * {@code True} if this task is periodic, {@code false} otherwise.
     */
    @Getter
    private final boolean periodic;

    /**
     * The work item with which this is associated.
     */
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private WorkItem workItem;

    /**
     * Constructs the object.
     *
     * @param runnable action to be executed
     * @param result value to be returned by the {@link #get()} operation
     * @param periodic {@code true} if this task is periodic, {@code false} otherwise
     */
    public PseudoScheduledFuture(Runnable runnable, T result, boolean periodic) {
        super(runnable, result);
        this.periodic = periodic;
    }

    /**
     * Constructs the object.
     *
     * @param callable action to be executed
     * @param periodic {@code true} if this task is periodic, {@code false} otherwise
     */
    public PseudoScheduledFuture(Callable<T> callable, boolean periodic) {
        super(callable);
        this.periodic = periodic;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(workItem.getDelay(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(workItem.getDelay(), other.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public void run() {
        if (isPeriodic()) {
            super.runAndReset();

        } else {
            super.run();
        }
    }
}
