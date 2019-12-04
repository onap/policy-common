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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Test;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * Test the default logging pattern.
 *
 */
public class DefaultLoggingPatternTest {
    // XLogger for this class
    private static final XLogger XLOGGER = XLoggerFactory.getXLogger(DefaultLoggingPatternTest.class);

    // Logger for this class
    private static final Logger LOGGER = XLoggerFactory.getXLogger(DefaultLoggingPatternTest.class);

    /**
     * Delete logging file after test.
     */
    @AfterClass
    public static void deleteLogFile() {
        File logFile = new File("testingLogs/common-modules/integrity-audit/logging-pattern-test.log");

        if (logFile.exists()) {
            logFile.delete();
        }
    }

    /**
     * Test XLogger output.
     *
     * @throws IOException on errors
     */
    @Test
    public void testDefaultLoggingPatternXLogger() throws IOException {
        testDefaultLoggingPattern(XLOGGER, "xlogger");
    }

    /**
     * Test Logger output.
     *
     * @throws IOException on errors
     */
    @Test
    public void testDefaultLoggingPatternLogger() throws IOException {
        testDefaultLoggingPattern(LOGGER, "logger");
    }

    /**
     * Test Logger output.
     *
     * @throws IOException on errors
     */
    public void testDefaultLoggingPattern(final Logger logger, final String loggerString) throws IOException {
        MDC.put("requestId", "TheRequestId");
        MDC.put("serviceInstanceId", "TheServiceInstanceId");
        MDC.put("serverName", "TheServerName");
        MDC.put("serviceName", "TheServiceName");
        MDC.put("instanceUuid", "TheInstanceUuid");
        MDC.put("severity", "TheSeverity");
        MDC.put("serverIpAddress", "TheServerIpAddress");
        MDC.put("server", "TheServer");
        MDC.put("clientIpAddress", "TheClientIpAddress");

        logger.info("This is a test logging string for " + loggerString);

        // Jump past the date, and the actual and expected logged strings should be the same
        String actualLoggedString =
                TextFileUtils.getTextFileAsString("testingLogs/common-modules/integrity-audit/logging-pattern-test.log")
                        .substring(23);
        String expectedLoggedString = TextFileUtils
                .getTextFileAsString("src/test/resources/" + loggerString + "-test.expectedlog").substring(23);

        assertThat(actualLoggedString).contains(expectedLoggedString);
    }
}
