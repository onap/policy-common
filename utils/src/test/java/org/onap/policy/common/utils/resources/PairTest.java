/*-
 * ============LICENSE_START=======================================================
 * policy-utils
 * ================================================================================
 * Copyright (C) 2017-2018, 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation
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

package org.onap.policy.common.utils.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class PairTest {

    @Test
    public void testPair() {
        Pair<String, String> pair = new Pair<String, String>("foo", "bar");

        assertEquals("foo", pair.first());
        assertEquals("bar", pair.second());
        assertEquals("foo", pair.getFirst());
        assertEquals("bar", pair.getSecond());

        pair.first("one");
        pair.second("two");

        assertEquals("one", pair.first());
        assertEquals("two", pair.second());

        assertEquals(pair.toString(), "Pair [first=one, second=two]");
    }

}
