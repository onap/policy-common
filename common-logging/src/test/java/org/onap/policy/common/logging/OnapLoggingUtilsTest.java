/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OnapLoggingUtilsTest {

    @Test
    void testGetLoggingContextForRequest_withXForwardedFor() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getLocalAddr()).thenReturn("192.168.1.1");
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18, 150.172.238.178");
        Mockito.when(request.getHeader("X-ECOMP-RequestID")).thenReturn("request-id-12345");

        OnapLoggingContext baseContext = new OnapLoggingContext();

        // Act
        OnapLoggingContext context = OnapLoggingUtils.getLoggingContextForRequest(request, baseContext);

        // Assert
        assertEquals("192.168.1.1", context.getServerIpAddress());
        assertEquals("203.0.113.195", context.getClientIpAddress());
        assertEquals("request-id-12345", context.getRequestId());
    }

    @Test
    void testGetLoggingContextForRequest_withoutXForwardedFor() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getLocalAddr()).thenReturn("192.168.1.1");
        Mockito.when(request.getRemoteAddr()).thenReturn("203.0.113.195");
        Mockito.when(request.getHeader("X-ECOMP-RequestID")).thenReturn("request-id-12345");

        OnapLoggingContext baseContext = new OnapLoggingContext();

        // Act
        OnapLoggingContext context = OnapLoggingUtils.getLoggingContextForRequest(request, baseContext);

        // Assert
        assertEquals("192.168.1.1", context.getServerIpAddress());
        assertEquals("203.0.113.195", context.getClientIpAddress());
        assertEquals("request-id-12345", context.getRequestId());
    }

    @Test
    void testGetLoggingContextForRequest_nullLocalAddr() {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getLocalAddr()).thenReturn(null);
        Mockito.when(request.getRemoteAddr()).thenReturn("203.0.113.195");
        Mockito.when(request.getHeader("X-ECOMP-RequestID")).thenReturn("request-id-12345");

        OnapLoggingContext baseContext = new OnapLoggingContext();

        // Act
        OnapLoggingContext context = OnapLoggingUtils.getLoggingContextForRequest(request, baseContext);

        // Assert
        assertEquals("203.0.113.195", context.getClientIpAddress());
        assertEquals("request-id-12345", context.getRequestId());
    }

    @Test
    void testFormatMessage_noArguments() {
        // Arrange
        String format = "This is a test message";

        // Act
        String result = OnapLoggingUtils.formatMessage(format);

        // Assert
        assertEquals("This is a test message", result);
    }

    @Test
    void testFormatMessage_withArguments() {
        // Arrange
        String format = "Hello, {}. Welcome to {}.";
        Object[] arguments = {"John", "ONAP"};

        // Act
        String result = OnapLoggingUtils.formatMessage(format, arguments);

        // Assert
        assertEquals("Hello, John. Welcome to ONAP.", result);
    }

    @Test
    void testFormatMessage_morePlaceholdersThanArguments() {
        // Arrange
        String format = "Hello, {}. Welcome to {}. Your ID is {}.";
        Object[] arguments = {"John", "ONAP", "12345"};

        // Act
        String result = OnapLoggingUtils.formatMessage(format, arguments);

        // Assert
        assertEquals("Hello, John. Welcome to ONAP. Your ID is 12345.", result);
    }

    @Test
    void testIsThrowable_withThrowable() {
        // Arrange
        Exception exception = new Exception("Test exception");

        // Act
        boolean result = OnapLoggingUtils.isThrowable(exception);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsThrowable_withNonThrowable() {
        // Arrange
        String nonThrowable = "This is a string";

        // Act
        boolean result = OnapLoggingUtils.isThrowable(nonThrowable);

        // Assert
        assertFalse(result);
    }
}
