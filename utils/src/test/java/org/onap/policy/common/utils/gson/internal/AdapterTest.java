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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;
import org.onap.policy.common.utils.gson.JacksonExclusionStrategy;
import org.onap.policy.common.utils.gson.internal.DataAdapterFactory.Data;
import org.onap.policy.common.utils.gson.internal.DataAdapterFactory.DerivedData;

public class AdapterTest {
    private static final String GET_VALUE_NAME = "getValue";
    private static final String VALUE_NAME = "value";
    private static final String MY_NAME = AdapterTest.class.getName();

    private static DataAdapterFactory dataAdapter = new DataAdapterFactory();

    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(dataAdapter)
                    .setExclusionStrategies(new JacksonExclusionStrategy()).create();

    /*
     * The remaining fields are just used within the tests.
     */

    private String value;

    // empty alias - should use field name
    @JsonProperty("")
    protected String emptyAlias;

    @JsonProperty("name-with-alias")
    protected String nameWithAlias;

    protected String unaliased;

    protected String $invalidFieldName;

    private List<Data> listField;

    private Data dataField;


    @Test
    public void testIsManagedField() {
        assertTrue(Adapter.isManaged(field(VALUE_NAME)));

        assertFalse(Adapter.isManaged(field("$invalidFieldName")));
    }

    @Test
    public void testIsManagedMethod() {
        assertTrue(Adapter.isManaged(mget(GET_VALUE_NAME)));

        assertFalse(Adapter.isManaged(mget("get$InvalidName")));
        assertFalse(Adapter.isManaged(mset("set$InvalidName")));
    }

    @Test
    public void testAdapterField_Converter() {
        Adapter adapter = new Adapter(gson, field("dataField"));

        // first, write something of type Data
        dataAdapter.reset();
        dataField = new Data(300);
        JsonElement tree = adapter.toJsonTree(dataField);
        assertEquals("{'id':300}".replace('\'', '"'), tree.toString());

        // now try a subclass
        dataAdapter.reset();
        dataField = new DerivedData(300, "three");
        tree = adapter.toJsonTree(dataField);
        assertEquals("{'id':300,'text':'three'}".replace('\'', '"'), tree.toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAdapterField_Converter_List() {
        listField = DataAdapterFactory.makeList();

        Adapter adapter = new Adapter(gson, field("listField"));

        dataAdapter.reset();
        JsonElement tree = adapter.toJsonTree(listField);
        assertTrue(dataAdapter.isDataWritten());
        assertEquals(DataAdapterFactory.ENCODED_LIST, tree.toString());

        // encode it twice so it uses the cached converter
        dataAdapter.reset();
        tree = adapter.toJsonTree(listField);
        assertTrue(dataAdapter.isDataWritten());
        assertEquals(DataAdapterFactory.ENCODED_LIST, tree.toString());

        dataAdapter.reset();
        List<Data> lst2 = (List<Data>) adapter.fromJsonTree(tree);
        assertTrue(dataAdapter.isDataRead());

        assertEquals(listField.toString(), lst2.toString());

        // decode it twice so it uses the cached converter
        dataAdapter.reset();
        lst2 = (List<Data>) adapter.fromJsonTree(tree);
        assertTrue(dataAdapter.isDataRead());

        assertEquals(listField.toString(), lst2.toString());
    }

    @Test
    public void testAdapterMethod_Converter() throws Exception {
        listField = DataAdapterFactory.makeList();

        Method getter = mget("getMyList");

        Adapter aget = new Adapter(gson, getter, true, getter.getReturnType());

        dataAdapter.reset();
        JsonElement tree = aget.toJsonTree(listField);
        assertTrue(dataAdapter.isDataWritten());
        assertEquals(DataAdapterFactory.ENCODED_LIST, tree.toString());

        Method setter = AdapterTest.class.getDeclaredMethod("setMyList", List.class);
        Adapter aset = new Adapter(gson, setter, true, setter.getGenericParameterTypes()[0]);

        dataAdapter.reset();
        @SuppressWarnings("unchecked")
        List<Data> lst2 = (List<Data>) aset.fromJsonTree(tree);
        assertTrue(dataAdapter.isDataRead());

        assertEquals(listField.toString(), lst2.toString());
    }

    @Test
    public void testGetPropName_testGetFullName_testMakeError() {
        // test field
        Adapter adapter = new Adapter(gson, field(VALUE_NAME));

        assertEquals(VALUE_NAME, adapter.getPropName());
        assertEquals(MY_NAME + ".value", adapter.getFullName());


        // test getter
        adapter = new Adapter(gson, mget(GET_VALUE_NAME), true, String.class);

        assertEquals(VALUE_NAME, adapter.getPropName());
        assertEquals(MY_NAME + ".getValue", adapter.getFullName());

        assertEquals("hello: " + MY_NAME + ".getValue", adapter.makeError("hello: "));


        // test setter
        adapter = new Adapter(gson, mset("setValue"), false, String.class);

        assertEquals(VALUE_NAME, adapter.getPropName());
        assertEquals(MY_NAME + ".setValue", adapter.getFullName());
    }

    @Test
    public void testToJsonTree() {
        Adapter adapter = new Adapter(gson, field(VALUE_NAME));

        JsonElement tree = adapter.toJsonTree("hello");
        assertTrue(tree.isJsonPrimitive());
        assertEquals("hello", tree.getAsString());
    }

    @Test
    public void testFromJsonTree() {
        Adapter adapter = new Adapter(gson, field(VALUE_NAME));

        assertEquals("world", adapter.fromJsonTree(new JsonPrimitive("world")));
    }

    @Test
    public void testDetmPropName() {
        assertEquals("emptyAlias", Adapter.detmPropName(field("emptyAlias")));
        assertEquals("name-with-alias", Adapter.detmPropName(field("nameWithAlias")));
        assertEquals("unaliased", Adapter.detmPropName(field("unaliased")));
        assertEquals(null, Adapter.detmPropName(field("$invalidFieldName")));
    }

    @Test
    public void testDetmGetterPropName() {
        assertEquals("emptyAlias", Adapter.detmGetterPropName(mget("getEmptyAlias")));
        assertEquals("get-with-alias", Adapter.detmGetterPropName(mget("getWithAlias")));
        assertEquals("plain", Adapter.detmGetterPropName(mget("getPlain")));
        assertEquals("primBool", Adapter.detmGetterPropName(mget("isPrimBool")));
        assertEquals("boxedBool", Adapter.detmGetterPropName(mget("isBoxedBool")));
        assertEquals(null, Adapter.detmGetterPropName(mget("isString")));
        assertEquals(null, Adapter.detmGetterPropName(mget("noGet")));
        assertEquals(null, Adapter.detmGetterPropName(mget("get")));
        assertEquals(null, Adapter.detmGetterPropName(mget("get$InvalidName")));
    }

    @Test
    public void testDetmSetterPropName() {
        assertEquals("emptyAlias", Adapter.detmSetterPropName(mset("setEmptyAlias")));
        assertEquals("set-with-alias", Adapter.detmSetterPropName(mset("setWithAlias")));
        assertEquals("plain", Adapter.detmSetterPropName(mset("setPlain")));
        assertEquals(null, Adapter.detmSetterPropName(mset("noSet")));
        assertEquals(null, Adapter.detmSetterPropName(mset("set")));
        assertEquals(null, Adapter.detmSetterPropName(mset("set$InvalidName")));
    }

    @Test
    public void testGetQualifiedNameField() throws Exception {
        assertEquals(MY_NAME + ".value", Adapter.getQualifiedName(AdapterTest.class.getDeclaredField(VALUE_NAME)));
    }

    @Test
    public void testGetQualifiedNameMethod() throws Exception {
        assertEquals(MY_NAME + ".getValue", Adapter.getQualifiedName(mget(GET_VALUE_NAME)));
    }

    /**
     * Gets a field from this class, by name.
     *
     * @param name name of the field to get
     * @return the field
     */
    private Field field(String name) {
        try {
            return AdapterTest.class.getDeclaredField(name);

        } catch (SecurityException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a "getter" method from this class, by name.
     *
     * @param name name of the method to get
     * @return the method
     */
    private Method mget(String name) {
        try {
            return AdapterTest.class.getDeclaredMethod(name);

        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a "setter" method from this class, by name.
     *
     * @param name name of the method to get
     * @return the method
     */
    private Method mset(String name) {
        try {
            return AdapterTest.class.getDeclaredMethod(name, String.class);

        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * The remaining methods are just used within the tests.
     */

    protected String getValue() {
        return value;
    }

    // empty alias - should use method name
    @JsonProperty("")
    protected String getEmptyAlias() {
        return "";
    }

    @JsonProperty("get-with-alias")
    protected String getWithAlias() {
        return "";
    }

    // no alias, begins with "get"
    protected String getPlain() {
        return "";
    }

    // begins with "is", returns primitive boolean
    protected boolean isPrimBool() {
        return true;
    }

    // begins with "is", returns boxed Boolean
    protected Boolean isBoxedBool() {
        return true;
    }

    // begins with "is", but doesn't return a boolean
    protected String isString() {
        return "";
    }

    // doesn't begin with "get"
    protected String noGet() {
        return "";
    }

    // nothing after "get"
    protected String get() {
        return "";
    }

    // name has a bogus character
    protected String get$InvalidName() {
        return "";
    }


    protected void setValue(String text) {
        // do nothing
    }

    // empty alias - should use method name
    @JsonProperty("")
    protected void setEmptyAlias(String text) {
        // do nothing
    }

    @JsonProperty("set-with-alias")
    protected void setWithAlias(String text) {
        // do nothing
    }

    // no alias, begins with "set"
    protected void setPlain(String text) {
        // do nothing
    }

    // doesn't begin with "set"
    protected void noSet(String text) {
        // do nothing
    }

    // nothing after "get"
    protected void set(String text) {
        // do nothing
    }

    // name has a bogus character
    protected void set$InvalidName(String text) {
        // do nothing
    }

    // returns a list
    protected List<Data> getMyList() {
        return listField;
    }

    // accepts a list
    protected void setMyList(List<Data> newList) {
        listField = newList;
    }
}
