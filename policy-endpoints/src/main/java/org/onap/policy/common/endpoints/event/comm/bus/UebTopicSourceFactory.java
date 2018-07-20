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
 * UEB Topic Source Factory
 */
public interface UebTopicSourceFactory {

    /**
     * Creates an UEB Topic Source based on properties files
     * 
     * @param properties Properties containing initialization values
     * 
     * @return an UEB Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public List<UebTopicSource> build(Properties properties);

    /**
     * Instantiates a new UEB Topic Source
     * 
     * @param servers list of servers
     * @param topic topic name
     * @param apiKey API Key
     * @param apiSecret API Secret
     * @param consumerGroup Consumer Group
     * @param consumerInstance Consumer Instance
     * @param fetchTimeout Read Fetch Timeout
     * @param fetchLimit Fetch Limit
     * @param managed is this source endpoint managed?
     * 
     * @return an UEB Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public UebTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret,
            String consumerGroup, String consumerInstance, int fetchTimeout, int fetchLimit, boolean managed,
            boolean useHttps, boolean allowSelfSignedCerts);

    /**
     * Instantiates a new UEB Topic Source
     * 
     * @param servers list of servers
     * @param topic topic name
     * @param apiKey API Key
     * @param apiSecret API Secret
     * 
     * @return an UEB Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public UebTopicSource build(List<String> servers, String topic, String apiKey, String apiSecret);

    /**
     * Instantiates a new UEB Topic Source
     * 
     * @param servers list of servers
     * @param topic topic name
     * 
     * @return an UEB Topic Source
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public UebTopicSource build(List<String> servers, String topic);

    /**
     * Destroys an UEB Topic Source based on a topic
     * 
     * @param topic topic name
     * @throws IllegalArgumentException if invalid parameters are present
     */
    public void destroy(String topic);

    /**
     * Destroys all UEB Topic Sources
     */
    public void destroy();

    /**
     * gets an UEB Topic Source based on topic name
     * 
     * @param topic the topic name
     * @return an UEB Topic Source with topic name
     * @throws IllegalArgumentException if an invalid topic is provided
     * @throws IllegalStateException if the UEB Topic Source is an incorrect state
     */
    public UebTopicSource get(String topic);

    /**
     * Provides a snapshot of the UEB Topic Sources
     * 
     * @return a list of the UEB Topic Sources
     */
    public List<UebTopicSource> inventory();
}
