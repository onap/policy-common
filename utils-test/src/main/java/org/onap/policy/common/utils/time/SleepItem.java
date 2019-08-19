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

import java.util.concurrent.CountDownLatch;

/**
 * Work item used when a thread invokes sleep(). The thread's "sleep()" method will
 * enqueue this item and then invoke {@link #await()} to wait for the test/controlling
 * thread to fire it, indicating that the end of the sleep time has been reached.
 */
public class SleepItem extends WorkItem {
    /**
     * Thread that invoked "sleep()".
     */
    private final Thread thread;

    /**
     * This will be decremented when this work item is fired, thus releasing the
     * "sleeping" thread to continue its work.
     */
    private final CountDownLatch latch = new CountDownLatch(1);


    /**
     * Constructs the object.
     *
     * @param currentTime time with which this item is associated
     * @param sleepMs time for which the thread should sleep
     * @param thread thread that invoked "sleep()"
     */
    public SleepItem(TestTime currentTime, long sleepMs, Thread thread) {
        super(currentTime, sleepMs);
        this.thread = thread;
    }

    @Override
    public void interrupt() {
        thread.interrupt();
    }

    @Override
    public void fire() {
        latch.countDown();
    }

    /**
     * Waits for the sleep time to be reached.
     *
     * @throws InterruptedException if the current thread is interrupted
     */
    public void await() throws InterruptedException {
        latch.await();
    }

    @Override
    public String toString() {
        return "SleepItem [nextMs=" + getNextMs() + ", latch=" + latch + ", thread=" + thread + "]";
    }
}
