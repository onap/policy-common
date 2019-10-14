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

import java.security.GeneralSecurityException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

import org.onap.policy.common.utils.security.CryptoUtils;

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
        String json = "{'aes_encryption_key':'abcdefghijklmnopqrstuvwxyzabcdef',"
                + "'aai.rest.password':'enc:YZ8EqzsxIOzIuK416SWAdrv+0cKKkqsQt/NYH9+uxwI='}".replace('\'', '"');
        //String decrypted = pco.decode(json, String.class);
        //System.out.println("decrypted: " + decrypted);
    }
}