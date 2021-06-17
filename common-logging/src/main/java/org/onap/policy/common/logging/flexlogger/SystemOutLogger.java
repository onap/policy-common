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

import static org.onap.policy.common.logging.flexlogger.DisplayUtils.displayMessage;

import com.att.eelf.configuration.EELFLogger.Level;
import java.io.Serializable;
import java.util.Arrays;
import java.util.UUID;
import lombok.Getter;
import org.onap.policy.common.logging.OnapLoggingUtils;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;

/**
 * SystemOutLogger implements all the methods of interface Logger by calling System.out.println
 */
@Getter
public class SystemOutLogger implements Logger, Serializable {

    private static final long serialVersionUID = 4956408061058933929L;
    private String className = "";
    private boolean debugEnabled = true;
    private boolean infoEnabled = true;
    private boolean warnEnabled = true;
    private boolean errorEnabled = true;
    private boolean auditEnabled = true;
    private boolean metricsEnabled = true;
    private String transId = UUID.randomUUID().toString();

    /**
     * Constructor.
     *
     * @param clazz the class
     */
    public SystemOutLogger(Class<?> clazz) {
        displayMessage("create instance of SystemOutLogger");
        if (clazz != null) {
            className = clazz.getName();
        }
        initLevel();
    }

    /**
     * Constructor.
     *
     * @param className the class name
     */
    public SystemOutLogger(String className) {
        displayMessage("create instance of SystemOutLogger");
        if (className != null) {
            this.className = className;
        }
        initLevel();
    }

    /**
     * Sets logging levels.
     */
    private void initLevel() {

        if (PolicyLogger.getDebugLevel() == Level.DEBUG) {
            debugEnabled = true;
            infoEnabled = true;
            warnEnabled = true;
        } else {
            debugEnabled = false;
        }

        if (PolicyLogger.getDebugLevel() == Level.INFO) {
            infoEnabled = true;
            warnEnabled = true;
            debugEnabled = false;
        }

        if (PolicyLogger.getDebugLevel() == Level.OFF) {
            infoEnabled = false;
            warnEnabled = false;
            debugEnabled = false;
        }

        if (PolicyLogger.getErrorLevel() == Level.OFF) {
            errorEnabled = false;
        }

        if (PolicyLogger.getAuditLevel() == Level.OFF) {
            auditEnabled = false;
        }

        if (PolicyLogger.getMetricsLevel() == Level.OFF) {
            metricsEnabled = false;
        }
    }

    /**
     * Sets transaction Id.
     */
    @Override
    public void setTransId(String transId) {

        displayMessage(transId);
        this.transId = transId;
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void debug(Object message) {

        displayMessage(transId + "|" + className + " : " + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void debug(Object message, Throwable throwable) {
        displayMessage(transId + "|" + className + " : " + message + ":" + throwable);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void debug(String message, Object...arguments) {
        if (arguments.length == 1 && OnapLoggingUtils.isThrowable(arguments[0])) {
            displayMessage(transId + "|" + className + " : " + message + ":" + arguments[0]);
        } else {
            displayMessage(OnapLoggingUtils.formatMessage(message, arguments));
        }
    }

    /**
     * Records an error message.
     *
     * @param message the message
     */
    @Override
    public void error(Object message) {

        displayMessage(transId + "|" + className + " : " + message);
    }

    /**
     * Records an error message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void error(Object message, Throwable throwable) {
        displayMessage(transId + "|" + className + " : " + message + ":" + throwable);
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
        displayMessage(className + " : " + "MessageCodes :" + msg + Arrays.asList(arguments));
    }

    /**
     * Records an error message.
     *
     * @param msg the message code
     * @param arguments the messages
     */
    @Override
    public void error(MessageCodes msg, String... arguments) {
        displayMessage(transId + "|" + className + " : " + "MessageCode:" + msg + Arrays.asList(arguments));
    }

    /**
     * Records a error message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void error(String message, Object...arguments) {
        if (arguments.length == 1 && OnapLoggingUtils.isThrowable(arguments[0])) {
            displayMessage(transId + "|" + className + " : " + message + ":" + arguments[0]);
        } else {
            displayMessage(OnapLoggingUtils.formatMessage(message, arguments));
        }
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void info(Object message) {
        displayMessage(transId + "|" + className + " : " + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void info(Object message, Throwable throwable) {
        displayMessage(transId + "|" + className + " : " + message + ":" + throwable);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void info(String message, Object...arguments) {
        if (arguments.length == 1 && OnapLoggingUtils.isThrowable(arguments[0])) {
            displayMessage(transId + "|" + className + " : " + message + ":" + arguments[0]);
        } else {
            displayMessage(OnapLoggingUtils.formatMessage(message, arguments));
        }
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void warn(Object message) {
        displayMessage(transId + "|" + className + " : " + message);
    }

    /**
     * Records a message.
     *
     * @param msg the message code
     * @param arguments the messages
     */
    @Override
    public void warn(MessageCodes msg, String... arguments) {

        displayMessage(transId + "|" + className + " : " + "MessageCodes:" + msg + Arrays.asList(arguments));
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void warn(Object message, Throwable throwable) {
        displayMessage(transId + "|" + className + " : " + message + ":" + throwable);
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

        displayMessage(transId + "|" + className + " : " + "MessageCodes:" + msg + Arrays.asList(arguments));

    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param arguments variable number of arguments
     */
    @Override
    public void warn(String message, Object...arguments) {
        if (arguments.length == 1 && OnapLoggingUtils.isThrowable(arguments[0])) {
            displayMessage(transId + "|" + className + " : " + message + ":" + arguments[0]);
        } else {
            displayMessage(OnapLoggingUtils.formatMessage(message, arguments));
        }
    }

    /**
     * Records a message.
     *
     * @param message the message
     */
    @Override
    public void trace(Object message) {
        displayMessage(transId + "|" + className + " : " + message);
    }

    /**
     * Records a message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void trace(Object message, Throwable throwable) {
        displayMessage(transId + "|" + className + " : " + message + ":" + throwable);
    }

    /**
     * Records an audit message.
     *
     * @param message the message
     */
    @Override
    public void audit(Object message) {

        displayMessage(transId + "|" + className + " : " + message);
    }

    /**
     * Records an audit message.
     *
     * @param message the message
     * @param throwable the throwable
     */
    @Override
    public void audit(Object message, Throwable throwable) {
        displayMessage(transId + "|" + className + " : " + message + ":" + throwable);
    }

    /**
     * Records an audit message.
     *
     * @param message the message
     */
    @Override
    public void audit(String message, Object... arguments) {
        if (arguments.length == 1) {
            displayMessage(transId + "|" + className + " : " + message + ":" + arguments[0]);
        } else {
            displayMessage(OnapLoggingUtils.formatMessage(message, arguments));
        }
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     */
    @Override
    public void recordAuditEventStart(String eventId) {

        displayMessage(transId + "|" + className + " : " + eventId);

    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     */
    @Override
    public void recordAuditEventStart(UUID eventId) {

        displayMessage(eventId);
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

        displayMessage(className + " : " + eventId + ":" + rule + ":" + policyVersion);
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

        displayMessage(className + " : " + eventId + ":" + rule + ":" + policyVersion);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     * @param rule the rule
     */
    @Override
    public void recordAuditEventEnd(String eventId, String rule) {

        displayMessage(className + " : " + eventId + ":" + rule);
    }

    /**
     * Records an audit message.
     *
     * @param eventId the event ID
     * @param rule the rule
     */
    @Override
    public void recordAuditEventEnd(UUID eventId, String rule) {

        displayMessage(className + " : " + eventId + ":" + rule);
    }

    /**
     * Records a metrics message.
     *
     * @param eventId the event ID
     * @param message the message
     */
    @Override
    public void recordMetricEvent(String eventId, String message) {

        displayMessage(className + " : " + "eventId:" + eventId + "message:" + message);

    }

    /**
     * Records a metrics message.
     *
     * @param eventId the event ID
     * @param message the message
     */
    @Override
    public void recordMetricEvent(UUID eventId, String message) {

        displayMessage(className + " : " + eventId + ":" + message);
    }

    /**
     * Records a metrics message.
     *
     * @param message the message
     */
    @Override
    public void metrics(Object message) {

        displayMessage(className + " : " + message);
    }

    /**
     * Records a metrics message.
     *
     * @param message the message
     * @param arguments the arguments
     */
    @Override
    public void metrics(String message, Object... arguments) {
        if (arguments.length == 1) {
            displayMessage(className + " : " + message + " : " + arguments[0]);
        } else {
            displayMessage(OnapLoggingUtils.formatMessage(message, arguments));
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
        displayMessage(message);
    }


    /**
     * Returns true for trace enabled, or false for not.
     *
     * @return boolean
     */
    @Override
    public boolean isTraceEnabled() {
        // default
        return false;
    }

    /**
     * Records transaction Id.
     *
     * @param transId the transaction ID
     */
    @Override
    public void postMdcInfoForTriggeredRule(String transId) {

        displayMessage(transId);
    }
}
