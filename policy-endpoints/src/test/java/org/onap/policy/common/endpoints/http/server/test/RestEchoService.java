/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;


@Api(value = "echo")
@Path("/junit/echo")
public class RestEchoService {

    @GET
    @Path("{word}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "echoes back whatever received")
    public String echo(@PathParam("word") String word) {
        return word;
    }

    @PUT
    @Path("{word}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "echoes back whatever received")
    public String echoPut(@PathParam("word") String word, Object entity) {
        return "PUT:" + word + ":" + entity.toString();
    }

    @POST
    @Path("/full/request")
    @Produces({MediaType.APPLICATION_JSON, YamlMessageBodyHandler.APPLICATION_YAML})
    @ApiOperation(value = "echoes back the request structure", response = RestEchoReqResp.class)
    public Response echoFullyPost(RestEchoReqResp reqResp) {
        return Response.status(Status.OK).entity(reqResp).build();
    }

    @POST
    @Path("{word}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "echoes back whatever received")
    public String echoPost(@PathParam("word") String word, Object entity)  {
        return "POST:" + word + ":" + entity.toString();
    }

    @DELETE
    @Path("{word}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "echoes back whatever received")
    public String echoDelete(@PathParam("word") String word)  {
        return "DELETE:" + word;
    }
}
