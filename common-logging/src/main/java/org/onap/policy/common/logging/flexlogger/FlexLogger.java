/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.logging.flexlogger;

import static org.onap.policy.common.logging.flexlogger.DisplayUtils.displayMessage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.policy.common.logging.eelf.PolicyLogger;
import org.onap.policy.common.logging.flexlogger.PropertyUtil.Listener;

/**
 * FlexLogger acts as factory to generate instances of Logger based on logger type.
 */
public class FlexLogger extends SecurityManager {

    private static final String GET_LOGGER_PREFIX = "FlexLogger:getLogger : loggerType = ";
    private static LoggerType loggerType = LoggerType.EELF;
    private static ConcurrentHashMap<String, EelfLogger> eelfLoggerMap = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, SystemOutLogger> systemOutMap = new ConcurrentHashMap<>();

    // --- init logger first
    static {
        loggerType = initlogger();
    }

    /**
     * Returns an instance of Logger.
     *
     * @param clazz the class
     */
    public static Logger getLogger(Class<?> clazz) {
        Logger logger = null;
        displayMessage(GET_LOGGER_PREFIX + loggerType);
        switch (loggerType) {

            case EELF:
                logger = getEelfLogger(clazz, false);
                break;
            case SYSTEMOUT:
            default:
                logger = getSystemOutLogger();
                break;
        }

        return logger;

    }

    /**
     * Returns an instance of Logger.
     */
    public static Logger getLogger() {
        Logger logger = null;
        displayMessage(GET_LOGGER_PREFIX + loggerType);
        switch (loggerType) {

            case EELF:
                logger = getEelfLogger(null, false);
                break;
            case SYSTEMOUT:
            default:
                logger = getSystemOutLogger();
                break;
        }

        return logger;

    }

    /**
     * Returns an instance of Logger.
     *
     * @param clazz the class
     * @param isNewTransaction is a new transaction
     */
    public static Logger getLogger(Class<?> clazz, boolean isNewTransaction) {
        Logger logger = null;
        displayMessage(GET_LOGGER_PREFIX + loggerType);
        switch (loggerType) {

            case EELF:
                logger = getEelfLogger(clazz, isNewTransaction);
                break;
            case SYSTEMOUT:
            default:
                logger = getSystemOutLogger();
                break;
        }

        return logger;

    }

    /**
     * Returns an instance of Logger.
     *
     * @param isNewTransaction is a new transaction
     */
    public static Logger getLogger(boolean isNewTransaction) {
        Logger logger = null;
        displayMessage(GET_LOGGER_PREFIX + loggerType);
        switch (loggerType) {

            case EELF:
                logger = getEelfLogger(null, isNewTransaction);
                break;
            case SYSTEMOUT:
            default:
                logger = getSystemOutLogger();
                break;
        }

        return logger;
    }

    /**
     * Returns the calling class name.
     */
    public String getClassName() {
        displayMessage("getClassContext()[3].getName() " + getClassContext()[3].getName());
        return getClassContext()[3].getName();
    }

    /**
     * Returns an instance of EelfLogger.
     *
     * @param clazz the class
     * @param isNewTransaction is a new transaction
     */
    private static EelfLogger getEelfLogger(Class<?> clazz, boolean isNewTransaction) {

        String className;
        EelfLogger logger;
        if (clazz != null) {
            className = clazz.getName();
        } else {
            className = new FlexLogger().getClassName();
        }

        logger = eelfLoggerMap.computeIfAbsent(className, key -> new EelfLogger(clazz, isNewTransaction));

        if (isNewTransaction) {
            String transId = PolicyLogger.postMdcInfoForEvent(null);
            logger.setTransId(transId);
        }

        displayMessage("eelfLoggerMap size : " + eelfLoggerMap.size() + " class name: " + className);
        return logger;
    }

    /**
     * Returns an instance of SystemOutLogger.
     */
    private static SystemOutLogger getSystemOutLogger() {

        String className = new FlexLogger().getClassName();

        return systemOutMap.computeIfAbsent(className, SystemOutLogger::new);
    }

    /**
     * loads the logger properties.
     */
    private static LoggerType initlogger() {
        LoggerType loggerType = LoggerType.EELF;
        Properties properties = null;

        try {
            properties = PropertyUtil.getProperties("config/policyLogger.properties");
            displayMessage("FlexLogger:properties => " + properties);

            if (properties != null) {
                String overrideLogbackLevel = properties.getProperty("override.logback.level.setup");
                displayMessage("FlexLogger:overrideLogbackLevel => " + overrideLogbackLevel);
                String loggerTypeString = properties.getProperty("logger.type");
                if ("EELF".equalsIgnoreCase(loggerTypeString) && "TRUE".equalsIgnoreCase(overrideLogbackLevel)) {
                    displayMessage("FlexLogger: start listener.");
                    properties = PropertyUtil.getProperties("config/policyLogger.properties",
                            new PropertiesCallBack("FlexLogger-CallBack"));
                }
            }
        } catch (IOException e1) {
            displayMessage("initlogger" + e1);
        } finally {
            // OK to pass no properties (null)
            loggerType = PolicyLogger.init(properties);
        }

        return loggerType;
    }

    /**
     * PropertiesCallBack is listening any updates on the policyLogger.properties
     */
    public static class PropertiesCallBack implements Listener {
        String name;

        public PropertiesCallBack(String name) {
            this.name = name;
        }

        /**
         * This method will be called automatically if he policyLogger.properties got updated
         */
        @Override
        public void propertiesChanged(Properties properties, Set<String> changedKeys) {

            String debugLevel = properties.getProperty("debugLogger.level");
            String metricsLevel = properties.getProperty("metricsLogger.level");
            String auditLevel = properties.getProperty("audit.level");
            String errorLevel = properties.getProperty("error.level");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
            Instant startTime = Instant.now();
            String formatedTime = sdf.format(Date.from(startTime));
            displayMessage("FlexLogger.propertiesChanged : called at time : " + formatedTime);
            displayMessage("FlexLogger.propertiesChanged : debugLevel : " + debugLevel);

            if (changedKeys != null) {

                if (changedKeys.contains("debugLogger.level")) {
                    PolicyLogger.setDebugLevel(debugLevel);
                }

                if (changedKeys.contains("metricsLogger.level")) {
                    PolicyLogger.setMetricsLevel(metricsLevel);
                }

                if (changedKeys.contains("error.level")) {
                    PolicyLogger.setErrorLevel(errorLevel);
                }

                if (changedKeys.contains("audit.level")) {
                    PolicyLogger.setAuditLevel(auditLevel);
                }
            }
        }
    }
}
