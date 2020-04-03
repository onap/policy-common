/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018, 2020 AT&T Intellectual Property. All rights reserved.
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

import org.junit.Test;

/**
 * Test class for PropertyInvalidException.
 */
public class PropertyInvalidExceptionTest extends SupportBasicPropertyExceptionTester {

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyException#PropertyException
     * (java.lang.String, java.lang.String)}.
     */
    @Test
    public void testPropertyExceptionStringField() {
        verifyPropertyExceptionStringField_AllPopulated(new PropertyInvalidException(PROPERTY, FIELD));
        verifyPropertyExceptionStringField_NullProperty(new PropertyInvalidException(null, FIELD));
        verifyPropertyExceptionStringField_NullField(new PropertyInvalidException(PROPERTY, null));
        verifyPropertyExceptionStringField_BothNull(new PropertyInvalidException(null, null));
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyException#PropertyException
     * (java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testPropertyExceptionStringFieldString() {
        verifyPropertyExceptionStringFieldString(new PropertyInvalidException(PROPERTY, FIELD, MESSAGE));
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyException#PropertyException
     * (java.lang.String, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testPropertyExceptionStringFieldThrowable() {
        verifyPropertyExceptionStringFieldThrowable(new PropertyInvalidException(PROPERTY, FIELD, THROWABLE));
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyException#PropertyException
     * (java.lang.String, java.lang.String, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testPropertyExceptionStringFieldStringThrowable() {
        verifyPropertyExceptionStringFieldStringThrowable(
                        new PropertyInvalidException(PROPERTY, FIELD, MESSAGE, THROWABLE));
    }

}
