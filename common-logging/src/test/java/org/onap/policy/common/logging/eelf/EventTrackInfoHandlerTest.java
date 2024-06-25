/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018, 2020 AT&T Intellectual Property. All rights reserved.
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

import java.time.Instant;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventTrackInfoHandlerTest {

    private static final Instant inow = Instant.now();
    private static final Instant iexpired = Instant.ofEpochMilli(10000L);

    private static final EventData data1 = new EventData("abc", inow, inow);
    private static final EventData data2 = new EventData("def", inow, inow);

    private static EventTrackInfo tracker;
    private static ConcurrentMap<String, EventData> info;

    private EventTrackInfoHandler hdlr;

    @BeforeAll
    public static void setUpBeforeClass() {
        tracker = PolicyLogger.getEventTracker();
        info = tracker.getEventInfo();
    }

    /**
     * Perform set up for test cases.
     */
    @BeforeEach
    void setUp() {
        info.clear();

        hdlr = new EventTrackInfoHandler();
    }

    @Test
    void testNoEvents() {
        hdlr.run();
        assertEquals(0, info.size());
    }

    @Test
    void testNothingExpired() {
        tracker.storeEventData(data1);
        tracker.storeEventData(data2);

        hdlr.run();
        assertEquals(2, info.size());
    }

    @Test
    void testSomeExpired() {
        // not expired
        tracker.storeEventData(data1);
        tracker.storeEventData(data2);

        // start time is expired
        tracker.storeEventData(new EventData("expiredA", iexpired, inow));
        tracker.storeEventData(new EventData("expiredB", iexpired, inow));

        // end time is expired, but that has no impact - these should be retained
        EventData oka = new EventData("okA", inow, iexpired);
        EventData okb = new EventData("okB", inow, iexpired);

        tracker.storeEventData(oka);
        tracker.storeEventData(okb);

        hdlr.run();
        assertEquals(4, info.size());

        assertEquals(data1, info.get("abc"));
        assertEquals(data2, info.get("def"));
        assertEquals(oka, info.get("okA"));
        assertEquals(okb, info.get("okB"));
    }

    @Test
    void testMultipleRuns() {

        hdlr.run();
        assertEquals(0, info.size());

        // not expired
        tracker.storeEventData(data1);
        tracker.storeEventData(data2);

        hdlr.run();
        assertEquals(2, info.size());

        // start time is expired
        tracker.storeEventData(new EventData("expiredA", iexpired, inow));
        tracker.storeEventData(new EventData("expiredB", iexpired, inow));

        // end time is expired, but that has no impact - these should be retained
        tracker.storeEventData(new EventData("okA", inow, iexpired));
        tracker.storeEventData(new EventData("okB", inow, iexpired));

        hdlr.run();
        assertEquals(4, info.size());

        hdlr.run();
        assertEquals(4, info.size());
    }

}
