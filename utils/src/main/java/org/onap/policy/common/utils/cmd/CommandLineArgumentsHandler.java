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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.utils.resources.ResourceUtils;

/**
 * Class for command line common processing.
 *
 * @author Adheli Tavares (adheli.tavares@est.tech)
 *
 */
public class CommandLineArgumentsHandler {
    private static final String FILE_MESSAGE_PREAMBLE = " file \"";
    private static final int HELP_LINE_LENGTH = 120;

    private final Options options;

    private final String helpClassName;
    private final String component;

    @Getter
    @Setter
    private String configurationFilePath = null;

    @Getter
    @Setter
    private String propertyFilePath = null;

    @Getter
    private CommandLine commandLine = null;

    /**
     * Construct the options with default values for the CLI editor.
     */
    protected CommandLineArgumentsHandler(String helpClassName, String component) {
        this.helpClassName = helpClassName;
        this.component = component;
        //@formatter:off
        options = new Options();
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("outputs the usage of this command")
                .required(false)
                .type(Boolean.class)
                .build());
        options.addOption(Option.builder("v")
                .longOpt("version")
                .desc("outputs the version of " + this.component)
                .required(false)
                .type(Boolean.class)
                .build());
        options.addOption(Option.builder("c")
                .longOpt("config-file")
                .desc(String.format("the full path to the configuration file to use, "
                        + "the configuration file must be a Json file containing the %s parameters", this.component))
                .hasArg().argName("CONFIG_FILE")
                .required(false)
                .type(String.class)
                .build());
        //@formatter:on
    }

    /**
     * Construct the options for the CLI editor with extra options.
     */
    public CommandLineArgumentsHandler(String helpClassName, String component, Option... customOptions) {
        this(helpClassName, component);
        if (customOptions != null && customOptions.length > 0) {
            for (Option option : customOptions) {
                options.addOption(option);
            }
        }
    }

    /**
     * Construct the options with brand new options for the CLI editor.
     */
    public CommandLineArgumentsHandler(String helpClassName, String component, Options options) {
        this.options = options;
        this.helpClassName = helpClassName;
        this.component = component;
    }

    /**
     * Parse the command line options.
     *
     * @param args The command line arguments
     * @return a string with a message for help and version, or null if there is no message
     * @throws CommandLineException on command argument errors
     */
    public String parse(final String[] args) throws CommandLineException {
        // Clear all our arguments
        setConfigurationFilePath(null);
        setPropertyFilePath(null);

        try {
            commandLine = new DefaultParser().parse(options, args);
        } catch (final ParseException | NullPointerException e) {
            throw new CommandLineException("invalid command line arguments specified", e);
        }

        // Arguments left over after Commons CLI does its stuff
        final String[] remainingArgs = removeEmptyValues(commandLine.getArgs());

        if (remainingArgs.length > 0) {
            throw new CommandLineException("too many command line arguments specified: " + Arrays.toString(args));
        }

        if (commandLine.hasOption('h')) {
            return help();
        }

        if (commandLine.hasOption('v')) {
            return version();
        }

        if (commandLine.hasOption('c')) {
            setConfigurationFilePath(commandLine.getOptionValue('c'));
        }

        if (commandLine.hasOption('p')) {
            setPropertyFilePath(commandLine.getOptionValue('p'));
        }

        return null;
    }

    /**
     * Validate the command line options.
     *
     * @throws CommandLineException on command argument validation errors
     */
    public void validate() throws CommandLineException {
        validateReadableFile(this.component + " configuration", configurationFilePath);
    }

    /**
     * Print version information for policy distribution.
     *
     * @return the version string
     */
    public String version() {
        return ResourceUtils.getResourceAsString("version.txt");
    }

    /**
     * Print help information for policy distribution.
     *
     * @return the help string
     */
    public String help() {
        final HelpFormatter helpFormatter = new HelpFormatter();
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        final String cmdLineSyntax = this.helpClassName + " [options...]";

        helpFormatter.printHelp(printWriter, HELP_LINE_LENGTH, cmdLineSyntax, "options", options, 0, 0, "");

        return stringWriter.toString();
    }

    /**
     * Gets the full expanded configuration file path.
     *
     * @return the configuration file path
     */
    public String getFullConfigurationFilePath() {
        return ResourceUtils.getFilePath4Resource(getConfigurationFilePath());
    }

    /**
     * Check set configuration file path.
     *
     * @return true, if check set configuration file path
     */
    public boolean checkSetConfigurationFilePath() {
        return StringUtils.isNotBlank(getConfigurationFilePath());
    }

    /**
     * Gets the full expanded property file path.
     *
     * @return the property file path
     */
    public String getFullPropertyFilePath() {
        return ResourceUtils.getFilePath4Resource(getPropertyFilePath());
    }

    /**
     * Check set property file path.
     *
     * @return true, if check set property file path
     */
    public boolean checkSetPropertyFilePath() {
        return StringUtils.isNotBlank(getPropertyFilePath());
    }

    /**
     * Validate readable file.
     *
     * @param fileTag the file tag
     * @param fileName the file name
     * @throws CommandLineException on the file name passed as a parameter
     */
    protected void validateReadableFile(final String fileTag, final String fileName) throws CommandLineException {
        if (StringUtils.isBlank(fileName)) {
            throw new CommandLineException(fileTag + " file was not specified as an argument");
        }

        // The file name refers to a resource on the local file system
        final URL fileUrl = ResourceUtils.getUrl4Resource(fileName);
        if (fileUrl == null) {
            throw new CommandLineException(fileTag + FILE_MESSAGE_PREAMBLE + fileName + "\" does not exist");
        }

        try {
            Path path = Path.of(fileUrl.toURI());
            if (!Files.isRegularFile(path)) {
                throw new CommandLineException(fileTag + FILE_MESSAGE_PREAMBLE + fileName + "\" is not a normal file");
            }
            if (!Files.isReadable(path)) {
                throw new CommandLineException(fileTag + FILE_MESSAGE_PREAMBLE + fileName + "\" is unreadable");
            }
        } catch (URISyntaxException e) {
            throw new CommandLineException("Error parsing " + fileUrl.toString(), e);
        }

    }

    /**
     * Checks if args has any null or empty value after parsing.
     *
     * @param args remaining args from CLI parse.
     */
    private String[] removeEmptyValues(String[] args) {
        return Arrays.stream(args).filter(StringUtils::isNotBlank).toArray(String[]::new);
    }
}
