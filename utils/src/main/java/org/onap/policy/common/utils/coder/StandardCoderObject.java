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

package org.onap.policy.common.utils.coder;

import com.google.gson.JsonElement;

/**
 * Object type used by the {@link StandardCoder}. Different serialization tools have
 * different "standard objects". For instance, GSON uses {@link JsonElement}. This class
 * wraps that object so that it can be used without exposing the object, itself.
 */
public class StandardCoderObject {

    /**
     * Data wrapped by this.
     */
    private final JsonElement data;

    /**
     * Constructs the object.
     */
    public StandardCoderObject() {
        data = null;
    }

    /**
     * Constructs the object.
     *
     * @param data data wrapped by this object.
     */
    protected StandardCoderObject(JsonElement data) {
        this.data = data;
    }

    /**
     * Gets the data wrapped by this.
     *
     * @return the data wrapped by this
     */
    protected JsonElement getData() {
        return data;
    }

    /**
     * Gets a field's value from this object, traversing the object hierarchy.
     *
     * @param fields field hierarchy
     * @return the field value or {@code null} if the field does not exist or is not a
     *         primitive
     */
    public String getString(String... fields) {

        /*
         * This could be relatively easily modified to allow Integer arguments, as well,
         * which would be used to specify indices within an array.
         */

        JsonElement jel = data;

        for (String field : fields) {
            if (jel == null) {
                return null;
            }

            if (jel.isJsonObject()) {
                jel = jel.getAsJsonObject().get(field);

            } else {
                return null;
            }
        }

        return (jel != null && jel.isJsonPrimitive() ? jel.getAsString() : null);
    }
}
