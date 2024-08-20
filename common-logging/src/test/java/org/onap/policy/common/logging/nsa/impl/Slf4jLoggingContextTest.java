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

package org.onap.policy.common.logging.nsa.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.policy.common.logging.nsa.LoggingContext;
import org.onap.policy.common.logging.nsa.LoggingContextFactory;
import org.slf4j.MDC;

class Slf4jLoggingContextTest {

    @Mock
    static MDC mdcMock;

    static Slf4jLoggingContext context;

    @BeforeAll
    static void setUp() {
        mdcMock = mock(MDC.class);
        LoggingContextFactory.Builder builder = new LoggingContextFactory.Builder();
        LoggingContext loggingContext = builder.build();
        context = new Slf4jLoggingContext(loggingContext);
    }

    @Test
    void testPut() {
        assertThatCode(() -> context.put("testKey", "testValue")).doesNotThrowAnyException();
        assertThatCode(() -> context.put("testKey", 123456)).doesNotThrowAnyException();
    }

    @Test
    void testGet() {
        assertThatCode(() -> context.get("testKey", 123456)).doesNotThrowAnyException();
        assertThatCode(() -> context.get("testKey", "testValue")).doesNotThrowAnyException();
    }
}
