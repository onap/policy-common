/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PseudoScheduledFutureTest {
    private static final long DELAY_MS = 1000L;

    private int count;

    @Mock
    private WorkItem work;

    private PseudoScheduledFuture<Integer> future;

    /**
     * Sets up objects, including {@link #future}.
     */
    @BeforeEach
    public void setUp() {
        lenient().when(work.getDelay()).thenReturn(DELAY_MS);

        count = 0;
        future = new PseudoScheduledFuture<>(() -> ++count, true);
        future.setWorkItem(work);
    }

    @Test
    void testRun() {
        // verify with a periodic task - should execute twice
        count = 0;
        future.run();
        future.run();
        assertEquals(2, count);

        // verify with an aperiodic task - should only execute once
        future = new PseudoScheduledFuture<>(() -> ++count, false);
        count = 0;
        future.run();
        future.run();
        assertEquals(1, count);
    }

    @Test
    void testPseudoScheduledFutureRunnableTBoolean() throws Exception {
        final Integer result = 100;
        future = new PseudoScheduledFuture<>(() -> ++count, result, true);
        assertTrue(future.isPeriodic());
        future.run();
        future.run();
        assertEquals(2, count);

        // verify with aperiodic constructor
        future = new PseudoScheduledFuture<>(() -> ++count, result, false);
        count = 0;
        assertFalse(future.isPeriodic());
        future.run();
        future.run();
        assertEquals(1, count);
        assertEquals(result, future.get());
    }

    @Test
    void testPseudoScheduledFutureCallableOfTBoolean() throws Exception {
        assertTrue(future.isPeriodic());
        future.run();
        future.run();
        assertEquals(2, count);

        // verify with aperiodic constructor
        future = new PseudoScheduledFuture<>(() -> ++count, false);
        count = 0;
        assertFalse(future.isPeriodic());
        future.run();
        assertEquals(1, future.get().intValue());
        future.run();
        assertEquals(1, count);
    }

    @Test
    void testGetDelay() {
        assertEquals(DELAY_MS, future.getDelay(TimeUnit.MILLISECONDS));
        assertEquals(TimeUnit.MILLISECONDS.toSeconds(DELAY_MS), future.getDelay(TimeUnit.SECONDS));
    }

    @Test
    void testCompareTo() {
        Delayed delayed = mock(Delayed.class);
        when(delayed.getDelay(TimeUnit.MILLISECONDS)).thenReturn(DELAY_MS + 1);

        assertTrue(future.compareTo(delayed) < 0);
    }

    @Test
    void testIsPeriodic() {
        assertTrue(future.isPeriodic());
        assertFalse(new PseudoScheduledFuture<>(() -> ++count, false).isPeriodic());
    }

    @Test
    void testGetWorkItem() {
        assertSame(work, future.getWorkItem());
    }

    @Test
    void testSetWorkItem() {
        work = mock(WorkItem.class);
        future.setWorkItem(work);
        assertSame(work, future.getWorkItem());
    }

}
