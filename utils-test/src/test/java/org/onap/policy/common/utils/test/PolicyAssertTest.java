/*
 * ============LICENSE_START=======================================================
 * Common Utils-Test
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.onap.policy.common.utils.test.PolicyAssert.assertThrows;

import org.junit.Test;

public class PolicyAssertTest {

    private static final String EXPECTED = "expected exception";

    @Test
    public void test_ExpectedEx() {
        // exact type
        assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException(EXPECTED);
        });

        // cast to superclass is supported
        assertThrows(RuntimeException.class, () -> {
            throw new IllegalArgumentException(EXPECTED);
        });

        // supports errors
        assertThrows(LinkageError.class, () -> {
            throw new LinkageError(EXPECTED);
        });

        // supports any throwable
        assertThrows(Throwable.class, () -> {
            throw new Throwable(EXPECTED);
        });
    }

    @Test
    public void test_IncorrectEx() {
        try {
            assertThrows(IllegalStateException.class, () -> {
                throw new IllegalArgumentException(EXPECTED);
            });

        } catch (AssertionError err) {
            assertTrue(err.getMessage().contains("incorrect exception type"));
            return;
        }

        fail("test failed for incorrect exception type");
    }

    @Test
    public void test_MissingEx() {
        try {
            assertThrows(IllegalArgumentException.class, () -> {
            });

        } catch (AssertionError err) {
            assertTrue(err.getMessage().contains("missing exception"));
            return;
        }

        fail("test failed for missing exception");
    }

}
