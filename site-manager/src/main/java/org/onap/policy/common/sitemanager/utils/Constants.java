/*-
 * ============LICENSE_START=======================================================
 * site-manager
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.common.sitemanager.utils;

public final class Constants {

    public static final String SITE_NAME = "site";

    public static final String WHERE_R_SITE_SITE = " WHERE r.site = :" + SITE_NAME;

    public static final String WHERE_R_RESOURCE_NAME = " WHERE r.resourceName = :";

    public static final String RESOURCE_NAME = "resourceName";

    public static final String WHERE_S_RESOURCE_NAME = " WHERE s.resourceName = :";

    public static final String RESOURCE_REGISTRATION_QUERY = "SELECT r FROM ResourceRegistrationEntity r";

    public static final String STATE_MANAGEMENT_QUERY = "SELECT s FROM StateManagementEntity s";

    public static final String JDBC_PASSWORD_PROPERTY_NAME = "javax.persistence.jdbc.password";

    public static final String JDBC_USER_PROPERTY_NAME = "javax.persistence.jdbc.user";

    public static final String JDBC_URL_PROPERTY_NAME = "javax.persistence.jdbc.url";

    public static final String JDBC_DRIVER_PROPERTY_NAME = "javax.persistence.jdbc.driver";

    public static final String OPERATIONAL_PERSISTENCE_UNIT = "operationalPU";

    public static final String SITE_MANAGER_PROPERTIES_PROPERTY_NAME = "siteManager.properties";

    private Constants() {}
}
