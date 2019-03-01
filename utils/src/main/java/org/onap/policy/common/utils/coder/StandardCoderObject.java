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

package org.onap.policy.common.utils.coder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Object type used by the {@link StandardCoder}. Different serialization tools have
 * different "standard objects". For instance, GSON uses {@link JsonElement}. This class
 * wraps that object so that it can be used without exposing the object, itself.
 */
public class StandardCoderObject {

    /**
     * Used to read various sources and write various targets.
     */
    private static final StandardCoder coder = new StandardCoder();

    /**
     * Used to translate between the standard object and a POJO.
     */
    private static final Gson gson = coder.getGson();

    /**
     * Data wrapped by this.
     */
    private final JsonElement data;

    /**
     * Constructs the object.
     */
    public StandardCoderObject() {
        data = null;
    }

    /**
     * Constructs the object from a JSON string.
     *
     * @param json the JSON string from which to construct this
     * @throws CoderException if an error occurs
     */
    public StandardCoderObject(String json) throws CoderException {
        data = coder.decode(json, JsonElement.class);
    }

    /**
     * Constructs the object.
     *
     * @param data data wrapped by this object.
     */
    private StandardCoderObject(JsonElement data) {
        this.data = data;
    }

    /**
     * Translates this to a JSON string.
     *
     * @return a JSON string representing this object
     * @throws CoderException if an error occurs
     */
    public String asString() throws CoderException {
        return coder.encode(data);
    }

    /**
     * Translates this to an object/POJO of the given class.
     *
     * @param clazz class of object/POJO to create
     * @return a new object/POJO
     * @throws CoderException if an error occurs
     */
    public <T> T asObject(Class<T> clazz) throws CoderException {
        try {
            return gson.fromJson(data, clazz);

        } catch (RuntimeException ex) {
            throw new CoderException(ex);
        }
    }

    /**
     * Creates a standard object from the given object/POJO.
     *
     * @param object object from which this should be populated
     * @throws CoderException if an error occurs
     */
    public static StandardCoderObject readFromObject(Object object) throws CoderException {
        try {
            return new StandardCoderObject(gson.toJsonTree(object));

        } catch (RuntimeException ex) {
            throw new CoderException(ex);
        }
    }

    /**
     * Reads a standard object from the source.
     *
     * @param source source from which to read the object
     * @return the standard object represented by the source
     * @throws CoderException if an error occurs
     */
    public static StandardCoderObject readFrom(Reader source) throws CoderException {
        return new StandardCoderObject(coder.decode(source, JsonElement.class));
    }

    /**
     * Reads a standard object from the source.
     *
     * @param source source from which to read the object
     * @return the standard object represented by the source
     * @throws CoderException if an error occurs
     */
    public static StandardCoderObject readFrom(InputStream source) throws CoderException {
        return new StandardCoderObject(coder.decode(source, JsonElement.class));
    }

    /**
     * Reads a standard object from the source.
     *
     * @param source source from which to read the object
     * @return the standard object represented by the source
     * @throws CoderException if an error occurs
     */
    public static StandardCoderObject readFrom(File source) throws CoderException {
        return new StandardCoderObject(coder.decode(source, JsonElement.class));
    }

    /**
     * Writes this object to a target.
     *
     * @param target target to which to write
     * @throws CoderException if an error occurs
     */
    public void writeTo(Writer target) throws CoderException {
        coder.encode(target, data);
    }

    /**
     * Writes this object to a target.
     *
     * @param target target to which to write
     * @throws CoderException if an error occurs
     */
    public void writeTo(OutputStream target) throws CoderException {
        coder.encode(target, data);
    }

    /**
     * Writes this object to a target.
     *
     * @param target target to which to write
     * @throws CoderException if an error occurs
     */
    public void writeTo(File target) throws CoderException {
        coder.encode(target, data);
    }

    /**
     * Gets a field's value from this object, traversing the object hierarchy.
     *
     * @param fields field hierarchy
     * @return the field value or {@code null} if the field does not exist or is not a
     *         primitive
     */
    public String getString(String... fields) {

        /*
         * This could be relatively easily modified to allow Integer arguments, as well,
         * which would be used to specify indices within an array.
         */

        JsonElement jel = data;

        for (String field : fields) {
            if (jel == null) {
                return null;
            }

            if (jel.isJsonObject()) {
                jel = jel.getAsJsonObject().get(field);

            } else {
                return null;
            }
        }

        return (jel != null && jel.isJsonPrimitive() ? jel.getAsString() : null);
    }
}
