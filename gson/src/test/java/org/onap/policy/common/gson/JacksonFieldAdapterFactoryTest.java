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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.policy.common.gson.JacksonExclusionStrategy;
import org.onap.policy.common.gson.JacksonFieldAdapterFactory;
import org.onap.policy.common.gson.annotation.GsonJsonProperty;

public class JacksonFieldAdapterFactoryTest {

    private static JacksonFieldAdapterFactory factory = new JacksonFieldAdapterFactory();

    private static Gson gson = new GsonBuilder().setExclusionStrategies(new JacksonExclusionStrategy())
                    .registerTypeAdapterFactory(factory).create();

    @Test
    public void testCreate() {
        // unhandled types
        assertNull(factory.create(gson, TypeToken.get(JsonElement.class)));
        assertNull(factory.create(gson, TypeToken.get(NothingToSerialize.class)));

        assertNotNull(factory.create(gson, TypeToken.get(Data.class)));
        assertNotNull(factory.create(gson, TypeToken.get(Derived.class)));

        Data data = new Data();

        // deserialize the wrong type
        Data data2 = gson.fromJson("{\"abc\":100}", Data.class);
        assertEquals(data.toString(), data2.toString());

        data.id = 10;
        data.text = "hello";

        String result = gson.toJson(data);
        data2 = gson.fromJson(result, Data.class);
        assertEquals(data.toString(), data2.toString());

        Derived der = new Derived();
        der.setId(20);
        der.text = "world";
        der.unserialized = "abc";

        result = gson.toJson(der);

        // should not contain the unserialized field
        assertFalse(result.contains("abc"));

        Derived der2 = gson.fromJson(result, Derived.class);
        der.unserialized = null;
        assertEquals(der.toString(), der2.toString());

        /*
         * check list fields
         */
        DataList lst = new DataList();
        lst.theList = new ArrayList<>();
        lst.theList.add(new Data(200, "text 20"));
        lst.theList.add(new Data(210, "text 21"));

        result = gson.toJson(lst);
        assertEquals("{'theList':[{'my-id':200,'text':'text 20'},{'my-id':210,'text':'text 21'}]}".replace('\'', '"'),
                        result);

        DataList lst2 = gson.fromJson(result, DataList.class);
        assertEquals(stripIdent(lst.toString()), stripIdent(lst2.toString()));
        assertEquals(Data.class, lst2.theList.get(0).getClass());
    }

    /**
     * Strips an object identifier from a text string.
     *
     * @param text text from which to strip the identifier
     * @return the text, without the identifier
     */
    private String stripIdent(String text) {
        return text.replaceFirst("@\\w+", "@");
    }

    private static class Data {
        @GsonJsonProperty("my-id")
        private int id;

        public String text;

        public Data() {
            super();
        }

        public Data(int id, String text) {
            this.id = id;
            this.text = text;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Data [id=" + id + ", text=" + text + "]";
        }
    }

    private static class Derived extends Data {
        // not serialized
        private String unserialized;

        @Override
        public String toString() {
            return "Derived [unserialized=" + unserialized + ", toString()=" + super.toString() + "]";
        }
    }

    private static class DataList {
        @GsonJsonProperty
        private List<Data> theList;
    }

    protected static class NothingToSerialize {
        // not serialized
        protected String unserialized;
    }
}
