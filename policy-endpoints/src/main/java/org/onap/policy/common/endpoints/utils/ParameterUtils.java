/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;
import org.onap.policy.common.endpoints.parameters.TopicParameters;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is common utility class with utility methods for parameters.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class ParameterUtils {

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(ParameterUtils.class);

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
            updateTopicProperties(topicProperties, "source", source);
        }
        for (TopicParameters sink : topicSinks) {
            updateTopicProperties(topicProperties, "sink", sink);
        }

        return topicProperties;
    }

    /**
     * Common method to update topic properties object using the parameters passed.
     *
     * @param topicProperties the topic properties object which is to be updated
     * @param keyName either it is source or sink
     * @param topicParameters the topic parameters object
     */
    public static void updateTopicProperties(Properties topicProperties, String keyName,
        TopicParameters topicParameters) {
        String topicCommInfra = topicParameters.getTopicCommInfrastructure();
        String topicName = topicParameters.getTopic();
        List<String> servers = topicParameters.getServers();

        String propKey = topicCommInfra + "." + keyName + PolicyEndPointProperties.PROPERTY_TOPIC_TOPICS_SUFFIX;
        if (topicProperties.containsKey(propKey)) {
            topicProperties.setProperty(propKey, topicProperties.getProperty(propKey) + "," + topicName);
        } else {
            topicProperties.setProperty(propKey, topicName);
        }
        String propWithTopicKey = propKey + "." + topicName;
        topicProperties.setProperty(propWithTopicKey + PolicyEndPointProperties.PROPERTY_TOPIC_SERVERS_SUFFIX,
                String.join(",", servers));

        Field[] fields = BusTopicParams.class.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isSynthetic()) {
                try {
                    Object parameter = new PropertyDescriptor(field.getName(), TopicParameters.class)
                        .getReadMethod().invoke(topicParameters);
                    if ((parameter instanceof String && StringUtils.isNotBlank(parameter.toString()))
                        || (parameter instanceof Number && ((Number) parameter).longValue() >= 0)) {
                        topicProperties.setProperty(propWithTopicKey + "." + field.getName(), parameter.toString());
                    }
                    if (parameter instanceof Boolean && (Boolean) parameter) {
                        topicProperties.setProperty(propWithTopicKey + "." + field.getName(),
                            Boolean.toString((Boolean) parameter));
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | IntrospectionException e) {
                    logger.error("Error while creating Properties object from TopicParameters", e);
                }
            }
        }

    }
}
