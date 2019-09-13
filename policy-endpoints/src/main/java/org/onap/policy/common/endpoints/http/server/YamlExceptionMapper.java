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

import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Catches JSON exceptions when decoding a REST request and converts them from an HTTP 500
 * error code to an HTTP 400 error code.
 */
@Provider
@Produces(YamlMessageBodyHandler.APPLICATION_YAML)
public class YamlExceptionMapper implements ExceptionMapper<YAMLException> {
    private static Logger logger = LoggerFactory.getLogger(YamlExceptionMapper.class);

    @Override
    public Response toResponse(YAMLException exception) {
        logger.warn("invalid YAML request", exception);
        return Response.status(Response.Status.BAD_REQUEST).entity(new SimpleResponse("Invalid request")).build();
    }

    @Getter
    private static class SimpleResponse {
        private String errorDetails;

        public SimpleResponse(String errorDetails) {
            this.errorDetails = errorDetails;
        }
    }
}
