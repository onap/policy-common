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
import com.google.gson.JsonElement;
import org.junit.Before;
import org.junit.Test;

public class StandardCoderObjectTest {
    private static final Gson gson = new Gson();
    private static final String PROP1 = "abc";
    private static final String PROP2 = "ghi";
    private static final String PROP2b = "jkl";
    private static final String VAL1 = "def";
    private static final String VAL2 = "mno";
    private static final String JSON = "{'abc':'def','ghi':{'jkl':'mno'}}".replace('\'', '"');

    private StandardCoderObject sco;

    /**
     * Creates a standard object, populated with some data.
     *
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        sco = new StandardCoderObject(gson.fromJson(JSON, JsonElement.class));
    }

    @Test
    public void testStandardCoderObject() {
        assertNull(new StandardCoderObject().getData());
    }

    @Test
    public void testStandardCoderObjectJsonElement() {
        assertNotNull(sco.getData());
    }

    @Test
    public void testStandardCoderObjectString() throws Exception {
        assertEquals(VAL1, sco.getString(PROP1));
    }

    @Test
    public void testGetString() throws Exception {
        // one field
        assertEquals(VAL1, sco.getString(PROP1));

        // multiple fields
        assertEquals(VAL2, sco.getString(PROP2, PROP2b));

        // not found
        assertNull(sco.getString("xyz"));

        // read from null object
        assertNull(new StandardCoderObject().getString());
        assertNull(new StandardCoderObject().getString(PROP1));

        // not a primitive
        JsonElement obj = gson.fromJson("{'abc':[]}".replace('\'', '"'), JsonElement.class);
        assertNull(new StandardCoderObject(obj).getString(PROP1));

        // not a JSON object
        assertNull(new StandardCoderObject(obj).getString(PROP1, PROP2));
    }
}
