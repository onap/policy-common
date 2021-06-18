/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple object registry, similar in spirit to JNDI, but suitable for use in a
 * stand-alone JVM.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Registry {
    private static final Logger logger = LoggerFactory.getLogger(Registry.class);

    private static Registry instance = new Registry();

    /**
     * Registry map.
     */
    private Map<String, Object> name2object = new ConcurrentHashMap<>();

    /**
     * Registers an object.
     *
     * @param name name by which the object is known
     * @param object object to be registered
     * @throws IllegalStateException if an object is already registered for that name
     * @throws IllegalArgumentException if either argument is null
     */
    public static void register(String name, Object object) {
        if (name == null) {
            throw new IllegalArgumentException("attempt to register: " + name);
        }

        if (object == null) {
            throw new IllegalArgumentException("attempt to register null object for " + name);
        }

        instance.name2object.compute(name, (key, oldval) -> {

            if (oldval != null) {
                throw new IllegalStateException("already registered: " + name);
            }

            return object;
        });
    }

    /**
     * Registers an object, replacing any previously existing binding.
     *
     * @param name name by which the object is known
     * @param object object to be registered
     * @throws IllegalArgumentException if either argument is null
     */
    public static void registerOrReplace(String name, Object object) {
        if (name == null) {
            throw new IllegalArgumentException("attempt to register: " + name);
        }

        if (object == null) {
            throw new IllegalArgumentException("attempt to register null object for " + name);
        }

        instance.name2object.compute(name, (key, oldval) -> {

            if (oldval != null) {
                logger.warn("replacing previously registered: {}", name);
            }

            return object;
        });
    }

    /**
     * Unregisters an object.
     *
     * @param name name by which the object is known
     * @return {@code true} if the object was unregistered, {@code false} if it did not
     *         exist
     */
    public static boolean unregister(String name) {
        return (instance.name2object.remove(name) != null);
    }

    /**
     * Gets the object by the given name.
     *
     * @param name name of the object to get
     * @return the object
     * @throws IllegalArgumentException if no object is registered by the given name
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String name) {
        Object obj = instance.name2object.get(name);
        if (obj == null) {
            throw new IllegalArgumentException("not registered: " + name);
        }

        return (T) obj;
    }

    /**
     * Gets the object by the given name.
     *
     * @param name name of the object to get
     * @param clazz object's class
     * @return the object
     * @throws IllegalArgumentException if no object is registered by the given name
     */
    public static <T> T get(String name, Class<T> clazz) {
        Object obj = instance.name2object.get(name);
        if (obj == null) {
            throw new IllegalArgumentException("not registered: " + name);
        }

        return clazz.cast(obj);
    }

    /**
     * Gets the object by the given name, providing a default value if the name is not
     * registered.
     *
     * @param name name of the object to get
     * @param clazz object's class
     * @param defaultVal the default value to return, if the object does not exist
     * @return the object, if it exists, the default value, otherwise
     */
    public static <T> T getOrDefault(String name, Class<T> clazz, T defaultVal) {
        Object obj = instance.name2object.get(name);
        return (obj != null ? clazz.cast(obj) : defaultVal);
    }

    /**
     * Creates a new registry instance. This is typically only used by junit tests, as it
     * discards any previous registry entries.
     */
    public static void newRegistry() {
        instance = new Registry();
    }
}
