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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;
import org.onap.policy.common.utils.properties.exception.PropertyAccessException;
import org.onap.policy.common.utils.properties.exception.PropertyException;
import org.onap.policy.common.utils.properties.exception.PropertyInvalidException;
import org.onap.policy.common.utils.properties.exception.PropertyMissingException;

/**
 * Configuration whose fields are initialized by reading from a set of {@link Properties},
 * as directed by the {@link Property} annotations that appear on fields within the
 * subclass.
 * <p>
 * It is possible that an invalid <i>defaultValue</i> is specified via the
 * {@link Property} annotation. This could remain undetected until an optional property is
 * left out of the {@link Properties}. Consequently, it will always be validated unless it
 * is empty and <i>emptyOk</i> is {@code false}.
 */
public class PropertyConfiguration {

    /**
     * Constructs a configuration, without populating any fields; fields should be
     * populated later by invoking {@link #setAllFields(Properties)}.
     */
    public PropertyConfiguration() {
        super();
    }

    /**
     * Initializes each "@Property" field with its value, as found in the properties.
     * 
     * @param props properties from which to extract the values
     * @throws PropertyException if an error occurs
     */
    public PropertyConfiguration(Properties props) throws PropertyException {
        setAllFields(props);
    }

    /**
     * Walks the class hierarchy of "this" object, populating fields defined in each
     * class, using values extracted from the given property set.
     * 
     * @param props properties from which to extract the values
     * @throws PropertyException if an error occurs
     */
    public void setAllFields(Properties props) throws PropertyException {
        Class<?> clazz = getClass();

        while (clazz != PropertyConfiguration.class) {
            for (Field field : clazz.getDeclaredFields()) {
                setValue(field, props);
            }

            clazz = clazz.getSuperclass();
        }
    }

    /**
     * Sets a field's value, within an object, based on what's in the properties.
     * 
     * @param field field whose value is to be set
     * @param props properties from which to get the value
     * @return {@code true} if the property's value was set, {@code false} otherwise
     * @throws PropertyException if an error occurs
     */
    protected boolean setValue(Field field, Properties props) throws PropertyException {
        Property prop = field.getAnnotation(Property.class);
        if (prop == null) {
            return false;
        }

        checkModifiable(field, prop);

        if (setValue(field, props, prop)) {
            return true;
        }

        throw new PropertyAccessException(prop.name(), field.getName(), "unsupported field type");
    }

    /**
     * Sets a field's value from a particular property.
     * 
     * @param field field whose value is to be set
     * @param props properties from which to get the value
     * @param prop property of interest
     * @return {@code true} if the property's value was set, {@code false} otherwise
     * @throws PropertyException if an error occurs
     */
    protected boolean setValue(Field field, Properties props, Property prop) throws PropertyException {

        try {
            Object val = getValue(field.getName(), field.getType(), props, prop);
            if (val == null) {
                return false;

            } else {

                /*
                 * According to java docs & blogs, "field" is our own copy, so we're free
                 * to change the flags without impacting the real permissions of the field
                 * within the real class.
                 */
                field.setAccessible(true);

                field.set(this, val);
                return true;
            }

        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new PropertyAccessException(prop.name(), field.getName(), e);
        }
    }

    /**
     * Gets a property value, coercing it to the field's type.
     * 
     * @param fieldName field whose value is to be set
     * @param clazz field's type
     * @param props properties from which to get the value
     * @param prop property of interest
     * @return the value extracted from the property, or {@code null} if the field type is
     *         not supported
     * @throws PropertyException if an error occurs
     */
    protected Object getValue(String fieldName, Class<?> clazz, Properties props, Property prop)
                    throws PropertyException {

        // can still add support for short, float, double, enum

        if (clazz == String.class) {
            return getStringValue(fieldName, props, prop);

        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return getBooleanValue(fieldName, props, prop);

        } else if (clazz == Integer.class || clazz == int.class) {
            return getIntegerValue(fieldName, props, prop);

        } else if (clazz == Long.class || clazz == long.class) {
            return getLongValue(fieldName, props, prop);

        } else {
            return null;
        }
    }

    /**
     * Verifies that the field can be modified, i.e., it's neither <i>static</i>, nor
     * <i>final</i>.
     * 
     * @param field field whose value is to be set
     * @param prop property of interest
     * @throws PropertyAccessException if the field is not modifiable
     */
    protected void checkModifiable(Field field, Property prop) throws PropertyAccessException {
        int mod = field.getModifiers();

        if (Modifier.isStatic(mod)) {
            throw new PropertyAccessException(prop.name(), field.getName(), "'static' variable cannot be modified");
        }

        if (Modifier.isFinal(mod)) {
            throw new PropertyAccessException(prop.name(), field.getName(), "'final' variable cannot be modified");
        }
    }

    /**
     * Gets a property value, coercing it to a String.
     * 
     * @param fieldName field whose value is to be set
     * @param props properties from which to get the value
     * @param prop property of interest
     * @return the value extracted from the property
     * @throws PropertyException if an error occurs
     */
    protected String getStringValue(String fieldName, Properties props, Property prop) throws PropertyException {

        /*
         * Note: the default value for a String type is always valid, thus no need to
         * check it.
         */

        return getPropValue(fieldName, props, prop);
    }

    /**
     * Gets a property value, coercing it to a Boolean.
     * 
     * @param fieldName field whose value is to be set
     * @param props properties from which to get the value
     * @param prop property of interest
     * @return the value extracted from the property
     * @throws PropertyException if an error occurs
     */
    protected Boolean getBooleanValue(String fieldName, Properties props, Property prop) throws PropertyException {
        // validate the default value
        checkDefaultValue(fieldName, prop, xxx -> makeBoolean(fieldName, prop, prop.defaultValue()));

        return makeBoolean(fieldName, prop, getPropValue(fieldName, props, prop));
    }

    /**
     * Gets a property value, coercing it to an Integer.
     * 
     * @param fieldName field whose value is to be set
     * @param props properties from which to get the value
     * @param prop property of interest
     * @return the value extracted from the property
     * @throws PropertyException if an error occurs
     */
    protected Integer getIntegerValue(String fieldName, Properties props, Property prop) throws PropertyException {
        // validate the default value
        checkDefaultValue(fieldName, prop, xxx -> makeInteger(fieldName, prop, prop.defaultValue()));

        return makeInteger(fieldName, prop, getPropValue(fieldName, props, prop));
    }

    /**
     * Gets a property value, coercing it to a Long.
     * 
     * @param fieldName field whose value is to be set
     * @param props properties from which to get the value
     * @param prop property of interest
     * @return the value extracted from the property
     * @throws PropertyException if an error occurs
     */
    protected Long getLongValue(String fieldName, Properties props, Property prop) throws PropertyException {
        // validate the default value
        checkDefaultValue(fieldName, prop, xxx -> makeLong(fieldName, prop, prop.defaultValue()));

        return makeLong(fieldName, prop, getPropValue(fieldName, props, prop));
    }

    /**
     * Gets a value from the property set.
     * 
     * @param fieldName field whose value is to be set
     * @param props properties from which to get the value
     * @param prop property of interest
     * @return the value extracted from the property, or the <i>defaultValue</i> if the
     *         value does not exist
     * @throws PropertyMissingException if the property does not exist and the
     *         <i>defaultValue</i> is empty and <i>emptyOk</i> is {@code false}
     */
    protected String getPropValue(String fieldName, Properties props, Property prop) throws PropertyMissingException {
        String propnm = prop.name();

        String val = getRawPropertyValue(props, propnm);
        if (val != null && (!val.isEmpty() || prop.emptyOk())) {
            return val;
        }

        val = prop.defaultValue();
        if (!val.isEmpty() || prop.emptyOk()) {
            return val;
        }

        throw new PropertyMissingException(prop.name(), fieldName);
    }

    /**
     * Gets the property value, straight from the property set.
     * 
     * @param props properties from which to get the value
     * @param propnm name of the property of interest
     * @return
     */
    protected String getRawPropertyValue(Properties props, String propnm) {
        return props.getProperty(propnm);
    }

    /**
     * Coerces a String value into a Boolean.
     * 
     * @param fieldName field whose value is to be set
     * @param prop property of interest
     * @param value value to be coerced
     * @return the Boolean value represented by the String value
     * @throws PropertyInvalidException if the value does not represent a valid Boolean
     */
    private Boolean makeBoolean(String fieldName, Property prop, String value) throws PropertyInvalidException {
        if ("true".equals(value.toLowerCase())) {
            return Boolean.TRUE;

        } else if ("false".equals(value.toLowerCase())) {
            return Boolean.FALSE;

        } else {
            throw new PropertyInvalidException(prop.name(), fieldName, "expecting 'true' or 'false'");
        }
    }

    /**
     * Coerces a String value into an Integer.
     * 
     * @param fieldName field whose value is to be set
     * @param prop property of interest
     * @param value value to be coerced
     * @return the Integer value represented by the String value
     * @throws PropertyInvalidException if the value does not represent a valid Integer
     */
    private Integer makeInteger(String fieldName, Property prop, String value) throws PropertyInvalidException {
        try {
            return Integer.valueOf(value);

        } catch (NumberFormatException e) {
            throw new PropertyInvalidException(prop.name(), fieldName, e);
        }
    }

    /**
     * Coerces a String value into a Long.
     * 
     * @param fieldName field whose value is to be set
     * @param prop property of interest
     * @param value value to be coerced
     * @return the Long value represented by the String value
     * @throws PropertyInvalidException if the value does not represent a valid Long
     */
    private Long makeLong(String fieldName, Property prop, String value) throws PropertyInvalidException {
        try {
            return Long.valueOf(value);

        } catch (NumberFormatException e) {
            throw new PropertyInvalidException(prop.name(), fieldName, e);
        }
    }

    /**
     * Applies a function to check a property's default value. If the function throws an
     * exception about an invalid property, then it's re-thrown as an exception about an
     * invalid <i>defaultValue</i>.
     * 
     * @param fieldName name of the field being checked
     * @param prop property of interest
     * @param func function to invoke to check the default value
     */
    private void checkDefaultValue(String fieldName, Property prop, CheckDefaultValueFunction func)
                    throws PropertyInvalidException {

        if (!prop.defaultValue().isEmpty() || prop.emptyOk()) {
            try {
                func.apply(null);

            } catch (PropertyInvalidException ex) {
                throw new PropertyInvalidException(ex.getPropertyName(), fieldName, "defaultValue is invalid",
                                ex.getCause());
            }
        }
    }

    /**
     * Functions to check a default value.
     */
    @FunctionalInterface
    private static interface CheckDefaultValueFunction {

        /**
         * Checks the default value.
         * 
         * @param arg always {@code null}
         * @throws PropertyInvalidException if an error occurs
         */
        public void apply(Void arg) throws PropertyInvalidException;
    }

    /**
     * Annotation that declares a variable to be configured via {@link Properties}.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)

    protected static @interface Property {

        /**
         * Name of the property.
         * 
         * @return the property name
         */
        public String name();

        /**
         * Default value, used when the property does not exist.
         * 
         * @return the default value
         */
        public String defaultValue() default "";

        /**
         * Indicates whether or not it's ok to use the default value, even if it's empty.
         * 
         * @return {@code true} if it's ok to use the default value when it's empty,
         *         {@code false} otherwise.
         */
        public boolean emptyOk() default false;

    }
}
