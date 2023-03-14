/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020,2023 Nordix Foundation.
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

package org.onap.policy.common.endpoints.http.server;

import java.util.List;
import java.util.Properties;

/**
 * Factory of HTTP Servlet-Enabled Servlets.
 */
public interface HttpServletServerFactory {

    /**
     * Builds an http or https rest server with support for servlets.
     *
     * @param name name
     * @param https use secured http over tls connection
     * @param host binding host
     * @param port port
     * @param sniHostCheck SNI Host checking flag
     * @param contextPath server base path
     * @param swagger enable swagger documentation
     * @param managed is it managed by infrastructure
     * @return http server
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    HttpServletServer build(String name, boolean https, String host, int port, boolean sniHostCheck, String contextPath,
        boolean swagger, boolean managed);

    /**
     * Builds an http rest server with support for servlets.
     *
     * @param name name
     * @param host binding host
     * @param port port
     * @param contextPath server base path
     * @param swagger enable swagger documentation
     * @param managed is it managed by infrastructure
     * @return http server
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    HttpServletServer build(String name, String host, int port, String contextPath, boolean swagger, boolean managed);

    /**
     * Build a list of http rest servers per properties.
     *
     * @param properties properties based configuration
     * @return list of http servers
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    List<HttpServletServer> build(Properties properties);

    /**
     * Builds an http or https server to manage static resources.
     *
     * @param name name
     * @param https use secured http over tls connection
     * @param host binding host
     * @param port port
     * @param sniHostCheck SNI Host checking flag
     * @param contextPath server base path
     * @param managed is it managed by infrastructure
     * @return http server
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    HttpServletServer buildStaticResourceServer(String name, boolean https, String host, int port, boolean sniHostCheck,
        String contextPath, boolean managed);

    /**
     * Gets a server based on the port.
     *
     * @param port port
     * @return http server
     */
    HttpServletServer get(int port);

    /**
     * Provides an inventory of servers.
     *
     * @return inventory of servers
     */
    List<HttpServletServer> inventory();

    /**
     * Destroys server bound to a port.
     *
     * @param port the port the server is bound to
     */
    void destroy(int port);

    /**
     * Destroys the factory and therefore all servers.
     */
    void destroy();
}
