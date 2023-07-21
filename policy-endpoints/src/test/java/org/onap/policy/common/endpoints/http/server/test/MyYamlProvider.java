/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.common.endpoints.http.server.test;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import lombok.AccessLevel;
import lombok.Setter;
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;

/**
 * YamlMessageBodyHandler that tracks activities.
 */
public class MyYamlProvider extends YamlMessageBodyHandler {

    @Setter(AccessLevel.PRIVATE)
    private static boolean readSome = false;

    @Setter(AccessLevel.PRIVATE)
    private static boolean wroteSome = false;

    /**
     * Constructs the object and resets the variables to indicate that no activity has
     * occurred yet.
     */
    public MyYamlProvider() {
        super();
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                    MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {

        setReadSome(true);
        return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public void writeTo(Object object, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

        setWroteSome(true);
        super.writeTo(object, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    public static boolean hasReadSome() {
        return readSome;
    }

    public static boolean hasWrittenSome() {
        return wroteSome;
    }

    public static void resetSome() {
        MyYamlProvider.readSome = false;
        MyYamlProvider.wroteSome = false;
    }

}
