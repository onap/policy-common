/*-
 * ============LICENSE_START=======================================================
 * ONAP
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

package org.onap.policy.common.endpoints.http.client;

import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.onap.policy.common.capabilities.Startable;

/**
 * Http Client interface.
 */
public interface HttpClient extends Startable {

    /**
     * Factory.
     */
    HttpClientFactory factory = new IndexedHttpClientFactory();

    /**
     * GET request.
     *
     * @param path context uri path.
     * @return response
     */
    Response get(String path);

    /**
     * GET request.
     *
     * @return response
     */
    Response get();

    /**
     * PUT request.
     *
     * @param path context uri path
     * @param entity body
     * @param headers headers
     *
     * @return response.
     */
    Response put(String path, Entity<?> entity, Map<String, Object> headers);

    /**
     * POST request.
     *
     * @param path context uri path
     * @param entity body
     * @param headers headers
     *
     * @return response.
     */
    Response post(String path, Entity<?> entity, Map<String, Object> headers);

    /**
     * DELETE request.
     *
     * @param path context uri path
     * @param headers headers
     *
     * @return response.
     */
    Response delete(String path, Map<String, Object> headers);

    /**
     * Retrieve the body from the HTTP transaction.
     *
     * @param response response.
     * @param entityType body type.
     * @param <T> body class.
     *
     * @return response.
     */
    static <T> T getBody(Response response, Class<T> entityType) {
        return response.readEntity(entityType);
    }

    /**
     * Get the client name.
     * @return name
     */
    String getName();

    /**
     * HTTPS support.
     *
     * @return if the client uses https
     */
    boolean isHttps();

    /**
     * Self-signed certificates.
     *
     * @return if the self-signed certificates are allowed
     */
    boolean isSelfSignedCerts();

    /**
     * Get the host name.
     *
     * @return host name
     */
    String getHostname();

    /**
     * Get the port.
     *
     * @return port
     */
    int getPort();

    /**
     * Get the base path.
     *
     * @return base path
     */
    String getBasePath();

    /**
     * Get the user name.
     *
     * @return the user name
     */
    String getUserName();

    /**
     * Get the password.
     *
     * @return the password
     */
    String getPassword();

    /**
     * Get the base URL.
     *
     * @return the base URL
     */
    String getBaseUrl();

}
