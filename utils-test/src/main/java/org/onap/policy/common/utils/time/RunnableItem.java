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

import java.util.concurrent.Future;
import lombok.AccessLevel;
import lombok.Getter;
import org.onap.policy.common.utils.time.TestTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Work item that may be run/executed.
 */
class RunnableItem extends WorkItem {
    private static final Logger logger = LoggerFactory.getLogger(RunnableItem.class);

    /**
     * Object with which this item is associated.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Object associate;

    /**
     * Action to execute.
     */
    @Getter(AccessLevel.PROTECTED)
    private final Runnable action;


    /**
     * Constructs the object.
     *
     * @param currentTime time with which this item is associated
     * @param associate object with which this item is associated (e.g., Timer)
     * @param delayMs time, in milliseconds, before this item should be executed
     * @param action action to be performed
     */
    public RunnableItem(TestTime currentTime, Object associate, long delayMs, Runnable action) {
        super(currentTime, delayMs);
        this.associate = associate;
        this.action = action;

        // ensure the task can properly compute its delay
        if (action instanceof PseudoScheduledFuture) {
            ((PseudoScheduledFuture<?>) action).setWorkItem(this);
        }
    }

    @Override
    public boolean isAssociatedWith(Object associate) {
        return (this.associate == associate);
    }

    @Override
    public boolean wasCancelled() {
        return (action instanceof Future && ((Future<?>) action).isCancelled());
    }

    @Override
    public void fire() {
        try {
            action.run();
        } catch (RuntimeException e) {
            logger.warn("work item {} threw an exception {}", this, e);
        }
    }

    @Override
    public String toString() {
        return "RunnableItem [nextMs=" + getNextMs() + ", associate=" + associate + "]";
    }
}
