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
import static org.junit.Assert.assertTrue;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;

public class UebTopicSinkFactoryTest extends UebTopicFactoryTestBase<UebTopicSink> {

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
        super.testBuildBusTopicParams();
        super.testBuildBusTopicParams_Ex();
    }

    @Test
    public void testBuildListOfStringString() {
        super.testBuildListOfStringString();

        // check parameters that were used
        BusTopicParams params = getLastParams();
        assertEquals(false, params.isAllowSelfSignedCerts());
    }

    @Test
    public void testBuildProperties() {
        super.testBuildProperties();
        super.testBuildProperties_Variations();
        super.testBuildProperties_Multiple();
        
        initFactory();

        assertEquals(1, buildTopics(makePropBuilder().makeTopic(MY_TOPIC).build()).size());

        BusTopicParams params = getLastParams();
        assertEquals(MY_PARTITION, params.getPartitionId());
    }

    @Test
    public void testDestroyString_testGet_testInventory() {
        super.testDestroyString_testGet_testInventory();
        super.testDestroyString_Ex();
    }

    @Test
    public void testDestroy() {
        super.testDestroy();
    }

    @Test
    public void testGet() {
        super.testGet_Ex();
    }

    @Test
    public void testToString() {
        assertTrue(factory.toString().startsWith("IndexedUebTopicSinkFactory ["));
    }

    @Override
    protected void initFactory() {
        if (factory != null) {
            factory.destroy();
        }

        factory = new SinkFactory();
    }

    @Override
    protected List<UebTopicSink> buildTopics(Properties properties) {
        return factory.build(properties);
    }

    @Override
    protected UebTopicSink buildTopic(BusTopicParams params) {
        return factory.build(params);
    }

    @Override
    protected UebTopicSink buildTopic(List<String> servers, String topic) {
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
    protected List<UebTopicSink> getInventory() {
        return factory.inventory();
    }

    @Override
    protected UebTopicSink getTopic(String topic) {
        return factory.get(topic);
    }

    @Override
    protected BusTopicParams getLastParams() {
        return factory.params.getLast();
    }

    @Override
    protected TopicPropertyBuilder makePropBuilder() {
        return new UebTopicPropertyBuilder(PROPERTY_UEB_SINK_TOPICS);
    }

    /**
     * Factory that records the parameters of all of the sinks it creates.
     */
    private static class SinkFactory extends IndexedUebTopicSinkFactory {
        private Deque<BusTopicParams> params = new LinkedList<>();

        @Override
        protected UebTopicSink makeSink(BusTopicParams busTopicParams) {
            params.add(busTopicParams);
            return super.makeSink(busTopicParams);
        }
    }
}
