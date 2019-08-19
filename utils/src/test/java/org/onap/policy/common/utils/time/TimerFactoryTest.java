/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertTrue;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class TimerFactoryTest {
    private TimerFactory factory;

    @Before
    public void setUp() {
        factory = new TimerFactory();
    }

    @Test
    public void testMakeTimer() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Timer timer = factory.makeTimer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                latch.countDown();
            }
        }, 1);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        timer.cancel();
    }

}
