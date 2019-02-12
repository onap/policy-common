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

package org.onap.policy.common.gson.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.List;
import org.junit.Test;
import org.onap.policy.common.gson.JacksonExclusionStrategy;
import org.onap.policy.common.gson.internal.DataAdapterFactory.Data;
import org.onap.policy.common.gson.internal.FieldSerializer;

public class FieldSerializerTest {
    private static final String TEXT_FIELD_NAME = "text";

    private static DataAdapterFactory dataAdapter = new DataAdapterFactory();

    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(dataAdapter)
                    .setExclusionStrategies(new JacksonExclusionStrategy()).create();

    private FieldSerializer ser;

    protected String text;

    private List<Data> listField;

    @Test
    public void testAddToTree() throws Exception {
        ser = new FieldSerializer(gson, FieldSerializerTest.class.getDeclaredField(TEXT_FIELD_NAME));

        // serialize null value first
        text = null;

        JsonObject json = new JsonObject();
        ser.addToTree(this, json);
        assertTrue(json.get(TEXT_FIELD_NAME).isJsonNull());

        // serialize an actual value
        text = "hello";
        ser.addToTree(this, json);
        assertEquals("hello", json.get(TEXT_FIELD_NAME).getAsString());

        /*
         * check list field
         */
        listField = DataAdapterFactory.makeList();

        ser = new FieldSerializer(gson, FieldSerializerTest.class.getDeclaredField("listField"));

        dataAdapter.reset();
        JsonElement tree = ser.toJsonTree(listField);
        assertTrue(dataAdapter.isDataWritten());
        assertEquals(DataAdapterFactory.ENCODED_LIST, tree.toString());
    }

    @Test
    public void testAddToTree_GetEx() throws Exception {
        ser = new FieldSerializer(gson, FieldSerializerTest.class.getDeclaredField(TEXT_FIELD_NAME)) {
            @Override
            protected Object getFromObject(Object source) throws IllegalAccessException {
                throw new IllegalAccessException("expected exception");
            }
        };

        text = "world";

        assertThatThrownBy(() -> ser.addToTree(this, new JsonObject())).isInstanceOf(JsonParseException.class)
                        .hasMessage(FieldSerializer.GET_ERR + FieldSerializerTest.class.getName() + ".text");
    }
}
