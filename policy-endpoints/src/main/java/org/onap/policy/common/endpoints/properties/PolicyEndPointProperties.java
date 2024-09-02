/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2022,2023-2024 Nordix Foundation.
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
    public static final String PROPERTY_TOPIC_EFFECTIVE_TOPIC_SUFFIX = ".effectiveTopic";
    public static final String PROPERTY_TOPIC_EVENTS_SUFFIX = ".events";
    public static final String PROPERTY_TOPIC_EVENTS_FILTER_SUFFIX = ".filter";
    public static final String PROPERTY_TOPIC_EVENTS_CUSTOM_MODEL_CODER_GSON_SUFFIX = ".events.custom.gson";

    public static final String PROPERTY_TOPIC_SOURCE_CONSUMER_GROUP_SUFFIX = ".consumerGroup";
    public static final String PROPERTY_TOPIC_SOURCE_CONSUMER_INSTANCE_SUFFIX = ".consumerInstance";
    public static final String PROPERTY_TOPIC_SOURCE_FETCH_TIMEOUT_SUFFIX = ".fetchTimeout";
    public static final String PROPERTY_TOPIC_SOURCE_FETCH_LIMIT_SUFFIX = ".fetchLimit";
    public static final String PROPERTY_MANAGED_SUFFIX = ".managed";
    public static final String PROPERTY_ADDITIONAL_PROPS_SUFFIX = ".additionalProps";

    public static final String PROPERTY_TOPIC_SINK_PARTITION_KEY_SUFFIX = ".partitionKey";

    public static final String PROPERTY_ALLOW_SELF_SIGNED_CERTIFICATES_SUFFIX = ".selfSignedCertificates";

    public static final String PROPERTY_NOOP_SOURCE_TOPICS = "noop.source.topics";
    public static final String PROPERTY_NOOP_SINK_TOPICS = "noop.sink.topics";

    /* KAFKA Properties */

    public static final String PROPERTY_KAFKA_SOURCE_TOPICS = "kafka.source.topics";
    public static final String PROPERTY_KAFKA_SINK_TOPICS = "kafka.sink.topics";

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
    public static final String PROPERTY_HTTP_PROMETHEUS_SUFFIX = ".prometheus";

    public static final String PROPERTY_HTTP_HTTPS_SUFFIX = ".https";
    public static final String PROPERTY_HTTP_SWAGGER_SUFFIX = ".swagger";
    public static final String PROPERTY_HTTP_SNI_HOST_CHECK_SUFFIX = ".sniHostCheck";

    public static final String PROPERTY_HTTP_SERIALIZATION_PROVIDER = ".serialization.provider";

    /* HTTP Client Properties */

    public static final String PROPERTY_HTTP_CLIENT_SERVICES = "http.client.services";

    public static final String PROPERTY_HTTP_URL_SUFFIX = PROPERTY_HTTP_CONTEXT_URIPATH_SUFFIX;

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
