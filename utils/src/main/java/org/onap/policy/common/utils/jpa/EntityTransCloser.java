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

import javax.persistence.EntityTransaction;

/**
 * Wrapper for an <i>EntityTransaction</i> that is auto-rolled back when closed. This is useful in
 * try-with-resources statements.
 */
public class EntityTransCloser implements AutoCloseable {

    /**
     * Transaction to be rolled back.
     */
    private final EntityTransaction trans;

    /**
     * Begins a transaction.
     * 
     * @param et transaction to wrap/begin
     */
    public EntityTransCloser(EntityTransaction et) {
        trans = et;
        trans.begin();
    }

    /**
     * Gets the wrapped transaction.
     * 
     * @return the transaction
     */
    public EntityTransaction getTransation() {
        return trans;
    }

    /**
     * Commits the transaction.
     */
    public void commit() {
        trans.commit();
    }

    /**
     * Rolls back the transaction.
     */
    public void rollback() {
        trans.rollback();
    }

    @Override
    public void close() {
        if (trans.isActive()) {
            trans.rollback();
        }
    }

}
