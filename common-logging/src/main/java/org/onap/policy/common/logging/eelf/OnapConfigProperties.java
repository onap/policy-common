/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
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

package org.onap.policy.common.logging.eelf;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OnapConfigProperties {

    /**
     * The Date-time of the start of a transaction.
     */
    public static final String BEGIN_TIME_STAMP = "BeginTimestamp";

    /**
     * The Date-time of the end of transaction.
     */
    public static final String END_TIME_STAMP = "EndTimestamp";

    /**
     * Externally advertised API invoked by clients of this component.
     */
    public static final String SERVICE_NAME = "ServiceName";

    /**
     * Client or user invoking the API.
     */
    public static final String PARTNER_NAME = "PartnerName";

    public static final String TARGET_ENTITY = "TargetEntity";

    public static final String TARGET_SERVICE_NAME = "TargetServiceName";

    /**
     * High level success or failure (COMPLETE or ERROR).
     */
    public static final String STATUS_CODE = "StatusCode";

    /**
     * Application specific response code.
     */
    public static final String RESPONSE_CODE = "ResponseCode";

    /**
     * Human-readable description of the application specific response code.
     */
    public static final String RESPONSE_DESCRIPTION = "ResponseDescription";

    /**
     * Externally advertised API invoked by clients of this component.
     */
    public static final String ELAPSED_TIME = "ElapsedTime";

    /**
     * High level failure (ERROR).
     */
    public static final String ERROR_CATEGORY = "ErrorCategory";

    public static final String ERROR_CODE = "ErrorCode";

    public static final String ERROR_DESCRIPTION = "ErrorDescription";

    public static final String CLASS_NAME = "ClassName";

    public static final String SERVER_NAME = "ServerName";

    public static final String INVOCATION_ID = "InvocationID";
}
