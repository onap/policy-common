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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Test;

public class StandardCoderObjectTest {
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
        sco = new StandardCoderObject(JSON);
    }

    @Test
    public void testStandardCoderObject() {
        // verify that this doesn't throw an exception
        new StandardCoderObject();
    }

    @Test
    public void testStandardCoderObjectString() throws Exception {
        assertEquals(VAL1, sco.getString(PROP1));
    }

    @Test
    public void testAsString() throws Exception {
        assertEquals(JSON, sco.asString());
    }

    @Test
    public void testAsObject() throws Exception {
        MyObject obj = sco.asObject(MyObject.class);
        assertEquals(VAL1, obj.abc);
        assertEquals(VAL2, obj.ghi.jkl);

        // convert null -> exception
        assertThatThrownBy(() -> sco.asObject(null)).isInstanceOf(CoderException.class);
    }

    @Test
    public void testReadFromObject() throws Exception {
        MyObject obj = new MyObject();
        obj.abc = VAL1;
        obj.ghi = new SubObject();
        obj.ghi.jkl = VAL2;

        sco = StandardCoderObject.readFromObject(obj);
        assertEquals(JSON, sco.asString());

        // class instead of object -> exception
        assertThatThrownBy(() -> StandardCoderObject.readFromObject(String.class)).isInstanceOf(CoderException.class);
    }

    @Test
    public void testReadFromReader() throws Exception {
        sco = StandardCoderObject.readFrom(new StringReader(JSON));
        assertEquals(JSON, sco.asString());
    }

    @Test
    public void testReadFromInputStream() throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(JSON.getBytes(StandardCharsets.UTF_8));
        sco = StandardCoderObject.readFrom(stream);
        assertEquals(JSON, sco.asString());
    }

    @Test
    public void testReadFromFile() throws Exception {
        File file = new File(getClass().getResource(StandardCoderObject.class.getSimpleName() + ".json").getFile());
        sco = StandardCoderObject.readFrom(file);
        assertEquals(JSON, sco.asString());
    }

    @Test
    public void testWriteToWriter() throws Exception {
        StringWriter wtr = new StringWriter();
        sco.writeTo(wtr);
        assertEquals(JSON, wtr.toString());
    }

    @Test
    public void testWriteToOutputStream() throws Exception {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        sco.writeTo(stream);
        assertEquals(JSON, new String(stream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testWriteToFile() throws Exception {
        File file = new File(
                        getClass().getResource(StandardCoderObject.class.getSimpleName() + ".json").getFile() + "X");
        file.deleteOnExit();
        sco.writeTo(file);

        assertEquals(JSON, new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void testGetString() throws Exception {
        // one field
        assertEquals(VAL1, sco.getString(PROP1));

        // multiple fields
        assertEquals(VAL2, sco.getString(PROP2, PROP2b));

        // read from null object
        assertNull(new StandardCoderObject().getString());
        assertNull(new StandardCoderObject().getString(PROP1));

        // not a primitive
        assertNull(new StandardCoderObject("{'abc':[]}").getString(PROP1));

        // not a JSON object
        assertNull(new StandardCoderObject("{'abc':[]}").getString(PROP1, PROP2));
    }


    private static class SubObject {
        private String jkl;
    }

    private static class MyObject {
        private String abc;
        private SubObject ghi;
    }
}
