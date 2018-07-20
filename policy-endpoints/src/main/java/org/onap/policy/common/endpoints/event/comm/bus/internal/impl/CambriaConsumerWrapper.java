/*
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

package org.onap.policy.common.endpoints.event.comm.bus.internal.impl;

import com.att.nsa.cambria.client.CambriaClientBuilders;
import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
import com.att.nsa.cambria.client.CambriaConsumer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.onap.policy.common.endpoints.event.comm.bus.internal.FilterableBusConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cambria based consumer
 */
public class CambriaConsumerWrapper implements FilterableBusConsumer {

    /**
     * logger
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
     * Cambria client
     */
    private CambriaConsumer consumer;

    /**
     * Cambria client to use for next fetch
     */
    private CambriaConsumer newConsumer = null;

    /**
     * fetch timeout
     */
    protected int fetchTimeout;

    /**
     * close condition
     */
    protected Object closeCondition = new Object();

    /**
     * Cambria Consumer Wrapper
     *
     * @param servers messaging bus hosts
     * @param topic topic
     * @param apiKey API Key
     * @param apiSecret API Secret
     * @param consumerGroup Consumer Group
     * @param consumerInstance Consumer Instance
     * @param fetchTimeout Fetch Timeout
     * @param fetchLimit Fetch Limit
     * @throws GeneralSecurityException
     * @throws MalformedURLException
     */
    public CambriaConsumerWrapper(List<String> servers, String topic, String apiKey, String apiSecret,
            String consumerGroup, String consumerInstance, int fetchTimeout, int fetchLimit, boolean useHttps,
            boolean useSelfSignedCerts) {
        this(servers, topic, apiKey, apiSecret, null, null, consumerGroup, consumerInstance, fetchTimeout, fetchLimit,
                useHttps, useSelfSignedCerts);
    }

    public CambriaConsumerWrapper(List<String> servers, String topic, String apiKey, String apiSecret, String username,
            String password, String consumerGroup, String consumerInstance, int fetchTimeout, int fetchLimit,
            boolean useHttps, boolean useSelfSignedCerts) {

        this.fetchTimeout = fetchTimeout;

        this.builder = new CambriaClientBuilders.ConsumerBuilder();

        builder.knownAs(consumerGroup, consumerInstance).usingHosts(servers).onTopic(topic).waitAtServer(fetchTimeout)
                .receivingAtMost(fetchLimit);

        // Set read timeout to fetch timeout + 30 seconds (TBD: this should be configurable)
        builder.withSocketTimeout(fetchTimeout + 30000);

        if (useHttps) {
            builder.usingHttps();

            if (useSelfSignedCerts) {
                builder.allowSelfSignedCertificates();
            }
        }

        if (apiKey != null && !apiKey.isEmpty() && apiSecret != null && !apiSecret.isEmpty()) {
            builder.authenticatedBy(apiKey, apiSecret);
        }

        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            builder.authenticatedByHttp(username, password);
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
             * Since an exception occurred, "consumer" still has its old value, thus it should not
             * be closed at this point.
             */
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String toString() {
        return "CambriaConsumerWrapper [fetchTimeout=" + fetchTimeout + "]";
    }
}
