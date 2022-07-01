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
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
        super.testBuildBusTopicParams();
        super.testBuildBusTopicParams_Ex();
    }

    @Test
    @Override
    public void testBuildProperties() {

        super.testBuildProperties();

        // check source-specific parameters that were used
        BusTopicParams params = factory.params.getFirst();
        assertEquals(MY_CONS_GROUP, params.getConsumerGroup());
        assertEquals(MY_CONS_INST, params.getConsumerInstance());
        assertEquals(MY_FETCH_LIMIT, params.getFetchLimit());
        assertEquals(MY_FETCH_TIMEOUT, params.getFetchTimeout());

        super.testBuildProperties_Variations();
        super.testBuildProperties_Multiple();

        // check default values for source-specific parameters
        checkDefault(PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX,
            params2 -> params2.getFetchLimit() == PolicyEndPointProperties.DEFAULT_LIMIT_FETCH,
            null, "", "invalid-limit-number");

        checkDefault(PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX,
            params2 -> params2.getFetchTimeout() == PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH,
            null, "", "invalid-timeout-number");
    }

    @Test
    public void testBuildListOfStringStringStringString() {
        KafkaTopicSource source1 = factory.build(servers, MY_TOPIC, MY_API_KEY, MY_API_SECRET);
        assertNotNull(source1);

        // check source-specific parameters that were used
        BusTopicParams params = factory.params.getFirst();
        assertEquals(MY_API_KEY, params.getApiKey());
        assertEquals(MY_API_SECRET, params.getApiSecret());
        assertEquals(PolicyEndPointProperties.DEFAULT_LIMIT_FETCH, params.getFetchLimit());
        assertEquals(PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH, params.getFetchTimeout());
    }

    @Test
    @Override
    public void testBuildListOfStringString() {
        super.testBuildListOfStringString();

        // check source-specific parameters that were used
        BusTopicParams params = factory.params.getFirst();
        assertEquals(null, params.getApiKey());
        assertEquals(null, params.getApiSecret());
        assertEquals(PolicyEndPointProperties.DEFAULT_LIMIT_FETCH, params.getFetchLimit());
        assertEquals(PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH, params.getFetchTimeout());

        assertEquals(true, params.isAllowSelfSignedCerts());
    }

    @Test
    @Override
    public void testDestroyString_testGet_testInventory() {
        super.testDestroyString_testGet_testInventory();
        super.testDestroyString_Ex();
    }

    @Test
    @Override
    public void testDestroy() {
        super.testDestroy();
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
