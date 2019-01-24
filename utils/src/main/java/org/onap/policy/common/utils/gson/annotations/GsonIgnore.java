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

package org.onap.policy.common.utils.gson.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation indicating that a field is not to be serialized or de-serialized via gson.
 * This is similar to the jackson <i>JsonIgnore</i> annotation, but as methods are
 * automatically ignored by gson, it is only applicable to fields. The gson spec says to
 * use the "transient" keyword to prevent serialization and de-serialization of a field.
 * However, that also impacts the java built-in serialization. Use of this annotation
 * allows a field to be included while using java built-in serialization, but skipped when
 * using gson.
 * 
 * <p>Note: the GsonIgnoreStrategy must be registered with the gson object for this
 * to have effect.
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface GsonIgnore {

}
