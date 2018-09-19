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
import org.onap.aaf.cadi.filter.CadiFilter;
import org.onap.policy.common.endpoints.http.server.internal.JettyJerseyServer;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory of HTTP Servlet-Enabled Servlets.
 */
public interface HttpServletServerFactory {

    /**
     * Builds an http or https server with support for servlets.
     * 
     * @param name name
     * @param https use secured http over tls connection
     * @param host binding host
     * @param port port
     * @param contextPath server base path
     * @param swagger enable swagger documentation
     * @param managed is it managed by infrastructure
     * @return http server
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    HttpServletServer build(String name, boolean https, String host, int port, String contextPath, boolean swagger,
            boolean managed);

    /**
     * Builds an http server with support for servlets.
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
    HttpServletServer build(String name, String host, int port, String contextPath, boolean swagger,
        boolean managed);

    /**
     * Build a list of http servers per properties.
     * 
     * @param properties properties based configuration
     * @return list of http servers
     * @throws IllegalArgumentException when invalid parameters are provided
     */
    List<HttpServletServer> build(Properties properties);

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


/**
 * Indexed factory implementation.
 */
class IndexedHttpServletServerFactory implements HttpServletServerFactory {

    private static final String SPACES_COMMA_SPACES = "\\s*,\\s*";

    /**
     * logger.
     */
    protected static Logger logger = LoggerFactory.getLogger(IndexedHttpServletServerFactory.class);

    /**
     * servers index.
     */
    protected HashMap<Integer, HttpServletServer> servers = new HashMap<>();

    @Override
    public synchronized HttpServletServer build(String name, boolean https, 
                    String host, int port, String contextPath, boolean swagger,
            boolean managed) {

        if (servers.containsKey(port)) {
            return servers.get(port);
        }

        JettyJerseyServer server = new JettyJerseyServer(name, https, host, port, contextPath, swagger);
        if (managed) {
            servers.put(port, server);
        }

        return server;
    }

    @Override
    public synchronized HttpServletServer build(String name, String host, int port, String contextPath,
                                                boolean swagger, boolean managed) {
        return build(name, false, host, port, contextPath, swagger, managed);
    }


    @Override
    public synchronized List<HttpServletServer> build(Properties properties) {

        ArrayList<HttpServletServer> serviceList = new ArrayList<>();

        String serviceNames = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES);
        if (serviceNames == null || serviceNames.isEmpty()) {
            logger.warn("No topic for HTTP Service: {}", properties);
            return serviceList;
        }

        List<String> serviceNameList = Arrays.asList(serviceNames.split(SPACES_COMMA_SPACES));

        for (String serviceName : serviceNameList) {
            String servicePortString = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES
                    + "." + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX);

            int servicePort;
            try {
                if (servicePortString == null || servicePortString.isEmpty()) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("No HTTP port for service in {}", serviceName);
                    }
                    continue;
                }
                servicePort = Integer.parseInt(servicePortString);
            } catch (NumberFormatException nfe) {
                if (logger.isWarnEnabled()) {
                    logger.warn("No HTTP port for service in {}", serviceName);
                }
                continue;
            }

            String hostName = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX);

            String contextUriPath = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX);

            String userName = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX);

            String password = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX);

            String authUriPath = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_URIPATH_SUFFIX);

            String restClasses = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX);

            String filterClasses = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_FILTER_CLASSES_SUFFIX);

            String restPackages = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_REST_PACKAGES_SUFFIX);
            String restUriPath = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_REST_URIPATH_SUFFIX);

            String managedString = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX);
            boolean managed = true;
            if (managedString != null && !managedString.isEmpty()) {
                managed = Boolean.parseBoolean(managedString);
            }

            String swaggerString = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                    + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX);
            boolean swagger = false;
            if (swaggerString != null && !swaggerString.isEmpty()) {
                swagger = Boolean.parseBoolean(swaggerString);
            }

            String httpsString = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                + serviceName + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX);
            boolean https = false;
            if (httpsString != null && !httpsString.isEmpty()) {
                https = Boolean.parseBoolean(httpsString);
            }

            String aafString = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "."
                + serviceName + PolicyEndPointProperties.PROPERTY_AAF_SUFFIX);
            boolean aaf = false;
            if (aafString != null && !aafString.isEmpty()) {
                aaf = Boolean.parseBoolean(aafString);
            }

            HttpServletServer service = build(serviceName, https, hostName, servicePort, contextUriPath, swagger, managed);

            /* authentication method either AAF or HTTP Basic Auth */

            if (aaf) {
                service.addFilterClass(contextUriPath, CadiFilter.class.getCanonicalName());
            } else if (userName != null && !userName.isEmpty() && password != null && !password.isEmpty()) {
                    service.setBasicAuthentication(userName, password, authUriPath);
            }

            if (filterClasses != null && !filterClasses.isEmpty()) {
                List<String> filterClassesList = Arrays.asList(filterClasses.split(SPACES_COMMA_SPACES));
                for (String filterClass : filterClassesList) {
                    service.addFilterClass(restUriPath, filterClass);
                }
            }

            if (restClasses != null && !restClasses.isEmpty()) {
                List<String> restClassesList = Arrays.asList(restClasses.split(SPACES_COMMA_SPACES));
                for (String restClass : restClassesList) {
                    service.addServletClass(restUriPath, restClass);
                }
            }

            if (restPackages != null && !restPackages.isEmpty()) {
                List<String> restPackageList = Arrays.asList(restPackages.split(SPACES_COMMA_SPACES));
                for (String restPackage : restPackageList) {
                    service.addServletPackage(restUriPath, restPackage);
                }
            }

            serviceList.add(service);
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
        for (HttpServletServer server : httpServletServers) {
            server.shutdown();
        }

        synchronized (this) {
            this.servers.clear();
        }
    }

}
