/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.pdp.common.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.onap.policy.pdp.common.enums.PdpHealthStatus;
import org.onap.policy.pdp.common.enums.PdpState;

/**
 * Class to represent the PDP_STATUS message that all the PDP's will send to PAP.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString
public class PdpStatus extends PdpMessage {

    public static final String PDP_STATUS_MESSAGE_NAME = "pdp_status";

    private String name;
    private String version;
    private String pdpType;
    private PdpState state;
    private PdpHealthStatus healthy;
    private String description;
    private String pdpGroup;
    private String pdpSubgroup;
    private List<String> supportedPolicyTypes;
    private List<Policy> policies;
    private String instance;
    private String deploymentInstanceInfo;
    private String properties;
    private PdpStatistics statistics;
    private PdpResponseDetails response;

    /**
     * Constructor for instantiating PdpStatus class with message name.
     *
     */
    public PdpStatus() {
        super(PDP_STATUS_MESSAGE_NAME);
    }
}
