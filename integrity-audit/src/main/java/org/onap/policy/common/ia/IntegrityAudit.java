/*-
 * ============LICENSE_START=======================================================
 * Integrity Audit
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.ia.IntegrityAuditProperties.NodeTypeEnum;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;

/**
 * class IntegrityAudit Audits all persisted entities for all resource clusters for all sites and
 * logs any anomalies.
 */
public class IntegrityAudit {

    private static final Logger logger = FlexLogger.getLogger(IntegrityAudit.class);

    @Getter
    @Setter
    private static boolean unitTesting;

    @Getter
    private boolean threadInitialized = false;

    AuditThread auditThread = null;

    private String persistenceUnit;
    private Properties properties;
    private String resourceName;


    /*
     * This is the audit period in milliseconds. For example, if it had a value of 3600000, the
     * audit can only run once per hour. If it has a value of 6000, it can run once per minute.
     *
     * Values: integrityAuditPeriodSeconds < 0 (negative number) indicates the audit is off
     * integrityAuditPeriodSeconds == 0 indicates the audit is to run continuously
     * integrityAuditPeriodSeconds > 0 indicates the audit is to run at most once during the
     * indicated period
     *
     */
    @Getter
    private int integrityAuditPeriodSeconds;

    /**
     * IntegrityAudit constructor.
     *
     * @param resourceName the resource name
     * @param persistenceUnit the persistence unit
     * @param properties the properties
     * @throws IntegrityAuditException if an error occurs
     */
    public IntegrityAudit(String resourceName, String persistenceUnit, Properties properties)
            throws IntegrityAuditException {

        logger.info("Constructor: Entering and checking for nulls");
        var parmList = new StringBuilder();
        if (parmsAreBad(resourceName, persistenceUnit, properties, parmList)) {
            logger.error("Constructor: Parms contain nulls; cannot run audit for resourceName=" + resourceName
                    + ", persistenceUnit=" + persistenceUnit + ", bad parameters: " + parmList);
            throw new IntegrityAuditException("Constructor: Parms contain nulls; cannot run audit for resourceName="
                    + resourceName + ", persistenceUnit=" + persistenceUnit + ", bad parameters: " + parmList);
        }

        this.persistenceUnit = persistenceUnit;
        this.properties = properties;
        this.resourceName = resourceName;

        // IntegrityAuditProperties.AUDIT_PERIOD_SECONDS and
        // IntegrityAuditProperties.AUDIT_PERIOD_MILLISECONDS are allowed to be null
        if (properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS) != null) {
            this.integrityAuditPeriodSeconds =
                    Integer.parseInt(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS).trim());
        } else {
            // If it is null, set it to the default value
            this.integrityAuditPeriodSeconds = IntegrityAuditProperties.DEFAULT_AUDIT_PERIOD_SECONDS;
        }
        logger.info("Constructor: Exiting");

    }

    /**
     * Determine if the nodeType conforms to the required node types.
     */
    public static boolean isNodeTypeEnum(String nt) {
        for (NodeTypeEnum n : NodeTypeEnum.values()) {
            if (n.toString().equalsIgnoreCase(nt)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Makes sure we don't try to run the audit with bad parameters.
     */
    public static boolean parmsAreBad(String resourceName, String persistenceUnit, Properties properties,
            StringBuilder badparams) {

        boolean parmsAreBad = checkEmpty(badparams, "resourceName", resourceName);
        parmsAreBad = checkEmpty(badparams, "persistenceUnit", persistenceUnit) || parmsAreBad;

        if (properties == null || properties.isEmpty()) {
            badparams.append("properties ");
            parmsAreBad = true;
        } else {
            parmsAreBad = checkProperties(properties, badparams) || parmsAreBad;
        } // End else
        logger.debug("parmsAreBad: exit:" + "\nresourceName: " + resourceName + "\npersistenceUnit: " + persistenceUnit
                + "\nproperties: " + properties);

        return parmsAreBad;
    }

    private static boolean checkEmpty(StringBuilder builder, String name, String value) {
        if (StringUtils.isEmpty(value)) {
            builder.append(name);
            builder.append(' ');
            return true;

        } else {
            return false;
        }
    }

    private static boolean checkProperties(Properties properties, StringBuilder badparams) {
        boolean parmsAreBad =
                        checkEmpty(badparams, "dbDriver", properties.getProperty(IntegrityAuditProperties.DB_DRIVER));
        parmsAreBad = checkEmpty(badparams, "dbUrl", properties.getProperty(IntegrityAuditProperties.DB_URL))
                        || parmsAreBad;
        parmsAreBad = checkEmpty(badparams, "dbUser", properties.getProperty(IntegrityAuditProperties.DB_USER))
                        || parmsAreBad;

        // dbPwd may be empty
        checkEmpty(badparams, "dbPwd", properties.getProperty(IntegrityAuditProperties.DB_PWD));

        parmsAreBad = checkEmpty(badparams, "siteName", properties.getProperty(IntegrityAuditProperties.SITE_NAME))
                        || parmsAreBad;
        parmsAreBad = checkNodeType(properties, badparams) || parmsAreBad;

        return checkAuditPeriod(properties, badparams) || parmsAreBad;
    }

    private static boolean checkNodeType(Properties properties, StringBuilder badparams) {
        String nodeType = properties.getProperty(IntegrityAuditProperties.NODE_TYPE);
        if (nodeType == null || nodeType.isEmpty()) {
            badparams.append("nodeType ");
            return true;
        } else {
            nodeType = nodeType.trim();
            if (!isNodeTypeEnum(nodeType)) {
                badparams.append("nodeType must be one of[");
                for (NodeTypeEnum n : NodeTypeEnum.values()) {
                    badparams.append(n.toString());
                    badparams.append(' ');
                }
                badparams.append("] ");
                return true;
            }
        }
        return false;
    }

    private static boolean checkAuditPeriod(Properties properties, StringBuilder badparams) {
        // IntegrityAuditProperties.AUDIT_PERIOD_SECONDS and
        // IntegrityAuditProperties.AUDIT_PERIOD_MILLISECONDS are allowed to be null
        if (properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS) != null) {
            try {
                Integer.parseInt(properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS).trim());
            } catch (NumberFormatException nfe) {
                badparams.append(", auditPeriodSeconds="
                        + properties.getProperty(IntegrityAuditProperties.AUDIT_PERIOD_SECONDS).trim());
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the audit thread.
     *
     * @throws IntegrityAuditException if an error occurs
     */
    public void startAuditThread() throws IntegrityAuditException {
        logger.info("startAuditThread: Entering");

        if (integrityAuditPeriodSeconds >= 0) {
            this.auditThread = makeAuditThread(this.resourceName, this.persistenceUnit,
                            this.properties, integrityAuditPeriodSeconds);
            logger.info("startAuditThread: Audit started and will run every " + integrityAuditPeriodSeconds
                            + " seconds");
            this.auditThread.start();

        } else {
            logger.info("startAuditThread: Suppressing integrity audit, integrityAuditPeriodSeconds="
                    + integrityAuditPeriodSeconds);
        }

        logger.info("startAuditThread: Exiting");
    }

    /**
     * Stops the audit thread.
     */
    public void stopAuditThread() {

        logger.info("stopAuditThread: Entering");

        if (this.auditThread != null) {
            this.auditThread.interrupt();
        } else {
            logger.info("stopAuditThread: auditThread never instantiated; no need to interrupt");
        }

        logger.info("stopAuditThread: Exiting");
    }

    public void setThreadInitialized(boolean isThreadInitialized) {
        logger.info("setThreadInitialized: Setting isThreadInitialized=" + isThreadInitialized);
        this.threadInitialized = isThreadInitialized;
    }

    /**
     * Waits a bit for the AuditThread to complete. Used by JUnit tests.
     *
     * @param twaitms wait time, in milliseconds
     * @return {@code true} if the thread stopped within the given time, {@code false} otherwise
     * @throws InterruptedException if the thread is interrupted
     */
    protected boolean joinAuditThread(long twaitms) throws InterruptedException {
        if (this.auditThread == null) {
            return true;

        } else {
            this.auditThread.join(twaitms);
            return !this.auditThread.isAlive();
        }
    }

    /**
     * Return if audit thread.
     *
     * @return {@code true} if an audit thread exists, {@code false} otherwise
     */
    protected boolean haveAuditThread() {
        return (this.auditThread != null);
    }

    /**
     * Creates an audit thread. May be overridden by junit tests.
     *
     * @param resourceName2 the resource name
     * @param persistenceUnit2 the persistence unit
     * @param properties2 properties
     * @param integrityAuditPeriodSeconds2
     *
     * @return a new audit thread
     * @throws IntegrityAuditException audit exception
     */
    protected AuditThread makeAuditThread(String resourceName2, String persistenceUnit2, Properties properties2,
                    int integrityAuditPeriodSeconds2) throws IntegrityAuditException {

        return new AuditThread(resourceName2, persistenceUnit2, properties2, integrityAuditPeriodSeconds2, this);
    }
}
