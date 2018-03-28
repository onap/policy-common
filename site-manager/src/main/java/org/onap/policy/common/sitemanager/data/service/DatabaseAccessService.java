/*-
 * ============LICENSE_START=======================================================
 * site-manager
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.common.sitemanager.data.service;

import java.util.Collection;
import java.util.List;
import org.onap.policy.common.im.jpa.ResourceRegistrationEntity;
import org.onap.policy.common.im.jpa.StateManagementEntity;

public interface DatabaseAccessService extends AutoCloseable {

    <T> List<T> execute(final Class<T> clazz, final String query, final String paramName, final String paramValue);

    <T> List<T> execute(final Class<T> clazz, final String query);

    /**
     * Get {@link StateManagementEntity} entities from database.
     * 
     * @param resourceOption resource name (optional)
     * @param stateOption site name (optional)
     * @return list of {@link StateManagementEntity} entities found
     */
    List<StateManagementEntity> getStateManagementEntities(final String resourceOption, final String stateOption);

    /**
     * Get {@link ResourceRegistrationEntity} entities from database.
     * 
     * @param resourceOption resource name (optional)
     * @param stateOption site name (optional)
     * @return list of {@link ResourceRegistrationEntity} entities found
     */
    List<ResourceRegistrationEntity> getResourceRegistrationEntities(final String resourceOption,
            final String stateOption);

    <T> void persist(final Collection<T> entities);

    <T> void refreshEntity(final T enity);
}
