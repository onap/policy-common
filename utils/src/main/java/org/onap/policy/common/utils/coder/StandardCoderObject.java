/*
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.Serializable;

/**
 * Object type used by the {@link StandardCoder}. Different serialization tools have
 * different "standard objects". For instance, GSON uses {@link JsonElement}. This class
 * wraps that object so that it can be used without exposing the object, itself.
 */
public class StandardCoderObject implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * @param fields field hierarchy. These may be strings, identifying fields within the
     *        object, or Integers, identifying an index within an array
     * @return the field value or {@code null} if the field does not exist or is not a
     *         primitive
     */
    public String getString(Object... fields) {

        JsonElement jel = data;

        for (Object field : fields) {
            if (jel == null) {
                return null;
            }

            if (field instanceof String) {
                jel = getFieldFromObject(jel, field.toString());

            } else if (field instanceof Integer) {
                jel = getItemFromArray(jel, (int) field);

            } else {
                throw new IllegalArgumentException("subscript is not a string or integer: " + field);
            }
        }

        return (jel != null && jel.isJsonPrimitive() ? jel.getAsString() : null);
    }

    /**
     * Gets an item from an object.
     * @param element object from which to extract the item
     * @param field name of the field from which to extract the item
     * @return the item, or {@code null} if the element is not an object or if the index is out of bounds
     */
    protected JsonElement getFieldFromObject(JsonElement element, String field) {
        if (!element.isJsonObject()) {
            return null;
        }

        return element.getAsJsonObject().get(field);
    }

    /**
     * Gets an item from an array.
     * @param element array from which to extract the item
     * @param index index of the item to extract
     * @return the item, or {@code null} if the element is not an array or if the index is out of bounds
     */
    protected JsonElement getItemFromArray(JsonElement element, int index) {
        if (!element.isJsonArray()) {
            return null;
        }

        JsonArray array = element.getAsJsonArray();

        if (index < 0) {
            throw new IllegalArgumentException("subscript is invalid: " + index);
        }

        if (index >= array.size()) {
            return null;
        }

        return array.get(index);
    }
}
