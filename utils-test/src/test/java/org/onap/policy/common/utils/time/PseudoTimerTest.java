/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.TimerTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PseudoTimerTest {
    private static final long DELAY_MS = 1000L;
    private static final long PERIOD_MS = 2000L;

    private int count;
    private TestTimeMulti currentTime;
    private PseudoTimer timer;

    /**
     * Sets up objects, including {@link #timer}.
     */
    @BeforeEach
    public void setUp() {
        count = 0;
        currentTime = new TestTimeMulti();
        timer = new PseudoTimer(currentTime);
    }

    @Test
    void testCancel() {
        // schedule two tasks
        timer.scheduleAtFixedRate(new MyTask(), DELAY_MS, PERIOD_MS);
        timer.schedule(new MyTask(), DELAY_MS);

        assertFalse(currentTime.isEmpty());

        // cancel the timer
        timer.cancel();

        // invoke it again to ensure no exception
        timer.cancel();
    }

    @Test
    void testPurge() {
        assertEquals(0, timer.purge());
        assertEquals(0, timer.purge());
    }

    @Test
    void testScheduleTimerTaskLong() throws InterruptedException {
        timer.schedule(new MyTask(), DELAY_MS);
        assertFalse(currentTime.isEmpty());

        // wait for the initial delay
        currentTime.waitFor(DELAY_MS);
        assertEquals(1, count);

        assertTrue(currentTime.isEmpty());
    }

    @Test
    void testScheduleTimerTaskDate() {
        MyTask task = new MyTask();
        Date curdate = new Date();
        assertThatThrownBy(() -> timer.schedule(task, curdate)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testScheduleTimerTaskLongLong() throws InterruptedException {
        timer.schedule(new MyTask(), DELAY_MS, PERIOD_MS);
        assertFalse(currentTime.isEmpty());

        // wait for the initial delay plus a couple of additional periods
        final long tbegin = System.currentTimeMillis();
        currentTime.waitFor(DELAY_MS + PERIOD_MS * 2);
        assertTrue(count >= 3);

        assertFalse(currentTime.isEmpty());

        // this thread should not have blocked while waiting
        assertTrue(System.currentTimeMillis() < tbegin + 2000);
    }

    @Test
    void testScheduleTimerTaskDateLong() {
        MyTask task = new MyTask();
        Date curdate = new Date();
        assertThatThrownBy(() -> timer.schedule(task, curdate, 1L)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testScheduleAtFixedRateTimerTaskLongLong() throws InterruptedException {
        timer.scheduleAtFixedRate(new MyTask(), DELAY_MS, PERIOD_MS);
        assertFalse(currentTime.isEmpty());

        // wait for the initial delay plus a couple of additional periods
        final long tbegin = System.currentTimeMillis();
        currentTime.waitFor(DELAY_MS + PERIOD_MS * 2);
        assertTrue(count >= 3);

        assertFalse(currentTime.isEmpty());

        // this thread should not have blocked while waiting
        assertTrue(System.currentTimeMillis() < tbegin + 2000);
    }

    @Test
    void testScheduleAtFixedRateTimerTaskDateLong() {
        MyTask task = new MyTask();
        Date curdate = new Date();
        assertThatThrownBy(() -> timer.scheduleAtFixedRate(task, curdate, 1L))
                        .isInstanceOf(UnsupportedOperationException.class);
    }

    private class MyTask extends TimerTask {
        @Override
        public void run() {
            ++count;
        }

    }
}
