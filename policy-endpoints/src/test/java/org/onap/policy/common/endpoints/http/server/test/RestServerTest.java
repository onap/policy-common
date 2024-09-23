/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactory;
import org.onap.policy.common.endpoints.http.server.JsonExceptionMapper;
import org.onap.policy.common.endpoints.http.server.RestServer;
import org.onap.policy.common.endpoints.http.server.RestServer.Factory;
import org.onap.policy.common.endpoints.http.server.YamlExceptionMapper;
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.parameters.rest.RestServerParameters;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.springframework.test.util.ReflectionTestUtils;

class RestServerTest {
    private static final String METRICS_URI = "/metrics";
    private static final String SERVER1 = "my-server-A";
    private static final String SERVER2 = "my-server-B";
    private static final String FACTORY_FIELD = "factory";
    private static final String HOST = "my-host";
    private static final String PARAM_NAME = "my-param";
    private static final String PASS = "my-pass";
    private static final Integer PORT = 9876;
    private static final String USER = "my-user";

    private static Factory saveFactory;
    private static RestServer realRest;
    private static int realPort;
    private static RestServerParameters params;

    private RestServer rest;
    private HttpServletServer server1;
    private HttpServletServer server2;
    private Factory factory;
    private HttpServletServerFactory serverFactory;
    private String errorMsg;

    /**
     * Starts the REST server.
     * @throws Exception if an error occurs
     */
    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        saveFactory = (Factory) ReflectionTestUtils.getField(RestServer.class, FACTORY_FIELD);

        realPort = NetworkUtil.allocPort();

        initRealParams();

        realRest = new RestServer(params, RealProvider.class) {
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
    }

    /**
     * Restores the factory and stops the REST server.
     */
    @AfterAll
    public static void tearDownAfterClass() {
        ReflectionTestUtils.setField(RestServer.class, FACTORY_FIELD, saveFactory);

        realRest.stop();
    }

    /**
     * Initializes mocks.
     */
    @BeforeEach
    public void setUp() {
        server1 = mock(HttpServletServer.class);
        server2 = mock(HttpServletServer.class);
        factory = mock(Factory.class);
        serverFactory = mock(HttpServletServerFactory.class);

        initParams();

        when(factory.getServerFactory()).thenReturn(serverFactory);
        when(serverFactory.build(any())).thenReturn(Arrays.asList(server1, server2));

        when(server1.getName()).thenReturn(SERVER1);
        when(server2.getName()).thenReturn(SERVER2);

        ReflectionTestUtils.setField(RestServer.class, FACTORY_FIELD, factory);
    }

    @Test
    void testRestServer() {
        rest = new RestServer(params, Filter2.class, Provider1.class, Provider2.class);

        rest.start();
        verify(server1).start();
        verify(server2).start();

        rest.stop();
        verify(server1).stop();
        verify(server2).stop();
    }

    @Test
    void testRestServerListList() {
        rest = new RestServer(params, List.of(Filter2.class), List.of(Provider1.class, Provider2.class));

        rest.start();
        verify(server1).start();
        verify(server2).start();

        rest.stop();
        verify(server1).stop();
        verify(server2).stop();
    }

    @Test
    void testRestServer_MissingProviders() {
        assertThatIllegalArgumentException().isThrownBy(() -> new RestServer(params, List.of(Filter2.class), null));
    }

    @Test
    void testGetServerProperties_testGetProviderNames() {
        rest = new RestServer(params, Provider1.class, Provider2.class);

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
        assertEquals(String.join(",", GsonMessageBodyHandler.class.getName(), YamlMessageBodyHandler.class.getName(),
                        JsonExceptionMapper.class.getName(), YamlExceptionMapper.class.getName()),
                        props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER));
        assertEquals("false", props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PROMETHEUS_SUFFIX));
    }

    @Test
    void testExplicitPrometheusAddedToProperty() {
        when(params.isPrometheus()).thenReturn(true);
        rest = new RestServer(params, Filter2.class, Provider1.class, Provider2.class);
        ArgumentCaptor<Properties> cap = ArgumentCaptor.forClass(Properties.class);
        verify(serverFactory).build(cap.capture());

        Properties props = cap.getValue();
        String svcpfx = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + PARAM_NAME;

        assertEquals("true", props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PROMETHEUS_SUFFIX));
        assertThat(props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERVLET_URIPATH_SUFFIX)).isBlank();
        assertThat(props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERVLET_CLASS_SUFFIX)).isBlank();
    }

    @Test
    void testStandardServletAddedToProperty() {
        when(params.getServletUriPath()).thenReturn("/metrics");
        when(params.getServletClass()).thenReturn(MetricsServlet.class.getName());
        rest = new RestServer(params, Filter2.class, Provider1.class, Provider2.class);
        ArgumentCaptor<Properties> cap = ArgumentCaptor.forClass(Properties.class);
        verify(serverFactory).build(cap.capture());

        Properties props = cap.getValue();
        String svcpfx = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + PARAM_NAME;

        assertEquals("false", props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PROMETHEUS_SUFFIX));
        assertEquals(METRICS_URI,
            props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERVLET_URIPATH_SUFFIX));
        assertEquals(MetricsServlet.class.getName(),
            props.getProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERVLET_CLASS_SUFFIX));
    }

    @Test
    void testInvalidJson() throws Exception {
        initRealParams();

        assertEquals(200, roundTrip(new StandardCoder().encode(new MyRequest())));
        assertEquals(400, roundTrip("{'bogus-json'"));
        assertThat(errorMsg).contains("Invalid request");
    }

    @Test
    void testInvalidYaml() throws Exception {
        initRealParams();

        assertEquals(200, roundTrip(new StandardCoder().encode(new MyRequest()),
                        YamlMessageBodyHandler.APPLICATION_YAML));
        assertEquals(400, roundTrip("<bogus yaml", YamlMessageBodyHandler.APPLICATION_YAML));
        assertThat(errorMsg).contains("Invalid request");
    }

    private int roundTrip(String request) throws IOException {
        return roundTrip(request, MediaType.APPLICATION_JSON);
    }

    private int roundTrip(String request, String mediaType) throws IOException {
        URL url = new URL("http://" + params.getHost() + ":" + params.getPort() + "/request");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        String auth = params.getUserName() + ":" + params.getPassword();
        conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()));
        conn.setRequestProperty("Content-type", mediaType);
        conn.setRequestProperty("Accept", mediaType);
        conn.connect();

        try (PrintWriter wtr = new PrintWriter(conn.getOutputStream())) {
            wtr.write(request);
        }

        int code = conn.getResponseCode();

        if (code == 200) {
            errorMsg = "";
        } else {
            errorMsg = IOUtils.toString(conn.getErrorStream(), StandardCharsets.UTF_8);
        }

        return code;
    }

    @Test
    void testToString() {
        rest = new RestServer(params, Filter2.class, Provider1.class, Provider2.class);
        assertNotNull(rest.toString());
    }

    @Test
    void testFactory() {
        assertNotNull(saveFactory);
        assertNotNull(saveFactory.getServerFactory());
    }

    private static void initRealParams() {
        initParams();

        when(params.getHost()).thenReturn("localhost");
        when(params.getPort()).thenReturn(realPort);
        when(params.isHttps()).thenReturn(false);
        when(params.isAaf()).thenReturn(false);
    }

    private static void initParams() {
        params = mock(RestServerParameters.class);

        when(params.getHost()).thenReturn(HOST);
        when(params.getName()).thenReturn(PARAM_NAME);
        when(params.getPassword()).thenReturn(PASS);
        when(params.getPort()).thenReturn(PORT);
        when(params.getUserName()).thenReturn(USER);
        when(params.isAaf()).thenReturn(true);
        when(params.isHttps()).thenReturn(true);
    }

    private static class Filter2 implements jakarta.servlet.Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
            // do nothing
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
    @Produces({MediaType.APPLICATION_JSON, YamlMessageBodyHandler.APPLICATION_YAML})
    @Consumes({MediaType.APPLICATION_JSON, YamlMessageBodyHandler.APPLICATION_YAML})
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
