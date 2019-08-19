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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.FutureTask;
import org.junit.Before;
import org.junit.Test;

public class RunnableItemTest {
    private static final long DELAY_MS = 100L;
    private static final Object ASSOCIATE = new Object();

    private TestTime currentTime;
    private int count;
    private RunnableItem item;

    /**
     * Sets up objects, including {@link #item}.
     */
    @Before
    public void setUp() {
        currentTime = new TestTime();
        count = 0;
        item = new RunnableItem(currentTime, ASSOCIATE, DELAY_MS, () -> count++);
    }

    @Test
    public void testWasCancelled() {
        assertFalse(item.wasCancelled());

        FutureTask<Object> future = new FutureTask<>(() -> count++);
        item = new RunnableItem(currentTime, ASSOCIATE, DELAY_MS, future);
        assertFalse(item.wasCancelled());

        future.cancel(true);
        assertTrue(item.wasCancelled());
    }

    @Test
    public void testIsAssociatedWith() {
        assertFalse(item.isAssociatedWith(this));
        assertTrue(item.isAssociatedWith(ASSOCIATE));
    }

    @Test
    public void testFire() {
        item.fire();
        assertEquals(1, count);

        // verify that fire() works even if the action throws an exception
        new RunnableItem(currentTime, ASSOCIATE, DELAY_MS, () -> {
            throw new RuntimeException("expected exception");
        }).fire();
    }

    @Test
    public void testRunnableItem_testGetAssociate_testGetAction() {
        assertSame(ASSOCIATE, item.getAssociate());
        assertNotNull(item.getAction());
        assertEquals(currentTime.getMillis() + DELAY_MS, item.getNextMs());

        item.getAction().run();
        assertEquals(1, count);

        // verify that work item is set when constructed with a future
        PseudoScheduledFuture<Integer> schedFuture = new PseudoScheduledFuture<>(() -> count + 1, false);
        item = new RunnableItem(currentTime, ASSOCIATE, DELAY_MS, schedFuture);
        assertSame(item, schedFuture.getWorkItem());

        // verify that work item is NOT set when constructed with a plain future
        item = new RunnableItem(currentTime, ASSOCIATE, DELAY_MS, new FutureTask<>(() -> count + 1));
    }

    @Test
    public void testToString() {
        assertNotNull(item.toString());
    }
}
