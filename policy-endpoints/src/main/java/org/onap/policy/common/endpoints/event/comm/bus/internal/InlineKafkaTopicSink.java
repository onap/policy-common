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

import java.util.Map;
import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.event.comm.bus.KafkaTopicSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation publishes events for the associated KAFKA topic, inline with the calling
 * thread.
 */
public class InlineKafkaTopicSink extends InlineBusTopicSink implements KafkaTopicSink {

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(InlineKafkaTopicSink.class);

    protected Map<String, String> additionalProps = null;

    /**
     * Argument-based KAFKA Topic Writer instantiation. BusTopicParams contains below mentioned
     * attributes.
     *
     * <p>servers              list of KAFKA servers available for publishing
     * topic                the topic to publish to
     * partitionId          the partition key (optional, autogenerated if not provided)
     * useHttps             does connection use HTTPS?
     * @param busTopicParams contains attributes needed
     * @throws IllegalArgumentException if invalid arguments are detected
     */
    public InlineKafkaTopicSink(BusTopicParams busTopicParams) {
        super(busTopicParams);
        this.additionalProps = busTopicParams.getAdditionalProps();
    }

    /**
     * Instantiation of internal resources.
     */
    @Override
    public void init() {

        this.publisher = new BusPublisher.KafkaPublisherWrapper(BusTopicParams.builder()
                .servers(this.servers)
                .topic(this.effectiveTopic)
                .useHttps(this.useHttps)
                .additionalProps(this.additionalProps)
                .build());
        logger.info("{}: KAFKA SINK created", this);
    }

    @Override
    public String toString() {
        return "InlineKafkaTopicSink [getTopicCommInfrastructure()=" + getTopicCommInfrastructure() + ", toString()="
                        + super.toString() + "]";
    }

    @Override
    public CommInfrastructure getTopicCommInfrastructure() {
        return Topic.CommInfrastructure.KAFKA;
    }
}