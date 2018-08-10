/*
 * ============LICENSE_START=======================================================
 * ONAP
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

import org.onap.policy.common.capabilities.Startable;

/**
 * Http Servlet Server interface.
 */
public interface HttpServletServer extends Startable {


    /**
     * Factory of Http Servlet Servers.
     */
    HttpServletServerFactory factory = new IndexedHttpServletServerFactory();

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
     * @throws IllegalStateException unable to process because of invalid state
     */
    void addServletClass(String servletPath, String restClass);

    /**
     * Adds a package containing JAX-RS classes to serve REST requests.
     * 
     * @param servletPath servlet path
     * @param restPackage JAX-RS package to scan
     * 
     * @throws IllegalArgumentException unable to process because of invalid input
     * @throws IllegalStateException unable to process because of invalid state
     */
    void addServletPackage(String servletPath, String restPackage);

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
