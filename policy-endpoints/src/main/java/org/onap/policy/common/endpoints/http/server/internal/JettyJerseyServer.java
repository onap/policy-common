/*-
 * ============LICENSE_START=======================================================
 * policy-endpoints
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

package org.onap.policy.common.endpoints.http.server.internal;

import io.swagger.jersey.config.JerseyJaxrsConfig;
import java.util.HashMap;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST Jetty Server that uses Jersey Servlets to support JAX-RS Web Services.
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
            "com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider";

    /**
     * Jersey Swagger Classes Init Param Value.
     */
    protected static final String SWAGGER_INIT_CLASSNAMES_PARAM_VALUE =
            "io.swagger.jaxrs.listing.ApiListingResource," + "io.swagger.jaxrs.listing.SwaggerSerializers";
    /**
     * Logger.
     */
    protected static Logger logger = LoggerFactory.getLogger(JettyJerseyServer.class);

    /**
     * Container for servlets.
     */
    protected HashMap<String, ServletHolder> servlets = new HashMap<>();

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
        if (hostname == null || hostname.isEmpty() || hostname.equals(NetworkUtil.IPv4_WILDCARD_ADDRESS)) {
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
    protected synchronized ServletHolder getServlet(String servletPath) {

        ServletHolder jerseyServlet = servlets.get(servletPath);
        if (jerseyServlet == null) {
            jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, servletPath);
            jerseyServlet.setInitOrder(0);
            servlets.put(servletPath, jerseyServlet);
        }

        return jerseyServlet;
    }

    @Override
    public synchronized void addServletPackage(String servletPath, String restPackage) {
        String servPath = servletPath;
        if (restPackage == null || restPackage.isEmpty()) {
            throw new IllegalArgumentException("No discoverable REST package provided");
        }

        if (servPath == null || servPath.isEmpty()) {
            servPath = "/*";
        }

        ServletHolder jerseyServlet = this.getServlet(servPath);
        
        jerseyServlet.setInitParameter(ServerProperties.MOXY_JSON_FEATURE_DISABLE, "true");

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

        if (restClass == null || restClass.isEmpty()) {
            throw new IllegalArgumentException("No discoverable REST class provided");
        }

        if (servletPath == null || servletPath.isEmpty()) {
            servletPath = "/*";
        }

        ServletHolder jerseyServlet = this.getServlet(servletPath);

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
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JettyJerseyServer [servlets=").append(servlets).append(", swaggerId=").append(swaggerId)
                .append(", toString()=").append(super.toString()).append("]");
        return builder.toString();
    }

    @Override
    public void setSerializationProvider(String provider) {
        classProvider = provider;
    }
}
