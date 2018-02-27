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

import static org.junit.Assert.*;
import java.util.UUID;
import org.junit.Test;
import org.onap.policy.common.logging.eelf.MessageCodes;

public class EelfLoggerTest {
    
    EelfLogger eelfLogger = new EelfLogger("EelfLoggerTest", "transactionId");

    @Test
    public void testEelfLoggerClassOfQ() {
        new EelfLogger(this.getClass());
    }

    @Test
    public void testEelfLoggerString() {
        new EelfLogger("EelfLoggerTest");
    }

    @Test
    public void testEelfLoggerClassOfQBoolean() {
        new EelfLogger(this.getClass(), true);
    }

    @Test
    public void testEelfLoggerStringBoolean() {
        new EelfLogger("EelfLoggerTest", true);
    }

    @Test
    public void testEelfLoggerClassOfQString() {
        new EelfLogger(this.getClass(), "transactionId");
    }

    @Test
    public void testEelfLoggerStringString() {
        new EelfLogger("EelfLoggerTest", "transactionId");
    }

    @Test
    public void testSetAndGetTransId() {
        assertEquals("transactionId", eelfLogger.getTransId());
        eelfLogger.setTransId("transactionId2");
        assertEquals("transactionId2", eelfLogger.getTransId());
    }

    @Test
    public void testDebugObject() {
        eelfLogger.debug("message");
    }

    @Test
    public void testErrorObject() {
        eelfLogger.error("message");
    }

    @Test
    public void testInfoObject() {
        eelfLogger.info("message");
    }

    @Test
    public void testWarnObject() {
        eelfLogger.warn("message");
    }

    @Test
    public void testTraceObject() {
        eelfLogger.trace("message");
    }

    @Test
    public void testIsDebugEnabled() {
        eelfLogger.isDebugEnabled();
    }

    @Test
    public void testIsInfoEnabled() {
        eelfLogger.isInfoEnabled();
    }

    @Test
    public void testIsWarnEnabled() {
        eelfLogger.isWarnEnabled();
    }

    @Test
    public void testIsErrorEnabled() {
        eelfLogger.isErrorEnabled();
    }

    @Test
    public void testIsAuditEnabled() {
        eelfLogger.isAuditEnabled();
    }

    @Test
    public void testIsMetricsEnabled() {
        eelfLogger.isMetricsEnabled();
    }

    @Test
    public void testIsTraceEnabled() {
        eelfLogger.isTraceEnabled();
    }

    @Test
    public void testAuditObject() {
        eelfLogger.isAuditEnabled();
    }

    @Test
    public void testDebugObjectThrowable() {
        eelfLogger.debug("message", new NullPointerException());
    }

    @Test
    public void testErrorObjectThrowable() {
        eelfLogger.error("message", new NullPointerException());
    }

    @Test
    public void testInfoObjectThrowable() {
        eelfLogger.info("message", new NullPointerException());
    }

    @Test
    public void testWarnObjectThrowable() {
        eelfLogger.warn("message", new NullPointerException());
    }

    @Test
    public void testTraceObjectThrowable() {
        eelfLogger.trace("message", new NullPointerException());
    }

    @Test
    public void testAuditObjectThrowable() {
        eelfLogger.audit("message", new NullPointerException());
    }

    @Test
    public void testRecordAuditEventStartString() {
        eelfLogger.recordAuditEventStart("eventId");
    }

    @Test
    public void testRecordAuditEventStartUUID() {
        eelfLogger.recordAuditEventStart(UUID.randomUUID());
    }

    @Test
    public void testRecordAuditEventEndStringStringString() {
        eelfLogger.recordAuditEventEnd("eventId", "rule", "policyVersion");
    }

    @Test
    public void testRecordAuditEventEndUUIDStringString() {
        eelfLogger.recordAuditEventEnd(UUID.randomUUID(), "rule", "policyVersion");
    }

    @Test
    public void testRecordAuditEventEndStringString() {
        eelfLogger.recordAuditEventEnd("eventId", "rule");
    }

    @Test
    public void testRecordAuditEventEndUUIDString() {
        eelfLogger.recordAuditEventEnd(UUID.randomUUID(), "rule");
    }

    @Test
    public void testRecordMetricEventStringString() {
        eelfLogger.recordMetricEvent("eventId", "str1");
    }

    @Test
    public void testRecordMetricEventUUIDString() {
        eelfLogger.recordMetricEvent(UUID.randomUUID(), "str2");
    }

    @Test
    public void testMetrics() {
        eelfLogger.metrics(1);
    }

    @Test
    public void testErrorMessageCodesThrowableStringArray() {
        eelfLogger.error(MessageCodes.GENERAL_ERROR, new NullPointerException(), "str1", "str2");
    }

    @Test
    public void testErrorMessageCodesStringArray() {
        eelfLogger.error(MessageCodes.GENERAL_ERROR, "str1", "str2");
    }

    @Test
    public void testPostMDCInfoForEventString() {
        eelfLogger.postMDCInfoForEvent("transactionId");
    }

    @Test
    public void testWarnMessageCodesStringArray() {
        eelfLogger.warn(MessageCodes.GENERAL_ERROR, "str1", "str2");
    }

    @Test
    public void testWarnMessageCodesThrowableStringArray() {
        eelfLogger.warn(MessageCodes.GENERAL_ERROR, new NullPointerException(), "str1", "str2");
    }

    @Test
    public void testPostMDCInfoForTriggeredRule() {
        eelfLogger.postMDCInfoForTriggeredRule("transactionId");
    }

    @Test
    public void testPostMDCInfoForEventObject() {
        eelfLogger.postMDCInfoForEvent(1);
    }

}
