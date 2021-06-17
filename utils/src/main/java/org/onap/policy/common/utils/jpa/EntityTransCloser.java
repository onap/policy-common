/*
 * ============LICENSE_START=======================================================
 * Common Utils
 * ================================================================================
 * Copyright (C) 2018, 2021 AT&T Intellectual Property. All rights reserved.
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
import lombok.Getter;

/**
 * Wrapper for an <i>EntityTransaction</i> that is auto-rolled back when closed. This is useful in
 * try-with-resources statements.
 */
public class EntityTransCloser implements AutoCloseable {

    /**
     * Transaction to be rolled back.
     */
    @Getter
    private final EntityTransaction transaction;

    /**
     * Begins a transaction.
     *
     * @param et transaction to wrap/begin
     */
    public EntityTransCloser(EntityTransaction et) {
        transaction = et;
        transaction.begin();
    }

    /**
     * Commits the transaction.
     */
    public void commit() {
        transaction.commit();
    }

    /**
     * Rolls back the transaction.
     */
    public void rollback() {
        transaction.rollback();
    }

    @Override
    public void close() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }
}
