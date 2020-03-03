/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm.client;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;

/**
 * A "bidirectional" topic, which is a pair of topics, one of which is used to publish
 * requests and the other to receive responses.
 */
@Getter
public class BidirectionalTopicClient {
    private final String sinkTopic;
    private final String sourceTopic;
    private final TopicSink sink;
    private final TopicSource source;
    private final CommInfrastructure sinkTopicCommInfrastructure;
    private final CommInfrastructure sourceTopicCommInfrastructure;

    /**
     * Constructs the object.
     *
     * @param sinkTopic sink topic name
     * @param sourceTopic source topic name
     * @throws BidirectionalTopicClientException if either topic does not exist
     */
    public BidirectionalTopicClient(String sinkTopic, String sourceTopic) throws BidirectionalTopicClientException {
        this.sinkTopic = sinkTopic;
        this.sourceTopic = sourceTopic;

        // init sinkClient
        List<TopicSink> sinks = getTopicEndpointManager().getTopicSinks(sinkTopic);
        if (sinks.isEmpty()) {
            throw new BidirectionalTopicClientException("no sinks for topic: " + sinkTopic);
        } else if (sinks.size() > 1) {
            throw new BidirectionalTopicClientException("too many sinks for topic: " + sinkTopic);
        }

        this.sink = sinks.get(0);

        // init source
        List<TopicSource> sources = getTopicEndpointManager().getTopicSources(Arrays.asList(sourceTopic));
        if (sources.isEmpty()) {
            throw new BidirectionalTopicClientException("no sources for topic: " + sourceTopic);
        } else if (sources.size() > 1) {
            throw new BidirectionalTopicClientException("too many sources for topic: " + sourceTopic);
        }

        this.source = sources.get(0);

        this.sinkTopicCommInfrastructure = sink.getTopicCommInfrastructure();
        this.sourceTopicCommInfrastructure = source.getTopicCommInfrastructure();
    }

    public boolean send(String message) {
        return sink.send(message);
    }

    public void register(TopicListener topicListener) {
        source.register(topicListener);
    }

    public boolean offer(String event) {
        return source.offer(event);
    }

    public void unregister(TopicListener topicListener) {
        source.unregister(topicListener);
    }

    // these may be overridden by junit tests

    protected TopicEndpoint getTopicEndpointManager() {
        return TopicEndpointManager.getManager();
    }
}
