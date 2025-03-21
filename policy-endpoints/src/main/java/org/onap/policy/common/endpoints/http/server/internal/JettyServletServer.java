/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020, 2023-2025 Nordix Foundation.
 * Modifications Copyright (C) 2020-2021 Bell Canada. All rights reserved.
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

import io.prometheus.metrics.exporter.servlet.jakarta.PrometheusMetricsServlet;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import jakarta.servlet.Servlet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http Server implementation using Embedded Jetty.
 */
@ToString
public abstract class JettyServletServer implements HttpServletServer, Runnable {

    /**
     * Keystore/Truststore system property names.
     */
    public static final String SYSTEM_KEYSTORE_PROPERTY_NAME = "javax.net.ssl.keyStore";
    public static final String SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME = "javax.net.ssl.keyStorePassword"; // NOSONAR
    public static final String SYSTEM_TRUSTSTORE_PROPERTY_NAME = "javax.net.ssl.trustStore";
    public static final String SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME = "javax.net.ssl.trustStorePassword"; // NOSONAR

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(JettyServletServer.class);

    private static final String NOT_SUPPORTED = " is not supported on this type of jetty server";

    /**
     * Server name.
     */
    @Getter
    protected final String name;

    /**
     * Server host address.
     */
    @Getter
    protected final String host;

    /**
     * Server port to bind.
     */
    @Getter
    protected final int port;

    /**
     * Should SNI host checking be done.
     */
    @Getter
    protected boolean sniHostCheck;

    /**
     * Server auth username.
     */
    @Getter
    protected String user;

    /**
     * Server auth password name.
     */
    @Getter
    protected String password;

    /**
     * Server base context path.
     */
    protected final String contextPath;

    /**
     * Embedded jetty server.
     */
    protected final Server jettyServer;

    /**
     * Servlet context.
     */
    protected final ServletContextHandler context;

    /**
     * Jetty connector.
     */
    protected final ServerConnector connector;

    /**
     * Jetty thread.
     */
    protected Thread jettyThread;

    /**
     * Container for default servlets.
     */
    protected final Map<String, ServletHolder> servlets = new HashMap<>();

    /**
     * Start condition.
     */
    @ToString.Exclude
    protected final Object startCondition = new Object();

    /**
     * Constructor.
     *
     * @param name         server name
     * @param host         server host
     * @param port         server port
     * @param sniHostCheck SNI Host checking flag
     * @param contextPath  context path
     * @throws IllegalArgumentException if invalid parameters are passed in
     */
    protected JettyServletServer(String name, boolean https, String host, int port, boolean sniHostCheck,
                                 String contextPath) {
        String srvName = name;

        if (srvName == null || srvName.isEmpty()) {
            srvName = "http-" + port;
        }

        if (port <= 0 || port >= 65535) {
            throw new IllegalArgumentException("Invalid Port provided: " + port);
        }

        String srvHost = host;
        if (srvHost == null || srvHost.isEmpty()) {
            srvHost = "localhost";
        }

        String ctxtPath = contextPath;
        if (ctxtPath == null || ctxtPath.isEmpty()) {
            ctxtPath = "/";
        }

        this.name = srvName;

        this.host = srvHost;
        this.port = port;
        this.sniHostCheck = sniHostCheck;

        this.contextPath = ctxtPath;

        this.context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        this.context.setContextPath(ctxtPath);

        this.jettyServer = new Server();

        var requestLog = new CustomRequestLog(new Slf4jRequestLogWriter(), CustomRequestLog.EXTENDED_NCSA_FORMAT);
        this.jettyServer.setRequestLog(requestLog);

        if (https) {
            this.connector = httpsConnector();
        } else {
            this.connector = httpConnector();
        }

        this.connector.setName(srvName);
        this.connector.setReuseAddress(true);
        this.connector.setPort(port);
        this.connector.setHost(srvHost);

        this.jettyServer.addConnector(this.connector);
        this.jettyServer.setHandler(context);
    }

    @Override
    public void addFilterClass(String filterPath, String filterClass) {
        if (filterClass == null || filterClass.isEmpty()) {
            throw new IllegalArgumentException("No filter class provided");
        }

        String tempFilterPath = filterPath;
        if (filterPath == null || filterPath.isEmpty()) {
            tempFilterPath = "/*";
        }

        context.addFilter(filterClass, tempFilterPath,
            EnumSet.of(jakarta.servlet.DispatcherType.INCLUDE, jakarta.servlet.DispatcherType.REQUEST));
    }

    protected ServletHolder getServlet(@NonNull Class<? extends Servlet> servlet, @NonNull String servletPath) {
        synchronized (servlets) {
            return servlets.computeIfAbsent(servletPath, key -> context.addServlet(servlet, servletPath));
        }
    }

    protected ServletHolder getServlet(String servletClass, String servletPath) {
        synchronized (servlets) {
            return servlets.computeIfAbsent(servletPath, key -> context.addServlet(servletClass, servletPath));
        }
    }

    /**
     * Returns the https connector.
     *
     * @return the server connector
     */
    public ServerConnector httpsConnector() {
        SslContextFactory.Server sslContextFactoryServer = new SslContextFactory.Server();

        String keyStore = System.getProperty(SYSTEM_KEYSTORE_PROPERTY_NAME);
        if (keyStore != null) {
            sslContextFactoryServer.setKeyStorePath(keyStore);

            String ksPassword = System.getProperty(SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME);
            if (ksPassword != null) {
                sslContextFactoryServer.setKeyStorePassword(ksPassword);
            }
        }

        String trustStore = System.getProperty(SYSTEM_TRUSTSTORE_PROPERTY_NAME);
        if (trustStore != null) {
            sslContextFactoryServer.setTrustStorePath(trustStore);

            String tsPassword = System.getProperty(SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME);
            if (tsPassword != null) {
                sslContextFactoryServer.setTrustStorePassword(tsPassword);
            }
        }


        var httpsConfiguration = new HttpConfiguration();
        SecureRequestCustomizer src = new SecureRequestCustomizer();
        src.setSniHostCheck(sniHostCheck);
        httpsConfiguration.addCustomizer(src);

        return new ServerConnector(jettyServer, sslContextFactoryServer, new HttpConnectionFactory(httpsConfiguration));
    }

    public ServerConnector httpConnector() {
        return new ServerConnector(this.jettyServer);
    }

    @Override
    public void setBasicAuthentication(String user, String password, String servletPath) {
        String srvltPath = servletPath;

        if (StringUtils.isBlank(user) || StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Missing user and/or password");
        }

        if (StringUtils.isBlank(srvltPath)) {
            srvltPath = "/*";
        }

        final var hashLoginService = new HashLoginService();
        final var userStore = new UserStore();
        userStore.addUser(user, Credential.getCredential(password), new String[] {
            "user"
        });
        hashLoginService.setUserStore(userStore);
        hashLoginService.setName(this.connector.getName() + "-login-service");

        var constraint = Constraint.from("user");

        var securityHandler = new SecurityHandler.PathMapped();
        securityHandler.put(srvltPath, constraint);
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.setRealmName(this.connector.getName() + "-realm");
        securityHandler.setLoginService(hashLoginService);

        this.context.setSecurityHandler(securityHandler);

        this.user = user;
        this.password = password;
    }

    /**
     * jetty server execution.
     */
    @Override
    public void run() {
        try {
            logger.info("{}: RUN", this);

            this.jettyServer.start();

            if (logger.isTraceEnabled()) {
                logger.trace("{}: STARTED: {}", this, this.jettyServer.dump());
            }

            synchronized (this.startCondition) {
                this.startCondition.notifyAll();
            }

            this.jettyServer.join();

        } catch (InterruptedException e) {
            logger.error("{}: error found while bringing up server", this, e);
            Thread.currentThread().interrupt();

        } catch (Exception e) {
            logger.error("{}: error found while bringing up server", this, e);
        }
    }

    @Override
    public boolean waitedStart(long maxWaitTime) throws InterruptedException {
        logger.info("{}: WAITED-START", this);

        if (maxWaitTime < 0) {
            throw new IllegalArgumentException("max-wait-time cannot be negative");
        }

        long pendingWaitTime = maxWaitTime;

        if (!this.start()) {
            return false;
        }

        synchronized (this.startCondition) {

            while (!this.jettyServer.isRunning()) {
                try {
                    long startTs = System.currentTimeMillis();

                    this.startCondition.wait(pendingWaitTime);

                    if (maxWaitTime == 0) {
                        /* spurious notification */
                        continue;
                    }

                    long endTs = System.currentTimeMillis();
                    pendingWaitTime = pendingWaitTime - (endTs - startTs);

                    logger.info("{}: pending time is {} ms.", this, pendingWaitTime);

                    if (pendingWaitTime <= 0) {
                        return false;
                    }

                } catch (InterruptedException e) {
                    logger.warn("{}: waited-start has been interrupted", this);
                    throw e;
                }
            }

            return this.jettyServer.isRunning();
        }
    }

    @Override
    public boolean start() {
        logger.info("{}: STARTING", this);

        synchronized (this) {
            if (jettyThread == null || !this.jettyThread.isAlive()) {

                this.jettyThread = new Thread(this);
                this.jettyThread.setName(this.name + "-" + this.port);
                this.jettyThread.start();
            }
        }

        return true;
    }

    @Override
    public boolean stop() {
        logger.info("{}: STOPPING", this);

        synchronized (this) {
            if (jettyThread == null) {
                return true;
            }

            if (!jettyThread.isAlive()) {
                this.jettyThread = null;
            }

            try {
                this.connector.stop();
            } catch (Exception e) {
                logger.error("{}: error while stopping management server", this, e);
            }

            try {
                this.jettyServer.stop();
            } catch (Exception e) {
                logger.error("{}: error while stopping management server", this, e);
                return false;
            }

            Thread.yield();
        }

        return true;
    }

    @Override
    public void shutdown() {
        logger.info("{}: SHUTTING DOWN", this);

        this.stop();

        Thread jettyThreadCopy;
        synchronized (this) {
            if ((jettyThreadCopy = this.jettyThread) == null) {
                return;
            }
        }

        if (jettyThreadCopy.isAlive()) {
            try {
                jettyThreadCopy.join(2000L);
            } catch (InterruptedException e) {
                logger.warn("{}: error while shutting down management server", this);
                Thread.currentThread().interrupt();
            }
            if (!jettyThreadCopy.isInterrupted()) {
                try {
                    jettyThreadCopy.interrupt();
                } catch (Exception e) {
                    // do nothing
                    logger.warn("{}: exception while shutting down (OK)", this, e);
                }
            }
        }

        this.jettyServer.destroy();
    }

    @Override
    public boolean isAlive() {
        if (this.jettyThread != null) {
            return this.jettyThread.isAlive();
        }

        return false;
    }

    @Override
    public void setSerializationProvider(String provider) {
        throw new UnsupportedOperationException("setSerializationProvider()" + NOT_SUPPORTED);
    }

    @Override
    public void addServletClass(String servletPath, String servletClass) {
        throw new UnsupportedOperationException("addServletClass()" + NOT_SUPPORTED);
    }

    @Override
    public void addStdServletClass(@NonNull String servletPath, @NonNull String plainServletClass) {
        this.getServlet(plainServletClass, servletPath);
    }

    @Override
    public void setPrometheus(String metricsPath) {
        this.getServlet(PrometheusMetricsServlet.class, metricsPath);
        JvmMetrics.builder().register();
    }

    @Override
    public boolean isPrometheus() {
        for (ServletHolder servlet : context.getServletHandler().getServlets()) {
            if (PrometheusMetricsServlet.class.getName().equals(servlet.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addServletPackage(String servletPath, String restPackage) {
        throw new UnsupportedOperationException("addServletPackage()" + NOT_SUPPORTED);
    }

    @Override
    public void addServletResource(String servletPath, String resourceBase) {
        throw new UnsupportedOperationException("addServletResource()" + NOT_SUPPORTED);
    }

}
