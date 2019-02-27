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
import com.google.gson.JsonIOException;
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

/**
 * JSON encoder and decoder using the "standard" mechanism, which is currently gson.
 */
public class StandardCoder implements Coder {

    /**
     * Gson object used to encode and decode messages.
     */
    private static final Gson GSON = new Gson();

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
    public void encode(Writer writer, Object object) throws CoderException {
        try {
            toJson(writer, object);

        } catch (RuntimeException | IOException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public void encode(OutputStream stream, Object object) throws CoderException {
        OutputStreamWriter wtr = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        encode(wtr, object);

        try {
            wtr.flush();

        } catch (IOException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public void encode(File file, Object object) throws CoderException {
        try (OutputStream stream = makeOutputStream(file)) {
            encode(stream, object);

        } catch (IOException e) {
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
    public <T> T decode(Reader jsonStream, Class<T> clazz) throws CoderException {
        try {
            return fromJson(jsonStream, clazz);

        } catch (RuntimeException e) {
            throw new CoderException(e);
        }
    }

    @Override
    public <T> T decode(InputStream jsonStream, Class<T> clazz) throws CoderException {
        return decode(new InputStreamReader(jsonStream, StandardCharsets.UTF_8), clazz);
    }

    @Override
    public <T> T decode(File jsonFile, Class<T> clazz) throws CoderException {
        try (FileInputStream input = new FileInputStream(jsonFile)) {
            return decode(input, clazz);

        } catch (IOException e) {
            throw new CoderException(e);
        }
    }

    // the remaining methods are wrappers that can be overridden by junit tests

    /**
     * Makes an output stream for the given file.
     *
     * @param file file of interest
     * @return an output stream to write to the file
     * @throws FileNotFoundException if the file cannot be created
     */
    protected OutputStream makeOutputStream(File file) throws FileNotFoundException {
        return new FileOutputStream(file);
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
     * @param writer target to which to write the encoded json
     * @param object object to be encoded
     * @throws IOException if an I/O error occurs
     * @throws JsonIOException if a JSON I/O error occurs
     */
    protected void toJson(Writer wtr, Object object) throws JsonIOException, IOException {
        GSON.toJson(object, object.getClass(), GSON.newJsonWriter(wtr));
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
     * @param jsonReader source from which to read the json string to be decoded
     * @param clazz class of object to be decoded
     * @return the object represented by the given json string
     */
    protected <T> T fromJson(Reader jsonReader, Class<T> clazz) {
        return GSON.fromJson(jsonReader, clazz);
    }
}
