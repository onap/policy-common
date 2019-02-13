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

package org.onap.policy.common.utils.gson;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.script.ScriptException;
import org.junit.Before;
import org.junit.Test;

public class GsonTestUtilsTest {
    private static final String HELLO = "hello";
    
    private GsonTestUtils utils;

    @Before
    public void setUp() {
        utils = new GsonTestUtils();
    }

    @Test
    public void testGetGson() {
        assertNotNull(utils.getGson());
    }

    @Test
    public void testGsonRoundTrip() {
        Data data = new Data();
        data.setId(500);

        // try will null text
        data.setText(null);
        assertEquals(data.toString(), utils.gsonRoundTrip(data, Data.class).toString());

        // try will non-null text
        data.setText(HELLO);
        assertEquals(data.toString(), utils.gsonRoundTrip(data, Data.class).toString());
    }

    @Test
    public void compareGsonObjectFile() {
        Data data = new Data();
        data.setId(500);
        data.setText(HELLO);

        utils.compareGson(data, new File("GsonTestUtilsTest.json"));

        // file not found
        assertThatThrownBy(() -> utils.compareGson(data, new File("GsonTestUtilsTest-NotFound.json")))
                        .isInstanceOf(JsonParseException.class).hasCauseInstanceOf(FileNotFoundException.class);

        // force I/O error while reading file
        GsonTestUtils utils2 = new GsonTestUtils() {
            @Override
            protected String readFile(File file) throws IOException {
                throw new IOException("expected exception");
            }
        };
        assertThatThrownBy(() -> utils2.compareGson(data, new File("GsonTestUtilsTest.json")))
                        .isInstanceOf(JsonParseException.class).hasCauseInstanceOf(IOException.class)
                        .hasMessage("error reading: GsonTestUtilsTest.json");
    }

    @Test
    public void compareGsonObjectString() {
        Data data = new Data();
        data.setId(600);
        data.setText(HELLO);

        utils.compareGson(data, "{'id': ${obj.id}, 'text': '${obj.text}'}".replace('\'', '"'));
    }

    @Test
    public void compareGsonObjectJsonElement() {
        Data data = new Data();
        data.setId(650);
        data.setText(HELLO);

        JsonObject json = new JsonObject();
        json.addProperty("id", data.getId());
        json.addProperty("text", data.getText());

        utils.compareGson(data, json);

        // mismatch
        data.setText("world");
        assertThatThrownBy(() -> utils.compareGson(data, json)).isInstanceOf(AssertionError.class);
    }

    @Test
    public void testApplyScripts() {
        Data data = new Data();
        data.setId(700);
        data.setText(HELLO);

        String result = utils.applyScripts("no interpolation", data);
        assertEquals("no interpolation", result);

        result = utils.applyScripts("${obj.id} at start, ${obj.text} in middle, and end ${obj.id}", data);
        assertEquals("700 at start, hello in middle, and end 700", result);

        // try null value
        data.setText(null);
        result = utils.applyScripts("use ${obj.text} this", data);
        assertEquals("use null this", result);

        assertThatThrownBy(() -> utils.applyScripts("use ${obj.text} this", null)).isInstanceOf(RuntimeException.class)
                        .hasCauseInstanceOf(ScriptException.class).hasMessage("cannot expand element: ${obj.text}");
    }

    @Test
    public void testReorderJsonObject() {
        JsonObject outer = new JsonObject();
        outer.addProperty("objA", true);
        outer.add("objANull", JsonNull.INSTANCE);
        outer.addProperty("objAStr", "obj-a-string");

        JsonObject inner = new JsonObject();
        inner.addProperty("objB", true);
        outer.add("objBNull", JsonNull.INSTANCE);

        JsonArray arr = new JsonArray();
        arr.add(100);
        arr.add(inner);
        arr.add(false);

        String expected = utils.gsonEncode(outer);

        utils.reorder(outer);
        assertEquals(expected, utils.gsonEncode(outer));
    }

    @Test
    public void testReorderJsonArray() {
        JsonObject inner = new JsonObject();
        inner.addProperty("objC", true);
        inner.add("objCNull", JsonNull.INSTANCE);
        inner.addProperty("objCStr", "obj-c-string");

        JsonArray arr = new JsonArray();
        arr.add(200);
        arr.add(inner);
        arr.add(false);

        String expected = utils.gsonEncode(arr);

        utils.reorder(arr);
        assertEquals(expected, utils.gsonEncode(arr));
    }

    @Test
    public void testReorderJsonElement() {
        // null element
        JsonElement jsonEl = null;
        assertNull(utils.reorder(jsonEl));

        // object element
        JsonObject obj = new JsonObject();
        obj.addProperty("objD", true);
        obj.add("objDNull", JsonNull.INSTANCE);
        obj.addProperty("objDStr", "obj-d-string");
        String expected = utils.gsonEncode(obj);
        jsonEl = obj;
        utils.reorder(jsonEl);
        assertEquals(expected, utils.gsonEncode(jsonEl));

        // boolean
        jsonEl = obj.get("objD");
        expected = utils.gsonEncode(jsonEl);
        utils.reorder(jsonEl);
        assertEquals(expected, utils.gsonEncode(jsonEl));

        // JsonNull
        jsonEl = JsonNull.INSTANCE;
        expected = utils.gsonEncode(jsonEl);
        utils.reorder(jsonEl);
        assertEquals(expected, utils.gsonEncode(jsonEl));

        // array element
        JsonObject inner = new JsonObject();
        inner.addProperty("objE", true);
        inner.add("objENull", JsonNull.INSTANCE);
        inner.addProperty("objEStr", "obj-e-string");

        JsonArray arr = new JsonArray();
        arr.add(300);
        arr.add(inner);
        arr.add(false);
        expected = utils.gsonEncode(arr);
        jsonEl = arr;
        utils.reorder(jsonEl);
        assertEquals(expected, utils.gsonEncode(jsonEl));
    }

    public static class Data {
        private int id;
        private String text;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "Data [id=" + id + ", text=" + text + "]";
        }
    }
}
