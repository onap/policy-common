/*
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2018, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.ia;

import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.onap.policy.common.utils.time.CurrentTime;

/**
 * "Current" time used by IntegrityMonitor classes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditorTime {

    /**
     * Instance to be used.
     */
    private static final CurrentTime currentTime = new CurrentTime();

    /**
     * Supplies the instance to be used for accessing the current time. This may be
     * overridden by junit tests to provide a different time instance for each thread when
     * multiple threads are run in parallel.
     */
    private static Supplier<CurrentTime> supplier = () -> currentTime;

    /**
     * Get instance.
     *
     * @return the CurrentTime singleton
     */
    public static CurrentTime getInstance() {
        return supplier.get();
    }
}
