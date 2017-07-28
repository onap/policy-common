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

import java.util.UUID;

public class FlexLoggerTester {

	
	public void testLogging(){
		
		// get an instance of logger 
		Logger  logger = FlexLogger.getLogger(FlexLoggerTester.class);
		
		//logger.info("this is a testing of FlexLogger with logger type:" + FlexLogger.loggerType);
		
		logger.info("logger.isAuditEnabled():" + logger.isAuditEnabled());
		logger.info("logger.isDebugEnabled():" + logger.isDebugEnabled());
		logger.info("logger.isErrorEnabled():" + logger.isErrorEnabled());
		logger.info("logger.isInfoEnabled():" + logger.isInfoEnabled());
		logger.info("logger.isMetricsEnabled():" + logger.isMetricsEnabled());
		logger.info("logger.isWarnEnabled():" + logger.isWarnEnabled());
		
		if(logger.isDebugEnabled())
		    logger.debug("this is from logger.debug call");
		else
			logger.info("this is from logger.info call");

		if(logger.isMetricsEnabled()) logger.metrics("this is from logger.metrics call");
		
		logger.error("this is from logger.error call");
		if(logger.isAuditEnabled()) 
			logger.audit("this is from logger.audit call");
		else{
			logger.audit("shouldn't see this line in audit log");
			logger.info("shouldn't see this line in audit log");
		}
		
		if(logger.isMetricsEnabled()) 
			logger.metrics("this is from logger.metrics call");
		else{
			logger.metrics("shouldn't see this line in metrics log");
			logger.info("shouldn't see this line in metrics log");
		}
		
		if(logger.isErrorEnabled()) {
			logger.error("this is from logger.error call");
		}else{
			logger.error("shouldn't see this logger.error call in error.log");
			logger.info("error is not enabled");
		}
		
		logger.info("logger.isDebugEnabled() returned value:" + logger.isDebugEnabled());
		logger.recordAuditEventEnd("123345456464998", "from recordAuditEventEnd call", "12345");
		logger.recordAuditEventEnd(UUID.randomUUID(), "from recordAuditEventEnd call", "abcdf");
		logger.recordAuditEventStart("from recordAuditEventStart call");
		logger.recordAuditEventStart(UUID.randomUUID().toString());
		logger.recordMetricEvent("123345456464998", "from recordMetricEvent call");
		logger.recordMetricEvent(UUID.randomUUID(), "from recordMetricEvent call");
		logger.trace("from trace call");

	}
}
