/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The StateTransition class coordinates all state transitions.
 */
public class StateTransition {
    private static final Pattern COMMA_PAT = Pattern.compile(",");

    private static final String DEPENDENCY_FAILED = "dependency.failed";

    private static final String ANY_DISABLED_ANY_COLDSTANDBY = "${1},disabled,${3},coldstandby,";
    private static final String ANY_DISABLED_ANY_COLDSTANDBY_STANDBY_STATUS_EXCEPTION =
                    "${1},disabled,${3},coldstandby,StandbyStatusException";
    private static final String LOCKED_ENABLED_NULL_COLDSTANDBY_STANDBY_STATUS_EXCEPTION =
                    "locked,enabled,null,coldstandby,StandbyStatusException";
    private static final String UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE = "unlocked,enabled,null,providingservice,";
    private static final String UNLOCKED_DISABLED_DEPENDENCY_HOTSTANDBY = "unlocked,disabled,dependency,hotstandby,";
    private static final String ANY_DISABLED_DEPENDENCY_NULL = "${1},disabled,dependency,null,";
    private static final String ANY_DISABLED_DEPENDENCY_COLDSTANDBY = "${1},disabled,dependency,coldstandby,";
    private static final String ANY_DISABLED_DEPENDENCY_FAILED_NULL = "${1},disabled,dependency.failed,null,";
    private static final String ANY_DISABLED_DEPENDENCY_FAILED_COLDSTANDBY =
                    "${1},disabled,dependency.failed,coldstandby,";
    private static final String ANY_DISABLED_FAILED_NULL = "${1},disabled,failed,null,";
    private static final String ANY_DISABLED_FAILED_COLDSTANDBY = "${1},disabled,failed,coldstandby,";
    private static final String UNLOCKED_DISABLED_ANY_NULL = "unlocked,disabled,${3},null,";
    private static final String UNLOCKED_DISABLED_ANY_COLDSTANDBY = "unlocked,disabled,${3},coldstandby,";
    private static final String UNLOCKED_ENABLED_NULL_NULL = "unlocked,enabled,null,null,";
    private static final String LOCKED_DISABLED_ANY_NULL = "locked,disabled,${3},null,";
    private static final String LOCKED_DISABLED_ANY_COLDSTANDBY = "locked,disabled,${3},coldstandby,";
    private static final String UNLOCKED_ENABLED_NULL_HOTSTANDBY = "unlocked,enabled,null,hotstandby,";
    private static final String UNLOCKED_ENABLED_NULL_ANY = "unlocked,enabled,null,${4},";
    private static final String LOCKED_ENABLED_NULL_NULL = "locked,enabled,null,null,";
    private static final String LOCKED_ENABLED_NULL_COLDSTANDBY = "locked,enabled,null,coldstandby,";
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
     * This is only used while populating {@link #STATE_TABLE}.
     */
    private static final List<Pair<String, String[]>> TRANSITION_ITEMS = new ArrayList<>(10);

    /**
     * State-transition table.
     */
    private static final Map<String, String> STATE_TABLE = new HashMap<>();

    static {
        populateStateTable();
    }


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


        var stateElement = new StateElement();

        // dependency,failed is stored as dependency.failed in StateTable
        String availStatus2 = availStatus;
        if (availStatus2 != null) {
            availStatus2 = availStatus.replace(",", ".");
        }
        String key = adminState + "," + opState + "," + availStatus2 + "," + standbyStatus + "," + actionName;
        logger.debug("Ending State search key: {}", key);
        String value = STATE_TABLE.get(key);

        if (value != null) {
            String[] parts = COMMA_PAT.split(value, 5);
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
     * standby status in the coldstandby value.
     */

    private static void populateStateTable() {
        /*
         * These are the items we'll be using while populating the state transition table.
         */
        TRANSITION_ITEMS.clear();
        TRANSITION_ITEMS.add(Pair.of("${1}", new String[] {"unlocked", "locked"}));
        TRANSITION_ITEMS.add(Pair.of("${2}", new String[] {"enabled", "disabled"}));
        TRANSITION_ITEMS.add(Pair.of("${3}", new String[] {"null", "failed", "dependency", DEPENDENCY_FAILED}));
        TRANSITION_ITEMS.add(Pair.of("${3:fail}", new String[] {"failed", DEPENDENCY_FAILED}));
        TRANSITION_ITEMS.add(Pair.of("${3:dep}", new String[] {"dependency", DEPENDENCY_FAILED}));
        TRANSITION_ITEMS.add(Pair.of("${4}", new String[] {"null", "coldstandby", "hotstandby", "providingservice"}));

        STATE_TABLE.clear();

        // lock
        populate("${1},enabled,${3},${4},lock", LOCKED_ENABLED_NULL_COLDSTANDBY);
        populate("${1},enabled,${3},null,lock", LOCKED_ENABLED_NULL_NULL);

        populate("${1},disabled,${3},${4},lock", LOCKED_DISABLED_ANY_COLDSTANDBY);
        populate("${1},disabled,${3},null,lock", LOCKED_DISABLED_ANY_NULL);


        // unlock
        populate("unlocked,enabled,${3},${4},unlock", UNLOCKED_ENABLED_NULL_ANY);
        populate("unlocked,enabled,${3},coldstandby,unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);

        populate("locked,enabled,${3},${4},unlock", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        populate("locked,enabled,${3},null,unlock", UNLOCKED_ENABLED_NULL_NULL);

        populate("${1},disabled,${3},${4},unlock", UNLOCKED_DISABLED_ANY_COLDSTANDBY);
        populate("${1},disabled,${3},null,unlock", UNLOCKED_DISABLED_ANY_NULL);


        // disableFailed
        populate("${1},${2},${3},${4},disableFailed", ANY_DISABLED_FAILED_COLDSTANDBY);
        populate("${1},${2},${3},null,disableFailed", ANY_DISABLED_FAILED_NULL);

        populate("${1},disabled,${3:dep},${4},disableFailed", ANY_DISABLED_DEPENDENCY_FAILED_COLDSTANDBY);
        populate("${1},disabled,${3:dep},null,disableFailed", ANY_DISABLED_DEPENDENCY_FAILED_NULL);


        // enableNotFailed
        populate("unlocked,${2},${3},${4},enableNotFailed", UNLOCKED_ENABLED_NULL_ANY);
        populate("unlocked,${2},${3},coldstandby,enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);

        populate("unlocked,disabled,${3},${4},enableNotFailed", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        populate("unlocked,disabled,${3},null,enableNotFailed", UNLOCKED_ENABLED_NULL_NULL);

        populate("locked,${2},${3},${4},enableNotFailed", LOCKED_ENABLED_NULL_COLDSTANDBY);
        populate("locked,${2},${3},null,enableNotFailed", LOCKED_ENABLED_NULL_NULL);

        populate("${1},disabled,${3:dep},${4},enableNotFailed", ANY_DISABLED_DEPENDENCY_COLDSTANDBY);
        populate("${1},disabled,${3:dep},null,enableNotFailed", ANY_DISABLED_DEPENDENCY_NULL);


        // disableDependency
        populate("${1},${2},${3},${4},disableDependency", ANY_DISABLED_DEPENDENCY_COLDSTANDBY);
        populate("${1},${2},${3},null,disableDependency", ANY_DISABLED_DEPENDENCY_NULL);

        populate("${1},disabled,${3:fail},${4},disableDependency", ANY_DISABLED_DEPENDENCY_FAILED_COLDSTANDBY);
        populate("${1},disabled,${3:fail},null,disableDependency", ANY_DISABLED_DEPENDENCY_FAILED_NULL);

        populate("unlocked,enabled,dependency,hotstandby,disableDependency", UNLOCKED_DISABLED_DEPENDENCY_HOTSTANDBY);


        // enableNoDependency
        populate("unlocked,enabled,${3},${4},enableNoDependency", UNLOCKED_ENABLED_NULL_ANY);
        populate("unlocked,enabled,${3},coldstandby,enableNoDependency", UNLOCKED_ENABLED_NULL_HOTSTANDBY);

        populate("unlocked,disabled,${3},${4},enableNoDependency", UNLOCKED_ENABLED_NULL_HOTSTANDBY);
        populate("unlocked,disabled,${3},null,enableNoDependency", UNLOCKED_ENABLED_NULL_NULL);

        populate("locked,${2},${3},${4},enableNoDependency", LOCKED_ENABLED_NULL_COLDSTANDBY);
        populate("locked,${2},${3},null,enableNoDependency", LOCKED_ENABLED_NULL_NULL);

        populate("${1},disabled,${3:fail},${4},enableNoDependency", ANY_DISABLED_FAILED_COLDSTANDBY);
        populate("${1},disabled,${3:fail},null,enableNoDependency", ANY_DISABLED_FAILED_NULL);


        // promote
        populate("unlocked,enabled,${3},${4},promote", UNLOCKED_ENABLED_NULL_PROVIDINGSERVICE);

        populate("locked,enabled,${3},${4},promote", LOCKED_ENABLED_NULL_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);

        populate("${1},disabled,${3},${4},promote", ANY_DISABLED_ANY_COLDSTANDBY_STANDBY_STATUS_EXCEPTION);


        // demote
        populate("unlocked,enabled,${3},${4},demote", UNLOCKED_ENABLED_NULL_HOTSTANDBY);

        populate("locked,enabled,${3},${4},demote", LOCKED_ENABLED_NULL_COLDSTANDBY);

        populate("${1},disabled,${3},${4},demote", ANY_DISABLED_ANY_COLDSTANDBY);
    }

    /**
     * Populates {@link #STATE_TABLE} with the incoming and outgoing strings, trying all
     * substitutions of the item place-holders that appear within the strings.
     * @param incoming incoming string, with optional item place-holders
     * @param outgoing outgoing string, with optional item place-holders
     */
    private static void populate(String incoming, String outgoing) {
        populate(incoming, outgoing, 0);
    }

    /**
     * Makes appropriate substitutions within the incoming and outgoing strings, looping
     * through all possible items at the given position. Once the position has reached the
     * end of the item table, the incoming/outgoing result is added to
     * {@link #STATE_TABLE}.
     *
     * @param incoming incoming string, with optional item place-holders
     * @param outgoing outgoing string, with optional item place-holders
     * @param pos current position within the transition items
     */
    private static void populate(String incoming, String outgoing, int pos) {

        if (pos >= TRANSITION_ITEMS.size()) {
            // used up all possible replacements - add result to the table
            STATE_TABLE.put(incoming, outgoing);
            return;
        }

        Pair<String, String[]> pair = TRANSITION_ITEMS.get(pos);
        String key = pair.getKey();

        if (!incoming.contains(key) && !outgoing.contains(key)) {
            // strings do not contain a place-holder for this position - try the next
            populate(incoming, outgoing, pos + 1);
            return;
        }

        // process all items associated with this place-holder
        for (String item : pair.getValue()) {
            String incoming2 = incoming.replace(key, item);
            String outgoing2 = outgoing.replace(key, item);
            populate(incoming2, outgoing2, pos + 1);
        }
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
