/*-
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

package org.onap.policy.common.endpoints.report;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Class to perform unit test of HealthCheckReport.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@ericsson.com)
 */
public class TestHealthCheckReport {

    @Test
    public void testReport() {
        final String name = "Policy";
        final String url = "self";
        final boolean healthy = true;
        final int code = 200;
        final String message = "alive";
        final HealthCheckReport report = new HealthCheckReport();
        report.setName(name);
        report.setUrl(url);
        report.setHealthy(healthy);
        report.setCode(code);
        report.setMessage(message);
        validateReport(name, url, healthy, code, message, report);
    }

    private void validateReport(final String name, final String url, final boolean healthy, final int code,
            final String message, final HealthCheckReport report) {
        assertEquals(name, report.getName());
        assertEquals(url, report.getUrl());
        assertEquals(healthy, report.isHealthy());
        assertEquals(code, report.getCode());
        assertEquals(message, report.getMessage());
        assertEquals("Report [name=Policy, url=self, healthy=true, code=200, message=alive]", report.toString());
    }
}
