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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.onap.policy.common.endpoints.http.server.internal.JettyJerseyServer;
import org.onap.policy.common.endpoints.properties.HttpServerPropertiesHelper;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of HTTP Servlet-Enabled Servlets
 */
public interface HttpServletServerFactory {

    /**
     * builds an http or https server with support for servlets
     *
     * @param name service name
     * @param https use https?
     * @param host binding host
     * @param port port
     * @param contextPath server base path
     * @param swagger generate swagger spec?
     * @param managed managed by the engine infrastructure?
     * @return http server
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    HttpServletServer build(String name, boolean https, String host, int port, String contextPath,
        boolean swagger, boolean managed);

    /**
     * builds an http server with support for servlets
     *
     * @param name service name
     * @param host binding host
     * @param port port
     * @param contextPath server base path
     * @param swagger generate swagger spec?
     * @param managed managed by the engine infrastructure?
     * @return http server
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    HttpServletServer build(String name, String host, int port, String contextPath,
        boolean swagger, boolean managed);

    /**
     * list of http servers per properties
     *
     * @param properties properties based configuration
     * @return list of http servers
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    List<HttpServletServer> build(Properties properties);

    /**
     * gets a server based on the port
     *
     * @param port port
     * @return http server
     */
    HttpServletServer get(int port);

    /**
     * provides an inventory of servers
     *
     * @return inventory of servers
     */
    List<HttpServletServer> inventory();

    /**
     * destroys server bound to a port
     * @param port port
     */
    void destroy(int port);

    /**
     * destroys the factory and therefore all servers
     */
    void destroy();
}

/**
 * Indexed factory implementation
 */
class IndexedHttpServletServerFactory implements HttpServletServerFactory {

    /**
     * logger
     */
    protected static Logger logger = LoggerFactory.getLogger(IndexedHttpServletServerFactory.class);

    /**
     * servers index
     */
    protected HashMap<Integer, HttpServletServer> servers = new HashMap<>();

    @Override
    public synchronized HttpServletServer build(String name, boolean https, String host, int port,
        String contextPath, boolean swagger,
        boolean managed) {

        if (servers.containsKey(port))
            return servers.get(port);

        JettyJerseyServer server = new JettyJerseyServer(name, https, host, port, contextPath, swagger);
        if (managed)
            servers.put(port, server);

        return server;
    }

    @Override
    public HttpServletServer build(String name, String host, int port, String contextPath, boolean swagger,
                                    boolean managed) {
        return build(name, false, host, port, contextPath, swagger, managed);
    }

    @Override
    public synchronized List<HttpServletServer> build(Properties properties) {
        ArrayList<HttpServletServer> serviceList = new ArrayList<>();

        HttpServerPropertiesHelper serverProperties = new HttpServerPropertiesHelper(properties);
        for (String serviceName : serverProperties.getEndpointNames()) {
            try {
                int servicePort = serverProperties.getPort(serviceName);
                String hostName = serverProperties.getHost(serviceName);
                String contextUriPath = serverProperties.getContextUriPath(serviceName);
                String userName = serverProperties.getUserName(serviceName);
                String password = serverProperties.getPassword(serviceName);
                String authUriPath = serverProperties.getAuthUriPath(serviceName);
                String restUriPath = serverProperties.getRestUriPath(serviceName);
                boolean https = serverProperties.isHttps(serviceName, false);

                boolean managed = serverProperties.isManaged(serviceName, true);
                boolean swagger = serverProperties.isSwagger(serviceName, false);

                HttpServletServer service = build(serviceName, https, hostName, servicePort, contextUriPath,
                    swagger, managed);
                if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
                    service.setBasicAuthentication(userName, password, authUriPath);
                }

                for (String restClass : serverProperties.getRestClasses(serviceName)) {
                    service.addServletClass(restUriPath, restClass);
                }

                for (String restPackage : serverProperties.getRestPackages(serviceName)) {
                    service.addServletPackage(restUriPath, restPackage);
                }

                serviceList.add(service);
            } catch (NumberFormatException nfe) {
                logger.warn("No HTTP port found for service in {}", serviceName, nfe);
            }
        }

        return serviceList;
    }

    @Override
    public synchronized HttpServletServer get(int port) {

        if (servers.containsKey(port)) {
            return servers.get(port);
        }

        throw new IllegalArgumentException("Http Server for " + port + " not found");
    }

    @Override
    public synchronized List<HttpServletServer> inventory() {
        return new ArrayList<>(this.servers.values());
    }

    @Override
    public synchronized void destroy(int port) {

        if (!servers.containsKey(port)) {
            return;
        }

        HttpServletServer server = servers.remove(port);
        server.shutdown();
    }

    @Override
    public synchronized void destroy() {
        List<HttpServletServer> httpServletServers = this.inventory();
        for (HttpServletServer server: httpServletServers) {
            server.shutdown();
        }

        synchronized(this) {
            this.servers.clear();
        }
    }

}
