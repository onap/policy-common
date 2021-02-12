/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.gson;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * GSON Type Adapter for "ZonedDateTime" fields, that uses the standard
 * ISO_ZONED_DATE_TIME formatter.
 */
public class ZonedDateTimeTypeAdapter extends TypeAdapter<ZonedDateTime> {
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    private final DateTimeFormatter formatter;


    /**
     * Constructs an adapter that uses the ISO_ZONED_DATE_TIME formatter.
     */
    public ZonedDateTimeTypeAdapter() {
        this(DEFAULT_FORMATTER);
    }

    /**
     * Constructs an adapter that uses the specified formatter for reading and writing.
     * @param formatter date-time formatter
     */
    public ZonedDateTimeTypeAdapter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public ZonedDateTime read(JsonReader in) throws IOException {
        try {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else {
                return ZonedDateTime.parse(in.nextString(), formatter);
            }

        } catch (DateTimeParseException e) {
            throw new JsonParseException("invalid date", e);
        }
    }

    @Override
    public void write(JsonWriter out, ZonedDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            String text = value.format(formatter);
            out.value(text);
        }
    }
}
