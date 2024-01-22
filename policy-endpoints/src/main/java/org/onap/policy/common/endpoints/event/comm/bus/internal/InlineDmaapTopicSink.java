/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modified Copyright (C) 2018 Samsung Electronics Co., Ltd.
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

import java.util.Map;
import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation publishes events for the associated DMAAP topic, inline with the calling
 * thread.
 */
public class InlineDmaapTopicSink extends InlineBusTopicSink implements DmaapTopicSink {

    protected static Logger logger = LoggerFactory.getLogger(InlineDmaapTopicSink.class);

    protected final String userName;
    protected final String password;

    protected String environment = null;
    protected String aftEnvironment = null;
    protected String partner = null;
    protected String latitude = null;
    protected String longitude = null;

    protected Map<String, String> additionalProps = null;

    /**
     * BusTopicParams contains the below mentioned attributes.
     * servers              DMaaP servers
     * topic                DMaaP Topic to be monitored
     * apiKey               DMaaP API Key (optional)
     * apiSecret            DMaaP API Secret (optional)
     * environment          DME2 Environment
     * aftEnvironment       DME2 AFT Environment
     * partner              DME2 Partner
     * latitude             DME2 Latitude
     * longitude            DME2 Longitude
     * additionalProps      Additional properties to pass to DME2
     * useHttps             does connection use HTTPS?
     * allowTracing         is tracing allowed?
     * allowSelfSignedCerts are self-signed certificates allow
     * @param busTopicParams Contains the above mentioned parameters
     * @throws IllegalArgumentException An invalid parameter passed in
     */
    public InlineDmaapTopicSink(BusTopicParams busTopicParams) {

        super(busTopicParams);

        this.userName = busTopicParams.getUserName();
        this.password = busTopicParams.getPassword();

        this.environment = busTopicParams.getEnvironment();
        this.aftEnvironment = busTopicParams.getAftEnvironment();
        this.partner = busTopicParams.getPartner();

        this.latitude = busTopicParams.getLatitude();
        this.longitude = busTopicParams.getLongitude();

        this.additionalProps = busTopicParams.getAdditionalProps();
    }


    @Override
    public void init() {
        if (allNullOrEmpty(this.environment, this.aftEnvironment, this.latitude, this.longitude, this.partner)) {
            this.publisher = new BusPublisher.CambriaPublisherWrapper(BusTopicParams.builder()
                    .servers(this.servers)
                    .topic(this.effectiveTopic)
                    .apiKey(this.apiKey)
                    .apiSecret(this.apiSecret)
                    .userName(this.userName)
                    .password(this.password)
                    .useHttps(this.useHttps)
                    .allowTracing(this.allowTracing)
                    .allowSelfSignedCerts(this.allowSelfSignedCerts)
                    .build());
        } else {
            this.publisher = new BusPublisher.DmaapDmePublisherWrapper(BusTopicParams.builder()
                    .servers(this.servers)
                    .topic(this.effectiveTopic)
                    .userName(this.userName)
                    .password(this.password)
                    .environment(this.environment)
                    .aftEnvironment(this.aftEnvironment)
                    .partner(this.partner)
                    .latitude(this.latitude)
                    .longitude(this.longitude)
                    .additionalProps(this.additionalProps)
                    .useHttps(this.useHttps)
                    .allowTracing(this.allowTracing)
                    .build());
        }

        logger.info("{}: DMAAP SINK created", this);
    }

    @Override
    public CommInfrastructure getTopicCommInfrastructure() {
        return Topic.CommInfrastructure.DMAAP;
    }


    @Override
    public String toString() {
        return "InlineDmaapTopicSink [userName=" + userName + ", password=" + password
                + ", getTopicCommInfrastructure()=" + getTopicCommInfrastructure() + ", toString()=" + super.toString()
                + "]";
    }

}
