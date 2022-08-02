/*
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import org.onap.policy.common.ia.jpa.IntegrityAuditEntity;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;

/**
 * class DbDao provides the inteface to the DBs for the purpose of audits.
 *
 */
public class DbDao {
    private static final Logger logger = FlexLogger.getLogger();

    private String resourceName;
    private String persistenceUnit;
    private String dbDriver;
    private String dbUrl;
    private String dbUser;
    private String siteName;
    private String nodeType;
    private Properties properties = null;

    private EntityManagerFactory emf;

    /*
     * Supports designation serialization.
     */
    private static final Object lock = new Object();

    /*
     * Common strings.
     */
    private static final String RESOURCE_MESSAGE = "Resource: ";
    private static final String WITH_PERSISTENCE_MESSAGE = " with PersistenceUnit: ";
    private static final String DBDAO_MESSAGE = "DbDao: ";
    private static final String ENCOUNTERED_MESSAGE = "ecountered a problem in execution: ";

    /*
     * DB SELECT String.
     */
    private static final String SELECT_STRING = "Select i from IntegrityAuditEntity i "
            + "where i.resourceName=:rn and i.persistenceUnit=:pu";

    /**
     * DbDao Constructor.
     *
     * @param resourceName the resource name
     * @param persistenceUnit the persistence unit
     * @param properties the properties
     * @throws IntegrityAuditException if an error occurs
     */
    public DbDao(String resourceName, String persistenceUnit, Properties properties) throws IntegrityAuditException {
        this(resourceName, persistenceUnit, properties, null);
    }

    /**
     * DbDao Constructor.
     *
     * @param resourceName the resource name
     * @param persistenceUnit the persistence unit
     * @param properties the properties
     * @param altDbUrl may be {@code null}
     * @throws IntegrityAuditException if an error occurs
     */
    protected DbDao(String resourceName, String persistenceUnit, Properties properties, String altDbUrl)
            throws IntegrityAuditException {
        logger.debug("DbDao contructor: enter");

        validateProperties(resourceName, persistenceUnit, properties);

        emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);

        register(altDbUrl);

        logger.debug("DbDao contructor: exit");
    }

    /**
     * Release resources (i.e., the EntityManagerFactory).
     */
    public void destroy() {
        emf.close();
    }

    /**
     * validateProperties will validate the properties.
     *
     * @param resourceName the rseource name
     * @param persistenceUnit the persistence unit
     * @param properties the properties
     * @throws IntegrityAuditPropertiesException if an error occurs
     */
    private void validateProperties(String resourceName, String persistenceUnit, Properties properties)
            throws IntegrityAuditPropertiesException {
        StringBuilder badparams = new StringBuilder();
        if (IntegrityAudit.parmsAreBad(resourceName, persistenceUnit, properties, badparams)) {
            String msg = "DbDao: Bad parameters: badparams" + badparams;
            throw new IntegrityAuditPropertiesException(msg);
        }
        this.resourceName = resourceName;
        this.persistenceUnit = persistenceUnit;
        this.dbDriver = properties.getProperty(IntegrityAuditProperties.DB_DRIVER).trim();
        this.dbUrl = properties.getProperty(IntegrityAuditProperties.DB_URL).trim();
        this.dbUser = properties.getProperty(IntegrityAuditProperties.DB_USER).trim();
        this.siteName = properties.getProperty(IntegrityAuditProperties.SITE_NAME).trim();
        this.nodeType = properties.getProperty(IntegrityAuditProperties.NODE_TYPE).trim().toLowerCase();
        this.properties = properties;
        logger.debug("DbDao.assignProperties: exit:" + "\nresourceName: " + this.resourceName + "\npersistenceUnit: "
                + this.persistenceUnit + "\nproperties: " + this.properties);
    }

    /**
     * getAllMyEntries gets all the DB entries for a particular class.
     *
     * @param className the class name
     * @return all the DB entries for the given class
     */
    public Map<Object, Object> getAllMyEntries(String className) {
        logger.debug("getAllMyEntries: Entering, className=" + className);
        HashMap<Object, Object> resultMap = new HashMap<>();
        EntityManager em = emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Object> cq = cb.createQuery();
            Root<?> rootEntry = cq.from(Class.forName(className));
            CriteriaQuery<Object> all = cq.select(rootEntry);
            TypedQuery<Object> allQuery = em.createQuery(all);
            List<Object> objectList = allQuery.getResultList();
            // Now create the map

            PersistenceUnitUtil util = emf.getPersistenceUnitUtil();
            for (Object o : objectList) {
                Object key = util.getIdentifier(o);
                resultMap.put(key, o);
            }
        } catch (Exception e) {
            logger.error("getAllEntries encountered exception: ", e);
        }
        em.close();
        logger.debug("getAllMyEntries: Exit, resultMap.keySet()=" + resultMap.keySet());
        return resultMap;
    }

    /**
     * getAllMyEntries gets all entries for a class.
     *
     * @param className the name of the class
     * @param keySet the keys to get the entries for
     * @return the map of requested entries
     */
    public Map<Object, Object> getAllMyEntries(String className, Set<Object> keySet) {
        logger.debug("getAllMyEntries: Entering, className=" + className + ",\n keySet=" + keySet);

        HashMap<Object, Object> resultMap = new HashMap<>();
        EntityManager em = emf.createEntityManager();
        try {
            Class<?> clazz = Class.forName(className);
            for (Object key : keySet) {
                Object entry = em.find(clazz, key);
                resultMap.put(key, entry);
            }
        } catch (Exception e) {
            logger.error("getAllMyEntries encountered exception: ", e);
        }
        em.close();

        logger.debug("getAllMyEntries: Returning resultMap, size=" + resultMap.size());
        return resultMap;
    }

    /**
     * getAllEntries gets all entriesfor a particular persistence unit adn className.
     *
     * @param persistenceUnit the persistence unit
     * @param properties the properties
     * @param className the class name
     * @return the map of entries
     */
    public Map<Object, Object> getAllEntries(String persistenceUnit, Properties properties, String className) {

        logger.debug("getAllEntries: Entering, persistenceUnit=" + persistenceUnit + ",\n className=" + className);
        HashMap<Object, Object> resultMap = new HashMap<>();

        EntityManagerFactory theEmf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
        EntityManager em = theEmf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Object> cq = cb.createQuery();
            Root<?> rootEntry = cq.from(Class.forName(className));
            CriteriaQuery<Object> all = cq.select(rootEntry);
            TypedQuery<Object> allQuery = em.createQuery(all);
            List<Object> objectList = allQuery.getResultList();

            PersistenceUnitUtil util = theEmf.getPersistenceUnitUtil();
            for (Object o : objectList) {
                Object key = util.getIdentifier(o);
                resultMap.put(key, o);
            }
        } catch (Exception e) {
            logger.error("getAllEntries encountered exception:", e);
        }
        em.close();
        theEmf.close();

        logger.debug("getAllEntries: Returning resultMap, size=" + resultMap.size());

        return resultMap;
    }


    /**
     * getAllEntries gets all entries for a persistence unit.
     *
     * @param persistenceUnit the persistence unit
     * @param properties the properties
     * @param className the class name
     * @param keySet the keys
     * @return the map of entries
     */

    public Map<Object, Object> getAllEntries(String persistenceUnit, Properties properties, String className,
            Set<Object> keySet) {
        logger.debug("getAllEntries: Entering, persistenceUnit=" + persistenceUnit + ",\n properties= " + properties
                + ",\n className=" + className + ",\n keySet= " + keySet);
        EntityManagerFactory theEmf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
        EntityManager em = theEmf.createEntityManager();
        HashMap<Object, Object> resultMap = new HashMap<>();
        try {
            Class<?> clazz = Class.forName(className);
            for (Object key : keySet) {
                Object entry = em.find(clazz, key);
                resultMap.put(key, entry);
            }
        } catch (Exception e) {
            String msg = "getAllEntries encountered exception: " + e;
            logger.error(msg, e);
        }
        em.close();
        theEmf.close();
        logger.debug("getAllEntries: Exit, resultMap, size=" + resultMap.size());
        return resultMap;
    }

    /**
     * getIntegrityAuditEntities() Get all the IntegrityAuditEntities for a particular persistence
     * unit and node type.
     *
     * @param persistenceUnit the persistence unit
     * @param nodeType the node type
     * @return the list of IntegrityAuditEntity
     * @throws DbDaoTransactionException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public List<IntegrityAuditEntity> getIntegrityAuditEntities(String persistenceUnit, String nodeType)
            throws DbDaoTransactionException {
        logger.debug("getIntegrityAuditEntities: Entering, persistenceUnit=" + persistenceUnit + ",\n nodeType= "
                + nodeType);
        try {
            EntityManager em = emf.createEntityManager();
            // Start a transaction
            EntityTransaction et = em.getTransaction();

            et.begin();

            // if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not
            // found, create a new entry
            Query iaequery = em
                    .createQuery("Select i from IntegrityAuditEntity i where i.persistenceUnit=:pu and i.nodeType=:nt");
            iaequery.setParameter("pu", persistenceUnit);
            iaequery.setParameter("nt", nodeType);

            List<IntegrityAuditEntity> iaeList = iaequery.getResultList();

            // commit transaction
            et.commit();
            em.close();
            logger.debug("getIntegrityAuditEntities: Exit, iaeList=" + iaeList);
            return iaeList;
        } catch (Exception e) {
            String msg = DBDAO_MESSAGE + "getIntegrityAuditEntities() " + ENCOUNTERED_MESSAGE;
            logger.error(msg, e);
            throw new DbDaoTransactionException(e);
        }

    }

    /**
     * getMyIntegrityAuditEntity() gets my IntegrityAuditEntity.
     *
     * @return the IntegrityAuditEntity
     * @throws DbDaoTransactionException if an error occurs
     */
    public IntegrityAuditEntity getMyIntegrityAuditEntity() throws DbDaoTransactionException {

        return updateIae("getMyIntegrityAuditEntity", this.resourceName, this.persistenceUnit, (em, iae) -> {

            if (iae != null) {
                // refresh the object from DB in case cached data was returned
                em.refresh(iae);
                logger.info(RESOURCE_MESSAGE + this.resourceName + WITH_PERSISTENCE_MESSAGE + this.persistenceUnit
                        + " exists");
            } else {
                // If it does not exist, log an error
                logger.error("Attempting to setLastUpdated" + " on an entry that does not exist: resource "
                        + this.resourceName + WITH_PERSISTENCE_MESSAGE + this.persistenceUnit);
            }
        });
    }


    /**
     * getIntegrityAuditEntity() gets the IntegrityAuditEntity with a particular ID.
     *
     * @param id the ID
     * @return the IntegrityAuditEntity
     * @throws DbDaoTransactionException if an error occurs
     */
    public IntegrityAuditEntity getIntegrityAuditEntity(long id) throws DbDaoTransactionException {
        try {
            EntityManager em = emf.createEntityManager();

            // Start a transaction
            EntityTransaction et = em.getTransaction();

            et.begin();

            IntegrityAuditEntity iae = em.find(IntegrityAuditEntity.class, id);

            et.commit();
            em.close();

            return iae;
        } catch (Exception e) {
            String msg = DBDAO_MESSAGE + "getIntegrityAuditEntity() " + ENCOUNTERED_MESSAGE;
            logger.error(msg + e);
            throw new DbDaoTransactionException(e);
        }
    }

    /**
     * getPersistenceClassNames() gets all the persistence class names.
     *
     * @return the persistence class names
     */
    public Set<String> getPersistenceClassNames() {
        logger.debug("DbDao: getPersistenceClassNames() entry");
        HashSet<String> returnList = new HashSet<>();
        final Metamodel mm = emf.getMetamodel();
        logger.debug("\n" + persistenceUnit + " persistence unit classes:");
        for (final ManagedType<?> managedType : mm.getManagedTypes()) {
            Class<?> clazz = managedType.getJavaType();
            logger.debug("    " + clazz.getSimpleName());
            returnList.add(clazz.getName()); // the full class name needed to make a query using jpa
        }
        logger.debug("DbDao: getPersistenceClassNames() exit");
        return returnList;
    }

    /**
     * Register the IntegrityAudit instance.
     *
     * @param altDbUrl alternate DB URL to be placed into the record, or {@code null} to use the
     *        default
     */
    private void register(String altDbUrl) throws DbDaoTransactionException {

        updateIae("register", this.resourceName, this.persistenceUnit, (em, iae) -> {
            IntegrityAuditEntity iae2 = iae;

            // If it already exists, we just want to update the properties and lastUpdated date
            if (iae2 != null) {
                // refresh the object from DB in case cached data was returned
                em.refresh(iae2);
                logger.info(RESOURCE_MESSAGE + this.resourceName + WITH_PERSISTENCE_MESSAGE + this.persistenceUnit
                        + " exists and entry be updated");
            } else {
                // If it does not exist, we also must add teh resourceName, persistenceUnit and
                // designated values
                logger.info("Adding resource " + resourceName + WITH_PERSISTENCE_MESSAGE + this.persistenceUnit
                        + " to IntegrityAuditEntity table");
                iae2 = new IntegrityAuditEntity();
                iae2.setResourceName(this.resourceName);
                iae2.setPersistenceUnit(this.persistenceUnit);
                iae2.setDesignated(false);
            }

            register2(altDbUrl, em, iae2);
        });

    }

    private void register2(String altDbUrl, EntityManager em, IntegrityAuditEntity iae) {
        // update/set properties in entry
        iae.setSite(this.siteName);
        iae.setNodeType(this.nodeType);
        iae.setJdbcDriver(this.dbDriver);
        iae.setJdbcPassword(properties.getProperty(IntegrityAuditProperties.DB_PWD).trim());
        iae.setJdbcUrl(altDbUrl == null ? this.dbUrl : altDbUrl);
        iae.setJdbcUser(dbUser);

        em.persist(iae);
        // flush to the DB
        em.flush();
    }

    public void setDesignated(boolean designated) throws DbDaoTransactionException {
        setDesignated(this.resourceName, this.persistenceUnit, designated);
    }

    /**
     * Set designated.
     *
     * @param resourceName the resource name
     * @param persistenceUnit the persistence unit
     * @param desig true if is designated
     * @throws DbDaoTransactionException if an error occurs
     */
    public void setDesignated(String resourceName, String persistenceUnit, boolean desig)
            throws DbDaoTransactionException {
        logger.debug("setDesignated: enter, resourceName: " + resourceName + ", persistenceUnit: " + persistenceUnit
                + ", designated: " + desig);

        updateIae("setDesignated", resourceName, persistenceUnit, (em, iae) -> {

            if (iae != null) {
                // refresh the object from DB in case cached data was returned
                em.refresh(iae);
                logger.info(RESOURCE_MESSAGE + resourceName + WITH_PERSISTENCE_MESSAGE + persistenceUnit
                        + " exists and designated be updated");
                iae.setDesignated(desig);

                em.persist(iae);
                // flush to the DB
                em.flush();
            } else {
                // If it does not exist, log an error
                logger.error("Attempting to setDesignated(" + desig + ") on an entry that does not exist:"
                        + " resource " + resourceName + WITH_PERSISTENCE_MESSAGE + persistenceUnit);
            }
        });

    }

    /**
     * Queries for an audit entity and then updates it using an "updater" function.
     *
     * @param methodName name of the method that invoked this
     * @param resourceName the resource name
     * @param persistenceUnit the persistence unit
     * @param updater function to update the entity; the argument will be the entity to be
     *        updated, or {@code null} if the entity is not found
     * @return the entity that was found, or {@code null} if the entity is not found
     * @throws DbDaoTransactionException if an error occurs
     */
    private IntegrityAuditEntity updateIae(String methodName, String resourceName, String persistenceUnit,
                    BiConsumer<EntityManager, IntegrityAuditEntity> updater) throws DbDaoTransactionException {
        try {

            EntityManager em = emf.createEntityManager();

            // Start a transaction
            EntityTransaction et = em.getTransaction();

            et.begin();

            // if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not
            // found, create a new entry
            TypedQuery<IntegrityAuditEntity> iaequery = em.createQuery(SELECT_STRING, IntegrityAuditEntity.class);
            iaequery.setParameter("rn", resourceName);
            iaequery.setParameter("pu", persistenceUnit);

            List<IntegrityAuditEntity> iaeList = iaequery.getResultList();
            IntegrityAuditEntity iae;

            if (!iaeList.isEmpty()) {
                // ignores multiple results
                iae = iaeList.get(0);

            } else {
                // If it does not exist
                iae = null;
            }

            updater.accept(em, iae);

            // close the transaction
            et.commit();
            // close the EntityManager
            em.close();

            return iae;

        } catch (Exception e) {
            String msg = DBDAO_MESSAGE + methodName + "() " + ENCOUNTERED_MESSAGE;
            logger.error(msg + e);
            throw new DbDaoTransactionException(e);
        }

    }

    /**
     * Set last updated.
     *
     * @throws DbDaoTransactionException if an error occurs
     */
    public void setLastUpdated() throws DbDaoTransactionException {
        logger.debug("setLastUpdated: enter, resourceName: " + this.resourceName + ", persistenceUnit: "
                + this.persistenceUnit);

        updateIae("setLastUpdated", this.resourceName, this.persistenceUnit, (em, iae) -> {

            if (iae != null) {
                // refresh the object from DB in case cached data was returned
                em.refresh(iae);
                logger.info(RESOURCE_MESSAGE + this.resourceName + WITH_PERSISTENCE_MESSAGE + this.persistenceUnit
                        + " exists and lastUpdated be updated");
                iae.setLastUpdated(AuditorTime.getInstance().getDate());

                em.persist(iae);
                // flush to the DB
                em.flush();
            } else {
                // If it does not exist, log an error
                logger.error("Attempting to setLastUpdated" + " on an entry that does not exist:" + " resource "
                        + this.resourceName + WITH_PERSISTENCE_MESSAGE + this.persistenceUnit);
            }
        });
    }

    /**
     * Normally this method should only be used in a JUnit test environment. Manually deletes all
     * PDP records in droolspdpentity table.
     */
    public int deleteAllIntegrityAuditEntities() throws DbDaoTransactionException {

        try {

            if (!IntegrityAudit.isUnitTesting()) {
                String msg = DBDAO_MESSAGE + "deleteAllIntegrityAuditEntities() "
                        + "should only be invoked during JUnit testing";
                logger.error(msg);
                throw new DbDaoTransactionException(msg);
            }

            EntityManager em = emf.createEntityManager();
            // Start a transaction
            EntityTransaction et = em.getTransaction();

            et.begin();

            // if IntegrityAuditEntity entry exists for resourceName and PU, update it. If not
            // found, create a new entry
            Query iaequery = em.createQuery("Delete from IntegrityAuditEntity");

            int returnCode = iaequery.executeUpdate();

            // commit transaction
            et.commit();
            em.close();

            logger.info("deleteAllIntegrityAuditEntities: returnCode=" + returnCode);

            return returnCode;

        } catch (Exception e) {
            String msg = DBDAO_MESSAGE + "deleteAllIntegrityAuditEntities() " + "encountered a problem in execution: ";
            logger.error(msg + e);
            throw new DbDaoTransactionException(e);
        }

    }

    /**
     * Changes designation to specified resourceName
     *
     * <p>static lock object in conjunction with synchronized keyword ensures that designation
     * changes are done serially within a resource. I.e. static lock ensures that multiple
     * instantiations of DbDao don't interleave changeDesignated() invocations and potentially
     * produce simultaneous designations.
     *
     * <p>Optimistic locking (the default, versus pessimistic) is sufficient to avoid simultaneous
     * designations from interleaved changeDesignated() invocations from different resources
     * (entities), because it prevents "dirty" and "non-repeatable" reads.
     *
     * <p>See http://www.objectdb.com/api/java/jpa/LockModeType
     *
     * <p>and
     *
     * <p>http://stackoverflow.com/questions/2120248/how-to-synchronize-a-static-
     * variable-among-threads-running-different-instances-o
     */
    public void changeDesignated(String resourceName, String persistenceUnit, String nodeType)
            throws DbDaoTransactionException {

        if (logger.isDebugEnabled()) {
            logger.debug("changeDesignated: Entering, resourceName=" + resourceName + ", persistenceUnit="
                    + persistenceUnit + ", nodeType=" + nodeType);
        }

        long startTime = AuditorTime.getInstance().getMillis();

        synchronized (lock) {

            EntityManager em = null;
            try {

                em = emf.createEntityManager();
                em.getTransaction().begin();

                /*
                 * Define query
                 */
                Query query = em.createQuery(
                        "Select i from IntegrityAuditEntity i where i.persistenceUnit=:pu and i.nodeType=:nt");
                query.setParameter("pu", persistenceUnit);
                query.setParameter("nt", nodeType);

                /*
                 * Execute query using pessimistic write lock. This ensures that if anyone else is
                 * currently reading the records we'll throw a LockTimeoutException.
                 */
                setDesignatedEntity(resourceName, query);

                if (logger.isDebugEnabled()) {
                    logger.debug("changeDesignated: Committing designation to resourceName=" + resourceName);
                }
                em.getTransaction().commit();

                /*
                 * If we get a LockTimeoutException, no harm done really. We'll probably be
                 * successful on the next attempt. The odds of another DbDao instance on this entity
                 * or another entity attempting a simultaneous IntegrityAuditEntity table
                 * read/update are pretty slim (we're only in this method for two or three
                 * milliseconds)
                 */
            } catch (Exception e) {
                if (em != null) {
                    em.getTransaction().rollback();

                    String msg = "DbDao: changeDesignated() caught Exception, message=" + e.getMessage();
                    logger.error(msg + e);
                    throw new DbDaoTransactionException(msg, e);
                } else {
                    String msg = "DbDao: changeDesignated() caught Exception, message="
                            + e.getMessage() + ". Error rolling back transaction.";
                    logger.error(msg + e);
                    throw new DbDaoTransactionException(msg, e);
                }
            }

        } // end synchronized block

        if (logger.isDebugEnabled()) {
            logger.debug("changeDesignated: Exiting; time expended="
                            + (AuditorTime.getInstance().getMillis() - startTime) + "ms");
        }

    }

    private void setDesignatedEntity(String resourceName, Query query) {
        for (Object o : query.getResultList()) {
            if (!(o instanceof IntegrityAuditEntity)) {
                continue;
            }

            IntegrityAuditEntity integrityAuditEntity = (IntegrityAuditEntity) o;
            if (integrityAuditEntity.getResourceName().equals(resourceName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("changeDesignated: Designating resourceName="
                            + integrityAuditEntity.getResourceName());
                }
                integrityAuditEntity.setDesignated(true);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("changeDesignated: Removing designation from resourceName="
                            + integrityAuditEntity.getResourceName());
                }
                integrityAuditEntity.setDesignated(false);
            }
        }
    }

}
