/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
@NoArgsConstructor
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
