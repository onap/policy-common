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

import static org.onap.policy.common.sitemanager.utils.Constants.RESOURCE_NAME;
import static org.onap.policy.common.sitemanager.utils.Constants.RESOURCE_REGISTRATION_QUERY;
import static org.onap.policy.common.sitemanager.utils.Constants.SITE_NAME;
import static org.onap.policy.common.sitemanager.utils.Constants.STATE_MANAGEMENT_QUERY;
import static org.onap.policy.common.sitemanager.utils.Constants.WHERE_R_RESOURCE_NAME;
import static org.onap.policy.common.sitemanager.utils.Constants.WHERE_R_SITE_SITE;
import static org.onap.policy.common.sitemanager.utils.Constants.WHERE_S_RESOURCE_NAME;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.onap.policy.common.im.jpa.ResourceRegistrationEntity;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.onap.policy.common.utils.jpa.EntityTransCloser;

public class DatabaseAccessServiceImpl implements DatabaseAccessService {

    private final EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    public DatabaseAccessServiceImpl(final String persistenceUnitName, final Properties properties) {
        this.entityManagerFactory = getEntityManagerFactory(persistenceUnitName, properties);
        entityManager = entityManagerFactory.createEntityManager();
    }

    @Override
    public <T> List<T> execute(final Class<T> clazz, final String query, final String paramName,
            final String paramValue) {
        final TypedQuery<T> typedQuery = entityManager.createQuery(query, clazz);
        typedQuery.setParameter(paramName, paramValue);
        return typedQuery.getResultList();

    }

    @Override
    public <T> List<T> execute(final Class<T> clazz, final String query) {
        final TypedQuery<T> typedQuery = entityManager.createQuery(query, clazz);
        return typedQuery.getResultList();
    }

    @Override
    public List<StateManagementEntity> getStateManagementEntities(final String rOption, final String sOption) {
        if (rOption != null) {
            final String query = STATE_MANAGEMENT_QUERY + WHERE_S_RESOURCE_NAME + RESOURCE_NAME;
            return execute(StateManagementEntity.class, query, RESOURCE_NAME, rOption);
        } else if (sOption != null) {
            return execute(StateManagementEntity.class, STATE_MANAGEMENT_QUERY);
        }
        return execute(StateManagementEntity.class, STATE_MANAGEMENT_QUERY);

    }

    @Override
    public List<ResourceRegistrationEntity> getResourceRegistrationEntities(final String rOption,
            final String sOption) {
        if (rOption != null) {
            final String query = RESOURCE_REGISTRATION_QUERY + WHERE_R_RESOURCE_NAME + RESOURCE_NAME;
            return execute(ResourceRegistrationEntity.class, query, RESOURCE_NAME, rOption);
        } else if (sOption != null) {
            final String query = RESOURCE_REGISTRATION_QUERY + WHERE_R_SITE_SITE;
            return execute(ResourceRegistrationEntity.class, query, SITE_NAME, sOption);
        }
        return execute(ResourceRegistrationEntity.class, RESOURCE_REGISTRATION_QUERY);
    }

    @Override
    public <T> void persist(final Collection<T> entities) {
        try (final EntityTransCloser et = new EntityTransCloser(entityManager.getTransaction())) {
            for (final T entity : entities) {
                entityManager.persist(entity);
            }
            entityManager.flush();
            et.commit();
        }
    }

    @Override
    public <T> void refreshEntity(final T enity) {
        entityManager.refresh(enity);

    }

    @Override
    public void close() {
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }

    protected EntityManagerFactory getEntityManagerFactory(final String persistenceUnitName,
            final Properties properties) {
        return Persistence.createEntityManagerFactory(persistenceUnitName, properties);
    }

}
