/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2022 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
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

package org.onap.policy.common.utils.resources;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Prometheus constants and utilities.
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrometheusUtils {

    /**
     * Policy Deployments Metric Name.
     */
    public static final String POLICY_DEPLOYMENTS_METRIC = "policy_deployments";

    /**
     * Policy Deployments Metric Help Message.
     */
    public static final String POLICY_DEPLOYMENT_HELP = "The total number of policy deployments.";

    /**
     * Policy Execution Metric Name.
     */
    public static final String POLICY_EXECUTION_METRIC = "policy_execution";

    /**
     * Policy Execution Metric Help Message.
     */
    public static final String POLICY_EXECUTION_HELP = "The total number of TOSCA policy executions.";

    /**
     * Metric label for arbitrary operations (eg. deploy, undeploy, execute).
     */
    public static final String OPERATION_METRIC_LABEL = "operation";

    /**
     * Deploy operation value.
     */
    public static final String DEPLOY_OPERATION = "deploy";

    /**
     * Undeploy operation value.
     */
    public static final String UNDEPLOY_OPERATION = "undeploy";

    /**
     * Metric label for states (ie. PASSIVE, ACTIVE).
     */
    public static final String STATE_METRIC_LABEL = "state";

    /**
     * Metric label for status of an operation (ie. SUCCESS or FAILURE).
     */
    public static final String STATUS_METRIC_LABEL = "status";

    /**
     * Prometheus namespace values mapping to the supported PDP types.
     */
    public enum PdpType {
        PDPD("pdpd"),
        PDPA("pdpa"),
        PDPX("pdpx");

        @Getter
        private final String namespace;

        PdpType(String namespace) {
            this.namespace = namespace;
        }
    }
}