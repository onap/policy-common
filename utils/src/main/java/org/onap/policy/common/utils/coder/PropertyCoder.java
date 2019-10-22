/*
 * ============LICENSE_START=======================================================
 * ONAP PAP
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
import org.onap.policy.common.utils.security.CryptoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON encoder and decoder using the "property" mechanism, which is currently gson.
 *
 */
public class PropertyCoder {
    public static final Logger logger = LoggerFactory.getLogger(PropertyCoder.class);
    /**
     * Gson object used to encode and decode messages.
     */
    @Getter(AccessLevel.PROTECTED)
    private static Gson GSON = new Gson();

    /**
     * Decode json for encrypted password.
     *
     * @param json string
     * @param clazz class T object
     * @return a class T object
     */
    public <T> T decode(String json, String secretKey, Class<T> clazz) throws CoderException {
        JsonElement jsonElement = GSON.fromJson(json, JsonElement.class);
        return new MyDecoder(jsonElement, secretKey).decrypt(jsonElement, clazz);
    }

    public <T> T decode(Reader reader, String secretKey, Class<T> clazz) throws CoderException {
        JsonElement jsonElement = GSON.fromJson(reader, JsonElement.class);
        return new MyDecoder(jsonElement, secretKey).decrypt(jsonElement, clazz);
    }

    private static class MyDecoder extends StandardCoder {
        private CryptoUtils crypto = null;

        MyDecoder(JsonElement jsonElement, String secretKey) throws CoderException {
            if (!jsonElement.getAsJsonObject().isJsonObject()) {
                return;
            }
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            // Use key from input first, then from jsonObject
            if (!StringUtils.isBlank(secretKey)) {
                crypto = new CryptoUtils(secretKey);
            }
        }

        private <T> T decrypt(JsonElement jsonElement, Class<T> clazz) {
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
            String value = jsonElement.getAsString();
            if (!value.startsWith("enc:")) {
                return jsonElement;
            }
            if (crypto != null) {
                value = crypto.decrypt(value);
            }
            return new JsonPrimitive(value);
        }

        private JsonArray decryptArray(JsonArray jsonArray) {
            JsonArray newArray = new JsonArray();
            for (JsonElement element: jsonArray) {
                newArray.add(decrypt(element));
            }
            return newArray;
        }

        private JsonObject decryptObject(JsonObject jsonObject) {
            JsonObject newObject = new JsonObject();
            Set<Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                String key = entry.getKey();
                JsonElement jsonElement = decrypt(entry.getValue());
                newObject.add(key, jsonElement);
            }
            return newObject;
        }
    }
}