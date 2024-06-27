/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PeriodicItemTest {
    private static final long DELAY_MS = 100L;
    private static final long PERIOD_MS = 200L;
    private static final Object ASSOCIATE = new Object();

    private TestTime currentTime;
    private int count;
    private PeriodicItem item;

    /**
     * Sets up objects, including {@link #item}.
     */
    @BeforeEach
    public void setUp() {
        currentTime = new TestTime();
        count = 0;
        item = new PeriodicItem(currentTime, ASSOCIATE, DELAY_MS, PERIOD_MS, () -> count++);
    }

    @Test
    void testBumpNextTime() {
        assertTrue(item.bumpNextTime());
        assertEquals(currentTime.getMillis() + PERIOD_MS, item.getNextMs());
    }

    @Test
    void testToString() {
        assertNotNull(item.toString());
    }

    @Test
    void testPeriodicItem() {
        assertSame(ASSOCIATE, item.getAssociate());
        assertNotNull(item.getAction());
        assertEquals(currentTime.getMillis() + DELAY_MS, item.getNextMs());

        item.getAction().run();
        assertEquals(1, count);

        // invalid period
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new PeriodicItem(currentTime, ASSOCIATE, DELAY_MS, 0, () -> count++));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new PeriodicItem(currentTime, ASSOCIATE, DELAY_MS, -1, () -> count++));
    }

}
