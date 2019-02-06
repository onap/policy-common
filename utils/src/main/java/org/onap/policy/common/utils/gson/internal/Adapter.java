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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Super class of adapters used to serialize and de-serialize an item.
 */
public class Adapter {

    /**
     * Pattern to match valid identifiers.
     */
    private static final Pattern VALID_NAME_PAT = Pattern.compile("[a-zA-Z_]\\w*");

    /**
     * Name of the property within the json structure containing the item.
     */
    private final String propName;

    /**
     * Gson object that will provide the type converter.
     */
    private final Gson gson;

    /**
     * Converter used when reading.
     */
    private final ConvInfo reader;

    /**
     * Converter used when writing, allocated lazily, once an actual type is determined.
     */
    private volatile ConvInfo writer = null;

    /**
     * Name of the item being lifted - used when throwing exceptions.
     */
    private final String fullName;

    /**
     * Constructs the object.
     *
     * @param gson Gson object providing type adapters
     * @param field method used to access the item from within an object
     */
    public Adapter(Gson gson, Field field) {
        this.propName = detmPropName(field);
        this.reader = new ConvInfo(TypeToken.get(field.getGenericType()));
        this.gson = gson;
        this.fullName = getQualifiedName(field);

        field.setAccessible(true);
    }

    /**
     * Constructs the object.
     *
     * @param gson Gson object providing type adapters
     * @param accessor method used to access the item from within an object
     * @param forGetter {@code true} if the name is for a "getter" method, {@code false}
     *        if for a "setter"
     * @param valueType the class of value on which this operates
     */
    public Adapter(Gson gson, Method accessor, boolean forGetter, Type valueType) {
        this.propName = (forGetter ? detmGetterPropName(accessor) : detmSetterPropName(accessor));
        this.reader = new ConvInfo(TypeToken.get(valueType));
        this.gson = gson;
        this.fullName = getQualifiedName(accessor);

        accessor.setAccessible(true);
    }

    /**
     * Converts an object to a json tree.
     *
     * @param object the object to be converted
     * @return a json tree representing the object
     */
    @SuppressWarnings("unchecked")
    public JsonElement toJsonTree(Object object) {
        // always use a converter for the specific subclass
        Class<? extends Object> clazz = object.getClass();

        if (writer == null) {
            writer = new ConvInfo(TypeToken.get(clazz));
        }

        ConvInfo wtr = writer;
        TypeAdapter<Object> conv =
                        (TypeAdapter<Object>) (wtr.clazz == clazz ? wtr.getConverter() : gson.getAdapter(clazz));

        return conv.toJsonTree(object);
    }

    /**
     * Converts a json tree to an object.
     *
     * @param tree the tree to be converted
     * @return the object represented by the tree
     */
    public Object fromJsonTree(JsonElement tree) {
        return reader.getConverter().fromJsonTree(tree);
    }

    public final String getPropName() {
        return propName;
    }

    public final String getFullName() {
        return fullName;
    }

    /**
     * Makes an error message, appending the item's full name to the message prefix.
     *
     * @param prefix the message prefix
     * @return the error message
     */
    public String makeError(String prefix) {
        return (prefix + fullName);
    }

    /**
     * Determines if the field is managed by the walker.
     *
     * @param field the field to examine
     * @return {@code true} if the field is managed by the walker, {@code false} otherwise
     */
    public static boolean isManaged(Field field) {
        return VALID_NAME_PAT.matcher(field.getName()).matches();
    }

    /**
     * Determines if the method is managed by the walker.
     *
     * @param method the method to examine
     * @return {@code true} if the method is managed by the walker, {@code false}
     *         otherwise
     */
    public static boolean isManaged(Method method) {
        return VALID_NAME_PAT.matcher(method.getName()).matches();
    }

    /**
     * Determines the property name of an item within the json structure.
     *
     * @param field the item within the object
     * @return the json property name for the item or {@code null} if the name is invalid
     */
    public static String detmPropName(Field field) {
        // use the serialized name, if specified
        JsonProperty prop = field.getAnnotation(JsonProperty.class);
        if (prop != null && !prop.value().isEmpty()) {
            return prop.value();
        }

        // no name provided - use it as is
        return (isManaged(field) ? field.getName() : null);
    }

    /**
     * Determines the property name of an item, within the json structure, associated with
     * a "get" method.
     *
     * @param method method to be invoked to get the item within the object
     * @return the json property name for the item, or {@code null} if the method name is
     *         not valid
     */
    public static String detmGetterPropName(Method method) {

        return detmPropNameCommon(method, () -> {

            if (!isManaged(method)) {
                return null;
            }

            String name = method.getName();
            Class<?> treturn = method.getReturnType();

            if (name.startsWith("get")) {
                return name.substring(3);

            } else if (name.startsWith("is") && treturn == boolean.class || treturn == Boolean.class) {
                return name.substring(2);

            } else {
                // not a valid name for a "getter" method
                return null;
            }
        });
    }

    /**
     * Determines the property name of an item, within the json structure, associated with
     * a "set" method.
     *
     * @param method method to be invoked to set the item within the object
     * @return the json property name for the item, or {@code null} if the method name is
     *         not valid
     */
    public static String detmSetterPropName(Method method) {

        return detmPropNameCommon(method, () -> {

            if (!isManaged(method)) {
                return null;
            }

            String name = method.getName();

            if (name.startsWith("set")) {
                return name.substring(3);

            } else {
                // not a valid name for a "getter" method
                return null;
            }
        });
    }

    /**
     * Determines the property name of an item within the json structure.
     *
     * @param method method to be invoked to get/set the item within the object
     * @param extractor function to extract the name directly from the method name
     * @return the json property name for the item, or {@code null} if the method name is
     *         not valid
     */
    private static String detmPropNameCommon(Method method, Supplier<String> extractor) {

        // use the property name, if specified
        JsonProperty propName = method.getAnnotation(JsonProperty.class);
        if (propName != null && !propName.value().isEmpty()) {
            return propName.value();
        }

        // no name provided - must compute it from the method name
        String name = extractor.get();

        if (name == null || name.isEmpty()) {
            // nothing left after stripping the prefix - invalid name
            return null;
        }

        // translate the first letter to lower-case
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    /**
     * Gets the fully qualified name of a field.
     *
     * @param field field whose name is desired
     * @return the field fully qualified name
     */
    public static String getQualifiedName(Field field) {
        return (field.getDeclaringClass().getName() + "." + field.getName());
    }

    /**
     * Gets the fully qualified name of a method.
     *
     * @param method method whose name is desired
     * @return the method's fully qualified name
     */
    public static String getQualifiedName(Method method) {
        return (method.getDeclaringClass().getName() + "." + method.getName());
    }

    /**
     * Converter info.
     */
    private class ConvInfo {

        /**
         * Type on which the converter works.
         */
        private TypeToken<?> type;

        /**
         * Class of object on which the converter works.
         */
        private Class<?> clazz;

        /**
         * Converter to use, initialized lazily.
         */
        private TypeAdapter<?> conv = null;

        /**
         * Constructs the object.
         *
         * @param type type of object to be converted
         */
        public ConvInfo(TypeToken<?> type) {
            this.type = type;
            this.clazz = type.getRawType();
        }

        public final TypeAdapter<?> getConverter() {
            if (conv == null) {
                this.conv = gson.getAdapter(type);
            }

            return conv;
        }
    }
}
