/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.common.message.bus.event;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.topic.TopicParameters;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * Class to hold/create all parameters for test cases.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class CommonTestData {

    public static final String TOPIC_NAME = "policy-pdp-pap";
    public static final String TOPIC_INFRA = "kafka";
    public static final String TOPIC_SERVER = "kafka:9092";

    public static final List<TopicParameters> TOPIC_PARAMS =
        List.of(getTopicParameters(TOPIC_NAME, TOPIC_INFRA, TOPIC_SERVER));

    protected static final Coder coder = new StandardCoder();

    /**
     * Create topic parameters for test cases.
     *
     * @param topicName   name of topic
     * @param topicInfra  topicCommInfrastructure
     * @param topicServer topic server
     * @return topic parameters
     */
    public static TopicParameters getTopicParameters(String topicName, String topicInfra, String topicServer) {
        final TopicParameters topicParams = new TopicParameters();
        topicParams.setTopic(topicName);
        topicParams.setTopicCommInfrastructure(topicInfra);
        topicParams.setServers(List.of(topicServer));
        return topicParams;
    }

    /**
     * Converts the contents of a map to a parameter class.
     *
     * @param source property map
     * @param clazz  class of object to be created from the map
     * @return a new object represented by the map
     */
    public <T extends ParameterGroup> T toObject(final Map<String, Object> source, final Class<T> clazz) {
        try {
            return coder.decode(coder.encode(source), clazz);

        } catch (final CoderException e) {
            throw new RuntimeException("cannot create " + clazz.getName() + " from map", e);
        }
    }

    /**
     * Returns a property map for a TopicParameters map for test cases.
     *
     * @param isEmpty boolean value to represent that object created should be empty or not
     * @return a property map suitable for constructing an object
     */
    public Map<String, Object> getTopicParameterGroupMap(final boolean isEmpty) {
        final Map<String, Object> map = new TreeMap<>();
        if (!isEmpty) {
            map.put("topicSources", TOPIC_PARAMS);
            map.put("topicSinks", TOPIC_PARAMS);
        }

        return map;
    }

    /**
     * Gets the standard parameter group as a String.
     *
     * @param filePath path of the file
     * @return the standard parameters
     * @throws IOException when file read operation fails
     */
    public String getParameterGroupAsString(String filePath) throws IOException {
        File file = new File(filePath);
        return Files.readString(file.toPath());
    }
}
