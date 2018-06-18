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

import static org.junit.Assert.*;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.properties.exception.PropertyException;
import org.onap.policy.common.utils.properties.exception.PropertyMissingException;
import static org.onap.policy.common.utils.properties.SpecPropertyConfiguration.*;

/**
 * 
 */
public class SpecPropertyConfigurationTest {
    
    /**
     * The specializer.
     */
    private static final String SPEC = "my.name";

    /**
     * Properties used when invoking constructors.
     */
    private Properties props;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        props = new Properties();
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#getRawPropertyValue(java.util.Properties, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test
    public void testGetRawPropertyValue() throws PropertyException {
        class Config extends SpecPropertyConfiguration {

            // no spec
            @Property(name = "prefix.suffix")
            private String noSpec;
            
            // no spec, other type
            @Property(name = "no.spec.bool")
            private boolean noSpecBool;
            
            // type 1, no prefix
            @Property(name = "{$}.suffix")
            private String type1NoPrefix;
            
            // type 1, no suffix
            @Property(name = "prefix.{$}")
            private String type1NoSuffix;
            
            // type 1, both prefix and suffix
            @Property(name = "prefix.{$}.suffix")
            private String type1Both;
            
            // type 1, other type
            @Property(name = "an.{$}.int")
            private int type1Int;
            
            // type 2, no prefix
            @Property(name = "{abc.?.def}.suffix")
            private String type2NoPrefix;
            
            // type 2, no suffix
            @Property(name = "prefix.{abc.?.def}")
            private String type2NoSuffix;
            
            // type 2, no spec prefix
            @Property(name = "prefix.{?.def}.suffix")
            private String type2NoSpecPrefix;
            
            // type 2, no spec suffix
            @Property(name = "prefix{.abc.?}.suffix")
            private String type2NoSpecSuffix;
            
            // type 2, all components
            @Property(name = "prefix.{abc.?.def.}suffix")
            private String type2Both;
            
            // type 2, other type
            @Property(name = "a.{abc.?.def.}long")
            private long type2Long;
            
            public Config(String specialization, Properties props) throws PropertyException {
                super(specialization, props);
            }

            @SuppressWarnings("unused")
            public void setNoSpec(String noSpec) {
                this.noSpec = noSpec;
            }

            @SuppressWarnings("unused")
            public void setNoSpecBool(boolean noSpecBool) {
                this.noSpecBool = noSpecBool;
            }

            @SuppressWarnings("unused")
            public void setType1NoPrefix(String type1NoPrefix) {
                this.type1NoPrefix = type1NoPrefix;
            }

            @SuppressWarnings("unused")
            public void setType1NoSuffix(String type1NoSuffix) {
                this.type1NoSuffix = type1NoSuffix;
            }

            @SuppressWarnings("unused")
            public void setType1Both(String type1Both) {
                this.type1Both = type1Both;
            }

            @SuppressWarnings("unused")
            public void setType1Int(int type1Int) {
                this.type1Int = type1Int;
            }

            @SuppressWarnings("unused")
            public void setType2NoPrefix(String type2NoPrefix) {
                this.type2NoPrefix = type2NoPrefix;
            }

            @SuppressWarnings("unused")
            public void setType2NoSuffix(String type2NoSuffix) {
                this.type2NoSuffix = type2NoSuffix;
            }

            @SuppressWarnings("unused")
            public void setType2NoSpecPrefix(String type2NoSpecPrefix) {
                this.type2NoSpecPrefix = type2NoSpecPrefix;
            }

            @SuppressWarnings("unused")
            public void setType2NoSpecSuffix(String type2NoSpecSuffix) {
                this.type2NoSpecSuffix = type2NoSpecSuffix;
            }

            @SuppressWarnings("unused")
            public void setType2Both(String type2Both) {
                this.type2Both = type2Both;
            }

            @SuppressWarnings("unused")
            public void setType2Long(long type2Long) {
                this.type2Long = type2Long;
            }
        };

        props.setProperty("prefix.suffix", "no.spec");
        props.setProperty("no.spec.bool", "true");
        props.setProperty("world.suffix", "type1.no.prefix");
        props.setProperty("prefix.world", "type1.no.suffix");
        props.setProperty("prefix.world.suffix", "type1.both");
        props.setProperty("an.world.int", "200");
        props.setProperty("abc.world.def.suffix", "type2.no.prefix");
        props.setProperty("prefix.abc.world.def", "type2.no.suffix");
        props.setProperty("prefix.world.def.suffix", "type2.no.spec.prefix");
        props.setProperty("prefix.abc.world.suffix", "type2.no.spec.suffix");
        props.setProperty("prefix.abc.world.def.suffix", "type2.both");
        props.setProperty("a.abc.world.def.long", "3000");
        
        Config cfg = new Config("world", props);
        
        assertEquals("no.spec", cfg.noSpec);
        assertEquals(true, cfg.noSpecBool);
        assertEquals("type1.no.prefix", cfg.type1NoPrefix);
        assertEquals("type1.no.suffix", cfg.type1NoSuffix);
        assertEquals("type1.both", cfg.type1Both);
        assertEquals(200, cfg.type1Int);
        assertEquals("type2.no.prefix", cfg.type2NoPrefix);
        assertEquals("type2.no.suffix", cfg.type2NoSuffix);
        assertEquals("type2.no.spec.prefix", cfg.type2NoSpecPrefix);
        assertEquals("type2.no.spec.suffix", cfg.type2NoSpecSuffix);
        assertEquals("type2.both", cfg.type2Both);
        assertEquals(3000L, cfg.type2Long);
    }
    @Test
    public void testGetRawPropertyValue_Type2_Generalized() throws PropertyException {
        class Config extends SpecPropertyConfiguration {
            
            // type 2, all components
            @Property(name = "prefix.{abc.?.def.}suffix")
            private String value;
            
            public Config(String specialization, Properties props) throws PropertyException {
                super(specialization, props);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        };

        props.setProperty("prefix.suffix", "no.spec");
        
        Config cfg = new Config("world", props);
        
        assertEquals("no.spec", cfg.value);
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#getRawPropertyValue(java.util.Properties, java.lang.String)}.
     * @throws PropertyException 
     */
    @Test(expected = PropertyMissingException.class)
    public void testGetRawPropertyValue_NotFound() throws PropertyException {
        class Config extends SpecPropertyConfiguration {
            
            @Property(name = "not.found")
            private String notFound;
            
            public Config(String specialization, Properties props) throws PropertyException {
                super(specialization, props);
            }

            @SuppressWarnings("unused")
            public void setNotFound(String notFound) {
                this.notFound = notFound;
            }
        };
        
        new Config("not found", props);
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#SpecPropertyConfiguration(java.lang.String)}.
     * @throws PropertyException 
     */
    @Test
    public void testSpecPropertyConfigurationString() throws PropertyException {
        final String propnm = "string.{$}.prop";
        final String propval = "hello";
        
        class Config extends SpecPropertyConfiguration {
            
            @Property(name = propnm)
            private String value;

            public Config(String specialization) {
                super(specialization);
            }

            @SuppressWarnings("unused")
            public void setValue(String value) {
                this.value = value;
            }
        };

        props.setProperty(specialize(propnm, SPEC), propval);
        
        Config cfg = new Config(SPEC);
        assertEquals(null, cfg.value);
        
        cfg.setAllFields(props);
        assertEquals(propval, cfg.value);
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#SpecPropertyConfiguration(java.lang.String, java.util.Properties)}.
     * @throws PropertyException 
     */
    @Test
    public void testSpecPropertyConfigurationStringProperties() throws PropertyException {
        final String propnm = "int.{$}.prop";
        final int propval = 10;
        
        class Config extends SpecPropertyConfiguration {

            @Property(name = propnm)
            private int value;
            
            public Config(String specialization, Properties props) throws PropertyException {
                super(specialization, props);
            }

            @SuppressWarnings("unused")
            public void setValue(int value) {
                this.value = value;
            }
        };

        props.setProperty(specialize(propnm, SPEC), String.valueOf(propval));
        
        Config cfg = new Config(SPEC, props);
        
        assertEquals(propval, cfg.value);
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#generalize(java.lang.String)}.
     */
    @Test
    public void testGeneralize_NoSpec() {
        final String xyzPdq = "xyz.pdq";
        
        // no spec
        assertEquals(xyzPdq, generalize(xyzPdq));
        
        // spec type 1 throws an exception - we'll test it separately

        // spec type 2
        assertEquals(xyzPdq, generalize("xyz.{xxx.?.yyy.}pdq"));
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#generalize(java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGeneralize_Spec1() { 
        generalize("abc.{$}.def");
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#generalizeType2(java.lang.String, java.util.regex.Matcher)}.
     */
    @Test
    public void testGeneralizeType2() {
        assertEquals("abc.def", generalize("abc.{xyz?pdq}def"));

        assertEquals("", generalize("{xyz?pdq}"));
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#specialize(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testSpecialize() {
        final String spec = "get.spec";
        final String abcDef = "abc.def";
        
        // no spec
        assertEquals(abcDef, specialize(abcDef, spec));

        // spec type 1
        assertEquals("abc.get.spec.def", specialize("abc.{$}.def", spec));

        // spec type 2
        assertEquals("abc.xxx.get.spec.yyy.def", specialize("abc.{xxx.?.yyy.}def", spec));
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#specializeType1(java.lang.String, java.lang.String, java.util.regex.Matcher)}.
     */
    @Test
    public void testSpecializeType1() {
        final String spec = "spec1";
        
        // no prefix 
        assertEquals("spec1.def", specialize("{$}.def", spec));
        
        // no suffix 
        assertEquals("abc.spec1", specialize("abc.{$}", spec));
        
        // with both prefix and suffix 
        assertEquals("abc.spec1.def", specialize("abc.{$}.def", spec));
    }

    /**
     * Test method for {@link org.onap.policy.common.utils.properties.SpecPropertyConfiguration#specializeType2(java.lang.String, java.lang.String, java.util.regex.Matcher)}.
     */
    @Test
    public void testSpecializeType2() {
        final String spec = "spec2";
        
        // no prefix 
        assertEquals("xxx.spec2.yyy.def", specialize("{xxx.?.yyy.}def", spec));
        
        // no suffix 
        assertEquals("abc.xxx.spec2.yyy", specialize("abc{.xxx.?.yyy}", spec));
        
        // no spec prefix
        assertEquals("abc.spec2.yyy.def", specialize("abc.{?.yyy.}def", spec));
        
        // no spec suffix
        assertEquals("abc.xxx.spec2.def", specialize("abc.{xxx.?}.def", spec));
        
        // no components
        assertEquals(spec, specialize("{?}", spec));
        
        // all components 
        assertEquals("abc.xxx.spec2.yyy.def", specialize("abc.{xxx.?.yyy.}def", spec));
    }

}
