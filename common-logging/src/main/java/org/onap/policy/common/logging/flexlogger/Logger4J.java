/*
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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

import com.att.eelf.configuration.EELFLogger.Level;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;

/**
 * Logger4J implements all the methods of interface Logger by calling org.apache.log4j.Logger
 */
public class Logger4J implements org.onap.policy.common.logging.flexlogger.Logger, Serializable {

    private static final long serialVersionUID = 3183729429888828471L;
    private Logger log = null;
    private String methodName = "";
    private String className = "";
    private String transId = UUID.randomUUID().toString();

    /**
     * Constructor.
     *
     * @param clazz the class
     */
    public Logger4J(Class<?> clazz) {
        displayMessage("create instance of Logger4J");
        if (clazz != null) {
            log = Logger.getLogger(clazz);
            className = clazz.getName();
        }
    }

    /**
     * Constructor.
     *
     * @param name the name of the logger
     * @param className the name of the class
     */
    public Logger4J(String name, String className) {
        displayMessage("create instance of Logger4J");
        if (name != null) {
            log = Logger.getLogger(name);
        }
        this.className = className;
    }

    /**
     * Sets transaction Id.
     */
    @Override
    public void setTransId(String transId) {
        log.info(transId);
        this.transId = transId;
    }

    /**
     * Returns transaction Id.
     */
    @Override
    public String getTransId() {
        return transId;
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void debug(Object message) {
        if (isDebugEnabled()) {
            log.debug(transId + "|" + message);
        }
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void debug(Object message, Throwable throwable) {
        log.debug(message, throwable);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void debug(String message, Object... arguments) {
        if (arguments.length > 0 && isThrowable(arguments[arguments.length - 1])) {
            debug(formatMessage(message, arguments),
                (Throwable)arguments[arguments.length - 1]);
        } else {
            log.debug(formatMessage(message, arguments));
        }
    }

    /**
     * Records an error message.
     *
     * @param message the message
     */
    @Override
    public void error(Object message) {
        log.error(transId + "|" + className + "|" + message);
    }

    /**
     * Records an error message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void error(Object message, Throwable throwable) {
        log.error(message, throwable);
    }

    /**
     * Records an error message.
     *
     * @param msg the message code
     * @param throwable the throwable
     * @param arguments the messages
     */
    @Override
    public void error(MessageCodes msg, Throwable throwable, String... arguments) {
        log.error(transId + "|" + className + "|" + "MessageCodes :" + msg + Arrays.asList(arguments));

    }

    /**
     * Records an error message.
     *
     * @param msg the message code
     * @param arguments the messages
     */
    @Override
    public void error(MessageCodes msg, String... arguments) {
        log.error(transId + "|" + className + "|" + "MessageCode:" + msg + Arrays.asList(arguments));
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void error(String message, Object... arguments) {
        if (arguments.length > 0 && isThrowable(arguments[arguments.length - 1])) {
            error(formatMessage(message, arguments),
                (Throwable)arguments[arguments.length - 1]);
        } else {
            log.error(formatMessage(message, arguments));
        }
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void info(Object message) {
        log.info(transId + "|" + className + "|" + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void info(Object message, Throwable throwable) {
        log.info(message, throwable);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void info(String message, Object... arguments) {
        if (arguments.length > 0 && isThrowable(arguments[arguments.length - 1])) {
            info(formatMessage(message, arguments),
                (Throwable)arguments[arguments.length - 1]);
        } else {
            log.info(formatMessage(message, arguments));
        }
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void warn(Object message) {
        log.warn(transId + "|" + className + "|" + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void warn(Object message, Throwable throwable) {
        log.warn(message, throwable);
    }

    /**
     * Records a message.
     *
     * @param msg the message code
     * @param arguments the messages
     */
    @Override
    public void warn(MessageCodes msg, String... arguments) {
        log.warn(className + "|" + "MessageCodes:" + msg + Arrays.asList(arguments));
    }

    /**
     * Records a message.
     *
     * @param msg the message code
     * @param throwable the throwable
     * @param arguments the messages
     */
    @Override
    public void warn(MessageCodes msg, Throwable throwable, String... arguments) {
        log.warn(className + "|" + "MessageCodes:" + msg + Arrays.asList(arguments));
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void warn(String message, Object... arguments) {
        if (arguments.length > 0 && isThrowable(arguments[arguments.length - 1])) {
            warn(formatMessage(message, arguments),
                (Throwable)arguments[arguments.length - 1]);
        } else {
            log.warn(formatMessage(message, arguments));
        }
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void trace(Object message) {
        log.trace(transId + "|" + className + "|" + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void trace(Object message, Throwable throwable) {
        log.trace(message, throwable);
    }

    /**
     * Returns true for debug enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    /**
     * Returns true for error enabled, or false for not.
     *
     * @return boolean
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isErrorEnabled() {
        return log.isEnabledFor(Priority.ERROR);
    }

    /**
     * Returns true for info enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    /**
     * Returns true for warn enabled, or false for not.
     *
     * @return boolean
     */
    @SuppressWarnings("deprecation")
    @Override
    public boolean isWarnEnabled() {
        // return log4j value
        return log.isEnabledFor(Priority.WARN);
    }

    /**
     * Returns true for audit enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isAuditEnabled() {
        return (PolicyLogger.getAuditLevel() != Level.OFF);
    }

    /**
     * Returns true for metrics enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isMetricsEnabled() {
        return (PolicyLogger.getMetricsLevel() != Level.OFF);
    }

    /**
     * Records an audit message.
     *
     * @param message the message
     */
    @Override
    public void audit(Object message) {
        log.info(className + "|" + message);
    }

    /**
     * Records an audit message.
     *
     * @param message the message
     * @param throwable the throwable
     */

    @Override
    public void audit(Object message, Throwable throwable) {
        log.info(message, throwable);
    }

    /**
     * Records an audit message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void audit(String message, Object... arguments) {
        if (arguments.length > 0 && isThrowable(arguments[arguments.length - 1])) {
            info(formatMessage(message, arguments),
                (Throwable)arguments[arguments.length - 1]);
        } else {
            log.info(formatMessage(message, arguments));
        }
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     */
    @Override
    public void recordAuditEventStart(String eventId) {
        log.info(className + "|recordAuditEventStart with eventId " + eventId);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     */
    @Override
    public void recordAuditEventStart(UUID eventId) {
        if (eventId != null) {
            recordAuditEventStart(eventId.toString());
        }
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     * @param rule the rule
     * @param policyVersion the policy version
     */
    @Override
    public void recordAuditEventEnd(String eventId, String rule, String policyVersion) {
        log.info(className + "|" + eventId + ":" + rule);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     * @param rule the rule
     * @param policyVersion the policy version
     */
    @Override
    public void recordAuditEventEnd(UUID eventId, String rule, String policyVersion) {
        if (eventId != null) {
            recordAuditEventEnd(eventId.toString(), rule, policyVersion);
        } else {
            recordAuditEventEnd(eventId, rule, policyVersion);
        }
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     * @param rule the rule
     */
    @Override
    public void recordAuditEventEnd(String eventId, String rule) {
        log.info(className + "|" + eventId + ":" + rule);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     * @param rule the rule
     */
    @Override
    public void recordAuditEventEnd(UUID eventId, String rule) {
        if (eventId != null) {
            recordAuditEventEnd(eventId.toString(), rule);
        } else {
            recordAuditEventEnd(eventId, rule);
        }
    }

    /**
     * Records a metrics message.
     *
     * @param eventId the event ID
     * @param message the message
     */
    @Override
    public void recordMetricEvent(String eventId, String message) {
        log.info(className + "|" + eventId + ":" + message);

    }

    /**
     * Records a metrics message.
     *
     * @param eventId the event ID
     * @param message the message
     */
    @Override
    public void recordMetricEvent(UUID eventId, String message) {
        if (eventId != null) {
            recordMetricEvent(eventId.toString(), message);
        } else {
            recordMetricEvent(eventId, message);
        }
    }

    /**
     * Records a metrics message.
     *
     * @param message the message
     */
    @Override
    public void metrics(Object message) {
        log.info(message);
    }

    /**
     * Records a metrics message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void metrics(String message, Object... arguments) {
        if (arguments.length > 0 && isThrowable(arguments[arguments.length - 1])) {
            info(formatMessage(message, arguments),
                (Throwable)arguments[arguments.length - 1]);
        } else {
            log.info(formatMessage(message, arguments));
        }
    }

    /**
     * Returns transaction Id.
     *
     * @param transId the transaction ID
     */
    @Override
    public String postMdcInfoForEvent(String transId) {
        String transactionId = transId;
        if (transactionId == null || transactionId.isEmpty()) {
            transactionId = UUID.randomUUID().toString();
        }

        return transactionId;
    }

    /**
     * Records transaction Id.
     *
     * @param message the message
     */
    @Override
    public void postMdcInfoForEvent(Object message) {
        log.info(message);
    }

    /**
     * Returns true for trace enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    /**
     * Records transaction Id.
     *
     * @param transId the transaction ID
     */
    @Override
    public void postMdcInfoForTriggeredRule(String transId) {
        log.info(transId);
    }

    /* ============================================================ */

    /*
     * Support for 'Serializable' -- the default rules don't work for the 'log' field
     */

    private void writeObject(ObjectOutputStream out) throws IOException {
        // write out 'methodName', 'className', 'transId' strings
        out.writeUTF(methodName);
        out.writeUTF(className);
        out.writeUTF(transId);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        // read in 'methodName', 'className', 'transId' strings
        methodName = in.readUTF();
        className = in.readUTF();
        transId = in.readUTF();

        // look up associated logger
        log = Logger.getLogger(className);
    }

    /**
     * Create message text replace {} place holder with data
     * if last argument is throwable/exception, pass it as argument to logger.
     * @param format message format can contains text and {}
     * @param arguments output arguments
     * @return
     */
    private String formatMessage(String format, Object ...arguments) {
        if (arguments.length <= 0 || arguments[0] == null) {
            return format;
        }
        int index;
        StringBuilder builder = new StringBuilder();
        String[] token = format.split("[{][}]");
        for (index = 0; index < arguments.length; index++) {
            if (index < token.length) {
                builder.append(token[index]);
                builder.append(arguments[index]);
            } else {
                break;
            }
        }
        for (int index2 = index; index2 < token.length; index2++) {
            builder.append(token[index2]);
        }

        return builder.toString();
    }

    /**
     * Check object is throwable.
     * @param obj to verify
     * @return true if object is throwable or false otherwise
     */
    private boolean isThrowable(Object obj) {
        return (obj instanceof Throwable || Throwable.class.isInstance(obj));
    }
}
