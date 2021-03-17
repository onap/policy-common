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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.onap.policy.common.utils.resources.ResourceUtils;

/**
 * Keystore, containing a self-signed certificate, valid for one day (see the argument to
 * the "-valid" flag below). For use in junit tests.
 */
public class SelfSignedKeyStore {
    public static final String KEYSTORE_PASSWORD = "Pol1cy_0nap";
    public static final String PRIVATE_KEY_PASSWORD = KEYSTORE_PASSWORD;
    public static final String RELATIVE_PATH = "target/test-classes/policy-keystore";

    /**
     * File containing subject-alternative names (i.e., list of servers that may use this
     * keystore).
     */
    private static final String KEYSTORE_SAN = "keystore_san.txt";

    @Getter
    private final String keystoreName;


    /**
     * Generates the keystore, if it does not exist or if it's more than a few hours old.
     *
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if an interrupt occurs
     */
    public SelfSignedKeyStore() throws IOException, InterruptedException {
        this(RELATIVE_PATH);
    }

    /**
     * Generates the keystore, if it does not exist or if it's more than a few hours old.
     *
     * @param relativePath path to the keystore, relative to the "user.dir" system
     *        property
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if an interrupt occurs
     */
    public SelfSignedKeyStore(String relativePath) throws IOException, InterruptedException {
        keystoreName = System.getProperty("user.dir") + "/" + relativePath;

        // use existing file if it isn't too old
        File keystore = new File(keystoreName);
        if (keystore.exists()) {
            if (System.currentTimeMillis() < keystore.lastModified()
                            + TimeUnit.MILLISECONDS.convert(5, TimeUnit.HOURS)) {
                return;
            }

            Files.delete(keystore.toPath());
        }

        /*
         * Read the list of subject-alternative names, joining the lines with commas, and
         * dropping the trailing comma.
         */
        String sanName = getKeystoreSanName();
        String subAltNames = ResourceUtils.getResourceAsString(sanName);
        if (subAltNames == null) {
            throw new FileNotFoundException(sanName);
        }

        subAltNames = subAltNames.replace("\r", "").replace("\n", ",");
        subAltNames = "SAN=" + subAltNames.substring(0, subAltNames.length() - 1);

        // build up the "keytool" command

        // @formatter:off
        ProcessBuilder builder = new ProcessBuilder("keytool", "-genkeypair",
                        "-alias", "policy@policy.onap.org",
                        "-validity", "1",
                        "-keyalg", "RSA",
                        "-dname", "C=US, O=ONAP, OU=OSAAF, OU=policy@policy.onap.org:DEV, CN=policy",
                        "-keystore", keystoreName,
                        "-keypass", PRIVATE_KEY_PASSWORD,
                        "-storepass", KEYSTORE_PASSWORD,
                        "-ext", subAltNames);
        // @formatter:on

        Process proc = builder.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT).start();
        proc.waitFor();

        int exitCode = proc.exitValue();
        if (exitCode != 0) {
            throw new IOException("keytool exited with " + exitCode);
        }
    }

    // may be overridden by junit tests

    protected String getKeystoreSanName() {
        return KEYSTORE_SAN;
    }
}
