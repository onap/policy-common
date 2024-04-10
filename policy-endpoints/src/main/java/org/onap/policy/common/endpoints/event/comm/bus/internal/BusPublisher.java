/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Samsung Electronics Co., Ltd.
 * Modifications Copyright (C) 2020,2023 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2022-2024 Nordix Foundation.
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

import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingProducerInterceptor;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface BusPublisher {

    String NO_MESSAGE_PROVIDED = "No message provided";
    String LOG_CLOSE = "{}: CLOSE";
    String LOG_CLOSE_FAILED = "{}: CLOSE FAILED";

    /**
     * sends a message.
     *
     * @param partitionId id
     * @param message the message
     * @return true if success, false otherwise
     * @throws IllegalArgumentException if no message provided
     */
    boolean send(String partitionId, String message);

    /**
     * closes the publisher.
     */
    void close();

    /**
     * Kafka based library publisher.
     */
    class KafkaPublisherWrapper implements BusPublisher {

        private static final Logger logger = LoggerFactory.getLogger(KafkaPublisherWrapper.class);
        private static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

        private final String topic;

        /**
         * Kafka publisher.
         */
        private final Producer<String, String> producer;
        protected Properties kafkaProps;

        /**
         * Kafka Publisher Wrapper.
         *
         * @param busTopicParams topic parameters
         */
        protected KafkaPublisherWrapper(BusTopicParams busTopicParams) {

            if (busTopicParams.isTopicInvalid()) {
                throw new IllegalArgumentException("No topic for Kafka");
            }

            this.topic = busTopicParams.getTopic();

            // Setup Properties for consumer
            kafkaProps = new Properties();
            kafkaProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, busTopicParams.getServers().get(0));
            if (busTopicParams.isAdditionalPropsValid()) {
                kafkaProps.putAll(busTopicParams.getAdditionalProps());
            }
            if (kafkaProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG) == null) {
                kafkaProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
            }
            if (kafkaProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG) == null) {
                kafkaProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
            }

            if (busTopicParams.isAllowTracing()) {
                kafkaProps.setProperty(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
                        TracingProducerInterceptor.class.getName());
            }

            producer = new KafkaProducer<>(kafkaProps);
        }

        @Override
        public boolean send(String partitionId, String message) {
            if (message == null) {
                throw new IllegalArgumentException(NO_MESSAGE_PROVIDED);
            }

            try {
                // Create the record
                ProducerRecord<String, String> producerRecord =
                        new ProducerRecord<>(topic, UUID.randomUUID().toString(), message);

                this.producer.send(producerRecord);
                producer.flush();
            } catch (Exception e) {
                logger.warn("{}: SEND of {} cannot be performed because of {}", this, message, e.getMessage(), e);
                return false;
            }
            return true;
        }

        @Override
        public void close() {
            logger.info(LOG_CLOSE, this);

            try {
                this.producer.close();
            } catch (Exception e) {
                logger.warn("{}: CLOSE FAILED because of {}", this, e.getMessage(), e);
            }
        }

        @Override
        public String toString() {
            return "KafkaPublisherWrapper []";
        }

    }
}
