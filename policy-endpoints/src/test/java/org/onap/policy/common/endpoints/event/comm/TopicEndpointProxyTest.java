/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.bus.NoopTopicFactories;
import org.onap.policy.common.endpoints.event.comm.bus.NoopTopicPropertyBuilder;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicFactories;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicPropertyBuilder;
import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;
import org.onap.policy.common.endpoints.parameters.TopicParameters;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.utils.gson.GsonTestUtils;

public class TopicEndpointProxyTest {

    private static final String NOOP_SOURCE_TOPIC = "noop-source";
    private static final String NOOP_SINK_TOPIC = "noop-sink";

    private static final String UEB_SOURCE_TOPIC = "ueb-source";
    private static final String UEB_SINK_TOPIC = "ueb-sink";

    private Properties configuration = new Properties();
    private TopicParameterGroup group = new TopicParameterGroup();

    /**
     * Constructor.
     */
    public TopicEndpointProxyTest() {
        group.setTopicSinks(new LinkedList<>());
        group.setTopicSources(new LinkedList<>());

        NoopTopicPropertyBuilder noopSourceBuilder =
                new NoopTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_NOOP_SOURCE_TOPICS)
                        .makeTopic(NOOP_SOURCE_TOPIC);
        configuration.putAll(noopSourceBuilder.build());
        group.getTopicSources().add(noopSourceBuilder.getParams());

        NoopTopicPropertyBuilder noopSinkBuilder =
                new NoopTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_NOOP_SINK_TOPICS)
                        .makeTopic(NOOP_SINK_TOPIC);
        configuration.putAll(noopSinkBuilder.build());
        group.getTopicSinks().add(noopSinkBuilder.getParams());

        UebTopicPropertyBuilder uebSourceBuilder =
                new UebTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS)
                        .makeTopic(UEB_SOURCE_TOPIC);
        configuration.putAll(uebSourceBuilder.build());
        group.getTopicSources().add(uebSourceBuilder.getParams());

        UebTopicPropertyBuilder uebSinkBuilder =
                new UebTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS)
                        .makeTopic(UEB_SINK_TOPIC);
        configuration.putAll(uebSinkBuilder.build());
        group.getTopicSinks().add(uebSinkBuilder.getParams());

        TopicParameters invalidCommInfraParams =
                new NoopTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_NOOP_SOURCE_TOPICS)
                        .makeTopic(NOOP_SOURCE_TOPIC).getParams();
        invalidCommInfraParams.setTopicCommInfrastructure(Topic.CommInfrastructure.REST.name());
        group.getTopicSources().add(invalidCommInfraParams);
        group.getTopicSinks().add(invalidCommInfraParams);
    }

    private <T extends Topic> boolean exists(List<T> topics, String topicName) {
        return topics.stream().map(Topic::getTopic).anyMatch(topicName::equals);
    }

    private <T extends Topic> boolean allSources(List<T> topics) {
        return exists(topics, NOOP_SOURCE_TOPIC) && exists(topics, UEB_SOURCE_TOPIC);
    }

    private <T extends Topic> boolean allSinks(List<T> topics) {
        return exists(topics, NOOP_SINK_TOPIC) && exists(topics, UEB_SINK_TOPIC);
    }

    private <T extends Topic> boolean anySource(List<T> topics) {
        return exists(topics, NOOP_SOURCE_TOPIC) || exists(topics, UEB_SOURCE_TOPIC);
    }

    private <T extends Topic> boolean anySink(List<T> topics) {
        return exists(topics, NOOP_SINK_TOPIC) || exists(topics, UEB_SINK_TOPIC);
    }

    /**
     * Destroys all managed topics.
     */
    @After
    public void tearDown() {
        NoopTopicFactories.getSinkFactory().destroy();
        NoopTopicFactories.getSourceFactory().destroy();

        UebTopicFactories.getSinkFactory().destroy();
        UebTopicFactories.getSourceFactory().destroy();
    }

    @Test
    public void testSerialize() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        manager.addTopicSinks(configuration);

        assertThatCode(() -> new GsonTestUtils().compareGson(manager, TopicEndpointProxyTest.class))
                .doesNotThrowAnyException();
    }

    @Test
    public void testAddTopicSourcesListOfTopicParameters() {
        TopicEndpoint manager = new TopicEndpointProxy();

        List<TopicSource> sources = manager.addTopicSources(group.getTopicSources());
        assertSame(2, sources.size());

        assertTrue(allSources(sources));
        assertFalse(anySink(sources));
    }

    @Test
    public void testAddTopicSourcesProperties() {
        TopicEndpoint manager = new TopicEndpointProxy();

        List<TopicSource> sources = manager.addTopicSources(configuration);
        assertSame(2, sources.size());

        assertTrue(allSources(sources));
        assertFalse(anySink(sources));
    }

    @Test
    public void testAddTopicSinksListOfTopicParameters() {
        TopicEndpoint manager = new TopicEndpointProxy();

        List<TopicSink> sinks = manager.addTopicSinks(group.getTopicSinks());
        assertSame(2, sinks.size());

        assertFalse(anySource(sinks));
        assertTrue(allSinks(sinks));
    }

    @Test
    public void testAddTopicSinksProperties() {
        TopicEndpoint manager = new TopicEndpointProxy();

        List<TopicSink> sinks = manager.addTopicSinks(configuration);
        assertSame(2, sinks.size());

        assertFalse(anySource(sinks));
        assertTrue(allSinks(sinks));
    }

    @Test
    public void testAddTopicsProperties() {
        TopicEndpoint manager = new TopicEndpointProxy();

        List<Topic> topics = manager.addTopics(configuration);
        assertSame(4, topics.size());

        assertTrue(allSources(topics));
        assertTrue(allSinks(topics));
    }

    @Test
    public void testAddTopicsTopicParameterGroup() {
        TopicEndpoint manager = new TopicEndpointProxy();

        List<Topic> topics = manager.addTopics(group);
        assertSame(4, topics.size());

        assertTrue(allSources(topics));
        assertTrue(allSinks(topics));
    }

    @Test
    public void testAddTopicsTopicParameterGroupNull() {
        TopicEndpoint manager = new TopicEndpointProxy();

        List<Topic> topics = manager.addTopics(new TopicParameterGroup());
        assertEquals(0, topics.size());
    }

    @Test
    public void testLockSinks_lockSources_locked() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.lock();
        for (Topic topic : manager.addTopics(group)) {
            assertTrue(topic.isLocked());
        }
    }

    @Test
    public void testLockSinks_lockSources_unlocked() {
        TopicEndpoint manager = new TopicEndpointProxy();
        for (Topic topic : manager.addTopics(group)) {
            assertFalse(topic.isLocked());
        }
    }

    @Test
    public void testGetTopicSources() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        manager.addTopicSinks(configuration);

        List<TopicSource> sources = manager.getTopicSources();
        assertSame(2, sources.size());

        assertTrue(allSources(sources));
        assertFalse(anySink(sources));
    }

    @Test
    public void testGetTopicSinks() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        manager.addTopicSinks(configuration);

        List<TopicSink> sinks = manager.getTopicSinks();
        assertSame(2, sinks.size());

        assertFalse(anySource(sinks));
        assertTrue(allSinks(sinks));
    }

    @Test
    public void testGetUebTopicSources() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        assertSame(1, manager.getUebTopicSources().size());
    }

    @Test
    public void testGetNoopTopicSources() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        assertSame(1, manager.getNoopTopicSources().size());
    }

    @Test
    public void testGetUebTopicSinks() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSinks(configuration);
        assertSame(1, manager.getUebTopicSinks().size());
    }

    @Test
    public void testGetNoopTopicSinks() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSinks(configuration);
        assertSame(1, manager.getNoopTopicSinks().size());
    }

    @Test
    public void testLifecycle() {
        TopicEndpoint manager = new TopicEndpointProxy();

        assertTrue(manager.start());
        assertTrue(manager.isAlive());

        assertTrue(manager.stop());
        assertFalse(manager.isAlive());

        assertTrue(manager.start());
        assertTrue(manager.isAlive());

        manager.shutdown();
        assertFalse(manager.isAlive());
    }

    @Test
    public void testLock() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.lock();
        assertTrue(manager.isLocked());

        manager.unlock();
        assertFalse(manager.isLocked());
    }

    @Test
    public void testGetTopicSource() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSources(configuration);

        assertSame(NOOP_SOURCE_TOPIC, manager.getTopicSource(CommInfrastructure.NOOP, NOOP_SOURCE_TOPIC).getTopic());
        assertSame(UEB_SOURCE_TOPIC, manager.getTopicSource(CommInfrastructure.UEB, UEB_SOURCE_TOPIC).getTopic());

        assertThatIllegalStateException()
                .isThrownBy(() -> manager.getTopicSource(CommInfrastructure.NOOP, NOOP_SINK_TOPIC));
        assertThatIllegalStateException()
                .isThrownBy(() -> manager.getTopicSource(CommInfrastructure.UEB, UEB_SINK_TOPIC));
    }

    @Test
    public void testGetTopicSink() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSinks(configuration);

        assertSame(NOOP_SINK_TOPIC, manager.getTopicSink(CommInfrastructure.NOOP, NOOP_SINK_TOPIC).getTopic());
        assertSame(UEB_SINK_TOPIC, manager.getTopicSink(CommInfrastructure.UEB, UEB_SINK_TOPIC).getTopic());

        assertThatIllegalStateException()
                .isThrownBy(() -> manager.getTopicSink(CommInfrastructure.NOOP, NOOP_SOURCE_TOPIC));
        assertThatIllegalStateException()
                .isThrownBy(() -> manager.getTopicSink(CommInfrastructure.UEB, UEB_SOURCE_TOPIC));
    }

    @Test
    public void testGetUebTopicSource() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSources(configuration);

        assertSame(UEB_SOURCE_TOPIC, manager.getUebTopicSource(UEB_SOURCE_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getUebTopicSource(NOOP_SOURCE_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getUebTopicSource(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getUebTopicSource(""));
    }

    @Test
    public void testGetUebTopicSink() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSinks(configuration);

        assertSame(UEB_SINK_TOPIC, manager.getUebTopicSink(UEB_SINK_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getUebTopicSink(NOOP_SINK_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getUebTopicSink(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getUebTopicSink(""));
    }

    @Test
    public void testGetNoopTopicSource() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSources(configuration);

        assertSame(NOOP_SOURCE_TOPIC, manager.getNoopTopicSource(NOOP_SOURCE_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getNoopTopicSource(UEB_SOURCE_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getNoopTopicSource(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getNoopTopicSource(""));
    }

    @Test
    public void testGetNoopTopicSink() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSinks(configuration);

        assertSame(NOOP_SINK_TOPIC, manager.getNoopTopicSink(NOOP_SINK_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getNoopTopicSink(UEB_SINK_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getNoopTopicSink(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getNoopTopicSink(""));
    }
}
