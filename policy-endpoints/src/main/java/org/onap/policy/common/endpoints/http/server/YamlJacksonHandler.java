/*
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

package org.onap.policy.common.endpoints.http.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import org.onap.policy.common.gson.JacksonHandler;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;

/**
 * Provider that serializes and de-serializes YAML via snakeyaml and gson using jackson
 * default behaviors and annotations.
 */
@Provider
@Consumes(MediaType.WILDCARD)
@Produces(MediaType.WILDCARD)
public class YamlJacksonHandler extends YamlMessageBodyHandler {

    /**
     * Translator to be used. We want a GSON object that's configured the same was as it
     * is in {@link JacksonHandler}, so just get it from there.
     */
    private static final YamlJsonTranslator TRANS = new YamlJsonTranslator(new JacksonHandler().getGson());

    /**
     * Constructs the object.
     */
    public YamlJacksonHandler() {
        super(TRANS);
    }
}
