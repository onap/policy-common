/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation.
 * Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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

import java.util.Map;
import javax.servlet.http.HttpServlet;
import org.onap.policy.common.capabilities.Startable;

/**
 * Http Servlet Server interface.
 */
public interface HttpServletServer extends Startable {
    /**
     * Gets the server name.
     *
     * @return the server name
     */
    String getName();

    /**
     * Get the port.
     *
     * @return port
     */
    int getPort();

    /**
     * Enables basic authentication with user and password on the the relative path relativeUriPath.
     *
     * @param user user
     * @param password password
     * @param relativeUriPath relative path
     */
    void setBasicAuthentication(String user, String password, String relativeUriPath);

    /**
     * Enables AAF based authentication.
     *
     * @param filterPath filter path
     */
    void setAafAuthentication(String filterPath);

    /**
     * Checks if AAF authentication has been enabled.
     */
    boolean isAaf();

    /**
     * Sets the serialization provider to be used when classes are added to the service.
     *
     * @param provider the provider to use for message serialization and de-serialization
     */
    void setSerializationProvider(String provider);

    /**
     * Adds a filter at the specified path.
     *
     * @param filterPath filter path
     * @param filterClass filter class
     */
    void addFilterClass(String filterPath, String filterClass);

    /**
     * Adds a JAX-RS servlet class to serve REST requests.
     *
     * @param servletPath servlet path
     * @param restClass JAX-RS API Class
     *
     * @throws IllegalArgumentException unable to process because of invalid input
     * @throws IllegalStateException unable to process because of invalid state, for example
     *         different types of servlets map to the same servletPath
     */
    void addServletClass(String servletPath, String restClass);

    /**
     * Adds a package containing JAX-RS classes to serve REST requests.
     *
     * @param servletPath servlet path
     * @param restPackage JAX-RS package to scan
     *
     * @throws IllegalArgumentException unable to process because of invalid input
     * @throws IllegalStateException unable to process because of invalid state, for example
     *         different types of servlets map to the same servletPath
     */
    void addServletPackage(String servletPath, String restPackage);

    /**
     * Add a static resource path to manage static resources.
     *
     * @param servletPath servlet path
     * @param resourceBase static resources folder
     *
     * @throws IllegalArgumentException unable to process because of invalid input
     * @throws IllegalStateException unable to process because of invalid state, for example
     *         different types of servlets map to the same servletPath
     */
    void addServletResource(String servletPath, String resourceBase);

    /**
     * Adds servlets with its corresponding servletPath
     *
     * @param servlets a map with servlet path as the key and the servlet class as value
     *
     */
    void addServlets(Map<String, Class<? extends HttpServlet>> servlets);

    /**
     * Blocking start of the http server.
     *
     * @param maxWaitTime max time to wait for the start to take place
     * @return true if start was successful
     *
     * @throws IllegalArgumentException if arguments are invalid
     * @throws InterruptedException if the blocking operation is interrupted
     */
    boolean waitedStart(long maxWaitTime) throws InterruptedException;

}
