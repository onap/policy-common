/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.im;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateElement {
    private static final Logger logger = LoggerFactory.getLogger(StateElement.class);

    String adminState = null;
    String opState = null;
    String availStatus = null;
    String standbyStatus = null;
    String actionName = null;
    String endingAdminState = null;
    String endingOpState = null;
    String endingAvailStatus = null;
    String endingStandbyStatus = null;
    String exception = null;

    public StateElement() {
        // Empty constructor
    }

    public String getAdminState() {
        return this.adminState;
    }

    public void setAdminState(String adminState) {
        this.adminState = adminState;
    }

    public String getOpState() {
        return this.opState;
    }

    public void setOpState(String opState) {
        this.opState = opState;
    }

    public String getAvailStatus() {
        return this.availStatus;
    }

    public void setAvailStatus(String availStatus) {
        this.availStatus = availStatus;
    }

    public String getStandbyStatus() {
        return this.standbyStatus;
    }

    public void setStandbyStatus(String standbyStatus) {
        this.standbyStatus = standbyStatus;
    }

    public String getActionName() {
        return this.actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getEndingAdminState() {
        return this.endingAdminState;
    }

    public void setEndingAdminState(String endingAdminState) {
        this.endingAdminState = endingAdminState;
    }

    public String getEndingOpState() {
        return this.endingOpState;
    }

    public void setEndingOpState(String endingOpState) {
        this.endingOpState = endingOpState;
    }

    public String getEndingAvailStatus() {
        return this.endingAvailStatus;
    }

    public void setEndingAvailStatus(String endingAvailStatus) {
        this.endingAvailStatus = endingAvailStatus;
    }

    public String getEndingStandbyStatus() {
        return this.endingStandbyStatus;
    }

    public void setEndingStandbyStatus(String endingStandbyStatus) {
        this.endingStandbyStatus = endingStandbyStatus;
    }

    public String getException() {
        return this.exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * Display the state element.
     */
    public void displayStateElement() {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "adminState=[{}], opState=[{}], availStatus=[{}], standbyStatus=[{}], "
                            + "actionName=[{}], endingAdminState=[{}], endingOpState=[{}], "
                            + "endingAvailStatus=[{}], endingStandbyStatus=[{}], exception=[{}]",
                    getAdminState(), getOpState(), getAvailStatus(), getStandbyStatus(), getActionName(),
                    getEndingAdminState(), getEndingOpState(), getEndingAvailStatus(), getEndingStandbyStatus(),
                    getException());
        }
    }
}
