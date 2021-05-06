/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2018, 2020-2021 AT&T Intellectual Property. All rights reserved.
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
import org.onap.policy.common.im.IntegrityMonitor;
import org.onap.policy.common.im.IntegrityMonitorException;
import org.onap.policy.common.im.StateManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for component MBeans.
 */
public class ComponentAdmin implements ComponentAdminMBean {
    private static final String STATE_MANAGER = "stateManager";

    private static final Logger logger = LoggerFactory.getLogger(ComponentAdmin.class.getName());

    private final String name;
    private MBeanServer registeredMBeanServer;
    private ObjectName registeredObjectName;
    private IntegrityMonitor integrityMonitor = null;
    private StateManagement stateManager = null;

    /**
     * Constructor.
     *
     * @param name the MBean name
     * @param integrityMonitor the integrity monitor
     * @param stateManager the state manager
     * @throws ComponentAdminException if an error occurs
     */
    public ComponentAdmin(String name, IntegrityMonitor integrityMonitor, StateManagement stateManager)
            throws ComponentAdminException {
        if ((name == null) || (integrityMonitor == null) || (stateManager == null)) {
            logger.error("Error: ComponentAdmin constructor called with invalid input");
            throw new ComponentAdminException("null input");
        }

        this.name = "ONAP_POLICY_COMP:name=" + name;
        this.integrityMonitor = integrityMonitor;
        this.stateManager = stateManager;

        try {
            register();
        } catch (ComponentAdminException e) {
            logger.debug("Failed to register ComponentAdmin MBean");
            throw e;
        }
    }

    /**
     * Registers with the MBean server.
     *
     * @throws ComponentAdminException a JMX exception
     */
    public synchronized void register() throws ComponentAdminException {

        try {
            logger.debug("Registering {} MBean", name);

            var mbeanServer = findMBeanServer();

            if (mbeanServer == null) {
                return;
            }

            var objectName = new ObjectName(name);

            if (mbeanServer.isRegistered(objectName)) {
                logger.debug("Unregistering a previously registered {} MBean", name);
                mbeanServer.unregisterMBean(objectName);
            }

            mbeanServer.registerMBean(this, objectName);
            registeredMBeanServer = mbeanServer;
            registeredObjectName = objectName;

        } catch (MalformedObjectNameException | MBeanRegistrationException | InstanceNotFoundException
                | InstanceAlreadyExistsException | NotCompliantMBeanException e) {
            throw new ComponentAdminException(e);
        }
    }

    /**
     * Checks if this MBean is registered with the MBeanServer.
     *
     * @return true if this MBean is registered with the MBeanServer.
     */
    public boolean isRegistered() {
        return registeredObjectName != null;
    }

    /**
     * Unregisters with the MBean server.
     *
     * @throws ComponentAdminException a JMX exception
     */
    public synchronized void unregister() throws ComponentAdminException {

        if (registeredObjectName == null) {
            return;
        }


        try {
            registeredMBeanServer.unregisterMBean(registeredObjectName);

        } catch (MBeanRegistrationException | InstanceNotFoundException e) {
            throw new ComponentAdminException(e);
        }

        registeredMBeanServer = null;
        registeredObjectName = null;
    }

    @Override
    public String toString() {
        return ComponentAdmin.class.getSimpleName() + "[" + name + "]";
    }

    /**
     * Finds the MBeanServer.
     *
     * @return the MBeanServer, or null if it is not found
     */
    public static MBeanServer findMBeanServer() {
        ArrayList<MBeanServer> mbeanServers = MBeanServerFactory.findMBeanServer(null);

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
     *
     * @return the MBeanServer
     */
    public static MBeanServer createMBeanServer() {
        return MBeanServerFactory.createMBeanServer("DefaultDomain");
    }

    /**
     * Get the MBean object name for the specified feature name.
     *
     * @param componentName component name
     * @return the object name
     * @throws MalformedObjectNameException a JMX exception
     */
    public static ObjectName getObjectName(String componentName) throws MalformedObjectNameException {
        return new ObjectName("ONAP_POLICY_COMP:name=" + componentName);
    }

    @Override
    public void test() throws IntegrityMonitorException {
        // Call evaluateSanity on IntegrityMonitor to run the test
        logger.debug("test() called...");
        if (integrityMonitor != null) {
            integrityMonitor.evaluateSanity();
        } else {
            logger.error("Unable to invoke test() - state manager instance is null");
            throw new ComponentAdminException(STATE_MANAGER);
        }

    }

    @Override
    public void lock() throws IntegrityMonitorException {
        logger.debug("lock() called...");
        if (stateManager != null) {
            stateManager.lock();
        } else {
            logger.error("Unable to invoke lock() - state manager instance is null");
            throw new ComponentAdminException(STATE_MANAGER);
        }
    }

    @Override
    public void unlock() throws IntegrityMonitorException {
        logger.debug("unlock() called...");
        if (stateManager != null) {
            stateManager.unlock();
        } else {
            logger.error("Unable to invoke unlock() - state manager instance is null");
            throw new ComponentAdminException(STATE_MANAGER);
        }

    }
}
