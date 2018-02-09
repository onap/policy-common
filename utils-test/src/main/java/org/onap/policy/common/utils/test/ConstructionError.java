/*
 * ============LICENSE_START=======================================================
 * Common Utils-Test
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

package org.onap.policy.common.utils.test;

/**
 * An error that occurred while trying to construct an object for a junit test.
 */
public class ConstructionError extends AssertionError {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ConstructionError() {
		super();
	}

	/**
	 * @param message
	 */
	public ConstructionError(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ConstructionError(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConstructionError(String message, Throwable cause) {
		super(message, cause);
	}

}
