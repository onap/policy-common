/*
 * ============LICENSE_START=======================================================
 * Common Utils
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import javax.persistence.EntityManagerFactory;

/**
 * Wrapper for an <i>EntityManagerFactory</i>, providing auto-close functionality. This is useful in
 * try-with-resources statements.
 */
public class EntityMgrFactoryCloser implements AutoCloseable {

    /**
     * The wrapped factory.
     */
    private final EntityManagerFactory emf;

    /**
     * Construct an instance with the given EntityManagerFactory.
     * 
     * @param emf manager to be auto-closed
     */
    public EntityMgrFactoryCloser(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Gets the EntityManagerFactory wrapped within this object.
     * 
     * @return the associated EntityManagerFactory
     */
    public EntityManagerFactory getFactory() {
        return emf;
    }

    @Override
    public void close() {
        emf.close();
    }

}
