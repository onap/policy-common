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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.Timer;
import java.util.TimerTask;
import org.mockito.invocation.InvocationOnMock;

/**
 * A timer factory that uses {@link PseudoTime} to execute its tasks.
 */
public class PseudoTimerFactory extends TimerFactory {

    /**
     * Time with which this item is associated.
     */
    private final PseudoTime currentTime;

    /**
     * Constructs the object.
     *
     * @param currentTime object to be used to execute timer tasks
     */
    public PseudoTimerFactory(PseudoTime currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    public Timer makeTimer() {
        Timer timer = mock(Timer.class);

        // @formatter:off
        doAnswer(invocation -> doScheduleAtFixedRate(timer, invocation))
            .when(timer)
            .scheduleAtFixedRate(any(), anyLong(), anyLong());

        doAnswer(invocation -> doScheduleTask(timer, invocation))
            .when(timer)
            .schedule(any(), anyLong());

        doAnswer(invocation -> currentTime.cancelItems(timer))
            .when(timer)
            .cancel();
        // @formatter:on

        return timer;
    }

    /**
     * Schedules a repeated task by adding an item to the queue.
     *
     * @param timer timer with which the task should be associated
     * @param invocation arguments passed to scheduleAtFixedRate()
     * @return {@code null}
     */
    private Void doScheduleAtFixedRate(Timer timer, InvocationOnMock invocation) {
        TimerTask task = invocation.getArgumentAt(0, TimerTask.class);
        long delayMs = invocation.getArgumentAt(1, Long.class);
        long periodMs = invocation.getArgumentAt(2, Long.class);

        currentTime.enqueue(new PeriodicItem(currentTime, timer, delayMs, periodMs, task));

        return null;
    }

    /**
     * Schedules a one-time task by adding an item to the queue.
     *
     * @param timer timer with which the task should be associated
     * @param invocation arguments passed to schedule()
     * @return {@code null}
     */
    private Void doScheduleTask(Timer timer, InvocationOnMock invocation) {
        TimerTask task = invocation.getArgumentAt(0, TimerTask.class);
        long delayMs = invocation.getArgumentAt(1, Long.class);

        currentTime.enqueue(new RunnableItem(currentTime, timer, delayMs, task));

        return null;
    }
}
