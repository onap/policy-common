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

package org.onap.policy.common.logging.flexlogger;

import com.att.eelf.configuration.EELFLogger.Level;
import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;

/**
 * EelfLogger implements all the methods of interface Logger by calling PolicyLogger methods.
 */

public class EelfLogger implements Logger, Serializable {

    private static final long serialVersionUID = 5385586713941277192L;
    private String className = "";
    @Getter
    private String transId = UUID.randomUUID().toString();

    /**
     * Constructor.
     *
     * @param clazz the class
     */
    public EelfLogger(Class<?> clazz) {
        if (clazz != null) {
            className = clazz.getName();
        }
        PolicyLogger.postMdcInfoForEvent(null);
    }

    /**
     * Constructor.
     *
     * @param className the class name
     */
    public EelfLogger(String className) {
        if (className != null) {
            this.className = className;
        }
        PolicyLogger.postMdcInfoForEvent(null);
    }

    /**
     * Constructor.
     *
     * @param clazz the class
     * @param isNewTransaction is a new transaction
     */
    public EelfLogger(Class<?> clazz, boolean isNewTransaction) {
        if (clazz != null) {
            className = clazz.getName();
        }
        if (isNewTransaction) {
            transId = PolicyLogger.postMdcInfoForEvent(null);
        } else {
            transId = PolicyLogger.getTransId();
        }
    }

    /**
     * Constructor.
     *
     * @param className the class name
     * @param isNewTransaction is a new transaction
     */
    public EelfLogger(String className, boolean isNewTransaction) {
        if (className != null) {
            this.className = className;
        }
        if (isNewTransaction) {
            transId = PolicyLogger.postMdcInfoForEvent(null);
        } else {
            transId = PolicyLogger.getTransId();
        }
    }

    /**
     * Constructor.
     *
     * @param clazz the class
     * @param transId the transaction ID
     */
    public EelfLogger(Class<?> clazz, String transId) {
        if (clazz != null) {
            className = clazz.getName();
        }
        PolicyLogger.postMdcInfoForEvent(transId);
        this.transId = transId;
    }

    /**
     * Constructor.
     *
     * @param className the class name
     * @param transId the transaction ID
     */
    public EelfLogger(String className, String transId) {
        if (className != null) {
            this.className = className;
        }
        PolicyLogger.postMdcInfoForEvent(transId);
        this.transId = transId;
    }

    /**
     * Sets transaction Id for logging.
     *
     * @param transId the transaction ID
     */
    @Override
    public void setTransId(String transId) {

        PolicyLogger.setTransId(transId);
        this.transId = transId;
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void debug(Object message) {
        PolicyLogger.debug(className, "" + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void debug(Object message, Throwable throwable) {
        PolicyLogger.debug(MessageCodes.GENERAL_INFO, throwable, message.toString());
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments the arguments for message
     */
    @Override
    public void debug(String message, Object... arguments) {
        PolicyLogger.debug(message, arguments);
    }

    /**
     * Records an error message.
     *
     * @param message the message
     */
    @Override
    public void error(Object message) {
        PolicyLogger.error(className, "" + message);
    }

    /**
     * Records an error message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void error(Object message, Throwable throwable) {
        PolicyLogger.error(MessageCodes.ERROR_UNKNOWN, throwable, message.toString());
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
        PolicyLogger.error(msg, className, throwable, arguments);
    }

    /**
     * Records an error message.
     *
     * @param msg the message code
     * @param arguments the messages
     */
    @Override
    public void error(MessageCodes msg, String... arguments) {
        PolicyLogger.error(msg, arguments);
    }

    /**
     * Records an error message.
     *
     * @param message the message
     * @param arguments the arguments for message
     */
    @Override
    public void error(String message, Object... arguments) {
        PolicyLogger.error(message, arguments);
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void info(Object message) {
        PolicyLogger.info(className, "" + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void info(Object message, Throwable throwable) {
        PolicyLogger.info(MessageCodes.GENERAL_INFO, throwable, message.toString());
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments the arguments for message
     */
    @Override
    public void info(String message, Object... arguments) {
        PolicyLogger.info(message, arguments);
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void warn(Object message) {
        PolicyLogger.warn(className, "" + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void warn(Object message, Throwable throwable) {
        PolicyLogger.warn(MessageCodes.GENERAL_WARNING, throwable, message.toString());
    }

    /**
     * Records a message.
     *
     * @param msg the message codes
     * @param arguments the messages
     */
    @Override
    public void warn(MessageCodes msg, String... arguments) {
        PolicyLogger.warn(msg, className, arguments);
    }

    /**
     * Records a message.
     *
     * @param msg the message
     * @param throwable the throwable
     * @param arguments the messages
     */
    @Override
    public void warn(MessageCodes msg, Throwable throwable, String... arguments) {
        PolicyLogger.warn(msg, className, throwable, arguments);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments the arguments for message
     */
    @Override
    public void warn(String message, Object... arguments) {
        PolicyLogger.warn(message, arguments);
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void trace(Object message) {
        PolicyLogger.trace(className, "" + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void trace(Object message, Throwable throwable) {
        PolicyLogger.trace(message);
    }

    /**
     * Returns true for debug enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isDebugEnabled() {
        return PolicyLogger.isDebugEnabled();
    }

    /**
     * Returns true for info enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isInfoEnabled() {
        return PolicyLogger.isInfoEnabled();
    }

    /**
     * Returns true for warn enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isWarnEnabled() {
        return PolicyLogger.isWarnEnabled();
    }

    /**
     * Returns true for error enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isErrorEnabled() {
        return PolicyLogger.isErrorEnabled();
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
     * Returns true for trace enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isTraceEnabled() {
        return PolicyLogger.isDebugEnabled();
    }

    /**
     * Records an audit message.
     *
     * @param arg0 the message
     */
    @Override
    public void audit(Object arg0) {
        PolicyLogger.audit(className, "" + arg0);
    }

    /**
     * Records an audit message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void audit(Object message, Throwable throwable) {
        PolicyLogger.audit(message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments the arguments for message
     */
    @Override
    public void audit(String message, Object... arguments) {
        PolicyLogger.audit(message, arguments);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     */
    @Override
    public void recordAuditEventStart(String eventId) {
        PolicyLogger.recordAuditEventStart(eventId);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     */
    @Override
    public void recordAuditEventStart(UUID eventId) {
        PolicyLogger.recordAuditEventStart(eventId);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     * @param rule the rule
     * @param policyVersion the policy cersion
     */
    @Override
    public void recordAuditEventEnd(String eventId, String rule, String policyVersion) {
        PolicyLogger.recordAuditEventEnd(eventId, rule, policyVersion);
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
        PolicyLogger.recordAuditEventEnd(eventId, rule, policyVersion);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     * @param rule the rule
     */
    @Override
    public void recordAuditEventEnd(String eventId, String rule) {
        PolicyLogger.recordAuditEventEnd(eventId, rule);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     * @param rule the rule
     */
    @Override
    public void recordAuditEventEnd(UUID eventId, String rule) {
        PolicyLogger.recordAuditEventEnd(eventId, rule);
    }

    /**
     * Records a metrics message.
     *
     * @param eventId the event ID
     * @param message the message
     */
    @Override
    public void recordMetricEvent(String eventId, String message) {
        PolicyLogger.recordMetricEvent(eventId, message);
    }

    /**
     * Records a metrics message.
     *
     * @param eventId the event ID
     * @param message the message
     */
    @Override
    public void recordMetricEvent(UUID eventId, String message) {
        PolicyLogger.recordMetricEvent(eventId, message);
    }

    /**
     * Records a metrics message.
     *
     * @param message the message
     */
    @Override
    public void metrics(Object message) {
        PolicyLogger.metrics(className, message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments the arguments for message
     */
    @Override
    public void metrics(String message, Object... arguments) {
        PolicyLogger.metrics(message, arguments);
    }

    /**
     * Populates MDC Info.
     *
     * @param transId the transaction ID
     */
    @Override
    public String postMdcInfoForEvent(String transId) {
        return PolicyLogger.postMdcInfoForEvent(transId);
    }

    /**
     * Populates MDC Info.
     *
     * @param obj the object
     */
    @Override
    public void postMdcInfoForEvent(Object obj) {
        PolicyLogger.postMdcInfoForEvent(obj);
    }

    /**
     * Populates MDC Info for the rule triggered.
     *
     * @param transId the transaction ID
     */
    @Override
    public void postMdcInfoForTriggeredRule(String transId) {
        PolicyLogger.postMdcInfoForTriggeredRule(transId);
    }

}
