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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import org.onap.policy.common.utils.gson.annotations.GsonExpose;

/**
 * Adapter factory that handles classes using the {@link GsonExpose} annotation. It also
 * auto-exposes any method having the <i>SerializedName</i> annotation.
 */
public class GsonExposeTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        List<Serializer> serList = new LinkedList<>();
        findExposeAnnotations(gson, serList, type.getRawType());

        if (serList.isEmpty()) {
            // the class doesn't expose anything, thus we don't handle it
            return null;
        }

        final Serializer[] serializers = serList.toArray(new Serializer[0]);
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

        return new TypeAdapter<T>() {

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                JsonObject tree = delegate.toJsonTree(value).getAsJsonObject();
                addToTree(value, tree);

                elementAdapter.write(out, tree);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                return delegate.read(in);
            }

            /**
             * Gets the exposed items from the value and adds them to the tree.
             *
             * @param value value from which to get the exposed items
             * @param tree tree into which the items are to be added
             */
            private void addToTree(T value, JsonObject tree) {
                for (Serializer ser : serializers) {
                    ser.addToTree(value, tree);
                }
            }
        };
    }

    /**
     * Finds all items to be exposed and adds them to the list of serializers.
     *
     * @param gson Gson object providing type adapters for the exposed items
     * @param serList list onto which the serializers should be added
     * @param rawType class to be examined for exposed items
     */
    private <T> void findExposeAnnotations(Gson gson, List<Serializer> serList, Class<? super T> rawType) {
        for (Method method : rawType.getMethods()) {
            if (method.getAnnotation(GsonExpose.class) == null && method.getAnnotation(SerializedName.class) == null) {
                // this method is not exposed
                continue;
            }

            if (Modifier.isStatic(method.getModifiers())) {
                // "static" methods are not supported
                throw new JsonParseException(GsonExpose.class.getSimpleName() + " applied to 'static' method: "
                                + method.getDeclaringClass().getName() + "." + method.getName());
            }

            if (Void.TYPE.equals(method.getReturnType())) {
                // "setter" methods are not supported yet
                throw new JsonParseException(GsonExpose.class.getSimpleName() + " applied to 'void' method: "
                                + method.getDeclaringClass().getName() + "." + method.getName());
            }

            serList.add(new Serializer(gson, method));
        }
    }

    /**
     * Determines the property name of the item within the json structure.
     *
     * @param method method to be invoked to get/set the item within the object
     * @return the json property name for the item
     */
    public static String detmName(Method method) {
        // use the serialized name, if specified
        SerializedName annot = method.getAnnotation(SerializedName.class);
        if (annot != null) {
            if (annot.value().isEmpty()) {
                throw new JsonParseException("empty property name for " + GsonExpose.class.getSimpleName() + ": "
                                + method.getDeclaringClass().getName() + "." + method.getName());
            }

            /*
             * Don't bother checking alternate names specified within the annotation, as
             * we don't have to de-serialize them at this point in time.
             */

            return annot.value();
        }

        // no name provided - must compute it from the method name
        String name = method.getName();

        // strip get/is prefix
        if (name.startsWith("get")) {
            name = name.substring(3);
        } else if (name.startsWith("is")) {
            name = name.substring(2);
        }

        if (name.isEmpty()) {
            throw new JsonParseException("invalid property name for " + GsonExpose.class.getSimpleName() + ": "
                            + method.getDeclaringClass().getName() + "." + method.getName());
        }

        // translate the first letter to lower-case
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    /**
     * Used to serialize an exposed item.
     */
    private static class Serializer {
        /**
         * Name of the property within the json structure into which the item will be
         * placed.
         */
        private final String propName;

        /**
         * Method used to get the item from within an object.
         */
        private final Method getter;

        /**
         * Adapter used to convert the item's value into a {@link JsonElement}.
         */
        @SuppressWarnings("rawtypes")
        private final TypeAdapter converter;

        /**
         * Constructs the object.
         *
         * @param gson Gson object providing type adapters
         * @param getter method used to get the item from within an object
         */
        public Serializer(Gson gson, Method getter) {

            if (getter.getParameterCount() != 0) {
                // method expects arguments
                throw new JsonParseException(GsonExpose.class.getSimpleName() + " applied to method with arguments: "
                                + getter.getDeclaringClass().getName() + "." + getter.getName());
            }

            this.propName = detmName(getter);
            this.getter = getter;
            this.converter = gson.getAdapter(getter.getReturnType());
        }

        /**
         * Extracts the item's value from an object and adds it to a tree.
         *
         * @param object object from which to extract the value
         * @param tree tree into which to place the extracted value
         */
        public void addToTree(Object object, JsonObject tree) {
            try {
                // get the value from the object
                Object value = getter.invoke(object);
                if (value == null) {
                    // TODO need this?
                    tree.add(propName, null);
                    return;
                }

                @SuppressWarnings("unchecked")
                JsonElement jsonEl = converter.toJsonTree(value);

                tree.add(propName, jsonEl);

            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new JsonParseException("cannot invoke method for " + GsonExpose.class.getSimpleName() + ": "
                                + getter.getDeclaringClass().getName() + "." + getter.getName(), e);
            }
        }
    }
}
