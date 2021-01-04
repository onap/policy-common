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

package org.onap.policy.common.parameters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.annotations.Items;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;

public class TestEntryValidator extends ValidatorUtil {

    // annotations for keys and values

    @Items()
    private int emptyAnnot;

    @Items(notBlank = {@NotBlank})
    private int keyAnnot;

    @Items(min = {@Min(5)})
    private int valueAnnot;


    @Before
    public void setUp() {
        bean = new BeanValidator();
    }

    @Test
    public void testIsEmpty() {
        // no annotations for key or value
        assertThat(new EntryValidator(bean, getAnnot("emptyAnnot"), getAnnot("emptyAnnot")).isEmpty()).isTrue();

        // annotations for key, value, or both
        assertThat(new EntryValidator(bean, getAnnot("keyAnnot"), getAnnot("emptyAnnot")).isEmpty()).isFalse();
        assertThat(new EntryValidator(bean, getAnnot("emptyAnnot"), getAnnot("valueAnnot")).isEmpty()).isFalse();
        assertThat(new EntryValidator(bean, getAnnot("keyAnnot"), getAnnot("valueAnnot")).isEmpty()).isFalse();
    }

    @Test
    public void testValidateEntry() {
        EntryValidator validator = new EntryValidator(bean, getAnnot("keyAnnot"), getAnnot("valueAnnot"));

        // valid key & value
        BeanValidationResult result = new BeanValidationResult(MY_NAME, this);
        validator.validateEntry(result, makeEntry(HELLO, 10));
        assertThat(result.getResult()).isNull();

        // invalid key
        result = new BeanValidationResult(MY_NAME, this);
        validator.validateEntry(result, makeEntry("", 20));
        assertThat(result.getResult()).doesNotContain("\"value\"").contains("\"key\"", "blank");

        // invalid value
        result = new BeanValidationResult(MY_NAME, this);
        validator.validateEntry(result, makeEntry(HELLO, -10));
        assertThat(result.getResult()).contains(HELLO, "\"value\"", "-10").doesNotContain("\"key\"");

        // both invalid
        result = new BeanValidationResult(MY_NAME, this);
        validator.validateEntry(result, makeEntry("", -100));
        assertThat(result.getResult()).contains("\"key\"", "blank", "\"value\"", "-100");
    }

    @Test
    public void testGetName() {
        EntryValidator validator = new EntryValidator(bean, getAnnot("emptyAnnot"), getAnnot("emptyAnnot"));
        assertThat(validator.getName(makeEntry(null, 0))).isEmpty();
        assertThat(validator.getName(makeEntry("", 0))).isEmpty();
        assertThat(validator.getName(makeEntry(HELLO, 0))).isEqualTo(HELLO);
    }

    /**
     * Makes a Map entry with the given key and value.
     *
     * @param key desired key
     * @param value desired value
     * @return a new Map entry
     */
    Map.Entry<String, Integer> makeEntry(String key, int value) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(key, value);
        return map.entrySet().iterator().next();
    }
}
