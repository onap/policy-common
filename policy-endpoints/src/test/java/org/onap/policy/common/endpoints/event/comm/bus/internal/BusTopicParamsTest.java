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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams.TopicParamsBuilder;

public class BusTopicParamsTest extends BusTopicTestBase {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void test() {
        BusTopicParams params = makeBuilder().build();

        assertEquals(addProps, params.getAdditionalProps());
        assertEquals("aft-env", params.getAftEnvironment());
        assertEquals(true, params.isAllowSelfSignedCerts());
        assertEquals("my-api-key", params.getApiKey());
        assertEquals("my-api-secret", params.getApiSecret());
        assertEquals("my-base", params.getBasePath());
        assertEquals("my-client", params.getClientName());
        assertEquals("my-cons-group", params.getConsumerGroup());
        assertEquals("my-cons-inst", params.getConsumerInstance());
        assertEquals("my-env", params.getEnvironment());
        assertEquals(100, params.getFetchLimit());
        assertEquals(101, params.getFetchTimeout());
        assertEquals("my-host", params.getHostname());
        assertEquals("my-lat", params.getLatitude());
        assertEquals("my-long", params.getLongitude());
        assertEquals(true, params.isManaged());
        assertEquals("my-part", params.getPartitionId());
        assertEquals("my-partner", params.getPartner());
        assertEquals("my-pass", params.getPassword());
        assertEquals(102, params.getPort());
        assertEquals(servers, params.getServers());
        assertEquals("my-topic", params.getTopic());
        assertEquals(true, params.isUseHttps());
        assertEquals("my-user", params.getUserName());

        // ensure that booleans are independent of each other
        testBoolean("true:false:false", (bldr, flag) -> bldr.allowSelfSignedCerts(flag));
        testBoolean("false:true:false", (bldr, flag) -> bldr.managed(flag));
        testBoolean("false:false:true", (bldr, flag) -> bldr.useHttps(flag));

        // test validity methods
        assertTrue(params.isAdditionalPropsValid());
        assertFalse(params.isAftEnvironmentInvalid());
        assertTrue(params.isApiKeyValid());
        assertTrue(params.isApiSecretValid());
        assertFalse(params.isClientNameInvalid());
        assertFalse(params.isConsumerGroupInvalid());
        assertFalse(params.isConsumerInstanceInvalid());
        assertFalse(params.isEnvironmentInvalid());
        assertFalse(params.isHostnameInvalid());
        assertFalse(params.isLatitudeInvalid());
        assertFalse(params.isLongitudeInvalid());
        assertFalse(params.isPartitionIdInvalid());
        assertFalse(params.isPartnerInvalid());
        assertTrue(params.isPasswordValid());
        assertFalse(params.isPortInvalid());
        assertFalse(params.isServersInvalid());
        assertFalse(params.isTopicInvalid());
        assertTrue(params.isUserNameValid());

        // test inverted validity
        assertFalse(makeBuilder().additionalProps(null).build().isAdditionalPropsValid());
        assertTrue(makeBuilder().aftEnvironment("").build().isAftEnvironmentInvalid());
        assertFalse(makeBuilder().apiKey("").build().isApiKeyValid());
        assertFalse(makeBuilder().apiSecret("").build().isApiSecretValid());
        assertTrue(makeBuilder().clientName("").build().isClientNameInvalid());
        assertTrue(makeBuilder().consumerGroup("").build().isConsumerGroupInvalid());
        assertTrue(makeBuilder().consumerInstance("").build().isConsumerInstanceInvalid());
        assertTrue(makeBuilder().environment("").build().isEnvironmentInvalid());
        assertTrue(makeBuilder().hostname("").build().isHostnameInvalid());
        assertTrue(makeBuilder().latitude("").build().isLatitudeInvalid());
        assertTrue(makeBuilder().longitude("").build().isLongitudeInvalid());
        assertTrue(makeBuilder().partitionId("").build().isPartitionIdInvalid());
        assertTrue(makeBuilder().partner("").build().isPartnerInvalid());
        assertFalse(makeBuilder().password("").build().isPasswordValid());
        assertTrue(makeBuilder().port(-1).build().isPortInvalid());
        assertTrue(makeBuilder().port(65536).build().isPortInvalid());
        assertTrue(makeBuilder().servers(null).build().isServersInvalid());
        assertTrue(makeBuilder().servers(new LinkedList<>()).build().isServersInvalid());
        assertTrue(makeBuilder().servers(Arrays.asList("")).build().isServersInvalid());
        assertFalse(makeBuilder().servers(Arrays.asList("one-server")).build().isServersInvalid());
        assertTrue(makeBuilder().topic("").build().isTopicInvalid());
        assertFalse(makeBuilder().userName("").build().isUserNameValid());
    }

    /**
     * Tests the boolean methods by applying a function, once with {@code false} and once
     * with {@code true}. Verifies that all of the boolean methods return the correct
     * value by concatenating them.
     * 
     * @param expectedTrue the string that is expected when {@code true} is passed to the
     *        method
     * @param function function to be applied to the builder
     */
    private void testBoolean(String expectedTrue, BiConsumer<TopicParamsBuilder, Boolean> function) {
        TopicParamsBuilder builder = BusTopicParams.builder();

        // first try the "false" case
        function.accept(builder, false);

        BusTopicParams params = builder.build();
        assertEquals("false:false:false",
                        "" + params.isAllowSelfSignedCerts() + ":" + params.isManaged() + ":" + params.isUseHttps());


        // now try the "true" case
        function.accept(builder, true);

        params = builder.build();
        assertEquals(expectedTrue,
                        "" + params.isAllowSelfSignedCerts() + ":" + params.isManaged() + ":" + params.isUseHttps());
    }
}
