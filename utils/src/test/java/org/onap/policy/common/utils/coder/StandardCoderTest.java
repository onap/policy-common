/*
 * ============LICENSE_START=======================================================
 * ONAP PAP
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class StandardCoderTest {
    private static final String EXPECTED_EXCEPTION = "expected exception";

    private static final JsonParseException jpe = new JsonParseException(EXPECTED_EXCEPTION);
    private static final IOException ioe = new IOException(EXPECTED_EXCEPTION);

    private StandardCoder coder;

    @Before
    public void setUp() {
        coder = new StandardCoder();
    }

    @Test
    public void testEncodeObject() throws Exception {
        List<Integer> arr = Arrays.asList(1100, 1110);
        assertEquals("[1100,1110]", coder.encode(arr));

        // test exception case
        coder = spy(new StandardCoder());
        when(coder.toJson(arr)).thenThrow(jpe);
        assertThatThrownBy(() -> coder.encode(arr)).isInstanceOf(CoderException.class).hasCause(jpe);
    }

    @Test
    public void testEncodeWriterObject() throws Exception {
        List<Integer> arr = Arrays.asList(1200, 1210);
        StringWriter wtr = new StringWriter();
        coder.encode(wtr, arr);
        assertEquals("[1200,1210]", wtr.toString());

        // test json exception
        coder = spy(new StandardCoder());
        doThrow(jpe).when(coder).toJson(wtr, arr);
        assertThatThrownBy(() -> coder.encode(wtr, arr)).isInstanceOf(CoderException.class).hasCause(jpe);
    }

    @Test
    public void testEncodeOutputStreamObject() throws Exception {
        List<Integer> arr = Arrays.asList(1300, 1310);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        coder.encode(stream, arr);
        assertEquals("[1300,1310]", stream.toString("UTF-8"));

        // test json exception
        Writer wtr = new StringWriter();
        coder = spy(new StandardCoder());
        when(coder.makeWriter(stream)).thenReturn(wtr);
        doThrow(jpe).when(coder).toJson(wtr, arr);
        assertThatThrownBy(() -> coder.encode(stream, arr)).isInstanceOf(CoderException.class).hasCause(jpe);

        // test exception when flushed
        wtr = spy(new OutputStreamWriter(stream));
        doThrow(ioe).when(wtr).flush();
        coder = spy(new StandardCoder());
        when(coder.makeWriter(stream)).thenReturn(wtr);
        assertThatThrownBy(() -> coder.encode(stream, arr)).isInstanceOf(CoderException.class).hasCause(ioe);
    }

    @Test
    public void testEncodeFileObject() throws Exception {
        File file = new File(getClass().getResource(StandardCoder.class.getSimpleName() + ".json").getFile() + "X");
        file.deleteOnExit();
        List<Integer> arr = Arrays.asList(1400, 1410);
        coder.encode(file, arr);
        assertEquals("[1400,1410]", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));

        // test json exception
        StringWriter wtr = new StringWriter();
        coder = spy(new StandardCoder());
        when(coder.makeWriter(file)).thenReturn(wtr);
        doThrow(jpe).when(coder).toJson(wtr, arr);
        assertThatThrownBy(() -> coder.encode(file, arr)).isInstanceOf(CoderException.class).hasCause(jpe);

        // test exception when closed
        coder = spy(new StandardCoder());
        wtr = spy(new StringWriter());
        doThrow(ioe).when(wtr).close();
        coder = spy(new StandardCoder());
        when(coder.makeWriter(file)).thenReturn(wtr);
        assertThatThrownBy(() -> coder.encode(file, arr)).isInstanceOf(CoderException.class).hasCause(ioe);
    }

    @Test
    public void testDecodeStringClass() throws Exception {
        String text = "[2200,2210]";
        assertEquals(text, coder.decode(text, JsonElement.class).toString());

        // test json exception
        coder = spy(new StandardCoder());
        when(coder.fromJson(text, JsonElement.class)).thenThrow(jpe);
        assertThatThrownBy(() -> coder.decode(text, JsonElement.class)).isInstanceOf(CoderException.class)
                        .hasCause(jpe);
    }

    @Test
    public void testDecodeReaderClass() throws Exception {
        String text = "[2300,2310]";
        assertEquals(text, coder.decode(new StringReader(text), JsonElement.class).toString());

        // test json exception
        coder = spy(new StandardCoder());
        StringReader rdr = new StringReader(text);
        when(coder.fromJson(rdr, JsonElement.class)).thenThrow(jpe);
        assertThatThrownBy(() -> coder.decode(rdr, JsonElement.class)).isInstanceOf(CoderException.class).hasCause(jpe);
    }

    @Test
    public void testDecodeInputStreamClass() throws Exception {
        String text = "[2400,2410]";
        assertEquals(text,
                        coder.decode(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), JsonElement.class)
                                        .toString());

        // test json exception
        coder = spy(new StandardCoder());
        ByteArrayInputStream stream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        StringReader rdr = new StringReader(text);
        when(coder.makeReader(stream)).thenReturn(rdr);
        when(coder.fromJson(rdr, JsonElement.class)).thenThrow(jpe);
        assertThatThrownBy(() -> coder.decode(stream, JsonElement.class)).isInstanceOf(CoderException.class)
                        .hasCause(jpe);
    }

    @Test
    public void testDecodeFileClass() throws Exception {
        File file = new File(getClass().getResource(StandardCoder.class.getSimpleName() + ".json").getFile());
        String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertEquals(text, coder.decode(file, JsonElement.class).toString());

        // test FileNotFoundException case
        assertThatThrownBy(() -> coder.decode(new File("unknown-file"), JsonElement.class))
                        .isInstanceOf(CoderException.class).hasCauseInstanceOf(FileNotFoundException.class);

        // test json exception
        Reader rdr = new StringReader(text);
        coder = spy(new StandardCoder());
        when(coder.makeReader(file)).thenReturn(rdr);
        when(coder.fromJson(rdr, JsonElement.class)).thenThrow(jpe);
        assertThatThrownBy(() -> coder.decode(file, JsonElement.class)).isInstanceOf(CoderException.class)
                        .hasCause(jpe);

        // test IOException case
        rdr = spy(new FileReader(file));
        doThrow(ioe).when(rdr).close();
        coder = spy(new StandardCoder());
        when(coder.makeReader(file)).thenReturn(rdr);
        assertThatThrownBy(() -> coder.decode(file, JsonElement.class)).isInstanceOf(CoderException.class)
                        .hasCause(ioe);
    }

    @Test
    public void testToStandard() throws Exception {
        MyObject obj = new MyObject();
        obj.abc = "xyz";
        StandardCoderObject sco = coder.toStandard(obj);
        assertNotNull(sco.getData());
        assertEquals("{'abc':'xyz'}".replace('\'', '"'), sco.getData().toString());

        // class instead of object -> exception
        assertThatThrownBy(() -> coder.toStandard(String.class)).isInstanceOf(CoderException.class);
    }

    @Test
    public void testFromStandard() throws Exception {
        MyObject obj = new MyObject();
        obj.abc = "pdq";
        StandardCoderObject sco = coder.toStandard(obj);

        MyObject obj2 = coder.fromStandard(sco, MyObject.class);
        assertEquals(obj.toString(), obj2.toString());

        // null class -> exception
        assertThatThrownBy(() -> coder.fromStandard(sco, null)).isInstanceOf(CoderException.class);
    }

    @Test
    public void testStandardTypeAdapter() throws Exception {
        String json = "{'abc':'def'}".replace('\'', '"');
        StandardCoderObject sco = coder.fromJson(json, StandardCoderObject.class);
        assertNotNull(sco.getData());
        assertEquals(json, sco.getData().toString());
        assertEquals(json, coder.toJson(sco));

        // invalid json -> exception
        assertThatThrownBy(() -> coder.fromJson(new StringReader("["), StandardCoderObject.class));
    }

    @Test
    public void testMapDouble() throws Exception {
        MyMap map = new MyMap();
        map.props = new HashMap<>();
        map.props.put("plainString", "def");
        map.props.put("negInt", -10);
        map.props.put("doubleVal", 12.5);
        map.props.put("posLong", 100000000000L);

        String json = coder.encode(map);

        map.props.clear();
        map = coder.decode(json, MyMap.class);

        assertEquals(-10, map.props.get("negInt"));
        assertEquals(100000000000L, map.props.get("posLong"));
        assertEquals(12.5, map.props.get("doubleVal"));
    }


    private static class MyObject {
        private String abc;

        @Override
        public String toString() {
            return "MyObject [abc=" + abc + "]";
        }
    }

    private static class MyMap {
        private Map<String, Object> props;
    }
}
