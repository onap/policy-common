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

import com.att.nsa.apiClient.http.HttpClient.ConnectionType;
import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.cambria.client.CambriaClientBuilders;
import com.att.nsa.cambria.client.CambriaClientBuilders.PublisherBuilder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.onap.policy.common.endpoints.event.comm.bus.internal.BusPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cambria based library publisher
 */
public class CambriaPublisherWrapper implements BusPublisher {

    private static Logger logger = LoggerFactory.getLogger(CambriaPublisherWrapper.class);

    /**
     * The actual Cambria publisher
     */
    @JsonIgnore
    protected volatile CambriaBatchingPublisher publisher;

    public CambriaPublisherWrapper(List<String> servers, String topic, String apiKey, String apiSecret,
            boolean useHttps) {
        this(servers, topic, apiKey, apiSecret, null, null, useHttps, false);
    }

    public CambriaPublisherWrapper(List<String> servers, String topic, String apiKey, String apiSecret, String username,
            String password, boolean useHttps, boolean selfSignedCerts) {

        PublisherBuilder builder = new CambriaClientBuilders.PublisherBuilder();

        builder.usingHosts(servers).onTopic(topic);

        // Set read timeout to 30 seconds (TBD: this should be configurable)
        builder.withSocketTimeout(30000);

        if (useHttps) {
            if (selfSignedCerts) {
                builder.withConnectionType(ConnectionType.HTTPS_NO_VALIDATION);
            } else {
                builder.withConnectionType(ConnectionType.HTTPS);
            }
        }


        if (apiKey != null && !apiKey.isEmpty() && apiSecret != null && !apiSecret.isEmpty()) {
            builder.authenticatedBy(apiKey, apiSecret);
        }

        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            builder.authenticatedByHttp(username, password);
        }

        try {
            this.publisher = builder.build();
        } catch (MalformedURLException | GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
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

        try {
            this.publisher.send(partitionId, message);
        } catch (Exception e) {
            logger.warn("{}: SEND of {} cannot be performed because of {}", this, message, e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
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
