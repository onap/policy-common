/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020, 2023-2025 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jetty Server that uses DefaultServlets to support web static resources' management.
 */
@ToString
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
     * Constructor.
     *
     * @param name name
     * @param https enable https?
     * @param host host server host
     * @param port port server port
     * @param sniHostCheck SNI Host checking flag
     * @param contextPath context path
     * @throws IllegalArgumentException in invalid arguments are provided
     */
    public JettyStaticResourceServer(String name, boolean https, String host, int port, boolean sniHostCheck,
        String contextPath) {

        super(name, https, host, port, sniHostCheck, contextPath);
    }

    /**
     * Retrieves cached default servlet based on servlet path.
     *
     * @param servletPath servlet path
     * @return the jetty servlet holder
     *
     * @throws IllegalArgumentException if invalid arguments are provided
     */
    protected synchronized ServletHolder getDefaultServlet(String servletPath) {
        return super.getServlet(DefaultServlet.class, servletPath);
    }

    @Override
    public synchronized void addServletResource(String servletPath, String resourceBase) {

        if (StringUtils.isBlank(resourceBase)) {
            throw new IllegalArgumentException("No resourceBase provided");
        }

        if (servletPath == null || servletPath.isEmpty()) {
            servletPath = "/*";
        }

        ServletHolder defaultServlet = this.getDefaultServlet(servletPath);

        defaultServlet.setInitParameter(SERVLET_HOLDER_RESOURCE_BASE, resourceBase);
        defaultServlet.setInitParameter(SERVLET_HOLDER_DIR_ALLOWED, "false");
        defaultServlet.setInitParameter(SERVLET_HOLDER_PATH_INFO_ONLY, "true");

        if (logger.isDebugEnabled()) {
            logger.debug("{}: added Default Servlet: {}", this, defaultServlet.dump());
        }
    }
}
