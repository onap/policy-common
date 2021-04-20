/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.parameters;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import org.onap.policy.common.parameters.ParameterGroupImpl;

/**
 * Class to hold all parameters needed for rest server.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
@org.onap.policy.common.parameters.annotations.NotNull
@org.onap.policy.common.parameters.annotations.NotBlank
@Getter
public class RestServerParameters extends ParameterGroupImpl {
    @NotBlank
    private String host;

    @org.onap.policy.common.parameters.annotations.Min(1)
    @Min(1)
    private int port;

    @NotBlank
    private String userName;

    @NotBlank
    private String password;

    private boolean https;
    private boolean aaf;

    public RestServerParameters() {
        super(RestServerParameters.class.getSimpleName());
    }
}
