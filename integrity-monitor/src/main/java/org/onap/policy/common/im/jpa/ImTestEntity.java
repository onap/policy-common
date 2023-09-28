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
@Table(name = "ImTestEntity")
@NamedQuery(name = " ImTestEntity.findAll", query = "SELECT e FROM ImTestEntity e ")
@NamedQuery(name = "ImTestEntity.deleteAll", query = "DELETE FROM ImTestEntity WHERE 1=1")
@Getter
@Setter
@NoArgsConstructor
public class ImTestEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ImTestId")
    @Setter(AccessLevel.NONE)
    private long imTestId;

    @Column(name = "created_by", nullable = false, length = 255)
    private String createdBy = "guest";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", updatable = false)
    private Date createdDate;

    @Column(name = "modified_by", nullable = false, length = 255)
    private String modifiedBy = "guest";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date", nullable = false)
    private Date modifiedDate;

    /**
     * PrePersist callback method.
     */
    @PrePersist
    public void prePersist() {
        var date = MonitorTime.getInstance().getDate();
        this.createdDate = date;
        this.modifiedDate = date;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedDate = MonitorTime.getInstance().getDate();
    }
}
