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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.lang.reflect.GenericArrayType;
import java.util.LinkedList;
import java.util.TreeMap;
import org.junit.BeforeClass;
import org.junit.Test;

public class JacksonExclusionStrategyTest {

    private static JacksonExclusionStrategy strategy;
    private static Gson gson;

    @BeforeClass
    public static void setUpBeforeClass() {
        strategy = new JacksonExclusionStrategy();
        gson = new GsonBuilder().setExclusionStrategies(strategy).create();
    }

    @Test
    public void testWithGson() {
        Derived data = new Derived();
        data.setId(10);
        data.setText("some text");
        data.setValue("some value");

        // no fields should be serialized
        String result = gson.toJson(data);
        assertEquals("{}", result);

        // no fields should be deserialized
        result = "{'id':20, 'text':'my text', 'value':'my value'}".replace('\'', '"');
        Derived data2 = gson.fromJson(result, Derived.class);
        assertEquals(new Derived().toString(), data2.toString());
    }

    @Test
    public void testShouldSkipField() throws Exception {
        // should skip every field of Data
        assertTrue(strategy.shouldSkipField(new FieldAttributes(Data.class.getDeclaredField("id"))));
        assertTrue(strategy.shouldSkipField(new FieldAttributes(Data.class.getDeclaredField("text"))));

        // should not skip fields in Map
        assertFalse(strategy.shouldSkipField(new FieldAttributes(MyMap.class.getDeclaredField("mapId"))));
    }

    @Test
    public void testShouldSkipClass() {
        assertFalse(strategy.shouldSkipClass(null));
        assertFalse(strategy.shouldSkipClass(Object.class));
    }

    @Test
    public void testIsManaged() {
        assertTrue(JacksonExclusionStrategy.isManaged(Data.class));
        assertTrue(JacksonExclusionStrategy.isManaged(Intfc.class));
        assertTrue(JacksonExclusionStrategy.isManaged(com.google.gson.TypeAdapter.class));

        // generic classes
        assertFalse(JacksonExclusionStrategy.isManaged(new Data[0].getClass()));
        assertFalse(JacksonExclusionStrategy.isManaged(Enum.class));
        assertFalse(JacksonExclusionStrategy.isManaged(boolean.class));
        assertFalse(JacksonExclusionStrategy.isManaged(byte.class));
        assertFalse(JacksonExclusionStrategy.isManaged(short.class));
        assertFalse(JacksonExclusionStrategy.isManaged(int.class));
        assertFalse(JacksonExclusionStrategy.isManaged(long.class));
        assertFalse(JacksonExclusionStrategy.isManaged(float.class));
        assertFalse(JacksonExclusionStrategy.isManaged(double.class));
        assertFalse(JacksonExclusionStrategy.isManaged(char.class));
        assertFalse(JacksonExclusionStrategy.isManaged(Boolean.class));
        assertFalse(JacksonExclusionStrategy.isManaged(Byte.class));
        assertFalse(JacksonExclusionStrategy.isManaged(Short.class));
        assertFalse(JacksonExclusionStrategy.isManaged(Integer.class));
        assertFalse(JacksonExclusionStrategy.isManaged(Long.class));
        assertFalse(JacksonExclusionStrategy.isManaged(Float.class));
        assertFalse(JacksonExclusionStrategy.isManaged(Double.class));
        assertFalse(JacksonExclusionStrategy.isManaged(Character.class));
        assertFalse(JacksonExclusionStrategy.isManaged(String.class));
        assertFalse(JacksonExclusionStrategy.isManaged(MyMap.class));
        assertFalse(JacksonExclusionStrategy.isManaged(MyList.class));
        assertFalse(JacksonExclusionStrategy.isManaged(MyJson.class));
        assertFalse(JacksonExclusionStrategy.isManaged(GenericArrayType.class));
    }

    /**
     * Used to verify that no fields are exposed.
     */
    public static class Data {
        private int id;
        public String text;

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

    public static class Derived extends Data {
        protected String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Derived [value=" + value + ", " + super.toString() + "]";
        }
    }

    /**
     * Used to verify that enums are not managed.
     */
    public static enum Enum {
        UP, DOWN,
    }
    
    public static interface Intfc {
        int getId();
    }

    /**
     * Used to verify that Maps are not managed.
     */
    public static class MyMap extends TreeMap<String, Data> {
        private static final long serialVersionUID = 1L;

        private int mapId;

        public int getMapId() {
            return mapId;
        }
    }

    /**
     * Used to verify that Collections are not managed.
     */
    public static class MyList extends LinkedList<Data> {
        private static final long serialVersionUID = 1L;
    }

    /**
     * Used to verify that JsonElements are not managed.
     */
    public static class MyJson extends JsonElement {
        @Override
        public JsonElement deepCopy() {
            return null;
        }
    }
}
