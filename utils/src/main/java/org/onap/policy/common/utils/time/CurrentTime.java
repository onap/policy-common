/*
 * ============LICENSE_START=======================================================
 * Common Utils
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

package org.onap.policy.common.utils.time;

import java.util.Date;
import lombok.NoArgsConstructor;

/**
 * Methods to access the current time. Classes can use objects of this type to get current
 * time information, while allowing the objects to be overridden by junit tests.
 */
@NoArgsConstructor
public class CurrentTime {

    /**
     * Get the millisecond time.
     *
     * @return the current time, in milliseconds
     */
    public long getMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Get the current date.
     *
     * @return the current Date
     */
    public Date getDate() {
        return new Date();
    }

    /**
     * Sleeps for a period of time.
     *
     * @param sleepMs amount of time to sleep, in milliseconds
     * @throws InterruptedException can be interrupted
     */
    public void sleep(long sleepMs) throws InterruptedException {
        Thread.sleep(sleepMs);
    }
}
