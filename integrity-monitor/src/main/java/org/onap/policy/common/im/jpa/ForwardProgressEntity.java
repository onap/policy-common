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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/*
 * The Entity class to persist a policy object ForwardProgress
 */
import org.onap.policy.common.im.MonitorTime;

@Entity
@Table(name = "ForwardProgressEntity")
@NamedQuery(name = " ForwardProgressEntity.findAll", query = "SELECT e FROM ForwardProgressEntity e ")
@NamedQuery(name = "ForwardProgressEntity.deleteAll", query = "DELETE FROM ForwardProgressEntity WHERE 1=1")
// @SequenceGenerator(name="seqForwardProgress", initialValue=1, allocationSize=1)
@Getter
@Setter
@NoArgsConstructor
public class ForwardProgressEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="seqForwardProgress")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "forwardProgressId")
    @Setter(AccessLevel.NONE)
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

    /**
     * PrePersist callback method.
     */
    @PrePersist
    public void prePersist() {
        var date = MonitorTime.getInstance().getDate();
        this.createdDate = date;
        this.lastUpdated = date;
        this.fpcCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = MonitorTime.getInstance().getDate();
    }
}
