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

package org.onap.policy.common.utils.provider;

/**
 * Provider API. This can be used with a try-with-resources statement. When
 * {@link #close()} is invoked, any active transaction will automatically be rolled back.
 * Note: only one transaction should be active at a time.
 */
public interface Provider extends AutoCloseable {

    /**
     * Begins a transaction.
     *
     * @return a transaction
     */
    Transaction beginTransaction() throws Exception;
}
