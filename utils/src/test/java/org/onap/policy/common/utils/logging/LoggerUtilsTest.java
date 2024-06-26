/*-
* ============LICENSE_START=======================================================
* ONAP Policy
* ================================================================================
* Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
* Modifications Copyright (C) 2024 Nordix Foundation
* ================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END============================================
* ===================================================================
*
*/

package org.onap.policy.common.utils.logging;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class LoggerUtilsTest {
    protected static final Logger logger = LoggerFactory.getLogger(LoggerUtilsTest.class);

    @Test
    void testMarker() {
        assertTrue(logger.isInfoEnabled());
        logger.info("line 1");
        logger.info(LoggerUtils.METRIC_LOG_MARKER, "line 1 Metric");
        logger.info(LoggerUtils.AUDIT_LOG_MARKER, "line 1 Audit");
        logger.info(LoggerUtils.SECURITY_LOG_MARKER, "line 1 Security");
        logger.info(LoggerUtils.TRANSACTION_LOG_MARKER, "line 1 Transaction");
        LoggerUtils.setLevel(LoggerUtils.ROOT_LOGGER, "debug");
        logger.debug("line 2");
        logger.debug(LoggerUtils.METRIC_LOG_MARKER, "line 2 Metric");
        logger.debug(LoggerUtils.AUDIT_LOG_MARKER, "line 2 Audit");
        logger.debug(LoggerUtils.SECURITY_LOG_MARKER, "line 2 Security");
        logger.info(LoggerUtils.TRANSACTION_LOG_MARKER, "line 2 Transaction");
        assertTrue(logger.isDebugEnabled());
    }
}
