/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PolicyEndPointProperties {

    /* Generic property suffixes */

    public static final String PROPERTY_TOPIC_SERVERS_SUFFIX = ".servers";
    public static final String PROPERTY_TOPIC_TOPICS_SUFFIX = ".topics";
    public static final String PROPERTY_TOPIC_API_KEY_SUFFIX = ".apiKey";
    public static final String PROPERTY_TOPIC_API_SECRET_SUFFIX = ".apiSecret";
    public static final String PROPERTY_TOPIC_AAF_MECHID_SUFFIX = ".aafMechId";
    public static final String PROPERTY_TOPIC_AAF_PASSWORD_SUFFIX = ".aafPassword"; //NOSONAR
    public static final String PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX = ".effectiveTopic";
    public static final String PROPERTY_TOPIC_EVENTS_SUFFIX = ".events";
    public static final String PROPERTY_TOPIC_EVENTS_FILTER_SUFFIX = ".filter";
    public static final String PROPERTY_TOPIC_EVENTS_CUSTOM_MODEL_CODER_GSON_SUFFIX = ".events.custom.gson";

    public static final String PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX = ".consumerGroup";
    public static final String PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX = ".consumerInstance";
    public static final String PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX = ".fetchTimeout";
    public static final String PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX = ".fetchLimit";
    public static final String PROPERTY_MANAGED_SUFFIX = ".managed";
    public static final String PROPERTY_AAF_SUFFIX = ".aaf";

    public static final String PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX = ".partitionKey";

    public static final String PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX = ".selfSignedCertificates";

    /* UEB Properties */

    public static final String PROPERTY_UEB_SOURCE_TOPICS = "ueb.source.topics";
    public static final String PROPERTY_UEB_SINK_TOPICS = "ueb.sink.topics";

    /* DMAAP Properties */

    public static final String PROPERTY_DMAAP_SOURCE_TOPICS = "dmaap.source.topics";
    public static final String PROPERTY_DMAAP_SINK_TOPICS = "dmaap.sink.topics";

    public static final String PROPERTY_DMAAP_DME2_PARTNER_SUFFIX = ".dme2.partner";
    public static final String PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX = ".dme2.routeOffer";
    public static final String PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX = ".dme2.environment";
    public static final String PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX = ".dme2.aft.environment";
    public static final String PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX = ".dme2.latitude";
    public static final String PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX = ".dme2.longitude";

    public static final String PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX = ".dme2.epReadTimeoutMs";
    public static final String PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX = ".dme2.epConnTimeout";
    public static final String PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX = ".dme2.roundtripTimeoutMs";
    public static final String PROPERTY_DMAAP_DME2_VERSION_SUFFIX = ".dme2.version";
    public static final String PROPERTY_DMAAP_DME2_SERVICE_NAME_SUFFIX = ".dme2.serviceName";
    public static final String PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX = ".dme2.subContextPath";
    public static final String PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX =
            ".dme2.sessionStickinessRequired";

    public static final String PROPERTY_NOOP_SOURCE_TOPICS = "noop.source.topics";
    public static final String PROPERTY_NOOP_SINK_TOPICS = "noop.sink.topics";

    /* HTTP Server Properties */

    public static final String PROPERTY_HTTP_SERVER_SERVICES = "http.server.services";

    public static final String PROPERTY_HTTP_HOST_SUFFIX = ".host";
    public static final String PROPERTY_HTTP_PORT_SUFFIX = ".port";
    public static final String PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX = ".contextUriPath";

    public static final String PROPERTY_HTTP_AUTH_USERNAME_SUFFIX = ".userName";
    public static final String PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX = ".password"; //NOSONAR
    public static final String PROPERTY_HTTP_AUTH_URIPATH_SUFFIX = ".authUriPath";

    public static final String PROPERTY_HTTP_FILTER_CLASSES_SUFFIX = ".filterClasses";
    public static final String PROPERTY_HTTP_REST_CLASSES_SUFFIX = ".restClasses";
    public static final String PROPERTY_HTTP_REST_PACKAGES_SUFFIX = ".restPackages";
    public static final String PROPERTY_HTTP_REST_URIPATH_SUFFIX = ".restUriPath";

    public static final String PROPERTY_HTTP_SERVLET_URIPATH_SUFFIX = ".servletUriPath";
    public static final String PROPERTY_HTTP_SERVLET_CLASS_SUFFIX = ".servletClass";

    public static final String PROPERTY_HTTP_HTTPS_SUFFIX = ".https";
    public static final String PROPERTY_HTTP_SWAGGER_SUFFIX = ".swagger";

    public static final String PROPERTY_HTTP_SERIALIZATION_PROVIDER = ".serialization.provider";

    /* HTTP Client Properties */

    public static final String PROPERTY_HTTP_CLIENT_SERVICES = "http.client.services";

    public static final String PROPERTY_HTTP_URL_SUFFIX = PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX;


    /* DMaaP DME2 Topic Properties */

    public static final String DME2_READ_TIMEOUT_PROPERTY = "AFT_DME2_EP_READ_TIMEOUT_MS";
    public static final String DME2_EP_CONN_TIMEOUT_PROPERTY = "AFT_DME2_EP_CONN_TIMEOUT";
    public static final String DME2_ROUNDTRIP_TIMEOUT_PROPERTY = "AFT_DME2_ROUNDTRIP_TIMEOUT_MS";
    public static final String DME2_VERSION_PROPERTY = "Version";
    public static final String DME2_ROUTE_OFFER_PROPERTY = "routeOffer";
    public static final String DME2_SERVICE_NAME_PROPERTY = "ServiceName";
    public static final String DME2_SUBCONTEXT_PATH_PROPERTY = "SubContextPath";
    public static final String DME2_SESSION_STICKINESS_REQUIRED_PROPERTY = "sessionstickinessrequired";


    /* Topic Sink Values */

    /**
     * Log Failures after X number of retries.
     */
    public static final int DEFAULT_LOG_SEND_FAILURES_AFTER = 1;


    /* Topic Source values */

    /**
     * Default Timeout fetching in milliseconds.
     */
    public static final int DEFAULT_TIMEOUT_MS_FETCH = 15000;

    /**
     * Default maximum number of messages fetch at the time.
     */
    public static final int DEFAULT_LIMIT_FETCH = 100;

    /**
     * Definition of No Timeout fetching.
     */
    public static final int NO_TIMEOUT_MS_FETCH = -1;

    /**
     * Definition of No limit fetching.
     */
    public static final int NO_LIMIT_FETCH = -1;
}
