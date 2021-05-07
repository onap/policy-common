/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

import com.google.re2j.Pattern;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for generating POJOs from Properties.
 */
public class PropertyObjectUtils {

    public static final Logger logger = LoggerFactory.getLogger(PropertyObjectUtils.class);
    private static final Pattern NAME_PAT = Pattern.compile("\\[(\\d{1,3})\\]$");
    private static final Pattern DOT_PAT = Pattern.compile("[.]");

    private PropertyObjectUtils() {
        // do nothing
    }

    /**
     * Converts a set of properties to a Map. Supports json-path style property names with
     * "." separating components, where components may have an optional subscript.
     *
     * @param properties properties to be converted
     * @param prefix properties whose names begin with this prefix are included. The
     *        prefix is stripped from the name before adding the value to the map
     * @return a hierarchical map representing the properties
     */
    public static Map<String, Object> toObject(Properties properties, String prefix) {
        String dottedPrefix = prefix + (prefix.isEmpty() || prefix.endsWith(".") ? "" : ".");
        int pfxlen = dottedPrefix.length();

        Map<String, Object> map = new LinkedHashMap<>();

        for (String name : properties.stringPropertyNames()) {
            if (name.startsWith(dottedPrefix)) {
                String[] components = DOT_PAT.split(name.substring(pfxlen));
                setProperty(map, components, properties.getProperty(name));
            }
        }

        return map;
    }

    /**
     * Sets a property within a hierarchical map.
     *
     * @param map map into which the value should be placed
     * @param names property name components
     * @param value value to be placed into the map
     */
    private static void setProperty(Map<String, Object> map, String[] names, String value) {
        Map<String, Object> node = map;

        final int lastComp = names.length - 1;

        // process all but the final component
        for (var comp = 0; comp < lastComp; ++comp) {
            node = getNode(node, names[comp]);
        }

        // process the final component
        String name = names[lastComp];
        var matcher = NAME_PAT.matcher(name);

        if (!matcher.find()) {
            // no subscript
            node.put(name, value);
            return;
        }

        // subscripted
        List<Object> array = getArray(node, name.substring(0, matcher.start()));
        var index = Integer.parseInt(matcher.group(1));
        expand(array, index);
        array.set(index, value);
    }

    /**
     * Gets a node.
     *
     * @param map map from which to get the object
     * @param name name of the element to get from the map, with an optional subscript
     * @return a Map
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getNode(Map<String, Object> map, String name) {
        var matcher = NAME_PAT.matcher(name);

        if (!matcher.find()) {
            // no subscript
            return getObject(map, name);
        }

        // subscripted
        List<Object> array = getArray(map, name.substring(0, matcher.start()));
        var index = Integer.parseInt(matcher.group(1));
        expand(array, index);

        Object item = array.get(index);
        if (item instanceof Map) {
            return (Map<String, Object>) item;

        } else {
            LinkedHashMap<String, Object> result = new LinkedHashMap<>();
            array.set(index, result);
            return result;
        }
    }

    /**
     * Ensures that an array's size is large enough to hold the specified element.
     *
     * @param array array to be expanded
     * @param index index of the desired element
     */
    private static void expand(List<Object> array, int index) {
        while (array.size() <= index) {
            array.add(null);
        }
    }

    /**
     * Gets an object (i.e., Map) from a map. If the particular element is not a Map, then
     * it is replaced with an empty Map.
     *
     * @param map map from which to get the object
     * @param name name of the element to get from the map, without any subscript
     * @return a Map
     */
    private static Map<String, Object> getObject(Map<String, Object> map, String name) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) map.compute(name, (key, value) -> {
            if (value instanceof Map) {
                return value;
            } else {
                return new LinkedHashMap<>();
            }
        });

        return result;
    }

    /**
     * Gets an array from a map. If the particular element is not an array, then it is
     * replaced with an empty array.
     *
     * @param map map from which to get the array
     * @param name name of the element to get from the map, without any subscript
     * @return an array
     */
    private static List<Object> getArray(Map<String, Object> map, String name) {
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) map.compute(name, (key, value) -> {
            if (value instanceof List) {
                return value;
            } else {
                return new ArrayList<>();
            }
        });

        return result;
    }

    /**
     * Compresses lists contained within a generic object, removing all {@code null}
     * items.
     *
     * @param object object to be compressed
     * @return the original object, modified in place
     */
    public static Object compressLists(Object object) {
        if (object instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> asMap = (Map<String, Object>) object;
            compressMapValues(asMap);

        } else if (object instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> asList = (List<Object>) object;
            compressListItems(asList);
        }

        // else: ignore anything else

        return object;
    }

    /**
     * Walks a hierarchical map and removes {@code null} items found in any Lists.
     *
     * @param map map whose lists are to be compressed
     */
    private static void compressMapValues(Map<String, Object> map) {
        for (Object value : map.values()) {
            compressLists(value);
        }
    }

    /**
     * Removes {@code null} items from the list. In addition, it walks the items within
     * the list, compressing them, as well.
     *
     * @param list the list to be compressed
     */
    private static void compressListItems(List<Object> list) {
        Iterator<Object> iter = list.iterator();
        while (iter.hasNext()) {
            Object item = iter.next();
            if (item == null) {
                // null item - remove it
                iter.remove();

            } else {
                compressLists(item);
            }
        }
    }
}
