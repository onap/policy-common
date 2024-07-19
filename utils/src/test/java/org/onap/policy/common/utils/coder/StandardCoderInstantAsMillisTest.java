/*
 * ============LICENSE_START=======================================================
 * ONAP PAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonElement;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.ToString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StandardCoderInstantAsMillisTest {
    private static final long INSTANT_MILLIS = 1583249713500L;
    private static final String INSTANT_TEXT = String.valueOf(INSTANT_MILLIS);

    private StandardCoder coder;

    @BeforeEach
    public void setUp() {
        coder = new StandardCoderInstantAsMillis();
    }

    @Test
    void testConvert() throws CoderException {
        MyObject obj = makeObject();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = coder.convert(obj, LinkedHashMap.class);

        assertThat(map.toString()).contains(INSTANT_TEXT);

        MyObject obj2 = coder.convert(map, MyObject.class);
        assertEquals(obj.toString(), obj2.toString());
    }

    @Test
    void testEncodeDecode() throws CoderException {
        MyObject obj = makeObject();
        assertThat(coder.encode(obj, false)).contains(INSTANT_TEXT);
        assertThat(coder.encode(obj, true)).contains(INSTANT_TEXT);

        String json = coder.encode(obj);
        MyObject obj2 = coder.decode(json, MyObject.class);
        assertEquals(obj.toString(), obj2.toString());

        StringWriter wtr = new StringWriter();
        coder.encode(wtr, obj);
        json = wtr.toString();
        assertThat(json).contains(INSTANT_TEXT);

        StringReader rdr = new StringReader(json);
        obj2 = coder.decode(rdr, MyObject.class);
        assertEquals(obj.toString(), obj2.toString());
    }

    @Test
    void testJson() {
        MyObject obj = makeObject();
        assertThat(coder.toPrettyJson(obj)).contains(INSTANT_TEXT);
    }

    @Test
    void testToJsonTree_testFromJsonJsonElementClassT() {
        MyMap map = new MyMap();
        map.props = new LinkedHashMap<>();
        map.props.put("jel keyA", "jel valueA");
        map.props.put("jel keyB", "jel valueB");

        JsonElement json = coder.toJsonTree(map);
        assertEquals("{'props':{'jel keyA':'jel valueA','jel keyB':'jel valueB'}}".replace('\'', '"'), json.toString());

        Object result = coder.fromJson(json, MyMap.class);

        assertNotNull(result);
        assertEquals("{jel keyA=jel valueA, jel keyB=jel valueB}", result.toString());
    }

    @Test
    void testConvertFromDouble() throws Exception {
        String text = "[listA, {keyA=100}, 200]";
        assertEquals(text, coder.decode(text, Object.class).toString());

        text = "{keyB=200}";
        assertEquals(text, coder.decode(text, Object.class).toString());
    }

    @Test
    void testToStandard() throws Exception {
        MyObject obj = makeObject();
        StandardCoderObject sco = coder.toStandard(obj);
        assertNotNull(sco.getData());
        assertEquals("{'abc':'xyz','instant':1583249713500}".replace('\'', '"'), sco.getData().toString());

        // class instead of object -> exception
        assertThatThrownBy(() -> coder.toStandard(String.class)).isInstanceOf(CoderException.class);
    }

    @Test
    void testFromStandard() throws Exception {
        MyObject obj = new MyObject();
        obj.abc = "pdq";
        StandardCoderObject sco = coder.toStandard(obj);

        MyObject obj2 = coder.fromStandard(sco, MyObject.class);
        assertEquals(obj.toString(), obj2.toString());

        // null class -> exception
        assertThatThrownBy(() -> coder.fromStandard(sco, null)).isInstanceOf(CoderException.class);
    }


    private MyObject makeObject() {
        MyObject obj = new MyObject();
        obj.abc = "xyz";
        obj.instant = Instant.ofEpochMilli(INSTANT_MILLIS);
        return obj;
    }


    @ToString
    private static class MyObject {
        private String abc;
        private Instant instant;
    }

    public static class MyMap {
        private Map<String, Object> props;

        @Override
        public String toString() {
            return props.toString();
        }
    }
}
