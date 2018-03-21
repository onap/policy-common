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

import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.onap.policy.common.sitemanager.exception.IllegalCommandLineArgumentException;

public class CommandLineHelper {

    private static final String HELP_QUESTION_MARK_ARGUMENT_NAME = "?";

    private static final String HELP_ARGUMENT_NAME = "h";

    private static final String RESOURCE_ARGUMENT_NAME = "r";

    private static final String SITE_ARGUMENT_NAME = "s";

    private final CommandLine commandLine;

    private static final CommandLineParser PARSER = new DefaultParser();

    private Printable printable;

    public CommandLineHelper(final String[] args, final Printable printable) {
        this.commandLine = getCommandLine(getOptions(), args);
        this.printable = printable;
    }

    private Options getOptions() {
        final Options options = new Options();
        options.addOption(Option.builder(SITE_ARGUMENT_NAME).hasArg(true).desc("specify site").build());
        options.addOption(Option.builder(RESOURCE_ARGUMENT_NAME).hasArg(true).desc("specify resource name").build());
        options.addOption(Option.builder(HELP_ARGUMENT_NAME).hasArg(false).desc("display help").build());
        options.addOption(Option.builder(HELP_QUESTION_MARK_ARGUMENT_NAME).hasArg(false).desc("display help").build());

        return options;
    }

    public String getSite() {
        return commandLine.getOptionValue(SITE_ARGUMENT_NAME);
    }

    public String getResourceName() {
        return commandLine.getOptionValue(RESOURCE_ARGUMENT_NAME);
    }

    private CommandLine getCommandLine(final Options options, final String[] args) {
        try {
            return PARSER.parse(options, args);
        } catch (final ParseException | NullPointerException exception) {
            throw new IllegalCommandLineArgumentException(exception.getMessage(), exception);
        }
    }

    public boolean isValid() {
        // fetch options, and remaining arguments
        final String sOption = commandLine.getOptionValue(SITE_ARGUMENT_NAME);
        final String rOption = commandLine.getOptionValue(RESOURCE_ARGUMENT_NAME);
        final List<String> argList = commandLine.getArgList();

        // a number of commands require either the '-r' option or '-s' option
        final boolean optionLetterSpecified = (rOption != null || sOption != null);

        if (argList.isEmpty()) {
            printable.println(ErrorMessages.NO_COMMAND_SPECIFIED);
            return false;
        }
        // a number of commands require either the '-r' option or '-s' option
        final String arg0 = argList.get(0);
        final ExtraCommandLineArgument argument = ExtraCommandLineArgument.getExtraCommandLineArgument(arg0);

        if (!argument.isValid(argList, printable, optionLetterSpecified)) {
            return false;
        }

        if (sOption != null && rOption != null) {
            printable.println(arg0 + ErrorMessages.R_AND_S_OPTIONS_ARE_MUTUALLY_EXCLUSIVE);
            return false;
        }

        return true;
    }

    public boolean isHelpArgumentSet() {
        return commandLine.hasOption(HELP_ARGUMENT_NAME) || commandLine.hasOption(HELP_QUESTION_MARK_ARGUMENT_NAME);
    }

    public List<String> getArgList() {
        return commandLine.getArgList();
    }

}
