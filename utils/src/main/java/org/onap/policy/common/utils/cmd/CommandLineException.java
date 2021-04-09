/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.common.utils.cmd;

/**
 * Exception used for CommandLineArguments class.
 *
 * @author Adheli Tavares (adheli.tavares@est.tech)
 *
 */
public class CommandLineException extends Exception {

    /**
     * Generated serialVersionUID.
     */
    private static final long serialVersionUID = -1200607308084606425L;

    /**
     * Instantiates a new exception with a message.
     *
     * @param message the message
     */
    public CommandLineException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new exception with a message and a caused by exception.
     *
     * @param message the message
     * @param exp the exception that caused this exception to be thrown
     */
    public CommandLineException(final String message, final Exception exp) {
        super(message, exp);
    }
}
