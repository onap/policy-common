/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024-2025 Nordix Foundation
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

package org.onap.policy.common.utils.coder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.annotations.SerializedName;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PropertyCoderTest {
    private PropertyCoder propertyCoder = null;
    private static final String AES_ENCRYPTION_KEY = "aes_encryption_key";

    /*
     * Note: to generate the encrypted values, invoke CryptoUtils passing both the value
     * to be encrypted and the secret key.
     *
     * The secret key should typically be 32 characters long, resulting in a 256-bit
     * key, and is placed in "aes_encryption_key".
     *
     * For "xacml.pdp.rest.password", the encrypted value was generated via:
     *  java org.onap.policy.common.utils.security.CryptoUtils enc alpha abcdefghijklmnopqrstuvwxyzabcdef
     *
     * For "pass", the encrypted value was generated via:
     *  java org.onap.policy.common.utils.security.CryptoUtils enc hello abcdefghijklmnopqrstuvwxyzabcdef
     */
    private static final String JSON =
            ("{'aes_encryption_key':'abcdefghijklmnopqrstuvwxyzabcdef'"
            + ",'xacml.pdp.rest.password':'enc:FSfOhDygtmnX3gkMSfTFMoBFW+AG5k6goNj2KZgQmeF0DqgcMg=='"
            + ",'xacml.pdp.rest.user':'testpdp'"
            + ",'xacml.pdp.rest.client.user':'policy'"
            + ",'xacml.pdp.rest.client.password':'policy'"
            + ",'xacml.pdp.rest.environment':'TEST'"
            + ",'servers':[{'name':'server1','port':'10',"
            + "'pass':'enc:08Fj6tLhmWjkZkf52O2A2ZNT8PpL80yEOEKXlbV/gnm0lkR9OA=='}"
            + ",{'name':'server2','port':'20','pass':'plaintext'}]"
            + "}").replace('\'', '"');

    /**
     * Creates a standard object, populated with some data.
     *
     * @throws Exception if an error occurs
     */
    @BeforeEach
    public void setUp() throws Exception {
        propertyCoder = new PropertyCoder();
    }

    @Test
    void testPropertyCoder() {
        MyClass data = propertyCoder.decode(JSON, AES_ENCRYPTION_KEY, MyClass.class);
        assertEquals("alpha", data.getPdpRestPass());
        assertEquals("hello", data.servers.get(0).pass);
        assertEquals("server1", data.servers.get(0).name);
        assertEquals("10", data.servers.get(0).port);
        assertEquals("plaintext", data.servers.get(1).pass);
    }

    @Test
    void testPropertyCoderReader() {
        Reader reader = new StringReader(JSON);
        MyClass data = propertyCoder.decode(reader, AES_ENCRYPTION_KEY, MyClass.class);
        assertEquals("alpha", data.getPdpRestPass());
        assertEquals("hello", data.servers.get(0).pass);
        assertEquals("server1", data.servers.get(0).name);
        assertEquals("10", data.servers.get(0).port);
        assertEquals("plaintext", data.servers.get(1).pass);
    }

    @Getter
    public static class MyClass {
        @SerializedName("aes_encryption_key")
        private String key;
        @SerializedName("xacml.pdp.rest.password")
        private String pdpRestPass;
        @SerializedName("xacml.pdp.rest.user")
        private String pdpRestUser;
        @SerializedName("xacml.pdp.rest.client.user")
        private String pdpClientUser;
        @SerializedName("xacml.pdp.rest.client.password")
        private String pdpClientPass;
        @SerializedName("xacml.pdp.rest.environment")
        private String pdpRestEnv;
        @SerializedName("servers")
        private List<ServerClass> servers;
    }

    public static class ServerClass {
        private String name;
        private String port;
        private String pass;
    }
}
