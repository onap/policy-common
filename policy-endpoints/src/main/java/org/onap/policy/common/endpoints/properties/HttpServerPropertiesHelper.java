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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * HTTP Server Endpoints Properties Helper
 */
public class HttpServerPropertiesHelper extends HttpEndpointPropertiesHelper implements HttpServerProperties {

    public HttpServerPropertiesHelper(Properties properties) {
        super(properties);
    }

    @Override
    public String getEndpointPropertyName() {
        return PROPERTY_HTTP_SERVER_SERVICES;
    }

    public String getAuthUriPath(String serviceName) {
        return properties.getProperty(getPropertyName(serviceName, PROPERTY_HTTP_AUTH_URIPATH_SUFFIX));
    }

    public void setAuthUriPath(String serviceName, String authUriPath) {
        properties.setProperty(getPropertyName(serviceName, PROPERTY_HTTP_AUTH_URIPATH_SUFFIX), authUriPath);
    }

    public String getRestUriPath(String serviceName) {
        return properties.getProperty(getPropertyName(serviceName, PROPERTY_HTTP_REST_URIPATH_SUFFIX));
    }

    public void setRestUriPath(String serviceName, String restUriPath) {
        properties.setProperty(getPropertyName(serviceName, PROPERTY_HTTP_REST_URIPATH_SUFFIX), restUriPath);
    }

    public List<String> getRestPackages(String serviceName) {
        String packages = properties.getProperty(getPropertyName(serviceName, PROPERTY_HTTP_REST_PACKAGES_SUFFIX));
        if (packages == null)
            return new ArrayList<String>();

        return Arrays.asList(packages.split(SPACES_COMMA_SPACES));
    }

    public void setRestPackages(String serviceName, String commaSeparatedPackages) {
        if (serviceName == null || commaSeparatedPackages == null)
            return;

        properties.setProperty(getPropertyName(serviceName, PROPERTY_HTTP_REST_PACKAGES_SUFFIX),
                               commaSeparatedPackages);
    }

    public void setRestPackages(String serviceName, List<String> restPackages) {
        if (serviceName == null || restPackages == null || restPackages.isEmpty())
            return;

        String commaSeparatedPackages = new String();
        for (String restPackage : restPackages) {
            commaSeparatedPackages += restPackage + ",";
        }

        setRestPackages(serviceName, commaSeparatedPackages.substring(0, commaSeparatedPackages.length()-1));
    }

    public List<String> getRestClasses(String serviceName) {
        String classes = properties.getProperty(getPropertyName(serviceName, PROPERTY_HTTP_REST_CLASSES_SUFFIX));
        if (classes == null)
            return new ArrayList<String>();

        return Arrays.asList(classes.split(SPACES_COMMA_SPACES));
    }

    public void setRestClasses(String serviceName, String commaSeparatedClasses) {
        if (serviceName == null || commaSeparatedClasses == null)
            return;

        properties.setProperty(getPropertyName(serviceName, PROPERTY_HTTP_REST_CLASSES_SUFFIX),
            commaSeparatedClasses);
    }

    public void setRestClasses(String serviceName, List<String> restClasses) {
        if (serviceName == null || restClasses == null || restClasses.isEmpty())
            return;

        String commaSeparatedClasses = new String();
        for (String restClass : restClasses) {
            commaSeparatedClasses += restClass + ",";
        }

        setRestClasses(serviceName, commaSeparatedClasses.substring(0, commaSeparatedClasses.length()-1));
    }

    public String getSwagger(String serviceName) {
        return properties.getProperty(getPropertyName(serviceName, PROPERTY_HTTP_SWAGGER_SUFFIX));
    }

    public void setSwagger(String serviceName, boolean swagger) {
        properties.setProperty(getPropertyName(serviceName, PROPERTY_HTTP_SWAGGER_SUFFIX), String.valueOf(swagger));
    }

    public boolean isSwagger(String serviceName, boolean defaultSwagger) {
        String swagger = properties.getProperty(getPropertyName(serviceName, PROPERTY_HTTP_SWAGGER_SUFFIX), String.valueOf(defaultSwagger));
        return Boolean.parseBoolean(swagger);
    }
}
