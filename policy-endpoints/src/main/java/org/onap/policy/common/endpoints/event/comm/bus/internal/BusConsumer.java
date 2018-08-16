/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Samsung Electronics Co., Ltd.
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
import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.impl.MRConsumerImpl;
import com.att.nsa.mr.client.response.MRConsumerResponse;
import com.att.nsa.mr.test.clients.ProtocolTypeConstants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSinkFactory;
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
     * @throws Exception when error encountered by underlying libraries
     */
    public Iterable<String> fetch() throws InterruptedException, IOException;

    /**
     * close underlying library consumer.
     */
    public void close();

    /**
     * BusConsumer that supports server-side filtering.
     */
    public interface FilterableBusConsumer extends BusConsumer {

        /**
         * Sets the server-side filter.
         *
         * @param filter new filter value, or {@code null}
         * @throws IllegalArgumentException if the consumer cannot be built with the new filter
         */
        public void setFilter(String filter);
    }

    /**
     * Cambria based consumer.
     */
    public static class CambriaConsumerWrapper implements FilterableBusConsumer {

        /**
         * logger.
         */
        private static Logger logger = LoggerFactory.getLogger(CambriaConsumerWrapper.class);

        /**
         * Used to build the consumer.
         */
        private final ConsumerBuilder builder;

        /**
         * Locked while updating {@link #consumer} and {@link #newConsumer}.
         */
        private final Object consLocker = new Object();

        /**
         * Cambria client.
         */
        private CambriaConsumer consumer;

        /**
         * Cambria client to use for next fetch.
         */
        private CambriaConsumer newConsumer = null;

        /**
         * fetch timeout.
         */
        protected int fetchTimeout;

        /**
         * close condition.
         */
        protected Object closeCondition = new Object();

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

            this.fetchTimeout = busTopicParams.getFetchTimeout();

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
        public Iterable<String> fetch() throws IOException, InterruptedException {
            try {
                return getCurrentConsumer().fetch();
            } catch (final IOException e) {
                logger.error("{}: cannot fetch because of {} - backoff for {} ms.", this, e.getMessage(),
                        this.fetchTimeout);
                synchronized (this.closeCondition) {
                    this.closeCondition.wait(this.fetchTimeout);
                }

                throw e;
            }
        }

        @Override
        public void close() {
            synchronized (closeCondition) {
                closeCondition.notifyAll();
            }

            getCurrentConsumer().close();
        }

        private CambriaConsumer getCurrentConsumer() {
            CambriaConsumer old = null;
            CambriaConsumer ret;

            synchronized (consLocker) {
                if (this.newConsumer != null) {
                    // replace old consumer with new consumer
                    old = this.consumer;
                    this.consumer = this.newConsumer;
                    this.newConsumer = null;
                }

                ret = this.consumer;
            }

            if (old != null) {
                old.close();
            }

            return ret;
        }

        @Override
        public void setFilter(String filter) {
            logger.info("{}: setting DMAAP server-side filter: {}", this, filter);
            builder.withServerSideFilter(filter);

            try {
                CambriaConsumer previous;
                synchronized (consLocker) {
                    previous = this.newConsumer;
                    this.newConsumer = builder.build();
                }

                if (previous != null) {
                    // there was already a new consumer - close it
                    previous.close();
                }

            } catch (MalformedURLException | GeneralSecurityException e) {
                /*
                 * Since an exception occurred, "consumer" still has its old value, thus it should
                 * not be closed at this point.
                 */
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public String toString() {
            return "CambriaConsumerWrapper [fetchTimeout=" + fetchTimeout + "]";
        }
    }

    /**
     * MR based consumer.
     */
    public abstract class DmaapConsumerWrapper implements BusConsumer {

        /**
         * logger.
         */
        private static Logger logger = LoggerFactory.getLogger(DmaapConsumerWrapper.class);

        /**
         * Name of the "protocol" property.
         */
        protected static final String PROTOCOL_PROP = "Protocol";

        /**
         * fetch timeout.
         */
        protected int fetchTimeout;

        /**
         * close condition.
         */
        protected Object closeCondition = new Object();

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
        public DmaapConsumerWrapper(BusTopicParams busTopicParams) throws MalformedURLException {

            this.fetchTimeout = busTopicParams.getFetchTimeout();

            if (busTopicParams.isTopicInvalid()) {
                throw new IllegalArgumentException("No topic for DMaaP");
            }

            this.consumer = new MRConsumerImpl(busTopicParams.getServers(), busTopicParams.getTopic(),
                    busTopicParams.getConsumerGroup(), busTopicParams.getConsumerInstance(),
                    busTopicParams.getFetchTimeout(), busTopicParams.getFetchLimit(), null,
                    busTopicParams.getApiKey(), busTopicParams.getApiSecret());

            this.consumer.setUsername(busTopicParams.getUserName());
            this.consumer.setPassword(busTopicParams.getPassword());
        }

        @Override
        public Iterable<String> fetch() throws InterruptedException, IOException {
            final MRConsumerResponse response = this.consumer.fetchWithReturnConsumerResponse();
            if (response == null) {
                logger.warn("{}: DMaaP NULL response received", this);

                synchronized (closeCondition) {
                    closeCondition.wait(fetchTimeout);
                }
                return new ArrayList<>();
            } else {
                logger.debug("DMaaP consumer received {} : {}" + response.getResponseCode(),
                        response.getResponseMessage());

                if (response.getResponseCode() == null || !"200".equals(response.getResponseCode())) {

                    logger.error("DMaaP consumer received: {} : {}", response.getResponseCode(),
                            response.getResponseMessage());

                    synchronized (closeCondition) {
                        closeCondition.wait(fetchTimeout);
                    }

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
            synchronized (closeCondition) {
                closeCondition.notifyAll();
            }

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


            final String dme2RouteOffer = busTopicParams.getAdditionalProps()
                    .get(DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY);

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

            props.setProperty(DmaapTopicSinkFactory.DME2_SERVICE_NAME_PROPERTY, serviceName);

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
                props.setProperty(DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY, dme2RouteOffer);
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


