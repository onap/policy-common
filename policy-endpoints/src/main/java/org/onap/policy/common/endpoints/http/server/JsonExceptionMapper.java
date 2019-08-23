/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.http.server;

import com.google.gson.JsonSyntaxException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Catches JSON exceptions when decoding a REST request and converts them from an HTTP 500
 * error code to an HTTP 400 error code.
 */
@Produces(MediaType.APPLICATION_JSON)
public class JsonExceptionMapper implements ExceptionMapper<JsonSyntaxException> {
    private static Logger logger = LoggerFactory.getLogger(JsonExceptionMapper.class);

    @Override
    public Response toResponse(JsonSyntaxException exception) {
        logger.warn("invalid JSON request", exception);
        return Response.status(Response.Status.BAD_REQUEST).entity(new SimpleResponse("Invalid JSON request")).build();
    }

    @Getter
    private static class SimpleResponse {
        private String errorDetails;

        public SimpleResponse(String errorDetails) {
            this.errorDetails = errorDetails;
        }
    }
}
