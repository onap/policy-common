/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
import java.io.Reader;
import java.io.Writer;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.gson.InstantAsMillisTypeAdapter;

/**
 * JSON encoder and decoder using the "standard" mechanism, but encodes Instant fields as
 * Long milliseconds.
 */
public class StandardCoderInstantAsMillis extends StandardCoder {

    /**
     * Gson object used to encode and decode messages.
     */
    @Getter(AccessLevel.PROTECTED)
    private static final Gson GSON;

    /**
     * Gson object used to encode messages in "pretty" format.
     */
    @Getter(AccessLevel.PROTECTED)
    private static final Gson GSON_PRETTY;

    static {
        GsonBuilder builder = GsonMessageBodyHandler
                        .configBuilder(new GsonBuilder().registerTypeAdapter(StandardCoderObject.class,
                                        new StandardTypeAdapter()))
                        .registerTypeAdapter(Instant.class, new InstantAsMillisTypeAdapter());

        GSON = builder.create();
        GSON_PRETTY = builder.setPrettyPrinting().create();
    }

    /**
     * Constructs the object.
     */
    public StandardCoderInstantAsMillis() {
        super();
    }

    @Override
    protected String toPrettyJson(Object object) {
        return GSON_PRETTY.toJson(object);
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

    @Override
    protected JsonElement toJsonTree(Object object) {
        return GSON.toJsonTree(object);
    }

    @Override
    protected String toJson(Object object) {
        return GSON.toJson(object);
    }

    @Override
    protected void toJson(Writer target, Object object) {
        GSON.toJson(object, object.getClass(), target);
    }

    @Override
    protected <T> T fromJson(JsonElement json, Class<T> clazz) {
        return convertFromDouble(clazz, GSON.fromJson(json, clazz));
    }

    @Override
    protected <T> T fromJson(String json, Class<T> clazz) {
        return convertFromDouble(clazz, GSON.fromJson(json, clazz));
    }

    @Override
    protected <T> T fromJson(Reader source, Class<T> clazz) {
        return convertFromDouble(clazz, GSON.fromJson(source, clazz));
    }
}
