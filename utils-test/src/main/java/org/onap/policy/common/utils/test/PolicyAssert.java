/*
 * ============LICENSE_START=======================================================
 * ONAP
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

package org.onap.policy.common.utils.test;

public class PolicyAssert {

    /**
     * Should not instantiate this.
     */
    private PolicyAssert() {
        // do nothing
    }

    /**
     * Invokes a function that is expected to throw an exception.
     *
     * @param clazz class of exception that is expected
     * @param func function
     * @return the exception that was thrown
     * @throws AssertionError if the function does not throw an exception or throws the
     *         wrong type of exception
     */
    public static <T extends Throwable> T assertThrows(Class<T> clazz, RunnableWithEx func) {
        try {
            func.run();

        } catch (Throwable thrown) {
            try {
                return clazz.cast(thrown);

            } catch (ClassCastException thrown2) {
                throw new AssertionError("incorrect exception type", thrown2);
            }
        }

        throw new AssertionError("missing exception");
    }

    /**
     * Runnable that may throw an exception.
     */
    @FunctionalInterface
    public static interface RunnableWithEx {
        public void run() throws Throwable;
    }
}
