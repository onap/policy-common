/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.onap.policy.common.logging.nsa.LoggingContext;
import org.slf4j.MDC;

/**
 * A logging context for SLF4J.
 *
 */
public class Slf4jLoggingContext implements LoggingContext {
    public Slf4jLoggingContext(LoggingContext base) {}

    @Override
    public void put(String key, String value) {
        MDC.put(key, value);
    }

    @Override
    public void put(String key, long value) {
        put(key, Long.toString(value));
    }

    @Override
    public String get(String key, String defaultValue) {
        String result = MDC.get(key);
        if (result == null) {
            result = defaultValue;
        }
        return result;
    }

    @Override
    public long get(String key, long defaultValue) {
        final String str = get(key, Long.toString(defaultValue));
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException x) {
            return defaultValue;
        }
    }
}
