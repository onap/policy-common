/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.http.server.test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactory;
import org.onap.policy.common.endpoints.http.server.RestServer;
import org.onap.policy.common.endpoints.http.server.RestServer.Factory;
import org.onap.policy.common.endpoints.http.server.aaf.AafAuthFilter;
import org.onap.policy.common.endpoints.parameters.RestServerParameters;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.powermock.reflect.Whitebox;

public class RestServerTest {
    private static final String SERVER1 = "my-server-A";
    private static final String SERVER2 = "my-server-B";
    private static final String FACTORY_FIELD = "factory";
    private static final String HOST = "my-host";
    private static final String PARAM_NAME = "my-param";
    private static final String PASS = "my-pass";
    private static final Integer PORT = 9876;
    private static final String USER = "my-user";
    private static Factory saveFactory;

    private RestServer rest;
    private HttpServletServer server1;
    private HttpServletServer server2;
    private Factory factory;
    private HttpServletServerFactory serverFactory;
    private RestServerParameters params;

    @BeforeClass
    public static void setUpBeforeClass() {
        saveFactory = Whitebox.getInternalState(RestServer.class, FACTORY_FIELD);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Whitebox.setInternalState(RestServer.class, FACTORY_FIELD, saveFactory);
    }

    /**
     * Initializes mocks.
     */
    @Before
    public void setUp() {
        server1 = mock(HttpServletServer.class);
        server2 = mock(HttpServletServer.class);
        factory = mock(Factory.class);
        serverFactory = mock(HttpServletServerFactory.class);
        params = mock(RestServerParameters.class);

        when(factory.getServerFactory()).thenReturn(serverFactory);
        when(serverFactory.build(any())).thenReturn(Arrays.asList(server1, server2));

        when(server1.getName()).thenReturn(SERVER1);
        when(server2.getName()).thenReturn(SERVER2);

        when(params.getHost()).thenReturn(HOST);
        when(params.getName()).thenReturn(PARAM_NAME);
        when(params.getPassword()).thenReturn(PASS);
        when(params.getPort()).thenReturn(PORT);
        when(params.getUserName()).thenReturn(USER);
        when(params.isAaf()).thenReturn(true);
        when(params.isHttps()).thenReturn(true);

        Whitebox.setInternalState(RestServer.class, FACTORY_FIELD, factory);
    }

    @Test
    public void testRestServer() {
        rest = new RestServer(params, Filter.class, Provider1.class, Provider2.class);

        rest.start();
        verify(server1).start();
        verify(server2).start();

        rest.stop();
        verify(server1).stop();
        verify(server2).stop();
    }

    @Test
    public void testRestServer_NoAaf() {
        rest = new RestServer(params, Filter.class, Provider1.class, Provider2.class);
        verify(server1, never()).addFilterClass(any(), any());
        verify(server2, never()).addFilterClass(any(), any());
    }

    @Test
    public void testRestServer_OnlyOneAaf() {
        when(server2.isAaf()).thenReturn(true);

        rest = new RestServer(params, Filter.class, Provider1.class, Provider2.class);

        verify(server1, never()).addFilterClass(any(), any());
        verify(server2).addFilterClass(null, Filter.class.getName());
    }

    @Test
    public void testRestServer_BothAaf() {
        when(server1.isAaf()).thenReturn(true);
        when(server2.isAaf()).thenReturn(true);

        rest = new RestServer(params, Filter.class, Provider1.class, Provider2.class);

        verify(server1).addFilterClass(null, Filter.class.getName());
        verify(server2).addFilterClass(null, Filter.class.getName());
    }

    @Test
    public void testRestServer_BothAaf_NoFilter() {
        when(server1.isAaf()).thenReturn(true);
        when(server2.isAaf()).thenReturn(true);

        rest = new RestServer(params, null, Provider1.class, Provider2.class);

        verify(server1, never()).addFilterClass(any(), any());
        verify(server2, never()).addFilterClass(any(), any());
    }

    @Test
    public void testRestServer_MissingProviders() {
        assertThatIllegalArgumentException().isThrownBy(() -> new RestServer(params, Filter.class));
    }

    @Test
    public void testGetServerProperties_testGetProviderNames() {
        rest = new RestServer(params, Filter.class, Provider1.class, Provider2.class);

        ArgumentCaptor<Properties> cap = ArgumentCaptor.forClass(Properties.class);
        verify(serverFactory).build(cap.capture());

        Properties props = cap.getValue();
        String svcpfx = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + PARAM_NAME;

        assertEquals(HOST, props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX));
        assertEquals(String.valueOf(PORT),
                        props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX));
        assertEquals(Provider1.class.getName() + "," + Provider2.class.getName(),
                        props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX));
        assertEquals("false", props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX));
        assertEquals("true", props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX));
        assertEquals(USER, props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX));
        assertEquals(PASS, props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX));
        assertEquals("true", props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX));
        assertEquals("true", props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_AAF_SUFFIX));
        assertEquals(GsonMessageBodyHandler.class.getName(),
                        props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER));
    }

    @Test
    public void testToString() {
        rest = new RestServer(params, Filter.class, Provider1.class, Provider2.class);
        assertNotNull(rest.toString());
    }

    @Test
    public void testFactory() {
        assertNotNull(saveFactory);
        assertNotNull(saveFactory.getServerFactory());
    }

    private static class Filter extends AafAuthFilter {
        @Override
        protected String getPermissionType(HttpServletRequest request) {
            return "";
        }

        @Override
        protected String getPermissionInstance(HttpServletRequest request) {
            return "";
        }
    }

    private static class Provider1 {
        private Provider1() {
            // do nothing
        }
    }

    private static class Provider2 {
        private Provider2() {
            // do nothing
        }
    }
}
