/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.common.utils.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.logging.LoggerMarkerFilter.AuditLoggerMarkerFilter;
import org.onap.policy.common.utils.logging.LoggerMarkerFilter.MetricLoggerMarkerFilter;
import org.onap.policy.common.utils.logging.LoggerMarkerFilter.SecurityLoggerMarkerFilter;
import org.onap.policy.common.utils.logging.LoggerMarkerFilter.TransactionLoggerMarkerFilter;
import org.slf4j.Marker;

class LoggerMarkerFilterTest {

    private ILoggingEvent mockEvent;
    private Marker mockMarker;

    @BeforeEach
    void setUp() {
        mockEvent = mock(ILoggingEvent.class);
        mockMarker = mock(Marker.class);
    }

    @Test
    void testDecideAcceptWithMetricMarker() {
        MetricLoggerMarkerFilter filter = new MetricLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(Collections.singletonList(LoggerUtils.METRIC_LOG_MARKER));
        filter.start();

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.ACCEPT, reply, "The filter should accept the event with the METRIC_LOG_MARKER.");
    }

    @Test
    void testDecideDenyWithoutMetricMarker() {
        MetricLoggerMarkerFilter filter = new MetricLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(Collections.singletonList(mockMarker));
        filter.start();

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.DENY, reply, "The filter should deny the event without the METRIC_LOG_MARKER.");
    }

    @Test
    void testDecideAcceptWithSecurityMarker() {
        SecurityLoggerMarkerFilter filter = new SecurityLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(Collections.singletonList(LoggerUtils.SECURITY_LOG_MARKER));
        filter.start();

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.ACCEPT, reply, "The filter should accept the event with the SECURITY_LOG_MARKER.");
    }

    @Test
    void testDecideDenyWithoutSecurityMarker() {
        SecurityLoggerMarkerFilter filter = new SecurityLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(Collections.singletonList(mockMarker));
        filter.start();

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.DENY, reply, "The filter should deny the event without the SECURITY_LOG_MARKER.");
    }

    @Test
    void testDecideAcceptWithAuditMarker() {
        AuditLoggerMarkerFilter filter = new AuditLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(Collections.singletonList(LoggerUtils.AUDIT_LOG_MARKER));
        filter.start();

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.ACCEPT, reply, "The filter should accept the event with the AUDIT_LOG_MARKER.");
    }

    @Test
    void testDecideDenyWithoutAuditMarker() {
        AuditLoggerMarkerFilter filter = new AuditLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(Collections.singletonList(mockMarker));
        filter.start();

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.DENY, reply, "The filter should deny the event without the AUDIT_LOG_MARKER.");
    }

    @Test
    void testDecideAcceptWithTransactionMarker() {
        TransactionLoggerMarkerFilter filter = new TransactionLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(Collections.singletonList(LoggerUtils.TRANSACTION_LOG_MARKER));
        filter.start();

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.ACCEPT, reply, "The filter should accept the event with the TRANSACTION_LOG_MARKER.");
    }

    @Test
    void testDecideDenyWithoutTransactionMarker() {
        TransactionLoggerMarkerFilter filter = new TransactionLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(Collections.singletonList(mockMarker));
        filter.start();

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.DENY, reply, "The filter should deny the event without the TRANSACTION_LOG_MARKER.");
    }

    @Test
    void testDecideDenyWhenNotStarted() {
        MetricLoggerMarkerFilter filter = new MetricLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(Collections.singletonList(LoggerUtils.METRIC_LOG_MARKER));
        // Filter is not started

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.DENY, reply, "The filter should deny the event if the filter is not started.");
    }

    @Test
    void testDecideDenyWithNullEvent() {
        MetricLoggerMarkerFilter filter = new MetricLoggerMarkerFilter();
        filter.start();

        FilterReply reply = filter.decide(null);
        assertEquals(FilterReply.DENY, reply, "The filter should deny if the event is null.");
    }

    @Test
    void testDecideDenyWithNullMarkerList() {
        MetricLoggerMarkerFilter filter = new MetricLoggerMarkerFilter();
        when(mockEvent.getMarkerList()).thenReturn(null);
        filter.start();

        FilterReply reply = filter.decide(mockEvent);
        assertEquals(FilterReply.DENY, reply, "The filter should deny if the marker list is null.");
    }
}
