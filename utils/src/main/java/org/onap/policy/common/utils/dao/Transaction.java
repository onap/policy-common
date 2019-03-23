/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.dao;

/**
 * Transaction API. This can be used with a try-with-resources statement. When
 * {@link #close()} is invoked, if the transaction is still active, then it will
 * automatically roll the transaction back.
 */
public interface Transaction extends AutoCloseable {

    /**
     * Commits the transaction, if it is still active.
     *
     * @throws Exception if an error occurs
     */
    void commit() throws Exception;

    /**
     * Rolls back the transaction, if it is still active.
     *
     * @throws Exception if an error occurs
     */
    void rollback() throws Exception;
}
