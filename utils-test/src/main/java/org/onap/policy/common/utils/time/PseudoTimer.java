/*-
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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A timer that uses {@link TestTimeMulti} to execute its tasks.
 *
 * <p/>Note: this only supports the run() method of {@link TimerTask}; the other methods,
 * including cancel() are not supported.  However, tasks may be canceled via
 * {@link Timer#cancel()}.
 *
 * <p/>Currently, this does not support any of the scheduling methods that take dates,
 * though that could be added relatively easily.
 */
public class PseudoTimer extends Timer {
    private static final String NOT_IMPLEMENTED_YET = "not implemented yet";

    /**
     * Time with which this item is associated.
     */
    private final TestTimeMulti currentTime;


    /**
     * Constructs the object.
     *
     * @param currentTime object to be used to execute timer tasks
     */
    public PseudoTimer(TestTimeMulti currentTime) {
        // create as a daemon so jvm doesn't hang when it attempts to exit
        super(true);

        this.currentTime = currentTime;

        // don't need the timer's thread
        super.cancel();
    }

    @Override
    public void schedule(TimerTask task, long delayMs) {
        currentTime.enqueue(new RunnableItem(currentTime, this, delayMs, task));
    }

    @Override
    public void schedule(TimerTask task, Date time) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public void schedule(TimerTask task, long delayMs, long periodMs) {
        currentTime.enqueue(new PeriodicItem(currentTime, this, delayMs, periodMs, task));
    }

    @Override
    public void schedule(TimerTask task, Date firstTime, long period) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public void scheduleAtFixedRate(TimerTask task, long delayMs, long periodMs) {
        currentTime.enqueue(new PeriodicItem(currentTime, this, delayMs, periodMs, task));
    }

    @Override
    public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET);
    }

    @Override
    public void cancel() {
        currentTime.cancelItems(this);
    }

    @Override
    public int purge() {
        return 0;
    }
}
