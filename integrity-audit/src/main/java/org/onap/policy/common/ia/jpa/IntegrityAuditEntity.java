/*
 * ============LICENSE_START=======================================================
 * Integrity Audit
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

package org.onap.policy.common.ia.jpa;

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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.onap.policy.common.ia.AuditorTime;

@Entity
@Table(name = "IntegrityAuditEntity")
@NamedQuery(name = " IntegrityAuditEntity.findAll", query = "SELECT e FROM IntegrityAuditEntity e ")
@NamedQuery(name = "IntegrityAuditEntity.deleteAll", query = "DELETE FROM IntegrityAuditEntity WHERE 1=1")
@NoArgsConstructor
@Getter
@Setter
public class IntegrityAuditEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private static boolean unitTesting;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @Setter(AccessLevel.PRIVATE)
    private long id;

    @Column(name = "persistenceUnit", nullable = false)
    private String persistenceUnit;

    @Column(name = "site", nullable = true)
    private String site;

    @Column(name = "nodeType", nullable = true)
    private String nodeType;

    @Column(name = "resourceName", nullable = false, unique = true)
    private String resourceName;

    @Column(name = "designated", nullable = true)
    private boolean designated = false;

    @Column(name = "jdbcDriver", nullable = false)
    private String jdbcDriver;

    @Column(name = "jdbcUrl", nullable = false)
    private String jdbcUrl;

    @Column(name = "jdbcUser", nullable = false)
    private String jdbcUser;

    @Column(name = "jdbcPassword", nullable = false)
    private String jdbcPassword;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "createdDate", updatable = true)
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastUpdated")
    private Date lastUpdated;

    /**
     * Pre persist.
     */
    @PrePersist
    public void prePersist() {
        var date = AuditorTime.getInstance().getDate();
        this.createdDate = date;
        this.lastUpdated = date;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = AuditorTime.getInstance().getDate();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (isUnitTesting()) {
            /*
             * Note: other fields may be added here, as long as the created-date and last-updated
             * date are not included.
             */
            out.writeObject(jdbcUrl);

        } else {
            out.defaultWriteObject();
        }
    }
}
