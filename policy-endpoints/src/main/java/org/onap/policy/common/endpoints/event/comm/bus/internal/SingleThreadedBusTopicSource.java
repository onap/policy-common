/*-
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018-2019 Samsung Electronics Co., Ltd.
 * Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.bus.BusTopicSource;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This topic source implementation specializes in reading messages over a bus topic source and
 * notifying its listeners.
 */
public abstract class SingleThreadedBusTopicSource extends BusTopicBase
        implements Runnable, BusTopicSource {

    /**
     * Not to be converted to PolicyLogger. This will contain all instract /out traffic and only
     * that in a single file in a concise format.
     */
    private static Logger logger = LoggerFactory.getLogger(SingleThreadedBusTopicSource.class);

    /**
     * Bus consumer group.
     */
    @Getter
    protected final String consumerGroup;

    /**
     * Bus consumer instance.
     */
    @Getter
    protected final String consumerInstance;

    /**
     * Bus fetch timeout.
     */
    @Getter
    protected final int fetchTimeout;

    /**
     * Bus fetch limit.
     */
    @Getter
    protected final int fetchLimit;

    /**
     * Message Bus Consumer.
     */
    protected BusConsumer consumer;

    /**
     * Independent thread reading message over my topic.
     */
    protected Thread busPollerThread;

    /**
     * Used to indicate that {@link #stop()} has been called. Initially, it so indicates
     * (i.e., because {@link #start()} has not been called yet). Replaced with a new,
     * unfinished latch when start() is invoked, decremented when stop() is invoked. Never
     * null.
     */
    private CountDownLatch stopped = new CountDownLatch(0);


    /**
     * Constructor.
     *
     * @param busTopicParams topic parameters
     *
     * @throws IllegalArgumentException An invalid parameter passed in
     */
    protected SingleThreadedBusTopicSource(BusTopicParams busTopicParams) {

        super(busTopicParams);

        if (busTopicParams.isConsumerGroupInvalid() && busTopicParams.isConsumerInstanceInvalid()) {
            this.consumerGroup = UUID.randomUUID().toString();
            this.consumerInstance = NetworkUtil.getHostname();

        } else if (busTopicParams.isConsumerGroupInvalid()) {
            this.consumerGroup = UUID.randomUUID().toString();
            this.consumerInstance = busTopicParams.getConsumerInstance();

        } else if (busTopicParams.isConsumerInstanceInvalid()) {
            this.consumerGroup = busTopicParams.getConsumerGroup();
            this.consumerInstance = UUID.randomUUID().toString();

        } else {
            this.consumerGroup = busTopicParams.getConsumerGroup();
            this.consumerInstance = busTopicParams.getConsumerInstance();
        }

        if (busTopicParams.getFetchTimeout() <= 0) {
            this.fetchTimeout = PolicyEndPointProperties.NO_TIMEOUT_MS_FETCH;
        } else {
            this.fetchTimeout = busTopicParams.getFetchTimeout();
        }

        if (busTopicParams.getFetchLimit() <= 0) {
            this.fetchLimit = PolicyEndPointProperties.NO_LIMIT_FETCH;
        } else {
            this.fetchLimit = busTopicParams.getFetchLimit();
        }

    }

    /**
     * Initialize the Bus client.
     */
    public abstract void init() throws MalformedURLException;

    @Override
    public void register(TopicListener topicListener) {

        super.register(topicListener);

        try {
            if (!alive && !locked) {
                this.start();
            } else {
                logger.info("{}: register: start not attempted", this);
            }
        } catch (Exception e) {
            logger.warn("{}: cannot start after registration of because of: {}", this, topicListener, e);
        }
    }

    @Override
    public void unregister(TopicListener topicListener) {
        boolean stop;
        synchronized (this) {
            super.unregister(topicListener);
            stop = this.topicListeners.isEmpty();
        }

        if (stop) {
            this.stop();
        }
    }

    @Override
    public boolean start() {
        logger.info("{}: starting", this);

        synchronized (this) {

            if (alive) {
                return true;
            }

            if (locked) {
                throw new IllegalStateException(this + " is locked.");
            }

            if (this.busPollerThread == null || !this.busPollerThread.isAlive() || this.consumer == null) {

                try {
                    this.init();
                    this.alive = true;
                    this.stopped = new CountDownLatch(1);
                    this.busPollerThread = makePollerThread();
                    this.busPollerThread.setName(this.getTopicCommInfrastructure() + "-source-" + this.getTopic());
                    busPollerThread.start();
                    return true;
                } catch (Exception e) {
                    throw new IllegalStateException(this + ": cannot start", e);
                }
            }
        }

        return false;
    }

    /**
     * Makes a new thread to be used for polling.
     *
     * @return a new Thread
     */
    protected Thread makePollerThread() {
        return new Thread(this);
    }

    @Override
    public boolean stop() {
        logger.info("{}: stopping", this);

        synchronized (this) {
            final BusConsumer consumerCopy = this.consumer;

            this.alive = false;
            this.consumer = null;
            this.stopped.countDown();

            if (consumerCopy != null) {
                try {
                    consumerCopy.close();
                } catch (Exception e) {
                    logger.warn("{}: stop failed because of {}", this, e.getMessage(), e);
                }
            }
        }

        Thread.yield();

        return true;
    }

    /**
     * Run thread method for the Bus Reader.
     */
    @Override
    public void run() {
        long exceptionWaitTime = fetchTimeout == PolicyEndPointProperties.NO_TIMEOUT_MS_FETCH
                        ? PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH
                        : fetchTimeout;

        while (this.alive) {
            long begin = System.currentTimeMillis();
            long remaining = 0;

            try {
                fetchAllMessages();
            } catch (IOException | RuntimeException e) {
                logger.error("{}: cannot fetch", this, e);
                remaining = begin + exceptionWaitTime - System.currentTimeMillis();
            }

            try {
                if (remaining > 0) {
                    /*
                     * fetch failed too quickly, which might indicate that we're in a
                     * fast-fail loop, so we force it to wait the full fetch time
                     */
                    CountDownLatch stopper;
                    synchronized (this) {
                        stopper = this.stopped;
                    }

                    logger.info("{}: sleeping {}ms", this, remaining);
                    if (stopper.await(remaining, TimeUnit.MILLISECONDS)) {
                        logger.info("{}: stop() called - sleep terminated");
                    }
                }

            } catch (InterruptedException e) {
                logger.warn("{}: sleep interrupted", this, e);
                Thread.currentThread().interrupt();
            }
        }

        logger.info("{}: exiting thread", this);
    }

    private void fetchAllMessages() throws IOException {
        for (String event : this.consumer.fetch()) {
            synchronized (this) {
                this.recentEvents.add(event);
            }

            NetLoggerUtil.log(EventType.IN, this.getTopicCommInfrastructure(), this.topic, event);

            broadcast(event);

            if (!this.alive) {
                return;
            }
        }
    }

    @Override
    public boolean offer(String event) {
        if (!this.alive) {
            throw new IllegalStateException(this + " is not alive.");
        }

        synchronized (this) {
            this.recentEvents.add(event);
        }

        NetLoggerUtil.log(EventType.IN, this.getTopicCommInfrastructure(), this.topic, event);

        return broadcast(event);
    }

    @Override
    public String toString() {
        return "SingleThreadedBusTopicSource [consumerGroup=" + consumerGroup + ", consumerInstance=" + consumerInstance
                + ", fetchTimeout=" + fetchTimeout + ", fetchLimit=" + fetchLimit + ", consumer=" + this.consumer
                + ", alive=" + alive + ", locked=" + locked + ", uebThread=" + busPollerThread + ", topicListeners="
                + topicListeners.size() + ", toString()=" + super.toString() + "]";
    }

    @Override
    public void shutdown() {
        this.stop();
        this.topicListeners.clear();
    }
}
