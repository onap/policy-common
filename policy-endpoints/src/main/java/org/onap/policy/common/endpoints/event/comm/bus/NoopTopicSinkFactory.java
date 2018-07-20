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

package org.onap.policy.common.endpoints.event.comm.bus;

import java.util.List;
import java.util.Properties;

/**
 * Noop Topic Sink Factory
 */
public interface NoopTopicSinkFactory {

    /**
     * Creates noop topic sinks based on properties files
     * 
     * @param properties Properties containing initialization values
     * 
     * @return a noop topic sink
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public List<NoopTopicSink> build(Properties properties);

    /**
     * builds a noop sink
     * 
     * @param servers list of servers
     * @param topic topic name
     * @param managed is this sink endpoint managed?
     * @return a noop topic sink
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public NoopTopicSink build(List<String> servers, String topic, boolean managed);

    /**
     * Destroys a sink based on the topic
     * 
     * @param topic topic name
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public void destroy(String topic);

    /**
     * gets a sink based on topic name
     * 
     * @param topic the topic name
     * 
     * @return a sink with topic name
     * @throws IllegalArgumentException if an invalid topic is provided
     * @throws IllegalStateException if the sink is in an incorrect state
     */
    public NoopTopicSink get(String topic);

    /**
     * Provides a snapshot of the UEB Topic Writers
     * 
     * @return a list of the UEB Topic Writers
     */
    public List<NoopTopicSink> inventory();

    /**
     * Destroys all sinks
     */
    public void destroy();
}
