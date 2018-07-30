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

package org.onap.policy.common.parameters.testclasses;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterConstants;
import org.onap.policy.common.parameters.ValidationStatus;

public class TestParametersL00 implements ParameterGroup {
    private String name;
    private int l00IntField = 0;
    private String l00StringField = "Legal " + this.getClass().getCanonicalName();
    private TestParametersL10 l00L10Nested = new TestParametersL10("l00L10Nested");
    private TestParametersLGeneric l00LGenericNested = new TestParametersLGeneric("l00LGenericNested");
    private Map<String, TestParametersLGeneric> l00LGenericNestedMap = new LinkedHashMap<>();

    /**
     * Default constructor
     */
    public TestParametersL00() {
    }
    
    /**
     * Create a test parameter group.
     * 
     * @param name the parameter group name
     */
    public TestParametersL00(final String name) {
        this.name = name;

        TestParametersLGeneric l00LGenericNestedMapVal0 = new TestParametersLGeneric("l00LGenericNestedMapVal0");
        l00LGenericNestedMap.put(l00LGenericNestedMapVal0.getName(), l00LGenericNestedMapVal0);
        TestParametersLGeneric l00LGenericNestedMapVal1 = new TestParametersLGeneric("l00LGenericNestedMapVal1");
        l00LGenericNestedMap.put(l00LGenericNestedMapVal1.getName(), l00LGenericNestedMapVal1);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setL00IntField(int l00IntField) {
        this.l00IntField = l00IntField;
    }

    public void setL00StringField(String l00StringField) {
        this.l00StringField = l00StringField;
    }

    public void setL00L10Nested(TestParametersL10 l00l10Nested) {
        l00L10Nested = l00l10Nested;
    }

    public void setL00LGenericNested(TestParametersLGeneric l00lGenericNested) {
        l00LGenericNested = l00lGenericNested;
    }

    public void setL00LGenericNestedMap(Map<String, TestParametersLGeneric> l00lGenericNestedMap) {
        l00LGenericNestedMap = l00lGenericNestedMap;
    }

    /**
     * Trigger a validation message.
     * 
     * @param triggerStatus Validation status to trigger
     * @param level Number of levels to recurse before stopping
     */
    public void triggerValidationStatus(final ValidationStatus triggerStatus, int level) {
        if (level == 0) {
            return;
        } else {
            level--;
        }

        switch (triggerStatus) {
            case CLEAN:
                l00StringField = "Legal " + this.getClass().getCanonicalName();
                l00IntField = 0;
                break;
            case OBSERVATION:
                l00StringField = "aString";
                l00IntField = 2;
                break;
            case WARNING:
                l00StringField = "l00StringField";
                l00IntField = 3;
                break;
            case INVALID:
                l00StringField = "";
                l00IntField = -1;
                break;
            default:
                break;
        }

        l00L10Nested.triggerValidationStatus(triggerStatus, level);
        l00LGenericNested.triggerValidationStatus(triggerStatus, level);

        for (TestParametersLGeneric nestedParameterGroup : l00LGenericNestedMap.values()) {
            nestedParameterGroup.triggerValidationStatus(triggerStatus, level);
        }

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GroupValidationResult validate() {
        GroupValidationResult validationResult = new GroupValidationResult(this);

        if (name == null || name.trim().length() == 0) {
            validationResult.setResult("name", ValidationStatus.INVALID, "name must be a non-blank string");
        }

        if (l00StringField == null || l00StringField.trim().length() == 0) {
            validationResult.setResult("l00StringField", ValidationStatus.INVALID,
                            "l00StringField must be a non-blank string");
        } else if (l00StringField.equals("l00StringField")) {
            validationResult.setResult("l00StringField", ValidationStatus.WARNING,
                            "using the field name for the parameter value is dangerous");
        } else if (l00StringField.equals("aString")) {
            validationResult.setResult("l00StringField", ValidationStatus.OBSERVATION,
                            "this value for name is unhelpful");
        } else {
            validationResult.setResult("l00StringField", ValidationStatus.CLEAN,
                            ParameterConstants.PARAMETER_HAS_STATUS_MESSAGE + ValidationStatus.CLEAN.toString());
        }

        if (l00IntField < 0) {
            validationResult.setResult("l00IntField", ValidationStatus.INVALID,
                            "l00IntField must be a positive integer");
        } else if (l00IntField > 2) {
            validationResult.setResult("l00IntField", ValidationStatus.WARNING,
                            "values greater than 2 are not recommended");
        } else if (l00IntField == 2) {
            validationResult.setResult("l00IntField", ValidationStatus.OBSERVATION, "this field has been set to 2");
        } else {
            validationResult.setResult("l00IntField", ValidationStatus.CLEAN,
                            ParameterConstants.PARAMETER_HAS_STATUS_MESSAGE + ValidationStatus.CLEAN.toString());
        }

        validationResult.setResult("l00L10Nested", l00L10Nested.validate());
        validationResult.setResult("l00LGenericNested", l00LGenericNested.validate());

        for (Entry<String, TestParametersLGeneric> nestedGroupEntry : l00LGenericNestedMap.entrySet()) {
            validationResult.setResult("l00LGenericNestedMap", nestedGroupEntry.getKey(),
                            nestedGroupEntry.getValue().validate());
        }

        return validationResult;
    }
}
