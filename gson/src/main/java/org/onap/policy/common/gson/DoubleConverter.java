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

package org.onap.policy.common.gson;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converter for Double values. By default, GSON treats all Objects that are numbers, as
 * Double. This converts Doubles to Integer or Long, if possible. It converts stand-alone
 * Doubles, as well as those found within Arrays and Maps.
 */
public class DoubleConverter {

    private DoubleConverter() {
        // do nothing
    }

    /**
     * Performs conversion of all values in an list.
     *
     * @param list the list whose values are to be converted
     * @return a new list containing the modified values
     */
    public static List<Object> convertFromDouble(List<Object> list) {
        if (list == null) {
            return list;
        }

        return list.stream().map(DoubleConverter::convertFromDouble).collect(Collectors.toList());
    }

    /**
     * Performs in-place conversion of all values in a map.
     *
     * @param map the map whose values are to be converted
     * @return the modified map
     */
    public static Map<String, Object> convertFromDouble(Map<String, Object> map) {
        if (map == null) {
            return map;
        }

        Set<Entry<String, Object>> set = map.entrySet();

        for (Entry<String, Object> entry : set) {
            entry.setValue(convertFromDouble(entry.getValue()));
        }

        return map;
    }

    /**
     * Converts a value. If the value is a List, then it recursively converts the
     * entries of the List. Likewise with a map, however, the map is converted in place.
     *
     * @param value value to be converted
     * @return the converted value
     */
    @SuppressWarnings({"unchecked"})
    public static Object convertFromDouble(Object value) {
        if (value == null) {
            return value;
        }

        if (value instanceof List) {
            return convertFromDouble((List<Object>) value);
        }

        if (value instanceof Map) {
            return convertFromDouble((Map<String, Object>) value);
        }

        if (!(value instanceof Double)) {
            return value;
        }

        Double num = (Double) value;
        long longval = num.longValue();

        if (Double.compare(num.doubleValue(), longval) != 0) {
            // it isn't integral - return unchanged value
            return value;
        }

        // it's integral - determine if it's an integer or a long
        int intval = (int) longval;

        if (intval == longval) {
            return intval;
        }

        return longval;
    }
}
