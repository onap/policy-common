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

public class IntegrityAuditProperties {

	public static final int DEFAULT_AUDIT_PERIOD_SECONDS = -1; // Audit does not run
	
	public static final String DB_DRIVER = "javax.persistence.jdbc.driver";
	public static final String DB_URL = "javax.persistence.jdbc.url";
	public static final String DB_USER = "javax.persistence.jdbc.user";
	public static final String DB_PWD = "javax.persistence.jdbc.password";
	public static final String AUDIT_PERIOD_SECONDS = "integrity_audit_period_seconds";
	public static final String AUDIT_PERIOD_MILLISECONDS = "integrity_audit_period_milliseconds";
	
	
	public static final String SITE_NAME = "site_name";
	public static final String NODE_TYPE = "node_type";
	
	public enum NodeTypeEnum {
		pdp_xacml,
		pdp_drools,
		pap,
		pap_admin,
		logparser,
		brms_gateway,
		astra_gateway,
		elk_server,
		pypdp

	}
	
	private IntegrityAuditProperties() {
		
	}
	
}
