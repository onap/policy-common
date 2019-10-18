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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.security.GeneralSecurityException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.junit.Before;
import org.junit.Test;

public class PropertyCoderTest {
    private static final Gson gson = new Gson();
    private PropertyCoder pco = null;

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
    public void testPropertyCoder() throws CoderException, GeneralSecurityException {
        String json = ("{'aes_encryption_key':'abcdefghijklmnopqrstuvwxyzabcdef',"
                + "'xacml.pdp.rest.password':'enc:YZ8EqzsxIOzIuK416SWAdrv+0cKKkqsQt/NYH9+uxwI=',"
                + "'xacml.pdp.rest.user':'testpdp',"
                + "'xacml.pdp.rest.client.user':'policy',"
                + "'xacml.pdp.rest.client.password':'policy',"
                + "'xacml.pdp.rest.environment':'TEST'}").replace('\'', '"');
        MyClass data = pco.decode(json, MyClass.class);
        assertEquals(data.getPdpRestPass(), "alpha");
    }

    @Test
    public void testPropertyCoderReader() throws CoderException, GeneralSecurityException,
        IOException {
        Reader reader = new FileReader("src/test/resources/testdir/testjson.txt");
        char[] buf = new char[10000];
        int size = reader.read(buf);
        String json = new String(buf, 0, size);
        if (size != -1) {
            MyClass data = pco.decode(new String(json), MyClass.class);
            assertEquals(data.getPdpRestPass(), "alpha");
        }
        reader.close();
    }

    public static class MyClass {
        @SerializedName("aes_encryption_key")
        @Getter(AccessLevel.PROTECTED) private String key;
        @SerializedName("xacml.pdp.rest.password")
        @Getter(AccessLevel.PROTECTED) private String pdpRestPass;
    }
}