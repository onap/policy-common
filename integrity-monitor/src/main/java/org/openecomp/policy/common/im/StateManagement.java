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

package org.openecomp.policy.common.im;

import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

//import org.apache.log4j.Logger;

import org.openecomp.policy.common.im.jpa.StateManagementEntity;
import org.openecomp.policy.common.im.StateElement;
import org.openecomp.policy.common.im.StandbyStatusException;
import org.openecomp.policy.common.logging.flexlogger.FlexLogger; 
import org.openecomp.policy.common.logging.flexlogger.Logger;

/**
 * 
 * StateManagement class handles all state changes per the Telecom standard X.731.
 * It extends the Observable class and, thus, has an interface to register
 * instances of the StateChangeNotifier/Observer class.  When any state change
 * occurs, the registered observers are notified.
 *
 */
public class StateManagement extends Observable {
  private static final Logger logger = FlexLogger.getLogger(StateManagement.class);
  public static final String LOCKED               = "locked";   
  public static final String UNLOCKED             = "unlocked";   
  public static final String ENABLED              = "enabled"; 
  public static final String DISABLED             = "disabled";
  public static final String ENABLE_NOT_FAILED    = "enableNotFailed";   
  public static final String DISABLE_FAILED       = "disableFailed"; 
  public static final String FAILED               = "failed"; 
  public static final String DEPENDENCY           = "dependency"; 
  public static final String DEPENDENCY_FAILED    = "dependency,failed";
  public static final String DISABLE_DEPENDENCY   = "disableDependency";
  public static final String ENABLE_NO_DEPENDENCY = "enableNoDependency";
  public static final String NULL_VALUE           = "null";
  public static final String LOCK                 = "lock";   
  public static final String UNLOCK               = "unlock";  
  public static final String PROMOTE              = "promote"; 
  public static final String DEMOTE               = "demote"; 
  public static final String HOT_STANDBY          = "hotstandby"; 
  public static final String COLD_STANDBY         = "coldstandby"; 
  public static final String PROVIDING_SERVICE    = "providingservice"; 
  
  public static final String ADMIN_STATE     = "adminState"; 
  public static final String OPERATION_STATE = "opState"; 
  public static final String AVAILABLE_STATUS= "availStatus"; 
  public static final String STANDBY_STATUS  = "standbyStatus"; 
  
  private String resourceName = null; 
  private String adminState = null; 
  private String opState = null; 
  private String availStatus = null; 
  private String standbyStatus = null; 
  private EntityManager em; 
  private StateTransition st = null;
    
  /*
	 * Guarantees single-threadedness of all actions. Only one action can execute
	 * at a time. That avoids race conditions between actions being called
	 * from different places.
	 * 
	 * Some actions can take significant time to complete and, if another conflicting
	 * action is called during its execution, it could put the system in an inconsistent
	 * state.  This very thing happened when demote was called and the active/standby
	 * algorithm, seeing the state attempted to promote the PDP-D.
	 * 
	 */
	private static final Object SYNCLOCK = new Object();
	private static final Object FLUSHLOCK = new Object();
	
  /**
   * StateManagement constructor
   * @param emf
   * @param resourceName
   * @throws Exception
   */
  public StateManagement(EntityManagerFactory emf, String resourceName) throws Exception
  {
	  logger.debug("StateManagement: constructor, resourceName: " + resourceName);
	  em = emf.createEntityManager();
      EntityTransaction et = em.getTransaction();

      if(!et.isActive()){
    	  et.begin();
      }
      this.resourceName = resourceName; 
	  logger.info("resourceName = " + this.resourceName);
      

      try {
        //Create a StateManagementEntity object
  	    logger.debug("findStateManagementEntity for " + this.resourceName); 
        StateManagementEntity sm = findStateManagementEntity(em, this.resourceName); 

        //persist the administrative state
        if (sm != null) {
          logger.debug("Persist adminstrative state, resourceName = " + this.resourceName); 
          em.persist(sm);
          synchronized(FLUSHLOCK){
       		  et.commit(); 
          }
        } else {
        	synchronized(FLUSHLOCK){
       			et.commit(); 
        	}
        }
        
  	  //Load the StateTransition hash table
        st = new StateTransition();

        logger.debug("StateManagement: constructor end, resourceName: " + this.resourceName);
      } catch(Exception ex) {
    	  logger.error("StateManagement: constructor caught unexpected exception: " + ex);
    	  ex.printStackTrace();
    	  synchronized(FLUSHLOCK){
    		  if(et.isActive()){
    			  et.rollback();
    		  }
    	  }
    	  throw new Exception("StateManagement: Exception: " + ex.toString());
      } 
  }
  
  /**
   * initializeState() is called when it is necessary to set the StateManagement to a known initial state.
   * It preserves the Administrative State since it must persist across node reboots.
   * Starting from this state, the IntegrityMonitory will determine the Operational State and the
   * owning application will set the StandbyStatus.
   */
  public void initializeState() throws Exception
  {
	  synchronized (SYNCLOCK){
		  logger.debug("\nStateManagement: SYNCLOCK initializeState() operation for resourceName = " + this.resourceName + "\n");
		  logger.debug("StateManagement: initializeState() operation started, resourceName = " + this.resourceName);
		  EntityTransaction et = em.getTransaction();

		  if(!et.isActive()){
			  et.begin();
		  }

		  try {
			  logger.debug("findStateManagementEntity for " + this.resourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, this.resourceName); 
			  // set state
			  sm.setAdminState(sm.getAdminState()); //preserve the Admin state
			  sm.setOpState(StateManagement.ENABLED); 
			  sm.setAvailStatus(StateManagement.NULL_VALUE); 
			  sm.setStandbyStatus(StateManagement.NULL_VALUE); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  setChanged();
			  notifyObservers(ADMIN_STATE);

			  logger.debug("StateManagement: initializeState() operation completed, resourceName = " + this.resourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.initializeState() caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.initializeState() Exception: " + ex);
		  }
	  }
  } 
  
  /**
   * lock() changes the administrative state to locked.
   * @throws Exception
   */
  public void lock() throws Exception
  {
	  synchronized (SYNCLOCK){
		  logger.debug("\nStateManagement: SYNCLOCK lock() operation for resourceName = " + this.resourceName + "\n");
		  logger.debug("StateManagement: lock() operation started, resourceName = " + this.resourceName);
		  EntityTransaction et = em.getTransaction();

		  if(!et.isActive()){
			  et.begin();
		  }

		  try {
			  logger.debug("findStateManagementEntity for " + this.resourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, this.resourceName); 

			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), LOCK); 

			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus());

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  setChanged();
			  notifyObservers(ADMIN_STATE);

			  logger.debug("StateManagement: lock() operation completed, resourceName = " + this.resourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.lock() caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.lock() Exception: " + ex.toString());
		  } 
	  }
  }
  
  /**
   * unlock() changes the administrative state to unlocked.
   * @throws Exception
   */
  public void unlock() throws Exception
  {
	  synchronized (SYNCLOCK){
		  logger.debug("\nStateManagement: SYNCLOCK unlock() operation for resourceName = " + this.resourceName + "\n");
		  logger.debug("StateManagement: unlock() operation started, resourceName = " + this.resourceName);
		  EntityTransaction et = em.getTransaction();

		  if(!et.isActive()){
			  et.begin();  
		  }

		  try {
			  logger.debug("findStateManagementEntity for " + this.resourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, this.resourceName); 
			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), UNLOCK); 
			  // set transition state
			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus()); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  setChanged();
			  notifyObservers(ADMIN_STATE);

			  logger.debug("StateManagement: unlock() operation completed, resourceName = " + this.resourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.unlock() caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.unlock() Exception: " + ex);
		  }
	  }
  } 
  
  /**
   * enableNotFailed() removes the "failed" availability status and changes the operational
   * state to enabled if no dependency is also failed.
   * @throws Exception
   */
  public void enableNotFailed() throws Exception
  {
	  synchronized (SYNCLOCK){
		  logger.debug("\nStateManagement: SYNCLOCK enabledNotFailed() operation for resourceName = " + this.resourceName + "\n");
		  logger.debug("StateManagement: enableNotFailed() operation started, resourceName = " + this.resourceName);
		  EntityTransaction et = em.getTransaction();

		  if(!et.isActive()){
			  et.begin();
		  }

		  try {
			  logger.debug("findStateManagementEntity for " + this.resourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, this.resourceName); 
			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), ENABLE_NOT_FAILED); 
			  // set transition state
			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus()); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  setChanged();
			  notifyObservers(OPERATION_STATE);

			  logger.debug("StateManagement enableNotFailed() operation completed, resourceName = " + this.resourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.enableNotFailed() caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.enableNotFailed() Exception: " + ex);
		  }
	  }
  } 
  
  /**
   * disableFailed() changes the operational state to disabled and adds availability status of "failed"
   * @throws Exception
   */
  public void disableFailed() throws Exception
  {
	  synchronized (SYNCLOCK){
		  logger.debug("\nStateManagement: SYNCLOCK disabledFailed() operation for resourceName = " + this.resourceName + "\n");
		  logger.debug("StateManagement: disableFailed() operation started, resourceName = " + this.resourceName);
		  EntityTransaction et = em.getTransaction();
		  if(!et.isActive()){
			  et.begin();
		  }

		  try {
			  logger.debug("findStateManagementEntity for " + this.resourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, this.resourceName); 
			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), DISABLE_FAILED); 
			  // set transition state
			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus()); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  setChanged();
			  notifyObservers(OPERATION_STATE);

			  logger.debug("StateManagement: disableFailed() operation completed, resourceName = " + this.resourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.disableFailed() caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.disableFailed() Exception: " + ex);
		  }
	  }
  } 
  /**
   * This version of disableFailed is to be used to manipulate the state of a remote resource in the event
   * that remote resource has failed but its state is still showing that it is viable.
   * @throws Exception
   */
  public void disableFailed(String otherResourceName) throws Exception
  {
	  synchronized (SYNCLOCK){
		  if(otherResourceName == null){
			  logger.error("\nStateManagement: SYNCLOCK disableFailed(otherResourceName) operation: resourceName is NULL.\n");
			  return;
		  }
		  logger.debug("\nStateManagement: SYNCLOCK disabledFailed(otherResourceName) operation for resourceName = " 
				  + otherResourceName + "\n");
		  logger.debug("StateManagement: disableFailed(otherResourceName) operation started, resourceName = " 
				  + otherResourceName);
		  EntityTransaction et = em.getTransaction();
		  if(!et.isActive()){
			  et.begin();
		  }

		  try {
			  logger.debug("findStateManagementEntity for " + otherResourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, otherResourceName); 
			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), DISABLE_FAILED); 
			  // set transition state
			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus()); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  setChanged();
			  notifyObservers(OPERATION_STATE);

			  logger.debug("StateManagement: disableFailed(otherResourceName) operation completed, resourceName = " 
					  + otherResourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.disableFailed(otherResourceName) caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.disableFailed(otherResourceName) Exception: " + ex);
		  }
	  }
  } 

  /**
   * disableDependency() changes operational state to disabled and adds availability status of "dependency"
   * @throws Exception
   */
  public void disableDependency() throws Exception
  {
	  synchronized (SYNCLOCK){
		  logger.debug("\nStateManagement: SYNCLOCK disableDependency() operation for resourceName = " + this.resourceName + "\n");
		  logger.debug("StateManagement: disableDependency() operation started, resourceName = " + this.resourceName);
		  EntityTransaction et = em.getTransaction();

		  if(!et.isActive()){
			  et.begin();
		  }

		  try {
			  logger.debug("findStateManagementEntity for " + this.resourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, this.resourceName); 
			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), DISABLE_DEPENDENCY); 
			  // set transition state
			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus()); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  setChanged();
			  notifyObservers(OPERATION_STATE);

			  logger.debug("StateManagement: disableDependency() operation completed, resourceName = " + this.resourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.disableDependency() caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.disableDependency() Exception: " + ex);
		  }
	  }
  } 
  
  /**
   * enableNoDependency() removes the availability status of "dependency " and will change the 
   * operational state to enabled if not otherwise failed.
   * @throws Exception
   */
  public void enableNoDependency() throws Exception
  {
	  synchronized (SYNCLOCK){
		  logger.debug("\nStateManagement: SYNCLOCK enableNoDependency() operation for resourceName = " + this.resourceName + "\n");
		  logger.debug("StateManagement: enableNoDependency() operation started, resourceName = " + this.resourceName);
		  EntityTransaction et = em.getTransaction();

		  if(!et.isActive()){
			  et.begin();
		  }

		  try {
			  logger.debug("findStateManagementEntity for " + this.resourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, this.resourceName); 
			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), ENABLE_NO_DEPENDENCY); 
			  // set transition state
			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus()); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  setChanged();
			  notifyObservers(OPERATION_STATE);

			  logger.debug("StateManagement: enableNoDependency() operation completed, resourceName = " + this.resourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.enableNoDependency() caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.enableNoDependency() Exception: " + ex);
		  }
	  }
  } 
  
  /**
   * promote() changes the standby status to providingservice if not otherwise failed.
   * @throws StandbyStatusException
   * @throws Exception
   */
  public void promote() throws StandbyStatusException, Exception
  {
	  synchronized (SYNCLOCK){
		  logger.debug("\nStateManagement: SYNCLOCK promote() operation for resourceName = " + this.resourceName + "\n");
		  logger.debug("StateManagement: promote() operation started, resourceName = " + this.resourceName);
		  EntityTransaction et = em.getTransaction();

		  if(!et.isActive()){
			  et.begin();
		  }
		  
		  StateManagementEntity sm;

		  try{
			  logger.debug("findStateManagementEntity for " + this.resourceName); 
			  sm = findStateManagementEntity(em, this.resourceName); 
			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), PROMOTE); 
			  // set transition state
			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus()); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.commit(); 
				  }
			  }
			  setChanged();
			  notifyObservers(STANDBY_STATUS);
		  }catch(Exception ex){
			  logger.error("StateManagement.promote() caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.promote() Exception: " + ex);
		  }

		  logger.debug("StateManagement: promote() operation completed, resourceName = " + this.resourceName);
		  if (sm.getStandbyStatus().equals(StateManagement.COLD_STANDBY)){
			  String msg = "Failure to promote " + this.resourceName + " StandbyStatus = " + StateManagement.COLD_STANDBY;
			  throw new StandbyStatusException(msg);
		  }
	  }
  } 

  /**
   * demote() changes standbystatus to hotstandby or, if failed, coldstandby
   * @throws Exception
   */
  public void demote() throws Exception
  {
	  synchronized (SYNCLOCK){
		  logger.debug("\nStateManagement: SYNCLOCK demote() operation for resourceName = " + this.resourceName + "\n");
		  logger.debug("StateManagement: demote() operation started, resourceName = " + this.resourceName);
		  EntityTransaction et = em.getTransaction();

		  if(!et.isActive()){
			  et.begin();
		  }

		  try {
			  logger.debug("findStateManagementEntity for " + this.resourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, this.resourceName); 
			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), DEMOTE); 
			  // set transition state
			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus()); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  setChanged();
			  notifyObservers(STANDBY_STATUS); 

			  logger.debug("StateManagement: demote() operation completed, resourceName = " + this.resourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.demote() caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.demote() Exception: " + ex);
		  }
	  }
  } 

  /**
   * 
   * Only used for a remote resource.  It will not notify observers.  It is used only in cases where
   * the remote resource has failed is such a way  that it cannot update its own states. In particular
   * this is observed by PDP-D DroolsPdpsElectionHandler when it is trying to determine which PDP-D should
   * be designated as the lead.
   * @param otherResourceName
   * @throws Exception
   */
  public void demote(String otherResourceName) throws Exception
  {
	  synchronized (SYNCLOCK){
		  if(otherResourceName==null){
			  logger.error("\nStateManagement: SYNCLOCK demote(otherResourceName) operation: resourceName is NULL.\n");
			  return;
		  }
		  logger.debug("\nStateManagement: SYNCLOCK demote(otherResourceName) operation for resourceName = " + otherResourceName + "\n");

		  EntityTransaction et = em.getTransaction();

		  if(!et.isActive()){
			  et.begin();
		  }

		  try {
			  logger.debug("StateManagement: SYNCLOCK demote(otherResourceName) findStateManagementEntity for " + otherResourceName); 
			  StateManagementEntity sm = findStateManagementEntity(em, otherResourceName); 
			  StateElement stateElement = st.getEndingState(sm.getAdminState(), sm.getOpState(), 
					  sm.getAvailStatus(), sm.getStandbyStatus(), DEMOTE); 
			  // set transition state
			  sm.setAdminState(stateElement.getEndingAdminState()); 
			  sm.setOpState(stateElement.getEndingOpState()); 
			  sm.setAvailStatus(stateElement.getEndingAvailStatus()); 
			  sm.setStandbyStatus(stateElement.getEndingStandbyStatus()); 

			  em.persist(sm);
			  synchronized(FLUSHLOCK){
				  et.commit(); 
			  }
			  //We don't notify observers because this is assumed to be a remote resource

			  logger.debug("StateManagement: demote(otherResourceName) operation completed, resourceName = " + otherResourceName);
		  } catch(Exception ex) {
			  logger.error("StateManagement.demote(otherResourceName) caught unexpected exception: " + ex);
			  ex.printStackTrace();
			  synchronized(FLUSHLOCK){
				  if(et.isActive()){
					  et.rollback();
				  }
			  }
			  throw new Exception("StateManagement.demote(otherResourceName) Exception: " + ex);
		  }
	  }
  } 
 
  /**
 * @return
 */
public String getAdminState() 
  {
	  logger.debug("StateManagement(6/1/16): getAdminState for resourceName " + this.resourceName);
	  try {
          Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
       
          query.setParameter("resource", this.resourceName);
       
          //Just test that we are retrieving the right object
          @SuppressWarnings("rawtypes")
          List resourceList = query.setLockMode(
				  LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
          if (!resourceList.isEmpty()) {
       	      // exist 
        	  StateManagementEntity stateManagementEntity = (StateManagementEntity) resourceList.get(0);
        	  // refresh the object from DB in case cached data was returned
        	  em.refresh(stateManagementEntity);
        	  this.adminState = stateManagementEntity.getAdminState(); 
          } else {
        	  this.adminState = null; 
          }
	  } catch(Exception ex) {
		  ex.printStackTrace();
		  logger.error("StateManagement: getAdminState exception: " + ex.toString()); 
	  }	  
      
	  return this.adminState;
  }
  
  /**
 * @return
 */
public String getOpState() 
  {
	  logger.debug("StateManagement(6/1/16): getOpState for resourceName " + this.resourceName);
	  try {
          Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
       
          query.setParameter("resource", this.resourceName);
       
          //Just test that we are retrieving the right object
          @SuppressWarnings("rawtypes")
          List resourceList = query.setLockMode(
				  LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
          if (!resourceList.isEmpty()) {
       	      // exist 
        	   StateManagementEntity stateManagementEntity = (StateManagementEntity) resourceList.get(0);
         	   // refresh the object from DB in case cached data was returned
         	   em.refresh(stateManagementEntity);
          	   this.opState = stateManagementEntity.getOpState(); 
          } else {
        	  this.opState = null; 
          }
	  } catch(Exception ex) {
		  ex.printStackTrace();
		  logger.error("StateManagement: getOpState exception: " + ex.toString()); 
	  }	  
      
	  return this.opState;
  }
  
  /**
 * @return
 */
  public String getAvailStatus() 
  {
	  logger.debug("StateManagement(6/1/16): getAvailStatus for resourceName " + this.resourceName);
	  try {
          Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
       
          query.setParameter("resource", this.resourceName);
       
          //Just test that we are retrieving the right object
          @SuppressWarnings("rawtypes")
          List resourceList = query.setLockMode(
				  LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
          if (!resourceList.isEmpty()) {
       	      // exist 
              StateManagementEntity stateManagementEntity = (StateManagementEntity) resourceList.get(0);
        	  // refresh the object from DB in case cached data was returned
        	  em.refresh(stateManagementEntity);
        	  this.availStatus = stateManagementEntity.getAvailStatus(); 
          } else {
        	  this.availStatus = null; 
          }
	  } catch(Exception ex) {
		  ex.printStackTrace();
		  logger.error("StateManagement: getAvailStatus exception: " + ex.toString()); 
	  }	  
      
	  return this.availStatus;
  }
  
  /**
 * @return
 */
  public String getStandbyStatus() 
  {
	  logger.debug("StateManagement(6/1/16): getStandbyStatus for resourceName " + this.resourceName);
	  try {
          Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
       
          query.setParameter("resource", this.resourceName);
       
          //Just test that we are retrieving the right object
          @SuppressWarnings("rawtypes")
          List resourceList = query.setLockMode(
				  LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
          if (!resourceList.isEmpty()) {
       	      // exist 
              StateManagementEntity stateManagementEntity = (StateManagementEntity) resourceList.get(0);
        	  // refresh the object from DB in case cached data was returned
        	  em.refresh(stateManagementEntity);
        	  this.standbyStatus = stateManagementEntity.getStandbyStatus(); 
          } else {
        	  this.standbyStatus = null; 
          }
	  } catch(Exception ex) {
		  ex.printStackTrace();
		  logger.error("StateManagement: getStandbyStatus exception: " + ex.toString()); 
	  }	  
      
	  return this.standbyStatus;
  }
  
  /**
   * Find a StateManagementEntity
   * @param em
   * @param otherResourceName
   * @return
   */
  private static StateManagementEntity findStateManagementEntity(EntityManager em, String otherResourceName)
  {
	  logger.debug("StateManagementEntity: findStateManagementEntity: Entry");
	  StateManagementEntity stateManagementEntity = null; 
	  try {
          Query query = em.createQuery("Select p from StateManagementEntity p where p.resourceName=:resource");
       
          query.setParameter("resource", otherResourceName);
       
          //Just test that we are retrieving the right object
          @SuppressWarnings("rawtypes")
          List resourceList = query.setLockMode(
				  LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
          if (!resourceList.isEmpty()) {
       	      // exist 
        	  stateManagementEntity = (StateManagementEntity) resourceList.get(0);
        	  // refresh the object from DB in case cached data was returned
        	  em.refresh(stateManagementEntity);
        	  stateManagementEntity.setModifiedDate(new Date());
          } else {
        	  // not exist - create one
       		  stateManagementEntity = new StateManagementEntity(); 
        	  stateManagementEntity.setResourceName(otherResourceName); 
        	  stateManagementEntity.setAdminState(UNLOCKED); 
        	  stateManagementEntity.setOpState(ENABLED); 
        	  stateManagementEntity.setAvailStatus(NULL_VALUE);	
        	  stateManagementEntity.setStandbyStatus(NULL_VALUE); // default
          }
	  } catch(Exception ex) {
		  ex.printStackTrace();
		  logger.error("findStateManagementEntity exception: " + ex.toString()); 
	  }	  
	  return stateManagementEntity; 
  }
  
  /**
   * Get the standbystatus of a particular resource
   * @param otherResourceName
   * @return
   */
  public String getStandbyStatus(String otherResourceName) {

		if (logger.isDebugEnabled()) {
			logger.debug("StateManagement: getStandbyStatus: Entering, resourceName='"
					+ otherResourceName + "'");
		}

		String standbyStatus = null;
		
		// The transaction is required for the LockModeType
		EntityTransaction et = em.getTransaction();
		if(!et.isActive()){
			et.begin();
		}
		try {

			Query stateManagementListQuery = em
					.createQuery("SELECT p FROM StateManagementEntity p WHERE p.resourceName=:resource");
			stateManagementListQuery.setParameter("resource", otherResourceName);
			List<?> stateManagementList = stateManagementListQuery.setLockMode(
					  LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
			if (stateManagementList.size() == 1
					&& stateManagementList.get(0) instanceof StateManagementEntity) {
				StateManagementEntity stateManagementEntity = (StateManagementEntity) stateManagementList
						.get(0);
	        	// refresh the object from DB in case cached data was returned
	        	em.refresh(stateManagementEntity);
				standbyStatus = stateManagementEntity.getStandbyStatus();
				if (logger.isDebugEnabled()) {
					logger.debug("getStandbyStatus: resourceName =" + otherResourceName
							+ " has standbyStatus=" + standbyStatus);
				}
			} else {
				logger.error("getStandbyStatus: resourceName =" + otherResourceName
						+ " not found in statemanagemententity table");
			}
			synchronized(FLUSHLOCK){
				et.commit();
			}
		} catch (Exception e) {
			logger.error("getStandbyStatus: Caught Exception attempting to get statemanagemententity record, message='"
					+ e.getMessage() + "'");
			e.printStackTrace();
			synchronized(FLUSHLOCK){
				if(et.isActive()){
					et.rollback();
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("getStandbyStatus: Returning standbyStatus="
					+ standbyStatus);
		}

		return standbyStatus;
  }
  
  /**
   * Clean up all the StateManagementEntities
   */
  public void deleteAllStateManagementEntities() {

	  logger.info("StateManagement: deleteAllStateManagementEntities: Entering");

	  /*
	   * Start transaction
	   */
	  EntityTransaction et = em.getTransaction();
	  if(!et.isActive()){
		  et.begin();
	  }

	  try{
		  Query stateManagementEntityListQuery = em
				  .createQuery("SELECT p FROM StateManagementEntity p");
		  @SuppressWarnings("unchecked")
		  List<StateManagementEntity> stateManagementEntityList = stateManagementEntityListQuery.setLockMode(
				  LockModeType.NONE).setFlushMode(FlushModeType.COMMIT).getResultList();
		  logger.info("deleteAllStateManagementEntities: Deleting "
				  + stateManagementEntityList.size()
				  + " StateManagementEntity records");
		  for (StateManagementEntity stateManagementEntity : stateManagementEntityList) {
			  logger.info("deleteAllStateManagementEntities: Deleting statemanagemententity with resourceName="
					  + stateManagementEntity.getResourceName() + " and standbyStatus="
					  + stateManagementEntity.getStandbyStatus());
			  em.remove(stateManagementEntity);
		  }
		  synchronized(FLUSHLOCK){
			  et.commit();
		  }
	  }catch(Exception ex){
		  logger.error("StateManagement.deleteAllStateManagementEntities() caught Exception: " + ex);
		  ex.printStackTrace();
		  synchronized(FLUSHLOCK){
			  if(et.isActive()){
				  et.rollback();
			  }
		  }
	  }
	  if(logger.isDebugEnabled()){
		  logger.info("deleteAllStateManagementEntities: Exiting");
	  }
  }

}
