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

package org.onap.policy.common.endpoints.http.server.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.server.internal.GsonMessageBodyHandler;

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

}
