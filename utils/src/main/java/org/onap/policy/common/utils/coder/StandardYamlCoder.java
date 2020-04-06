/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import java.io.Reader;
import java.io.Writer;

/**
 * YAML encoder and decoder using the "standard" mechanism, which is currently gson. All
 * of the methods perform conversion to/from YAML (instead of JSON).
 */
public class StandardYamlCoder extends StandardCoder {
    private final YamlJsonTranslator translator;

    /**
     * Constructs the object.
     */
    public StandardYamlCoder() {
        translator = new YamlJsonTranslator(gson);
    }

    @Override
    protected String toPrettyJson(Object object) {
        // YAML is already "pretty"
        return toJson(object);
    }

    @Override
    protected String toJson(Object object) {
        return translator.toYaml(object);
    }

    @Override
    protected void toJson(Writer target, Object object) {
        translator.toYaml(target, object);
    }

    @Override
    protected <T> T fromJson(String yaml, Class<T> clazz) {
        return translator.fromYaml(yaml, clazz);
    }

    @Override
    protected <T> T fromJson(Reader source, Class<T> clazz) {
        return translator.fromYaml(source, clazz);
    }
}
