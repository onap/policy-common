/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.common.im;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.onap.policy.common.im.exceptions.EntityRetrievalException;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.onap.policy.common.utils.jpa.EntityMgrCloser;
import org.onap.policy.common.utils.jpa.EntityTransCloser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * StateManagement class handles all state changes per the Telecom standard X.731. It extends the
 * Observable class and, thus, has an interface to register instances of the
 * StateChangeNotifier/Observer class. When any state change occurs, the registered observers are
 * notified.
 *
 */
public class StateManagement {
    private static final String RESOURCE_NAME = "resource";
    private static final String GET_STATE_MANAGEMENT_ENTITY_QUERY =
            "Select p from StateManagementEntity p where p.resourceName=:" + RESOURCE_NAME;
    private static final String FIND_MESSAGE = "findStateManagementEntity for {}";
    private static final Logger logger = LoggerFactory.getLogger(StateManagement.class);
    public static final String LOCKED = "locked";
    public static final String UNLOCKED = "unlocked";
    public static final String ENABLED = "enabled";
    public static final String DISABLED = "disabled";
    public static final String ENABLE_NOT_FAILED_ACTION = "enableNotFailed";
    public static final String DISABLE_FAILED_ACTION = "disableFailed";
    public static final String FAILED = "failed";
    public static final String DEPENDENCY = "dependency";
    public static final String DEPENDENCY_FAILED = "dependency,failed";
    public static final String DISABLE_DEPENDENCY_ACTION = "disableDependency";
    public static final String ENABLE_NO_DEPENDENCY_ACTION = "enableNoDependency";
    public static final String NULL_VALUE = "null";

    public static final String LOCK_ACTION = "lock";
    public static final String UNLOCK_ACTION = "unlock";
    public static final String PROMOTE_ACTION = "promote";
    public static final String DEMOTE_ACTION = "demote";
    public static final String HOT_STANDBY = "hotstandby";
    public static final String COLD_STANDBY = "coldstandby";
    public static final String PROVIDING_SERVICE = "providingservice";

    public static final String ADMIN_STATE = "adminState";
    public static final String OPERATION_STATE = "opState";
    public static final String AVAILABLE_STATUS = "availStatus";
    public static final String STANDBY_STATUS = "standbyStatus";

    private String resourceName = null;
    private String adminState = null;
    private String opState = null;
    private String availStatus = null;
    private String standbyStatus = null;
    private final EntityManagerFactory emf;
    private StateTransition st = null;

    /*
     * Guarantees single-threadedness of all actions. Only one action can execute at a time. That
     * avoids race conditions between actions being called from different places.
     *
     * Some actions can take significant time to complete and, if another conflicting action is
     * called during its execution, it could put the system in an inconsistent state. This very
     * thing happened when demote was called and the active/standby algorithm, seeing the state
     * attempted to promote the PDP-D.
     *
     */
    private static final Object SYNCLOCK = new Object();
    private static final Object FLUSHLOCK = new Object();

    /**
     * Observers to be notified when this object changes state.
     */
    private final Collection<StateChangeNotifier> observers = new ConcurrentLinkedQueue<>();

    /**
     * StateManagement constructor.
     *
     * @param entityManagerFactory the entity manager factory
     * @param resourceName the resource name
     * @throws StateManagementException if an error occurs
     */
    public StateManagement(final EntityManagerFactory entityManagerFactory, final String resourceName)
            throws StateManagementException {
        emf = entityManagerFactory;
        logger.debug("StateManagement: constructor, resourceName: {}", resourceName);

        this.resourceName = resourceName;

        setState("StateManagement", this.resourceName, sm -> null);

        // Load the StateTransition hash table
        st = new StateTransition();
    }

    /**
     * initializeState() is called when it is necessary to set the StateManagement to a known
     * initial state. It preserves the Administrative State since it must persist across node
     * reboots. Starting from this state, the IntegrityMonitory will determine the Operational State
     * and the owning application will set the StandbyStatus.
     */
    public void initializeState() throws StateManagementException {
        setState("initializeState", this.resourceName, sm -> {
            sm.setAdminState(sm.getAdminState()); // preserve the Admin state
            sm.setOpState(StateManagement.ENABLED);
            sm.setAvailStatus(StateManagement.NULL_VALUE);
            sm.setStandbyStatus(StateManagement.NULL_VALUE);
            return ADMIN_STATE;
        });
    }

    /**
     * Sets the management entity state.
     *
     * @param methodName name of the method that invoked this
     * @param resourceName resource name of the desired entity
     * @param updateState function to update the state; returns a string indicating which item
     *        was updated, {@code null} if no change was made
     * @throws StateManagementException if an error occurs
     */
    private void setState(String methodName, String resourceName, ExFunction<StateManagementEntity, String> updateState)
                    throws StateManagementException {

        synchronized (SYNCLOCK) {
            logger.debug("\nStateManagement: SYNCLOCK {}() operation for resourceName = {}\n", methodName,
                            resourceName);
            logger.debug("StateManagement: {}() operation started, resourceName = {}", methodName, resourceName);

            final var em = emf.createEntityManager();

            try (var emc = new EntityMgrCloser(em); MyTransaction et = new MyTransaction(em)) {

                logger.debug(FIND_MESSAGE, resourceName);

                final var sm = findStateManagementEntity(em, resourceName);
                String changed = updateState.update(sm);

                em.persist(sm);
                et.commit();

                if (changed != null) {
                    notifyObservers(changed);
                }

                logger.debug("StateManagement: {}() operation completed, resourceName = {}",
                                methodName, resourceName);
            } catch (final Exception ex) {
                throw new StateManagementException("StateManagement." + methodName + "() Exception: " + ex);
            }
        }
    }

    /**
     * Adds an observer to list of those to be notified when this changes.
     * @param observer observer to be added
     */
    public void addObserver(StateChangeNotifier observer) {
        observers.add(observer);
    }

    private void notifyObservers(String changed) {
        for (StateChangeNotifier obs : observers) {
            obs.update(this, changed);
        }
    }

    private void setStateUsingTable(String actionName, String resourceName, String changeName)
                    throws StateManagementException {

        setState(actionName, resourceName, sm -> {
            final var stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(),
                            sm.getAvailStatus(), sm.getStandbyStatus(), actionName);

            sm.setAdminState(stateElement.getEndingAdminState());
            sm.setOpState(stateElement.getEndingOpState());
            sm.setAvailStatus(stateElement.getEndingAvailStatus());
            sm.setStandbyStatus(stateElement.getEndingStandbyStatus());

            return changeName;
        });
    }

    /**
     * lock() changes the administrative state to locked.
     *
     * @throws StateManagementException if an error occurs
     */
    public void lock() throws StateManagementException {
        setStateUsingTable(LOCK_ACTION, this.resourceName, ADMIN_STATE);
    }

    /**
     * unlock() changes the administrative state to unlocked.
     *
     * @throws StateManagementException if an error occurs
     */
    public void unlock() throws StateManagementException {
        setStateUsingTable(UNLOCK_ACTION, this.resourceName, ADMIN_STATE);
    }

    /**
     * enableNotFailed() removes the "failed" availability status and changes the operational state
     * to enabled if no dependency is also failed.
     *
     * @throws StateManagementException if an error occurs
     */
    public void enableNotFailed() throws StateManagementException {
        setStateUsingTable(ENABLE_NOT_FAILED_ACTION, this.resourceName, OPERATION_STATE);
    }

    /**
     * disableFailed() changes the operational state to disabled and adds availability status of
     * "failed".
     *
     * @throws StateManagementException if an error occurs
     */
    public void disableFailed() throws StateManagementException {
        setStateUsingTable(DISABLE_FAILED_ACTION, this.resourceName, OPERATION_STATE);
    }

    /**
     * This version of disableFailed is to be used to manipulate the state of a remote resource in
     * the event that remote resource has failed but its state is still showing that it is viable.
     *
     * @throws StateManagementException if an error occurs
     */
    public void disableFailed(final String otherResourceName) throws StateManagementException {
        if (otherResourceName == null) {
            logger.error(
                "\nStateManagement: SYNCLOCK disableFailed(otherResourceName) operation: resourceName is NULL.\n");
            return;
        }

        setStateUsingTable(DISABLE_FAILED_ACTION, otherResourceName, OPERATION_STATE);
    }

    /**
     * disableDependency() changes operational state to disabled and adds availability status of
     * "dependency".
     *
     * @throws StateManagementException if an error occurs
     */
    public void disableDependency() throws StateManagementException {
        setStateUsingTable(DISABLE_DEPENDENCY_ACTION, this.resourceName, OPERATION_STATE);
    }

    /**
     * enableNoDependency() removes the availability status of "dependency " and will change the
     * operational state to enabled if not otherwise failed.
     *
     * @throws StateManagementException if an error occurs
     */
    public void enableNoDependency() throws StateManagementException {
        setStateUsingTable(ENABLE_NO_DEPENDENCY_ACTION, this.resourceName, OPERATION_STATE);
    }

    /**
     * promote() changes the standby status to providingservice if not otherwise failed.
     *
     * @throws IntegrityMonitorException if the status fails to change
     */
    public void promote() throws IntegrityMonitorException {
        AtomicReference<String> newStatus = new AtomicReference<>();

        setState(PROMOTE_ACTION, resourceName, sm -> {
            final var stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(),
                            sm.getAvailStatus(), sm.getStandbyStatus(), PROMOTE_ACTION);

            sm.setAdminState(stateElement.getEndingAdminState());
            sm.setOpState(stateElement.getEndingOpState());
            sm.setAvailStatus(stateElement.getEndingAvailStatus());
            sm.setStandbyStatus(stateElement.getEndingStandbyStatus());

            newStatus.set(sm.getStandbyStatus());

            return STANDBY_STATUS;
        });

        if (StateManagement.COLD_STANDBY.equals(newStatus.get())) {
            final String msg =
                    "Failure to promote " + this.resourceName + " StandbyStatus = " + StateManagement.COLD_STANDBY;
            throw new StandbyStatusException(msg);
        }
    }

    /**
     * demote() changes standbystatus to hotstandby or, if failed, coldstandby.
     *
     * @throws StateManagementException if an error occurs
     */
    public void demote() throws StateManagementException {
        setStateUsingTable(DEMOTE_ACTION, this.resourceName, STANDBY_STATUS);
    }

    /**
     * Only used for a remote resource. It will not notify observers. It is used only in cases where
     * the remote resource has failed is such a way that it cannot update its own states. In
     * particular this is observed by PDP-D DroolsPdpsElectionHandler when it is trying to determine
     * which PDP-D should be designated as the lead.
     *
     * @param otherResourceName the resouce name
     * @throws StateManagementException if an error occurs
     */
    public void demote(final String otherResourceName) throws StateManagementException {
        if (otherResourceName == null) {
            logger.error(
                    "\nStateManagement: SYNCLOCK demote(otherResourceName) operation: resourceName is NULL.\n");
            return;
        }

        setStateUsingTable(DEMOTE_ACTION, otherResourceName, null);
    }

    /**
     * Get the administration state.
     *
     * @return the administration state
     */
    public String getAdminState() {
        getEntityState("getAdminState", this.resourceName,
            sm -> this.adminState = sm.getAdminState(),
            () -> this.adminState = null);
        return this.adminState;
    }

    private void getEntityState(String methodName, String resourceName, Consumer<StateManagementEntity> function,
                    Runnable notFound) {

        logger.debug("StateManagement(6/1/16): {} for resourceName {}", methodName, resourceName);

        final var em = emf.createEntityManager();
        try (final var emc = new EntityMgrCloser(em)) {
            final TypedQuery<StateManagementEntity> query =
                    em.createQuery(GET_STATE_MANAGEMENT_ENTITY_QUERY, StateManagementEntity.class);

            query.setParameter(RESOURCE_NAME, this.resourceName);

            // Just test that we are retrieving the right object
            final List<StateManagementEntity> resourceList =
                    query.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
            if (!resourceList.isEmpty()) {
                // exist
                final var stateManagementEntity = resourceList.get(0);
                // refresh the object from DB in case cached data was returned
                em.refresh(stateManagementEntity);
                function.accept(stateManagementEntity);
            } else {
                notFound.run();
            }
        } catch (final Exception ex) {
            logger.error("StateManagement: {} exception: {}", methodName, ex.getMessage(), ex);
        }

    }

    /**
     * Get the operational state.
     *
     * @return the operational state
     */
    public String getOpState() {
        getEntityState("getOpState", this.resourceName,
            sm -> this.opState = sm.getOpState(),
            () -> this.opState = null);
        return this.opState;
    }

    /**
     * Get the availability status.
     *
     * @return the availability status
     */
    public String getAvailStatus() {
        getEntityState("getAvailStatus", this.resourceName,
            sm -> this.availStatus = sm.getAvailStatus(),
            () -> this.availStatus = null);
        return this.availStatus;
    }

    /**
     * Get the standy status.
     *
     * @return the standby status
     */
    public String getStandbyStatus() {
        getEntityState("getStandbyStatus", this.resourceName,
            sm -> this.standbyStatus = sm.getStandbyStatus(),
            () -> this.standbyStatus = null);
        return this.standbyStatus;
    }

    /**
     * Get the standbystatus of a particular resource.
     *
     * @param otherResourceName the resource
     * @return the standby status
     */
    public String getStandbyStatus(final String otherResourceName) {
        AtomicReference<String> tempStandbyStatus = new AtomicReference<>();

        getEntityState("getStandbyStatus", otherResourceName,
            sm -> tempStandbyStatus.set(sm.getStandbyStatus()),
            () -> logger.error("getStandbyStatus: resourceName ={} not found in statemanagemententity table",
                                    otherResourceName));

        logger.debug("getStandbyStatus: Returning standbyStatus={}", tempStandbyStatus.get());

        return tempStandbyStatus.get();
    }

    /**
     * Find a StateManagementEntity.
     *
     * @param em the entity manager
     * @param otherResourceName the resource name
     * @return the StateManagementEntity
     */
    private static StateManagementEntity findStateManagementEntity(final EntityManager em,
            final String otherResourceName) {
        logger.debug("StateManagementEntity: findStateManagementEntity: Entry");
        try {
            final TypedQuery<StateManagementEntity> query =
                    em.createQuery(GET_STATE_MANAGEMENT_ENTITY_QUERY, StateManagementEntity.class);

            query.setParameter(RESOURCE_NAME, otherResourceName);

            // Just test that we are retrieving the right object
            final List<StateManagementEntity> resourceList =
                    query.setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
            if (!resourceList.isEmpty()) {
                // exist
                final var stateManagementEntity = resourceList.get(0);
                // refresh the object from DB in case cached data was returned
                em.refresh(stateManagementEntity);
                stateManagementEntity.setModifiedDate(MonitorTime.getInstance().getDate());
                return stateManagementEntity;
            } else {
                // not exist - create one
                final var stateManagementEntity = new StateManagementEntity();
                stateManagementEntity.setResourceName(otherResourceName);
                stateManagementEntity.setAdminState(UNLOCKED);
                stateManagementEntity.setOpState(ENABLED);
                stateManagementEntity.setAvailStatus(NULL_VALUE);
                stateManagementEntity.setStandbyStatus(NULL_VALUE); // default
                return stateManagementEntity;
            }
        } catch (final Exception ex) {
            throw new EntityRetrievalException("findStateManagementEntity exception", ex);
        }
    }

    /**
     * Clean up all the StateManagementEntities.
     */
    public void deleteAllStateManagementEntities() {

        logger.debug("StateManagement: deleteAllStateManagementEntities: Entering");

        /*
         * Start transaction
         */
        final var em = emf.createEntityManager();

        try (var emc = new EntityMgrCloser(em); MyTransaction et = new MyTransaction(em)) {
            final TypedQuery<StateManagementEntity> stateManagementEntityListQuery =
                    em.createQuery("SELECT p FROM StateManagementEntity p", StateManagementEntity.class);
            final List<StateManagementEntity> stateManagementEntityList = stateManagementEntityListQuery
                    .setLockMode(LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
            logger.debug("deleteAllStateManagementEntities: Deleting {} StateManagementEntity records",
                            stateManagementEntityList.size());
            for (final StateManagementEntity stateManagementEntity : stateManagementEntityList) {
                logger.debug("deleteAllStateManagementEntities: Deleting statemanagemententity with resourceName={} and"
                                + " standbyStatus={}", stateManagementEntity.getResourceName(),
                                stateManagementEntity.getStandbyStatus());
                em.remove(stateManagementEntity);
            }

            et.commit();
        } catch (final Exception ex) {
            logger.error("StateManagement.deleteAllStateManagementEntities() caught Exception: ", ex);
        }
        logger.debug("deleteAllStateManagementEntities: Exiting");
    }

    @FunctionalInterface
    private static interface ExFunction<T, R> {
        public R update(T object) throws IntegrityMonitorException;
    }

    private static class MyTransaction extends EntityTransCloser {

        /**
         * Create an instance.
         *
         * @param em the entity manager
         */
        public MyTransaction(final EntityManager em) {
            super(em.getTransaction());
        }

        @Override
        public void commit() {
            synchronized (FLUSHLOCK) {
                if (getTransaction().isActive()) {
                    super.commit();
                }
            }
        }

        @Override
        public void rollback() {
            synchronized (FLUSHLOCK) {
                if (getTransaction().isActive()) {
                    super.rollback();
                }
            }
        }

    }

}
