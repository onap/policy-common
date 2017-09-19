/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
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

package org.onap.policy.common.logging.flexlogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;
import com.att.eelf.configuration.EELFLogger.Level;

/**
 * 
 * Logger4J implements all the methods of interface Logger by calling org.apache.log4j.Logger
 *
 */
public class Logger4J implements org.onap.policy.common.logging.flexlogger.Logger, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3183729429888828471L;
	private Logger log = null;
    private String methodName = "";
    private String className = "";
	private String transId = UUID.randomUUID().toString();
	
    /**
     * Constructor
     * @param clazz
     */
	public Logger4J (Class<?> clazz){
		System.out.println("create instance of Logger4J");
		if(clazz != null){
		   log = Logger.getLogger(clazz);
		   className = clazz.getName();
		}
	}
	
    /**
     * Constructor
     * @param s
     * @param className
     */
	public Logger4J (String s, String className){
		System.out.println("create instance of Logger4J");
		if(s != null){
		   log = Logger.getLogger(s);
		}
		this.className = className;
	}

    /**
     * Sets transaction Id
     */
	@Override
	public void setTransId(String transId){		
		log.info(transId);
		this.transId = transId;
	}
	
    /**
     * Returns transaction Id
     */
	@Override
	public String getTransId(){		
		return transId;
	}

	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void debug(Object message) {		
		if(isDebugEnabled()){
		   log.debug(transId + "|" + message);
		}
	}

	/**
	 * Records an error message
	 * @param message
	 */
	@Override
	public void error(Object message) {
		log.error( transId + "|" + className +"|" + message);
	}

	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void info(Object message) {
		log.info( transId + "|" + className +"|" + message);
	}
	
	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void warn(Object message) {
		log.warn( transId + "|" + className +"|" + message);
	}

	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void trace(Object message) {
		log.trace(transId + "|"+ className +"|"  + message);
	}
	
	/**
	 * Returns true for debug enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isDebugEnabled(){
		return log.isDebugEnabled();
	}
	
	/**
	 * Returns true for error enabled, or false for not
	 * @return boolean
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean isErrorEnabled(){
		return log.isEnabledFor(Priority.ERROR);
	}
	
	/**
	 * Returns true for info enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isInfoEnabled(){
		return log.isInfoEnabled();
	}
	
	/**
	 * Returns true for warn enabled, or false for not
	 * @return boolean
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean isWarnEnabled(){
		//return log4j value
		return log.isEnabledFor(Priority.WARN);
	}
	
	/**
	 * Returns true for audit enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isAuditEnabled(){		
		if(PolicyLogger.getAuditLevel() != null && PolicyLogger.getAuditLevel().toString().equals(Level.OFF.toString())){
			return false;
		}else {
			return true;
		}
	}
	
	/**
	 * Returns true for metrics enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isMetricsEnabled(){			
		if(PolicyLogger.getMetricsLevel() != null && PolicyLogger.getMetricsLevel().toString().equals(Level.OFF.toString())){
			return false;
		}else {
			return true;
		}
	}

	/**
	 * Records an audit message
	 * @param arg0
	 */
	@Override
	public void audit(Object arg0) {
		log.info(className +"|" +arg0);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 */	
	@Override
	public void recordAuditEventStart(String eventId) {
		log.info(className +"|recordAuditEventStart with eventId " + eventId);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 */	
	@Override
	public void recordAuditEventStart(UUID eventId) {
		if(eventId != null){
		   recordAuditEventStart(eventId.toString());	
		}
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 * @param policyVersion
	 */
	@Override
	public void recordAuditEventEnd(String eventId, String rule, String policyVersion) {		
		log.info(className +"|"+ eventId + ":" + rule);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 * @param policyVersion
	 */	
	@Override
	public void recordAuditEventEnd(UUID eventId, String rule, String policyVersion) {
		if(eventId != null){
			recordAuditEventEnd(eventId.toString(), rule, policyVersion);
		}else{
			recordAuditEventEnd(eventId, rule, policyVersion);
		}
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 */	
	@Override
	public void recordAuditEventEnd(String eventId, String rule) {		
		log.info(className +"|" +eventId + ":" + rule);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 */	
	@Override
	public void recordAuditEventEnd(UUID eventId, String rule) {
		if(eventId != null){
			recordAuditEventEnd(eventId.toString(), rule);
		}else{
			recordAuditEventEnd(eventId, rule);
		}
	}

	/**
	 * Records a metrics message 
	 * @param eventId
	 * @param arg1
	 */	
	@Override
	public void recordMetricEvent(String eventId, String arg1) {		
		log.info(className +"|" +eventId + ":" + arg1);
		
	}

	/**
	 * Records a metrics message 
	 * @param eventId
	 * @param arg1
	 */	
	@Override
	public void recordMetricEvent(UUID eventId, String arg1) {		
		if(eventId != null){
			recordMetricEvent(eventId.toString(), arg1);
		}else{
			recordMetricEvent(eventId, arg1);
		}		
	}

	/**
	 * Records a metrics message 
	 * @param arg0
	 */	
	@Override
	public void metrics(Object arg0) {		
		log.info(arg0);
	}

	/**
	 * Records an error message 
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */		
	@Override
	public void error(MessageCodes msg, Throwable arg0, String... arguments){
		log.error(transId + "|" + className +"|" + "MessageCodes :" + msg + arguments);

	}
	
	/**
	 * Records an error message 
	 * @param msg
	 * @param arguments
	 */		
	@Override
	public void error(MessageCodes msg, String... arguments){		
		log.error(transId + "|" + className +"|" + "MessageCode:" + msg + arguments);
	}
	
	/**
	 * Returns transaction Id 
	 * @param transId
	 */	
	@Override
	public String postMDCInfoForEvent(String transId) {		
		if(transId == null || transId.isEmpty()){
			transId = UUID.randomUUID().toString();
		}
		
	    return transId;
	}
	
	/**
	 * Records a message 
	 * @param msg
	 * @param arguments
	 */
    @Override
	public void warn(MessageCodes msg, String... arguments){    	
    	log.warn(className +"|" +"MessageCodes:" + msg + arguments);
    }

	/**
	 * Records a message 
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */
    @Override
	public void warn(MessageCodes msg, Throwable arg0, String... arguments){
    	log.warn(className +"|" +"MessageCodes:" + msg + arguments);    	
    }

	/**
	 * Records a message
	 * @param message
	 * @param t
	 */
	@Override
	public void debug(Object message, Throwable t) {
		log.debug(message, t);		
	}

	/**
	 * Records an error message
	 * @param message
	 * @param t
	 */
	@Override
	public void error(Object message, Throwable t) {
		log.error(message, t);
	}

	/**
	 * Records a message 
	 * @param message
	 * @param t
	 */
	@Override
	public void info(Object message, Throwable t) {
		log.info(message, t);
	}

	/**
	 * Records a message 
	 * @param message
	 * @param t
	 */
	@Override
	public void warn(Object message, Throwable t) {
		log.warn(message, t);
	}

	/**
	 * Records a message 
	 * @param message
	 * @param t
	 */
	@Override
	public void trace(Object message, Throwable t) {
		log.trace(message, t);
	}
	
	/**
	 * Records an audit message 
	 * @param arg0
	 * @param t
	 */

	@Override
	public void audit(Object arg0, Throwable t) {
		log.info(arg0, t);
	}

	/**
	 * Returns true for trace enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}
	
	/**
	 * Records transaction Id
	 * @param transId
	 */
    @Override
    public void postMDCInfoForTriggeredRule(String transId){    	
    	log.info(transId);
    }
    
	/**
	 * Records transaction Id
	 * @param o
	 */      
    @Override
	public void postMDCInfoForEvent(Object o){
    	log.info(o);
	}

	/* ============================================================ */

	/*
	 * Support for 'Serializable' --
	 * the default rules don't work for the 'log' field
	 */

	private void writeObject(ObjectOutputStream out) throws IOException {
		// write out 'methodName', 'className', 'transId' strings
		out.writeObject(methodName);
		out.writeObject(className);
		out.writeObject(transId);
	}

	private void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {

		// read in 'methodName', 'className', 'transId' strings
		methodName = (String)(in.readObject());
		className = (String)(in.readObject());
		transId = (String)(in.readObject());
	
		// look up associated logger
		log = Logger.getLogger(className);
	}
}
