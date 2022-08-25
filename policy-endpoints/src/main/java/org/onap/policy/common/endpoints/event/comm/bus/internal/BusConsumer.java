/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Samsung Electronics Co., Ltd.
 * Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

import com.att.nsa.cambria.client.CambriaClientBuilders;
import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
import com.att.nsa.cambria.client.CambriaConsumer;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
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
    public abstract static class FetchingBusConsumer implements BusConsumer {
        private static Logger logger = LoggerFactory.getLogger(FetchingBusConsumer.class);

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
         * servers messaging bus hosts.
         * topic topic
         * apiKey API Key
         * apiSecret API Secret
         * consumerGroup Consumer Group
         * consumerInstance Consumer Instance
         * fetchTimeout Fetch Timeout
         * fetchLimit Fetch Limit
         *
         * @param busTopicParams - The parameters for the bus topic
         * @throws GeneralSecurityException - Security exception
         * @throws MalformedURLException - Malformed URL exception
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
    public static class KafkaConsumerWrapper extends FetchingBusConsumer {

        /**
         * logger.
         */
        private static Logger logger = LoggerFactory.getLogger(KafkaConsumerWrapper.class);

        private static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
        private static final String PARTITION_CONFIG = "org.apache.kafka.clients.consumer.RoundRobinAssignor";
        private static final String AUTO_OFFSET_CONFIG = "earliest";

        /**
         * Kafka consumer.
         */
        protected KafkaConsumer<String, String> consumer;
        protected Properties kafkaProps;

        /**
         * Kafka Consumer Wrapper.
         * BusTopicParam object contains the following parameters
         * servers messaging bus hosts.
         * topic topic
         *
         * @param busTopicParams - The parameters for the bus topic
         * @throws GeneralSecurityException - Security exception
         * @throws MalformedURLException - Malformed URL exception
         */
        public KafkaConsumerWrapper(BusTopicParams busTopicParams) throws MalformedURLException {
            super(busTopicParams);

            if (busTopicParams.isTopicInvalid()) {
                throw new IllegalArgumentException("No topic for Kafka");
            }

            //Setup Properties for consumer
            kafkaProps = new Properties();
            kafkaProps.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    busTopicParams.getServers().get(0));

            kafkaProps.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
            kafkaProps.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
            kafkaProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, busTopicParams.getConsumerGroup());
            kafkaProps.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            kafkaProps.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_CONFIG);
            kafkaProps.setProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, PARTITION_CONFIG);

            if (busTopicParams.isAdditionalPropsValid()) {
                addAdditionalProps(busTopicParams);
            }

            consumer = new KafkaConsumer<>(kafkaProps);
            //Subscribe to the topic
            consumer.subscribe(Arrays.asList(busTopicParams.getTopic()));
        }

        @Override
        public Iterable<String> fetch() throws IOException {
            ConsumerRecords<String, String> records = this.consumer.poll(Duration.ofMillis(fetchTimeout));
            if (records == null || records.count() <= 0) {
                return Collections.emptyList();
            }
            List<String> messages = new ArrayList<>(records.count());
            try {
                for (TopicPartition partition : records.partitions()) {
                    messages.clear();
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                    for (ConsumerRecord<String, String> record : partitionRecords) {
                        messages.add(record.value());
                    }
                    long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                    consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
                }
                if (CollectionUtils.isEmpty(messages)) {
                    return Collections.emptyList();
                }
            } catch (Exception e) {
                logger.error("Kafka Fetch failed with exception.", e);
            }
            return messages;
        }

        private void addAdditionalProps(BusTopicParams busTopicParams) {
            String securityProtocol = busTopicParams.getAdditionalProps()
                .get(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
            if (securityProtocol != null) {
                kafkaProps.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);
            }

            String saslMechanism = busTopicParams.getAdditionalProps().get(SaslConfigs.SASL_MECHANISM);
            if (saslMechanism != null) {
                kafkaProps.setProperty(SaslConfigs.SASL_MECHANISM, saslMechanism);
            }

            String saslJaasConfig = busTopicParams.getAdditionalProps().get(SaslConfigs.SASL_JAAS_CONFIG);
            if (saslJaasConfig != null) {
                kafkaProps.setProperty(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig);
            }
        }

        @Override
        public void close() {
            super.close();
            try {
                fetch();
            } catch (IOException e) {
                logger.error("Kafka Fetch failed with exception.", e);
            }
            this.consumer.close();
            logger.info("Kafka Consumer exited");
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
        private static Logger logger = LoggerFactory.getLogger(DmaapConsumerWrapper.class);

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
         * <p>servers          messaging bus hosts
         * topic            topic
         * apiKey           API Key
         * apiSecret        API Secret
         * username         AAF Login
         * password         AAF Password
         * consumerGroup    Consumer Group
         * consumerInstance Consumer Instance
         * fetchTimeout     Fetch Timeout
         * fetchLimit       Fetch Limit
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
        public Iterable<String> fetch() throws IOException {
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
    public static class DmaapAafConsumerWrapper extends DmaapConsumerWrapper {

        private static Logger logger = LoggerFactory.getLogger(DmaapAafConsumerWrapper.class);

        private final Properties props;

        /**
         * BusTopicParams contain the following parameters.
         * MR Consumer Wrapper.
         *
         * <p>servers messaging bus hosts
         * topic topic
         * apiKey API Key
         * apiSecret API Secret
         * aafLogin AAF Login
         * aafPassword AAF Password
         * consumerGroup Consumer Group
         * consumerInstance Consumer Instance
         * fetchTimeout Fetch Timeout
         * fetchLimit Fetch Limit
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

            props = new Properties();

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

    public static class DmaapDmeConsumerWrapper extends DmaapConsumerWrapper {

        private static Logger logger = LoggerFactory.getLogger(DmaapDmeConsumerWrapper.class);

        private final Properties props;

        /**
         * Constructor.
         *
         * @param busTopicParams topic paramters
         *
         * @throws MalformedURLException must provide a valid URL
         */
        public DmaapDmeConsumerWrapper(BusTopicParams busTopicParams) throws MalformedURLException {


            super(busTopicParams);


            final String dme2RouteOffer = (busTopicParams.isAdditionalPropsValid()
                            ? busTopicParams.getAdditionalProps().get(
                                            PolicyEndPointProperties.DME2_ROUTE_OFFER_PROPERTY)
                            : null);

            if (busTopicParams.isEnvironmentInvalid()) {
                throw parmException(busTopicParams.getTopic(),
                        PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX);
            }
            if (busTopicParams.isAftEnvironmentInvalid()) {
                throw parmException(busTopicParams.getTopic(),
                        PolicyEndPointProperties.PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX);
            }
            if (busTopicParams.isLatitudeInvalid()) {
                throw parmException(busTopicParams.getTopic(),
                        PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX);
            }
            if (busTopicParams.isLongitudeInvalid()) {
                throw parmException(busTopicParams.getTopic(),
                        PolicyEndPointProperties.PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX);
            }

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

            props = new Properties();

            props.setProperty(PolicyEndPointProperties.DME2_SERVICE_NAME_PROPERTY, serviceName);

            props.setProperty("username", busTopicParams.getUserName());
            props.setProperty("password", busTopicParams.getPassword());

            /* These are required, no defaults */
            props.setProperty("topic", busTopicParams.getTopic());

            props.setProperty("Environment", busTopicParams.getEnvironment());
            props.setProperty("AFT_ENVIRONMENT", busTopicParams.getAftEnvironment());

            if (busTopicParams.getPartner() != null) {
                props.setProperty("Partner", busTopicParams.getPartner());
            }
            if (dme2RouteOffer != null) {
                props.setProperty(PolicyEndPointProperties.DME2_ROUTE_OFFER_PROPERTY, dme2RouteOffer);
            }

            props.setProperty("Latitude", busTopicParams.getLatitude());
            props.setProperty("Longitude", busTopicParams.getLongitude());

            /* These are optional, will default to these values if not set in additionalProps */
            props.setProperty("AFT_DME2_EP_READ_TIMEOUT_MS", "50000");
            props.setProperty("AFT_DME2_ROUNDTRIP_TIMEOUT_MS", "240000");
            props.setProperty("AFT_DME2_EP_CONN_TIMEOUT", "15000");
            props.setProperty("Version", "1.0");
            props.setProperty("SubContextPath", "/");
            props.setProperty("sessionstickinessrequired", "no");

            /* These should not change */
            props.setProperty("TransportType", "DME2");
            props.setProperty("MethodType", "GET");

            if (busTopicParams.isUseHttps()) {
                props.setProperty(PROTOCOL_PROP, "https");

            } else {
                props.setProperty(PROTOCOL_PROP, "http");
            }

            props.setProperty("contenttype", "application/json");

            if (busTopicParams.isAdditionalPropsValid()) {
                for (Map.Entry<String, String> entry : busTopicParams.getAdditionalProps().entrySet()) {
                    props.put(entry.getKey(), entry.getValue());
                }
            }

            MRClientFactory.prop = props;
            this.consumer.setProps(props);

            logger.info("{}: CREATION", this);
        }

        private IllegalArgumentException parmException(String topic, String propnm) {
            return new IllegalArgumentException("Missing " + PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "."
                    + topic + propnm + " property for DME2 in DMaaP");

        }
    }
}


