/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX;
import static org.onap.policy.common.endpoints.properties.PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;

/**
 * Base class for Topic Factory tests that use BusTopicParams.
 *
 * @param <T> type of topic managed by the factory
 */
public abstract class BusTopicFactoryTestBase<T extends Topic> extends TopicFactoryTestBase<T> {

    /**
     * Builds a topic.
     *
     * @param params the parameters used to configure the topic
     * @return a new topic
     */
    protected abstract T buildTopic(BusTopicParams params);

    /**
     * Builds a topic.
     *
     * @param servers list of servers
     * @param topic the topic name
     * @return a new topic
     */
    protected abstract T buildTopic(List<String> servers, String topic);

    /**
     * Gets the parameters used to build the most recent topic.
     *
     * @return the most recent topic's parameters
     */
    protected abstract BusTopicParams getLastParams();

    /**
     * Tests building a topic using BusTopicParams.
     */
    public void testBuildBusTopicParams() {
        initFactory();

        // two unmanaged topics
        T item = buildTopic(makeBuilder().managed(false).build());
        T item2 = buildTopic(makeBuilder().managed(false).topic(TOPIC2).build());
        assertNotNull(item);
        assertNotNull(item2);
        assertTrue(item != item2);

        // duplicate topics, but since they aren't managed, they should be different
        T item3 = buildTopic(makeBuilder().managed(false).build());
        T item4 = buildTopic(makeBuilder().managed(false).build());
        assertNotNull(item3);
        assertNotNull(item4);
        assertTrue(item != item3);
        assertTrue(item != item4);
        assertTrue(item3 != item4);

        // two managed topics
        T item5 = buildTopic(makeBuilder().build());
        T item6 = buildTopic(makeBuilder().topic(TOPIC2).build());
        assertNotNull(item5);
        assertNotNull(item6);

        // re-build same managed topics - should get exact same objects
        assertTrue(item5 == buildTopic(makeBuilder().topic(MY_TOPIC).build()));
        assertTrue(item6 == buildTopic(makeBuilder().topic(TOPIC2).build()));
    }

    /**
     * Tests exception cases when building a topic using BusTopicParams.
     */
    public void testBuildBusTopicParams_Ex() {
        // null topic
        RuntimeException actual = expectException(() -> buildTopic(makeBuilder().topic(null).build()));
        assertEquals(IllegalArgumentException.class, actual.getClass());

        // empty topic
        actual = expectException(() -> buildTopic(makeBuilder().topic("").build()));
        assertEquals(IllegalArgumentException.class, actual.getClass());
    }

    /**
     * Tests building a topic using a list of servers and a topic.
     */
    public void testBuildListOfStringString() {
        initFactory();

        T item1 = buildTopic(servers, MY_TOPIC);
        assertNotNull(item1);

        // check parameters that were used
        BusTopicParams params = getLastParams();
        assertEquals(servers, params.getServers());
        assertEquals(MY_TOPIC, params.getTopic());
        assertEquals(true, params.isManaged());
        assertEquals(false, params.isUseHttps());

        T item2 = buildTopic(servers, TOPIC2);
        assertNotNull(item2);
        assertTrue(item1 != item2);

        // duplicate - should be the same, as these topics are managed
        T item3 = buildTopic(servers, TOPIC2);
        assertTrue(item2 == item3);
    }

    /**
     * Tests building a topic using Properties. Verifies parameters specific to Bus
     * topics.
     */
    public void testBuildProperties() {
        initFactory();

        assertEquals(1, buildTopics(makePropBuilder().makeTopic(MY_TOPIC).build()).size());

        BusTopicParams params = getLastParams();
        assertEquals(true, params.isManaged());
        assertEquals(true, params.isUseHttps());
        assertEquals(true, params.isAllowSelfSignedCerts());
        assertEquals(MY_API_KEY, params.getApiKey());
        assertEquals(MY_API_SECRET, params.getApiSecret());
        assertEquals(Arrays.asList(SERVER), params.getServers());
        assertEquals(MY_TOPIC, params.getTopic());
    }

    @Override
    public void testBuildProperties_Variations() {
        super.testBuildProperties_Variations();

        // check boolean properties that default to true
        checkDefault(PROPERTY_MANAGED_SUFFIX, BusTopicParams::isManaged);

        // check boolean properties that default to false
        checkDefault(PROPERTY_HTTP_HTTPS_SUFFIX, params -> !params.isUseHttps());
        checkDefault(PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX, params -> !params.isAllowSelfSignedCerts());
    }

    /**
     * Verifies that a parameter has the correct default, if the original builder property
     * is not provided.
     *
     * @param builderName name of the builder property
     * @param validate function to test the validity of the property
     * @param values the values to which the property should be set, defaults to
     *        {@code null} and ""
     */
    protected void checkDefault(String builderName, Function<BusTopicParams, Boolean> validate, Object... values) {
        Object[] values2 = (values.length > 0 ? values : new Object[] {null, ""});

        for (Object value : values2) {
            // always start with a fresh factory
            initFactory();

            TopicPropertyBuilder builder = makePropBuilder().makeTopic(MY_TOPIC);

            if (value == null) {
                builder.removeTopicProperty(builderName);

            } else {
                builder.setTopicProperty(builderName, value.toString());
            }

            assertEquals("size for default " + value, 1, buildTopics(builder.build()).size());
            assertTrue("default for " + value, validate.apply(getLastParams()));
        }
    }

    /**
     * Verifies that an "additional" property does not exist, if the original builder
     * property is not provided.
     *
     * @param builderName name of the builder property
     * @param addName name of the "additional" property
     */
    protected void expectNullAddProp(String builderName, String addName) {

        // remove the property
        initFactory();
        Properties props = makePropBuilder().makeTopic(MY_TOPIC).removeTopicProperty(builderName).build();
        assertEquals(1, buildTopics(props).size());
        assertFalse(getLastParams().getAdditionalProps().containsKey(addName));


        // repeat, this time using an empty string instead of null
        initFactory();
        props = makePropBuilder().makeTopic(MY_TOPIC).setTopicProperty(builderName, "").build();
        assertEquals(1, buildTopics(props).size());
        assertFalse(getLastParams().getAdditionalProps().containsKey(addName));
    }
}
