/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-1018 AT&T Intellectual Property. All rights reserved.
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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.onap.policy.common.endpoints.http.client.internal.JerseyClient;
import org.onap.policy.common.endpoints.properties.HttpClientPropertiesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http Client Factory
 */
public interface HttpClientFactory {

    /**
     * build and http client with the following parameters
     */
    HttpClient build(String name, boolean https,
        boolean selfSignedCerts,
        String hostname, int port,
        String baseUrl, String userName,
        String password, boolean managed)
        throws KeyManagementException, NoSuchAlgorithmException;

    /**
     * build http client from properties
     */
    List<HttpClient> build(Properties properties)
        throws KeyManagementException, NoSuchAlgorithmException;

    /**
     * get http client
     * @param name the name
     * @return the http client
     */
    HttpClient get(String name);

    /**
     * list of http clients
     * @return http clients
     */
    List<HttpClient> inventory();

    /**
     * destroy by name
     * @param name name
     */
    void destroy(String name);

    void destroy();
}

/**
 * http client factory implementation indexed by name
 */
class IndexedHttpClientFactory implements HttpClientFactory {

    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedHttpClientFactory.class);

    private HashMap<String, HttpClient> clients = new HashMap<>();

    @Override
    public synchronized HttpClient build(String name, boolean https, boolean selfSignedCerts,
        String hostname, int port,
        String baseUrl, String userName, String password,
        boolean managed)
        throws KeyManagementException, NoSuchAlgorithmException {
        if (clients.containsKey(name))
            return clients.get(name);

        JerseyClient client =
            new JerseyClient(name, https, selfSignedCerts, hostname, port, baseUrl, userName, password);

        if (managed)
            clients.put(name, client);

        return client;
    }

    @Override
    public synchronized List<HttpClient> build(Properties properties) {
        ArrayList<HttpClient> clientList = new ArrayList<>();

        HttpClientPropertiesHelper clientProperties = new HttpClientPropertiesHelper(properties);
        for (String clientName : clientProperties.getEndpointNames()) {
            try {
                int port = clientProperties.getPort(clientName);
                String hostName = clientProperties.getHost(clientName);
                String baseUrl = clientProperties.getContextUriPath(clientName);
                String userName = clientProperties.getUserName(clientName);
                String password = clientProperties.getPassword(clientName);
                boolean managed = clientProperties.isManaged(clientName, true);
                boolean https = clientProperties.isHttps(clientName, false);

                HttpClient client =
                    this.build(clientName, https, https, hostName, port, baseUrl,
                        userName, password, managed);
                clientList.add(client);
            } catch (NumberFormatException nfe) {
                logger.error("No HTTP port found for HTTP Client Endpoint {}", clientName, nfe);
            } catch (Exception e) {
                logger.error("Cannot build HTTP Client Endpoint {}", clientName, e);
            }
        }

        return clientList;
    }

    @Override
    public synchronized HttpClient get(String name) {
        if (clients.containsKey(name)) {
            return clients.get(name);
        }

        throw new IllegalArgumentException("Http Client " + name + " not found");
    }

    @Override
    public synchronized List<HttpClient> inventory() {
        return new ArrayList<>(this.clients.values());
    }

    @Override
    public synchronized void destroy(String name) {
        if (!clients.containsKey(name)) {
            return;
        }

        HttpClient client = clients.remove(name);
        try {
            client.shutdown();
        } catch (IllegalStateException e) {
            logger.error("http-client-factory: cannot shutdown client {}", client, e);
        }
    }

    @Override
    public void destroy() {
        List<HttpClient> clientsInventory = this.inventory();
        for (HttpClient client: clientsInventory) {
            client.shutdown();
        }

        synchronized(this) {
            this.clients.clear();
        }
    }

}
