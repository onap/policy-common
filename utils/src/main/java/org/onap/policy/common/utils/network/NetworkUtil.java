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

package org.onap.policy.common.utils.network;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network Utilities.
 */
public class NetworkUtil {

    public static final Logger logger = LoggerFactory.getLogger(NetworkUtil.class.getName());

    /**
     * IPv4 Wildcard IP address.
     */
    public static final String IPv4_WILDCARD_ADDRESS = "0.0.0.0";


    /**
     * A trust manager that always trusts certificates.
     */
    // @formatter:off
    private static final TrustManager[] ALWAYS_TRUST_MANAGER = new TrustManager[] {
        new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(final java.security.cert.X509Certificate[] certs,
                            final String authType) {}

            @Override
            public void checkServerTrusted(final java.security.cert.X509Certificate[] certs,
                            final String authType) {}
        }
    };
    // @formatter:on

    private NetworkUtil() {
        // Empty constructor
    }

    /**
     * Allocates an available port on which a server may listen.
     *
     * @return an available port
     * @throws IOException if a socket cannot be created
     */
    public static int allocPort() throws IOException {
        try (ServerSocket socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress(0));

            return socket.getLocalPort();
        }
    }

    /**
     * Allocates an available port on which a server may listen.
     *
     * @param hostName the server's host name
     * @return an available port
     * @throws IOException if a socket cannot be created
     */
    public static int allocPort(String hostName) throws IOException {
        try (ServerSocket socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress(hostName, 0));

            return socket.getLocalPort();
        }
    }

    /**
     * Gets a trust manager that accepts all certificates.
     *
     * @return a trust manager that accepts all certificates
     */
    public static TrustManager[] getAlwaysTrustingManager() {
        return ALWAYS_TRUST_MANAGER;
    }

    /**
     * try to connect to $host:$port $retries times while we are getting connection failures.
     *
     * @param host host
     * @param port port
     * @param retries number of attempts
     * @return true is port is open, false otherwise
     * @throws InterruptedException if execution has been interrupted
     */
    public static boolean isTcpPortOpen(String host, int port, int retries, long interval)
            throws InterruptedException, IOException {
        int retry = 0;
        while (retry < retries) {
            try (Socket s = new Socket(host, port)) {
                logger.debug("{}:{} connected - retries={} interval={}", host, port, retries, interval);
                return true;
            } catch (final ConnectException e) {
                retry++;
                logger.trace("{}:{} connected - retries={} interval={}", host, port, retries, interval, e);
                Thread.sleep(interval);
            }
        }

        logger.warn("{}:{} closed = retries={} interval={}", host, port, retries, interval);
        return false;
    }

    /**
     * Gets host name.
     *
     * @return host name
     */
    public static String getHostname() {

        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }

        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("cannot resolve local hostname", e);
            /* continue */
        }

        return "localhost";
    }

    /**
     * Gets host's IP.
     *
     * @return host IP
     */
    public static String getHostIp() {

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("cannot resolve local hostname", e);
            /* continue */
        }

        return "127.0.0.1";
    }
}
