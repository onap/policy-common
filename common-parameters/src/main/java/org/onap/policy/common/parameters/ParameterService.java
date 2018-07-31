/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.common.parameters;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The parameter service makes ONAP PF parameter groups available to all classes in a JVM.
 * 
 * <p>The reason for having a parameter service is to avoid having to pass parameters down long call chains in modules
 * such as PDPs and editors. The parameter service makes correct and verified parameters available statically.
 * 
 * <p>The parameter service must be used with care because changing a parameter set anywhere in a JVM will affect all
 * users of those parameters anywhere in the JVM.
 *
 * @author Liam Fallon (liam.fallon@ericsson.com)
 */
public abstract class ParameterService {
    // The map holding the parameters
    private static Map<String, ParameterGroup> parameterGroupMap = new ConcurrentHashMap<>();

    /**
     * This class is an abstract static class that cannot be extended.
     */
    private ParameterService() {
    }

    /**
     * Register a parameter group with the parameter service.
     *
     * @param parameterGroup the parameter group
     */
    public static void register(final ParameterGroup parameterGroup) {
        if (!parameterGroupMap.containsKey(parameterGroup.getName())) {
            parameterGroupMap.put(parameterGroup.getName(), parameterGroup);
        } else {
            throw new ParameterRuntimeException(
                            "\"" + parameterGroup.getName() + "\" already registered in parameter service");
        }
    }

    /**
     * Remove a parameter group from the parameter service.
     *
     * @param parameterGroupName the name of the parameter group
     */
    public static void deregister(final String parameterGroupName) {
        if (parameterGroupMap.containsKey(parameterGroupName)) {
            parameterGroupMap.remove(parameterGroupName);
        } else {
            throw new ParameterRuntimeException("\"" + parameterGroupName + "\" not registered in parameter service");
        }
    }

    /**
     * Get a parameter group from the parameter service.
     *
     * @param parameterGroupName the name of the parameter group
     * @return The parameter group
     */
    public static ParameterGroup get(final String parameterGroupName) {
        final ParameterGroup parameterGroup = parameterGroupMap.get(parameterGroupName);

        if (parameterGroup == null) {
            throw new ParameterRuntimeException("\"" + parameterGroupName + "\" not found in parameter service");
        }

        return parameterGroup;
    }

    /**
     * Check if a parameter group is defined on the parameter service.
     *
     * @param parameterGroupName the name of the parameter group
     * @return true if the parameter is defined
     */
    public static boolean contains(final String parameterGroupName) {
        return parameterGroupMap.containsKey(parameterGroupName) && parameterGroupMap.get(parameterGroupName) != null;
    }

    /**
     * Get all parameter groups.
     *
     * @return The entries
     */
    public static Set<Entry<String, ParameterGroup>> getAll() {
        return parameterGroupMap.entrySet();
    }

    /**
     * Clear all parameter groups in the parameter service.
     */
    public static void clear() {
        parameterGroupMap.clear();
    }
}
