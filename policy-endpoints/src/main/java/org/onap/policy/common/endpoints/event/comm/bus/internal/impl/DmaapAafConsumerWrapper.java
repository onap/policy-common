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

import com.att.nsa.mr.client.impl.MRConsumerImpl;
import com.att.nsa.mr.test.clients.ProtocolTypeConstants;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MR based consumer
 */
public class DmaapAafConsumerWrapper extends DmaapConsumerWrapper {

    private static Logger logger = LoggerFactory.getLogger(DmaapAafConsumerWrapper.class);

    private final Properties props;

    /**
     * MR Consumer Wrapper
     *
     * @param servers messaging bus hosts
     * @param topic topic
     * @param apiKey API Key
     * @param apiSecret API Secret
     * @param aafLogin AAF Login
     * @param aafPassword AAF Password
     * @param consumerGroup Consumer Group
     * @param consumerInstance Consumer Instance
     * @param fetchTimeout Fetch Timeout
     * @param fetchLimit Fetch Limit
     * @throws MalformedURLException
     */
    public DmaapAafConsumerWrapper(List<String> servers, String topic, String apiKey, String apiSecret, String aafLogin,
            String aafPassword, String consumerGroup, String consumerInstance, int fetchTimeout, int fetchLimit,
            boolean useHttps) throws MalformedURLException {

        super(servers, topic, apiKey, apiSecret, aafLogin, aafPassword, consumerGroup, consumerInstance, fetchTimeout,
                fetchLimit);

        // super constructor sets servers = {""} if empty to avoid errors when using DME2
        if ((servers.size() == 1 && ("".equals(servers.get(0)))) || (servers == null) || (servers.isEmpty())) {
            throw new IllegalArgumentException("Must provide at least one host for HTTP AAF");
        }

        this.consumer.setProtocolFlag(ProtocolTypeConstants.AAF_AUTH.getValue());

        props = new Properties();

        if (useHttps) {
            props.setProperty(PROTOCOL_PROP, "https");
            this.consumer.setHost(servers.get(0) + ":3905");

        } else {
            props.setProperty(PROTOCOL_PROP, "http");
            this.consumer.setHost(servers.get(0) + ":3904");
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
