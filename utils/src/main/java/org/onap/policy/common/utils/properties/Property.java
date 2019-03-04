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

package org.onap.policy.common.utils.properties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Properties;


/**
 * Annotation that declares a variable to be configured via {@link Properties}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)

public @interface Property {

    /**
     * Name of the property.
     *
     * @return the property name
     */
    public String name();

    /**
     * Default value, used when the property does not exist.
     *
     * @return the default value
     */
    public String defaultValue() default "";

    /**
     * Comma-separated options identifying what's acceptable. The word, "empty",
     * indicates that an empty string, "", is an acceptable value.
     *
     * @return options identifying what's acceptable
     */
    public String accept() default "";
}
