/*--
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.utils.coder;

import com.worldturner.medeia.api.JsonSchemaVersion;
import com.worldturner.medeia.api.SchemaSource;
import com.worldturner.medeia.api.StringSchemaSource;
import com.worldturner.medeia.api.ValidationFailedException;
import com.worldturner.medeia.api.gson.MedeiaGsonApi;
import com.worldturner.medeia.schema.validation.SchemaValidator;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import lombok.NonNull;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension to the StandardCoder to support streaming validation against a Draft-07 Json schema specification.
 */

@ToString
public class StandardValCoder extends StandardCoder {

    // The medeia-validator library integrates better than
    // other libraries considered with GSON, and therefore
    // the StandardCoder.

    private static final Logger logger = LoggerFactory.getLogger(StandardValCoder.class);

    private final MedeiaGsonApi validatorApi = new MedeiaGsonApi();
    private final SchemaValidator validator;

    /**
     * StandardCoder with validation.
     */
    public StandardValCoder(@NonNull String jsonSchema, @NonNull String name) {
        SchemaSource schemaSource = new StringSchemaSource(jsonSchema, JsonSchemaVersion.DRAFT07, null, name);
        this.validator = validatorApi.loadSchema(schemaSource);
    }

    @Override
    protected String toPrettyJson(Object object) {
        /*
         * The validator strips off the "pretty" stuff (i.e., spaces), thus we have to validate and generate the pretty
         * JSON in separate steps.
         */
        gson.toJson(object, object.getClass(), validatorApi.createJsonWriter(validator, new StringWriter()));

        return super.toPrettyJson(object);
    }

    @Override
    protected String toJson(@NonNull Object object) {
        var output = new StringWriter();
        toJson(output, object);
        return output.toString();
    }

    @Override
    protected void toJson(@NonNull Writer target, @NonNull Object object) {
        gson.toJson(object, object.getClass(), validatorApi.createJsonWriter(validator, target));
    }

    @Override
    protected <T> T fromJson(@NonNull Reader source, @NonNull Class<T> clazz) {
        return convertFromDouble(clazz, gson.fromJson(validatorApi.createJsonReader(validator, source), clazz));
    }

    @Override
    protected <T> T fromJson(String json, Class<T> clazz) {
        var reader = new StringReader(json);
        return convertFromDouble(clazz, gson.fromJson(validatorApi.createJsonReader(validator, reader), clazz));
    }

    /**
     * Is the json conformant?.
     */
    public boolean isConformant(@NonNull String json) {
        try {
            conformance(json);
        } catch (CoderException e) {
            logger.info("JSON is not conformant to schema", e);
            return false;
        }
        return true;
    }

    /**
     * Check a json string for conformance against its schema definition.
     */
    public void conformance(@NonNull String json) throws CoderException {
        try {
            validatorApi.parseAll(validatorApi.createJsonReader(validator, new StringReader(json)));
        } catch (ValidationFailedException e) {
            throw new CoderException(e);
        }
    }
}
