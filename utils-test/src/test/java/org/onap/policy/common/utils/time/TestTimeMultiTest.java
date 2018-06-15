/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * 
 */
public class TestTimeMultiTest {

    private static final int NTHREADS = 10;
    private static final int NTIMES = 100;
    private static final long WAIT_SEC = 5L;
    private static final long MIN_SLEEP_MS = 5L;

    private TestTimeMulti ttm;
    private Semaphore done;

    @Test
    public void test() throws Exception {
        ttm = new TestTimeMulti(NTHREADS);
        done = new Semaphore(0);

        long tbeg = ttm.getMillis();

        // create threads
        List<MyThread> threads = new ArrayList<>(NTHREADS);
        for (int x = 0; x < NTHREADS; ++x) {
            threads.add(new MyThread(x + MIN_SLEEP_MS));
        }

        // launch threads
        for (MyThread thr : threads) {
            thr.start();
        }

        // wait for each one to complete
        for (MyThread thr : threads) {
            assertTrue("complete " + thr.getSleepMs(), done.tryAcquire(WAIT_SEC, TimeUnit.HOURS));
            ttm.threadCompleted();
        }

        // check results
        for (MyThread thr : threads) {
            assertEquals("time " + thr.getSleepMs(), thr.texpected, thr.tactual);
        }

        assertTrue(ttm.getMillis() >= tbeg + NTIMES * MIN_SLEEP_MS);
    }

    private class MyThread extends Thread {

        private final long sleepMs;

        private volatile long texpected;
        private volatile long tactual;

        public MyThread(long sleepMs) {
            this.sleepMs = sleepMs;

            this.setDaemon(true);
        }

        public long getSleepMs() {
            return sleepMs;
        }

        @Override
        public void run() {
            try {
                for (int x = 0; x < NTIMES; ++x) {
                    texpected = ttm.getMillis() + sleepMs;
                    ttm.sleep(sleepMs);

                    if ((tactual = ttm.getMillis()) != texpected) {
                        break;
                    }

                    if ((tactual = ttm.getDate().getTime()) != texpected) {
                        break;
                    }
                }

            } catch (InterruptedException expected) {
                Thread.currentThread().interrupt();
            }

            done.release();
        }
    }
}
