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

package org.onap.policy.common.utils.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import java.lang.annotation.Annotation;
import org.onap.policy.common.utils.gson.annotations.GsonIgnore;

/**
 * Strategy, used with gson, to exclude fields marked with {@link GsonIgnore} from being
 * serialized or de-serialized. Separate instances of this class may be used to exclude
 * fields marked with other annotations, as well.
 */
public class GsonIgnoreStrategy implements ExclusionStrategy {

    /**
     * The annotation that identifies fields that are to be ignored.
     */
    private final Class<? extends Annotation> annotClass;

    /**
     * Constructs the object.
     */
    public GsonIgnoreStrategy() {
        this(GsonIgnore.class);
    }

    /**
     * Constructs the object, excluding fields using a particular annotation.
     *
     * @param annotClass fields annotated with this class are ignored
     */
    public GsonIgnoreStrategy(Class<? extends Annotation> annotClass) {
        this.annotClass = annotClass;
    }

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttrs) {
        return (fieldAttrs.getAnnotation(annotClass) != null);
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
