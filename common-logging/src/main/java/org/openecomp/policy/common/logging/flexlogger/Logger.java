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

package org.openecomp.policy.common.logging.flexlogger;

import static org.openecomp.policy.common.logging.eelf.Configuration.STATUS_CODE;

import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;
import java.util.UUID;

import org.slf4j.MDC;

import org.openecomp.policy.common.logging.eelf.MessageCodes;

/**
 * 
 * Interface Logger - implemented by Logger4J, EelfLogger and SystemOutLogger
 *
 */
public interface Logger {	
	
	/**
	 * Prints messages with the level.DEBUG
	 */
	public void debug(Object message);
	
	/**
	 * Prints messages with the level.ERROR
	 */
	public void error(Object message);
	
	/**
	 * Prints messages with the level.ERROR
	 */
	public void error(MessageCodes msg, Throwable arg0, String... arguments);

	/**
	 * Prints messages with the level.INFO
	 */
	public void info(Object message);
	
	/**
	 * Prints messages with the level.WARN
	 */
	public void warn(Object message);		

	/**
	 * Prints messages with the level.TRACE
	 */
	public void trace(Object message);	
	
	/**
	 * Prints messages in audit log with the level.INFO
	 */
	public void audit(Object arg0);
	
	/**
	 * Prints messages with the level.DEBUG
	 */
	public void debug(Object message, Throwable t);
	
	/**
	 * Prints messages with the level.ERROR
	 */
	public void error(Object message, Throwable t);

	/**
	 * Prints messages with the level.INFO
	 */
	public void info(Object message, Throwable t);
	
	/**
	 * Prints messages with the level.WARN
	 */
	public void warn(Object message, Throwable t);		

	/**
	 * Prints messages with the level.TRACE
	 */
	public void trace(Object message, Throwable t);	
	
	/**
	 * Prints messages in audit log with the level.INFO
	 */
	public void audit(Object arg0, Throwable t);
	
	/**
	 * Records event Id in audit log with the level.INFO
	 */
	public void recordAuditEventStart(String eventId);
	
	/**
	 * Records the starting time of the event with its request Id as the key
	 */
	public void recordAuditEventStart(UUID eventId);
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 */
	public void recordAuditEventEnd(String eventId, String rule, String policyVersion );
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 */
	public void recordAuditEventEnd(UUID eventId, String rule, String policyVersion);
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 */
	public void recordAuditEventEnd(String eventId, String rule);
	
	/**
	 * Records the ending time of the event with its request Id as the key
	 */
	public void recordAuditEventEnd(UUID eventId, String rule);
	
	
	/**
	 * Records the Metrics with event Id and log message 
	 */
	public void recordMetricEvent(String eventId, String arg1);
	
	/**
	 * Records the Metrics with event Id and log message 
	 */
	public void recordMetricEvent(UUID eventId, String arg1);
	
	/**
	 * Records the Metrics log message
	 */
	public void metrics(Object arg0);
	
	/**
	 * Returns a boolean value, true for debug logging enabled, false for not enabled
	 */
	public boolean isDebugEnabled();
	
	/**
	 * Returns a boolean value, true for error logging enabled, false for not enabled
	 */
	public boolean isErrorEnabled();
	
	/**
	 * Returns a boolean value, true for warn logging enabled, false for not enabled
	 */
	public boolean isWarnEnabled();
	
	/**
	 * Returns a boolean value, true for info logging enabled, false for not enabled
	 */
	public boolean isInfoEnabled();	

	/**
	 * Returns a boolean value, true for error logging enabled, false for not enabled
	 */
	public boolean isAuditEnabled();
	
	/**
	 * Returns a boolean value, true for warn logging enabled, false for not enabled
	 */
	public boolean isMetricsEnabled();
	
	/**
	 * Returns a boolean value, true for trace logging enabled, false for not enabled
	 */
	public boolean isTraceEnabled();
	
	
	/**
	 * Populates MDC info
	 */	
	public String postMDCInfoForEvent(String transId);
	
	/**
	 * Prints messages with the level.WARN
	 */
	public void warn(MessageCodes msg, String... arguments) ;
	
	/**
	 * Prints messages with the level.WARN
	 */
	public void warn(MessageCodes msg, Throwable arg0, String... arguments) ;
	
	/**
	 * Prints messages with the level.ERROR
	 */
	public void error(MessageCodes msg, String... arguments) ;
	
	/**
	 * Sets transaction Id
	 */
	public void setTransId(String transId);

	/**
	 * Returns transaction Id
	 */
	String getTransId();
	
	/**
	 * Populates MDC Info for the rule triggered
	 */
	public void postMDCInfoForTriggeredRule(String transId);
	
	/**
	 * Populates MDC Info
	 */
	public void postMDCInfoForEvent(Object o);
	
}
