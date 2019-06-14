/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Samsung Electronics Co., Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.internal.JettyJerseyServer;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.utils.network.NetworkUtil;

public class HttpClientTest {
    private static final String TEST_HTTP_NO_AUTH_CLIENT = "testHttpNoAuthClient";
    private static final String TEST_HTTP_AUTH_CLIENT = "testHttpAuthClient";
    private static final String LOCALHOST = "localhost";
    private static final String JUNIT_ECHO = "junit/echo";
    private static final String HELLO = "hello";
    private static final String MY_VALUE = "myValue";
    private static final String FALSE_STRING = "false";
    private static final String ALPHA123 = "alpha123";
    private static final String PUT_HELLO = "PUT:hello:{myParameter=myValue}";
    private static final String DOT_GSON = "." + "GSON";
    private static final String DOT_JACKSON = "." + "JACKSON";
    private static final String DOT_PDP = "." + "PDP";
    private static final String DOT_PAP = "." + "PAP";

    private static final HashMap<String, String> savedValuesMap = new HashMap<>();

    /**
     * Setup before class method.
     *
     * @throws InterruptedException can be interrupted
     * @throws IOException can have an IO exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws InterruptedException, IOException {
        /* echo server - http + no auth */

        final HttpServletServer echoServerNoAuth =
                HttpServletServer.factory.build("echo", LOCALHOST, 6666, "/", false, true);
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

        final HttpServletServer echoServerAuth =
                HttpServletServer.factory.build("echo", true, LOCALHOST, 6667, "/", false, true);
        echoServerAuth.setBasicAuthentication("x", "y", null);
        echoServerAuth.addServletPackage("/*", HttpClientTest.class.getPackage().getName());
        echoServerAuth.addFilterClass("/*", TestFilter.class.getName());
        echoServerAuth.addFilterClass("/*", TestAuthorizationFilter.class.getName());
        echoServerAuth.addFilterClass("/*", TestAafAuthFilter.class.getName());
        echoServerAuth.addFilterClass("/*", TestAafGranularAuthFilter.class.getName());
        echoServerAuth.waitedStart(5000);

        if (!NetworkUtil.isTcpPortOpen(LOCALHOST, echoServerAuth.getPort(), 5, 10000L)) {
            throw new IllegalStateException("cannot connect to port " + echoServerAuth.getPort());
        }
    }

    /**
     * Clear https clients and reset providers.
     */
    @Before
    public void setUp() {
        HttpClient.factory.destroy();

        MyGsonProvider.resetSome();
        MyJacksonProvider.resetSome();
    }

    /**
     * After the class is created method.
     */
    @AfterClass
    public static void tearDownAfterClass() {
        HttpServletServer.factory.destroy();
        HttpClient.factory.destroy();

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
    public void testHttpGetNoAuthClient() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false,
            6666);
        final Response response = client.get(HELLO);
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(HELLO, body);
    }

    @Test
    public void testHttpPutNoAuthClient() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false, 6666);

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        final Response response = client.put(HELLO, entity, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);
    }

    @Test
    public void testHttpPostNoAuthClient() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false,
            6666);

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        final Response response = client.post(HELLO, entity, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals("POST:hello:{myParameter=myValue}", body);
    }

    @Test
    public void testHttpDeletetNoAuthClient() throws Exception {
        final HttpClient client = getNoAuthHttpClient(TEST_HTTP_NO_AUTH_CLIENT, false,
            6666);

        final Response response = client.delete(HELLO, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals("DELETE:hello", body);
    }

    @Test
    public void testHttpGetAuthClient() throws Exception {
        final HttpClient client = getAuthHttpClient();

        final Response response = client.get(HELLO);
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(HELLO, body);
    }

    @Test
    public void testHttpPutAuthClient() throws Exception {
        final HttpClient client = getAuthHttpClient();

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        final Response response = client.put(HELLO, entity, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);
    }

    @Test
    public void testHttpPutAuthClient_JacksonProvider() throws Exception {
        final HttpClient client = HttpClient.factory.build(BusTopicParams.builder().clientName(TEST_HTTP_AUTH_CLIENT)
                        .useHttps(true).allowSelfSignedCerts(true).hostname(LOCALHOST).port(6667)
                        .basePath(JUNIT_ECHO).userName("x").password("y").managed(true)
                        .serializationProvider(MyJacksonProvider.class.getName()).build());

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        final Response response = client.put(HELLO, entity, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);

        assertTrue(MyJacksonProvider.hasWrittenSome());

        assertFalse(MyGsonProvider.hasWrittenSome());
    }

    @Test
    public void testHttpPutAuthClient_GsonProvider() throws Exception {
        final HttpClient client = HttpClient.factory.build(BusTopicParams.builder().clientName(TEST_HTTP_AUTH_CLIENT)
                        .useHttps(true).allowSelfSignedCerts(true).hostname(LOCALHOST).port(6667)
                        .basePath(JUNIT_ECHO).userName("x").password("y").managed(true)
                        .serializationProvider(MyGsonProvider.class.getName()).build());

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);
        final Response response = client.put(HELLO, entity, Collections.emptyMap());
        final String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);

        assertTrue(MyGsonProvider.hasWrittenSome());

        assertFalse(MyJacksonProvider.hasWrittenSome());
    }

    @Test
    public void testHttpAuthClient401() throws Exception {
        final HttpClient client = getNoAuthHttpClient("testHttpAuthClient401", true,
            6667);
        final Response response = client.get(HELLO);
        assertEquals(401, response.getStatus());
    }

    @Test
    public void testHttpAuthClientProps() throws Exception {
        final Properties httpProperties = new Properties();

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, "PAP,PDP");
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
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");

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
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PAP
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES, "PAP,PDP");
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

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, LOCALHOST);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, "7778");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_URL_SUFFIX, "pdp");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, FALSE_STRING);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX, "testpdp");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX, ALPHA123);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_PDP
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");

        final List<HttpServletServer> servers = HttpServletServer.factory.build(httpProperties);
        assertEquals(2, servers.size());

        final List<HttpClient> clients = HttpClient.factory.build(httpProperties);
        assertEquals(2, clients.size());

        for (final HttpServletServer server : servers) {
            server.waitedStart(10000);
        }

        final HttpClient clientPap = HttpClient.factory.get("PAP");
        final Response response = clientPap.get();
        assertEquals(200, response.getStatus());

        final HttpClient clientPdp = HttpClient.factory.get("PDP");
        final Response response2 = clientPdp.get("test");
        assertEquals(500, response2.getStatus());

        assertFalse(MyJacksonProvider.hasWrittenSome());
        assertFalse(MyGsonProvider.hasWrittenSome());
    }

    @Test
    public void testHttpAuthClientProps_MixedProviders() throws Exception {
        final Properties httpProperties = new Properties();

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES, "GSON,JACKSON");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_GSON
                        + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, LOCALHOST);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_GSON
                        + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, "6666");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_GSON
                        + PolicyEndPointProperties.PROPERTY_HTTP_URL_SUFFIX, JUNIT_ECHO);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_GSON
                        + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, FALSE_STRING);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_GSON
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");
        httpProperties.setProperty(
                        PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_GSON
                                        + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER,
                        MyGsonProvider.class.getName());

        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_JACKSON
                        + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, LOCALHOST);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_JACKSON
                        + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, "6666");
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_JACKSON
                        + PolicyEndPointProperties.PROPERTY_HTTP_URL_SUFFIX, JUNIT_ECHO);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_JACKSON
                        + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, FALSE_STRING);
        httpProperties.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_JACKSON
                        + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");
        httpProperties.setProperty(
                        PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + DOT_JACKSON
                                        + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER,
                        MyJacksonProvider.class.getName());

        final List<HttpClient> clients = HttpClient.factory.build(httpProperties);
        assertEquals(2, clients.size());

        Entity<MyEntity> entity = Entity.entity(new MyEntity(MY_VALUE), MediaType.APPLICATION_JSON);

        // use gson client
        MyGsonProvider.resetSome();
        MyJacksonProvider.resetSome();
        HttpClient client = HttpClient.factory.get("GSON");

        Response response = client.put(HELLO, entity, Collections.emptyMap());
        String body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);

        assertTrue(MyGsonProvider.hasWrittenSome());
        assertFalse(MyJacksonProvider.hasWrittenSome());

        // use jackson client
        MyGsonProvider.resetSome();
        MyJacksonProvider.resetSome();
        client = HttpClient.factory.get("JACKSON");

        response = client.put(HELLO, entity, Collections.emptyMap());
        body = HttpClient.getBody(response, String.class);

        assertEquals(200, response.getStatus());
        assertEquals(PUT_HELLO, body);

        assertTrue(MyJacksonProvider.hasWrittenSome());
        assertFalse(MyGsonProvider.hasWrittenSome());
    }

    private HttpClient getAuthHttpClient()
                    throws KeyManagementException, NoSuchAlgorithmException, ClassNotFoundException {
        return HttpClient.factory.build(BusTopicParams.builder().clientName(TEST_HTTP_AUTH_CLIENT)
            .useHttps(true).allowSelfSignedCerts(true).hostname(LOCALHOST).port(6667).basePath(JUNIT_ECHO)
            .userName("x").password("y").managed(true).build());
    }

    private HttpClient getNoAuthHttpClient(String clientName, boolean https, int port)
        throws KeyManagementException, NoSuchAlgorithmException, ClassNotFoundException {
        return HttpClient.factory.build(BusTopicParams.builder().clientName(clientName)
            .useHttps(https).allowSelfSignedCerts(https).hostname(LOCALHOST).port(port).basePath(JUNIT_ECHO)
            .userName(null).password(null).managed(true).build());
    }


    class MyEntity {

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

}
