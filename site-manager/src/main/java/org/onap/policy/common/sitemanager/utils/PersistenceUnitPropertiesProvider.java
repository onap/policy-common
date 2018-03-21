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

import static org.onap.policy.common.sitemanager.utils.Constants.JDBC_DRIVER_PROPERTY_NAME;
import static org.onap.policy.common.sitemanager.utils.Constants.JDBC_PASSWORD_PROPERTY_NAME;
import static org.onap.policy.common.sitemanager.utils.Constants.JDBC_URL_PROPERTY_NAME;
import static org.onap.policy.common.sitemanager.utils.Constants.JDBC_USER_PROPERTY_NAME;
import static org.onap.policy.common.sitemanager.utils.ErrorMessages.SITE_MANAGER_PROPERY_FILE_MISSING_PROPERTY;
import static org.onap.policy.common.sitemanager.utils.ErrorMessages.SITE_MANAGER_PROPERY_FILE_NOT_DEFINED_MESSAGE;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.onap.policy.common.sitemanager.exception.MissingPropertyException;
import org.onap.policy.common.sitemanager.exception.PropertyFileProcessingException;

public class PersistenceUnitPropertiesProvider {

    private PersistenceUnitPropertiesProvider() {
        super();
    }

    public static Properties getProperties(final String propertiesFileName, final Printable printable) {
        if (propertiesFileName == null) {
            printable.println(SITE_MANAGER_PROPERY_FILE_NOT_DEFINED_MESSAGE);
            printable.println(SITE_MANAGER_PROPERY_FILE_MISSING_PROPERTY);
            throw new PropertyFileProcessingException("Property file name is null :" + propertiesFileName);
        }

        final Path filePath = Paths.get(propertiesFileName);
        final Properties properties = getProperties(filePath, printable);

        // verify that we have all of the properties needed
        if (isNotValid(properties, JDBC_DRIVER_PROPERTY_NAME, JDBC_URL_PROPERTY_NAME, JDBC_USER_PROPERTY_NAME,
                JDBC_PASSWORD_PROPERTY_NAME)) {
            // one or more missing properties
            printable.println(SITE_MANAGER_PROPERY_FILE_MISSING_PROPERTY);
            throw new MissingPropertyException("missing mandatory attributes");
        }
        return properties;
    }

    private static boolean isNotValid(final Properties properties, final String... values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (final String val : values) {
            if (properties.get(val) == null) {
                return true;
            }
        }
        return false;
    }

    private static Properties getProperties(final Path filePath, final Printable printable) {
        if (!filePath.toFile().exists()) {
            printable.println(SITE_MANAGER_PROPERY_FILE_NOT_DEFINED_MESSAGE);
            printable.println(SITE_MANAGER_PROPERY_FILE_MISSING_PROPERTY);
            throw new PropertyFileProcessingException("Property file not found");
        }

        final Properties properties = new Properties();
        try (final BufferedReader bufferedReader = Files.newBufferedReader(filePath);) {
            properties.load(bufferedReader);
        } catch (final Exception exception) {
            printable.println("Exception loading properties: " + exception);
            printable.println(ErrorMessages.SITE_MANAGER_PROPERY_FILE_NOT_DEFINED_MESSAGE);
            printable.println(ErrorMessages.SITE_MANAGER_PROPERY_FILE_MISSING_PROPERTY);
            throw new PropertyFileProcessingException("Exception loading properties: ", exception);
        }
        return properties;
    }

}
