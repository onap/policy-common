/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.logging.eelf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventTrackInfoTest {

    private static final Instant istart = Instant.ofEpochMilli(100000L);
    private static final Instant iend = Instant.ofEpochMilli(200000L);

    private static final EventData data1 = new EventData("abc", istart, iend);
    private static final EventData data2 = new EventData("def", iend, istart);

    private EventTrackInfo info;

    @BeforeEach
    void setUp() {
        info = new EventTrackInfo();

    }

    /**
     * Test method for {@link EventTrackInfo#EventTrackInfo()}.
     */
    @Test
    void testEventTrackInfo() {
        assertNotNull(info.getEventInfo());
    }

    private void assertNotNull(ConcurrentMap<String, EventData> eventInfo) {
    }

    /**
     * Test method for {@link EventTrackInfo#getEventDataByRequestId(String)}.
     */
    @Test
    void testGetEventDataByRequestId() {
        info.storeEventData(data1);
        info.storeEventData(data2);

        assertSame(data1, info.getEventDataByRequestId("abc"));
        assertSame(data2, info.getEventDataByRequestId("def"));
        assertNull(info.getEventDataByRequestId("hello"));
    }

    /**
     * Test method for {@link EventTrackInfo#storeEventData(EventData)}.
     */
    @Test
    void testStoreEventData() {
        // should ignore null
        info.storeEventData(null);
        assertTrue(info.getEventInfo().isEmpty());

        // should ignore if request id is null or empty
        info.storeEventData(new EventData());
        info.storeEventData(new EventData("", istart, iend));
        assertTrue(info.getEventInfo().isEmpty());

        info.storeEventData(data1);
        info.storeEventData(data2);
        assertEquals(2, info.getEventInfo().size());

        // look-up by request id
        assertSame(data1, info.getEventDataByRequestId("abc"));
        assertSame(data2, info.getEventDataByRequestId("def"));

        // doesn't replace existing value
        info.storeEventData(new EventData("abc", iend, istart));
        assertEquals(2, info.getEventInfo().size());
        assertSame(data1, info.getEventDataByRequestId("abc"));
        assertSame(data2, info.getEventDataByRequestId("def"));
    }

    /**
     * Test method for {@link EventTrackInfo#remove(String)}.
     */
    @Test
    void testRemove() {
        info.storeEventData(data1);
        info.storeEventData(data2);

        info.remove("abc");

        // ensure only that item was removed
        assertEquals(1, info.getEventInfo().size());

        // look-up by request id
        assertNull(info.getEventDataByRequestId("abc"));
        assertSame(data2, info.getEventDataByRequestId("def"));
    }

    /**
     * Test method for {@link EventTrackInfo#getEventInfo()}.
     */
    @Test
    void testGetEventInfo() {
        info.storeEventData(data1);
        info.storeEventData(data2);

        assertEquals(2, info.getEventInfo().size());
        assertSame(data1, info.getEventInfo().get("abc"));
        assertSame(data2, info.getEventInfo().get("def"));
    }

}
