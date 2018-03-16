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

package org.onap.policy.common.logging.nsa;

/**
 * An interface for providing data into the underlying logging context. Systems should use this
 * interface rather than log system specific MDC solutions in order to reduce dependencies.
 * 
 * <p>A LoggingContext is specific to the calling thread.
 * 
 */
public interface LoggingContext {
    /**
     * Put a key/value pair into the logging context, replacing an entry with the same key.
     * 
     * @param key the key
     * @param value the value
     */
    void put(String key, String value);

    /**
     * Put a key/value pair into the logging context, replacing an entry with the same key.
     * 
     * @param key the key
     * @param value the value
     */
    void put(String key, long value);

    /**
     * Get a string value, returning the default value if the value is missing.
     * 
     * @param key the key
     * @param defaultValue the default value
     * @return a string value
     */
    String get(String key, String defaultValue);

    /**
     * Get a long value, returning the default value if the value is missing or not a long.
     * 
     * @param key the key
     * @param defaultValue the default value
     * @return a long value
     */
    long get(String key, long defaultValue);
}
