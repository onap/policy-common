/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.resources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for manipulating directories.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectoryUtils {
    private static final Logger logger = LoggerFactory.getLogger(DirectoryUtils.class);

    /**
     * Creates a directory file, only accessible by the owner.
     *
     * @param prefix file name prefix
     * @return a new, temporary directory
     * @throws IOException if an error occurs
     */
    public static Path createTempDirectory(String prefix) throws IOException {
        /*
         * Disabling sonar, as the code below sets the permissions, just as sonar
         * suggests it be fixed.
         */
        var path = Files.createTempDirectory(prefix);           // NOSONAR
        logger.info("created temporary directory, {}", path);

        var file = path.toFile();

        TextFileUtils.setDefaultPermissions(file);

        // ensure nothing has been written to it yet
        FileUtils.cleanDirectory(file);

        return path;
    }
}
