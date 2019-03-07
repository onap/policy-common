/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import com.google.common.base.Charsets;

import java.security.GeneralSecurityException;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AES Encryption Utilities.
 */
public class CryptoUtils {
    private static Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    /**
     * Definition of encryption algorithm.
     */
    private static final String ALGORITHM = "AES";

    /**
     * Detailed definition of encryption algorithm.
     */
    private static final String ALGORITHM_DETAILS = ALGORITHM + "/CBC/PKCS5PADDING";

    private static final int IV_BLOCK_SIZE_IN_BITS = 128;

    /**
     * An Initial Vector of 16 Bytes, so 32 Hexadecimal Chars.
     */
    private static final int IV_BLOCK_SIZE_IN_BYTES = IV_BLOCK_SIZE_IN_BITS / 8;

    private static int validSize = (2 * IV_BLOCK_SIZE_IN_BYTES) + 4;

    private SecretKeySpec secretKeySpec;

    /**
     * This method is used as the main entry point when testing.
     *
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            if (args[0].equals("enc")) {
                String encryptedValue = encrypt(args[1], args[2]);
                System.out.println("original value: " + args[1] + " encrypted value: " + encryptedValue);
            } else if (args[0].equals("dec")) {
                String decryptedValue = decrypt(args[1], args[2]);
                System.out.println("original value: " + args[1] + " decrypted value: " + decryptedValue);
            } else {
                System.out.println("Unknown request: " + args[0]);
            }
        } else {
            System.out.println("Usage  : CryptoUtils enc/dec password   secretKey");
            System.out.println("Example: CryptoUtils enc     HelloWorld 1234");
            System.out.println("Example: CryptoUtils dec     enc:112233 1234");
        }
    }

    public CryptoUtils(SecretKeySpec secretKeySpec) {
        this.secretKeySpec = secretKeySpec;
    }

    public CryptoUtils(String secretKey) {
        this.secretKeySpec = readSecretKeySpec(secretKey);
    }

    /**
     * Encrypt a value based on the Policy Encryption Key.
     *
     * @param value
     *  The plain text string
     * @return The encrypted String
     * @throws GeneralSecurityException
     *  In case of issue with the encryption
     */
    public String encrypt(String value) throws GeneralSecurityException {
        return encryptValue(value, secretKeySpec);
    }

    /**
     * Encrypt a value based on the Policy Encryption Key.
     *
     * @param value
     *  The plain text string
     * @param secretKey
     *  The secret key
     * @return The encrypted String
     */
    public static String encrypt(String value, String secretKey) {
        SecretKeySpec keySpec = readSecretKeySpec(secretKey);
        return encryptValue(value, keySpec);
    }

    private static String encryptValue(String value, SecretKeySpec keySpec) {
        if (value == null || value.isEmpty()) {
            logger.debug("Empty/null value passed in for decryption");
            return value;
        }
        if (value.startsWith("enc:")) {
            return value;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_DETAILS);
            byte[] iv = new byte[IV_BLOCK_SIZE_IN_BYTES];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivspec);

            return "enc:" + DatatypeConverter.printBase64Binary(
                    ArrayUtils.addAll(iv, cipher.doFinal(value.getBytes(Charsets.UTF_8))));
        } catch (Exception e) {
            logger.error("Could not encrypt value - exception: ", e);
            return value;
        }
    }

    /**
     * Decrypt a value based on the Policy Encryption Key if string begin with 'enc:'.
     *
     * @param value
     *  The encrypted string that must be decrypted using the Policy Encryption Key
     * @return The String decrypted if string begin with 'enc:'
     * @throws GeneralSecurityException
     *   In case of issue with the encryption
     */
    public String decrypt(String value) throws GeneralSecurityException {
        return decryptValue(value, secretKeySpec);
    }

    /**
     * Decrypt a value based on the Policy Encryption Key if string begin with 'enc:'.
     *
     * @param value
     *  The encrypted string that must be decrypted using the Policy Encryption Key
     * @param secretKey
     *  The secret key
     * @return The String decrypted if string begin with 'enc:'
     */
    public static String decrypt(String value, String secretKey) {
        SecretKeySpec keySpec = readSecretKeySpec(secretKey);
        if (keySpec != null) {
            return decryptValue(value, keySpec);
        } else {
            return value;
        }
    }

    private static String decryptValue(String value, SecretKeySpec keySpec) {
        if (value == null || value.isEmpty()) {
            logger.debug("Empty/null value passed in for encryption");
            return value;
        }
        if (value.length() < validSize || !value.startsWith("enc:")) {
            logger.debug("Invalid value requirements for decryption");
            return value;
        }
        try {
            String pureValue = value.substring(4);
            byte[] encryptedValue = DatatypeConverter.parseBase64Binary(pureValue);

            Cipher cipher = Cipher.getInstance(ALGORITHM_DETAILS);
            IvParameterSpec ivspec = new IvParameterSpec(
                    ArrayUtils.subarray(encryptedValue, 0, IV_BLOCK_SIZE_IN_BYTES));
            byte[] realData = ArrayUtils.subarray(encryptedValue, IV_BLOCK_SIZE_IN_BYTES, encryptedValue.length);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivspec);
            byte[] decrypted = cipher.doFinal(realData);
            return new String(decrypted, Charsets.UTF_8);
        } catch (Exception e) {
            logger.error("Could not decrypt value - exception: ", e);
        }
        return value;
    }

    /**
     * Method used to generate the SecretKeySpec from a Hex String.
     *
     * @param keyString
     *            The key as a string in base64 String
     * @return The SecretKeySpec created
     * @throws DecoderException
     *             In case of issues with the decoding of base64 String
     */
    private static SecretKeySpec getSecretKeySpec(String keyString) {
        byte[] key = DatatypeConverter.parseBase64Binary(keyString);
        return new SecretKeySpec(key, ALGORITHM);
    }

    /**
     * Get Secret Key Spec from user provided secret key string.
     *
     * @param secretKey
     *            user provided secretKey String
     * @return SecretKeySpec secret key spec read from getSecretKeySpec
     */
    private static SecretKeySpec readSecretKeySpec(String secretKey) {
        if (secretKey != null && !secretKey.isEmpty()) {
            SecretKeySpec keySpec = null;
            try {
                keySpec = getSecretKeySpec(secretKey);
                return keySpec;
            } catch (Exception e) {
                logger.error("Invalid key - exception: ", e);
                return null;
            }
        } else {
            logger.error("Secretkey can not be null or empty");
            return null;
        }
    }
}