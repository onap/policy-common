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

package org.onap.policy.common.im.jmx;

import java.util.ArrayList;
import java.util.Iterator;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.onap.policy.common.im.IntegrityMonitor;
import org.onap.policy.common.im.StateManagement;

/**
 * Base class for component MBeans.
 */
public class ComponentAdmin implements ComponentAdminMBean {
	private static final Logger logger = LoggerFactory.getLogger(ComponentAdmin.class.getName());

	private final String name;
	private MBeanServer registeredMBeanServer;
	private ObjectName registeredObjectName;
	private IntegrityMonitor integrityMonitor = null;
	private StateManagement stateManager = null;

	/**
	 * Constructor.
	 * @param name the MBean name
	 * @param integrityMonitor
	 * @param stateManager  
	 * @throws Exception 
	 */
	public ComponentAdmin(String name, IntegrityMonitor integrityMonitor, StateManagement stateManager) throws Exception {
		if ((name == null) || (integrityMonitor == null) || (stateManager == null)) {
			logger.error("Error: ComponentAdmin constructor called with invalid input");
			throw new NullPointerException("null input");
		}

		this.name = "ONAP_POLICY_COMP:name=" + name;
		this.integrityMonitor = integrityMonitor;
		this.stateManager = stateManager;
		
		try {
			register();
		} catch (Exception e) {
			logger.info("Failed to register ComponentAdmin MBean");
			throw e;
		}
	}
	
	/**
	 * Registers with the MBean server.
	 * @throws MalformedObjectNameException a JMX exception
	 * @throws InstanceNotFoundException a JMX exception
	 * @throws MBeanRegistrationException a JMX exception
	 * @throws NotCompliantMBeanException a JMX exception
	 * @throws InstanceAlreadyExistsException a JMX exception
	 */
	public synchronized void register() throws MalformedObjectNameException,
			MBeanRegistrationException, InstanceNotFoundException,
			InstanceAlreadyExistsException, NotCompliantMBeanException {


			logger.info("Registering {} MBean", name);


		MBeanServer mbeanServer = findMBeanServer();

		if (mbeanServer == null) {
			return;
		}

		ObjectName objectName = new ObjectName(name);

		if (mbeanServer.isRegistered(objectName)) {
			logger.info("Unregistering a previously registered {} MBean", name);
			mbeanServer.unregisterMBean(objectName);
		}

		mbeanServer.registerMBean(this, objectName);
		registeredMBeanServer = mbeanServer;
		registeredObjectName = objectName;
	}
	
	/**
	 * Checks if this MBean is registered with the MBeanServer.
	 * @return true if this MBean is registered with the MBeanServer.
	 */
	public boolean isRegistered() {
		return registeredObjectName != null;
	}
	
	/**
	 * Unregisters with the MBean server.
	 * @throws InstanceNotFoundException a JMX exception
	 * @throws MBeanRegistrationException a JMX exception
	 */
	public synchronized void unregister() throws MBeanRegistrationException,
			InstanceNotFoundException {

		if (registeredObjectName == null) {
			return;
		}


		registeredMBeanServer.unregisterMBean(registeredObjectName);
		registeredMBeanServer = null;
		registeredObjectName = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return ComponentAdmin.class.getSimpleName() + "[" + name + "]";
	}
	
	/**
	 * Finds the MBeanServer.
	 * @return the MBeanServer, or null if it is not found
	 */
	public static MBeanServer findMBeanServer() {
		ArrayList<MBeanServer> mbeanServers =
			MBeanServerFactory.findMBeanServer(null);

		Iterator<MBeanServer> iter = mbeanServers.iterator();
		MBeanServer mbeanServer;

		while (iter.hasNext()) {
			mbeanServer = iter.next();
			if ("DefaultDomain".equals(mbeanServer.getDefaultDomain())) {
				return mbeanServer;
			}
		}

		return null;
	}

	/**
	 * Creates the MBeanServer (intended for unit testing only).
	 * @return the MBeanServer
	 */
	public static MBeanServer createMBeanServer() {
		return MBeanServerFactory.createMBeanServer("DefaultDomain");
	}
	
	/**
	 * Get the MBean object name for the specified feature name.
	 * @param componentName component name
	 * @return the object name
	 * @throws MalformedObjectNameException a JMX exception
	 */
	public static ObjectName getObjectName(String componentName)
			throws MalformedObjectNameException {
		return new ObjectName("ONAP_POLICY_COMP:name=" + componentName);
	}

	@Override
	public void test() throws Exception {
		// Call evaluateSanity on IntegrityMonitor to run the test
		logger.info("test() called...");
		if (integrityMonitor != null) {
			integrityMonitor.evaluateSanity();
		}
		else {
			logger.error("Unable to invoke test() - state manager instance is null");
			throw new NullPointerException("stateManager");
		}
		
	}

	@Override
	public void lock() throws Exception {
		logger.info("lock() called...");
		if (stateManager != null) {
			stateManager.lock();
		}
		else {
			logger.error("Unable to invoke lock() - state manager instance is null");
			throw new NullPointerException("stateManager");
		}
	}

	@Override
	public void unlock() throws Exception {
		logger.info("unlock() called...");
		if (stateManager != null) {
			stateManager.unlock();
		}
		else {
			logger.error("Unable to invoke unlock() - state manager instance is null");
			throw new NullPointerException("stateManager");
		}
		
	}
}
