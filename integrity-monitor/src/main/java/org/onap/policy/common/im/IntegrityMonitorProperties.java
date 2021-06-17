/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2018, 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.im;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntegrityMonitorProperties {

    public static final String DB_DRIVER = "javax.persistence.jdbc.driver";
    public static final String DB_URL = "javax.persistence.jdbc.url";
    public static final String DB_USER = "javax.persistence.jdbc.user";
    public static final String DB_PWD = "javax.persistence.jdbc.password"; //NOSONAR

    // intervals specified are in seconds
    public static final int DEFAULT_MONITOR_INTERVAL = 30;
    public static final int DEFAULT_FAILED_COUNTER_THRESHOLD = 3;
    public static final int DEFAULT_TEST_INTERVAL = 10;
    public static final int DEFAULT_WRITE_FPC_INTERVAL = 5;
    public static final int DEFAULT_MAX_FPC_UPDATE_INTERVAL = 120;
    public static final int DEFAULT_CHECK_DEPENDENCY_INTERVAL = 10;

    public static final String FP_MONITOR_INTERVAL = "fp_monitor_interval";
    public static final String FAILED_COUNTER_THRESHOLD = "failed_counter_threshold";
    public static final String TEST_TRANS_INTERVAL = "test_trans_interval";
    public static final String WRITE_FPC_INTERVAL = "write_fpc_interval";
    public static final String CHECK_DEPENDENCY_INTERVAL = "check_dependency_interval";

    public static final String DEPENDENCY_GROUPS = "dependency_groups";
    public static final String SITE_NAME = "site_name";
    public static final String NODE_TYPE = "node_type";

    public static final String TEST_VIA_JMX = "test_via_jmx";
    public static final String JMX_FQDN = "jmx_fqdn";
    public static final String MAX_FPC_UPDATE_INTERVAL = "max_fpc_update_interval";
    public static final String STATE_AUDIT_INTERVAL_MS = "state_audit_interval_ms";
    public static final String REFRESH_STATE_AUDIT_INTERVAL_MS = "refresh_state_audit_interval_ms";

    // AllSeemsWell types
    public static final Boolean ALLNOTWELL = Boolean.FALSE;
    public static final Boolean ALLSEEMSWELL = Boolean.TRUE;
}
