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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Superclass used to test subclasses of {@link PropertyException}.
 */
public class SupportBasicPropertyExceptionTester {

    /**
     * The "message" that's passed each time an exception is constructed.
     */
    protected static final String MESSAGE = "some error";

    /**
     * The "throwable" that's passed each time an exception is constructed.
     */
    protected static final Throwable THROWABLE = new Throwable();

    /**
     * Name of the "property" to be passed each time an exception is constructed.
     */
    protected static final String PROPERTY = "myName";

    /**
     * Name of the "property" field.
     */
    protected static final String FIELD = "PROPERTY";

    /*
     * Methods to perform various tests on the except subclass.
     */

    protected void verifyPropertyExceptionStringField_AllPopulated(PropertyException ex) {
        standardTests(ex);
    }

    protected void verifyPropertyExceptionStringField_NullProperty(PropertyException ex) {
        assertEquals(null, ex.getPropertyName());
        assertEquals(FIELD, ex.getFieldName());
        assertNotNull(ex.getMessage());
        assertNotNull(ex.toString());
    }

    protected void verifyPropertyExceptionStringField_NullField(PropertyException ex) {
        assertEquals(PROPERTY, ex.getPropertyName());
        assertEquals(null, ex.getFieldName());
        assertNotNull(ex.getMessage());
        assertNotNull(ex.toString());
    }

    protected void verifyPropertyExceptionStringField_BothNull(PropertyException ex) {
        assertEquals(null, ex.getPropertyName());
        assertEquals(null, ex.getFieldName());
        assertNotNull(ex.getMessage());
        assertNotNull(ex.toString());
    }

    protected void verifyPropertyExceptionStringFieldString(PropertyException ex) {
        standardTests(ex);
        standardMessageTests(ex);
    }

    protected void verifyPropertyExceptionStringFieldThrowable(PropertyException ex) {
        standardTests(ex);
        standardThrowableTests(ex);
    }

    protected void verifyPropertyExceptionStringFieldStringThrowable(PropertyException ex) {
        standardTests(ex);
        standardMessageTests(ex);
        standardThrowableTests(ex);
    }

    /**
     * Performs standard tests that should apply to all subclasses.
     *
     * @param ex exception to test
     */
    protected void standardTests(PropertyException ex) {
        assertEquals(PROPERTY, ex.getPropertyName());
        assertEquals(FIELD, ex.getFieldName());
        assertNotNull(ex.getMessage());
        assertNotNull(ex.toString());
    }

    /**
     * Performs standard tests for exceptions that were provided a message in their
     * constructor.
     *
     * @param ex exception to test
     */
    protected void standardMessageTests(PropertyException ex) {
        assertThat(ex.getMessage()).endsWith(MESSAGE);
    }

    /**
     * Performs standard tests for exceptions that were provided a throwable in their
     * constructor.
     *
     * @param ex exception to test
     */
    protected void standardThrowableTests(PropertyException ex) {
        assertEquals(THROWABLE, ex.getCause());
    }

}
