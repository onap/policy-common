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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/*
 * The Entity class to persist a policy object ResourceRegistration
 */

@Entity
@Table(name = "ResourceRegistrationEntity")
@NamedQueries({
    @NamedQuery(name = " ResourceRegistrationEntity.findAll", query = "SELECT e FROM ResourceRegistrationEntity e "),
    @NamedQuery(
        name = "ResourceRegistrationEntity.deleteAll",
        query = "DELETE FROM ResourceRegistrationEntity WHERE 1=1")
})
@Getter
@Setter
@NoArgsConstructor
public class ResourceRegistrationEntity extends DateEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ResourceRegistrationId")
    @Setter(AccessLevel.NONE)
    private long resourceRegistrationId;

    @Column(name = "resourceName", nullable = false, length = 100, unique = true)
    private String resourceName;

    @Column(name = "resourceUrl", nullable = false, length = 255, unique = true)
    private String resourceUrl;

    @Column(name = "site", nullable = true, length = 50)
    private String site;

    @Column(name = "nodeType", nullable = true, length = 50)
    private String nodeType;
}
