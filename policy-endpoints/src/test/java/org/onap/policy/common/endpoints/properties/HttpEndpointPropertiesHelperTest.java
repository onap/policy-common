/*
 * ============LICENSE_START=======================================================
 * ONAP
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
package org.onap.policy.common.endpoints.properties;

import java.util.Arrays;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HttpEndpointPropertiesHelperTest {

    class HttpAPropertiesHelper extends HttpEndpointPropertiesHelper {

        public HttpAPropertiesHelper(Properties properties) {
            super(properties);
        }

        @Override
        public String getEndpointPropertyName() {
            return "http.A.services";
        }
    }

    private static HttpAPropertiesHelper propHelper;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        propHelper = new HttpAPropertiesHelper(properties);
    }

    @Test
    public void test() {
        Assert.assertEquals("http.A.services", propHelper.getEndpointPropertyName());

        propHelper.setEndpointNames(Arrays.asList("M"));
        Assert.assertTrue(propHelper.getEndpointNames().size() == 1);
        Assert.assertEquals("M", propHelper.getEndpointNames().get(0));

        propHelper.setEndpointNames("X,Y");
        Assert.assertTrue(propHelper.getEndpointNames().size() == 2);
        Assert.assertEquals("X", propHelper.getEndpointNames().get(0));
        Assert.assertEquals("Y", propHelper.getEndpointNames().get(1));

        propHelper.setHost("X", "host");
        Assert.assertEquals("host", propHelper.getHost("X"));

        propHelper.setPort("X", 7777);
        Assert.assertTrue(7777 == propHelper.getPort("X"));

        propHelper.setUserName("X", "username");
        Assert.assertEquals("username", propHelper.getUserName("X"));

        propHelper.setPassword("X", "password");
        Assert.assertEquals("password", propHelper.getPassword("X"));

        propHelper.setContextUriPath("Y", "context");
        Assert.assertNotEquals("context", propHelper.getContextUriPath("X"));
        Assert.assertEquals("context", propHelper.getContextUriPath("Y"));

        propHelper.setHttps("X", true);
        Assert.assertTrue(propHelper.isHttps("X", false));
        Assert.assertTrue(propHelper.isHttps("Y", true));
        Assert.assertFalse(propHelper.isHttps("Y", false));
        Assert.assertEquals("true", propHelper.getHttps("X"));

        propHelper.setManaged("X", true);
        Assert.assertTrue(propHelper.isManaged("X", false));
        Assert.assertTrue(propHelper.isManaged("Y", true));
        Assert.assertFalse(propHelper.isManaged("Y", false));
        Assert.assertEquals("true", propHelper.getManaged("X"));
    }
}