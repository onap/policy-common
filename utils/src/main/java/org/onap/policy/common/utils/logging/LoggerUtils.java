/*-
 * ============LICENSE_START=======================================================
 * ONAP POLICY
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

package org.onap.policy.common.utils.logging;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


public class LoggerUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerUtils.class);

    /**
     * ROOT logger.
     */
    public static final String ROOT_LOGGER = "ROOT";
    /**
     * Metric logger.
     */
    public static final String METRIC_LOG_MARKER_NAME = "metric";
    /**
     * Audit Log Marker Name.
     */
    public static final String AUDIT_LOG_MARKER_NAME = "audit";
    /**
     * Security Log Marker Name.
     */
    public static final String SECURITY_LOG_MARKER_NAME = "security";
    /**
     * Transaction Log Marker Name.
     */
    public static final String TRANSACTION_LOG_MARKER_NAME = "transaction";
    /**
     * Marks a logging record for metric.
     */
    public static final Marker METRIC_LOG_MARKER = MarkerFactory.getMarker(METRIC_LOG_MARKER_NAME);
    /**
     * Marks a logging record for security.
     */
    public static final Marker SECURITY_LOG_MARKER = MarkerFactory.getMarker(SECURITY_LOG_MARKER_NAME);
    /**
     * Marks a logging record for audit.
     */
    public static final Marker AUDIT_LOG_MARKER = MarkerFactory.getMarker(AUDIT_LOG_MARKER_NAME);
    /**
     * Marks a logging record as an end-to-end transaction.
     */
    public static final Marker TRANSACTION_LOG_MARKER = MarkerFactory.getMarker(TRANSACTION_LOG_MARKER_NAME);

    /**
     * Logger delegate.
     */
    private final Logger mlogger;

    /**
     * Constructor.
     */
    public LoggerUtils(final Logger loggerP) {
        this.mlogger = checkNotNull(loggerP);
    }

    /**
     * Set the log level of a logger.
     *
     * @param loggerName logger name
     * @param loggerLevel logger level
     */
    public static String setLevel(String loggerName, String loggerLevel) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext)) {
            throw new IllegalStateException("The SLF4J logger factory is not configured for logback");
        }

        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final var logger = context.getLogger(loggerName);
        if (logger == null) {
            throw new IllegalArgumentException("no logger " + loggerName);
        }

        LOGGER.warn("setting {} logger to level {}", loggerName, loggerLevel);

        // use the current log level if the string provided cannot be converted to a valid Level.

        // NOSONAR: this method is currently used by the telemetry api (which should be authenticated).
        // It is no more or no less dangerous than an admin changing the logback level on the fly.
        // This is a controlled admin function that should not cause any risks when the system
        // is configured properly.
        logger.setLevel(ch.qos.logback.classic.Level.toLevel(loggerLevel, logger.getLevel()));  // NOSONAR

        return logger.getLevel().toString();
    }

    /**
     * Dependency-free nullcheck.
     *
     * @param in  to be checked
     * @param <T> argument (and return) type
     * @return input arg
     */
    private static <T> T checkNotNull(final T in) {
        if (in == null) {
            throw new NullPointerException();
        }
        return in;
    }
}
