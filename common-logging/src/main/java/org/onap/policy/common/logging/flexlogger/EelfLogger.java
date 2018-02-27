/*
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import java.io.Serializable;
import java.util.UUID;

import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import com.att.eelf.configuration.EELFLogger.Level;

/**
 * 
 * EelfLogger implements all the methods of interface Logger by calling PolicyLogger methods
 *
 */

public class EelfLogger implements Logger, Serializable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 5385586713941277192L;
	private String className = "";
    private String transId = UUID.randomUUID().toString();
    
    /**
     * Constructor
     * @param clazz
     */
	public EelfLogger(Class<?> clazz) {
		if(clazz != null){
		   className = clazz.getName();
		}
		PolicyLogger.postMDCInfoForEvent(null);	
	}
	
    /**
     * Constructor
	 * @param s
     */
	public EelfLogger(String s) {
		if(s != null){
		   className = s;
		}
		PolicyLogger.postMDCInfoForEvent(null);	
	}
	
	/**
	 * Constructor
	 * @param clazz
	 * @param isNewTransaction
	 */
	public EelfLogger(Class<?> clazz, boolean isNewTransaction) {
		if(clazz != null){
		   className = clazz.getName();
		}
        if(isNewTransaction){        	
		    transId = PolicyLogger.postMDCInfoForEvent(null);		    
        }else{        	
        	transId = PolicyLogger.getTransId();
        }		
	}
	
	/**
	 * Constructor
	 * @param s
	 * @param isNewTransaction
	 */
	public EelfLogger(String s, boolean isNewTransaction) {
		if(s != null){
		   className = s;
		}
        if(isNewTransaction){        	
		    transId = PolicyLogger.postMDCInfoForEvent(null);		    
        }else{        	
        	transId = PolicyLogger.getTransId();
        }		
	}
	
	/**
	 * Constructor
	 * @param clazz
	 * @param transId
	 */
	public EelfLogger(Class<?> clazz, String transId) {
		if(clazz != null){
		   className = clazz.getName();
		}
		PolicyLogger.postMDCInfoForEvent(transId);
		this.transId = transId;
	}
	
	/**
	 * Constructor
	 * @param s
	 * @param transId
	 */
	public EelfLogger(String s, String transId) {
		if(s != null){
		   className = s;
		}
		PolicyLogger.postMDCInfoForEvent(transId);
		this.transId = transId;
	}
	
	/**
	 * Sets transaction Id for logging
	 * @param transId
	 */
	@Override
	public void setTransId(String transId){
		
		PolicyLogger.setTransId(transId);
		this.transId = transId;
	}
	
	/**
	 * Returns transaction Id for logging
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
		PolicyLogger.debug(className, ""+message);
	}

	/**
	 * Records an error message
	 * @param message
	 */
	@Override
	public void error(Object message) {		
		PolicyLogger.error(className, ""+message);
	}

	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void info(Object message) {		
        PolicyLogger.info(className, ""+message);
	}	

	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void warn(Object message) {		
		PolicyLogger.warn(className, ""+message);
	}

	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void trace(Object message) {		
		PolicyLogger.trace(className, ""+message);
	}
	
	/**
	 * Returns true for debug enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isDebugEnabled(){		
		return PolicyLogger.isDebugEnabled();
	}
	
	/**
	 * Returns true for info enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isInfoEnabled(){		
		return PolicyLogger.isInfoEnabled();
	}
	
	/**
	 * Returns true for warn enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isWarnEnabled(){		
		return PolicyLogger.isWarnEnabled();
	}
	
	/**
	 * Returns true for error enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isErrorEnabled(){		
		return PolicyLogger.isErrorEnabled();
	}
	
	/**
	 * Returns true for audit enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isAuditEnabled(){
		return(PolicyLogger.getAuditLevel() != Level.OFF);
	}
	
	/**
	 * Returns true for metrics enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isMetricsEnabled(){
		return(PolicyLogger.getMetricsLevel() != Level.OFF);
	}
	
	/**
	 * Returns true for trace enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isTraceEnabled(){		
		return PolicyLogger.isDebugEnabled();
	}
	
	/**
	 * Records an audit message
	 * @param arg0
	 */
	@Override
	public void audit(Object arg0) {		
		PolicyLogger.audit(className, ""+ arg0);		
	}
    
	/**
	 * Records a message 
	 * @param message
	 * @param t
	 */
	@Override
	public void debug(Object message, Throwable t) { 
		PolicyLogger.debug(MessageCodes.GENERAL_INFO, t, message.toString()); 
	}

	/**
	 * Records an error message 
	 * @param message
	 * @param t
	 */
	@Override
	public void error(Object message, Throwable t) { 
		PolicyLogger.error(MessageCodes.ERROR_UNKNOWN, t, message.toString()); 
	}

	/**
	 * Records a message 
	 * @param message
	 * @param t
	 */
	@Override
	public void info(Object message, Throwable t) { 
		PolicyLogger.info(MessageCodes.GENERAL_INFO, t, message.toString()); 
	}

	/**
	 * Records a message 
	 * @param message
	 * @param t
	 */
	@Override
	public void warn(Object message, Throwable t) { 
		PolicyLogger.warn(MessageCodes.GENERAL_WARNING, t, message.toString()); 
	}

	/**
	 * Records a message 
	 * @param message
	 * @param t
	 */
	@Override
	public void trace(Object message, Throwable t) { 
		PolicyLogger.trace(message); 
	} 
	
	/**
	 * Records an audit message 
	 * @param arg0
	 * @param t
	 */
	@Override
	public void audit(Object arg0, Throwable t) { 
		PolicyLogger.audit(arg0); 
	} 
	
	/**
	 * Records an audit message 
	 * @param eventId
	 */	
	@Override
	public void recordAuditEventStart(String eventId) {		
		PolicyLogger.recordAuditEventStart(eventId);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 */	
	@Override
	public void recordAuditEventStart(UUID eventId) {		
		PolicyLogger.recordAuditEventStart(eventId);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 * @param policyVersion
	 */	
	@Override
	public void recordAuditEventEnd(String eventId, String rule, String policyVersion) {		
		PolicyLogger.recordAuditEventEnd(eventId, rule, policyVersion);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 * @param policyVersion
	 */	
	@Override
	public void recordAuditEventEnd(UUID eventId, String rule, String policyVersion) {	
		PolicyLogger.recordAuditEventEnd(eventId, rule, policyVersion);
	}
	
	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 */	
	@Override
	public void recordAuditEventEnd(String eventId, String rule) {		
		PolicyLogger.recordAuditEventEnd(eventId, rule);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 */	
	@Override
	public void recordAuditEventEnd(UUID eventId, String rule) {		
		PolicyLogger.recordAuditEventEnd(eventId, rule);
	}	

	/**
	 * Records a metrics message 
	 * @param eventId
	 * @param arg1
	 */	
	@Override
	public void recordMetricEvent(String eventId, String arg1) {
		PolicyLogger.recordMetricEvent(eventId, arg1);
	}

	/**
	 * Records a metrics message 
	 * @param eventId
	 * @param arg1
	 */	
	@Override
	public void recordMetricEvent(UUID eventId, String arg1) {		
		PolicyLogger.recordMetricEvent(eventId, arg1);
	}

	/**
	 * Records a metrics message 
	 * @param arg0
	 */	
	@Override
	public void metrics(Object arg0) {
		PolicyLogger.metrics(className, arg0);
	}

	/**
	 * Records an error message 
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */		
	@Override
	public void error(MessageCodes msg, Throwable arg0, String... arguments){		
		PolicyLogger.error(msg, className, arg0, arguments);
	}
	
	/**
	 * Records an error message 
	 * @param msg
	 * @param arguments
	 */	
	@Override
	public void error(MessageCodes msg, String... arguments){		
		PolicyLogger.error(msg, arguments);  
	}
	
	/**
	 * Populates MDC Info
	 * @param transId
	 */	
	@Override
	public String postMDCInfoForEvent(String transId) {		
		return PolicyLogger.postMDCInfoForEvent(transId);
	
	}
	
	/**
	 * Records a message 
	 * @param msg
	 * @param arguments
	 */
    @Override
	public void warn(MessageCodes msg, String... arguments){    	
    	PolicyLogger.warn(msg, className, arguments);
    }
	
	/**
	 * Records a message 
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */
    @Override
	public void warn(MessageCodes msg, Throwable arg0, String... arguments){    	
    	PolicyLogger.warn(msg, className, arg0, arguments);    	
    }
	
	/**
	 * Populates MDC Info for the rule triggered
	 * @param transId
	 */
    @Override
    public void postMDCInfoForTriggeredRule(String transId){    	
    	PolicyLogger.postMDCInfoForTriggeredRule(transId);
    }
    
	/**
	 * Populates MDC Info
	 * @param o
	 */    
    @Override
	public void postMDCInfoForEvent(Object o){
    	PolicyLogger.postMDCInfoForEvent(o);
	}
}
