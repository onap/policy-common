/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.security.GeneralSecurityException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */

public class CryptoUtilsTest {
    private static Logger logger = LoggerFactory.getLogger(CryptoUtilsTest.class);
    private final String pass = "HelloWorld";
    private final String secretKey = "12345678901234567890123456789012";
    private final String encryptedPass = "enc:8XxseP5W5ODxzPrReNKd9JBYLv0iiAzy9BHnMKau5yg=";

    @Test
    public void testEncrypt() throws GeneralSecurityException {
        logger.info("\ntestEncrypt:");
        // secretKey should come from property file or env variable on client side

        CryptoUtils cryptoUtils = new CryptoUtils(secretKey);
        String encryptedValue = cryptoUtils.encrypt(pass);
        logger.info("original value : " + pass + "  encrypted value: " + encryptedValue);

        String decryptedValue = cryptoUtils.decrypt(encryptedValue);
        logger.info("encrypted value: " + encryptedValue + "  decrypted value : " + decryptedValue);
        assertEquals(pass, decryptedValue);
    }

    @Test
    public void testDecrypt() throws GeneralSecurityException {
        logger.info("\ntestDecrypt:");
        // secretKey should come from property file or env variable on client side

        CryptoUtils cryptoUtils = new CryptoUtils(secretKey);
        String decryptedValue = cryptoUtils.decrypt(encryptedPass);
        logger.info("encrypted value: " + encryptedPass + "  decrypted value : " + decryptedValue);
        assertEquals(pass, decryptedValue);
    }

    @Test
    public void testStaticEncrypt() {
        logger.info("\ntestStaticEncrypt:");
        // secretKey should come from property file or env variable on client side

        String encryptedValue = CryptoUtils.encrypt(pass, secretKey);
        logger.info("original value : " + pass + "  encrypted value: " + encryptedValue);

        String decryptedValue = CryptoUtils.decrypt(encryptedValue, secretKey);
        logger.info("encrypted value: " + encryptedValue + "  decrypted value : " + decryptedValue);
        assertEquals(pass, decryptedValue);
    }

    @Test
    public void testStaticDecrypt() {
        logger.info("\ntestStaticDecrypt:");
        // secretKey should come from property file or env variable on client side

        String decryptedValue = CryptoUtils.decrypt(encryptedPass, secretKey);
        logger.info("encrypted value: " + encryptedPass + "  decrypted value : " + decryptedValue);
        assertEquals(pass, decryptedValue);
    }

    @Test
    public void testBadInputs() {
        String badKey = CryptoUtils.encrypt(pass, "test");
        assertEquals(pass, badKey);

        String badDecrypt = CryptoUtils.decrypt(encryptedPass, "");
        assertEquals(encryptedPass, badDecrypt);

        String emptyValue = CryptoUtils.encrypt(new String(), secretKey);
        assertEquals("", emptyValue);

        String emptyDecrypt = CryptoUtils.decrypt(new String(), secretKey);
        assertEquals("", emptyDecrypt);

        String nullValue = CryptoUtils.encrypt(null, secretKey);
        assertNull(nullValue);

        String nullDecrypt = CryptoUtils.decrypt(null, secretKey);
        assertNull(nullDecrypt);
    }

    @Test
    public void testAll() {
        logger.info("\ntestAll:");
        // secretKey should come from property file or env variable on client side

        String encryptedValue = CryptoUtils.encrypt(pass, secretKey);
        logger.info("original value : " + pass + "  encrypted value: " + encryptedValue);

        String encryptedAgain = CryptoUtils.encrypt(encryptedValue, secretKey);

        assertEquals(encryptedValue, encryptedAgain);

        String decryptedValue = CryptoUtils.decrypt(encryptedAgain, secretKey);
        logger.info("encrypted value: " + encryptedAgain + "  decrypted value : " + decryptedValue);
        assertEquals(pass, decryptedValue);

        String decryptedAgain = CryptoUtils.decrypt(decryptedValue, secretKey);
        assertEquals(decryptedValue, decryptedAgain);
    }
}