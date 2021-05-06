/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.http.server;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Provider that serializes and de-serializes YAML via snakeyaml and gson.
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class YamlMessageBodyHandler implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    public static final Logger logger = LoggerFactory.getLogger(YamlMessageBodyHandler.class);

    public static final String APPLICATION_YAML = "application/yaml";

    /**
     * Translator that's used when none is specified. We want a GSON object that's
     * configured the same way as it is in {@link GsonMessageBodyHandler}, so just get it
     * from there.
     */
    private static final YamlJsonTranslator DEFAULT_TRANSLATOR =
                    new YamlJsonTranslator(GsonMessageBodyHandler.configBuilder(new GsonBuilder()).create());

    private final YamlJsonTranslator translator;

    /**
     * Constructs the object.
     */
    public YamlMessageBodyHandler() {
        this(DEFAULT_TRANSLATOR);
    }

    /**
     * Constructs the object.
     *
     * @param translator translator to use to translate to/from YAML
     */
    public YamlMessageBodyHandler(YamlJsonTranslator translator) {
        logger.info("Accepting YAML for REST calls");
        this.translator = translator;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return canHandle(mediaType);
    }

    @Override
    public long getSize(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

        try (var writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
            translator.toYaml(writer, object);
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return canHandle(mediaType);
    }

    /**
     * Determines if this provider can handle the given media type.
     *
     * @param mediaType the media type of interest
     * @return {@code true} if this provider handles the given media type, {@code false}
     *         otherwise
     */
    private boolean canHandle(MediaType mediaType) {
        return (mediaType != null && "yaml".equalsIgnoreCase(mediaType.getSubtype()));
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                    MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {

        try (var streamReader = new InputStreamReader(entityStream, StandardCharsets.UTF_8)) {
            Class<?> clazz = (Class<?>) genericType;
            return translator.fromYaml(streamReader, clazz);

        } catch (JsonSyntaxException e) {
            throw new YAMLException(e);
        }
    }
}
