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

package org.onap.policy.common.utils.services;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class RegistryTest {
    private static final String UNKNOWN = "unknown";
    private static final String NAME_STR = "name-string";
    private static final String NAME_OBJ = "name-object";
    private static final String NAME_INT = "name-integer";

    private static final String DATA_STR = "data-1";
    private static final Object DATA_OBJ = new Object();
    private static final Integer DATA_INT = 5;

    /**
     * Set up.
     */
    @Before
    public void setUp() {
        Registry.newRegistry();

        Registry.register(NAME_STR, DATA_STR);
        Registry.register(NAME_OBJ, DATA_OBJ);
        Registry.register(NAME_INT, DATA_INT);
    }

    /**
     * Sunny day scenario is tested by other tests, so we focus on exceptions here.
     */
    @Test
    public void testRegister_Ex() {
        assertThatIllegalStateException().isThrownBy(() -> Registry.register(NAME_STR, DATA_STR));

        assertThatIllegalArgumentException().isThrownBy(() -> Registry.register(null, DATA_STR));
        assertThatIllegalArgumentException().isThrownBy(() -> Registry.register(UNKNOWN, null));
    }

    @Test
    public void testUnregister() {
        assertTrue(Registry.unregister(NAME_STR));

        assertEquals(null, Registry.getOrDefault(NAME_STR, String.class, null));

        assertFalse(Registry.unregister(NAME_STR));
    }

    @Test
    public void testGet() {
        assertSame(DATA_STR, Registry.get(NAME_STR, String.class));
        assertSame(DATA_OBJ, Registry.get(NAME_OBJ, Object.class));
        assertSame(DATA_INT, Registry.get(NAME_INT, Integer.class));

        // does not exist
        assertThatIllegalArgumentException().isThrownBy(() -> Registry.get(UNKNOWN, Object.class));

        // wrong type
        assertThatThrownBy(() -> Registry.get(NAME_INT, String.class)).isInstanceOf(ClassCastException.class);
    }

    @Test
    public void testGetOrDefault() {
        assertSame(DATA_STR, Registry.getOrDefault(NAME_STR, String.class, null));
        assertSame(DATA_OBJ, Registry.getOrDefault(NAME_OBJ, Object.class, "xyz"));
        assertSame(DATA_INT, Registry.getOrDefault(NAME_INT, Integer.class, 10));

        assertEquals(null, Registry.getOrDefault(UNKNOWN, String.class, null));
        assertEquals("abc", Registry.getOrDefault(UNKNOWN, String.class, "abc"));
        assertEquals(Integer.valueOf(11), Registry.getOrDefault(UNKNOWN, Integer.class, 11));
    }

    @Test
    public void testNewRegistry() {
        assertSame(DATA_STR, Registry.get(NAME_STR, String.class));

        Registry.newRegistry();

        // should not exist
        assertEquals(null, Registry.getOrDefault(NAME_STR, String.class, null));

        // should be able to register it again now
        Registry.register(NAME_STR, DATA_STR);
        assertSame(DATA_STR, Registry.get(NAME_STR, String.class));
    }

}
