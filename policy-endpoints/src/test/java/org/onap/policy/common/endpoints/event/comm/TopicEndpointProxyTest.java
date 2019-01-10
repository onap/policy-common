/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicPropertyBuilder;
import org.onap.policy.common.endpoints.event.comm.bus.NoopTopicPropertyBuilder;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;

public class TopicEndpointProxyTest {

    private static final String NOOP_SOURCE_TOPIC = "noop-source";
    private static final String NOOP_SINK_TOPIC = "noop-sink";

    private static final String UEB_SOURCE_TOPIC = "ueb-source";
    private static final String UEB_SINK_TOPIC = "ueb-sink";

    private static final String DMAAP_SOURCE_TOPIC = "dmaap-source";
    private static final String DMAAP_SINK_TOPIC = "dmaap-sink";

    private Properties configuration = new Properties();

    /**
     * Constructor.
     */
    public TopicEndpointProxyTest() {
        Properties noopSourceProperties =
            new NoopTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_NOOP_SOURCE_TOPICS)
                .makeTopic(NOOP_SOURCE_TOPIC).build();

        Properties noopSinkProperties =
            new NoopTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_NOOP_SINK_TOPICS)
                .makeTopic(NOOP_SINK_TOPIC).build();

        Properties uebSourceProperties =
            new NoopTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_UEB_SOURCE_TOPICS)
                .makeTopic(UEB_SOURCE_TOPIC).build();

        Properties uebSinkProperties =
            new NoopTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_UEB_SINK_TOPICS)
                .makeTopic(UEB_SINK_TOPIC).build();

        Properties dmaapSourceProperties =
            new DmaapTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_DMAAP_SOURCE_TOPICS)
                .makeTopic(DMAAP_SOURCE_TOPIC).build();

        Properties dmaapSinkProperties =
            new DmaapTopicPropertyBuilder(PolicyEndPointProperties.PROPERTY_DMAAP_SINK_TOPICS)
                .makeTopic(DMAAP_SINK_TOPIC).build();

        configuration.putAll(noopSourceProperties);
        configuration.putAll(noopSinkProperties);
        configuration.putAll(uebSourceProperties);
        configuration.putAll(uebSinkProperties);
        configuration.putAll(dmaapSourceProperties);
        configuration.putAll(dmaapSinkProperties);
    }

    private <T extends Topic> boolean exists(List<T> topics, String topicName) {
        return topics.stream().map(Topic::getTopic).anyMatch(topicName::equals);
    }

    private <T extends Topic> boolean allSources(List<T> topics) {
        return exists(topics, NOOP_SOURCE_TOPIC)
            && exists(topics, UEB_SOURCE_TOPIC)
            && exists(topics, DMAAP_SOURCE_TOPIC);
    }

    private <T extends Topic> boolean allSinks(List<T> topics) {
        return exists(topics, NOOP_SINK_TOPIC)
            && exists(topics, UEB_SINK_TOPIC)
            && exists(topics, DMAAP_SINK_TOPIC);
    }

    private <T extends Topic> boolean anySource(List<T> topics) {
        return exists(topics, NOOP_SOURCE_TOPIC)
            || exists(topics, UEB_SOURCE_TOPIC)
            || exists(topics, DMAAP_SOURCE_TOPIC);
    }

    private <T extends Topic> boolean anySink(List<T> topics) {
        return exists(topics, NOOP_SINK_TOPIC)
            || exists(topics, UEB_SINK_TOPIC)
            || exists(topics, DMAAP_SINK_TOPIC);
    }

    @Test
    public void addTopicSources() {
        TopicEndpoint manager = new TopicEndpointProxy();

        List<TopicSource>  sources = manager.addTopicSources(configuration);
        assertSame(3, sources.size());

        assertTrue(allSources(sources));
        assertFalse(anySink(sources));
    }

    @Test
    public void addTopicSinks() {
        TopicEndpoint manager = new TopicEndpointProxy();

        List<TopicSink>  sinks = manager.addTopicSinks(configuration);
        assertSame(3, sinks.size());

        assertFalse(anySource(sinks));
        assertTrue(allSinks(sinks));
    }

    @Test
    public void getTopicSources() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        manager.addTopicSinks(configuration);

        List<TopicSource>  sources = manager.getTopicSources();
        assertSame(3, sources.size());

        assertTrue(allSources(sources));
        assertFalse(anySink(sources));
    }

    @Test
    public void getTopicSinks() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        manager.addTopicSinks(configuration);

        List<TopicSink>  sinks = manager.getTopicSinks();
        assertSame(3, sinks.size());

        assertFalse(anySource(sinks));
        assertTrue(allSinks(sinks));
    }

    @Test
    public void getUebTopicSources() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        assertSame(1, manager.getUebTopicSources().size());
    }

    @Test
    public void getDmaapTopicSources() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        assertSame(1, manager.getDmaapTopicSources().size());
    }

    @Test
    public void getNoopTopicSources() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSources(configuration);
        assertSame(1, manager.getNoopTopicSources().size());
    }

    @Test
    public void getUebTopicSinks() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSinks(configuration);
        assertSame(1, manager.getNoopTopicSinks().size());
    }

    @Test
    public void getDmaapTopicSinks() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSinks(configuration);
        assertSame(1, manager.getDmaapTopicSinks().size());
    }

    @Test
    public void getNoopTopicSinks() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.addTopicSinks(configuration);
        assertSame(1, manager.getNoopTopicSinks().size());
    }

    @Test
    public void lifecycle() {
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
    public void lock() {
        TopicEndpoint manager = new TopicEndpointProxy();

        manager.lock();
        assertTrue(manager.isLocked());

        manager.unlock();
        assertFalse(manager.isLocked());
    }

    @Test
    public void getTopicSource() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSources(configuration);

        assertSame(NOOP_SOURCE_TOPIC, manager.getTopicSource(CommInfrastructure.NOOP, NOOP_SOURCE_TOPIC).getTopic());
        assertSame(UEB_SOURCE_TOPIC, manager.getTopicSource(CommInfrastructure.UEB, UEB_SOURCE_TOPIC).getTopic());
        assertSame(DMAAP_SOURCE_TOPIC, manager.getTopicSource(CommInfrastructure.DMAAP, DMAAP_SOURCE_TOPIC).getTopic());

        assertThatIllegalStateException()
            .isThrownBy(() -> manager.getTopicSource(CommInfrastructure.NOOP, NOOP_SINK_TOPIC));
        assertThatIllegalStateException()
            .isThrownBy(() -> manager.getTopicSource(CommInfrastructure.UEB, UEB_SINK_TOPIC));
        assertThatIllegalStateException()
            .isThrownBy(() -> manager.getTopicSource(CommInfrastructure.DMAAP, DMAAP_SINK_TOPIC));
    }

    @Test
    public void getTopicSink() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSinks(configuration);

        assertSame(NOOP_SINK_TOPIC, manager.getTopicSink(CommInfrastructure.NOOP, NOOP_SINK_TOPIC).getTopic());
        assertSame(UEB_SINK_TOPIC, manager.getTopicSink(CommInfrastructure.UEB, UEB_SINK_TOPIC).getTopic());
        assertSame(DMAAP_SINK_TOPIC, manager.getTopicSink(CommInfrastructure.DMAAP, DMAAP_SINK_TOPIC).getTopic());

        assertThatIllegalStateException()
            .isThrownBy(() -> manager.getTopicSink(CommInfrastructure.NOOP, NOOP_SOURCE_TOPIC));
        assertThatIllegalStateException()
            .isThrownBy(() -> manager.getTopicSink(CommInfrastructure.UEB, UEB_SOURCE_TOPIC));
        assertThatIllegalStateException()
            .isThrownBy(() -> manager.getTopicSink(CommInfrastructure.DMAAP, DMAAP_SOURCE_TOPIC));
    }

    @Test
    public void getUebTopicSource() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSources(configuration);

        assertSame(UEB_SOURCE_TOPIC, manager.getUebTopicSource(UEB_SOURCE_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getUebTopicSource(NOOP_SOURCE_TOPIC));
        assertThatIllegalStateException().isThrownBy(() -> manager.getUebTopicSource(DMAAP_SOURCE_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getUebTopicSource(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getUebTopicSource(""));
    }

    @Test
    public void getUebTopicSink() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSinks(configuration);

        assertSame(UEB_SINK_TOPIC, manager.getUebTopicSink(UEB_SINK_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getUebTopicSink(NOOP_SINK_TOPIC));
        assertThatIllegalStateException().isThrownBy(() -> manager.getUebTopicSink(DMAAP_SINK_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getUebTopicSink(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getUebTopicSink(""));
    }

    @Test
    public void getDmaapTopicSource() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSources(configuration);

        assertSame(DMAAP_SOURCE_TOPIC, manager.getDmaapTopicSource(DMAAP_SOURCE_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getDmaapTopicSource(NOOP_SOURCE_TOPIC));
        assertThatIllegalStateException().isThrownBy(() -> manager.getDmaapTopicSource(UEB_SOURCE_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getDmaapTopicSource(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getDmaapTopicSource(""));
    }

    @Test
    public void getDmaapTopicSink() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSinks(configuration);

        assertSame(DMAAP_SINK_TOPIC, manager.getDmaapTopicSink(DMAAP_SINK_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getDmaapTopicSink(NOOP_SINK_TOPIC));
        assertThatIllegalStateException().isThrownBy(() -> manager.getDmaapTopicSink(UEB_SINK_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getDmaapTopicSink(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getDmaapTopicSink(""));
    }


    @Test
    public void getNoopTopicSource() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSources(configuration);

        assertSame(NOOP_SOURCE_TOPIC, manager.getNoopTopicSource(NOOP_SOURCE_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getNoopTopicSource(DMAAP_SOURCE_TOPIC));
        assertThatIllegalStateException().isThrownBy(() -> manager.getNoopTopicSource(UEB_SOURCE_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getNoopTopicSource(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getNoopTopicSource(""));
    }

    @Test
    public void getNoopTopicSink() {
        TopicEndpoint manager = new TopicEndpointProxy();
        manager.addTopicSinks(configuration);

        assertSame(NOOP_SINK_TOPIC, manager.getNoopTopicSink(NOOP_SINK_TOPIC).getTopic());

        assertThatIllegalStateException().isThrownBy(() -> manager.getNoopTopicSink(DMAAP_SINK_TOPIC));
        assertThatIllegalStateException().isThrownBy(() -> manager.getNoopTopicSink(UEB_SINK_TOPIC));

        assertThatIllegalArgumentException().isThrownBy(() -> manager.getNoopTopicSink(null));
        assertThatIllegalArgumentException().isThrownBy(() -> manager.getNoopTopicSink(""));
    }
}