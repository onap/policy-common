/*
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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.internal.JerseyClient;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP client factory implementation indexed by name.
 */
class IndexedHttpClientFactory implements HttpClientFactory {

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(IndexedHttpClientFactory.class);

    protected HashMap<String, HttpClient> clients = new HashMap<>();

    @Override
    public synchronized HttpClient build(BusTopicParams busTopicParams)
            throws KeyManagementException, NoSuchAlgorithmException {
        if (clients.containsKey(busTopicParams.getClientName())) {
            return clients.get(busTopicParams.getClientName());
        }

        JerseyClient client =
                new JerseyClient(busTopicParams);

        if (busTopicParams.isManaged()) {
            clients.put(busTopicParams.getClientName(), client);
        }

        return client;
    }

    @Override
    public synchronized List<HttpClient> build(Properties properties)
            throws KeyManagementException, NoSuchAlgorithmException {
        ArrayList<HttpClient> clientList = new ArrayList<>();

        String clientNames = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES);
        if (clientNames == null || clientNames.isEmpty()) {
            return clientList;
        }

        List<String> clientNameList = new ArrayList<>(Arrays.asList(clientNames.split("\\s*,\\s*")));

        for (String clientName : clientNameList) {
            String httpsString = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + "."
                    + clientName + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX);
            boolean https = false;
            if (StringUtils.isNotBlank(httpsString)) {
                https = Boolean.parseBoolean(httpsString);
            }

            String hostName = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + "."
                    + clientName + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX);

            String servicePortString = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES
                    + "." + clientName + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX);
            int port;
            try {
                if (servicePortString == null || servicePortString.isEmpty()) {
                    continue;
                }
                port = Integer.parseInt(servicePortString);
            } catch (NumberFormatException nfe) {
                logger.error("http-client-factory: cannot parse port {}", servicePortString, nfe);
                continue;
            }

            String baseUrl = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + "."
                    + clientName + PolicyEndPointProperties.PROPERTY_HTTP_URL_SUFFIX);

            String userName = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + "."
                    + clientName + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX);

            String password = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + "."
                    + clientName + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX);

            String managedString = properties.getProperty(PolicyEndPointProperties.PROPERTY_HTTP_CLIENT_SERVICES + "."
                    + clientName + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX);
            boolean managed = true;
            if (managedString != null && !managedString.isEmpty()) {
                managed = Boolean.parseBoolean(managedString);
            }

            try {
                HttpClient client =
                        this.build(BusTopicParams.builder()
                                .clientName(clientName)
                                .useHttps(https)
                                .allowSelfSignedCerts(https)
                                .hostname(hostName)
                                .port(port)
                                .basePath(baseUrl)
                                .userName(userName)
                                .password(password)
                                .managed(managed)
                                .build());
                clientList.add(client);
            } catch (Exception e) {
                logger.error("http-client-factory: cannot build client {}", clientName, e);
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
        for (HttpClient client : clientsInventory) {
            client.shutdown();
        }

        synchronized (this) {
            this.clients.clear();
        }
    }

}
