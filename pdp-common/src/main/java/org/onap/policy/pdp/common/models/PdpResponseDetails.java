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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.onap.policy.pdp.common.enums.PdpResponseStatus;

/**
 * Class to represent PDP response details.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Getter
@Setter
@ToString
public class PdpResponseDetails {

    private String responseTo;
    private PdpResponseStatus responseStatus;
    private String responseMessage;
}
