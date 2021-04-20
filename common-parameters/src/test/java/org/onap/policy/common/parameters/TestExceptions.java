/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import java.io.IOException;
import org.junit.Test;

public class TestExceptions {
    private static final String PARAMETER_EXCEPTION = "Parameter Exception";
    private static final String EXCEPTION_OBJECT = "Exception Object";

    @Test
    public void testParameterException() {
        assertEquals(PARAMETER_EXCEPTION, new ParameterException(PARAMETER_EXCEPTION).getMessage());

        assertEquals(EXCEPTION_OBJECT,
                        new ParameterException(PARAMETER_EXCEPTION, EXCEPTION_OBJECT).getObject().toString());

        Exception testException = new IOException("IO Exception");
        assertEquals("Parameter Exception\ncaused by: Parameter Exception\ncaused by: IO Exception",
                        new ParameterException(PARAMETER_EXCEPTION, testException, EXCEPTION_OBJECT)
                                        .getCascadedMessage());
    }

    @Test
    public void testParameterRuntimeException() {
        assertEquals(PARAMETER_EXCEPTION, new ParameterRuntimeException(PARAMETER_EXCEPTION).getMessage());

        assertEquals(EXCEPTION_OBJECT,
                        new ParameterRuntimeException(PARAMETER_EXCEPTION, EXCEPTION_OBJECT).getObject().toString());

        Exception testException = new IOException("IO Exception");
        assertEquals("Parameter Exception\ncaused by: Parameter Exception\ncaused by: IO Exception",
                        new ParameterRuntimeException(PARAMETER_EXCEPTION, testException, EXCEPTION_OBJECT)
                                        .getCascadedMessage());
    }
}
