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
 * Exception indicating that a property's value cannot be converted to the type required
 * by the target field.
 */
public class PropertyAccessException extends PropertyException {
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     */
    public PropertyAccessException(String propnm, Field field) {
        super(propnm, field);
    }

    /**
     * 
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     * @param message error message
     */
    public PropertyAccessException(String propnm, Field field, String message) {
        super(propnm, field, message);
    }

    /**
     * 
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     * @param cause cause of the exception
     */
    public PropertyAccessException(String propnm, Field field, Throwable cause) {
        super(propnm, field, cause);
    }

    /**
     * 
     * @param propnm name of the property causing the exception, or {@code null}
     * @param field field causing the exception, or {@code null}
     * @param message error message
     * @param cause cause of the exception
     */
    public PropertyAccessException(String propnm, Field field, String message, Throwable cause) {
        super(propnm, field, message, cause);
    }

}
