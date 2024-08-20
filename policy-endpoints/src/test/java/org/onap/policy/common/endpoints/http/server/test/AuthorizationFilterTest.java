/*
 * ============LICENSE_START=======================================================
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

package org.onap.policy.common.endpoints.http.server.test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.policy.common.endpoints.http.server.AuthorizationFilter;

class AuthorizationFilterTest {

    AuthorizationFilter filter;

    @Mock
    ServletRequest request;

    @Mock
    HttpServletRequest httpRequest;

    @Mock
    HttpServletResponse httpResponse;

    @Mock
    ServletResponse response;

    @Mock
    FilterChain chain;

    @BeforeEach
    void setUp() {
        request = mock(ServletRequest.class);
        response = mock(ServletResponse.class);
        chain = mock(FilterChain.class);
        httpRequest = mock(HttpServletRequest.class);
        httpResponse = mock(HttpServletResponse.class);

        filter = new AuthorizationFilter() {
            @Override
            protected String getRole(HttpServletRequest request) {
                return "testRole";
            }
        };
    }

    @Test
    void testAuthorizationFilter() throws ServletException, IOException {
        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
            .isInstanceOf(ServletException.class)
            .hasMessageContaining("Not an HttpServletRequest instance");

        assertThatThrownBy(() -> filter.doFilter(httpRequest, response, chain))
            .isInstanceOf(ServletException.class)
            .hasMessageContaining("Not an HttpServletResponse instance");

        assertThatCode(() -> filter.doFilter(httpRequest, httpResponse, chain))
            .doesNotThrowAnyException();
    }
}
