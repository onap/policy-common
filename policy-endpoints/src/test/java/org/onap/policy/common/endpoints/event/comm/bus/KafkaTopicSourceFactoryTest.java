/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_KAFKA_SOURCE_TOPICS;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;

public class KafkaTopicSourceFactoryTest extends KafkaTopicFactoryTestBase<KafkaTopicSource> {

    private SourceFactory factory;

    /**
     * Creates the object to be tested.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();

        factory = new SourceFactory();
    }

    @After
    public void tearDown() {
        factory.destroy();
    }

    @Test
    @Override
    public void testBuildBusTopicParams() {
        super.testBuildBusTopicParams_Ex();
    }

    @Test
    @Override
    public void testBuildProperties() {

        initFactory();

        List<KafkaTopicSource> topics = buildTopics(makePropBuilder().makeTopic(MY_TOPIC).build());
        assertEquals(1, topics.size());
        assertEquals(MY_TOPIC, topics.get(0).getTopic());
    }

    @Test
    @Override
    public void testDestroyString_testGet_testInventory() {
        super.testDestroyString_Ex();
    }

    @Test
    public void testGet() {
        super.testGet_Ex();
    }

    @Test
    public void testToString() {
        assertTrue(factory.toString().startsWith("IndexedKafkaTopicSourceFactory ["));
    }

    @Override
    protected void initFactory() {
        if (factory != null) {
            factory.destroy();
        }

        factory = new SourceFactory();
    }

    @Override
    protected List<KafkaTopicSource> buildTopics(Properties properties) {
        return factory.build(properties);
    }

    @Override
    protected KafkaTopicSource buildTopic(BusTopicParams params) {
        return factory.build(params);
    }

    @Override
    protected KafkaTopicSource buildTopic(List<String> servers, String topic) {
        return factory.build(servers, topic);
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
    protected List<KafkaTopicSource> getInventory() {
        return factory.inventory();
    }

    @Override
    protected KafkaTopicSource getTopic(String topic) {
        return factory.get(topic);
    }

    @Override
    protected BusTopicParams getLastParams() {
        return factory.params.getLast();
    }

    @Override
    protected TopicPropertyBuilder makePropBuilder() {
        return new KafkaTopicPropertyBuilder(PROPERTY_KAFKA_SOURCE_TOPICS);
    }

    /**
     * Factory that records the parameters of all of the sources it creates.
     */
    private static class SourceFactory extends IndexedKafkaTopicSourceFactory {
        private Deque<BusTopicParams> params = new LinkedList<>();

        @Override
        protected KafkaTopicSource makeSource(BusTopicParams busTopicParams) {
            params.add(busTopicParams);
            return super.makeSource(busTopicParams);
        }
    }
}
