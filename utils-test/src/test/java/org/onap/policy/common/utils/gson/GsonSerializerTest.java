/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.common.utils.gson;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class GsonSerializerTest {

    @Test
    void testReadJsonReader() {
        JsonReader rdr = new JsonReader(new StringReader("10"));

        GsonSerializer<Object> ser = new GsonSerializer<Object>() {
            @Override
            public void write(JsonWriter out, Object value) throws IOException {
                // do nothing
            }
        };

        assertThatThrownBy(() -> ser.read(rdr)).isInstanceOf(UnsupportedOperationException.class)
                        .hasMessage("read from pseudo TypeAdapter");
    }
}
