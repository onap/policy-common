/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import jakarta.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AES Encryption Utilities.
 */
public class CryptoUtils implements CryptoCoder {
    private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);

    /**
     * Definition of encryption algorithm.
     */
    private static final String ALGORITHM = "AES";

    /**
     * Detailed definition of encryption algorithm.
     */
    private static final String ALGORITHM_DETAILS = ALGORITHM + "/GCM/NoPadding";

    private static final int TAG_SIZE_IN_BITS = 128;

    private static final int IV_BLOCK_SIZE_IN_BITS = 128;

    /**
     * An Initial Vector of 16 Bytes, so 32 Hexadecimal Chars.
     */
    private static final int IV_BLOCK_SIZE_IN_BYTES = IV_BLOCK_SIZE_IN_BITS / 8;

    /**
     * Minimum length of an encrypted value.
     */
    private static final int MIN_VALUE_SIZE = (2 * IV_BLOCK_SIZE_IN_BYTES) + 4;

    private SecretKeySpec secretKeySpec;

    /**
     * Used to generate a random "iv". Strong randomness is not needed, as this is only
     * used as a "salt".  (Thus sonar is disabled.)
     */
    private static final Random RANDOM = new Random();  // NOSONAR

    /**
     * CryptoUtils - encryption tool constructor.
     * @param secretKeySpec
     *     AES supports 128, 192 or 256-bit long key size, it can be plain text or generated with key generator
     */
    public CryptoUtils(SecretKeySpec secretKeySpec) {
        this.secretKeySpec = secretKeySpec;
    }

    public CryptoUtils(String secretKey) {
        this.secretKeySpec = readSecretKeySpec(secretKey);
    }

    /**
     * Encrypt a value based on the Policy Encryption Key.
     * Equivalent openssl command: echo -n "123456" | openssl aes-128-cbc -e -K PrivateHexkey
     * -iv 16BytesIV | xxd -u -g100
     *
     * <p>Final result is to put in properties file is: IV + Outcome of openssl command
     *
     * @param value
     *     The plain text string
     * @return The encrypted String
     */
    @Override
    public String encrypt(String value) {
        return encryptValue(value, secretKeySpec);
    }

    /**
     * Encrypt a value based on the Policy Encryption Key.
     * @param value
     *     The plain text string
     * @param secretKey
     *     The secret key
     * @return The encrypted String
     */
    public static String encrypt(String value, String secretKey) {
        var keySpec = readSecretKeySpec(secretKey);
        return encryptValue(value, keySpec);
    }

    private static String encryptValue(String value, SecretKeySpec keySpec) {
        if (value == null || value.isEmpty()) {
            logger.debug("Empty/null value passed in for decryption");
            return value;
        }
        if (isEncrypted(value)) {
            return value;
        }
        try {
            var cipher = Cipher.getInstance(ALGORITHM_DETAILS);
            var iv = new byte[IV_BLOCK_SIZE_IN_BYTES];
            RANDOM.nextBytes(iv);
            var ivspec = new GCMParameterSpec(TAG_SIZE_IN_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivspec);

            return "enc:" + DatatypeConverter.printBase64Binary(
                    ArrayUtils.addAll(iv, cipher.doFinal(value.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            logger.error("Could not encrypt value - exception: ", e);
            return value;
        }
    }

    /**
     * Decrypt a value based on the Policy Encryption Key if string begin with 'enc:'.
     * Equivalent openssl command: echo -n 'Encrypted string' | xxd -r -ps | openssl aes-128-cbc -d
     * -K PrivateHexKey -iv 16BytesIVFromEncryptedString
     *
     * @param value
     *     The encrypted string that must be decrypted using the Policy Encryption Key
     * @return The String decrypted if string begin with 'enc:'
     */
    @Override
    public String decrypt(String value) {
        return decryptValue(value, secretKeySpec);
    }

    /**
     * Decrypt a value based on the Policy Encryption Key if string begin with 'enc:'.
     *
     * @param value
     *     The encrypted string that must be decrypted using the Policy Encryption Key
     * @param secretKey
     *     The secret key
     * @return The String decrypted if string begin with 'enc:'
     */
    public static String decrypt(String value, String secretKey) {
        var keySpec = readSecretKeySpec(secretKey);
        if (keySpec != null) {
            return decryptValue(value, keySpec);
        } else {
            return value;
        }
    }

    private static String decryptValue(String value, SecretKeySpec keySpec) {
        if (value == null || value.isEmpty() || !isEncrypted(value)) {
            return value;
        }
        if (value.length() < MIN_VALUE_SIZE) {
            throw new IllegalArgumentException("Invalid size on input value");
        }
        try {
            var pureValue = value.substring(4);
            byte[] encryptedValue = DatatypeConverter.parseBase64Binary(pureValue);

            var cipher = Cipher.getInstance(ALGORITHM_DETAILS);
            var ivspec = new GCMParameterSpec(TAG_SIZE_IN_BITS,
                    ArrayUtils.subarray(encryptedValue, 0, IV_BLOCK_SIZE_IN_BYTES));
            byte[] realData = ArrayUtils.subarray(encryptedValue, IV_BLOCK_SIZE_IN_BYTES, encryptedValue.length);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivspec);
            byte[] decrypted = cipher.doFinal(realData);
            return new String(decrypted, StandardCharsets.UTF_8);
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
            try {
                return getSecretKeySpec(secretKey);
            } catch (Exception e) {
                logger.error("Invalid key - exception: ", e);
                return null;
            }
        } else {
            logger.error("Secretkey can not be null or empty");
            return null;
        }
    }

    /**
     * Check if string is encrypted by verify if string prefix with 'enc:'.
     *
     * @param value
     *     The encrypted string or plain text value
     * @return boolean value indicate if string prefix with enc: or not
     */
    public static boolean isEncrypted(String value) {
        return (value != null && value.startsWith("enc:"));
    }

    /**
     * This method is used as the main entry point when testing.
     *
     */
    public static void main(String[] args) {
        if (args.length == 3) {
            if ("enc".equals(args[0])) {
                String encryptedValue = encrypt(args[1], args[2]);
                logger.info("original value: {} encrypted value: {}", args[1], encryptedValue);
            } else if ("dec".equals(args[0])) {
                String decryptedValue = decrypt(args[1], args[2]);
                logger.info("original value: {} decrypted value: {}", args[1], decryptedValue);
            } else {
                logger.info("Unknown request: {}", args[0]);
            }
        } else {
            logger.info("Usage  : CryptoUtils enc/dec password   secretKey");
            logger.info("Example: CryptoUtils enc     HelloWorld 1234");
            logger.info("Example: CryptoUtils dec     enc:112233 1234");
        }
    }
}
