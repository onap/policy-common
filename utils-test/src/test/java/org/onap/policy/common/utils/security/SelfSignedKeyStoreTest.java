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

package org.onap.policy.common.utils.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SelfSignedKeyStoreTest {
    private static final String USER_DIR_PROP = "user.dir";

    private static String saveUserDir;
    private static String defaultName;
    private static File defaultKeystore;


    /**
     * Saves the user.dir property and initializes static fields.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        saveUserDir = System.getProperty(USER_DIR_PROP);
        defaultName = saveUserDir + "/" + SelfSignedKeyStore.RELATIVE_PATH;
        defaultKeystore = new File(defaultName);
    }

    @Before
    public void setUp() {
        System.setProperty(USER_DIR_PROP, saveUserDir);
        delete(defaultKeystore);
    }

    @Test
    public void testSelfSignedKeyStore() throws Exception {
        SelfSignedKeyStore ks = new SelfSignedKeyStore();

        assertThat(ks.getKeystoreName()).isEqualTo(defaultName);
        assertThat(defaultKeystore).exists();
    }

    @Test
    public void testSelfSignedKeyStoreString() throws IOException, InterruptedException {
        String relName = "target/my-keystore";
        String altName = saveUserDir + "/" + relName;
        File altFile = new File(altName);

        delete(altFile);

        SelfSignedKeyStore ks = new SelfSignedKeyStore(relName);

        assertThat(ks.getKeystoreName()).isEqualTo(altName);
        assertThat(altFile).exists();
    }

    /**
     * Tests the constructor, when the keystore already exists.
     */
    @Test
    public void testSelfSignedKeyStoreStringExists() throws Exception {
        new SelfSignedKeyStore();
        assertThat(defaultKeystore).exists();

        // change the timestamp on the keystore so it's a little old, but not too old
        defaultKeystore.setLastModified(
                        System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES));
        long tcurrent = defaultKeystore.lastModified();

        // this should not recreate the keystore, thus the timestamp should be unchanged
        new SelfSignedKeyStore();
        assertThat(defaultKeystore.lastModified()).isEqualTo(tcurrent);

        // change the timestamp on the keystore so it's too old
        defaultKeystore.setLastModified(
                        System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1000, TimeUnit.HOURS));
        tcurrent = defaultKeystore.lastModified();

        // this should recreate the keystore
        new SelfSignedKeyStore();
        assertThat(defaultKeystore.lastModified()).isGreaterThan(tcurrent);
    }

    /**
     * Tests the constructor, when the SAN file is not found.
     */
    @Test
    public void testSelfSignedKeyStoreStringNoSanFile() throws Exception {
        assertThatThrownBy(() -> new SelfSignedKeyStore() {
            @Override
            protected String getKeystoreSanName() {
                return "unknown/san/file.txt";
            }
        }).isInstanceOf(FileNotFoundException.class).hasMessageContaining("file.txt");
    }

    /**
     * Tests the constructor, when write fails.
     */
    @Test
    public void testSelfSignedKeyStoreStringWriteFailure() throws Exception {
        assertThatThrownBy(() -> new SelfSignedKeyStore("target/unknown/path/to/keystore"))
                        .isInstanceOf(IOException.class);
    }

    @Test
    public void testGetKeystoreName() throws Exception {
        String relpath = SelfSignedKeyStore.RELATIVE_PATH;

        // append the first part of the relative path to user.dir
        System.setProperty(USER_DIR_PROP, saveUserDir + "/" + relpath.substring(0, relpath.indexOf('/')));

        // create a keystore using the remaining components of the relative path
        SelfSignedKeyStore ks = new SelfSignedKeyStore(relpath.substring(relpath.indexOf('/') + 1));

        assertThat(ks.getKeystoreName()).isEqualTo(defaultName);
        assertThat(defaultKeystore).exists();

        // try again using the original relative path - should fail, as it's now deeper
        assertThatThrownBy(() -> new SelfSignedKeyStore(relpath)).isInstanceOf(IOException.class);
    }

    private static void delete(File file) {
        if (file.exists()) {
            assertThat(file.delete()).isTrue();
        }
    }
}
