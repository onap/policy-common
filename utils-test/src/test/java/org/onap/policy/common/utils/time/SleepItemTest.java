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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class SleepItemTest {
    private static final int SLEEP_MS = 250;
    private static final long MAX_WAIT_MS = 5000L;

    private TestTime currentTime;
    private Thread thread;
    private CountDownLatch started;
    private CountDownLatch finished;
    private volatile InterruptedException threadEx;
    private SleepItem item;

    /**
     * Sets up objects, including a Thread and a SleepItem.
     */
    @Before
    public void setUp() {
        currentTime = new TestTime();
        started = new CountDownLatch(1);
        finished = new CountDownLatch(1);

        thread = new Thread() {
            @Override
            public void run() {
                try {
                    started.countDown();
                    item.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    threadEx = e;
                }

                finished.countDown();
            }
        };
        thread.setDaemon(true);

        item = new SleepItem(currentTime, SLEEP_MS, thread);
    }

    @Test
    public void testInterrupt() throws InterruptedException {
        startThread();

        item.interrupt();

        assertTrue(finished.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS));
        assertNotNull(threadEx);
    }

    @Test
    public void testFire_testAwait() throws InterruptedException {
        startThread();

        // verify that it hasn't finished yet
        thread.join(250);
        assertTrue(finished.getCount() > 0);

        // now fire it and verify that it finishes
        item.fire();
        assertTrue(finished.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS));

        assertNull(threadEx);
    }

    @Test
    public void testSleepItem() {
        assertEquals(currentTime.getMillis() + SLEEP_MS, item.getNextMs());
    }

    @Test
    public void testToString() {
        assertNotNull(item.toString());
    }


    private void startThread() throws InterruptedException {
        thread.start();
        started.await();
    }
}
