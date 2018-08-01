/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Base HTTP Property Configuration Helper
 */
public abstract class HttpEndpointPropertiesHelper implements HttpEndpointProperties {

    protected static final String SPACES_COMMA_SPACES = "\\s*,\\s*";

    protected final Properties properties;

    protected HttpEndpointPropertiesHelper(Properties properties) {
        if (properties == null)
            throw new IllegalArgumentException("no properties provided");

        this.properties = properties;
    }

    public abstract String getEndpointPropertyName();

    public List<String> getEndpointNames() {
        String endpointNames =
            this.properties.getProperty(this.getEndpointPropertyName());

        return Arrays.asList(endpointNames.split(SPACES_COMMA_SPACES));
    }

    public void setEndpointNames(String endpointNames) {
        if (endpointNames == null)
            return;

        this.properties.setProperty(getEndpointPropertyName(), endpointNames);
    }

    public void setEndpointNames(List<String> endpointNames) {
        if (endpointNames == null || endpointNames.isEmpty())
            return;

        String commaEndpoints = new String();
        for (String endpointName : endpointNames) {
            commaEndpoints += endpointName + ",";
        }
        setEndpointNames(commaEndpoints.substring(0, commaEndpoints.length()-1));
    }

    protected String getPropertyName(String entityName, String propertySuffix) {
        return getEndpointPropertyName() + "." + entityName + propertySuffix;
    }

    public String getHost(String entityName) {
        return properties.getProperty(getPropertyName(entityName, PROPERTY_HTTP_HOST_SUFFIX));
    }

    public void setHost(String entityName, String host) {
        properties.setProperty(getPropertyName(entityName, PROPERTY_HTTP_HOST_SUFFIX), host);
    }

    public int getPort(String entityName) {
        String port =
            properties.getProperty(getPropertyName(entityName, PROPERTY_HTTP_PORT_SUFFIX));
        return Integer.parseInt(port);
    }

    public void setPort(String entityName, int port) {
        properties.setProperty(getPropertyName(entityName, PROPERTY_HTTP_PORT_SUFFIX), String.valueOf(port));
    }

    public String getUserName(String entityName) {
        return properties.getProperty(getPropertyName(entityName, PROPERTY_HTTP_AUTH_USERNAME_SUFFIX));
    }

    public void setUserName(String entityName, String userName) {
        properties.setProperty(getPropertyName(entityName, PROPERTY_HTTP_AUTH_USERNAME_SUFFIX), userName);
    }

    public String getPassword(String entityName) {
        return properties.getProperty(getPropertyName(entityName, PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX));
    }

    public void setPassword(String entityName, String password) {
        properties.setProperty(getPropertyName(entityName, PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX), password);
    }

    public String getHttps(String entityName) {
        return properties.getProperty(getPropertyName(entityName, PROPERTY_HTTPS_SUFFIX));
    }

    public boolean isHttps(String entityName, boolean defaultHttps) {
        String https = properties.getProperty(getPropertyName(entityName, PROPERTY_HTTPS_SUFFIX), String.valueOf(defaultHttps));
        return Boolean.parseBoolean(https);
    }

    public void setHttps(String entityName, boolean https) {
        properties.setProperty(getPropertyName(entityName, PROPERTY_HTTPS_SUFFIX), String.valueOf(https));
    }

    public String getContextUriPath(String entityName) {
        return properties.getProperty(getPropertyName(entityName, PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX));
    }

    public void setContextUriPath(String entityName, String contextUriPath) {
        properties.setProperty(getPropertyName(entityName, PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX), contextUriPath);
    }

    public String getManaged(String entityName) {
        return properties.getProperty(getPropertyName(entityName, PROPERTY_MANAGED_SUFFIX));
    }

    public boolean isManaged(String entityName, boolean defaultManaged) {
        String managed = properties.getProperty(getPropertyName(entityName, PROPERTY_MANAGED_SUFFIX), String.valueOf(defaultManaged));
        return Boolean.parseBoolean(managed);
    }

    public void setManaged(String entityName, boolean managed) {
        properties.setProperty(getPropertyName(entityName, PROPERTY_MANAGED_SUFFIX), String.valueOf(managed));
    }

}
