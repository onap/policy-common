/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;
import java.util.Properties;

import org.onap.policy.common.capabilities.Lockable;
import org.onap.policy.common.capabilities.Startable;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSource;
import org.onap.policy.common.endpoints.event.comm.bus.NoopTopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicSource;

/**
 * Abstraction to managed the system's Networked Topic Endpoints, sources of all events input into
 * the System.
 */
public interface TopicEndpoint extends Startable, Lockable {

    /**
     * Add Topic Sources to the communication infrastructure initialized per properties
     *
     * @param properties properties for Topic Source construction
     * @return a generic Topic Source
     * @throws IllegalArgumentException when invalid arguments are provided
     */
    public List<TopicSource> addTopicSources(Properties properties);

    /**
     * Add Topic Sinks to the communication infrastructure initialized per properties
     *
     * @param properties properties for Topic Sink construction
     * @return a generic Topic Sink
     * @throws IllegalArgumentException when invalid arguments are provided
     */
    public List<TopicSink> addTopicSinks(Properties properties);

    /**
     * gets all Topic Sources
     *
     * @return the Topic Source List
     */
    List<TopicSource> getTopicSources();

    /**
     * get the Topic Sources for the given topic name
     *
     * @param topicName the topic name
     *
     * @return the Topic Source List
     * @throws IllegalStateException if the entity is in an invalid state
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public List<TopicSource> getTopicSources(List<String> topicNames);

    /**
     * gets the Topic Source for the given topic name and underlying communication infrastructure
     * type
     *
     * @param commType communication infrastructure type
     * @param topicName the topic name
     *
     * @return the Topic Source
     * @throws IllegalStateException if the entity is in an invalid state, for example multiple
     *         TopicReaders for a topic name and communication infrastructure
     * @throws IllegalArgumentException if invalid parameters are present
     * @throws UnsupportedOperationException if the operation is not supported.
     */
    public TopicSource getTopicSource(Topic.CommInfrastructure commType, String topicName);

    /**
     * get the UEB Topic Source for the given topic name
     *
     * @param topicName the topic name
     *
     * @return the UEB Topic Source
     * @throws IllegalStateException if the entity is in an invalid state, for example multiple
     *         TopicReaders for a topic name and communication infrastructure
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public UebTopicSource getUebTopicSource(String topicName);

    /**
     * get the DMAAP Topic Source for the given topic name
     *
     * @param topicName the topic name
     *
     * @return the DMAAP Topic Source
     * @throws IllegalStateException if the entity is in an invalid state, for example multiple
     *         TopicReaders for a topic name and communication infrastructure
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public DmaapTopicSource getDmaapTopicSource(String topicName);

    /**
     * get the Topic Sinks for the given topic name
     *
     * @param topicNames the topic names
     * @return the Topic Sink List
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public List<TopicSink> getTopicSinks(List<String> topicNames);

    /**
     * get the Topic Sinks for the given topic name and underlying communication infrastructure type
     *
     * @param topicName the topic name
     * @param commType communication infrastructure type
     *
     * @return the Topic Sink List
     * @throws IllegalStateException if the entity is in an invalid state, for example multiple
     *         TopicWriters for a topic name and communication infrastructure
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public TopicSink getTopicSink(Topic.CommInfrastructure commType, String topicName);

    /**
     * get the Topic Sinks for the given topic name and all the underlying communication
     * infrastructure type
     *
     * @param topicName the topic name
     * @param commType communication infrastructure type
     *
     * @return the Topic Sink List
     * @throws IllegalStateException if the entity is in an invalid state, for example multiple
     *         TopicWriters for a topic name and communication infrastructure
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public List<TopicSink> getTopicSinks(String topicName);

    /**
     * get the UEB Topic Source for the given topic name
     *
     * @param topicName the topic name
     *
     * @return the Topic Source
     * @throws IllegalStateException if the entity is in an invalid state, for example multiple
     *         TopicReaders for a topic name and communication infrastructure
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public UebTopicSink getUebTopicSink(String topicName);

    /**
     * get the no-op Topic Sink for the given topic name
     *
     * @param topicName the topic name
     *
     * @return the Topic Source
     * @throws IllegalStateException if the entity is in an invalid state, for example multiple
     *         TopicReaders for a topic name and communication infrastructure
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public NoopTopicSink getNoopTopicSink(String topicName);

    /**
     * get the DMAAP Topic Source for the given topic name
     *
     * @param topicName the topic name
     *
     * @return the Topic Source
     * @throws IllegalStateException if the entity is in an invalid state, for example multiple
     *         TopicReaders for a topic name and communication infrastructure
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public DmaapTopicSink getDmaapTopicSink(String topicName);

    /**
     * gets only the UEB Topic Sources
     *
     * @return the UEB Topic Source List
     */
    public List<UebTopicSource> getUebTopicSources();

    /**
     * gets only the DMAAP Topic Sources
     *
     * @return the DMAAP Topic Source List
     */
    public List<DmaapTopicSource> getDmaapTopicSources();

    /**
     * gets all Topic Sinks
     *
     * @return the Topic Sink List
     */
    public List<TopicSink> getTopicSinks();

    /**
     * gets only the UEB Topic Sinks
     *
     * @return the UEB Topic Sink List
     */
    public List<UebTopicSink> getUebTopicSinks();

    /**
     * gets only the DMAAP Topic Sinks
     *
     * @return the DMAAP Topic Sink List
     */
    public List<DmaapTopicSink> getDmaapTopicSinks();

    /**
     * gets only the NOOP Topic Sinks
     *
     * @return the NOOP Topic Sinks List
     */
    public List<NoopTopicSink> getNoopTopicSinks();
}
