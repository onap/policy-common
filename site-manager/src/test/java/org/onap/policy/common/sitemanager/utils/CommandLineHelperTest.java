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
import static org.onap.policy.common.sitemanager.utils.ExtraCommandLineArgument.UNLOCK;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.onap.policy.common.sitemanager.exception.IllegalCommandLineArgumentException;

public class CommandLineHelperTest {

    private static final String STATE_NAME = "STATE_NAME";
    private static final String RESOURCE_NAME = "RESOURCE_NAME";

    @Test
    public void test_CommandLineHelper_emptyArgs() throws IOException {
        final String[] args = new String[] {};
        final PrintableImpl printable = new PrintableImpl();
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertFalse(objUnderTest.isValid());
        assertEquals(Arrays.asList(ErrorMessages.NO_COMMAND_SPECIFIED), printable.getResult());
    }

    @Test
    public void test_CommandLineHelper_NullArgs() throws IOException {
        final String[] args = null;
        final PrintableImpl printable = new PrintableImpl();
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertFalse(objUnderTest.isValid());
        assertEquals(Arrays.asList(ErrorMessages.NO_COMMAND_SPECIFIED), printable.getResult());
    }

    @Test
    public void test_CommandLineHelper_ShowValidArgs() throws IOException {
        final String[] args = new String[] {SHOW.getValue(), "-s", RESOURCE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertTrue(objUnderTest.isValid());
        assertTrue(printable.getResult().isEmpty());
    }

    @Test
    public void test_CommandLineHelper_LockValidArgs() throws IOException {
        final String[] args = new String[] {LOCK.getValue(), "-s", RESOURCE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertTrue(objUnderTest.isValid());
        assertTrue(printable.getResult().isEmpty());
    }

    @Test
    public void test_CommandLineHelper_UnLockValidArgs() throws IOException {
        final String[] args = new String[] {UNLOCK.getValue(), "-s", RESOURCE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertTrue(objUnderTest.isValid());
        assertTrue(printable.getResult().isEmpty());
    }

    @Test
    public void test_CommandLineHelper_SetAdminStateValidArgs() throws IOException {
        final String[] args = new String[] {SET_ADMIN_STATE.getValue(), RESOURCE_NAME, "-r", RESOURCE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertTrue(objUnderTest.isValid());
        assertTrue(printable.getResult().isEmpty());
        assertEquals(RESOURCE_NAME, objUnderTest.getResourceName());
    }

    @Test
    public void test_CommandLineHelper_SetAdminStateWithStateValidArgs() throws IOException {
        final String[] args = new String[] {SET_ADMIN_STATE.getValue(), RESOURCE_NAME, "-s", STATE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertTrue(objUnderTest.isValid());
        assertTrue(printable.getResult().isEmpty());
        assertEquals(STATE_NAME, objUnderTest.getSite());

        assertEquals(Arrays.asList(SET_ADMIN_STATE.getValue(), RESOURCE_NAME), objUnderTest.getArgList());
    }

    @Test
    public void test_CommandLineHelper_HelpValidArgs() throws IOException {
        final String[] args = new String[] {"-h"};
        final PrintableImpl printable = new PrintableImpl();
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);
        assertTrue(objUnderTest.isHelpArgumentSet());
    }

    @Test(expected = IllegalCommandLineArgumentException.class)
    public void test_CommandLineHelper_invaidArgs_printHelp() throws IOException {

        final PrintableImpl printable = new PrintableImpl();

        final String[] args = new String[] {"---", ""};
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertFalse(objUnderTest.isValid());

    }

    @Test
    public void test_CommandLineHelper_invaidArgsMissingAttributes_printHelp() throws IOException {

        final PrintableImpl printable = new PrintableImpl();
        final String[] args = new String[] {SET_ADMIN_STATE.getValue(), "-s", RESOURCE_NAME};
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertFalse(objUnderTest.isValid());
        final List<String> actualMessages = printable.getResult();

        assertEquals(Arrays.asList(ErrorMessages.SET_ADMIN_STATE_MISSING_NEW_STATE_VALUE), actualMessages);

    }

    @Test
    public void test_CommandLineHelper_invaidShowArgsExtraAttributes_printHelp() throws IOException {

        final PrintableImpl printable = new PrintableImpl();
        final String[] args = new String[] {SHOW.getValue(), RESOURCE_NAME};
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertFalse(objUnderTest.isValid());
        final List<String> actualMessages = printable.getResult();

        assertEquals(Arrays.asList(ErrorMessages.SHOW_EXTRA_ARGUMENTS), actualMessages);

    }

    @Test
    public void test_CommandLineHelper_invaidLockArgsExtraAttributes_printHelp() throws IOException {

        final PrintableImpl printable = new PrintableImpl();
        final String[] args = new String[] {LOCK.getValue(), RESOURCE_NAME};
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertFalse(objUnderTest.isValid());
        final List<String> actualMessages = printable.getResult();

        assertEquals(
                Arrays.asList(ErrorMessages.LOCK_EXTRA_ARGUMENTS, ErrorMessages.LOCK_EITHER_S_OR_R_OPTION_IS_NEEDED),
                actualMessages);

    }

    @Test
    public void test_CommandLineHelper_invaidUnLockArgsExtraAttributes_printHelp() throws IOException {

        final PrintableImpl printable = new PrintableImpl();
        final String[] args = new String[] {UNLOCK.getValue(), RESOURCE_NAME};
        final CommandLineHelper objUnderTest = new CommandLineHelper(args, printable);

        assertFalse(objUnderTest.isValid());
        final List<String> actualMessages = printable.getResult();

        assertEquals(Arrays.asList(ErrorMessages.UNLOCK_EXTRA_ARGUMENTS,
                ErrorMessages.UNLOCK_EITHER_S_OR_R_OPTION_IS_NEEDED), actualMessages);

    }

}
