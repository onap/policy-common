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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.onap.policy.common.im.jpa.ResourceRegistrationEntity;
import org.onap.policy.common.im.jpa.StateManagementEntity;
import org.onap.policy.common.sitemanager.MainTestRunner.ExitCodeTestException;
import org.onap.policy.common.sitemanager.data.service.DatabaseAccessService;
import org.onap.policy.common.sitemanager.data.service.DatabaseAccessServiceImpl;
import org.onap.policy.common.sitemanager.utils.Constants;
import org.onap.policy.common.sitemanager.utils.ErrorMessages;
import org.onap.policy.common.sitemanager.utils.PrintableImpl;
import org.onap.policy.common.utils.jpa.EntityMgrCloser;
import org.onap.policy.common.utils.jpa.EntityTransCloser;

/**
 * This class contains the main entry point for Site Manager.
 */
public class MainTest {

    private static final String SITE_NAME = "SITE";

    private static final String PROPERTY_FILE_NAME = "siteManagerPropertyFile.properties";

    private static final String RESOURCE_LOCAL = "RESOURCE_LOCAL";

    private static final String DROP_AND_CREATE_TABLES = "drop-and-create-tables";

    private static final String NULL_STRING = "NULL";

    private static final String DATABASE_H2_PLATFORM = "org.eclipse.persistence.platform.database.H2Platform";

    private static final String ECLIPSELINK_ID_VALIDATION = "eclipselink.id-validation";

    private static final String ECLIPSELINK_TARGET_DATABASE = "eclipselink.target-database";

    private static final String DDL_GENERATION = "eclipselink.ddl-generation";

    private static final String TRANSACTION_TYPE = "javax.persistence.transactionType";

    private static final String RESOURCE_NAME = "RESOURCE_NAME";

    private static final String ADMIN_STATE_NEW_VALUE = "NEW_VALUE";

    private static final Date CURRENT_DATE = new Date();

    private static final String COMMENTS = "";

    private static final String DEFAULT_DB_URL_PREFIX = "jdbc:h2:mem:";

    private static final String IN_MEMORY_DB_URL = DEFAULT_DB_URL_PREFIX + "myDb";

    private static final String H2_DB_DRIVER = "org.h2.Driver";

    private final Properties properties = new Properties();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private EntityManagerFactory managerFactory;

    private final MainTestRunner testRunner = new MainTestRunner();

    @Before
    public void setUp() throws Exception {

        properties.put(Constants.JDBC_DRIVER_PROPERTY_NAME, H2_DB_DRIVER);
        properties.put(Constants.JDBC_URL_PROPERTY_NAME, IN_MEMORY_DB_URL);
        properties.put(Constants.JDBC_USER_PROPERTY_NAME, "test");
        properties.put(Constants.JDBC_PASSWORD_PROPERTY_NAME, "test");
        properties.put(TRANSACTION_TYPE, RESOURCE_LOCAL);
        properties.put(DDL_GENERATION, DROP_AND_CREATE_TABLES);
        properties.put(ECLIPSELINK_TARGET_DATABASE, DATABASE_H2_PLATFORM);
        properties.put(ECLIPSELINK_ID_VALIDATION, NULL_STRING);

        final File file = temporaryFolder.newFile(PROPERTY_FILE_NAME);
        creatPropertyFile(file, properties);
        System.setProperty(Constants.SITE_MANAGER_PROPERTIES_PROPERTY_NAME, file.toString());
        managerFactory = Persistence.createEntityManagerFactory(Constants.OPERATIONAL_PERSISTENCE_UNIT, properties);

        testRunner.setUp();
    }

    @After
    public void destroy() {
        Main.stateManagementTable.clear();
        Main.resourceRegistrationTable.clear();
        testRunner.destroy();
        properties.clear();;
        if (managerFactory.isOpen()) {
            managerFactory.close();
        }
    }

    @Test
    public void test_process_initializitonWithOutException() throws IOException {

        persist(managerFactory.createEntityManager(), getStateManagementEntity());

        persist(managerFactory.createEntityManager(), getResourceRegistrationEntity());

        final String[] args = new String[] {"show"};
        final PrintableImpl printable = new PrintableImpl();
        final Main objUnderTest = getMain();
        objUnderTest.process(args, printable);

        assertEquals(1, Main.resourceRegistrationTable.size());
        assertEquals(1, Main.stateManagementTable.size());
        assertTrue(printable.getResult().isEmpty());

    }

    @Test
    public void test_process_setAdminStateWithResourceNameArgument() throws IOException {
        persist(managerFactory.createEntityManager(), getStateManagementEntity());
        persist(managerFactory.createEntityManager(), getResourceRegistrationEntity());

        final String[] args = new String[] {"setAdminState", "-r", RESOURCE_NAME, ADMIN_STATE_NEW_VALUE};
        final PrintableImpl printable = new PrintableImpl();
        final Main objUnderTest = getMain();
        objUnderTest.process(args, printable);

        assertEquals(1, Main.resourceRegistrationTable.size());
        assertEquals(1, Main.stateManagementTable.size());
        assertTrue(printable.getResult().isEmpty());

        final List<StateManagementEntity> execute = execute(managerFactory.createEntityManager(),
                StateManagementEntity.class, Constants.STATE_MANAGEMENT_QUERY);

        assertEquals(ADMIN_STATE_NEW_VALUE, execute.get(0).getAdminState());

    }

    @Test
    public void test_process_setAdminStateWithStateArgument() throws IOException {
        persist(managerFactory.createEntityManager(), getStateManagementEntity());
        persist(managerFactory.createEntityManager(), getResourceRegistrationEntity());

        final String[] args = new String[] {"setAdminState", "-s", SITE_NAME, RESOURCE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        final Main objUnderTest = getMain();
        objUnderTest.process(args, printable);

        assertEquals(1, Main.resourceRegistrationTable.size());
        assertEquals(1, Main.stateManagementTable.size());
        final List<String> result = printable.getResult();
        assertTrue(result.isEmpty());

    }

    @Test
    public void test_process_propertyFileNotAvailable() throws IOException {

        final File file = temporaryFolder.newFile("New" + PROPERTY_FILE_NAME);
        System.setProperty(Constants.SITE_MANAGER_PROPERTIES_PROPERTY_NAME, file.toString());

        final String[] args = new String[] {"setAdminState", "-s", SITE_NAME, RESOURCE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        try {
            final Main objUnderTest = getMain();
            objUnderTest.process(args, printable);
            fail("ExitCodeTestException must be thrown in MainTestRunner class when System.exit() is called in Main class");
        } catch (final ExitCodeTestException exitCodeTestException) {
            assertEquals(3, exitCodeTestException.exitCode);

        }
        assertTrue(Main.stateManagementTable.isEmpty());
        assertTrue(Main.resourceRegistrationTable.isEmpty());
        assertFalse(printable.getResult().isEmpty());

    }

    @Test
    public void test_process_emptyPropertyFile() throws IOException {

        final File file = temporaryFolder.newFile("New" + PROPERTY_FILE_NAME);
        System.setProperty(Constants.SITE_MANAGER_PROPERTIES_PROPERTY_NAME, file.toString());
        creatPropertyFile(file, new Properties());
        final String[] args = new String[] {"setAdminState", "-s", SITE_NAME, RESOURCE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        try {
            final Main objUnderTest = getMain();
            objUnderTest.process(args, printable);
            fail("ExitCodeTestException must be thrown in MainTestRunner class when System.exit() is called in Main class");
        } catch (final ExitCodeTestException exitCodeTestException) {
            assertEquals(3, exitCodeTestException.exitCode);

        }
        assertTrue(Main.stateManagementTable.isEmpty());
        assertTrue(Main.resourceRegistrationTable.isEmpty());
        assertFalse(printable.getResult().isEmpty());

    }

    @Test
    public void test_process_MissingAttributesInPropertyFile() throws IOException {

        final File file = temporaryFolder.newFile("New" + PROPERTY_FILE_NAME);
        System.setProperty(Constants.SITE_MANAGER_PROPERTIES_PROPERTY_NAME, file.toString());
        final Properties properties = new Properties();
        properties.put(Constants.JDBC_DRIVER_PROPERTY_NAME, H2_DB_DRIVER);
        creatPropertyFile(file, properties);
        final String[] args = new String[] {"setAdminState", "-s", SITE_NAME, RESOURCE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        try {
            final Main objUnderTest = getMain();
            objUnderTest.process(args, printable);
            fail("ExitCodeTestException must be thrown in MainTestRunner class when System.exit() is called in Main class");
        } catch (final ExitCodeTestException exitCodeTestException) {
            assertEquals(3, exitCodeTestException.exitCode);

        }
        assertTrue(Main.stateManagementTable.isEmpty());
        assertTrue(Main.resourceRegistrationTable.isEmpty());
        assertFalse(printable.getResult().isEmpty());

    }

    @Test
    public void test_process_MissingOneAttributesInPropertyFile() throws IOException {

        final File file = temporaryFolder.newFile("New" + PROPERTY_FILE_NAME);
        System.setProperty(Constants.SITE_MANAGER_PROPERTIES_PROPERTY_NAME, file.toString());
        final Properties properties = new Properties();
        properties.put(Constants.JDBC_DRIVER_PROPERTY_NAME, H2_DB_DRIVER);
        properties.put(Constants.JDBC_URL_PROPERTY_NAME, IN_MEMORY_DB_URL);
        properties.put(Constants.JDBC_USER_PROPERTY_NAME, "test");
        creatPropertyFile(file, properties);
        final String[] args = new String[] {"setAdminState", "-s", SITE_NAME, RESOURCE_NAME};
        final PrintableImpl printable = new PrintableImpl();
        try {
            final Main objUnderTest = getMain();
            objUnderTest.process(args, printable);
            fail("ExitCodeTestException must be thrown in MainTestRunner class when System.exit() is called in Main class");
        } catch (final ExitCodeTestException exitCodeTestException) {
            assertEquals(3, exitCodeTestException.exitCode);

        }
        assertTrue(Main.stateManagementTable.isEmpty());
        assertTrue(Main.resourceRegistrationTable.isEmpty());
        assertFalse(printable.getResult().isEmpty());

    }

    @Test
    public void test_process_emptyArguments_printError() throws IOException {

        final PrintableImpl printable = new PrintableImpl();
        try {
            final String[] args = new String[] {};
            final Main objUnderTest = getMain();
            objUnderTest.process(args, printable);
            fail("ExitCodeTestException must be thrown in MainTestRunner class when System.exit() is called in Main class");
        } catch (final ExitCodeTestException exitCodeTestException) {
            assertEquals(2, exitCodeTestException.exitCode);

        }

        assertTrue(Main.stateManagementTable.isEmpty());
        assertTrue(Main.resourceRegistrationTable.isEmpty());
        final List<String> actualMessages = printable.getResult();
        assertFalse(actualMessages.isEmpty());

        assertEquals(Arrays.asList(ErrorMessages.NO_COMMAND_SPECIFIED, ErrorMessages.HELP_STRING), actualMessages);

    }

    @Test
    public void test_process_nonEmptyWithHelpArguments_printHelp() throws IOException {

        final PrintableImpl printable = new PrintableImpl();
        try {
            final String[] args = new String[] {"---", ""};
            final Main objUnderTest = getMain();
            objUnderTest.process(args, printable);
            fail("ExitCodeTestException must be thrown in MainTestRunner class when System.exit() is called in Main class");
        } catch (final ExitCodeTestException exitCodeTestException) {
            assertEquals(1, exitCodeTestException.exitCode);

        }
        assertTrue(Main.stateManagementTable.isEmpty());
        assertTrue(Main.resourceRegistrationTable.isEmpty());
        final List<String> actualMessages = printable.getResult();
        assertFalse(actualMessages.isEmpty());
        assertEquals(Arrays.asList("Unrecognized option: ---", ErrorMessages.HELP_STRING), actualMessages);

    }

    @Test
    public void test_process_HelpArguments_printHelp() {

        final PrintableImpl printable = new PrintableImpl();
        try {
            final String[] args = new String[] {"-h"};
            final Main objUnderTest = getMain();
            objUnderTest.process(args, printable);
            fail("ExitCodeTestException must be thrown in MainTestRunner class when System.exit() is called in Main class");
        } catch (final ExitCodeTestException exitCodeTestException) {
            assertEquals(0, exitCodeTestException.exitCode);

        }
        assertTrue(Main.stateManagementTable.isEmpty());
        assertTrue(Main.resourceRegistrationTable.isEmpty());
        final List<String> actualMessages = printable.getResult();
        assertFalse(actualMessages.isEmpty());
        assertEquals(Arrays.asList(ErrorMessages.HELP_STRING), actualMessages);

    }

    @Test
    public void test_process_missingArguments_printHelpAndErrorMessage() {

        final PrintableImpl printable = new PrintableImpl();
        try {
            final String[] args = new String[] {"setAdminState", "-s", RESOURCE_NAME};
            final Main objUnderTest = getMain();
            objUnderTest.process(args, printable);
            fail("ExitCodeTestException must be thrown in MainTestRunner class when System.exit() is called in Main class");
        } catch (final ExitCodeTestException exitCodeTestException) {
            assertEquals(2, exitCodeTestException.exitCode);

        }
        assertTrue(Main.stateManagementTable.isEmpty());
        assertTrue(Main.resourceRegistrationTable.isEmpty());
        final List<String> actualMessages = printable.getResult();
        assertFalse(actualMessages.isEmpty());
        assertEquals(Arrays.asList(ErrorMessages.SET_ADMIN_STATE_MISSING_NEW_STATE_VALUE, ErrorMessages.HELP_STRING),
                actualMessages);

    }

    @Test
    public void test_process_extraShowArguments_printHelpAndErrorMessage() {
        final String[] args = new String[] {"show", RESOURCE_NAME};

        runAndAssertErrorMessage(args, ErrorMessages.SHOW_EXTRA_ARGUMENTS, ErrorMessages.HELP_STRING);
    }

    @Test
    public void test_process_lockShowArguments_printHelpAndErrorMessage() {
        final String[] args = new String[] {"lock", RESOURCE_NAME};

        runAndAssertErrorMessage(args, ErrorMessages.LOCK_EXTRA_ARGUMENTS,
                ErrorMessages.LOCK_EITHER_S_OR_R_OPTION_IS_NEEDED, ErrorMessages.HELP_STRING);
    }

    @Test
    public void test_process_unknowArguments_printHelpAndErrorMessage() {
        final String[] args = new String[] {"x", RESOURCE_NAME};

        runAndAssertErrorMessage(args, "x" + ErrorMessages.UNKNOWN_COMMAND, ErrorMessages.HELP_STRING);
    }

    @Test
    public void test_process_SetAdminStateArguments_printHelpAndErrorMessage() {
        final String[] args = new String[] {"setAdminState", RESOURCE_NAME, RESOURCE_NAME};

        runAndAssertErrorMessage(args, ErrorMessages.SET_ADMIN_STATE_EXTRA_ARGUMENTS,
                ErrorMessages.SET_ADMIN_STATE_EITHER_S_OR_R_OPTION_IS_NEEDED, ErrorMessages.HELP_STRING);
    }

    @Test
    public void test_process_SetAdminStateArguments_printHelpAndErrorMessage2() {
        final String[] args = new String[] {"lock", "-s", SITE_NAME, "-r", RESOURCE_NAME};

        runAndAssertErrorMessage(args, "lock" + ErrorMessages.R_AND_S_OPTIONS_ARE_MUTUALLY_EXCLUSIVE,
                ErrorMessages.HELP_STRING);
    }

    @Test
    public void test_process_unlockShowArguments_printHelpAndErrorMessage() {
        final String[] args = new String[] {"unlock", RESOURCE_NAME};

        runAndAssertErrorMessage(args, ErrorMessages.UNLOCK_EXTRA_ARGUMENTS,
                ErrorMessages.UNLOCK_EITHER_S_OR_R_OPTION_IS_NEEDED, ErrorMessages.HELP_STRING);
    }

    @Test
    public void test_process_StateAndResourceArguments_printHelpAndErrorMessage() {
        final String[] args = new String[] {"-s", SITE_NAME, "-r", RESOURCE_NAME};

        runAndAssertErrorMessage(args, ErrorMessages.NO_COMMAND_SPECIFIED, ErrorMessages.HELP_STRING);

    }

    private void runAndAssertErrorMessage(final String[] args, final String... expectedErrorMessages) {
        final PrintableImpl printable = new PrintableImpl();
        try {
            final Main objUnderTest = getMain();
            objUnderTest.process(args, printable);
            fail("ExitCodeTestException must be thrown in MainTestRunner class when System.exit() is called in Main class");
        } catch (final ExitCodeTestException exitCodeTestException) {
            assertEquals(2, exitCodeTestException.exitCode);

        }
        assertTrue(Main.stateManagementTable.isEmpty());
        assertTrue(Main.resourceRegistrationTable.isEmpty());
        final List<String> actualMessages = printable.getResult();
        assertFalse(actualMessages.isEmpty());
        assertEquals(Arrays.asList(expectedErrorMessages), actualMessages);
    }

    private <T> void persist(final EntityManager entityManager, final T entity) {
        final EntityTransaction entityTransaction = entityManager.getTransaction();
        try (final EntityMgrCloser emc = new EntityMgrCloser(entityManager);
                final EntityTransCloser transaction = new EntityTransCloser(entityTransaction)) {
            entityManager.persist(entity);
            transaction.commit();
        }
    }

    public <T> List<T> execute(final EntityManager entityManager, final Class<T> clazz, final String query) {
        try (final EntityMgrCloser entityMgrCloser = new EntityMgrCloser(entityManager);) {
            final TypedQuery<T> typedQuery = entityManager.createQuery(query, clazz);
            return typedQuery.getResultList();
        }
    }

    private Main getMain() {
        final Main objUnderTest = new Main() {
            @Override
            DatabaseAccessService getDatabaseAccessService(final String persistenceUnitName,
                    final Properties properties) {
                return new DatabaseAccessServiceImpl(managerFactory);
            };
        };
        return objUnderTest;
    }

    private StateManagementEntity getStateManagementEntity() {
        final StateManagementEntity entity = new StateManagementEntity();
        entity.setModifiedDate(CURRENT_DATE);
        entity.setAdminState("AdminState");
        entity.setResourceName(RESOURCE_NAME);
        entity.setAvailStatus("AvailStatus");
        entity.setOpState("OpState");
        entity.setStandbyStatus("StandbyStatus");
        return entity;
    }

    private ResourceRegistrationEntity getResourceRegistrationEntity() {
        final ResourceRegistrationEntity entity = new ResourceRegistrationEntity();
        entity.setLastUpdated(CURRENT_DATE);
        entity.setNodeType("NODE_NAME");
        entity.setResourceName(RESOURCE_NAME);
        entity.setResourceUrl("/path/to/something");
        entity.setSite(SITE_NAME);
        return entity;
    }

    private void creatPropertyFile(final File file, final Properties properties) throws IOException {
        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(file.toPath());) {
            properties.store(bufferedWriter, COMMENTS);
        }
    }
}
