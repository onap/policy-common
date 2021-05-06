/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;

/**
 * Noop Topic Factory.
 */
public abstract class NoopTopicFactory<T extends NoopTopicEndpoint> extends TopicBaseHashedFactory<T> {

    /**
     * Get Topics Property Name.
     *
     * @return property name.
     */
    protected abstract String getTopicsPropertyName();

    /**
     * {@inheritDoc}.
     */
    @Override
    protected List<String> getTopicNames(Properties properties) {
        String topics = properties.getProperty(getTopicsPropertyName());
        if (topics == null || topics.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.asList(topics.split("\\s*,\\s*"));
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected List<String> getServers(String topicName, Properties properties) {
        String servers =
            properties.getProperty(getTopicsPropertyName() + "." + topicName
                + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX);

        if (servers == null || servers.isEmpty()) {
            servers = CommInfrastructure.NOOP.toString();
        }

        return new ArrayList<>(Arrays.asList(servers.split("\\s*,\\s*")));
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected boolean isManaged(String topicName, Properties properties) {
        var managedString =
            properties.getProperty(getTopicsPropertyName()
                + "." + topicName + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX);

        var managed = true;
        if (managedString != null && !managedString.isEmpty()) {
            managed = Boolean.parseBoolean(managedString);
        }

        return managed;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public T build(List<String> serverList, String topic, boolean managed) {
        List<String> servers;
        if (serverList == null || serverList.isEmpty()) {
            servers = Collections.singletonList(CommInfrastructure.NOOP.toString());
        } else {
            servers = serverList;
        }

        return super.build(servers, topic, managed);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String toString() {
        return "NoopTopicFactory[ " + super.toString() + " ]";
    }
}

