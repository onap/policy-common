/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

public interface PolicyEndPointProperties {

    /* Generic property suffixes */

    String PROPERTY_TOPIC_SERVERS_SUFFIX = ".servers";
    String PROPERTY_TOPIC_API_KEY_SUFFIX = ".apiKey";
    String PROPERTY_TOPIC_API_SECRET_SUFFIX = ".apiSecret";
    String PROPERTY_TOPIC_AAF_MECHID_SUFFIX = ".aafMechId";
    String PROPERTY_TOPIC_AAF_PASSWORD_SUFFIX = ".aafPassword";
    String PROPERTY_TOPIC_EVENTS_SUFFIX = ".events";
    String PROPERTY_TOPIC_EVENTS_FILTER_SUFFIX = ".filter";
    String PROPERTY_TOPIC_EVENTS_CUSTOM_MODEL_CODER_GSON_SUFFIX = ".events.custom.gson";
    String PROPERTY_TOPIC_EVENTS_CUSTOM_MODEL_CODER_JACKSON_SUFFIX = ".events.custom.jackson";

    String PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX = ".consumerGroup";
    String PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX = ".consumerInstance";
    String PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX = ".fetchTimeout";
    String PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX = ".fetchLimit";
    String PROPERTY_MANAGED_SUFFIX = ".managed";
    String PROPERTY_AAF_SUFFIX = ".aaf";

    String PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX = ".partitionKey";

    String PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX = ".selfSignedCertificates";

    /* UEB Properties */

    String PROPERTY_UEB_SOURCE_TOPICS = "ueb.source.topics";
    String PROPERTY_UEB_SINK_TOPICS = "ueb.sink.topics";

    /* DMAAP Properties */

    String PROPERTY_DMAAP_SOURCE_TOPICS = "dmaap.source.topics";
    String PROPERTY_DMAAP_SINK_TOPICS = "dmaap.sink.topics";

    String PROPERTY_DMAAP_DME2_PARTNER_SUFFIX = ".dme2.partner";
    String PROPERTY_DMAAP_DME2_ROUTE_OFFER_SUFFIX = ".dme2.routeOffer";
    String PROPERTY_DMAAP_DME2_ENVIRONMENT_SUFFIX = ".dme2.environment";
    String PROPERTY_DMAAP_DME2_AFT_ENVIRONMENT_SUFFIX = ".dme2.aft.environment";
    String PROPERTY_DMAAP_DME2_LATITUDE_SUFFIX = ".dme2.latitude";
    String PROPERTY_DMAAP_DME2_LONGITUDE_SUFFIX = ".dme2.longitude";

    String PROPERTY_DMAAP_DME2_EP_READ_TIMEOUT_MS_SUFFIX = ".dme2.epReadTimeoutMs";
    String PROPERTY_DMAAP_DME2_EP_CONN_TIMEOUT_SUFFIX = ".dme2.epConnTimeout";
    String PROPERTY_DMAAP_DME2_ROUNDTRIP_TIMEOUT_MS_SUFFIX = ".dme2.roundtripTimeoutMs";
    String PROPERTY_DMAAP_DME2_VERSION_SUFFIX = ".dme2.version";
    String PROPERTY_DMAAP_DME2_SERVICE_NAME_SUFFIX = ".dme2.serviceName";
    String PROPERTY_DMAAP_DME2_SUB_CONTEXT_PATH_SUFFIX = ".dme2.subContextPath";
    String PROPERTY_DMAAP_DME2_SESSION_STICKINESS_REQUIRED_SUFFIX =
            ".dme2.sessionStickinessRequired";

    String PROPERTY_NOOP_SOURCE_TOPICS = "noop.source.topics";
    String PROPERTY_NOOP_SINK_TOPICS = "noop.sink.topics";

    /* HTTP Server Properties */

    String PROPERTY_HTTP_SERVER_SERVICES = "http.server.services";

    String PROPERTY_HTTP_HOST_SUFFIX = ".host";
    String PROPERTY_HTTP_PORT_SUFFIX = ".port";
    String PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX = ".contextUriPath";

    String PROPERTY_HTTP_AUTH_USERNAME_SUFFIX = ".userName";
    String PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX = ".password";
    String PROPERTY_HTTP_AUTH_URIPATH_SUFFIX = ".authUriPath";

    String PROPERTY_HTTP_FILTER_CLASSES_SUFFIX = ".filterClasses";
    String PROPERTY_HTTP_REST_CLASSES_SUFFIX = ".restClasses";
    String PROPERTY_HTTP_REST_PACKAGES_SUFFIX = ".restPackages";
    String PROPERTY_HTTP_REST_URIPATH_SUFFIX = ".restUriPath";

    String PROPERTY_HTTP_HTTPS_SUFFIX = ".https";
    String PROPERTY_HTTP_SWAGGER_SUFFIX = ".swagger";

    /* HTTP Client Properties */

    String PROPERTY_HTTP_CLIENT_SERVICES = "http.client.services";

    String PROPERTY_HTTP_URL_SUFFIX = PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX;
}
