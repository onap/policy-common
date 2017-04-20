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

package org.openecomp.policy.common.logging;

//import static org.openecomp.policy.common.logging.eelf.Configuration.TRANSACTION_BEGIN_TIME_STAMP;
//import static org.openecomp.policy.common.logging.eelf.Configuration.TRANSACTION_ELAPSED_TIME;
//import static org.openecomp.policy.common.logging.eelf.Configuration.TRANSACTION_END_TIME_STAMP;

import static com.att.eelf.configuration.Configuration.MDC_ALERT_SEVERITY;
import static com.att.eelf.configuration.Configuration.MDC_INSTANCE_UUID;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.MDC;

import org.openecomp.policy.common.logging.nsa.LoggingContextFactory;
import org.openecomp.policy.common.logging.nsa.SharedLoggingContext;

/**
 * A facade over the org.openecomp.policy.common.logging.nsa.SharedLoggingContext interface/implementation that makes it
 * more convenient to use. SharedLoggingContext builds on the SLF4J/log4j Mapped Diagnostic Context (MDC)
 * feature, which provides a hashmap-based context for data items that need to be logged, where the
 * hashmap is kept in ThreadLocal storage. The data items can be referenced in the log4j configuration
 * using the EnhancedPatternLayout appender layout class, and the notation "%X{key}" in the ConversionPattern
 * string, where "key" is one of the keys in the MDC hashmap (which is determined by what hashmap entries the
 * application code creates). Example:
 *   log4j.appender.auditAppender.layout=org.apache.log4j.EnhancedPatternLayout
 *   log4j.appender.auditAppender.layout.ConversionPattern=%d|%X{requestId}|%X{serviceInstanceId}|...|%m%n
 * where "requestId" and "serviceInstanceId" are entries in the MDC hashmap.
 * 
 * The notable functionality the SharedLoggingContext adds over MDC is that it maintains its own copy
 * of the MDC data items in a hashmap (not in ThreadLocal storage), which allows more control of the data.
 * The ECOMPLoggingContext constructor that takes another ECOMPLoggingContext object as a parameter makes
 * use of this feature. For example if there is a thread pulling requests from a queue for processing, it
 * can keep a base logging context with data that is common to all requests, and for each request, create a
 * new logging context with that base context as a parameter; this will create a new logging context with
 * those initial values and none of the request-specific values from the previous request such as a request ID.
 
 * The setter methods in this class set corresponding items in the SharedLoggingContext/MDC hashmaps.
 * These items correspond to the fields defined in the "ECOMP platform application logging guidelines"
 * document.  In addition, there is a pair of convenience functions, transactionStarted() and
 * transactionEnded(), that can be called at the beginning and end of transaction processing to calculate
 * the duration of the transaction and record that value in the "timer" item.
 * 
 */
public class ECOMPLoggingContext {

	private static final String REQUEST_ID = "requestId";
	private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
	private static final String THREAD_ID = "threadId";
	private static final String SERVER_NAME = "serverName";
	private static final String SERVICE_NAME = "serviceName";
	private static final String PARTNER_NAME = "partnerName";
	private static final String STATUS_CODE = "statusCode";
	private static final String TARGET_ENTITY = "targetEntity";
	private static final String TARGET_SERVICE_NAME = "targetServiceName";
	private static final String INSTANCE_UUID = "instanceUuid";
	private static final String SEVERITY = "severity";
	private static final String SERVER_IP_ADDRESS = "serverIpAddress";
	private static final String SERVER = "server";
	private static final String CLIENT_IP_ADDRESS = "clientIpAddress";
	private static final String CLASSNAME = "classname";
	private static final String TRANSACTION_BEGIN_TIME_STAMP = "TransactionBeginTimestamp";
	private static final String TRANSACTION_END_TIME_STAMP = "TransactionEndTimestamp";
	private static final String TRANSACTION_ELAPSED_TIME = "TransactionElapsedTime";
	private static final String METRIC_BEGIN_TIME_STAMP = "MetricBeginTimestamp";
	private static final String METRIC_END_TIME_STAMP = "MetricEndTimestamp";
	private static final String METRIC_ELAPSED_TIME = "MetricElapsedTime";
	

    private static LoggingContextFactory.Builder loggingContextBuilder = new LoggingContextFactory.Builder();

    protected SharedLoggingContext context = null;
//    private long transactionStartTime = 0;
    private Instant transactionStartTime;
    private Instant metricStartTime;
    
    /**
     * Create a new ECOMPLoggingContext with no base context.
     */
	public ECOMPLoggingContext() {
		context = (SharedLoggingContext) loggingContextBuilder.forSharing().build();
	}

	/**
	 * Create a new ECOMPLoggingContext initially populated with the values
	 * in the given base context.  This can be used for example in a servlet
	 * where each incoming request may be processed by a separate thread, but
	 * there may be some logging data items that will be unchanging and common
	 * to all the threads.  That constant data can be populated in a base
	 * context, and then each request handler creates new context passing that
	 * base context to populate the common data in the new one.
	 * @param baseContext
	 */
	public ECOMPLoggingContext(ECOMPLoggingContext baseContext) {
		context = (SharedLoggingContext) loggingContextBuilder.forSharing().build();
		// a logging context could be passed into a thread (e.g. one that is servicing a queue)
		// that already had a logging context established, so the MDC hashmap could contain
		// entries that are no longer appropriate; so clear out the MDC hashmap before
		// transferring the new logging context values.
		// x
		MDC.clear();
		baseContext.context.transferTo(context);
		transactionStartTime = baseContext.transactionStartTime;
		setServiceName("POLICY");
		setPartnerName("USER");
		setStatusCode("COMPLETE");
		setTargetEntity("POLICY");
		setTargetServiceName("PE Process");
	}
	
	/**
	 * Indicate the start of transaction processing.  The current system time
	 * will be recorded for use by <code>transactionEnded()</code> to calculate
	 * the duration of the transaction.
	 */
	public void transactionStarted() {
//		transactionStartTime = System.currentTimeMillis();
		transactionStartTime = Instant.now();
		setTransactionBeginTimestamp(transactionStartTime);
	}
	
	/**
	 * Indicate the end of transaction processing.  The difference between the
	 * current system time and the time recorded by <code>transactionStarted()</code>
	 * will be recorded in the data item with key "TransactionElapsedTime".
	 */
	public void transactionEnded() {
		Instant transactionEndTime = Instant.now();
		setTransactionEndTimestamp(transactionEndTime);
		setTransactionElapsedTime(transactionEndTime);
	}
	
	/**
	 * Indicate the start of metric processing.  The current system time
	 * will be recorded for use by <code>metricEnded()</code> to calculate
	 * the duration of the metric.
	 */
	public void metricStarted() {
//		transactionStartTime = System.currentTimeMillis();
		metricStartTime = Instant.now();
		setMetricBeginTimestamp(metricStartTime);
	}
	
	/**
	 * Indicate the end of metric processing.  The difference between the
	 * current system time and the time recorded by <code>metricStarted()</code>
	 * will be recorded in the data item with key "MetricElapsedTime".
	 */
	public void metricEnded() {
		Instant metricEndTime = Instant.now();
		setMetricEndTimestamp(metricEndTime);
		setMetricElapsedTime(metricEndTime);
	}

	
	/**
	 * Set the value for the data item with key "requestId"
	 * 
	 * @param id
	 */
	public void setRequestID(String id) {
		context.put(REQUEST_ID, id);
	}
	/**
	 * Get the value for the data item with key "requestId"
	 * @return current value, or empty string if not set
	 */
	public String getRequestID() {
		return context.get(REQUEST_ID, "");
	}
	
	/**
	 * Set the value for the data item with key "serviceInstanceId"
	 * 
	 * @param id
	 */
	public void setServiceInstanceID(String id) {
		context.put(SERVICE_INSTANCE_ID, id);
	}
	/**
	 * Get the value for the data item with key "serviceInstanceId"
	 * @return current value, or empty string if not set
	 */
	public String getServiceInstanceID() {
		return context.get(SERVICE_INSTANCE_ID, "");
	}

	/**
	 * Set the value for the data item with key "threadId".
	 * An alternative to using this item is to use the
	 * %t conversion character in the appender's conversion string.
	 * 
	 * @param id
	 */
	public void setThreadID(String id) {
		context.put(THREAD_ID, id);
	}
	/**
	 * Get the value for the data item with key "threadId"
	 * @return current value, or empty string if not set
	 */
	public String getThreadID() {
		return context.get(THREAD_ID, "");
	}
	
	/**
	 * Set the value for the data item with key "serverName"
	 * 
	 * @param id
	 */
	public void setServerName(String name) {
		context.put(SERVER_NAME, name);
	}
	/**
	 * Get the value for the data item with key "serverName"
	 * @return current value, or empty string if not set
	 */
	public String getServerName() {
		return context.get(SERVER_NAME, "");
	}
	
	/**
	 * Set the value for the data item with key "serviceName"
	 * 
	 * @param id
	 */
	public void setServiceName(String name) {
		context.put(SERVICE_NAME, name);
	}
	/**
	 * Get the value for the data item with key "serviceName"
	 * @return current value, or empty string if not set
	 */
	public String getServiceName() {
		return context.get(SERVICE_NAME, "");
	}
	
	/**
	 * Set the value for the data item with key "partnerName"
	 * 
	 * @param id
	 */
	public void setPartnerName(String name) {
		context.put(PARTNER_NAME, name);
	}
	/**
	 * Get the value for the data item with key "partnerName"
	 * @return current value, or empty string if not set
	 */
	public String getPartnerName() {
		return context.get(PARTNER_NAME, "");
	}
	
	/**
	 * Set the value for the data item with key "statusCode"
	 * 
	 * @param id
	 */
	public void setStatusCode(String name) {
		context.put(STATUS_CODE, name);
	}
	/**
	 * Get the value for the data item with key "statusCode"
	 * @return current value, or empty string if not set
	 */
	public String getStatusCode() {
		return context.get(STATUS_CODE, "");
	}
	
	/**
	 * Set the value for the data item with key "targetEntity"
	 * 
	 * @param id
	 */
	public void setTargetEntity(String name) {
		context.put(TARGET_ENTITY, name);
	}
	/**
	 * Get the value for the data item with key "targetEntity"
	 * @return current value, or empty string if not set
	 */
	public String getTargetEntity() {
		return context.get(TARGET_ENTITY, "");
	}
	
	/**
	 * Set the value for the data item with key "targetServiceName"
	 * 
	 * @param id
	 */
	public void setTargetServiceName(String name) {
		context.put(TARGET_SERVICE_NAME, name);
	}
	/**
	 * Get the value for the data item with key "targetServiceName"
	 * @return current value, or empty string if not set
	 */
	public String getTargetServiceName() {
		return context.get(TARGET_SERVICE_NAME, "");
	}
	
	/**
	 * Set the value for the data item with key "instanceUuid"
	 * 
	 * @param id
	 */
	public void setInstanceUUID(String uuid) {
		context.put(INSTANCE_UUID, uuid);
	}
	/**
	 * Get the value for the data item with key "instanceUuid"
	 * @return current value, or empty string if not set
	 */
	public String getInstanceUUID() {
		return context.get(INSTANCE_UUID, "");
	}
	
	/**
	 * Set the value for the data item with key "severity"
	 * 
	 * @param id
	 */
	public void setSeverity(Long severity) {
		context.put(SEVERITY, severity);
	}
	/**
	 * Get the value for the data item with key "severity"
	 * @return current value, or empty string if not set
	 */
	public String getSeverity() {
		return context.get(SEVERITY, "");
	}
	
	/**
	 * Set the value for the data item with key "serverIp"
	 * 
	 * @param id
	 */
	public void setServerIPAddress(String serverIP) {
		context.put(SERVER_IP_ADDRESS, serverIP);
	}
	/**
	 * Get the value for the data item with key "serverIp"
	 * @return current value, or empty string if not set
	 */
	public String getServerIPAddress() {
		return context.get(SERVER_IP_ADDRESS, "");
	}
	
	/**
	 * Set the value for the data item with key "server"
	 * 
	 * @param id
	 */
	public void setServer(String server) {
		context.put(SERVER, server);
	}
	/**
	 * Get the value for the data item with key "server"
	 * @return current value, or empty string if not set
	 */
	public String getServer() {
		return context.get(SERVER, "");
	}
	
	/**
	 * Set the value for the data item with key "clientIp"
	 * 
	 * @param id
	 */
	public void setClientIPAddress(String clientIP) {
		context.put(CLIENT_IP_ADDRESS, clientIP);
	}
	/**
	 * Get the value for the data item with key "clientIp"
	 * @return current value, or empty string if not set
	 */
	public String getClientIPAddress() {
		return context.get(CLIENT_IP_ADDRESS, "");
	}
	
	/**
	 * Set the value for the data item with key "classname".
	 * Use of this item is not recommended (unless it is used to
	 * indicate something other than the Java classname) since
	 * it would be unwieldy to maintain a correct value across
	 * calls to/returns from methods in other classes.
	 * Use of the PatternLayout %c conversion character in the
	 * conversion string will give a more reliable value.
	 * 
	 * @param id
	 */
	public void setClassname(String classname) {
		context.put(CLASSNAME, classname);
	}
	/**
	 * Get the value for the data item with key "classname"
	 * @return current value, or empty string if not set
	 */
	public String getClassname() {
		return context.get(CLASSNAME, "");
	}

	/**
	 * Set the value for the data item with key "timer".
	 * An alternative to calling this method directly is to call
	 * <code>transactionStarted()</code> at the start of transaction
	 * processing and <code>transactionEnded()</code> at the end,
	 * which will compute the time difference in milliseconds
	 * and store the result as the "timer" value.
	 * 
	 * @param id
	 */
//	public void setTimer(Long timer) {
//		context.put(TIMER, timer);
//	}
	
//	public void setTimer(Long elapsedtime) {
//		String unit = " milliseconds";
//		context.put(TRANSACTION_ELAPSED_TIME, elapsedtime + unit);
//	}

	/**
	 * Get the value for the data item with key "timer"
	 * @return current value, or 0 if not set
	 */
//	public long getTimer() {
//		return context.get(TRANSACTION_ELAPSED_TIME, 0);
//	}
	
	/**
	 * Set the value for the data item with key "TransactionBeginTimestamp"
	 * 
	 * @param id
	 */
	public void setTransactionBeginTimestamp(Instant transactionStartTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		
		String formatedTime = sdf.format(Date.from(transactionStartTime));
		context.put(TRANSACTION_BEGIN_TIME_STAMP, formatedTime );
	}
	
	/**
	 * Get the value for the data item with key "TransactionBeginTimestamp"
	 * @return current value, or 0 if not set
	 */
	public long getTransactionBeginTimestamp() {
		return context.get(TRANSACTION_BEGIN_TIME_STAMP, 0);
	}
	
	/**
	 * Set the value for the data item with key "TransactionEndTimestamp"
	 * 
	 * @param id
	 */
	public void setTransactionEndTimestamp(Instant transactionEndTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		
		String formatedTime = sdf.format(Date.from(transactionEndTime));
		context.put(TRANSACTION_END_TIME_STAMP, formatedTime );
	}
	
	/**
	 * Get the value for the data item with key "TransactionEndTimestamp"
	 * @return current value, or 0 if not set
	 */
	public long getTransactionEndTimestamp() {
		return context.get(TRANSACTION_END_TIME_STAMP, 0);
	}
	
	/**
	 * Set the value for the data item with key "TransactionElapsedTime".
	 * An alternative to calling this method directly is to call
	 * <code>transactionStarted()</code> at the start of transaction
	 * processing and <code>transactionEnded()</code> at the end,
	 * which will compute the time difference in milliseconds
	 * and store the result as the "ns" value.
	 * 
	 * @param id
	 */
	
	public void setTransactionElapsedTime(Instant transactionEndTime) {
		long ns = Duration.between(transactionStartTime, transactionEndTime).toMillis();
		//String unit = " Seconds";
		//if(ns == 1){
			//unit = " Second";
		//}
		
		//if(ns < 1){
			//ns = Duration.between(transactionStartTime, transactionEndTime).toMillis();
			//unit = " milliseconds";
		//}
		context.put(TRANSACTION_ELAPSED_TIME, ns); // + unit);
	}

	/**
	 * Get the value for the data item with key "TransactionElapsedTime"
	 * @return current value, or 0 if not set
	 */
	public long getTransactionElapsedTime() {
		return context.get(TRANSACTION_ELAPSED_TIME, 0);
	}
	
	/**
	 * Set the value for the data item with key "MetricBeginTimestamp"
	 * 
	 * @param id
	 */
	public void setMetricBeginTimestamp(Instant metricStartTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		
		String formatedTime = sdf.format(Date.from(metricStartTime));
		context.put(METRIC_BEGIN_TIME_STAMP, formatedTime );
	}
	
	/**
	 * Get the value for the data item with key "MetricBeginTimestamp"
	 * @return current value, or 0 if not set
	 */
	public long getMetricBeginTimestamp() {
		return context.get(METRIC_BEGIN_TIME_STAMP, 0);
	}
	
	/**
	 * Set the value for the data item with key "MetricEndTimestamp"
	 * 
	 * @param id
	 */
	public void setMetricEndTimestamp(Instant metricEndTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
		
		String formatedTime = sdf.format(Date.from(metricEndTime));
		context.put(METRIC_END_TIME_STAMP, formatedTime );
	}
	
	/**
	 * Get the value for the data item with key "MetricEndTimestamp"
	 * @return current value, or 0 if not set
	 */
	public long getMetricEndTimestamp() {
		return context.get(METRIC_END_TIME_STAMP, 0);
	}
	
	/**
	 * Set the value for the data item with key "MetricElapsedTime".
	 * An alternative to calling this method directly is to call
	 * <code>metricStarted()</code> at the start of metric
	 * processing and <code>metricEnded()</code> at the end,
	 * which will compute the time difference in milliseconds
	 * and store the result as the "ns" value.
	 * 
	 * @param id
	 */
	
	public void setMetricElapsedTime(Instant metricEndTime) {
		long ns = Duration.between(metricStartTime, metricEndTime).toMillis();
		//String unit = " Seconds";
		//if(ns == 1){
			//unit = " Second";
		//}
		
		//if(ns < 1){
			//ns = Duration.between(metricStartTime, metricEndTime).toMillis();
			//unit = " milliseconds";
		//}
		context.put(METRIC_ELAPSED_TIME, ns); // + unit);
	}

	/**
	 * Get the value for the data item with key "MetricElapsedTime"
	 * @return current value, or 0 if not set
	 */
	public long getMetricElapsedTime() {
		return context.get(METRIC_ELAPSED_TIME, 0);
	}

}
