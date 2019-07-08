/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * EventData can be used for logging a rule event.
 */
@Getter
@Setter
public class EventData {

    private String requestId = null;
    private Instant startTime = null;
    private Instant endTime = null;

    // Default constructor takes no arguments.
    // Is empty because instance variables are assigned
    // their default values upon declaration.
    public EventData() {
        // See above comments for the reason this constructor is empty
    }

    /**
     * Create an instance.
     *
     * @param requestId the request ID
     * @param startTime the start time
     * @param endTime the end time
     */
    public EventData(String requestId, Instant startTime, Instant endTime) {
        this.requestId = requestId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    @Override
    public String toString() {
        return requestId + " Starting Time : " + this.startTime + " Ending Time : " + this.endTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            String otherRequestId = (String) obj;
            return requestId != null && requestId.equals(otherRequestId);
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EventData other = (EventData) obj;
        if (requestId == null) {
            if (other.requestId != null) {
                return false;
            }
        } else if (!requestId.equals(other.requestId)) {
            return false;
        }
        return true;
    }
}
