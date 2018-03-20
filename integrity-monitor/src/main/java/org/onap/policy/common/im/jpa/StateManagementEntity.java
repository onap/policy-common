/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "StateManagementEntity")
@NamedQuery(name = "StateManagementEntity.findAll", query = "SELECT e FROM StateManagementEntity e")
// @SequenceGenerator(name="seqSM", initialValue=1, allocationSize=1)

public class StateManagementEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seqSM")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
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
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modifiedDate", nullable = false)
    private Date modifiedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = new Date();
        this.modifiedDate = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = new Date();
    }

    public StateManagementEntity() {
        // default constructor
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getAdminState() {
        return this.adminState;
    }

    public void setAdminState(String adminState) {
        this.adminState = adminState;
    }

    public String getOpState() {
        return this.opState;
    }

    public void setOpState(String opState) {
        this.opState = opState;

    }

    public String getAvailStatus() {
        return this.availStatus;
    }

    public void setAvailStatus(String availStatus) {
        this.availStatus = availStatus;
    }

    public String getStandbyStatus() {
        return this.standbyStatus;
    }

    public void setStandbyStatus(String standbyStatus) {
        this.standbyStatus = standbyStatus;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    /**
     * Clone a StateManagementEntity.
     * 
     * @param sm the StateManagementEntity to clone
     * @return a new StateManagementEntity
     */
    public static StateManagementEntity clone(StateManagementEntity sm) {
        StateManagementEntity newStateManagementEntity = new StateManagementEntity();
        newStateManagementEntity.setResourceName(sm.getResourceName());
        newStateManagementEntity.setAdminState(sm.getResourceName());
        newStateManagementEntity.setOpState(sm.getOpState());
        newStateManagementEntity.setAdminState(sm.getAdminState());
        newStateManagementEntity.setAvailStatus(sm.getAvailStatus());
        newStateManagementEntity.setStandbyStatus(sm.getStandbyStatus());

        return newStateManagementEntity;
    }
}
