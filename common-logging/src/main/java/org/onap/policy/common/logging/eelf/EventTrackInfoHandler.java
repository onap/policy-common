/*
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

package org.onap.policy.common.logging.eelf;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;

/**
 * EventTrackInfoHandler is the handler of clean up all expired event objects.
 */
public class EventTrackInfoHandler extends TimerTask {

    String className = this.getClass().getSimpleName();

    @Override
    public void run() {

        PolicyLogger.info(className + " Release expired event records start...");

        cleanUp();

        PolicyLogger.info(className + " Release expired event records done");
    }

    /**
     * Removes all expired event objects from the ConcurrentHashMap of EventData.
     */
    private void cleanUp() {

        var eventTrackInfo = PolicyLogger.getEventTracker();
        if (eventTrackInfo == null) {
            return;
        }
        ConcurrentMap<String, EventData> eventInfo = eventTrackInfo.getEventInfo();
        if (eventInfo == null || eventInfo.isEmpty()) {
            return;
        }

        Instant startTime;
        long ns;

        ArrayList<String> expiredEvents = null;

        for (Map.Entry<String, EventData> entry : eventInfo.entrySet()) {
            EventData event = entry.getValue();
            startTime = event.getStartTime();
            ns = Duration.between(startTime, Instant.now()).getSeconds();

            PolicyLogger.info(className + " duration time : " + ns);

            PolicyLogger.info(className + " PolicyLogger.EXPIRED_TIME : " + PolicyLogger.expiredTime);

            // if longer than EXPIRED_TIME, remove the object

            if (ns > PolicyLogger.expiredTime) {
                if (expiredEvents == null) {
                    expiredEvents = new ArrayList<>();
                }
                expiredEvents.add(entry.getKey());

                PolicyLogger.info(className + " add expired event request ID: " + event.getRequestId());
            }
        }

        synchronized (eventInfo) {
            if (expiredEvents != null) {
                for (String expiredKey : expiredEvents) {
                    eventInfo.remove(expiredKey);
                    PolicyLogger.info(className + " removed expired event request ID: " + expiredKey);
                }
            }
        }
    }
}
