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

import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import org.onap.policy.common.utils.security.CryptoUtils;

/**
 * JSON encoder and decoder using the "property" mechanism, which is currently gson
 * any given object instance is not multi-thread safe.
 *
 */
public class PropertyCoder {
    /**
     * Gson object used to encode and decode messages.
     */
    @Getter(AccessLevel.PROTECTED)
    private static Gson GSON = new Gson();
    private static String AES_ENCRYPTION_KEY = "aes_encryption_key";

    /**
     * Decode json for encrypted password.
     *
     * @param json string
     * @param clazz class T object
     * @return a class T object
     */
    public <T> T decode(String json, Class<T> clazz) throws GeneralSecurityException {
        JsonElement jsonElement = GSON.fromJson(json, JsonElement.class);
        return new MyDecoder(jsonElement).decrypt(jsonElement, clazz);
    }

    private static class MyDecoder extends StandardCoder {
        private CryptoUtils crypto = null;

        MyDecoder(JsonElement jsonElement) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (!jsonObject.isJsonObject()) {
                return;
            }
            String secretKey = jsonObject.get(AES_ENCRYPTION_KEY).getAsString();
            if (secretKey == null) {
                secretKey = System.getenv(AES_ENCRYPTION_KEY);
            }
            if (secretKey != null) {
                crypto = new CryptoUtils(secretKey);
            }
        }

        private <T> T decrypt(JsonElement jsonElement, Class<T> clazz) throws GeneralSecurityException {
            JsonElement newElement = decrypt(jsonElement);
            return fromJson(newElement, clazz);
        }
        
        private JsonElement decrypt(JsonElement jsonElement) throws GeneralSecurityException {
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
            value = crypto.decrypt(value);
            return new JsonPrimitive(value);
        }

        private JsonArray decryptArray(JsonArray jsonArray) throws GeneralSecurityException {
            JsonArray newArray = new JsonArray();
            for (JsonElement element: jsonArray) {
                newArray.add(decrypt(element));
            }
            return newArray;
        }

        private JsonObject decryptObject(JsonObject jsonObject) throws GeneralSecurityException {
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