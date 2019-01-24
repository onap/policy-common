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

package org.onap.policy.common.endpoints.http.server.internal;

import com.google.gson.Gson;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Provider that serializes and de-serializes JSON via gson.
 * 
 * <p>Note: <i>jersey</i> will ignore this class if the maven artifact,
 * <i>jersey-media-json-jackson</i>, is included, regardless of whether it's included
 * directly or indirectly.
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class GsonMessageBodyHandler implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

    /**
     * Object to be used to serialize and de-serialize.
     */
    private Gson gson;

    /**
     * Constructs the object, using a plain Gson object.
     */
    public GsonMessageBodyHandler() {
        this(new Gson());
    }

    /**
     * Constructs the object.
     * 
     * @param gson the Gson object to be used to serialize and de-serialize
     */
    public GsonMessageBodyHandler(Gson gson) {
        this.gson = gson;
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
                    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                    throws IOException, WebApplicationException {

        try (OutputStreamWriter writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
            Type jsonType = (type.equals(genericType) ? type : genericType);
            gson.toJson(object, jsonType, writer);
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
        if (mediaType == null) {
            return true;
        }

        String subtype = mediaType.getSubtype();

        return "json".equalsIgnoreCase(subtype) || subtype.endsWith("+json") || "javascript".equals(subtype)
                        || "x-javascript".equals(subtype) || "x-json".equals(subtype);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                    MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
                    throws IOException, WebApplicationException {

        try (InputStreamReader streamReader = new InputStreamReader(entityStream, StandardCharsets.UTF_8)) {
            Type jsonType = (type.equals(genericType) ? type : genericType);
            return gson.fromJson(streamReader, jsonType);
        }
    }
}
