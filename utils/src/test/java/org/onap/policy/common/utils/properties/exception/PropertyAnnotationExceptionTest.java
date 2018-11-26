/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 *  Test class for PropertyAnnotationException.
 */
public class PropertyAnnotationExceptionTest extends SupportBasicPropertyExceptionTester {

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyException#PropertyException
     * (java.lang.String, java.lang.String)}.
     */
    @Test
    public void testPropertyExceptionStringField() {
        doTestPropertyExceptionStringField_AllPopulated(new PropertyAnnotationException(PROPERTY, FIELD));
        doTestPropertyExceptionStringField_NullProperty(new PropertyAnnotationException(null, FIELD));
        doTestPropertyExceptionStringField_NullField(new PropertyAnnotationException(PROPERTY, null));
        doTestPropertyExceptionStringField_BothNull(new PropertyAnnotationException(null, null));
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyException#PropertyException
     * (java.lang.String, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testPropertyExceptionStringFieldString() {
        doTestPropertyExceptionStringFieldString(new PropertyAnnotationException(PROPERTY, FIELD, MESSAGE));
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyException#PropertyException
     * (java.lang.String, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testPropertyExceptionStringFieldThrowable() {
        doTestPropertyExceptionStringFieldThrowable(new PropertyAnnotationException(PROPERTY, FIELD, THROWABLE));
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.exception.PropertyException#PropertyException
     * (java.lang.String, java.lang.String, java.lang.String, java.lang.Throwable)}.
     */
    @Test
    public void testPropertyExceptionStringFieldStringThrowable() {
        doTestPropertyExceptionStringFieldStringThrowable(
                        new PropertyAnnotationException(PROPERTY, FIELD, MESSAGE, THROWABLE));
    }

}
