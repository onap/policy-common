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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class StandardCoderTest {

    private StandardCoder coder;

    @Before
    public void setUp() {
        coder = new StandardCoder();
    }

    @Test
    public void testEncode() throws Exception {
        List<Integer> arr = Arrays.asList(100, 110);
        assertEquals("[100,110]", coder.encode(arr));

        // test exception case
        JsonParseException jpe = new JsonParseException("expected exception");

        coder = spy(coder);
        when(coder.toJson(arr)).thenThrow(jpe);

        assertThatThrownBy(() -> coder.encode(arr)).isInstanceOf(CoderException.class).hasCause(jpe);
    }

    @Test
    public void testDecode() throws Exception {
        String text = "[200,210]";
        assertEquals(text, coder.decode(text, JsonElement.class).toString());

        // test exception case
        JsonParseException jpe = new JsonParseException("expected exception");

        coder = spy(coder);
        when(coder.fromJson(text, JsonElement.class)).thenThrow(jpe);

        assertThatThrownBy(() -> coder.decode(text, JsonElement.class)).isInstanceOf(CoderException.class)
                        .hasCause(jpe);
    }

}
