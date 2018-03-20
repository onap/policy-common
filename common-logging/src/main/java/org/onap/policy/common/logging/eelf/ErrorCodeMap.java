/*
 * ============LICENSE_START=======================================================
 * ONAP-Logging
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

package org.onap.policy.common.logging.eelf;

import java.util.EnumMap;

/**
 * ErrorCodeMap contains a HashMap of ErrorCodeInfo (error code and error description).
 */
public class ErrorCodeMap {

    private static final EnumMap<MessageCodes, ErrorCodeInfo> hm = new EnumMap<>(MessageCodes.class);

    private static final String ERROR_PERMISSIONS = "POLICY-100E";
    private static final String ERROR_PERMISSIONS_DESCRIPTION =
            "This is a Permissions Error. Please check the error message for detail information";

    private static final String ERROR_SCHEMA_INVALID = "POLICY-400E";
    private static final String ERROR_SCHEMA_INVALID_DESCRIPTION =
            "This is an Invalid Schema Error. Please check the error message for detail information";

    private static final String UPDATE_ERROR = "POLICY-502E";
    private static final String UPDATE_ERROR_DESCRIPTION =
            "This is an updating error. Please check the error message for  detail information";

    private static final String EXCEPTION_ERROR_CODE = "POLICY-503E";
    private static final String EXCEPTION_ERROR_DESCRIPTION =
            "This is an exception error message during the process. Please check the error message for detail "
                    + "information";

    private static final String MISS_PROPERTY_ERROR = "POLICY-504E";
    private static final String MISS_PROPERTY_ERROR_DESCRIPTION =
            "This is an error of missing properties. Please check the error message for  detail information";

    private static final String GENERAL_ERROR_CODE = "POLICY-515E";
    private static final String GENERAL_ERROR_DESCRIPTION =
            "This is a general error message during the process. Please check the error message for detail information";

    private static final String ERROR_SYSTEM_ERROR = "POLICY-516E";
    private static final String ERROR_SYSTEM_ERROR_DESCRIPTION =
            "This is a System Error. Please check the error message for detail information";

    private static final String ERROR_DATA_ISSUE = "POLICY-517E";
    private static final String ERROR_DATA_ISSUE_DESCRIPTION =
            "This is a Data Issue Error. Please check the error message for detail information";

    private static final String ERROR_PROCESS_FLOW = "POLICY-518E";
    private static final String ERROR_PROCESS_FLOW_DESCRIPTION =
            "This is a Process Flow Error. Please check the error message for detail information";

    private static final String ERROR_UNKNOWN = "POLICY-519E";
    private static final String ERROR_UNKNOWN_DESCRIPTION =
            "This is an Unknown Error. Please check the error message for detail information";

    private static final String ERROR_AUDIT = "POLICY-520E";
    private static final String ERROR_AUDIT_DESCRIPTION =
            "This is an audit Error. Please check the error message for detail information";

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

    private ErrorCodeMap() {}

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
