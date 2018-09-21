/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.validation;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.anything;

import com.openpojo.reflection.PojoClass;
import com.openpojo.validation.affirm.Affirm;
import com.openpojo.validation.test.Tester;
import com.openpojo.validation.utils.ValidationHelper;

import org.hamcrest.Matcher;


/**
 * Class to provide toString testing utility for testing pojo classes.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@ericsson.com)
 */
@SuppressWarnings("rawtypes")
public class ToStringTester implements Tester {

    private final Matcher matcher;

    public ToStringTester() {
        matcher = anything();
    }

    public ToStringTester(final Matcher matcher) {
        this.matcher = matcher;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(final PojoClass pojoClass) {
        final Class clazz = pojoClass.getClazz();
        if (anyOf(matcher).matches(clazz)) {
            final Object classInstance = ValidationHelper.getBasicInstance(pojoClass);

            Affirm.affirmFalse("Found default toString output",
                    classInstance.toString().matches(Object.class.getName() + "@" + "\\w+"));
        }

    }

}
