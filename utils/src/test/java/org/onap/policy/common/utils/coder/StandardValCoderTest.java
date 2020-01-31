/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.utils.coder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.worldturner.medeia.api.ValidationFailedException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Before;
import org.junit.Test;

public class StandardValCoderTest {
    private String jsonSchema;
    private String validJson;
    private String missingReqJson;
    private String badRegexJson;

    @Data
    @NoArgsConstructor
    public static class ValOuter {
        @Data
        @NoArgsConstructor
        public static class ValInner {
            public String subItemString;
            public Integer subItemInteger;
        }

        public String aaString;
        public int anInteger;
        public boolean aaBoolean;
        public List<ValInner> aaCollection;
    }

    @Before
    public void testSetUp() throws Exception {
        jsonSchema = getJson("src/test/resources/org/onap/policy/common/utils/coder/test.schema.json");
        validJson = getJson("src/test/resources/org/onap/policy/common/utils/coder/valid.json");
        missingReqJson = getJson("src/test/resources/org/onap/policy/common/utils/coder/missing-required.json");
        badRegexJson = getJson("src/test/resources/org/onap/policy/common/utils/coder/bad-regex.json");
    }

    @Test
    public void testDecode() throws CoderException {
        StandardValCoder valCoder = new StandardValCoder(jsonSchema, "test-schema");

        ValOuter valOuter = valCoder.decode(validJson, ValOuter.class);
        assertValidJson(valOuter);

        StringReader reader = new StringReader(validJson);
        valOuter = valCoder.decode(reader, ValOuter.class);
        assertValidJson(valOuter);

        try {
            valCoder.decode(missingReqJson, ValOuter.class);
            fail("missing required field should have been flagged by the schema validation");
        } catch (CoderException e) {
            assertEquals("required", ((ValidationFailedException) e.getCause()).getFailures().get(0).getRule());
            assertEquals("aaCollection",
                ((ValidationFailedException) e.getCause()).getFailures().get(0).getProperty());
            assertEquals("Required property aaCollection is missing from object",
                ((ValidationFailedException) e.getCause()).getFailures().get(0).getMessage());
        }

        try {
            valCoder.decode(badRegexJson, ValOuter.class);
            fail("bad regex should have been flagged by the schema validation");
        } catch (CoderException e) {
            assertEquals("properties", ((ValidationFailedException) e.getCause()).getFailures().get(0).getRule());
            assertEquals("aaString",
                    ((ValidationFailedException) e.getCause()).getFailures().get(0).getProperty());
            assertEquals("Property validation failed",
                    ((ValidationFailedException) e.getCause()).getFailures().get(0).getMessage());
            assertEquals("pattern",
                    ((ValidationFailedException) e.getCause()).getFailures()
                            .get(0).getDetails().iterator().next().getRule());
            assertEquals("Pattern ^([a-z]*)$ is not contained in text",
                    ((ValidationFailedException) e.getCause()).getFailures()
                            .get(0).getDetails().iterator().next().getMessage());
        }
    }

    @Test
    public void testEncode() throws CoderException {
        StandardValCoder valCoder = new StandardValCoder(jsonSchema, "test-schema");
        ValOuter valOuter = valCoder.decode(validJson, ValOuter.class);

        String valOuterJson = valCoder.encode(valOuter);
        assertEquals(valOuter, valCoder.decode(valOuterJson, ValOuter.class));
        assertValidJson(valOuter);

        StringWriter writer = new StringWriter();
        valCoder.encode(writer, valOuter);
        assertEquals(valOuterJson, writer.toString());
    }

    @Test
    public void testPretty() throws CoderException {
        StandardValCoder valCoder = new StandardValCoder(jsonSchema, "test-schema");
        ValOuter valOuter = valCoder.decode(validJson, ValOuter.class);

        String valOuterJson = valCoder.encode(valOuter);
        String prettyValOuterJson = valCoder.pretty(valOuter);
        assertNotEquals(valOuterJson, prettyValOuterJson);

        assertEquals(valOuter, valCoder.decode(prettyValOuterJson, ValOuter.class));
    }

    @Test
    public void testConformance() {
        StandardValCoder valCoder = new StandardValCoder(jsonSchema, "test-schema");
        assertTrue(valCoder.isConformant(validJson));
        assertFalse(valCoder.isConformant(missingReqJson));
        assertFalse(valCoder.isConformant(badRegexJson));
    }

    private void assertValidJson(ValOuter valOuter) {
        assertEquals("abcd", valOuter.getAaString());
        assertEquals(90, valOuter.getAnInteger());
        assertTrue(valOuter.isAaBoolean());
        assertEquals("defg", valOuter.getAaCollection().get(0).getSubItemString());
        assertEquals(Integer.valueOf(1200), valOuter.getAaCollection().get(0).getSubItemInteger());
    }

    private String getJson(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}