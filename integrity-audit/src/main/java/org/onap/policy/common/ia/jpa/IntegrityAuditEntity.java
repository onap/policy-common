/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * The Entity class to for management of IntegrityAudits
 */

@Entity
@Table(name="IntegrityAuditEntity")
@NamedQueries({
	@NamedQuery(name=" IntegrityAuditEntity.findAll", query="SELECT e FROM IntegrityAuditEntity e "),
	@NamedQuery(name="IntegrityAuditEntity.deleteAll", query="DELETE FROM IntegrityAuditEntity WHERE 1=1")
})

public class IntegrityAuditEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private static boolean isUnitTesting;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="id")
	private long id;
	
	@Column(name="persistenceUnit", nullable=false)
	private String persistenceUnit;
	
	@Column(name="site", nullable=true)
	private String site;
	
	@Column(name="nodeType", nullable=true)
	private String nodeType;
	
	@Column(name="resourceName", nullable=false, unique=true)
	private String resourceName;
	
	@Column(name="designated", nullable=true)
	private boolean designated = false;

	@Column(name="jdbcDriver", nullable=false)
	private String jdbcDriver;
	
	@Column(name="jdbcUrl", nullable=false)
	private String jdbcUrl;
	
	@Column(name="jdbcUser", nullable=false)
	private String jdbcUser;
	
	@Column(name="jdbcPassword", nullable=false)
	private String jdbcPassword;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="createdDate", updatable=true)
	private Date createdDate;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastUpdated")
	private Date lastUpdated;

	
	public IntegrityAuditEntity() {
		// Empty constructor
	}

	@PrePersist
	public void	prePersist() {
		Date date = new Date();
		this.createdDate = date;
		this.lastUpdated = date;
	}
	
	@PreUpdate
	public void preUpdate() {
		if (!isUnitTesting()) {
			this.lastUpdated = new Date();
		}
	}

	public long getId() {
		return id;
	}

	public String getPersistenceUnit() {
		return persistenceUnit;
	}

	public void setPersistenceUnit(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public boolean isDesignated() {
		return designated;
	}

	public void setDesignated(boolean designated) {
		this.designated = designated;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getJdbcUser() {
		return jdbcUser;
	}

	public void setJdbcUser(String jdbcUser) {
		this.jdbcUser = jdbcUser;
	}

	public String getJdbcPassword() {
		return jdbcPassword;
	}

	public void setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword = jdbcPassword;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Date getCreatedDate() {
		return createdDate;
	}
	
	public void setCreatedDate(Date created) {
		this.createdDate = created;
	}

	public static boolean isUnitTesting() {
		return isUnitTesting;
	}

	public static void setUnitTesting(boolean isUnitTesting) {
		IntegrityAuditEntity.isUnitTesting = isUnitTesting;
	}
}
