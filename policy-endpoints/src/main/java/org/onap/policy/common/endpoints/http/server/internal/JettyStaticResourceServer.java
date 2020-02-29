/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation.
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

import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jetty Server that uses DefaultServlets to support web static resources management.
 */
public class JettyStaticResourceServer extends JettyServletServer {

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
    protected static Logger logger = LoggerFactory.getLogger(JettyStaticResourceServer.class);

    /**
     * Container for default servlets.
     */
    protected HashMap<String, ServletHolder> servlets = new HashMap<>();

    /**
     * Constructor.
     *
     * @param name name
     * @param https enable https?
     * @param host host server host
     * @param port port server port
     * @param contextPath context path
     *
     * @throws IllegalArgumentException in invalid arguments are provided
     */
    public JettyStaticResourceServer(String name, boolean https, String host, int port, String contextPath) {

        super(name, https, host, port, contextPath);
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

        return servlets.computeIfAbsent(servPath, key -> context.addServlet(DefaultServlet.class, servPath));
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
            logger.debug("{}: added Default Servlet: {}", this, defaultServlet.dump());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JettyStaticContentServer [Defaultservlets=").append(servlets).append(", toString()=")
                .append(super.toString()).append("]");
        return builder.toString();
    }
}
