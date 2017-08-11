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

package org.onap.policy.common.im;

import java.util.*;


import org.onap.policy.common.im.StateElement; 
import org.onap.policy.common.im.StateManagement; 
import org.onap.policy.common.logging.flexlogger.FlexLogger; 
import org.onap.policy.common.logging.flexlogger.Logger;

/**
 * The StateTransition class coordinates all state transitions.
 */
public class StateTransition {
  private static final Logger logger = FlexLogger.getLogger(StateTransition.class);
  
  public static final String ADMIN_STATE     = "adminState"; 
  public static final String OPERATION_STATE = "opState"; 
  public static final String AVAILABLE_STATUS= "availStatus"; 
  public static final String STANDBY_STATUS  = "standbyStatus"; 
  public static final String ACTOIN_NAME     = "actionName";
   
  private HashMap<String, String> StateTable = new HashMap<>(); 
    
  /**
   * StateTransition constructor
   * @throws Exception
   */
  public StateTransition() throws StateTransitionException
  {
	  logger.debug("StateTransition constructor");

      try {
    	  logger.debug("Load StateTable started"); 
 
  		  setupStateTable();
      } catch(Exception ex) {
    	  throw new StateTransitionException("StateTransition Exception: " + ex.toString());
      } 
  }
  
  /**
   * Calculates the state transition and returns the end state
   * @param adminState
   * @param opState
   * @param availStatus
   * @param standbyStatus
   * @param actionName
   * @return
   * @throws Exception
   */
  public StateElement getEndingState(String adminState, String opState, String availStatus, 
		  String standbyStatus, String actionName) throws StateTransitionException
  {
	 logger.info("getEndingState");
	 logger.info("adminState=[" + adminState + "], opState=[" + opState + "], availStatus=[" + 
       availStatus + "], standbyStatus=[" + standbyStatus + "], actionName=[" + actionName + "]");
	 if(availStatus==null){
		 availStatus="null";
	 }
	 if(standbyStatus==null){
		 standbyStatus="null";
	 }
	 if(adminState==null || opState==null || actionName==null){
		 throw new StateTransitionException("Exception:StateTransition unable to process state: adminState=[" + adminState + "], opState=[" + opState + "], availStatus=[" + 
		            availStatus + "], standbyStatus=[" + standbyStatus + "], actionName=[" + actionName + "]");
	 }else if(!(adminState.equals(StateManagement.LOCKED) || adminState.equals(StateManagement.UNLOCKED))){
		 throw new StateTransitionException("Exception:StateTransition unable to process state: adminState=[" + adminState + "], opState=[" + opState + "], availStatus=[" + 
		            availStatus + "], standbyStatus=[" + standbyStatus + "], actionName=[" + actionName + "]");
	 }else if(!(opState.equals(StateManagement.ENABLED) || opState.equals(StateManagement.DISABLED))){
		 throw new StateTransitionException("Exception:StateTransition unable to process state: adminState=[" + adminState + "], opState=[" + opState + "], availStatus=[" + 
		            availStatus + "], standbyStatus=[" + standbyStatus + "], actionName=[" + actionName + "]");
	 }else if(!(standbyStatus.equals(StateManagement.NULL_VALUE) || 
			 standbyStatus.equals(StateManagement.COLD_STANDBY) ||
			 standbyStatus.equals(StateManagement.HOT_STANDBY) ||
			 standbyStatus.equals(StateManagement.PROVIDING_SERVICE))){
		 throw new StateTransitionException("Exception:StateTransition unable to process state: adminState=[" + adminState + "], opState=[" + opState + "], availStatus=[" + 
		            availStatus + "], standbyStatus=[" + standbyStatus + "], actionName=[" + actionName + "]");
	 }else if(!(availStatus.equals(StateManagement.NULL_VALUE) ||
			 availStatus.equals(StateManagement.DEPENDENCY) ||
			 availStatus.equals(StateManagement.DEPENDENCY_FAILED) ||
			 availStatus.equals(StateManagement.FAILED))){
		 throw new StateTransitionException("Exception:StateTransition unable to process state: adminState=[" + adminState + "], opState=[" + opState + "], availStatus=[" + 
		            availStatus + "], standbyStatus=[" + standbyStatus + "], actionName=[" + actionName + "]");
	 }
	 else if(!(actionName.equals(StateManagement.DEMOTE) || 
			 actionName.equals(StateManagement.DISABLE_DEPENDENCY) ||
			 actionName.equals(StateManagement.DISABLE_FAILED) ||
			 actionName.equals(StateManagement.ENABLE_NO_DEPENDENCY) ||
			 actionName.equals(StateManagement.ENABLE_NOT_FAILED) ||
			 actionName.equals(StateManagement.LOCK) ||
			 actionName.equals(StateManagement.PROMOTE) ||
			 actionName.equals(StateManagement.UNLOCK))){
		 throw new StateTransitionException("Exception:StateTransition unable to process state: adminState=[" + adminState + "], opState=[" + opState + "], availStatus=[" + 
		            availStatus + "], standbyStatus=[" + standbyStatus + "], actionName=[" + actionName + "]");
	 }

     StateElement stateElement = new StateElement(); 
	 try {
		 // dependency,failed is stored as dependency.failed in StateTable
		 String availStatus2 = availStatus;
		 if (availStatus2 != null) {
			 availStatus2 = availStatus.replace(",", "."); 
		 }  
	     String key = adminState + "," + opState + "," + availStatus2 + "," + standbyStatus + "," + actionName;
	     logger.debug("Ending State search key: " + key);
	     String value = StateTable.get(key); 
	      
	     if (value != null) {
             try {
        	     String parts[] = value.split(",", 5);
        	     stateElement.setEndingAdminState(parts[0].trim());
        	     stateElement.setEndingOpState(parts[1].trim());
        	     stateElement.setEndingAvailStatus(parts[2].trim().replace(".",  ","));
		         stateElement.setEndingStandbyStatus(parts[3].trim());
		         stateElement.setException(parts[4].trim());
		         stateElement.setAdminState(adminState);
		         stateElement.setOpState(opState);
		         stateElement.setAvailStatus(availStatus);
		         stateElement.setStandbyStatus(standbyStatus);
		         stateElement.setActionName(actionName);
		     
		         stateElement.displayStateElement();
             } catch(Exception ex) {
        	     logger.error("String split exception: " + ex.toString());
             }
 
       	 } else {
       	     String msg = "Ending state not found, adminState=[" + adminState + "], opState=[" + opState + "], availStatus=[" + 
             availStatus + "], standbyStatus=[" + standbyStatus + "], actionName=[" + actionName + "]"; 
       	     logger.error(msg);
       	     throw new StateTransitionException(msg);
       	 }
	 } catch (Exception ex) {
		 throw new StateTransitionException("Exception: " + ex.toString() + ", adminState=[" + adminState + "], opState=[" + opState + "], availStatus=[" + 
            availStatus + "], standbyStatus=[" + standbyStatus + "], actionName=[" + actionName + "]");
	 }

     return stateElement; 
  } 
  
  /**
   *  Adding State Transition info into HashMap. It includes all state/status and action combinations  
   * key  : adminState,opState,availStatus,standbyStatus,actionName
   * value: endingAdminState,endingOpState,endingAvailStatus,endingStandbyStatus,exception
   * Note : Use period instead of comma as seperator when store multi-value endingStandbyStatus (convert to 
   * comma during retrieval)
   * 
   * Note on illegal state/status combinations: This table has many state/status combinations that should never occur.
   * However, they *may* occur due to corruption or manual manipulation of the DB. So, in each case of an illegal 
   * combination, the state/status is first corrected before applying the action.  It is assumed that the administrative 
   * and operational states are always correct.  Second, if the availability status is in "agreement" with the operational 
   * state, it is assumed correct.  If it is null and the operational state is disabled, the availability status
   * is left null until a disabledfailed or disableddependency action is received. Or, if a enableNotFailed or 
   * enableNoDependency is received while the availability status is null, it will remain null, but the Operational state
   * will change to enabled.
   * 
   * If the standby status is not in agreement with the administrative and/or operational states, it is brought into 
   * agreement.  For example, if the administrative state is locked and the standby status is providingservice, the 
   * standby status is changed to coldstandby.
   * 
   * After bringing the states/status attributes into agreement, *then* the action is applied to them.  For example, if 
   * the administrative state is locked, the operational state is enabled, the availability status is null, the standby 
   * status is providingservice and the action is unlock, the standby status is changed to coldstandby and then the 
   * unlock action is applied. This will change the final state/status to administrative state = unlocked, operational 
   * state = disabled, availability status = null and standby status = hotstandby.
   * 
   * Note on standby status:  If the starting state of standby status is null and either a promote or demote action is
   * made, the assumption is that standbystatus is supported and therefore, the standby status will be changed to 
   * providingservice, hotstandby or coldstandby - depending on the value of the administrative and operational states.
   * If an attempt to promote is made when the administrative state is locked or operational state is disabled,
   * a StandbyStatusException will be thrown since promotion (state transition) is not possible. If the standby status
   * is coldstandby and a transition occurs on the administrative or operational state such that they are unlocked and
   * enabled, the standby status is automatically transitioned to hotstandby since it is only those two states that can
   * hold the statndby status in the coldstandby value.
   */
  
  private void setupStateTable() 
  {
      StateTable.put("unlocked,enabled,null,null,lock", "locked,enabled,null,null,");
      StateTable.put("unlocked,enabled,null,null,unlock", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,null,null,disableFailed", "unlocked,disabled,failed,null,");
      StateTable.put("unlocked,enabled,null,null,enableNotFailed", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,null,null,disableDependency", "unlocked,disabled,dependency,null,");
      StateTable.put("unlocked,enabled,null,null,enableNoDependency", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,null,null,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,null,null,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,null,coldstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,null,coldstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,null,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,null,coldstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,null,coldstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,null,coldstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,null,coldstandby,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,null,coldstandby,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,null,hotstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,null,hotstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,null,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,null,hotstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,null,hotstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,null,hotstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,null,hotstandby,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,null,hotstandby,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,null,providingservice,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,null,providingservice,unlock", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,null,providingservice,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,null,providingservice,enableNotFailed", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,null,providingservice,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,null,providingservice,enableNoDependency", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,null,providingservice,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,null,providingservice,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,null,lock", "locked,enabled,null,null,");
      StateTable.put("unlocked,enabled,failed,null,unlock", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,failed,null,disableFailed", "unlocked,disabled,failed,null,");
      StateTable.put("unlocked,enabled,failed,null,enableNotFailed", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,failed,null,disableDependency", "unlocked,disabled,dependency,null,");
      StateTable.put("unlocked,enabled,failed,null,enableNoDependency", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,failed,null,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,failed,null,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,coldstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,failed,coldstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,failed,coldstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,coldstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,failed,coldstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,coldstandby,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,failed,coldstandby,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,hotstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,failed,hotstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,failed,hotstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,hotstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,failed,hotstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,hotstandby,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,failed,hotstandby,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,failed,providingservice,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,failed,providingservice,unlock", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,failed,providingservice,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,failed,providingservice,enableNotFailed", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,failed,providingservice,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,failed,providingservice,enableNoDependency", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,failed,providingservice,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,failed,providingservice,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,null,lock", "locked,enabled,null,null,");
      StateTable.put("unlocked,enabled,dependency,null,unlock", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,dependency,null,disableFailed", "unlocked,disabled,failed,null,");
      StateTable.put("unlocked,enabled,dependency,null,enableNotFailed", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,dependency,null,disableDependency", "unlocked,disabled,dependency,null,");
      StateTable.put("unlocked,enabled,dependency,null,enableNoDependency", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,dependency,null,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency,null,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,coldstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,dependency,coldstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,dependency,coldstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,coldstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,dependency,coldstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,coldstandby,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency,coldstandby,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,hotstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,dependency,hotstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,dependency,hotstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,hotstandby,disableDependency", "unlocked,disabled,dependency,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,hotstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,hotstandby,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency,hotstandby,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency,providingservice,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,dependency,providingservice,unlock", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency,providingservice,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,dependency,providingservice,enableNotFailed", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency,providingservice,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,dependency,providingservice,enableNoDependency", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency,providingservice,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency,providingservice,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,null,lock", "locked,enabled,null,null,");
      StateTable.put("unlocked,enabled,dependency.failed,null,unlock", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,dependency.failed,null,disableFailed", "unlocked,disabled,failed,null,");
      StateTable.put("unlocked,enabled,dependency.failed,null,enableNotFailed", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,dependency.failed,null,disableDependency", "unlocked,disabled,dependency,null,");
      StateTable.put("unlocked,enabled,dependency.failed,null,enableNoDependency", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,enabled,dependency.failed,null,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency.failed,null,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,coldstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,coldstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,coldstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,coldstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,coldstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,coldstandby,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency.failed,coldstandby,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,hotstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,hotstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,hotstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,hotstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,hotstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,hotstandby,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency.failed,hotstandby,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,providingservice,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,providingservice,unlock", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency.failed,providingservice,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,providingservice,enableNotFailed", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency.failed,providingservice,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,enabled,dependency.failed,providingservice,enableNoDependency", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency.failed,providingservice,promote", "unlocked,enabled,null,providingservice,");
      StateTable.put("unlocked,enabled,dependency.failed,providingservice,demote", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,null,null,lock", "locked,disabled,null,null,");
      StateTable.put("unlocked,disabled,null,null,unlock", "unlocked,disabled,null,null,");
      StateTable.put("unlocked,disabled,null,null,disableFailed", "unlocked,disabled,failed,null,");
      StateTable.put("unlocked,disabled,null,null,enableNotFailed", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,disabled,null,null,disableDependency", "unlocked,disabled,dependency,null,");
      StateTable.put("unlocked,disabled,null,null,enableNoDependency", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,disabled,null,null,promote", "unlocked,disabled,null,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,null,null,demote", "unlocked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,null,coldstandby,lock", "locked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,null,coldstandby,unlock", "unlocked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,null,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,null,coldstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,null,coldstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,null,coldstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,null,coldstandby,promote", "unlocked,disabled,null,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,null,coldstandby,demote", "unlocked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,null,hotstandby,lock", "locked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,null,hotstandby,unlock", "unlocked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,null,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,null,hotstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,null,hotstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,null,hotstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,null,hotstandby,promote", "unlocked,disabled,null,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,null,hotstandby,demote", "unlocked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,null,providingservice,lock", "locked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,null,providingservice,unlock", "unlocked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,null,providingservice,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,null,providingservice,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,null,providingservice,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,null,providingservice,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,null,providingservice,promote", "unlocked,disabled,null,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,null,providingservice,demote", "unlocked,disabled,null,coldstandby,");
      StateTable.put("unlocked,disabled,failed,null,lock", "locked,disabled,failed,null,");
      StateTable.put("unlocked,disabled,failed,null,unlock", "unlocked,disabled,failed,null,");
      StateTable.put("unlocked,disabled,failed,null,disableFailed", "unlocked,disabled,failed,null,");
      StateTable.put("unlocked,disabled,failed,null,enableNotFailed", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,disabled,failed,null,disableDependency", "unlocked,disabled,dependency.failed,null,");
      StateTable.put("unlocked,disabled,failed,null,enableNoDependency", "unlocked,disabled,failed,null,");
      StateTable.put("unlocked,disabled,failed,null,promote", "unlocked,disabled,failed,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,failed,null,demote", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,coldstandby,lock", "locked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,coldstandby,unlock", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,coldstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,coldstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,failed,coldstandby,disableDependency", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,coldstandby,enableNoDependency", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,coldstandby,promote", "unlocked,disabled,failed,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,failed,coldstandby,demote", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,hotstandby,lock", "locked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,hotstandby,unlock", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,hotstandby,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,hotstandby,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,failed,hotstandby,disableDependency", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,hotstandby,enableNoDependency", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,hotstandby,promote", "unlocked,disabled,failed,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,failed,hotstandby,demote", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,providingservice,lock", "locked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,providingservice,unlock", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,providingservice,disableFailed", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,providingservice,enableNotFailed", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,failed,providingservice,disableDependency", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,providingservice,enableNoDependency", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,failed,providingservice,promote", "unlocked,disabled,failed,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,failed,providingservice,demote", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,null,lock", "locked,disabled,dependency,null,");
      StateTable.put("unlocked,disabled,dependency,null,unlock", "unlocked,disabled,dependency,null,");
      StateTable.put("unlocked,disabled,dependency,null,disableFailed", "unlocked,disabled,dependency.failed,null,");
      StateTable.put("unlocked,disabled,dependency,null,enableNotFailed", "unlocked,disabled,dependency,null,");
      StateTable.put("unlocked,disabled,dependency,null,disableDependency", "unlocked,disabled,dependency,null,");
      StateTable.put("unlocked,disabled,dependency,null,enableNoDependency", "unlocked,enabled,null,null,");
      StateTable.put("unlocked,disabled,dependency,null,promote", "unlocked,disabled,dependency,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,dependency,null,demote", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,coldstandby,lock", "locked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,coldstandby,unlock", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,coldstandby,disableFailed", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,coldstandby,enableNotFailed", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,coldstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,coldstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,dependency,coldstandby,promote", "unlocked,disabled,dependency,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,dependency,coldstandby,demote", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,hotstandby,lock", "locked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,hotstandby,unlock", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,hotstandby,disableFailed", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,hotstandby,enableNotFailed", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,hotstandby,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,hotstandby,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,dependency,hotstandby,promote", "unlocked,disabled,dependency,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,dependency,hotstandby,demote", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,providingservice,lock", "locked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,providingservice,unlock", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,providingservice,disableFailed", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,providingservice,enableNotFailed", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,providingservice,disableDependency", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency,providingservice,enableNoDependency", "unlocked,enabled,null,hotstandby,");
      StateTable.put("unlocked,disabled,dependency,providingservice,promote", "unlocked,disabled,dependency,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,dependency,providingservice,demote", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,null,lock", "locked,disabled,dependency.failed,null,");
      StateTable.put("unlocked,disabled,dependency.failed,null,unlock", "unlocked,disabled,dependency.failed,null,");
      StateTable.put("unlocked,disabled,dependency.failed,null,disableFailed", "unlocked,disabled,dependency.failed,null,");
      StateTable.put("unlocked,disabled,dependency.failed,null,enableNotFailed", "unlocked,disabled,dependency,null,");
      StateTable.put("unlocked,disabled,dependency.failed,null,disableDependency", "unlocked,disabled,dependency.failed,null,");
      StateTable.put("unlocked,disabled,dependency.failed,null,enableNoDependency", "unlocked,disabled,failed,null,");
      StateTable.put("unlocked,disabled,dependency.failed,null,promote", "unlocked,disabled,dependency.failed,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,dependency.failed,null,demote", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,coldstandby,lock", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,coldstandby,unlock", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,coldstandby,disableFailed", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,coldstandby,enableNotFailed", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,coldstandby,disableDependency", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,coldstandby,enableNoDependency", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,coldstandby,promote", "unlocked,disabled,dependency.failed,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,dependency.failed,coldstandby,demote", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,hotstandby,lock", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,hotstandby,unlock", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,hotstandby,disableFailed", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,hotstandby,enableNotFailed", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,hotstandby,disableDependency", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,hotstandby,enableNoDependency", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,hotstandby,promote", "unlocked,disabled,dependency.failed,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,dependency.failed,hotstandby,demote", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,providingservice,lock", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,providingservice,unlock", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,providingservice,disableFailed", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,providingservice,enableNotFailed", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,providingservice,disableDependency", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,providingservice,enableNoDependency", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("unlocked,disabled,dependency.failed,providingservice,promote", "unlocked,disabled,dependency.failed,coldstandby,StandbyStatusException");
      StateTable.put("unlocked,disabled,dependency.failed,providingservice,demote", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,enabled,null,null,lock", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,null,null,unlock", "unlocked,enabled,null,null,");
      StateTable.put("locked,enabled,null,null,disableFailed", "locked,disabled,failed,null,");
      StateTable.put("locked,enabled,null,null,enableNotFailed", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,null,null,disableDependency", "locked,disabled,dependency,null,");
      StateTable.put("locked,enabled,null,null,enableNoDependency", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,null,null,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,null,null,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,coldstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,coldstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,null,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,null,coldstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,coldstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,null,coldstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,coldstandby,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,null,coldstandby,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,hotstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,hotstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,null,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,null,hotstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,hotstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,null,hotstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,hotstandby,promote", "locked,enabled,null,coldstandby,StandbyStateException");
      StateTable.put("locked,enabled,null,hotstandby,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,providingservice,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,providingservice,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,null,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,null,providingservice,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,providingservice,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,null,providingservice,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,null,providingservice,promote", "locked,enabled,null,coldstandby,StandbyStateException");
      StateTable.put("locked,enabled,null,providingservice,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,null,lock", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,failed,null,unlock", "unlocked,enabled,null,null,");
      StateTable.put("locked,enabled,failed,null,disableFailed", "locked,disabled,failed,null,");
      StateTable.put("locked,enabled,failed,null,enableNotFailed", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,failed,null,disableDependency", "locked,disabled,dependency,null,");
      StateTable.put("locked,enabled,failed,null,enableNoDependency", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,failed,null,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,failed,null,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,coldstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,coldstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,failed,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,failed,coldstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,coldstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,failed,coldstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,coldstandby,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,failed,coldstandby,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,hotstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,hotstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,failed,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,failed,hotstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,hotstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,failed,hotstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,hotstandby,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,failed,hotstandby,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,providingservice,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,providingservice,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,failed,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,failed,providingservice,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,providingservice,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,failed,providingservice,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,failed,providingservice,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,failed,providingservice,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,null,lock", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,dependency,null,unlock", "unlocked,enabled,null,null,");
      StateTable.put("locked,enabled,dependency,null,disableFailed", "locked,disabled,failed,null,");
      StateTable.put("locked,enabled,dependency,null,enableNotFailed", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,dependency,null,disableDependency", "locked,disabled,dependency,null,");
      StateTable.put("locked,enabled,dependency,null,enableNoDependency", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,dependency,null,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,dependency,null,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,coldstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,coldstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,dependency,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,dependency,coldstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,coldstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,dependency,coldstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,coldstandby,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,dependency,coldstandby,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,hotstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,hotstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,dependency,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,dependency,hotstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,hotstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,dependency,hotstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,hotstandby,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,dependency,hotstandby,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,providingservice,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,providingservice,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,dependency,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,dependency,providingservice,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,providingservice,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,dependency,providingservice,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency,providingservice,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,dependency,providingservice,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,null,lock", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,dependency.failed,null,unlock", "unlocked,enabled,null,null,");
      StateTable.put("locked,enabled,dependency.failed,null,disableFailed", "locked,disabled,failed,null,");
      StateTable.put("locked,enabled,dependency.failed,null,enableNotFailed", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,dependency.failed,null,disableDependency", "locked,disabled,dependency,null,");
      StateTable.put("locked,enabled,dependency.failed,null,enableNoDependency", "locked,enabled,null,null,");
      StateTable.put("locked,enabled,dependency.failed,null,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,dependency.failed,null,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,coldstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,coldstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,dependency.failed,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,coldstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,coldstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,coldstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,coldstandby,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,dependency.failed,coldstandby,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,hotstandby,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,hotstandby,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,dependency.failed,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,hotstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,hotstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,hotstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,hotstandby,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,dependency.failed,hotstandby,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,providingservice,lock", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,providingservice,unlock", "unlocked,enabled,null,hotstandby,");
      StateTable.put("locked,enabled,dependency.failed,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,providingservice,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,providingservice,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,providingservice,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,enabled,dependency.failed,providingservice,promote", "locked,enabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,enabled,dependency.failed,providingservice,demote", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,null,lock", "locked,disabled,null,null,");
      StateTable.put("locked,disabled,null,null,unlock", "unlocked,disabled,null,null,");
      StateTable.put("locked,disabled,null,null,disableFailed", "locked,disabled,failed,null,");
      StateTable.put("locked,disabled,null,null,enableNotFailed", "locked,enabled,null,null,");
      StateTable.put("locked,disabled,null,null,disableDependency", "locked,disabled,dependency,null,");
      StateTable.put("locked,disabled,null,null,enableNoDependency", "locked,enabled,null,null,");
      StateTable.put("locked,disabled,null,null,promote", "locked,disabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,null,null,demote", "locked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,coldstandby,lock", "locked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,coldstandby,unlock", "unlocked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,null,coldstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,coldstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,null,coldstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,coldstandby,promote", "locked,disabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,null,coldstandby,demote", "locked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,hotstandby,lock", "locked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,hotstandby,unlock", "unlocked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,null,hotstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,hotstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,null,hotstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,hotstandby,promote", "locked,disabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,null,hotstandby,demote", "locked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,providingservice,lock", "locked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,providingservice,unlock", "unlocked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,null,providingservice,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,providingservice,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,null,providingservice,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,null,providingservice,promote", "locked,disabled,null,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,null,providingservice,demote", "locked,disabled,null,coldstandby,");
      StateTable.put("locked,disabled,failed,null,lock", "locked,disabled,failed,null,");
      StateTable.put("locked,disabled,failed,null,unlock", "unlocked,disabled,failed,null,");
      StateTable.put("locked,disabled,failed,null,disableFailed", "locked,disabled,failed,null,");
      StateTable.put("locked,disabled,failed,null,enableNotFailed", "locked,enabled,null,null,");
      StateTable.put("locked,disabled,failed,null,disableDependency", "locked,disabled,dependency.failed,null,");
      StateTable.put("locked,disabled,failed,null,enableNoDependency", "locked,disabled,failed,null,");
      StateTable.put("locked,disabled,failed,null,promote", "locked,disabled,failed,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,failed,null,demote", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,coldstandby,lock", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,coldstandby,unlock", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,coldstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,coldstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,failed,coldstandby,disableDependency", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,failed,coldstandby,enableNoDependency", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,coldstandby,promote", "locked,disabled,failed,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,failed,coldstandby,demote", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,hotstandby,lock", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,hotstandby,unlock", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,hotstandby,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,hotstandby,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,failed,hotstandby,disableDependency", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,failed,hotstandby,enableNoDependency", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,hotstandby,promote", "locked,disabled,failed,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,failed,hotstandby,demote", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,providingservice,lock", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,providingservice,unlock", "unlocked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,providingservice,disableFailed", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,providingservice,enableNotFailed", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,failed,providingservice,disableDependency", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,failed,providingservice,enableNoDependency", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,failed,providingservice,promote", "locked,disabled,failed,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,failed,providingservice,demote", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,dependency,null,lock", "locked,disabled,dependency,null,");
      StateTable.put("locked,disabled,dependency,null,unlock", "unlocked,disabled,dependency,null,");
      StateTable.put("locked,disabled,dependency,null,disableFailed", "locked,disabled,dependency.failed,null,");
      StateTable.put("locked,disabled,dependency,null,enableNotFailed", "locked,disabled,dependency,null,");
      StateTable.put("locked,disabled,dependency,null,disableDependency", "locked,disabled,dependency,null,");
      StateTable.put("locked,disabled,dependency,null,enableNoDependency", "locked,enabled,null,null,");
      StateTable.put("locked,disabled,dependency,null,promote", "locked,disabled,dependency,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,dependency,null,demote", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,coldstandby,lock", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,coldstandby,unlock", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,coldstandby,disableFailed", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency,coldstandby,enableNotFailed", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,coldstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,coldstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,dependency,coldstandby,promote", "locked,disabled,dependency,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,dependency,coldstandby,demote", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,hotstandby,lock", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,hotstandby,unlock", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,hotstandby,disableFailed", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency,hotstandby,enableNotFailed", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,hotstandby,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,hotstandby,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,dependency,hotstandby,promote", "locked,disabled,dependency,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,dependency,hotstandby,demote", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,providingservice,lock", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,providingservice,unlock", "unlocked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,providingservice,disableFailed", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency,providingservice,enableNotFailed", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,providingservice,disableDependency", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency,providingservice,enableNoDependency", "locked,enabled,null,coldstandby,");
      StateTable.put("locked,disabled,dependency,providingservice,promote", "locked,disabled,dependency,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,dependency,providingservice,demote", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,null,lock", "locked,disabled,dependency.failed,null,");
      StateTable.put("locked,disabled,dependency.failed,null,unlock", "unlocked,disabled,dependency.failed,null,");
      StateTable.put("locked,disabled,dependency.failed,null,disableFailed", "locked,disabled,dependency.failed,null,");
      StateTable.put("locked,disabled,dependency.failed,null,enableNotFailed", "locked,disabled,dependency,null,");
      StateTable.put("locked,disabled,dependency.failed,null,disableDependency", "locked,disabled,dependency.failed,null,");
      StateTable.put("locked,disabled,dependency.failed,null,enableNoDependency", "locked,disabled,failed,null,");
      StateTable.put("locked,disabled,dependency.failed,null,promote", "locked,disabled,dependency.failed,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,dependency.failed,null,demote", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,coldstandby,lock", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,coldstandby,unlock", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,coldstandby,disableFailed", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,coldstandby,enableNotFailed", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,coldstandby,disableDependency", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,coldstandby,enableNoDependency", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,coldstandby,promote", "locked,disabled,dependency.failed,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,dependency.failed,coldstandby,demote", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,hotstandby,lock", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,hotstandby,unlock", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,hotstandby,disableFailed", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,hotstandby,enableNotFailed", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,hotstandby,disableDependency", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,hotstandby,enableNoDependency", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,hotstandby,promote", "locked,disabled,dependency.failed,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,dependency.failed,hotstandby,demote", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,providingservice,lock", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,providingservice,unlock", "unlocked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,providingservice,disableFailed", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,providingservice,enableNotFailed", "locked,disabled,dependency,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,providingservice,disableDependency", "locked,disabled,dependency.failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,providingservice,enableNoDependency", "locked,disabled,failed,coldstandby,");
      StateTable.put("locked,disabled,dependency.failed,providingservice,promote", "locked,disabled,dependency.failed,coldstandby,StandbyStatusException");
      StateTable.put("locked,disabled,dependency.failed,providingservice,demote", "locked,disabled,dependency.failed,coldstandby,");
  }
  
  public void displayStateTable()
  {
	  Set<?> set = StateTable.entrySet();
      Iterator<?> iter = set.iterator();
 
	  while(iter.hasNext()) {
	      Map.Entry<?, ?> me = (Map.Entry<?, ?>)iter.next();
	      logger.debug((String)me.getKey() + ((String)me.getValue()).replace(".",  ",")); 
	  }
  }
}
