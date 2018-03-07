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
import java.util.Arrays;
import java.util.UUID;

import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;
import com.att.eelf.configuration.EELFLogger.Level;

/**
 * 
 * SystemOutLogger implements all the methods of interface Logger by calling System.out.println
 *
 */
public class SystemOutLogger implements Logger, Serializable {
	private static final Logger logger = FlexLogger.getLogger(SystemOutLogger.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 4956408061058933929L;
	private String className = "";
	private boolean isDebugEnabled = true;
	private boolean isInfoEnabled = true;
	private boolean isWarnEnabled = true;
	private boolean isErrorEnabled = true;
	private boolean isAuditEnabled = true;
	private boolean isMetricsEnabled = true;	
	private String transId = UUID.randomUUID().toString();

    /**
     * Constructor
     * @param clazz
     */
	public SystemOutLogger (Class<?> clazz){
		System.out.println("create instance of SystemOutLogger");
		if(clazz != null){
			className = clazz.getName();
		}
        initLevel();
	}
	
    /**
     * Constructor
     * @param s
     */
	public SystemOutLogger (String s){
		System.out.println("create instance of SystemOutLogger");
		if(s != null){
			className = s;
		}
        initLevel();
	}
	
	/**
	 * Sets logging levels
	 */
	private void initLevel(){
		
		if(PolicyLogger.getDebugLevel() == Level.DEBUG){
			isDebugEnabled = true;
			isInfoEnabled = true;
			isWarnEnabled = true;
		}else{
			isDebugEnabled = false;
		}
		
		if(PolicyLogger.getDebugLevel() == Level.INFO){
			isInfoEnabled = true;
			isWarnEnabled = true;
			isDebugEnabled = false;
		}
		
		if(PolicyLogger.getDebugLevel() == Level.OFF){
			isInfoEnabled = false;
			isWarnEnabled = false;
			isDebugEnabled = false;
		}
		
		if(PolicyLogger.getErrorLevel() == Level.OFF){
			isErrorEnabled = false;
		}

		if(PolicyLogger.getAuditLevel() == Level.OFF){
			isAuditEnabled = false;
		}
		
		if(PolicyLogger.getMetricsLevel() == Level.OFF){
			isMetricsEnabled = false;
		}
	}
	
    /**
     * Sets transaction Id
     */
	@Override
	public void setTransId(String transId){
		
		System.out.println(transId);
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

		System.out.println(transId + "|" + className+" : "+message);
	}

	/**
	 * Records an error message
	 * @param message
	 */
	@Override
	public void error(Object message) {

		System.out.println(transId + "|" + className+" : "+message);
	}

	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void info(Object message) {
	
		System.out.println(transId + "|" + className+" : "+message);

	}

	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void warn(Object message) {

		System.out.println(transId + "|" + className+" : "+message);
	}

	/**
	 * Records a message
	 * @param message
	 */
	@Override
	public void trace(Object message) {
		
		System.out.println(transId + "|" + className+" : "+message);
	}
	
	/**
	 * Returns true for debug enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isDebugEnabled(){
		
		return isDebugEnabled;
	}	
	
	/**
	 * Returns true for warn enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isWarnEnabled(){

		return isWarnEnabled;
	}
	
	/**
	 * Returns true for info enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isInfoEnabled(){

		return isInfoEnabled;
	}
	
	/**
	 * Returns true for error enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isErrorEnabled(){

		return isErrorEnabled;
	}
	
	/**
	 * Returns true for audit enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isAuditEnabled(){

		return isAuditEnabled;
	}
	
	/**
	 * Returns true for metrics enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isMetricsEnabled(){

		return isMetricsEnabled;
	}

	/**
	 * Records an audit message
	 * @param arg0
	 */
	@Override
	public void audit(Object arg0) {

		System.out.println(transId + "|" +className+" : "+arg0);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 */
	@Override
	public void recordAuditEventStart(String eventId) {

		System.out.println(transId + "|" +className+" : "+eventId);
		
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 */
	@Override
	public void recordAuditEventStart(UUID eventId) {

		System.out.println(eventId);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 * @param policyVersion
	 */
	@Override
	public void recordAuditEventEnd(String eventId, String rule, String policyVersion) {

		System.out.println(className+" : "+eventId  + ":" + rule + ":" + policyVersion);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 * @param policyVersion
	 */	
	@Override
	public void recordAuditEventEnd(UUID eventId, String rule, String policyVersion) {		
		
		System.out.println(className+" : "+eventId  + ":" + rule + ":" + policyVersion);		
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 */	
	@Override
	public void recordAuditEventEnd(String eventId, String rule) {

		System.out.println(className+" : "+eventId  + ":" + rule);
	}

	/**
	 * Records an audit message 
	 * @param eventId
	 * @param rule
	 */	
	@Override
	public void recordAuditEventEnd(UUID eventId, String rule) {		
		
		System.out.println(className+" : "+eventId  + ":" + rule);		
	}	
	
	/**
	 * Records a metrics message 
	 * @param eventId
	 * @param arg1
	 */	
	@Override
	public void recordMetricEvent(String eventId, String arg1) {
		
		logger.info(className+" : "+"eventId:" + eventId + "message:" + arg1);	
		
	}

	/**
	 * Records a metrics message 
	 * @param eventId
	 * @param arg1
	 */		
	@Override
	public void recordMetricEvent(UUID eventId, String arg1) {

		System.out.println(className+" : "+eventId  + ":" + arg1);
	}

	/**
	 * Records a metrics message 
	 * @param arg0
	 */	
	@Override
	public void metrics(Object arg0) {

		System.out.println(className+" : "+arg0);		
	}
	
	/**
	 * Records an error message 
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */		
	@Override
	public void error(MessageCodes msg, Throwable arg0, String... arguments){

		System.out.println(className+" : "+"MessageCodes :" + msg + Arrays.asList(arguments));

	}
	
	/**
	 * Records an error message 
	 * @param msg
	 * @param arguments
	 */	
	@Override
	public void error(MessageCodes msg, String... arguments){
		
		System.out.println(transId + "|" + className+" : "+"MessageCode:" + msg + Arrays.asList(arguments));
	}
	
	/**
	 * Returns transaction Id
	 * @param transId
	 */
	@Override
	public String postMDCInfoForEvent(String transId) {
		
		String transactionId = transId;
		if(transactionId == null || transactionId.isEmpty()){
			transactionId = UUID.randomUUID().toString();
		}
		
	    return transactionId;
	}
	
	/**
	 * Records a message 
	 * @param msg
	 * @param arguments
	 */
    @Override
	public void warn(MessageCodes msg, String... arguments){
    	
    	System.out.println(transId + "|" + className+" : "+"MessageCodes:" + msg + Arrays.asList(arguments));
    }

	/**
	 * Records a message 
	 * @param msg
	 * @param arg0
	 * @param arguments
	 */
    @Override
	public void warn(MessageCodes msg, Throwable arg0, String... arguments){
	
    	System.out.println(transId + "|" + className+" : "+"MessageCodes:" + msg + Arrays.asList(arguments));
    	
    }

	/**
	 * Records a message 
	 * @param message
	 * @param t
	 */
	@Override
	public void debug(Object message, Throwable t) {
		System.out.println(transId + "|" + className+" : "+ message + ":" + t);
	}

	/**
	 * Records an error message
	 * @param message
	 * @param t
	 */
	@Override
	public void error(Object message, Throwable t) {
		System.out.println(transId + "|" + className+" : "+ message + ":" + t);
	}

	/**
	 * Records a message
	 * @param message
	 * @param t
	 */
	@Override
	public void info(Object message, Throwable t) {
		System.out.println(transId + "|" + className+" : "+ message + ":" + t);
	}

	/**
	 * Records a message
	 * @param message
	 * @param t
	 */
	@Override
	public void warn(Object message, Throwable t) {
		System.out.println(transId + "|" + className+" : "+ message + ":" + t);
	}

	/**
	 * Records a message
	 * @param message
	 * @param t
	 */
	@Override
	public void trace(Object message, Throwable t) {
		System.out.println(transId + "|" + className+" : "+ message + ":" + t);
	}
	
	/**
	 * Records an audit message 
	 * @param arg0
	 * @param t
	 */
	@Override
	public void audit(Object arg0, Throwable t) {
		System.out.println(transId + "|" + className+" : "+ arg0 + ":" + t);
	}
	
	/**
	 * Returns true for trace enabled, or false for not
	 * @return boolean
	 */
	@Override
	public boolean isTraceEnabled() {
		// default
		return false;
	}
	
	/**
	 * Records transaction Id
	 * @param transId
	 */
    @Override
    public void postMDCInfoForTriggeredRule(String transId){
    	
    	System.out.println(transId);
    }
    
	/**
	 * Records transaction Id
	 * @param o
	 */
    @Override
	public void postMDCInfoForEvent(Object o){
    	System.out.println(o);
	}
}
