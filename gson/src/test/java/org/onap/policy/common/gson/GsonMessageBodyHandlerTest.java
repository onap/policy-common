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

package org.onap.policy.common.gson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;

public class GsonMessageBodyHandlerTest {
    private static final String GEN_TYPE = "some-type";
    private static final String[] subtypes = {"json", "jSoN", "hello+json", "javascript", "x-javascript", "x-json"};

    @SuppressWarnings("rawtypes")
    private static final Class GEN_CLASS = MyObject.class;

    @SuppressWarnings("unchecked")
    private static final Class<Object> CLASS_OBJ = GEN_CLASS;

    private GsonMessageBodyHandler hdlr;

    @Before
    public void setUp() {
        hdlr = new GsonMessageBodyHandler();
    }

    @Test
    public void testIsWriteable() {
        // null media type
        assertTrue(hdlr.isWriteable(null, null, null, null));

        for (String subtype : subtypes) {
            assertTrue("writeable " + subtype, hdlr.isWriteable(null, null, null, new MediaType(GEN_TYPE, subtype)));

        }

        // the remaining should be FALSE

        // null subtype
        assertFalse(hdlr.isWriteable(null, null, null, new MediaType(GEN_TYPE, null)));

        // text subtype
        assertFalse(hdlr.isWriteable(null, null, null, MediaType.TEXT_HTML_TYPE));
    }

    @Test
    public void testGetSize() {
        assertEquals(-1, hdlr.getSize(null, null, null, null, null));
    }

    @Test
    public void testWriteTo_testReadFrom() throws Exception {
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        MyObject obj1 = new MyObject(10);
        hdlr.writeTo(obj1, obj1.getClass(), CLASS_OBJ, null, null, null, outstr);

        Object obj2 = hdlr.readFrom(CLASS_OBJ, CLASS_OBJ, null, null, null,
                        new ByteArrayInputStream(outstr.toByteArray()));
        assertEquals(obj1.toString(), obj2.toString());
    }

    @Test
    public void testWriteTo_DifferentTypes() throws Exception {
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();

        // use a derived type, but specify the base type when writing
        MyObject obj1 = new MyObject(10) {};
        hdlr.writeTo(obj1, obj1.getClass(), CLASS_OBJ, null, null, null, outstr);

        Object obj2 = hdlr.readFrom(CLASS_OBJ, CLASS_OBJ, null, null, null,
                        new ByteArrayInputStream(outstr.toByteArray()));
        assertEquals(obj1.toString(), obj2.toString());
    }

    @Test
    public void testIsReadable() {
        // null media type
        assertTrue(hdlr.isReadable(null, null, null, null));

        // null subtype
        assertFalse(hdlr.isReadable(null, null, null, new MediaType(GEN_TYPE, null)));

        for (String subtype : subtypes) {
            assertTrue("readable " + subtype, hdlr.isReadable(null, null, null, new MediaType(GEN_TYPE, subtype)));

        }

        // the remaining should be FALSE

        // null subtype
        assertFalse(hdlr.isReadable(null, null, null, new MediaType(GEN_TYPE, null)));

        // text subtype
        assertFalse(hdlr.isReadable(null, null, null, MediaType.TEXT_HTML_TYPE));
    }

    @Test
    public void testReadFrom_DifferentTypes() throws Exception {
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        MyObject obj1 = new MyObject(10);
        hdlr.writeTo(obj1, obj1.getClass(), CLASS_OBJ, null, null, null, outstr);

        // use a derived type, but specify the base type when reading
        @SuppressWarnings("rawtypes")
        Class clazz = new MyObject() {}.getClass();

        @SuppressWarnings("unchecked")
        Class<Object> objclazz = clazz;

        Object obj2 = hdlr.readFrom(objclazz, CLASS_OBJ, null, null, null,
                        new ByteArrayInputStream(outstr.toByteArray()));
        assertEquals(obj1.toString(), obj2.toString());
    }

    @Test
    public void testMapDouble() throws Exception {
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

    @Test
    public void testInterestingFields() throws IOException {
        InterestingFields data = new InterestingFields();
        data.theInstant = Instant.ofEpochMilli(1583249713500L);
        data.theUuid = UUID.fromString("a850cb9f-3c5e-417c-abfd-0679cdcd1ab0");
        data.theDate = LocalDateTime.of(2020, 2, 3, 4, 5, 6, 789000000);

        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        hdlr.writeTo(data, data.getClass(), data.getClass(), null, null, null, outstr);

        // ensure fields are encoded as expected

        // @formatter:off
        assertThat(outstr.toString(StandardCharsets.UTF_8))
                            .contains("1583249713500")
                            .contains("\"2020-02-03T04:05:06\"")
                            .contains("a850cb9f-3c5e-417c-abfd-0679cdcd1ab0");
        // @formatter:on

        Object obj2 = hdlr.readFrom(Object.class, data.getClass(), null, null, null,
                        new ByteArrayInputStream(outstr.toByteArray()));
        assertEquals(data.toString(), obj2.toString());
    }


    public static class MyObject {
        private int id;

        public MyObject() {
            super();
        }

        public MyObject(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "MyObject [id=" + id + "]";
        }
    }

    private static class MyMap {
        private Map<String, Object> props;

        @Override
        public String toString() {
            return props.toString();
        }
    }

    private static class InterestingFields {
        private LocalDateTime theDate;
        private Instant theInstant;
        private UUID theUuid;

        @Override
        public String toString() {
            return "InterestingFields [theInstant=" + theInstant.getEpochSecond() + "n" + theInstant.getNano()
                            + ", theUuid=" + theUuid + ", theDate=" + theDate.getYear() + "." + theDate.getMonthValue()
                            + "." + theDate.getDayOfMonth() + "." + theDate.getHour() + "." + theDate.getMinute() + "."
                            + theDate.getSecond() + "]";
        }
    }
}
