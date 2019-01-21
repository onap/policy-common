/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 Samsung Electronics Co., Ltd.
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClient;
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
    
    protected static final String JERSEY_JACKSON_SERIALIZATION_PROVIDER =
                    "com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider";

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

        super();

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

        StringBuilder tmpBaseUrl = new StringBuilder();
        if (this.https) {
            tmpBaseUrl.append("https://");
            ClientBuilder clientBuilder;
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            if (this.selfSignedCerts) {
                sslContext.init(null, new TrustManager[] {new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        // always trusted
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        // always trusted
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                } }, new SecureRandom());
                clientBuilder =
                        ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier((host, session) -> true);
            } else {
                sslContext.init(null, null, null);
                clientBuilder = ClientBuilder.newBuilder().sslContext(sslContext);
            }
            this.client = clientBuilder.build();
        } else {
            tmpBaseUrl.append("http://");
            this.client = ClientBuilder.newClient();
        }

        if (this.userName != null && !this.userName.isEmpty() && this.password != null && !this.password.isEmpty()) {
            HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basic(userName, password);
            this.client.register(authFeature);
        }

        this.client.register(getSerProvClass(busTopicParams.getSerializationProvider()));
        this.client.property(ClientProperties.MOXY_JSON_FEATURE_DISABLE, "true");

        this.baseUrl = tmpBaseUrl.append(this.hostname).append(":").append(this.port).append("/")
                .append((this.basePath == null) ? "" : this.basePath).toString();
    }

    /**
     * Gets the serialization provider class.
     * 
     * @param serializationProvider class name of the serialization provider, or
     *        {@code null} to use the default
     * @return serialization provider class
     * @throws ClassNotFoundException if the serialization provider cannot be found
     */
    private Class<?> getSerProvClass(String serializationProvider) throws ClassNotFoundException {
        String className =
                        (serializationProvider == null ? JERSEY_JACKSON_SERIALIZATION_PROVIDER : serializationProvider);
        return Class.forName(className);
    }

    @Override
    public Response get(String path) {
        if (path != null && !path.isEmpty()) {
            return this.client.target(this.baseUrl).path(path).request().get();
        } else {
            return this.client.target(this.baseUrl).request().get();
        }
    }

    @Override
    public Response get() {
        return this.client.target(this.baseUrl).request().get();
    }

    @Override
    public Response put(String path, Entity<?> entity, Map<String, Object> headers) {
        return getBuilder(path, headers).put(entity);
    }

    @Override
    public Response post(String path, Entity<?> entity, Map<String, Object> headers) {
        return getBuilder(path, headers).post(entity);
    }

    @Override
    public Response delete(String path, Map<String, Object> headers) {
        return getBuilder(path, headers).delete();
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
        Builder builder = this.client.target(this.baseUrl).path(path).request();
        for (Entry<String, Object> header : headers.entrySet()) {
            builder.header(header.getKey(), header.getValue());
        }
        return builder;
    }


}
