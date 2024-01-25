/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.att.nsa.cambria.client.CambriaConsumer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer.CambriaConsumerWrapper;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer.FetchingBusConsumer;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer.KafkaConsumerWrapper;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.springframework.test.util.ReflectionTestUtils;

public class BusConsumerTest extends TopicTestBase {

    private static final int SHORT_TIMEOUT_MILLIS = 10;
    private static final int LONG_TIMEOUT_MILLIS = 3000;

    @Mock
    KafkaConsumer<String, String> mockedKafkaConsumer;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testFetchingBusConsumer() {
        // should not be negative
        var cons = new FetchingBusConsumerImpl(makeBuilder().fetchTimeout(-1).build());
        assertThat(cons.getSleepTime()).isEqualTo(PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH);

        // should not be zero
        cons = new FetchingBusConsumerImpl(makeBuilder().fetchTimeout(0).build());
        assertThat(cons.getSleepTime()).isEqualTo(PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH);

        // should not be too large
        cons = new FetchingBusConsumerImpl(
                        makeBuilder().fetchTimeout(PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH + 100).build());
        assertThat(cons.getSleepTime()).isEqualTo(PolicyEndPointProperties.DEFAULT_TIMEOUT_MS_FETCH);

        // should not be what was specified
        cons = new FetchingBusConsumerImpl(makeBuilder().fetchTimeout(100).build());
        assertThat(cons.getSleepTime()).isEqualTo(100);
    }

    @Test
    public void testFetchingBusConsumerSleepAfterFetchFailure() throws InterruptedException {

        var cons = new FetchingBusConsumerImpl(makeBuilder().fetchTimeout(SHORT_TIMEOUT_MILLIS).build()) {

            private CountDownLatch started = new CountDownLatch(1);

            @Override
            protected void sleepAfterFetchFailure() {
                started.countDown();
                super.sleepAfterFetchFailure();
            }
        };

        // full sleep
        long tstart = System.currentTimeMillis();
        cons.sleepAfterFetchFailure();
        assertThat(System.currentTimeMillis() - tstart).isGreaterThanOrEqualTo(SHORT_TIMEOUT_MILLIS);

        // close while sleeping - sleep should halt prematurely
        cons.fetchTimeout = LONG_TIMEOUT_MILLIS;
        cons.started = new CountDownLatch(1);
        Thread thread = new Thread(cons::sleepAfterFetchFailure);
        tstart = System.currentTimeMillis();
        thread.start();
        cons.started.await();
        cons.close();
        thread.join();
        assertThat(System.currentTimeMillis() - tstart).isLessThan(LONG_TIMEOUT_MILLIS);

        // interrupt while sleeping - sleep should halt prematurely
        cons.fetchTimeout = LONG_TIMEOUT_MILLIS;
        cons.started = new CountDownLatch(1);
        thread = new Thread(cons::sleepAfterFetchFailure);
        tstart = System.currentTimeMillis();
        thread.start();
        cons.started.await();
        thread.interrupt();
        thread.join();
        assertThat(System.currentTimeMillis() - tstart).isLessThan(LONG_TIMEOUT_MILLIS);
    }

    @Test
    public void testCambriaConsumerWrapper() {
        // verify that different wrappers can be built
        new CambriaConsumerWrapper(makeBuilder().build());
        new CambriaConsumerWrapper(makeBuilder().useHttps(false).build());
        new CambriaConsumerWrapper(makeBuilder().useHttps(true).build());
        new CambriaConsumerWrapper(makeBuilder().useHttps(true).allowSelfSignedCerts(false).build());
        new CambriaConsumerWrapper(makeBuilder().useHttps(true).allowSelfSignedCerts(true).build());
        new CambriaConsumerWrapper(makeBuilder().apiKey(null).build());
        new CambriaConsumerWrapper(makeBuilder().apiSecret(null).build());
        new CambriaConsumerWrapper(makeBuilder().apiKey(null).apiSecret(null).build());
        new CambriaConsumerWrapper(makeBuilder().userName(null).build());
        new CambriaConsumerWrapper(makeBuilder().password(null).build());

        assertThatCode(() -> new CambriaConsumerWrapper(makeBuilder().userName(null).password(null).build()))
                        .doesNotThrowAnyException();
    }

    @Test
    public void testCambriaConsumerWrapperFetch() throws Exception {
        CambriaConsumer inner = mock(CambriaConsumer.class);
        List<String> lst = Arrays.asList(MY_MESSAGE, MY_MESSAGE2);
        when(inner.fetch()).thenReturn(lst);

        CambriaConsumerWrapper cons = new CambriaConsumerWrapper(builder.build());
        ReflectionTestUtils.setField(cons, "consumer", inner);

        assertEquals(lst, IteratorUtils.toList(cons.fetch().iterator()));

        // arrange to throw exception next time fetch is called
        IOException ex = new IOException(EXPECTED);
        when(inner.fetch()).thenThrow(ex);

        cons.fetchTimeout = 10;

        try {
            cons.fetch();
            fail("missing exception");

        } catch (IOException e) {
            assertEquals(ex, e);
        }
    }

    @Test
    public void testCambriaConsumerWrapperClose() {
        CambriaConsumerWrapper cons = new CambriaConsumerWrapper(builder.build());
        assertThatCode(cons::close).doesNotThrowAnyException();
    }

    @Test
    public void testCambriaConsumerWrapperToString() {
        assertNotNull(new CambriaConsumerWrapper(makeBuilder().build()).toString());
    }

    @Test
    public void testKafkaConsumerWrapper() {
        // verify that different wrappers can be built
        assertThatCode(() -> new KafkaConsumerWrapper(makeKafkaBuilder().build())).doesNotThrowAnyException();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKafkaConsumerWrapper_InvalidTopic() {
        new KafkaConsumerWrapper(makeBuilder().topic(null).build());
    }

    @Test
    public void testKafkaConsumerWrapperFetch() {

        //Setup Properties for consumer
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaProps.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "test");
        kafkaProps.setProperty("enable.auto.commit", "true");
        kafkaProps.setProperty("auto.commit.interval.ms", "1000");
        kafkaProps.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaProps.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumerWrapper kafka = new KafkaConsumerWrapper(makeKafkaBuilder().build());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProps);
        kafka.consumer = consumer;

        assertThrows(java.lang.IllegalStateException.class, () -> kafka.fetch().iterator().hasNext());
        consumer.close();
    }

    @Test
    public void testFetchNoMessages() throws IOException {
        KafkaConsumerWrapper kafkaConsumerWrapper = new KafkaConsumerWrapper(makeKafkaBuilder().build());
        kafkaConsumerWrapper.consumer = mockedKafkaConsumer;

        when(mockedKafkaConsumer.poll(any())).thenReturn(new ConsumerRecords<>(Collections.emptyMap()));

        Iterable<String> result = kafkaConsumerWrapper.fetch();

        verify(mockedKafkaConsumer, times(1)).poll(any());

        assertThat(result != null);

        assertThat(!result.iterator().hasNext());

        mockedKafkaConsumer.close();
    }

    @Test
    public void testFetchWithMessages() {
        // Setup
        KafkaConsumerWrapper kafkaConsumerWrapper = new KafkaConsumerWrapper(makeKafkaBuilder().build());
        kafkaConsumerWrapper.consumer = mockedKafkaConsumer;

        ConsumerRecord<String, String> record = new ConsumerRecord<>("my-effective-topic", 0, 0, "key", "value");
        Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<>();
        recordsMap.put(new TopicPartition("my-effective-topic", 0), Collections.singletonList(record));
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(recordsMap);

        when(mockedKafkaConsumer.poll(any())).thenReturn(consumerRecords);

        Iterable<String> result = kafkaConsumerWrapper.fetch();

        verify(mockedKafkaConsumer, times(1)).poll(any());

        verify(mockedKafkaConsumer, times(1)).commitSync(any(Map.class));

        assertThat(result != null);

        assertThat(result.iterator().hasNext());

        assertThat(result.iterator().next().equals("value"));

        mockedKafkaConsumer.close();
    }

    @Test
    public void testFetchWithMessagesAndTraceparent() {
        // Setup
        KafkaConsumerWrapper kafkaConsumerWrapper = new KafkaConsumerWrapper(makeKafkaBuilder().build());
        kafkaConsumerWrapper.consumer = mockedKafkaConsumer;

        ConsumerRecord<String, String> record = new ConsumerRecord<>("my-effective-topic", 0, 0, "key", "value");
        record.headers().add(
                "traceparent",
                "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01".getBytes(StandardCharsets.UTF_8)
        );

        Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<>();
        recordsMap.put(new TopicPartition("my-effective-topic", 0), Collections.singletonList(record));
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(recordsMap);

        when(mockedKafkaConsumer.poll(any())).thenReturn(consumerRecords);

        Iterable<String> result = kafkaConsumerWrapper.fetch();

        verify(mockedKafkaConsumer, times(1)).poll(any());

        verify(mockedKafkaConsumer, times(1)).commitSync(any(Map.class));

        assertThat(result != null);

        assertThat(result.iterator().hasNext());

        assertThat(result.iterator().next().equals("value"));

        mockedKafkaConsumer.close();
    }


    @Test
    public void testKafkaConsumerWrapperClose() {
        assertThatCode(() -> new KafkaConsumerWrapper(makeKafkaBuilder().build()).close()).doesNotThrowAnyException();
    }

    @Test
    public void testKafkaConsumerWrapperToString() {
        assertNotNull(new KafkaConsumerWrapper(makeKafkaBuilder().build()) {}.toString());
    }

    private static class FetchingBusConsumerImpl extends FetchingBusConsumer {

        protected FetchingBusConsumerImpl(BusTopicParams busTopicParams) {
            super(busTopicParams);
        }

        @Override
        public Iterable<String> fetch() {
            return null;
        }
    }
}
