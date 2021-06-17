/*
 * ============LICENSE_START=======================================================
 * Common Utils-Test
 * ================================================================================
 * Copyright (C) 2018-2019, 2021 AT&T Intellectual Property. All rights reserved.
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
import java.util.concurrent.atomic.AtomicLong;
import lombok.NoArgsConstructor;

/**
 * "Current" time, when running junit tests. This is intended to be injected into classes
 * under test, to replace their {@link CurrentTime} objects. When {@link #sleep(long)} is
 * invoked, it simply advances the notion of "current" time and returns immediately.
 */
@NoArgsConstructor
public class TestTime extends CurrentTime {

    /**
     * "Current" time, in milliseconds, used by tests.
     */
    private AtomicLong tcur = new AtomicLong(System.currentTimeMillis());

    @Override
    public long getMillis() {
        return tcur.get();
    }

    @Override
    public Date getDate() {
        return new Date(tcur.get());
    }

    @Override
    public void sleep(long sleepMs) throws InterruptedException {
        if (sleepMs > 0) {
            tcur.addAndGet(sleepMs);
        }
    }
}
