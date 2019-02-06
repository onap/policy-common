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

package org.onap.policy.common.utils.gson.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonParseException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;

public class ClassWalkerTest {
    private MyWalker walker;

    /**
     * Set up.
     */
    @Before
    public void setUp() {
        walker = new MyWalker();
    }

    @Test
    public void testExamineClassOfQ_testExamineField_testExamineInField_testExamineOutField() {
        walker.walkClassHierarchy(DerivedFromBottom.class);

        assertEquals("[Intfc1, Intfc2, Intfc1, Intfc3, Bottom, DerivedFromBottom]", walker.classes.toString());

        List<String> inFields = walker.getInProps(Field.class).stream().map(field -> field.getName())
                        .collect(Collectors.toList());
        Collections.sort(inFields);
        assertEquals("[exposedField, overriddenValue, transField]", inFields.toString());

        List<String> outFields = walker.getInProps(Field.class).stream().map(field -> field.getName())
                        .collect(Collectors.toList());
        Collections.sort(outFields);
        assertEquals("[exposedField, overriddenValue, transField]", outFields.toString());

        // should work with interfaces without throwing an NPE
        walker.walkClassHierarchy(Intfc1.class);
    }

    @Test
    public void testHasAnyGetter() {
        walker.walkClassHierarchy(Object.class);
        assertNull(walker.getAnyGetter());
        assertNull(walker.getAnySetter());

        walker.walkClassHierarchy(AnyGetterIgnored.class);
        assertNull(walker.getAnyGetter());
        assertNull(walker.getAnySetter());

        walker.walkClassHierarchy(AnyGetterOnly.class);
        assertNotNull(walker.getAnyGetter());
        assertNull(walker.getAnySetter());
    }

    @Test
    public void testHasAnySetter() {
        walker.walkClassHierarchy(Object.class);
        assertNull(walker.getAnySetter());
        assertNull(walker.getAnyGetter());

        walker.walkClassHierarchy(AnySetterIgnored.class);
        assertNull(walker.getAnySetter());
        assertNull(walker.getAnyGetter());

        walker.walkClassHierarchy(AnySetterOnly.class);
        assertNotNull(walker.getAnySetter());
        assertNull(walker.getAnyGetter());
    }

    @Test
    public void testExamineMethod() {
        walker.walkClassHierarchy(DerivedFromData.class);

        assertEquals("[Data, DerivedFromData]", walker.classes.toString());

        // ensure all methods were examined
        Collections.sort(walker.methods);
        List<String> lst = Arrays.asList("getId", "getValue", "getOnlyOut", "getStatic", "getText", "getTheMap",
                        "getUnserialized", "getValue", "getWithParams", "setExtraParams", "setId", "setMap",
                        "setMapValue", "setMissingParams", "setNonPublic", "setOnlyIn", "setText", "setUnserialized",
                        "setValue", "setValue", "wrongGetPrefix", "wrongSetPrefix");
        Collections.sort(lst);
        assertEquals(lst.toString(), walker.methods.toString());

        assertNotNull(walker.getAnyGetter());
        assertEquals("getTheMap", walker.getAnyGetter().getName());

        List<String> getters = walker.getOutProps(Method.class).stream().map(method -> method.getName())
                        .collect(Collectors.toList());
        Collections.sort(getters);
        assertEquals("[getId, getOnlyOut, getValue]", getters.toString());

        assertNotNull(walker.getAnySetter());
        assertEquals("setMapValue", walker.getAnySetter().getName());

        List<String> setters = walker.getInProps(Method.class).stream().map(method -> method.getName())
                        .collect(Collectors.toList());
        Collections.sort(setters);
        assertEquals("[setId, setOnlyIn, setValue]", setters.toString());

        // getter with invalid parameter count
        assertThatThrownBy(() -> walker.walkClassHierarchy(AnyGetterMismatchParams.class))
                        .isInstanceOf(JsonParseException.class).hasMessage(ClassWalker.ANY_GETTER_MISMATCH_ERR
                                        + AnyGetterMismatchParams.class.getName() + ".getTheMap");

        // setter with too few parameters
        assertThatThrownBy(() -> walker.walkClassHierarchy(AnySetterTooFewParams.class))
                        .isInstanceOf(JsonParseException.class).hasMessage(ClassWalker.ANY_SETTER_MISMATCH_ERR
                                        + AnySetterTooFewParams.class.getName() + ".setOverride");

        // setter with too many parameters
        assertThatThrownBy(() -> walker.walkClassHierarchy(AnySetterTooManyParams.class))
                        .isInstanceOf(JsonParseException.class).hasMessage(ClassWalker.ANY_SETTER_MISMATCH_ERR
                                        + AnySetterTooManyParams.class.getName() + ".setOverride");

        // setter with invalid parameter type
        assertThatThrownBy(() -> walker.walkClassHierarchy(AnySetterInvalidParam.class))
                        .isInstanceOf(JsonParseException.class).hasMessage(ClassWalker.ANY_SETTER_TYPE_ERR
                                        + AnySetterInvalidParam.class.getName() + ".setOverride");
    }

    @Test
    public void testExamineMethod_AnyGetter() {
        walker.walkClassHierarchy(AnyGetterOverride.class);

        assertNotNull(walker.getAnyGetter());
        assertEquals("getOverride", walker.getAnyGetter().getName());
    }

    @Test
    public void testExamineMethod_AnySetter() {
        walker.walkClassHierarchy(AnySetterOverride.class);

        assertNotNull(walker.getAnySetter());
        assertEquals("setOverride", walker.getAnySetter().getName());
    }

    @Test
    public void testGetInNotIgnored_testGetOutNotIgnored() {
        walker.walkClassHierarchy(DerivedFromData.class);

        assertEquals("[id, onlyIn, text, value]", new TreeSet<>(walker.getInNotIgnored()).toString());
        assertEquals("[id, onlyOut, text, value]", new TreeSet<>(walker.getOutNotIgnored()).toString());
    }

    /**
     * Walker subclass that records items that are examined.
     */
    private static class MyWalker extends ClassWalker {
        private List<String> classes = new ArrayList<>();
        private List<String> methods = new ArrayList<>();

        @Override
        protected void examine(Class<?> clazz) {
            classes.add(clazz.getSimpleName());

            super.examine(clazz);
        }

        @Override
        protected void examine(Method method) {
            if (Adapter.isManaged(method)) {
                methods.add(method.getName());
            }

            super.examine(method);
        }
    }

    protected static interface Intfc1 {
        int id = 1000;
    }

    protected static interface Intfc2 {
        String text = "intfc2-text";
    }

    private static interface Intfc3 {

    }

    protected static class Bottom implements Intfc1, Intfc3 {
        private int id;
        public String value;

        public String invalid$fieldName;

        @JsonProperty("exposed")
        private String exposedField;

        @JsonIgnore
        public int ignored;

        public transient int ignoredTransField;

        @JsonProperty("trans")
        public transient int transField;

        @JsonIgnore
        public int getId() {
            return id;
        }

        @JsonIgnore
        public void setId(int id) {
            this.id = id;
        }
    }

    protected static class DerivedFromBottom extends Bottom implements Intfc1, Intfc2 {
        private String text;
        protected String anotherValue;

        @JsonProperty("value")
        public String overriddenValue;

        @JsonIgnore
        public String getText() {
            return text;
        }

        @JsonIgnore
        public void setText(String text) {
            this.text = text;
        }
    }

    protected static class Data {
        private int id;
        private String text;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        // not public, but property provided
        @JsonProperty("text")
        protected String getText() {
            return text;
        }

        // this will be ignored, because there's already a field by this name
        public void setText(String text) {
            this.text = text;
        }

        // should only show up in the output list
        public int getOnlyOut() {
            return 1100;
        }

        // will be overridden by subclass
        @JsonProperty("super-value-getter")
        public String getValue() {
            return null;
        }

        // will be overridden by subclass
        @JsonProperty("super-value-setter")
        public void setValue(String value) {
            // do nothing
        }
    }

    protected static class DerivedFromData extends Data {
        // not serialized
        private String unserialized;

        // overrides private field and public method from Data
        public String text;

        private Map<String, String> map;

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @JsonAnyGetter
        public Map<String, String> getTheMap() {
            return map;
        }

        @JsonIgnore
        public void setMap(Map<String, String> map) {
            this.map = map;
        }

        @JsonAnySetter
        public void setMapValue(String key, String value) {
            if (map == null) {
                map = new TreeMap<>();
            }

            map.put(key, value);
        }

        @JsonIgnore
        public String getUnserialized() {
            return unserialized;
        }

        @JsonIgnore
        public void setUnserialized(String unserialized) {
            this.unserialized = unserialized;
        }

        // should only show up in the input list
        public void setOnlyIn(int value) {
            // do nothing
        }

        // has a param - shouldn't be serialized
        public int getWithParams(String text) {
            return 1000;
        }

        // too few params - shouldn't be serialized
        public void setMissingParams() {
            // do nothing
        }

        // too many params - shouldn't be serialized
        public void setExtraParams(String text, String moreText) {
            // do nothing
        }

        // not public - shouldn't be serialized
        protected void setNonPublic(String text) {
            // do nothing
        }

        // doesn't start with "get"
        public String wrongGetPrefix() {
            return null;
        }

        // doesn't start with "set"
        public void wrongSetPrefix(String text) {
            // do nothing
        }

        // static
        public static String getStatic() {
            return null;
        }
    }

    /**
     * The "get" method has an incorrect argument count.
     */
    private static class AnyGetterMismatchParams {
        @JsonAnyGetter
        public Map<String, String> getTheMap(String arg) {
            return new TreeMap<>();
        }
    }

    /**
     * Has {@link JsonAnyGetter} method.
     */
    private static class AnyGetterOnly {
        @JsonAnyGetter
        private Map<String, Integer> getOverride() {
            return null;
        }
    }

    /**
     * Has {@link JsonAnyGetter} method, but it's ignored.
     */
    private static class AnyGetterIgnored {
        @JsonAnyGetter
        @JsonIgnore
        private Map<String, Integer> getOverride() {
            return null;
        }
    }

    /**
     * Has {@link JsonAnySetter} method.
     */
    private static class AnySetterOnly {
        @JsonAnySetter
        private void setOverride(String key, int value) {
            // do nothing
        }
    }

    /**
     * Has {@link JsonAnySetter} method, but it's ignored.
     */
    private static class AnySetterIgnored {
        @JsonAnySetter
        @JsonIgnore
        private void setOverride(String key, int value) {
            // do nothing
        }
    }

    /**
     * Has {@link JsonAnyGetter} method that overrides the super class' method.
     */
    private static class AnyGetterOverride extends DerivedFromData {
        private Map<String, Integer> overMap;

        @JsonAnyGetter
        private Map<String, Integer> getOverride() {
            return overMap;
        }
    }

    /**
     * Has {@link JsonAnySetter} method that overrides the super class' method.
     */
    private static class AnySetterOverride extends DerivedFromData {
        private Map<String, Integer> overMap;

        @JsonAnySetter
        private void setOverride(String key, int value) {
            if (overMap == null) {
                overMap = new TreeMap<>();
            }

            overMap.put(key, value);
        }
    }

    /**
     * Has {@link JsonAnySetter} method with too few parameters.
     */
    private static class AnySetterTooFewParams extends DerivedFromData {
        @JsonAnySetter
        public void setOverride(String key) {
            // do nothing
        }
    }

    /**
     * Has {@link JsonAnySetter} method with too few parameters.
     */
    private static class AnySetterTooManyParams extends DerivedFromData {
        @JsonAnySetter
        public void setOverride(String key, int value, String anotherValue) {
            // do nothing
        }
    }

    /**
     * Has {@link JsonAnySetter} method whose first argument type is incorrect.
     */
    private static class AnySetterInvalidParam extends DerivedFromData {
        @JsonAnySetter
        public void setOverride(Integer key, String value) {
            // do nothing
        }
    }
}
