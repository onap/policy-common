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
 * This class defines an abstract parameter class that acts as a base class for all parameters in the ONAP Policy
 * Framework. The abstract parameter class holds the name of a subclass of this abstract parameter class
 * {@link AbstractParameters}. The class of the parameter class is checked at construction and on calls to the
 * {@link #getParameterClass()} method.
 *
 * @author Liam Fallon (liam.fallon@ericsson.com)
 */
public abstract class AbstractParameters {
    // The name of the parameter subclass
    private final Class<? extends AbstractParameters> parameterClass;

    /**
     * Constructor, creates a parameter class that must be a subclass of {@link AbstractParameters}.
     *
     * @param parameterClass the parameter class that is a subclass of this class
     */
    public AbstractParameters(final Class<? extends AbstractParameters> parameterClass) {
        // Ensure that this class is actually an instance of the passed parameter class
        if (!parameterClass.isAssignableFrom(this.getClass())) {
            throw new ParameterRuntimeException("class \"" + parameterClass.getCanonicalName()
                            + "\" is not an instance of \"" + this.getClass().getCanonicalName() + "\"");
        }
        
        this.parameterClass = parameterClass;
    }

    /**
     * Gets the parameter class.
     *
     * @return the parameter class
     */
   public final Class<? extends AbstractParameters> getParameterClass() {
        return parameterClass;
    }

    /**
     * Gets the parameter class name.
     *
     * @return the parameter class name
     */
    public final String getParameterClassName() {
        return parameterClass.getCanonicalName();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AbstractParameters [parameterClassName=" + parameterClass.getCanonicalName() + "]";
    }
}
