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

/**
 * This is common utility class with utility methods for parameters.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public abstract class ParameterUtils {

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
            addTopicProperties(topicProperties, ".source.topics", source);
        }
        for (TopicParameters sink : topicSinks) {
            addTopicProperties(topicProperties, ".sink.topics", sink);
        }

        return topicProperties;
    }

    private static void addTopicProperties(Properties topicProperties, String keyName, TopicParameters topicParameter) {
        String propKey = topicParameter.getTopicCommInfrastructure() + keyName;
        if (topicProperties.containsKey(propKey)) {
            topicProperties.setProperty(propKey,
                    topicProperties.getProperty(propKey) + "," + topicParameter.getTopic());
        } else {
            topicProperties.setProperty(propKey, topicParameter.getTopic());
        }
        topicProperties.setProperty(propKey + "." + topicParameter.getTopic() + ".servers",
                String.join(",", topicParameter.getServers()));
    }
}
