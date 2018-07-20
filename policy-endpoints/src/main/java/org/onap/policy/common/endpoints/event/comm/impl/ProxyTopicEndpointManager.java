/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.common.endpoints.event.comm.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSource;
import org.onap.policy.common.endpoints.event.comm.bus.NoopTopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicSink;
import org.onap.policy.common.endpoints.event.comm.bus.UebTopicSource;
import org.onap.policy.common.endpoints.event.comm.bus.impl.IndexedDmaapTopicSinkFactory;
import org.onap.policy.common.endpoints.event.comm.bus.impl.IndexedDmaapTopicSourceFactory;
import org.onap.policy.common.endpoints.event.comm.bus.impl.IndexedNoopTopicSinkFactory;
import org.onap.policy.common.endpoints.event.comm.bus.impl.IndexedUebTopicSinkFactory;
import org.onap.policy.common.endpoints.event.comm.bus.impl.IndexedUebTopicSourceFactory;
import org.onap.policy.common.properties.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of the Topic Endpoint Manager, proxies operations to appropriate
 * implementations according to the communication infrastructure that are supported
 */
public class ProxyTopicEndpointManager implements TopicEndpoint {
    /**
     * Logger
     */
    private static Logger logger = LoggerFactory.getLogger(ProxyTopicEndpointManager.class);
    /**
     * Is this element locked?
     */
    protected volatile boolean locked = false;

    /**
     * Is this element alive?
     */
    protected volatile boolean alive = false;

    /**
     * singleton for global access
     */
    private static final TopicEndpoint manager = new ProxyTopicEndpointManager();

    /**
     * Get the singelton instance.
     * 
     * @return the instance
     */
    public static TopicEndpoint getInstance() {
        return manager;
    }

    @Override
    public List<TopicSource> addTopicSources(Properties properties) {

        // 1. Create UEB Sources
        // 2. Create DMAAP Sources

        final List<TopicSource> sources = new ArrayList<>();

        sources.addAll(IndexedUebTopicSourceFactory.getInstance().build(properties));
        sources.addAll(IndexedDmaapTopicSourceFactory.getInstance().build(properties));

        if (this.isLocked()) {
            for (final TopicSource source : sources) {
                source.lock();
            }
        }

        return sources;
    }

    @Override
    public List<TopicSink> addTopicSinks(Properties properties) {
        // 1. Create UEB Sinks
        // 2. Create DMAAP Sinks

        final List<TopicSink> sinks = new ArrayList<>();

        sinks.addAll(IndexedUebTopicSinkFactory.getInstance().build(properties));
        sinks.addAll(IndexedDmaapTopicSinkFactory.getInstance().build(properties));
        sinks.addAll(IndexedNoopTopicSinkFactory.getInstance().build(properties));

        if (this.isLocked()) {
            for (final TopicSink sink : sinks) {
                sink.lock();
            }
        }

        return sinks;
    }

    @Override
    public List<TopicSource> getTopicSources() {

        final List<TopicSource> sources = new ArrayList<>();

        sources.addAll(IndexedUebTopicSourceFactory.getInstance().inventory());
        sources.addAll(IndexedDmaapTopicSourceFactory.getInstance().inventory());

        return sources;
    }

    @Override
    public List<TopicSink> getTopicSinks() {

        final List<TopicSink> sinks = new ArrayList<>();

        sinks.addAll(IndexedUebTopicSinkFactory.getInstance().inventory());
        sinks.addAll(IndexedDmaapTopicSinkFactory.getInstance().inventory());
        sinks.addAll(IndexedNoopTopicSinkFactory.getInstance().inventory());

        return sinks;
    }

    @JsonIgnore
    @Override
    public List<UebTopicSource> getUebTopicSources() {
        return IndexedUebTopicSourceFactory.getInstance().inventory();
    }

    @JsonIgnore
    @Override
    public List<DmaapTopicSource> getDmaapTopicSources() {
        return IndexedDmaapTopicSourceFactory.getInstance().inventory();
    }

    @JsonIgnore
    @Override
    public List<UebTopicSink> getUebTopicSinks() {
        return IndexedUebTopicSinkFactory.getInstance().inventory();
    }

    @JsonIgnore
    @Override
    public List<DmaapTopicSink> getDmaapTopicSinks() {
        return IndexedDmaapTopicSinkFactory.getInstance().inventory();
    }

    @JsonIgnore
    @Override
    public List<NoopTopicSink> getNoopTopicSinks() {
        return IndexedNoopTopicSinkFactory.getInstance().inventory();
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

        boolean success = true;
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

        boolean success = true;
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
     *
     * @return list of managed endpoints
     */
    @JsonIgnore
    protected List<Startable> getEndpoints() {
        final List<Startable> endpoints = new ArrayList<>();

        endpoints.addAll(this.getTopicSources());
        endpoints.addAll(this.getTopicSinks());

        return endpoints;
    }

    @Override
    public void shutdown() {
        IndexedUebTopicSourceFactory.getInstance().destroy();
        IndexedUebTopicSinkFactory.getInstance().destroy();
        IndexedNoopTopicSinkFactory.getInstance().destroy();

        IndexedDmaapTopicSourceFactory.getInstance().destroy();
        IndexedDmaapTopicSinkFactory.getInstance().destroy();
    }

    @Override
    public boolean isAlive() {
        return this.alive;
    }

    @Override
    public boolean lock() {

        synchronized (this) {
            if (this.locked) {
                return true;
            }

            this.locked = true;
        }

        for (final TopicSource source : this.getTopicSources()) {
            source.lock();
        }

        for (final TopicSink sink : this.getTopicSinks()) {
            sink.lock();
        }

        return true;
    }

    @Override
    public boolean unlock() {
        synchronized (this) {
            if (!this.locked) {
                return true;
            }

            this.locked = false;
        }

        for (final TopicSource source : this.getTopicSources()) {
            source.unlock();
        }

        for (final TopicSink sink : this.getTopicSinks()) {
            sink.unlock();
        }

        return true;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
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
        }
        return sources;
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
    public TopicSource getTopicSource(Topic.CommInfrastructure commType, String topicName) {

        if (commType == null) {
            throw parmException(topicName);
        }

        if (topicName == null) {
            throw parmException(topicName);
        }

        switch (commType) {
            case UEB:
                return this.getUebTopicSource(topicName);
            case DMAAP:
                return this.getDmaapTopicSource(topicName);
            default:
                throw new UnsupportedOperationException("Unsupported " + commType.name());
        }
    }

    private IllegalArgumentException parmException(String topicName) {
        return new IllegalArgumentException(
                "Invalid parameter: a communication infrastructure required to fetch " + topicName);
    }

    @Override
    public TopicSink getTopicSink(Topic.CommInfrastructure commType, String topicName) {
        if (commType == null) {
            throw parmException(topicName);
        }

        if (topicName == null) {
            throw parmException(topicName);
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
    public List<TopicSink> getTopicSinks(String topicName) {
        if (topicName == null) {
            throw parmException(topicName);
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

    private void logNoSink(String topicName, Exception ex) {
        logger.debug("No sink for topic: {}", topicName, ex);
    }

    @Override
    public UebTopicSource getUebTopicSource(String topicName) {
        return IndexedUebTopicSourceFactory.getInstance().get(topicName);
    }

    @Override
    public UebTopicSink getUebTopicSink(String topicName) {
        return IndexedUebTopicSinkFactory.getInstance().get(topicName);
    }

    @Override
    public DmaapTopicSource getDmaapTopicSource(String topicName) {
        return IndexedDmaapTopicSourceFactory.getInstance().get(topicName);
    }

    @Override
    public DmaapTopicSink getDmaapTopicSink(String topicName) {
        return IndexedDmaapTopicSinkFactory.getInstance().get(topicName);
    }

    @Override
    public NoopTopicSink getNoopTopicSink(String topicName) {
        return IndexedNoopTopicSinkFactory.getInstance().get(topicName);
    }

}
