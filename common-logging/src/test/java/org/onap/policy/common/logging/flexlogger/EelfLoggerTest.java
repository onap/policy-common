/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;

import com.att.eelf.configuration.EELFLogger;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

public class EelfLoggerTest {

    EelfLogger eelfLogger = new EelfLogger("EelfLoggerTest", "transactionId");

    @Test
    public void testEelfLoggerClassOfQ() {
        new EelfLogger(this.getClass());
        assertNotNull(PolicyLogger.getTransId());
        assertFalse(PolicyLogger.getTransId().isEmpty());
    }

    @Test
    public void testEelfLoggerString() {
        new EelfLogger("EelfLoggerTest");
        assertNotNull(PolicyLogger.getTransId());
        assertFalse(PolicyLogger.getTransId().isEmpty());
    }

    @Test
    public void testEelfLoggerClassOfQBoolean() {
        new EelfLogger(this.getClass(), true);
        assertNotNull(PolicyLogger.getTransId());
        assertFalse(PolicyLogger.getTransId().isEmpty());
    }

    @Test
    public void testEelfLoggerStringBoolean() {
        new EelfLogger("EelfLoggerTest", true);
        assertNotNull(PolicyLogger.getTransId());
        assertFalse(PolicyLogger.getTransId().isEmpty());
    }

    @Test
    public void testEelfLoggerClassOfQString() {
        new EelfLogger(this.getClass(), "transactionId");
        assertEquals("transactionId", PolicyLogger.getTransId());
    }

    @Test
    public void testEelfLoggerStringString() {
        new EelfLogger("EelfLoggerTest", "transactionId");
        assertEquals("transactionId", PolicyLogger.getTransId());
    }

    @Test
    public void testSetAndGetTransId() {
        assertEquals("transactionId", eelfLogger.getTransId());
        eelfLogger.setTransId("transactionId2");
        assertEquals("transactionId2", eelfLogger.getTransId());
    }

    @Test
    public void testDebugObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.debug("message");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);
        eelfLogger.debug("message");
        Mockito.verify(mockLogger).debug(MessageCodes.GENERAL_INFO, "message");
    }

    @Test
    public void testErrorObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        eelfLogger.error("message");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
        eelfLogger.error("message");
        Mockito.verify(mockLogger).error(MessageCodes.GENERAL_ERROR, "message");
    }

    @Test
    public void testInfoObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.info("message");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        eelfLogger.info("message");
        Mockito.verify(mockLogger).info(MessageCodes.GENERAL_INFO, "message");
    }

    @Test
    public void testWarnObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.warn("message");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isWarnEnabled()).thenReturn(true);
        eelfLogger.warn("message");
        Mockito.verify(mockLogger).warn(MessageCodes.GENERAL_INFO, "message");
    }

    @Test
    public void testTraceObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.trace("message");
        Mockito.verify(mockLogger).trace(MessageCodes.GENERAL_INFO, "message");
    }

    @Test
    public void testIsDebugEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isDebugEnabled());
        assertTrue(eelfLogger.isDebugEnabled());

    }

    @Test
    public void testIsInfoEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isInfoEnabled());
        assertTrue(eelfLogger.isInfoEnabled());
    }

    @Test
    public void testIsWarnEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isWarnEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isWarnEnabled());
        assertTrue(eelfLogger.isWarnEnabled());
    }

    @Test
    public void testIsErrorEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        Mockito.when(mockLogger.isErrorEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isErrorEnabled());
        assertTrue(eelfLogger.isErrorEnabled());
    }

    @Test
    public void testIsAuditEnabled() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setAuditLevel("ERROR");
        assertTrue(eelfLogger.isAuditEnabled());
    }

    @Test
    public void testIsMetricsEnabled() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setMetricsLevel("ERROR");
        assertTrue(eelfLogger.isMetricsEnabled());
    }

    @Test
    public void testIsTraceEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(eelfLogger.isTraceEnabled());
        assertTrue(eelfLogger.isTraceEnabled());
    }

    @Test
    public void testAuditObject() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setAuditLevel("ERROR");
        assertTrue(eelfLogger.isAuditEnabled());
    }

    @Test
    public void testDebugObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.debug("message", new NullPointerException());
        Mockito.verify(mockLogger).debug((MessageCodes) Mockito.any(),
                Mockito.startsWith("message:java.lang.NullPointerException"));
    }

    @Test
    public void testErrorObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        eelfLogger.error("message", new NullPointerException());
        Mockito.verify(mockLogger).error((MessageCodes) Mockito.any(),
                Mockito.startsWith("message:java.lang.NullPointerException"));
        eelfLogger.error("message", new NullPointerException());
    }

    @Test
    public void testInfoObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.info("message", new NullPointerException());
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        Mockito.verify(mockLogger).info((MessageCodes) Mockito.any(),
                Mockito.startsWith("message:java.lang.NullPointerException"));
    }

    @Test
    public void testWarnObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.warn("message", new NullPointerException());
        Mockito.verify(mockLogger).warn((MessageCodes) Mockito.any(),
                Mockito.startsWith("message:java.lang.NullPointerException"));
    }

    @Test
    public void testTraceObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.trace("message", new NullPointerException());
        Mockito.verify(mockLogger).trace("{}", "message");
    }

    @Test
    public void testAuditObjectThrowable() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "auditLogger", mockLogger);
        eelfLogger.audit("message", new NullPointerException());
        Mockito.verify(mockLogger).info("{}", "message");
    }

    @Test
    public void testRecordAuditEventStartString() {
        eelfLogger.recordAuditEventStart("eventId");
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));
    }

    @Test
    public void testRecordAuditEventStartUuid() {
        UUID uuid = UUID.randomUUID();
        eelfLogger.recordAuditEventStart(uuid);
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));
    }

    @Test
    public void testRecordAuditEventEndStringStringString() {
        eelfLogger.recordAuditEventStart("eventId");
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));

        eelfLogger.recordAuditEventEnd("eventId", "rule", "policyVersion");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));
    }

    @Test
    public void testRecordAuditEventEndUuidStringString() {
        UUID uuid = UUID.randomUUID();
        eelfLogger.recordAuditEventStart(uuid);;
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));

        eelfLogger.recordAuditEventEnd(uuid, "rule", "policyVersion");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));
    }

    @Test
    public void testRecordAuditEventEndStringString() {
        eelfLogger.recordAuditEventStart("eventId");
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));

        eelfLogger.recordAuditEventEnd("eventId", "rule");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));
    }

    @Test
    public void testRecordAuditEventEndUuidString() {
        UUID uuid = UUID.randomUUID();
        eelfLogger.recordAuditEventStart(uuid);;
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));

        eelfLogger.recordAuditEventEnd(uuid, "rule");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));
    }

    @Test
    public void testRecordMetricEventStringString() {
        eelfLogger.recordMetricEvent("eventId", "str1");
        assertEquals("eventId", MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    public void testRecordMetricEventUuidString() {
        UUID uuid = UUID.randomUUID();
        eelfLogger.recordMetricEvent(uuid, "str2");
        assertEquals(uuid.toString(), MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    public void testMetrics() {
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
    public void testErrorMessageCodesThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        eelfLogger.error(MessageCodes.GENERAL_ERROR, new NullPointerException(), "str1", "str2");
        Mockito.verify(mockLogger).error((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    public void testErrorMessageCodesStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        eelfLogger.error(MessageCodes.GENERAL_ERROR, "str1", "str2");
        Mockito.verify(mockLogger).error(MessageCodes.GENERAL_ERROR, "str1", "str2");

    }

    @Test
    public void testPostMdcInfoForEventString() {
        eelfLogger.postMdcInfoForEvent("transactionId");
        assertEquals("transactionId", MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    public void testWarnMessageCodesStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.warn(MessageCodes.GENERAL_ERROR, "str1", "str2");
        Mockito.verify(mockLogger).warn(MessageCodes.GENERAL_ERROR, "str1", "str2");
    }

    @Test
    public void testWarnMessageCodesThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        eelfLogger.warn(MessageCodes.GENERAL_ERROR, new NullPointerException(), "str1", "str2");
        Mockito.verify(mockLogger).warn((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));

    }

    @Test
    public void testPostMdcInfoForTriggeredRule() {
        eelfLogger.postMdcInfoForTriggeredRule("transactionId");
        assertEquals("transactionId", MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    public void testPostMDdcInfoForEventObject() {
        eelfLogger.postMdcInfoForEvent(1);
        assertEquals("1", MDC.get(MDC_KEY_REQUEST_ID));
    }

}
