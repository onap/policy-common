/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.endpoints.http.server;

import java.util.List;
import java.util.Properties;
import org.onap.policy.common.endpoints.http.server.aaf.AafAuthFilter;
import org.onap.policy.common.endpoints.parameters.RestServerParameters;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.services.ServiceManagerContainer;

/**
 * Class to manage life cycle of a rest server.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
public class RestServer extends ServiceManagerContainer {

    /**
     * Factory used to access objects. May be overridden by junit tests.
     */
    private static Factory factory = new Factory();

    private final List<HttpServletServer> servers;

    /**
     * Constructs the object.
     *
     * @param restServerParameters the rest server parameters
     * @param aafFilter class of object to use to filter AAF requests, or {@code null}
     * @param jaxrsProviders classes providing the services
     */
    public RestServer(final RestServerParameters restServerParameters, Class<? extends AafAuthFilter> aafFilter,
                    Class<?>... jaxrsProviders) {

        if (jaxrsProviders.length == 0) {
            throw new IllegalArgumentException("no providers specified");
        }

        this.servers = factory.getServerFactory()
                        .build(getServerProperties(restServerParameters, getProviderClassNames(jaxrsProviders)));

        for (HttpServletServer server : this.servers) {
            if (aafFilter != null && server.isAaf()) {
                server.addFilterClass(null, aafFilter.getName());
            }

            addAction("REST " + server.getName(), server::start, server::stop);
        }
    }

    /**
     * Creates the server properties object using restServerParameters.
     *
     * @param restServerParameters the rest server parameters
     * @param names comma-separated list of classes providing the services
     *
     * @return the properties object
     */
    protected Properties getServerProperties(RestServerParameters restServerParameters, String names) {
        final Properties props = new Properties();
        props.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, restServerParameters.getName());

        final String svcpfx =
                        PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + restServerParameters.getName();

        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, restServerParameters.getHost());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX,
                        Integer.toString(restServerParameters.getPort()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX, names);
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "false");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, "true");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX,
            getValue(restServerParameters.getUserName()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX,
            getValue(restServerParameters.getPassword()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX,
                        String.valueOf(restServerParameters.isHttps()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_AAF_SUFFIX,
                        String.valueOf(restServerParameters.isAaf()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER,
                        String.join(",", GsonMessageBodyHandler.class.getName(), YamlMessageBodyHandler.class.getName(),
                                        JsonExceptionMapper.class.getName(), YamlExceptionMapper.class.getName()));
        return props;
    }

    /**
     * Gets the provider class names, as a comma-separated string.
     *
     * @param jaxrsProviders classes providing the services
     * @return the provider class names
     */
    private String getProviderClassNames(Class<?>[] jaxrsProviders) {
        StringBuilder names = new StringBuilder();

        for (Class<?> prov : jaxrsProviders) {
            if (names.length() > 0) {
                names.append(',');
            }

            names.append(prov.getName());
        }

        return names.toString();
    }

    private String getValue(final String value) {
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            return System.getenv(value.substring(2, value.length() - 1));
        }
        return value;
    }

    @Override
    public String toString() {
        return "RestServer [servers=" + servers + "]";
    }

    /**
     * Factory used to access objects.
     */
    public static class Factory {

        public HttpServletServerFactory getServerFactory() {
            return HttpServletServerFactoryInstance.getServerFactory();
        }
    }
}
