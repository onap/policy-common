/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonSyntaxException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.endpoints.http.server.JsonExceptionMapper;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

class JsonExceptionMapperTest {
    private JsonExceptionMapper mapper;

    @BeforeEach
    public void setUp() {
        mapper = new JsonExceptionMapper();
    }

    @Test
    void testToResponse() throws CoderException {
        JsonSyntaxException ex = new JsonSyntaxException("expected exception");
        Response resp = mapper.toResponse(ex);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), resp.getStatus());
        assertEquals("{'errorDetails':'Invalid request'}".replace('\'', '"'),
                        new StandardCoder().encode(resp.getEntity()));
    }
}
