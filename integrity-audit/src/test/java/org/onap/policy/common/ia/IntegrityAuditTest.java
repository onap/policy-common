/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.common.ia;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import org.junit.jupiter.api.Test;

class IntegrityAuditTest {
    private static final String PROPERTIES = "properties";
    private static final String RESOURCE_NAME = "resourceName";
    private static final String SOMETHING = "something";

    /**
     * Test if we can access the updated bad params outside of the parmsAreBad method.
     */
    @Test
    void testParmsAreBad() {
        // Try with 2 null params
        StringBuilder badParams = new StringBuilder();
        IntegrityAudit.parmsAreBad(null, SOMETHING, null, badParams);

        assertNotEquals("", badParams.toString());
        assertTrue(badParams.toString().contains(RESOURCE_NAME));
        assertTrue(badParams.toString().contains(PROPERTIES));

        // Try with 1 null params
        badParams = new StringBuilder();
        Properties props = new Properties();
        props.put(IntegrityAuditProperties.DB_DRIVER, "test_db_driver");
        IntegrityAudit.parmsAreBad(null, SOMETHING, props, badParams);

        assertNotEquals("", badParams.toString());
        assertTrue(badParams.toString().contains(RESOURCE_NAME));
        assertFalse(badParams.toString().contains(PROPERTIES));

        // Try with 0 null params
        badParams = new StringBuilder();
        IntegrityAudit.parmsAreBad("someting", SOMETHING, props, badParams);
        assertNotEquals("", badParams.toString());
        assertFalse(badParams.toString().contains(RESOURCE_NAME));
        assertFalse(badParams.toString().contains(PROPERTIES));

        // Try with invalid node type
        props.put(IntegrityAuditProperties.NODE_TYPE, "bogus");
        badParams = new StringBuilder();
        IntegrityAudit.parmsAreBad("someting", SOMETHING, props, badParams);
        assertNotEquals("", badParams.toString());
        assertTrue(badParams.toString().contains("nodeType"));

    }

}
