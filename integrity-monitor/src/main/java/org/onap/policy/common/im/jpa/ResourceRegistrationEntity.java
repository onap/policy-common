/*-
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2018, 2020-2021 AT&T Intellectual Property. All rights reserved.
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
import org.onap.policy.common.im.MonitorTime;
/*
 * The Entity class to persist a policy object ResourceRegistration
 */

@Entity
@Table(name = "ResourceRegistrationEntity")
@NamedQuery(name = " ResourceRegistrationEntity.findAll", query = "SELECT e FROM ResourceRegistrationEntity e ")
@NamedQuery(name = "ResourceRegistrationEntity.deleteAll", query = "DELETE FROM ResourceRegistrationEntity WHERE 1=1")
// @SequenceGenerator(name="seqResourceRegistration", initialValue=1, allocationSize=1)

public class ResourceRegistrationEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seqResourceRegistration")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ResourceRegistrationId")
    private long resourceRegistrationId;

    @Column(name = "resourceName", nullable = false, length = 100, unique = true)
    private String resourceName;

    @Column(name = "resourceUrl", nullable = false, length = 255, unique = true)
    private String resourceUrl;

    @Column(name = "site", nullable = true, length = 50)
    private String site;

    @Column(name = "nodeType", nullable = true, length = 50)
    private String nodeType;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated")
    private Date lastUpdated;

    public ResourceRegistrationEntity() {
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
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = MonitorTime.getInstance().getDate();
    }

    /**
     * Get the resource registration Id.
     *
     * @return the Id
     */
    public long getResourceRegistrationId() {
        return resourceRegistrationId;
    }

    public String getResourceName() {
        return this.resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceUrl() {
        return this.resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getSite() {
        return this.site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getNodeType() {
        return this.nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Get the createdDate.
     *
     * @return the createdDate
     */
    public Date getCreatedDate() {
        return createdDate;
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
