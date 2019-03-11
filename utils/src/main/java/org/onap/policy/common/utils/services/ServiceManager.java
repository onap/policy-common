/*
 * ============LICENSE_START=======================================================
 * ONAP PAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.services;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import org.onap.policy.common.capabilities.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a series of services. The services are started in order, and stopped in reverse
 * order.
 */
public class ServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    /**
     * Services to be started/stopped.
     */
    private final Deque<Service> items = new LinkedList<>();

    /**
     * Adds a service to the manager.
     *
     * @param stepName name to be logged when the service is started/stopped
     * @param starter function to start the service
     * @param stopper function to stop the service
     * @return this manager
     */
    public ServiceManager addAction(String stepName, RunnableWithEx starter, RunnableWithEx stopper) {
        items.add(new Service(stepName, starter, stopper));
        return this;
    }

    /**
     * Adds a service to the manager.  The manager will invoke the service's
     * {@link Startable#start()} and {@link Startable#stop()} methods.
     *
     * @param stepName name to be logged when the service is started/stopped
     * @param service object to be started/stopped
     * @return this manager
     */
    public ServiceManager addService(String stepName, Startable service) {
        items.add(new Service(stepName, () -> service.start(), () -> service.stop()));
        return this;
    }

    /**
     * Starts each service, in order. If a service throws an exception, then the
     * previously started services are stopped, in reverse order.
     *
     * @throws ServiceManagerException if a service fails to start
     */
    public void start() throws ServiceManagerException {
        Deque<Service> completed = new LinkedList<>();
        Exception ex = null;

        for (Service item : items) {
            try {
                logger.info("starting {}", item.stepName);
                item.starter.run();
                completed.add(item);

            } catch (Exception e) {
                logger.error("failed to start {}; rewinding steps", item.stepName);
                ex = e;
                break;
            }
        }

        if (ex == null) {
            return;
        }

        // one of the services failed to start - rewind those we've previously started
        try {
            rewind(completed);

        } catch (ServiceManagerException e) {
            logger.error("rewind failed", e);
        }

        throw new ServiceManagerException(ex);
    }

    /**
     * Stops the services, in reverse order from which they were started. Stops all of the
     * services, even if one of the "stop" functions throws an exception.
     *
     * @throws ServiceManagerException if a service fails to stop
     */
    public void stop() throws ServiceManagerException {
        rewind(items);
    }

    /**
     * Rewinds a list of services, stopping them in reverse order. Stops all of the
     * services, even if one of the "stop" functions throws an exception.
     *
     * @param running services that are running, in the order they were started
     * @throws ServiceManagerException if a service fails to stop
     */
    private void rewind(Deque<Service> running) throws ServiceManagerException {
        Exception ex = null;

        // stop everything, in reverse order
        Iterator<Service> it = running.descendingIterator();
        while (it.hasNext()) {
            Service item = it.next();
            try {
                logger.info("stopping {}", item.stepName);
                item.stopper.run();
            } catch (Exception e) {
                logger.error("failed to stop {}", item.stepName);
                ex = e;

                // do NOT break or re-throw, as we must stop ALL remaining items
            }
        }

        if (ex != null) {
            throw new ServiceManagerException(ex);
        }
    }

    /**
     * Service information.
     */
    private static class Service {
        private String stepName;
        private RunnableWithEx starter;
        private RunnableWithEx stopper;

        public Service(String stepName, RunnableWithEx starter, RunnableWithEx stopper) {
            this.stepName = stepName;
            this.starter = starter;
            this.stopper = stopper;
        }
    }

    @FunctionalInterface
    public static interface RunnableWithEx {
        public void run() throws Exception;
    }
}
