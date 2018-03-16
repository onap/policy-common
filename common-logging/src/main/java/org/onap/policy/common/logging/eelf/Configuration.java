/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
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

package org.onap.policy.common.logging.eelf;


public interface Configuration extends com.att.eelf.configuration.Configuration {

    /**
     * The Date-time of the start of a transaction.
     */
    String BEGIN_TIME_STAMP = "BeginTimestamp";

    /**
     * The Date-time of the end of transaction.
     */
    String END_TIME_STAMP = "EndTimestamp";

    /**
     * Externally advertised API invoked by clients of this component.
     */
    String SERVICE_NAME = "ServiceName";

    /**
     * Client or user invoking the API.
     */
    String PARTNER_NAME = "PartnerName";

    String TARGET_ENTITY = "TargetEntity";

    String TARGET_SERVICE_NAME = "TargetServiceName";

    /**
     * High level success or failure (COMPLETE or ERROR).
     */
    String STATUS_CODE = "StatusCode";

    /**
     * Application specific response code.
     */
    String RESPONSE_CODE = "ResponseCode";

    /**
     * Human readable description of the application specific response code.
     */
    String RESPONSE_DESCRIPTION = "ResponseDescription";

    /**
     * Externally advertised API invoked by clients of this component.
     */
    String ELAPSED_TIME = "ElapsedTime";

    /**
     * High level failure (ERROR).
     */
    String ERROR_CATEGORY = "ErrorCategory";

    String ERROR_CODE = "ErrorCode";

    String ERROR_DESCRIPTION = "ErrorDescription";

    String CLASS_NAME = "ClassName";

    String SERVER_NAME = "ServerName";
}
