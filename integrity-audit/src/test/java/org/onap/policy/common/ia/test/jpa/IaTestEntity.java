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

package org.onap.policy.common.ia.test.jpa;

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

@Entity
@Table(name="IaTestEntity")
@NamedQueries({
	@NamedQuery(name=" IaTestEntity.findAll", query="SELECT e FROM IaTestEntity e "),
	@NamedQuery(name="IaTestEntity.deleteAll", query="DELETE FROM IaTestEntity WHERE 1=1")
})

public class IaTestEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="ImTestId")
	private long imTestId;
	
	@Column(name="created_by", nullable=false, length=255)
	private String createdBy = "guest";
	
	@Column(name="person", nullable=false, length=255)
	private PersonSample person;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_date", updatable=false)
	private Date createdDate;

	@Column(name="modified_by", nullable=false, length=255)
	private String modifiedBy = "guest";

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="modified_date", nullable=false)
	private Date modifiedDate;

	public IaTestEntity() {
	}

	@PrePersist
	public void	prePersist() {
		Date date = new Date();
		this.createdDate = date;
		this.modifiedDate = date;
	}

	@PreUpdate
	public void preUpdate() {
		this.modifiedDate = new Date();
	}
	
	/**
	 * @return the Id
	 */
	public long getImTestId() {
		return imTestId;
	}
	
	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	/**
	 * @return the modifiedBy
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}
	
	/**
	 * @param modifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	
	/**
	 * @return the modifiedDate
	 */
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	/**
	 * @return the createdDate
	 */
	public Date getCreatedDate() {
		return createdDate;
	}
	
	/**
	 * @param the person to set
	 */
	public void setPersonTest(PersonSample p) {
		this.person = p;
	}
	
	/**
	 * @return the person
	 */
	public PersonSample getPersonTest() {
		return person;
	}
}
