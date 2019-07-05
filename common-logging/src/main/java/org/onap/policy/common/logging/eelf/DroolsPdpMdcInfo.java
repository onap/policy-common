/*
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DroolsPdpMdcInfo implements MdcInfo {

    private static ConcurrentHashMap<String, String> mdcMap = new ConcurrentHashMap<>();

    static {

        mdcMap.put(OnapConfigProperties.MDC_REMOTE_HOST, "");
        mdcMap.put(OnapConfigProperties.MDC_SERVICE_NAME, "Policy.droolsPdp");
        mdcMap.put(OnapConfigProperties.MDC_SERVICE_INSTANCE_ID, "Policy.droolsPdp.event");
        mdcMap.put(OnapConfigProperties.MDC_INSTANCE_UUID, "");
        mdcMap.put(OnapConfigProperties.MDC_ALERT_SEVERITY, "");
        mdcMap.put(OnapConfigProperties.PARTNER_NAME, "N/A");
        mdcMap.put(OnapConfigProperties.STATUS_CODE, "N/A");
        mdcMap.put(OnapConfigProperties.RESPONSE_CODE, "N/A");
        mdcMap.put(OnapConfigProperties.RESPONSE_DESCRIPTION, "N/A");
    }

    /**
     * Get the MMDC Info.
     *
     * @return the instance of ConcurrentHashMap.
     */
    @Override
    public ConcurrentMap<String, String> getMdcInfo() {
        return mdcMap;
    }
}
