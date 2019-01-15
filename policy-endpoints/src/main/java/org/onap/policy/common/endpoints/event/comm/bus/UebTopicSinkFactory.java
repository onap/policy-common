/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modified Copyright (C) 2018 Samsung Electronics Co., Ltd.
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
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;

/**
 * UEB Topic Sink Factory.
 */
public interface UebTopicSinkFactory {

    /**
     * Instantiates a new UEB Topic Writer.
     * 
     * @param busTopicParams parameters object
     * @return an UEB Topic Sink
     */
    UebTopicSink build(BusTopicParams busTopicParams);

    /**
     * Creates an UEB Topic Writer based on properties files.
     * 
     * @param properties Properties containing initialization values
     * 
     * @return an UEB Topic Writer
     * @throws IllegalArgumentException if invalid parameters are present
     */
    List<UebTopicSink> build(Properties properties);

    /**
     * Instantiates a new UEB Topic Writer.
     * 
     * @param servers list of servers
     * @param topic topic name
     * 
     * @return an UEB Topic Writer
     * @throws IllegalArgumentException if invalid parameters are present
     */
    UebTopicSink build(List<String> servers, String topic);

    /**
     * Destroys an UEB Topic Writer based on a topic.
     * 
     * @param topic topic name
     * @throws IllegalArgumentException if invalid parameters are present
     */
    void destroy(String topic);

    /**
     * Destroys all UEB Topic Writers.
     */
    void destroy();

    /**
     * gets an UEB Topic Writer based on topic name.
     * 
     * @param topic the topic name
     * 
     * @return an UEB Topic Writer with topic name
     * @throws IllegalArgumentException if an invalid topic is provided
     * @throws IllegalStateException if the UEB Topic Reader is an incorrect state
     */
    UebTopicSink get(String topic);

    /**
     * Provides a snapshot of the UEB Topic Writers.
     * 
     * @return a list of the UEB Topic Writers
     */
    List<UebTopicSink> inventory();
}
