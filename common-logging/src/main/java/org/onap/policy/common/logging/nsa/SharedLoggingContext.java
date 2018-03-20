/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * A logging context must be thread-specific. Contexts that implement SharedLoggingContext are
 * expected to be shared across threads, and they have to be able to populate another logging
 * context with their data.
 * 
 */
public interface SharedLoggingContext extends LoggingContext {
    /**
     * Copy this context's data to the given context. This must work across threads so that a base
     * context can be shared in another thread.
     * 
     * @param lc the shared logging context
     */
    void transferTo(SharedLoggingContext lc);
}
