/*
 * ============LICENSE_START=======================================================
 * Integrity Monitor
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

package org.onap.policy.common.im.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.policy.common.im.AdministrativeStateException;
import org.onap.policy.common.im.AllSeemsWellException;
import org.onap.policy.common.im.ForwardProgressException;
import org.onap.policy.common.im.IntegrityMonitorException;
import org.onap.policy.common.im.IntegrityMonitorPropertiesException;
import org.onap.policy.common.im.StandbyStatusException;
import org.onap.policy.common.im.StateManagementException;
import org.onap.policy.common.im.StateTransitionException;
import org.onap.policy.common.im.jmx.ComponentAdminException;
import org.onap.policy.common.utils.test.ExceptionsTester;

/**
 * Tests various Exception subclasses.
 */
public class ExceptionsTest extends ExceptionsTester {

	@Test
	public void testStateTransitionException() throws Exception {
		assertEquals(4, test(StateTransitionException.class));
	}

	@Test
	public void testStateManagementException() throws Exception {
		assertEquals(4, test(StateManagementException.class));
	}

	@Test
	public void testStandbyStatusException() throws Exception {
		assertEquals(5, test(StandbyStatusException.class));
	}

	@Test
	public void testIntegrityMonitorPropertiesException() throws Exception {
		assertEquals(4, test(IntegrityMonitorPropertiesException.class));
	}

	@Test
	public void testIntegrityMonitorException() throws Exception {
		assertEquals(4, test(IntegrityMonitorException.class));
	}

	@Test
	public void testForwardProgressException() throws Exception {
		assertEquals(4, test(ForwardProgressException.class));
	}

	@Test
	public void testAllSeemsWellException() throws Exception {
		assertEquals(4, test(AllSeemsWellException.class));
	}

	@Test
	public void testAdministrativeStateException() throws Exception {
		assertEquals(4, test(AdministrativeStateException.class));
	}

	@Test
	public void testComponentAdminException() throws Exception {
		assertEquals(4, test(ComponentAdminException.class));
	}
}
