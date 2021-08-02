/*-
* ============LICENSE_START=======================================================
* ONAP Policy
* ================================================================================
* Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.logging;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class LoggerUtilsTest {
    protected static final Logger logger = LoggerFactory.getLogger(LoggerUtilsTest.class);

    private LoggerUtils util;

    @Before
    public void setup() {
        this.util = new LoggerUtils(logger);
    }

    @Test
    public void testMarker() {
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