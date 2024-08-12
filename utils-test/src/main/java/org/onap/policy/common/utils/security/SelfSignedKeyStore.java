/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.onap.policy.common.utils.resources.ResourceUtils;

/**
 * Keystore, containing a self-signed certificate, valid for one day (see the argument to
 * the "-valid" flag below). For use in junit tests.
 */
@Getter
public class SelfSignedKeyStore {
    public static final String KEYSTORE_PASSWORD = "Pol1cy_0nap";
    public static final String PRIVATE_KEY_PASSWORD = KEYSTORE_PASSWORD;
    public static final String RELATIVE_PATH = "target/test-classes/policy-keystore";

    /**
     * File containing subject-alternative names (i.e., list of servers that may use this
     * keystore).
     */
    private static final String KEYSTORE_SAN = "keystore_san.txt";

    private final String keystoreName;


    /**
     * Generates the keystore, if it does not exist or if it's more than a few hours old.
     *
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if an interrupt occurs
     */
    public SelfSignedKeyStore() throws IOException {
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
    public SelfSignedKeyStore(String relativePath) throws IOException {
        keystoreName = System.getProperty("user.dir") + "/" + relativePath;

        // use existing file if it isn't too old
        var keystoreFile = new File(keystoreName);
        if (keystoreFile.exists()) {
            if (System.currentTimeMillis() < keystoreFile.lastModified()
                            + TimeUnit.MILLISECONDS.convert(5, TimeUnit.HOURS)) {
                return;
            }

            Files.delete(keystoreFile.toPath());
        }

        /*
         * Read the list of subject-alternative names, joining the lines with commas, and
         * dropping the trailing comma.
         */
        String sanFileName = getKeystoreSanName();
        var sanString = ResourceUtils.getResourceAsString(sanFileName);
        if (sanString == null) {
            throw new FileNotFoundException(sanFileName);
        }

        var sanArray = sanString.replace("DNS:", "").replace("\r", "").split("\n");
        GeneralName[] nameArray = Arrays.stream(sanArray).map(name -> new GeneralName(GeneralName.dNSName, name))
                        .toList().toArray(new GeneralName[0]);
        final var names = new GeneralNames(nameArray);

        try (var ostr = new FileOutputStream(keystoreFile)) {
            var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            final var keyPair = keyPairGenerator.generateKeyPair();

            final long tcur = System.currentTimeMillis();

            final var dn = new X500Name("C=US, O=ONAP, OU=OSAAF, OU=policy@policy.onap.org:DEV, CN=policy");
            final var serial = BigInteger.valueOf(new SecureRandom().nextInt());
            final var notBefore = new Date(tcur);
            final var notAfter = new Date(tcur + TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS));
            final var pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

            ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                            .setProvider(new BouncyCastleFipsProvider()).build(keyPair.getPrivate());

            X509CertificateHolder holder = new X509v3CertificateBuilder(dn, serial, notBefore, notAfter, dn, pubKeyInfo)
                            .addExtension(Extension.subjectAlternativeName, false, names).build(signer);

            var cert = new JcaX509CertificateConverter().setProvider(new BouncyCastleFipsProvider())
                            .getCertificate(holder);
            final Certificate[] chain = {cert};

            var keystore = KeyStore.getInstance("PKCS12");
            keystore.load(null, null);
            keystore.setKeyEntry("policy@policy.onap.org", keyPair.getPrivate(), PRIVATE_KEY_PASSWORD.toCharArray(),
                            chain);

            keystore.store(ostr, KEYSTORE_PASSWORD.toCharArray());

        } catch (NoSuchAlgorithmException | OperatorCreationException | CertificateException | KeyStoreException e) {
            throw new IOException("cannot create certificate", e);
        }
    }

    // may be overridden by junit tests

    protected String getKeystoreSanName() {
        return KEYSTORE_SAN;
    }
}
