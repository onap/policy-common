/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.att.eelf.configuration.EELFLogger.Level;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;

class SystemOutLoggerTest {

    SystemOutLogger systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");

    @Test
    void testSystemOutLoggerClassOfQ() {
        assertThatCode(() -> new SystemOutLogger(SystemOutLoggerTest.class)).doesNotThrowAnyException();
    }

    @Test
    void testSetAndGetTransId() {
        systemOutLogger.setTransId("transactionId");
        assertEquals("transactionId", systemOutLogger.getTransId());
    }

    @Test
    void testDebugObject() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            systemOutLogger.setTransId("transactionId");
            System.setOut(ps);
            systemOutLogger.debug("message");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : message"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    void testErrorObject() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            systemOutLogger.setTransId("transactionId");
            System.setOut(ps);
            systemOutLogger.error("message");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : message"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    void testInfoObject() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            systemOutLogger.setTransId("transactionId");
            System.setOut(ps);
            systemOutLogger.info("message");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : message"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    void testWarnObject() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            systemOutLogger.setTransId("transactionId");
            System.setOut(ps);
            systemOutLogger.warn("message");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : message"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    void testTraceObject() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            systemOutLogger.setTransId("transactionId");
            System.setOut(ps);
            systemOutLogger.trace("message");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : message"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    void testIsDebugEnabled() {
        PolicyLogger.setDebugLevel(Level.DEBUG);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertTrue(systemOutLogger.isDebugEnabled());
        PolicyLogger.setDebugLevel(Level.INFO);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertFalse(systemOutLogger.isDebugEnabled());
    }

    @Test
    void testIsWarnEnabled() {
        PolicyLogger.setDebugLevel(Level.WARN);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertTrue(systemOutLogger.isWarnEnabled());
        PolicyLogger.setDebugLevel(Level.OFF);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertFalse(systemOutLogger.isWarnEnabled());
    }

    @Test
    void testIsInfoEnabled() {
        PolicyLogger.setDebugLevel(Level.INFO);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertTrue(systemOutLogger.isInfoEnabled());
        PolicyLogger.setDebugLevel(Level.OFF);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertFalse(systemOutLogger.isInfoEnabled());
    }

    @Test
    void testIsErrorEnabled() {
        PolicyLogger.setErrorLevel(Level.ERROR);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertTrue(systemOutLogger.isErrorEnabled());
        PolicyLogger.setErrorLevel(Level.OFF);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertFalse(systemOutLogger.isErrorEnabled());
    }

    @Test
    void testIsAuditEnabled() {
        PolicyLogger.setAuditLevel(Level.INFO);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertTrue(systemOutLogger.isAuditEnabled());
        PolicyLogger.setAuditLevel(Level.OFF);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertFalse(systemOutLogger.isAuditEnabled());
    }

    @Test
    public void testIsMetricsEnabled() {
        PolicyLogger.setMetricsLevel(Level.INFO);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertTrue(systemOutLogger.isMetricsEnabled());
        PolicyLogger.setMetricsLevel(Level.OFF);
        systemOutLogger = new SystemOutLogger("SystemOutLoggerTest");
        assertFalse(systemOutLogger.isMetricsEnabled());
    }

    @Test
    public void testAuditObject() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            systemOutLogger.setTransId("transactionId");
            System.setOut(ps);
            systemOutLogger.audit("message");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : message"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testRecordAuditEventStartString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            systemOutLogger.setTransId("transactionId");
            System.setOut(ps);
            systemOutLogger.recordAuditEventStart("eventId");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : eventId"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testRecordAuditEventStartUuid() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            UUID uuid = UUID.randomUUID();
            System.setOut(ps);
            systemOutLogger.recordAuditEventStart(uuid);
            assertTrue(baos.toString().contains(uuid.toString()));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testRecordAuditEventEndStringStringString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.recordAuditEventEnd("eventId", "rule", "policyVersion");
            assertTrue(baos.toString().contains("SystemOutLoggerTest : eventId:rule:policyVersion"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testRecordAuditEventEndUuidStringString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            UUID uuid = UUID.randomUUID();
            System.setOut(ps);
            systemOutLogger.recordAuditEventEnd(uuid, "rule", "policyVersion");
            assertTrue(baos.toString().contains("SystemOutLoggerTest : " + uuid + ":rule:policyVersion"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testRecordAuditEventEndStringString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.recordAuditEventEnd("eventId", "rule");
            assertTrue(baos.toString().contains("SystemOutLoggerTest : eventId:rule"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testRecordAuditEventEndUuidString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            UUID uuid = UUID.randomUUID();
            System.setOut(ps);
            systemOutLogger.recordAuditEventEnd(uuid, "rule");
            assertTrue(baos.toString().contains("SystemOutLoggerTest : " + uuid + ":rule"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testRecordMetricEventStringString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.recordMetricEvent("eventId", "rule");
            assertTrue(baos.toString().contains("SystemOutLoggerTest : eventId:eventIdmessage:rule"), baos.toString());
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testRecordMetricEventUuidString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            UUID uuid = UUID.randomUUID();
            System.setOut(ps);
            systemOutLogger.recordMetricEvent(uuid, "str1");
            assertTrue(baos.toString().contains("SystemOutLoggerTest : " + uuid + ":str1"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testMetrics() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.metrics("message");
            assertTrue(baos.toString().contains("SystemOutLoggerTest : message"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testErrorMessageCodesThrowableStringArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.error(MessageCodes.ERROR_DATA_ISSUE, new NullPointerException(), "str1", "str2");
            assertTrue(baos.toString()
                    .contains("SystemOutLoggerTest : MessageCodes :" + MessageCodes.ERROR_DATA_ISSUE + "[str1, str2]"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testErrorMessageCodesStringArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.setTransId("transactionId");
            systemOutLogger.error(MessageCodes.ERROR_DATA_ISSUE, "str1", "str2");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : MessageCode:"
                    + MessageCodes.ERROR_DATA_ISSUE + "[str1, str2]"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testPostMdcInfoForEventString() {
        assertEquals("transactionId", systemOutLogger.postMdcInfoForEvent("transactionId"));
    }

    @Test
    public void testWarnMessageCodesStringArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.setTransId("transactionId");
            systemOutLogger.warn(MessageCodes.ERROR_DATA_ISSUE, "str1", "str2");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : MessageCodes:"
                    + MessageCodes.ERROR_DATA_ISSUE + "[str1, str2]"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testWarnMessageCodesThrowableStringArray() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.setTransId("transactionId");
            systemOutLogger.warn(MessageCodes.ERROR_DATA_ISSUE, new NullPointerException(), "str1", "str2");
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : MessageCodes:"
                    + MessageCodes.ERROR_DATA_ISSUE + "[str1, str2]"));
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testDebugObjectThrowable() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.setTransId("transactionId");
            systemOutLogger.debug("1", new NullPointerException());
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : 1:java.lang.NullPointerException"),
                baos.toString());
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testErrorObjectThrowable() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.setTransId("transactionId");
            systemOutLogger.error("1", new NullPointerException());
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : 1:java.lang.NullPointerException"),
                baos.toString());
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testInfoObjectThrowable() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.setTransId("transactionId");
            systemOutLogger.info("1", new NullPointerException());
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : 1:java.lang.NullPointerException"),
                baos.toString());
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testWarnObjectThrowable() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.setTransId("transactionId");
            systemOutLogger.warn("1", new NullPointerException());
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : 1:java.lang.NullPointerException"),
                baos.toString());
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testTraceObjectThrowable() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.setTransId("transactionId");
            systemOutLogger.trace(1, new NullPointerException());
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : 1:java.lang.NullPointerException"),
                baos.toString());
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testAuditObjectThrowable() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.setTransId("transactionId");
            systemOutLogger.audit("1", new NullPointerException());
            assertTrue(baos.toString().contains("transactionId|SystemOutLoggerTest : 1:java.lang.NullPointerException"),
                baos.toString());
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testIsTraceEnabled() {
        assertFalse(systemOutLogger.isTraceEnabled());
    }

    @Test
    public void testPostMdcInfoForTriggeredRule() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.postMdcInfoForTriggeredRule("transactionId");
            assertTrue(baos.toString().contains("transactionId"), baos.toString());
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

    @Test
    public void testPostMdcInfoForEventObject() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        try {
            System.setOut(ps);
            systemOutLogger.postMdcInfoForEvent(1);
            assertTrue(baos.toString().contains("1"), baos.toString());
        } finally {
            System.out.flush();
            System.setOut(old);
        }
    }

}
