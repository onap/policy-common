/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

public class DbDaoTransactionException extends IntegrityAuditException {
	private static final long serialVersionUID = 1L;
	public DbDaoTransactionException() {
		super();
	}
	public DbDaoTransactionException(String message) {
		super(message);
	}

	public DbDaoTransactionException(Throwable cause) {
		super(cause);
	}
	public DbDaoTransactionException(String message, Throwable cause) {
		super(message, cause);
	}
}
