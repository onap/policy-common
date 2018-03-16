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

package org.onap.policy.common.logging.eelf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.junit.Test;

public class EventDataTest {
    private static final Instant istart = Instant.ofEpochMilli(100000L);
    private static final Instant iend = Instant.ofEpochMilli(200000L);

    /**
     * Test method for {@link EventData#EventData()}.
     */
    @Test
    public void testEventData() {
        EventData eventData = new EventData();

        assertNull(eventData.getEndTime());
        assertNull(eventData.getRequestID());
        assertNull(eventData.getStartTime());
    }

    /**
     * Test method for {@link EventData#EventData(String, Instant, Instant)}.
     */
    @Test
    public void testEventDataStringInstantInstant() {
        EventData eventData = new EventData("myreq", istart, iend);

        assertEquals("myreq", eventData.getRequestID());
        assertEquals(istart, eventData.getStartTime());
        assertEquals(iend, eventData.getEndTime());
    }

    /**
     * Test method for {@link EventData#getRequestID()} and {@link EventData#setRequestID(String)}.
     */
    @Test
    public void testGetSetRequestId() {
        EventData eventData = new EventData();
        assertNull(eventData.getRequestID());

        eventData.setRequestID("abc");
        assertEquals("abc", eventData.getRequestID());

        eventData.setRequestID("def");
        assertEquals("def", eventData.getRequestID());
    }

    /**
     * Test method for {@link EventData#getStartTime()} and {@link EventData#setStartTime(Instant)}.
     */
    @Test
    public void testGetSetStartTime() {
        EventData eventData = new EventData();
        assertNull(eventData.getStartTime());

        eventData.setStartTime(istart);
        assertEquals(istart, eventData.getStartTime());

        eventData.setStartTime(iend);
        assertEquals(iend, eventData.getStartTime());

        // setting end-time should not effect start-time
        eventData.setEndTime(istart);
        assertEquals(iend, eventData.getStartTime());
    }

    /**
     * Test method for {@link EventData#getEndTime()} and {@link EventData#setEndTime(Instant)}.
     */
    @Test
    public void testGetSetEndTime() {
        EventData eventData = new EventData();
        assertNull(eventData.getEndTime());

        eventData.setEndTime(iend);
        assertEquals(iend, eventData.getEndTime());

        eventData.setEndTime(istart);
        assertEquals(istart, eventData.getEndTime());

        // setting start-time should not effect end-time
        eventData.setStartTime(iend);
        assertEquals(istart, eventData.getEndTime());
    }

    /**
     * Test method for {@link EventData#toString()}.
     */
    @Test
    public void testToString() {
        EventData eventData = new EventData("myreq", istart, iend);
        assertEquals("myreq Starting Time : 1970-01-01T00:01:40Z Ending Time : 1970-01-01T00:03:20Z",
                eventData.toString());
    }

    /**
     * Test method for {@link EventData#hashCode()}.
     */
    @Test
    public void testHashCode() {
        int hc1 = new EventData("abc", istart, iend).hashCode();

        assertNotEquals(hc1, new EventData("abd", istart, iend).hashCode());
        assertEquals(hc1, new EventData("abc", iend, istart).hashCode());
    }

    /**
     * Test method for {@link EventData#equals(Object)}.
     */
    @Test
    public void testEqualsObject() {
        final EventData d1 = new EventData("abc", istart, iend);
        final EventData d2 = new EventData("abd", istart, iend);
        final EventData d3 = new EventData("abc", iend, istart);

        // same object
        assertTrue(d1.equals(d1));

        // compare with null
        assertFalse(d1.equals(null));

        // compare with request id
        assertTrue(d1.equals("abc"));
        assertFalse(d1.equals("abd"));

        // compare with int - different class type
        assertFalse(d1.equals(10));

        // "this" has null request id
        assertFalse(new EventData().equals(d1));

        // both null
        assertTrue(new EventData().equals(new EventData()));

        // this request id is not null, other is null
        assertFalse(d1.equals(new EventData()));

        // neither null, same
        assertTrue(d1.equals(d3));

        // neither null, diff
        assertFalse(d1.equals(d2));
    }

}
