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

import com.att.nsa.cambria.client.CambriaClientBuilders;
import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
import com.att.nsa.cambria.client.CambriaConsumer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.kafkaclients.v2_6.TracingConsumerInterceptor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.jetbrains.annotations.NotNull;
import org.onap.dmaap.mr.client.MRClientFactory;
import org.onap.dmaap.mr.client.impl.MRConsumerImpl;
import org.onap.dmaap.mr.client.impl.MRConsumerImpl.MRConsumerImplBuilder;
import org.onap.dmaap.mr.client.response.MRConsumerResponse;
import org.onap.dmaap.mr.test.clients.ProtocolTypeConstants;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around libraries to consume from message bus.
 */
public interface BusConsumer {

    /**
     * fetch messages.
     *
     * @return list of messages
     * @throws IOException when error encountered by underlying libraries
     */
    public Iterable<String> fetch() throws IOException;

    /**
     * close underlying library consumer.
     */
    public void close();

    /**
     * Consumer that handles fetch() failures by sleeping.
     */
    abstract class FetchingBusConsumer implements BusConsumer {
        private static final Logger logger = LoggerFactory.getLogger(FetchingBusConsumer.class);

        /**
         * Fetch timeout.
         */
        protected int fetchTimeout;

        /**
         * Time to sleep on a fetch failure.
         */
        @Getter
        private final int sleepTime;

        /**
         * Counted down when {@link #close()} is invoked.
         */
        private final CountDownLatch closeCondition = new CountDownLatch(1);


        /**
         * Constructs the object.
         *
         * @param busTopicParams parameters for the bus topic
         */
        protected FetchingBusConsumer(BusTopicParams busTopicParams) {
            this.fetchTimeout = busTopicParams.getFetchTimeout();

            if (this.fetchTimeout <= 0) {
                this.sleepTime = PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH;
            } else {
                // don't sleep too long, even if fetch timeout is large
                this.sleepTime = Math.min(this.fetchTimeout, PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH);
            }
        }

        /**
         * Causes the thread to sleep; invoked after fetch() fails.  If the consumer is closed,
         * or the thread is interrupted, then this will return immediately.
         */
        protected void sleepAfterFetchFailure() {
            try {
                logger.info("{}: backoff for {}ms", this, sleepTime);
                if (this.closeCondition.await(this.sleepTime, TimeUnit.MILLISECONDS)) {
                    logger.info("{}: closed while handling fetch error", this);
                }

            } catch (InterruptedException e) {
                logger.warn("{}: interrupted while handling fetch error", this, e);
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void close() {
            this.closeCondition.countDown();
        }
    }

    /**
     * Cambria based consumer.
     */
    public static class CambriaConsumerWrapper extends FetchingBusConsumer {

        /**
         * logger.
         */
        private static Logger logger = LoggerFactory.getLogger(CambriaConsumerWrapper.class);

        /**
         * Used to build the consumer.
         */
        private final ConsumerBuilder builder;

        /**
         * Cambria client.
         */
        private final CambriaConsumer consumer;

        /**
         * Cambria Consumer Wrapper.
         * BusTopicParam object contains the following parameters
         * servers - messaging bus hosts.
         * topic - topic for messages
         * apiKey - API Key
         * apiSecret - API Secret
         * consumerGroup - Consumer Group
         * consumerInstance - Consumer Instance
         * fetchTimeout - Fetch Timeout
         * fetchLimit - Fetch Limit
         *
         * @param busTopicParams - The parameters for the bus topic
         */
        public CambriaConsumerWrapper(BusTopicParams busTopicParams) {
            super(busTopicParams);

            this.builder = new CambriaClientBuilders.ConsumerBuilder();

            builder.knownAs(busTopicParams.getConsumerGroup(), busTopicParams.getConsumerInstance())
                .usingHosts(busTopicParams.getServers()).onTopic(busTopicParams.getTopic())
                .waitAtServer(fetchTimeout).receivingAtMost(busTopicParams.getFetchLimit());

            // Set read timeout to fetch timeout + 30 seconds (TBD: this should be configurable)
            builder.withSocketTimeout(fetchTimeout + 30000);

            if (busTopicParams.isUseHttps()) {
                builder.usingHttps();

                if (busTopicParams.isAllowSelfSignedCerts()) {
                    builder.allowSelfSignedCertificates();
                }
            }

            if (busTopicParams.isApiKeyValid() && busTopicParams.isApiSecretValid()) {
                builder.authenticatedBy(busTopicParams.getApiKey(), busTopicParams.getApiSecret());
            }

            if (busTopicParams.isUserNameValid() && busTopicParams.isPasswordValid()) {
                builder.authenticatedByHttp(busTopicParams.getUserName(), busTopicParams.getPassword());
            }

            try {
                this.consumer = builder.build();
            } catch (MalformedURLException | GeneralSecurityException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public Iterable<String> fetch() throws IOException {
            try {
                return this.consumer.fetch();
            } catch (final IOException e) { //NOSONAR
                logger.error("{}: cannot fetch because of {}", this, e.getMessage());
                sleepAfterFetchFailure();
                throw e;
            }
        }

        @Override
        public void close() {
            super.close();
            this.consumer.close();
        }

        @Override
        public String toString() {
            return "CambriaConsumerWrapper [fetchTimeout=" + fetchTimeout + "]";
        }
    }

    /**
     * Kafka based consumer.
     */
    class KafkaConsumerWrapper extends FetchingBusConsumer {

        /**
         * logger.
         */
        private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerWrapper.class);

        private static final String KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";

        /**
         * Kafka consumer.
         */
        protected KafkaConsumer<String, String> consumer;
        protected Properties kafkaProps;

        protected boolean allowTracing;

        /**
         * Kafka Consumer Wrapper.
         * BusTopicParam - object contains the following parameters
         * servers - messaging bus hosts.
         * topic - topic
         *
         * @param busTopicParams - The parameters for the bus topic
         */
        public KafkaConsumerWrapper(BusTopicParams busTopicParams) {
            super(busTopicParams);

            if (busTopicParams.isTopicInvalid()) {
                throw new IllegalArgumentException("No topic for Kafka");
            }

            //Setup Properties for consumer
            kafkaProps = new Properties();
            kafkaProps.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                busTopicParams.getServers().get(0));

            if (busTopicParams.isAdditionalPropsValid()) {
                kafkaProps.putAll(busTopicParams.getAdditionalProps());
            }

            if (kafkaProps.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG) == null) {
                kafkaProps.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KEY_DESERIALIZER);
            }
            if (kafkaProps.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG) == null) {
                kafkaProps.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KEY_DESERIALIZER);
            }
            if (kafkaProps.get(ConsumerConfig.GROUP_ID_CONFIG) == null) {
                kafkaProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, busTopicParams.getConsumerGroup());
            }
            if (busTopicParams.isAllowTracing()) {
                this.allowTracing = true;
                kafkaProps.setProperty(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG,
                        TracingConsumerInterceptor.class.getName());
            }

            consumer = new KafkaConsumer<>(kafkaProps);
            //Subscribe to the topic
            consumer.subscribe(List.of(busTopicParams.getTopic()));
        }

        @Override
        public Iterable<String> fetch() {
            ConsumerRecords<String, String> records = this.consumer.poll(Duration.ofMillis(fetchTimeout));
            if (records == null || records.count() <= 0) {
                return Collections.emptyList();
            }
            List<String> messages = new ArrayList<>(records.count());
            try {
                if (allowTracing) {
                    createParentTraceContext(records);
                }

                for (TopicPartition partition : records.partitions()) {
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                    for (ConsumerRecord<String, String> partitionRecord : partitionRecords) {
                        messages.add(partitionRecord.value());
                    }
                    long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                    consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
                }
            } catch (Exception e) {
                logger.error("{}: cannot fetch, throwing exception after sleep...", this);
                sleepAfterFetchFailure();
                throw e;
            }
            return messages;
        }

        private void createParentTraceContext(ConsumerRecords<String, String> records) {
            TraceParentInfo traceParentInfo = new TraceParentInfo();
            for (ConsumerRecord<String, String> consumerRecord : records) {

                Headers consumerRecordHeaders = consumerRecord.headers();
                traceParentInfo = processTraceParentHeader(consumerRecordHeaders);
            }

            SpanContext spanContext = SpanContext.createFromRemoteParent(
                    traceParentInfo.getTraceId(), traceParentInfo.getSpanId(),
                    TraceFlags.getSampled(), TraceState.builder().build());

            Context.current().with(Span.wrap(spanContext)).makeCurrent();
        }

        private TraceParentInfo processTraceParentHeader(Headers headers) {
            TraceParentInfo traceParentInfo = new TraceParentInfo();
            if (headers.lastHeader("traceparent") != null) {
                traceParentInfo.setParentTraceId(new String(headers.lastHeader(
                        "traceparent").value(), StandardCharsets.UTF_8));

                String[] parts = traceParentInfo.getParentTraceId().split("-");
                traceParentInfo.setTraceId(parts[1]);
                traceParentInfo.setSpanId(parts[2]);
            }

            return traceParentInfo;
        }

        @Data
        @NoArgsConstructor
        private static class TraceParentInfo {
            private String parentTraceId;
            private String traceId;
            private String spanId;
        }

        @Override
        public void close() {
            super.close();
            this.consumer.close();
            logger.info("Kafka Consumer exited {}", this);
        }

        @Override
        public String toString() {
            return "KafkaConsumerWrapper [fetchTimeout=" + fetchTimeout + "]";
        }
    }

    /**
     * MR based consumer.
     */
    public abstract class DmaapConsumerWrapper extends FetchingBusConsumer {

        /**
         * logger.
         */
        private static final Logger logger = LoggerFactory.getLogger(DmaapConsumerWrapper.class);

        /**
         * Name of the "protocol" property.
         */
        protected static final String PROTOCOL_PROP = "Protocol";

        /**
         * MR Consumer.
         */
        protected MRConsumerImpl consumer;

        /**
         * MR Consumer Wrapper.
         *
         * <p>servers - messaging bus hosts
         * topic - topic
         * apiKey - API Key
         * apiSecret - API Secret
         * username - AAF Login
         * password - AAF Password
         * consumerGroup - Consumer Group
         * consumerInstance - Consumer Instance
         * fetchTimeout - Fetch Timeout
         * fetchLimit - Fetch Limit
         *
         * @param busTopicParams contains above listed attributes
         * @throws MalformedURLException URL should be valid
         */
        protected DmaapConsumerWrapper(BusTopicParams busTopicParams) throws MalformedURLException {
            super(busTopicParams);

            if (busTopicParams.isTopicInvalid()) {
                throw new IllegalArgumentException("No topic for DMaaP");
            }

            this.consumer = new MRConsumerImplBuilder()
                .setHostPart(busTopicParams.getServers())
                .setTopic(busTopicParams.getTopic())
                .setConsumerGroup(busTopicParams.getConsumerGroup())
                .setConsumerId(busTopicParams.getConsumerInstance())
                .setTimeoutMs(busTopicParams.getFetchTimeout())
                .setLimit(busTopicParams.getFetchLimit())
                .setApiKey(busTopicParams.getApiKey())
                .setApiSecret(busTopicParams.getApiSecret())
                .createMRConsumerImpl();

            this.consumer.setUsername(busTopicParams.getUserName());
            this.consumer.setPassword(busTopicParams.getPassword());
        }

        @Override
        public Iterable<String> fetch() {
            final MRConsumerResponse response = this.consumer.fetchWithReturnConsumerResponse();
            if (response == null) {
                logger.warn("{}: DMaaP NULL response received", this);

                sleepAfterFetchFailure();
                return new ArrayList<>();
            } else {
                logger.debug("DMaaP consumer received {} : {}", response.getResponseCode(),
                    response.getResponseMessage());

                if (!"200".equals(response.getResponseCode())) {

                    logger.error("DMaaP consumer received: {} : {}", response.getResponseCode(),
                        response.getResponseMessage());

                    sleepAfterFetchFailure();

                    /* fall through */
                }
            }

            if (response.getActualMessages() == null) {
                return new ArrayList<>();
            } else {
                return response.getActualMessages();
            }
        }

        @Override
        public void close() {
            super.close();
            this.consumer.close();
        }

        @Override
        public String toString() {
            return "DmaapConsumerWrapper [" + "consumer.getAuthDate()=" + consumer.getAuthDate()
                + ", consumer.getAuthKey()=" + consumer.getAuthKey() + ", consumer.getHost()=" + consumer.getHost()
                + ", consumer.getProtocolFlag()=" + consumer.getProtocolFlag() + ", consumer.getUsername()="
                + consumer.getUsername() + "]";
        }
    }

    /**
     * MR based consumer.
     */
    class DmaapAafConsumerWrapper extends DmaapConsumerWrapper {

        private static final Logger logger = LoggerFactory.getLogger(DmaapAafConsumerWrapper.class);

        /**
         * BusTopicParams contain the following parameters.
         * MR Consumer Wrapper.
         *
         * <p>servers messaging bus hosts
         * topic - topic
         * apiKey - API Key
         * apiSecret - API Secret
         * aafLogin - AAF Login
         * aafPassword - AAF Password
         * consumerGroup - Consumer Group
         * consumerInstance - Consumer Instance
         * fetchTimeout - Fetch Timeout
         * fetchLimit - Fetch Limit
         *
         * @param busTopicParams contains above listed params
         * @throws MalformedURLException URL should be valid
         */
        public DmaapAafConsumerWrapper(BusTopicParams busTopicParams) throws MalformedURLException {

            super(busTopicParams);

            // super constructor sets servers = {""} if empty to avoid errors when using DME2
            if (busTopicParams.isServersInvalid()) {
                throw new IllegalArgumentException("Must provide at least one host for HTTP AAF");
            }

            this.consumer.setProtocolFlag(ProtocolTypeConstants.AAF_AUTH.getValue());

            Properties props = new Properties();

            if (busTopicParams.isUseHttps()) {
                props.setProperty(PROTOCOL_PROP, "https");
                this.consumer.setHost(busTopicParams.getServers().get(0) + ":3905");

            } else {
                props.setProperty(PROTOCOL_PROP, "http");
                this.consumer.setHost(busTopicParams.getServers().get(0) + ":3904");
            }

            this.consumer.setProps(props);
            logger.info("{}: CREATION", this);
        }

        @Override
        public String toString() {
            final MRConsumerImpl consumer = this.consumer;

            return "DmaapConsumerWrapper [" + "consumer.getAuthDate()=" + consumer.getAuthDate()
                + ", consumer.getAuthKey()=" + consumer.getAuthKey() + ", consumer.getHost()=" + consumer.getHost()
                + ", consumer.getProtocolFlag()=" + consumer.getProtocolFlag() + ", consumer.getUsername()="
                + consumer.getUsername() + "]";
        }
    }

    class DmaapDmeConsumerWrapper extends DmaapConsumerWrapper {

        private static final Logger logger = LoggerFactory.getLogger(DmaapDmeConsumerWrapper.class);

        /**
         * Constructor.
         *
         * @param busTopicParams topic parameters
         * @throws MalformedURLException must provide a valid URL
         */
        public DmaapDmeConsumerWrapper(BusTopicParams busTopicParams) throws MalformedURLException {


            super(busTopicParams);


            final String dme2RouteOffer = (busTopicParams.isAdditionalPropsValid()
                ? busTopicParams.getAdditionalProps().get(
                PolicyEndPointProperties.DME2_ROUTE_OFFER_PROPERTY)
                : null);

            BusHelper.validateBusTopicParams(busTopicParams, PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS);

            if ((busTopicParams.isPartnerInvalid())
                && StringUtils.isBlank(dme2RouteOffer)) {
                throw new IllegalArgumentException(
                    "Must provide at least " + PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS
                        + "." + busTopicParams.getTopic()
                        + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_PARTNER_SUFFIX + " or "
                        + PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                        + busTopicParams.getTopic()
                        + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX + " for DME2");
            }

            final String serviceName = busTopicParams.getServers().get(0);

            this.consumer.setProtocolFlag(ProtocolTypeConstants.DME2.getValue());

            this.consumer.setUsername(busTopicParams.getUserName());
            this.consumer.setPassword(busTopicParams.getPassword());

            Properties props = getProperties(busTopicParams, serviceName, dme2RouteOffer);

            MRClientFactory.prop = props;
            this.consumer.setProps(props);

            logger.info("{}: CREATION", this);
        }

        @NotNull
        private static Properties getProperties(BusTopicParams busTopicParams, String serviceName,
                                                String dme2RouteOffer) {
            Properties props = new Properties();

            props.setProperty(PolicyEndPointProperties.DME2_SERVICE_NAME_PROPERTY, serviceName);

            props.setProperty("username", busTopicParams.getUserName());
            props.setProperty("password", busTopicParams.getPassword());

            /* These are required, no defaults */
            props.setProperty("topic", busTopicParams.getTopic());

            BusHelper.setCommonProperties(busTopicParams, dme2RouteOffer, props);

            props.setProperty("MethodType", "GET");

            if (busTopicParams.isUseHttps()) {
                props.setProperty(PROTOCOL_PROP, "https");

            } else {
                props.setProperty(PROTOCOL_PROP, "http");
            }

            props.setProperty("contenttype", "application/json");

            if (busTopicParams.isAdditionalPropsValid()) {
                props.putAll(busTopicParams.getAdditionalProps());
            }
            return props;
        }
    }
}


