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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_NOOP_SINK_TOPICS;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NoopTopicSinkFactoryTest extends TopicFactoryTestBase<NoopTopicSink> {

    private static final List<String> NOOP_SERVERS = Arrays.asList("noop");

    private IndexedNoopTopicSinkFactory factory;

    /**
     * Creates the object to be tested.
     */
    @Before
    public void setUp() {
        super.setUp();

        factory = new IndexedNoopTopicSinkFactory();
    }

    @After
    public void tearDown() {
        factory.destroy();
    }

    @Test
    public void testBuildListOfStringStringBoolean() {
        initFactory();

        NoopTopicSink item1 = buildTopic(servers, MY_TOPIC, true);
        assertNotNull(item1);

        assertEquals(servers, item1.getServers());
        assertEquals(MY_TOPIC, item1.getTopic());

        // managed topic - should not build a new one
        assertEquals(item1, buildTopic(servers, MY_TOPIC, true));

        NoopTopicSink item2 = buildTopic(servers, TOPIC2, true);
        assertNotNull(item2);
        assertTrue(item1 != item2);

        // duplicate - should be the same as these topics are managed
        NoopTopicSink item3 = buildTopic(Collections.emptyList(), TOPIC2, true);
        assertTrue(item2 == item3);

        // null server list
        initFactory();
        assertEquals(NOOP_SERVERS, buildTopic(null, MY_TOPIC, true).getServers());

        // empty server list
        initFactory();
        assertEquals(NOOP_SERVERS, buildTopic(Collections.emptyList(), MY_TOPIC, true).getServers());

        // unmanaged topic
        initFactory();
        item1 = buildTopic(servers, MY_TOPIC, false);
        assertTrue(item1 != buildTopic(servers, MY_TOPIC, false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildListOfStringStringBoolean_NullTopic() {
        buildTopic(servers, null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildListOfStringStringBoolean_EmptyTopic() {
        buildTopic(servers, "", true);
    }

    @Test
    public void testBuildProperties() {
        // managed topic
        initFactory();
        assertEquals(1, buildTopics(makePropBuilder().makeTopic(MY_TOPIC).build()).size());
        assertNotNull(factory.get(MY_TOPIC));

        // unmanaged topic - get() will throw an exception
        initFactory();
        assertEquals(1, buildTopics(makePropBuilder().makeTopic(MY_TOPIC)
                        .setTopicProperty(PROPERTY_MANAGED_SUFFIX, "false").build()).size());
        assertNotNull(expectException(() -> factory.get(MY_TOPIC)));

        // managed undefined - default to true
        initFactory();
        assertEquals(1, buildTopics(
                        makePropBuilder().makeTopic(MY_TOPIC).removeTopicProperty(PROPERTY_MANAGED_SUFFIX).build())
                                        .size());
        assertNotNull(factory.get(MY_TOPIC));

        // managed empty - default to true
        initFactory();
        assertEquals(1, buildTopics(
                        makePropBuilder().makeTopic(MY_TOPIC).setTopicProperty(PROPERTY_MANAGED_SUFFIX, "").build())
                                        .size());
        assertNotNull(factory.get(MY_TOPIC));

        initFactory();

        // null topic list
        assertTrue(buildTopics(makePropBuilder().build()).isEmpty());

        // empty topic list
        assertTrue(buildTopics(makePropBuilder().addTopic("").build()).isEmpty());

        // null server list
        initFactory();
        NoopTopicSink sink = buildTopics(makePropBuilder().makeTopic(MY_TOPIC)
                        .removeTopicProperty(PROPERTY_TOPIC_SERVERS_SUFFIX).build()).get(0);
        assertEquals(NOOP_SERVERS, sink.getServers());

        // empty server list
        initFactory();
        sink = buildTopics(makePropBuilder().makeTopic(MY_TOPIC).setTopicProperty(PROPERTY_TOPIC_SERVERS_SUFFIX, "")
                        .build()).get(0);
        assertEquals(NOOP_SERVERS, sink.getServers());

        // test other options
        super.testBuildProperties_Multiple();
    }

    @Test
    public void testDestroyString_testGet_testInventory() {
        super.testDestroyString_testGet_testInventory();
        super.testDestroyString_Ex();
    }

    @Test
    public void test() {
        super.testDestroy();
    }

    @Test
    public void testGet() {
        super.testGet_Ex();
    }

    @Override
    protected void initFactory() {
        if (factory != null) {
            factory.destroy();
        }

        factory = new IndexedNoopTopicSinkFactory();
    }

    @Override
    protected List<NoopTopicSink> buildTopics(Properties properties) {
        return factory.build(properties);
    }

    protected NoopTopicSink buildTopic(List<String> servers, String topic, boolean managed) {
        return factory.build(servers, topic, managed);
    }

    @Override
    protected void destroyFactory() {
        factory.destroy();
    }

    @Override
    protected void destroyTopic(String topic) {
        factory.destroy(topic);
    }

    @Override
    protected List<NoopTopicSink> getInventory() {
        return factory.inventory();
    }

    @Override
    protected NoopTopicSink getTopic(String topic) {
        return factory.get(topic);
    }

    @Override
    protected TopicPropertyBuilder makePropBuilder() {
        return new DmaapTopicPropertyBuilder(PROPERTY_NOOP_SINK_TOPICS);
    }
}
