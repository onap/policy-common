/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation.
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

import com.google.re2j.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.http.server.internal.JettyJerseyServer;
import org.onap.policy.common.endpoints.http.server.internal.JettyStaticResourceServer;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.endpoints.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indexed factory implementation.
 */
class IndexedHttpServletServerFactory implements HttpServletServerFactory {
    private static final Pattern COMMA_SPACE_PAT = Pattern.compile("\\s*,\\s*");

    /**
     * logger.
     */
    protected static Logger logger = LoggerFactory.getLogger(IndexedHttpServletServerFactory.class);

    /**
     * servers index.
     */
    protected HashMap<Integer, HttpServletServer> servers = new HashMap<>();

    @Override
    public synchronized HttpServletServer build(String name, boolean https, String host, int port, String contextPath,
        boolean swagger, boolean managed) {

        if (servers.containsKey(port)) {
            return servers.get(port);
        }

        var server = new JettyJerseyServer(name, https, host, port, contextPath, swagger);
        if (managed) {
            servers.put(port, server);
        }

        return server;
    }

    @Override
    public synchronized HttpServletServer build(String name, String host, int port, String contextPath, boolean swagger,
        boolean managed) {
        return build(name, false, host, port, contextPath, swagger, managed);
    }

    @Override
    public synchronized List<HttpServletServer> build(Properties properties) {

        ArrayList<HttpServletServer> serviceList = new ArrayList<>();

        String serviceNames = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES);
        if (StringUtils.isBlank(serviceNames)) {
            logger.warn("No topic for HTTP Service: {}", properties);
            return serviceList;
        }

        for (String serviceName : COMMA_SPACE_PAT.split(serviceNames)) {
            addService(serviceList, serviceName, properties);
        }

        return serviceList;
    }



    @Override
    public HttpServletServer buildStaticResourceServer(String name, boolean https, String host, int port,
            String contextPath, boolean managed) {
        if (servers.containsKey(port)) {
            return servers.get(port);
        }

        var server = new JettyStaticResourceServer(name, https, host, port, contextPath);
        if (managed) {
            servers.put(port, server);
        }

        return server;
    }

    private void addService(ArrayList<HttpServletServer> serviceList, String serviceName, Properties properties) {

        String servicePrefix = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + serviceName;

        var props = new PropertyUtils(properties, servicePrefix,
            (name, value, ex) -> logger
                        .warn("{}: {} {} is in invalid format for http service {} ", this, name, value, serviceName));

        var servicePort = props.getInteger(PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, -1);
        if (servicePort < 0) {
            logger.warn("No HTTP port for service in {}", serviceName);
            return;
        }

        final var hostName = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, null);
        final var contextUriPath = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX, null);
        var managed = props.getBoolean(PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, true);
        var swagger = props.getBoolean(PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, false);
        var https = props.getBoolean(PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, false);

        // create the service
        HttpServletServer service = build(serviceName, https, hostName, servicePort, contextUriPath, swagger, managed);

        // configure the service
        setSerializationProvider(props, service);
        setAuthentication(props, service, contextUriPath);

        final var restUriPath = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_REST_URIPATH_SUFFIX, null);

        addFilterClasses(props, service, restUriPath);
        addRestServletClasses(props, service, restUriPath);
        addServletPackages(props, service, restUriPath);

        addServletClass(props, service);

        serviceList.add(service);
    }

    private void setSerializationProvider(PropertyUtils props, HttpServletServer service) {

        final var classProv = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER, null);

        if (!StringUtils.isBlank(classProv)) {
            service.setSerializationProvider(classProv);
        }
    }

    private void setAuthentication(PropertyUtils props, HttpServletServer service, final String contextUriPath) {
        /* authentication method either AAF or HTTP Basic Auth */

        final var aaf = props.getBoolean(PolicyEndPointProperties.PROPERTY_AAF_SUFFIX, false);
        final var userName = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX, null);
        final var password = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX, null);
        final var authUriPath = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_AUTH_URIPATH_SUFFIX, null);

        if (aaf) {
            service.setAafAuthentication(contextUriPath);
        } else if (!StringUtils.isBlank(userName) && !StringUtils.isBlank(password)) {
            service.setBasicAuthentication(userName, password, authUriPath);
        }
    }

    private void addFilterClasses(PropertyUtils props, HttpServletServer service, final String restUriPath) {

        final var filterClasses =
                        props.getString(PolicyEndPointProperties.PROPERTY_HTTP_FILTER_CLASSES_SUFFIX, null);

        if (!StringUtils.isBlank(filterClasses)) {
            for (String filterClass : COMMA_SPACE_PAT.split(filterClasses)) {
                service.addFilterClass(restUriPath, filterClass);
            }
        }
    }

    private void addRestServletClasses(PropertyUtils props, HttpServletServer service, final String restUriPath) {
        final var restClasses = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX, null);

        if (!StringUtils.isBlank(restClasses)) {
            for (String restClass : COMMA_SPACE_PAT.split(restClasses)) {
                service.addServletClass(restUriPath, restClass);
            }
        }
    }

    private void addServletClass(PropertyUtils props, HttpServletServer service) {
        var servletClass = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_SERVLET_CLASS_SUFFIX, null);
        var servletUriPath = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_SERVLET_URIPATH_SUFFIX, null);

        if (!StringUtils.isBlank(servletClass) && !StringUtils.isBlank(servletUriPath)) {
            service.addStdServletClass(servletUriPath, servletClass);
        }
    }

    private void addServletPackages(PropertyUtils props, HttpServletServer service, final String restUriPath) {

        final var restPackages = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_REST_PACKAGES_SUFFIX, null);

        if (!StringUtils.isBlank(restPackages)) {
            for (String restPackage : COMMA_SPACE_PAT.split(restPackages)) {
                service.addServletPackage(restUriPath, restPackage);
            }
        }
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
