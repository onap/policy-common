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

package org.onap.policy.common.endpoints.http.server;

import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
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
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Provider that serializes and de-serializes JSON via gson.
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class YamlMessageBodyHandler implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    public static final Logger logger = LoggerFactory.getLogger(YamlMessageBodyHandler.class);

    /**
     * Constructs the object.
     */
    public YamlMessageBodyHandler() {
        logger.info("Accepting YAML for REST calls");
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

        try (OutputStreamWriter writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
            new MyYamlCoder().encode(writer, object);

        } catch (CoderException e) {
            throw new IOException(e);
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

        try (InputStreamReader streamReader = new InputStreamReader(entityStream, StandardCharsets.UTF_8)) {
            Class<?> clazz = (Class<?>) genericType;
            return new MyYamlCoder().decode(streamReader, clazz);
        }
    }

    /**
     * Yaml coder that yields YAMLException on input so that the http servlet can identify
     * it and generate a bad-request status code. Only the {@link #decode(Reader, Class)}
     * method must be overridden.
     */
    private static class MyYamlCoder extends StandardYamlCoder {
        @Override
        public <T> T decode(Reader source, Class<T> clazz) {
            try {
                return fromJson(source, clazz);

            } catch (JsonSyntaxException e) {
                throw new YAMLException(e);
            }
        }
    }
}
