/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property.
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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterConstants;
import org.onap.policy.common.parameters.ParameterGroupImpl;
import org.onap.policy.common.parameters.ValidationStatus;

public class TestParametersL00 extends ParameterGroupImpl {
    private static final String L00_INT_FIELD = "l00IntField";
    private static final String L00_STRING_FIELD = "l00StringField";

    private static final String A_CONSTANT = "A Constant";

    private int l00IntField = 0;
    @NotNull
    private String l00StringField = "Legal " + this.getClass().getName();
    @Valid
    private TestParametersL10 l00L10Nested = new TestParametersL10("l00L10Nested");
    @Valid
    private TestParametersLGeneric l00LGenericNested = new TestParametersLGeneric("l00LGenericNested");
    @NotNull
    private Map<@NotNull String, @NotNull @Valid TestParametersLGeneric> l00LGenericNestedMap = new LinkedHashMap<>();
    private boolean isSomeFlag;
    private boolean someNonIsFlag;

    /**
     * Default constructor.
     */
    public TestParametersL00() {
        super(A_CONSTANT);
    }

    /**
     * Create a test parameter group.
     *
     * @param name the parameter group name
     */
    public TestParametersL00(final String name) {
        super(name);

        TestParametersLGeneric l00LGenericNestedMapVal0 = new TestParametersLGeneric("l00LGenericNestedMapVal0");
        l00LGenericNestedMap.put(l00LGenericNestedMapVal0.getName(), l00LGenericNestedMapVal0);
        TestParametersLGeneric l00LGenericNestedMapVal1 = new TestParametersLGeneric("l00LGenericNestedMapVal1");
        l00LGenericNestedMap.put(l00LGenericNestedMapVal1.getName(), l00LGenericNestedMapVal1);
    }

    public int getL00IntField() {
        return l00IntField;
    }

    public String getL00StringField() {
        return l00StringField;
    }

    public TestParametersL10 getL00L10Nested() {
        return l00L10Nested;
    }

    public TestParametersLGeneric getL00LGenericNested() {
        return l00LGenericNested;
    }

    public Map<String, TestParametersLGeneric> getL00LGenericNestedMap() {
        return l00LGenericNestedMap;
    }

    public boolean isSomeFlag() {
        return isSomeFlag;
    }

    public boolean isSomeNonIsFlag() {
        return someNonIsFlag;
    }

    public void setSomeFlag(boolean isSomeFlag) {
        this.isSomeFlag = isSomeFlag;
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
                l00StringField = "Legal " + this.getClass().getName();
                l00IntField = 0;
                break;
            case OBSERVATION:
                l00StringField = "aString";
                l00IntField = 2;
                break;
            case WARNING:
                l00StringField = L00_STRING_FIELD;
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
    public GroupValidationResult validate() {
        GroupValidationResult validationResult = super.validate();

        if (StringUtils.isBlank(getName())) {
            validationResult.setResult("name", ValidationStatus.INVALID, "name must be a non-blank string");
        }

        if (StringUtils.isBlank(l00StringField)) {
            validationResult.setResult(L00_STRING_FIELD, ValidationStatus.INVALID,
                            "l00StringField must be a non-blank string");
        } else if (l00StringField.equals(L00_STRING_FIELD)) {
            validationResult.setResult(L00_STRING_FIELD, ValidationStatus.WARNING,
                            "using the field name for the parameter value is dangerous");
        } else if (l00StringField.equals("aString")) {
            validationResult.setResult(L00_STRING_FIELD, ValidationStatus.OBSERVATION,
                            "this value for name is unhelpful");
        } else {
            validationResult.setResult(L00_STRING_FIELD, ValidationStatus.CLEAN,
                            ParameterConstants.PARAMETER_HAS_STATUS_MESSAGE + ValidationStatus.CLEAN.toString());
        }

        if (l00IntField < 0) {
            validationResult.setResult(L00_INT_FIELD, ValidationStatus.INVALID,
                            "l00IntField must be a positive integer");
        } else if (l00IntField > 2) {
            validationResult.setResult(L00_INT_FIELD, ValidationStatus.WARNING,
                            "values greater than 2 are not recommended");
        } else if (l00IntField == 2) {
            validationResult.setResult(L00_INT_FIELD, ValidationStatus.OBSERVATION, "this field has been set to 2");
        } else {
            validationResult.setResult(L00_INT_FIELD, ValidationStatus.CLEAN,
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
