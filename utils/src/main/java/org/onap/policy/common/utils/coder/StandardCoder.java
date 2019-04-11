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
import org.onap.policy.common.gson.MapDoubleAdapterFactory;

/**
 * JSON encoder and decoder using the "standard" mechanism, which is currently gson.
 */
public class StandardCoder implements Coder {

    /**
     * Gson object used to encode and decode messages.
     */
    private static final Gson GSON =
                    new GsonBuilder().registerTypeAdapter(StandardCoderObject.class, new StandardTypeAdapter())
                                    .registerTypeAdapterFactory(new MapDoubleAdapterFactory()).create();

    /**
     * Constructs the object.
     */
    public StandardCoder() {
        super();
    }

    @Override
    public String encode(Object object) throws CoderException {
        try {
            return toJson(object);

        } catch (RuntimeException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public void encode(Writer target, Object object) throws CoderException {
        try {
            toJson(target, object);

        } catch (RuntimeException | IOException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public void encode(OutputStream target, Object object) throws CoderException {
        try {
            Writer wtr = makeWriter(target);
            toJson(wtr, object);

            // flush, but don't close
            wtr.flush();

        } catch (RuntimeException | IOException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public void encode(File target, Object object) throws CoderException {
        try (Writer wtr = makeWriter(target)) {
            toJson(wtr, object);

            // no need to flush or close here

        } catch (RuntimeException | IOException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public <T> T decode(String json, Class<T> clazz) throws CoderException {
        try {
            return fromJson(json, clazz);

        } catch (RuntimeException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public <T> T decode(Reader source, Class<T> clazz) throws CoderException {
        try {
            return fromJson(source, clazz);

        } catch (RuntimeException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public <T> T decode(InputStream source, Class<T> clazz) throws CoderException {
        try {
            return fromJson(makeReader(source), clazz);

        } catch (RuntimeException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public <T> T decode(File source, Class<T> clazz) throws CoderException {
        try (Reader input = makeReader(source)) {
            return fromJson(input, clazz);

        } catch (RuntimeException | IOException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public StandardCoderObject toStandard(Object object) throws CoderException {
        try {
            return new StandardCoderObject(GSON.toJsonTree(object));

        } catch (RuntimeException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public <T> T fromStandard(StandardCoderObject sco, Class<T> clazz) throws CoderException {
        try {
            return GSON.fromJson(sco.getData(), clazz);

        } catch (RuntimeException e) {
            throw new CoderException(e);
        }
    }

    // the remaining methods are wrappers that can be overridden by junit tests

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
     * Decodes a json string into an object, without catching exceptions.
     *
     * @param json json string to be decoded
     * @param clazz class of object to be decoded
     * @return the object represented by the given json string
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    /**
     * Decodes a json string into an object, without catching exceptions.
     *
     * @param source source from which to read the json string to be decoded
     * @param clazz class of object to be decoded
     * @return the object represented by the given json string
     */
    protected <T> T fromJson(Reader source, Class<T> clazz) {
        return GSON.fromJson(source, clazz);
    }

    /**
     * Adapter for standard objects.
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
    }
}
