/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterGroupImpl;
import org.onap.policy.common.parameters.ValidationStatus;

/**
 * Class to hold all parameters needed for topic properties.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
@Getter
@Setter
public class TopicParameterGroup extends ParameterGroupImpl {
    @NotNull
    private List<@NotNull @Valid @VerifyTopicParameter TopicParameters> topicSources;
    @NotNull
    private List<@NotNull @Valid @VerifyTopicParameter TopicParameters> topicSinks;

    /**
     * Constructs the object.
     */
    public TopicParameterGroup() {
        super(TopicParameterGroup.class.getSimpleName());

        this.topicSources = List.of();
        this.topicSinks = List.of();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public GroupValidationResult validate() {
        GroupValidationResult result = super.validate();
        if (result.isValid()) {
            StringBuilder errorMsg = new StringBuilder();
            StringBuilder missingSourceParams = checkMissingMandatoryParams(topicSources);
            if (missingSourceParams.length() > 0) {
                errorMsg.append(missingSourceParams.append("missing in topicSources. "));
            }
            StringBuilder missingSinkParams = checkMissingMandatoryParams(topicSinks);
            if (missingSinkParams.length() > 0) {
                errorMsg.append(missingSinkParams.append("missing in topicSinks."));
            }

            if (errorMsg.length() > 0) {
                errorMsg.insert(0, "Mandatory parameters are missing. ");
                result.setResult(ValidationStatus.INVALID, errorMsg.toString());
            }
        }
        return result;
    }

    private StringBuilder checkMissingMandatoryParams(List<TopicParameters> topicParametersList) {
        StringBuilder missingParams = new StringBuilder();
        for (TopicParameters topicParameters : topicParametersList) {
            if (StringUtils.isBlank(topicParameters.getTopic())) {
                missingParams.append("topic, ");
            }
            if (StringUtils.isBlank(topicParameters.getTopicCommInfrastructure())) {
                missingParams.append("topicCommInfrastructure, ");
            }
            if (null == topicParameters.getServers() || topicParameters.getServers().isEmpty()) {
                missingParams.append("servers, ");
            }
        }
        return missingParams;
    }

    /**
     * Annotation needed to trigger validation of topic parameters.
     */
    @Retention(RUNTIME)
    @Target(TYPE_USE)
    @Constraint(validatedBy = {TopicParameterValidator.class})
    public static @interface VerifyTopicParameter {

        /**
         * The error message.
         */
        String message() default "no message";

        /**
         * The groups.
         */
        Class<?>[] groups() default {};

        /**
         * The payload.
         */
        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Validator for a single set of topic parameters. Validates additional constraints
     * other than those that are normally validated.
     */
    public static class TopicParameterValidator implements ConstraintValidator<VerifyTopicParameter, TopicParameters> {

        @Override
        public boolean isValid(TopicParameters topicParameters, ConstraintValidatorContext context) {
            context.disableDefaultConstraintViolation();
            boolean valid = true;
            if (StringUtils.isBlank(topicParameters.getTopic())) {
                valid = false;
                context.buildConstraintViolationWithTemplate("must not be blank").addPropertyNode("topic")
                                .addConstraintViolation();
            }
            if (null == topicParameters.getServers() || topicParameters.getServers().isEmpty()) {
                valid = false;
                context.buildConstraintViolationWithTemplate("must not be empty").addPropertyNode("servers")
                                .addConstraintViolation();
            }

            return valid;
        }
    }
}
