/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018, 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.common.utils.properties.exception;

import org.junit.jupiter.api.Test;

/**
 * Test class for PropertyAccessException.
 */
class PropertyAccessExceptionTest extends SupportBasicPropertyExceptionTester {

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyAccessException#PropertyAccessException
     * (java.lang.String, java.lang.String)}.
     */
    @Test
    void testPropertyAccessExceptionStringField() {
        verifyPropertyExceptionStringField_AllPopulated(new PropertyAccessException(PROPERTY, FIELD));
        verifyPropertyExceptionStringField_NullProperty(new PropertyAccessException(null, FIELD));
        verifyPropertyExceptionStringField_NullField(new PropertyAccessException(PROPERTY, null));
        verifyPropertyExceptionStringField_BothNull(new PropertyAccessException(null, null));
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyAccessException#PropertyAccessException
     * (java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    void testPropertyAccessExceptionStringFieldString() {
        verifyPropertyExceptionStringFieldString(new PropertyAccessException(PROPERTY, FIELD, MESSAGE));
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyAccessException#PropertyAccessException
     * (java.lang.String, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    void testPropertyAccessExceptionStringFieldThrowable() {
        verifyPropertyExceptionStringFieldThrowable(new PropertyAccessException(PROPERTY, FIELD, THROWABLE));
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyAccessException#PropertyAccessException
     * (java.lang.String, java.lang.String, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    void testPropertyAccessExceptionStringFieldStringThrowable() {
        verifyPropertyExceptionStringFieldStringThrowable(
                        new PropertyAccessException(PROPERTY, FIELD, MESSAGE, THROWABLE));
    }

}
