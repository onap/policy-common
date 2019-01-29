/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modified Copyright (C) 2018 Samsung Electronics Co., Ltd.
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

import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicSource;

/**
 * This topic source implementation specializes in reading messages over an UEB Bus topic source and
 * notifying its listeners.
 */
public class SingleThreadedUebTopicSource extends SingleThreadedBusTopicSource implements UebTopicSource {

    /**
     * Constructor.
     *
     * @param busTopicParams Parameters object containing all the required inputs
     * @throws IllegalArgumentException An invalid parameter passed in
     */
    public SingleThreadedUebTopicSource(BusTopicParams busTopicParams) {
        super(busTopicParams);

        this.init();
    }

    /**
     * Initialize the Cambria client.
     */
    @Override
    public void init() {
        this.consumer = new BusConsumer.CambriaConsumerWrapper(BusTopicParams.builder()
                .servers(this.servers)
                .topic(this.topic)
                .apiKey(this.apiKey)
                .apiSecret(this.apiSecret)
                .consumerGroup(this.consumerGroup)
                .consumerInstance(this.consumerInstance)
                .fetchTimeout(this.fetchTimeout)
                .fetchLimit(this.fetchLimit)
                .useHttps(this.useHttps)
                .allowSelfSignedCerts(this.allowSelfSignedCerts).build());
    }

    @Override
    public CommInfrastructure getTopicCommInfrastructure() {
        return Topic.CommInfrastructure.UEB;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SingleThreadedUebTopicSource [getTopicCommInfrastructure()=")
                .append(getTopicCommInfrastructure()).append(", toString()=").append(super.toString()).append("]");
        return builder.toString();
    }

}
