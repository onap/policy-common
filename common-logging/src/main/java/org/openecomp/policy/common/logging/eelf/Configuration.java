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


public interface Configuration  extends com.att.eelf.configuration.Configuration{
	
	 /**
	  * The Date-time of the start of a transaction
	  */
	 public String BEGIN_TIME_STAMP = "BeginTimestamp";
	 
	 /**
	  * The Date-time of the end of transaction
	  */
	 public String END_TIME_STAMP = "EndTimestamp";

	 /**
	  * Externally advertised API invoked by clients of this component
	  */
	public String SERVICE_NAME = "ServiceName";
	
	 /**
	  * Client or user invoking the API
	  */
	public String PARTNER_NAME = "PartnerName";
	
	/**
	 * Target Entity
	 */
    public String TARGET_ENTITY = "TargetEntity"; 
	
    /**
     * Target service name
     */
	public String TARGET_SERVICE_NAME = "TargetServiceName"; 
	
	 /**
	  * High level success or failure (COMPLETE or ERROR)
	  */
	public String STATUS_CODE = "StatusCode";
	
	 /**
	  * Application specific response code
	  */
	public String RESPONSE_CODE = "ResponseCode";
	
	 /**
	  * Human readable description of the application specific response code
	  */
	public String RESPONSE_DESCRIPTION = "ResponseDescription";
	
	 /**
	  * Externally advertised API invoked by clients of this component
	  */
	public String ELAPSED_TIME = "ElapsedTime";
	
	 /**
	  * High level failure (ERROR)
	  */
	public String ERROR_CATEGORY = "ErrorCategory";
	
	 /**
	  * Error Code
	  */
	public String ERROR_CODE = "ErrorCode";	
	
	 /**
	  * Error Description
	  */
	public String ERROR_DESCRIPTION = "ErrorDesciption";	
	
	 /**
	  * Class name
	  */
	public String CLASS_NAME = "ClassName";		
	
	 /**
	  * Server name
	  */
	public String SERVER_NAME = "ServerName";
	
}
