/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2018, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.report;

/**
 * Class to represent health check report of a service.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@ericsson.com)
 */
public class HealthCheckReport {

    private String name;
    private String url;
    private boolean healthy;
    private int code;
    private String message;

    /**
     * Returns the name of this report.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this report.
     *
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the url of this report.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the url of this report.
     *
     * @param url the url to set
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Returns the health status of this report.
     *
     * @return the healthy
     */
    public boolean isHealthy() {
        return healthy;
    }

    /**
     * Set the health status of this report.
     *
     * @param healthy the healthy to set
     */
    public void setHealthy(final boolean healthy) {
        this.healthy = healthy;
    }

    /**
     * Returns the code of this report.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Set the code of this report.
     *
     * @param code the code to set
     */
    public void setCode(final int code) {
        this.code = code;
    }

    /**
     * Returns the message of this report.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message of this report.
     *
     * @param message the message to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        builder.append("Report [name=");
        builder.append(getName());
        builder.append(", url=");
        builder.append(getUrl());
        builder.append(", healthy=");
        builder.append(isHealthy());
        builder.append(", code=");
        builder.append(getCode());
        builder.append(", message=");
        builder.append(getMessage());
        builder.append("]");
        return builder.toString();
    }
}
