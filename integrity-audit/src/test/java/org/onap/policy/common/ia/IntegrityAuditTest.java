/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.ia;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;
import org.onap.policy.common.ia.IntegrityAudit;
import org.onap.policy.common.ia.IntegrityAuditProperties;

public class IntegrityAuditTest {

	@Test
	/**
	 * Test if we can access the updated bad params outside of the parmsAreBad method
	 */
	public void parmsAreBadTest() {
		// Try with 2 null params
		StringBuilder badParams = new StringBuilder();
		IntegrityAudit.parmsAreBad(null, "something", null, badParams);
		
		assertFalse("".equals(badParams.toString()));
		assertTrue(badParams.toString().contains("resourceName"));
		assertTrue(badParams.toString().contains("properties"));
		
		// Try with 1 null params
		badParams = new StringBuilder();
		Properties props = new Properties();
		props.put(IntegrityAuditProperties.DB_DRIVER, "test_db_driver");
		IntegrityAudit.parmsAreBad(null, "something", props, badParams);
		
		assertFalse("".equals(badParams.toString()));
		assertTrue(badParams.toString().contains("resourceName"));
		assertFalse(badParams.toString().contains("properties"));
		
		// Try with 0 null params
		badParams = new StringBuilder();
		IntegrityAudit.parmsAreBad("someting", "something", props, badParams);
		assertFalse("".equals(badParams.toString()));
		assertFalse(badParams.toString().contains("resourceName"));
		assertFalse(badParams.toString().contains("properties"));
		
		// Try with invalid node type
		props.put(IntegrityAuditProperties.NODE_TYPE, "bogus");
		badParams = new StringBuilder();
		IntegrityAudit.parmsAreBad("someting", "something", props, badParams);
		assertFalse("".equals(badParams.toString()));
		assertTrue(badParams.toString().contains("nodeType"));

	}

}
