/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018-2019 Samsung Electronics Co., Ltd.
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
import java.util.Map;
import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This topic reader implementation specializes in reading messages over DMAAP topic and notifying
 * its listeners.
 */
public class SingleThreadedDmaapTopicSource extends SingleThreadedBusTopicSource implements DmaapTopicSource, Runnable {

    private static Logger logger = LoggerFactory.getLogger(SingleThreadedDmaapTopicSource.class);


    protected final String userName;
    protected final String password;

    protected String environment = null;
    protected String aftEnvironment = null;
    protected String partner = null;
    protected String latitude = null;
    protected String longitude = null;

    protected Map<String, String> additionalProps = null;


    /**
     * Constructor.
     *
     * @param busTopicParams Parameters object containing all the required inputs
     *
     * @throws IllegalArgumentException An invalid parameter passed in
     */
    public SingleThreadedDmaapTopicSource(BusTopicParams busTopicParams) {

        super(busTopicParams);

        this.userName = busTopicParams.getUserName();
        this.password = busTopicParams.getPassword();

        this.environment = busTopicParams.getEnvironment();
        this.aftEnvironment = busTopicParams.getAftEnvironment();
        this.partner = busTopicParams.getPartner();

        this.latitude = busTopicParams.getLatitude();
        this.longitude = busTopicParams.getLongitude();

        this.additionalProps = busTopicParams.getAdditionalProps();
        try {
            this.init();
        } catch (Exception e) {
            throw new IllegalArgumentException("ERROR during init in dmaap-source: cannot create topic " + topic, e);
        }
    }


    /**
     * Initialize the Cambria or MR Client.
     */
    @Override
    public void init() throws MalformedURLException {
        BusTopicParams.TopicParamsBuilder builder = BusTopicParams.builder()
            .servers(this.servers)
            .topic(this.effectiveTopic)
            .apiKey(this.apiKey)
            .apiSecret(this.apiSecret)
            .consumerGroup(this.consumerGroup)
            .consumerInstance(this.consumerInstance)
            .fetchTimeout(this.fetchTimeout)
            .fetchLimit(this.fetchLimit)
            .useHttps(this.useHttps);

        if (anyNullOrEmpty(this.userName, this.password)) {
            this.consumer = new BusConsumer.CambriaConsumerWrapper(builder
                    .allowSelfSignedCerts(this.allowSelfSignedCerts)
                    .build());
        } else if (allNullOrEmpty(this.environment, this.aftEnvironment, this.latitude, this.longitude, this.partner)) {
            this.consumer = new BusConsumer.CambriaConsumerWrapper(builder
                    .userName(this.userName)
                    .password(this.password)
                    .allowSelfSignedCerts(this.allowSelfSignedCerts)
                    .build());
        } else {
            this.consumer = new BusConsumer.DmaapDmeConsumerWrapper(builder
                    .userName(this.userName)
                    .password(this.password)
                    .environment(this.environment)
                    .aftEnvironment(this.aftEnvironment)
                    .partner(this.partner)
                    .latitude(this.latitude)
                    .longitude(this.longitude)
                    .additionalProps(this.additionalProps)
                    .build());
        }

        logger.info("{}: INITTED", this);
    }

    @Override
    public CommInfrastructure getTopicCommInfrastructure() {
        return Topic.CommInfrastructure.DMAAP;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append("SingleThreadedDmaapTopicSource [userName=").append(userName).append(", password=")
                .append((password == null || password.isEmpty()) ? "-" : password.length())
                .append(", getTopicCommInfrastructure()=").append(getTopicCommInfrastructure()).append(", toString()=")
                .append(super.toString()).append("]");
        return builder.toString();
    }


}
