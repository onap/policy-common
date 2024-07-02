/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Class to perform unit test of HealthCheckReport.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@ericsson.com)
 */
class TestHealthCheckReport {

    private static final int CODE_INT = 12345;
    private static final String URL_STRING = "http://test-url.test";
    private static final String NAME_STRING = "testName";
    private static final String MESSAGE_STRING = "Testing the getters and setters";
    private static final boolean HEALTHY_BOOL = true;

    static HealthCheckReport report;

    @BeforeAll
    static void setUp() {
        report = new HealthCheckReport();
    }

    @Test
    void testHealthCheckReportConstructor() {
        assertNotNull(report);
    }

    @Test
    void testHealthCheckReportGettersAndSetters() {
        report.setCode(CODE_INT);
        report.setUrl(URL_STRING);
        report.setName(NAME_STRING);
        report.setHealthy(HEALTHY_BOOL);
        report.setMessage(MESSAGE_STRING);

        assertEquals(CODE_INT, report.getCode());
        assertEquals(URL_STRING, report.getUrl());
        assertEquals(NAME_STRING, report.getName());
        assertEquals(HEALTHY_BOOL, report.isHealthy());
        assertEquals(MESSAGE_STRING, report.getMessage());

        assertThat(report.toString()).contains("code=12345", "url=http://test-url.test",
            "name=testName", "healthy=true", "message=Testing the getters and setters");
    }
}
