/*
 * ============LICENSE_START=======================================================
 * Common Utils
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.onap.policy.common.utils.dao.Dao;

/**
 * Wrapper for an <i>EntityManager</i>, providing auto-close functionality. This is useful in
 * try-with-resources statements.
 */
public class EntityMgrCloser implements Dao {

    /**
     * The wrapped manager.
     */
    private final EntityManager em;

    /**
     * Construct an instance with the EntityManager.
     *
     * @param em manager to be auto-closed
     */
    public EntityMgrCloser(EntityManager em) {
        this.em = em;
    }

    /**
     * Gets the EntityManager wrapped within this object.
     *
     * @return the associated EntityManager
     */
    public EntityManager getManager() {
        return em;
    }

    @Override
    public void close() {
        try {
            EntityTransaction trans = em.getTransaction();
            if (trans.isActive()) {
                trans.rollback();
            }

        } finally {
            em.close();
        }
    }

    @Override
    public EntityTransCloser beginTransaction() {
        return new EntityTransCloser(em.getTransaction());
    }
}
