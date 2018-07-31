/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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

public class ExceptionTest {

    @Test
    public void testParameterException() {
        assertEquals("Parameter Exception", new ParameterException("Parameter Exception").getMessage());

        String exceptionObject = "Exception Object";
        assertEquals("Exception Object",
                        new ParameterException("Parameter Exception", exceptionObject).getObject().toString());

        Exception testException = new IOException("IO Exception");
        assertEquals("Parameter Exception\ncaused by: Parameter Exception\ncaused by: IO Exception",
                        new ParameterException("Parameter Exception", testException, exceptionObject)
                                        .getCascadedMessage());
    }

    @Test
    public void testParameterRuntimeException() {
        assertEquals("Parameter Exception", new ParameterRuntimeException("Parameter Exception").getMessage());

        String exceptionObject = "Exception Object";
        assertEquals("Exception Object",
                        new ParameterRuntimeException("Parameter Exception", exceptionObject).getObject().toString());

        Exception testException = new IOException("IO Exception");
        assertEquals("Parameter Exception\ncaused by: Parameter Exception\ncaused by: IO Exception",
                        new ParameterRuntimeException("Parameter Exception", testException, exceptionObject)
                                        .getCascadedMessage());
    }
}
