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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorkItemTest {
    private TestTime currentTime;
    private WorkItem item;

    @BeforeEach
    public void setUp() {
        currentTime = new TestTime();
        item = new WorkItem(currentTime, 0);
    }

    @Test
    public void testWorkItem() {
        assertThatIllegalArgumentException().isThrownBy(() -> new WorkItem(currentTime, -1));

        // should not throw an exception
        new WorkItem(currentTime, 1);
    }

    @Test
    public void testGetDelay() {
        assertEquals(1, item.getDelay());
    }

    @Test
    public void testWasCancelled() {
        assertFalse(item.wasCancelled());
    }

    @Test
    public void testBumpNextTime() {
        assertFalse(item.bumpNextTime());
    }

    @Test
    public void testBumpNextTimeLong() {
        assertThatIllegalArgumentException().isThrownBy(() -> item.bumpNextTime(-1));

        long cur = currentTime.getMillis();
        item.bumpNextTime(5);
        assertEquals(cur + 5, item.getNextMs());

        item.bumpNextTime(0);

        // should bump the time by at least 1
        assertEquals(cur + 1, item.getNextMs());
    }

    @Test
    public void testInterrupt() {
        item.interrupt();
        assertFalse(Thread.interrupted());
    }

    @Test
    public void testIsAssociatedWith() {
        assertFalse(item.isAssociatedWith(this));
    }

    @Test
    public void testFire() {
        // ensure no exception is thrown
        assertThatCode(() -> item.fire()).doesNotThrowAnyException();
    }

    @Test
    public void testGetNextMs() {
        assertEquals(currentTime.getMillis() + 1, item.getNextMs());
        assertEquals(currentTime.getMillis() + 10, new WorkItem(currentTime, 10).getNextMs());
    }

}
