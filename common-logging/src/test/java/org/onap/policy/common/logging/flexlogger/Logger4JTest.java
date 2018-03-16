/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.util.TestUtils;

public class Logger4JTest {

    private Logger4J logger4J = new Logger4J("str1", "Logger4JTest");

    @Test
    public void testLogger4JClassOfQ() {
        new Logger4J(this.getClass());
    }

    @Test
    public void testSetAndGetTransId() {
        logger4J.setTransId("transactionId");
        assertEquals("transactionId", logger4J.getTransId());
    }

    @Test
    public void testDebugObject() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
        logger4J.setTransId("transactionId");
        logger4J.debug("message");
        Mockito.verify(logger).debug("transactionId|message");
    }

    @Test
    public void testErrorObject() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        logger4J.error("message");
        Mockito.verify(logger).error("transactionId|Logger4JTest|message");
    }

    @Test
    public void testInfoObject() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        logger4J.info("message");
        Mockito.verify(logger).info("transactionId|Logger4JTest|message");
    }

    @Test
    public void testWarnObject() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        logger4J.warn("message");
        Mockito.verify(logger).warn("transactionId|Logger4JTest|message");
    }

    @Test
    public void testTraceObject() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        logger4J.trace("message");
        Mockito.verify(logger).trace("transactionId|Logger4JTest|message");
    }

    @Test
    public void testIsDebugEnabled() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        Mockito.when(logger.isDebugEnabled()).thenReturn(true).thenReturn(false);
        assertTrue(logger4J.isDebugEnabled());
        assertFalse(logger4J.isDebugEnabled());
    }

    @Test
    public void testIsErrorEnabled() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        Mockito.when(logger.isEnabledFor(Priority.ERROR)).thenReturn(true).thenReturn(false);
        assertTrue(logger4J.isErrorEnabled());
        assertFalse(logger4J.isErrorEnabled());
    }

    @Test
    public void testIsInfoEnabled() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        Mockito.when(logger.isInfoEnabled()).thenReturn(true).thenReturn(false);
        assertTrue(logger4J.isInfoEnabled());
        assertFalse(logger4J.isInfoEnabled());
    }

    @Test
    public void testIsWarnEnabled() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        Mockito.when(logger.isEnabledFor(Priority.WARN)).thenReturn(true).thenReturn(false);
        assertTrue(logger4J.isWarnEnabled());
        assertFalse(logger4J.isWarnEnabled());
    }

    @Test
    public void testAuditObject() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.audit("str1");
        Mockito.verify(logger).info("Logger4JTest|str1");
    }

    @Test
    public void testRecordAuditEventStartString() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.recordAuditEventEnd("eventId", "rule");
        Mockito.verify(logger).info("Logger4JTest|eventId:rule");
    }

    @Test
    public void testRecordAuditEventStartUUID() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        UUID uuid = UUID.randomUUID();
        logger4J.recordAuditEventStart(uuid);
        Mockito.verify(logger).info("Logger4JTest|recordAuditEventStart with eventId " + uuid.toString());
    }

    @Test
    public void testRecordAuditEventEndStringStringString() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.recordAuditEventEnd("eventId", "rule", "policyVersion");
        Mockito.verify(logger).info("Logger4JTest|eventId:rule");
    }

    @Test
    public void testRecordAuditEventEndUUIDStringString() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        UUID uuid = UUID.randomUUID();
        logger4J.recordAuditEventEnd(uuid, "rule", "policyVersion");
        Mockito.verify(logger).info("Logger4JTest|" + uuid.toString() + ":rule");
    }

    @Test
    public void testRecordAuditEventEndStringString() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.recordAuditEventEnd("eventId", "rule");
        Mockito.verify(logger).info("Logger4JTest|eventId:rule");
    }

    @Test
    public void testRecordAuditEventEndUUIDString() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        UUID uuid = UUID.randomUUID();
        logger4J.recordAuditEventEnd(uuid, "rule");
        Mockito.verify(logger).info("Logger4JTest|" + uuid.toString() + ":rule");
    }

    @Test
    public void testRecordMetricEventStringString() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.recordMetricEvent("eventId", "str1");
        Mockito.verify(logger).info("Logger4JTest|eventId:str1");
    }

    @Test
    public void testRecordMetricEventUUIDString() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        UUID uuid = UUID.randomUUID();
        logger4J.recordMetricEvent(uuid, "str1");
        Mockito.verify(logger).info("Logger4JTest|" + uuid.toString() + ":str1");
    }

    @Test
    public void testMetrics() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.metrics("str1");
        Mockito.verify(logger).info("str1");
    }

    @Test
    public void testErrorMessageCodesThrowableStringArray() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        logger4J.error(MessageCodes.GENERAL_ERROR, new NullPointerException(), "str1", "str2");
        Mockito.verify(logger)
                .error("transactionId|Logger4JTest|MessageCodes :" + MessageCodes.GENERAL_ERROR + "[str1, str2]");
    }

    @Test
    public void testErrorMessageCodesStringArray() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        logger4J.error(MessageCodes.GENERAL_ERROR, "str1", "str2");
        Mockito.verify(logger)
                .error("transactionId|Logger4JTest|MessageCode:" + MessageCodes.GENERAL_ERROR + "[str1, str2]");
    }

    @Test
    public void testPostMDCInfoForEventString() {
        String returnedTransactionId = logger4J.postMDCInfoForEvent("transactionId");
        assertEquals("transactionId", returnedTransactionId);
    }

    @Test
    public void testPostMDCInfoForEventEmptyString() {
        String returnedTransactionId = logger4J.postMDCInfoForEvent("");
        assertNotNull("", returnedTransactionId);
        assertNotEquals("", returnedTransactionId);
    }

    @Test
    public void testWarnMessageCodesStringArray() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.warn(MessageCodes.GENERAL_ERROR, "str1", "str2");
        Mockito.verify(logger).warn("Logger4JTest|MessageCodes:" + MessageCodes.GENERAL_ERROR + "[str1, str2]");
    }

    @Test
    public void testWarnMessageCodesThrowableStringArray() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        logger4J.warn(MessageCodes.GENERAL_ERROR, new NullPointerException(), "str1", "str2");
        Mockito.verify(logger).warn("Logger4JTest|MessageCodes:" + MessageCodes.GENERAL_ERROR + "[str1, str2]");
    }

    @Test
    public void testDebugObjectThrowable() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        Exception exception = new NullPointerException();
        logger4J.debug("message", exception);
        Mockito.verify(logger).debug("message", exception);
    }

    @Test
    public void testErrorObjectThrowable() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        Exception exception = new NullPointerException();
        logger4J.error("message", exception);
        Mockito.verify(logger).error("message", exception);
    }

    @Test
    public void testInfoObjectThrowable() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.setTransId("transactionId");
        Exception exception = new NullPointerException();
        logger4J.info("message", exception);
        Mockito.verify(logger).info("message", exception);
    }

    @Test
    public void testWarnObjectThrowable() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        Exception exception = new NullPointerException();
        logger4J.warn("message", exception);
        Mockito.verify(logger).warn("message", exception);
    }

    @Test
    public void testTraceObjectThrowable() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        Exception exception = new NullPointerException();
        logger4J.trace("message", exception);
        Mockito.verify(logger).trace("message", exception);
    }

    @Test
    public void testAuditObjectThrowable() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        Exception exception = new NullPointerException();
        logger4J.audit("message", exception);
        Mockito.verify(logger).info("message", exception);
    }

    @Test
    public void testIsTraceEnabled() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.isTraceEnabled();
        Mockito.verify(logger).isTraceEnabled();
    }

    @Test
    public void testPostMDCInfoForTriggeredRule() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.postMDCInfoForTriggeredRule("transactionId");
        Mockito.verify(logger).info("transactionId");
    }

    @Test
    public void testPostMDCInfoForEventObject() {
        Logger logger = Mockito.mock(Logger.class);
        TestUtils.overrideField(Logger4J.class, logger4J, "log", logger);
        logger4J.postMDCInfoForEvent(1);
        Mockito.verify(logger).info(1);
    }

}
