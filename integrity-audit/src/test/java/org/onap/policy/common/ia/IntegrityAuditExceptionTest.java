/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
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
package org.onap.policy.common.ia;

import static org.junit.Assert.*;

import org.junit.Test;

public class IntegrityAuditExceptionTest {

	@Test
	public void test() {
		IntegrityAuditException e = new IntegrityAuditException();
		assertNull(e.getMessage());
		e = new IntegrityAuditException("");
		assertNotNull(e.getMessage());
		e = new IntegrityAuditException(new Throwable());
		assertNotNull(e.getCause());
		e = new IntegrityAuditException("", new Throwable());
		assertNotNull(e.getMessage());
		assertNotNull(e.getCause());
	}

}
