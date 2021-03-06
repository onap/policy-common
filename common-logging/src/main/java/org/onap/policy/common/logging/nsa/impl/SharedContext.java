/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2018, 2020 AT&T Intellectual Property. All rights reserved.
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

import java.util.HashMap;
import java.util.Map.Entry;
import org.onap.policy.common.logging.nsa.LoggingContext;
import org.onap.policy.common.logging.nsa.SharedLoggingContext;

/**
 * A shared logging context for SLF4J.
 *
 */
public class SharedContext extends Slf4jLoggingContext implements SharedLoggingContext {
    private final HashMap<String, String> contextMap;

    public SharedContext(LoggingContext base) {
        super(base);
        contextMap = new HashMap<>();
    }

    @Override
    public void put(String key, String value) {
        super.put(key, value);
        contextMap.put(key, value);
    }

    @Override
    public void transferTo(SharedLoggingContext lc) {
        for (Entry<String, String> e : contextMap.entrySet()) {
            lc.put(e.getKey(), e.getValue());
        }
    }
}
