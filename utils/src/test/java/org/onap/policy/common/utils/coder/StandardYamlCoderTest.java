/*-
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.junit.Before;
import org.junit.Test;

public class StandardYamlCoderTest {
    private static final File YAML_FILE =
                    new File("src/test/resources/org/onap/policy/common/utils/coder/StandardYamlCoder.yaml");

    private StandardYamlCoder coder;
    private Container cont;

    @Before
    public void setUp() throws CoderException {
        coder = new StandardYamlCoder();
        cont = coder.decode(YAML_FILE, Container.class);
    }

    @Test
    public void testToJsonObject() throws CoderException {
        String yaml = coder.encode(cont);

        Container cont2 = coder.decode(yaml, Container.class);
        assertEquals(cont, cont2);
    }

    @Test
    public void testToJsonWriterObject() throws Exception {
        IOException ex = new IOException("expected exception");

        // writer that throws an exception when the write() method is invoked
        Writer wtr = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw ex;
            }

            @Override
            public void flush() throws IOException {
                // do nothing
            }

            @Override
            public void close() throws IOException {
                // do nothing
            }
        };

        assertThatThrownBy(() -> coder.encode(wtr, cont)).isInstanceOf(CoderException.class);

        wtr.close();
    }

    @Test
    public void testFromJsonStringClassOfT() throws Exception {
        String yaml = new String(Files.readAllBytes(YAML_FILE.toPath()), StandardCharsets.UTF_8);
        Container cont2 = coder.decode(yaml, Container.class);
        assertEquals(cont, cont2);
    }

    @Test
    public void testFromJsonReaderClassOfT() {
        assertNotNull(cont.item);
        assertTrue(cont.item.boolVal);
        assertEquals(1000L, cont.item.longVal);
        assertEquals(1010.1f, cont.item.floatVal, 0.00001);

        assertEquals(4, cont.list.size());
        assertNull(cont.list.get(1));

        assertEquals(20, cont.list.get(0).intVal);
        assertEquals("string 30", cont.list.get(0).stringVal);
        assertNull(cont.list.get(0).nullVal);

        assertEquals(40.0, cont.list.get(2).doubleVal, 0.000001);
        assertNull(cont.list.get(2).nullVal);
        assertNotNull(cont.list.get(2).another);
        assertEquals(50, cont.list.get(2).another.intVal);

        assertTrue(cont.list.get(3).boolVal);

        assertNotNull(cont.map);
        assertEquals(3, cont.map.size());

        assertNotNull(cont.map.get("itemA"));
        assertEquals("stringA", cont.map.get("itemA").stringVal);

        assertNotNull(cont.map.get("itemB"));
        assertEquals("stringB", cont.map.get("itemB").stringVal);

        assertNotNull(cont.map.get("itemC"));
        assertTrue(cont.map.get("itemC").boolVal);
    }


    @EqualsAndHashCode
    public static class Container {
        private Item item;
        private List<Item> list;
        private Map<String, Item> map;
    }

    @EqualsAndHashCode
    public static class Item {
        private boolean boolVal;
        private int intVal;
        private long longVal;
        private double doubleVal;
        private float floatVal;
        private String stringVal;
        private Object nullVal;
        private Item another;
    }
}
