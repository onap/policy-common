/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Samsung Electronics Co., Ltd.
 * Modifications Copyright (C) 2020,2023 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2022-2023 Nordix Foundation.
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

import com.att.nsa.apiClient.http.HttpClient.ConnectionType;
import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.cambria.client.CambriaClientBuilders;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.onap.dmaap.mr.client.impl.MRSimplerBatchPublisher;
import org.onap.dmaap.mr.client.response.MRPublisherResponse;
import org.onap.dmaap.mr.test.clients.ProtocolTypeConstants;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.annotation.GsonJsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface BusPublisher {

    public static final String NO_MESSAGE_PROVIDED = "No message provided";

    /**
     * sends a message.
     *
     * @param partitionId id
     * @param message the message
     * @return true if success, false otherwise
     * @throws IllegalArgumentException if no message provided
     */
    public boolean send(String partitionId, String message);

    /**
     * closes the publisher.
     */
    public void close();

    /**
     * Cambria based library publisher.
     */
    public static class CambriaPublisherWrapper implements BusPublisher {

        private static Logger logger = LoggerFactory.getLogger(CambriaPublisherWrapper.class);

        /**
         * The actual Cambria publisher.
         */
        @GsonJsonIgnore
        protected CambriaBatchingPublisher publisher;

        /**
         * Constructor.
         *
         * @param busTopicParams topic parameters
         */
        public CambriaPublisherWrapper(BusTopicParams busTopicParams) {

            var builder = new CambriaClientBuilders.PublisherBuilder();

            builder.usingHosts(busTopicParams.getServers()).onTopic(busTopicParams.getTopic());

            // Set read timeout to 30 seconds (TBD: this should be configurable)
            builder.withSocketTimeout(30000);

            if (busTopicParams.isUseHttps()) {
                if (busTopicParams.isAllowSelfSignedCerts()) {
                    builder.withConnectionType(ConnectionType.HTTPS_NO_VALIDATION);
                } else {
                    builder.withConnectionType(ConnectionType.HTTPS);
                }
            }

            if (busTopicParams.isApiKeyValid() && busTopicParams.isApiSecretValid()) {
                builder.authenticatedBy(busTopicParams.getApiKey(), busTopicParams.getApiSecret());
            }

            if (busTopicParams.isUserNameValid() && busTopicParams.isPasswordValid()) {
                builder.authenticatedByHttp(busTopicParams.getUserName(), busTopicParams.getPassword());
            }

            try {
                this.publisher = builder.build();
            } catch (MalformedURLException | GeneralSecurityException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public boolean send(String partitionId, String message) {
            if (message == null) {
                throw new IllegalArgumentException(NO_MESSAGE_PROVIDED);
            }

            try {
                this.publisher.send(partitionId, message);
            } catch (Exception e) {
                logger.warn("{}: SEND of {} cannot be performed because of {}", this, message, e.getMessage(), e);
                return false;
            }
            return true;
        }

        @Override
        public void close() {
            logger.info("{}: CLOSE", this);

            try {
                this.publisher.close();
            } catch (Exception e) {
                logger.warn("{}: CLOSE FAILED because of {}", this, e.getMessage(), e);
            }
        }

        @Override
        public String toString() {
            return "CambriaPublisherWrapper []";
        }

    }

    /**
     * Kafka based library publisher.
     */
    public static class KafkaPublisherWrapper implements BusPublisher {

        private static Logger logger = LoggerFactory.getLogger(KafkaPublisherWrapper.class);
        private static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";

        private String topic;

        /**
         * Kafka publisher.
         */
        private Producer<String, String> producer;
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
                for (Map.Entry<String, String> entry : busTopicParams.getAdditionalProps().entrySet()) {
                    kafkaProps.put(entry.getKey(), entry.getValue());
                }
            }
            if (kafkaProps.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG) == null) {
                kafkaProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
            }
            if (kafkaProps.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG) == null) {
                kafkaProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
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
            logger.info("{}: CLOSE", this);

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

    /**
     * DmaapClient library wrapper.
     */
    public abstract class DmaapPublisherWrapper implements BusPublisher {

        private static Logger logger = LoggerFactory.getLogger(DmaapPublisherWrapper.class);

        /**
         * MR based Publisher.
         */
        protected MRSimplerBatchPublisher publisher;
        protected Properties props;

        /**
         * MR Publisher Wrapper.
         *
         * @param servers messaging bus hosts
         * @param topic topic
         * @param username AAF or DME2 Login
         * @param password AAF or DME2 Password
         */
        protected DmaapPublisherWrapper(ProtocolTypeConstants protocol, List<String> servers, String topic,
                String username, String password, boolean useHttps) {

            if (StringUtils.isBlank(topic)) {
                throw new IllegalArgumentException("No topic for DMaaP");
            }

            configureProtocol(topic, protocol, servers, useHttps);

            this.publisher.logTo(LoggerFactory.getLogger(MRSimplerBatchPublisher.class.getName()));

            this.publisher.setUsername(username);
            this.publisher.setPassword(password);

            props = new Properties();

            props.setProperty("Protocol", (useHttps ? "https" : "http"));
            props.setProperty("contenttype", "application/json");
            props.setProperty("username", username);
            props.setProperty("password", password);

            props.setProperty("topic", topic);

            this.publisher.setProps(props);

            if (protocol == ProtocolTypeConstants.AAF_AUTH) {
                this.publisher.setHost(servers.get(0));
            }

            logger.info("{}: CREATION: using protocol {}", this, protocol.getValue());
        }

        private void configureProtocol(String topic, ProtocolTypeConstants protocol, List<String> servers,
                boolean useHttps) {

            if (protocol == ProtocolTypeConstants.AAF_AUTH) {
                if (servers == null || servers.isEmpty()) {
                    throw new IllegalArgumentException("No DMaaP servers or DME2 partner provided");
                }

                ArrayList<String> dmaapServers = new ArrayList<>();
                String port = useHttps ? ":3905" : ":3904";
                for (String server : servers) {
                    dmaapServers.add(server + port);
                }

                this.publisher = new MRSimplerBatchPublisher.Builder().againstUrls(dmaapServers).onTopic(topic).build();

                this.publisher.setProtocolFlag(ProtocolTypeConstants.AAF_AUTH.getValue());

            } else if (protocol == ProtocolTypeConstants.DME2) {
                ArrayList<String> dmaapServers = new ArrayList<>();
                dmaapServers.add("0.0.0.0:3904");

                this.publisher = new MRSimplerBatchPublisher.Builder().againstUrls(dmaapServers).onTopic(topic).build();

                this.publisher.setProtocolFlag(ProtocolTypeConstants.DME2.getValue());

            } else {
                throw new IllegalArgumentException("Invalid DMaaP protocol " + protocol);
            }
        }

        @Override
        public void close() {
            logger.info("{}: CLOSE", this);

            try {
                this.publisher.close(1, TimeUnit.SECONDS);

            } catch (InterruptedException e) {
                logger.warn("{}: CLOSE FAILED", this, e);
                Thread.currentThread().interrupt();

            } catch (Exception e) {
                logger.warn("{}: CLOSE FAILED", this, e);
            }
        }

        @Override
        public boolean send(String partitionId, String message) {
            if (message == null) {
                throw new IllegalArgumentException(NO_MESSAGE_PROVIDED);
            }

            this.publisher.setPubResponse(new MRPublisherResponse());
            this.publisher.send(partitionId, message);
            MRPublisherResponse response = this.publisher.sendBatchWithResponse();
            if (response != null) {
                logger.debug("DMaaP publisher received {} : {}", response.getResponseCode(),
                        response.getResponseMessage());
            }

            return true;
        }

        @Override
        public String toString() {
            return "DmaapPublisherWrapper [" + "publisher.getAuthDate()=" + publisher.getAuthDate()
                    + ", publisher.getAuthKey()=" + publisher.getAuthKey() + ", publisher.getHost()="
                    + publisher.getHost() + ", publisher.getProtocolFlag()=" + publisher.getProtocolFlag()
                    + ", publisher.getUsername()=" + publisher.getUsername() + "]";
        }
    }

    /**
     * DmaapClient library wrapper.
     */
    public static class DmaapAafPublisherWrapper extends DmaapPublisherWrapper {
        /**
         * MR based Publisher.
         */
        public DmaapAafPublisherWrapper(List<String> servers, String topic, String aafLogin, String aafPassword,
                boolean useHttps) {

            super(ProtocolTypeConstants.AAF_AUTH, servers, topic, aafLogin, aafPassword, useHttps);
        }
    }

    public static class DmaapDmePublisherWrapper extends DmaapPublisherWrapper {

        /**
         * Constructor.
         *
         * @param busTopicParams topic parameters
         */
        public DmaapDmePublisherWrapper(BusTopicParams busTopicParams) {

            super(ProtocolTypeConstants.DME2, busTopicParams.getServers(), busTopicParams.getTopic(),
                    busTopicParams.getUserName(), busTopicParams.getPassword(), busTopicParams.isUseHttps());

            String dme2RouteOffer = busTopicParams.isAdditionalPropsValid()
                    ? busTopicParams.getAdditionalProps().get(PolicyEndPointProperties.DME2_ROUTE_OFFER_PROPERTY)
                    : null;

            validateParams(busTopicParams, dme2RouteOffer);

            String serviceName = busTopicParams.getServers().get(0);

            /* These are required, no defaults */
            props.setProperty("Environment", busTopicParams.getEnvironment());
            props.setProperty("AFT_ENVIRONMENT", busTopicParams.getAftEnvironment());

            props.setProperty(PolicyEndPointProperties.DME2_SERVICE_NAME_PROPERTY, serviceName);

            if (busTopicParams.getPartner() != null) {
                props.setProperty("Partner", busTopicParams.getPartner());
            }
            if (dme2RouteOffer != null) {
                props.setProperty(PolicyEndPointProperties.DME2_ROUTE_OFFER_PROPERTY, dme2RouteOffer);
            }

            props.setProperty("Latitude", busTopicParams.getLatitude());
            props.setProperty("Longitude", busTopicParams.getLongitude());

            // ServiceName also a default, found in additionalProps

            /* These are optional, will default to these values if not set in optionalProps */
            props.setProperty("AFT_DME2_EP_READ_TIMEOUT_MS", "50000");
            props.setProperty("AFT_DME2_ROUNDTRIP_TIMEOUT_MS", "240000");
            props.setProperty("AFT_DME2_EP_CONN_TIMEOUT", "15000");
            props.setProperty("Version", "1.0");
            props.setProperty("SubContextPath", "/");
            props.setProperty("sessionstickinessrequired", "no");

            /* These should not change */
            props.setProperty("TransportType", "DME2");
            props.setProperty("MethodType", "POST");

            if (busTopicParams.isAdditionalPropsValid()) {
                addAdditionalProps(busTopicParams);
            }

            this.publisher.setProps(props);
        }

        private void validateParams(BusTopicParams busTopicParams, String dme2RouteOffer) {
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

            if ((busTopicParams.isPartnerInvalid()) && StringUtils.isBlank(dme2RouteOffer)) {
                throw new IllegalArgumentException("Must provide at least "
                        + PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS + "." + busTopicParams.getTopic()
                        + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_PARTNER_SUFFIX + " or "
                        + PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "." + busTopicParams.getTopic()
                        + PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX + " for DME2");
            }
        }

        private void addAdditionalProps(BusTopicParams busTopicParams) {
            for (Map.Entry<String, String> entry : busTopicParams.getAdditionalProps().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (value != null) {
                    props.setProperty(key, value);
                }
            }
        }

        private IllegalArgumentException parmException(String topic, String propnm) {
            return new IllegalArgumentException("Missing " + PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS + "."
                    + topic + propnm + " property for DME2 in DMaaP");

        }
    }
}
