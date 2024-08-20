/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2024 Nordix Foundation.
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

package org.onap.policy.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OnapLoggingContextTest {

    private OnapLoggingContext loggingContext;

    @BeforeEach
    void setUp() {
        loggingContext = new OnapLoggingContext();
    }

    @Test
    void testSetAndGetRequestId() {
        String requestId = "request123";
        loggingContext.setRequestId(requestId);
        assertEquals(requestId, loggingContext.getRequestId());
    }

    @Test
    void testSetAndGetServiceInstanceId() {
        String serviceInstanceId = "service123";
        loggingContext.setServiceInstanceId(serviceInstanceId);
        assertEquals(serviceInstanceId, loggingContext.getServiceInstanceId());
    }

    @Test
    void testTransactionTimeCalculations() throws InterruptedException {
        loggingContext.transactionStarted();
        Thread.sleep(100); // NOSONAR simulate some processing time
        loggingContext.transactionEnded();

        long elapsedTime = loggingContext.getTransactionElapsedTime();
        assertEquals(0, loggingContext.getTransactionBeginTimestamp());
        assertEquals(0, loggingContext.getTransactionEndTimestamp());
        assertEquals(100, elapsedTime, 10); // assert elapsed time is around 100ms (tolerance of 10ms)
    }

    @Test
    void testMetricTimeCalculations() throws InterruptedException {
        loggingContext.metricStarted();
        Thread.sleep(200); // NOSONAR simulate some metric processing time
        loggingContext.metricEnded();

        long elapsedTime = loggingContext.getMetricElapsedTime();
        assertEquals(0, loggingContext.getMetricBeginTimestamp());
        assertEquals(0, loggingContext.getMetricEndTimestamp());
        assertEquals(200, elapsedTime, 20); // assert elapsed time is around 200ms (tolerance of 10ms)
    }

    @Test
    void testBaseContextConstructor() {
        loggingContext.setRequestId("request123");
        loggingContext.setServiceInstanceId("service123");

        OnapLoggingContext newLoggingContext = new OnapLoggingContext(loggingContext);

        assertEquals("request123", newLoggingContext.getRequestId());
        assertEquals("service123", newLoggingContext.getServiceInstanceId());
    }

    @Test
    void testSetAndGetCustomValues() {
        loggingContext.setServiceName("testService");
        loggingContext.setServerName("testServer");
        loggingContext.setPartnerName("testPartner");

        assertEquals("testService", loggingContext.getServiceName());
        assertEquals("testServer", loggingContext.getServerName());
        assertEquals("testPartner", loggingContext.getPartnerName());
    }
}
