/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.onap.policy.common.parameters.testclasses.TestParametersL00;
import org.yaml.snakeyaml.Yaml;

public class TestYamlInput {
    @Test
    public void testYamlInput() throws IOException {
        TestParametersL00 testParameterGroup = null;

        // Read the parameters from JSON using Gson
        final Yaml yaml = new Yaml();
        testParameterGroup = yaml.loadAs(new FileReader("src/test/resources/parameters/TestParameters.yaml"),
                        TestParametersL00.class);

        GroupValidationResult validationResult = testParameterGroup.validate();
        assertTrue(validationResult.isValid());
        assertThat(new BeanValidator2().validate(testParameterGroup)).isNull();
        assertEquals("l00NameFromFile", testParameterGroup.getName());

        String expectedResult = new String(Files.readAllBytes(
                        Paths.get("src/test/resources/expectedValidationResults/TestJsonYamlValidationResult.txt")))
                                        .replaceAll("\\s+", "");
        assertEquals(expectedResult, validationResult.getResult("", "  ", true).replaceAll("\\s+", ""));
    }
}
