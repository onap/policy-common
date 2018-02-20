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

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 */
public class EventTrackInfoTest {
	
	private static final Instant istart = Instant.ofEpochMilli(100000l);
	private static final Instant iend = Instant.ofEpochMilli(200000l);
	
	private static final EventData data1 = new EventData("abc", istart, iend);
	private static final EventData data2 = new EventData("def", iend, istart);
	
	private EventTrackInfo info;
	
	@Before
	public void setUp() {
		info = new EventTrackInfo();
		
	}

	/**
	 * Test method for {@link EventTrackInfo#EventTrackInfo()}.
	 */
	@Test
	public void testEventTrackInfo() {
		assertNotNull(info.getEventInfo());
	}

	/**
	 * Test method for {@link EventTrackInfo#getEventDataByRequestID(String)}.
	 */
	@Test
	public void testGetEventDataByRequestID() {
		info.storeEventData(data1);
		info.storeEventData(data2);

		assertTrue(data1 == info.getEventDataByRequestID("abc"));
		assertTrue(data2 == info.getEventDataByRequestID("def"));
		assertNull(info.getEventDataByRequestID("hello"));
	}

	/**
	 * Test method for {@link EventTrackInfo#storeEventData(EventData)}.
	 */
	@Test
	public void testStoreEventData() {
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
		assertTrue(data1 == info.getEventDataByRequestID("abc"));
		assertTrue(data2 == info.getEventDataByRequestID("def"));
		
		// doesn't replace existing value
		info.storeEventData(new EventData("abc", iend, istart));
		assertEquals(2, info.getEventInfo().size());
		assertTrue(data1 == info.getEventDataByRequestID("abc"));
		assertTrue(data2 == info.getEventDataByRequestID("def"));
	}

	/**
	 * Test method for {@link EventTrackInfo#remove(String)}.
	 */
	@Test
	public void testRemove() {
		info.storeEventData(data1);
		info.storeEventData(data2);
		
		info.remove("abc");
		
		// ensure only that item was removed
		assertEquals(1, info.getEventInfo().size());

		// look-up by request id
		assertNull(info.getEventDataByRequestID("abc"));
		assertTrue(data2 == info.getEventDataByRequestID("def"));
	}

	/**
	 * Test method for {@link EventTrackInfo#getEventInfo()}.
	 */
	@Test
	public void testGetEventInfo() {		
		info.storeEventData(data1);
		info.storeEventData(data2);
		
		assertEquals(2, info.getEventInfo().size());
		assertTrue(data1 == info.getEventInfo().get("abc"));
		assertTrue(data2 == info.getEventInfo().get("def"));
	}

}
