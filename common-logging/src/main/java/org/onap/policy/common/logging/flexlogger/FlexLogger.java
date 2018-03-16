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

package org.onap.policy.common.logging.flexlogger;

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
    private static ConcurrentHashMap<String, Logger4J> logger4JMap = new ConcurrentHashMap<>();
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
        System.out.println(GET_LOGGER_PREFIX + loggerType);
        switch (loggerType) {

            case EELF:
                logger = getEelfLogger(clazz, false);
                break;
            case LOG4J:
                logger = getLog4JLogger();
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
     * @param name the name of the logger
     */
    public static Logger getLogger(String name) {
        Logger logger = null;
        System.out.println(GET_LOGGER_PREFIX + loggerType);
        switch (loggerType) {

            case EELF:
                logger = getEelfLogger(null, false);
                break;
            case LOG4J:
                logger = getLog4JLogger(name);
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
        System.out.println(GET_LOGGER_PREFIX + loggerType);
        switch (loggerType) {

            case EELF:
                logger = getEelfLogger(clazz, isNewTransaction);
                break;
            case LOG4J:
                logger = getLog4JLogger();
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
     * @param name the name of the logger
     * @param isNewTransaction is a new transaction
     */
    public static Logger getLogger(String name, boolean isNewTransaction) {
        Logger logger = null;
        System.out.println(GET_LOGGER_PREFIX + loggerType);
        switch (loggerType) {

            case EELF:
                logger = getEelfLogger(null, isNewTransaction);
                break;
            case LOG4J:
                logger = getLog4JLogger(name);
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
        System.out.println("getClassContext()[3].getName() " + getClassContext()[3].getName());
        return getClassContext()[3].getName();
    }

    /**
     * Returns an instance of Logger4J.
     */
    private static Logger4J getLog4JLogger() {
        String className = new FlexLogger().getClassName();

        if (!logger4JMap.containsKey(className)) {
            // for 1610 release use the default debug.log for log4j
            Logger4J logger = new Logger4J("debugLogger", className);
            logger4JMap.put(className, logger);
        }

        return logger4JMap.get(className);
    }

    /**
     * Returns an instance of Logger4J.
     * 
     * @param name the name of the logger
     */
    private static Logger4J getLog4JLogger(String name) {
        String className = new FlexLogger().getClassName();

        if (!logger4JMap.containsKey(className)) {
            Logger4J logger = new Logger4J(name, className);
            logger4JMap.put(className, logger);
        }

        return logger4JMap.get(className);
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

        if (!eelfLoggerMap.containsKey(className)) {
            logger = new EelfLogger(clazz, isNewTransaction);
            eelfLoggerMap.put(className, logger);
        } else {
            logger = eelfLoggerMap.get(className);
            if (logger == null) {
                logger = new EelfLogger(clazz, isNewTransaction);
                eelfLoggerMap.put(className, logger);
            }
            // installl already created but it is new transaction
            if (isNewTransaction) {
                String transId = PolicyLogger.postMDCInfoForEvent(null);
                logger.setTransId(transId);
            }
        }
        System.out.println("eelfLoggerMap size : " + eelfLoggerMap.size() + " class name: " + className);
        return logger;
    }

    /**
     * Returns an instance of SystemOutLogger.
     */
    private static SystemOutLogger getSystemOutLogger() {

        String className = new FlexLogger().getClassName();

        if (!systemOutMap.containsKey(className)) {
            SystemOutLogger logger = new SystemOutLogger(className);
            systemOutMap.put(className, logger);
        }

        return systemOutMap.get(className);
    }

    /**
     * loads the logger properties.
     */
    private static LoggerType initlogger() {
        LoggerType loggerType = LoggerType.EELF;
        String overrideLogbackLevel = "FALSE";
        String loggerTypeString = "";
        Properties properties = null;

        try {
            properties = PropertyUtil.getProperties("config/policyLogger.properties");
            System.out.println("FlexLogger:properties => " + properties);

            if (properties != null) {
                overrideLogbackLevel = properties.getProperty("override.logback.level.setup");
                System.out.println("FlexLogger:overrideLogbackLevel => " + overrideLogbackLevel);
                loggerTypeString = properties.getProperty("logger.type");
                if (loggerTypeString != null) {
                    if ("EELF".equalsIgnoreCase(loggerTypeString)) {
                        loggerType = LoggerType.EELF;
                        if ("TRUE".equalsIgnoreCase(overrideLogbackLevel)) {
                            System.out.println("FlexLogger: start listener.");
                            properties = PropertyUtil.getProperties("config/policyLogger.properties",
                                    new PropertiesCallBack("FlexLogger-CallBack"));
                        }
                    } else if ("LOG4J".equalsIgnoreCase(loggerTypeString)) {
                        loggerType = LoggerType.LOG4J;
                    } else if ("SYSTEMOUT".equalsIgnoreCase(loggerTypeString)) {
                        loggerType = LoggerType.SYSTEMOUT;
                    }

                    System.out.println("FlexLogger.logger_Type value: " + loggerTypeString);
                }
            }
        } catch (IOException e1) {
            System.out.println("initlogger" + e1);
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
            System.out.println("FlexLogger.propertiesChanged : called at time : " + formatedTime);
            System.out.println("FlexLogger.propertiesChanged : debugLevel : " + debugLevel);

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
