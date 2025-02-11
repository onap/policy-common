/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2025 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.endpoints.http.server.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JettyServletServerTest {

    @Test
    void testJettyServletServer() {
        var server = new TestServlet("", false, "", 8080, false, "");
        assertNotNull(server);
        assertEquals("http-8080", server.getName());
        assertEquals("localhost", server.getHost());
        assertEquals(8080, server.getPort());
        assertEquals("/", server.contextPath);
    }

    @Test
    void testPortExceptions() {
        assertThrows(IllegalArgumentException.class,
            () -> new TestServlet("", false, "", -1, false, ""));
        assertThrows(IllegalArgumentException.class,
            () -> new TestServlet("", false, "", 65535, false, ""));
    }

    @Test
    void testBasicAuthServletPath() {
        var server = new TestServlet("", false, "", 8080, false, "");
        assertDoesNotThrow(() -> server.setBasicAuthentication("user", "password", "/path"));
    }

    @Test
    void testBasicAuthException() {
        var server = new TestServlet("", false, "", 8080, false, "");
        assertThrows(IllegalArgumentException.class, () -> server.setBasicAuthentication("", "pass", "path"));
        assertThrows(IllegalArgumentException.class, () -> server.setBasicAuthentication("user", "", "path"));
    }

    public static class TestServlet extends JettyServletServer {

        public TestServlet(String name, boolean https, String host, int port, boolean sniHostCheck,
                              String contextPath) {
            super(name, https, host, port, sniHostCheck, contextPath);
        }
    }
}
