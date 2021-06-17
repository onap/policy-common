/*
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

import java.util.EnumMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * ErrorCodeMap contains a HashMap of ErrorCodeInfo (error code and error description).
 * Standard error code:
 * 100 – permission errors
 * 200 – availability errors
 * 300 – data errors
 * 400 – schema errors
 * 500 – business process errors
 * 900 – unknown errors
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorCodeMap {

    private static final EnumMap<MessageCodes, ErrorCodeInfo> hm = new EnumMap<>(MessageCodes.class);

    private static final String CHECK_ERROR_MESSAGE = " Please check the error message for detail information";

    private static final String ERROR_PERMISSIONS = "100";
    private static final String ERROR_PERMISSIONS_DESCRIPTION = "This is a Permissions Error." + CHECK_ERROR_MESSAGE;

    private static final String ERROR_SCHEMA_INVALID = "400";
    private static final String ERROR_SCHEMA_INVALID_DESCRIPTION = "This is an Invalid Schema Error."
        + CHECK_ERROR_MESSAGE;


    private static final String UPDATE_ERROR = "300";
    private static final String UPDATE_ERROR_DESCRIPTION = "This is an updating error." + CHECK_ERROR_MESSAGE;

    private static final String EXCEPTION_ERROR_CODE = "500";
    private static final String EXCEPTION_ERROR_DESCRIPTION = "This is an exception error message during the process."
        + CHECK_ERROR_MESSAGE;

    private static final String MISS_PROPERTY_ERROR = "300";
    private static final String MISS_PROPERTY_ERROR_DESCRIPTION = "This is an error of missing properties."
        + CHECK_ERROR_MESSAGE;

    private static final String GENERAL_ERROR_CODE = "500";
    private static final String GENERAL_ERROR_DESCRIPTION = "This is a general error message during the process."
        + CHECK_ERROR_MESSAGE;

    private static final String ERROR_SYSTEM_ERROR = "200";
    private static final String ERROR_SYSTEM_ERROR_DESCRIPTION = "This is a System Error." + CHECK_ERROR_MESSAGE;

    private static final String ERROR_DATA_ISSUE = "300";
    private static final String ERROR_DATA_ISSUE_DESCRIPTION = "This is a Data Issue Error." + CHECK_ERROR_MESSAGE;

    private static final String ERROR_PROCESS_FLOW = "500";
    private static final String ERROR_PROCESS_FLOW_DESCRIPTION = "This is a Process Flow Error." + CHECK_ERROR_MESSAGE;

    private static final String ERROR_UNKNOWN = "900";
    private static final String ERROR_UNKNOWN_DESCRIPTION = "This is an Unknown Error." + CHECK_ERROR_MESSAGE;

    private static final String ERROR_AUDIT = "300";
    private static final String ERROR_AUDIT_DESCRIPTION = "This is an audit Error." + CHECK_ERROR_MESSAGE;

    static {
        hm.put(MessageCodes.EXCEPTION_ERROR, new ErrorCodeInfo(EXCEPTION_ERROR_CODE, EXCEPTION_ERROR_DESCRIPTION));
        hm.put(MessageCodes.GENERAL_ERROR, new ErrorCodeInfo(GENERAL_ERROR_CODE, GENERAL_ERROR_DESCRIPTION));
        hm.put(MessageCodes.MISS_PROPERTY_ERROR,
            new ErrorCodeInfo(MISS_PROPERTY_ERROR, MISS_PROPERTY_ERROR_DESCRIPTION));
        hm.put(MessageCodes.UPDATE_ERROR, new ErrorCodeInfo(UPDATE_ERROR, UPDATE_ERROR_DESCRIPTION));
        hm.put(MessageCodes.ERROR_SYSTEM_ERROR, new ErrorCodeInfo(ERROR_SYSTEM_ERROR, ERROR_SYSTEM_ERROR_DESCRIPTION));
        hm.put(MessageCodes.ERROR_DATA_ISSUE, new ErrorCodeInfo(ERROR_DATA_ISSUE, ERROR_DATA_ISSUE_DESCRIPTION));
        hm.put(MessageCodes.ERROR_PERMISSIONS, new ErrorCodeInfo(ERROR_PERMISSIONS, ERROR_PERMISSIONS_DESCRIPTION));
        hm.put(MessageCodes.ERROR_DATA_ISSUE, new ErrorCodeInfo(ERROR_DATA_ISSUE, ERROR_DATA_ISSUE_DESCRIPTION));
        hm.put(MessageCodes.ERROR_PROCESS_FLOW, new ErrorCodeInfo(ERROR_PROCESS_FLOW, ERROR_PROCESS_FLOW_DESCRIPTION));
        hm.put(MessageCodes.ERROR_SCHEMA_INVALID,
            new ErrorCodeInfo(ERROR_SCHEMA_INVALID, ERROR_SCHEMA_INVALID_DESCRIPTION));
        hm.put(MessageCodes.ERROR_UNKNOWN, new ErrorCodeInfo(ERROR_UNKNOWN, ERROR_UNKNOWN_DESCRIPTION));
        hm.put(MessageCodes.ERROR_AUDIT, new ErrorCodeInfo(ERROR_AUDIT, ERROR_AUDIT_DESCRIPTION));
    }

    public static ErrorCodeInfo getErrorCodeInfo(MessageCodes messageCode) {
        return hm.get(messageCode);
    }

    static class ErrorCodeInfo {

        private String errorCode;
        private String errorDesc;

        public ErrorCodeInfo(String errorCode, String errorDesc) {
            this.errorCode = errorCode;
            this.errorDesc = errorDesc;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorDesc() {
            return errorDesc;
        }

    }

}
