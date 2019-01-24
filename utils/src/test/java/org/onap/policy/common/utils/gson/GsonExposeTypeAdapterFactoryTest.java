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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.policy.common.utils.gson.annotations.GsonExpose;

public class GsonExposeTypeAdapterFactoryTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private GsonExposeTypeAdapterFactory factory;
    private Gson gson;

    /**
     * Set up.
     */
    @Before
    public void setUp() {
        factory = new GsonExposeTypeAdapterFactory();
        gson = new GsonBuilder().registerTypeAdapterFactory(factory).create();
    }

    @Test
    public void testCreate() {
        // nothing exposed, thus no adapter
        assertNull(factory.create(gson, TypeToken.getParameterized(NoExpose.class)));

        // something exposed, thus there should be an adapter
        assertNotNull(factory.create(gson, TypeToken.getParameterized(Data.class)));

        Data data = new Data(10);
        String result = gson.toJson(data);
        Data data2 = gson.fromJson(result, Data.class);
        assertEquals(data.toString(), data2.toString());
    }

    @Test
    public void testFindExposeAnnotations_StaticMethod() {
        expectedEx.expect(JsonParseException.class);
        expectedEx.expectMessage(GsonExpose.class.getSimpleName() + " applied to 'static' method: "
                        + StaticData.class.getName() + ".getId");
        gson.toJson(new StaticData());
    }

    @Test
    public void testFindExposeAnnotations_VoidMethod() {
        expectedEx.expect(JsonParseException.class);
        expectedEx.expectMessage(GsonExpose.class.getSimpleName() + " applied to 'void' method: "
                        + SetData.class.getName() + ".setId");
        gson.toJson(new SetData());
    }

    @Test
    public void testSerializer_WithArgs() {
        expectedEx.expect(JsonParseException.class);
        expectedEx.expectMessage(GsonExpose.class.getSimpleName() + " applied to method with arguments: "
                        + GetWithArgsData.class.getName() + ".getAnotherId");
        gson.toJson(new GetWithArgsData());
    }

    @Test
    public void testDetmName() {
        Data data = new Data(100);
        JsonObject json = gson.toJsonTree(data).getAsJsonObject();

        // plain field
        assertEquals(100, json.get("id").getAsInt());

        // not exposed
        assertNull(json.get("notExposedId"));
        
        // exposed via serialized name
        assertEquals(100, json.get("some-id").getAsInt());

        // built from getXxx()
        assertEquals(100, json.get("myId").getAsInt());

        // built from isXxx()
        assertTrue(json.get("alive").getAsBoolean());

        // built from SerializedName
        assertEquals(100, json.get("another-id").getAsInt());

        // built from plainMethodName()
        assertEquals(100, json.get("otherId").getAsInt());

        // null value
        assertNull(json.get("nullValue"));
        assertFalse(json.has("nullValue"));
    }

    @Test
    public void testDetmName_Empty() {
        expectedEx.expect(JsonParseException.class);
        expectedEx.expectMessage("empty property name for " + GsonExpose.class.getSimpleName() + ": "
                        + EmptyNameData.class.getName() + ".get");
        gson.toJson(new EmptyNameData());
    }

    @Test
    public void testDetmName_TooShort() {
        expectedEx.expect(JsonParseException.class);
        expectedEx.expectMessage("invalid property name for " + GsonExpose.class.getSimpleName() + ": "
                        + ShortNameData.class.getName() + ".get");
        gson.toJson(new ShortNameData());
    }

    @Test
    public void testAddToTree_MethodException() {
        expectedEx.expect(JsonParseException.class);
        expectedEx.expectMessage("cannot invoke method for " + GsonExpose.class.getSimpleName() + ": "
                        + ExData.class.getName() + ".getEx");
        gson.toJson(new ExData());
    }

    /**
     * This class does not use the annotation.
     */
    public static class NoExpose {

        public int getMyId() {
            return 10;
        }
    }

    /**
     * This class uses the annotation.
     */
    public static class Data {
        private int id;

        public Data() {
            super();
        }

        public Data(int id) {
            this.id = id;
        }
        
        public int getNotExposedId() {
            return id;
        }

        @SerializedName("some-id")
        public int getSomeId() {
            return id;
        }

        @GsonExpose
        public int getMyId() {
            return id;
        }

        @GsonExpose
        public boolean isAlive() {
            return true;
        }

        @GsonExpose
        @SerializedName("another-id")
        public int getAnotherId() {
            return id;
        }

        @GsonExpose
        public int otherId() {
            return id;
        }

        @GsonExpose
        public String getNullValue() {
            return null;
        }

        @Override
        public String toString() {
            return "Data [id=" + id + "]";
        }
    }

    /**
     * This class uses the annotation on a "set" method.
     */
    public static class SetData {

        @GsonExpose
        public void setId(int id) {

        }
    }

    /**
     * This class uses the annotation on a "get" method that expects arguments.
     */
    public static class GetWithArgsData {

        @GsonExpose
        public int getAnotherId(int offset) {
            return offset;
        }
    }

    /**
     * This class uses the annotation on a "static" method.
     */
    public static class StaticData {

        @GsonExpose
        public static int getId() {
            return 0;
        }
    }

    /**
     * This class uses the annotation on a method whose name is empty.
     */
    public static class EmptyNameData {

        @GsonExpose
        @SerializedName("")
        public int getId() {
            return 0;
        }
    }

    /**
     * This class uses the annotation on a method whose name is too short.
     */
    public static class ShortNameData {

        @GsonExpose
        public int get() {
            return 0;
        }
    }

    /**
     * This class uses the annotation on a method that will throw an exception.
     */
    public static class ExData {

        @GsonExpose
        public int getEx() {
            throw new RuntimeException("expected exception");
        }
    }
}
