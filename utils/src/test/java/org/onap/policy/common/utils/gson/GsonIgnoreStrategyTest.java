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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.utils.gson.GsonIgnoreStrategy;
import org.onap.policy.common.utils.gson.annotations.GsonIgnore;

public class GsonIgnoreStrategyTest {
    private static GsonIgnoreStrategy strategy;
    private static Gson gson;

    @BeforeClass
    public static void setUpBeforeClass() {
        strategy = new GsonIgnoreStrategy();
        gson = new GsonBuilder().setExclusionStrategies(strategy).create();
    }

    @Test
    public void testShouldSkipField() {
        Data data = new Data("abc", "my-name", "world", "my-answer");
        String result = gson.toJson(data);

        // should not contain the name field
        assertFalse(result.contains("name"));

        // should not contain the answer field
        assertFalse(result.contains("answer"));

        Data expected = new Data("abc", null, "world", null);
        Data actual = gson.fromJson(result, Data.class);
        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void testShouldSkipClass() {
        // always false, regardless of the class
        assertFalse(strategy.shouldSkipClass(Object.class));
        assertFalse(strategy.shouldSkipClass(Data.class));
    }

    private static class Data {
        private String id;

        @GsonIgnore
        private String name;

        private String value;

        @GsonIgnore
        private String answer;

        public Data(String id, String name, String value, String answer) {
            this.id = id;
            this.name = name;
            this.value = value;
            this.answer = answer;
        }

        @Override
        public String toString() {
            return "Data [id=" + id + ", name=" + name + ", value=" + value + ", answer=" + answer + "]";
        }
    }
}
