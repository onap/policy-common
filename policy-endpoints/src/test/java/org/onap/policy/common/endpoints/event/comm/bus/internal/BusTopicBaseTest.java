/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm.bus.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.utils.gson.GsonTestUtils;

public class BusTopicBaseTest extends TopicTestBase {

    private BusTopicBaseImpl base;

    /**
     * Initializes the object to be tested.
     */
    @Before
    public void setUp() {
        super.setUp();

        base = new BusTopicBaseImpl(builder.build());
    }

    @Test
    public void testToString() {
        assertNotNull(base.toString());
    }
    
    @Test
    public void testSerialize() {
        new GsonTestUtils().compareJackson2Gson(base);
    }

    @Test
    public void testGetApiKey() {
        assertEquals(MY_API_KEY, base.getApiKey());
    }

    @Test
    public void testGetApiSecret() {
        assertEquals(MY_API_SECRET, base.getApiSecret());
    }

    @Test
    public void testIsUseHttps() {
        assertEquals(true, base.isUseHttps());
        assertEquals(false, new BusTopicBaseImpl(builder.useHttps(false).build()).isUseHttps());
    }

    @Test
    public void testIsAllowSelfSignedCerts() {
        assertEquals(true, base.isAllowSelfSignedCerts());
        assertEquals(false, new BusTopicBaseImpl(builder.allowSelfSignedCerts(false).build()).isAllowSelfSignedCerts());
    }

    @Test
    public void testAnyNullOrEmpty() {
        assertFalse(base.anyNullOrEmpty());
        assertFalse(base.anyNullOrEmpty("any-none-null", "any-none-null-B"));

        assertTrue(base.anyNullOrEmpty(null, "any-first-null"));
        assertTrue(base.anyNullOrEmpty("any-middle-null", null, "any-middle-null-B"));
        assertTrue(base.anyNullOrEmpty("any-last-null", null));
        assertTrue(base.anyNullOrEmpty("any-empty", ""));
    }

    @Test
    public void testAllNullOrEmpty() {
        assertTrue(base.allNullOrEmpty());
        assertTrue(base.allNullOrEmpty(""));
        assertTrue(base.allNullOrEmpty(null, ""));

        assertFalse(base.allNullOrEmpty("all-ok-only-one"));
        assertFalse(base.allNullOrEmpty("all-ok-one", "all-ok-two"));
        assertFalse(base.allNullOrEmpty("all-ok-null", null));
        assertFalse(base.allNullOrEmpty("", "all-ok-empty"));
        assertFalse(base.allNullOrEmpty("", "all-one-ok", null));
    }

    public static class BusTopicBaseImpl extends BusTopicBase {

        public BusTopicBaseImpl(BusTopicParams busTopicParams) {
            super(busTopicParams);
        }

        @Override
        public CommInfrastructure getTopicCommInfrastructure() {
            return CommInfrastructure.NOOP;
        }

        @Override
        public boolean start() {
            return true;
        }

        @Override
        public boolean stop() {
            return true;
        }

        @Override
        public void shutdown() {
            // do nothing
        }

    }
}
