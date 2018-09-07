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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterConstants;
import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.ValidationStatus;

public class TestParametersL10 implements ParameterGroup {
    private String name;
    private int l10IntField = 0;
    private String l10StringField = "Legal " + this.getClass().getCanonicalName();
    private TestParametersLGeneric l10LGenericNested0 = new TestParametersLGeneric("l10LGenericNested0");
    private TestParametersLGeneric l10LGenericNested1 = new TestParametersLGeneric("l10LGenericNested1");
    private Map<String, TestParametersLGeneric> l10LGenericNestedMap = new LinkedHashMap<>();

    /**
     * Default constructor.
     */
    public TestParametersL10() {
        // Default Constructor
    }
    
    /**
     * Create a test parameter group.
     * 
     * @param name the parameter group name
     */
    public TestParametersL10(final String name) {
        this.name = name;

        TestParametersLGeneric l10LGenericNestedMapVal0 = new TestParametersLGeneric("l10LGenericNestedMapVal0");
        l10LGenericNestedMap.put(l10LGenericNestedMapVal0.getName(), l10LGenericNestedMapVal0);
        TestParametersLGeneric l10LGenericNestedMapVal1 = new TestParametersLGeneric("l10LGenericNestedMapVal1");
        l10LGenericNestedMap.put(l10LGenericNestedMapVal1.getName(), l10LGenericNestedMapVal1);
    }

    public int getL10IntField() {
        return l10IntField;
    }

    public String getL10StringField() {
        return l10StringField;
    }

    public TestParametersLGeneric getL10LGenericNested0() {
        return l10LGenericNested0;
    }

    public TestParametersLGeneric getL10LGenericNested1() {
        return l10LGenericNested1;
    }

    public Map<String, TestParametersLGeneric> getL10LGenericNestedMap() {
        return l10LGenericNestedMap;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setL10IntField(int l10IntField) {
        this.l10IntField = l10IntField;
    }

    public void setL10StringField(String l10StringField) {
        this.l10StringField = l10StringField;
    }

    public void setL10LGenericNested0(TestParametersLGeneric l10lGenericNested0) {
        l10LGenericNested0 = l10lGenericNested0;
    }

    public void setL10LGenericNested1(TestParametersLGeneric l10lGenericNested1) {
        l10LGenericNested1 = l10lGenericNested1;
    }

    public void setL10LGenericNestedMap(Map<String, TestParametersLGeneric> l10lGenericNestedMap) {
        l10LGenericNestedMap = l10lGenericNestedMap;
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
                l10StringField = "Legal " + this.getClass().getCanonicalName();
                l10IntField = 0;
                break;
            case OBSERVATION:
                l10StringField = "aString";
                l10IntField = 2;
                break;
            case WARNING:
                l10StringField = "l10StringField";
                l10IntField = 3;
                break;
            case INVALID:
                l10StringField = "";
                l10IntField = -1;
                break;
            default:
                break;
        }

        l10LGenericNested0.triggerValidationStatus(triggerStatus, level);
        l10LGenericNested1.triggerValidationStatus(triggerStatus, level);

        for (TestParametersLGeneric nestedParameterGroup : l10LGenericNestedMap.values()) {
            nestedParameterGroup.triggerValidationStatus(triggerStatus, level);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public GroupValidationResult validate() {
        GroupValidationResult validationResult = new GroupValidationResult(this);

        if (l10StringField == null || l10StringField.trim().length() == 0) {
            validationResult.setResult("l10StringField", ValidationStatus.INVALID,
                            "l10StringField must be a non-blank string");
        } else if (l10StringField.equals("l10StringField")) {
            validationResult.setResult("l10StringField", ValidationStatus.WARNING,
                            "using the field name for the parameter value is dangerous");
        } else if (l10StringField.equals("aString")) {
            validationResult.setResult("l10StringField", ValidationStatus.OBSERVATION,
                            "this value for name is unhelpful");
        } else {
            validationResult.setResult("l10StringField", ValidationStatus.CLEAN,
                            ParameterConstants.PARAMETER_HAS_STATUS_MESSAGE + ValidationStatus.CLEAN.toString());
        }

        if (l10IntField < 0) {
            validationResult.setResult("l10IntField", ValidationStatus.INVALID,
                            "l10IntField must be a positive integer");
        } else if (l10IntField > 2) {
            validationResult.setResult("l10IntField", ValidationStatus.WARNING,
                            "values greater than 2 are not recommended");
        } else if (l10IntField == 2) {
            validationResult.setResult("l10IntField", ValidationStatus.OBSERVATION, "this field has been set to 2");
        } else {
            validationResult.setResult("l10IntField", ValidationStatus.CLEAN,
                            ParameterConstants.PARAMETER_HAS_STATUS_MESSAGE + ValidationStatus.CLEAN.toString());
        }

        if (l10LGenericNested0 != null) {
            validationResult.setResult("l10LGenericNested0", l10LGenericNested0.validate());
        }
        validationResult.setResult("l10LGenericNested1", l10LGenericNested1.validate());

        for (Entry<String, TestParametersLGeneric> nestedGroupEntry : l10LGenericNestedMap.entrySet()) {
            validationResult.setResult("l10LGenericNestedMap", nestedGroupEntry.getKey(),
                            nestedGroupEntry.getValue().validate());
        }

        return validationResult;
    }
}
