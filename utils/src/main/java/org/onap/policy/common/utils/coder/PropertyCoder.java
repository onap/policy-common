/*
 * ============LICENSE_START=======================================================
 * ONAP PAP
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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import org.onap.policy.common.gson.DoubleConverter;
import org.onap.policy.common.gson.MapDoubleAdapterFactory;
import org.onap.policy.common.utils.security.CryptoUtils;

/**
 * JSON encoder and decoder using the "property" mechanism, which is currently gson
 * any given object instance is not multi-thread safe.
 * 
 */
public class PropertyCoder extends StandardCoderObject {
    /**
     * Gson object used to encode and decode messages.
     */
    @Getter(AccessLevel.PROTECTED)
    private static final Gson GSON =
                    new GsonBuilder().registerTypeAdapter(StandardCoderObject.class, new StandardTypeAdapter())
                                     .registerTypeAdapterFactory(new MapDoubleAdapterFactory()).create();
    private static String AES_ENCRYPTION_KEY = "aes_encryption_key";

    /**
     * Constructs the object.
     */
    public PropertyCoder() {
        super();
    }
    
    /**
     * Makes a writer for the given file.
     *
     * @param target file of interest
     * @return a writer for the file
     * @throws FileNotFoundException if the file cannot be created
     */
    protected Writer makeWriter(File target) throws FileNotFoundException {
        return makeWriter(new FileOutputStream(target));
    }

    /**
     * Makes a writer for the given stream.
     *
     * @param target stream of interest
     * @return a writer for the stream
     */
    protected Writer makeWriter(OutputStream target) {
        return new OutputStreamWriter(target, StandardCharsets.UTF_8);
    }

    /**
     * Makes a reader for the given file.
     *
     * @param source file of interest
     * @return a reader for the file
     * @throws FileNotFoundException if the file does not exist
     */
    protected Reader makeReader(File source) throws FileNotFoundException {
        return makeReader(new FileInputStream(source));
    }

    /**
     * Makes a reader for the given stream.
     *
     * @param source stream of interest
     * @return a reader for the stream
     */
    protected Reader makeReader(InputStream source) {
        return new InputStreamReader(source, StandardCharsets.UTF_8);
    }

    /**
     * Encodes an object into a json tree, without catching exceptions.
     *
     * @param object object to be encoded
     * @return a json element representing the object
     */
    protected JsonElement toJsonTree(Object object) {
        return GSON.toJsonTree(object);
    }

    /**
     * Encodes an object into json, without catching exceptions.
     *
     * @param object object to be encoded
     * @return a json string representing the object
     */
    protected String toJson(Object object) {
        return GSON.toJson(object);
    }

    /**
     * Encodes an object into json, without catching exceptions.
     *
     * @param target target to which to write the encoded json
     * @param object object to be encoded
     * @throws IOException if an I/O error occurs
     */
    protected void toJson(Writer target, Object object) throws IOException {
        GSON.toJson(object, object.getClass(), target);
    }

    /**
     * Decodes a json element into an object, without catching exceptions.
     *
     * @param json json element to be decoded
     * @param clazz class of object to be decoded
     * @return the object represented by the given json element
     */
    protected <T> T fromJson(JsonElement json, Class<T> clazz) {
        return convertFromDouble(clazz, GSON.fromJson(json, clazz));
    }

    /**
     * Decodes a json string into an object, without catching exceptions.
     *
     * @param json json string to be decoded
     * @param clazz class of object to be decoded
     * @return the object represented by the given json string
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        return convertFromDouble(clazz, GSON.fromJson(json, clazz));
    }

    /**
     * Decodes a json string into an object, without catching exceptions.
     *
     * @param source source from which to read the json string to be decoded
     * @param clazz class of object to be decoded
     * @return the object represented by the given json string
     */
    protected <T> T fromJson(Reader source, Class<T> clazz) {
        return convertFromDouble(clazz, GSON.fromJson(source, clazz));
    }

    /**
     * Converts a value from Double to Integer/Long, walking the value's contents if it's
     * a List/Map. Only applies if the specified class refers to the Object class.
     * Otherwise, it leaves the value unchanged.
     *
     * @param clazz class of object to be decoded
     * @param value value to be converted
     * @return the converted value
     */
    private <T> T convertFromDouble(Class<T> clazz, T value) {
        if (clazz != Object.class) {
            return value;
        }

        return clazz.cast(DoubleConverter.convertFromDouble(value));
    }
    
    /**
     * Adapter for Standar objects.
     */
    private static class StandardTypeAdapter extends TypeAdapter<StandardCoderObject> {

        /**
         * Used to read/write a JsonElement.
         */
        private static TypeAdapter<JsonElement> elementAdapter = new Gson().getAdapter(JsonElement.class);

        /**
         * Constructs the object.
         */
        public StandardTypeAdapter() {
            super();
        }

        @Override
        public void write(JsonWriter out, StandardCoderObject value) throws IOException {
            elementAdapter.write(out, value.getData());
        }

        @Override
        public StandardCoderObject read(JsonReader in) throws IOException {
            return new StandardCoderObject(elementAdapter.read(in));
        }
        
        /**
         * Decodes a json element into an object, 
         * throw exceptions if encrypted password but crypto is null.
         *
         * @param json json element to be decoded
         * @param clazz class of object to be decoded
         * @return the object represented by the given json element
         */
        protected <T> T fromJson(JsonElement json, Class<T> clazz) throws Exception {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonObject jsonObjectNew = new JsonObject();
            String secretKey = jsonObject.get(AES_ENCRYPTION_KEY).getAsString();
            CryptoUtils cryptoUtils = null;
            try {
                if (secretKey != null) {
                    cryptoUtils = new CryptoUtils(secretKey);
                }
                Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                for (Map.Entry<String, JsonElement> entry : entrySet) {
                    String name = entry.getKey();
                    String value = jsonObject.get(entry.getKey()).getAsString();
                    if (!value.startsWith("enc:")) {
                        continue;
                    }
                    if (cryptoUtils != null) {
                        value = cryptoUtils.decrypt(value);
                    } else {
                        throw new Exception("Exception: encrypted password cannot be decrypted.");
                    }
                    jsonObjectNew.addProperty(name, value);
                }
            } catch (GeneralSecurityException e) {
                throw new Exception("Exception: " + e.toString());
            }
            Gson gson = new Gson();
            return gson.fromJson(jsonObjectNew, clazz);
        }
        
        /**
         * Decodes a json string into an object.
         *
         * @param json json string to be decoded
         * @param clazz class of object to be decoded
         * @return the object represented by the given json string
         */
        protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
            Gson gson = new Gson();
            JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
            return fromJson(jsonElement, clazz);
        }
        
        /**
         * Decodes a json string into an object.
         *
         * @param source source from which to read the json string to be decoded
         * @param clazz class of object to be decoded
         * @return the object represented by the given json string
         */
        protected <T> T fromJson(Reader source, Class<T> clazz) throws Exception {
            Gson gson = new Gson();
            JsonElement jsonElement = gson.fromJson(source, JsonElement.class);
            return fromJson(jsonElement, clazz);
        }
    }
}