/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.ia;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.slf4j.MDC;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * Test the default logging pattern.
 *
 */
public class DefaultLoggingPatternTest {
    // Logger for this class
    private static final XLogger LOGGER = XLoggerFactory.getXLogger(DefaultLoggingPatternTest.class);

    @Test
    public void testDefaultLoggingPattern() throws IOException {
        MDC.put("requestId", "TheRequestId");
        MDC.put("serviceInstanceId", "TheServiceInstanceId");
        MDC.put("serverName", "TheServerName");
        MDC.put("serviceName", "TheServiceName");
        MDC.put("instanceUuid", "TheInstanceUuid");
        MDC.put("severity", "TheSeverity");
        MDC.put("serverIpAddress", "TheServerIpAddress");
        MDC.put("server", "TheServer");
        MDC.put("clientIpAddress", "TheClientIpAddress");

        LOGGER.info("This is a test logging string");

        // Jump past the date, and the actual and expected logged strings should be the same
        String actualLoggedString =
                TextFileUtils.getTextFileAsString("testingLogs/common-modules/integrity-audit/logging-pattern-test.log")
                        .substring(23);
        String expectedLoggedString =
                TextFileUtils.getTextFileAsString("src/test/resources/logging-pattern-test-expected.log").substring(23);

        assertEquals(expectedLoggedString, actualLoggedString);
    }

    /**
     * Delete logging file after test.
     */
    @After
    public void deleteLogFile() {
        File logFile = new File("testingLogs/common-modules/integrity-audit/logging-pattern-test.log");

        if (logFile.exists()) {
            logFile.delete();
        }
    }
}
