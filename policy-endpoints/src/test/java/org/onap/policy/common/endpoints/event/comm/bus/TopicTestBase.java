/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm.bus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams.TopicParamsBuilder;

/**
 * Base class for Topic Test classes.
 */
public class TopicTestBase {

    public static final String MY_AFT_ENV = "my-aft-env";
    public static final String MY_API_KEY = "my-api-key";
    public static final String MY_API_SECRET = "my-api-secret";
    public static final String MY_BASE_PATH = "my-base";
    public static final String MY_CLIENT_NAME = "my-client";
    public static final String MY_CONS_GROUP = "my-cons-group";
    public static final String MY_CONS_INST = "my-cons-inst";
    public static final String MY_ENV = "my-env";
    public static final int MY_FETCH_LIMIT = 100;
    public static final int MY_FETCH_TIMEOUT = 101;
    public static final String MY_HOST = "my-host";
    public static final String MY_LAT = "my-lat";
    public static final String MY_LONG = "my-long";
    public static final String MY_PARTNER = "my-partner";
    public static final String MY_PASS = "my-pass";
    public static final int MY_PORT = 102;
    public static final String MY_TOPIC = "my-topic";
    public static final String MY_EFFECTIVE_TOPIC = "my-effective-topic";
    public static final String MY_USERNAME = "my-user";

    public static final String MY_MESSAGE = "my-message";
    public static final String MY_PARTITION = "my-partition";
    public static final String MY_MESSAGE2 = "my-message-2";
    public static final String MY_PARTITION2 = "my-partition-2";

    public static final String ROUTE_PROP = "routeOffer";
    public static final String MY_ROUTE = "my-route";

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

        return BusTopicParams.builder().additionalProps(addProps).aftEnvironment(MY_AFT_ENV).allowSelfSignedCerts(true)
                        .apiKey(MY_API_KEY).apiSecret(MY_API_SECRET).basePath(MY_BASE_PATH).clientName(MY_CLIENT_NAME)
                        .consumerGroup(MY_CONS_GROUP).consumerInstance(MY_CONS_INST).environment(MY_ENV)
                        .fetchLimit(MY_FETCH_LIMIT).fetchTimeout(MY_FETCH_TIMEOUT).hostname(MY_HOST).latitude(MY_LAT)
                        .longitude(MY_LONG).managed(true).partitionId(MY_PARTITION).partner(MY_PARTNER)
                        .password(MY_PASS).port(MY_PORT).servers(servers).topic(MY_TOPIC)
                        .effectiveTopic(MY_EFFECTIVE_TOPIC).useHttps(true).userName(MY_USERNAME);
    }
}
