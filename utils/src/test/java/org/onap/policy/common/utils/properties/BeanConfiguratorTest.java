/*
 * ============LICENSE_START=======================================================
 * ONAP - Common Modules
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

package org.onap.policy.common.utils.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.properties.exception.PropertyAccessException;
import org.onap.policy.common.utils.properties.exception.PropertyException;
import org.onap.policy.common.utils.properties.exception.PropertyInvalidException;
import org.onap.policy.common.utils.properties.exception.PropertyMissingException;

/**
 * Test class for PropertyConfiguration.
 */
class BeanConfiguratorTest {
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String FALSE_STRING = "false";
    private static final String A_VALUE = "a.value";
    private static final String NUMBER_STRING_LONG = "20000";
    private static final String NUMBER_STRING = "200";

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

    @BeforeEach
    public void setUp() {
        props = new Properties();
        beancfg = new BeanConfigurator();
    }

    @Test
    void testConfigureFromProperties() throws PropertyException {
        testStringValueNoDefault();
    }

    private void testStringValueNoDefault() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        PlainStringConfig cfg = new PlainStringConfig();

        assertSame(cfg, beancfg.configureFromProperties(cfg, props));

        assertEquals(STRING_VALUE, cfg.value);
    }

    @Test
    void testSetAllFields() throws Exception {

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
        props.setProperty("grandparent.value", FALSE_STRING);
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE + "x", cfg.value);
        assertEquals(50001L, cfg.parentValue);
        assertEquals(false, cfg.grandparentValue);
    }

    @Test
    void testSetAllFields_NoProperties() throws Exception {

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
    void testSetValueObjectFieldProperties_FieldSet() throws PropertyException {
        testStringValueNoDefault();
    }

    @Test
    void testSetValueObjectFieldProperties_NoAnnotation() throws PropertyException {
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

    @Test
    void testSetValueObjectFieldProperties_WrongFieldType() {
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
        assertThrows(PropertyAccessException.class, () -> beancfg.configureFromProperties(
            new Config(), props));
    }

    @Test
    void testGetSetter_NoSetter() {
        class Config {

            @Property(name = THE_VALUE)
            private String value;
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        assertThrows(PropertyAccessException.class, () -> beancfg.configureFromProperties(
            new Config(), props));
    }

    @Test
    void testSetValueObjectMethodFieldPropertiesProperty_NoProperty_NoDefault() {
        assertThrows(PropertyMissingException.class, () -> beancfg.configureFromProperties(
            new PlainStringConfig(), props));

    }

    @Test
    void testSetValueObjectMethodFieldPropertiesProperty_IllegalArgEx() {
        props.setProperty(THE_VALUE, STRING_VALUE);

        beancfg = new BeanConfigurator() {
            @Override
            protected Object getValue(Field field, Properties props, Property prop) {
                throw new IllegalArgumentException(EXPECTED_EXCEPTION);
            }
        };

        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new PlainStringConfig(), props));
    }

    @Test
    void testSetValueObjectMethodFieldPropertiesProperty_MethodEx() {
        class Config extends PlainStringConfig {

            @Override
            public void setValue(String value) {
                throw new IllegalArgumentException(EXPECTED_EXCEPTION);
            }
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        assertThrows(PropertyAccessException.class, () -> beancfg.configureFromProperties(
            new Config(), props));
    }

    @Test
    void testGetValue() throws PropertyException {
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

        props.setProperty("string", STRING_VALUE);
        props.setProperty("boolean.true", "true");
        props.setProperty("boolean.false", FALSE_STRING);
        props.setProperty("primitive.boolean.true", "true");
        props.setProperty("primitive.boolean.false", FALSE_STRING);
        props.setProperty("integer", "100");
        props.setProperty("primitive.integer", "101");
        props.setProperty("long", "10000");
        props.setProperty("primitive.long", "10001");

        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(STRING_VALUE, cfg.stringValue);
        assertEquals(true, cfg.boolTrueValue);
        assertEquals(false, cfg.boolFalseValue);
        assertEquals(true, cfg.primBoolTrueValue);
        assertEquals(false, cfg.primBoolFalseValue);
        assertEquals(100, cfg.intValue.intValue());
        assertEquals(101, cfg.primIntValue);
        assertEquals(10000, cfg.longValue.longValue());
        assertEquals(10001, cfg.primLongValue);
    }

    @Test
    void testGetValue_UnsupportedType() {
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
        assertThrows(PropertyAccessException.class, () -> beancfg.configureFromProperties(
            new Config(), props));

    }

    @Test
    void testCheckModifiable_OtherModifiers() throws PropertyException {
        // this class contains all the supported field types
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

    @Test
    void testCheckModifiable_Static() {
        props.setProperty(THE_VALUE, STRING_VALUE);
        assertThrows(PropertyAccessException.class, () -> beancfg.configureFromProperties(
            new StaticPropConfig(), props));

    }

    @Test
    void testCheckModifiable_Final() {
        class Config {

            // Cannot set a property into an "final" field
            @Property(name = THE_VALUE)
            private final String value = "";
        }

        props.setProperty(THE_VALUE, STRING_VALUE);
        assertThrows(PropertyAccessException.class, () -> beancfg.configureFromProperties(
            new Config(), props));

    }

    @Test
    void testCheckMethod_Static() {
        props.setProperty(THE_VALUE, STRING_VALUE);
        assertThrows(PropertyAccessException.class, () -> beancfg.configureFromProperties(
            new StaticMethodConfig(), props));

    }

    @Test
    void testGetStringValue() throws PropertyException {
        testStringValueNoDefault();
    }

    @Test
    void testGetBooleanValue_NoDefault() throws PropertyException {
        props.setProperty(THE_VALUE, "true");
        PlainBooleanConfig cfg = new PlainBooleanConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(true, cfg.value);
    }

    @Test
    void testGetBooleanValue_InvalidDefault() {
        class Config {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private Boolean value;

            @SuppressWarnings("unused")
            public void setValue(Boolean value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, "true");
        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new Config(), props));
    }

    @Test
    void testGetBooleanValue_ValidDefault_True() throws PropertyException {
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
        props.setProperty(THE_VALUE, FALSE_STRING);
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(false, cfg.value);
    }

    @Test
    void testGetBooleanValue_ValidDefault_False() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE, defaultValue = FALSE_STRING)
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
        props.setProperty(THE_VALUE, FALSE_STRING);
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(false, cfg.value);
    }

    @Test
    void testGetIntegerValue_NoDefault() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE)
            private Integer value;

            @SuppressWarnings("unused")
            public void setValue(Integer value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, NUMBER_STRING);
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(200, cfg.value.intValue());
    }

    @Test
    void testGetIntegerValue_InvalidDefault() {
        class Config {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private Integer value;

            @SuppressWarnings("unused")
            public void setValue(Integer value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, NUMBER_STRING);
        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new Config(), props));

    }

    @Test
    void testGetIntegerValue_ValidDefault() throws PropertyException {
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
        props.setProperty(THE_VALUE, NUMBER_STRING);
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(200, cfg.value.intValue());
    }

    @Test
    void testGetLongValue_NoDefault() throws PropertyException {
        class Config {

            @Property(name = THE_VALUE)
            private Long value;

            @SuppressWarnings("unused")
            public void setValue(Long value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, NUMBER_STRING_LONG);
        Config cfg = new Config();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(20000L, cfg.value.longValue());
    }

    @Test
    void testGetLongValue_InvalidDefault() {
        class Config {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private Long value;

            @SuppressWarnings("unused")
            public void setValue(Long value) {
                this.value = value;
            }
        }

        props.setProperty(THE_VALUE, NUMBER_STRING_LONG);
        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new Config(), props));

    }

    @Test
    void testGetLongValue_ValidDefault() throws PropertyException {
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
        props.setProperty(THE_VALUE, NUMBER_STRING_LONG);
        cfg = new Config();
        beancfg.configureFromProperties(cfg, props);
        assertEquals(20000L, cfg.value.longValue());
    }

    @Test
    void testGetPropValue_Prop_NoDefault() throws PropertyException {
        testStringValueNoDefault();
    }

    @Test
    void testGetPropValue_Prop_Default() throws PropertyException {
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
    void testGetPropValue_EmptyProp_EmptyOk() throws PropertyException {
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
    void testGetPropValue_NullProp_EmptyOk() throws PropertyException {
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
    void testGetPropValue_EmptyDefault_EmptyOk() throws PropertyException {
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
    void testGetPropValue_Default_EmptyOk() throws PropertyException {
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

    @Test
    void testGetPropValue_EmptyDefault_EmptyNotOk() {
        class Config {

            @Property(name = THE_VALUE, defaultValue = "")
            private String value;

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        }

        assertThrows(PropertyMissingException.class, () -> beancfg.configureFromProperties(
            new Config(), props));
    }

    @Test
    void testGetPropValue_Default_EmptyNotOk() throws PropertyException {
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
    void testMakeBoolean_False() throws PropertyException {
        props.setProperty(THE_VALUE, FALSE_STRING);
        PlainBooleanConfig cfg = new PlainBooleanConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(false, cfg.value);
    }

    @Test
    void testMakeBoolean_Invalid() {
        props.setProperty(THE_VALUE, INVALID_VALUE);

        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new PlainBooleanConfig(), props));
    }

    @Test
    void testMakeInteger_Valid() throws PropertyException {
        props.setProperty(THE_VALUE, "300");
        PlainPrimIntConfig cfg = new PlainPrimIntConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(300, cfg.value);
    }

    @Test
    void testMakeInteger_Invalid() {
        props.setProperty(THE_VALUE, INVALID_VALUE);
        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new PlainPrimIntConfig(), props));

    }

    @Test
    void testMakeInteger_TooBig() {
        props.setProperty(THE_VALUE, String.valueOf(Integer.MAX_VALUE + 10L));
        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new PlainPrimIntConfig(), props));
    }

    @Test
    void testMakeLong_Valid() throws PropertyException {
        props.setProperty(THE_VALUE, "30000");
        PlainPrimLongConfig cfg = new PlainPrimLongConfig();
        beancfg.configureFromProperties(cfg, props);

        assertEquals(30000L, cfg.value);
    }

    @Test
    void testMakeLong_Invalid() {
        props.setProperty(THE_VALUE, INVALID_VALUE);
        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new PlainPrimLongConfig(), props));
    }

    @Test
    void testCheckDefaultValue_NotEmpty_Valid() throws PropertyException {
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

    @Test
    void testCheckDefaultValue_NotEmpty_Invalid() {
        class Config {

            @Property(name = THE_VALUE, defaultValue = INVALID_VALUE)
            private long value;

            @SuppressWarnings("unused")
            public void setValue(long value) {
                this.value = value;
            }
        }

        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new Config(), props));
    }

    @Test
    void testCheckDefaultValue_Empty_EmptyOk_Invalid() {
        assertThrows(PropertyInvalidException.class, () -> beancfg.configureFromProperties(
            new PrimLongDefaultBlankAcceptEmptyConfig(), props));
    }

    @Test
    void testIsEmptyOkPropertyString_True() throws PropertyException {
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

    @Test
    void testIsEmptyOkPropertyString_False() {

        assertThrows(PropertyException.class, () -> beancfg
            .configureFromProperties(new PrimLongDefaultBlankAcceptBlankConfig(), props));
    }

    @Test
    void testIsEmptyOkProperty_True() throws PropertyException {
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
    void testIsEmptyOkProperty_False() {
        assertThrows(PropertyMissingException.class, () -> beancfg
            .configureFromProperties(new PrimLongDefaultBlankAcceptBlankConfig(), props));
    }

    @Test
    void testPutToProperties() throws Exception {

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
        assertEquals(STRING_VALUE, props.getProperty(A_VALUE));
        assertEquals("other", props.getProperty("a.other.value"));

        // original prefix is empty
        beancfg.addToProperties(cfg, props, "", "not");
        assertEquals(STRING_VALUE, props.getProperty("not.the.value"));

        // original prefix is ends with "."
        beancfg.addToProperties(cfg, props, "the.", "a");
        assertEquals(STRING_VALUE, props.getProperty(A_VALUE));

        // new prefix is empty
        beancfg.addToProperties(cfg, props, "", "");
        assertEquals(STRING_VALUE, props.getProperty(THE_VALUE));

        // new prefix is ends with "."
        beancfg.addToProperties(cfg, props, "the", "xxx.");
        assertEquals(STRING_VALUE, props.getProperty("xxx.value"));
    }

    @Test
    void testPutProperty() throws Exception {

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
        assertEquals(STRING_VALUE, props.getProperty(A_VALUE));
        assertFalse(props.contains("a.null.value"));
        assertEquals("some other value", props.getProperty("some.other.prefix"));
    }

    @Test
    void testGetGetter() throws Exception {

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
        assertEquals(FALSE_STRING, props.getProperty("prim.bool"));
        assertEquals(FALSE_STRING, props.getProperty("plain.bool.get"));
        assertEquals("true", props.getProperty("prim.bool.get"));
        assertEquals("1100", props.getProperty("int"));
        assertEquals(STRING_VALUE, props.getProperty("string"));
    }

    @Test
    void testGetGetter_NoGetter() {

        class Config {
            @Property(name = THE_VALUE)
            private String value;
        }

        Config cfg = new Config();
        cfg.value = STRING_VALUE;
        assertThrows(PropertyAccessException.class, () -> beancfg
            .addToProperties(cfg, props, "", ""));
    }

    @Test
    void testGetGetter_NoGetterForBoolean() {

        class Config {
            @Property(name = THE_VALUE)
            private boolean value;
        }

        Config cfg = new Config();
        cfg.value = true;
        assertThrows(PropertyAccessException.class, () -> beancfg
            .addToProperties(cfg, props, "", ""));
    }

    @Test
    void testGetGetter_PrivateGetter() {

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
        assertThrows(PropertyAccessException.class, () -> beancfg
            .addToProperties(cfg, props, "", ""));
    }

    @Test
    void testGetGetter_SecurityEx() {

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
            protected Method getGetter(Field field, String methodName) {
                throw new SecurityException(EXPECTED_EXCEPTION);
            }
        };

        assertThrows(PropertyAccessException.class, () -> beancfg
            .addToProperties(cfg, props, "", ""));
    }

    @Test
    void testGetBeanValue_Ex() {

        class Config {

            @Property(name = THE_VALUE)
            private String value;

            @SuppressWarnings("unused")
            public String getValue() {
                throw new RuntimeException(EXPECTED_EXCEPTION);
            }
        }


        final Config cfg = new Config();
        cfg.value = STRING_VALUE;

        assertThrows(PropertyAccessException.class, () -> beancfg
            .addToProperties(cfg, props, "the", "a"));
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
            // do nothing
        }
    }

    class PrimLongDefaultBlankAcceptEmptyConfig {

        @Property(name = THE_VALUE, defaultValue = "", accept = "empty")
        private long value;

        public void setValue(long value) {
            this.value = value;
        }
    }

    class PrimLongDefaultBlankAcceptBlankConfig {

        @Property(name = THE_VALUE, defaultValue = "", accept = "")
        private long value;

        public void setValue(long value) {
            this.value = value;
        }
    }

    /**
     * This is just used as a mix-in to ensure that the configuration ignores interfaces.
     */
    public static interface DoesNothing {

    }
}
