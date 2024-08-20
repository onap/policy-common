/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;

import com.att.eelf.configuration.EELFLogger;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

class EelfLoggerTest {

    EelfLogger eelfLogger = new EelfLogger("EelfLoggerTest", "transactionId");

    @Test
    void testEelfLoggerClassOfQ() {
        new EelfLogger(this.getClass());
        assertNotNull(PolicyLogger.getTransId());
        assertFalse(PolicyLogger.getTransId().isEmpty());
    }

    @Test
    void testEelfLoggerString() {
        new EelfLogger("EelfLoggerTest");
        assertNotNull(PolicyLogger.getTransId());
        assertFalse(PolicyLogger.getTransId().isEmpty());
    }

    @Test
    void testEelfLoggerClassOfQBoolean() {
        new EelfLogger(this.getClass(), true);
        assertNotNull(PolicyLogger.getTransId());
        assertFalse(PolicyLogger.getTransId().isEmpty());
    }

    @Test
    void testEelfLoggerStringBoolean() {
        new EelfLogger("EelfLoggerTest", true);
        assertNotNull(PolicyLogger.getTransId());
        assertFalse(PolicyLogger.getTransId().isEmpty());
    }

    @Test
    void testEelfLoggerClassOfQString() {
        new EelfLogger(this.getClass(), "transactionId");
        assertEquals("transactionId", PolicyLogger.getTransId());
    }

    @Test
    void testEelfLoggerStringString() {
        new EelfLogger("EelfLoggerTest", "transactionId");
        assertEquals("transactionId", PolicyLogger.getTransId());
    }

    @Test
    void testSetAndGetTransId() {
        assertEquals("transactionId", eelfLogger.getTransId());
        eelfLogger.setTransId("transactionId2");
        assertEquals("transactionId2", eelfLogger.getTransId());
    }

    @Test
    void testDebugObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.debug("message");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);
        eelfLogger.debug("message");
        Mockito.verify(mockLogger).debug(MessageCodes.GENERAL_INFO, "message");
    }

    @Test
    void testDebugObjectArgs() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.debug("message", "arg1", "arg2");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);
        assertTrue(eelfLogger.isDebugEnabled());
    }

    @Test
    void testErrorObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        eelfLogger.error("message");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
        eelfLogger.error("message");
        Mockito.verify(mockLogger).error(MessageCodes.GENERAL_ERROR, "message");
    }

    @Test
    void testErrorObjectArgs() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        eelfLogger.error("message", "args1", "arg2");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
        assertTrue(eelfLogger.isErrorEnabled());
    }

    @Test
    void testInfoObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.info("message");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        eelfLogger.info("message");
        Mockito.verify(mockLogger).info(MessageCodes.GENERAL_INFO, "message");
    }

    @Test
    void testInfoObjectArgs() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.info("message", "arg1", "arg2");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        assertTrue(eelfLogger.isInfoEnabled());
    }

    @Test
    void testWarnObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.warn("message");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isWarnEnabled()).thenReturn(true);
        eelfLogger.warn("message");
        Mockito.verify(mockLogger).warn(MessageCodes.GENERAL_INFO, "message");
    }

    @Test
    void testWarnObjectArgs() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.warn("message", "arg1", "arg2");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isWarnEnabled()).thenReturn(true);
        assertTrue(eelfLogger.isWarnEnabled());
    }

    @Test
    void testTraceObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.trace("message");
        Mockito.verify(mockLogger).trace(MessageCodes.GENERAL_INFO, "message");
    }

    @Test
    void testIsDebugEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isDebugEnabled());
        assertTrue(eelfLogger.isDebugEnabled());

    }

    @Test
    void testIsInfoEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isInfoEnabled());
        assertTrue(eelfLogger.isInfoEnabled());
    }

    @Test
    void testIsWarnEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isWarnEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isWarnEnabled());
        assertTrue(eelfLogger.isWarnEnabled());
    }

    @Test
    void testIsErrorEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        Mockito.when(mockLogger.isErrorEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isErrorEnabled());
        assertTrue(eelfLogger.isErrorEnabled());
    }

    @Test
    void testIsMetricsEnabled() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setMetricsLevel("ERROR");
        assertTrue(eelfLogger.isMetricsEnabled());
        PolicyLogger.setMetricsLevel(EELFLogger.Level.OFF);
        assertFalse(eelfLogger.isMetricsEnabled());
    }

    @Test
    void testIsTraceEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isTraceEnabled());
        assertTrue(eelfLogger.isTraceEnabled());
    }

    @Test
    void testAuditObject() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setAuditLevel("ERROR");
        assertTrue(eelfLogger.isAuditEnabled());
        PolicyLogger.setAuditLevel(EELFLogger.Level.OFF);
        assertFalse(eelfLogger.isAuditEnabled());
    }

    @Test
    void testDebugObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.debug("message", new NullPointerException());
        Mockito.verify(mockLogger).debug((MessageCodes) Mockito.any(),
                Mockito.startsWith("message:java.lang.NullPointerException"));
    }

    @Test
    void testErrorObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        eelfLogger.error("message", new NullPointerException());
        Mockito.verify(mockLogger).error((MessageCodes) Mockito.any(),
                Mockito.startsWith("message:java.lang.NullPointerException"));
        eelfLogger.error("message", new NullPointerException());
    }

    @Test
    void testInfoObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.info("message", new NullPointerException());
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        Mockito.verify(mockLogger).info((MessageCodes) Mockito.any(),
                Mockito.startsWith("message:java.lang.NullPointerException"));
    }

    @Test
    void testWarnObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.warn("message", new NullPointerException());
        Mockito.verify(mockLogger).warn((MessageCodes) Mockito.any(),
                Mockito.startsWith("message:java.lang.NullPointerException"));
    }

    @Test
    void testTraceObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.trace("message", new NullPointerException());
        Mockito.verify(mockLogger).trace("{}", "message");
    }

    @Test
    void testAuditObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "auditLogger", mockLogger);
        eelfLogger.audit("message", new NullPointerException());
        Mockito.verify(mockLogger).info("{}", "message");
    }

    @Test
    void testAuditObjectArgs() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "auditLogger", mockLogger);
        assertThatCode(() -> eelfLogger.audit("messagearg1")).doesNotThrowAnyException();
    }

    @Test
    void testAuditObjectArgsAndMessage() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "auditLogger", mockLogger);
        assertThatCode(() -> eelfLogger.audit("message", "arg1", "arg2")).doesNotThrowAnyException();
    }

    @Test
    void testRecordAuditEventStartString() {
        eelfLogger.recordAuditEventStart("eventId");
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));
    }

    @Test
    void testRecordAuditEventStartUuid() {
        UUID uuid = UUID.randomUUID();
        eelfLogger.recordAuditEventStart(uuid);
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));
    }

    @Test
    void testRecordAuditEventEndStringStringString() {
        eelfLogger.recordAuditEventStart("eventId");
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));

        eelfLogger.recordAuditEventEnd("eventId", "rule", "policyVersion");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));
    }

    @Test
    void testRecordAuditEventEndUuidStringString() {
        UUID uuid = UUID.randomUUID();
        eelfLogger.recordAuditEventStart(uuid);;
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));

        eelfLogger.recordAuditEventEnd(uuid, "rule", "policyVersion");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));
    }

    @Test
    void testRecordAuditEventEndStringString() {
        eelfLogger.recordAuditEventStart("eventId");
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));

        eelfLogger.recordAuditEventEnd("eventId", "rule");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));
    }

    @Test
    void testRecordAuditEventEndUuidString() {
        UUID uuid = UUID.randomUUID();
        eelfLogger.recordAuditEventStart(uuid);;
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));

        eelfLogger.recordAuditEventEnd(uuid, "rule");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));
    }

    @Test
    void testRecordMetricEventStringString() {
        eelfLogger.recordMetricEvent("eventId", "str1");
        assertEquals("eventId", MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    void testRecordMetricEventUuidString() {
        UUID uuid = UUID.randomUUID();
        eelfLogger.recordMetricEvent(uuid, "str2");
        assertEquals(uuid.toString(), MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    void testMetrics() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "metricsLogger", mockLogger);
        eelfLogger.metrics(1);
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        eelfLogger.metrics(1);
        Mockito.verify(mockLogger).info(Mockito.eq(MessageCodes.RULE_METRICS_INFO), Mockito.anyString(),
                Mockito.eq("1"));
    }

    @Test
    void testMetricsArgs() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "metricsLogger", mockLogger);
        eelfLogger.metrics("metricsMessage", "arg1", "arg2");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        assertFalse(eelfLogger.isMetricsEnabled());
    }

    @Test
    void testErrorMessageCodesThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        eelfLogger.error(MessageCodes.GENERAL_ERROR, new NullPointerException(), "str1", "str2");
        Mockito.verify(mockLogger).error((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    void testErrorMessageCodesStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        eelfLogger.error(MessageCodes.GENERAL_ERROR, "str1", "str2");
        Mockito.verify(mockLogger).error(MessageCodes.GENERAL_ERROR, "str1", "str2");

    }

    @Test
    void testPostMdcInfoForEventString() {
        eelfLogger.postMdcInfoForEvent("transactionId");
        assertEquals("transactionId", MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    void testWarnMessageCodesStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.warn(MessageCodes.GENERAL_ERROR, "str1", "str2");
        Mockito.verify(mockLogger).warn(MessageCodes.GENERAL_ERROR, "str1", "str2");
    }

    @Test
    void testWarnMessageCodesThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.warn(MessageCodes.GENERAL_ERROR, new NullPointerException(), "str1", "str2");
        Mockito.verify(mockLogger).warn((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));

    }

    @Test
    void testPostMdcInfoForTriggeredRule() {
        eelfLogger.postMdcInfoForTriggeredRule("transactionId");
        assertEquals("transactionId", MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    void testPostMDdcInfoForEventObject() {
        eelfLogger.postMdcInfoForEvent(1);
        assertEquals("1", MDC.get(MDC_KEY_REQUEST_ID));
    }

}
