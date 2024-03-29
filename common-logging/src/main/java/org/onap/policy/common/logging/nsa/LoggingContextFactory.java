/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.onap.policy.common.logging.nsa.impl.SharedContext;
import org.onap.policy.common.logging.nsa.impl.Slf4jLoggingContext;

/**
 * A factory for setting up a LoggingContext.
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingContextFactory {
    public static class Builder {

        private LoggingContext baseContext = null;
        private boolean forShared = false;

        public Builder withBaseContext(LoggingContext lc) {
            baseContext = lc;
            return this;
        }

        public Builder forSharing() {
            forShared = true;
            return this;
        }

        public LoggingContext build() {
            return forShared ? new SharedContext(baseContext) : new Slf4jLoggingContext(baseContext);
        }
    }
}
