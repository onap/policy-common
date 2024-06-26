/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.common.endpoints.listeners;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;
import org.slf4j.LoggerFactory;

class RequestIdDispatcherTest {

    /**
     * Used to attach an appender to the class' logger.
     */
    private static final Logger logger = (Logger) LoggerFactory.getLogger(RequestIdDispatcher.class);
    private static final ExtractAppender appender = new ExtractAppender();

    /**
     * Original logging level for the logger.
     */
    private static Level saveLevel;

    private static final CommInfrastructure INFRA = CommInfrastructure.NOOP;
    private static final String REQID_FIELD = "requestId";
    private static final String TOPIC = "my-topic";
    private static final String REQID1 = "request-1";
    private static final String REQID2 = "request-2";

    private static final Coder coder = new StandardCoder();

    private RequestIdDispatcher<MyMessage> primary;
    private TypedMessageListener<MyMessage> secondary1;
    private TypedMessageListener<MyMessage> secondary2;
    private TypedMessageListener<MyMessage> secondary3;
    private TypedMessageListener<MyMessage> secondary4;
    private MyMessage status;

    /**
     * Initializes statics.
     */
    @BeforeAll
    public static void setUpBeforeClass() {
        saveLevel = logger.getLevel();
        logger.setLevel(Level.INFO);

        appender.setContext(logger.getLoggerContext());
        appender.start();
    }

    @AfterAll
    public static void tearDownAfterClass() {
        logger.setLevel(saveLevel);
        appender.stop();
    }

    /**
     * Create various mocks and primary listener.
     */
    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {
        appender.clearExtractions();

        secondary1 = mock(TypedMessageListener.class);
        secondary2 = mock(TypedMessageListener.class);
        secondary3 = mock(TypedMessageListener.class);
        secondary4 = mock(TypedMessageListener.class);

        primary = new RequestIdDispatcher<>(MyMessage.class, REQID_FIELD);
    }

    @AfterEach
    public void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    void testRegisterMessageListener() {
        primary.register(secondary1);

        // should process message that does not have a request id
        status = new MyMessage();
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1).onTopicEvent(INFRA, TOPIC, status);

        // should process again
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1, times(2)).onTopicEvent(INFRA, TOPIC, status);

        // should NOT process a message that has a request id
        status = new MyMessage(REQID1);
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1, never()).onTopicEvent(INFRA, TOPIC, status);
    }

    @Test
    void testRegisterStringMessageListener() {
        primary.register(REQID1, secondary1);

        // should NOT process message that does not have a request id
        status = new MyMessage();
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1, never()).onTopicEvent(INFRA, TOPIC, status);

        // should process a message that has the desired request id
        status = new MyMessage(REQID1);
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1).onTopicEvent(INFRA, TOPIC, status);

        // should process again
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1, times(2)).onTopicEvent(INFRA, TOPIC, status);

        // should NOT process a message that does NOT have the desired request id
        status = new MyMessage(REQID2);
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1, never()).onTopicEvent(INFRA, TOPIC, status);

        // null request id => exception
        assertThatIllegalArgumentException().isThrownBy(() -> primary.register(null, secondary1));

        // empty request id => exception
        assertThatIllegalArgumentException().isThrownBy(() -> primary.register("", secondary1));
    }

    @Test
    void testUnregisterMessageListener() {
        primary.register(secondary1);
        primary.register(secondary2);

        // should process message
        status = new MyMessage();
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1).onTopicEvent(INFRA, TOPIC, status);
        verify(secondary2).onTopicEvent(INFRA, TOPIC, status);

        primary.unregister(secondary1);

        // should NOT process again
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1, times(1)).onTopicEvent(INFRA, TOPIC, status);

        // other listener should still have processed it
        verify(secondary2, times(2)).onTopicEvent(INFRA, TOPIC, status);
    }

    @Test
    void testUnregisterString() {
        primary.register(REQID1, secondary1);
        primary.register(REQID2, secondary2);

        // should process a message that has the desired request id
        status = new MyMessage(REQID1);
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1).onTopicEvent(INFRA, TOPIC, status);

        primary.unregister(REQID1);

        // should NOT re-process
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1, times(1)).onTopicEvent(INFRA, TOPIC, status);

        // secondary should still be able to process
        status = new MyMessage(REQID2);
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary2).onTopicEvent(INFRA, TOPIC, status);
    }

    @Test
    void testOnTopicEvent() {
        primary.register(REQID1, secondary1);
        primary.register(REQID2, secondary2);
        primary.register(secondary3);
        primary.register(secondary4);

        // without request id
        status = new MyMessage();
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1, never()).onTopicEvent(INFRA, TOPIC, status);
        verify(secondary2, never()).onTopicEvent(INFRA, TOPIC, status);
        verify(secondary3).onTopicEvent(INFRA, TOPIC, status);
        verify(secondary4).onTopicEvent(INFRA, TOPIC, status);

        // with request id
        status = new MyMessage(REQID1);
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        verify(secondary1).onTopicEvent(INFRA, TOPIC, status);
        verify(secondary2, never()).onTopicEvent(INFRA, TOPIC, status);
        verify(secondary3, never()).onTopicEvent(INFRA, TOPIC, status);
        verify(secondary4, never()).onTopicEvent(INFRA, TOPIC, status);
    }

    @Test
    void testOfferToListener() {
        logger.addAppender(appender);

        // no listener for this
        status = new MyMessage(REQID1);
        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));

        assertFalse(appender.getExtracted().toString().contains("failed to process message"));

        // listener throws an exception
        primary.register(secondary1);

        status = new MyMessage();

        RuntimeException ex = new RuntimeException("expected exception");
        doThrow(ex).when(secondary1).onTopicEvent(INFRA, TOPIC, status);

        primary.onTopicEvent(INFRA, TOPIC, makeSco(status));
        assertTrue(appender.getExtracted().toString().contains("failed to process message"));
    }

    /**
     * Makes a standard object from a status message.
     *
     * @param source message to be converted
     * @return a standard object representing the message
     */
    private StandardCoderObject makeSco(MyMessage source) {
        try {
            return coder.toStandard(source);

        } catch (CoderException e) {
            throw new RuntimeException(e);
        }
    }

    protected static class MyMessage {
        private String requestId;

        public MyMessage() {
            super();
        }

        public MyMessage(String requestId) {
            this.requestId = requestId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            MyMessage other = (MyMessage) obj;
            if (requestId == null) {
                if (other.requestId != null) {
                    return false;
                }
            } else if (!requestId.equals(other.requestId)) {
                return false;
            }
            return true;
        }
    }
}
