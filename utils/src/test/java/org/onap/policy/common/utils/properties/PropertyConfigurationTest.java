/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.properties.exception.PropertyAccessException;
import org.onap.policy.common.utils.properties.exception.PropertyException;
import org.onap.policy.common.utils.properties.exception.PropertyInvalidException;
import org.onap.policy.common.utils.properties.exception.PropertyMissingException;

/**
 * Test class for PropertyConfiguration.
 */
public class PropertyConfigurationTest {

    /**
     * Property used for most of the simple configuration subclasses.
     */
    private static final String THE_VALUE = "the.value";

    /**
     * String property value.
     */
    private static final String STRING_VALUE = "a string";

    /**
     * Default value for string property.
     */
    private static final String STRING_VALUE_DEFAULT = "another string";

    /**
     * Value that cannot be coerced into any other type.
     */
    private static final String INVALID_VALUE = "invalid";

    /**
     * Properties used when invoking constructors.
     */
    private Properties props;

    @Before
    public void setUp() {
        props = new Properties();
    }

    @Test
    public void testPropertyConfiguration() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);

        PlainStringConfig cfg = new PlainStringConfig();
        assertEquals(null, cfg.value);

        cfg.setAllFields(props);
        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testPropertyConfigurationProperties() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        PlainStringConfig cfg = new PlainStringConfig(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testSetAllFields() throws Exception {

        /*
         * Implements an extra interface, just to see that it doesn't cause issues.
         */
        class GrandParentConfig extends PropertyConfiguration implements DoesNothing {

            @Property(name = "grandparent.value")
            protected boolean grandparentValue;

            @SuppressWarnings("unused")
            public void setGrandparentValue(boolean grandparentValue) {
                this.grandparentValue = grandparentValue;
            }
        }

        /*
         * Implements the extra interface, too.
         */
        class ParentConfig extends GrandParentConfig implements DoesNothing {

            @Property(name = "parent.value")
            protected long parentValue;

            @SuppressWarnings("unused")
            public void setParentValue(long parentValue) {
                this.parentValue = parentValue;
            }
        }

        class Config extends ParentConfig {

            @Property(name = THE_VALUE)
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }


        final Config cfg = new Config();

        // try one set of values
        props.setProperty(THE_VALUE, STRING_VALUE);
        props.setProperty("parent.value", "50000");
        props.setProperty("grandparent.value", "true");
        cfg.setAllFields(props);

        assertEquals(STRING_VALUE, cfg.value);
        assertEquals(50000L, cfg.parentValue);
        assertEquals(true, cfg.grandparentValue);

        // now a different set of values
        props.setProperty(THE_VALUE, STRING_VALUE + "x");
        props.setProperty("parent.value", "50001");
        props.setProperty("grandparent.value", "false");
        cfg.setAllFields(props);

        assertEquals(STRING_VALUE + "x", cfg.value);
        assertEquals(50001L, cfg.parentValue);
        assertEquals(false, cfg.grandparentValue);
    }

    @Test
    public void testSetAllFields_NoProperties() throws Exception {

        class Config extends PropertyConfiguration {

            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }


        Config cfg = new Config();

        props.setProperty(THE_VALUE, STRING_VALUE);
        cfg.setAllFields(props);

        assertEquals(null, cfg.value);
    }

    @Test
    public void testSetValueFieldProperties_FieldSet() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        PlainStringConfig cfg = new PlainStringConfig(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testSetValueFieldProperties_NoAnnotation() throws PropertyException {
        class Config extends PropertyConfiguration {

            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config(props);

        assertNull(cfg.value);
    }

    @Test(expected = PropertyAccessException.class)
    public void testSetValueFieldProperties_WrongFieldType() throws PropertyException {
        class Config extends PropertyConfiguration {

            // Cannot set a property into an "Exception" field
            @Property(name = THE_VALUE)
            private Exception value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Exception value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        new Config(props);
    }

    @Test(expected = PropertyAccessException.class)
    public void testGetSetter_NoSetter() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        new Config(props);
    }

    @Test(expected = PropertyMissingException.class)
    public void testSetValueMethodFieldPropertiesProperty_NoProperty_NoDefault() throws PropertyException {
        new PlainStringConfig(props);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testSetValueMethodFieldPropertiesProperty_InvalidValue() throws PropertyException {
        class Config extends PlainPrimIntConfig {

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            /**
             * This returns a boolean, but the field is an "int", so it should throw an
             * exception when it tries to stuff the value into the field.
             */
            @Override
            protected Object getValue(Field field, Properties props, Property prop) throws PropertyException {
                return Boolean.TRUE;
            }
        }

        new Config(props);
    }

    @Test(expected = PropertyAccessException.class)
    public void testSetValueMethodFieldPropertiesProperty_MethodEx() throws PropertyException {
        class Config extends PlainStringConfig {

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @Override
            public void setValue(String value) {
                throw new IllegalArgumentException("expected exception");
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        new Config(props);
    }

    @Test
    public void testGetValue() throws PropertyException {
        // this class contains all of the supported field types
        class Config extends PropertyConfiguration {

            @Property(name = "string")
            private String stringValue;

            @Property(name = "boolean.true")
            private Boolean boolTrueValue;

            @Property(name = "boolean.false")
            private Boolean boolFalseValue;

            @Property(name = "primitive.boolean.true")
            private boolean primBoolTrueValue;

            @Property(name = "primitive.boolean.false")
            private boolean primBoolFalseValue;

            @Property(name = "integer")
            private Integer intValue;

            @Property(name = "primitive.integer")
            private int primIntValue;

            @Property(name = "long")
            private Long longValue;

            @Property(name = "primitive.long")
            private long primLongValue;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public String getStringValue() {
                return stringValue;
            }

            @SuppressWarnings("unused")
            public void setStringValue(String stringValue) {
                this.stringValue = stringValue;
            }

            @SuppressWarnings("unused")
            public Boolean getBoolTrueValue() {
                return boolTrueValue;
            }

            @SuppressWarnings("unused")
            public void setBoolTrueValue(Boolean boolTrueValue) {
                this.boolTrueValue = boolTrueValue;
            }

            @SuppressWarnings("unused")
            public Boolean getBoolFalseValue() {
                return boolFalseValue;
            }

            @SuppressWarnings("unused")
            public void setBoolFalseValue(Boolean boolFalseValue) {
                this.boolFalseValue = boolFalseValue;
            }

            @SuppressWarnings("unused")
            public boolean isPrimBoolTrueValue() {
                return primBoolTrueValue;
            }

            @SuppressWarnings("unused")
            public void setPrimBoolTrueValue(boolean primBoolTrueValue) {
                this.primBoolTrueValue = primBoolTrueValue;
            }

            @SuppressWarnings("unused")
            public boolean isPrimBoolFalseValue() {
                return primBoolFalseValue;
            }

            @SuppressWarnings("unused")
            public void setPrimBoolFalseValue(boolean primBoolFalseValue) {
                this.primBoolFalseValue = primBoolFalseValue;
            }

            @SuppressWarnings("unused")
            public Integer getIntValue() {
                return intValue;
            }

            @SuppressWarnings("unused")
            public void setIntValue(Integer intValue) {
                this.intValue = intValue;
            }

            @SuppressWarnings("unused")
            public int getPrimIntValue() {
                return primIntValue;
            }

            @SuppressWarnings("unused")
            public void setPrimIntValue(int primIntValue) {
                this.primIntValue = primIntValue;
            }

            @SuppressWarnings("unused")
            public Long getLongValue() {
                return longValue;
            }

            @SuppressWarnings("unused")
            public void setLongValue(Long longValue) {
                this.longValue = longValue;
            }

            @SuppressWarnings("unused")
            public long getPrimLongValue() {
                return primLongValue;
            }

            @SuppressWarnings("unused")
            public void setPrimLongValue(long primLongValue) {
                this.primLongValue = primLongValue;
            }
        }

        props.setProperty("string", "a string");
        props.setProperty("boolean.true", "true");
        props.setProperty("boolean.false", "false");
        props.setProperty("primitive.boolean.true", "true");
        props.setProperty("primitive.boolean.false", "false");
        props.setProperty("integer", "100");
        props.setProperty("primitive.integer", "101");
        props.setProperty("long", "10000");
        props.setProperty("primitive.long", "10001");

        Config cfg = new Config(props);

        assertEquals("a string", cfg.stringValue);
        assertEquals(true, cfg.boolTrueValue);
        assertEquals(false, cfg.boolFalseValue);
        assertEquals(true, cfg.primBoolTrueValue);
        assertEquals(false, cfg.primBoolFalseValue);
        assertEquals(100, cfg.intValue.intValue());
        assertEquals(101, cfg.primIntValue);
        assertEquals(10000, cfg.longValue.longValue());
        assertEquals(10001, cfg.primLongValue);
    }

    @Test(expected = PropertyAccessException.class)
    public void testGetValue_UnsupportedType() throws PropertyException {
        class Config extends PropertyConfiguration {

            // Cannot set a property into an "Exception" field
            @Property(name = THE_VALUE)
            private Exception value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Exception value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        new Config(props);
    }

    @Test
    public void testCheckModifiable_OtherModifiers() throws PropertyException {
        // this class contains all of the supported field types
        class Config extends PropertyConfiguration {

            @Property(name = "public")
            public String publicString;

            @Property(name = "private")
            private String privateString;

            @Property(name = "protected")
            protected String protectedString;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setPublicString(String publicString) {
                this.publicString = publicString;
            }

            @SuppressWarnings("unused")
            public void setPrivateString(String privateString) {
                this.privateString = privateString;
            }

            @SuppressWarnings("unused")
            public void setProtectedString(String protectedString) {
                this.protectedString = protectedString;
            }
        }

        props.setProperty("public", "a public string");
        props.setProperty("private", "a private string");
        props.setProperty("protected", "a protected string");

        Config cfg = new Config(props);

        assertEquals("a public string", cfg.publicString);
        assertEquals("a private string", cfg.privateString);
        assertEquals("a protected string", cfg.protectedString);
    }

    @Test(expected = PropertyAccessException.class)
    public void testCheckModifiable_Static() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        new StaticPropConfig(props);
    }

    @Test(expected = PropertyAccessException.class)
    public void testCheckModifiable_Final() throws PropertyException {
        class Config extends PropertyConfiguration {

            // Cannot set a property into an "final" field
            @Property(name = THE_VALUE)
            private final String value = "";

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        new Config(props);
    }

    @Test(expected = PropertyAccessException.class)
    public void testCheckSetter_Static() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        new StaticMethodConfig(props);
    }

    @Test
    public void testGetStringValue() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        PlainStringConfig cfg = new PlainStringConfig(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testGetBooleanValue_NoDefault() throws PropertyException {
        props.setProperty(THE_VALUE, "true");
        PlainBooleanConfig cfg = new PlainBooleanConfig(props);

        assertEquals(true, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testGetBooleanValue_InvalidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private Boolean value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Boolean value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "true");
        new Config(props);
    }

    @Test
    public void testGetBooleanValue_ValidDefault_True() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "true")
            private Boolean value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Boolean value) {
                this.value = value;
            }
        }

        // property not defined
        Config cfg = new Config(props);
        assertEquals(true, cfg.value);

        // try again, with the property defined as true
        props.setProperty(THE_VALUE, "true");
        cfg = new Config(props);
        assertEquals(true, cfg.value);

        // try again, with the property defined as false
        props.setProperty(THE_VALUE, "false");
        cfg = new Config(props);
        assertEquals(false, cfg.value);
    }

    @Test
    public void testGetBooleanValue_ValidDefault_False() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "false")
            private Boolean value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Boolean value) {
                this.value = value;
            }
        }

        // property not defined
        Config cfg = new Config(props);
        assertEquals(false, cfg.value);

        // try again, with the property defined as true
        props.setProperty(THE_VALUE, "true");
        cfg = new Config(props);
        assertEquals(true, cfg.value);

        // try again, with the property defined as false
        props.setProperty(THE_VALUE, "false");
        cfg = new Config(props);
        assertEquals(false, cfg.value);
    }

    @Test
    public void testGetIntegerValue_NoDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private Integer value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Integer value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "200");
        Config cfg = new Config(props);

        assertEquals(200, cfg.value.intValue());
    }

    @Test(expected = PropertyInvalidException.class)
    public void testGetIntegerValue_InvalidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private Integer value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Integer value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "200");
        new Config(props);
    }

    @Test
    public void testGetIntegerValue_ValidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "201")
            private Integer value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Integer value) {
                this.value = value;
            }
        }

        // property not defined
        Config cfg = new Config(props);
        assertEquals(201, cfg.value.intValue());

        // try again, with the property defined
        props.setProperty(THE_VALUE, "200");
        cfg = new Config(props);
        assertEquals(200, cfg.value.intValue());
    }

    @Test
    public void testGetLongValue_NoDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private Long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Long value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "20000");
        Config cfg = new Config(props);

        assertEquals(20000L, cfg.value.longValue());
    }

    @Test(expected = PropertyInvalidException.class)
    public void testGetLongValue_InvalidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private Long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Long value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "20000");
        new Config(props);
    }

    @Test
    public void testGetLongValue_ValidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "20001")
            private Long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(Long value) {
                this.value = value;
            }
        }

        // property not defined
        Config cfg = new Config(props);
        assertEquals(20001L, cfg.value.longValue());

        // try again, with the property defined
        props.setProperty(THE_VALUE, "20000");
        cfg = new Config(props);
        assertEquals(20000L, cfg.value.longValue());
    }

    @Test
    public void testGetPropValue_Prop_NoDefault() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        PlainStringConfig cfg = new PlainStringConfig(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testGetPropValue_Prop_Default() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = STRING_VALUE_DEFAULT)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testGetPropValue_EmptyProp_EmptyOk() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, accept = "empty")
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "");
        Config cfg = new Config(props);

        assertEquals("", cfg.value);
    }

    @Test
    public void testGetPropValue_EmptyDefault_EmptyOk() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "", accept = "empty")
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        Config cfg = new Config(props);

        assertEquals("", cfg.value);
    }

    @Test
    public void testGetPropValue_Default_EmptyOk() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = STRING_VALUE, accept = "empty")
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test(expected = PropertyMissingException.class)
    public void testGetPropValue_EmptyDefault_EmptyNotOk() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "")
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        new Config(props);
    }

    @Test
    public void testGetPropValue_Default_EmptyNotOk() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = STRING_VALUE, accept = "")
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testGetRawPropertyValue() throws PropertyException {
        class Config extends PlainStringConfig {

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @Override
            protected String getRawPropertyValue(Properties props, String propnm) {
                return STRING_VALUE;
            }
        }

        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.getValue());

    }

    @Test
    public void testMakeBoolean_True() throws PropertyException {
        props.setProperty(THE_VALUE, "true");
        PlainBooleanConfig cfg = new PlainBooleanConfig(props);

        assertEquals(true, cfg.value);
    }

    @Test
    public void testMakeBoolean_False() throws PropertyException {
        props.setProperty(THE_VALUE, "false");
        PlainBooleanConfig cfg = new PlainBooleanConfig(props);

        assertEquals(false, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testMakeBoolean_Invalid() throws PropertyException {
        props.setProperty(THE_VALUE, INVALID_VALUE);
        new PlainBooleanConfig(props);
    }

    @Test
    public void testMakeInteger_Valid() throws PropertyException {
        props.setProperty(THE_VALUE, "300");
        PlainPrimIntConfig cfg = new PlainPrimIntConfig(props);

        assertEquals(300, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testMakeInteger_Invalid() throws PropertyException {
        props.setProperty(THE_VALUE, INVALID_VALUE);
        new PlainPrimIntConfig(props);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testMakeInteger_TooBig() throws PropertyException {
        props.setProperty(THE_VALUE, String.valueOf(Integer.MAX_VALUE + 10L));
        new PlainPrimIntConfig(props);
    }

    @Test
    public void testMakeLong_Valid() throws PropertyException {
        props.setProperty(THE_VALUE, "30000");
        PlainPrimLongConfig cfg = new PlainPrimLongConfig(props);

        assertEquals(30000L, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testMakeLong_Invalid() throws PropertyException {
        props.setProperty(THE_VALUE, INVALID_VALUE);
        new PlainPrimLongConfig(props);
    }

    @Test
    public void testCheckDefaultValue_NotEmpty_Valid() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "700")
            private long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        Config cfg = new Config(props);

        assertEquals(700L, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testCheckDefaultValue_NotEmpty_Invalid() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        new Config(props);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testCheckDefaultValue_Empty_EmptyOk_Invalid() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "", accept = "empty")
            private long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        new Config(props);
    }

    @Test
    public void testIsEmptyOkPropertyString_True() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "", accept = "empty")
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        // missing property - should default to ""
        Config cfg = new Config(props);
        assertEquals("", cfg.value);

        // add an empty property - should take the property's value
        props.setProperty(THE_VALUE, "");
        cfg.setAllFields(props);
        assertEquals("", cfg.value);
        
        // add the property - should take the property's value
        props.setProperty(THE_VALUE, STRING_VALUE);
        cfg.setAllFields(props);
        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test(expected = PropertyMissingException.class)
    public void testIsEmptyOkPropertyString_False() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "", accept = "")
            private long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        new Config(props);
    }

    @Test
    public void testIsEmptyOkProperty_True() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "", accept = "empty")
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        Config cfg = new Config(props);

        assertEquals("", cfg.value);
    }

    @Test(expected = PropertyMissingException.class)
    public void testIsEmptyOkProperty_False() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue = "", accept = "")
            private long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        new Config(props);
    }

    /**
     * Config with a String value having no qualifiers.
     */
    public class PlainStringConfig extends PropertyConfiguration {

        @Property(name = THE_VALUE)
        private String value;
        
        public PlainStringConfig() {
            
        }
        
        public PlainStringConfig(Properties props) throws PropertyException {
            super(props);
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * Config with a Boolean value having no qualifiers.
     */
    public class PlainBooleanConfig extends PropertyConfiguration {

        @Property(name = THE_VALUE)
        private Boolean value;
        
        public PlainBooleanConfig(Properties props) throws PropertyException {
            super(props);
        }

        public void setValue(Boolean value) {
            this.value = value;
        }
    }

    /**
     * Config with an int value having no qualifiers.
     */
    public class PlainPrimIntConfig extends PropertyConfiguration {

        @Property(name = THE_VALUE)
        private int value;

        public PlainPrimIntConfig(Properties props) throws PropertyException {
            super(props);
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    /**
     * Config with a long value having no qualifiers.
     */
    public class PlainPrimLongConfig extends PropertyConfiguration {

        @Property(name = THE_VALUE)
        private long value;

        public PlainPrimLongConfig(Properties props) throws PropertyException {
            super(props);
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    /**
     * A config whose field is "static".
     */
    public static class StaticPropConfig extends PropertyConfiguration {

        // "static" field cannot be set
        @Property(name = THE_VALUE)
        private static String value;

        public StaticPropConfig(Properties props) throws PropertyException {
            super(props);
        }

        public static void setValue(String value) {
            StaticPropConfig.value = value;
        }
    }

    /**
     * A config whose method is "static".
     */
    public static class StaticMethodConfig extends PropertyConfiguration {

        // "static" field cannot be set
        @Property(name = THE_VALUE)
        private String value;

        public StaticMethodConfig(Properties props) throws PropertyException {
            super(props);
        }

        public static void setValue(String value) {
            
        }
    }

    /**
     * This is just used as a mix-in to ensure that the configuration ignores interfaces.
     */
    public static interface DoesNothing {

    }
}
