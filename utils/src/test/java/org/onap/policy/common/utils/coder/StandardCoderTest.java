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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class StandardCoderTest {
    private static final String EXPECTED_EXCEPTION = "expected exception";

    private static JsonParseException jpe = new JsonParseException(EXPECTED_EXCEPTION);

    private StandardCoder coder;
    private StandardCoderWithEx exCoder;
    private IOException ioe;

    @Before
    public void setUp() {
        coder = new StandardCoder();
        exCoder = new StandardCoderWithEx();
    }

    @Test
    public void testEncodeObject() throws Exception {
        List<Integer> arr = Arrays.asList(1100, 1110);
        assertEquals("[1100,1110]", coder.encode(arr));

        // test exception case
        assertThatThrownBy(() -> exCoder.encode(arr)).isInstanceOf(CoderException.class).hasCause(jpe);
    }

    @Test
    public void testEncodeWriterObject() throws Exception {
        List<Integer> arr = Arrays.asList(1200, 1210);
        StringWriter wtr = new StringWriter();
        coder.encode(wtr, arr);
        assertEquals("[1200,1210]", wtr.toString());

        // test exception case
        assertThatThrownBy(() -> exCoder.encode(new StringWriter(), arr)).isInstanceOf(CoderException.class)
                        .hasCause(jpe);
    }

    @Test
    public void testEncodeOutputStreamObject() throws Exception {
        List<Integer> arr = Arrays.asList(1300, 1310);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        coder.encode(stream, arr);
        assertEquals("[1300,1310]", stream.toString("UTF-8"));

        // test exception case
        assertThatThrownBy(() -> exCoder.encode(stream, arr)).isInstanceOf(CoderException.class).hasCause(jpe);

        // test exception when flushed
        IOException ex = new IOException(EXPECTED_EXCEPTION);
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                throw ex;
            }
        };
        assertThatThrownBy(() -> coder.encode(stream2, arr)).isInstanceOf(CoderException.class).hasCause(ex);
    }

    @Test
    public void testEncodeFileObject() throws Exception {
        List<Integer> arr = Arrays.asList(1400, 1410);
        File file = new File(getClass().getResource(StandardCoder.class.getSimpleName() + ".json").getFile() + "X");
        file.deleteOnExit();
        coder.encode(file, arr);
        assertEquals("[1400,1410]", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));

        // test exception case
        assertThatThrownBy(() -> exCoder.encode(file, arr)).isInstanceOf(CoderException.class).hasCause(jpe);

        // test exception when closed
        IOException ex = new IOException(EXPECTED_EXCEPTION);
        coder = spy(coder);
        when(coder.makeOutputStream(file)).thenReturn(new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                throw ex;
            }
        });
        assertThatThrownBy(() -> coder.encode(file, arr)).isInstanceOf(CoderException.class).hasCause(ex);
    }

    @Test
    public void testDecodeStringClass() throws Exception {
        String text = "[2200,2210]";
        assertEquals(text, coder.decode(text, JsonElement.class).toString());

        // test exception case
        assertThatThrownBy(() -> exCoder.decode(text, JsonElement.class)).isInstanceOf(CoderException.class)
                        .hasCause(jpe);
    }

    @Test
    public void testDecodeReaderClass() throws Exception {
        String text = "[2300,2310]";
        assertEquals(text, coder.decode(new StringReader(text), JsonElement.class).toString());

        // test exception case
        assertThatThrownBy(() -> exCoder.decode(new StringReader(text), JsonElement.class))
                        .isInstanceOf(CoderException.class).hasCause(jpe);
    }

    @Test
    public void testDecodeInputStreamClass() throws Exception {
        String text = "[2400,2410]";
        assertEquals(text, coder.decode(new ByteArrayInputStream(text.getBytes()), JsonElement.class).toString());

        // test exception case
        assertThatThrownBy(() -> exCoder.decode(new ByteArrayInputStream(text.getBytes()), JsonElement.class))
                        .isInstanceOf(CoderException.class).hasCause(jpe);
    }

    @Test
    public void testDecodeFileClass() throws Exception {
        File file = new File(getClass().getResource(StandardCoder.class.getSimpleName() + ".json").getFile());
        String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        assertEquals(text, coder.decode(file, JsonElement.class).toString());

        // test exception case
        assertThatThrownBy(() -> exCoder.decode(file, JsonElement.class)).isInstanceOf(CoderException.class)
                        .hasCause(jpe);

        // test IOException case
        ioe = new IOException(EXPECTED_EXCEPTION);
        assertThatThrownBy(() -> exCoder.decode(new File("unknown-file"), JsonElement.class))
                        .isInstanceOf(CoderException.class).hasCauseInstanceOf(FileNotFoundException.class);
    }

    /**
     * Coder that throws an exception when any of the toJson() or fromJson() methods are
     * invoked.
     */
    private class StandardCoderWithEx extends StandardCoder {
        @Override
        protected String toJson(Object object) {
            throw jpe;
        }

        @Override
        protected void toJson(Writer wtr, Object object) throws IOException {
            if (ioe != null) {
                throw ioe;
            }

            if (jpe != null) {
                throw jpe;
            }

            super.toJson(wtr, object);
        }

        @Override
        protected <T> T fromJson(String json, Class<T> clazz) {
            throw jpe;
        }

        @Override
        protected <T> T fromJson(Reader jsonReader, Class<T> clazz) {
            throw jpe;
        }
    }
}
