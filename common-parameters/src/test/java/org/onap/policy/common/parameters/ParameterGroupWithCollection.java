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

import java.util.ArrayList;
import java.util.List;

public class ParameterGroupWithCollection implements AbstractParameterGroup {
    private String name;
    private List<Integer> intArrayList = new ArrayList<>();

    /**
     * Create a test parameter group.
     * @param name the parameter group name
     */
    public ParameterGroupWithCollection(final String name) {
        this.name = name;
        
        intArrayList.add(1);
        intArrayList.add(2);
        intArrayList.add(3);
    }

    public List<Integer> getIntArrayList() {
        return intArrayList;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public GroupValidationResult validate() {
        return new GroupValidationResult(this);
    }
}
