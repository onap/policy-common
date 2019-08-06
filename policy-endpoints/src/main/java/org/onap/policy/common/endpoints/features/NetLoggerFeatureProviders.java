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

package org.onap.policy.common.endpoints.features;

import lombok.Getter;
import org.onap.policy.common.utils.services.OrderedServiceImpl;

/**
 * Providers for network logging feature.
 */
public class NetLoggerFeatureProviders {

    /**
     * Feature providers implementing this interface.
     */
    @Getter
    private static final OrderedServiceImpl<NetLoggerFeatureApi> providers =
                    new OrderedServiceImpl<>(NetLoggerFeatureApi.class);

    private NetLoggerFeatureProviders() {
        // do nothing
    }
}
