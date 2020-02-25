/*-
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

package org.onap.policy.common.endpoints.http.server.internal;

import io.swagger.jersey.config.JerseyJaxrsConfig;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Jetty Server that uses Jersey Servlets to support JAX-RS Web Services.
 *
 * <p>Note: the serialization provider will always be added to the server's class providers, as will the swagger
 * providers (assuming swagger has been enabled). This happens whether {@link #addServletClass(String, String)} is used
 * or {@link #addServletPackage(String, String)} is used. Thus it's possible to have both the server's class provider
 * property and the server's package provider property populated.
 */
public class JettyJerseyServer extends JettyServletServer {

    /**
     * Swagger API Base Path.
     */
    protected static final String SWAGGER_API_BASEPATH = "swagger.api.basepath";

    /**
     * Swagger Context ID.
     */
    protected static final String SWAGGER_CONTEXT_ID = "swagger.context.id";

    /**
     * Swagger Scanner ID.
     */
    protected static final String SWAGGER_SCANNER_ID = "swagger.scanner.id";

    /**
     * Swagger Pretty Print.
     */
    protected static final String SWAGGER_PRETTY_PRINT = "swagger.pretty.print";

    /**
     * Jersey Jackson Classes Init Param Value.
     */
    protected static final String JERSEY_JACKSON_INIT_CLASSNAMES_PARAM_VALUE =
            "org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider";

    /**
     * Jersey Swagger Classes Init Param Value.
     */
    protected static final String SWAGGER_INIT_CLASSNAMES_PARAM_VALUE =
            "io.swagger.jaxrs.listing.ApiListingResource," + "io.swagger.jaxrs.listing.SwaggerSerializers";

    /**
     * Servlet Holder Resource Base Path.
     */
    protected static final String SERVLET_HOLDER_RESOURCE_BASE = "resourceBase";

    /**
     * Servlet Holder Directory Allowed.
     */
    protected static final String SERVLET_HOLDER_DIR_ALLOWED = "dirAllowed";

    /**
     * Servlet Holder Path Information Only.
     */
    protected static final String SERVLET_HOLDER_PATH_INFO_ONLY = "pathInfoOnly";

    /**
     * Logger.
     */
    protected static Logger logger = LoggerFactory.getLogger(JettyJerseyServer.class);

    /**
     * Container for jersey servlets.
     */
    protected HashMap<String, ServletHolder> jerseyServlets = new HashMap<>();

    /**
     * Container for default servlets.
     */
    protected HashMap<String, ServletHolder> defaultServlets = new HashMap<>();

    /**
     * Swagger ID.
     */
    protected String swaggerId = null;

    /**
     * The serialization provider to be used when classes are added to the service.
     */
    private String classProvider = JERSEY_JACKSON_INIT_CLASSNAMES_PARAM_VALUE;

    /**
     * Constructor.
     *
     * @param name name
     * @param https enable https?
     * @param host host server host
     * @param port port server port
     * @param swagger support swagger?
     * @param contextPath context path
     *
     * @throws IllegalArgumentException in invalid arguments are provided
     */
    public JettyJerseyServer(String name, boolean https, String host, int port, String contextPath, boolean swagger) {

        super(name, https, host, port, contextPath);
        if (swagger) {
            this.swaggerId = "swagger-" + this.port;
            attachSwaggerServlet(https);
        }
    }

    /**
     * Attaches a swagger initialization servlet.
     */
    protected void attachSwaggerServlet(boolean https) {

        ServletHolder swaggerServlet = context.addServlet(JerseyJaxrsConfig.class, "/");

        String hostname = this.connector.getHost();
        if (StringUtils.isBlank(hostname) || hostname.equals(NetworkUtil.IPV4_WILDCARD_ADDRESS)) {
            hostname = NetworkUtil.getHostname();
        }

        swaggerServlet.setInitParameter(SWAGGER_API_BASEPATH,
                ((https) ? "https://" : "http://") + hostname + ":" + this.connector.getPort() + "/");
        swaggerServlet.setInitParameter(SWAGGER_CONTEXT_ID, swaggerId);
        swaggerServlet.setInitParameter(SWAGGER_SCANNER_ID, swaggerId);
        swaggerServlet.setInitParameter(SWAGGER_PRETTY_PRINT, "true");
        swaggerServlet.setInitOrder(2);

        if (logger.isDebugEnabled()) {
            logger.debug("{}: Swagger Servlet has been attached: {}", this, swaggerServlet.dump());
        }
    }

    /**
     * Retrieves cached server based on servlet path.
     *
     * @param servletPath servlet path
     * @return the jetty servlet holder
     *
     * @throws IllegalArgumentException if invalid arguments are provided
     */
    protected synchronized ServletHolder getJerseyServlet(String servletPath) {

        return jerseyServlets.computeIfAbsent(servletPath, key -> {

            ServletHolder jerseyServlet =
                    context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, servletPath);
            jerseyServlet.setInitOrder(0);

            return jerseyServlet;
        });
    }

    /**
     * Retrieves cached default servlet based on servlet path.
     *
     * @param servletPath servlet path
     * @return the jetty servlet holder
     *
     * @throws IllegalArgumentException if invalid arguments are provided
     */
    protected synchronized ServletHolder getDefaultServlet(String servPath) {

        return defaultServlets.computeIfAbsent(servPath, key -> context.addServlet(DefaultServlet.class, servPath));
    }

    @Override
    public synchronized void addServletPackage(String servletPath, String restPackage) {
        String servPath = servletPath;
        if (StringUtils.isBlank(restPackage)) {
            throw new IllegalArgumentException("No discoverable REST package provided");
        }

        if (servPath == null || servPath.isEmpty()) {
            servPath = "/*";
        }

        ServletHolder jerseyServlet = this.getJerseyServlet(servPath);

        initStandardParams(jerseyServlet);

        String initPackages = jerseyServlet.getInitParameter(ServerProperties.PROVIDER_PACKAGES);
        if (initPackages == null) {
            initPackages = restPackage;

        } else {
            initPackages += "," + restPackage;
        }

        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_PACKAGES, initPackages);

        if (logger.isDebugEnabled()) {
            logger.debug("{}: added REST package: {}", this, jerseyServlet.dump());
        }
    }

    @Override
    public synchronized void addServletClass(String servletPath, String restClass) {

        if (StringUtils.isBlank(restClass)) {
            throw new IllegalArgumentException("No discoverable REST class provided");
        }

        if (servletPath == null || servletPath.isEmpty()) {
            servletPath = "/*";
        }

        ServletHolder jerseyServlet = this.getJerseyServlet(servletPath);

        initStandardParams(jerseyServlet);

        String initClasses = jerseyServlet.getInitParameter(ServerProperties.PROVIDER_CLASSNAMES);
        if (initClasses == null) {
            initClasses = restClass;

        } else {
            initClasses += "," + restClass;
        }

        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, initClasses);

        if (logger.isDebugEnabled()) {
            logger.debug("{}: added REST class: {}", this, jerseyServlet.dump());
        }
    }

    @Override
    public synchronized void addServletResource(String servletPath, String resoureBase) {

        if (StringUtils.isBlank(resoureBase)) {
            throw new IllegalArgumentException("No resourceBase provided");
        }

        if (servletPath == null || servletPath.isEmpty()) {
            servletPath = "/*";
        }

        ServletHolder defaultServlet = this.getDefaultServlet(servletPath);

        defaultServlet.setInitParameter(SERVLET_HOLDER_RESOURCE_BASE, resoureBase);
        defaultServlet.setInitParameter(SERVLET_HOLDER_DIR_ALLOWED, "false");
        defaultServlet.setInitParameter(SERVLET_HOLDER_PATH_INFO_ONLY, "true");

        if (logger.isDebugEnabled()) {
            logger.debug("{}: added REST class: {}", this, defaultServlet.dump());
        }
    }

    /**
     * Adds "standard" parameters to the initParameter set. Sets swagger parameters, if specified, and sets the class
     * provider property. This can be invoked multiple times, but only the first actually causes any changes to the
     * parameter set.
     *
     * @param jerseyServlet servlet into which parameters should be added
     */
    private void initStandardParams(ServletHolder jerseyServlet) {
        String initClasses = jerseyServlet.getInitParameter(ServerProperties.PROVIDER_CLASSNAMES);
        if (initClasses != null) {
            return;
        }

        initClasses = classProvider;

        if (this.swaggerId != null) {
            initClasses += "," + SWAGGER_INIT_CLASSNAMES_PARAM_VALUE;

            jerseyServlet.setInitParameter(SWAGGER_CONTEXT_ID, swaggerId);
            jerseyServlet.setInitParameter(SWAGGER_SCANNER_ID, swaggerId);
        }

        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, initClasses);

        jerseyServlet.setInitParameter(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, "true");
    }

    /**
     * Note: this must be invoked <i>before</i> {@link #addServletClass(String, String)} or
     * {@link #addServletPackage(String, String)}.
     */
    @Override
    public void setSerializationProvider(String provider) {
        classProvider = provider;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JettyJerseyServer [Jerseyservlets=").append(jerseyServlets).append(", Defaultservlets=")
                .append(defaultServlets).append(", swaggerId=").append(swaggerId).append(", toString()=")
                .append(super.toString()).append("]");
        return builder.toString();
    }
}
