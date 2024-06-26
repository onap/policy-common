/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

package org.onap.policy.common.endpoints.http.server.test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.ToString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;
import org.yaml.snakeyaml.error.YAMLException;

class YamlMessageBodyHandlerTest {
    private static final String EXPECTED_EXCEPTION = "expected exception";

    private static final String GEN_TYPE = "some-type";
    private static final String[] subtypes = {"yaml"};

    @SuppressWarnings("rawtypes")
    private static final Class GEN_CLASS = MyObject.class;

    @SuppressWarnings("unchecked")
    private static final Class<Object> CLASS_OBJ = GEN_CLASS;

    private YamlMessageBodyHandler hdlr;

    @BeforeEach
    public void setUp() {
        hdlr = new YamlMessageBodyHandler();
    }

    @Test
    void testIsWriteable() {
        for (String subtype : subtypes) {
            assertTrue(hdlr.isWriteable(null, null, null, new MediaType(GEN_TYPE, subtype)), "writeable " + subtype);

        }

        // the remaining should be FALSE

        // null media type
        assertFalse(hdlr.isWriteable(null, null, null, null));

        // null subtype
        assertFalse(hdlr.isWriteable(null, null, null, new MediaType(GEN_TYPE, null)));

        // text subtype
        assertFalse(hdlr.isWriteable(null, null, null, MediaType.TEXT_HTML_TYPE));
    }

    @Test
    void testGetSize() {
        assertEquals(-1, hdlr.getSize(null, null, null, null, null));
    }

    @Test
    void testWriteTo_testReadFrom() throws Exception {
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        MyObject obj1 = new MyObject(10);
        hdlr.writeTo(obj1, obj1.getClass(), CLASS_OBJ, null, null, null, outstr);

        Object obj2 = hdlr.readFrom(CLASS_OBJ, CLASS_OBJ, null, null, null,
                        new ByteArrayInputStream(outstr.toByteArray()));
        assertEquals(obj1.toString(), obj2.toString());
    }

    @Test
    void testWriteTo_DifferentTypes() throws Exception {
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();

        // use a derived type, but specify the base type when writing
        MyObject obj1 = new DerivedObject(10);
        hdlr.writeTo(obj1, obj1.getClass(), CLASS_OBJ, null, null, null, outstr);

        Object obj2 = hdlr.readFrom(CLASS_OBJ, CLASS_OBJ, null, null, null,
                        new ByteArrayInputStream(outstr.toByteArray()));
        assertEquals(obj1.toString(), obj2.toString());
    }

    @Test
    void testWriteTo_Ex() throws Exception {
        OutputStream outstr = new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw new IOException(EXPECTED_EXCEPTION);
            }
        };

        MyObject obj1 = new MyObject(10);
        assertThatThrownBy(() -> hdlr.writeTo(obj1, MyObject.class, CLASS_OBJ, null, null, null, outstr))
                        .isInstanceOf(YAMLException.class);

        outstr.close();
    }

    @Test
    void testIsReadable() {
        for (String subtype : subtypes) {
            assertTrue(hdlr.isReadable(null, null, null, new MediaType(GEN_TYPE, subtype)), "readable " + subtype);

        }

        // the remaining should be FALSE

        // null media type
        assertFalse(hdlr.isReadable(null, null, null, null));

        // null subtype
        assertFalse(hdlr.isReadable(null, null, null, new MediaType(GEN_TYPE, null)));

        // text subtype
        assertFalse(hdlr.isReadable(null, null, null, MediaType.TEXT_HTML_TYPE));
    }

    @Test
    void testReadFrom_DifferentTypes() throws Exception {
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        MyObject obj1 = new MyObject(10);
        hdlr.writeTo(obj1, obj1.getClass(), CLASS_OBJ, null, null, null, outstr);

        // use a derived type, but specify the base type when reading
        @SuppressWarnings("rawtypes")
        Class clazz = DerivedObject.class;

        @SuppressWarnings("unchecked")
        Class<Object> objclazz = clazz;

        Object obj2 = hdlr.readFrom(objclazz, CLASS_OBJ, null, null, null,
                        new ByteArrayInputStream(outstr.toByteArray()));
        assertEquals(obj1.toString(), obj2.toString());
    }

    @Test
    void testReadFrom_Ex() throws Exception {
        InputStream inpstr = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException(EXPECTED_EXCEPTION);
            }
        };

        assertThatThrownBy(() -> hdlr.readFrom(CLASS_OBJ, CLASS_OBJ, null, null, null, inpstr))
                        .isInstanceOf(YAMLException.class);

        inpstr.close();
    }

    @Test
    void testReadFrom_Invalid() throws Exception {
        InputStream inpstr = new ByteArrayInputStream("plain text".getBytes());

        assertThatThrownBy(() -> hdlr.readFrom(CLASS_OBJ, CLASS_OBJ, null, null, null, inpstr))
                        .isInstanceOf(YAMLException.class);

        inpstr.close();
    }

    @Test
    void testMapDouble() throws Exception {
        MyMap map = new MyMap();
        map.props = new HashMap<>();
        map.props.put("plainString", "def");
        map.props.put("negInt", -10);
        map.props.put("doubleVal", 12.5);
        map.props.put("posLong", 100000000000L);

        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        hdlr.writeTo(map, map.getClass(), map.getClass(), null, null, null, outstr);

        Object obj2 = hdlr.readFrom(Object.class, map.getClass(), null, null, null,
                        new ByteArrayInputStream(outstr.toByteArray()));
        assertEquals(map.toString(), obj2.toString());

        map = (MyMap) obj2;

        assertEquals(-10, map.props.get("negInt"));
        assertEquals(100000000000L, map.props.get("posLong"));
        assertEquals(12.5, map.props.get("doubleVal"));
    }

    public static class DerivedObject extends MyObject {
        public DerivedObject(int id) {
            super(id);
        }
    }

    @ToString
    public static class MyObject {
        private int id;

        public MyObject() {
            super();
        }

        public MyObject(int id) {
            this.id = id;
        }
    }

    private static class MyMap {
        private Map<String, Object> props;

        @Override
        public String toString() {
            return props.toString();
        }
    }
}
