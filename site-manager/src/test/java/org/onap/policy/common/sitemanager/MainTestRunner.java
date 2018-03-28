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

package org.onap.policy.common.sitemanager;

import java.security.Permission;

public class MainTestRunner {

    private SecurityManager savedSecurityManager;
    private final ExitCodeSecurityManager exitCodeSecurityManager = new ExitCodeSecurityManager();
    private boolean isSetUp = false;

    /**
     * Set {@link SecurityManager} to {@link ExitCodeTestException}.
     */
    public void setUp() {
        if (!isSetUp) {
            savedSecurityManager = System.getSecurityManager();
            System.setSecurityManager(exitCodeSecurityManager);
            isSetUp = true;
        }
    }

    /**
     * Restore save {@link SecurityManager}.
     */
    public void destroy() {
        if (isSetUp) {
            System.setSecurityManager(savedSecurityManager);
            isSetUp = false;
        }
    }

    class ExitCodeTestException extends SecurityException {
        private static final long serialVersionUID = 2690072276259821984L;
        public final int exitCode;

        public ExitCodeTestException(final int exitCode) {
            super("Test specific exit code exception to handle System.exit, value: " + exitCode);
            this.exitCode = exitCode;
        }
    }

    private class ExitCodeSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(final Permission perm) {}

        @Override
        public void checkPermission(final Permission perm, final Object context) {}

        @Override
        public void checkExit(final int status) {
            super.checkExit(status);
            throw new ExitCodeTestException(status);
        }

    }
}
