/*-
 * ============LICENSE_START=======================================================
 * site-manager
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.common.sitemanager.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.onap.policy.common.sitemanager.utils.ExtraCommandLineArgument.LOCK;
import static org.onap.policy.common.sitemanager.utils.ExtraCommandLineArgument.SET_ADMIN_STATE;
import static org.onap.policy.common.sitemanager.utils.ExtraCommandLineArgument.SHOW;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

public class ExtraCommandLineArgumentTest {

    private static final String EMPTY_STRING = "";
    private static final String RESOURCE_NAME = "RESOURCE_NAME";

    @Test
    public void test_ExtraCommandLineArgument_ShowValidArgs() throws IOException {
        final PrintableImpl printable = new PrintableImpl();

        final ExtraCommandLineArgument objUnderTest =
                ExtraCommandLineArgument.getExtraCommandLineArgument(SHOW.getValue());

        assertTrue(objUnderTest.isValid(Arrays.asList(SHOW.getValue()), printable));
        assertTrue(printable.getResult().isEmpty());
    }

    @Test
    public void test_ExtraCommandLineArgument_SetAdminStateValidArgs() throws IOException {
        final PrintableImpl printable = new PrintableImpl();

        final ExtraCommandLineArgument objUnderTest =
                ExtraCommandLineArgument.getExtraCommandLineArgument(SET_ADMIN_STATE.getValue());

        assertTrue(objUnderTest.isValid(Arrays.asList(SET_ADMIN_STATE.getValue(), RESOURCE_NAME), printable, true));
        assertTrue(printable.getResult().isEmpty());
    }

    @Test
    public void test_ExtraCommandLineArgument_LockStateValidArgs() throws IOException {
        final PrintableImpl printable = new PrintableImpl();

        final ExtraCommandLineArgument objUnderTest =
                ExtraCommandLineArgument.getExtraCommandLineArgument(LOCK.getValue());

        assertTrue(objUnderTest.isValid(Arrays.asList(LOCK.getValue()), printable, true));
        assertTrue(printable.getResult().isEmpty());
    }

    @Test
    public void test_ExtraCommandLineArgument_InValidArgs() throws IOException {
        final PrintableImpl printable = new PrintableImpl();

        final ExtraCommandLineArgument objUnderTest =
                ExtraCommandLineArgument.getExtraCommandLineArgument(EMPTY_STRING);

        assertTrue(objUnderTest.equals(ExtraCommandLineArgument.INVALID));
        assertFalse(objUnderTest.isValid(Arrays.asList(EMPTY_STRING), printable, false));
        assertFalse(printable.getResult().isEmpty());
        assertEquals(Arrays.asList(ErrorMessages.UNKNOWN_COMMAND), printable.getResult());
    }

}
