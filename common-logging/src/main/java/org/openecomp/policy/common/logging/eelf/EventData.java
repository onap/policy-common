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

import java.time.Instant;

import javax.swing.text.html.parser.Entity;

/**
 * 
 * EventData can be used for logging a rule event.
 *
 */
public class EventData {
	
	private String requestID = null;
	private Instant startTime = null;
	private Instant endTime = null;

	public EventData() {
		
	}
	
	public EventData(String requestID, Instant startTime, Instant endTime) {
		
		this.requestID = requestID;
		this.startTime = startTime;
		this.endTime = endTime;		
	}

	public String getRequestID() {
		return requestID;
	}

	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setStartTime(Instant startTime) {
		this.startTime = startTime;
	}

	public Instant getEndTime() {
		return endTime;
	}

	public void setEndTime(Instant endTime) {
		this.endTime = endTime;
	}
	
	public String toString(){
		return requestID + " Starting Time : " + this.startTime + " Ending Time : " + this.endTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((requestID == null) ? 0 : requestID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;	
		if (obj instanceof String) {
			String requestId = (String) obj;
			if(requestID != null && requestID.equals(requestId)){
				return true;
			}
			return false;
		}
		if (getClass() != obj.getClass())
			return false;
		EventData other = (EventData) obj;
		if (requestID == null) {
			if (other.requestID != null)
				return false;
		} else if (!requestID.equals(other.requestID))
			return false;
		return true;
	}
}
