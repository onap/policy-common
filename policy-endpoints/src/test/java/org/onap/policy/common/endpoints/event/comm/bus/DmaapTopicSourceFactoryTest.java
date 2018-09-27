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
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX;

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

public class DmaapTopicSourceFactoryTest extends BusTopicTestBase {

    private static final String SERVER = "my-server";
    private static final String TOPIC2 = "my-topic-2";

    private static final String MY_CONN_TIMEOUT = "200";
    private static final String MY_READ_TIMEOUT = "201";
    private static final String MY_ROUNDTRIP_TIMEOUT = "202";
    private static final String MY_STICKINESS = "true";
    private static final String MY_SUBCONTEXT = "my-subcontext";
    private static final String MY_DME_VERSION = "my-version";

    private SourceFactory factory;

    /**
     * Creates the object to be tested.
     */
    @Before
    public void setUp() {
        super.setUp();

        factory = new SourceFactory();
    }

    @After
    public void tearDown() {
        factory.destroy();
    }

    @Test
    public void testBuildBusTopicParams() {
        // two unmanaged topics
        DmaapTopicSource source = factory.build(makeBuilder().managed(false).build());
        DmaapTopicSource source2 = factory.build(makeBuilder().managed(false).topic(TOPIC2).build());
        assertNotNull(source);
        assertNotNull(source2);
        assertTrue(source != source2);

        // duplicate topics, but since they aren't managed, they should be different
        DmaapTopicSource source3 = factory.build(makeBuilder().managed(false).build());
        DmaapTopicSource source4 = factory.build(makeBuilder().managed(false).build());
        assertNotNull(source3);
        assertNotNull(source4);
        assertTrue(source != source3);
        assertTrue(source != source4);
        assertTrue(source3 != source4);

        // two managed topics
        DmaapTopicSource source5 = factory.build(makeBuilder().build());
        DmaapTopicSource source6 = factory.build(makeBuilder().topic(TOPIC2).build());
        assertNotNull(source5);
        assertNotNull(source6);

        // re-build same managed topics - should get exact same objects
        assertTrue(source5 == factory.build(BusTopicParams.builder().topic(MY_TOPIC).build()));
        assertTrue(source6 == factory.build(makeBuilder().topic(TOPIC2).build()));
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
        assertEquals(MY_PARTNER, params.getPartner());
        assertEquals(Arrays.asList(SERVER), params.getServers());
        assertEquals(MY_TOPIC, params.getTopic());
        assertEquals(MY_FETCH_LIMIT, params.getFetchLimit());
        assertEquals(MY_FETCH_TIMEOUT, params.getFetchTimeout());

        Map<String, String> add = params.getAdditionalProps();
        assertEquals(MY_CONN_TIMEOUT, add.get(DmaapTopicSourceFactory.DME2_EP_CONN_TIMEOUT_PROPERTY));
        assertEquals(MY_READ_TIMEOUT, add.get(DmaapTopicSourceFactory.DME2_READ_TIMEOUT_PROPERTY));
        assertEquals(MY_ROUNDTRIP_TIMEOUT, add.get(DmaapTopicSourceFactory.DME2_ROUNDTRIP_TIMEOUT_PROPERTY));
        assertEquals(MY_ROUTE, add.get(DmaapTopicSourceFactory.DME2_ROUTE_OFFER_PROPERTY));
        assertEquals(MY_STICKINESS, add.get(DmaapTopicSourceFactory.DME2_SESSION_STICKINESS_REQUIRED_PROPERTY));
        assertEquals(MY_SUBCONTEXT, add.get(DmaapTopicSourceFactory.DME2_SUBCONTEXT_PATH_PROPERTY));
        assertEquals(MY_DME_VERSION, add.get(DmaapTopicSourceFactory.DME2_VERSION_PROPERTY));
    }

    @Test
    public void testBuildProperties_Variations() {
        TopicPropertyBuilder builder = makePropBuilder().makeTopic(MY_TOPIC);

        // null sources
        Properties props = builder.build();
        props.remove(PROPERTY_DMAAP_SOURCE_TOPICS);
        assertTrue(factory.build(props).isEmpty());

        // empty sources
        props = builder.build();
        props.setProperty(PROPERTY_DMAAP_SOURCE_TOPICS, "");
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
        checkDefault(builder, PROPERTY_HTTP_HTTPS_SUFFIX, params -> !params.isUseHttps());
        checkDefault(builder, PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX,
                        params -> !params.isAllowSelfSignedCerts());

        // check other properties having default values
        checkDefault(builder, PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX,
                        params -> params.getFetchTimeout() == DmaapTopicSource.DEFAULT_TIMEOUT_MS_FETCH, null, "",
                        "invalid-timeout");
        checkDefault(builder, PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX,
                        params -> params.getFetchLimit() == DmaapTopicSource.DEFAULT_LIMIT_FETCH, null, "",
                        "invalid-limit");

        // check "additional" properties
        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX,
                        DmaapTopicSourceFactory.DME2_EP_CONN_TIMEOUT_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX,
                        DmaapTopicSourceFactory.DME2_READ_TIMEOUT_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX,
                        DmaapTopicSourceFactory.DME2_ROUNDTRIP_TIMEOUT_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX,
                        DmaapTopicSourceFactory.DME2_ROUTE_OFFER_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX,
                        DmaapTopicSourceFactory.DME2_SESSION_STICKINESS_REQUIRED_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX,
                        DmaapTopicSourceFactory.DME2_SUBCONTEXT_PATH_PROPERTY);

        expectNullAddProp(builder, PROPERTY_DMAAP_DME2_VERSION_SUFFIX, DmaapTopicSourceFactory.DME2_VERSION_PROPERTY);
    }

    @Test
    public void testBuildProperties_Multiple() {
        TopicPropertyBuilder builder =
                        makePropBuilder().makeTopic(MY_TOPIC).makeTopic(TOPIC2).addTopic(MY_TOPIC).addTopic(MY_TOPIC);

        List<DmaapTopicSource> lst = factory.build(builder.build());
        assertEquals(4, lst.size());

        int index = 0;
        DmaapTopicSource source = lst.get(index++);
        assertTrue(source != lst.get(index++));
        assertTrue(source == lst.get(index++));
        assertTrue(source == lst.get(index++));
    }

    @Test
    public void testBuildListOfStringStringStringString() {
        DmaapTopicSource source1 = factory.build(servers, MY_TOPIC, MY_API_KEY, MY_API_SECRET);
        assertNotNull(source1);

        // check parameters that were used
        BusTopicParams params = factory.params.get(0);
        assertEquals(servers, params.getServers());
        assertEquals(MY_TOPIC, params.getTopic());
        assertEquals(true, params.isManaged());
        assertEquals(false, params.isUseHttps());
        assertEquals(false, params.isAllowSelfSignedCerts());
        assertEquals(MY_API_KEY, params.getApiKey());
        assertEquals(MY_API_SECRET, params.getApiSecret());
        assertEquals(DmaapTopicSource.DEFAULT_LIMIT_FETCH, params.getFetchLimit());
        assertEquals(DmaapTopicSource.DEFAULT_TIMEOUT_MS_FETCH, params.getFetchTimeout());
    }

    @Test
    public void testBuildListOfStringString() {
        DmaapTopicSource source1 = factory.build(servers, MY_TOPIC);
        assertNotNull(source1);

        // check parameters that were used
        BusTopicParams params = factory.params.get(0);
        assertEquals(servers, params.getServers());
        assertEquals(MY_TOPIC, params.getTopic());
        assertEquals(true, params.isManaged());
        assertEquals(false, params.isUseHttps());
        assertEquals(false, params.isAllowSelfSignedCerts());
        assertEquals(null, params.getApiKey());
        assertEquals(null, params.getApiSecret());
        assertEquals(DmaapTopicSource.DEFAULT_LIMIT_FETCH, params.getFetchLimit());
        assertEquals(DmaapTopicSource.DEFAULT_TIMEOUT_MS_FETCH, params.getFetchTimeout());

        DmaapTopicSource source2 = factory.build(servers, TOPIC2);
        assertNotNull(source2);
        assertTrue(source1 != source2);

        // duplicate - should be the same as these topics are managed
        DmaapTopicSource source3 = factory.build(Collections.emptyList(), TOPIC2);
        assertTrue(source2 == source3);
    }

    @Test
    public void testDestroyString_testGet_testInventory() {
        List<DmaapTopicSource> lst = factory.build(makePropBuilder().makeTopic(MY_TOPIC).makeTopic(TOPIC2).build());

        int index = 0;
        DmaapTopicSource source1 = lst.get(index++);
        DmaapTopicSource source2 = lst.get(index++);

        assertEquals(2, factory.inventory().size());
        assertTrue(factory.inventory().contains(source1));
        assertTrue(factory.inventory().contains(source2));

        source1.start();
        source2.start();

        assertEquals(source1, factory.get(MY_TOPIC));
        assertEquals(source2, factory.get(TOPIC2));

        factory.destroy(MY_TOPIC);
        assertFalse(source1.isAlive());
        assertTrue(source2.isAlive());
        assertEquals(source2, factory.get(TOPIC2));
        assertEquals(1, factory.inventory().size());
        assertTrue(factory.inventory().contains(source2));

        // repeat
        factory.destroy(MY_TOPIC);
        assertFalse(source1.isAlive());
        assertTrue(source2.isAlive());

        // with other topic
        factory.destroy(TOPIC2);
        assertFalse(source1.isAlive());
        assertFalse(source2.isAlive());
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
        List<DmaapTopicSource> lst = factory.build(makePropBuilder().makeTopic(MY_TOPIC).makeTopic(TOPIC2).build());

        int index = 0;
        DmaapTopicSource source1 = lst.get(index++);
        DmaapTopicSource source2 = lst.get(index++);

        source1.start();
        source2.start();

        factory.destroy();
        assertFalse(source1.isAlive());
        assertFalse(source2.isAlive());
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
        assertTrue(factory.toString().startsWith("IndexedDmaapTopicSourceFactory ["));
    }

    private DmaapTopicPropertyBuilder makePropBuilder() {
        return new DmaapTopicPropertyBuilder(PROPERTY_DMAAP_SOURCE_TOPICS);
    }

    /**
     * Verifies that a parameter has the correct default, if the original builder property
     * is not provided.
     *
     * @param builder used to build a set of properties
     * @param builderName name of the builder property
     * @param getter function to get the property from a set of parameters
     * @param values possible values to try, defaults to {null, ""}
     */
    private void checkDefault(TopicPropertyBuilder builder, String builderName,
                    Function<BusTopicParams, Boolean> getter, Object... values) {

        Object[] values2 = (values.length > 0 ? values : new String[] {null, ""});

        for (Object value : values2) {
            // always start with a fresh factory
            factory.destroy();
            factory = new SourceFactory();

            if (value == null) {
                builder.removeTopicProperty(builderName);

            } else {
                builder.setTopicProperty(builderName, value.toString());
            }

            assertEquals(1, factory.build(builder.build()).size());
            assertTrue(getter.apply(factory.params.get(0)));
        }
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
        factory = new SourceFactory();

        Properties props = builder.build();
        props.remove(PROPERTY_DMAAP_SOURCE_TOPICS + "." + MY_TOPIC + builderName);

        assertEquals(1, factory.build(props).size());
        assertFalse(factory.params.get(0).getAdditionalProps().containsKey(addName));

        // repeat, this time using an empty string instead of null
        factory.destroy();
        factory = new SourceFactory();

        props.setProperty(PROPERTY_DMAAP_SOURCE_TOPICS + "." + MY_TOPIC + builderName, "");

        assertEquals(1, factory.build(props).size());
        assertFalse(factory.params.get(0).getAdditionalProps().containsKey(addName));
    }

    /**
     * Factory that records the parameters of all of the sources it creates.
     */
    private static class SourceFactory extends IndexedDmaapTopicSourceFactory {
        private List<BusTopicParams> params = new LinkedList<>();

        @Override
        protected DmaapTopicSource makeSource(BusTopicParams busTopicParams) {
            params.add(busTopicParams);
            return super.makeSource(busTopicParams);
        }
    }
}
