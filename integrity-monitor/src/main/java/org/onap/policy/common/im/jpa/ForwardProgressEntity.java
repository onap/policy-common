/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2018, 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serial;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ForwardProgressEntity")
@NamedQuery(name = " ForwardProgressEntity.findAll", query = "SELECT e FROM ForwardProgressEntity e ")
@NamedQuery(name = "ForwardProgressEntity.deleteAll", query = "DELETE FROM ForwardProgressEntity WHERE 1=1")
@Getter
@Setter
@NoArgsConstructor
public class ForwardProgressEntity extends DateEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "forwardProgressId")
    @Setter(AccessLevel.NONE)
    private long forwardProgressId;

    @Column(name = "resourceName", nullable = false, length = 100, unique = true)
    private String resourceName;

    @Column(name = "fpc_count", nullable = false)
    private long fpcCount;

    /**
     * PrePersist callback method.
     */
    @PrePersist
    @Override
    public void prePersist() {
        this.fpcCount = 0;
        super.prePersist();
    }
}
