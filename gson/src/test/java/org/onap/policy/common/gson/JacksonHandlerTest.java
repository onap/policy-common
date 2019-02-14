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
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.ws.rs.core.MediaType;
import org.junit.Test;
import org.onap.policy.common.gson.JacksonHandler;
import org.onap.policy.common.gson.annotation.GsonJsonAnyGetter;
import org.onap.policy.common.gson.annotation.GsonJsonAnySetter;

public class JacksonHandlerTest {

    @Test
    public void test() throws Exception {
        JacksonHandler hdlr = new JacksonHandler();

        assertTrue(hdlr.isReadable(null, null, null, MediaType.APPLICATION_JSON_TYPE));
        assertFalse(hdlr.isReadable(null, null, null, MediaType.TEXT_PLAIN_TYPE));

        JsonObject expected = new JsonObject();
        expected.addProperty("myId", 100);
        expected.addProperty("value", "a value");
        expected.addProperty("abc", "def");
        expected.addProperty("hello", "world");

        Data data = new Data();
        data.id = 10;
        data.value = "a value";
        data.props = new HashMap<>();
        data.props.put("abc", "def");
        data.props.put("hello", "world");

        /*
         * Ensure everything serializes as expected.
         */
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        hdlr.writeTo(data, Data.class, Data.class, null, null, null, outstr);

        StringReader rdr = new StringReader(outstr.toString("UTF-8"));
        JsonObject json = new Gson().fromJson(rdr, JsonObject.class);

        assertEquals(expected, json);

        /*
         * Ensure everything deserializes as expected.
         */
        Data data2 = (Data) hdlr.readFrom(Object.class, Data.class, null, null, null,
                        new ByteArrayInputStream(outstr.toByteArray()));

        // id is not serialized, so we must copy it manually before comparing
        data2.id = data.id;

        assertEquals(data.toString(), data2.toString());
    }

    /**
     * This class includes all policy-specific gson annotations.
     */
    public static class Data {
        protected int id;

        protected String value;

        protected Map<String, String> props;

        public int getMyId() {
            return 100;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @GsonJsonAnyGetter
        public Map<String, String> getProps() {
            return props;
        }

        /**
         * Sets a property.
         * @param name property name
         * @param value new value
         */
        @GsonJsonAnySetter
        public void setProperty(String name, String value) {
            if (props == null) {
                props = new TreeMap<>();
            }

            props.put(name, value);
        }

        @Override
        public String toString() {
            return "Data [id=" + id + ", value=" + value + ", props=" + props + "]";
        }
    }
}
