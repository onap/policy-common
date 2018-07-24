/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2016-2018 Ericsson. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestAbstractParameters {

    @Test
    public void testAbstractParameters() {
        final LegalParameters parameters = new LegalParameters();
        assertNotNull(parameters);
        assertEquals("AbstractParameters [parameterClassName=org.onap.policy.common.parameters.LegalParameters]",
                        parameters.toString());

        assertEquals(LegalParameters.class, parameters.getParameterClass());
        assertEquals("org.onap.policy.common.parameters.LegalParameters", parameters.getParameterClassName());

        try {
            new IllegalParametersBadClass();
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("class not found for parameter class name \"somewhere.over.the.rainbow\"", e.getMessage());
        }

        try {
            new IllegalParametersNotParameters();
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("class \"java.lang.String\" is not an instance of \"org.onap.policy.common.parameters.IllegalParametersNotParameters\"",
                            e.getMessage());
        }
    }
}
