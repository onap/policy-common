/*--
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

/**
 * Work item that runs periodically.
 */
class PeriodicItem extends RunnableItem {

    /**
     * Time, in milliseconds, to wait between executions.
     */
    private final long periodMs;


    /**
     * Constructs the object.
     *
     * @param currentTime time with which this item is associated
     * @param associate object with which this item is associated (e.g., Timer)
     * @param delayMs time, in milliseconds, before this item should be executed
     * @param periodMs time, in milliseconds, to delay between each execution
     * @param action action to be performed
     */
    public PeriodicItem(TestTime currentTime, Object associate, long delayMs, long periodMs, Runnable action) {
        super(currentTime, associate, delayMs, action);

        if (periodMs <= 0) {
            throw new IllegalArgumentException("invalid period " + periodMs);
        }

        this.periodMs = periodMs;
    }

    @Override
    public boolean bumpNextTime() {
        bumpNextTime(periodMs);
        return true;
    }

    @Override
    public String toString() {
        return "PeriodicItem [nextMs=" + getNextMs() + ", periodMs=" + periodMs + ", associate=" + getAssociate() + "]";
    }
}
