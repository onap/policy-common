/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.logging;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import org.onap.policy.common.logging.nsa.LoggingContextFactory;
import org.onap.policy.common.logging.nsa.SharedLoggingContext;
import org.slf4j.MDC;

/**
 * A facade over the org.onap.policy.common.logging.nsa.SharedLoggingContext
 * interface/implementation that makes it more convenient to use. SharedLoggingContext builds on the
 * SLF4J/log4j Mapped Diagnostic Context (MDC) feature, which provides a hashmap-based context for
 * data items that need to be logged, where the hashmap is kept in ThreadLocal storage. The data
 * items can be referenced in the log4j configuration using the EnhancedPatternLayout appender
 * layout class, and the notation "%X{key}" in the ConversionPattern string, where "key" is one of
 * the keys in the MDC hashmap (which is determined by what hashmap entries the application code
 * creates). Example: log4j.appender.auditAppender.layout=org.apache.log4j.EnhancedPatternLayout
 * log4j.appender.auditAppender.layout.ConversionPattern=%d|%X{requestId}|%X{serviceInstanceId}|...|%m%n
 * where "requestId" and "serviceInstanceId" are entries in the MDC hashmap.
 *
 * <p>The notable functionality the SharedLoggingContext adds over MDC is that it maintains its own
 * copy of the MDC data items in a hashmap (not in ThreadLocal storage), which allows more control
 * of the data. The ONAPLoggingContext constructor that takes another ONAPLoggingContext object as a
 * parameter makes use of this feature. For example if there is a thread pulling requests from a
 * queue for processing, it can keep a base logging context with data that is common to all
 * requests, and for each request, create a new logging context with that base context as a
 * parameter; this will create a new logging context with those initial values and none of the
 * request-specific values from the previous request such as a request ID.
 *
 * <p>The setter methods in this class set corresponding items in the SharedLoggingContext/MDC
 * hashmaps. These items correspond to the fields defined in the "ONAP platform application logging
 * guidelines" document. In addition, there is a pair of convenience functions, transactionStarted()
 * and transactionEnded(), that can be called at the beginning and end of transaction processing to
 * calculate the duration of the transaction and record that value in the "timer" item.
 *
 */
public class OnapLoggingContext {

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
    private static final String POLICY = "POLICY";
    private static final String USER = "USER";
    private static final String COMPLETE = "COMPLETE";
    private static final String PE_PROCESS = "PE Process";
    private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS+00:00";
    private static final String EMPTY_STRING = "";
    private static final int DEFAULT_VALUE = 0;


    private static LoggingContextFactory.Builder loggingContextBuilder = new LoggingContextFactory.Builder();

    protected SharedLoggingContext context = null;
    private Instant transactionStartTime;
    private Instant metricStartTime;

    /**
     * Create a new ONAPLoggingContext with no base context.
     */
    public OnapLoggingContext() {
        context = (SharedLoggingContext) loggingContextBuilder.forSharing().build();
    }

    /**
     * Create a new ONAPLoggingContext initially populated with the values in the given base
     * context. This can be used for example in a servlet where each incoming request may be
     * processed by a separate thread, but there may be some logging data items that will be
     * unchanging and common to all the threads. That constant data can be populated in a base
     * context, and then each request handler creates new context passing that base context to
     * populate the common data in the new one.
     *
     * @param baseContext onap logging context
     */
    public OnapLoggingContext(OnapLoggingContext baseContext) {
        context = (SharedLoggingContext) loggingContextBuilder.forSharing().build();
        // a logging context could be passed into a thread (e.g. one that is servicing a queue)
        // that already had a logging context established, so the MDC hashmap could contain
        // entries that are no longer appropriate; so clear out the MDC hashmap before
        // transferring the new logging context values.
        // x
        MDC.clear();
        baseContext.context.transferTo(context);
        transactionStartTime = baseContext.transactionStartTime;
        setServiceName(POLICY);
        setPartnerName(USER);
        setStatusCode(COMPLETE);
        setTargetEntity(POLICY);
        setTargetServiceName(PE_PROCESS);
    }

    /**
     * Indicate the start of transaction processing. The current system time will be recorded for
     * use by <code>transactionEnded()</code> to calculate the duration of the transaction.
     */
    public void transactionStarted() {
        transactionStartTime = Instant.now();
        setTransactionBeginTimestamp(transactionStartTime);
    }

    /**
     * Indicate the end of transaction processing. The difference between the current system time
     * and the time recorded by <code>transactionStarted()</code> will be recorded in the data item
     * with key "TransactionElapsedTime".
     */
    public void transactionEnded() {
        var transactionEndTime = Instant.now();
        setTransactionEndTimestamp(transactionEndTime);
        setTransactionElapsedTime(transactionEndTime);
    }

    /**
     * Indicate the start of metric processing. The current system time will be recorded for use by
     * <code>metricEnded()</code> to calculate the duration of the metric.
     */
    public void metricStarted() {
        metricStartTime = Instant.now();
        setMetricBeginTimestamp(metricStartTime);
    }

    /**
     * Indicate the end of metric processing. The difference between the current system time and the
     * time recorded by <code>metricStarted()</code> will be recorded in the data item with key
     * "MetricElapsedTime".
     */
    public void metricEnded() {
        var metricEndTime = Instant.now();
        setMetricEndTimestamp(metricEndTime);
        setMetricElapsedTime(metricEndTime);
    }

    /**
     * Set the value for the data item with key "requestId".
     *
     * @param id request identifier
     */
    public void setRequestId(String id) {
        context.put(REQUEST_ID, id);
    }

    /**
     * Get the value for the data item with key "requestId".
     *
     * @return current value, or empty string if not set
     */
    public String getRequestId() {
        return context.get(REQUEST_ID, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "serviceInstanceId".
     *
     * @param id service identifier
     */
    public void setServiceInstanceId(String id) {
        context.put(SERVICE_INSTANCE_ID, id);
    }

    /**
     * Get the value for the data item with key "serviceInstanceId".
     *
     * @return current value, or empty string if not set
     */
    public String getServiceInstanceId() {
        return context.get(SERVICE_INSTANCE_ID, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "threadId". An alternative to using this item is to
     * use the %t conversion character in the appender's conversion string.
     *
     * @param id thread identifier
     */
    public void setThreadId(String id) {
        context.put(THREAD_ID, id);
    }

    /**
     * Get the value for the data item with key "threadId".
     *
     * @return current value, or empty string if not set
     */
    public String getThreadId() {
        return context.get(THREAD_ID, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "serverName".
     *
     * @param name server name
     */
    public void setServerName(String name) {
        context.put(SERVER_NAME, name);
    }

    /**
     * Get the value for the data item with key "serverName".
     *
     * @return current value, or empty string if not set
     */
    public String getServerName() {
        return context.get(SERVER_NAME, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "serviceName".
     *
     * @param name service name
     */
    public void setServiceName(String name) {
        context.put(SERVICE_NAME, name);
    }

    /**
     * Get the value for the data item with key "serviceName".
     *
     * @return current value, or empty string if not set
     */
    public String getServiceName() {
        return context.get(SERVICE_NAME, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "partnerName".
     *
     * @param name partner name
     */
    public void setPartnerName(String name) {
        context.put(PARTNER_NAME, name);
    }

    /**
     * Get the value for the data item with key "partnerName".
     *
     * @return current value, or empty string if not set
     */
    public String getPartnerName() {
        return context.get(PARTNER_NAME, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "statusCode".
     *
     * @param name status code
     */
    public void setStatusCode(String name) {
        context.put(STATUS_CODE, name);
    }

    /**
     * Get the value for the data item with key "statusCode".
     *
     * @return current value, or empty string if not set
     */
    public String getStatusCode() {
        return context.get(STATUS_CODE, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "targetEntity".
     *
     * @param name target entity
     */
    public void setTargetEntity(String name) {
        context.put(TARGET_ENTITY, name);
    }

    /**
     * Get the value for the data item with key "targetEntity".
     *
     * @return current value, or empty string if not set
     */
    public String getTargetEntity() {
        return context.get(TARGET_ENTITY, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "targetServiceName".
     *
     * @param name target service name
     */
    public void setTargetServiceName(String name) {
        context.put(TARGET_SERVICE_NAME, name);
    }

    /**
     * Get the value for the data item with key "targetServiceName".
     *
     * @return current value, or empty string if not set
     */
    public String getTargetServiceName() {
        return context.get(TARGET_SERVICE_NAME, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "instanceUuid".
     *
     * @param uuid instance uuid
     */
    public void setInstanceUuid(String uuid) {
        context.put(INSTANCE_UUID, uuid);
    }

    /**
     * Get the value for the data item with key "instanceUuid".
     *
     * @return current value, or empty string if not set
     */
    public String getInstanceUuid() {
        return context.get(INSTANCE_UUID, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "severity".
     *
     * @param severity severity
     */
    public void setSeverity(Long severity) {
        context.put(SEVERITY, severity);
    }

    /**
     * Get the value for the data item with key "severity".
     *
     * @return current value, or empty string if not set
     */
    public String getSeverity() {
        return context.get(SEVERITY, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "serverIp".
     *
     * @param serverIp server ip address
     */
    public void setServerIpAddress(String serverIp) {
        context.put(SERVER_IP_ADDRESS, serverIp);
    }

    /**
     * Get the value for the data item with key "serverIp".
     *
     * @return current value, or empty string if not set
     */
    public String getServerIpAddress() {
        return context.get(SERVER_IP_ADDRESS, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "server".
     *
     * @param server server
     */
    public void setServer(String server) {
        context.put(SERVER, server);
    }

    /**
     * Get the value for the data item with key "server".
     *
     * @return current value, or empty string if not set
     */
    public String getServer() {
        return context.get(SERVER, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "clientIp".
     *
     * @param clientIp client ip address
     */
    public void setClientIpAddress(String clientIp) {
        context.put(CLIENT_IP_ADDRESS, clientIp);
    }

    /**
     * Get the value for the data item with key "clientIp".
     *
     * @return current value, or empty string if not set
     */
    public String getClientIpAddress() {
        return context.get(CLIENT_IP_ADDRESS, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "classname". Use of this item is not recommended
     * (unless it is used to indicate something other than the Java classname) since it would be
     * unwieldy to maintain a correct value across calls to/returns from methods in other classes.
     * Use of the PatternLayout %c conversion character in the conversion string will give a more
     * reliable value.
     *
     * @param classname class name
     */
    public void setClassname(String classname) {
        context.put(CLASSNAME, classname);
    }

    /**
     * Get the value for the data item with key "classname".
     *
     * @return current value, or empty string if not set
     */
    public String getClassname() {
        return context.get(CLASSNAME, EMPTY_STRING);
    }

    /**
     * Set the value for the data item with key "TransactionBeginTimestamp".
     *
     * @param transactionStartTime transaction start time
     */
    public void setTransactionBeginTimestamp(Instant transactionStartTime) {
        var sdf = new SimpleDateFormat(TIME_FORMAT);
        context.put(TRANSACTION_BEGIN_TIME_STAMP, sdf.format(Date.from(transactionStartTime)));
    }

    /**
     * Get the value for the data item with key "TransactionBeginTimestamp".
     *
     * @return current value, or 0 if not set
     */
    public long getTransactionBeginTimestamp() {
        return context.get(TRANSACTION_BEGIN_TIME_STAMP, DEFAULT_VALUE);
    }

    /**
     * Set the value for the data item with key "TransactionEndTimestamp".
     *
     * @param transactionEndTime transaction end time
     */
    public void setTransactionEndTimestamp(Instant transactionEndTime) {
        var sdf = new SimpleDateFormat(TIME_FORMAT);
        context.put(TRANSACTION_END_TIME_STAMP, sdf.format(Date.from(transactionEndTime)));
    }

    /**
     * Get the value for the data item with key "TransactionEndTimestamp".
     *
     * @return current value, or 0 if not set
     */
    public long getTransactionEndTimestamp() {
        return context.get(TRANSACTION_END_TIME_STAMP, DEFAULT_VALUE);
    }

    /**
     * Set the value for the data item with key "TransactionElapsedTime". An alternative to calling
     * this method directly is to call <code>transactionStarted()</code> at the start of transaction
     * processing and <code>transactionEnded()</code> at the end, which will compute the time
     * difference in milliseconds and store the result as the "ns" value.
     *
     * @param transactionEndTime transaction end time
     */

    public void setTransactionElapsedTime(Instant transactionEndTime) {
        long ns = Duration.between(transactionStartTime, transactionEndTime).toMillis();
        context.put(TRANSACTION_ELAPSED_TIME, ns);
    }

    /**
     * Get the value for the data item with key "TransactionElapsedTime".
     *
     * @return current value, or 0 if not set
     */
    public long getTransactionElapsedTime() {
        return context.get(TRANSACTION_ELAPSED_TIME, DEFAULT_VALUE);
    }

    /**
     * Set the value for the data item with key "MetricBeginTimestamp".
     *
     * @param metricStartTime metric start time
     */
    public void setMetricBeginTimestamp(Instant metricStartTime) {
        var sdf = new SimpleDateFormat(TIME_FORMAT);
        context.put(METRIC_BEGIN_TIME_STAMP, sdf.format(Date.from(metricStartTime)));
    }

    /**
     * Get the value for the data item with key "MetricBeginTimestamp".
     *
     * @return current value, or 0 if not set
     */
    public long getMetricBeginTimestamp() {
        return context.get(METRIC_BEGIN_TIME_STAMP, DEFAULT_VALUE);
    }

    /**
     * Set the value for the data item with key "MetricEndTimestamp".
     *
     * @param metricEndTime metric end time
     */
    public void setMetricEndTimestamp(Instant metricEndTime) {
        var sdf = new SimpleDateFormat(TIME_FORMAT);
        context.put(METRIC_END_TIME_STAMP, sdf.format(Date.from(metricEndTime)));
    }

    /**
     * Get the value for the data item with key "MetricEndTimestamp".
     *
     * @return current value, or 0 if not set
     */
    public long getMetricEndTimestamp() {
        return context.get(METRIC_END_TIME_STAMP, DEFAULT_VALUE);
    }

    /**
     * Set the value for the data item with key "MetricElapsedTime". An alternative to calling this
     * method directly is to call <code>metricStarted()</code> at the start of metric processing and
     * <code>metricEnded()</code> at the end, which will compute the time difference in milliseconds
     * and store the result as the "ns" value.
     *
     * @param metricEndTime metric end time
     */

    public void setMetricElapsedTime(Instant metricEndTime) {
        long ns = Duration.between(metricStartTime, metricEndTime).toMillis();
        context.put(METRIC_ELAPSED_TIME, ns);
    }

    /**
     * Get the value for the data item with key "MetricElapsedTime".
     *
     * @return current value, or 0 if not set
     */
    public long getMetricElapsedTime() {
        return context.get(METRIC_ELAPSED_TIME, DEFAULT_VALUE);
    }
}
