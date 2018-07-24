/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2016-2018 Ericsson. All rights reserved.
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

package org.onap.policy.common.parameters;

/**
 * Exception thrown oon parameter reading, validation, and check errors.
 *
 * @author Liam Fallon (liam.fallon@ericsson.com)
 */
public class ParameterException extends Exception {
    private static final long serialVersionUID = -8507246953751956974L;

    // The object on which the exception was thrown
    private final transient Object object;

    /**
     * Instantiates a new parameter exception.
     *
     * @param message the message on the exception
     */
    public ParameterException(final String message) {
        this(message, null);
    }

    /**
     * Instantiates a new parameter exception.
     *
     * @param message the message on the exception
     * @param object the object that the exception was thrown on
     */
    public ParameterException(final String message, final Object object) {
        super(message);
        this.object = object;
    }

    /**
     * Instantiates a new parameter exception.
     *
     * @param message the message on the exception
     * @param e the exception that caused this parameter exception
     */
    public ParameterException(final String message, final Exception e) {
        this(message, e, null);
    }

    /**
     * Instantiates a new parameter exception.
     *
     * @param message the message on the exception
     * @param e the exception that caused this parameter exception
     * @param object the object that the exception was thrown on
     */
    public ParameterException(final String message, final Exception e, final Object object) {
        super(message, e);
        this.object = object;
    }

    /**
     * Get the message from this exception and its causes.
     *
     * @return the cascaded messages from this exception and the exceptions that caused it
     */
    public String getCascadedMessage() {
        return buildCascadedMessage(this);
    }

    /**
     * Build a cascaded message from an exception and all its nested exceptions
     * 
     * @param throwable the top level exception
     * @return cascaded message string
     */
    public static String buildCascadedMessage(Throwable throwable) {
        final StringBuilder builder = new StringBuilder();
        builder.append(throwable.getMessage());

        for (Throwable t = throwable; t != null; t = t.getCause()) {
            builder.append("\ncaused by: ");
            builder.append(t.getMessage());
        }

        return builder.toString();
    }

    /**
     *
     * Get the object on which the exception was thrown.
     *
     * @return The object on which the exception was thrown
     */
    public Object getObject() {
        return object;
    }
}
