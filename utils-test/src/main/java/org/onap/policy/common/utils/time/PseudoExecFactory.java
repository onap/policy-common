/*
 * ============LICENSE_START=======================================================
 * ONAP
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

package org.onap.policy.common.utils.time;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory to create executor services that use {@link PseudoTime} to execute there tasks.
 */
public class PseudoExecFactory extends ExecutorFactory {

    /**
     * Time with which this item is associated.
     */
    private final PseudoTime support;

    /**
     * Constructs the object.
     *
     * @param support time with which this item is associated
     */
    public PseudoExecFactory(PseudoTime support) {
        this.support = support;
    }

    @Override
    public ScheduledExecutorService newScheduledThreadPool(int numThreads) {
        return new PseudoScheduledExecutorService(support);
    }

    @Override
    public ExecutorService newFixedThreadPool(int numThreads) {
        return this.newScheduledThreadPool(numThreads);
    }
}
