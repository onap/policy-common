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

import org.junit.Test;

/**
 * 
 */
public class EventDataTest {
	private static final Instant istart = Instant.ofEpochMilli(100000l);
	private static final Instant iend = Instant.ofEpochMilli(200000l);

	/**
	 * Test method for {@link EventData#EventData()}.
	 */
	@Test
	public void testEventData() {
		EventData d = new EventData();

		assertNull(d.getEndTime());
		assertNull(d.getRequestID());
		assertNull(d.getStartTime());
	}

	/**
	 * Test method for {@link EventData#EventData(String, Instant, Instant)}.
	 */
	@Test
	public void testEventDataStringInstantInstant() {
		EventData d = new EventData("myreq", istart, iend);

		assertEquals("myreq", d.getRequestID());
		assertEquals(istart, d.getStartTime());
		assertEquals(iend, d.getEndTime());
	}

	/**
	 * Test method for {@link EventData#getRequestID()} and
	 * {@link EventData#setRequestID(String)}.
	 */
	@Test
	public void testGetSetRequestID() {
		EventData d = new EventData();
		assertNull(d.getRequestID());

		d.setRequestID("abc");
		assertEquals("abc", d.getRequestID());

		d.setRequestID("def");
		assertEquals("def", d.getRequestID());
	}

	/**
	 * Test method for {@link EventData#getStartTime()} and
	 * {@link EventData#setStartTime(Instant)}.
	 */
	@Test
	public void testGetSetStartTime() {
		EventData d = new EventData();
		assertNull(d.getStartTime());

		d.setStartTime(istart);
		assertEquals(istart, d.getStartTime());

		d.setStartTime(iend);
		assertEquals(iend, d.getStartTime());

		// setting end-time should not effect start-time
		d.setEndTime(istart);
		assertEquals(iend, d.getStartTime());
	}

	/**
	 * Test method for {@link EventData#getEndTime()} and
	 * {@link EventData#setEndTime(Instant)}.
	 */
	@Test
	public void testGetSetEndTime() {
		EventData d = new EventData();
		assertNull(d.getEndTime());

		d.setEndTime(iend);
		assertEquals(iend, d.getEndTime());

		d.setEndTime(istart);
		assertEquals(istart, d.getEndTime());

		// setting start-time should not effect end-time
		d.setStartTime(iend);
		assertEquals(istart, d.getEndTime());
	}

	/**
	 * Test method for {@link EventData#toString()}.
	 */
	@Test
	public void testToString() {
		EventData d = new EventData("myreq", istart, iend);
		assertEquals("myreq Starting Time : 1970-01-01T00:01:40Z Ending Time : 1970-01-01T00:03:20Z", d.toString());
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
		EventData d1 = new EventData("abc", istart, iend);
		EventData d2 = new EventData("abd", istart, iend);
		EventData d3 = new EventData("abc", iend, istart);

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
