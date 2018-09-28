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

package org.onap.policy.common.endpoints.event.comm.bus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_DME2_VERSION_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;

public class DmaapTopicSinkFactoryTest extends BusTopicTestBase {

    private static final String SERVER = "my-server";
    private static final String TOPIC2 = "my-topic-2";

    private static final String MY_CONN_TIMEOUT = "200";
    private static final String MY_READ_TIMEOUT = "201";
    private static final String MY_ROUNDTRIP_TIMEOUT = "202";
    private static final String MY_STICKINESS = "true";
    private static final String MY_SUBCONTEXT = "my-subcontext";
    private static final String MY_DME_VERSION = "my-version";

    private SinkFactory factory;

    /**
     * Creates the object to be tested.
     */
    @Before
    public void setUp() {
        super.setUp();

        factory = new SinkFactory();
    }

    @After
    public void tearDown() {
        factory.destroy();
    }

    @Test
    public void testBuildBusTopicParams() {
        // two unmanaged topics
        DmaapTopicSink sink = factory.build(makeBuilder().managed(false).build());
        DmaapTopicSink sink2 = factory.build(makeBuilder().managed(false).topic(TOPIC2).build());
        assertNotNull(sink);
        assertNotNull(sink2);
        assertTrue(sink != sink2);

        // duplicate topics, but since they aren't managed, they should be different
        DmaapTopicSink sink3 = factory.build(makeBuilder().managed(false).build());
        DmaapTopicSink sink4 = factory.build(makeBuilder().managed(false).build());
        assertNotNull(sink3);
        assertNotNull(sink4);
        assertTrue(sink != sink3);
        assertTrue(sink != sink4);
        assertTrue(sink3 != sink4);

        // two managed topics
        DmaapTopicSink sink5 = factory.build(makeBuilder().build());
        DmaapTopicSink sink6 = factory.build(makeBuilder().topic(TOPIC2).build());
        assertNotNull(sink5);
        assertNotNull(sink6);

        // re-build same managed topics - should get exact same objects
        assertTrue(sink5 == factory.build(BusTopicParams.builder().topic(MY_TOPIC).build()));
        assertTrue(sink6 == factory.build(makeBuilder().topic(TOPIC2).build()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildBusTopicParams_NullTopic() {
        factory.build(makeBuilder().topic(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildBusTopicParams_EmptyTopic() {
        factory.build(makeBuilder().topic("").build());
    }

    @Test
    public void testBuildListOfStringString() {
        DmaapTopicSink sink1 = factory.build(servers, MY_TOPIC);
        assertNotNull(sink1);

        // check parameters that were used
        BusTopicParams params = factory.params.get(0);
        assertEquals(servers, params.getServers());
        assertEquals(MY_TOPIC, params.getTopic());
        assertEquals(true, params.isManaged());
        assertEquals(false, params.isUseHttps());
        assertEquals(false, params.isAllowSelfSignedCerts());

        DmaapTopicSink sink2 = factory.build(servers, TOPIC2);
        assertNotNull(sink2);
        assertTrue(sink1 != sink2);

        // duplicate - should be the same as these topics are managed
        DmaapTopicSink sink3 = factory.build(Collections.emptyList(), TOPIC2);
        assertTrue(sink2 == sink3);
    }

    @Test
    public void testBuildProperties() {
        assertEquals(1, factory.build(makePropBuilder().makeTopic(MY_TOPIC).build()).size());

        BusTopicParams params = factory.params.get(0);
        assertEquals(true, params.isManaged());
        assertEquals(true, params.isUseHttps());
        assertEquals(true, params.isAllowSelfSignedCerts());
        assertEquals(MY_API_KEY, params.getApiKey());
        assertEquals(MY_API_SECRET, params.getApiSecret());
        assertEquals(MY_ENV, params.getEnvironment());
        assertEquals(MY_LAT, params.getLatitude());
        assertEquals(MY_LONG, params.getLongitude());
        assertEquals(MY_PARTITION, params.getPartitionId());
        assertEquals(MY_PARTNER, params.getPartner());
        assertEquals(Arrays.asList(SERVER), params.getServers());
        assertEquals(MY_TOPIC, params.getTopic());

        Map<String, String> add = params.getAdditionalProps();
        assertEquals(MY_CONN_TIMEOUT, add.get(DmaapTopicSinkFactory.DME2_EP_CONN_TIMEOUT_PROPERTY));
        assertEquals(MY_READ_TIMEOUT, add.get(DmaapTopicSinkFactory.DME2_READ_TIMEOUT_PROPERTY));
        assertEquals(MY_ROUNDTRIP_TIMEOUT, add.get(DmaapTopicSinkFactory.DME2_ROUNDTRIP_TIMEOUT_PROPERTY));
        assertEquals(MY_ROUTE, add.get(DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY));
        assertEquals(MY_STICKINESS, add.get(DmaapTopicSinkFactory.DME2_SESSION_STICKINESS_REQUIRED_PROPERTY));
        assertEquals(MY_SUBCONTEXT, add.get(DmaapTopicSinkFactory.DME2_SUBCONTEXT_PATH_PROPERTY));
        assertEquals(MY_DME_VERSION, add.get(DmaapTopicSinkFactory.DME2_VERSION_PROPERTY));
    }

    @Test
    public void testBuildProperties_Variations() {
        TopicPropertyBuilder builder = makePropBuilder().makeTopic(MY_TOPIC);

        // null sinks
        Properties props = builder.build();
        props.remove(PROPERTY_DMAAP_SINK_TOPICS);
        assertTrue(factory.build(props).isEmpty());

        // empty sinks
        props = builder.build();
        props.setProperty(PROPERTY_DMAAP_SINK_TOPICS, "");
        assertTrue(factory.build(props).isEmpty());

        // null servers
        assertTrue(factory.build(makePropBuilder().makeTopic(MY_TOPIC)
                        .removeTopicProperty(PROPERTY_TOPIC_SERVERS_SUFFIX).build()).isEmpty());

        // empty servers
        assertTrue(factory.build(makePropBuilder().makeTopic(MY_TOPIC)
                        .removeTopicProperty(PROPERTY_TOPIC_SERVERS_SUFFIX).build()).isEmpty());

        // check boolean properties that default to true
        checkDefault(builder, PROPERTY_MANAGED_SUFFIX, BusTopicParams::isManaged);

        // check boolean properties that default to false
        checkDefault(builder, PROPERTY_HTTP_HTTPS_SUFFIX, params -> ! params.isUseHttps());
        checkDefault(builder, PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX,
            params -> ! params.isAllowSelfSignedCerts());

        // check "additional" properties
        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX,
                        DmaapTopicSinkFactory.DME2_EP_CONN_TIMEOUT_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX,
                        DmaapTopicSinkFactory.DME2_READ_TIMEOUT_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX,
                        DmaapTopicSinkFactory.DME2_ROUNDTRIP_TIMEOUT_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX,
                        DmaapTopicSinkFactory.DME2_ROUTE_OFFER_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX,
                        DmaapTopicSinkFactory.DME2_SESSION_STICKINESS_REQUIRED_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX,
                        DmaapTopicSinkFactory.DME2_SUBCONTEXT_PATH_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_VERSION_SUFFIX, DmaapTopicSinkFactory.DME2_VERSION_PROPERTY);
    }

    @Test
    public void testBuildProperties_Multiple() {
        TopicPropertyBuilder builder =
                        makePropBuilder().makeTopic(MY_TOPIC).makeTopic(TOPIC2).addTopic(MY_TOPIC).addTopic(MY_TOPIC);

        List<DmaapTopicSink> lst = factory.build(builder.build());
        assertEquals(4, lst.size());

        int index = 0;
        DmaapTopicSink sink = lst.get(index++);
        assertTrue(sink != lst.get(index++));
        assertTrue(sink == lst.get(index++));
        assertTrue(sink == lst.get(index++));
    }

    @Test
    public void testDestroyString_testGet_testInventory() {
        List<DmaapTopicSink> lst = factory.build(makePropBuilder().makeTopic(MY_TOPIC).makeTopic(TOPIC2).build());

        int index = 0;
        DmaapTopicSink sink1 = lst.get(index++);
        DmaapTopicSink sink2 = lst.get(index++);

        assertEquals(2, factory.inventory().size());
        assertTrue(factory.inventory().contains(sink1));
        assertTrue(factory.inventory().contains(sink2));

        sink1.start();
        sink2.start();

        assertEquals(sink1, factory.get(MY_TOPIC));
        assertEquals(sink2, factory.get(TOPIC2));

        factory.destroy(MY_TOPIC);
        assertFalse(sink1.isAlive());
        assertTrue(sink2.isAlive());
        assertEquals(sink2, factory.get(TOPIC2));
        assertEquals(1, factory.inventory().size());
        assertTrue(factory.inventory().contains(sink2));

        // repeat
        factory.destroy(MY_TOPIC);
        assertFalse(sink1.isAlive());
        assertTrue(sink2.isAlive());

        // with other topic
        factory.destroy(TOPIC2);
        assertFalse(sink1.isAlive());
        assertFalse(sink2.isAlive());
        assertEquals(0, factory.inventory().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestroyString_NullTopic() {
        factory.destroy(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestroyString_EmptyTopic() {
        factory.destroy("");
    }

    @Test
    public void testDestroy() {
        List<DmaapTopicSink> lst = factory.build(makePropBuilder().makeTopic(MY_TOPIC).makeTopic(TOPIC2).build());

        int index = 0;
        DmaapTopicSink sink1 = lst.get(index++);
        DmaapTopicSink sink2 = lst.get(index++);

        sink1.start();
        sink2.start();

        factory.destroy();
        assertFalse(sink1.isAlive());
        assertFalse(sink2.isAlive());
        assertEquals(0, factory.inventory().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_NullTopic() {
        factory.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_EmptyTopic() {
        factory.get("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGet_UnknownTopic() {
        factory.build(makePropBuilder().makeTopic(MY_TOPIC).build());
        factory.get(TOPIC2);
    }

    @Test
    public void testToString() {
        assertTrue(factory.toString().startsWith("IndexedDmaapTopicSinkFactory ["));
    }

    private DmaapTopicPropertyBuilder makePropBuilder() {
        return new DmaapTopicPropertyBuilder(PROPERTY_DMAAP_SINK_TOPICS);
    }

    /**
     * Verifies that a parameter has the correct default, if the original builder property
     * is not provided.
     *
     * @param builder used to build a set of properties
     * @param builderName name of the builder property
     * @param getter function to get the property from a set of parameters
     */
    private void checkDefault(TopicPropertyBuilder builder, String builderName,
                    Function<BusTopicParams, Boolean> getter) {

        /*
         * Not sure why the "managed" property is treated differently, but it is.
         */
        String prefix = PROPERTY_DMAAP_SINK_TOPICS + "." + MY_TOPIC;

        // always start with a fresh factory
        factory.destroy();
        factory = new SinkFactory();

        Properties props = builder.build();
        props.remove(prefix + builderName);

        assertEquals(1, factory.build(props).size());
        assertTrue(getter.apply(factory.params.get(0)));

        // repeat, this time using an empty string instead of null
        factory.destroy();
        factory = new SinkFactory();

        props.setProperty(prefix + builderName, "");

        assertEquals(1, factory.build(props).size());
        assertTrue(getter.apply(factory.params.get(0)));
    }

    /**
     * Verifies that an "additional" property does not exist, if the original builder
     * property is not provided.
     *
     * @param builder used to build a set of properties
     * @param builderName name of the builder property
     * @param addName name of the "additional" property
     */
    private void expectNullAddProp(TopicPropertyBuilder builder, String builderName, String addName) {
        // always start with a fresh factory
        factory.destroy();
        factory = new SinkFactory();

        Properties props = builder.build();
        props.remove(PROPERTY_DMAAP_SINK_TOPICS + "." + MY_TOPIC + builderName);

        assertEquals(1, factory.build(props).size());
        assertFalse(factory.params.get(0).getAdditionalProps().containsKey(addName));

        // repeat, this time using an empty string instead of null
        factory.destroy();
        factory = new SinkFactory();

        props.setProperty(PROPERTY_DMAAP_SINK_TOPICS + "." + MY_TOPIC + builderName, "");

        assertEquals(1, factory.build(props).size());
        assertFalse(factory.params.get(0).getAdditionalProps().containsKey(addName));
    }

    /**
     * Factory that records the parameters of all of the sinks it creates.
     */
    private static class SinkFactory extends IndexedDmaapTopicSinkFactory {
        private List<BusTopicParams> params = new LinkedList<>();

        @Override
        protected DmaapTopicSink makeSink(BusTopicParams busTopicParams) {
            params.add(busTopicParams);
            return super.makeSink(busTopicParams);
        }
    }
}
