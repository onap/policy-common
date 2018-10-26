/*-
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

package org.onap.policy.common.endpoints.http.server.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.Slf4jRequestLog;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.onap.aaf.cadi.filter.CadiFilter;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http Server implementation using Embedded Jetty.
 */
public abstract class JettyServletServer implements HttpServletServer, Runnable {

    /**
     * Keystore/Truststore system property names.
     */
    public static final String SYSTEM_KEYSTORE_PROPERTY_NAME = "javax.net.ssl.keyStore";
    public static final String SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME = "javax.net.ssl.keyStorePassword";
    public static final String SYSTEM_TRUSTSTORE_PROPERTY_NAME = "javax.net.ssl.trustStore";
    public static final String SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME = "javax.net.ssl.trustStorePassword";

    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(JettyServletServer.class);

    /**
     * Server name.
     */
    protected final String name;

    /**
     * Server host address.
     */
    protected final String host;

    /**
     * Server port to bind.
     */
    protected final int port;

    /**
     * Server auth user name.
     */
    protected String user;

    /**
     * Server auth password name.
     */
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
    protected volatile Thread jettyThread;

    /**
     * Start condition.
     */
    protected Object startCondition = new Object();

    /**
     * Constructor.
     * 
     * @param name server name
     * @param host server host
     * @param port server port
     * @param contextPath context path
     * 
     * @throws IllegalArgumentException if invalid parameters are passed in
     */
    public JettyServletServer(String name, boolean https, String host, int port, String contextPath) {
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

        this.contextPath = ctxtPath;

        this.context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        this.context.setContextPath(ctxtPath);

        this.jettyServer = new Server();
        this.jettyServer.setRequestLog(new Slf4jRequestLog());

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

    public JettyServletServer(String name, String host, int port, String contextPath) {
        this(name, false, host, port, contextPath);
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

        context.addFilter(filterClass, tempFilterPath, EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
    }

    /**
     * Returns the https connector.
     * 
     * @return the server connector
     */
    public ServerConnector httpsConnector() {
        SslContextFactory sslContextFactory = new SslContextFactory();

        String keyStore = System.getProperty(SYSTEM_KEYSTORE_PROPERTY_NAME);
        if (keyStore != null) {
            sslContextFactory.setKeyStorePath(keyStore);

            String ksPassword = System.getProperty(SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME);
            if (ksPassword != null) {
                sslContextFactory.setKeyStorePassword(ksPassword);
            }
        }

        String trustStore = System.getProperty(SYSTEM_TRUSTSTORE_PROPERTY_NAME);
        if (trustStore != null) {
            sslContextFactory.setTrustStorePath(trustStore);

            String tsPassword = System.getProperty(SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME);
            if (tsPassword != null) {
                sslContextFactory.setTrustStorePassword(tsPassword);
            }
        }

        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        return new ServerConnector(jettyServer, sslContextFactory, new HttpConnectionFactory(https));
    }

    public ServerConnector httpConnector() {
        return new ServerConnector(this.jettyServer);
    }

    @Override
    public void setAafAuthentication(String filterPath) {
        this.addFilterClass(filterPath, CadiFilter.class.getCanonicalName());
    }

    @Override
    public boolean isAaf() {
        for (FilterHolder filter : context.getServletHandler().getFilters()) {
            if (CadiFilter.class.getCanonicalName().equals(filter.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setBasicAuthentication(String user, String password, String servletPath) {
        String srvltPath = servletPath;

        if (user == null || user.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Missing user and/or password");
        }

        if (srvltPath == null || srvltPath.isEmpty()) {
            srvltPath = "/*";
        }

        final HashLoginService hashLoginService = new HashLoginService();
        final UserStore userStore = new UserStore();
        userStore.addUser(user, Credential.getCredential(password), new String[] {"user"});
        hashLoginService.setUserStore(userStore);
        hashLoginService.setName(this.connector.getName() + "-login-service");

        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[] {"user"});
        constraint.setAuthenticate(true);

        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec(srvltPath);

        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.setRealmName(this.connector.getName() + "-realm");
        securityHandler.addConstraintMapping(constraintMapping);
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
            logger.info("{}: STARTING", this);

            this.jettyServer.start();

            if (logger.isInfoEnabled()) {
                logger.info("{}: STARTED: {}", this, this.jettyServer.dump());
            }

            synchronized (this.startCondition) {
                this.startCondition.notifyAll();
            }

            this.jettyServer.join();
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

        if (this.jettyThread == null) {
            return;
        }

        Thread jettyThreadCopy = this.jettyThread;

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
    public int getPort() {
        return this.port;
    }

    /**
     * Get name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get host.
     * 
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Get user.
     * 
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Get password.
     * 
     * @return the password
     */
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("JettyServer [name=").append(name).append(", host=").append(host).append(", port=").append(port)
                .append(", user=").append(user).append(", password=").append(password != null).append(", contextPath=")
                .append(contextPath).append(", jettyServer=").append(jettyServer).append(", context=")
                .append(this.context).append(", connector=").append(connector).append(", jettyThread=")
                .append(jettyThread).append("]");
        return builder.toString();
    }

}
