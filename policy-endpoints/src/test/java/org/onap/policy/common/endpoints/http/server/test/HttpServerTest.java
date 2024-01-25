/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020, 2023-2024 Nordix Foundation.
 * Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import io.prometheus.client.servlet.jakarta.exporter.MetricsServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.gson.GsonTestUtils;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpServletServer JUNIT tests.
 */
public class HttpServerTest {
    private static final String JVM_MEMORY_BYTES_USED = "jvm_memory_bytes_used";
    private static final String METRICS_URI = "/metrics";
    private static final String PROMETHEUS = "prometheus";
    private static final String LOCALHOST = "localhost";
    private static final String JSON_MEDIA = "application/json";
    private static final String YAML_MEDIA = YamlMessageBodyHandler.APPLICATION_YAML;
    private static final String SWAGGER_JSON = "/openapi.json";
    private static final String JUNIT_ECHO_HELLO = "/junit/echo/hello";
    private static final String JUNIT_ECHO_FULL_REQUEST = "/junit/echo/full/request";
    private static final String SOME_TEXT = "some text";
    private static final String HELLO = "hello";

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(HttpServerTest.class);

    private static final String LOCALHOST_PREFIX = "http://localhost:";

    private static final Gson gson = new Gson();

    /**
     * Server port.  Incremented by 10 with each test.
     */
    private static int port = 5608;

    private String portUrl;

    /**
     * Increments the port number, clears the servers, and resets the providers.
     */
    @Before
    public void setUp() {
        incrementPort();
        portUrl = LOCALHOST_PREFIX + port;

        HttpServletServerFactoryInstance.getServerFactory().destroy();

        MyGsonProvider.resetSome();
        MyYamlProvider.resetSome();
    }

    private static void incrementPort() {
        port += 10;
    }

    /**
     * To delete temporary properties cadi_longitude,and cadi_latitude.
     */
    @AfterClass
    public static void tearDownAfterClass() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
        System.clearProperty("cadi_longitude");
        System.clearProperty("cadi_latitude");
    }

    @Test
    public void testDefaultPackageServer() throws Exception {
        logger.info("-- testDefaultPackageServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", LOCALHOST, port, "/", false, true);
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText(SOME_TEXT);
        String reqText = gson.toJson(request);

        String response = http(portUrl + JUNIT_ECHO_FULL_REQUEST, JSON_MEDIA, reqText);
        assertEquals(reqText, response);
    }

    @Test
    public void testGsonPackageServer() throws Exception {
        logger.info("-- testGsonPackageServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", LOCALHOST, port, "/", false, true);

        server.setSerializationProvider(MyGsonProvider.class.getName());
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText(SOME_TEXT);
        String reqText = gson.toJson(request);

        String response = http(portUrl + JUNIT_ECHO_FULL_REQUEST, JSON_MEDIA, reqText);
        assertEquals(reqText, response);

        assertTrue(MyGsonProvider.hasReadSome());
        assertTrue(MyGsonProvider.hasWrittenSome());

        assertFalse(MyYamlProvider.hasReadSome());
        assertFalse(MyYamlProvider.hasWrittenSome());
    }

    @Test
    public void testYamlPackageServer() throws Exception {
        logger.info("-- testYamlPackageServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", LOCALHOST, port, "/", false, true);

        server.setSerializationProvider(MyYamlProvider.class.getName());
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText(SOME_TEXT);
        String reqText = new StandardYamlCoder().encode(request);

        String response = http(portUrl + JUNIT_ECHO_FULL_REQUEST, YAML_MEDIA, reqText);

        // response reader strips newlines, so we should, too, before comparing
        assertEquals(reqText.replace("\n", ""), response);

        assertTrue(MyYamlProvider.hasReadSome());
        assertTrue(MyYamlProvider.hasWrittenSome());

        assertFalse(MyGsonProvider.hasReadSome());
        assertFalse(MyGsonProvider.hasWrittenSome());
    }

    @Test
    public void testDefaultClassServer() throws Exception {
        logger.info("-- testDefaultClassServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", LOCALHOST, port, "/", false, true);
        server.addServletClass("/*", RestEchoService.class.getName());
        server.addFilterClass("/*", TestFilter.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText(SOME_TEXT);
        String reqText = gson.toJson(request);

        String response = http(portUrl + JUNIT_ECHO_FULL_REQUEST, JSON_MEDIA, reqText);
        assertEquals(reqText, response);
    }

    /**
     * This test checks a server from a plain java servlet (note it uses prometheus as the sample server).
     */
    @Test
    public void testStdServletServer() throws Exception {
        logger.info("-- testStdServletServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
            .build(PROMETHEUS, LOCALHOST, port, "/", false, true);

        server.addStdServletClass("/prom-generic-servlet/metrics", MetricsServlet.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());
        assertTrue(server.isPrometheus());

        String response = http(portUrl + "/prom-generic-servlet/metrics");
        assertThat(response).contains(JVM_MEMORY_BYTES_USED);
    }

    /**
     * This test explicitly creates a prometheus server.
     */
    @Test
    public void testExplicitPrometheusServer() throws Exception {
        logger.info("-- testPrometheusServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
            .build(PROMETHEUS, LOCALHOST, port, "/", false, true);
        server.setPrometheus(METRICS_URI);
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());
        assertTrue(server.isPrometheus());

        String response = http(portUrl + METRICS_URI);
        assertThat(response).contains(JVM_MEMORY_BYTES_USED);
    }

    /**
     * This test is an all-in-one for a single server: prometheus, jax-rs, servlet, swagger, and filters.
     */
    @Test
    public void testPrometheusJaxRsFilterSwaggerServer() throws Exception {
        logger.info("-- testPrometheusServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
            .build(PROMETHEUS, LOCALHOST, port, "/", true, true);

        server.addServletClass("/*", RestEchoService.class.getName());
        server.addFilterClass("/*", TestFilter.class.getName());
        server.setPrometheus(METRICS_URI);

        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());
        assertTrue(server.isPrometheus());

        String response = http(portUrl + METRICS_URI);
        assertThat(response).contains(JVM_MEMORY_BYTES_USED);

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText(SOME_TEXT);
        String reqText = gson.toJson(request);

        response = http(portUrl + JUNIT_ECHO_FULL_REQUEST, JSON_MEDIA, reqText);
        assertEquals(reqText, response);

        response = http(portUrl + SWAGGER_JSON);
        assertThat(response).contains("openapi");
    }

    @Test
    public void testJacksonClassServer() throws Exception {
        logger.info("-- testJacksonClassServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", LOCALHOST, port, "/", false, true);
        server.addServletClass("/*", RestEchoService.class.getName());
        server.addFilterClass("/*", TestFilter.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText(SOME_TEXT);
        String reqText = gson.toJson(request);

        String response = http(portUrl + JUNIT_ECHO_FULL_REQUEST, JSON_MEDIA, reqText);
        assertEquals(reqText, response);

        assertFalse(MyGsonProvider.hasReadSome());
        assertFalse(MyGsonProvider.hasWrittenSome());

        assertFalse(MyYamlProvider.hasReadSome());
        assertFalse(MyYamlProvider.hasWrittenSome());
    }

    @Test
    public void testGsonClassServer() throws Exception {
        logger.info("-- testGsonClassServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", LOCALHOST, port, "/", false, true);
        server.setSerializationProvider(MyGsonProvider.class.getName());
        server.addServletClass("/*", RestEchoService.class.getName());
        server.addFilterClass("/*", TestFilter.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText(SOME_TEXT);
        String reqText = gson.toJson(request);

        String response = http(portUrl + JUNIT_ECHO_FULL_REQUEST, JSON_MEDIA, reqText);
        assertEquals(reqText, response);

        assertTrue(MyGsonProvider.hasReadSome());
        assertTrue(MyGsonProvider.hasWrittenSome());

        assertFalse(MyYamlProvider.hasReadSome());
        assertFalse(MyYamlProvider.hasWrittenSome());
    }

    @Test
    public void testYamlClassServer() throws Exception {
        logger.info("-- testYamlClassServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", LOCALHOST, port, "/", false, true);
        server.setSerializationProvider(MyYamlProvider.class.getName());
        server.addServletClass("/*", RestEchoService.class.getName());
        server.addFilterClass("/*", TestFilter.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText(SOME_TEXT);
        String reqText = new StandardYamlCoder().encode(request);

        String response = http(portUrl + JUNIT_ECHO_FULL_REQUEST, YAML_MEDIA, reqText);

        // response reader strips newlines, so we should, too, before comparing
        assertEquals(reqText.replace("\n", ""), response);

        assertTrue(MyYamlProvider.hasReadSome());
        assertTrue(MyYamlProvider.hasWrittenSome());

        assertFalse(MyGsonProvider.hasReadSome());
        assertFalse(MyGsonProvider.hasWrittenSome());
    }

    @Test
    public void testSerialize() {
        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", LOCALHOST, port, "/", false, true);
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getName());

        // ensure we can serialize the server
        new GsonTestUtils().compareGson(server, HttpServerTest.class);
        assertThatCode(() -> new GsonTestUtils().compareGson(server, HttpServerTest.class)).doesNotThrowAnyException();
    }

    @Test
    public void testSingleServer() throws Exception {
        logger.info("-- testSingleServer() --");

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo", LOCALHOST, port, "/", false, true);
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        String response = http(portUrl + JUNIT_ECHO_HELLO);
        assertEquals(HELLO, response);

        assertThatThrownBy(() -> http(portUrl + SWAGGER_JSON)).isInstanceOf(IOException.class);

        response = http(portUrl + "/junit/echo/hello?block=true");
        assertEquals("FILTERED", response);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());
        assertEquals(1, HttpServletServerFactoryInstance.getServerFactory().inventory().size());

        HttpServletServerFactoryInstance.getServerFactory().destroy(port);
        assertEquals(0, HttpServletServerFactoryInstance.getServerFactory().inventory().size());
    }

    @Test
    public void testMultipleServers() throws Exception {
        logger.info("-- testMultipleServers() --");

        HttpServletServer server1 = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo-1", false, LOCALHOST, port, false, "/", true, true);
        server1.addServletPackage("/*", this.getClass().getPackage().getName());
        server1.waitedStart(5000);

        int port2 = port + 1;

        HttpServletServer server2 = HttpServletServerFactoryInstance.getServerFactory()
                        .build("echo-2", LOCALHOST, port2, "/", false, true);
        server2.addServletPackage("/*", this.getClass().getPackage().getName());
        server2.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());
        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port2).isAlive());

        String response = http(portUrl + JUNIT_ECHO_HELLO);
        assertEquals(HELLO, response);

        response = http(portUrl + SWAGGER_JSON);
        assertNotNull(response);

        response = http(LOCALHOST_PREFIX + port2 + JUNIT_ECHO_HELLO);
        assertEquals(HELLO, response);

        assertThatThrownBy(() -> http(LOCALHOST_PREFIX + port2 + SWAGGER_JSON)).isInstanceOf(IOException.class);

        HttpServletServerFactoryInstance.getServerFactory().destroy();
        assertTrue(HttpServletServerFactoryInstance.getServerFactory().inventory().isEmpty());
    }

    @Test
    public void testMultiServicePackage() throws Exception {
        logger.info("-- testMultiServicePackage() --");

        String randomName = UUID.randomUUID().toString();

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build(randomName, LOCALHOST, port, "/", false, true);
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        String response = http(portUrl + JUNIT_ECHO_HELLO);
        assertEquals(HELLO, response);

        response = http(portUrl + "/junit/endpoints/http/servers");
        assertTrue(response.contains(randomName));

        HttpServletServerFactoryInstance.getServerFactory().destroy();
        assertTrue(HttpServletServerFactoryInstance.getServerFactory().inventory().isEmpty());
    }

    @Test
    public void testServiceClass() throws Exception {
        logger.info("-- testServiceClass() --");
        String randomName = UUID.randomUUID().toString();

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build(randomName, LOCALHOST, port, "/", false, true);
        server.addServletClass("/*", RestEchoService.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        String response = http(portUrl + JUNIT_ECHO_HELLO);
        assertEquals(HELLO, response);

        HttpServletServerFactoryInstance.getServerFactory().destroy();
        assertTrue(HttpServletServerFactoryInstance.getServerFactory().inventory().isEmpty());
    }

    @Test
    public void testMultiServiceClass() throws Exception {
        logger.info("-- testMultiServiceClass() --");

        String randomName = UUID.randomUUID().toString();

        HttpServletServer server = HttpServletServerFactoryInstance.getServerFactory()
                        .build(randomName, LOCALHOST, port, "/", false, true);
        server.addServletClass("/*", RestEchoService.class.getName());
        server.addServletClass("/*", RestEndpoints.class.getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());

        String response = http(portUrl + JUNIT_ECHO_HELLO);
        assertEquals(HELLO, response);

        response = http(portUrl + "/junit/endpoints/http/servers");
        assertTrue(response.contains(randomName));

        HttpServletServerFactoryInstance.getServerFactory().destroy();
        assertTrue(HttpServletServerFactoryInstance.getServerFactory().inventory().isEmpty());
    }

    @Test
    public void testSingleStaticResourceServer() throws Exception {
        logger.info("-- testSingleStaticResourceServer() --");

        HttpServletServer staticServer = HttpServletServerFactoryInstance.getServerFactory()
                .buildStaticResourceServer("Static Resources Server", false, LOCALHOST, port, false, "/", true);
        Throwable thrown = catchThrowable(() -> staticServer.addServletResource("/*", null));
        assertThat(thrown).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No resourceBase provided");

        staticServer.addServletResource(null,
                Objects.requireNonNull(HttpServerTest.class.getClassLoader().getResource("webapps/root"))
                    .toExternalForm());

        thrown = catchThrowable(() -> staticServer.addServletClass("/*", RestEchoService.class.getName()));
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("is not supported on this type of jetty server");

        thrown = catchThrowable(() -> staticServer.addServletPackage("/api/*", this.getClass().getPackage().getName()));
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("is not supported on this type of jetty server");

        thrown = catchThrowable(() -> staticServer.setSerializationProvider(MyGsonProvider.class.getName()));
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("is not supported on this type of jetty server");

        staticServer.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());
        assertEquals(1, HttpServletServerFactoryInstance.getServerFactory().inventory().size());

        String response = http(portUrl);
        assertThat(response).contains("Test Jetty Static Resources Root");

        HttpServletServerFactoryInstance.getServerFactory().destroy(port);
        assertEquals(0, HttpServletServerFactoryInstance.getServerFactory().inventory().size());
    }

    @Test
    public void testMultiStaticResourceServer() throws Exception {
        logger.info("-- testMultiStaticResourceServer() --");

        HttpServletServer staticResourceServer = HttpServletServerFactoryInstance.getServerFactory()
                .buildStaticResourceServer("Static Resources Server", false, LOCALHOST, port, false, "/", true);
        staticResourceServer.addServletResource("/root/*",
                Objects.requireNonNull(HttpServerTest.class.getClassLoader().getResource("webapps/root"))
                    .toExternalForm());
        staticResourceServer.addServletResource("/alt-root/*",
                Objects.requireNonNull(HttpServerTest.class.getClassLoader().getResource("webapps/alt-root"))
                    .toExternalForm());
        staticResourceServer.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());
        assertEquals(1, HttpServletServerFactoryInstance.getServerFactory().inventory().size());

        String response = http(portUrl + "/root/");
        assertThat(response).contains("Test Jetty Static Resources Root");

        response = http(portUrl + "/alt-root/");
        assertThat(response).contains("Test Jetty Static Resources Alt-Root");

        HttpServletServerFactoryInstance.getServerFactory().destroy(port);
        assertEquals(0, HttpServletServerFactoryInstance.getServerFactory().inventory().size());
    }

    @Test
    public void testMultiTypesServer() throws Exception {
        logger.info("-- testMultiTypesServer() --");

        HttpServletServer staticResourceServer = HttpServletServerFactoryInstance.getServerFactory()
                .buildStaticResourceServer("Static Resources Server", false, LOCALHOST, port, false, "/", true);
        staticResourceServer.addServletResource("/root/*",
                Objects.requireNonNull(HttpServerTest.class.getClassLoader().getResource("webapps/root"))
                    .toExternalForm());
        staticResourceServer.waitedStart(5000);

        int port2 = port + 1;
        HttpServletServer jerseyServer =
                HttpServletServerFactoryInstance.getServerFactory().build("echo", LOCALHOST, port2, "/", false, true);
        jerseyServer.addServletPackage("/api/*", this.getClass().getPackage().getName());

        Throwable thrown = catchThrowable(() -> jerseyServer.addServletResource("/root/*",
                Objects.requireNonNull(HttpServerTest.class.getClassLoader().getResource("webapps/root"))
                    .toExternalForm()));
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("is not supported on this type of jetty server");

        jerseyServer.waitedStart(5000);

        assertTrue(HttpServletServerFactoryInstance.getServerFactory().get(port).isAlive());
        assertEquals(2, HttpServletServerFactoryInstance.getServerFactory().inventory().size());

        String response = http(portUrl + "/root/");
        assertThat(response).contains("Test Jetty Static Resources Root");

        response = http(LOCALHOST_PREFIX + port2 + "/api" + JUNIT_ECHO_HELLO);
        assertEquals(HELLO, response);

        HttpServletServerFactoryInstance.getServerFactory().destroy();
        assertEquals(0, HttpServletServerFactoryInstance.getServerFactory().inventory().size());
    }

    /**
     * performs an http request.
     *
     * @throws MalformedURLException make sure URL is good
     * @throws IOException thrown is IO exception occurs
     * @throws InterruptedException thrown if thread interrupted occurs
     */
    private String http(String urlString)
            throws IOException, InterruptedException {
        URL url = new URL(urlString);
        if (!NetworkUtil.isTcpPortOpen(url.getHost(), url.getPort(), 25, 100)) {
            throw new IllegalStateException("port never opened: " + url);
        }
        return response(url.openConnection());
    }

    /**
     * Performs a http request.
     *
     * @throws MalformedURLException make sure URL is good
     * @throws IOException thrown is IO exception occurs
     * @throws InterruptedException thrown if thread interrupted occurs
     */
    private String http(String urlString, String mediaType, String post)
            throws IOException, InterruptedException {
        URL url = new URL(urlString);
        if (!NetworkUtil.isTcpPortOpen(url.getHost(), url.getPort(), 25, 100)) {
            throw new IllegalStateException("port never opened: " + url);
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", mediaType);
        conn.setRequestProperty("Accept", mediaType);
        IOUtils.write(post, conn.getOutputStream(), StandardCharsets.UTF_8);
        return response(conn);
    }

    /**
     * gets http response.
     *
     * @param conn connection from which to read
     *
     * @throws IOException if an I/O error occurs
     */
    private String response(URLConnection conn) throws IOException {
        try (InputStream inpstr = conn.getInputStream()) {
            return String.join("", IOUtils.readLines(inpstr, StandardCharsets.UTF_8));
        }
    }

}
