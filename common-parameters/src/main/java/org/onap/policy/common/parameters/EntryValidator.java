/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Validator of an entry within a Map.
 */
public class EntryValidator {
    private final ItemValidator keyValidator;
    private final ItemValidator valueValidator;

    /**
     * Constructs the object.
     *
     * @param validator provider of validation methods
     * @param keyAnnotationContainer an annotation containing validation annotations to be
     *        applied to the entry key
     * @param valueAnnotationContainer an annotation containing validation annotations to
     *        be applied to the entry value
     */
    public EntryValidator(BeanValidator validator, Annotation keyAnnotationContainer,
                    Annotation valueAnnotationContainer) {
        keyValidator = new ItemValidator(validator, keyAnnotationContainer);
        valueValidator = new ItemValidator(validator, valueAnnotationContainer);
    }

    public boolean isEmpty() {
        return (keyValidator.isEmpty() && valueValidator.isEmpty());
    }

    /**
     * Performs validation of a single entry.
     *
     * @param result validation results are added here
     * @param entry value to be validated
     */
    public <K, V> void validateEntry(BeanValidationResult result, Map.Entry<K, V> entry) {
        String name = getName(entry);

        BeanValidationResult result2 = new BeanValidationResult(name, entry);
        keyValidator.validateValue(result2, "key", entry.getKey());
        valueValidator.validateValue(result2, "value", entry.getValue());

        if (!result2.isClean()) {
            result.addResult(result2);
        }
    }

    /**
     * Gets a name for the entry.
     *
     * @param entry entry whose name is to be determined
     * @return a name for the entry
     */
    protected <K, V> String getName(Map.Entry<K, V> entry) {
        K key = entry.getKey();
        if (key == null) {
            return "";
        }

        return key.toString();
    }
}
