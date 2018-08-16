/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm.bus.internal;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Member variables of this Params class are as follows.
 *
 * <p>servers DMaaP servers
 * topic DMaaP Topic to be monitored
 * apiKey DMaaP API Key (optional)
 * apiSecret DMaaP API Secret (optional)
 * consumerGroup DMaaP Reader Consumer Group
 * consumerInstance DMaaP Reader Instance
 * fetchTimeout DMaaP fetch timeout
 * fetchLimit DMaaP fetch limit
 * environment DME2 Environment
 * aftEnvironment DME2 AFT Environment
 * partner DME2 Partner
 * latitude DME2 Latitude
 * longitude DME2 Longitude
 * additionalProps Additional properties to pass to DME2
 * useHttps does connection use HTTPS?
 * allowSelfSignedCerts are self-signed certificates allow
 */
public class BusTopicParams {

    private int port;
    private List<String> servers;
    private String topic;
    private String apiKey;
    private String apiSecret;
    private String consumerGroup;
    private String consumerInstance;
    private int fetchTimeout;
    private int fetchLimit;
    private boolean useHttps;
    private boolean allowSelfSignedCerts;
    private boolean managed;

    private String userName;
    private String password;
    private String environment;
    private String aftEnvironment;
    private String partner;
    private String latitude;
    private String longitude;
    private Map<String, String> additionalProps;
    private String partitionId;
    private String clientName;
    private String hostname;
    private String basePath;

    public static TopicParamsBuilder builder() {
        return new TopicParamsBuilder();
    }

    public String getPartitionId() {
        return partitionId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getAftEnvironment() {
        return aftEnvironment;
    }

    public String getPartner() {
        return partner;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public Map<String, String> getAdditionalProps() {
        return additionalProps;
    }

    public List<String> getServers() {
        return servers;
    }

    public String getTopic() {
        return topic;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public String getConsumerInstance() {
        return consumerInstance;
    }

    public int getFetchTimeout() {
        return fetchTimeout;
    }

    public int getFetchLimit() {
        return fetchLimit;
    }

    public boolean isUseHttps() {
        return useHttps;
    }

    public boolean isAllowSelfSignedCerts() {
        return allowSelfSignedCerts;
    }

    boolean isEnvironmentNullOrEmpty() {
        return (environment == null || environment.trim().isEmpty());
    }

    boolean isAftEnvironmentNullOrEmpty() {
        return (aftEnvironment == null || aftEnvironment.trim().isEmpty());
    }

    boolean isLatitudeNullOrEmpty() {
        return (latitude == null || latitude.trim().isEmpty());
    }

    boolean isLongitudeNullOrEmpty() {
        return (longitude == null || longitude.trim().isEmpty());
    }

    boolean isConsumerInstanceNullOrEmpty() {
        return (consumerInstance == null || consumerInstance.trim().isEmpty());
    }

    boolean isConsumerGroupNullOrEmpty() {
        return (consumerGroup == null || consumerGroup.trim().isEmpty());
    }

    public boolean isClientNameNullOrEmpty() {
        return (clientName == null || clientName.trim().isEmpty());
    }

    boolean isApiKeyValid() {
        return !(apiKey == null || apiKey.trim().isEmpty());
    }

    boolean isApiSecretValid() {
        return !(apiSecret == null || apiSecret.trim().isEmpty());
    }

    boolean isUserNameValid() {
        return !(userName == null || userName.trim().isEmpty());
    }

    boolean isPasswordValid() {
        return !(password == null || password.trim().isEmpty());
    }

    boolean isPartnerNullOrEmpty() {
        return (partner == null || partner.trim().isEmpty());
    }

    boolean isServersNullOrEmpty() {
        return (servers == null || servers.isEmpty()
                || (servers.size() == 1 && ("".equals(servers.get(0)))));
    }

    boolean isAdditionalPropsValid() {
        return additionalProps != null;
    }

    boolean isTopicNullOrEmpty() {
        return (topic == null || topic.trim().isEmpty());
    }

    boolean isPartitionIdNullOrEmpty() {
        return (partitionId == null || partitionId.trim().isEmpty());
    }

    public boolean isManaged() {
        return managed;
    }

    public String getClientName() {
        return clientName;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getBasePath() {
        return basePath;
    }

    public boolean isHostnameNullOrEmpty() {
        return (hostname == null || hostname.trim().isEmpty());
    }

    public boolean isPortOutOfRange() {
        return  (getPort() <= 0 || getPort() >= 65535);
    }

    public static class TopicParamsBuilder {

        final BusTopicParams params = new BusTopicParams();

        private TopicParamsBuilder() {
        }

        public TopicParamsBuilder servers(List<String> servers) {
            this.params.servers = servers;
            return this;
        }

        public TopicParamsBuilder topic(String topic) {
            this.params.topic = topic;
            return this;
        }

        public TopicParamsBuilder apiKey(String apiKey) {
            this.params.apiKey = apiKey;
            return this;
        }

        public TopicParamsBuilder apiSecret(String apiSecret) {
            this.params.apiSecret = apiSecret;
            return this;
        }

        public TopicParamsBuilder consumerGroup(String consumerGroup) {
            this.params.consumerGroup = consumerGroup;
            return this;
        }

        public TopicParamsBuilder consumerInstance(String consumerInstance) {
            this.params.consumerInstance = consumerInstance;
            return this;
        }

        public TopicParamsBuilder fetchTimeout(int fetchTimeout) {
            this.params.fetchTimeout = fetchTimeout;
            return this;
        }

        public TopicParamsBuilder fetchLimit(int fetchLimit) {
            this.params.fetchLimit = fetchLimit;
            return this;
        }

        public TopicParamsBuilder useHttps(boolean useHttps) {
            this.params.useHttps = useHttps;
            return this;
        }

        public TopicParamsBuilder allowSelfSignedCerts(boolean allowSelfSignedCerts) {
            this.params.allowSelfSignedCerts = allowSelfSignedCerts;
            return this;
        }

        public TopicParamsBuilder userName(String userName) {
            this.params.userName = userName;
            return this;
        }

        public TopicParamsBuilder password(String password) {
            this.params.password = password;
            return this;
        }

        public TopicParamsBuilder environment(String environment) {
            this.params.environment = environment;
            return this;
        }

        public TopicParamsBuilder aftEnvironment(String aftEnvironment) {
            this.params.aftEnvironment = aftEnvironment;
            return this;
        }

        public TopicParamsBuilder partner(String partner) {
            this.params.partner = partner;
            return this;
        }

        public TopicParamsBuilder latitude(String latitude) {
            this.params.latitude = latitude;
            return this;
        }

        public TopicParamsBuilder longitude(String longitude) {
            this.params.longitude = longitude;
            return this;
        }

        public TopicParamsBuilder additionalProps(Map<String, String> additionalProps) {
            this.params.additionalProps = additionalProps;
            return this;
        }

        public TopicParamsBuilder partitionId(String partitionId) {
            this.params.partitionId = partitionId;
            return this;
        }

        public BusTopicParams build() {
            return params;
        }

        public TopicParamsBuilder managed(boolean managed) {
            this.params.managed = managed;
            return this;
        }

        public TopicParamsBuilder hostname(String hostname) {
            this.params.hostname = hostname;
            return this;
        }

        public TopicParamsBuilder clientName(String clientName) {
            this.params.clientName = clientName;
            return this;
        }

        public TopicParamsBuilder port(int port) {
            this.params.port = port;
            return this;
        }

        public TopicParamsBuilder basePath(String basePath) {
            this.params.basePath = basePath;
            return this;
        }

    }
}

