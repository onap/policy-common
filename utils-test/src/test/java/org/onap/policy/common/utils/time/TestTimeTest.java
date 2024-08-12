/*
 * ============LICENSE_START=======================================================
 * Common Utils-Test
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestTimeTest {

    @Test
    void test() throws Exception {
        TestTime tm = new TestTime();
        TestTime tm2 = new TestTime();

        final long treal = System.currentTimeMillis();

        long tcur = tm.getMillis();
        assertEquals(tcur, tm.getDate().getTime());

        long tsleep = 10000L;
        long tcur2 = tm2.getMillis();

        // sleep a bit and then check values
        tcur2 += tsleep;
        tm2.sleep(tsleep);
        assertEquals(tcur2, tm2.getMillis());
        assertEquals(tcur2, tm2.getDate().getTime());

        // sleep some more and then check values
        tcur2 += tsleep;
        tm2.sleep(tsleep);
        assertEquals(tcur2, tm2.getMillis());
        assertEquals(tcur2, tm2.getDate().getTime());

        // check again - to ensure unchanged
        assertEquals(tcur2, tm2.getMillis());
        assertEquals(tcur2, tm2.getDate().getTime());

        // original should also be unchanged
        assertEquals(tcur, tm.getMillis());
        assertEquals(tcur, tm.getDate().getTime());

        // ensure that no real time has elapsed
        assertTrue(System.currentTimeMillis() < treal + tsleep / 2);

        // negative sleep should not modify the time
        tcur = tm.getMillis();
        tm.sleep(-1);
        assertEquals(tcur, tm.getMillis());
    }

}
