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

import com.att.nsa.mr.client.impl.MRSimplerBatchPublisher;
import com.att.nsa.mr.client.response.MRPublisherResponse;
import com.att.nsa.mr.test.clients.ProtocolTypeConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.onap.policy.common.endpoints.event.comm.bus.internal.BusPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DmaapClient library wrapper
 */
public abstract class DmaapPublisherWrapper implements BusPublisher {

    private static Logger logger = LoggerFactory.getLogger(DmaapPublisherWrapper.class);

    /**
     * MR based Publisher
     */
    protected MRSimplerBatchPublisher publisher;
    protected Properties props;

    /**
     * MR Publisher Wrapper
     *
     * @param servers messaging bus hosts
     * @param topic topic
     * @param username AAF or DME2 Login
     * @param password AAF or DME2 Password
     */
    public DmaapPublisherWrapper(ProtocolTypeConstants protocol, List<String> servers, String topic, String username,
            String password, boolean useHttps) {


        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("No topic for DMaaP");
        }


        if (protocol == ProtocolTypeConstants.AAF_AUTH) {
            if (servers == null || servers.isEmpty()) {
                throw new IllegalArgumentException("No DMaaP servers or DME2 partner provided");
            }

            ArrayList<String> dmaapServers = new ArrayList<>();
            if (useHttps) {
                for (String server : servers) {
                    dmaapServers.add(server + ":3905");
                }

            } else {
                for (String server : servers) {
                    dmaapServers.add(server + ":3904");
                }
            }


            this.publisher = new MRSimplerBatchPublisher.Builder().againstUrls(dmaapServers).onTopic(topic).build();

            this.publisher.setProtocolFlag(ProtocolTypeConstants.AAF_AUTH.getValue());
        } else if (protocol == ProtocolTypeConstants.DME2) {
            ArrayList<String> dmaapServers = new ArrayList<>();
            dmaapServers.add("0.0.0.0:3904");

            this.publisher = new MRSimplerBatchPublisher.Builder().againstUrls(dmaapServers).onTopic(topic).build();

            this.publisher.setProtocolFlag(ProtocolTypeConstants.DME2.getValue());
        }

        this.publisher.logTo(LoggerFactory.getLogger(MRSimplerBatchPublisher.class.getName()));

        this.publisher.setUsername(username);
        this.publisher.setPassword(password);

        props = new Properties();

        if (useHttps) {
            props.setProperty("Protocol", "https");
        } else {
            props.setProperty("Protocol", "http");
        }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        logger.info("{}: CLOSE", this);

        try {
            this.publisher.close(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("{}: CLOSE FAILED because of {}", this, e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean send(String partitionId, String message) {
        if (message == null) {
            throw new IllegalArgumentException("No message provided");
        }

        this.publisher.setPubResponse(new MRPublisherResponse());
        this.publisher.send(partitionId, message);
        MRPublisherResponse response = this.publisher.sendBatchWithResponse();
        if (response != null) {
            logger.debug("DMaaP publisher received {} : {}", response.getResponseCode(), response.getResponseMessage());
        }

        return true;
    }

    @Override
    public String toString() {
        return "DmaapPublisherWrapper [" + "publisher.getAuthDate()=" + publisher.getAuthDate()
                + ", publisher.getAuthKey()=" + publisher.getAuthKey() + ", publisher.getHost()=" + publisher.getHost()
                + ", publisher.getProtocolFlag()=" + publisher.getProtocolFlag() + ", publisher.getUsername()="
                + publisher.getUsername() + "]";
    }
}
