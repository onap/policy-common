/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Samsung Electronics Co., Ltd.
 * Modifications Copyright 2023-2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientConfigException;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.http.server.internal.JettyJerseyServer;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.utils.network.NetworkUtil;

class HttpClientTest {
    private static final String TEST_HTTP_NO_AUTH_CLIENT = "testHttpNoAuthClient";
    private static final String TEST_HTTP_AUTH_CLIENT = "testHttpAuthClient";
    private static final String LOCALHOST = "localhost";
    private static final String JUNIT_ECHO = "junit/echo";
    private static final String HELLO = "hello";
    private static final String MY_VALUE = "myValue";
    private static final String FALSE_STRING = "false";
    private static final String ALPHA123 = "alpha123";
    private static final String PUT_HELLO = "PUT:hello:{myParameter=myValue}";
    private static final String DOT_PDP = "." + "PDP";
    private static final String DOT_PAP = "." + "PAP";

    private static final HashMap<String, String> savedValuesMap = new HashMap<>();

    /**
     * Setup before class method.
     *
     * @throws InterruptedException can be interrupted
     */
    @BeforeAll
    public static void setUpBeforeClass() throws InterruptedException {
        /* echo server - http + no auth */

        final HttpServletServer echoServerNoAuth =
                HttpServletServerFactoryInstance.getServerFactory().build("echo", LOCALHOST, 6666, "/", false, true);
        echoServerNoAuth.addServletPackage("/*", HttpClientTest.class.getPackage().getName());
        echoServerNoAuth.waitedStart(5000);

        if (!NetworkUtil.isTcpPortOpen(LOCALHOST, echoServerNoAuth.getPort(), 5, 10000L)) {
            throw new IllegalStateException("cannot connect to port " + echoServerNoAuth.getPort());
        }

        String keyStoreSystemProperty = System.getProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME);
        if (keyStoreSystemProperty != null) {
            savedValuesMap.put(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME, keyStoreSystemProperty);
        }

        String keyStorePasswordSystemProperty =
                System.getProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME);
        if (keyStorePasswordSystemProperty != null) {
            savedValuesMap.put(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME,
                    keyStorePasswordSystemProperty);
        }

        String trustStoreSystemProperty = System.getProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME);
        if (trustStoreSystemProperty != null) {
            savedValuesMap.put(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME, trustStoreSystemProperty);
        }

        String trustStorePasswordSystemProperty =
                System.getProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME);
        if (trustStorePasswordSystemProperty != null) {
            savedValuesMap.put(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME,
                    trustStorePasswordSystemProperty);
        }

        System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME, "src/test/resources/keystore-test");
        System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME, "kstest");

        System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME, "src/test/resources/keystore-test");
        System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME, "kstest");

        /* echo server - https + basic auth */

        final HttpServletServer echoServerAuth = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", true, LOCALHOST, 6667, false, "/", false, true);
        echoServerAuth.setBasicAuthentication("x", "y", null);
        echoServerAuth.addServletPackage("/*", HttpClientTest.class.getPackage().getName());
        echoServerAuth.addFilterClass("/*", TestFilter.class.getName());
        echoServerAuth.addFilterClass("/*", TestAuthorizationFilter.class.getName());
        echoServerAuth.waitedStart(5000);

        if (!NetworkUtil.isTcpPortOpen(LOCALHOST, echoServerAuth.getPort(), 5, 10000L)) {
            throw new IllegalStateException("cannot connect to port " + echoServerAuth.getPort());
        }
    }

    /**
     * Clear https clients and reset providers.
     */
    @BeforeEach
    public void setUp() {
        HttpClientFactoryInstance.getClientFactory().destroy();

        MyGsonProvider.resetSome();
    }

    /**
     * After the class is created method.
     */
    @AfterAll
    public static void tearDownAfterClass() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
        HttpClientFactoryInstance.getClientFactory().destroy();

        if (savedValuesMap.containsKey(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME)) {
            System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME,
                    savedValuesMap.get(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME));
            savedValuesMap.remove(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME);
        } else {
            System.clearProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME);
        }

        if (savedValuesMap.containsKey(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME)) {
            System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME,
                    savedValuesMap.get(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME));
            savedValuesMap.remove(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME);
        } else {
            System.clearProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME);
        }

        if (savedValuesMap.containsKey(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME)) {
            System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME,
                    savedValuesMap.get(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME));
            savedValuesMap.remove(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME);
        } else {
            System.clearProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME);
        }

        if (savedValuesMap.containsKey(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME)) {
            System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME,
                    savedValuesMap.get(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME));
            savedValuesMap.remove(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME);
        } else {
            System.clearProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME);
        }


    }

    @Test
    void testHttpGetNoAuthClient() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false,
            6666);
        final Response response = client.get(HELLO);
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(HELLO, body);
    }

    @Test
    void testHttpGetNoAuthClientAsync() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false,
            6666);
        MyCallback callback = new MyCallback();
        final Response response = client.get(callback, HELLO, new TreeMap<>()).get();

        verifyCallback("testHttpGetNoAuthClientAsync", callback, response);

        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(HELLO, body);
    }

    private void verifyCallback(String testName, MyCallback callback, final Response response)
                    throws InterruptedException {
        assertTrue(callback.await(), testName);
        assertNull(callback.getThrowable(), testName);
        assertSame(response, callback.getResponse(), testName);
    }

    @Test
    void testHttpPutNoAuthClient() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false, 6666);

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        final Response response = client.put(HELLO, entity, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);
    }

    @Test
    void testHttpPutNoAuthClientAsync() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false, 6666);

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        MyCallback callback = new MyCallback();
        final Response response = client.put(callback, HELLO, entity, Collections.emptyMap()).get();

        verifyCallback("testHttpPutNoAuthClientAsync", callback, response);

        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);
    }

    @Test
    void testHttpPostNoAuthClient() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false,
            6666);

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        final Response response = client.post(HELLO, entity, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals("POST:hello:{myParameter=myValue}", body);
    }

    @Test
    void testHttpPostNoAuthClientAsync() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false,
            6666);

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        MyCallback callback = new MyCallback();
        final Response response = client.post(callback, HELLO, entity, Collections.emptyMap()).get();

        verifyCallback("testHttpPostNoAuthClientAsync", callback, response);

        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals("POST:hello:{myParameter=myValue}", body);
    }

    @Test
    void testHttpDeletetNoAuthClient() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false,
            6666);

        final Response response = client.delete(HELLO, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals("DELETE:hello", body);
    }

    @Test
    void testHttpDeletetNoAuthClientAsync() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false,
            6666);

        MyCallback callback = new MyCallback();
        final Response response = client.delete(callback, HELLO, Collections.emptyMap()).get();

        verifyCallback("testHttpDeletetNoAuthClientAsync", callback, response);

        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals("DELETE:hello", body);
    }

    /**
     * Perform one asynchronous test with auth client; don't need to test every method.
     * @throws Exception if an error occurs
     */
    @Test
    void testHttpAsyncAuthClient() throws Exception {
        final HttpClient client = getAuthHttpClient();

        MyCallback callback = new MyCallback();
        final Response response = client.get(callback, HELLO, null).get();

        verifyCallback("testHttpAsyncAuthClient", callback, response);

        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(HELLO, body);
    }

    @Test
    void testHttpGetAuthClient() throws Exception {
        final HttpClient client = getAuthHttpClient();

        final Response response = client.get(HELLO);
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(HELLO, body);
    }

    @Test
    void testHttpPutAuthClient() throws Exception {
        final HttpClient client = getAuthHttpClient();

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        final Response response = client.put(HELLO, entity, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);
    }

    @Test
    void testHttpPutAuthClient_GsonProvider() throws Exception {
        final HttpClient client = HttpClientFactoryInstance.getClientFactory()
                        .build(BusTopicParams.builder().clientName(TEST_HTTP_AUTH_CLIENT).useHttps(true)
                                        .allowSelfSignedCerts(true).hostname(LOCALHOST).port(6667).basePath(JUNIT_ECHO)
                                        .userName("x").password("y").managed(true)
                                        .serializationProvider(MyGsonProvider.class.getName()).build());

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        final Response response = client.put(HELLO, entity, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);

        assertTrue(MyGsonProvider.hasWrittenSome());
    }

    @Test
    void testHttpAuthClient401() throws Exception {
        final HttpClient client = getNoAuthHttpClient("testHttpAuthClient401", true,
            6667);
        final Response response = client.get(HELLO);
        assertEquals(401, response.getStatus());
    }

    @Test
    void testHttpAuthClientProps() throws Exception {
        final Properties httpProperties = new Properties();

        /* PAP and PDP services */

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, "PAP,PDP");

        /* PAP server service configuration */

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, LOCALHOST);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, "7777");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX, "testpap");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX, ALPHA123);
        httpProperties.setProperty(
                        PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PAP
                                        + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX,
                        RestMockHealthCheck.class.getName());
        httpProperties.setProperty(
                        PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PAP
                                        + PolicyEndPointProperties.PROPERTY_HTTP_FILTER_CLASSES_SUFFIX,
                        TestFilter.class.getName());
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_SERVLET_CLASS_SUFFIX, MetricsServlet.class.getName());
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PAP
                                           + PolicyEndPointProperties.PROPERTY_HTTP_SERVLET_URIPATH_SUFFIX,
                                   "/pap/test/random/metrics");

        /* PDP server service configuration */

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, LOCALHOST);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, "7778");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX, "testpdp");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX, ALPHA123);
        httpProperties.setProperty(
                        PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PDP
                                        + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX,
                        RestMockHealthCheck.class.getName());
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PDP
                                           + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, "true");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + DOT_PDP
            + PolicyEndPointProperties.PROPERTY_HTTP_PROMETHEUS_SUFFIX, "true");

        /* PDP and PAP client services */

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES, "PAP,PDP");

        /* PAP client service configuration */

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, LOCALHOST);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, "7777");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_URL_SUFFIX, "pap/test");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, FALSE_STRING);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX, "testpap");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX, ALPHA123);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");

        /* PDP client service configuration */

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, LOCALHOST);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, "7778");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, FALSE_STRING);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX, "testpdp");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX, ALPHA123);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");

        final List<HttpServletServer> servers =
                        HttpServletServerFactoryInstance.getServerFactory().build(httpProperties);
        assertEquals(2, servers.size());

        final List<HttpClient> clients = HttpClientFactoryInstance.getClientFactory().build(httpProperties);
        assertEquals(2, clients.size());

        for (final HttpServletServer server : servers) {
            server.waitedStart(10000);
        }

        Response response;
        final HttpClient clientPap = HttpClientFactoryInstance.getClientFactory().get("PAP");
        response = clientPap.get();
        assertEquals(200, response.getStatus());

        final HttpClient clientPdp = HttpClientFactoryInstance.getClientFactory().get("PDP");

        response = clientPdp.get("pdp/test");
        assertEquals(500, response.getStatus());

        response = clientPdp.get("metrics");
        assertEquals(200, response.getStatus());

        response = clientPdp.get("openapi.json");
        assertEquals(200, response.getStatus());

        assertFalse(MyGsonProvider.hasWrittenSome());

        // try with empty path
        response = clientPap.get("");
        assertEquals(200, response.getStatus());

        response = clientPap.get("random/metrics");
        assertEquals(200, response.getStatus());

        response = clientPap.get("metrics");
        assertEquals(404, response.getStatus());

        // try it asynchronously, too
        MyCallback callback = new MyCallback();
        response = clientPap.get(callback, null).get();
        verifyCallback("testHttpAuthClientProps", callback, response);
        assertEquals(200, response.getStatus());

        // try it asynchronously, with empty path
        callback = new MyCallback();
        response = clientPap.get(callback, "", null).get();
        verifyCallback("testHttpAuthClientProps - empty path", callback, response);
        assertEquals(200, response.getStatus());
    }

    private HttpClient getAuthHttpClient() throws HttpClientConfigException {
        return HttpClientFactoryInstance.getClientFactory()
                        .build(BusTopicParams.builder().clientName(TEST_HTTP_AUTH_CLIENT).useHttps(true)
                                        .allowSelfSignedCerts(true).hostname(LOCALHOST).port(6667).basePath(JUNIT_ECHO)
                                        .userName("x").password("y").managed(true).build());
    }

    private HttpClient getNoAuthHttpClient(String clientName, boolean https, int port)
                    throws HttpClientConfigException {
        return HttpClientFactoryInstance.getClientFactory()
                        .build(BusTopicParams.builder().clientName(clientName).useHttps(https)
                                        .allowSelfSignedCerts(https).hostname(LOCALHOST).port(port).basePath(JUNIT_ECHO)
                                        .userName(null).password(null).managed(true).build());
    }


    static class MyEntity {

        private String myParameter;

        public MyEntity(final String myParameter) {
            this.myParameter = myParameter;
        }

        public void setMyParameter(final String myParameter) {
            this.myParameter = myParameter;
        }

        public String getMyParameter() {
            return myParameter;
        }

    }

    static class MyCallback implements InvocationCallback<Response> {
        @Getter
        private Response response;

        @Getter
        private Throwable throwable;

        private CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void completed(Response response) {
            this.response = response;
            latch.countDown();
        }

        @Override
        public void failed(Throwable throwable) {
            this.throwable = throwable;
            latch.countDown();
        }

        public boolean await() throws InterruptedException {
            return latch.await(5, TimeUnit.SECONDS);
        }
    }
}
