/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

/**
 * 
 */
package org.onap.policy.common.im.jmx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;

/**
 * Class to create a JMX RMI connection to the JmxAgent.
 */
public final class JmxAgentConnection {
	
    private static final Logger logger = FlexLogger.getLogger(JmxAgentConnection.class);


	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_PORT = "9996";

	private String host;
	private String port;
	private JMXConnector connector;
	private String jmxUrl = null;
	
	/**
	 * Set up the host/port from the properties.   Use defaults if missing from the properties.
	 * @param properties the properties used to look for host and port
	 */
	public JmxAgentConnection() {
		host = DEFAULT_HOST;
		port = DEFAULT_PORT;
	}
	
	public JmxAgentConnection(String url) {
		jmxUrl = url;
	}

	/**
	 * Generate jmxAgent url.
	 * service:jmx:rmi:///jndi/rmi://host.domain:9999/jmxAgent
	 * 
	 * @param host
	 *            host.domain
	 * @param port
	 *            9999
	 * @return jmxAgent url.
	 */
	private static String jmxAgentUrl(String host, String port) {

		String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port
				+ "/jmxrmi";

		return url;
	}

	/**
	 * Get a connection to the jmxAgent MBeanServer.
	 * @return the connection
	 * @throws Exception on error
	 */
	public MBeanServerConnection getMBeanConnection() throws Exception {
		JMXServiceURL url;
		if (jmxUrl == null) {
			url = new JMXServiceURL(jmxAgentUrl(host, port));
		}
		else {
			url = new JMXServiceURL(jmxUrl);
		}
		Map<String, Object> env = new HashMap<>();
		
		connector = JMXConnectorFactory.newJMXConnector(url, env);
		connector.connect();
		connector.addConnectionNotificationListener(
				new NotificationListener() {

					@Override
					public void handleNotification(
							Notification notification, Object handback) {
						if (notification.getType().equals(
								JMXConnectionNotification.FAILED)) {
							// handle disconnect
							disconnect();
						}
					}
				}, null, null);

		return connector.getMBeanServerConnection();
	}
	
	/**
	 * Disconnect.
	 */
	public void disconnect() {
		if (connector != null) {
			try { connector.close(); } catch (IOException e) { logger.debug(e); }
		}
	}
}
