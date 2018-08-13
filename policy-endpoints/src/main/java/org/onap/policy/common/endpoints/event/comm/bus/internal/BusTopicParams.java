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

import java.util.List;
import java.util.Map;

/**
 * Member variables of this Params class are as follows
 * servers DMaaP servers
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

    public static TopicParamsBuilder builder() {
        return new TopicParamsBuilder();
    }

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

    private String userName;
    private String password;
    private String environment;
    private String aftEnvironment;
    private String partner;
    private String latitude;
    private String longitude;
    private Map<String, String> additionalProps;
    private String partitionId;
    private boolean managed;

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

    public static class TopicParamsBuilder {
        BusTopicParams m = new BusTopicParams();

        private TopicParamsBuilder() {
        }

        public TopicParamsBuilder servers(List<String> servers) {
            this.m.servers = servers;
            return this;
        }

        public TopicParamsBuilder topic(String topic) {
            this.m.topic = topic;
            return this;
        }

        public TopicParamsBuilder apiKey(String apiKey) {
            this.m.apiKey = apiKey;
            return this;
        }

        public TopicParamsBuilder apiSecret(String apiSecret) {
            this.m.apiSecret = apiSecret;
            return this;
        }

        public TopicParamsBuilder consumerGroup(String consumerGroup) {
            this.m.consumerGroup = consumerGroup;
            return this;
        }

        public TopicParamsBuilder consumerInstance(String consumerInstance) {
            this.m.consumerInstance = consumerInstance;
            return this;
        }

        public TopicParamsBuilder fetchTimeout(int fetchTimeout) {
            this.m.fetchTimeout = fetchTimeout;
            return this;
        }

        public TopicParamsBuilder fetchLimit(int fetchLimit) {
            this.m.fetchLimit = fetchLimit;
            return this;
        }

        public TopicParamsBuilder useHttps(boolean useHttps) {
            this.m.useHttps = useHttps;
            return this;
        }

        public TopicParamsBuilder allowSelfSignedCerts(boolean allowSelfSignedCerts) {
            this.m.allowSelfSignedCerts = allowSelfSignedCerts;
            return this;
        }

        public TopicParamsBuilder userName(String userName) {
            this.m.userName = userName;
            return this;
        }

        public TopicParamsBuilder password(String password) {
            this.m.password = password;
            return this;
        }

        public TopicParamsBuilder environment(String environment) {
            this.m.environment = environment;
            return this;
        }

        public TopicParamsBuilder aftEnvironment(String aftEnvironment) {
            this.m.aftEnvironment = aftEnvironment;
            return this;
        }

        public TopicParamsBuilder partner(String partner) {
            this.m.partner = partner;
            return this;
        }

        public TopicParamsBuilder latitude(String latitude) {
            this.m.latitude = latitude;
            return this;
        }

        public TopicParamsBuilder longitude(String longitude) {
            this.m.longitude = longitude;
            return this;
        }

        public TopicParamsBuilder additionalProps(Map<String, String> additionalProps) {
            this.m.additionalProps = additionalProps;
            return this;
        }

        public TopicParamsBuilder partitionId(String partitionId) {
            this.m.partitionId = partitionId;
            return this;
        }

        public BusTopicParams build() {
            return m;
        }

        public TopicParamsBuilder managed(boolean managed) {
            this.m.managed = managed;
            return this;
        }
    }
}

