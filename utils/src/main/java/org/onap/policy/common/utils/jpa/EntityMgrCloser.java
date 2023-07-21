/*
 * ============LICENSE_START=======================================================
 * Common Utils
 * ================================================================================
 * Copyright (C) 2018, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.jpa;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Wrapper for an <i>EntityManager</i>, providing auto-close functionality. This is useful in
 * try-with-resources statements.
 */
@AllArgsConstructor
public class EntityMgrCloser implements AutoCloseable {

    /**
     * The wrapped manager.
     */
    @Getter
    private final EntityManager manager;

    @Override
    public void close() {
        manager.close();
    }

}
