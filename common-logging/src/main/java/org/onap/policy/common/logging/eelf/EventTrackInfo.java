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

package org.onap.policy.common.logging.eelf;

import java.io.IOException;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.onap.policy.common.logging.flexlogger.PropertyUtil;

/**
 * 
 * EventTrackInfo contains a ConcurrentHashMap of EventData
 *
 */
public class EventTrackInfo {

    private ConcurrentHashMap<String, EventData> eventInfo = null;    
       
	public EventTrackInfo() {
		/*
		 * An initial capacity of 16 ensures the number of elements before resizing happens
		 * Load factor of 0,9 ensures a dense packaging inside ConcurrentHashMap which will optimize memory use
		 * ConcurencyLevel set to 1 will ensure that only one shard is created and maintained 
		 */
		eventInfo = new ConcurrentHashMap<String, EventData>(16, 0.9f, 1);	
	}
   
	/**
	 * Returns an instance of EventData associated to this requestID
	 * @param requestID
	 * @return EventData
	 */
	public EventData getEventDataByRequestID(String requestID){
		return eventInfo.get(requestID);
	}
	
	/**
	 * Stores an EventData object in a ConcurrentHashMap using its requestID as key.
	 * @param event
	 */
	public void storeEventData(EventData event){
		
		if(event == null || event.getRequestID() == null || event.getRequestID().isEmpty()){
			return;
		}
		//in case override the start time, check the original event was already stored or not 
		if(!eventInfo.containsKey(event.getRequestID())){
			eventInfo.put(event.getRequestID(), event);		
		}
	}
	
	/**
	 * Removes an EventData object from a ConcurrentHashMap using the eventId as key. 
	 * @param eventId
	 */
	public void remove(String eventId){
		if(eventInfo != null){
			eventInfo.remove(eventId);
		}
	}
	
	/**
	 * Returns a ConcurrentHashMap of EventData
	 */
	public ConcurrentHashMap<String, EventData> getEventInfo() {
		return eventInfo;
	}

}
