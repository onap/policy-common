/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
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

package org.onap.policy.common.im;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The StateTransition class coordinates all state transitions.
 */
public class StateTransition {
    private static final String UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE = "unlocked,enabled,null,providingservice,";
    private static final String UNLOCKED_ENABLED_NULL_NULL = "unlocked,enabled,null,null,";
    private static final String UNLOCKED_ENABLED_NULL_HOTSTANDBY = "unlocked,enabled,null,hotstandby,";
    private static final String UNLOCKED_DISABLED_FAILED_NULL = "unlocked,disabled,failed,null,";
    private static final String UNLOCKED_DISABLED_DEPENDENCY_NULL = "unlocked,disabled,dependency,null,";
    private static final String LOCKED_ENABLED_NULL_NULL = "locked,enabled,null,null,";
    private static final String LOCKED_ENABLED_NULL_COLDSTANDBY = "locked,enabled,null,coldstandby,";
    private static final String LOCKED_DISABLED_DEPENDENCY_FAILED_COLDSTANDBY_STANDBY_STATUS_EXCEPTION =
                    "locked,disabled,dependency.failed,coldstandby,StandbyStatusException";
    private static final String LOCKED_DISABLED_DEPENDENCY_COLDSTANDBY_STANDBY_STATUS_EXCEPTION =
                    "locked,disabled,dependency,coldstandby,StandbyStatusException";

    private static final Logger logger = LoggerFactory.getLogger(StateTransition.class);

    public static final String ADMIN_STATE = "adminState";
    public static final String OPERATION_STATE = "opState";
    public static final String AVAILABLE_STATUS = "availStatus";
    public static final String STANDBY_STATUS = "standbyStatus";
    public static final String ACTOIN_NAME = "actionName";

    /*
     * Common strings.
     */
    private static final String EXCEPTION_STRING = "Exception:StateTransition unable to process state: adminState=[";
    private static final String OPSTATE_STRING = "], opState=[";
    private static final String AVAILSTATUS_STRING = "], availStatus=[";
    private static final String STANDBY_STRING = "], standbyStatus=[";
    private static final String ACTION_STRING = "], actionName=[";

    /*
     * Valid values for each type.
     */
    private static final Set<String> VALID_ADMIN_STATE = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    StateManagement.LOCKED,
                    StateManagement.UNLOCKED)));

    private static final Set<String> VALID_OP_STATE = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    StateManagement.ENABLED,
                    StateManagement.DISABLED)));

    private static final Set<String> VALID_STANDBY_STATUS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    StateManagement.NULL_VALUE,
                    StateManagement.COLD_STANDBY,
                    StateManagement.HOT_STANDBY,
                    StateManagement.PROVIDING_SERVICE)));

    private static final Set<String> VALID_AVAIL_STATUS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    StateManagement.NULL_VALUE,
                    StateManagement.DEPENDENCY,
                    StateManagement.DEPENDENCY_FAILED,
                    StateManagement.FAILED)));

    private static final Set<String> VALID_ACTION_NAME = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    StateManagement.DEMOTE_ACTION,
                    StateManagement.DISABLE_DEPENDENCY_ACTION,
                    StateManagement.DISABLE_FAILED_ACTION,
                    StateManagement.ENABLE_NO_DEPENDENCY_ACTION,
                    StateManagement.ENABLE_NOT_FAILED_ACTION,
                    StateManagement.LOCK_ACTION,
                    StateManagement.PROMOTE_ACTION,
                    StateManagement.UNLOCK_ACTION)));


    /**
     * State-transition table.
     */
    private static final Map<String, String> STATE_TABLE = Collections.unmodifiableMap(makeStateTable());


    /**
     * Calculates the state transition and returns the end state.
     *
     * @param adminState the administration state
     * @param opState the operational state
     * @param availStatus the availability status
     * @param standbyStatus the standby status
     * @param actionName the action name
     * @return the StateEement
     * @throws StateTransitionException if an error occurs
     */
    public StateElement getEndingState(String adminState, String opState, String availStatus, String standbyStatus,
            String actionName) throws StateTransitionException {
        logger.debug("getEndingState");
        logger.debug("adminState=[{}], opState=[{}], availStatus=[{}], standbyStatus=[{}], actionName[{}]",
                        adminState, opState, availStatus, standbyStatus, actionName);

        if (availStatus == null) {
            availStatus = StateManagement.NULL_VALUE;
        }
        if (standbyStatus == null) {
            standbyStatus = StateManagement.NULL_VALUE;
        }

        if (!VALID_ADMIN_STATE.contains(adminState) || !VALID_OP_STATE.contains(opState)
                        || !VALID_STANDBY_STATUS.contains(standbyStatus)) {
            throw new StateTransitionException(
                            EXCEPTION_STRING + adminState + OPSTATE_STRING + opState + AVAILSTATUS_STRING + availStatus
                                            + STANDBY_STRING + standbyStatus + ACTION_STRING + actionName + "]");
        }

        if (!VALID_AVAIL_STATUS.contains(availStatus) || !VALID_ACTION_NAME.contains(actionName)) {
            throw new StateTransitionException(
                            EXCEPTION_STRING + adminState + OPSTATE_STRING + opState + AVAILSTATUS_STRING + availStatus
                                            + STANDBY_STRING + standbyStatus + ACTION_STRING + actionName + "]");
        }


        StateElement stateElement = new StateElement();

        // dependency,failed is stored as dependency.failed in StateTable
        String availStatus2 = availStatus;
        if (availStatus2 != null) {
            availStatus2 = availStatus.replace(",", ".");
        }
        String key = adminState + "," + opState + "," + availStatus2 + "," + standbyStatus + "," + actionName;
        logger.debug("Ending State search key: {}", key);
        String value = STATE_TABLE.get(key);

        if (value != null) {
            String[] parts = value.split(",", 5);
            stateElement.setEndingAdminState(parts[0].trim());
            stateElement.setEndingOpState(parts[1].trim());
            stateElement.setEndingAvailStatus(parts[2].trim().replace(".", ","));
            stateElement.setEndingStandbyStatus(parts[3].trim());
            stateElement.setException(parts[4].trim());
            stateElement.setAdminState(adminState);
            stateElement.setOpState(opState);
            stateElement.setAvailStatus(availStatus);
            stateElement.setStandbyStatus(standbyStatus);
            stateElement.setActionName(actionName);

            stateElement.displayStateElement();

        } else {
            String msg = "Ending state not found, adminState=[" + adminState + OPSTATE_STRING + opState
                    + AVAILSTATUS_STRING + availStatus + STANDBY_STRING + standbyStatus + ACTION_STRING
                    + actionName + "]";
            logger.error("{}", msg);
            throw new StateTransitionException(msg);
        }

        return stateElement;
    }

    /**
     * Adding State Transition info into HashMap. It includes all state/status and action
     * combinations key : adminState,opState,availStatus,standbyStatus,actionName value:
     * endingAdminState,endingOpState,endingAvailStatus,endingStandbyStatus,exception Note : Use
     * period instead of comma as seperator when store multi-value endingStandbyStatus (convert to
     * comma during retrieval)
     *
     * <p>Note on illegal state/status combinations: This table has many state/status combinations
     * that should never occur. However, they *may* occur due to corruption or manual manipulation
     * of the DB. So, in each case of an illegal combination, the state/status is first corrected
     * before applying the action. It is assumed that the administrative and operational states are
     * always correct. Second, if the availability status is in "agreement" with the operational
     * state, it is assumed correct. If it is null and the operational state is disabled, the
     * availability status is left null until a disabledfailed or disableddependency action is
     * received. Or, if a enableNotFailed or enableNoDependency is received while the availability
     * status is null, it will remain null, but the Operational state will change to enabled.
     *
     * <p>If the standby status is not in agreement with the administrative and/or operational
     * states, it is brought into agreement. For example, if the administrative state is locked and
     * the standby status is providingservice, the standby status is changed to coldstandby.
     *
     * <p>After bringing the states/status attributes into agreement, *then* the action is applied
     * to them. For example, if the administrative state is locked, the operational state is
     * enabled, the availability status is null, the standby status is providingservice and the
     * action is unlock, the standby status is changed to coldstandby and then the unlock action is
     * applied. This will change the final state/status to administrative state = unlocked,
     * operational state = disabled, availability status = null and standby status = hotstandby.
     *
     * <p>Note on standby status: If the starting state of standby status is null and either a
     * promote or demote action is made, the assumption is that standbystatus is supported and
     * therefore, the standby status will be changed to providingservice, hotstandby or coldstandby
     * - depending on the value of the administrative and operational states. If an attempt to
     * promote is made when the administrative state is locked or operational state is disabled, a
     * StandbyStatusException will be thrown since promotion (state transition) is not possible. If
     * the standby status is coldstandby and a transition occurs on the administrative or
     * operational state such that they are unlocked and enabled, the standby status is
     * automatically transitioned to hotstandby since it is only those two states that can hold the
     * statndby status in the coldstandby value.
     *
     * @return a new state-transaction table
     */

    private static Map<String, String> makeStateTable() {
        Map<String,String> stateTable = new HashMap<>();

        stateTable.put("unlocked,enabled,null,null,lock", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,null,null,unlock", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,null,null,disableFailed", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("unlocked,enabled,null,null,enableNotFailed", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,null,null,disableDependency", UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("unlocked,enabled,null,null,enableNoDependency", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,null,null,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,null,null,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,null,coldstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,null,coldstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,null,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,null,coldstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,null,coldstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,null,coldstandby,enableNoDependency", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,null,coldstandby,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,null,coldstandby,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,null,hotstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,null,hotstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,null,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,null,hotstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,null,hotstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,null,hotstandby,enableNoDependency", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,null,hotstandby,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,null,hotstandby,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,null,providingservice,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,null,providingservice,unlock", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,null,providingservice,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,null,providingservice,enableNotFailed",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,null,providingservice,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,null,providingservice,enableNoDependency",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,null,providingservice,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,null,providingservice,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,null,lock", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,failed,null,unlock", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,failed,null,disableFailed", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("unlocked,enabled,failed,null,enableNotFailed", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,failed,null,disableDependency", UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("unlocked,enabled,failed,null,enableNoDependency", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,failed,null,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,failed,null,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,coldstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,failed,coldstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,failed,coldstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,coldstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,failed,coldstandby,enableNoDependency", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,coldstandby,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,failed,coldstandby,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,hotstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,failed,hotstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,failed,hotstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,hotstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,failed,hotstandby,enableNoDependency", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,hotstandby,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,failed,hotstandby,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,failed,providingservice,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,failed,providingservice,unlock", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,failed,providingservice,disableFailed",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,failed,providingservice,enableNotFailed",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,failed,providingservice,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,failed,providingservice,enableNoDependency",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,failed,providingservice,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,failed,providingservice,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,null,lock", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,dependency,null,unlock", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,dependency,null,disableFailed", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("unlocked,enabled,dependency,null,enableNotFailed", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,dependency,null,disableDependency", UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("unlocked,enabled,dependency,null,enableNoDependency", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,dependency,null,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency,null,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,coldstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,dependency,coldstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,coldstandby,disableFailed",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,dependency,coldstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,coldstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,dependency,coldstandby,enableNoDependency",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,coldstandby,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency,coldstandby,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,hotstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,dependency,hotstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,dependency,hotstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,hotstandby,disableDependency",
                "unlocked,disabled,dependency,hotstandby,");
        stateTable.put("unlocked,enabled,dependency,hotstandby,enableNoDependency",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,hotstandby,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency,hotstandby,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency,providingservice,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,dependency,providingservice,unlock",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency,providingservice,disableFailed",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,dependency,providingservice,enableNotFailed",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency,providingservice,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,dependency,providingservice,enableNoDependency",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency,providingservice,promote",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency,providingservice,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,null,lock", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,dependency.failed,null,unlock", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,dependency.failed,null,disableFailed", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("unlocked,enabled,dependency.failed,null,enableNotFailed", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,dependency.failed,null,disableDependency",
                UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("unlocked,enabled,dependency.failed,null,enableNoDependency", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,enabled,dependency.failed,null,promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency.failed,null,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,coldstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,coldstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,coldstandby,disableFailed",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,dependency.failed,coldstandby,enableNotFailed",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,coldstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,dependency.failed,coldstandby,enableNoDependency",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,coldstandby,promote",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency.failed,coldstandby,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,hotstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,hotstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,hotstandby,disableFailed",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,dependency.failed,hotstandby,enableNotFailed",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,hotstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,dependency.failed,hotstandby,enableNoDependency",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,hotstandby,promote",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency.failed,hotstandby,demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,providingservice,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("unlocked,enabled,dependency.failed,providingservice,unlock",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency.failed,providingservice,disableFailed",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,enabled,dependency.failed,providingservice,enableNotFailed",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency.failed,providingservice,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,enabled,dependency.failed,providingservice,enableNoDependency",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency.failed,providingservice,promote",
                UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);
        stateTable.put("unlocked,enabled,dependency.failed,providingservice,demote",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,null,null,lock", "locked,disabled,null,null,");
        stateTable.put("unlocked,disabled,null,null,unlock", "unlocked,disabled,null,null,");
        stateTable.put("unlocked,disabled,null,null,disableFailed", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("unlocked,disabled,null,null,enableNotFailed", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,disabled,null,null,disableDependency", UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("unlocked,disabled,null,null,enableNoDependency", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,disabled,null,null,promote",
                "unlocked,disabled,null,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,null,null,demote", "unlocked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,null,coldstandby,lock", "locked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,null,coldstandby,unlock", "unlocked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,null,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,null,coldstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,null,coldstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,null,coldstandby,enableNoDependency", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,null,coldstandby,promote",
                "unlocked,disabled,null,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,null,coldstandby,demote", "unlocked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,null,hotstandby,lock", "locked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,null,hotstandby,unlock", "unlocked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,null,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,null,hotstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,null,hotstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,null,hotstandby,enableNoDependency", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,null,hotstandby,promote",
                "unlocked,disabled,null,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,null,hotstandby,demote", "unlocked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,null,providingservice,lock", "locked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,null,providingservice,unlock", "unlocked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,null,providingservice,disableFailed",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,null,providingservice,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,null,providingservice,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,null,providingservice,enableNoDependency",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,null,providingservice,promote",
                "unlocked,disabled,null,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,null,providingservice,demote", "unlocked,disabled,null,coldstandby,");
        stateTable.put("unlocked,disabled,failed,null,lock", "locked,disabled,failed,null,");
        stateTable.put("unlocked,disabled,failed,null,unlock", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("unlocked,disabled,failed,null,disableFailed", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("unlocked,disabled,failed,null,enableNotFailed", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,disabled,failed,null,disableDependency", "unlocked,disabled,dependency.failed,null,");
        stateTable.put("unlocked,disabled,failed,null,enableNoDependency", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("unlocked,disabled,failed,null,promote",
                "unlocked,disabled,failed,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,failed,null,demote", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,coldstandby,lock", "locked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,coldstandby,unlock", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,coldstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,failed,coldstandby,disableDependency",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,coldstandby,enableNoDependency",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,coldstandby,promote",
                "unlocked,disabled,failed,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,failed,coldstandby,demote", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,hotstandby,lock", "locked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,hotstandby,unlock", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,hotstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,failed,hotstandby,disableDependency",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,hotstandby,enableNoDependency",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,hotstandby,promote",
                "unlocked,disabled,failed,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,failed,hotstandby,demote", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,providingservice,lock", "locked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,providingservice,unlock", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,providingservice,disableFailed",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,providingservice,enableNotFailed",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,failed,providingservice,disableDependency",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,providingservice,enableNoDependency",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,failed,providingservice,promote",
                "unlocked,disabled,failed,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,failed,providingservice,demote", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,null,lock", "locked,disabled,dependency,null,");
        stateTable.put("unlocked,disabled,dependency,null,unlock", UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("unlocked,disabled,dependency,null,disableFailed", "unlocked,disabled,dependency.failed,null,");
        stateTable.put("unlocked,disabled,dependency,null,enableNotFailed", UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("unlocked,disabled,dependency,null,disableDependency", UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("unlocked,disabled,dependency,null,enableNoDependency", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("unlocked,disabled,dependency,null,promote",
                "unlocked,disabled,dependency,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,dependency,null,demote", "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,coldstandby,lock", "locked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,coldstandby,unlock", "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,coldstandby,disableFailed",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,coldstandby,enableNotFailed",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,coldstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,coldstandby,enableNoDependency",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,dependency,coldstandby,promote",
                "unlocked,disabled,dependency,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,dependency,coldstandby,demote", "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,hotstandby,lock", "locked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,hotstandby,unlock", "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,hotstandby,disableFailed",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,hotstandby,enableNotFailed",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,hotstandby,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,hotstandby,enableNoDependency",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,dependency,hotstandby,promote",
                "unlocked,disabled,dependency,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,dependency,hotstandby,demote", "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,providingservice,lock", "locked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,providingservice,unlock",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,providingservice,disableFailed",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,providingservice,enableNotFailed",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,providingservice,disableDependency",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency,providingservice,enableNoDependency",
                UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("unlocked,disabled,dependency,providingservice,promote",
                "unlocked,disabled,dependency,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,dependency,providingservice,demote",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,null,lock", "locked,disabled,dependency.failed,null,");
        stateTable.put("unlocked,disabled,dependency.failed,null,unlock", "unlocked,disabled,dependency.failed,null,");
        stateTable.put("unlocked,disabled,dependency.failed,null,disableFailed",
                "unlocked,disabled,dependency.failed,null,");
        stateTable.put("unlocked,disabled,dependency.failed,null,enableNotFailed",
                UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("unlocked,disabled,dependency.failed,null,disableDependency",
                "unlocked,disabled,dependency.failed,null,");
        stateTable.put("unlocked,disabled,dependency.failed,null,enableNoDependency", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("unlocked,disabled,dependency.failed,null,promote",
                "unlocked,disabled,dependency.failed,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,dependency.failed,null,demote",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,coldstandby,lock",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,coldstandby,unlock",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,coldstandby,disableFailed",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,coldstandby,enableNotFailed",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,coldstandby,disableDependency",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,coldstandby,enableNoDependency",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,coldstandby,promote",
                "unlocked,disabled,dependency.failed,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,dependency.failed,coldstandby,demote",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,hotstandby,lock",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,hotstandby,unlock",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,hotstandby,disableFailed",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,hotstandby,enableNotFailed",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,hotstandby,disableDependency",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,hotstandby,enableNoDependency",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,hotstandby,promote",
                "unlocked,disabled,dependency.failed,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,dependency.failed,hotstandby,demote",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,providingservice,lock",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,providingservice,unlock",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,providingservice,disableFailed",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,providingservice,enableNotFailed",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,providingservice,disableDependency",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,providingservice,enableNoDependency",
                "unlocked,disabled,failed,coldstandby,");
        stateTable.put("unlocked,disabled,dependency.failed,providingservice,promote",
                "unlocked,disabled,dependency.failed,coldstandby,StandbyStatusException");
        stateTable.put("unlocked,disabled,dependency.failed,providingservice,demote",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,enabled,null,null,lock", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,null,null,unlock", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,null,null,disableFailed", "locked,disabled,failed,null,");
        stateTable.put("locked,enabled,null,null,enableNotFailed", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,null,null,disableDependency", "locked,disabled,dependency,null,");
        stateTable.put("locked,enabled,null,null,enableNoDependency", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,null,null,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,null,null,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,coldstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,coldstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,null,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,null,coldstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,coldstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,null,coldstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,coldstandby,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,null,coldstandby,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,hotstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,hotstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,null,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,null,hotstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,hotstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,null,hotstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,hotstandby,promote",
                "locked,enabled,null,coldstandby,StandbyStateException");
        stateTable.put("locked,enabled,null,hotstandby,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,providingservice,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,providingservice,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,null,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,null,providingservice,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,providingservice,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,null,providingservice,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,null,providingservice,promote",
                "locked,enabled,null,coldstandby,StandbyStateException");
        stateTable.put("locked,enabled,null,providingservice,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,null,lock", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,failed,null,unlock", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,failed,null,disableFailed", "locked,disabled,failed,null,");
        stateTable.put("locked,enabled,failed,null,enableNotFailed", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,failed,null,disableDependency", "locked,disabled,dependency,null,");
        stateTable.put("locked,enabled,failed,null,enableNoDependency", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,failed,null,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,failed,null,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,coldstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,coldstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,failed,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,failed,coldstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,coldstandby,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,failed,coldstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,coldstandby,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,failed,coldstandby,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,hotstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,hotstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,failed,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,failed,hotstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,hotstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,failed,hotstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,hotstandby,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,failed,hotstandby,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,providingservice,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,providingservice,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,failed,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,failed,providingservice,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,providingservice,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,failed,providingservice,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,failed,providingservice,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,failed,providingservice,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,null,lock", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,dependency,null,unlock", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,dependency,null,disableFailed", "locked,disabled,failed,null,");
        stateTable.put("locked,enabled,dependency,null,enableNotFailed", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,dependency,null,disableDependency", "locked,disabled,dependency,null,");
        stateTable.put("locked,enabled,dependency,null,enableNoDependency", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,dependency,null,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,dependency,null,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,coldstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,coldstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,dependency,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,dependency,coldstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,coldstandby,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,dependency,coldstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,coldstandby,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,dependency,coldstandby,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,hotstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,hotstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,dependency,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,dependency,hotstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,hotstandby,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,dependency,hotstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,hotstandby,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,dependency,hotstandby,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,providingservice,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,providingservice,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,dependency,providingservice,disableFailed",
                "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,dependency,providingservice,enableNotFailed",
                LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,providingservice,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,dependency,providingservice,enableNoDependency",
                LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency,providingservice,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,dependency,providingservice,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,null,lock", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,dependency.failed,null,unlock", UNLOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,dependency.failed,null,disableFailed", "locked,disabled,failed,null,");
        stateTable.put("locked,enabled,dependency.failed,null,enableNotFailed", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,dependency.failed,null,disableDependency", "locked,disabled,dependency,null,");
        stateTable.put("locked,enabled,dependency.failed,null,enableNoDependency", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,enabled,dependency.failed,null,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,dependency.failed,null,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,coldstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,coldstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,coldstandby,disableFailed",
                "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,dependency.failed,coldstandby,enableNotFailed",
                LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,coldstandby,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,dependency.failed,coldstandby,enableNoDependency",
                LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,coldstandby,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,dependency.failed,coldstandby,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,hotstandby,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,hotstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,hotstandby,disableFailed",
                "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,dependency.failed,hotstandby,enableNotFailed",
                LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,hotstandby,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,dependency.failed,hotstandby,enableNoDependency",
                LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,hotstandby,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,dependency.failed,hotstandby,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,providingservice,lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,providingservice,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,providingservice,disableFailed",
                "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,enabled,dependency.failed,providingservice,enableNotFailed",
                LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,providingservice,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,enabled,dependency.failed,providingservice,enableNoDependency",
                LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,enabled,dependency.failed,providingservice,promote",
                "locked,enabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,enabled,dependency.failed,providingservice,demote", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,null,null,lock", "locked,disabled,null,null,");
        stateTable.put("locked,disabled,null,null,unlock", "unlocked,disabled,null,null,");
        stateTable.put("locked,disabled,null,null,disableFailed", "locked,disabled,failed,null,");
        stateTable.put("locked,disabled,null,null,enableNotFailed", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,disabled,null,null,disableDependency", "locked,disabled,dependency,null,");
        stateTable.put("locked,disabled,null,null,enableNoDependency", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,disabled,null,null,promote", "locked,disabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,disabled,null,null,demote", "locked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,null,coldstandby,lock", "locked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,null,coldstandby,unlock", "unlocked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,null,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,null,coldstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,null,coldstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,null,coldstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,null,coldstandby,promote",
                "locked,disabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,disabled,null,coldstandby,demote", "locked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,null,hotstandby,lock", "locked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,null,hotstandby,unlock", "unlocked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,null,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,null,hotstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,null,hotstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,null,hotstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,null,hotstandby,promote",
                "locked,disabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,disabled,null,hotstandby,demote", "locked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,null,providingservice,lock", "locked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,null,providingservice,unlock", "unlocked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,null,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,null,providingservice,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,null,providingservice,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,null,providingservice,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,null,providingservice,promote",
                "locked,disabled,null,coldstandby,StandbyStatusException");
        stateTable.put("locked,disabled,null,providingservice,demote", "locked,disabled,null,coldstandby,");
        stateTable.put("locked,disabled,failed,null,lock", "locked,disabled,failed,null,");
        stateTable.put("locked,disabled,failed,null,unlock", UNLOCKED_DISABLED_FAILED_NULL);
        stateTable.put("locked,disabled,failed,null,disableFailed", "locked,disabled,failed,null,");
        stateTable.put("locked,disabled,failed,null,enableNotFailed", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,disabled,failed,null,disableDependency", "locked,disabled,dependency.failed,null,");
        stateTable.put("locked,disabled,failed,null,enableNoDependency", "locked,disabled,failed,null,");
        stateTable.put("locked,disabled,failed,null,promote",
                "locked,disabled,failed,coldstandby,StandbyStatusException");
        stateTable.put("locked,disabled,failed,null,demote", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,coldstandby,lock", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,coldstandby,unlock", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,coldstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,failed,coldstandby,disableDependency",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,failed,coldstandby,enableNoDependency", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,coldstandby,promote",
                "locked,disabled,failed,coldstandby,StandbyStatusException");
        stateTable.put("locked,disabled,failed,coldstandby,demote", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,hotstandby,lock", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,hotstandby,unlock", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,hotstandby,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,failed,hotstandby,disableDependency",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,failed,hotstandby,enableNoDependency", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,hotstandby,promote",
                "locked,disabled,failed,coldstandby,StandbyStatusException");
        stateTable.put("locked,disabled,failed,hotstandby,demote", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,providingservice,lock", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,providingservice,unlock", "unlocked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,providingservice,enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,failed,providingservice,disableDependency",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,failed,providingservice,enableNoDependency",
                "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,failed,providingservice,promote",
                "locked,disabled,failed,coldstandby,StandbyStatusException");
        stateTable.put("locked,disabled,failed,providingservice,demote", "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,dependency,null,lock", "locked,disabled,dependency,null,");
        stateTable.put("locked,disabled,dependency,null,unlock", UNLOCKED_DISABLED_DEPENDENCY_NULL);
        stateTable.put("locked,disabled,dependency,null,disableFailed", "locked,disabled,dependency.failed,null,");
        stateTable.put("locked,disabled,dependency,null,enableNotFailed", "locked,disabled,dependency,null,");
        stateTable.put("locked,disabled,dependency,null,disableDependency", "locked,disabled,dependency,null,");
        stateTable.put("locked,disabled,dependency,null,enableNoDependency", LOCKED_ENABLED_NULL_NULL);
        stateTable.put("locked,disabled,dependency,null,promote",
                LOCKED_DISABLED_DEPENDENCY_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);
        stateTable.put("locked,disabled,dependency,null,demote", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,coldstandby,lock", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,coldstandby,unlock", "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,coldstandby,disableFailed",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency,coldstandby,enableNotFailed",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,coldstandby,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,coldstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,dependency,coldstandby,promote",
                LOCKED_DISABLED_DEPENDENCY_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);
        stateTable.put("locked,disabled,dependency,coldstandby,demote", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,hotstandby,lock", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,hotstandby,unlock", "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,hotstandby,disableFailed",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency,hotstandby,enableNotFailed",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,hotstandby,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,hotstandby,enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,dependency,hotstandby,promote",
                LOCKED_DISABLED_DEPENDENCY_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);
        stateTable.put("locked,disabled,dependency,hotstandby,demote", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,providingservice,lock", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,providingservice,unlock",
                "unlocked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,providingservice,disableFailed",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency,providingservice,enableNotFailed",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,providingservice,disableDependency",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency,providingservice,enableNoDependency",
                LOCKED_ENABLED_NULL_COLDSTANDBY);
        stateTable.put("locked,disabled,dependency,providingservice,promote",
                LOCKED_DISABLED_DEPENDENCY_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);
        stateTable.put("locked,disabled,dependency,providingservice,demote", "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,null,lock", "locked,disabled,dependency.failed,null,");
        stateTable.put("locked,disabled,dependency.failed,null,unlock", "unlocked,disabled,dependency.failed,null,");
        stateTable.put("locked,disabled,dependency.failed,null,disableFailed",
                "locked,disabled,dependency.failed,null,");
        stateTable.put("locked,disabled,dependency.failed,null,enableNotFailed", "locked,disabled,dependency,null,");
        stateTable.put("locked,disabled,dependency.failed,null,disableDependency",
                "locked,disabled,dependency.failed,null,");
        stateTable.put("locked,disabled,dependency.failed,null,enableNoDependency", "locked,disabled,failed,null,");
        stateTable.put("locked,disabled,dependency.failed,null,promote",
                LOCKED_DISABLED_DEPENDENCY_FAILED_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);
        stateTable.put("locked,disabled,dependency.failed,null,demote",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,coldstandby,lock",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,coldstandby,unlock",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,coldstandby,disableFailed",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,coldstandby,enableNotFailed",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,coldstandby,disableDependency",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,coldstandby,enableNoDependency",
                "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,coldstandby,promote",
                LOCKED_DISABLED_DEPENDENCY_FAILED_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);
        stateTable.put("locked,disabled,dependency.failed,coldstandby,demote",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,hotstandby,lock",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,hotstandby,unlock",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,hotstandby,disableFailed",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,hotstandby,enableNotFailed",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,hotstandby,disableDependency",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,hotstandby,enableNoDependency",
                "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,hotstandby,promote",
                LOCKED_DISABLED_DEPENDENCY_FAILED_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);
        stateTable.put("locked,disabled,dependency.failed,hotstandby,demote",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,providingservice,lock",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,providingservice,unlock",
                "unlocked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,providingservice,disableFailed",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,providingservice,enableNotFailed",
                "locked,disabled,dependency,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,providingservice,disableDependency",
                "locked,disabled,dependency.failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,providingservice,enableNoDependency",
                "locked,disabled,failed,coldstandby,");
        stateTable.put("locked,disabled,dependency.failed,providingservice,promote",
                LOCKED_DISABLED_DEPENDENCY_FAILED_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);
        stateTable.put("locked,disabled,dependency.failed,providingservice,demote",
                "locked,disabled,dependency.failed,coldstandby,");

        return stateTable;
    }

    /**
     * Display the state table.
     */
    public void displayStateTable() {
        if (!logger.isDebugEnabled()) {
            return;
        }

        for (Entry<String, String> me : STATE_TABLE.entrySet()) {
            String key = me.getKey() + me.getValue().replace(".", ",");
            logger.debug("{}", key);
        }
    }
}
