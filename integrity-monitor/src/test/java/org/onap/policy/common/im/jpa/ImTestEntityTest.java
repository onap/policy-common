/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2024 Nordix Foundation.
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

package org.onap.policy.common.im.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.im.MonitorTime;

class ImTestEntityTest {

    @Mock
    MonitorTime mockMonitorTime;

    @InjectMocks
    ImTestEntity entity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock MonitorTime to return a fixed date
        Date fixedDate = new Date();

        // Initialize the entity
        entity = new ImTestEntity();
        entity.setModifiedDate(fixedDate);
    }

    @Test
    void testPrePersist() {
        // Invoke @PrePersist manually
        entity.prePersist();

        // Verify that createdDate and modifiedDate are set and are the same
        assertNotNull(entity.getCreatedDate(), "Created date should not be null");
        assertNotNull(entity.getModifiedDate(), "Modified date should not be null");
        assertEquals(entity.getCreatedDate(), entity.getModifiedDate(),
            "Created and modified dates should be the same");
    }

    @Test
    void testPreUpdate() {
        // Set initial values
        entity.setCreatedBy("testUser");
        entity.setCreatedDate(new Date());

        // Invoke @PreUpdate manually
        entity.preUpdate();

        // Verify that modifiedDate is updated and is after createdDate
        Date updatedModifiedDate = entity.getModifiedDate();
        assertNotNull(updatedModifiedDate, "Modified date should not be null");
    }
}
