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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * 
 * EventTrackInfoHandler is the handler of clean up all expired event objcts
 *
 */
public class EventTrackInfoHandler extends TimerTask {

	String className = this.getClass().getSimpleName();

	@Override
	public void run() {

		PolicyLogger.info(className
				+ " Release expired event records start...");

		CleanUp();

		PolicyLogger.info(className + " Release expired event records done");
	}
    
	/**
	 * Removes all expired event objects from the ConcurrentHashMap of EventData
	 */
	private void CleanUp() {

		if (PolicyLogger.getEventTracker() == null
				|| PolicyLogger.getEventTracker().getEventInfo() == null
				|| PolicyLogger.getEventTracker().getEventInfo().isEmpty()) {
			return;
		}

		Instant startTime = null;
		long ns = 0;

		ArrayList<String> expiredEvents = null;

		for (String key: PolicyLogger.getEventTracker().getEventInfo().keySet()) {
			EventData event  = PolicyLogger.getEventTracker().getEventInfo().get(key);
			startTime = event.getStartTime();
			ns = Duration.between(startTime, Instant.now()).getSeconds();

			PolicyLogger.info(className
					+ " duration time : " + ns);

			PolicyLogger.info(className
					+ " PolicyLogger.EXPIRED_TIME : " + PolicyLogger.EXPIRED_TIME);	

			// if longer than EXPIRED_TIME, remove the object

			if (ns > PolicyLogger.EXPIRED_TIME){	
				if (expiredEvents == null) {
					expiredEvents = new ArrayList<String>();
				}
				expiredEvents.add(key);

				PolicyLogger.info(className
						+ " add expired event request ID: "
						+ event.getRequestID());
			}
		}
		
		synchronized (PolicyLogger.getEventTracker().getEventInfo()) {  
			if (expiredEvents != null) {
				for (String expiredKey : expiredEvents) {
					PolicyLogger.getEventTracker().getEventInfo()
							.remove(expiredKey);
					System.out.println(className
							+ " removed expired event request ID: "
							+ expiredKey);
					PolicyLogger.info(className
							+ " removed expired event request ID: "
							+ expiredKey);
				}
			}

		}

	}

}
