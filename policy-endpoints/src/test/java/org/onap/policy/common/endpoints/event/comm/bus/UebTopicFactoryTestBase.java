/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.onap.policy.common.endpoints.event.comm.Topic;

/**
 * Base class for UebTopicXxxFactory tests.
 *
 * @param <T> type of topic managed by the factory
 */
public abstract class UebTopicFactoryTestBase<T extends Topic> extends BusTopicFactoryTestBase<T> {

    @Override
    public void testBuildBusTopicParams_Ex() {

        super.testBuildBusTopicParams_Ex();

        // null servers
        RuntimeException actual = expectException(() -> buildTopic(makeBuilder().servers(null).build()));
        assertEquals(IllegalArgumentException.class, actual.getClass());

        // empty servers
        actual = expectException(() -> buildTopic(makeBuilder().servers(Collections.emptyList()).build()));
        assertEquals(IllegalArgumentException.class, actual.getClass());
    }
}
