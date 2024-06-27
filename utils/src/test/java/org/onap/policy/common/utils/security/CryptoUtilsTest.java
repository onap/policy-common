/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.GeneralSecurityException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */

class CryptoUtilsTest {
    private static Logger logger = LoggerFactory.getLogger(CryptoUtilsTest.class);
    private static final String PASS = "HelloWorld";
    private static final String SECRET_KEY = "MTIzNDU2Nzg5MDEyMzQ1Ng==";
    private static final String ENCRYPTED_PASS = "enc:Z6QzirpPyDpwmIcNbE3U2iq6g/ubJBEdzssoigxGGChlQtdWOLD8y00O";
    private static final String DECRYPTED_MSG = "encrypted value: {}  decrypted value : {}";
    private static final String ENCRYPTED_MSG = "original value : {}  encrypted value: {}";

    @Test
    void testEncrypt() throws GeneralSecurityException {
        logger.info("testEncrypt:");
        CryptoCoder cryptoUtils = new CryptoUtils(SECRET_KEY);
        String encryptedValue = cryptoUtils.encrypt(PASS);
        logger.info(ENCRYPTED_MSG, PASS, encryptedValue);
        assertTrue(encryptedValue.startsWith("enc:"));

        String decryptedValue = cryptoUtils.decrypt(encryptedValue);
        logger.info(DECRYPTED_MSG, encryptedValue, decryptedValue);
        assertEquals(PASS, decryptedValue);
    }

    @Test
    void testDecrypt() throws GeneralSecurityException {
        logger.info("testDecrypt:");
        CryptoCoder cryptoUtils = new CryptoUtils(SECRET_KEY);
        String decryptedValue = cryptoUtils.decrypt(ENCRYPTED_PASS);
        logger.info(DECRYPTED_MSG, ENCRYPTED_PASS, decryptedValue);
        assertEquals(PASS, decryptedValue);
    }

    @Test
    void testStaticEncrypt() {
        logger.info("testStaticEncrypt:");
        String encryptedValue = CryptoUtils.encrypt(PASS, SECRET_KEY);
        logger.info(ENCRYPTED_MSG, PASS, encryptedValue);

        String decryptedValue = CryptoUtils.decrypt(encryptedValue, SECRET_KEY);
        logger.info(DECRYPTED_MSG, encryptedValue, decryptedValue);
        assertEquals(PASS, decryptedValue);
    }

    @Test
    void testStaticDecrypt() {
        logger.info("testStaticDecrypt:");
        String decryptedValue = CryptoUtils.decrypt(ENCRYPTED_PASS, SECRET_KEY);
        logger.info(DECRYPTED_MSG, ENCRYPTED_PASS, decryptedValue);
        assertEquals(PASS, decryptedValue);
    }

    @Test
    void testBadInputs() {
        String badKey = CryptoUtils.encrypt(PASS, "test");
        assertEquals(PASS, badKey);

        String badDecrypt = CryptoUtils.decrypt(ENCRYPTED_PASS, "");
        assertEquals(ENCRYPTED_PASS, badDecrypt);

        String emptyValue = CryptoUtils.encrypt("", SECRET_KEY);
        assertEquals("", emptyValue);

        String emptyDecrypt = CryptoUtils.decrypt("", SECRET_KEY);
        assertEquals("", emptyDecrypt);

        String nullValue = CryptoUtils.encrypt(null, SECRET_KEY);
        assertNull(nullValue);

        String nullDecrypt = CryptoUtils.decrypt(null, SECRET_KEY);
        assertNull(nullDecrypt);
    }

    @Test
    void testAll() {
        logger.info("testAll:");
        String encryptedValue = CryptoUtils.encrypt(PASS, SECRET_KEY);
        logger.info(ENCRYPTED_MSG, PASS, encryptedValue);

        String encryptedAgain = CryptoUtils.encrypt(encryptedValue, SECRET_KEY);

        assertEquals(encryptedValue, encryptedAgain);

        String decryptedValue = CryptoUtils.decrypt(encryptedAgain, SECRET_KEY);
        logger.info(DECRYPTED_MSG, encryptedAgain, decryptedValue);
        assertEquals(PASS, decryptedValue);

        String decryptedAgain = CryptoUtils.decrypt(decryptedValue, SECRET_KEY);
        assertEquals(decryptedValue, decryptedAgain);
    }
}
