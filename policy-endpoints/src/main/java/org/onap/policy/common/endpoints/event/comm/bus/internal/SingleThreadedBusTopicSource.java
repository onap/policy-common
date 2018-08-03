/*-
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.onap.policy.common.endpoints.event.comm.FilterableTopicSource;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.bus.BusTopicSource;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer.FilterableBusConsumer;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This topic source implementation specializes in reading messages over a bus topic source and
 * notifying its listeners
 */
public abstract class SingleThreadedBusTopicSource extends BusTopicBase
        implements Runnable, BusTopicSource, FilterableTopicSource {

    /**
     * Not to be converted to PolicyLogger. This will contain all instract /out traffic and only
     * that in a single file in a concise format.
     */
    private static Logger logger = LoggerFactory.getLogger(InlineBusTopicSink.class);
    private static final Logger netLogger = LoggerFactory.getLogger(NETWORK_LOGGER);

    /**
     * Bus consumer group
     */
    protected final String consumerGroup;

    /**
     * Bus consumer instance
     */
    protected final String consumerInstance;

    /**
     * Bus fetch timeout
     */
    protected final int fetchTimeout;

    /**
     * Bus fetch limit
     */
    protected final int fetchLimit;

    /**
     * Message Bus Consumer
     */
    protected BusConsumer consumer;

    /**
     * Independent thread reading message over my topic
     */
    protected Thread busPollerThread;


    /**
     * 
     *
     * @param busTopicParams@throws IllegalArgumentException An invalid parameter passed in
     */
    public SingleThreadedBusTopicSource(BusTopicParams busTopicParams) {

        super(busTopicParams.getServers(), busTopicParams.getTopic(), busTopicParams.getApiKey(), busTopicParams.getApiSecret(), busTopicParams.isUseHttps(), busTopicParams.isAllowSelfSignedCerts());

        if (busTopicParams.getConsumerGroup() == null || busTopicParams.getConsumerGroup().isEmpty()) {
            this.consumerGroup = UUID.randomUUID().toString();
        } else {
            this.consumerGroup = busTopicParams.getConsumerGroup();
        }

        if (busTopicParams.getConsumerInstance() == null || busTopicParams.getConsumerInstance().isEmpty()) {
            this.consumerInstance = NetworkUtil.getHostname();
        } else {
            this.consumerInstance = busTopicParams.getConsumerInstance();
        }

        if (busTopicParams.getFetchTimeout() <= 0) {
            this.fetchTimeout = NO_TIMEOUT_MS_FETCH;
        } else {
            this.fetchTimeout = busTopicParams.getFetchTimeout();
        }

        if (busTopicParams.getFetchLimit() <= 0) {
            this.fetchLimit = NO_LIMIT_FETCH;
        } else {
            this.fetchLimit = busTopicParams.getFetchLimit();
        }

    }

    /**
     * Initialize the Bus client
     */
    public abstract void init() throws MalformedURLException;

    @Override
    public void register(TopicListener topicListener) {

        super.register(topicListener);

        try {
            if (!alive && !locked) {
                this.start();
            } else {
                logger.info("{}: register: start not attempted", this);
            }
        } catch (Exception e) {
            logger.warn("{}: cannot start after registration of because of: {}", this, topicListener, e.getMessage(),
                    e);
        }
    }

    @Override
    public void unregister(TopicListener topicListener) {
        boolean stop;
        synchronized (this) {
            super.unregister(topicListener);
            stop = this.topicListeners.isEmpty();
        }

        if (stop) {
            this.stop();
        }
    }

    @Override
    public boolean start() {
        logger.info("{}: starting", this);

        synchronized (this) {

            if (alive) {
                return true;
            }

            if (locked) {
                throw new IllegalStateException(this + " is locked.");
            }

            if (this.busPollerThread == null || !this.busPollerThread.isAlive() || this.consumer == null) {

                try {
                    this.init();
                    this.alive = true;
                    this.busPollerThread = new Thread(this);
                    this.busPollerThread.setName(this.getTopicCommInfrastructure() + "-source-" + this.getTopic());
                    busPollerThread.start();
                } catch (Exception e) {
                    logger.warn("{}: cannot start because of {}", this, e.getMessage(), e);
                    throw new IllegalStateException(e);
                }
            }
        }

        return this.alive;
    }

    @Override
    public boolean stop() {
        logger.info("{}: stopping", this);

        synchronized (this) {
            BusConsumer consumerCopy = this.consumer;

            this.alive = false;
            this.consumer = null;

            if (consumerCopy != null) {
                try {
                    consumerCopy.close();
                } catch (Exception e) {
                    logger.warn("{}: stop failed because of {}", this, e.getMessage(), e);
                }
            }
        }

        Thread.yield();

        return true;
    }

    /**
     * Run thread method for the Bus Reader
     */
    @Override
    public void run() {
        while (this.alive) {
            try {
                for (String event : this.consumer.fetch()) {
                    synchronized (this) {
                        this.recentEvents.add(event);
                    }

                    netLogger.info("[IN|{}|{}]{}{}", this.getTopicCommInfrastructure(), this.topic,
                            System.lineSeparator(), event);

                    broadcast(event);

                    if (!this.alive) {
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("{}: cannot fetch because of ", this, e.getMessage(), e);
            }
        }

        logger.info("{}: exiting thread", this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean offer(String event) {
        if (!this.alive) {
            throw new IllegalStateException(this + " is not alive.");
        }

        synchronized (this) {
            this.recentEvents.add(event);
        }

        netLogger.info("[IN|{}|{}]{}{}", this.getTopicCommInfrastructure(), this.topic, System.lineSeparator(), event);


        return broadcast(event);
    }


    @Override
    public void setFilter(String filter) {
        if (consumer instanceof FilterableBusConsumer) {
            ((FilterableBusConsumer) consumer).setFilter(filter);

        } else {
            throw new UnsupportedOperationException("no server-side filtering for topic " + topic);
        }
    }

    @Override
    public String toString() {
        return "SingleThreadedBusTopicSource [consumerGroup=" + consumerGroup + ", consumerInstance=" + consumerInstance
                + ", fetchTimeout=" + fetchTimeout + ", fetchLimit=" + fetchLimit + ", consumer=" + this.consumer
                + ", alive=" + alive + ", locked=" + locked + ", uebThread=" + busPollerThread + ", topicListeners="
                + topicListeners.size() + ", toString()=" + super.toString() + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConsumerInstance() {
        return consumerInstance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        this.stop();
        this.topicListeners.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFetchTimeout() {
        return fetchTimeout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFetchLimit() {
        return fetchLimit;
    }

    /**
     * Member variables of this Params class are as follows
     * servers DMaaP servers
     * topic DMaaP Topic to be monitored
     * apiKey DMaaP API Key (optional)
     * apiSecret DMaaP API Secret (optional)
     * consumerGroup DMaaP Reader Consumer Group
     * consumerInstance DMaaP Reader Instance
     * fetchTimeout DMaaP fetch timeout
     * fetchLimit DMaaP fetch limit
     * environment DME2 Environment
     * aftEnvironment DME2 AFT Environment
     * partner DME2 Partner
     * latitude DME2 Latitude
     * longitude DME2 Longitude
     * additionalProps Additional properties to pass to DME2
     * useHttps does connection use HTTPS?
     * allowSelfSignedCerts are self-signed certificates allow
     *
     */
    public static class BusTopicParams {

        public static TopicParamsBuilder builder() {
            return new TopicParamsBuilder();
        }
        private List<String> servers;
        private String topic;
        private String apiKey;
        private String apiSecret;
        private String consumerGroup;
        private String consumerInstance;
        private int fetchTimeout;
        private int fetchLimit;
        private boolean useHttps;
        private boolean allowSelfSignedCerts;

        private String userName;
        private String password;
        private String environment;
        private String aftEnvironment;
        private String partner;
        private String latitude;
        private String longitude;
        private Map<String, String> additionalProps;

        public String getUserName() {
            return userName;
        }

        public String getPassword() {
            return password;
        }

        public String getEnvironment() {
            return environment;
        }

        public String getAftEnvironment() {
            return aftEnvironment;
        }

        public String getPartner() {
            return partner;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public Map<String, String> getAdditionalProps() {
            return additionalProps;
        }

        public List<String> getServers() {
            return servers;
        }

        public String getTopic() {
            return topic;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getApiSecret() {
            return apiSecret;
        }

        public String getConsumerGroup() {
            return consumerGroup;
        }

        public String getConsumerInstance() {
            return consumerInstance;
        }

        public int getFetchTimeout() {
            return fetchTimeout;
        }

        public int getFetchLimit() {
            return fetchLimit;
        }

        public boolean isUseHttps() {
            return useHttps;
        }

        public boolean isAllowSelfSignedCerts() {
            return allowSelfSignedCerts;
        }


        public static class TopicParamsBuilder {
            BusTopicParams m = new BusTopicParams();

            private TopicParamsBuilder() {
            }

            public TopicParamsBuilder servers(List<String> servers) {
                this.m.servers = servers;
                return this;
            }

            public TopicParamsBuilder topic(String topic) {
                this.m.topic = topic;
                return this;
            }

            public TopicParamsBuilder apiKey(String apiKey) {
                this.m.apiKey = apiKey;
                return this;
            }

            public TopicParamsBuilder apiSecret(String apiSecret) {
                this.m.apiSecret = apiSecret;
                return this;
            }

            public TopicParamsBuilder consumerGroup(String consumerGroup) {
                this.m.consumerGroup = consumerGroup;
                return this;
            }

            public TopicParamsBuilder consumerInstance(String consumerInstance) {
                this.m.consumerInstance = consumerInstance;
                return this;
            }

            public TopicParamsBuilder fetchTimeout(int fetchTimeout) {
                this.m.fetchTimeout = fetchTimeout;
                return this;
            }

            public TopicParamsBuilder fetchLimit(int fetchLimit) {
                this.m.fetchLimit = fetchLimit;
                return this;
            }

            public TopicParamsBuilder useHttps(boolean useHttps) {
                this.m.useHttps = useHttps;
                return this;
            }

            public TopicParamsBuilder allowSelfSignedCerts(boolean allowSelfSignedCerts) {
                this.m.allowSelfSignedCerts = allowSelfSignedCerts;
                return this;
            }

            public TopicParamsBuilder userName(String userName) {
                this.m.userName = userName;
                return this;
            }

            public TopicParamsBuilder password(String password) {
                this.m.password = password;
                return this;
            }

            public TopicParamsBuilder environment(String environment) {
                this.m.environment = environment;
                return this;
            }

            public TopicParamsBuilder aftEnvironment(String aftEnvironment) {
                this.m.aftEnvironment = aftEnvironment;
                return this;
            }

            public TopicParamsBuilder partner(String partner) {
                this.m.partner = partner;
                return this;
            }

            public TopicParamsBuilder latitude(String latitude) {
                this.m.latitude = latitude;
                return this;
            }

            public TopicParamsBuilder longitude(String longitude) {
                this.m.longitude = longitude;
                return this;
            }

            public TopicParamsBuilder additionalProps(Map<String, String> additionalProps) {
                this.m.additionalProps = additionalProps;
                return this;
            }

            public BusTopicParams build() {
                return m;
            }

        }

    }
}
