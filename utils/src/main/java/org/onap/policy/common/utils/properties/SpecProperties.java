/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018, 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.Properties;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Properties with an optional specialization (e.g., session name, controller name).
 */
@Getter(AccessLevel.PROTECTED)
public class SpecProperties extends Properties {
    private static final long serialVersionUID = 1L;

    /**
     * The property prefix, ending with ".".
     */
    private final String prefix;

    /**
     * The specialized property prefix, ending with ".".
     */
    private final String specPrefix;

    /**
     * Constructor.
     *
     * @param prefix the property name prefix that appears before any specialization, may
     *        be ""
     * @param specialization the property name specialization (e.g., session name)
     */
    public SpecProperties(String prefix, String specialization) {
        this.prefix = withTrailingDot(prefix);
        this.specPrefix = withTrailingDot(this.prefix + specialization);
    }

    /**
     * Constructor.
     *
     * @param prefix the property name prefix that appears before any specialization, may
     *        be ""
     * @param specialization the property name specialization (e.g., session name)
     * @param props the default properties
     */
    public SpecProperties(String prefix, String specialization, Properties props) {
        super(props);

        this.prefix = withTrailingDot(prefix);
        this.specPrefix = withTrailingDot(this.prefix + specialization);
    }

    /**
     * Adds a trailing "." to a String, if it doesn't already have one.
     *
     * @param text text to which the "." should be added
     * @return the text, with a trailing "."
     */
    private static String withTrailingDot(String text) {
        return text.isEmpty() || text.endsWith(".") ? text : text + ".";
    }

    /**
     * Gets the property whose value has the given key, looking first for the specialized
     * property name, and then for the generalized property name.
     *
     * @param key property name, without the specialization
     * @return the value from the property set, or {@code null} if the property set does
     *         not contain the value
     */
    @Override
    public String getProperty(String key) {
        if (!key.startsWith(prefix)) {
            return super.getProperty(key);
        }

        var suffix = key.substring(prefix.length());

        String val = super.getProperty(specPrefix + suffix);
        if (val != null) {
            return val;
        }

        return super.getProperty(key);
    }

    @Override
    public final synchronized int hashCode() {
        throw new UnsupportedOperationException("SpecProperties cannot be hashed");
    }

    @Override
    public final synchronized boolean equals(Object obj) {
        throw new UnsupportedOperationException("cannot compare SpecProperties");
    }
}
