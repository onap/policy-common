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

package org.onap.policy.common.utils.properties.exception;

import static org.junit.Assert.*;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.properties.PropertyConfiguration;

/**
 * 
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

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#PropertyConfiguration(java.util.Properties)}.
     * 
     * @throws PropertyException
     */
    @Test
    public void testPropertyConfiguration() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#makeLong(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test
    public void testWalkClassHierarchy() throws PropertyException {
        class GrandParentConfig extends PropertyConfiguration {

            @Property(name = "grandparent.value")
            protected boolean grandparentValue;

            public GrandParentConfig(Properties props) throws PropertyException {
                super(props);
            }
        };
        
        class ParentConfig extends GrandParentConfig {

            @Property(name = "parent.value")
            protected long parentValue;

            public ParentConfig(Properties props) throws PropertyException {
                super(props);
            }
        };

        class Config extends ParentConfig {

            @Property(name = THE_VALUE)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        props.setProperty("parent.value", "50000");
        props.setProperty("grandparent.value", "true");
        
        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.value);
        assertEquals(50000L, cfg.parentValue);
        assertEquals(true, cfg.grandparentValue);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#setValue(java.lang.reflect.Field, java.util.Properties)}.
     * 
     * @throws PropertyException
     */
    @Test
    public void testSetValueFieldProperties_FieldSet() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#setValue(java.lang.reflect.Field, java.util.Properties)}.
     * 
     * @throws PropertyException
     */
    @Test
    public void testSetValueFieldProperties_NoAnnotation() throws PropertyException {
        class Config extends PropertyConfiguration {

            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config(props);

        assertNull(cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#setValue(java.lang.reflect.Field, java.util.Properties)}.
     * 
     * @throws PropertyException
     */
    @Test(expected = PropertyAccessException.class)
    public void testSetValueFieldProperties_WrongFieldType() throws PropertyException {
        class Config extends PropertyConfiguration {

            // Cannot set a property into an "Exception" field
            @Property(name = THE_VALUE)
            private Exception value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#setValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyMissingException.class)
    public void testSetValueFieldPropertyProperties_NoProperty_NoDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetValue() throws PropertyException {
        // this class contains all of the supported field types
        class Config extends PropertyConfiguration {
            
            @Property(name = "string")
            private String stringValue;
            
            @Property(name = "boolean")
            private Boolean boolValue;
            
            @Property(name = "primitive.boolean")
            private boolean primBoolValue;
            
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
        };

        props.setProperty("string", "a string");
        props.setProperty("boolean", "true");
        props.setProperty("primitive.boolean", "true");
        props.setProperty("integer", "100");
        props.setProperty("primitive.integer", "101");
        props.setProperty("long", "10000");
        props.setProperty("primitive.long", "10001");
        
        Config cfg = new Config(props);

        assertEquals("a string", cfg.stringValue);
        assertEquals(true, cfg.boolValue);
        assertEquals(true, cfg.primBoolValue);
        assertEquals(100, cfg.intValue.intValue());
        assertEquals(101, cfg.primIntValue);
        assertEquals(10000, cfg.longValue.longValue());
        assertEquals(10001, cfg.primLongValue);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyAccessException.class)
    public void testGetValue_UnsupportedType() throws PropertyException {
        class Config extends PropertyConfiguration {

            // Cannot set a property into an "Exception" field
            @Property(name = THE_VALUE)
            private Exception value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#checkModifiable(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyAccessException.class)
    public void testCheckModifiable_Static() throws PropertyException {
        props.setProperty(THE_VALUE, STRING_VALUE);
        new StaticConfig(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#checkModifiable(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyAccessException.class)
    public void testCheckModifiable_Final() throws PropertyException {
        class Config extends PropertyConfiguration {

            // Cannot set a property into an "final" field
            @Property(name = THE_VALUE)
            private final String value = "";

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getStringValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetStringValue() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getBooleanValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetBooleanValue_NoDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private Boolean value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "true");
        Config cfg = new Config(props);

        assertEquals(true, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getBooleanValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyInvalidException.class)
    public void testGetBooleanValue_InvalidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue=INVALID_VALUE, useDefault=true)
            private Boolean value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "true");
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getBooleanValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetBooleanValue_ValidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue="true", useDefault=true)
            private Boolean value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };
        
        // property not defined
        Config cfg = new Config(props);
        assertEquals(true, cfg.value);
        
        // try again, with the property defined
        props.setProperty(THE_VALUE, "true");
        cfg = new Config(props);
        assertEquals(true, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getIntegerValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetIntegerValue_NoDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private Integer value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "200");
        Config cfg = new Config(props);

        assertEquals(200, cfg.value.intValue());
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getIntegerValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     */
    @Test(expected = PropertyInvalidException.class)
    public void testGetIntegerValue_InvalidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue=INVALID_VALUE, useDefault=true)
            private Integer value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "200");
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getIntegerValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetIntegerValue_ValidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue="201", useDefault=true)
            private Integer value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };
        
        // property not defined
        Config cfg = new Config(props);
        assertEquals(201, cfg.value.intValue());
        
        // try again, with the property defined
        props.setProperty(THE_VALUE, "200");
        cfg = new Config(props);
        assertEquals(200, cfg.value.intValue());
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getLongValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetLongValue_NoDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private Long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "20000");
        Config cfg = new Config(props);

        assertEquals(20000L, cfg.value.longValue());
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getLongValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyInvalidException.class)
    public void testGetLongValue_InvalidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue=INVALID_VALUE, useDefault=true)
            private Long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "20000");
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getLongValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetLongValue_ValidDefault() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE, defaultValue="20001", useDefault=true)
            private Long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };
        
        // property not defined
        Config cfg = new Config(props);
        assertEquals(20001L, cfg.value.longValue());
        
        // try again, with the property defined
        props.setProperty(THE_VALUE, "20000");
        cfg = new Config(props);
        assertEquals(20000L, cfg.value.longValue());
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getPropValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetPropValue_Prop_Default() throws PropertyException {
        class Config extends PropertyConfiguration {
            
            @Property(name = THE_VALUE, defaultValue = STRING_VALUE_DEFAULT, useDefault = true)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getPropValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetPropValue_Prop_NoDefault() throws PropertyException {
        class Config extends PropertyConfiguration {
            
            @Property(name = THE_VALUE)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, STRING_VALUE);
        Config cfg = new Config(props);

        assertEquals(STRING_VALUE, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getPropValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetPropValue_NoProp_Default() throws PropertyException {
        class Config extends PropertyConfiguration {
            
            @Property(name = THE_VALUE, defaultValue = STRING_VALUE_DEFAULT, useDefault = true)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        // do NOT set the property
        
        Config cfg = new Config(props);

        assertEquals(STRING_VALUE_DEFAULT, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getPropValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyMissingException.class)
    public void testGetPropValue_NoProp_NoDefault() throws PropertyException {
        class Config extends PropertyConfiguration {
            
            @Property(name = THE_VALUE)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        // do NOT set the property
        
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#getPropValue(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyMissingException.class)
    public void testGetPropValue_NoProp_DefaultButDisabled() throws PropertyException {
        class Config extends PropertyConfiguration {
            
            @Property(name = THE_VALUE, defaultValue = STRING_VALUE_DEFAULT, useDefault = false)
            private String value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        // do NOT set the property
        
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#makeBoolean(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test
    public void testMakeBoolean_True() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private Boolean value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "true");
        Config cfg = new Config(props);

        assertEquals(true, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#makeBoolean(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test
    public void testMakeBoolean_False() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private Boolean value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "false");
        Config cfg = new Config(props);

        assertEquals(false, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#makeBoolean(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyInvalidException.class)
    public void testMakeBoolean_Invalid() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private Boolean value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, INVALID_VALUE);
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#makeInteger(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test
    public void testMakeInteger_Valid() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private int value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "300");
        Config cfg = new Config(props);

        assertEquals(300, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#makeInteger(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyInvalidException.class)
    public void testMakeInteger_Invalid() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private int value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, INVALID_VALUE);
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#makeInteger(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyInvalidException.class)
    public void testMakeInteger_TooBig() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private int value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, String.valueOf(Integer.MAX_VALUE + 10L));
        new Config(props);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#makeLong(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test
    public void testMakeLong_Valid() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, "30000");
        Config cfg = new Config(props);

        assertEquals(30000L, cfg.value);
    }

    /**
     * Test method for
     * {@link org.onap.policy.common.utils.properties.PropertyConfiguration#makeLong(java.lang.reflect.Field, org.onap.policy.common.utils.properties.PropertyConfiguration.Property, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyInvalidException.class)
    public void testMakeLong_Invalid() throws PropertyException {
        class Config extends PropertyConfiguration {

            @Property(name = THE_VALUE)
            private long value;

            public Config(Properties props) throws PropertyException {
                super(props);
            }
        };

        props.setProperty(THE_VALUE, INVALID_VALUE);
        new Config(props);
    }
    
    /**
     * A config whose annotated property is "static".
     */
    public static class StaticConfig extends PropertyConfiguration {

        // "static" field cannot be set
        @Property(name = THE_VALUE)
        private static String value;

        public StaticConfig(Properties props) throws PropertyException {
            super(props);
        }
    };
}
