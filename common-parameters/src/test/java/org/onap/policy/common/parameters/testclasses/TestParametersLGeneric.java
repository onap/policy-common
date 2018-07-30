/*-
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

package org.onap.policy.common.parameters.testclasses;

import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterConstants;
import org.onap.policy.common.parameters.ValidationStatus;

public class TestParametersLGeneric implements ParameterGroup {
    private String name;
    private int lgenericIntField = 0;
    private String lgenericStringField = "Legal " + this.getClass().getCanonicalName();
    
    /**
     * Default constructor
     */
    public TestParametersLGeneric() {
    }
    
    /**
     * Create a test parameter group.
     * 
     * @param name the parameter group name
     */
    public TestParametersLGeneric(final String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLgenericIntField(int lgenericIntField) {
        this.lgenericIntField = lgenericIntField;
    }

    public void setLgenericStringField(String lgenericStringField) {
        this.lgenericStringField = lgenericStringField;
    }

    /**
     * Trigger a validation message.
     * 
     * @param level Number of levels to recurse before stopping
     */
    public void triggerValidationStatus(final ValidationStatus triggerStatus, int level) {
        if (level == 0) {
            return;
        }
        else {
            level--;
        }

        switch (triggerStatus) {
            case CLEAN:
                lgenericStringField = "Legal " + this.getClass().getCanonicalName();
                lgenericIntField = 0;
                break;
            case OBSERVATION:
                lgenericStringField = "aString";
                lgenericIntField = 2;
                break;
            case WARNING:
                lgenericStringField = "lgenericStringField";
                lgenericIntField = 3;
                break;
            case INVALID:
                lgenericStringField = "";
                lgenericIntField = -1;
                break;
            default:
                break;
        }

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public GroupValidationResult validate() {
        GroupValidationResult validationResult = new GroupValidationResult(this);

        if (lgenericStringField == null || lgenericStringField.trim().length() == 0) {
            validationResult.setResult("lgenericStringField", ValidationStatus.INVALID,
                            "lgenericStringField must be a non-blank string");
        } else if (lgenericStringField.equals("lgenericStringField")) {
            validationResult.setResult("lgenericStringField", ValidationStatus.WARNING,
                            "using the field name for the parameter value is dangerous");
        } else if (lgenericStringField.equals("aString")) {
            validationResult.setResult("lgenericStringField", ValidationStatus.OBSERVATION,
                            "this value for name is unhelpful");
        } else {
            validationResult.setResult("lgenericStringField", ValidationStatus.CLEAN,
                            ParameterConstants.PARAMETER_HAS_STATUS_MESSAGE + ValidationStatus.CLEAN.toString());
        }

        if (lgenericIntField < 0) {
            validationResult.setResult("lgenericIntField", ValidationStatus.INVALID,
                            "lgenericIntField must be a positive integer");
        } else if (lgenericIntField > 2) {
            validationResult.setResult("lgenericIntField", ValidationStatus.WARNING,
                            "values greater than 2 are not recommended");
        } else if (lgenericIntField == 2) {
            validationResult.setResult("lgenericIntField", ValidationStatus.OBSERVATION,
                            "this field has been set to 2");
        } else {
            validationResult.setResult("lgenericIntField", ValidationStatus.CLEAN,
                            ParameterConstants.PARAMETER_HAS_STATUS_MESSAGE + ValidationStatus.CLEAN.toString());
        }

        return validationResult;
    }
}
