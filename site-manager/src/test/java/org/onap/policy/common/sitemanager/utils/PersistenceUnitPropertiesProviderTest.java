/*-
 * ============LICENSE_START=======================================================
 * site-manager
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.policy.common.sitemanager.exception.MissingPropertyException;
import org.onap.policy.common.sitemanager.exception.PropertyFileProcessingException;

public class PersistenceUnitPropertiesProviderTest {
    private static final String PROPERTIES_FILE_NAME = "file.properties";

    private static final String COMMENTS = "";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test(expected = PropertyFileProcessingException.class)
    public void test_getProperties_null_throwException() {
        final Printable printable = new PrintableImpl();

        PersistenceUnitPropertiesProvider.getProperties(null, printable);
    }

    @Test(expected = PropertyFileProcessingException.class)
    public void test_getProperties_emptyString_throwException() {
        final Printable printable = new PrintableImpl();

        PersistenceUnitPropertiesProvider.getProperties("", printable);
    }

    @Test(expected = MissingPropertyException.class)
    public void test_getProperties_emptyPropertyFile_throwException() throws IOException {
        final Printable printable = new PrintableImpl();
        final File file = temporaryFolder.newFile(PROPERTIES_FILE_NAME);
        creatPropertyFile(file, new Properties());
        PersistenceUnitPropertiesProvider.getProperties(file.toString(), printable);
    }

    @Test(expected = MissingPropertyException.class)
    public void test_getProperties_PropertyFileWithMissingProperties_throwException() throws IOException {
        final Printable printable = new PrintableImpl();
        final File file = temporaryFolder.newFile(PROPERTIES_FILE_NAME);
        final Properties properties = new Properties();
        properties.put(Constants.JDBC_DRIVER_PROPERTY_NAME, "org.h2");
        creatPropertyFile(file, properties);
        PersistenceUnitPropertiesProvider.getProperties(file.toString(), printable);
    }

    @Test
    public void test_getProperties_PropertyFileValidProperties_throwException() throws IOException {
        final Printable printable = new PrintableImpl();
        final File file = temporaryFolder.newFile(PROPERTIES_FILE_NAME);

        final Properties properties = new Properties();
        properties.put(Constants.JDBC_DRIVER_PROPERTY_NAME, "Driver");
        properties.put(Constants.JDBC_URL_PROPERTY_NAME, "inMem:Database");
        properties.put(Constants.JDBC_USER_PROPERTY_NAME, "test");
        properties.put(Constants.JDBC_PASSWORD_PROPERTY_NAME, "test");
        creatPropertyFile(file, properties);
        final Properties actualProperties = PersistenceUnitPropertiesProvider.getProperties(file.toString(), printable);

        assertEquals(properties, actualProperties);
    }

    private void creatPropertyFile(final File file, final Properties properties) throws IOException {
        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(file.toPath());) {
            properties.store(bufferedWriter, COMMENTS);
        }
    }

}
