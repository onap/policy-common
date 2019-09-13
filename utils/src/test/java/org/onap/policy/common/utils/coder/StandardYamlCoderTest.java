/*-
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

package org.onap.policy.common.utils.coder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.YamlJsonTranslatorTest.Container;

public class StandardYamlCoderTest {
    private static final File YAML_FILE =
                    new File("src/test/resources/org/onap/policy/common/utils/coder/YamlJsonTranslator.yaml");

    private StandardYamlCoder coder;
    private Container cont;

    @Before
    public void setUp() throws CoderException {
        coder = new StandardYamlCoder();
        cont = coder.decode(YAML_FILE, Container.class);
    }

    @Test
    public void testToJsonObject() throws CoderException {
        String yaml = coder.encode(cont);

        Container cont2 = coder.decode(yaml, Container.class);
        assertEquals(cont, cont2);
    }

    @Test
    public void testToJsonWriterObject() throws CoderException {
        StringWriter wtr = new StringWriter();
        coder.encode(wtr, cont);
        String yaml = wtr.toString();

        Container cont2 = coder.decode(yaml, Container.class);
        assertEquals(cont, cont2);
    }

    @Test
    public void testFromJsonStringClassOfT() throws Exception {
        String yaml = new String(Files.readAllBytes(YAML_FILE.toPath()), StandardCharsets.UTF_8);
        Container cont2 = coder.decode(yaml, Container.class);
        assertEquals(cont, cont2);
    }

    @Test
    public void testFromJsonReaderClassOfT() {
        YamlJsonTranslatorTest.verify(cont);
    }

    @Test
    public void testStandardTypeAdapter() throws Exception {
        String yaml = "abc: def\n";
        StandardCoderObject sco = coder.fromJson(yaml, StandardCoderObject.class);
        assertNotNull(sco.getData());
        assertEquals("{'abc':'def'}".replace('\'', '"'), sco.getData().toString());
        assertEquals(yaml, coder.toJson(sco));
    }
}
