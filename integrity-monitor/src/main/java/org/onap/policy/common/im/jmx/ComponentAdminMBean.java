/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
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

package org.onap.policy.common.im.jmx;

import org.onap.policy.common.im.IntegrityMonitorException;

/**
 * Provides operations to test health, lock and unlock components.
 */
public interface ComponentAdminMBean {
	/**
	 * Test health of component.
	 * 
	 * @throws IntegrityMonitorException
	 *            if the component fails the health check
	 */
	void test() throws IntegrityMonitorException;

	/**
	 * Administratively lock component.
	 * 
	 * @throws IntegrityMonitorException
	 *            if the component lock fails
	 */
	void lock() throws IntegrityMonitorException;
	
	/**
	 * Administratively unlock component.
	 * 
	 * @throws IntegrityMonitorException
	 *            if the component unlock fails
	 */
	void unlock() throws IntegrityMonitorException;
}
