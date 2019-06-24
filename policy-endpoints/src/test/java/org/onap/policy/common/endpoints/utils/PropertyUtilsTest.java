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

package org.onap.policy.common.endpoints.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;

public class PropertyUtilsTest {
    private static final String DFLT_STRING = "my-default";
    private static final int DLFT_INT = 1000;

    private PropertyUtils utils;
    private String invalidName;
    private String invalidValue;

    /**
     * Initializes {@link #utils}.
     */
    @Before
    public void setUp() {
        Properties properties = new Properties();
        properties.put("myPrefix.my-string", "some text");
        properties.put("myPrefix.empty-string", "");

        properties.put("myPrefix.my-bool", "true");
        properties.put("myPrefix.my-bool2", "false");
        properties.put("myPrefix.empty-bool", "");
        properties.put("myPrefix.invalid-bool", "not a bool");

        properties.put("myPrefix.my-int", "100");
        properties.put("myPrefix.my-int2", "200");
        properties.put("myPrefix.empty-int", "");
        properties.put("myPrefix.invalid-int", "not an int");

        utils = new PropertyUtils(properties, "myPrefix", (name, value) -> {
            invalidName = name;
            invalidValue = value;
        });
    }

    @Test
    public void testGetString() {
        assertEquals("some text", utils.getString(".my-string", DFLT_STRING));
        assertEquals(DFLT_STRING, utils.getString(".empty-string", DFLT_STRING));
        assertEquals(DFLT_STRING, utils.getString(".missing-string", DFLT_STRING));

        assertNull(invalidName);
        assertNull(invalidValue);
    }

    @Test
    public void testGetBoolean() {
        assertEquals(true, utils.getBoolean(".my-bool", false));
        assertEquals(false, utils.getBoolean(".my-bool2", true));
        assertEquals(true, utils.getBoolean(".empty-bool", true));
        assertEquals(false, utils.getBoolean(".invalid-bool", true));
        assertEquals(true, utils.getBoolean(".missing-bool", true));

        assertNull(invalidName);
        assertNull(invalidValue);
    }

    @Test
    public void testGetInteger() {
        assertEquals(100, utils.getInteger(".my-int", DLFT_INT));
        assertEquals(200, utils.getInteger(".my-int2", DLFT_INT));
        assertEquals(DLFT_INT, utils.getInteger(".empty-int", DLFT_INT));
        assertEquals(DLFT_INT, utils.getInteger(".missing-int", DLFT_INT));

        assertNull(invalidName);
        assertNull(invalidValue);

        assertEquals(DLFT_INT, utils.getInteger(".invalid-int", DLFT_INT));
    }

}
