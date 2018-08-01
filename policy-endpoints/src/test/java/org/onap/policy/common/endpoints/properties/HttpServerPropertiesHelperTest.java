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

package org.onap.policy.drools.http.server;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.drools.http.client.HttpClientProperties;
import org.onap.policy.drools.http.server.HttpServerPropertiesHelper;

public class HttpServerPropertiesHelperTest {

    private static HttpServerPropertiesHelper propHelper;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        propHelper = new HttpServerPropertiesHelper(properties);
    }

    @Test
    public void test() {
        propHelper.setEndpointNames("X");
        Assert.assertTrue(HttpServerProperties.PROPERTY_HTTP_SERVER_SERVICES.equals((propHelper.getEndpointPropertyName())));

        propHelper.setAuthUriPath("X", "auth");
        Assert.assertTrue("auth".equals(propHelper.getAuthUriPath("X")));

        propHelper.setRestUriPath("X", "rest");
        Assert.assertTrue("rest".equals(propHelper.getRestUriPath("X")));

        propHelper.setRestPackages("X", "package1,package2");
        Assert.assertTrue(propHelper.getRestPackages("X").size() == 2);
        Assert.assertTrue("package1".equals(propHelper.getRestPackages("X").get(0)));
        Assert.assertTrue("package2".equals(propHelper.getRestPackages("X").get(1)));

        propHelper.setRestPackages("X", Arrays.asList("packageA","packageB"));
        Assert.assertTrue(propHelper.getRestPackages("X").size() == 2);
        Assert.assertTrue("packageA".equals(propHelper.getRestPackages("X").get(0)));
        Assert.assertTrue("packageB".equals(propHelper.getRestPackages("X").get(1)));

        propHelper.setRestClasses("X", "class1,class2");
        Assert.assertTrue(propHelper.getRestClasses("X").size() == 2);
        Assert.assertTrue("class1".equals(propHelper.getRestClasses("X").get(0)));
        Assert.assertTrue("class2".equals(propHelper.getRestClasses("X").get(1)));

        propHelper.setRestClasses("X", Arrays.asList("classA","classB"));
        Assert.assertTrue(propHelper.getRestClasses("X").size() == 2);
        Assert.assertTrue("classA".equals(propHelper.getRestClasses("X").get(0)));
        Assert.assertTrue("classB".equals(propHelper.getRestClasses("X").get(1)));

        propHelper.setSwagger("X", true);
        Assert.assertTrue(propHelper.isSwagger("X", false));
        Assert.assertTrue(propHelper.isSwagger("Y", true));
        Assert.assertFalse(propHelper.isSwagger("Y", false));
        Assert.assertTrue("true".equals(propHelper.getSwagger("X")));
    }
}