/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.logging.flexlogger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utilities to display messages. These are generally used while logging is being
 * configured, or when logging being directed to System.out. As a result, it directly
 * writes to System.out rather than to a logger.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DisplayUtils {

    /*
     * As the comment above says, these purposely write to System.out rather than a
     * logger, thus sonar is disabled.
     */

    public static void displayMessage(Object message) {
        System.out.println(message);    // NOSONAR
    }

    public static void displayErrorMessage(Object msg) {
        System.err.println(msg);        // NOSONAR
    }
}
