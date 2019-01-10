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

import java.util.List;
import org.onap.policy.common.endpoints.event.comm.TopicSource;

/**
 * No Operation Topic Source.
 */
public class NoopTopicSource extends NoopTopicEndpoint implements TopicSource {

    /**
     * Factory.
     */
    public static final NoopTopicSourceFactory factory = new NoopTopicSourceFactory();

    /**
     * {@inheritDoc}.
     */
    public NoopTopicSource(List<String> servers, String topic) {
        super(servers, topic);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public boolean offer(String event) {
        return super.io(event);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String toString() {
        return "NoopTopicSource[" + super.toString() + "]";
    }

}
