/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
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

package org.onap.policy.common.endpoints.http.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.http.server.internal.JettyJerseyServer;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.endpoints.utils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public synchronized HttpServletServer build(String name, boolean https, String host, int port, String contextPath,
        boolean swagger, boolean managed) {

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
    public synchronized HttpServletServer build(String name, String host, int port, String contextPath, boolean swagger,
        boolean managed) {
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

        for (String serviceName : serviceNames.split(SPACES_COMMA_SPACES)) {
            addService(serviceList, serviceName, properties);
        }

        return serviceList;
    }

    private void addService(ArrayList<HttpServletServer> serviceList, String serviceName, Properties properties) {

        String servicePrefix = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + serviceName;

        PropertyUtils props = new PropertyUtils(properties, servicePrefix, (name, value) -> logger
                        .warn("{}: {} {} is in invalid format for http service {} ", this, name, value, serviceName));

        int servicePort = props.getInteger(PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, -1);
        if (servicePort < 0) {
            logger.warn("No HTTP port for service in {}", serviceName);
            return;
        }

        final String hostName = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, null);
        final String contextUriPath =
                        props.getString(PolicyEndPointProperties.PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX, null);
        boolean managed = props.getBoolean(PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, true);
        boolean swagger = props.getBoolean(PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, false);
        boolean https = props.getBoolean(PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, false);

        // create the service
        HttpServletServer service = build(serviceName, https, hostName, servicePort, contextUriPath, swagger, managed);

        // configure the service
        setSerializationProvider(props, service);
        setAuthentication(props, service, contextUriPath);

        final String restUriPath = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_REST_URIPATH_SUFFIX, null);

        addFilterClasses(props, service, restUriPath);
        addServletClasses(props, service, restUriPath);
        addServletPackages(props, service, restUriPath);

        serviceList.add(service);
    }

    private void setSerializationProvider(PropertyUtils props, HttpServletServer service) {

        final String classProv = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER, null);

        if (!StringUtils.isBlank(classProv)) {
            service.setSerializationProvider(classProv);
        }
    }

    private void setAuthentication(PropertyUtils props, HttpServletServer service, final String contextUriPath) {
        /* authentication method either AAF or HTTP Basic Auth */

        boolean aaf = props.getBoolean(PolicyEndPointProperties.PROPERTY_AAF_SUFFIX, false);
        final String userName = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX, null);
        final String password = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX, null);
        final String authUriPath = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_AUTH_URIPATH_SUFFIX, null);

        if (aaf) {
            service.setAafAuthentication(contextUriPath);
        } else if (!StringUtils.isBlank(userName) && !StringUtils.isBlank(password)) {
            service.setBasicAuthentication(userName, password, authUriPath);
        }
    }

    private void addFilterClasses(PropertyUtils props, HttpServletServer service, final String restUriPath) {

        final String filterClasses =
                        props.getString(PolicyEndPointProperties.PROPERTY_HTTP_FILTER_CLASSES_SUFFIX, null);

        if (!StringUtils.isBlank(filterClasses)) {
            List<String> filterClassesList = Arrays.asList(filterClasses.split(SPACES_COMMA_SPACES));
            for (String filterClass : filterClassesList) {
                service.addFilterClass(restUriPath, filterClass);
            }
        }
    }

    private void addServletClasses(PropertyUtils props, HttpServletServer service, final String restUriPath) {

        final String restClasses = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX, null);

        if (!StringUtils.isBlank(restClasses)) {
            List<String> restClassesList = Arrays.asList(restClasses.split(SPACES_COMMA_SPACES));
            for (String restClass : restClassesList) {
                service.addServletClass(restUriPath, restClass);
            }
        }
    }

    private void addServletPackages(PropertyUtils props, HttpServletServer service, final String restUriPath) {

        final String restPackages = props.getString(PolicyEndPointProperties.PROPERTY_HTTP_REST_PACKAGES_SUFFIX, null);

        if (!StringUtils.isBlank(restPackages)) {
            List<String> restPackageList = Arrays.asList(restPackages.split(SPACES_COMMA_SPACES));
            for (String restPackage : restPackageList) {
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
