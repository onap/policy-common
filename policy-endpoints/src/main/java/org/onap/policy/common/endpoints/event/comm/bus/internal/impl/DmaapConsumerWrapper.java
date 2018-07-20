/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.endpoints.event.comm.bus.internal.impl;

import com.att.nsa.mr.client.impl.MRConsumerImpl;
import com.att.nsa.mr.client.response.MRConsumerResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MR based consumer
 */
public abstract class DmaapConsumerWrapper implements BusConsumer {

    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(DmaapConsumerWrapper.class);

    /**
     * Name of the "protocol" property.
     */
    protected static final String PROTOCOL_PROP = "Protocol";

    /**
     * fetch timeout
     */
    protected int fetchTimeout;

    /**
     * close condition
     */
    protected Object closeCondition = new Object();

    /**
     * MR Consumer
     */
    protected MRConsumerImpl consumer;

    /**
     * MR Consumer Wrapper
     *
     * @param servers messaging bus hosts
     * @param topic topic
     * @param apiKey API Key
     * @param apiSecret API Secret
     * @param username AAF Login
     * @param password AAF Password
     * @param consumerGroup Consumer Group
     * @param consumerInstance Consumer Instance
     * @param fetchTimeout Fetch Timeout
     * @param fetchLimit Fetch Limit
     * @throws MalformedURLException
     */
    public DmaapConsumerWrapper(List<String> servers, String topic, String apiKey, String apiSecret, String username,
            String password, String consumerGroup, String consumerInstance, int fetchTimeout, int fetchLimit)
            throws MalformedURLException {

        this.fetchTimeout = fetchTimeout;

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("No topic for DMaaP");
        }

        this.consumer = new MRConsumerImpl(servers, topic, consumerGroup, consumerInstance, fetchTimeout, fetchLimit,
                null, apiKey, apiSecret);

        this.consumer.setUsername(username);
        this.consumer.setPassword(password);
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
            logger.debug("DMaaP consumer received {} : {}" + response.getResponseCode(), response.getResponseMessage());

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
