/*
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

package org.onap.policy.common.logging.eelf;

import static com.att.eelf.configuration.Configuration.MDC_ALERT_SEVERITY;
import static com.att.eelf.configuration.Configuration.MDC_INSTANCE_UUID;
import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static com.att.eelf.configuration.Configuration.MDC_REMOTE_HOST;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_FQDN;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_IP_ADDRESS;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_INSTANCE_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.BEGIN_TIME_STAMP;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.ELAPSED_TIME;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.END_TIME_STAMP;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.ERROR_CATEGORY;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.ERROR_CODE;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.ERROR_DESCRIPTION;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.INVOCATION_ID;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.PARTNER_NAME;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.RESPONSE_CODE;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.RESPONSE_DESCRIPTION;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.SERVER_NAME;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.STATUS_CODE;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.TARGET_ENTITY;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.TARGET_SERVICE_NAME;
import static org.onap.policy.common.logging.flexlogger.DisplayUtils.displayErrorMessage;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import com.att.eelf.configuration.EELFManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.logging.OnapLoggingUtils;
import org.onap.policy.common.logging.flexlogger.LoggerType;
import org.slf4j.MDC;

/**
 * PolicyLogger contains all the static methods for EELF logging.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PolicyLogger {

    private static EELFLogger errorLogger = EELFManager.getErrorLogger();

    private static EELFLogger metricsLogger = EELFManager.getMetricsLogger();

    private static EELFLogger auditLogger = EELFManager.getAuditLogger();

    private static EELFLogger debugLogger = EELFManager.getDebugLogger();

    private static final String POLICY_LOGGER = "PolicyLogger";

    private static EventTrackInfo eventTracker = new EventTrackInfo();

    private static String hostName = null;
    private static String hostAddress = null;
    private static String component = null;

    private static boolean isEventTrackerRunning = false;
    private static Timer timer = null;

    // Default:Timer initial delay and the delay between in milliseconds before task is to be
    // execute
    private static int timerDelayTime = 1000;

    // Default:Timer scheduleAtFixedRate period - time in milliseconds between successive task
    // executions
    private static int checkInterval = 30 * 1000;

    // Default:longest time an event info can be stored in the concurrentHashMap for logging - in
    // seconds
    static int expiredTime = 60 * 60 * 1000 * 24; // one day

    // Default:the size of the concurrentHashMap which stores the event starting time - when its
    // size reaches this limit, the Timer get executed
    private static int concurrentHashMapLimit = 5000;

    // Default:the size of the concurrentHashMap which stores the event starting time - when its
    // size drops to this point, stop the Timer
    private static int stopCheckPoint = 2500;

    @Getter
    @Setter
    private static boolean isOverrideLogbackLevel = false;

    private static Level debugLevel = Level.INFO;
    private static Level auditLevel = Level.INFO;
    private static Level metricsLevel = Level.INFO;
    private static Level errorLevel = Level.ERROR;
    private static String classNameProp = "ClassName";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS+00:00";
    private static final String COMPLETE_STATUS = "COMPLETE";
    private static final String ERROR_CATEGORY_VALUE = "ERROR";

    static {
        if (hostName == null || hostAddress == null) {
            try {
                hostName = InetAddress.getLocalHost().getHostName();
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                PolicyLogger.error(MessageCodes.EXCEPTION_ERROR, e, POLICY_LOGGER, "UnknownHostException");
            }
        }
    }

    public static synchronized Level getDebugLevel() {
        return debugLevel;
    }

    public static synchronized void setDebugLevel(Level level) {
        debugLevel = level;
    }

    /**
     * Sets Debug Level.
     */
    public static synchronized void setDebugLevel(String newDebugLevel) {

        if (isOverrideLogbackLevel) {
            PolicyLogger.debugLevel = Level.valueOf(newDebugLevel);
            debugLogger.setLevel(debugLevel);
        }

    }

    public static synchronized Level getAuditLevel() {
        return auditLevel;
    }

    public static synchronized void setAuditLevel(Level level) {
        auditLevel = level;
    }

    /**
     * Sets Audit OFF or ON.
     */
    public static synchronized void setAuditLevel(String newAuditLevel) {

        if (isOverrideLogbackLevel) {
            if ("OFF".equalsIgnoreCase(newAuditLevel)) {
                PolicyLogger.auditLevel = Level.OFF;
                auditLogger.setLevel(auditLevel);
            } else {
                // --- set default value
                auditLogger.setLevel(Level.INFO);
                PolicyLogger.auditLevel = Level.INFO;
            }
        }
    }

    public static synchronized Level getMetricsLevel() {
        return metricsLevel;
    }

    public static synchronized void setMetricsLevel(Level level) {
        metricsLevel = level;
    }

    /**
     * Sets Metrics OFF or ON.
     */
    public static synchronized void setMetricsLevel(String newMetricsLevel) {

        if (isOverrideLogbackLevel) {
            if ("OFF".equalsIgnoreCase(newMetricsLevel)) {
                PolicyLogger.metricsLevel = Level.OFF;
                metricsLogger.setLevel(metricsLevel);
            } else {
                // --- set default value
                metricsLogger.setLevel(Level.INFO);
                PolicyLogger.metricsLevel = Level.INFO;
            }
        }

    }

    public static synchronized Level getErrorLevel() {
        return errorLevel;
    }

    public static synchronized void setErrorLevel(Level level) {
        errorLevel = level;
    }

    /**
     * Sets Error OFF or ON.
     */
    public static synchronized void setErrorLevel(String newErrorLevel) {

        if (isOverrideLogbackLevel) {
            if ("OFF".equalsIgnoreCase(newErrorLevel)) {
                PolicyLogger.errorLevel = Level.OFF;
                errorLogger.setLevel(errorLevel);
            } else {
                // --- set default value
                errorLogger.setLevel(Level.ERROR);
                PolicyLogger.errorLevel = Level.ERROR;
            }
        }
    }

    public static synchronized String getClassname() {
        return classNameProp;
    }

    public static synchronized void setClassname(String name) {
        classNameProp = name;
    }

    /**
     * Populates MDC info.
     *
     * @param transId the transaction ID
     * @return String
     */
    public static String postMdcInfoForEvent(String transId) {
        MDC.clear();

        String transactionId = transId;

        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        if ("DROOLS".equalsIgnoreCase(component)) {
            MDC.put(TARGET_ENTITY, "POLICY");
            MDC.put(TARGET_SERVICE_NAME, "drools evaluate rule");
            return postMdcInfoForEvent(transactionId, new DroolsPdpMdcInfo());
        } else {
            // For Xacml
            MDC.put(TARGET_ENTITY, "POLICY");
            MDC.put(TARGET_SERVICE_NAME, "PE Process");
        }

        MDC.put(MDC_REMOTE_HOST, "");
        MDC.put(MDC_KEY_REQUEST_ID, transactionId);
        MDC.put(MDC_SERVICE_NAME, "Policy.xacmlPdp");
        MDC.put(MDC_SERVICE_INSTANCE_ID, "Policy.xacmlPdp.event");

        setMdcHostInfo();
        seTimeStamps();

        return transactionId;
    }

    /**
     * Populate MDC Info using the passed in mdcInfo.
     *
     * @param transId the transaction ID
     * @param mdcInfo the MDC info
     * @return String
     */
    private static String postMdcInfoForEvent(String transId, MdcInfo mdcInfo) {

        MDC.put(MDC_KEY_REQUEST_ID, transId);
        if (mdcInfo != null && mdcInfo.getMdcInfo() != null && !mdcInfo.getMdcInfo().isEmpty()) {

            ConcurrentMap<String, String> mdcMap = mdcInfo.getMdcInfo();
            Iterator<String> keyIterator = mdcMap.keySet().iterator();
            String key;

            while (keyIterator.hasNext()) {
                key = keyIterator.next();
                MDC.put(key, mdcMap.get(key));
            }
        }

        setMdcHostInfo();

        var startTime = Instant.now();
        var endTime = Instant.now();

        seTimeStamps(startTime, endTime);

        return transId;
    }

    /**
     * Sets transaction Id to MDC.
     *
     * @param eventObject event object
     */
    public static void postMdcInfoForEvent(Object eventObject) {
        postMdcInfoForEvent("" + eventObject);
    }

    private static void setMdcHostInfo() {
        try {
            MDC.put(MDC_SERVER_FQDN, hostName);
            MDC.put(MDC_SERVER_IP_ADDRESS, hostAddress);
        } catch (Exception e) {
            errorLogger.error(MessageCodes.EXCEPTION_ERROR, e, POLICY_LOGGER);
        }
    }

    /**
     * Set Timestamps for start, end and duration of logging a transaction.
     */
    private static void seTimeStamps() {
        MDC.put(MDC_INSTANCE_UUID, "");
        MDC.put(MDC_ALERT_SEVERITY, "");

        var startTime = Instant.now();
        var endTime = Instant.now();

        seTimeStamps(startTime, endTime);

        MDC.put(PARTNER_NAME, "N/A");

        MDC.put(STATUS_CODE, COMPLETE_STATUS);
        MDC.put(RESPONSE_CODE, "N/A");
        MDC.put(RESPONSE_DESCRIPTION, "N/A");

    }

    private static void seTimeStamps(Instant startTime, Instant endTime) {
        var sdf = new SimpleDateFormat(DATE_FORMAT);

        String formatedTime = sdf.format(Date.from(startTime));
        MDC.put(BEGIN_TIME_STAMP, formatedTime);

        // set default values for these required fields below, they can be overridden
        formatedTime = sdf.format(Date.from(endTime));
        MDC.put(END_TIME_STAMP, formatedTime);
        MDC.put(ELAPSED_TIME, Long.toString(Duration.between(startTime, endTime).toMillis()));
    }

    /**
     * Sets transaction Id to MDC.
     *
     * @param transId the transaction ID
     */
    public static void setTransId(String transId) {

        MDC.put(MDC_KEY_REQUEST_ID, transId);
    }

    /**
     * Returns current transaction Id used in MDC.
     *
     * @return transId
     */
    public static String getTransId() {

        return MDC.get(MDC_KEY_REQUEST_ID);
    }

    /**
     * Resets transaction Id in MDC for the rule triggered by this event.
     *
     * @param transId the transaction ID
     * @return String
     */
    public static String postMdcInfoForTriggeredRule(String transId) {

        String transactionId = transId;

        MDC.clear();

        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_REMOTE_HOST, "");
        MDC.put(MDC_KEY_REQUEST_ID, transactionId);
        MDC.put(MDC_SERVICE_NAME, "Policy.droolsPdp");
        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        setMdcHostInfo();
        MDC.put(MDC_INSTANCE_UUID, "");
        MDC.put(MDC_ALERT_SEVERITY, "");
        MDC.put(STATUS_CODE, COMPLETE_STATUS);

        return transactionId;

    }

    /**
     * Resets transaction Id in MDC for the rule triggered by this event.
     *
     * @param obj object
     */
    public static void postMdcUuidForTriggeredRule(Object obj) {

        postMdcInfoForTriggeredRule("" + obj);

    }

    // ************************************************************************************************
    /**
     * Records the Info event with String [] arguments.
     *
     * @param msg the message code
     * @param className the class name
     * @param arguments the messages
     */
    public static void info(MessageCodes msg, String className, String... arguments) {
        MDC.put(classNameProp, className);
        debugLogger.info(msg, arguments);
    }

    /**
     * Records only one String message.
     *
     * @param arg0 the message
     */
    public static void info(Object arg0) {
        MDC.put(classNameProp, "");
        debugLogger.info(MessageCodes.GENERAL_INFO, String.valueOf(arg0));
    }

    /**
     * Records a message with passed in message code, Throwable object, a list of string values.
     *
     * @param msg the message code
     * @param arg0 the throwable
     * @param arguments the messages
     */
    public static void info(MessageCodes msg, Throwable arg0, String... arguments) {
        MDC.put(classNameProp, "");
        String arguments2 = getNormalizedStackTrace(arg0, arguments);
        debugLogger.info(msg, arguments2);
    }

    /**
     * Records a message with passed in message code, class name, Throwable object, a list of string
     * values.
     *
     * @param msg the message code
     * @param className the class name
     * @param arg0 the throwable
     * @param arguments the messages
     */
    public static void info(MessageCodes msg, String className, Throwable arg0, String... arguments) {
        MDC.put(classNameProp, className);
        String arguments2 = getNormalizedStackTrace(arg0, arguments);
        debugLogger.info(msg, arguments2);
    }

    /**
     * Records a message with passed in message text and variable number of arguments.
     *
     * @param message class name if one argument, otherwise message text
     * @param arguments variable number of arguments
     */
    public static void info(String message, Object... arguments) {
        if (!debugLogger.isInfoEnabled()) {
            return;
        }
        if (arguments.length == 1 && !OnapLoggingUtils.isThrowable(arguments[0])) {
            MDC.put(classNameProp, message);
            debugLogger.info(MessageCodes.GENERAL_INFO,
                arguments[0] == null ? "" : arguments[0].toString());
            return;
        }
        if (arguments.length == 1 && OnapLoggingUtils.isThrowable(arguments[0])) {
            String arguments2 = getNormalizedStackTrace((Throwable) arguments[0], "");
            debugLogger.info(MessageCodes.GENERAL_INFO, message + arguments2);
            return;
        }

        MDC.put(classNameProp, "");
        debugLogger.info(message, arguments);
    }

    /**
     * Records only one String message.
     *
     * @param arg0 the message
     */
    public static void warn(Object arg0) {
        MDC.put(classNameProp, "");
        debugLogger.warn(MessageCodes.GENERAL_WARNING, "" + arg0);
    }

    /**
     * Records only one String message without its class name passed in.
     *
     * @param arg0 the message
     */
    public static void warn(String arg0) {
        MDC.put(classNameProp, "");
        debugLogger.warn(MessageCodes.GENERAL_WARNING, arg0);
    }

    /**
     * Records a message with passed in message code, class name and a list of string values.
     *
     * @param msg the message code
     * @param className the class name
     * @param arguments the messages
     */
    public static void warn(MessageCodes msg, String className, String... arguments) {
        MDC.put(classNameProp, className);
        debugLogger.warn(msg, arguments);
    }

    /**
     * Records a message with passed in message code, Throwable object, a list of string values.
     *
     * @param msg the message code
     * @param arg0 the throwable
     * @param arguments the messages
     */
    public static void warn(MessageCodes msg, Throwable arg0, String... arguments) {
        MDC.put(classNameProp, "");
        String arguments2 = getNormalizedStackTrace(arg0, arguments);
        debugLogger.warn(msg, arguments2);
    }

    /**
     * Records a message with passed in message code, Throwable object, a list of string values.
     *
     * @param msg the message code
     * @param className the class name
     * @param arg0 the throwable
     * @param arguments the messages
     */
    public static void warn(MessageCodes msg, String className, Throwable arg0, String... arguments) {
        MDC.put(classNameProp, className);
        String arguments2 = getNormalizedStackTrace(arg0, arguments);
        debugLogger.warn(msg, arguments2);
    }

    /**
     * Records a message with passed in message text and variable number of arguments.
     *
     * @param message class name if one argument, otherwise message text
     * @param arguments variable number of arguments
     */
    public static void warn(String message, Object... arguments) {
        if (!debugLogger.isWarnEnabled()) {
            return;
        }
        if (arguments.length == 1 && !OnapLoggingUtils.isThrowable(arguments[0])) {
            MDC.put(classNameProp, message);
            debugLogger.warn(MessageCodes.GENERAL_INFO,
                arguments[0] == null ? "" : arguments[0].toString());
            return;
        }
        if (arguments.length == 1 && OnapLoggingUtils.isThrowable(arguments[0])) {
            String arguments2 = getNormalizedStackTrace((Throwable) arguments[0], "");
            debugLogger.warn(MessageCodes.GENERAL_INFO, message + arguments2);
            return;
        }
        MDC.put(classNameProp, "");
        debugLogger.warn(message, arguments);
    }

    /**
     * Records only one String message.
     *
     * @param arg0 the message
     */
    public static void error(String arg0) {
        MDC.put(classNameProp, "");
        setErrorCode(MessageCodes.GENERAL_ERROR);
        errorLogger.error(MessageCodes.GENERAL_ERROR, arg0);
    }

    /**
     * Records only one String message.
     *
     * @param arg0 the message
     */
    public static void error(Object arg0) {
        MDC.put(classNameProp, "");
        setErrorCode(MessageCodes.GENERAL_ERROR);
        errorLogger.error(MessageCodes.GENERAL_ERROR, "" + arg0);
    }

    /**
     * Records a message with passed in message code, Throwable object, a list of string values.
     *
     * @param msg the message code
     * @param arg0 the throwable
     * @param arguments the messages
     */
    public static void error(MessageCodes msg, Throwable arg0, String... arguments) {
        MDC.put(classNameProp, "");
        setErrorCode(msg);
        String arguments2 = getNormalizedStackTrace(arg0, arguments);
        errorLogger.error(msg, arguments2);
    }

    /**
     * Records a message with passed in message code, class name, Throwable object, a list of string
     * values.
     *
     * @param msg the message code
     * @param className the class name
     * @param arg0 the throwable
     * @param arguments the messages
     */
    public static void error(MessageCodes msg, String className, Throwable arg0, String... arguments) {
        MDC.put(classNameProp, className);
        setErrorCode(msg);
        String arguments2 = getNormalizedStackTrace(arg0, arguments);
        errorLogger.error(msg, arguments2);
    }

    /**
     * Records a message with passed in message code and a list of string values.
     *
     * @param msg the message code
     * @param arguments the messages
     */
    public static void error(MessageCodes msg, String... arguments) {
        MDC.put(classNameProp, "");
        setErrorCode(msg);
        errorLogger.error(msg, arguments);
    }

    /**
     * Records a message with passed in message text and variable number of arguments.
     *
     * @param message class name if one argument, otherwise message text
     * @param arguments variable number of arguments
     */
    public static void error(String message, Object... arguments) {
        if (!errorLogger.isErrorEnabled()) {
            return;
        }
        if (arguments.length == 1 && !OnapLoggingUtils.isThrowable(arguments[0])) {
            MDC.put(classNameProp, message);
            setErrorCode(MessageCodes.GENERAL_ERROR);
            errorLogger.error(MessageCodes.GENERAL_ERROR,
                arguments[0] == null ? "" : arguments[0].toString());
            return;
        }
        if (arguments.length == 1 && OnapLoggingUtils.isThrowable(arguments[0])) {
            String arguments2 = getNormalizedStackTrace((Throwable) arguments[0], "");
            errorLogger.error(MessageCodes.GENERAL_ERROR, message + arguments2);
            return;
        }
        MDC.put(classNameProp, "");
        setErrorCode(MessageCodes.GENERAL_ERROR);
        errorLogger.error(message, arguments);
    }

    /**
     * Records a message with passed in message code and a list of string values.
     *
     * @param msg the message code
     * @param arguments the messages
     */
    public static void debug(MessageCodes msg, String... arguments) {
        MDC.put(classNameProp, "");
        debugLogger.debug(msg, arguments);
    }

    /**
     * Records only one String message.
     *
     * @param arg0 the message
     */
    public static void debug(String arg0) {
        MDC.put(classNameProp, "");
        debugLogger.debug(arg0);
    }

    /**
     * Records only one String message.
     *
     * @param arg0 the message
     */
    public static void debug(Object arg0) {

        MDC.put(classNameProp, "");
        debugLogger.debug("{}", arg0);
    }

    /**
     * Records a message with passed in message code, hrowable object, a list of string values.
     *
     * @param msg the message code
     * @param arg0 the throwable
     * @param arguments the messages
     */
    public static void debug(MessageCodes msg, Throwable arg0, String... arguments) {
        MDC.put(classNameProp, "");
        String arguments2 = getNormalizedStackTrace(arg0, arguments);
        debugLogger.debug(msg, arguments2);
    }

    /**
     * Records a message with passed in message code, class name, Throwable object, a list of
     * string. values
     *
     * @param msg the message code
     * @param className the class name
     * @param arg0 the throwable
     * @param arguments the messages
     */
    public static void debug(MessageCodes msg, String className, Throwable arg0, String... arguments) {
        MDC.put(classNameProp, className);
        String arguments2 = getNormalizedStackTrace(arg0, arguments);
        debugLogger.debug(msg, arguments2);
    }

    /**
     * Records a message with passed in message text and variable number of arguments.
     *
     * @param message class name if one argument, otherwise message text
     * @param arguments variable number of arguments
     */
    public static void debug(String message, Object... arguments) {
        if (!debugLogger.isDebugEnabled()) {
            return;
        }
        if (arguments.length == 1 && !OnapLoggingUtils.isThrowable(arguments[0])) {
            MDC.put(classNameProp, message);
            debugLogger.debug(MessageCodes.GENERAL_INFO,
                arguments[0] == null ? "" : arguments[0].toString());
            return;
        }
        if (arguments.length == 1 && OnapLoggingUtils.isThrowable(arguments[0])) {
            String arguments2 = getNormalizedStackTrace((Throwable) arguments[0], "");
            debugLogger.debug(MessageCodes.GENERAL_INFO, message + arguments2);
            return;
        }
        MDC.put(classNameProp, "");
        debugLogger.debug(message, arguments);
    }

    /**
     * Records only one String message.
     *
     * @param arg0 the message
     */
    public static void audit(Object arg0) {
        MDC.put(INVOCATION_ID, MDC.get(MDC_KEY_REQUEST_ID));
        MDC.put(STATUS_CODE, COMPLETE_STATUS);
        MDC.put(RESPONSE_CODE, "0");
        MDC.put(classNameProp, "");
        auditLogger.info("{}", arg0);
    }

    /**
     * Records a message with passed in message text and variable number of arguments.
     *
     * @param message class name if one argument, otherwise message text
     * @param arguments variable number of arguments
     */
    public static void audit(String message, Object... arguments) {
        if (!auditLogger.isInfoEnabled()) {
            return;
        }
        MDC.put(INVOCATION_ID, postMdcInfoForEvent(null));
        MDC.put(STATUS_CODE, COMPLETE_STATUS);
        MDC.put(RESPONSE_CODE, "0");
        if (arguments.length == 1 && !OnapLoggingUtils.isThrowable(arguments[0])) {
            MDC.put(classNameProp, message);
            auditLogger.info("{}", arguments[0] == null ? "" : arguments[0].toString());
            return;
        }

        MDC.put(classNameProp, "");
        auditLogger.info(message, arguments);
    }

    /**
     * returns true for enabled, false for not enabled.
     */
    public static boolean isDebugEnabled() {

        return debugLogger.isDebugEnabled();
    }

    /**
     * returns true for enabled, false for not enabled.
     */
    public static boolean isErrorEnabled() {

        return errorLogger.isErrorEnabled();
    }

    /**
     * returns true for enabled, false for not enabled.
     */
    public static boolean isWarnEnabled() {

        return debugLogger.isWarnEnabled();
    }

    /**
     * returns true for enabled, false for not enabled.
     */
    public static boolean isInfoEnabled1() {

        return debugLogger.isInfoEnabled();
    }

    /**
     * returns true for enabled, false for not enabled.
     */
    public static boolean isAuditEnabled() {

        return debugLogger.isInfoEnabled();
    }

    /**
     * returns true for enabled, false for not enabled.
     */
    public static boolean isInfoEnabled() {

        return debugLogger.isInfoEnabled();
    }

    /**
     * Records only one String message with its class name.
     *
     * @param className the class name
     * @param arg0 the message
     */
    public static void trace(String className, String arg0) {
        MDC.put(classNameProp, className);
        debugLogger.trace(MessageCodes.GENERAL_INFO, arg0);
    }

    /**
     * Records only one String message.
     *
     * @param arg0 the message
     */
    public static void trace(Object arg0) {

        MDC.put(classNameProp, "");
        debugLogger.trace("{}", arg0);
    }

    /**
     * Records the starting time of the event with its request Id as the key.
     *
     * @param eventId the event ID
     */
    public static void recordAuditEventStart(String eventId) {

        MDC.put(STATUS_CODE, COMPLETE_STATUS);
        postMdcInfoForEvent(eventId);

        if (eventTracker == null) {
            eventTracker = new EventTrackInfo();
        }
        var event = new EventData();
        event.setRequestId(eventId);
        event.setStartTime(Instant.now());
        eventTracker.storeEventData(event);
        MDC.put(MDC_KEY_REQUEST_ID, eventId);
        debugLogger.info("CONCURRENTHASHMAP_LIMIT : {}", concurrentHashMapLimit);
        // --- Tracking the size of the concurrentHashMap, if it is above limit, keep EventTrack
        // Timer running
        int size = eventTracker.getEventInfo().size();

        debugLogger.info("EventInfo concurrentHashMap Size : {} on {}", size, new Date());
        debugLogger.info("isEventTrackerRunning : {}", isEventTrackerRunning);

        if (size >= concurrentHashMapLimit) {


            if (!isEventTrackerRunning) {

                startCleanUp();
                isEventTrackerRunning = true;
            }

        } else if (size <= stopCheckPoint && isEventTrackerRunning) {

            stopCleanUp();
        }
    }

    /**
     * Records the starting time of the event with its request Id as the key.
     *
     * @param eventId the event ID
     */
    public static void recordAuditEventStart(UUID eventId) {

        if (eventId == null) {
            return;
        }

        if (eventTracker == null) {
            eventTracker = new EventTrackInfo();
        }

        recordAuditEventStart(eventId.toString());

    }

    /**
     * Records the ending time of the event with its request Id as the key.
     *
     * @param eventId the event ID
     * @param rule the rule
     */
    public static void recordAuditEventEnd(String eventId, String rule) {

        if (eventTracker == null) {
            return;
        }
        if (eventId == null) {
            return;
        }

        creatAuditEventTrackingRecord(eventId, rule, "");

    }

    /**
     * Records the ending time of the event with its request Id as the key.
     *
     * @param eventId the event ID
     * @param rule the rule
     * @param policyVersion the policy version
     */
    public static void recordAuditEventEnd(String eventId, String rule, String policyVersion) {

        if (eventTracker == null) {
            return;
        }
        if (eventId == null) {
            return;
        }

        creatAuditEventTrackingRecord(eventId, rule, policyVersion);

    }

    /**
     * Records the ending time of the event with its request Id as the key.
     *
     * @param eventId the event ID
     * @param rule the rule
     * @param policyVersion the policy version
     */
    public static void recordAuditEventEnd(UUID eventId, String rule, String policyVersion) {

        if (eventId == null) {
            return;
        }

        recordAuditEventEnd(eventId.toString(), rule, policyVersion);

    }


    /**
     * Records the ending time of the event with its request Id as the key.
     *
     * @param eventId the event ID
     * @param rule the rule
     */
    public static void recordAuditEventEnd(UUID eventId, String rule) {

        if (eventId == null) {
            return;
        }

        recordAuditEventEnd(eventId.toString(), rule);

    }

    /**
     * Records the ending time of the event with its request Id as the key.
     *
     * @param eventId the event ID
     * @param rule the rule
     * @param policyVersion the policy version
     */
    public static void creatAuditEventTrackingRecord(String eventId, String rule, String policyVersion) {

        if (eventTracker == null) {
            return;
        }

        var event = eventTracker.getEventDataByRequestId(eventId);

        if (event != null) {
            Instant endTime = event.getEndTime();
            if (endTime == null) {
                endTime = Instant.now();
            }
            MDC.put(STATUS_CODE, COMPLETE_STATUS);
            recordAuditEventStartToEnd(eventId, rule, event.getStartTime(), endTime, policyVersion);
        }
    }

    /**
     * Records the ending time of the event with its request Id as the key.
     *
     * @param eventId the event ID
     * @param rule the rule
     */
    public static void creatAuditEventTrackingRecord(UUID eventId, String rule) {

        if (eventId == null) {
            return;
        }

        if (eventTracker == null) {
            return;
        }

        var event = eventTracker.getEventDataByRequestId(eventId.toString());

        if (event != null) {
            Instant endTime = event.getEndTime();
            if (endTime == null) {
                endTime = Instant.now();
            }

            recordAuditEventStartToEnd(eventId.toString(), rule, event.getStartTime(), endTime, "N/A");
        }
    }

    public static EventTrackInfo getEventTracker() {
        return eventTracker;
    }

    /**
     * Records the audit with an event starting and ending times.
     *
     * @param eventId the event ID
     * @param rule the rule
     * @param startTime the start time
     * @param endTime the end time
     * @param policyVersion the policy version
     */
    public static void recordAuditEventStartToEnd(String eventId, String rule, Instant startTime, Instant endTime,
            String policyVersion) {

        if (startTime == null || endTime == null) {
            return;
        }
        if (eventId != null && !eventId.isEmpty()) {
            MDC.put(MDC_KEY_REQUEST_ID, eventId);
        }

        seTimeStamps(startTime, endTime);

        MDC.put(RESPONSE_CODE, "N/A");
        MDC.put(RESPONSE_DESCRIPTION, "N/A");

        long ns = Duration.between(startTime, endTime).toMillis();

        auditLogger.info(MessageCodes.RULE_AUDIT_START_END_INFO, MDC.get(MDC_SERVICE_NAME), rule, startTime.toString(),
                endTime.toString(), Long.toString(ns), policyVersion);

        // --- remove the record from the concurrentHashMap
        if (eventTracker != null && eventTracker.getEventDataByRequestId(eventId) != null) {

            eventTracker.remove(eventId);
            debugLogger.info("eventTracker.remove({})", eventId);

        }
    }

    /**
     * Records the metrics with an event Id and log message.
     *
     * @param eventId the event ID
     * @param arg1 the message
     */
    public static void recordMetricEvent(String eventId, String arg1) {

        seTimeStamps();

        String serviceName = MDC.get(MDC_SERVICE_NAME);
        MDC.put(MDC_KEY_REQUEST_ID, eventId);
        MDC.put(STATUS_CODE, COMPLETE_STATUS);
        metricsLogger.info(MessageCodes.RULE_AUDIT_END_INFO, serviceName, arg1);

    }

    /**
     * Records the metrics with an event Id, class name and log message.
     *
     * @param eventId the event ID
     * @param className the class name
     * @param arg1 the message
     */
    public static void recordMetricEvent(String eventId, String className, String arg1) {

        seTimeStamps();

        MDC.put(classNameProp, className);
        String serviceName = MDC.get(MDC_SERVICE_NAME);
        MDC.put(MDC_KEY_REQUEST_ID, eventId);
        MDC.put(STATUS_CODE, COMPLETE_STATUS);
        metricsLogger.info(MessageCodes.RULE_AUDIT_END_INFO, serviceName, arg1);
    }

    /**
     * Records the metrics with an event Id and log message.
     *
     * @param eventId the event ID
     * @param arg1 the message
     */
    public static void recordMetricEvent(UUID eventId, String arg1) {

        if (eventId == null) {
            return;
        }
        String serviceName = MDC.get(MDC_SERVICE_NAME);
        MDC.put(MDC_KEY_REQUEST_ID, eventId.toString());
        metricsLogger.info(MessageCodes.RULE_AUDIT_END_INFO, serviceName, arg1);
    }

    /**
     * Records a String message for metrics logs.
     *
     * @param arg0 the message
     */
    public static void recordMetricEvent(String arg0) {
        seTimeStamps();
        String serviceName = MDC.get(MDC_SERVICE_NAME);
        metricsLogger.info(MessageCodes.RULE_METRICS_INFO, serviceName, arg0);
    }


    /**
     * Records the metrics event with a String message.
     *
     * @param arg0 the message
     */
    public static void metrics(String arg0) {
        seTimeStamps();
        MDC.put(INVOCATION_ID, MDC.get(MDC_KEY_REQUEST_ID));
        MDC.put(RESPONSE_CODE, "0");
        String serviceName = MDC.get(MDC_SERVICE_NAME);
        metricsLogger.info(MessageCodes.RULE_METRICS_INFO, serviceName, arg0);
    }

    /**
     * Records the metrics event with a String message.
     *
     * @param arg0 the message
     */
    public static void metrics(Object arg0) {
        seTimeStamps();
        MDC.put(INVOCATION_ID, MDC.get(MDC_KEY_REQUEST_ID));
        MDC.put(RESPONSE_CODE, "0");
        MDC.put(classNameProp, "");
        String serviceName = MDC.get(MDC_SERVICE_NAME);
        metricsLogger.info(MessageCodes.RULE_METRICS_INFO, serviceName, "" + arg0);
    }

    /**
     * Records a message with passed in message text and variable number of arguments.
     *
     * @param message class name if one argument, otherwise message text
     * @param arguments variable number of arguments
     */
    public static void metrics(String message, Object... arguments) {
        if (!metricsLogger.isInfoEnabled()) {
            return;
        }
        seTimeStamps();
        MDC.put(INVOCATION_ID, MDC.get(MDC_KEY_REQUEST_ID));
        MDC.put(RESPONSE_CODE, "0");
        if (arguments.length == 1 && !OnapLoggingUtils.isThrowable(arguments[0])) {
            MDC.put(classNameProp, message);
            String serviceName = MDC.get(MDC_SERVICE_NAME);
            metricsLogger.info(MessageCodes.RULE_METRICS_INFO, serviceName,
                arguments[0] == null ? "" : arguments[0].toString());
            return;
        }
        if (arguments.length == 1 && OnapLoggingUtils.isThrowable(arguments[0])) {
            String arguments2 = getNormalizedStackTrace((Throwable) arguments[0], "");
            metricsLogger.info(MessageCodes.RULE_METRICS_INFO, message + arguments2);
            return;
        }

        MDC.put(classNameProp, "");
        metricsLogger.info(message, arguments);
    }

    /**
     * Records the metrics event with a String message.
     *
     * @param arg0 the message
     */
    public static void metricsPrintln(String arg0) {
        MDC.clear();
        metricsLogger.info(arg0);
    }

    /**
     * Removes all the return lines from the printStackTrace.
     *
     * @param throwable the throwable
     * @param arguments the messages
     */
    private static String getNormalizedStackTrace(Throwable throwable, String... arguments) {
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String newStValue = sw.toString().replace('|', '!').replace("\n", " - ");
        int curSize = arguments == null ? 0 : arguments.length;
        var newArgument = new StringBuilder();
        for (var i = 0; i < curSize; i++) {
            newArgument.append(arguments[i]);
            newArgument.append(":");
        }
        newArgument.append(newStValue);
        return newArgument.toString();
    }

    /**
     * Starts the process of cleaning up the ConcurrentHashMap of EventData.
     */
    private static void startCleanUp() {

        if (!isEventTrackerRunning) {
            var ttrcker = new EventTrackInfoHandler();
            timer = new Timer(true);
            timer.scheduleAtFixedRate(ttrcker, timerDelayTime, checkInterval);
            debugLogger.info("EventTrackInfoHandler begins! : {}", new Date());
        } else {
            debugLogger.info("Timer is still running : {}", new Date());

        }
    }


    /**
     * Stops the process of cleaning up the ConcurrentHashMap of EventData.
     */
    private static void stopCleanUp() {

        if (isEventTrackerRunning && timer != null) {
            timer.cancel();
            timer.purge();
            debugLogger.info("Timer stopped: {}", new Date());
        } else {
            debugLogger.info("Timer was already stopped : {}", new Date());

        }
        isEventTrackerRunning = false;

    }

    /**
     * Loads all the attributes from policyLogger.properties file
     */
    public static LoggerType init(Properties properties) {

        var loggerProperties = getLoggerProperties(properties);

        // fetch and verify definitions of some properties
        try {
            setOverrideLogbackLevels(loggerProperties);

            setLoggerLevel(loggerProperties, "debugLogger.level", "INFO", PolicyLogger::setDebugLevel);

            // Only check if it is to turn on or off
            setLoggerOnOff(loggerProperties, "metricsLogger.level", PolicyLogger::setMetricsLevel);
            setLoggerOnOff(loggerProperties, "audit.level", PolicyLogger::setAuditLevel);
            setLoggerOnOff(loggerProperties, "error.level", PolicyLogger::setErrorLevel);

            isEventTrackerRunning = false;

            timerDelayTime = getIntProp(loggerProperties, "timer.delay.time", timerDelayTime);
            checkInterval = getIntProp(loggerProperties, "check.interval", checkInterval);
            expiredTime = getIntProp(loggerProperties, "event.expired.time", expiredTime);
            concurrentHashMapLimit = getIntProp(loggerProperties, "concurrentHashMap.limit", concurrentHashMapLimit);
            stopCheckPoint = getIntProp(loggerProperties, "stop.check.point", stopCheckPoint);

            component = loggerProperties.getProperty("policy.component", "DROOLS");
            debugLogger.info("component: {}", component);

            return detmLoggerType(loggerProperties);

        } catch (Exception e) {
            MDC.put(ERROR_CATEGORY, ERROR_CATEGORY_VALUE);

            if (ErrorCodeMap.getErrorCodeInfo(MessageCodes.GENERAL_ERROR) != null) {
                MDC.put(ERROR_CODE, ErrorCodeMap.getErrorCodeInfo(MessageCodes.GENERAL_ERROR).getErrorCode());
                MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.getErrorCodeInfo(MessageCodes.GENERAL_ERROR).getErrorDesc());

            }
            errorLogger.error("failed to get the policyLogger.properties, so use their default values", e);

            return LoggerType.EELF;
        }

    }

    private static int getIntProp(Properties properties, String propName, int defaultValue) {
        final var propValue = Integer.parseInt(properties.getProperty(propName, String.valueOf(defaultValue)));

        debugLogger.info("{} value: {}", propName, propValue);

        if (propValue > 0) {
            return propValue;

        } else {
            MDC.put(ERROR_CATEGORY, ERROR_CATEGORY_VALUE);
            if (ErrorCodeMap.getErrorCodeInfo(MessageCodes.GENERAL_ERROR) != null) {
                MDC.put(ERROR_CODE, ErrorCodeMap.getErrorCodeInfo(MessageCodes.GENERAL_ERROR).getErrorCode());
                MDC.put(ERROR_DESCRIPTION,
                        ErrorCodeMap.getErrorCodeInfo(MessageCodes.GENERAL_ERROR).getErrorDesc());

            }
            errorLogger.error("failed to get the {}, so use its default value: {}", propName, defaultValue);
            return defaultValue;
        }
    }

    private static Properties getLoggerProperties(Properties properties) {
        if (properties != null) {
            return properties;
        } else {
            displayErrorMessage("PolicyLogger cannot find its configuration - continue");
            return new Properties();
        }
    }

    private static void setLoggerLevel(Properties properties, String propName, String defaultValue,
                    Consumer<String> setter) {

        final String propValue = properties.getProperty(propName, defaultValue);

        if (!StringUtils.isBlank(propValue)) {
            debugLogger.info("{} level: {}", propName, propValue);
        }

        setter.accept(propValue);
    }

    private static void setLoggerOnOff(Properties properties, String propName, Consumer<String> setter) {
        final String propValue = properties.getProperty(propName, "ON");

        if (Level.OFF.toString().equalsIgnoreCase(propValue)) {
            debugLogger.info("{} level: {}", propName, propValue);
        }

        setter.accept(propValue);
    }

    private static void setOverrideLogbackLevels(Properties loggerProperties) {
        final String overrideLogbackLevel = loggerProperties.getProperty("override.logback.level.setup");

        if (!StringUtils.isBlank(overrideLogbackLevel)) {
            isOverrideLogbackLevel = "TRUE".equalsIgnoreCase(overrideLogbackLevel);
        }
    }

    private static LoggerType detmLoggerType(Properties loggerProperties) {
        final String loggerTypeProp = loggerProperties.getProperty("logger.type", LoggerType.EELF.toString());
        debugLogger.info("loggerType value: {}", loggerTypeProp);

        switch (loggerTypeProp.toUpperCase()) {
            case "EELF":
                return LoggerType.EELF;
            case "SYSTEMOUT":
                return LoggerType.SYSTEMOUT;

            default:
                return LoggerType.EELF;
        }
    }

    /**
     * Sets server information to MDC.
     */
    public static void setServerInfo(String serverHost, String serverPort) {
        MDC.put(SERVER_NAME, serverHost + ":" + serverPort);
    }

    /**
     * Sets error category, code and description.
     */
    private static void setErrorCode(MessageCodes errcode) {
        MDC.put(ERROR_CATEGORY, ERROR_CATEGORY_VALUE);
        if (ErrorCodeMap.getErrorCodeInfo(errcode) != null) {
            MDC.put(ERROR_CODE, ErrorCodeMap.getErrorCodeInfo(errcode).getErrorCode());
            MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.getErrorCodeInfo(errcode).getErrorDesc());
        } else {
            MDC.put(ERROR_CODE,
                    ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_UNKNOWN).getErrorCode());
            MDC.put(ERROR_DESCRIPTION,
                    ErrorCodeMap.getErrorCodeInfo(MessageCodes.ERROR_UNKNOWN).getErrorDesc());
        }
    }
}
