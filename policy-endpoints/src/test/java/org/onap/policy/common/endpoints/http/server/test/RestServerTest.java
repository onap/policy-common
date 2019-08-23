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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.Getter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactory;
import org.onap.policy.common.endpoints.http.server.JsonExceptionMapper;
import org.onap.policy.common.endpoints.http.server.RestServer;
import org.onap.policy.common.endpoints.http.server.RestServer.Factory;
import org.onap.policy.common.endpoints.http.server.aaf.AafAuthFilter;
import org.onap.policy.common.endpoints.parameters.RestServerParameters;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
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

    private RestServer realRest;
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

        realRest = null;
    }

    /**
     * Stops the rest server.
     */
    @After
    public void tearDown() {
        if (realRest != null) {
            realRest.stop();
        }
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
        assertEquals(String.join(",", GsonMessageBodyHandler.class.getName(), JsonExceptionMapper.class.getName()),
                        props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER));
    }

    @Test
    public void testInvalidJson() throws Exception {
        when(params.getHost()).thenReturn("localhost");
        when(params.getPort()).thenReturn(NetworkUtil.allocPort());
        when(params.isHttps()).thenReturn(false);
        when(params.isAaf()).thenReturn(false);

        // use real factory
        Whitebox.setInternalState(RestServer.class, FACTORY_FIELD, saveFactory);

        realRest = new RestServer(params, null, RealProvider.class) {
            @Override
            protected Properties getServerProperties(RestServerParameters restServerParameters, String names) {
                Properties props = super.getServerProperties(restServerParameters, names);

                String svcpfx = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                                + restServerParameters.getName();
                props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, "false");

                return props;
            }
        };

        realRest.start();
        assertTrue(NetworkUtil.isTcpPortOpen(params.getHost(), params.getPort(), 100, 100));

        assertEquals(200, roundTrip(new StandardCoder().encode(new MyRequest())));
        assertEquals(400, roundTrip("{'bogus-json'"));
    }

    private int roundTrip(String request) throws IOException {
        URL url = new URL("http://" + params.getHost() + ":" + params.getPort() + "/request");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        String auth = params.getUserName() + ":" + params.getPassword();
        conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
        conn.setRequestProperty("Content-type", MediaType.APPLICATION_JSON);
        conn.connect();

        try (PrintWriter wtr = new PrintWriter(conn.getOutputStream())) {
            wtr.write(request);
        }

        return conn.getResponseCode();
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

    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public static class RealProvider {
        @POST
        @Path("/request")
        public Response decision(MyRequest body) {
            return Response.status(Response.Status.OK).entity(new MyResponse()).build();
        }
    }

    @Getter
    public static class MyRequest {
        private String data;
    }

    @Getter
    public static class MyResponse {
        private String text = "hello";
    }
}
