/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.common.utils.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.Test;

public class TestCommandLineArguments {
    private static final String FAKE_HELP_CLASS = "org.onap.policy.HelpClass";
    private static final String FAKE_COMPONENT = "fake policy cpm";
    private static final String TEST_CONFIG_FILE = "cmdFiles/configuration.json";
    private static final String TEST_PROPERTY_FILE = "cmdFiles/property.json";
    private static final String ERR_MSG_INVALID_ARGS = "invalid command line arguments specified";
    private static final String ERR_MSG_POLICY_CONFIG_FILE =
            "fake policy cpm configuration file was not specified as an argument";

    CommandLineArgumentsHandler testCmd = new CommandLineArgumentsHandler(FAKE_HELP_CLASS, FAKE_COMPONENT);

    @Test
    public void testVersion() throws CommandLineException {
        String[] version = {"-v"};
        assertThat(testCmd.parse(version)).startsWith("ONAP Version test.");
    }

    @Test
    public void testHelp() throws CommandLineException {
        String[] help = {"-h"};
        assertThat(testCmd.parse(help)).startsWith("usage: org.onap.policy.HelpClass [options...]");
    }

    @Test
    public void testParse() throws CommandLineException {
        String[] args = {"-c", TEST_CONFIG_FILE};
        testCmd.parse(args);

        assertTrue(testCmd.checkSetConfigurationFilePath());
        assertThat(testCmd.getFullConfigurationFilePath()).contains(TEST_CONFIG_FILE);
    }

    @Test
    public void testParse_ShouldThrowExceptionWithInvalidArguments() {
        String[] invalidArgs = {"-a"};
        assertThatThrownBy(() -> testCmd.parse(invalidArgs)).hasMessage(ERR_MSG_INVALID_ARGS)
                .hasRootCauseMessage("Unrecognized option: -a");
    }

    @Test
    public void testParse_ShouldThrowExceptionWithExtraArguments() {
        String[] remainingArgs = {"-c", TEST_CONFIG_FILE, "extraArgs"};
        String expectedErrorMsg =
                "too many command line arguments specified: [-c, cmdFiles/configuration.json, extraArgs]";
        assertThatThrownBy(() -> testCmd.parse(remainingArgs)).hasMessage(expectedErrorMsg);
    }

    @Test
    public void testParse_ShouldThrowExceptionWhenFileNameNull() {
        String[] nullArgs = {"-c", null};
        assertThatThrownBy(() -> testCmd.parse(nullArgs)).hasMessage(ERR_MSG_INVALID_ARGS).hasRootCauseMessage(null);
    }

    @Test
    public void testValidate() throws CommandLineException {
        String[] validConfigArgs = {"-c", TEST_CONFIG_FILE};
        testCmd.parse(validConfigArgs);
        assertThatCode(() -> testCmd.validate()).doesNotThrowAnyException();
    }

    @Test
    public void testValidate_ShouldThrowExceptionWhenConfigFileNotPresent() throws CommandLineException {
        String[] versionArgs = {"-v"};
        testCmd.parse(versionArgs);
        assertValidate(versionArgs, ERR_MSG_POLICY_CONFIG_FILE);
    }

    @Test
    public void testValidate_ShouldThrowExceptionWhenFileNameEmpty() {
        String[] argsOnlyKeyNoValue = {"-c", ""};
        assertValidate(argsOnlyKeyNoValue, ERR_MSG_POLICY_CONFIG_FILE);
        assertFalse(testCmd.checkSetConfigurationFilePath());
    }

    @Test
    public void testValidate_ShouldThrowExceptionWhenFileNameEmptySpace() {
        String[] argsOnlyKeyNoValue = {"-c", " "};
        assertValidate(argsOnlyKeyNoValue, ERR_MSG_POLICY_CONFIG_FILE);
        assertFalse(testCmd.checkSetConfigurationFilePath());
    }

    @Test
    public void testValidate_ShouldThrowExceptionWhenFileNameDoesNotExist() {
        String[] fileNameNotExistentArgs = {"-c", "someFileName.json"};
        assertValidate(fileNameNotExistentArgs,
                "fake policy cpm configuration file \"someFileName.json\" does not exist");
    }

    @Test
    public void testValidate_ShouldThrowExceptionWhenFileNameIsNotFile() {
        String[] folderAsFileNameArgs = {"-c", "src/test/resources"};
        assertValidate(folderAsFileNameArgs,
                "fake policy cpm configuration file \"src/test/resources\" is not a normal file");
    }

    @Test
    public void testAddExtraOptions() throws CommandLineException {
        Option extra = Option.builder("p").longOpt("property-file")
                .desc("the full path to the topic property file to use, the property file contains the "
                        + FAKE_COMPONENT + " properties")
                .hasArg().argName("PROP_FILE").required(false).type(String.class).build();

        CommandLineArgumentsHandler testCmdExtraOpt =
                new CommandLineArgumentsHandler(FAKE_HELP_CLASS, FAKE_COMPONENT, extra);

        String[] args = {"-p", TEST_PROPERTY_FILE};
        testCmdExtraOpt.parse(args);

        assertTrue(testCmdExtraOpt.checkSetPropertyFilePath());
        assertThat(testCmdExtraOpt.getFullPropertyFilePath()).contains(TEST_PROPERTY_FILE);

        String[] argsNoProperty = {"-p", ""};
        testCmdExtraOpt.parse(argsNoProperty);

        assertFalse(testCmdExtraOpt.checkSetPropertyFilePath());
    }

    @Test
    public void testNewOptions() throws CommandLineException {
        Options newOptions = new Options();
        newOptions.addOption(
                Option.builder("a").longOpt("fake-option").desc("the fake property to check command line parse")
                        .hasArg().argName("FAKE_OPT").required(false).type(String.class).build());

        CommandLineArgumentsHandler testCmdExtraOpt =
                new CommandLineArgumentsHandler(FAKE_HELP_CLASS, FAKE_COMPONENT, newOptions);

        String[] args = {"-a", "aaaa"};
        testCmdExtraOpt.parse(args);

        assertTrue(testCmdExtraOpt.getCommandLine().hasOption("a"));

        // should raise exception as -c is not present on options;
        // default options should've been replaced by constructor parameter.
        String[] argsError = {"-c", "aaaa.json"};
        assertThatThrownBy(() -> testCmdExtraOpt.parse(argsError)).hasMessage(ERR_MSG_INVALID_ARGS)
                .hasRootCauseMessage("Unrecognized option: -c");
    }

    private void assertValidate(String[] testArgs, String expectedErrorMsg) {
        try {
            testCmd.parse(testArgs);
        } catch (CommandLineException e) {
            fail(e.getMessage());
        }
        assertThatThrownBy(() -> testCmd.validate()).hasMessage(expectedErrorMsg);
    }
}
