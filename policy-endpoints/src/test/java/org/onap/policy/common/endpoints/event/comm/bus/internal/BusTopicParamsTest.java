/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation.
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

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams.TopicParamsBuilder;

public class BusTopicParamsTest extends TopicTestBase {

    @Before
    @Override
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testGetters() {
        BusTopicParams params = makeBuilder().build();

        assertEquals(addProps, params.getAdditionalProps());
        assertEquals(MY_AFT_ENV, params.getAftEnvironment());
        assertTrue(params.isAllowSelfSignedCerts());
        assertEquals(MY_API_KEY, params.getApiKey());
        assertEquals(MY_API_SECRET, params.getApiSecret());
        assertEquals(MY_BASE_PATH, params.getBasePath());
        assertEquals(MY_CLIENT_NAME, params.getClientName());
        assertEquals(MY_CONS_GROUP, params.getConsumerGroup());
        assertEquals(MY_CONS_INST, params.getConsumerInstance());
        assertEquals(MY_ENV, params.getEnvironment());
        assertEquals(MY_FETCH_LIMIT, params.getFetchLimit());
        assertEquals(MY_FETCH_TIMEOUT, params.getFetchTimeout());
        assertEquals(MY_HOST, params.getHostname());
        assertEquals(MY_LAT, params.getLatitude());
        assertEquals(MY_LONG, params.getLongitude());
        assertTrue(params.isManaged());
        assertEquals(MY_PARTITION, params.getPartitionId());
        assertEquals(MY_PARTNER, params.getPartner());
        assertEquals(MY_PASS, params.getPassword());
        assertEquals(MY_PORT, params.getPort());
        assertEquals(servers, params.getServers());
        assertEquals(MY_TOPIC, params.getTopic());
        assertEquals(MY_EFFECTIVE_TOPIC, params.getEffectiveTopic());
        assertTrue(params.isUseHttps());
        assertEquals(MY_USERNAME, params.getUserName());
    }

    @Test
    public void testBooleanGetters() {
        // ensure that booleans are independent of each other
        testBoolean("true:false:false", TopicParamsBuilder::allowSelfSignedCerts);
        testBoolean("false:true:false", TopicParamsBuilder::managed);
        testBoolean("false:false:true", TopicParamsBuilder::useHttps);
    }

    @Test
    public void testValidators() {
        BusTopicParams params = makeBuilder().build();

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
    }

    @Test
    public void testInvertedValidators() {
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
        assertTrue(makeBuilder().servers(List.of("")).build().isServersInvalid());
        assertFalse(makeBuilder().servers(List.of("one-server")).build().isServersInvalid());
        assertTrue(makeBuilder().topic("").build().isTopicInvalid());
        assertFalse(makeBuilder().userName("").build().isUserNameValid());
    }

    /**
     * Tests the boolean methods by applying a function, once with {@code false} and once
     * with {@code true}. Verifies that all the boolean methods return the correct
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
                        params.isAllowSelfSignedCerts() + ":" + params.isManaged() + ":" + params.isUseHttps());


        // now try the "true" case
        function.accept(builder, true);

        params = builder.build();
        assertEquals(expectedTrue,
                        params.isAllowSelfSignedCerts() + ":" + params.isManaged() + ":" + params.isUseHttps());
    }
}
