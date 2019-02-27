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

/**
 * JSON encoder and decoder.
 */
public interface Coder {

    /**
     * Encodes an object into json.
     *
     * @param object object to be encoded
     * @return a json string representing the object
     * @throws CoderException if an error occurs
     */
    String encode(Object object) throws CoderException;

    /**
     * Decodes a json string into an object.
     *
     * @param json json string to be decoded
     * @param clazz class of object to be decoded
     * @return the object represented by the given json string
     * @throws CoderException if an error occurs
     */
    <T> T decode(String json, Class<T> clazz) throws CoderException;
}
