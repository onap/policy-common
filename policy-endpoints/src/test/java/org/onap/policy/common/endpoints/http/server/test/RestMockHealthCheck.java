/*-
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/")
public class RestMockHealthCheck {

    @GET
    @Path("pap/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response papHealthCheck() {   
        return Response.status(Status.OK).entity("All Alive").build();
    }
    
    @GET
    @Path("pdp/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pdpHealthCheck() {   
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("At least some Dead").build();
    }


}
