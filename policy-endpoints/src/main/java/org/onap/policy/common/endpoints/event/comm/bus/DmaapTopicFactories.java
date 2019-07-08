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

import lombok.Getter;

public class DmaapTopicFactories {

    /**
     * Factory for instantiation and management of sinks.
     */
    @Getter
    private static final DmaapTopicSinkFactory sinkFactory = new IndexedDmaapTopicSinkFactory();

    /**
     * Factory for instantiation and management of sources.
     */
    @Getter
    private static final DmaapTopicSourceFactory sourceFactory = new IndexedDmaapTopicSourceFactory();


    private DmaapTopicFactories() {
        // do nothing
    }
}
