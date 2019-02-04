/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.utils.gson.GsonTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpServletServer JUNIT tests.
 */
public class HttpServerTest {

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(HttpServerTest.class);

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
        port += 10;
        portUrl = LOCALHOST_PREFIX + port;

        HttpServletServer.factory.destroy();

        MyJacksonProvider.resetSome();
        MyGsonProvider.resetSome();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        HttpServletServer.factory.destroy();
    }

    @Test
    public void testDefaultPackageServer() throws Exception {
        logger.info("-- testDefaultPackageServer() --");

        HttpServletServer server = HttpServletServer.factory.build("echo", "localhost", port, "/", false, true);
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getCanonicalName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText("some text");
        String reqText = gson.toJson(request);

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/full/request", reqText);
        assertEquals(reqText, response);
    }

    @Test
    public void testJacksonPackageServer() throws Exception {
        logger.info("-- testJacksonPackageServer() --");

        HttpServletServer server = HttpServletServer.factory.build("echo", "localhost", port, "/", false, true);

        server.setSerializationProvider(MyJacksonProvider.class.getCanonicalName());
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getCanonicalName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText("some text");
        String reqText = gson.toJson(request);

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/full/request", reqText);
        assertEquals(reqText, response);

        assertTrue(MyJacksonProvider.hasReadSome());
        assertTrue(MyJacksonProvider.hasWrittenSome());

        assertFalse(MyGsonProvider.hasReadSome());
        assertFalse(MyGsonProvider.hasWrittenSome());
    }

    @Test
    public void testGsonPackageServer() throws Exception {
        logger.info("-- testGsonPackageServer() --");

        HttpServletServer server = HttpServletServer.factory.build("echo", "localhost", port, "/", false, true);

        server.setSerializationProvider(MyGsonProvider.class.getCanonicalName());
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getCanonicalName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText("some text");
        String reqText = gson.toJson(request);

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/full/request", reqText);
        assertEquals(reqText, response);

        assertTrue(MyGsonProvider.hasReadSome());
        assertTrue(MyGsonProvider.hasWrittenSome());

        assertFalse(MyJacksonProvider.hasReadSome());
        assertFalse(MyJacksonProvider.hasWrittenSome());
    }

    @Test
    public void testDefaultClassServer() throws Exception {
        logger.info("-- testDefaultClassServer() --");

        HttpServletServer server = HttpServletServer.factory.build("echo", "localhost", port, "/", false, true);
        server.addServletClass("/*", RestEchoService.class.getCanonicalName());
        server.addFilterClass("/*", TestFilter.class.getCanonicalName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText("some text");
        String reqText = gson.toJson(request);

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/full/request", reqText);
        assertEquals(reqText, response);
    }

    @Test
    public void testJacksonClassServer() throws Exception {
        logger.info("-- testJacksonClassServer() --");

        HttpServletServer server = HttpServletServer.factory.build("echo", "localhost", port, "/", false, true);
        server.setSerializationProvider(MyJacksonProvider.class.getCanonicalName());
        server.addServletClass("/*", RestEchoService.class.getCanonicalName());
        server.addFilterClass("/*", TestFilter.class.getCanonicalName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText("some text");
        String reqText = gson.toJson(request);

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/full/request", reqText);
        assertEquals(reqText, response);

        assertTrue(MyJacksonProvider.hasReadSome());
        assertTrue(MyJacksonProvider.hasWrittenSome());

        assertFalse(MyGsonProvider.hasReadSome());
        assertFalse(MyGsonProvider.hasWrittenSome());
    }

    @Test
    public void testGsonClassServer() throws Exception {
        logger.info("-- testGsonClassServer() --");

        HttpServletServer server = HttpServletServer.factory.build("echo", "localhost", port, "/", false, true);
        server.setSerializationProvider(MyGsonProvider.class.getCanonicalName());
        server.addServletClass("/*", RestEchoService.class.getCanonicalName());
        server.addFilterClass("/*", TestFilter.class.getCanonicalName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());

        RestEchoReqResp request = new RestEchoReqResp();
        request.setRequestId(100);
        request.setText("some text");
        String reqText = gson.toJson(request);

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/full/request", reqText);
        assertEquals(reqText, response);

        assertTrue(MyGsonProvider.hasReadSome());
        assertTrue(MyGsonProvider.hasWrittenSome());

        assertFalse(MyJacksonProvider.hasReadSome());
        assertFalse(MyJacksonProvider.hasWrittenSome());
    }
    
    @Test
    public void testSerialize() {
        HttpServletServer server = HttpServletServer.factory.build("echo", "localhost", port, "/", false, true);
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getCanonicalName());

        // ensure we can serialize the server
        new GsonTestUtils().compareJackson2Gson(server);
    }

    @Test
    public void testSingleServer() throws Exception {
        logger.info("-- testSingleServer() --");

        HttpServletServer server = HttpServletServer.factory.build("echo", "localhost", port, "/", false, true);
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.addFilterClass("/*", TestFilter.class.getCanonicalName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());
        assertFalse(HttpServletServer.factory.get(port).isAaf());

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/hello");
        assertTrue("hello".equals(response));

        response = null;
        try {
            response = http(HttpServletServer.factory.get(port), portUrl + "/swagger.json");
        } catch (IOException e) {
            // Expected
        }
        assertTrue(response == null);

        response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/hello?block=true");
        assertEquals("FILTERED", response);

        assertTrue(HttpServletServer.factory.get(port).isAlive());
        assertEquals(1, HttpServletServer.factory.inventory().size());

        server.setAafAuthentication("/*");
        assertTrue(HttpServletServer.factory.get(port).isAaf());

        HttpServletServer.factory.destroy(port);
        assertEquals(0, HttpServletServer.factory.inventory().size());
    }

    @Test
    public void testMultipleServers() throws Exception {
        logger.info("-- testMultipleServers() --");

        HttpServletServer server1 = HttpServletServer.factory.build("echo-1", false,"localhost", port, "/", true, true);
        server1.addServletPackage("/*", this.getClass().getPackage().getName());
        server1.waitedStart(5000);

        int port2 = port + 1;

        HttpServletServer server2 = HttpServletServer.factory.build("echo-2", "localhost", port2, "/", false, true);
        server2.addServletPackage("/*", this.getClass().getPackage().getName());
        server2.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());
        assertTrue(HttpServletServer.factory.get(port2).isAlive());

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/hello");
        assertTrue("hello".equals(response));

        response = http(HttpServletServer.factory.get(port), portUrl + "/swagger.json");
        assertTrue(response != null);

        response = http(HttpServletServer.factory.get(port2), LOCALHOST_PREFIX + port2 + "/junit/echo/hello");
        assertTrue("hello".equals(response));

        response = null;
        try {
            response = http(HttpServletServer.factory.get(port2), LOCALHOST_PREFIX + port2 + "/swagger.json");
        } catch (IOException e) {
            // Expected
        }
        assertTrue(response == null);

        HttpServletServer.factory.destroy();
        assertTrue(HttpServletServer.factory.inventory().size() == 0);
    }

    @Test
    public void testMultiServicePackage() throws Exception {
        logger.info("-- testMultiServicePackage() --");

        String randomName = UUID.randomUUID().toString();

        HttpServletServer server = HttpServletServer.factory.build(randomName, "localhost", port, "/", false, true);
        server.addServletPackage("/*", this.getClass().getPackage().getName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/hello");
        assertTrue("hello".equals(response));

        response = http(HttpServletServer.factory.get(port), portUrl + "/junit/endpoints/http/servers");
        assertTrue(response.contains(randomName));

        HttpServletServer.factory.destroy();
        assertTrue(HttpServletServer.factory.inventory().size() == 0);
    }

    @Test
    public void testServiceClass() throws Exception {
        logger.info("-- testServiceClass() --");
        String randomName = UUID.randomUUID().toString();

        HttpServletServer server = HttpServletServer.factory.build(randomName, "localhost", port, "/", false, true);
        server.addServletClass("/*", RestEchoService.class.getCanonicalName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/hello");
        assertTrue("hello".equals(response));

        HttpServletServer.factory.destroy();
        assertTrue(HttpServletServer.factory.inventory().size() == 0);
    }

    @Test
    public void testMultiServiceClass() throws Exception {
        logger.info("-- testMultiServiceClass() --");

        String randomName = UUID.randomUUID().toString();

        HttpServletServer server = HttpServletServer.factory.build(randomName, "localhost", port, "/", false, true);
        server.addServletClass("/*", RestEchoService.class.getCanonicalName());
        server.addServletClass("/*", RestEndpoints.class.getCanonicalName());
        server.waitedStart(5000);

        assertTrue(HttpServletServer.factory.get(port).isAlive());

        String response = http(HttpServletServer.factory.get(port), portUrl + "/junit/echo/hello");
        assertTrue("hello".equals(response));

        response = http(HttpServletServer.factory.get(port), portUrl + "/junit/endpoints/http/servers");
        assertTrue(response.contains(randomName));

        HttpServletServer.factory.destroy();
        assertTrue(HttpServletServer.factory.inventory().size() == 0);
    }

    /**
     * performs an http request.
     *
     * @throws MalformedURLException make sure URL is good
     * @throws IOException thrown is IO exception occurs
     * @throws InterruptedException thrown if thread interrupted occurs
     */
    protected String http(HttpServletServer server, String urlString)
            throws MalformedURLException, IOException, InterruptedException {
        URL url = new URL(urlString);
        String response = null;
        int numRetries = 1;
        int maxNumberRetries = 5;
        while (numRetries <= maxNumberRetries) {
            try {
                URLConnection conn = url.openConnection();
                response = response(conn);
                break;
            } catch (ConnectException e) {
                logger.warn("http server {} @ {} ({}) - cannot connect yet ..", server, urlString, numRetries, e);
                numRetries++;
                Thread.sleep(10000L);
            } catch (Exception e) {
                throw e;
            }
        }

        return response;
    }

    /**
     * Performs an http request.
     *
     * @throws MalformedURLException make sure URL is good
     * @throws IOException thrown is IO exception occurs
     * @throws InterruptedException thrown if thread interrupted occurs
     */
    protected String http(HttpServletServer server, String urlString, String post)
            throws MalformedURLException, IOException, InterruptedException {
        URL url = new URL(urlString);
        String response = null;
        int numRetries = 1;
        int maxNumberRetries = 5;
        while (numRetries <= maxNumberRetries) {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                IOUtils.write(post, conn.getOutputStream());
                response = response(conn);
                break;
            } catch (ConnectException e) {
                logger.warn("http server {} @ {} ({}) - cannot connect yet ..", server, urlString, numRetries, e);
                numRetries++;
                Thread.sleep(10000L);
            } catch (Exception e) {
                logger.error("http error", e);
                throw e;
            }
        }

        return response;
    }

    /**
     * gets http response.
     *
     * @param conn connection from which to read
     *
     * @throws IOException if an I/O error occurs
     */
    protected String response(URLConnection conn) throws IOException {
        String response = "";
        try (BufferedReader ioReader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = ioReader.readLine()) != null) {
                response += line;
            }
        }
        return response;
    }

}
