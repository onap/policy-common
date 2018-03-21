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

public enum ExtraCommandLineArgument {
    SHOW("show") {

        @Override
        public boolean isValid(final List<String> argList, final Printable printable) {
            if (argList.size() != 1) {
                printable.println(ErrorMessages.SHOW_EXTRA_ARGUMENTS);
                return false;
            }
            return true;
        }

        @Override
        public boolean isValid(final List<String> argList, final Printable printable,
                final boolean optionLetterSpecified) {
            return isValid(argList, printable);
        }
    },
    SET_ADMIN_STATE("setAdminState") {
        @Override
        public boolean isValid(final List<String> argList, final Printable printable,
                final boolean optionLetterSpecified) {
            boolean isValid = true;
            switch (argList.size()) {
                case 1:
                    printable.println(ErrorMessages.SET_ADMIN_STATE_MISSING_NEW_STATE_VALUE);
                    isValid = false;
                    break;
                case 2:
                    isValid = true;
                    break;
                default:
                    printable.println(ErrorMessages.SET_ADMIN_STATE_EXTRA_ARGUMENTS);
                    isValid = false;
            }
            if (!optionLetterSpecified) {
                printable.println(ErrorMessages.SET_ADMIN_STATE_EITHER_S_OR_R_OPTION_IS_NEEDED);
                return false;
            }

            return isValid;
        }

    },
    LOCK("lock") {

        @Override
        public boolean isValid(final List<String> argList, final Printable printable,
                final boolean optionLetterSpecified) {
            boolean isValid = true;
            if (argList.size() != 1) {
                printable.println(ErrorMessages.LOCK_EXTRA_ARGUMENTS);
                isValid = false;
            }
            if (!optionLetterSpecified) {
                printable.println(ErrorMessages.LOCK_EITHER_S_OR_R_OPTION_IS_NEEDED);
                isValid = false;
            }
            return isValid;
        }
    },
    UNLOCK("unlock") {

        @Override
        public boolean isValid(final List<String> argList, final Printable printable,
                final boolean optionLetterSpecified) {
            boolean isValid = true;
            if (argList.size() != 1) {
                printable.println(ErrorMessages.UNLOCK_EXTRA_ARGUMENTS);
                isValid = false;
            }
            if (!optionLetterSpecified) {
                printable.println(ErrorMessages.UNLOCK_EITHER_S_OR_R_OPTION_IS_NEEDED);
                isValid = false;
            }
            return isValid;
        }
    },
    INVALID("") {
        @Override
        public boolean isValid(final List<String> argList, final Printable printable,
                final boolean optionLetterSpecified) {
            printable.println(argList.get(0) + ErrorMessages.UNKNOWN_COMMAND);
            return false;
        }
    };

    private String value;

    private ExtraCommandLineArgument(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ExtraCommandLineArgument getExtraCommandLineArgument(final String value) {
        for (final ExtraCommandLineArgument argument : ExtraCommandLineArgument.values()) {
            if (argument.getValue().equals(value)) {
                return argument;
            }
        }
        return ExtraCommandLineArgument.INVALID;
    }

    public boolean isValid(final List<String> argList, final Printable printable) {
        return isValid(argList, printable, false);
    }

    public abstract boolean isValid(final List<String> argList, final Printable printable,
            final boolean optionLetterSpecified);

}
