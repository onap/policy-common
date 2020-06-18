/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.logging;

import javax.servlet.http.HttpServletRequest;

public class OnapLoggingUtils {

    private OnapLoggingUtils() {
        // Private constructor to prevent subclassing
    }

    /**
     * Get the ONAPLoggingContext for a request.
     *
     * @param request the request
     * @param baseContext the context to supply to the ONAPLoggingContext
     * @return the ONAPLoggingContext
     */
    public static OnapLoggingContext getLoggingContextForRequest(HttpServletRequest request,
        OnapLoggingContext baseContext) {
        OnapLoggingContext requestContext = new OnapLoggingContext(baseContext);
        if (request.getLocalAddr() != null) { // may be null in junit tests
            requestContext.setServerIpAddress(request.getLocalAddr());
        }
        // get client IP address as leftmost address in X-Forwarded-For header if present,
        // otherwise from remote address in the request
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && forwarded.trim().length() > 0) {
            forwarded = forwarded.trim().split(",")[0];
            requestContext.setClientIpAddress(forwarded);
        } else if (request.getRemoteAddr() != null) { // may be null in junit tests
            requestContext.setClientIpAddress(request.getRemoteAddr());
        }
        // RequestID
        // This needs to be renamed to ONAP when the other components in ONAP
        // rename to this.
        String requestId = request.getHeader("X-ECOMP-RequestID");
        if (requestId != null && requestId.trim().length() > 0) {
            requestContext.setRequestId(requestId);
        }
        return requestContext;
    }

    /**
     * Create message text replace {} place holder with data
     * if last argument is throwable/exception, pass it as argument to logger.
     * @param format message format can contains text and {}
     * @param arguments output arguments
     * @return
     */
    public static String formatMessage(String format, Object...arguments) {
        if (arguments.length <= 0 || arguments[0] == null) {
            return format;
        }
        int index;
        StringBuilder builder = new StringBuilder();
        String[] token = format.split("[{][}]");
        for (index = 0; index < arguments.length; index++) {
            if (index < token.length) {
                builder.append(token[index]);
                builder.append(arguments[index]);
            } else {
                break;
            }
        }
        for (int index2 = index; index2 < token.length; index2++) {
            builder.append(token[index2]);
        }

        return builder.toString();
    }

    /**
     * Check object is throwable.
     * @param obj to verify
     * @return true if object is throwable or false otherwise
     */
    public static boolean isThrowable(Object obj) {
        return (obj instanceof Throwable);
    }
}
