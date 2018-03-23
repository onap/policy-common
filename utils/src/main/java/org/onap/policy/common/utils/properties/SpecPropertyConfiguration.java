/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.onap.policy.common.utils.properties.exception.PropertyException;

/**
 * PropertyConfiguration whose property names are specialized, using a specialization. A
 * property name can take one of the following forms:
 * <dl>
 * <dt>aaa{$}Ddd</dt>
 * <dd>if the specialization is "Xxx", then it looks for the value associated with the
 * property named "aaaXxxDdd"</dd>
 * <dt>aaa{Bbb?Ccc}Ddd</dt>
 * <dd>if the specialization is "Xxx", then it looks for the value associated with the
 * property named "aaaBbbXxxCccDdd". If the property does not exist, then it looks for the
 * value associated with the property named "aaaDdd" (i.e., without the
 * specialization)</dd>
 * <dt>aaa</dt>
 * <dd>simply looks for the value associated with the property named "aaa", without using
 * the specialization</dd>
 * </dl>
 * <p>
 * In the above examples, any of the components (e.g., "aaa") may be empty.
 */
public class SpecPropertyConfiguration extends PropertyConfiguration {

    /**
     * Pattern to extract the specializer from a property name. Group 1 matches the form,
     * "{$}", while groups 2 and 3 match the prefix and suffix, respectively, of the form,
     * "{prefix?suffix}".
     */
    private static final Pattern SPEC_PAT = Pattern.compile(""
                    // start of specialization info
                    + "\\{(?:"
                    // specialization type 1
                    + "(\\$)"
                    // alternative
                    + "|"
                    // specialization type 2
                    + "(?:"
                    // specialization type 2 prefix, may be empty
                    + "([^}?]*)"
                    // place-holder for the specialization, itself
                    + "\\?"
                    // specialization type 2 suffix, may be empty
                    + "([^}]*)"
                    // end of specialization type 2
                    + ")"
                    // end of specialization info
                    + ")\\}");

    /**
     * The specialization to be used within property names.
     */
    private final String specialization;

    /**
     * Constructs a configuration, without populating any fields; fields should be
     * populated later by invoking {@link #setAllFields(Properties)}.
     * 
     * @param specialization specialization to be substituted within property names
     */
    public SpecPropertyConfiguration(String specialization) {
        super();

        this.specialization = specialization;
    }

    /**
     * 
     * Initializes each "@Property" field with its value, as found in the properties.
     * 
     * @param specialization specialization to be substituted within property names
     * @param props properties from which to extract the values
     * @throws PropertyException if an error occurs
     */
    public SpecPropertyConfiguration(String specialization, Properties props) throws PropertyException {
        super();

        this.specialization = specialization;

        setAllFields(props);
    }

    /**
     * Gets a property's value, examining the property name for each of the types of
     * specialization.
     */
    @Override
    protected String getRawPropertyValue(Properties props, String propnm) {
        Matcher mat = SPEC_PAT.matcher(propnm);

        if (!mat.find()) {
            // property name isn't specialized - use it as is
            return super.getRawPropertyValue(props, propnm);

        } else if (mat.group(1) != null) {
            // replace "{$}" with the specialization name
            return super.getRawPropertyValue(props, specializeType1(propnm, specialization, mat));

        } else {
            // first try to get the property using the specialization info
            String val = super.getRawPropertyValue(props, specializeType2(propnm, specialization, mat));
            if (val != null) {
                return val;
            }

            // wasn't found - try again, without any specialization info
            return super.getRawPropertyValue(props, generalizeType2(propnm, mat));
        }
    }

    /**
     * Generalizes a property name by stripping any specialization info from it. This is
     * typically used to construct property names for junit testing.
     * 
     * @param propnm property name to be stripped of specialization info
     * @return the generalized property name
     * @throws IllegalArgumentException if the property name requires specialization
     *         (i.e., contains "{$}")
     */
    public static String generalize(String propnm) {
        Matcher mat = SPEC_PAT.matcher(propnm);

        if (!mat.find()) {
            // property name has no specialization info
            return propnm;

        } else if (mat.group(1) != null) {
            // the "{$}" form requires specialization
            throw new IllegalArgumentException("property requires specialization");

        } else {
            // property name has specialization info - strip it out
            return generalizeType2(propnm, mat);
        }
    }

    /**
     * 
     * Generalizes a property name of specialization type 2 (i.e., "{xxx?yyy}" form).
     * 
     * @param propnm property name to be stripped of specialization info
     * @param matcher the matcher that matched the "{xxx?yyy}"
     * @return the generalized property name
     */
    private static String generalizeType2(String propnm, Matcher mat) {
        String prefix = propnm.substring(0, mat.start());
        String suffix = propnm.substring(mat.end());
        
        return prefix + suffix;
    }

    /**
     * Specializes a property name by applying the specialization. This is typically used
     * to construct property names for junit testing.
     * 
     * @param propnm property name to be stripped of specialization info
     * @param spec specialization to apply
     * @return the specialized property name
     */
    public static String specialize(String propnm, String spec) {
        Matcher mat = SPEC_PAT.matcher(propnm);

        if (!mat.find()) {
            // property name has no specialization info - leave it as is
            return propnm;

        } else if (mat.group(1) != null) {
            // the "{$}" form requires specialization
            return specializeType1(propnm, spec, mat);

        } else {
            // the "{xxx?yyy}" form requires specialization
            return specializeType2(propnm, spec, mat);
        }
    }

    /**
     * Specializes a property name of specialization type 1 (i.e., "{$}" form).
     * 
     * @param propnm property name to be stripped of specialization info
     * @param spec specialization to apply
     * @param matcher the matcher that matched the "{$}"
     * @return the specialized property name
     */
    private static String specializeType1(String propnm, String spec, Matcher mat) {
        String prefix = propnm.substring(0, mat.start());
        String suffix = propnm.substring(mat.end());

        return prefix + spec + suffix;
    }

    /**
     * Specializes a property name of specialization type 2 (i.e., "{xxx?yyy}" form).
     * 
     * @param propnm property name to be stripped of specialization info
     * @param spec specialization to apply
     * @param matcher the matcher that matched the "{xxx?yyy}"
     * @return the specialized property name
     */
    private static String specializeType2(String propnm, String spec, Matcher matcher) {
        String prefix = propnm.substring(0, matcher.start());
        String suffix = propnm.substring(matcher.end());

        String specPrefix = matcher.group(2);
        String specSuffix = matcher.group(3);

        return (prefix + specPrefix + spec + specSuffix + suffix);
    }
}
