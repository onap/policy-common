/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams.TopicParamsBuilder;

/**
 * Base class for BusTopicXxxTest classes.
 */
public class BusTopicTestBase {

    /**
     * Message used within exceptions that are expected.
     */
    public static final String EXPECTED = "expected exception";
    
    /**
     * Additional properties to be added to the parameter builder.
     */
    protected Map<String, String> addProps;

    /**
     * Servers to be added to the parameter builder.
     */
    protected List<String> servers;

    /**
     * Parameter builder used to build topic parameters.
     */
    protected TopicParamsBuilder builder;

    /**
     * Initializes {@link #addProps}, {@link #servers}, and {@link #builder}.
     */
    public void setUp() {
        addProps = new TreeMap<>();
        addProps.put("my-key-A", "my-value-A");
        addProps.put("my-key-B", "my-value-B");

        servers = Arrays.asList("svra", "svrb");

        builder = makeBuilder();
    }

    /**
     * Makes a fully populated parameter builder.
     * 
     * @return a new parameter builder
     */
    public TopicParamsBuilder makeBuilder() {
        return makeBuilder(addProps, servers);
    }

    /**
     * Makes a fully populated parameter builder.
     * 
     * @param addProps additional properties to be added to the builder
     * @param servers servers to be added to the builder
     * @return a new parameter builder
     */
    public TopicParamsBuilder makeBuilder(Map<String, String> addProps, List<String> servers) {

        return BusTopicParams.builder().additionalProps(addProps).aftEnvironment("aft-env").allowSelfSignedCerts(true)
                        .apiKey("my-api-key").apiSecret("my-api-secret").basePath("my-base").clientName("my-client")
                        .consumerGroup("my-cons-group").consumerInstance("my-cons-inst").environment("my-env")
                        .fetchLimit(100).fetchTimeout(101).hostname("my-host").latitude("my-lat").longitude("my-long")
                        .managed(true).partitionId("my-part").partner("my-partner").password("my-pass").port(102)
                        .servers(servers).topic("my-topic").useHttps(true).userName("my-user");
    }
}
