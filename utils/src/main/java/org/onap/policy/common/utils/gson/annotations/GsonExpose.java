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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation indicating that a method is to be included in serialization or
 * de-serialization when using gson. This is similar to the jackson <i>JsonProperty</i>
 * annotation, but it is only applicable to methods, as fields can already be managed via
 * the <i>SerializedName</i> annotation.
 * 
 * <p>Currently, this only applies to serialization, causing the <i>no-argument</i> method
 * to be invoked and the result serialized. Unless overridden via the <i>SerializedName</i>
 * annotation, the name of the output field is determined by stripping any leading "get"
 * or "is" from the method name and then converting the first letter to lower-case.
 * 
 * <p>Note: the GsonExposeTypeAdapterFactory must be registered with the gson object for
 * this to have effect.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface GsonExpose {

}
