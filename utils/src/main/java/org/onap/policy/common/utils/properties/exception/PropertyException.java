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

import java.lang.reflect.Field;

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
     * 
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     */
    public PropertyException(String propnm, Field field) {
        super(makeMessage(propnm, field));

        this.propertyName = propnm;
        this.fieldName = extractFieldName(field);
    }

    /**
     * 
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     * @param message error message
     */
    public PropertyException(String propnm, Field field, String message) {
        super(makeMessage(propnm, field, message));

        this.propertyName = propnm;
        this.fieldName = extractFieldName(field);
    }

    /**
     * 
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     * @param cause cause of the exception
     */
    public PropertyException(String propnm, Field field, Throwable cause) {
        super(makeMessage(propnm, field), cause);

        this.propertyName = propnm;
        this.fieldName = extractFieldName(field);
    }

    /**
     * 
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     * @param message error message
     * @param cause cause of the exception
     */
    public PropertyException(String propnm, Field field, String message, Throwable cause) {
        super(makeMessage(propnm, field, message), cause);

        this.propertyName = propnm;
        this.fieldName = extractFieldName(field);
    }

    /**
     * 
     * @return name of the property for which the exception was thrown, or {@code null} if
     *         no name was provided
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * 
     * @return name of the field for which the exception was thrown, or {@code null} if no
     *         field was provided
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     * @param message error message, never {@code null}
     * @return an error message composed of the three items
     */
    private static String makeMessage(String propnm, Field field, String message) {
        return makeMessage(propnm, field) + ": " + message;
    }

    /**
     * 
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     * @return an error message composed of the two items
     */
    private static String makeMessage(String propnm, Field field) {
        StringBuilder bldr = new StringBuilder(50);

        if (propnm == null) {
            bldr.append("property exception");

        } else {
            bldr.append("exception for property " + propnm);
        }

        if (field != null) {
            bldr.append(" with field " + extractFieldName(field));
        }

        return bldr.toString();
    }

    /**
     * Extracts the field name from a field.
     * 
     * @param field field from which to extract the name, or {@code null}
     * @return the field name, or {@code null} if <i>field</i> is {@code null}
     */
    private static String extractFieldName(Field field) {
        return (field == null ? null : field.getName());
    }

}
