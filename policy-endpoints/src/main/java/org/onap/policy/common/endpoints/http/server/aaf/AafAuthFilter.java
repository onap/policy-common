/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.http.server.aaf;

import jakarta.servlet.http.HttpServletRequest;
import org.onap.policy.common.endpoints.http.server.AuthorizationFilter;

/**
 * Generic Authorization AAF Filter Skeleton.   This class will return
 * a permission in AAF format.  Subclasses are responsible to provide
 * the AAF permission type and instance.
 */
public abstract class AafAuthFilter extends AuthorizationFilter {

    public static final String DEFAULT_NAMESPACE = "org.onap.policy";

    @Override
    protected String getRole(HttpServletRequest request) {
        return
            String.format("%s|%s|%s", getPermissionType(request), getPermissionInstance(request),
                          request.getMethod().toLowerCase());
    }

    protected abstract String getPermissionType(HttpServletRequest request);
    
    protected abstract String getPermissionInstance(HttpServletRequest request);
}
