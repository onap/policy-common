/*
 * ============LICENSE_START=======================================================
 * ONAP PAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.Reader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.utils.security.CryptoCoder;
import org.onap.policy.common.utils.security.CryptoUtils;

/**
 * JSON encoder and decoder using the "property" mechanism, which is currently gson.
 *
 */
public class PropertyCoder {
    /**
     * Gson object used to encode and decode messages.
     */
    @Getter(AccessLevel.PROTECTED)
    private static final Gson GSON = new Gson();

    /**
     * Decode json for encrypted password.
     *
     * @param json string
     * @param keyProperty contains property within jsonObject for secretKey
     * @param clazz class T object
     * @return a class T object
     */
    public <T> T decode(String json, String keyProperty, Class<T> clazz) {
        var jsonElement = GSON.fromJson(json, JsonElement.class);
        return new MyDecoder(jsonElement, keyProperty).decrypt(jsonElement, clazz);
    }

    public <T> T decode(Reader reader, String keyProperty, Class<T> clazz) {
        var jsonElement = GSON.fromJson(reader, JsonElement.class);
        return new MyDecoder(jsonElement, keyProperty).decrypt(jsonElement, clazz);
    }

    private static class MyDecoder extends StandardCoder {
        private CryptoCoder crypto = null;

        MyDecoder(JsonElement jsonElement, String keyProperty) {
            if (!jsonElement.isJsonObject()) {
                return;
            }
            var jsonObject = jsonElement.getAsJsonObject();
            // Use keyProperty from input to retrieve secretKey
            var secretKey = jsonObject.get(keyProperty).getAsString();
            if (!StringUtils.isBlank(secretKey)) {
                crypto = new CryptoUtils(secretKey);
            }
        }

        private <T> T decrypt(JsonElement jsonElement, Class<T> clazz) {
            if (crypto == null) {
                return fromJson(jsonElement, clazz);
            }
            JsonElement newElement = decrypt(jsonElement);
            return fromJson(newElement, clazz);
        }

        private JsonElement decrypt(JsonElement jsonElement) {
            if (jsonElement.isJsonObject()) {
                return decryptObject(jsonElement.getAsJsonObject());
            }
            if (jsonElement.isJsonArray()) {
                return decryptArray(jsonElement.getAsJsonArray());
            }
            if (!jsonElement.getAsJsonPrimitive().isString()) {
                return jsonElement;
            }
            var value = jsonElement.getAsString();
            if (!value.startsWith("enc:")) {
                return jsonElement;
            }
            if (crypto != null) {
                value = crypto.decrypt(value);
            }
            return new JsonPrimitive(value);
        }

        private JsonArray decryptArray(JsonArray jsonArray) {
            if (crypto == null) {
                return jsonArray;
            }
            var newArray = new JsonArray();
            for (JsonElement element: jsonArray) {
                newArray.add(decrypt(element));
            }
            return newArray;
        }

        private JsonObject decryptObject(JsonObject jsonObject) {
            if (crypto == null) {
                return jsonObject;
            }
            var newObject = new JsonObject();
            Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                String key = entry.getKey();
                var jsonElement = decrypt(entry.getValue());
                newObject.add(key, jsonElement);
            }
            return newObject;
        }
    }
}
