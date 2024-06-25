/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2020 AT&T.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.onap.policy.common.logging.eelf.ErrorCodeMap.ErrorCodeInfo;

class ErrorCodeMapTest {

    @Test
    void testGetErrorCodeInfo() {
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.EXCEPTION_ERROR));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.GENERAL_ERROR));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.MISS_PROPERTY_ERROR));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.UPDATE_ERROR));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_SYSTEM_ERROR));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_DATA_ISSUE));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_PERMISSIONS));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_DATA_ISSUE));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_PROCESS_FLOW));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_SCHEMA_INVALID));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_UNKNOWN));
        assertNotNull(ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_AUDIT));
    }

    @Test
    void testErrorCodeInfoGetErrorCode() {
        ErrorCodeInfo errorCodeInfo = ErrorCodeMap.getErrorCodeInfo(MessageCodes.EXCEPTION_ERROR);
        assertEquals("500", errorCodeInfo.getErrorCode());
    }

    @Test
    void testErrorCodeInfoGetErrorDesc() {
        ErrorCodeInfo errorCodeInfo = ErrorCodeMap.getErrorCodeInfo(MessageCodes.EXCEPTION_ERROR);
        assertEquals("This is an exception error message during the process. Please check the error message for detail "
                + "information", errorCodeInfo.getErrorDesc());
    }

}
