/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
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

package org.onap.policy.common.ia;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.onap.policy.common.ia.jpa.IntegrityAuditEntity;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;

/**
 * AuditThread is the main thread for the IntegrityAudit
 * 
 */
public class AuditThread extends Thread {

	private static final Logger logger = FlexLogger.getLogger(AuditThread.class);

	/*
	 * Number of milliseconds that must elapse for audit to be considered
	 * complete. It's public for access by JUnit test logic.
	 */
	public static final long AUDIT_COMPLETION_INTERVAL = 30000;

	/*
	 * Number of iterations for audit simulation.
	 */
	public static final long AUDIT_SIMULATION_ITERATIONS = 3;

	/*
	 * Number of milliseconds to sleep between audit simulation iterations. It's
	 * public for access by JUnit test logic.
	 */
	public static final long AUDIT_SIMULATION_SLEEP_INTERVAL = 5000;

	/*
	 * Unless audit has already been run on this entity, number of milliseconds
	 * to sleep between audit thread iterations. If audit has already been run,
	 * we sleep integrityAuditPeriodMillis.  May be modified by JUnit tests.
	 */
	private static long auditThreadSleepIntervalMillis = 5000;

	/*
	 * Number of milliseconds that must elapse for audit to be considered
	 * complete.  May be modified by JUnit tests.
	 */
	private static long auditCompletionIntervalMillis = AUDIT_COMPLETION_INTERVAL;

	/*
	 * DB access class.
	 */
	private DbDAO dbDAO;

	/*
	 * E.g. pdp_xacml
	 */
	private String nodeType;

	/*
	 * Persistence unit for which this audit is being run.
	 */
	private String persistenceUnit;

	/*
	 * Name of this resource
	 */
	private String resourceName;

	/*
	 * E.g. DB_DRIVER, SITE_NAME, NODE_TYPE
	 */
	private Properties properties;

	/*
	 * See IntegrityAudit class for usage.
	 */
	private long integrityAuditPeriodMillis;
	
	/*
	 * The containing IntegrityAudit instance
	 */
	private IntegrityAudit integrityAudit;

	/**
	 * A latch is taken from this queue before starting an audit.  May be
	 * {@code null}. Used by JUnit tests.
	 */
	private BlockingQueue<CountDownLatch> auditLatchQueue;

	/**
	 * Latch to be decremented when the next audit completes.  May be
	 * {@code null}. Used by JUnit tests to wait for an audit to complete.
	 */
	private CountDownLatch auditCompletionLatch = null;

	/**
	 * AuditThread constructor
	 * @param resourceName
	 * @param persistenceUnit
	 * @param properties
	 * @param integrityAuditPeriodSeconds
	 * @param integrityAudit
	 * @throws Exception
	 */
	public AuditThread(String resourceName, String persistenceUnit,
			Properties properties, int integrityAuditPeriodSeconds, IntegrityAudit integrityAudit)
			throws Exception {
		
		this(resourceName, persistenceUnit, properties, integrityAuditPeriodSeconds,
				TimeUnit.SECONDS, integrityAudit, null);
	}

	/**
	 * AuditThread constructor
	 * @param resourceName
	 * @param persistenceUnit
	 * @param properties
	 * @param integrityAuditPeriod
	 * @param units
	 * @param integrityAudit
	 * @param queue 
	 * @throws Exception
	 */
	public AuditThread(String resourceName, String persistenceUnit,
			Properties properties, int integrityAuditPeriod, TimeUnit units, IntegrityAudit integrityAudit,
			BlockingQueue<CountDownLatch> queue)
			throws Exception {
		this.resourceName = resourceName;
		this.persistenceUnit = persistenceUnit;
		this.properties = properties;
		this.integrityAuditPeriodMillis = units.toMillis(integrityAuditPeriod);
		this.integrityAudit = integrityAudit;
		this.auditLatchQueue = queue;

		/*
		 * The DbDAO Constructor registers this node in the IntegrityAuditEntity
		 * table. Each resource (node) inserts its own name, persistenceUnit, DB
		 * access properties and other pertinent properties in the table. This
		 * allows the audit on each node to compare its own version of the
		 * entities for the persistenceUnit in question with the versions from
		 * all other nodes of similar type.
		 */
		dbDAO = new DbDAO(this.resourceName, this.persistenceUnit,
				this.properties);
		this.nodeType = properties.getProperty(IntegrityAuditProperties.NODE_TYPE);

	}

	@Override
	public void run() {

		logger.info("AuditThread.run: Entering");

		try {			
			/*
			 * For JUnit testing: wait for the first latch, decrement it to
			 * indicate that the thread has started, and then wait for the
			 * next latch, before we actually start doing anything. These
			 * simply return if there is no latch queue defined.
			 */
			getNextLatch();
			decrementLatch();
			getNextLatch();
			
			/*
			 * Triggers change in designation, unless no other viable candidate.
			 */
			boolean auditCompleted = false;

			DbAudit dbAudit = new DbAudit(dbDAO);

			IntegrityAuditEntity entityCurrentlyDesignated;
			IntegrityAuditEntity thisEntity;
			integrityAudit.setThreadInitialized(true); // An exception will set
														// it to false

			while (true) {
				try{
					/*
					 * It may have been awhile since we last cycled through this
					 * loop, so refresh the list of IntegrityAuditEntities.
					 */
					List<IntegrityAuditEntity> integrityAuditEntityList = getIntegrityAuditEntityList();

					/*
					 * We could've set entityCurrentlyDesignated as a side effect of
					 * getIntegrityAuditEntityList(), but then we would've had to
					 * make entityCurrentlyDesignated a class level attribute. Using
					 * this approach, we can keep it local to the run() method.
					 */
					entityCurrentlyDesignated = getEntityCurrentlyDesignated(integrityAuditEntityList);

					/*
					 * Need to refresh thisEntity each time through loop, because we
					 * need a fresh version of lastUpdated.
					 */
					thisEntity = getThisEntity(integrityAuditEntityList);

					/*
					 * If we haven't done the audit yet, note that we're current and
					 * see if we're designated.
					 */
					if (!auditCompleted) {
						dbDAO.setLastUpdated();

						/*
						 * If no current designation or currently designated node is
						 * stale, see if we're the next node to be designated.
						 */
						if (entityCurrentlyDesignated == null
								|| isStale(entityCurrentlyDesignated)) {
							IntegrityAuditEntity designationCandidate = getDesignationCandidate(integrityAuditEntityList);

							/*
							 * If we're the next node to be designated, run the
							 * audit.
							 */
							if (designationCandidate.getResourceName().equals(
									this.resourceName)) {
								runAudit(dbAudit);
								auditCompleted = true;
							} else {
								if (logger.isDebugEnabled()) {
									logger.debug("AuditThread.run: designationCandidate, "
											+ designationCandidate
											.getResourceName()
											+ ", not this entity, "
											+ thisEntity.getResourceName());
								}
							}

							/*
							 * Application may have been stopped and restarted, in
							 * which case we might be designated but auditCompleted
							 * will have been reset to false, so account for this.
							 */
						} else if (thisEntity.getResourceName().equals(
								entityCurrentlyDesignated.getResourceName())) {

							if (logger.isDebugEnabled()) {
								logger.debug("AuditThread.run: Re-running audit for "
										+ thisEntity.getResourceName());
							}
							runAudit(dbAudit);
							auditCompleted = true;

						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("AuditThread.run: Currently designated node, "
										+ entityCurrentlyDesignated
										.getResourceName()
										+ ", not yet stale and not this node");
							}
						}


						/*
						 * Audit already completed on this node, so allow the node
						 * to go stale until twice the AUDIT_COMPLETION_PERIOD has
						 * elapsed. This should give plenty of time for another node
						 * (if another node is out there) to pick up designation.
						 */
					} else {

						auditCompleted = resetAuditCompleted(auditCompleted,
								thisEntity);

					}

					/*
					 * If we've just run audit, sleep per the
					 * integrity_audit_period_seconds property, otherwise just sleep
					 * the normal interval.
					 */
					if (auditCompleted) {
						// indicate that an audit has completed
						decrementLatch();

						// don't start the next audit cycle until a latch has been provided
						getNextLatch();

						if (logger.isDebugEnabled()) {
							logger.debug("AuditThread.run: Audit completed; resourceName="
									+ this.resourceName
									+ " sleeping "
									+ integrityAuditPeriodMillis + "ms");
						}
						Thread.sleep(integrityAuditPeriodMillis);
						if (logger.isDebugEnabled()) {
							logger.debug("AuditThread.run: resourceName="
									+ this.resourceName + " awaking from "
									+ integrityAuditPeriodMillis + "ms sleep");
						}

					} else {

						if (logger.isDebugEnabled()) {
							logger.debug("AuditThread.run: resourceName="
									+ this.resourceName + ": Sleeping "
									+ AuditThread.auditThreadSleepIntervalMillis
									+ "ms");
						}
						Thread.sleep(AuditThread.auditThreadSleepIntervalMillis);
						if (logger.isDebugEnabled()) {
							logger.debug("AuditThread.run: resourceName="
									+ this.resourceName + ": Awaking from "
									+ AuditThread.auditThreadSleepIntervalMillis
									+ "ms sleep");
						}

					}
				
				} catch (Exception e){
					if(isInterruptedException(e)) {
						String msg = "AuditThread.run loop - Exception thrown: " + e.getMessage() 
								+ "; Stopping.";
						logger.error(MessageCodes.EXCEPTION_ERROR, e, msg);
						break;
					}
					
					String msg = "AuditThread.run loop - Exception thrown: " + e.getMessage() 
							+ "; Will try audit again in " + (integrityAuditPeriodMillis/1000) + " seconds";
					logger.error(MessageCodes.EXCEPTION_ERROR, e, msg);
					// Sleep and try again later
					Thread.sleep(integrityAuditPeriodMillis);
				}

			}

		} catch (Exception e) {
			String msg = "AuditThread.run: Could not start audit loop. Exception thrown; message="+ e.getMessage();
			logger.error(MessageCodes.EXCEPTION_ERROR, e, msg);
			integrityAudit.setThreadInitialized(false);
		}
		
		dbDAO.destroy();

		logger.info("AuditThread.run: Exiting");
	}

	/**
	 * Gets the next audit-completion latch from the queue. Blocks, if the
	 * queue is empty.
	 * @throws InterruptedException
	 */
	private void getNextLatch() throws InterruptedException {
		BlockingQueue<CountDownLatch> queue = this.auditLatchQueue;
		if(queue != null) {
			this.auditCompletionLatch = queue.take();
		}
	}

	/**
	 * Decrements the current audit-completion latch, if any.
	 */
	private void decrementLatch() {
		CountDownLatch latch = this.auditCompletionLatch;
		if(latch != null) {
			this.auditCompletionLatch = null;
			latch.countDown();
		}
	}

	/**
	 * Determines if an exception is an InterruptedException or was caused
	 * by an InterruptedException.
	 * @param ex	exception to be examined
	 * @return {@code true} if it's an InterruptedException, {@code false} otherwise
	 */
	private boolean isInterruptedException(Throwable ex) {
		while(ex != null) {
			if(ex instanceof InterruptedException) {
				return true;
			}
			
			ex = ex.getCause();
		}
		
		return false;
	}

	/*
	 * Used to create a list that is sorted lexicographically by resourceName.
	 */
	Comparator<IntegrityAuditEntity> comparator = new Comparator<IntegrityAuditEntity>() {
		@Override
		public int compare(final IntegrityAuditEntity r1,
				final IntegrityAuditEntity r2) {
			return r1.getResourceName().compareTo(r2.getResourceName());
		}
	};

	/**
	 * getDesignationCandidate()
	 * Using round robin algorithm, gets next candidate to be designated. Assumes
	 * list is sorted lexicographically by resourceName.
	 */
	private IntegrityAuditEntity getDesignationCandidate(
			List<IntegrityAuditEntity> integrityAuditEntityList) {
		
		//Note: assumes integrityAuditEntityList is already lexicographically sorted by resourceName

		if (logger.isDebugEnabled()) {
			logger.debug("getDesignationCandidate: Entering, integrityAuditEntityList.size()="
					+ integrityAuditEntityList.size());
		}

		IntegrityAuditEntity designationCandidate;
		IntegrityAuditEntity thisEntity = null;

		int designatedEntityIndex = -1;
		int entityIndex = 0;
		int priorCandidateIndex = -1;
		int subsequentCandidateIndex = -1;

		for (IntegrityAuditEntity integrityAuditEntity : integrityAuditEntityList) {

			if (logger.isDebugEnabled()) {
				logIntegrityAuditEntity(integrityAuditEntity);
			}

			if (integrityAuditEntity.getResourceName()
					.equals(this.resourceName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("getDesignationCandidate: thisEntity="
							+ integrityAuditEntity.getResourceName());
				}
				thisEntity = integrityAuditEntity;
			}

			if (integrityAuditEntity.isDesignated()) {
				if (logger.isDebugEnabled()) {
					logger.debug("getDesignationCandidate: Currently designated entity resourceName="
							+ integrityAuditEntity.getResourceName()
							+ ", persistenceUnit="
							+ integrityAuditEntity.getPersistenceUnit()
							+ ", lastUpdated="
							+ integrityAuditEntity.getLastUpdated()
							+ ", entityIndex=" + entityIndex);
				}
				designatedEntityIndex = entityIndex;

				/*
				 * Entity not currently designated
				 */
			} else {

				/*
				 * See if non-designated entity is stale.
				 */
				if (isStale(integrityAuditEntity)) {

					if (logger.isDebugEnabled()) {
						logger.debug("getDesignationCandidate: Entity is stale; resourceName="
								+ integrityAuditEntity.getResourceName()
								+ ", persistenceUnit="
								+ integrityAuditEntity.getPersistenceUnit()
								+ ", lastUpdated="
								+ integrityAuditEntity.getLastUpdated()
								+ ", entityIndex=" + entityIndex);
					}

					/*
					 * Entity is current.
					 */
				} else {

					if (designatedEntityIndex == -1) {

						if (priorCandidateIndex == -1) {
							if (logger.isDebugEnabled()) {
								logger.debug("getDesignationCandidate: Prior candidate found, resourceName="
										+ integrityAuditEntity
												.getResourceName()
										+ ", persistenceUnit="
										+ integrityAuditEntity
												.getPersistenceUnit()
										+ ", lastUpdated="
										+ integrityAuditEntity.getLastUpdated()
										+ ", entityIndex=" + entityIndex);
							}
							priorCandidateIndex = entityIndex;
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("getDesignationCandidate: Prior entity current but prior candidate already found; resourceName="
										+ integrityAuditEntity
												.getResourceName()
										+ ", persistenceUnit="
										+ integrityAuditEntity
												.getPersistenceUnit()
										+ ", lastUpdated="
										+ integrityAuditEntity.getLastUpdated()
										+ ", entityIndex=" + entityIndex);
							}
						}
					} else {
						if (subsequentCandidateIndex == -1) {
							if (logger.isDebugEnabled()) {
								logger.debug("getDesignationCandidate: Subsequent candidate found, resourceName="
										+ integrityAuditEntity
												.getResourceName()
										+ ", persistenceUnit="
										+ integrityAuditEntity
												.getPersistenceUnit()
										+ ", lastUpdated="
										+ integrityAuditEntity.getLastUpdated()
										+ ", entityIndex=" + entityIndex);
							}
							subsequentCandidateIndex = entityIndex;
						} else {
							if (logger.isDebugEnabled()) {
								logger.debug("getDesignationCandidate: Subsequent entity current but subsequent candidate already found; resourceName="
										+ integrityAuditEntity
												.getResourceName()
										+ ", persistenceUnit="
										+ integrityAuditEntity
												.getPersistenceUnit()
										+ ", lastUpdated="
										+ integrityAuditEntity.getLastUpdated()
										+ ", entityIndex=" + entityIndex);
							}
						}
					}

				} // end entity is current

			} // end entity not currently designated

			entityIndex++;

		} // end for loop

		/*
		 * Per round robin algorithm, if a current entity is found that is
		 * lexicographically after the currently designated entity, this entity
		 * becomes the designation candidate. If no current entity is found that
		 * is lexicographically after currently designated entity, we cycle back
		 * to beginning of list and pick the first current entity as the
		 * designation candidate.
		 */
		if (subsequentCandidateIndex != -1) {
			designationCandidate = integrityAuditEntityList
					.get(subsequentCandidateIndex);
			if (logger.isDebugEnabled()) {
				logger.debug("getDesignationCandidate: Exiting and returning subsequent designationCandidate="
						+ designationCandidate.getResourceName());
			}
		} else {
			if (priorCandidateIndex != -1) {
				designationCandidate = integrityAuditEntityList
						.get(priorCandidateIndex);
				if (logger.isDebugEnabled()) {
					logger.debug("getDesignationCandidate: Exiting and returning prior designationCandidate="
							+ designationCandidate.getResourceName());
				}
			} else {
				logger.debug("getDesignationCandidate: No subsequent or prior candidate found; designating thisEntity, resourceName="
						+ thisEntity.getResourceName());
				designationCandidate = thisEntity;
			}
		}

		return designationCandidate;

	}

	/**
	 * getEntityCurrentlyDesignated()
	 * Returns entity that is currently designated.
	 * @param integrityAuditEntityList
	 * @return
	 */
	private IntegrityAuditEntity getEntityCurrentlyDesignated(
			List<IntegrityAuditEntity> integrityAuditEntityList) {

		if (logger.isDebugEnabled()) {
			logger.debug("getEntityCurrentlyDesignated: Entering, integrityAuditEntityList.size="
					+ integrityAuditEntityList.size());
		}

		IntegrityAuditEntity entityCurrentlyDesignated = null;

		for (IntegrityAuditEntity integrityAuditEntity : integrityAuditEntityList) {

			if (integrityAuditEntity.isDesignated()) {
				if (logger.isDebugEnabled()) {
					logger.debug("getEntityCurrentlyDesignated: Currently designated entity resourceName="
							+ integrityAuditEntity.getResourceName()
							+ ", persistenceUnit="
							+ integrityAuditEntity.getPersistenceUnit()
							+ ", lastUpdated="
							+ integrityAuditEntity.getLastUpdated());
				}
				entityCurrentlyDesignated = integrityAuditEntity;
			}

		} // end for loop

		if (logger.isDebugEnabled()) {
			if (entityCurrentlyDesignated != null) {
				logger.debug("getEntityCurrentlyDesignated: Exiting and returning entityCurrentlyDesignated="
						+ entityCurrentlyDesignated.getResourceName());
			} else {
				logger.debug("getEntityCurrentlyDesignated: Exiting and returning entityCurrentlyDesignated="
						+ entityCurrentlyDesignated);
			}
		}
		return entityCurrentlyDesignated;

	}

	/**
	 * getIntegrityAuditEnityList gets the list of IntegrityAuditEntity 
	 * @return
	 * @throws DbDaoTransactionException
	 */
	private List<IntegrityAuditEntity> getIntegrityAuditEntityList()
			throws DbDaoTransactionException {

		if (logger.isDebugEnabled()) {
			logger.debug("getIntegrityAuditEntityList: Entering");
		}

		/*
		 * Get all records for this nodeType and persistenceUnit and then sort
		 * them lexicographically by resourceName. Get index of designated
		 * entity, if any.
		 */
		/*
		 * Sorted list of entities for a particular nodeType and
		 * persistenceUnit.
		 */
		List<IntegrityAuditEntity> integrityAuditEntityList = dbDAO.getIntegrityAuditEntities(
				this.persistenceUnit, this.nodeType);
		int listSize = integrityAuditEntityList.size();
		if (logger.isDebugEnabled()) {
			logger.debug("getIntegrityAuditEntityList: Got " + listSize
					+ " IntegrityAuditEntity records");
		}
		Collections.sort((List<IntegrityAuditEntity>) integrityAuditEntityList,
				comparator);

		if (logger.isDebugEnabled()) {
			logger.debug("getIntegrityAuditEntityList: Exiting and returning integrityAuditEntityList, size="
					+ listSize);
		}
		return integrityAuditEntityList;

	}


	/**
	 * Returns the IntegrityAuditEntity for this entity.
	 * @param integrityAuditEntityList
	 * @return
	 */
	private IntegrityAuditEntity getThisEntity(
			List<IntegrityAuditEntity> integrityAuditEntityList) {

		if (logger.isDebugEnabled()) {
			logger.debug("getThisEntity: Entering, integrityAuditEntityList.size="
					+ integrityAuditEntityList.size());
		}

		IntegrityAuditEntity thisEntity = null;

		for (IntegrityAuditEntity integrityAuditEntity : integrityAuditEntityList) {

			if (integrityAuditEntity.getResourceName().equals(this.resourceName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("getThisEntity: For this entity, resourceName="
							+ integrityAuditEntity.getResourceName()
							+ ", persistenceUnit="
							+ integrityAuditEntity.getPersistenceUnit()
							+ ", lastUpdated="
							+ integrityAuditEntity.getLastUpdated());
				}
				thisEntity = integrityAuditEntity;
			}

		} // end for loop

		if (logger.isDebugEnabled()) {
			if (thisEntity != null) {
				logger.debug("getThisEntity: Exiting and returning thisEntity="
						+ thisEntity.getResourceName());
			} else {
				logger.debug("getThisEntity: Exiting and returning thisEntity="
						+ thisEntity);
			}
		}
		return thisEntity;

	}


	/**
	 * Returns false if the lastUpdated time for the record in question is more
	 * than auditCompletionIntervalMillis seconds ago. During an audit, lastUpdated is updated every five
	 * seconds or so, but when an audit finishes, the node doing the audit stops
	 * updating lastUpdated.
	 * @param integrityAuditEntity
	 * @return
	 */
	private boolean isStale(IntegrityAuditEntity integrityAuditEntity) {

		if (logger.isDebugEnabled()) {
			logger.debug("isStale: Entering, resourceName="
					+ integrityAuditEntity.getResourceName()
					+ ", persistenceUnit="
					+ integrityAuditEntity.getPersistenceUnit()
					+ ", lastUpdated=" + integrityAuditEntity.getLastUpdated());
		}

		boolean stale = false;

		Date currentTime = new Date();
		Date lastUpdated = integrityAuditEntity.getLastUpdated();

		/*
		 * If lastUpdated is null, we assume that the audit never ran for that
		 * node.
		 */
		long lastUpdatedTime = 0;
		if (lastUpdated != null) {
			lastUpdatedTime = lastUpdated.getTime();
		}
		long timeDifference = currentTime.getTime() - lastUpdatedTime;
		if (timeDifference > auditCompletionIntervalMillis) {
			stale = true;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("isStale: Exiting and returning stale=" + stale
					+ ", timeDifference=" + timeDifference);
		}

		return stale;
	}

	private void logIntegrityAuditEntity(
			IntegrityAuditEntity integrityAuditEntity) {

		logger.debug("logIntegrityAuditEntity: id="
				+ integrityAuditEntity.getId() + ", jdbcDriver="
				+ integrityAuditEntity.getJdbcDriver() + ", jdbcPassword="
				+ integrityAuditEntity.getJdbcPassword() + ", jdbcUrl="
				+ integrityAuditEntity.getJdbcUrl() + ", jdbcUser="
				+ integrityAuditEntity.getJdbcUser() + ", nodeType="
				+ integrityAuditEntity.getNodeType() + ", persistenceUnit="
				+ integrityAuditEntity.getPersistenceUnit() + ", resourceName="
				+ integrityAuditEntity.getResourceName() + ", site="
				+ integrityAuditEntity.getSite() + ", createdDate="
				+ integrityAuditEntity.getCreatedDate() + ", lastUpdated="
				+ integrityAuditEntity.getLastUpdated() + ", designated="
				+ integrityAuditEntity.isDesignated());
	}
	
	/*
	 * If more than (auditCompletionIntervalMillis * 2) milliseconds have elapsed
	 * since we last ran the audit, reset auditCompleted, so
	 * 
	 * 1) we'll eventually re-run the audit, if no other node picks up the
	 * designation.
	 * 
	 * or
	 * 
	 * 2) We'll run the audit when the round robin comes back to us.
	 */
	private boolean resetAuditCompleted(boolean auditCompleted,
			IntegrityAuditEntity thisEntity) {

		if (logger.isDebugEnabled()) {
			logger.debug("resetAuditCompleted: auditCompleted="
					+ auditCompleted + "; for thisEntity, resourceName="
					+ thisEntity.getResourceName() + ", persistenceUnit="
					+ thisEntity.getPersistenceUnit() + ", lastUpdated="
					+ thisEntity.getLastUpdated());
		}

		long timeDifference;

		Date currentTime = new Date();
		Date lastUpdated = thisEntity.getLastUpdated();

		long lastUpdatedTime = lastUpdated.getTime();
		timeDifference = currentTime.getTime() - lastUpdatedTime;

		if (timeDifference > (auditCompletionIntervalMillis * 2)) {
			if (logger.isDebugEnabled()) {
				logger.debug("resetAuditCompleted: Resetting auditCompleted for resourceName="
						+ this.resourceName);
			}
			auditCompleted = false;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("resetAuditCompleted: For resourceName="
						+ resourceName
						+ ", time since last update is only "
						+ timeDifference + "; retaining current value for auditCompleted");
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("resetAuditCompleted: Exiting and returning auditCompleted="
					+ auditCompleted + ", timeDifference=" + timeDifference);
		}
		return auditCompleted;
	}

	private void runAudit(DbAudit dbAudit) throws Exception {

		if (logger.isDebugEnabled()) {
			logger.debug("runAudit: Entering, dbAudit=" + dbAudit
					+ "; notifying other resources that resourceName="
					+ this.resourceName + " is current");
		}

		/*
		 * changeDesignated marks all other nodes as non-designated and this
		 * node as designated.
		 */
		dbDAO.changeDesignated(this.resourceName, this.persistenceUnit,
				this.nodeType);

		if (logger.isDebugEnabled()) {
			logger.debug("runAudit: Running audit for persistenceUnit="
					+ this.persistenceUnit + " on resourceName="
					+ this.resourceName);
		}
		if (IntegrityAudit.isUnitTesting()) {
			dbAudit.dbAuditSimulate(this.resourceName, this.persistenceUnit, AuditThread.AUDIT_SIMULATION_ITERATIONS, AuditThread.auditThreadSleepIntervalMillis);
		} else {
			dbAudit.dbAudit(this.resourceName, this.persistenceUnit,
					this.nodeType);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("runAudit: Exiting");
		}

	}

	/**
	 * Adjusts the thread-sleep-interval to be used when an audit has
	 * <i>not</i> been completed.  Used by JUnit tests.
	 * @param auditThreadSleepIntervalMillis
	 */
	protected static void setAuditThreadSleepIntervalMillis(long auditThreadSleepIntervalMillis) {
		AuditThread.auditThreadSleepIntervalMillis = auditThreadSleepIntervalMillis;
	}

	/**
	 * Gets the current thread-sleep-interval to be used when an audit has
	 * <i>not</i> been completed.  Used by JUnit tests.
	 * @return the current sleep interval, in milli-seconds
	 */
	protected static long getAuditThreadSleepIntervalMillis() {
		return auditThreadSleepIntervalMillis;
	}

	/**
	 * Adjusts the audit-completion-interval.  Used by JUnit tests.
	 * @param auditThreadSleepIntervalMillis
	 */
	protected static void setAuditCompletionIntervalMillis(long auditThreadSleepIntervalMillis) {
		AuditThread.auditCompletionIntervalMillis = auditThreadSleepIntervalMillis;
	}

	/**
	 * Gets the audit-completion-interval.  Used by JUnit tests.
	 * @return the current audit-completion interval, in milli-seconds
	 */
	protected static long getAuditCompletionIntervalMillis() {
		return auditCompletionIntervalMillis;
	}

}
