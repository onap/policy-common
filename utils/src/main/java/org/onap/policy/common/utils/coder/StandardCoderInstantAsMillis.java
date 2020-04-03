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

package org.onap.policy.common.utils.coder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.Instant;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.gson.InstantAsMillisTypeAdapter;

/**
 * JSON encoder and decoder using the "standard" mechanism, but encodes Instant fields as
 * Long milliseconds.
 */
public class StandardCoderInstantAsMillis extends StandardCoder {

    /**
     * Gson object used to encode and decode messages.
     */
    private static final Gson GSON_INSTANT;

    /**
     * Gson object used to encode messages in "pretty" format.
     */
    private static final Gson GSON_INSTANT_PRETTY;

    static {
        GsonBuilder builder = GsonMessageBodyHandler
                        .configBuilder(new GsonBuilder().registerTypeAdapter(StandardCoderObject.class,
                                        new StandardTypeAdapter()))
                        .registerTypeAdapter(Instant.class, new InstantAsMillisTypeAdapter());

        GSON_INSTANT = builder.create();
        GSON_INSTANT_PRETTY = builder.setPrettyPrinting().create();
    }

    /**
     * Constructs the object.
     */
    public StandardCoderInstantAsMillis() {
        super(GSON_INSTANT, GSON_INSTANT_PRETTY);
    }
}
