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

import lombok.Getter;
import org.onap.policy.common.utils.time.TestTime;

/**
 * Work item to be executed at some time.
 */
class WorkItem {

    /**
     * Pseudo time with which this item is associated.
     */
    private final TestTime currentTime;

    /**
     * Time, in milliseconds, when the timer should fire next.
     */
    @Getter
    private long nextMs;


    /**
     * Constructs the object.
     *
     * @param currentTime time with which this item is associated
     * @param delayMs time, in milliseconds, before this item should be executed
     */
    public WorkItem(TestTime currentTime, long delayMs) {
        if (delayMs < 0) {
            throw new IllegalArgumentException("invalid delay " + delayMs);
        }

        this.currentTime = currentTime;
        bumpNextTime(delayMs);
    }

    /**
     * Gets the delay until the item should be fired.
     *
     * @return the delay until the item should be fired
     */
    public long getDelay() {
        return (nextMs - currentTime.getMillis());
    }

    /**
     * Determines if this work item was canceled.
     *
     * @return {@code true} if this item was canceled, {@code false} otherwise
     */
    public boolean wasCancelled() {
        return false;
    }

    /**
     * Bumps {@link #nextMs}, if this is a periodic task. The default method simply
     * returns {@code false}.
     *
     * @return {@code true} if the time was bumped, {@code false} otherwise (i.e., it is
     *         not a periodic task)
     */
    public boolean bumpNextTime() {
        return false;
    }

    /**
     * Bumps {@link #nextMs}, setting it to the current time plus the given delay.
     *
     * @param delayMs time, in milliseconds, before this item should be (re-)executed
     */
    protected void bumpNextTime(long delayMs) {
        if (delayMs < 0) {
            throw new IllegalArgumentException("negative delay");
        }

        // always bump by at least 1 millisecond
        this.nextMs = currentTime.getMillis() + Math.max(1, delayMs);
    }

    /**
     * Interrupts the thread that created the work item, if appropriate. The default
     * method does nothing.
     */
    public void interrupt() {
        // do nothing
    }

    /**
     * Determines if this item is associated with the given object. The default method
     * simply returns {@code false}.
     *
     * @param associate candidate associate (e.g., Timer)
     * @return {@code true} if the item is associated with the given object, {@code false}
     *         otherwise
     */
    public boolean isAssociatedWith(Object associate) {
        return false;
    }

    /**
     * Fires/executes this item. The default method does nothing.
     */
    public void fire() {
        // do nothing
    }
}
