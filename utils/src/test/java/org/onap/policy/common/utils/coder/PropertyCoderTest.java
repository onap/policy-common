/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;

import com.google.gson.annotations.SerializedName;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import lombok.Getter;

import org.junit.Before;
import org.junit.Test;

public class PropertyCoderTest {
    private PropertyCoder pco = null;
    private static final String json =
            ("{'aes_encryption_key':'abcdefghijklmnopqrstuvwxyzabcdef'"
            + ",'xacml.pdp.rest.password':'enc:YZ8EqzsxIOzIuK416SWAdrv+0cKKkqsQt/NYH9+uxwI='"
            + ",'xacml.pdp.rest.user':'testpdp'"
            + ",'xacml.pdp.rest.client.user':'policy'"
            + ",'xacml.pdp.rest.client.password':'policy'"
            + ",'xacml.pdp.rest.environment':'TEST'" 
            + ",'servers':[{'name':'server1','port':'10',"
            + "'pass':'enc:KXIY94KcAapOAAeFbtjQL4kBPB4k+NJfwdP+GpG3LWQ='}"
            + ",{'name':'server2','port':'20','pass':'plaintext'}]"
            + "}").replace('\'', '"');

    /**
     * Creates a standard object, populated with some data.
     *
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        pco = new PropertyCoder();
    }

    @Test
    public void testPropertyCoder() throws CoderException {
        MyClass data = pco.decode(json, MyClass.class);
        assertEquals(data.getPdpRestPass(), "alpha");
        assertEquals(data.servers.get(0).pass, "hello");
        assertEquals(data.servers.get(0).name, "server1");
        assertEquals(data.servers.get(0).port, "10");
        assertEquals(data.servers.get(1).pass, "plaintext");
    }

    @Test
    public void testPropertyCoderReader() throws CoderException {
        Reader reader = new StringReader(json);
        MyClass data = pco.decode(reader, MyClass.class);
        assertEquals(data.getPdpRestPass(), "alpha");
        assertEquals(data.servers.get(0).pass, "hello");
        assertEquals(data.servers.get(0).name, "server1");
        assertEquals(data.servers.get(0).port, "10");
        assertEquals(data.servers.get(1).pass, "plaintext");
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