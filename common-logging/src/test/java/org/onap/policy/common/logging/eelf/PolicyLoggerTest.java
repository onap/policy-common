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


package org.onap.policy.common.logging.eelf;

import static com.att.eelf.configuration.Configuration.MDC_ALERT_SEVERITY;
import static com.att.eelf.configuration.Configuration.MDC_INSTANCE_UUID;
import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static com.att.eelf.configuration.Configuration.MDC_REMOTE_HOST;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_FQDN;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_IP_ADDRESS;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_INSTANCE_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.PARTNER_NAME;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.RESPONSE_CODE;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.RESPONSE_DESCRIPTION;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.SERVER_NAME;
import static org.onap.policy.common.logging.eelf.OnapConfigProperties.STATUS_CODE;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.test.util.ReflectionTestUtils;

class PolicyLoggerTest {

    /**
     * Perform set up for test cases.
     */
    @BeforeEach
    void setUp() {
        Properties properties = new Properties();
        properties.setProperty("policy.component", "XACML");
        PolicyLogger.init(properties);
    }

    @Test
    void testSetAndGetDebugLevelLevel() {
        PolicyLogger.setDebugLevel(Level.INFO);
        assertEquals(Level.INFO, PolicyLogger.getDebugLevel());
        PolicyLogger.setDebugLevel(Level.DEBUG);
        assertEquals(Level.DEBUG, PolicyLogger.getDebugLevel());
    }

    @Test
    void testSetAndGetAuditLevelLevel() {
        PolicyLogger.setAuditLevel(Level.INFO);
        assertEquals(Level.INFO, PolicyLogger.getAuditLevel());
        PolicyLogger.setAuditLevel(Level.DEBUG);
        assertEquals(Level.DEBUG, PolicyLogger.getAuditLevel());
    }

    @Test
    void testSetAndGetMetricsLevelLevel() {
        PolicyLogger.setMetricsLevel(Level.INFO);
        assertEquals(Level.INFO, PolicyLogger.getMetricsLevel());
        PolicyLogger.setMetricsLevel(Level.DEBUG);
        assertEquals(Level.DEBUG, PolicyLogger.getMetricsLevel());
    }

    @Test
    void testSetAndGetErrorLevelLevel() {
        PolicyLogger.setErrorLevel(Level.INFO);
        assertEquals(Level.INFO, PolicyLogger.getErrorLevel());
        PolicyLogger.setErrorLevel(Level.DEBUG);
        assertEquals(Level.DEBUG, PolicyLogger.getErrorLevel());
    }

    @Test
    void testSetAndGetClassname() {
        assertEquals("ClassName", PolicyLogger.getClassname());
        PolicyLogger.setClassname("PolicyLoggerTest");
        assertEquals("PolicyLoggerTest", PolicyLogger.getClassname());
    }

    @Test
    void testPostMdcInfoForEventString() {
        PolicyLogger.postMdcInfoForEvent("transactionId");

        assertEquals("", MDC.get(MDC_REMOTE_HOST));
        assertEquals("transactionId", MDC.get(MDC_KEY_REQUEST_ID));
        assertEquals("Policy.xacmlPdp", MDC.get(MDC_SERVICE_NAME));
        assertEquals("Policy.xacmlPdp.event", MDC.get(MDC_SERVICE_INSTANCE_ID));
        assertEquals("", MDC.get(MDC_INSTANCE_UUID));
        assertEquals("", MDC.get(MDC_ALERT_SEVERITY));
        assertEquals("N/A", MDC.get(PARTNER_NAME));
        assertEquals("COMPLETE", MDC.get(STATUS_CODE));
        assertEquals("N/A", MDC.get(RESPONSE_CODE));
        assertEquals("N/A", MDC.get(RESPONSE_DESCRIPTION));
    }

    @Test
    void testPostMdcInfoForEventStringDrools() {
        Properties properties = new Properties();
        properties.setProperty("policy.component", "DROOLS");
        PolicyLogger.init(properties);

        PolicyLogger.postMdcInfoForEvent("transactionId");

        assertEquals("transactionId", MDC.get(MDC_KEY_REQUEST_ID));
        assertEquals("Policy.droolsPdp", MDC.get(MDC_SERVICE_NAME));
        assertEquals("Policy.droolsPdp.event", MDC.get(MDC_SERVICE_INSTANCE_ID));
    }

    @Test
    void testSetAndGetTransId() {
        PolicyLogger.setTransId("123456");
        assertEquals("123456", PolicyLogger.getTransId());
    }

    @Test
    void testPostMdcInfoForEventObject() {
        PolicyLogger.postMdcInfoForEvent(1);

        assertEquals("", MDC.get(MDC_REMOTE_HOST));
        assertEquals("1", MDC.get(MDC_KEY_REQUEST_ID));
        assertEquals("Policy.xacmlPdp", MDC.get(MDC_SERVICE_NAME));
        assertEquals("Policy.xacmlPdp.event", MDC.get(MDC_SERVICE_INSTANCE_ID));
        assertEquals("", MDC.get(MDC_INSTANCE_UUID));
        assertEquals("", MDC.get(MDC_ALERT_SEVERITY));
        assertEquals("N/A", MDC.get(PARTNER_NAME));
        assertEquals("COMPLETE", MDC.get(STATUS_CODE));
        assertEquals("N/A", MDC.get(RESPONSE_CODE));
        assertEquals("N/A", MDC.get(RESPONSE_DESCRIPTION));
    }

    @Test
    void testPostMdcInfoForTriggeredRule() {
        PolicyLogger.postMdcInfoForTriggeredRule("transactionId");

        assertEquals("", MDC.get(MDC_REMOTE_HOST));
        assertEquals("transactionId", MDC.get(MDC_KEY_REQUEST_ID));
        assertEquals("Policy.droolsPdp", MDC.get(MDC_SERVICE_NAME));
        assertEquals("", MDC.get(MDC_SERVICE_INSTANCE_ID));
        assertNotNull(MDC.get(MDC_SERVER_FQDN));
        assertNotNull(MDC.get(MDC_SERVER_IP_ADDRESS));
        assertEquals("", MDC.get(MDC_INSTANCE_UUID));
        assertEquals("", MDC.get(MDC_ALERT_SEVERITY));
        assertEquals("COMPLETE", MDC.get(STATUS_CODE));
    }

    @Test
    void testPostMdcUuidForTriggeredRule() {
        PolicyLogger.postMdcUuidForTriggeredRule(1);

        assertEquals("", MDC.get(MDC_REMOTE_HOST));
        assertEquals("1", MDC.get(MDC_KEY_REQUEST_ID));
        assertEquals("Policy.droolsPdp", MDC.get(MDC_SERVICE_NAME));
        assertEquals("", MDC.get(MDC_SERVICE_INSTANCE_ID));
        assertNotNull(MDC.get(MDC_SERVER_FQDN));
        assertNotNull(MDC.get(MDC_SERVER_IP_ADDRESS));
        assertEquals("", MDC.get(MDC_INSTANCE_UUID));
        assertEquals("", MDC.get(MDC_ALERT_SEVERITY));
        assertEquals("COMPLETE", MDC.get(STATUS_CODE));
    }

    @Test
    void testInfoMessageCodesStringStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.info(MessageCodes.ERROR_DATA_ISSUE, "str1", "str2");
        Mockito.verify(mockLogger).info(MessageCodes.ERROR_DATA_ISSUE, "str2");
    }

    @Test
    void testInfoStringString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.info("str1", "str2");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        PolicyLogger.info("str1", "str2");
        Mockito.verify(mockLogger).info(MessageCodes.GENERAL_INFO, "str2");
    }

    @Test
    void testInfoObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.info("str1");
        Mockito.verify(mockLogger).info(MessageCodes.GENERAL_INFO, "str1");
    }

    @Test
    void testInfoMessageCodesThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.info(MessageCodes.ERROR_DATA_ISSUE, new NullPointerException(), "str1", "str2");
        Mockito.verify(mockLogger).info((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    void testInfoMessageCodesStringThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.info(MessageCodes.ERROR_DATA_ISSUE, "PolicyLoggerTest", new NullPointerException(), "str1",
                "str2");
        Mockito.verify(mockLogger).info((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    void testWarnMessageCodesStringStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.warn(MessageCodes.ERROR_DATA_ISSUE, "str1");
        Mockito.verify(mockLogger).warn(MessageCodes.ERROR_DATA_ISSUE);
    }

    @Test
    void testWarnStringString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.warn("str1", "str2");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isWarnEnabled()).thenReturn(true);
        PolicyLogger.warn("str1", "str2");
        Mockito.verify(mockLogger).warn(MessageCodes.GENERAL_INFO, "str2");
    }

    @Test
    void testWarnObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.warn(1);
        Mockito.verify(mockLogger).warn(MessageCodes.GENERAL_WARNING, "1");
    }

    @Test
    void testWarnMessageCodesThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.warn(MessageCodes.ERROR_DATA_ISSUE, new NullPointerException(), "str1", "str2");
        Mockito.verify(mockLogger).warn((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    void testWarnMessageCodesStringThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.warn(MessageCodes.ERROR_DATA_ISSUE, "PolicyLoggerTest", new NullPointerException(), "str1",
                "str2");
        Mockito.verify(mockLogger).warn((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    void testWarnString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.warn("str1");
        Mockito.verify(mockLogger).warn(MessageCodes.GENERAL_WARNING, "str1");
    }

    @Test
    void testErrorStringString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        PolicyLogger.error("str1", "str2");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isErrorEnabled()).thenReturn(true);
        PolicyLogger.error("str1", "str2");
        Mockito.verify(mockLogger).error(MessageCodes.GENERAL_ERROR, "str2");
        assertEquals("500", MDC.get("ErrorCode"));
        assertEquals("This is a general error message during the process. Please check the error message for detail "
                + "information", MDC.get("ErrorDescription"));
    }

    @Test
    void testErrorString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        PolicyLogger.error("str1");
        Mockito.verify(mockLogger).error(MessageCodes.GENERAL_ERROR, "str1");
        assertEquals("ERROR", MDC.get("ErrorCategory"));
        assertEquals("500", MDC.get("ErrorCode"));
        assertEquals("This is a general error message during the process. Please check the error message for detail "
                + "information", MDC.get("ErrorDescription"));
    }

    @Test
    public void testErrorObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        PolicyLogger.error(1);
        Mockito.verify(mockLogger).error(MessageCodes.GENERAL_ERROR, "1");
        assertEquals("ERROR", MDC.get("ErrorCategory"));
        assertEquals("500", MDC.get("ErrorCode"));
        assertEquals("This is a general error message during the process. Please check the error message for detail "
                + "information", MDC.get("ErrorDescription"));
    }

    @Test
    public void testErrorMessageCodesThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        PolicyLogger.error(MessageCodes.ERROR_DATA_ISSUE, new NullPointerException(), "str1", "str2");
        Mockito.verify(mockLogger).error((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    public void testErrorMessageCodesStringThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        PolicyLogger.error(MessageCodes.ERROR_DATA_ISSUE, "PolicyLoggerTest", new NullPointerException(), "str1",
                "str2");
        Mockito.verify(mockLogger).error((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    public void testErrorMessageCodesStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        PolicyLogger.error(MessageCodes.ERROR_DATA_ISSUE, "str1", "str2");
        Mockito.verify(mockLogger).error(MessageCodes.ERROR_DATA_ISSUE, "str1", "str2");
    }

    @Test
    public void testDebugMessageCodesStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.debug(MessageCodes.ERROR_DATA_ISSUE, "str1", "str2");
        Mockito.verify(mockLogger).debug(MessageCodes.ERROR_DATA_ISSUE, "str1", "str2");
    }

    @Test
    public void testDebugStringString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.debug("str1", "str2");
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(true);
        PolicyLogger.debug("str1", "str2");
        Mockito.verify(mockLogger).debug(MessageCodes.GENERAL_INFO, "str2");
    }

    @Test
    public void testDebugString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.debug("str1");
        Mockito.verify(mockLogger).debug("str1");
    }

    @Test
    public void testDebugObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.debug(1);
        Mockito.verify(mockLogger).debug("{}", 1);
    }

    @Test
    public void testAuditStringObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "auditLogger", mockLogger);
        PolicyLogger.audit("PolicyLoggerTest", 1);
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        PolicyLogger.audit("PolicyLoggerTest", 1);
        assertEquals("PolicyLoggerTest", MDC.get("ClassName"));
        assertEquals("COMPLETE", MDC.get("StatusCode"));
        Mockito.verify(mockLogger).info("{}", "1");
    }

    @Test
    public void testAuditObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "auditLogger", mockLogger);
        PolicyLogger.audit(1);
        assertEquals("", MDC.get("ClassName"));
        assertEquals("COMPLETE", MDC.get("StatusCode"));
        Mockito.verify(mockLogger).info("{}", 1);
    }

    @Test
    public void testDebugMessageCodesThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.debug(MessageCodes.ERROR_DATA_ISSUE, new NullPointerException(), "str1", "str2");
        Mockito.verify(mockLogger).debug((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    public void testDebugMessageCodesStringThrowableStringArray() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.debug(MessageCodes.ERROR_DATA_ISSUE, "PolicyLoggerTest", new NullPointerException(), "str1",
                "str2");
        Mockito.verify(mockLogger).debug((MessageCodes) Mockito.any(),
                Mockito.startsWith("str1:str2:java.lang.NullPointerException"));
    }

    @Test
    public void testIsDebugEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isDebugEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(PolicyLogger.isDebugEnabled());
        assertTrue(PolicyLogger.isDebugEnabled());
    }

    @Test
    public void testIsErrorEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "errorLogger", mockLogger);
        Mockito.when(mockLogger.isErrorEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(PolicyLogger.isErrorEnabled());
        assertTrue(PolicyLogger.isErrorEnabled());
    }

    @Test
    public void testIsWarnEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isWarnEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(PolicyLogger.isWarnEnabled());
        assertTrue(PolicyLogger.isWarnEnabled());
    }

    @Test
    public void testIsInfoEnabled1() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(PolicyLogger.isInfoEnabled1());
        assertTrue(PolicyLogger.isInfoEnabled1());
    }

    @Test
    public void testIsAuditEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(PolicyLogger.isAuditEnabled());
        assertTrue(PolicyLogger.isAuditEnabled());
    }

    @Test
    public void testIsInfoEnabled() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(false).thenReturn(true);
        assertFalse(PolicyLogger.isInfoEnabled());
        assertTrue(PolicyLogger.isInfoEnabled());
    }

    @Test
    public void testTraceStringString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.trace("str1", "str2");
        Mockito.verify(mockLogger).trace(MessageCodes.GENERAL_INFO, "str2");
    }

    @Test
    public void testTraceObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "debugLogger", mockLogger);
        PolicyLogger.trace(1);
        Mockito.verify(mockLogger).trace("{}", 1);
    }

    @Test
    public void testRecordAuditEventStartAndEnd() {
        PolicyLogger.recordAuditEventStart("eventId");
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));

        PolicyLogger.recordAuditEventEnd("eventId", "rule");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));
    }

    @Test
    public void testRecordAuditEventStartAndEndUuid() {
        UUID uuid = UUID.randomUUID();
        PolicyLogger.recordAuditEventStart(uuid);;
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));

        PolicyLogger.recordAuditEventEnd(uuid, "rule");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));
    }


    @Test
    public void testRecordAuditEventEndStringStringString() {
        PolicyLogger.recordAuditEventStart("eventId");
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));

        PolicyLogger.recordAuditEventEnd("eventId", "rule", "policyVersion");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));
    }

    @Test
    public void testRecordAuditEventEndUuidStringString() {
        UUID uuid = UUID.randomUUID();
        PolicyLogger.recordAuditEventStart(uuid);;
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));

        PolicyLogger.recordAuditEventEnd(uuid, "rule", "policyVersion");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));
    }


    @Test
    public void testCreatAuditEventTrackingRecordStringStringString() {
        PolicyLogger.recordAuditEventStart("eventId");
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));

        PolicyLogger.creatAuditEventTrackingRecord("eventId", "rule", "policyVersion");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get("eventId"));
    }

    @Test
    public void testCreatAuditEventTrackingRecordUuidString() {
        UUID uuid = UUID.randomUUID();
        PolicyLogger.recordAuditEventStart(uuid);;
        assertNotNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));

        PolicyLogger.creatAuditEventTrackingRecord(uuid, "rule");
        assertNull(PolicyLogger.getEventTracker().getEventInfo().get(uuid.toString()));
    }

    @Test
    public void testRecordAuditEventStartToEnd() {
        PolicyLogger.recordAuditEventStartToEnd("eventId", "rule", Instant.now(), Instant.now(), "policyVersion");
        assertEquals("eventId", MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    public void testRecordMetricEventStringString() {
        PolicyLogger.recordMetricEvent("eventId", "str1");
        assertEquals("eventId", MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    public void testRecordMetricEventStringStringString() {
        PolicyLogger.recordMetricEvent("eventId", "PolicyLoggerTest", "str1");
        assertEquals("eventId", MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    public void testRecordMetricEventUuidString() {
        UUID uuid = UUID.randomUUID();
        PolicyLogger.recordMetricEvent(uuid, "str1");
        assertEquals(uuid.toString(), MDC.get(MDC_KEY_REQUEST_ID));
    }

    @Test
    public void testRecordMetricEventString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "metricsLogger", mockLogger);
        PolicyLogger.recordMetricEvent("eventId");
        Mockito.verify(mockLogger).info(Mockito.eq(MessageCodes.RULE_METRICS_INFO), Mockito.anyString(),
                Mockito.eq("eventId"));
    }

    @Test
    public void testMetricsString() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "metricsLogger", mockLogger);
        PolicyLogger.metrics("str1");
        Mockito.verify(mockLogger).info(Mockito.eq(MessageCodes.RULE_METRICS_INFO), Mockito.anyString(),
                Mockito.eq("str1"));
    }

    @Test
    public void testMetricsStringObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "metricsLogger", mockLogger);
        PolicyLogger.metrics("PolicyLoggerTest", 1);
        Mockito.verify(mockLogger, never()).info(Mockito.anyString(), Mockito.anyString());
        Mockito.when(mockLogger.isInfoEnabled()).thenReturn(true);
        PolicyLogger.metrics("PolicyLoggerTest", 1);
        Mockito.verify(mockLogger).info(Mockito.eq(MessageCodes.RULE_METRICS_INFO), Mockito.anyString(),
                Mockito.eq("1"));
    }

    @Test
    public void testMetricsObject() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "metricsLogger", mockLogger);
        PolicyLogger.metrics(1);
        Mockito.verify(mockLogger).info(Mockito.eq(MessageCodes.RULE_METRICS_INFO), Mockito.anyString(),
                Mockito.eq("1"));
    }

    @Test
    public void testMetricsPrintln() {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        ReflectionTestUtils.setField(PolicyLogger.class, "metricsLogger", mockLogger);
        PolicyLogger.metricsPrintln("str1");
        Mockito.verify(mockLogger).info("str1");
    }

    @Test
    public void testInitNullProperties() {
        assertThatCode(() -> PolicyLogger.init(null)).doesNotThrowAnyException();
    }

    @Test
    public void testInit() {
        Properties properties = new Properties();
        properties.setProperty("override.logback.level.setup", "true");
        properties.setProperty("metricsLogger.level", "OFF");
        properties.setProperty("error.level", "OFF");
        properties.setProperty("audit.level", "OFF");
        properties.setProperty("timer.delay.time", "0");
        properties.setProperty("check.interval", "0");
        properties.setProperty("event.expired.time", "0");
        properties.setProperty("concurrentHashMap.limit", "0");
        properties.setProperty("stop.check.point", "0");
        properties.setProperty("logger.property", "LOG4J");

        assertThatCode(() -> PolicyLogger.init(properties)).doesNotThrowAnyException();
    }

    @Test
    public void testSetDebugLevelString() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setDebugLevel("TRACE");
        assertEquals(Level.TRACE, PolicyLogger.getDebugLevel());
    }

    @Test
    public void testSetErrorLevelStringOff() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setErrorLevel("OFF");
        assertEquals(Level.OFF, PolicyLogger.getErrorLevel());
    }

    @Test
    public void testSetErrorLevelStringOther() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setErrorLevel("INFO");
        assertEquals(Level.ERROR, PolicyLogger.getErrorLevel());
    }

    @Test
    public void testSetMetricsLevelStringOff() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setMetricsLevel("OFF");
        assertEquals(Level.OFF, PolicyLogger.getMetricsLevel());
    }

    @Test
    public void testSetMetricsLevelStringOther() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setMetricsLevel("ERROR");
        assertEquals(Level.INFO, PolicyLogger.getMetricsLevel());
    }

    @Test
    public void testSetAuditLevelStringOff() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setAuditLevel("OFF");
        assertEquals(Level.OFF, PolicyLogger.getAuditLevel());
    }

    @Test
    public void testSetAuditLevelStringOther() {
        PolicyLogger.setOverrideLogbackLevel(true);
        PolicyLogger.setAuditLevel("ERROR");
        assertEquals(Level.INFO, PolicyLogger.getAuditLevel());
    }

    @Test
    public void testSetAndIsOverrideLogbackLevel() {
        PolicyLogger.setOverrideLogbackLevel(false);
        assertFalse(PolicyLogger.isOverrideLogbackLevel());
        PolicyLogger.setOverrideLogbackLevel(true);
        assertTrue(PolicyLogger.isOverrideLogbackLevel());
    }

    @Test
    public void testSetServerInfo() {
        PolicyLogger.setServerInfo("serverHost", "serverPort");
        assertEquals("serverHost:serverPort", MDC.get(SERVER_NAME));
    }

}
