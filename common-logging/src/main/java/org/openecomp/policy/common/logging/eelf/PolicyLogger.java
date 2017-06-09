/*-
 * ============LICENSE_START=======================================================
 * ECOMP-Logging
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

package org.openecomp.policy.common.logging.eelf;

import org.slf4j.MDC;

import static org.openecomp.policy.common.logging.eelf.Configuration.*;

import org.openecomp.policy.common.logging.flexlogger.LoggerType;
import org.openecomp.policy.common.logging.eelf.PolicyLogger;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import com.att.eelf.configuration.EELFManager;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * PolicyLogger contains all the static methods for EELF logging
 *
 */
public class PolicyLogger {

	private static EELFLogger errorLogger = EELFManager.getInstance()
			.getErrorLogger();

	private static EELFLogger metricsLogger = EELFManager.getInstance()
			.getMetricsLogger();

	private static EELFLogger auditLogger = EELFManager.getInstance()
			.getAuditLogger();

	private static EELFLogger debugLogger = EELFManager.getInstance()
			.getDebugLogger();
	
	private static EventTrackInfo eventTracker = new EventTrackInfo();
	
	private static String hostName = null;
	private static String hostAddress = null;
	private static String component = null;
	
	private static TimerTask ttrcker = null;
	private static boolean isEventTrackerRunning = false;
	private static Timer timer = null;
	
	//Default:Timer initial delay and the delay between in milliseconds before task is to be execute
	private static int TIMER_DELAY_TIME = 1000;
	
	//Default:Timer scheduleAtFixedRate period - time in milliseconds between successive task executions
	private static int CHECK_INTERVAL = 30 * 1000;
	
	//Default:longest time an event info can be stored in the concurrentHashMap for logging - in seconds 
	static int EXPIRED_TIME = 60*60*1000*24; //one day
	
	//Default:the size of the concurrentHashMap which stores the event starting time - when its size reaches this limit, the Timer get executed
	private static int CONCURRENTHASHMAP_LIMIT = 5000;
	
	//Default:the size of the concurrentHashMap which stores the event starting time - when its size drops to this point, stop the Timer
	private static int STOP_CHECK_POINT = 2500;
	
	private static boolean isOverrideLogbackLevel = false;
	
	public static Level DEBUG_LEVEL = Level.INFO;
	public static Level AUDIT_LEVEL = Level.INFO;
	public static Level METRICS_LEVEL = Level.INFO;
	public static Level ERROR_LEVEL = Level.ERROR;
	public static String CLASS_NAME = "ClassName";
	

	static{
		if (hostName == null || hostAddress == null) {
			try {
				hostName = InetAddress.getLocalHost().getHostName();
				hostAddress = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				PolicyLogger.error(MessageCodes.EXCEPTION_ERROR, e, "PolicyLogger", "UnknownHostException");
			}
		}
	}
	
	/**
	 * Populates MDC info 
	 * @param transId
	 * @return String
	 */
	public static String postMDCInfoForEvent(String transId) {
		MDC.clear();
		
		if(transId == null || transId.isEmpty()){
			transId = UUID.randomUUID().toString();
		}
		
		if(component != null && component.equalsIgnoreCase("DROOLS")){
			MDC.put(TARGET_ENTITY, "POLICY");
			MDC.put(TARGET_SERVICE_NAME,  "drools evaluate rule");	
			return postMDCInfoForEvent(transId, new DroolsPDPMDCInfo());
		} else {
			// For Xacml
			MDC.put(TARGET_ENTITY, "POLICY");
			MDC.put(TARGET_SERVICE_NAME,  "PE Process");
		}
		
		MDC.put(MDC_REMOTE_HOST, "");
		MDC.put(MDC_KEY_REQUEST_ID, transId);
		MDC.put(MDC_SERVICE_NAME, "Policy.xacmlPdp");
		MDC.put(MDC_SERVICE_INSTANCE_ID, "Policy.xacmlPdp.event");
		try {
			MDC.put(MDC_SERVER_FQDN, hostName);
			MDC.put(MDC_SERVER_IP_ADDRESS, hostAddress);
		} catch (Exception e) {
			errorLogger.error(MessageCodes.EXCEPTION_ERROR, e, "PolicyLogger");
		}
		Instant startTime = Instant.now();
		Instant endTime = Instant.now();
		long ns = Duration.between(startTime, endTime).toMillis(); // use millisecond as default and remove unit from log
		//String unit = " Seconds";
		//if(ns == 1){
			//unit = " Second";
		//}
		
		//if(ns < 1){
			//ns = Duration.between(startTime, endTime).toMillis();
			//unit = " milliseconds";
		//}
		MDC.put(MDC_INSTANCE_UUID, "");
		MDC.put(MDC_ALERT_SEVERITY, "");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
	
		String formatedTime = sdf.format(Date.from(startTime));
		MDC.put(BEGIN_TIME_STAMP, formatedTime );
		
		//set default values for these required fields below, they can be overridden
		formatedTime = sdf.format(Date.from(endTime));
		MDC.put(END_TIME_STAMP, formatedTime);
		MDC.put(ELAPSED_TIME, Long.toString(ns)); // + unit);
		
		MDC.put(PARTNER_NAME, "N/A");
		
		MDC.put(STATUS_CODE, "COMPLETE");
		MDC.put(RESPONSE_CODE, "N/A");
		MDC.put(RESPONSE_DESCRIPTION, "N/A");
		
		
		return transId;

	}
	
	/**
	 * Populate MDC Info using the passed in mdcInfo
	 * @param transId
	 * @param mdcInfo
	 * @return String
	 */
	private static String postMDCInfoForEvent(String transId, MDCInfo mdcInfo ) {
		
		MDC.put(MDC_KEY_REQUEST_ID, transId);
		if(mdcInfo != null && mdcInfo.getMDCInfo() != null && !mdcInfo.getMDCInfo().isEmpty()){
			
			ConcurrentHashMap<String, String> mdcMap = mdcInfo.getMDCInfo();
		    Iterator<String> keyIterator = mdcMap.keySet().iterator();
		    String key = "";
		    
		    while(keyIterator.hasNext()){
		    	key = keyIterator.next();
		    	MDC.put(key, mdcMap.get(key));
		    }		    
		}

		try {
			MDC.put(MDC_SERVER_FQDN, hostName);
			MDC.put(MDC_SERVER_IP_ADDRESS, hostAddress);
		} catch (Exception e) {
			errorLogger.error(MessageCodes.EXCEPTION_ERROR, e, "PolicyLogger");
		}
		Instant startTime = Instant.now();
		Instant endTime = Instant.now();
		long ns = Duration.between(startTime, endTime).toMillis(); // use millisecond as default and remove unit from log
		//String unit = " Seconds";
		//if(ns == 1){
			//unit = " Second";
		//}
		
		//if(ns < 1){
			//ns = Duration.between(startTime, endTime).toMillis();
			//unit = " milliseconds";
		//}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
	
		String formatedTime = sdf.format(Date.from(startTime));
		MDC.put(BEGIN_TIME_STAMP, formatedTime );
		
		//set default values for these required fields below, they can be overridden
		formatedTime = sdf.format(Date.from(endTime));
		MDC.put(END_TIME_STAMP, formatedTime);
		MDC.put(ELAPSED_TIME, Long.toString(ns)); // + unit);

		return transId;
	}

	/**
	 * Set Timestamps for start, end and duration of logging a transaction 
	 */
	private static void seTimeStamps(){
		
		Instant startTime = Instant.now();
		Instant endTime = Instant.now();
		long ns = Duration.between(startTime, endTime).toMillis();
		//String unit = " Seconds";
		//if(ns == 1){
			//unit = " Second";
		//}
		
		//if(ns < 1){
			//ns = Duration.between(startTime, endTime).toMillis();
			//unit = " milliseconds";
		//}
		MDC.put(MDC_INSTANCE_UUID, "");
		MDC.put(MDC_ALERT_SEVERITY, "");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
	
		String formatedTime = sdf.format(Date.from(startTime));
		MDC.put(BEGIN_TIME_STAMP, formatedTime );
		
		//set default values for these required fields below, they can be overridden
		formatedTime = sdf.format(Date.from(endTime));
		MDC.put(END_TIME_STAMP, formatedTime);
		MDC.put(ELAPSED_TIME, Long.toString(ns)); // + unit);
		
		MDC.put(PARTNER_NAME, "N/A");
		
		MDC.put(STATUS_CODE, "COMPLETE");
		MDC.put(RESPONSE_CODE, "N/A");
		MDC.put(RESPONSE_DESCRIPTION, "N/A");

	}
	
	/**
	 * Sets transaction Id to MDC
	 * @param transId
	 */
	public static void setTransId(String transId){
		
		MDC.put(MDC_KEY_REQUEST_ID, transId);
	}
	
	/**
	 * Returns current transaction Id used in MDC
	 * @return transId
	 */
	public static String getTransId(){
		
		return MDC.get(MDC_KEY_REQUEST_ID);		
	}
	
	/**
	 * Sets transaction Id to MDC
	 * @param o 
	 */
	public static void postMDCInfoForEvent(Object o){
		postMDCInfoForEvent(""+o);
	}

	/**
	 * Resets transaction Id in MDC for the rule triggered by this event
	 * @param transId
	 * @return String
	 */
	public static String postMDCInfoForTriggeredRule(String transId) {
		
		MDC.clear();
		
		if(transId == null || transId.isEmpty()){
			transId = UUID.randomUUID().toString();
		}
		MDC.put(MDC_REMOTE_HOST, "");
		MDC.put(MDC_KEY_REQUEST_ID, transId);
		MDC.put(MDC_SERVICE_NAME, "Policy.droolsPdp");
		MDC.put(MDC_SERVICE_INSTANCE_ID, "");
		try {
			MDC.put(MDC_SERVER_FQDN, hostName);
			MDC.put(MDC_SERVER_IP_ADDRESS, hostAddress);
		} catch (Exception e) {
			errorLogger.error(MessageCodes.EXCEPTION_ERROR, e, "PolicyLogger");
		}
		MDC.put(MDC_INSTANCE_UUID, "");
		MDC.put(MDC_ALERT_SEVERITY, "");
		MDC.put(STATUS_CODE, "COMPLETE");
		
		return transId;

	}
	
	/**
	 * Resets transaction Id in MDC for the rule triggered by this event
	 * @param o
	 */	
	public static void postMDCUUIDForTriggeredRule(Object o) {
		
		postMDCInfoForTriggeredRule("" + o);

	}

	// ************************************************************************************************
	/**
	 * Records the Info event with String [] arguments
	 * @param msg
	 * @param arguments
	 */
	public static void info(MessageCodes msg, String... arguments) {
		MDC.put(CLASS_NAME, "");
		debugLogger.info(msg, arguments);
	}
	
	/**
	 * Records the Info event with String [] arguments
	 * @param msg
	 * @param className
	 * @param arguments
	 */
	public static void info(MessageCodes msg, String className, String... arguments) {
		MDC.put(CLASS_NAME, className);
		debugLogger.info(msg, arguments);
	}
	
	/**
	 * Records only one String message with its class name
	 * @param className
	 * @param arg0
	 */
	public static void info( String className, String arg0) {
		MDC.put(CLASS_NAME, className);
		debugLogger.info(MessageCodes.GENERAL_INFO, arg0);
	}

	/**
	 * Records only one String message without its class name passed in
	 * @param arg0
	 */
	public static void info(String arg0) {
		MDC.put(CLASS_NAME, "");
		debugLogger.info(MessageCodes.GENERAL_INFO, arg0);
	}
	
	/**
	 * Records only one String message
	 * @param arg0
	 */
	public static void info(Object arg0) {
		MDC.put(CLASS_NAME, "");
		info(arg0);
	}

	/**
	 * Records a message with passed in message code, Throwable object, a list of string values
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */
	public static void info(MessageCodes msg, Throwable arg0,
			String... arguments) {
		MDC.put(CLASS_NAME, "");
	    String arguments2 = getNormalizedStackTrace(arg0, arguments);
	    debugLogger.info(msg, arguments2);
	}
	
	/**
	 * Records a message with passed in message code, class name, Throwable object, a list of string values
	 * @param msg
	 * @param className
	 * @param arg0
	 * @param arguments
	 */
	public static void info(MessageCodes msg, String className, Throwable arg0,
			String... arguments) {
		MDC.put(CLASS_NAME, className);
	    String arguments2 = getNormalizedStackTrace(arg0, arguments);
	    debugLogger.info(msg, arguments2);
	}
	
	/**
	 * Records only one String message with its class name
	 * @param arg0 log message
	 * @param className class name
	 */
	public static void warn( String className, String arg0) {
		MDC.put(CLASS_NAME, className);
		debugLogger.warn(MessageCodes.GENERAL_INFO, arg0);
	}

	/**
	 * Records only one String message
	 * @param arg0
	 */
	public static void warn(Object arg0) {
		MDC.put(CLASS_NAME, "");
		debugLogger.warn(MessageCodes.GENERAL_WARNING, "" + arg0);
	}
	
	/**
	 * Records only one String message without its class name passed in
	 * @param arg0
	 */
	public static void warn(String arg0) {
		MDC.put(CLASS_NAME, "");
		debugLogger.warn(MessageCodes.GENERAL_WARNING, arg0);
	}

	/**
	 * Records a message with passed in message code and a list of string values
	 * @param msg
	 * @param arguments
	 */
	public static void warn(MessageCodes msg, String... arguments) {
		MDC.put(CLASS_NAME, "");
		debugLogger.warn(msg, arguments);
	}
	
	/**
	 * Records a message with passed in message code, class name and a list of string values
	 * @param msg
	 * @param className
	 * @param arguments
	 */	
	public static void warn(MessageCodes msg, String className, String... arguments) {
		MDC.put(CLASS_NAME, className);
		debugLogger.warn(msg, arguments);
	}

	/**
	 * Records a message with passed in message code, Throwable object, a list of string values
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */
	public static void warn(MessageCodes msg, Throwable arg0,
			String... arguments) {
		MDC.put(CLASS_NAME, "");
	    String arguments2 = getNormalizedStackTrace(arg0, arguments);
	    debugLogger.warn(msg, arguments2);
	}
	
	/**
	 * Records a message with passed in message code, Throwable object, a list of string values
	 * @param msg
	 * @param className
	 * @param arg0
	 * @param arguments
	 */
	public static void warn(MessageCodes msg, String className, Throwable arg0,
			String... arguments) {
		MDC.put(CLASS_NAME, className);
	    String arguments2 = getNormalizedStackTrace(arg0, arguments);
	    debugLogger.warn(msg, arguments2);
	}

	/**
	 * Records only one String message with its class name
	 * @param className class name
	 * @param arg0 log message
	 */
	public static void error( String className, String arg0) {
		MDC.put(CLASS_NAME, className);
		if(ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR) != null){
		    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorCode());
			MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorDesc());

		}
		errorLogger.error(MessageCodes.GENERAL_ERROR, arg0);
	}
	
	/**
	 * Records only one String message
	 * @param arg0
	 */
	public static void error(String arg0) {
		MDC.put(CLASS_NAME, "");
		MDC.put(ERROR_CATEGORY, "ERROR");
		
		if(ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR) != null){
		    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorCode());
			MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorDesc());

		}
		errorLogger.error(MessageCodes.GENERAL_ERROR, arg0);
	}
	
	/**
	 * Records only one String message
	 * @param arg0
	 */
	public static void error(Object arg0) {
		MDC.put(CLASS_NAME, "");
		MDC.put(ERROR_CATEGORY, "ERROR");
		
		if(ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR) != null){
		    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorCode());
			MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorDesc());

		}
		errorLogger.error(MessageCodes.GENERAL_ERROR, "" + arg0);
	}
	
	/**
	 * Records a message with passed in message code, Throwable object, a list of string values
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */
	public static void error(MessageCodes msg, Throwable arg0,
			String... arguments) {
		MDC.put(CLASS_NAME, "");
		MDC.put(ERROR_CATEGORY, "ERROR");
		
		if(ErrorCodeMap.hm.get(msg) != null){
		    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(msg).getErrorCode());
			MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(msg).getErrorDesc());

		}
	    String arguments2 = getNormalizedStackTrace(arg0, arguments);
		errorLogger.error(msg, arguments2);
	}
	
	/**
	 * Records a message with passed in message code, class name, Throwable object, a list of string values
	 * @param msg
	 * @param className
	 * @param arg0
	 * @param arguments
	 */
	public static void error(MessageCodes msg, String className, Throwable arg0,
			String... arguments) {
		MDC.put(CLASS_NAME, className);
		MDC.put(ERROR_CATEGORY, "ERROR");
		
		if(ErrorCodeMap.hm.get(msg) != null){
		    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(msg).getErrorCode());
			MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(msg).getErrorDesc());

		}
	    String arguments2 = getNormalizedStackTrace(arg0, arguments);
		errorLogger.error(msg, arguments2);
	}

	/**
	 * Records a message with passed in message code and a list of string values
	 * @param msg
	 * @param arguments
	 */
	public static void error(MessageCodes msg, String... arguments) {
		MDC.put(CLASS_NAME, "");
		MDC.put(ERROR_CATEGORY, "ERROR");
		
		if(ErrorCodeMap.hm.get(msg) != null){
		    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(msg).getErrorCode());
			MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(msg).getErrorDesc());

		}
		errorLogger.error(msg, arguments);
	}

	/**
	 * Records a message with passed in message code and a list of string values
	 * @param msg
	 * @param arguments
	 */
	public static void debug(MessageCodes msg, String... arguments) {
		MDC.put(CLASS_NAME, "");
		debugLogger.debug(msg, arguments);
	}
	
	/**
	 * Records only one String message with its class name
	 * @param className
	 * @param arg0
	 */
	public static void debug( String className, String arg0) {
		MDC.put(CLASS_NAME, className);
		debugLogger.debug(MessageCodes.GENERAL_INFO, arg0);
	}

	/**
	 * Records only one String message
	 * @param arg0
	 */
	public static void debug(String arg0) {
		MDC.put(CLASS_NAME, "");
		debugLogger.debug(arg0);
	}
	
	/**
	 * Records only one String message
	 * @param arg0
	 */
	public static void debug(Object arg0) {

		MDC.put(CLASS_NAME, "");
		debugLogger.debug("" + arg0);
	}
	
	/**
	 * Records only one String message with its class name
	 * @param className
	 * @param arg0
	 */
	public static void audit(String className, Object arg0) {
		MDC.put(STATUS_CODE, "COMPLETE");
		MDC.put(CLASS_NAME, className);
		auditLogger.info("" + arg0);
	}

	/**
	 * Records only one String message
	 * @param arg0
	 */
	public static void audit(Object arg0) {
		MDC.put(STATUS_CODE, "COMPLETE");
		MDC.put(CLASS_NAME, "");
		auditLogger.info("" + arg0);
	}
	
	/**
	 * Records a message with passed in message code, hrowable object, a list of string values
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */
	public static void debug(MessageCodes msg, Throwable arg0,
			String... arguments) {
		MDC.put(CLASS_NAME, "");
	    String arguments2 = getNormalizedStackTrace(arg0, arguments);
		errorLogger.error(msg, arguments2);
	}
	
	/**
	 * Records a message with passed in message code, class name, Throwable object, a list of string values
	 * @param msg
	 * @param className
	 * @param arg0
	 * @param arguments
	 */
	public static void debug(MessageCodes msg, String className, Throwable arg0,
			String... arguments) {
		MDC.put(CLASS_NAME, className);
	    String arguments2 = getNormalizedStackTrace(arg0, arguments);
		errorLogger.error(msg, arguments2);
	}
	/**
	 * returns true for enabled, false for not enabled
	 */
	public static boolean isDebugEnabled(){
		
		return debugLogger.isDebugEnabled();
	}

	/**
	 * returns true for enabled, false for not enabled
	 */
	public static boolean isErrorEnabled(){
		
		return errorLogger.isErrorEnabled();
	}
	
	/**
	 * returns true for enabled, false for not enabled
	 */
	public static boolean isWarnEnabled(){
		
		return debugLogger.isWarnEnabled();
	}
	
	/**
	 * returns true for enabled, false for not enabled
	 */
	public static boolean isInfoEnabled1(){
		
		return debugLogger.isInfoEnabled();
	}	
	
	/**
	 * returns true for enabled, false for not enabled
	 */
	public static boolean isAuditEnabled(){
		
		return debugLogger.isInfoEnabled();
	}
	
	/**
	 * returns true for enabled, false for not enabled
	 */
	public static boolean isInfoEnabled(){
		
		return debugLogger.isInfoEnabled();
	}
	
	/**
	 * Records only one String message with its class name
	 * @param className
	 * @param arg0
	 */
	public static void trace( String className, String arg0) {
		MDC.put(CLASS_NAME, className);
		errorLogger.info(MessageCodes.GENERAL_INFO, arg0);
	}
		
	/**
	 * Records only one String message
	 * @param arg0
	 */
	public static void trace(Object arg0){
		
		MDC.put(CLASS_NAME, "");
		 debugLogger.trace(""+arg0);
	}	
	/**
	 * Records the starting time of the event with its request Id as the key
	 * @param eventId
	 */
	public static void recordAuditEventStart(String eventId) {
		
		MDC.put(STATUS_CODE, "COMPLETE");		
		postMDCInfoForEvent(eventId);
		
		if(eventTracker == null){
			eventTracker = new EventTrackInfo();
		}
		EventData  event = new EventData();
		event.setRequestID(eventId);
		event.setStartTime(Instant.now());
		eventTracker.storeEventData(event);
		MDC.put(MDC_KEY_REQUEST_ID, eventId);
		debugLogger.info("CONCURRENTHASHMAP_LIMIT : " + CONCURRENTHASHMAP_LIMIT);	
		//--- Tracking the size of the concurrentHashMap, if it is above limit, keep EventTrack Timer running
		int size = eventTracker.getEventInfo().size();
		
		debugLogger.info("EventInfo concurrentHashMap Size : " + size + " on " + new Date());	
		debugLogger.info("isEventTrackerRunning : " + isEventTrackerRunning);	
		
		if( size >= CONCURRENTHASHMAP_LIMIT){ 
			
			
			if(!isEventTrackerRunning){
				
			    startCleanUp();	
			    isEventTrackerRunning = true;
			}
			
		}else if( size <= STOP_CHECK_POINT){
		    
			if(isEventTrackerRunning){
				stopCleanUp();
			}			
		}
	}
	
	/**
	 * Records the starting time of the event with its request Id as the key
	 * @param eventId
	 */
	public static void recordAuditEventStart(UUID eventId) {
		
		if(eventId == null){
			return;
		}
		
		if(eventTracker == null){
			eventTracker = new EventTrackInfo();
		}
		
		recordAuditEventStart(eventId.toString());

	}
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 * @param eventId
	 * @param rule
	 */
	public static void recordAuditEventEnd(String eventId, String rule) {
		
		if(eventTracker == null){
			return;
		}
		if(eventId == null){
			return;
		}

		creatAuditEventTrackingRecord(eventId, rule, "");
		
	}
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 * @param eventId 
	 * @param rule
	 * @param policyVersion 
	 */
	public static void recordAuditEventEnd(String eventId, String rule , String policyVersion) {
		
		if(eventTracker == null){
			return;
		}
		if(eventId == null){
			return;
		}

		creatAuditEventTrackingRecord(eventId, rule, policyVersion);
		
	}
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 * @param eventId 
	 * @param rule
	 * @param policyVersion 
	 */
	public static void recordAuditEventEnd(UUID eventId, String rule, String policyVersion) {
		
		if(eventId == null){
			return;
		}
		
		recordAuditEventEnd(eventId.toString(), rule, policyVersion);
		
	}	
	
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 * @param eventId
	 * @param rule
	 */
	public static void recordAuditEventEnd(UUID eventId, String rule) {
		
		if(eventId == null){
			return;
		}
		
		recordAuditEventEnd(eventId.toString(), rule);
		
	}		
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 * @param eventId
	 * @param rule
	 * @param policyVersion 
	 */
	public static void creatAuditEventTrackingRecord(String eventId, String rule, String policyVersion) {
		
		if(eventTracker == null){
			return;
		}	
		
		EventData event = eventTracker.getEventDataByRequestID(eventId);
		
		if(event != null){
			Instant endTime = event.getEndTime();
			if(endTime == null){
				endTime = Instant.now();
			}
			MDC.put(STATUS_CODE, "COMPLETE");
			recordAuditEventStartToEnd(eventId, rule, event.getStartTime(), endTime, policyVersion);
		}
	}
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 * @param eventId 
	 * @param rule 
	 */
	public static void creatAuditEventTrackingRecord(UUID eventId, String rule) {
		
		if(eventId == null){
			return;
		}
		
		if(eventTracker == null){
			return;
		}	
		
		EventData event = eventTracker.getEventDataByRequestID(eventId.toString());
		
		if(event != null){
			Instant endTime = event.getEndTime();
			if(endTime == null){
				endTime = Instant.now();
			}
			
			recordAuditEventStartToEnd(eventId.toString(), rule, event.getStartTime(), endTime, "N/A");
		}
	}

	public static EventTrackInfo getEventTracker() {
		return eventTracker;
	}

	/**
	 * Records the audit with an event starting and ending times 
	 * @param eventId
	 * @param rule
	 * @param startTime
	 * @param endTime
	 * @param policyVersion
	 */
	public static void recordAuditEventStartToEnd(String eventId, String rule, Instant startTime, Instant endTime, String policyVersion) {
		
		if(startTime == null || endTime == null){
			return;
		}
		String serviceName = MDC.get(MDC_SERVICE_NAME);
		if(eventId != null && !eventId.isEmpty()){
		   MDC.put(MDC_KEY_REQUEST_ID, eventId);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		
		String formatedTime = sdf.format(Date.from(startTime));
		MDC.put(BEGIN_TIME_STAMP, formatedTime );
		
		//set default values for these required fields below, they can be overridden
		formatedTime = sdf.format(Date.from(endTime));
		MDC.put(END_TIME_STAMP, formatedTime);
		    	
		MDC.put(RESPONSE_CODE, "N/A");
		MDC.put(RESPONSE_DESCRIPTION, "N/A");
		
		long ns = Duration.between(startTime, endTime).toMillis();
		//String unit = " Seconds";
		//if(ns == 1){
			//unit = " Second";
		//}
		
		//if(ns < 1){
			//ns = Duration.between(startTime, endTime).toMillis();
			//unit = " milliseconds";
		//}
		
		MDC.put(ELAPSED_TIME, Long.toString(ns)); // + unit);
		
		auditLogger.info(MessageCodes.RULE_AUDIT_START_END_INFO,
			serviceName, rule, startTime.toString(), endTime.toString(), Long.toString(ns), policyVersion);
		
		//--- remove the record from the concurrentHashMap
		if(eventTracker != null){
			if(eventTracker.getEventDataByRequestID(eventId) != null){
				eventTracker.remove(eventId);
				debugLogger.info("eventTracker.remove(" + eventId + ")");
			}
		}	
	}

	/**
	 * Records the metrics with an event Id and log message 
	 * @param eventId
	 * @param arg1
	 */
	public static void recordMetricEvent(String eventId, String arg1) {
		
		seTimeStamps();  
		
		String serviceName = MDC.get(MDC_SERVICE_NAME);
		MDC.put(MDC_KEY_REQUEST_ID, eventId);
		metricsLogger.info(MessageCodes.RULE_AUDIT_END_INFO,
				serviceName, arg1);

	}
	
	/**
	 * Records the metrics with an event Id, class name and log message 
	 * @param eventId
	 * @param className
	 * @param arg1
	 */
	public static void recordMetricEvent(String eventId, String className,String arg1) {
		
		seTimeStamps();
		
		MDC.put(CLASS_NAME, className);
		String serviceName = MDC.get(MDC_SERVICE_NAME);
		MDC.put(MDC_KEY_REQUEST_ID, eventId);
		metricsLogger.info(MessageCodes.RULE_AUDIT_END_INFO,
				serviceName, arg1);
	}
	
	/**
	 * Records the metrics with an event Id and log message 
	 * @param eventId
	 * @param arg1
	 */
	public static void recordMetricEvent(UUID eventId, String arg1) {
		
		if(eventId == null){
			return;
		}		
		String serviceName = MDC.get(MDC_SERVICE_NAME);
		MDC.put(MDC_KEY_REQUEST_ID, eventId.toString());
		metricsLogger.info(MessageCodes.RULE_AUDIT_END_INFO,
				serviceName, arg1);
	}
	
	/**
	 * Records a String message for metrics logs
	 * @param arg0
	 */
	public static void recordMetricEvent(String arg0) {
		seTimeStamps();
		String serviceName = MDC.get(MDC_SERVICE_NAME);
		metricsLogger.info(MessageCodes.RULE_METRICS_INFO,
				serviceName, arg0);
	}
	

	/**
	 * Records the metrics event with a String message
	 * @param arg0
	 */
	public static void metrics(String arg0) {
		String serviceName = MDC.get(MDC_SERVICE_NAME);
		metricsLogger.info(MessageCodes.RULE_METRICS_INFO,
				serviceName, arg0);
	}
	
	/**
	 * Records the metrics event with a class name and a String message 
	 * @param arg0
	 */
	public static void metrics(String className, Object arg0) {
		seTimeStamps();
		MDC.put(CLASS_NAME, className);
		String serviceName = MDC.get(MDC_SERVICE_NAME);
		metricsLogger.info(MessageCodes.RULE_METRICS_INFO,
				serviceName, ""+arg0);
	}
	
	/**
	 * Records the metrics event with a String message
	 * @param arg0
	 */
	public static void metrics(Object arg0) {
		seTimeStamps();
		MDC.put(CLASS_NAME, "");
		String serviceName = MDC.get(MDC_SERVICE_NAME);
		metricsLogger.info(MessageCodes.RULE_METRICS_INFO,
				serviceName, ""+arg0);
	}
	
	/**
	 * Records the metrics event with a String message
	 * @param arg0
	 */
	public static void metricsPrintln(String arg0) {
		MDC.clear();
		metricsLogger.info(arg0);
	}
	
	/**
	 * Removes all the return lines from the printStackTrace
	 * @param t
	 * @param arguments
	 */
	private static String getNormalizedStackTrace (Throwable t, String...arguments) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        String newStValue = sw.toString().replace ('|', '!').replace ("\n", " - ");
        int curSize = (arguments == null ? 0 : arguments.length);
        StringBuffer newArgument = new StringBuffer();
        for(int i=0; i<curSize; i++) {
        	newArgument.append(arguments[i]);
        	newArgument.append(":");
        }
        newArgument.append(newStValue);       
		return newArgument.toString();
    }
	
	/**
	 * Starts the process of cleaning up the ConcurrentHashMap of EventData
	 */
	private static void startCleanUp(){
		
		if(!isEventTrackerRunning) {
			ttrcker = new EventTrackInfoHandler(); 
			timer = new Timer(true);
			timer.scheduleAtFixedRate(ttrcker, TIMER_DELAY_TIME, CHECK_INTERVAL);
			debugLogger.info("EventTrackInfoHandler begins! : " + new Date());
		}else{
			debugLogger.info("Timer is still running : " + new Date());

		}		
	}

	
	/**
	 * Stops the process of cleaning up the ConcurrentHashMap of EventData
	 */
	private static void stopCleanUp(){
		
		if(isEventTrackerRunning && timer != null){			
             timer.cancel();
             timer.purge();
 			debugLogger.info("Timer stopped: " + new Date());             
		}else{
			debugLogger.info("Timer was already stopped : " + new Date());

		}		
		isEventTrackerRunning = false;
		
	}
	
	/**
	 * Loads all the attributes from policyLogger.properties file
	 */
	public  static  LoggerType init(Properties properties) {

		Properties loggerProperties;
		if (properties != null) {
			loggerProperties = properties;
		} else {
			System.err.println("PolicyLogger cannot find its configuration - continue");
			loggerProperties = new Properties();
		}
			
		LoggerType logger_type = LoggerType.EELF;

		// fetch and verify definitions of some properties
		try{
			
			int timerDelayTime = Integer.parseInt(loggerProperties.getProperty("timer.delay.time", ""+1000));
			int checkInterval = Integer.parseInt(loggerProperties.getProperty("check.interval", ""+30000));
			int expiredDate = Integer.parseInt(loggerProperties.getProperty("event.expired.time",""+86400));
			int concurrentHashMapLimit = Integer.parseInt(loggerProperties.getProperty("concurrentHashMap.limit", ""+5000));
			int stopCheckPoint = Integer.parseInt(loggerProperties.getProperty("stop.check.point",""+2500));			
		    String loggerType = loggerProperties.getProperty("logger.type",logger_type.toString());
		    
		    String debugLevel = loggerProperties.getProperty("debugLogger.level","INFO");
		    String metricsLevel = loggerProperties.getProperty("metricsLogger.level","ON");
		    String auditLevel = loggerProperties.getProperty("error.level","ON");
		    String errorLevel = loggerProperties.getProperty("audit.level","ON");
		    component = loggerProperties.getProperty("policy.component","DROOLS");	
		    String overrideLogbackLevel = loggerProperties.getProperty("override.logback.level.setup");

		    if(overrideLogbackLevel != null && !overrideLogbackLevel.isEmpty()) {
		    	if(overrideLogbackLevel.equalsIgnoreCase("TRUE")){
		    		isOverrideLogbackLevel = true;
		    	}else{
		    		isOverrideLogbackLevel = false;
		    	}
		    }
		    
		
			if (debugLevel != null && !debugLevel.isEmpty()){
				
				DEBUG_LEVEL = Level.valueOf(debugLevel);
				
			}
			//Only check if it is to turn off or not
			if (errorLevel != null && errorLevel.equalsIgnoreCase(Level.OFF.toString())){
				
				ERROR_LEVEL = Level.valueOf(errorLevel);
				
			}
			//Only check if it is to turn off or not
			if (metricsLevel != null && metricsLevel.equalsIgnoreCase(Level.OFF.toString())){
				
				METRICS_LEVEL = Level.valueOf(metricsLevel);
				
			}
			//Only check if it is to turn off or not
			if (auditLevel != null && auditLevel.equalsIgnoreCase(Level.OFF.toString())){
				
				AUDIT_LEVEL = Level.valueOf(auditLevel);
				
			}			

            if(isOverrideLogbackLevel){
            	
				debugLogger.setLevel(DEBUG_LEVEL);	
				metricsLogger.setLevel(METRICS_LEVEL);				
				auditLogger.setLevel(AUDIT_LEVEL);				
				errorLogger.setLevel(ERROR_LEVEL);
				
            }
			isEventTrackerRunning = false;
			
			debugLogger.info("timerDelayTime value: " + timerDelayTime);

			debugLogger.info("checkInterval value: " + checkInterval);

			debugLogger.info("expiredDate value: " + expiredDate);

			debugLogger.info("concurrentHashMapLimit value: " + concurrentHashMapLimit);

			debugLogger.info("loggerType value: " + loggerType);		

			debugLogger.info("debugLogger level: " + debugLevel);	

			debugLogger.info("component: " + component);				

			if (timerDelayTime > 0){
				
				TIMER_DELAY_TIME = timerDelayTime;
				
			}else {
				MDC.put(ERROR_CATEGORY, "ERROR");
				if(ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR) != null){
				    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorCode());
					MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorDesc());

				}
				errorLogger.error("failed to get the timer.delay.time, so use its default value: " + TIMER_DELAY_TIME);
			}
			
			if (checkInterval > 0){
				
				CHECK_INTERVAL = checkInterval;
				
			}else {
				MDC.put(ERROR_CATEGORY, "ERROR");
				if(ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR) != null){
				    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorCode());
					MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorDesc());

				}
				errorLogger.error("failed to get the check.interval, so use its default value: " + CHECK_INTERVAL);
			}
			
			if (expiredDate > 0){
				
				EXPIRED_TIME = expiredDate;
				
			}else {
				MDC.put(ERROR_CATEGORY, "ERROR");
				
				if(ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR) != null){
				    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorCode());
					MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorDesc());

				}
				errorLogger.error("failed to get the event.expired.time, so use its default value: " + EXPIRED_TIME);
			}
			
			if (concurrentHashMapLimit > 0){
				
				CONCURRENTHASHMAP_LIMIT = concurrentHashMapLimit;
				
			}else {
				MDC.put(ERROR_CATEGORY, "ERROR");
				if(ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR) != null){
				    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorCode());
					MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorDesc());

				}
				errorLogger.error("failed to get the concurrentHashMap.limit, so use its default value: " + CONCURRENTHASHMAP_LIMIT);
			}	
			
			if (stopCheckPoint > 0){
				
				STOP_CHECK_POINT = stopCheckPoint;
				
			}else {
				MDC.put(ERROR_CATEGORY, "ERROR");
				if(ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR) != null){
				    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorCode());
					MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorDesc());

				}
				errorLogger.error("failed to get the stop.check.point, so use its default value: " + STOP_CHECK_POINT);
			}	
			
			if (loggerType != null){
				
				if (loggerType.equalsIgnoreCase("EELF")){
					
					logger_type = LoggerType.EELF;
					
				}else if (loggerType.equalsIgnoreCase("LOG4J")){
					
					logger_type = LoggerType.LOG4J;
					
				}else if (loggerType.equalsIgnoreCase("SYSTEMOUT")){
					
					logger_type = LoggerType.SYSTEMOUT;
					
				}
				
			}
			
			if (debugLevel != null && !debugLevel.isEmpty()){
				
				DEBUG_LEVEL = Level.valueOf(debugLevel);
				
			}
			//Only check if it is to turn off or not
			if (errorLevel != null && errorLevel.equalsIgnoreCase(Level.OFF.toString())){
				
				ERROR_LEVEL = Level.valueOf(errorLevel);
				
			}
			//Only check if it is to turn off or not
			if (metricsLevel != null && metricsLevel.equalsIgnoreCase(Level.OFF.toString())){
				
				METRICS_LEVEL = Level.valueOf(metricsLevel);
				
			}
			//Only check if it is to turn off or not
			if (auditLevel != null && auditLevel.equalsIgnoreCase(Level.OFF.toString())){
				
				AUDIT_LEVEL = Level.valueOf(auditLevel);
				
			}
			
		}catch(Exception e){
			MDC.put(ERROR_CATEGORY, "ERROR");
			
			if(ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR) != null){
			    MDC.put(ERROR_CODE, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorCode());
				MDC.put(ERROR_DESCRIPTION, ErrorCodeMap.hm.get(MessageCodes.GENERAL_ERROR).getErrorDesc());

			}
			errorLogger.error("failed to get the policyLogger.properties, so use their default values",e);
		}
		
		return logger_type;

	}
	
	/**
	 * Sets Debug Level
	 */
	public static void setDebugLevel(String debugLevel){
		
		if(isOverrideLogbackLevel){
			PolicyLogger.DEBUG_LEVEL = Level.valueOf(debugLevel); 
			debugLogger.setLevel(DEBUG_LEVEL);
		}
		
	}
	
    /**
     * Sets Error OFF or ON
     */
	public static void setErrorLevel(String errorLevel){	
		
		if(isOverrideLogbackLevel){
			if(errorLevel != null && errorLevel.equalsIgnoreCase("OFF")){
				PolicyLogger.ERROR_LEVEL = Level.OFF; 
				errorLogger.setLevel(ERROR_LEVEL);
			}else{
				//--- set default value
			    errorLogger.setLevel(Level.ERROR);
			    PolicyLogger.ERROR_LEVEL = Level.ERROR;
			}
		}
	}
	
	/**
	 * Sets Metrics OFF or ON
	 */
	public static void setMetricsLevel(String metricsLevel){
		
		if(isOverrideLogbackLevel){
			if(metricsLevel != null && metricsLevel.equalsIgnoreCase("OFF")){
				PolicyLogger.METRICS_LEVEL = Level.OFF;
				metricsLogger.setLevel(METRICS_LEVEL);
			}else {
				//--- set default value
				metricsLogger.setLevel(Level.INFO);
				PolicyLogger.METRICS_LEVEL = Level.INFO; 
			}
		}
		
	}
	
	/**
	 * Sets Audit OFF or ON
	 */
	public static void setAuditLevel(String auditLevel){
		
		if(isOverrideLogbackLevel){
			if(auditLevel != null && auditLevel.equalsIgnoreCase("OFF")){
				PolicyLogger.AUDIT_LEVEL = Level.OFF; 
				auditLogger.setLevel(AUDIT_LEVEL);
			}else {
				//--- set default value
				auditLogger.setLevel(Level.INFO);
				PolicyLogger.AUDIT_LEVEL = Level.INFO; 
			}
		}
	}
	
	/**
	 * Returns true for overriding logback levels; returns false for not
	 */
	public static boolean isOverrideLogbackLevel(){
		
		return isOverrideLogbackLevel;
	}
	
	/**
	 * Sets true for overriding logback levels; sets false for not
	 */	
	public static void setOverrideLogbackLevel(boolean odl){
		
		isOverrideLogbackLevel = odl;
		
	}
	/**
	 * Sets server information to MDC
	 */
	public static void setServerInfo(String serverHost, String serverPort){
		MDC.put(SERVER_NAME, serverHost+":"+serverPort);		
	}

}
