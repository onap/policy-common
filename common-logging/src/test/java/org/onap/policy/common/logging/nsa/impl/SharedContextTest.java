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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.logging.nsa.LoggingContext;
import org.onap.policy.common.logging.nsa.LoggingContextFactory;
import org.onap.policy.common.logging.nsa.SharedLoggingContext;

class SharedContextTest {

    static SharedContext context;

    @BeforeAll
    static void setUp() {
        LoggingContextFactory.Builder builder = new LoggingContextFactory.Builder();
        LoggingContext loggingContext = builder.build();
        context = new SharedContext(loggingContext);
    }

    @Test
    void testPut() {
        assertThatCode(() -> context.put("testKey", "testValue")).doesNotThrowAnyException();
    }

    @Test
    void testTransferTo() {
        assertThatCode(() -> context.transferTo(new SharedLoggingContext() {
            @Override
            public void transferTo(SharedLoggingContext lc) {
                // do nothing
            }

            @Override
            public void put(String key, String value) {
                // do nothing
            }

            @Override
            public void put(String key, long value) {
                // do nothing
            }

            @Override
            public String get(String key, String defaultValue) {
                return "";
            }

            @Override
            public long get(String key, long defaultValue) {
                return 0;
            }
        })).doesNotThrowAnyException();
    }
}
