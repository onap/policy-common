/*
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

package org.onap.policy.common.endpoints.event.comm.bus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoopTopicSinkTest extends NoopTopicEndpointTest<NoopTopicSinkFactory, NoopTopicSink> {

    public NoopTopicSinkTest() {
        super(new NoopTopicSinkFactory());
    }

    @Override
    protected boolean io(String message) {
        return endpoint.send(message);
    }

    @Test
    public void testToString() {
        assertTrue(endpoint.toString().startsWith("NoopTopicSink"));
    }

    @Test
    public void testSend() {
        NoopTopicSink sink = new NoopTopicSink(servers, MY_TOPIC) {
            @Override
            protected boolean broadcast(String message) {
                throw new RuntimeException(EXPECTED);
            }

        };

        sink.start();
        assertFalse(sink.send(MY_MESSAGE));
    }
}
