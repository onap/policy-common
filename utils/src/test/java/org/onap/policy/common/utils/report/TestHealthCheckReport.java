/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.common.utils.report;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.anything;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterClassName;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.Tester;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import com.openpojo.validation.utils.ValidationHelper;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

/**
 * Class to perform unit test of HealthCheckReport.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@ericsson.com)
 */
class TestHealthCheckReport {

    @Test
    void testHealthCheckReport() {
        final Validator validator =
                ValidatorBuilder.create().with(new GetterMustExistRule()).with(new SetterMustExistRule())
                        .with(new GetterTester()).with(new SetterTester()).with(new ToStringTester()).build();
        validator.validate(HealthCheckReport.class.getPackage().getName(),
                new FilterClassName(HealthCheckReport.class.getName()));
    }

    static class ToStringTester implements Tester {

        private final Matcher<?> matcher;

        public ToStringTester() {
            matcher = anything();
        }

        @Override
        public void run(final PojoClass pojoClass) {
            final Class<?> clazz = pojoClass.getClazz();
            if (anyOf(matcher).matches(clazz)) {
                final Object classInstance = ValidationHelper.getBasicInstance(pojoClass);

                Affirm.affirmFalse("Found default toString output",
                    classInstance.toString().matches(Object.class.getName() + "@" + "\\w+"));
            }

        }
    }
}
