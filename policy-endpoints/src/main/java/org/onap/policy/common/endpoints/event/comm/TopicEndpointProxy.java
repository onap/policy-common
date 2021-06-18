/*
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

package org.onap.policy.common.endpoints.event.comm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import lombok.Getter;
import org.onap.policy.common.capabilities.Startable;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicFactories;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSource;
import org.onap.policy.common.endpoints.event.comm.bus.NoopTopicFactories;
import org.onap.policy.common.endpoints.event.comm.bus.NoopTopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.NoopTopicSource;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicFactories;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicSource;
import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;
import org.onap.policy.common.endpoints.parameters.TopicParameters;
import org.onap.policy.common.gson.annotation.GsonJsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of the Topic Endpoint Manager, proxies operations to the appropriate
 * implementation(s).
 */
class TopicEndpointProxy implements TopicEndpoint {
    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(TopicEndpointProxy.class);

    /**
     * Is this element locked boolean.
     */
    @Getter
    private volatile boolean locked = false;

    /**
     * Is this element alive boolean.
     */
    @Getter
    private volatile boolean alive = false;

    @Override
    public List<Topic> addTopics(Properties properties) {
        List<Topic> topics = new ArrayList<>(addTopicSources(properties));
        topics.addAll(addTopicSinks(properties));
        return topics;
    }

    @Override
    public List<Topic> addTopics(TopicParameterGroup params) {
        List<TopicParameters> sinks =
                        (params.getTopicSinks() != null ? params.getTopicSinks() : Collections.emptyList());
        List<TopicParameters> sources =
                        (params.getTopicSources() != null ? params.getTopicSources() : Collections.emptyList());

        List<Topic> topics = new ArrayList<>(sinks.size() + sources.size());
        topics.addAll(addTopicSources(sources));
        topics.addAll(addTopicSinks(sinks));
        return topics;
    }

    @Override
    public List<TopicSource> addTopicSources(List<TopicParameters> paramList) {
        List<TopicSource> sources = new ArrayList<>(paramList.size());

        for (TopicParameters param : paramList) {
            switch (Topic.CommInfrastructure.valueOf(param.getTopicCommInfrastructure().toUpperCase())) {
                case UEB:
                    sources.add(UebTopicFactories.getSourceFactory().build(param));
                    break;
                case DMAAP:
                    sources.add(DmaapTopicFactories.getSourceFactory().build(param));
                    break;
                case NOOP:
                    sources.add(NoopTopicFactories.getSourceFactory().build(param));
                    break;
                default:
                    logger.debug("Unknown source type {} for topic: {}", param.getTopicCommInfrastructure(),
                                    param.getTopic());
                    break;
            }
        }

        lockSources(sources);

        return sources;
    }

    @Override
    public List<TopicSource> addTopicSources(Properties properties) {

        // 1. Create UEB Sources
        // 2. Create DMAAP Sources
        // 3. Create NOOP Sources

        List<TopicSource> sources = new ArrayList<>();

        sources.addAll(UebTopicFactories.getSourceFactory().build(properties));
        sources.addAll(DmaapTopicFactories.getSourceFactory().build(properties));
        sources.addAll(NoopTopicFactories.getSourceFactory().build(properties));

        lockSources(sources);

        return sources;
    }

    private void lockSources(List<TopicSource> sources) {
        if (this.isLocked()) {
            sources.forEach(TopicSource::lock);
        }
    }

    @Override
    public List<TopicSink> addTopicSinks(List<TopicParameters> paramList) {
        List<TopicSink> sinks = new ArrayList<>(paramList.size());

        for (TopicParameters param : paramList) {
            switch (Topic.CommInfrastructure.valueOf(param.getTopicCommInfrastructure().toUpperCase())) {
                case UEB:
                    sinks.add(UebTopicFactories.getSinkFactory().build(param));
                    break;
                case DMAAP:
                    sinks.add(DmaapTopicFactories.getSinkFactory().build(param));
                    break;
                case NOOP:
                    sinks.add(NoopTopicFactories.getSinkFactory().build(param));
                    break;
                default:
                    logger.debug("Unknown sink type {} for topic: {}", param.getTopicCommInfrastructure(),
                                    param.getTopic());
                    break;
            }
        }

        lockSinks(sinks);

        return sinks;
    }

    @Override
    public List<TopicSink> addTopicSinks(Properties properties) {
        // 1. Create UEB Sinks
        // 2. Create DMAAP Sinks
        // 3. Create NOOP Sinks

        final List<TopicSink> sinks = new ArrayList<>();

        sinks.addAll(UebTopicFactories.getSinkFactory().build(properties));
        sinks.addAll(DmaapTopicFactories.getSinkFactory().build(properties));
        sinks.addAll(NoopTopicFactories.getSinkFactory().build(properties));

        lockSinks(sinks);

        return sinks;
    }

    private void lockSinks(List<TopicSink> sinks) {
        if (this.isLocked()) {
            sinks.forEach(TopicSink::lock);
        }
    }

    @Override
    public List<TopicSource> getTopicSources() {

        final List<TopicSource> sources = new ArrayList<>();

        sources.addAll(UebTopicFactories.getSourceFactory().inventory());
        sources.addAll(DmaapTopicFactories.getSourceFactory().inventory());
        sources.addAll(NoopTopicFactories.getSourceFactory().inventory());

        return sources;
    }

    @Override
    public List<TopicSource> getTopicSources(List<String> topicNames) {

        if (topicNames == null) {
            throw new IllegalArgumentException("must provide a list of topics");
        }

        final List<TopicSource> sources = new ArrayList<>();
        for (final String topic : topicNames) {
            try {
                final TopicSource uebSource = this.getUebTopicSource(topic);
                if (uebSource != null) {
                    sources.add(uebSource);
                }
            } catch (final Exception e) {
                logger.debug("No UEB source for topic: {}", topic, e);
            }

            try {
                final TopicSource dmaapSource = this.getDmaapTopicSource(topic);
                if (dmaapSource != null) {
                    sources.add(dmaapSource);
                }
            } catch (final Exception e) {
                logger.debug("No DMAAP source for topic: {}", topic, e);
            }

            try {
                final TopicSource noopSource = this.getNoopTopicSource(topic);
                if (noopSource != null) {
                    sources.add(noopSource);
                }
            } catch (final Exception e) {
                logger.debug("No NOOP source for topic: {}", topic, e);
            }
        }
        return sources;
    }

    @Override
    public List<TopicSink> getTopicSinks() {

        final List<TopicSink> sinks = new ArrayList<>();

        sinks.addAll(UebTopicFactories.getSinkFactory().inventory());
        sinks.addAll(DmaapTopicFactories.getSinkFactory().inventory());
        sinks.addAll(NoopTopicFactories.getSinkFactory().inventory());

        return sinks;
    }

    @Override
    public List<TopicSink> getTopicSinks(List<String> topicNames) {

        if (topicNames == null) {
            throw new IllegalArgumentException("must provide a list of topics");
        }

        final List<TopicSink> sinks = new ArrayList<>();
        for (final String topic : topicNames) {
            try {
                final TopicSink uebSink = this.getUebTopicSink(topic);
                if (uebSink != null) {
                    sinks.add(uebSink);
                }
            } catch (final Exception e) {
                logger.debug("No UEB sink for topic: {}", topic, e);
            }

            try {
                final TopicSink dmaapSink = this.getDmaapTopicSink(topic);
                if (dmaapSink != null) {
                    sinks.add(dmaapSink);
                }
            } catch (final Exception e) {
                logger.debug("No DMAAP sink for topic: {}", topic, e);
            }

            try {
                final TopicSink noopSink = this.getNoopTopicSink(topic);
                if (noopSink != null) {
                    sinks.add(noopSink);
                }
            } catch (final Exception e) {
                logger.debug("No NOOP sink for topic: {}", topic, e);
            }
        }
        return sinks;
    }

    @Override
    public List<TopicSink> getTopicSinks(String topicName) {
        if (topicName == null) {
            throw parmException(null);
        }

        final List<TopicSink> sinks = new ArrayList<>();

        try {
            sinks.add(this.getUebTopicSink(topicName));
        } catch (final Exception e) {
            logNoSink(topicName, e);
        }

        try {
            sinks.add(this.getDmaapTopicSink(topicName));
        } catch (final Exception e) {
            logNoSink(topicName, e);
        }

        try {
            sinks.add(this.getNoopTopicSink(topicName));
        } catch (final Exception e) {
            logNoSink(topicName, e);
        }

        return sinks;
    }

    @GsonJsonIgnore
    @Override
    public List<UebTopicSource> getUebTopicSources() {
        return UebTopicFactories.getSourceFactory().inventory();
    }

    @GsonJsonIgnore
    @Override
    public List<DmaapTopicSource> getDmaapTopicSources() {
        return DmaapTopicFactories.getSourceFactory().inventory();
    }

    @GsonJsonIgnore
    @Override
    public List<NoopTopicSource> getNoopTopicSources() {
        return NoopTopicFactories.getSourceFactory().inventory();
    }

    @GsonJsonIgnore
    @Override
    public List<UebTopicSink> getUebTopicSinks() {
        return UebTopicFactories.getSinkFactory().inventory();
    }

    @GsonJsonIgnore
    @Override
    public List<DmaapTopicSink> getDmaapTopicSinks() {
        return DmaapTopicFactories.getSinkFactory().inventory();
    }

    @GsonJsonIgnore
    @Override
    public List<NoopTopicSink> getNoopTopicSinks() {
        return NoopTopicFactories.getSinkFactory().inventory();
    }

    @Override
    public boolean start() {

        synchronized (this) {
            if (this.locked) {
                throw new IllegalStateException(this + " is locked");
            }

            if (this.alive) {
                return true;
            }

            this.alive = true;
        }

        final List<Startable> endpoints = this.getEndpoints();

        var success = true;
        for (final Startable endpoint : endpoints) {
            try {
                success = endpoint.start() && success;
            } catch (final Exception e) {
                success = false;
                logger.error("Problem starting endpoint: {}", endpoint, e);
            }
        }

        return success;
    }

    @Override
    public boolean stop() {

        /*
         * stop regardless if it is locked, in other words, stop operation has precedence over
         * locks.
         */
        synchronized (this) {
            this.alive = false;
        }

        final List<Startable> endpoints = this.getEndpoints();

        var success = true;
        for (final Startable endpoint : endpoints) {
            try {
                success = endpoint.stop() && success;
            } catch (final Exception e) {
                success = false;
                logger.error("Problem stopping endpoint: {}", endpoint, e);
            }
        }

        return success;
    }

    /**
     * Gets the endpoints.
     *
     * @return list of managed endpoints
     */
    @GsonJsonIgnore
    protected List<Startable> getEndpoints() {
        final List<Startable> endpoints = new ArrayList<>();

        endpoints.addAll(this.getTopicSources());
        endpoints.addAll(this.getTopicSinks());

        return endpoints;
    }

    @Override
    public void shutdown() {
        this.stop();

        UebTopicFactories.getSourceFactory().destroy();
        UebTopicFactories.getSinkFactory().destroy();

        DmaapTopicFactories.getSourceFactory().destroy();
        DmaapTopicFactories.getSinkFactory().destroy();

        NoopTopicFactories.getSinkFactory().destroy();
        NoopTopicFactories.getSourceFactory().destroy();

    }

    @Override
    public boolean lock() {
        boolean shouldLock;

        synchronized (this) {
            shouldLock = !this.locked;
            this.locked = true;
        }

        if (shouldLock) {
            for (final TopicSource source : this.getTopicSources()) {
                source.lock();
            }

            for (final TopicSink sink : this.getTopicSinks()) {
                sink.lock();
            }
        }

        return true;
    }

    @Override
    public boolean unlock() {
        boolean shouldUnlock;

        synchronized (this) {
            shouldUnlock = this.locked;
            this.locked = false;
        }

        if (shouldUnlock) {
            for (final TopicSource source : this.getTopicSources()) {
                source.unlock();
            }

            for (final TopicSink sink : this.getTopicSinks()) {
                sink.unlock();
            }
        }

        return true;
    }

    @Override
    public TopicSource getTopicSource(Topic.CommInfrastructure commType, String topicName) {

        if (commType == null) {
            throw parmException(topicName);
        }

        if (topicName == null) {
            throw parmException(null);
        }

        switch (commType) {
            case UEB:
                return this.getUebTopicSource(topicName);
            case DMAAP:
                return this.getDmaapTopicSource(topicName);
            case NOOP:
                return this.getNoopTopicSource(topicName);
            default:
                throw new UnsupportedOperationException("Unsupported " + commType.name());
        }
    }

    @Override
    public TopicSink getTopicSink(Topic.CommInfrastructure commType, String topicName) {
        if (commType == null) {
            throw parmException(topicName);
        }

        if (topicName == null) {
            throw parmException(null);
        }

        switch (commType) {
            case UEB:
                return this.getUebTopicSink(topicName);
            case DMAAP:
                return this.getDmaapTopicSink(topicName);
            case NOOP:
                return this.getNoopTopicSink(topicName);
            default:
                throw new UnsupportedOperationException("Unsupported " + commType.name());
        }
    }

    @Override
    public UebTopicSource getUebTopicSource(String topicName) {
        return UebTopicFactories.getSourceFactory().get(topicName);
    }

    @Override
    public UebTopicSink getUebTopicSink(String topicName) {
        return UebTopicFactories.getSinkFactory().get(topicName);
    }

    @Override
    public DmaapTopicSource getDmaapTopicSource(String topicName) {
        return DmaapTopicFactories.getSourceFactory().get(topicName);
    }

    @Override
    public NoopTopicSource getNoopTopicSource(String topicName) {
        return NoopTopicFactories.getSourceFactory().get(topicName);
    }

    @Override
    public DmaapTopicSink getDmaapTopicSink(String topicName) {
        return DmaapTopicFactories.getSinkFactory().get(topicName);
    }

    @Override
    public NoopTopicSink getNoopTopicSink(String topicName) {
        return NoopTopicFactories.getSinkFactory().get(topicName);
    }

    private IllegalArgumentException parmException(String topicName) {
        return new IllegalArgumentException(
            "Invalid parameter: a communication infrastructure required to fetch " + topicName);
    }

    private void logNoSink(String topicName, Exception ex) {
        logger.debug("No sink for topic: {}", topicName, ex);
    }

}
