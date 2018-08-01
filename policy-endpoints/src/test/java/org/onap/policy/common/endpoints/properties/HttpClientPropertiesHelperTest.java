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

import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HttpClientPropertiesHelperTest {

    private static HttpClientPropertiesHelper propHelper;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        propHelper = new HttpClientPropertiesHelper(properties);
    }

    @Test
    public void test() {
        propHelper.setEndpointNames("X");
        Assert.assertEquals(HttpClientProperties.PROPERTY_HTTP_CLIENT_SERVICES, propHelper.getEndpointPropertyName());

        propHelper.setAllowSelfSignedCertificates("X", true);
        Assert.assertTrue(propHelper.isAllowSelfSignedCertificates("X", false));
        Assert.assertTrue(propHelper.isAllowSelfSignedCertificates("Y", true));
        Assert.assertFalse(propHelper.isAllowSelfSignedCertificates("Y", false));
        Assert.assertEquals("true", propHelper.getAllowSelfSignedCertificates("X"));
    }
}