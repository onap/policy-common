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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.Getter;
import org.eclipse.persistence.tools.file.FileUtil;
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
     * File resource, containing keystore, to use.
     */
    private static final String KEYSTORE_RESOURCE = "selfSignedKey.jks";

    @Getter
    private final String keystoreName;


    /**
     * Generates the keystore.
     *
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if an interrupt occurs
     */
    public SelfSignedKeyStore() throws IOException, InterruptedException {
        this(RELATIVE_PATH);
    }

    /**
     * Generates the keystore.
     *
     * @param relativePath path to the keystore, relative to the "user.dir" system
     *        property
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if an interrupt occurs
     */
    public SelfSignedKeyStore(String relativePath) throws IOException, InterruptedException {
        keystoreName = System.getProperty("user.dir") + "/" + relativePath;

        String resourceName = getKeystoreResourceName();
        InputStream resource = ResourceUtils.getResourceAsStream(resourceName);
        if (resource == null) {
            throw new FileNotFoundException(resourceName);
        }

        try (var input = resource;
                        var output = new FileOutputStream(keystoreName)) {
            FileUtil.copy(input, output);
        }
    }

    // may be overridden by junit tests

    protected String getKeystoreResourceName() {
        return KEYSTORE_RESOURCE;
    }
}
