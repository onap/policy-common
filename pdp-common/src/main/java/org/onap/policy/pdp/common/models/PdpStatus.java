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
@ToString
public class PdpStatus {

    @Getter
    private String messageName = "pdp_status";
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String version;
    @Getter
    @Setter
    private String pdpType;
    @Getter
    @Setter
    private PdpState state;
    @Getter
    @Setter
    private PdpHealthStatus healthy;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String pdpGroup;
    @Getter
    @Setter
    private String pdpSubgroup;
    @Getter
    @Setter
    private List<String> supportedPolicyTypes;
    @Getter
    @Setter
    private List<Policy> policies;
    @Getter
    @Setter
    private String instance;
    @Getter
    @Setter
    private String deploymentInstanceInfo;
    @Getter
    @Setter
    private String properties;
    @Getter
    @Setter
    private PdpStatistics statistics;
    @Getter
    @Setter
    private PdpResponseDetails response;
}
