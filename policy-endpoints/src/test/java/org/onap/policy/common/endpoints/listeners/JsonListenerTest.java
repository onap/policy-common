/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;
import org.slf4j.LoggerFactory;

public class JsonListenerTest {

    /**
     * Used to attach an appender to the class' logger.
     */
    private static final Logger logger = (Logger) LoggerFactory.getLogger(JsonListener.class);
    private static final ExtractAppender appender = new ExtractAppender();

    /**
     * Original logging level for the logger.
     */
    private static Level saveLevel;

    private static final CommInfrastructure INFRA = CommInfrastructure.NOOP;
    private static final String TOPIC = "my-topic";
    private static final String JSON = "{'abc':'def'}".replace('\'', '"');

    private JsonListener primary;

    /**
     * Initializes statics.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        saveLevel = logger.getLevel();
        logger.setLevel(Level.INFO);

        appender.setContext(logger.getLoggerContext());
        appender.start();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        logger.setLevel(saveLevel);
        appender.stop();
    }

    /**
     * Initializes mocks and a listener.
     */
    @Before
    public void setUp() {
        appender.clearExtractions();

        primary = new JsonListener() {
            @Override
            public void onTopicEvent(CommInfrastructure infra, String topic, StandardCoderObject sco) {
                // do nothing
            }
        };
    }

    @After
    public void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    public void testOnTopicEvent() {
        logger.addAppender(appender);

        primary = spy(primary);

        // success
        primary.onTopicEvent(INFRA, TOPIC, JSON);
        verify(primary).onTopicEvent(eq(INFRA), eq(TOPIC), any(StandardCoderObject.class));

        // repeat
        primary.onTopicEvent(INFRA, TOPIC, JSON);
        verify(primary, times(2)).onTopicEvent(eq(INFRA), eq(TOPIC), any(StandardCoderObject.class));

        assertFalse(appender.getExtracted().toString().contains("unable to decode"));

        // invalid json - decode fails
        appender.clearExtractions();
        primary.onTopicEvent(INFRA, TOPIC, "[");
        assertTrue(appender.getExtracted().toString().contains("unable to decode"));
        verify(primary, times(2)).onTopicEvent(any(), any(), any(StandardCoderObject.class));
    }
}
