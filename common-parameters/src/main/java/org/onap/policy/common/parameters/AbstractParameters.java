package org.onap.policy.common.parameters;
/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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

/**
 * This class defines an abstract parameter interface that acts as a base interface for all parameters in the ONAP
 * Policy Framework. All parameter POJOs are subclass of the abstract parameter class and can be used with the
 * {@link ParameterService}.
 *
 * @author Liam Fallon (liam.fallon@ericsson.com)
 */
public interface AbstractParameters {
    /**
     * Gets the parameter class.
     *
     * @return the parameter class
     */
    default Class<? extends AbstractParameters> getParameterClass() {
        return this.getClass();
    }

    /**
     * Gets the parameter class name.
     *
     * @return the parameter class name
     */
    default String getParameterClassName() {
        return this.getClass().getCanonicalName();
    }
}
