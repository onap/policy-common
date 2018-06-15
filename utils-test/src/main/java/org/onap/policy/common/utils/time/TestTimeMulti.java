/*
 * ============LICENSE_START=======================================================
 * Common Utils-Test
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

import java.util.Date;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;

/**
 * "Current" time, when running junit tests in multiple threads. This is intended to be
 * injected into classes under test, to replace their {@link CurrentTime} objects. The
 * {@link #sleep(long)} method blocks until all threads enter and then it moves the notion
 * of "current" time forward, allowing threads to resume, as the end of their sleep time
 * is reached. Additional threads do not resume until all threads have once again entered
 * {@link #sleep(long)} or when {@link #threadCompleted()} is invoked to indicate that a
 * thread will not re-enter {@link #sleep(long)}.
 */
public class TestTimeMulti extends CurrentTime {

    /**
     * Number of threads that will be sleeping simultaneously.
     */
    private int nthreads;

    /**
     * "Current" time, in milliseconds, used by tests.
     */
    private long tcur = System.currentTimeMillis();

    /**
     * Queue of sleeping threads waiting to be awakened.
     */
    private final PriorityQueue<Info> queue = new PriorityQueue<>();

    /**
     * Used to synchronize updates.
     */
    private final Object locker = new Object();

    /**
     * 
     * @param nthreads number of threads that will be sleeping simultaneously
     */
    public TestTimeMulti(int nthreads) {
        this.nthreads = nthreads;
    }

    @Override
    public long getMillis() {
        return tcur;
    }

    @Override
    public Date getDate() {
        return new Date(tcur);
    }

    @Override
    public void sleep(long sleepMs) throws InterruptedException {
        if (sleepMs <= 0) {
            return;
        }

        Info info = new Info(tcur + sleepMs);

        synchronized (locker) {
            queue.add(info);

            if (queue.size() == nthreads) {
                // all threads are now sleeping - wake one up
                wakeThreads();
            }
        }

        // this MUST happen outside of the "synchronized" block
        info.await();
    }

    /**
     * Indicates that a thread has terminated or that it will no longer be invoking
     * {@link #sleep(long)}. Awakens the next sleeping thread, if the queue is full after
     * removing the terminated thread.
     * 
     * @throws IllegalStateException if the queue is already full
     */
    public void threadCompleted() {
        synchronized (locker) {
            int sz = queue.size();
            if (sz >= nthreads) {
                throw new IllegalStateException("too many threads still sleeping");
            }

            --nthreads;

            if (sz == nthreads) {
                // after removing terminated thread - queue is now full; awaken something
                wakeThreads();
            }
        }
    }

    /**
     * Advances the "current" time and awakens any threads sleeping until that time.
     */
    private void wakeThreads() {
        Info info = queue.poll();
        if(info == null) {
            return;
        }

        tcur = info.getAwakenAtMs();
        info.wake();

        while ((info = queue.poll()) != null) {
            if (tcur == info.getAwakenAtMs()) {
                info.wake();

            } else {
                // not ready to wake this thread - put it back in the queue
                queue.add(info);
                break;
            }
        }
    }

    /**
     * Info about a sleeping thread.
     */
    private static class Info implements Comparable<Info> {

        /**
         * Time, in milliseconds, at which the associated thread should awaken.
         */
        private final long awakenAtMs;

        /**
         * This is triggered when the associated thread should awaken.
         */
        private final CountDownLatch latch = new CountDownLatch(1);

        /**
         * @param awakenAtMs time, in milliseconds, at which the associated thread should
         *        awaken
         */
        public Info(long awakenAtMs) {
            this.awakenAtMs = awakenAtMs;
        }

        public long getAwakenAtMs() {
            return awakenAtMs;
        }

        /**
         * Awakens the associated thread by decrementing its latch.
         */
        public void wake() {
            latch.countDown();
        }

        /**
         * Blocks the current thread until awakened (i.e., until its latch is
         * decremented).
         * 
         * @throws InterruptedException
         */
        public void await() throws InterruptedException {
            latch.await();
        }

        @Override
        public int compareTo(Info o) {
            int diff = Long.compare(awakenAtMs, o.awakenAtMs);

            // this assumes that Object.toString() is unique for each Info object
            if (diff == 0)
                diff = this.toString().compareTo(o.toString());

            return diff;
        }

    }
}
