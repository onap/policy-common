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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
public class BeanConfiguratorTest {

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

    private BeanConfigurator beancfg;

    @Before
    public void setUp() {
        props = new Properties();
        beancfg = new BeanConfigurator();
    }

    @Test
    public void testBeanConfigurator() {
        // verify that constructor does not throw an exception
        new BeanConfigurator();
    }

    @Test
    public void testConfigureFromProperties() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        PlainStringConfig cfg = new PlainStringConfig();

        assertSame(cfg, beancfg.configureFromProperties(cfg, props));

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testSetAllFields() throws Exception {

        /*
         * Implements an extra interface, just to see that it doesn't cause issues.
         */
        class GrandParentConfig implements DoesNothing {

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
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE, cfg.value);
        assertEquals(50000L, cfg.parentValue);
        assertEquals(true, cfg.grandparentValue);

        // now a different set of values
        props.setProperty(THE_VALUE, STRING_VALUE + "x");
        props.setProperty("parent.value", "50001");
        props.setProperty("grandparent.value", "false");
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE + "x", cfg.value);
        assertEquals(50001L, cfg.parentValue);
        assertEquals(false, cfg.grandparentValue);
    }

    @Test
    public void testSetAllFields_NoProperties() throws Exception {

        class Config {

            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }


        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(null, cfg.value);
    }

    @Test
    public void testSetValueObjectFieldProperties_FieldSet() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        PlainStringConfig cfg = new PlainStringConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testSetValueObjectFieldProperties_NoAnnotation() throws PropertyException {
        class Config {

            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertNull(cfg.value);
    }

    @Test(expected = PropertyAccessException.class)
    public void testSetValueObjectFieldProperties_WrongFieldType() throws PropertyException {
        class Config {

            // Cannot set a property into an "Exception" field
            @Property(name = THE_VALUE)
            private Exception value;

            @SuppressWarnings("unused")
            public void setValue(Exception value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        beancfg.configureFromProperties(new Config(), props);
    }

    @Test(expected = PropertyAccessException.class)
    public void testGetSetter_NoSetter() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE)
            private String value;
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        beancfg.configureFromProperties(new Config(), props);
    }

    @Test(expected = PropertyMissingException.class)
    public void testSetValueObjectMethodFieldPropertiesProperty_NoProperty_NoDefault() throws PropertyException {
        beancfg.configureFromProperties(new PlainStringConfig(), props);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testSetValueObjectMethodFieldPropertiesProperty_IllegalArgEx() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);

        beancfg = new BeanConfigurator() {
            @Override
            protected Object getValue(Field field, Properties props, Property prop) throws PropertyException {
                throw new IllegalArgumentException("expected exception");
            }
        };

        beancfg.configureFromProperties(new PlainStringConfig(), props);
    }

    @Test(expected = PropertyAccessException.class)
    public void testSetValueObjectMethodFieldPropertiesProperty_MethodEx() throws PropertyException {
        class Config extends PlainStringConfig {

            @Override
            public void setValue(String value) {
                throw new IllegalArgumentException("expected exception");
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        beancfg.configureFromProperties(new Config(), props);
    }

    @Test
    public void testGetValue() throws PropertyException {
        // this class contains all of the supported field types
        class Config {

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

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

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
        class Config {

            // Cannot set a property into an "Exception" field
            @Property(name = THE_VALUE)
            private Exception value;

            @SuppressWarnings("unused")
            public void setValue(Exception value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        beancfg.configureFromProperties(new Config(), props);
    }

    @Test
    public void testCheckModifiable_OtherModifiers() throws PropertyException {
        // this class contains all of the supported field types
        class Config {

            @Property(name = "public")
            public String publicString;

            @Property(name = "private")
            private String privateString;

            @Property(name = "protected")
            protected String protectedString;

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

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals("a public string", cfg.publicString);
        assertEquals("a private string", cfg.privateString);
        assertEquals("a protected string", cfg.protectedString);
    }

    @Test(expected = PropertyAccessException.class)
    public void testCheckModifiable_Static() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        beancfg.configureFromProperties(new StaticPropConfig(), props);
    }

    @Test(expected = PropertyAccessException.class)
    public void testCheckModifiable_Final() throws PropertyException {
        class Config {

            // Cannot set a property into an "final" field
            @Property(name = THE_VALUE)
            private final String value = "";
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        beancfg.configureFromProperties(new Config(), props);
    }

    @Test(expected = PropertyAccessException.class)
    public void testCheckMethod_Static() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        beancfg.configureFromProperties(new StaticMethodConfig(), props);
    }

    @Test
    public void testGetStringValue() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        PlainStringConfig cfg = new PlainStringConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testGetBooleanValue_NoDefault() throws PropertyException {
        props.setProperty(THE_VALUE, "true");
        PlainBooleanConfig cfg = new PlainBooleanConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(true, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testGetBooleanValue_InvalidDefault() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private Boolean value;

            @SuppressWarnings("unused")
            public void setValue(Boolean value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "true");
        beancfg.configureFromProperties(new Config(), props);
    }

    @Test
    public void testGetBooleanValue_ValidDefault_True() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "true")
            private Boolean value;

            @SuppressWarnings("unused")
            public void setValue(Boolean value) {
                this.value = value;
            }
        }

        // property not defined
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(true, cfg.value);

        // try again, with the property defined as true
        props.setProperty(THE_VALUE, "true");
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(true, cfg.value);

        // try again, with the property defined as false
        props.setProperty(THE_VALUE, "false");
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(false, cfg.value);
    }

    @Test
    public void testGetBooleanValue_ValidDefault_False() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "false")
            private Boolean value;

            @SuppressWarnings("unused")
            public void setValue(Boolean value) {
                this.value = value;
            }
        }

        // property not defined
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(false, cfg.value);

        // try again, with the property defined as true
        props.setProperty(THE_VALUE, "true");
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(true, cfg.value);

        // try again, with the property defined as false
        props.setProperty(THE_VALUE, "false");
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(false, cfg.value);
    }

    @Test
    public void testGetIntegerValue_NoDefault() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE)
            private Integer value;

            @SuppressWarnings("unused")
            public void setValue(Integer value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "200");
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(200, cfg.value.intValue());
    }

    @Test(expected = PropertyInvalidException.class)
    public void testGetIntegerValue_InvalidDefault() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private Integer value;

            @SuppressWarnings("unused")
            public void setValue(Integer value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "200");
        beancfg.configureFromProperties(new Config(), props);
    }

    @Test
    public void testGetIntegerValue_ValidDefault() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "201")
            private Integer value;

            @SuppressWarnings("unused")
            public void setValue(Integer value) {
                this.value = value;
            }
        }

        // property not defined
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(201, cfg.value.intValue());

        // try again, with the property defined
        props.setProperty(THE_VALUE, "200");
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(200, cfg.value.intValue());
    }

    @Test
    public void testGetLongValue_NoDefault() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE)
            private Long value;

            @SuppressWarnings("unused")
            public void setValue(Long value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "20000");
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(20000L, cfg.value.longValue());
    }

    @Test(expected = PropertyInvalidException.class)
    public void testGetLongValue_InvalidDefault() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private Long value;

            @SuppressWarnings("unused")
            public void setValue(Long value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "20000");
        beancfg.configureFromProperties(new Config(), props);
    }

    @Test
    public void testGetLongValue_ValidDefault() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "20001")
            private Long value;

            @SuppressWarnings("unused")
            public void setValue(Long value) {
                this.value = value;
            }
        }

        // property not defined
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(20001L, cfg.value.longValue());

        // try again, with the property defined
        props.setProperty(THE_VALUE, "20000");
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(20000L, cfg.value.longValue());
    }

    @Test
    public void testGetPropValue_Prop_NoDefault() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);

        PlainStringConfig cfg = new PlainStringConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testGetPropValue_Prop_Default() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = STRING_VALUE_DEFAULT)
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testGetPropValue_EmptyProp_EmptyOk() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, accept = "empty")
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "");

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals("", cfg.value);
    }

    @Test
    public void testGetPropValue_NullProp_EmptyOk() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, accept = "empty")
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals("", cfg.value);
    }

    @Test
    public void testGetPropValue_EmptyDefault_EmptyOk() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "", accept = "empty")
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals("", cfg.value);
    }

    @Test
    public void testGetPropValue_Default_EmptyOk() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = STRING_VALUE, accept = "empty")
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test(expected = PropertyMissingException.class)
    public void testGetPropValue_EmptyDefault_EmptyNotOk() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "")
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        beancfg.configureFromProperties(new Config(), props);
    }

    @Test
    public void testGetPropValue_Default_EmptyNotOk() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = STRING_VALUE, accept = "")
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    public void testMakeBoolean_True() throws PropertyException {
        props.setProperty(THE_VALUE, "true");
        PlainBooleanConfig cfg = new PlainBooleanConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(true, cfg.value);
    }

    @Test
    public void testMakeBoolean_False() throws PropertyException {
        props.setProperty(THE_VALUE, "false");
        PlainBooleanConfig cfg = new PlainBooleanConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(false, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testMakeBoolean_Invalid() throws PropertyException {
        props.setProperty(THE_VALUE, INVALID_VALUE);
        beancfg.configureFromProperties(new PlainBooleanConfig(), props);
    }

    @Test
    public void testMakeInteger_Valid() throws PropertyException {
        props.setProperty(THE_VALUE, "300");
        PlainPrimIntConfig cfg = new PlainPrimIntConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(300, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testMakeInteger_Invalid() throws PropertyException {
        props.setProperty(THE_VALUE, INVALID_VALUE);
        beancfg.configureFromProperties(new PlainPrimIntConfig(), props);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testMakeInteger_TooBig() throws PropertyException {
        props.setProperty(THE_VALUE, String.valueOf(Integer.MAX_VALUE + 10L));
        beancfg.configureFromProperties(new PlainPrimIntConfig(), props);
    }

    @Test
    public void testMakeLong_Valid() throws PropertyException {
        props.setProperty(THE_VALUE, "30000");
        PlainPrimLongConfig cfg = new PlainPrimLongConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(30000L, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testMakeLong_Invalid() throws PropertyException {
        props.setProperty(THE_VALUE, INVALID_VALUE);
        beancfg.configureFromProperties(new PlainPrimLongConfig(), props);
    }

    @Test
    public void testCheckDefaultValue_NotEmpty_Valid() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "700")
            private long value;

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(700L, cfg.value);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testCheckDefaultValue_NotEmpty_Invalid() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private long value;

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        beancfg.configureFromProperties(new Config(), props);
    }

    @Test(expected = PropertyInvalidException.class)
    public void testCheckDefaultValue_Empty_EmptyOk_Invalid() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "", accept = "empty")
            private long value;

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        beancfg.configureFromProperties(new Config(), props);
    }

    @Test
    public void testIsEmptyOkPropertyString_True() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "", accept = "empty")
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        // missing property - should default to ""
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals("", cfg.value);

        // add an empty property - should take the property's value
        props.setProperty(THE_VALUE, "");
        beancfg.configureFromProperties(cfg, props);
        assertEquals("", cfg.value);

        // add the property - should take the property's value
        props.setProperty(THE_VALUE, STRING_VALUE);
        beancfg.configureFromProperties(cfg, props);
        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test(expected = PropertyMissingException.class)
    public void testIsEmptyOkPropertyString_False() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "", accept = "")
            private long value;

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        beancfg.configureFromProperties(new Config(), props);
    }

    @Test
    public void testIsEmptyOkProperty_True() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "", accept = "empty")
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals("", cfg.value);
    }

    @Test(expected = PropertyMissingException.class)
    public void testIsEmptyOkProperty_False() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "", accept = "")
            private long value;

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        beancfg.configureFromProperties(new Config(), props);
    }

    @Test
    public void testPutToProperties() throws Exception {

        /*
         * Implements the extra interface, too.
         */
        class ParentConfig implements DoesNothing {

            @Property(name = "the.parent.value")
            protected long parentValue;

            @SuppressWarnings("unused")
            public long getParentValue() {
                return parentValue;
            }
        }

        class Config extends ParentConfig {

            @Property(name = THE_VALUE)
            private String value;

            @Property(name = "the.other.value")
            private String other;

            @SuppressWarnings("unused")
            public String getValue() {
                return value;
            }

            @SuppressWarnings("unused")
            public String getOther() {
                return other;
            }
        }


        final Config cfg = new Config();

        cfg.parentValue = 1010;
        cfg.value = STRING_VALUE;
        cfg.other = "other";

        beancfg.addToProperties(cfg, props, "the", "a");

        assertEquals("1010", props.getProperty("a.parent.value"));
        assertEquals(STRING_VALUE, props.getProperty("a.value"));
        assertEquals("other", props.getProperty("a.other.value"));

        // original prefix is empty
        beancfg.addToProperties(cfg, props, "", "not");
        assertEquals(STRING_VALUE, props.getProperty("not.the.value"));

        // original prefix is ends with "."
        beancfg.addToProperties(cfg, props, "the.", "a");
        assertEquals(STRING_VALUE, props.getProperty("a.value"));

        // new prefix is empty
        beancfg.addToProperties(cfg, props, "", "");
        assertEquals(STRING_VALUE, props.getProperty(THE_VALUE));

        // new prefix is ends with "."
        beancfg.addToProperties(cfg, props, "the", "xxx.");
        assertEquals(STRING_VALUE, props.getProperty("xxx.value"));
    }

    @Test
    public void testPutProperty() throws Exception {

        class Config {
            // no annotation - should not be copied
            private String noAnnotation;

            @Property(name = THE_VALUE)
            private String value;

            // null value - should not be copied
            @Property(name = "the.null.value")
            private String nullValue;

            // should be copied, but retain its prefix
            @Property(name = "some.other.prefix")
            private String other;

            @SuppressWarnings("unused")
            public String getNoAnnotation() {
                return noAnnotation;
            }

            @SuppressWarnings("unused")
            public String getValue() {
                return value;
            }

            @SuppressWarnings("unused")
            public String getNullValue() {
                return nullValue;
            }

            @SuppressWarnings("unused")
            public String getOther() {
                return other;
            }
        }

        Config cfg = new Config();
        cfg.noAnnotation = "no annotation";
        cfg.value = STRING_VALUE;
        cfg.nullValue = null;
        cfg.other = "some other value";
        beancfg.addToProperties(cfg, props, "the", "a");

        assertFalse(props.contains("noAnnotation"));
        assertEquals(STRING_VALUE, props.getProperty("a.value"));
        assertFalse(props.contains("a.null.value"));
        assertEquals("some other value", props.getProperty("some.other.prefix"));
    }

    @Test
    public void testGetGetter() throws Exception {

        class Config {
            // getter method starts with "is" for these
            @Property(name = "plain.bool")
            private Boolean plainBool;

            @Property(name = "prim.bool")
            private boolean primBool;

            // getter method starts with "get" for these
            @Property(name = "plain.bool.get")
            private Boolean plainBoolGet;

            @Property(name = "prim.bool.get")
            private boolean primBoolGet;

            @Property(name = "int")
            private int intValue;

            @Property(name = "string")
            private String stringValue;

            @SuppressWarnings("unused")
            public Boolean isPlainBool() {
                return plainBool;
            }

            @SuppressWarnings("unused")
            public boolean isPrimBool() {
                return primBool;
            }

            @SuppressWarnings("unused")
            public Boolean getPlainBoolGet() {
                return plainBoolGet;
            }

            @SuppressWarnings("unused")
            public boolean getPrimBoolGet() {
                return primBoolGet;
            }

            @SuppressWarnings("unused")
            public int getIntValue() {
                return intValue;
            }

            @SuppressWarnings("unused")
            public String getStringValue() {
                return stringValue;
            }
        }

        Config cfg = new Config();
        cfg.plainBool = true;
        cfg.primBool = false;
        cfg.plainBoolGet = false;
        cfg.primBoolGet = true;
        cfg.intValue = 1100;
        cfg.stringValue = STRING_VALUE;
        beancfg.addToProperties(cfg, props, "", "");

        assertEquals("true", props.getProperty("plain.bool"));
        assertEquals("false", props.getProperty("prim.bool"));
        assertEquals("false", props.getProperty("plain.bool.get"));
        assertEquals("true", props.getProperty("prim.bool.get"));
        assertEquals("1100", props.getProperty("int"));
        assertEquals(STRING_VALUE, props.getProperty("string"));
    }

    @Test(expected = PropertyAccessException.class)
    public void testGetGetter_NoGetter() throws Exception {

        class Config {
            @Property(name = THE_VALUE)
            private String value;
        }

        Config cfg = new Config();
        cfg.value = STRING_VALUE;
        beancfg.addToProperties(cfg, props, "", "");
    }

    @Test(expected = PropertyAccessException.class)
    public void testGetGetter_NoGetterForBoolean() throws Exception {

        class Config {
            @Property(name = THE_VALUE)
            private boolean value;
        }

        Config cfg = new Config();
        cfg.value = true;
        beancfg.addToProperties(cfg, props, "", "");
    }

    @Test(expected = PropertyAccessException.class)
    public void testGetGetter_PrivateGetter() throws Exception {

        class Config {
            @Property(name = THE_VALUE)
            private String value;

            @SuppressWarnings("unused")
            private String getValue() {
                return value;
            }
        }

        Config cfg = new Config();
        cfg.value = STRING_VALUE;
        beancfg.addToProperties(cfg, props, "", "");
    }

    @Test(expected = PropertyAccessException.class)
    public void testGetGetter_SecurityEx() throws Exception {

        class Config {
            @Property(name = THE_VALUE)
            private String value;

            @SuppressWarnings("unused")
            private String getValue() {
                return value;
            }
        }

        Config cfg = new Config();
        cfg.value = STRING_VALUE;

        beancfg = new BeanConfigurator() {
            @Override
            protected Method getGetter(Field field, String methodName) throws SecurityException {
                throw new SecurityException("expected exception");
            }
        };

        beancfg.addToProperties(cfg, props, "", "");
    }

    @Test(expected = PropertyAccessException.class)
    public void testGetBeanValue_Ex() throws Exception {

        class Config {

            @Property(name = THE_VALUE)
            private String value;

            @SuppressWarnings("unused")
            public String getValue() {
                throw new RuntimeException("expected exception");
            }
        }


        final Config cfg = new Config();
        cfg.value = STRING_VALUE;

        beancfg.addToProperties(cfg, props, "the", "a");

    }

    /**
     * Config with a String value having no qualifiers.
     */
    public class PlainStringConfig {

        @Property(name = THE_VALUE)
        private String value;

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
    public class PlainBooleanConfig {

        @Property(name = THE_VALUE)
        private Boolean value;

        public void setValue(Boolean value) {
            this.value = value;
        }
    }

    /**
     * Config with an int value having no qualifiers.
     */
    public class PlainPrimIntConfig {

        @Property(name = THE_VALUE)
        private int value;

        public void setValue(int value) {
            this.value = value;
        }
    }

    /**
     * Config with a long value having no qualifiers.
     */
    public class PlainPrimLongConfig {

        @Property(name = THE_VALUE)
        private long value;

        public void setValue(long value) {
            this.value = value;
        }
    }

    /**
     * A config whose field is "static".
     */
    public static class StaticPropConfig {

        // "static" field cannot be set
        @Property(name = THE_VALUE)
        private static String value;

        public static void setValue(String value) {
            StaticPropConfig.value = value;
        }
    }

    /**
     * A config whose method is "static".
     */
    public static class StaticMethodConfig {

        // "static" field cannot be set
        @Property(name = THE_VALUE)
        private String value;

        public static void setValue(String value) {

        }
    }

    /**
     * This is just used as a mix-in to ensure that the configuration ignores interfaces.
     */
    public static interface DoesNothing {

    }
}
