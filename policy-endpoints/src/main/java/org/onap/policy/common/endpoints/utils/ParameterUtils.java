/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.endpoints.utils;

import java.util.List;
import java.util.Properties;

import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;
import org.onap.policy.common.endpoints.parameters.TopicParameters;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;

/**
 * This is common utility class with utility methods for parameters.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class ParameterUtils {

    /**
     * Private constructor used to prevent sub class instantiation.
     */
    private ParameterUtils() {
        // Prevent construction of this class
    }

    /**
     * Create topic properties object from the parameters.
     *
     * @param topicParameters the topic parameters read from config file
     * @return the topic properties object
     */
    public static Properties getTopicProperties(TopicParameterGroup topicParameters) {
        Properties topicProperties = new Properties();
        List<TopicParameters> topicSources = topicParameters.getTopicSources();
        List<TopicParameters> topicSinks = topicParameters.getTopicSinks();

        // for each topicCommInfrastructure, there could be multiple topics (specified as comma separated string)
        // for each such topics, there could be multiple servers (specified as comma separated string)
        for (TopicParameters source : topicSources) {
            updateTopicProperties(topicProperties, "source", source.getTopicCommInfrastructure(), source.getTopic(),
                    source.getServers());
        }
        for (TopicParameters sink : topicSinks) {
            updateTopicProperties(topicProperties, "sink", sink.getTopicCommInfrastructure(), sink.getTopic(),
                    sink.getServers());
        }

        return topicProperties;
    }

    /**
     * Common method to update topic properties object using the parameters passed.
     *
     * @param topicProperties the topic properties object which is to be updated
     * @param keyName either it is source or sink
     * @param topicCommInfra the infra such as  dmaap, ueb or noop
     * @param topicName the topic
     * @param servers the list of server names for the topic
     */
    public static void updateTopicProperties(Properties topicProperties, String keyName, String topicCommInfra,
            String topicName, List<String> servers) {
        String propKey = topicCommInfra + "." + keyName + PolicyEndPointProperties.PROPERTY_TOPIC_TOPICS_SUFFIX;
        if (topicProperties.containsKey(propKey)) {
            topicProperties.setProperty(propKey, topicProperties.getProperty(propKey) + "," + topicName);
        } else {
            topicProperties.setProperty(propKey, topicName);
        }
        topicProperties.setProperty(propKey + "." + topicName + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX,
                String.join(",", servers));
    }
}
