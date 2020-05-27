/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Samsung Electronics Co., Ltd.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.common.endpoints.http.client.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.gson.annotation.GsonJsonIgnore;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http Client implementation using a Jersey Client.
 */
public class JerseyClient implements HttpClient {

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(JerseyClient.class);

    protected static final String JERSEY_DEFAULT_SERIALIZATION_PROVIDER =
                    "org.onap.policy.common.gson.GsonMessageBodyHandler";

    protected final String name;
    protected final boolean https;
    protected final boolean selfSignedCerts;
    protected final String hostname;
    protected final int port;
    protected final String basePath;
    protected final String userName;
    protected final String password;

    protected final Client client;
    protected final String baseUrl;

    protected boolean alive = true;

    /**
     * Constructor.
     *
     * <p>name the name https is it https or not selfSignedCerts are there self signed certs
     * hostname the hostname port port being used basePath base context userName user
     * password password
     *
     * @param busTopicParams Input parameters object
     * @throws KeyManagementException key exception
     * @throws NoSuchAlgorithmException no algorithm exception
     * @throws ClassNotFoundException if the serialization provider cannot be found
     */
    public JerseyClient(BusTopicParams busTopicParams)
                    throws KeyManagementException, NoSuchAlgorithmException, ClassNotFoundException {

        if (busTopicParams.isClientNameInvalid()) {
            throw new IllegalArgumentException("Name must be provided");
        }

        if (busTopicParams.isHostnameInvalid()) {
            throw new IllegalArgumentException("Hostname must be provided");
        }

        if (busTopicParams.isPortInvalid()) {
            throw new IllegalArgumentException("Invalid Port provided: " + busTopicParams.getPort());
        }

        this.name = busTopicParams.getClientName();
        this.https = busTopicParams.isUseHttps();
        this.hostname = busTopicParams.getHostname();
        this.port = busTopicParams.getPort();
        this.basePath = busTopicParams.getBasePath();
        this.userName = busTopicParams.getUserName();
        this.password = busTopicParams.getPassword();
        this.selfSignedCerts = busTopicParams.isAllowSelfSignedCerts();
        this.client = detmClient();

        if (!StringUtils.isBlank(this.userName) && !StringUtils.isBlank(this.password)) {
            HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basic(userName, password);
            this.client.register(authFeature);
        }

        this.client.property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, "true");

        registerSerProviders(busTopicParams.getSerializationProvider());

        this.baseUrl = (this.https ? "https://" : "http://") + this.hostname + ":" + this.port + "/"
                        + (this.basePath == null ? "" : this.basePath);
    }

    private Client detmClient() throws NoSuchAlgorithmException, KeyManagementException {
        if (this.https) {
            ClientBuilder clientBuilder;
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            if (this.selfSignedCerts) {
                sslContext.init(null, NetworkUtil.getAlwaysTrustingManager(), new SecureRandom());

                // This falls under self signed certs which is used for non-production testing environments where
                // the hostname in the cert is unlikely to be crafted properly.  We always return true for the
                // hostname verifier.  This causes a sonar vuln but we ignore it as it could cause problems in some
                // testing environments.
                clientBuilder =
                        ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(
                            (host, session) -> true); //NOSONAR
            } else {
                sslContext.init(null, null, null);
                clientBuilder = ClientBuilder.newBuilder().sslContext(sslContext);
            }
            return clientBuilder.build();

        } else {
            return ClientBuilder.newClient();
        }
    }

    /**
     * Registers the serialization provider(s) with the client.
     *
     * @param serializationProvider comma-separated list of serialization providers
     * @throws ClassNotFoundException if the serialization provider cannot be found
     */
    private void registerSerProviders(String serializationProvider) throws ClassNotFoundException {
        String providers = (StringUtils.isBlank(serializationProvider)
                        ? JERSEY_DEFAULT_SERIALIZATION_PROVIDER : serializationProvider);
        for (String prov : providers.split(",")) {
            this.client.register(Class.forName(prov));
        }
    }

    @Override
    public WebTarget getWebTarget() {
        return this.client.target(this.baseUrl);
    }

    @Override
    public Response get(String path) {
        if (!StringUtils.isBlank(path)) {
            return getWebTarget().path(path).request().get();
        } else {
            return getWebTarget().request().get();
        }
    }

    @Override
    public Response get() {
        return getWebTarget().request().get();
    }

    @Override
    public Future<Response> get(InvocationCallback<Response> callback, String path, Map<String, Object> headers) {
        Map<String, Object> headers2 = (headers != null ? headers : Collections.emptyMap());

        if (!StringUtils.isBlank(path)) {
            return getBuilder(path, headers2).async().get(callback);
        } else {
            return get(callback, headers2);
        }
    }

    @Override
    public Future<Response> get(InvocationCallback<Response> callback, Map<String, Object> headers) {
        Builder builder = getWebTarget().request();
        if (headers != null) {
            headers.forEach(builder::header);
        }
        return builder.async().get(callback);
    }

    @Override
    public Response put(String path, Entity<?> entity, Map<String, Object> headers) {
        return getBuilder(path, headers).put(entity);
    }

    @Override
    public Future<Response> put(InvocationCallback<Response> callback, String path, Entity<?> entity,
                    Map<String, Object> headers) {
        return getBuilder(path, headers).async().put(entity, callback);
    }

    @Override
    public Response post(String path, Entity<?> entity, Map<String, Object> headers) {
        return getBuilder(path, headers).post(entity);
    }

    @Override
    public Future<Response> post(InvocationCallback<Response> callback, String path, Entity<?> entity,
                    Map<String, Object> headers) {
        return getBuilder(path, headers).async().post(entity, callback);
    }

    @Override
    public Response delete(String path, Map<String, Object> headers) {
        return getBuilder(path, headers).delete();
    }

    @Override
    public Future<Response> delete(InvocationCallback<Response> callback, String path, Map<String, Object> headers) {
        return getBuilder(path, headers).async().delete(callback);
    }

    @Override
    public boolean start() {
        return alive;
    }

    @Override
    public boolean stop() {
        return !alive;
    }

    @Override
    public void shutdown() {
        synchronized (this) {
            alive = false;
        }

        try {
            this.client.close();
        } catch (Exception e) {
            logger.warn("{}: cannot close because of {}", this, e.getMessage(), e);
        }
    }

    @Override
    public synchronized boolean isAlive() {
        return this.alive;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isHttps() {
        return https;
    }

    @Override
    public boolean isSelfSignedCerts() {
        return selfSignedCerts;
    }

    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getBasePath() {
        return basePath;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @JsonIgnore
    @GsonJsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JerseyClient [name=");
        builder.append(name);
        builder.append(", https=");
        builder.append(https);
        builder.append(", selfSignedCerts=");
        builder.append(selfSignedCerts);
        builder.append(", hostname=");
        builder.append(hostname);
        builder.append(", port=");
        builder.append(port);
        builder.append(", basePath=");
        builder.append(basePath);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", password=");
        builder.append(password);
        builder.append(", client=");
        builder.append(client);
        builder.append(", baseUrl=");
        builder.append(baseUrl);
        builder.append(", alive=");
        builder.append(alive);
        builder.append("]");
        return builder.toString();
    }

    private Builder getBuilder(String path, Map<String, Object> headers) {
        Builder builder = getWebTarget().path(path).request();
        for (Entry<String, Object> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }
        return builder;
    }


}
