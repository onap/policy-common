/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;


@Path("/junit/echo")
public class RestEchoService {

    @GET
    @Path("{word}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "echoes back whatever received")
    public String echo(@PathParam("word") String word) {
        return word;
    }

    @PUT
    @Path("{word}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "echoes back whatever received")
    public String echoPut(@PathParam("word") String word, Object entity) {
        return "PUT:" + word + ":" + entity.toString();
    }

    @POST
    @Path("/full/request")
    @Produces({MediaType.APPLICATION_JSON, YamlMessageBodyHandler.APPLICATION_YAML})
    @Operation(summary = "echoes back the request structure")
    @ApiResponse(content = {@Content(schema = @Schema(implementation = RestEchoReqResp.class))})
    public Response echoFullyPost(RestEchoReqResp reqResp) {
        return Response.status(Status.OK).entity(reqResp).build();
    }

    @POST
    @Path("{word}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "echoes back whatever received")
    public String echoPost(@PathParam("word") String word, Object entity)  {
        return "POST:" + word + ":" + entity.toString();
    }

    @DELETE
    @Path("{word}")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "echoes back whatever received")
    public String echoDelete(@PathParam("word") String word)  {
        return "DELETE:" + word;
    }
}
