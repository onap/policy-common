/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Bell Canada. All rights reserved.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.resources;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Common messages to be used by all components.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageConstants {

    public static final String POLICY_API = "policy-api";
    public static final String POLICY_PAP = "policy-pap";
    public static final String POLICY_APEX_PDP = "policy-apex-pdp";
    public static final String POLICY_DROOLS_PDP = "policy-drools-pdp";
    public static final String POLICY_XACML_PDP = "policy-xacml-pdp";
    public static final String POLICY_DISTRIBUTION = "policy-distribution";
    public static final String POLICY_CLAMP = "policy-clamp";

    public static final String START_SUCCESS_MSG = "Started %s service successfully.";
    public static final String START_FAILURE_MSG = "Start of %s service failed.";
}
