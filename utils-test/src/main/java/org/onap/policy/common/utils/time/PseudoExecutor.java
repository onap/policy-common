/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import lombok.Getter;

/**
 * Executor that will run tasks until the queue is empty or a maximum number of tasks have
 * been executed. Doesn't actually run anything until {@link #runAll()} is invoked.
 */
public class PseudoExecutor implements Executor {

    /**
     * Tasks to be run.
     */
    @Getter
    private final Queue<Runnable> tasks = new LinkedList<>();


    /**
     * Gets the queue length.
     *
     * @return the queue length
     */
    public int getQueueLength() {
        return tasks.size();
    }

    @Override
    public void execute(Runnable command) {
        tasks.add(command);
    }

    /**
     * Runs all tasks until the queue is empty or the maximum number of tasks have been
     * reached.
     *
     * @param maxTasks maximum number of tasks to run
     * @return {@code true} if the queue is empty, {@code false} if the maximum number of
     *         tasks have been reached before the queue was emptied
     */
    public boolean runAll(int maxTasks) {
        for (var count = 0; count < maxTasks && !tasks.isEmpty(); ++count) {
            tasks.remove().run();
        }

        return tasks.isEmpty();
    }
}
