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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

public class MapDoubleAdapterFactoryTest {
    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new MapDoubleAdapterFactory()).create();

    @Test
    @SuppressWarnings("unchecked")
    public void test() {
        MyMap map = new MyMap();
        map.props = new HashMap<>();
        map.props.put("plainString", "def");
        map.props.put("posInt", 10);
        map.props.put("negInt", -10);
        map.props.put("doubleVal", 12.5);
        map.props.put("posLong", 100000000000L);
        map.props.put("negLong", -100000000000L);

        Map<String, Object> nested = new LinkedHashMap<>();
        map.props.put("nestedMap", nested);
        nested.put("nestedString", "world");
        nested.put("nestedInt", 50);

        String json = gson.toJson(map);

        map.props.clear();
        map = gson.fromJson(json, MyMap.class);

        assertEquals(json, gson.toJson(map));

        assertEquals(10, map.props.get("posInt"));
        assertEquals(-10, map.props.get("negInt"));
        assertEquals(100000000000L, map.props.get("posLong"));
        assertEquals(-100000000000L, map.props.get("negLong"));
        assertEquals(12.5, map.props.get("doubleVal"));
        assertEquals(nested, map.props.get("nestedMap"));

        nested = (Map<String, Object>) map.props.get("nestedMap");
        assertEquals(50, nested.get("nestedInt"));
    }

    @Test
    public void test_ValueIsNotObject() {
        MyDoubleMap map = new MyDoubleMap();
        map.props = new LinkedHashMap<>();
        map.props.put("plainDouble", 13.5);
        map.props.put("doubleAsInt", 100.0);

        String json = gson.toJson(map);

        map.props.clear();
        map = gson.fromJson(json, MyDoubleMap.class);

        // everything should still be Double - check by simply accessing
        map.props.get("plainDouble");
        map.props.get("doubleAsInt");
    }

    @Test
    public void test_KeyIsNotString() {
        MyObjectMap map = new MyObjectMap();

        map.props = new LinkedHashMap<>();
        map.props.put("plainDouble2", 14.5);
        map.props.put("doubleAsInt2", 200.0);

        String json = gson.toJson(map);

        map.props.clear();
        map = gson.fromJson(json, MyObjectMap.class);

        // everything should still be Double
        assertEquals(14.5, map.props.get("plainDouble2"));
        assertEquals(200.0, map.props.get("doubleAsInt2"));
    }

    private static class MyMap {
        private Map<String, Object> props;
    }

    private static class MyDoubleMap {
        private Map<String, Double> props;
    }

    private static class MyObjectMap {
        private Map<Object, Object> props;
    }

}
