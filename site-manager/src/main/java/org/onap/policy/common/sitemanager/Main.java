/*-
 * ============LICENSE_START=======================================================
 * site-manager
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.sitemanager;

/*
 * Site Manager argument list:
 *
 * none - dump help information show - dump information about all nodes ([site, nodetype,
 * resourceName], adminState, opState, availStatus, standbyStatus) The first 3 determine the sort
 * order. setAdminState [ -s <site> | -r <resourceName> ] <new-state> lock [ -s <site> | -r
 * <resourceName> ] unlock [ -s <site> | -r <resourceName> ]
 */

import static org.onap.policy.common.sitemanager.utils.Constants.OPERATIONAL_PERSISTENCE_UNIT;
import static org.onap.policy.common.sitemanager.utils.Constants.SITE_MANAGER_PROPERTIES_PROPERTY_NAME;
import static org.onap.policy.common.sitemanager.utils.ErrorMessages.HELP_STRING;
import static org.onap.policy.common.sitemanager.utils.ExtraCommandLineArgument.LOCK;
import static org.onap.policy.common.sitemanager.utils.ExtraCommandLineArgument.SET_ADMIN_STATE;
import static org.onap.policy.common.sitemanager.utils.ExtraCommandLineArgument.UNLOCK;
import static org.onap.policy.common.sitemanager.utils.JmxOpProcessor.jmxOp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import org.onap.policy.common.im.jpa.ResourceRegistrationEntity;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.onap.policy.common.sitemanager.data.service.DatabaseAccessService;
import org.onap.policy.common.sitemanager.data.service.DatabaseAccessServiceImpl;
import org.onap.policy.common.sitemanager.exception.IllegalCommandLineArgumentException;
import org.onap.policy.common.sitemanager.exception.MissingPropertyException;
import org.onap.policy.common.sitemanager.exception.NoMatchingEntryFoundException;
import org.onap.policy.common.sitemanager.exception.PropertyFileProcessingException;
import org.onap.policy.common.sitemanager.utils.CommandLineHelper;
import org.onap.policy.common.sitemanager.utils.PersistenceUnitPropertiesProvider;
import org.onap.policy.common.sitemanager.utils.Printable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the main entry point for Site Manager.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class.getName());

    // table mapping 'resourceName' to 'StateManagmentEntity'
    static Map<String, StateManagementEntity> stateManagementTable = new HashMap<>();

    // table mapping 'resourceName' to 'StateManagmentEntity'
    static Map<String, ResourceRegistrationEntity> resourceRegistrationTable = new HashMap<>();

    /**
     * Print out help information regarding command arguments.
     */
    private static String getHelpString() {
        return HELP_STRING;
    }

    /**
     * This is the main entry point
     *
     * @param args these are command-line arguments to 'siteManager'
     */
    public static void main(final String[] args) {
        final Printable printable = new PrintableImpl();
        new Main().process(args, printable);
    }

    public void process(final String[] args, final Printable printable) {
        try {
            final CommandLineHelper commandLineHelper = new CommandLineHelper(args, printable);

            if (commandLineHelper.isHelpArgumentSet()) {
                printable.println(getHelpString());
                System.exit(0);
            }

            if (!commandLineHelper.isValid()) {
                printable.println(getHelpString());
                System.exit(2);
            }
            process(commandLineHelper, printable);
        } catch (final IllegalCommandLineArgumentException illegalCommandLineArgumentException) {
            printable.println(illegalCommandLineArgumentException.getMessage());
            printable.println(getHelpString());
            System.exit(1);
        } catch (final PropertyFileProcessingException | MissingPropertyException exception) {
            System.exit(3);
        } catch (final NoMatchingEntryFoundException exception) {
            System.exit(4);
        }
    }

    private void process(final CommandLineHelper cmd, final Printable printable) {
        // fetch options, and remaining arguments
        final String sOption = cmd.getSite();
        final String rOption = cmd.getResourceName();
        final List<String> argList = cmd.getArgList();

        final String arg0 = argList.get(0);

        // read in properties used to access the database
        final String propertiesFileName = System.getProperty(SITE_MANAGER_PROPERTIES_PROPERTY_NAME);
        final Properties properties = PersistenceUnitPropertiesProvider.getProperties(propertiesFileName, printable);

        try (final DatabaseAccessService accessService =
                getDatabaseAccessService(OPERATIONAL_PERSISTENCE_UNIT, properties)) {

            final List<StateManagementEntity> stateManagementResultList =
                    accessService.getStateManagementEntities(rOption, sOption);
            final List<ResourceRegistrationEntity> resourceRegistrationResultList =
                    accessService.getResourceRegistrationEntities(rOption, sOption);

            // perform 'StateManagementEntity' query, and place matching entries
            // in 'stateManagementTable'
            for (final StateManagementEntity stateManagementEntity : stateManagementResultList) {
                stateManagementTable.put(stateManagementEntity.getResourceName(), stateManagementEntity);
            }

            // perform 'ResourceRegistrationQuery', and place matching entries
            // in 'resourceRegistrationTable' ONLY if there is also an associated
            // 'stateManagementTable' entry
            for (final ResourceRegistrationEntity resourceRegistrationEntity : resourceRegistrationResultList) {
                final String resourceName = resourceRegistrationEntity.getResourceName();
                if (stateManagementTable.get(resourceName) != null) {
                    // only include entries that have a corresponding
                    // state table entry -- silently ignore the rest
                    resourceRegistrationTable.put(resourceName, resourceRegistrationEntity);
                }
            }

            if (resourceRegistrationTable.size() == 0) {
                final String message = arg0 + ": No matching entries";
                printable.println(message);
                throw new NoMatchingEntryFoundException(message);
            }

            if (SET_ADMIN_STATE.getValue().equalsIgnoreCase(arg0)) {
                // update admin state on all of the nodes
                final String adminState = argList.get(1);
                // iterate over all matching 'ResourceRegistrationEntity' instances
                for (final ResourceRegistrationEntity r : resourceRegistrationTable.values()) {
                    // we know the corresponding 'StateManagementEntity' exists --
                    // 'ResourceRegistrationEntity' entries without a matching
                    // 'StateManagementEntity' entry were not placed in the table
                    final StateManagementEntity s = stateManagementTable.get(r.getResourceName());

                    // update the admin state, and save the changes
                    s.setAdminState(adminState);
                }
                accessService.persist(stateManagementTable.values());

            } else if (LOCK.getValue().equalsIgnoreCase(arg0) || UNLOCK.getValue().equalsIgnoreCase(arg0)) {
                // these use the JMX interface
                for (final ResourceRegistrationEntity r : resourceRegistrationTable.values()) {
                    // lock or unlock the entity
                    jmxOp(arg0, r, printable);

                    // change should be reflected in 'adminState'
                    accessService.refreshEntity(stateManagementTable.get(r.getResourceName()));
                }
            }
        } catch (final Exception exception) {
            printable.println(exception.getMessage());
        }

        // display all entries
        display();
    }

    /**
     * Compare two strings, either of which may be null
     *
     * @param first the first string
     * @param second the second string
     * @return a negative value if s1<s2, 0 if they are equal, and positive if s1>s2
     */
    private static int stringCompare(final String first, final String second) {
        if (first == null ^ second == null) {
            return (first == null) ? -1 : 1;
        }

        if (first == null && second == null) {
            return 0;
        }

        return first.compareTo(second);
    }

    /**
     * Update an array of 'length' fields using an array of Strings, any of which may be 'null'.
     * This method is used to determine the field width of each column in a tabular dump.
     *
     * @param current this is an array of length 7, containing the current maximum lengths of each
     *        column in the tabular dump
     * @param s this is an array of length 7, containing the current String entry for each column
     */
    private static void updateLengths(final int[] current, final String[] s) {
        for (int i = 0; i < 7; i += 1) {
            final String str = s[i];
            final int newLength = (str == null ? 4 : str.length());
            if (current[i] < newLength) {
                // this column needs to be expanded
                current[i] = newLength;
            }
        }
    }

    /**
     * Ordered display -- dump out all of the entries, in
     */
    static void display() {
        final TreeSet<String[]> treeset = new TreeSet<>((final String[] r1, final String[] r2) -> {
            int rval = 0;

            // the first 3 columns are 'Site', 'NodeType', and 'ResourceName',
            // and are used to sort the entries
            for (int i = 0; i < 3; i += 1) {
                if ((rval = stringCompare(r1[i], r2[i])) != 0)
                    break;
            }
            return (rval);
        });

        final String[] labels = new String[] {"Site", "NodeType", "ResourceName", "AdminState", "OpState",
                "AvailStatus", "StandbyStatus"};
        final String[] underlines = new String[] {"----", "--------", "------------", "----------", "-------",
                "-----------", "-------------"};

        // each column needs to be at least wide enough to fit the column label
        final int[] lengths = new int[7];
        updateLengths(lengths, labels);

        // Go through the 'resourceRegistrationTable', and generate the
        // associated table row. Maximum column widths are updated, and the
        // entry is inserted into tree, which has the effect of sorting the
        // entries.
        for (final ResourceRegistrationEntity r : resourceRegistrationTable.values()) {
            final StateManagementEntity s = stateManagementTable.get(r.getResourceName());

            // these are the entries to be displayed for this row
            final String[] values = new String[] {r.getSite(), r.getNodeType(), r.getResourceName(), s.getAdminState(),
                    s.getOpState(), s.getAvailStatus(), s.getStandbyStatus()};

            treeset.add(values);
            updateLengths(lengths, values);
        }

        // generate format string
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i += 1) {
            sb.append('%').append(i + 1).append("$-").append(lengths[i]).append("s ");
        }
        sb.setCharAt(sb.length() - 1, '\n');
        final String formatString = sb.toString();

        // display column headers
        logger.info(formatString, (Object[]) labels);
        logger.info(formatString, (Object[]) underlines);

        // display all of the rows
        for (final String[] values : treeset) {
            logger.info(formatString, (Object[]) values);
        }
    }

    DatabaseAccessService getDatabaseAccessService(final String persistenceUnitName, final Properties properties) {
        return new DatabaseAccessServiceImpl(persistenceUnitName, properties);
    }

    private static class PrintableImpl implements Printable {

        @Override
        public void println(final String value) {
            System.out.println(value);
        }
    }
}
