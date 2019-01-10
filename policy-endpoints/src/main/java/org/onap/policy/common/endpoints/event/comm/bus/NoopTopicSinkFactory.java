/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;

/**
 * Noop Topic Sink Factory.
 */
public class NoopTopicSinkFactory extends NoopTopicFactory<NoopTopicSink> {

    /**
     * {@inheritDoc}.
     */
    @Override
    protected String getTopicsPropertyName() {
        return PolicyEndPointProperties.PROPERTY_NOOP_SINK_TOPICS;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected NoopTopicSink build(List<String> servers, String topic) {
        return new NoopTopicSink(servers, topic);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String toString() {
        return "NoopTopicSinkFactory [" + super.toString() + "]";
    }

}

