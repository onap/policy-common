/*-
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

import static org.junit.Assert.assertEquals;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.features.NetLoggerFeatureApi;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.slf4j.Logger;

/**
 * Test class for network log utilities such as logging and feature invocation.
 */
public class NetLoggerUtilTest {

    /**
     * Clears events list before invoking every unit test.
     */
    @Before
    public void clearEvents() {
        TestAppender.clear();
    }

    /**
     * Tests obtaining the network logger instance.
     */
    @Test
    public void getNetworkLoggerTest() {
        assertEquals("network", NetLoggerUtil.getNetworkLogger().getName());
    }

    /**
     * Tests logging a message to the network logger and invoking features before/after logging.
     */
    @Test
    public void logTest() {
        NetLoggerUtil.log(EventType.IN, CommInfrastructure.NOOP, "test-topic", "hello world!");
        assertEquals(3, TestAppender.events.size());
    }

    /**
     * Tests that the network logger is used to log messages if a logger is not passed in.
     */
    @Test
    public void logDefaultTest() {
        NetLoggerUtil.log(null, EventType.IN, CommInfrastructure.NOOP, "test-topic", "hello world!");
        assertEquals(3, TestAppender.events.size());
        assertEquals("network", TestAppender.events.get(0).getLoggerName());
    }

    /**
     * A custom list appender to track messages being logged to the network logger.
     * NOTE: Check src/test/resources/logback-test.xml for network logger configurations.
     */
    public static class TestAppender extends AppenderBase<ILoggingEvent> {

        /**
         * List of logged events.
         */
        public static List<ILoggingEvent> events = new ArrayList<>();

        /**
         * Called after ever unit test to clear list of events.
         */
        public static void clear() {
            events.clear();
        }

        /**
         * Appends each event to the event list.
         */
        @Override
        protected void append(ILoggingEvent event) {
            events.add(event);
        }

    }

    /**
     * A test implementation of the NetLoggerFeatureApi.
     */
    public static class NetLoggerFeatureImpl implements NetLoggerFeatureApi {

        /**
         * Returns sequence number.
         */
        @Override
        public int getSequenceNumber() {
            return 1;
        }

        /**
         * Simple beforeLog message.
         */
        @Override
        public boolean beforeLog(Logger eventLogger, EventType type, CommInfrastructure protocol, String topic,
                        String message) {
            eventLogger.info("before feature test");
            return false;
        }

        /**
         * Simple afterLog message.
         */
        @Override
        public boolean afterLog(Logger eventLogger, EventType type, CommInfrastructure protocol, String topic,
                        String message) {
            eventLogger.info("after feature test");
            return false;
        }

    }

}
