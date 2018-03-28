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

public class ErrorMessages {

    public static final String EXTRA_ARGUMENTS = "Extra arguments";

    public static final String EITHER_S_OR_R_OPTION_IS_NEEDED = "Either '-s' or '-r' option is needed";

    public static final String R_AND_S_OPTIONS_ARE_MUTUALLY_EXCLUSIVE = ":  'r' and 's' options are mutually exclusive";

    public static final String UNKNOWN_COMMAND = ": Unknown command";

    public static final String UNLOCK_EITHER_S_OR_R_OPTION_IS_NEEDED = "unlock: " + EITHER_S_OR_R_OPTION_IS_NEEDED;

    public static final String UNLOCK_EXTRA_ARGUMENTS = "unlock: " + EXTRA_ARGUMENTS;

    public static final String LOCK_EITHER_S_OR_R_OPTION_IS_NEEDED = "lock: " + EITHER_S_OR_R_OPTION_IS_NEEDED;

    public static final String LOCK_EXTRA_ARGUMENTS = "lock: " + EXTRA_ARGUMENTS;

    public static final String SET_ADMIN_STATE_EITHER_S_OR_R_OPTION_IS_NEEDED =
            "setAdminState: " + EITHER_S_OR_R_OPTION_IS_NEEDED;

    public static final String SET_ADMIN_STATE_EXTRA_ARGUMENTS = "setAdminState: " + EXTRA_ARGUMENTS;

    public static final String SET_ADMIN_STATE_MISSING_NEW_STATE_VALUE = "setAdminState: Missing <new-state> value";

    public static final String SHOW_EXTRA_ARGUMENTS = "show: " + EXTRA_ARGUMENTS;

    public static final String NO_COMMAND_SPECIFIED = "No command specified";

    public static final String NO_MATCHING_ENTRIES = ": No matching entries";

    public static final String HELP_STRING = "Usage:\n" + "    siteManager show [ -s <site> | -r <resourceName> ] :\n"
            + "        display node information\n" + "    siteManager setAdminState { -s <site> | -r <resourceName> }"
            + " <new-state> :\n" + "        update admin state on selected nodes\n"
            + "    siteManager lock { -s <site> | -r <resourceName> } :\n" + "        lock selected nodes\n"
            + "    siteManager unlock { -s <site> | -r <resourceName> } :\n" + "        unlock selected nodes\n";

    public static final String SITE_MANAGER_PROPERY_FILE_MISSING_PROPERTY =
            "The following properties need to be specified:\n\n" + "    javax.persistence.jdbc.driver -"
                    + " typically 'org.mariadb.jdbc.Driver'\n"
                    + "    javax.persistence.jdbc.url - URL referring to the database,\n"
                    + "        which typically has the form:" + " 'jdbc:mariadb://<host>:<port>/<db>'\n"
                    + "        ('<db>' is probably 'xacml' in this case)\n"
                    + "    javax.persistence.jdbc.user - the user id for accessing the" + " database\n"
                    + "    javax.persistence.jdbc.password - password for accessing the" + " database\n";

    public static final String SITE_MANAGER_PROPERY_FILE_NOT_DEFINED_MESSAGE =
            "'siteManager' needs to be passed the system property\n"
                    + "'siteManager.properties', which is the file name of a\n"
                    + "properties file containing database access information\n\n";

    private ErrorMessages() {
        super();
    }

}
