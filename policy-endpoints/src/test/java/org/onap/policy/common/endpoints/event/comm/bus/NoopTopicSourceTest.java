/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm.bus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class NoopTopicSourceTest extends NoopTopicEndpointTest<NoopTopicSourceFactory, NoopTopicSource> {

    public NoopTopicSourceTest() {
        super(new NoopTopicSourceFactory());
    }

    @Override
    protected boolean io(String message) {
        return this.endpoint.offer(message);
    }

    @Test
    void testToString() {
        assertTrue(this.endpoint.toString().startsWith("NoopTopicSource"));
    }

    @Test
    void testOffer() {
        NoopTopicSource source = new NoopTopicSource(servers, MY_TOPIC) {
            @Override
            protected boolean broadcast(String message) {
                throw new RuntimeException(EXPECTED);
            }

        };

        source.start();
        assertFalse(source.offer(MY_MESSAGE));
    }
}
