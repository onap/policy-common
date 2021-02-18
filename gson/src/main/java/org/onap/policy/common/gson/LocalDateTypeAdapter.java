/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {
    private DateTimeFormatter formatter;

    public LocalDateTypeAdapter() {
        this(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public LocalDateTypeAdapter(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        try {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else {
                return LocalDate.parse(in.nextString(), formatter);
            }

        } catch (DateTimeParseException e) {
            throw new JsonParseException("invalid date", e);
        }
    }

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            String text = value.format(formatter);
            out.value(text);
        }
    }
}
