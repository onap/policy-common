/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Nordix Foundation.
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
import org.onap.policy.common.endpoints.event.comm.bus.KafkaTopicSource;

/**
 * This topic source implementation specializes in reading messages over an Kafka Bus topic source and
 * notifying its listeners.
 */
public class SingleThreadedKafkaTopicSource extends SingleThreadedBusTopicSource implements KafkaTopicSource {

    /**
     * Constructor.
     *
     * @param busTopicParams Parameters object containing all the required inputs
     * @throws IllegalArgumentException An invalid parameter passed in
     */
    public SingleThreadedKafkaTopicSource(BusTopicParams busTopicParams) {
        super(busTopicParams);
        this.init();
    }

    /**
     * Initialize the Cambria client.
     */
    @Override
    public void init() {
        this.consumer = new BusConsumer.KafkaConsumerWrapper(BusTopicParams.builder()
                .servers(this.servers)
                .topic(this.effectiveTopic)
                .useHttps(this.useHttps)
                .build());
    }

    @Override
    public CommInfrastructure getTopicCommInfrastructure() {
        return Topic.CommInfrastructure.KAFKA;
    }

    @Override
    public String toString() {
        return "SingleThreadedKafkaTopicSource [getTopicCommInfrastructure()=" + getTopicCommInfrastructure()
            + ", toString()=" + super.toString() + "]";
    }

}
