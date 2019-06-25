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

package org.onap.policy.common.utils.parameters;

import java.util.List;
import java.util.Properties;

/**
 * This is common utility class with utility methods for parameters.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public abstract class ParameterUtils {
    private static final String PROPERTY_SERVERS = ".servers";
    private static final String PROPERTY_SINK_TOPICS = ".sink.topics";
    private static final String PROPERTY_SOURCE_TOPICS = ".source.topics";
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

        for (TopicParameters source : topicSources) {
            if (topicProperties.containsKey(source.getTopicCommInfrastructure() + PROPERTY_SOURCE_TOPICS)) {
                topicProperties.setProperty(source.getTopicCommInfrastructure() + PROPERTY_SOURCE_TOPICS,
                    topicProperties.getProperty(source.getTopicCommInfrastructure() + PROPERTY_SOURCE_TOPICS) + ","
                        + source.getTopic());
            } else {
                topicProperties.setProperty(source.getTopicCommInfrastructure() + PROPERTY_SOURCE_TOPICS,
                    source.getTopic());
            }
            topicProperties.setProperty(source.getTopicCommInfrastructure() + PROPERTY_SOURCE_TOPICS + "."
                    + source.getTopic() + PROPERTY_SERVERS, String.join(",", source.getServers()));
        }

        for (TopicParameters sink : topicSinks) {
            if (topicProperties.containsKey(sink.getTopicCommInfrastructure() + PROPERTY_SINK_TOPICS)) {
                topicProperties.setProperty(sink.getTopicCommInfrastructure() + PROPERTY_SINK_TOPICS,
                    topicProperties.getProperty(sink.getTopicCommInfrastructure() + PROPERTY_SINK_TOPICS) + ","
                        + sink.getTopic());
            } else {
                topicProperties.setProperty(sink.getTopicCommInfrastructure() + PROPERTY_SINK_TOPICS, sink.getTopic());
            }
            topicProperties.setProperty(
                sink.getTopicCommInfrastructure() + PROPERTY_SINK_TOPICS + "." + sink.getTopic() + PROPERTY_SERVERS,
                String.join(",", sink.getServers()));
        }

        return topicProperties;
    }
}
