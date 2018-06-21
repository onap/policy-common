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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
/*
 * The Entity class to persist a policy object ForwardProgress
 */
import org.onap.policy.common.im.MonitorTime;

@Entity
@Table(name = "ForwardProgressEntity")
@NamedQueries({@NamedQuery(name = " ForwardProgressEntity.findAll", query = "SELECT e FROM ForwardProgressEntity e "),
        @NamedQuery(name = "ForwardProgressEntity.deleteAll", query = "DELETE FROM ForwardProgressEntity WHERE 1=1")})
// @SequenceGenerator(name="seqForwardProgress", initialValue=1, allocationSize=1)

public class ForwardProgressEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seqForwardProgress")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "forwardProgressId")
    private long forwardProgressId;

    @Column(name = "resourceName", nullable = false, length = 100, unique = true)
    private String resourceName;

    @Column(name = "fpc_count", nullable = false)
    private long fpcCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated")
    private Date lastUpdated;

    public ForwardProgressEntity() {
        // default constructor
    }

    /**
     * PrePersist callback method.
     */
    @PrePersist
    public void prePersist() {
        Date date = MonitorTime.getInstance().getDate();
        this.createdDate = date;
        this.lastUpdated = date;
        this.fpcCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = MonitorTime.getInstance().getDate();
    }

    /**
     * Get the forward progress Id.
     * 
     * @return the Id
     */
    public long getForwardProgressId() {
        return forwardProgressId;
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * Get the fpcCount.
     * 
     * @return the fpcCount
     */
    public long getFpcCount() {
        return fpcCount;
    }

    /**
     * Set the fpcCount.
     * 
     * @param fpcCount the fpcCount to set
     */
    public void setFpcCount(long fpcCount) {
        this.fpcCount = fpcCount;
    }

    /**
     * Get the lastUpdated.
     * 
     * @return the lastUpdated
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Set the lastUpdated.
     * 
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
