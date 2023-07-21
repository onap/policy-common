/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.onap.policy.common.im.MonitorTime;

@Entity
@Table(name = "StateManagementEntity")
@NamedQuery(name = "StateManagementEntity.findAll", query = "SELECT e FROM StateManagementEntity e")
@Getter
@Setter
@NoArgsConstructor
public class StateManagementEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(name = "resourceName", nullable = false, length = 100, unique = true)
    private String resourceName;

    @Column(name = "adminState", nullable = false, length = 20)
    private String adminState;

    @Column(name = "opState", nullable = false, length = 20)
    private String opState;

    @Column(name = "availStatus", nullable = false, length = 20)
    private String availStatus;

    @Column(name = "standbyStatus", nullable = false, length = 20)
    private String standbyStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_Date", updatable = false)
    @Setter(AccessLevel.NONE)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modifiedDate", nullable = false)
    private Date modifiedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = MonitorTime.getInstance().getDate();
        this.modifiedDate = MonitorTime.getInstance().getDate();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = MonitorTime.getInstance().getDate();
    }

    /**
     * Clone a StateManagementEntity.
     *
     * @param sm the StateManagementEntity to clone
     * @return a new StateManagementEntity
     */
    public static StateManagementEntity clone(StateManagementEntity sm) {
        var newStateManagementEntity = new StateManagementEntity();
        newStateManagementEntity.setResourceName(sm.getResourceName());
        newStateManagementEntity.setAdminState(sm.getResourceName());
        newStateManagementEntity.setOpState(sm.getOpState());
        newStateManagementEntity.setAdminState(sm.getAdminState());
        newStateManagementEntity.setAvailStatus(sm.getAvailStatus());
        newStateManagementEntity.setStandbyStatus(sm.getStandbyStatus());

        return newStateManagementEntity;
    }
}
