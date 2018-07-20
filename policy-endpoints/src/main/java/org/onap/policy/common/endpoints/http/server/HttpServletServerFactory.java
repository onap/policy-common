/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
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

package org.onap.policy.common.endpoints.http.server;

import java.util.List;
import java.util.Properties;

/**
 * Factory of HTTP Servlet-Enabled Servlets
 */
public interface HttpServletServerFactory {

    /**
     * builds an http server with support for servlets
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
    public HttpServletServer build(String name, String host, int port, String contextPath, boolean swagger,
            boolean managed);

    /**
     * list of http servers per properties
     * 
     * @param properties properties based configuration
     * @return list of http servers
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    public List<HttpServletServer> build(Properties properties);

    /**
     * gets a server based on the port
     * 
     * @param port port
     * @return http server
     */
    public HttpServletServer get(int port);

    /**
     * provides an inventory of servers
     * 
     * @return inventory of servers
     */
    public List<HttpServletServer> inventory();

    /**
     * destroys server bound to a port
     * 
     * @param port
     */
    public void destroy(int port);

    /**
     * destroys the factory and therefore all servers
     */
    public void destroy();
}
