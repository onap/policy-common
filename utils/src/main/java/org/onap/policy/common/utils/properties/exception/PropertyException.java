/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018, 2021 AT&T Intellectual Property. All rights reserved.
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

/**
 * Exception associated with a Property.
 */
public class PropertyException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Name of the property for which the exception was thrown.
     */
    private final String propertyName;

    /**
     * Name of the field for which the exception was thrown.
     */
    private final String fieldName;

    /**
     * Constructor.
     *
     * @param propName name of the property causing the exception, or {@code null}
     * @param fieldName name of the field causing the exception, or {@code null}
     */
    public PropertyException(String propName, String fieldName) {
        super(makeMessage(propName, fieldName));

        this.propertyName = propName;
        this.fieldName = fieldName;
    }

    /**
     * Constructor.
     *
     * @param propnm name of the property causing the exception, or {@code null}
     * @param fieldName name of the field causing the exception, or {@code null}
     * @param message error message
     */
    public PropertyException(String propnm, String fieldName, String message) {
        super(makeMessage(propnm, fieldName, message));

        this.propertyName = propnm;
        this.fieldName = fieldName;
    }

    /**
     * Constructor.
     *
     * @param propnm name of the property causing the exception, or {@code null}
     * @param fieldName name of the field causing the exception, or {@code null}
     * @param cause cause of the exception
     */
    public PropertyException(String propnm, String fieldName, Throwable cause) {
        super(makeMessage(propnm, fieldName), cause);

        this.propertyName = propnm;
        this.fieldName = fieldName;
    }

    /**
     * Constructor.
     *
     * @param propnm name of the property causing the exception, or {@code null}
     * @param fieldName name of the field causing the exception, or {@code null}
     * @param message error message
     * @param cause cause of the exception
     */
    public PropertyException(String propnm, String fieldName, String message, Throwable cause) {
        super(makeMessage(propnm, fieldName, message), cause);

        this.propertyName = propnm;
        this.fieldName = fieldName;
    }

    /**
     * Get the property name.
     *
     * @return name of the property for which the exception was thrown, or {@code null} if
     *         no name was provided
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Get the field name.
     *
     * @return name of the field for which the exception was thrown, or {@code null} if no
     *         field was provided
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Make the message.
     *
     * @param propnm name of the property causing the exception, or {@code null}
     * @param fieldName name of the field causing the exception, or {@code null}
     * @param message error message, never {@code null}
     * @return an error message composed of the three items
     */
    private static String makeMessage(String propnm, String fieldName, String message) {
        return makeMessage(propnm, fieldName) + ": " + message;
    }

    /**
     * Make the message.
     *
     * @param propnm name of the property causing the exception, or {@code null}
     * @param fieldName name of the field causing the exception, or {@code null}
     * @return an error message composed of the two items
     */
    private static String makeMessage(String propnm, String fieldName) {
        var bldr = new StringBuilder(50);

        if (propnm == null) {
            bldr.append("property exception");

        } else {
            bldr.append("exception for property " + propnm);
        }

        if (fieldName != null) {
            bldr.append(" with field " + fieldName);
        }

        return bldr.toString();
    }

}
