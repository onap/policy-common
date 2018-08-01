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

import java.util.Properties;

/**
 * HTTP Client Endpoints Properties Helper
 */
public class HttpClientPropertiesHelper extends HttpEndpointPropertiesHelper implements HttpClientProperties {

    public HttpClientPropertiesHelper(Properties properties) {
        super(properties);
    }

    @Override
    public String getEndpointPropertyName() {
        return PROPERTY_HTTP_CLIENT_SERVICES;
    }

    public String getAllowSelfSignedCertificates(String serviceName) {
        return properties.getProperty(getPropertyName(serviceName, PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX));
    }

    public void setAllowSelfSignedCertificates(String serviceName, boolean allowSelfSignedCertificates) {
        properties.setProperty(getPropertyName(serviceName, PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX), String.valueOf(allowSelfSignedCertificates));
    }

    public boolean isAllowSelfSignedCertificates(String serviceName, boolean allowSelfSignedCertificates) {
        return Boolean.parseBoolean(properties.getProperty(getPropertyName(serviceName, PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX),
                                                                           String.valueOf(allowSelfSignedCertificates)));
    }

}
