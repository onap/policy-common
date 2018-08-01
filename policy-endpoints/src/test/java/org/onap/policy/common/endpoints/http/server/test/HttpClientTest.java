/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.properties.HttpClientPropertiesHelper;
import org.onap.policy.common.endpoints.properties.HttpServerPropertiesHelper;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientTest {

    private static Logger logger = LoggerFactory.getLogger(HttpClientTest.class);

    @BeforeClass
    public static void setUp() throws InterruptedException, IOException {
        logger.info("-- setup() --");

        /* echo server - basic auth + https*/

        final HttpServletServer echoServerNoAuth =
            HttpServletServer.factory.build("echo", false, "localhost", 6666, "/", false, true);
        echoServerNoAuth.addServletPackage("/*", HttpClientTest.class.getPackage().getName());
        echoServerNoAuth.waitedStart(5000);

        if (!NetworkUtil.isTcpPortOpen("localhost", echoServerNoAuth.getPort(), 5, 10000L))
            throw new IllegalStateException("cannot connect to port " + echoServerNoAuth.getPort());

        /* unsecured echo server */

        System.setProperty("javax.net.ssl.keyStore", "src/test/resources/keystore-test");
        System.setProperty("javax.net.ssl.keyStorePassword", "kstest");

        final HttpServletServer echoServerAuth =
            HttpServletServer.factory.build("echo", true, "localhost", 6667, "/", false, true);
        echoServerAuth.setBasicAuthentication("x", "y", null);
        echoServerAuth.addServletPackage("/*", HttpClientTest.class.getPackage().getName());
        echoServerAuth.waitedStart(5000);

        if (!NetworkUtil.isTcpPortOpen("localhost", echoServerAuth.getPort(), 5, 10000L))
            throw new IllegalStateException("cannot connect to port " + echoServerAuth.getPort());
    }

    @AfterClass
    public static void tearDown() {
        logger.info("-- tearDown() --");

        HttpServletServer.factory.destroy();
        HttpClient.factory.destroy();
    }

    @Test
    public void testHttpNoAuthClient() throws Exception {
        logger.info("-- testHttpNoAuthClient() --");

        final HttpClient client = HttpClient.factory.build("testHttpNoAuthClient", false, false,
            "localhost", 6666, "junit/echo", null, null, true);
        final Response response = client.get("hello");
        final String body = HttpClient.getBody(response, String.class);

        assertTrue(response.getStatus() == 200);
        assertTrue(body.equals("hello"));
    }

    @Test
    public void testHttpAuthClient() throws Exception {
        logger.info("-- testHttpAuthClient() --");

        final HttpClient client = HttpClient.factory.build("testHttpAuthClient", true, true,
            "localhost", 6667, "junit/echo", "x", "y", true);
        final Response response = client.get("hello");
        final String body = HttpClient.getBody(response, String.class);

        assertTrue(response.getStatus() == 200);
        assertTrue(body.equals("hello"));
    }

    @Test
    public void testHttpAuthClient401() throws Exception {
        logger.info("-- testHttpAuthClient401() --");

        final HttpClient client = HttpClient.factory.build("testHttpAuthClient401", true, true,
            "localhost", 6667, "junit/echo", null, null, true);
        final Response response = client.get("hello");
        assertTrue(response.getStatus() == 401);
    }

    @Test
    public void testHttpAuthClientProps() throws Exception {
        logger.info("-- testHttpAuthClientProps() --");

        Properties httpProperties = new Properties();

        HttpServerPropertiesHelper serverProps = new HttpServerPropertiesHelper(httpProperties);
        serverProps.setEndpointNames("PAP,PDP");

        serverProps.setHost("PAP", "localhost");
        serverProps.setPort("PAP", 7777);
        serverProps.setUserName("PAP", "testpap");
        serverProps.setPassword("PAP", "alpha123");
        serverProps.setRestClasses("PAP", RestMockHealthCheck.class.getName());
        serverProps.setManaged("PAP", true);

        serverProps.setHost("PDP", "localhost");
        serverProps.setPort("PDP", 7778);
        serverProps.setUserName("PDP", "testpdp");
        serverProps.setPassword("PDP", "alpha123");
        serverProps.setRestClasses("PDP", RestMockHealthCheck.class.getName());
        serverProps.setManaged("PDP", true);

        HttpClientPropertiesHelper clientProps = new HttpClientPropertiesHelper(httpProperties);
        clientProps.setEndpointNames("PAP,PDP");

        clientProps.setHost("PAP", "localhost");
        clientProps.setPort("PAP", 7777);
        clientProps.setUserName("PAP", "testpap");
        clientProps.setPassword("PAP", "alpha123");
        clientProps.setContextUriPath("PAP", "pap/test");
        clientProps.setHttps("PAP", false);
        clientProps.setManaged("PAP", true);

        clientProps.setHost("PDP", "localhost");
        clientProps.setPort("PDP", 7778);
        clientProps.setUserName("PDP", "testpdp");
        clientProps.setPassword("PDP", "alpha123");
        clientProps.setContextUriPath("PDP", "pdp");
        clientProps.setHttps("PDP", false);
        clientProps.setManaged("PDP", true);

        List<HttpServletServer> servers = HttpServletServer.factory.build(httpProperties);
        assertTrue(servers.size() == 2);

        List<HttpClient> clients = HttpClient.factory.build(httpProperties);
        assertTrue(clients.size() == 2);

        for (final HttpServletServer server : servers) {
            server.waitedStart(10000);
        }

        HttpClient clientPAP = HttpClient.factory.get("PAP");
        Response response = clientPAP.get();
        assertTrue(response.getStatus() == 200);

        HttpClient clientPDP = HttpClient.factory.get("PDP");
        Response response2 = clientPDP.get("test");
        assertTrue(response2.getStatus() == 500);
    }


}
