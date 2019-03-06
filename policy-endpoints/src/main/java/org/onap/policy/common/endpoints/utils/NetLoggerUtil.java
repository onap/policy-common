/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.utils;

import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.features.NetLoggerFeatureApi;
import org.onap.policy.common.utils.slf4j.LoggerFactoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A network logging utility class that allows drools applications code to access the
 * network log (or other specified loggers) and logging features.
 *
 */
public class NetLoggerUtil {

    /**
     * Loggers.
     */
    private static Logger logger = LoggerFactory.getLogger(NetLoggerUtil.class);
    private static Logger netLogger = LoggerFactoryWrapper.getNetworkLogger();

    /**
     * Specifies if the message is coming in or going out.
     */
    public enum EventType {
        IN, OUT
    }

    /**
     * Logs a message to the network logger.
     *
     * @param type can either be IN or OUT
     * @param protocol the protocol used to receive/send the message
     * @param topic the topic the message came from or null if the type is REST
     * @param message message to be logged
     */
    public static void log(EventType type, CommInfrastructure protocol, String topic, String message) {
        log(netLogger, type, protocol, topic, message);
    }

    /**
     * Logs a message to the specified logger (i.e. a controller logger).
     *
     * @param eventLogger the logger that will have the message appended
     * @param type can either be IN or OUT
     * @param protocol the protocol used to receive/send the message
     * @param topic the topic the message came from or null if the type is REST
     * @param message message to be logged
     */
    public static void log(Logger eventLogger, EventType type, CommInfrastructure protocol, String topic,
                    String message) {
        if (eventLogger == null) {
            logger.debug("the logger is null, defaulting to network logger");
            eventLogger = netLogger;
        }

        if (featureBeforeLog(eventLogger, type, protocol, topic, message)) {
            return;
        }

        eventLogger.info("[{}|{}|{}]{}{}", type, protocol, topic, System.lineSeparator(), message);

        if (featureAfterLog(eventLogger, type, protocol, topic, message)) {
            return;
        }
    }

    /**
     * Executes features that pre-process a message before it is logged.
     *
     * @param eventLogger the logger that will have the message appended
     * @param type can either be IN or OUT
     * @param protocol the protocol used to receive/send the message
     * @param topic the topic the message came from or null if the type is REST
     * @param message message to be logged
     *
     * @return true if this feature intercepts and takes ownership of the operation
     *         preventing the invocation of lower priority features. False, otherwise
     */
    private static boolean featureBeforeLog(Logger eventLogger, EventType type, CommInfrastructure protocol,
                    String topic, String message) {
        for (NetLoggerFeatureApi feature : NetLoggerFeatureApi.providers.getList()) {
            try {
                if (feature.beforeLog(eventLogger, type, protocol, topic, message)) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("feature {} before-log failure because of {}", feature.getClass().getName(),
                                e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * Executes features that post-process a message after it is logged.
     *
     * @param eventLogger the logger that will have the message appended
     * @param type can either be IN or OUT
     * @param protocol the protocol used to receive/send the message
     * @param topic the topic the message came from or null if the type is rest
     * @param message message to be logged
     *
     * @return true if this feature intercepts and takes ownership of the operation
     *         preventing the invocation of lower priority features. False, otherwise
     */
    private static boolean featureAfterLog(Logger eventLogger, EventType type, CommInfrastructure protocol,
                    String topic, String message) {
        for (NetLoggerFeatureApi feature : NetLoggerFeatureApi.providers.getList()) {
            try {
                if (feature.afterLog(eventLogger, type, protocol, topic, message)) {
                    return true;
                }
            } catch (Exception e) {
                logger.error("feature {} after-log failure because of {}", feature.getClass().getName(), e.getMessage(),
                                e);
            }
        }
        return false;
    }
}
