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

package org.onap.policy.common.gson;

import com.google.gson.GsonBuilder;

/**
 * Provider used to serialize and deserialize policy objects via gson using jackson
 * default behaviors and annotations.
 */
public class JacksonHandler extends GsonMessageBodyHandler {

    /**
     * Constructs the object.
     */
    public JacksonHandler() {
        this(new GsonBuilder());
    }

    /**
     * Constructs the object.
     * @param builder builder to use to create the gson object
     */
    public JacksonHandler(GsonBuilder builder) {
        super(builder
                        .registerTypeAdapterFactory(new JacksonFieldAdapterFactory())
                        .registerTypeAdapterFactory(new JacksonMethodAdapterFactory())
                        .setExclusionStrategies(new JacksonExclusionStrategy())
                        .create());
    }

}
