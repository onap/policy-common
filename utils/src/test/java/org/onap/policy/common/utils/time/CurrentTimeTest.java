/*
 * ============LICENSE_START=======================================================
 * Common Utils
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CurrentTimeTest {

    @Test
    void testGetMillis() {
        long tcur = System.currentTimeMillis();
        long tval = new CurrentTime().getMillis();
        long tval2 = new CurrentTime().getMillis();
        long tend = System.currentTimeMillis();

        assertTrue(tval >= tcur && tval <= tend);
        assertTrue(tval2 >= tcur && tval2 <= tend);
    }

    @Test
    void testGetDate() {
        long tcur = System.currentTimeMillis();
        long tval = new CurrentTime().getDate().getTime();
        long tval2 = new CurrentTime().getDate().getTime();
        long tend = System.currentTimeMillis();

        assertTrue(tval >= tcur && tval <= tend);
        assertTrue(tval2 >= tcur && tval2 <= tend);
    }

    @Test
    void testSleep() throws Exception {
        long tcur = System.currentTimeMillis();
        new CurrentTime().sleep(10);
        long tend = System.currentTimeMillis();

        assertTrue(tend >= tcur + 10 - 1);
    }

}
