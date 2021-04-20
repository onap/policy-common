/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.parameters;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Implementation of a parameter group.
 */
@Getter
@Setter
public class ParameterGroupImpl implements ParameterGroup {
    /**
     * Group name. Note: this MUST not be "private" or it will not be validated.
     */
    @NotNull
    @NotBlank
    protected String name;

    /**
     * Constructs the object, with a {@code null} name.
     */
    public ParameterGroupImpl() {
        this.name = null;
    }

    /**
     * Constructs the object.
     *
     * @param name the group's name
     */
    public ParameterGroupImpl(String name) {
        this.name = name;
    }

    @Override
    public GroupValidationResult validate() {
        return new GroupValidationResult(this);
    }
}
