/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.mr.client.impl.MRSimplerBatchPublisher;
import com.att.nsa.mr.client.response.MRPublisherResponse;
import com.att.nsa.mr.test.clients.ProtocolTypeConstants;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusPublisher.CambriaPublisherWrapper;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusPublisher.DmaapAafPublisherWrapper;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusPublisher.DmaapDmePublisherWrapper;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusPublisher.DmaapPublisherWrapper;

public class BusPublisherTest extends TopicTestBase {

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testCambriaPublisherWrapper() {
        // verify that different wrappers can be built
        new CambriaPublisherWrapper(makeBuilder().build());
        new CambriaPublisherWrapper(makeBuilder().useHttps(false).build());
        new CambriaPublisherWrapper(makeBuilder().useHttps(true).build());
        new CambriaPublisherWrapper(makeBuilder().useHttps(true).allowSelfSignedCerts(false).build());
        new CambriaPublisherWrapper(makeBuilder().useHttps(true).allowSelfSignedCerts(true).build());
        new CambriaPublisherWrapper(makeBuilder().apiKey(null).build());
        new CambriaPublisherWrapper(makeBuilder().apiSecret(null).build());
        new CambriaPublisherWrapper(makeBuilder().apiKey(null).apiSecret(null).build());
        new CambriaPublisherWrapper(makeBuilder().userName(null).build());
        new CambriaPublisherWrapper(makeBuilder().password(null).build());
        new CambriaPublisherWrapper(makeBuilder().userName(null).password(null).build());
    }

    @Test
    public void testCambriaPublisherWrapperSend() throws Exception {
        CambriaBatchingPublisher pub = mock(CambriaBatchingPublisher.class);
        CambriaPublisherWrapper cambria = new CambriaPublisherWrapper(makeBuilder().build());
        cambria.publisher = pub;

        assertTrue(cambria.send(MY_PARTITION, MY_MESSAGE));

        // publisher exception
        when(pub.send(anyString(), anyString())).thenThrow(new IOException(EXPECTED));
        assertFalse(cambria.send(MY_PARTITION2, MY_MESSAGE2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCambriaPublisherWrapperSend_InvalidMsg() {
        CambriaPublisherWrapper cambria = new CambriaPublisherWrapper(makeBuilder().build());
        cambria.publisher = mock(CambriaBatchingPublisher.class);

        cambria.send(MY_PARTITION, null);
    }

    @Test
    public void testCambriaPublisherWrapperClose() throws Exception {
        CambriaBatchingPublisher pub = mock(CambriaBatchingPublisher.class);
        CambriaPublisherWrapper cambria = new CambriaPublisherWrapper(makeBuilder().build());
        cambria.publisher = pub;

        cambria.close();
        verify(pub).close();

        // try again, this time with an exception
        doThrow(new RuntimeException(EXPECTED)).when(pub).close();
        cambria.close();
    }

    @Test
    public void testDmaapPublisherWrapper() {
        // verify with different constructor arguments
        new DmaapAafPublisherWrapper(servers, MY_TOPIC, MY_USERNAME, MY_PASSWD, true);
        new DmaapAafPublisherWrapper(servers, MY_TOPIC, MY_USERNAME, MY_PASSWD, false);
        new DmaapPublisherWrapper(ProtocolTypeConstants.DME2, servers, MY_TOPIC, MY_USERNAME, MY_PASSWD, true) {};
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapPublisherWrapper_InvalidTopic() {
        new DmaapPublisherWrapper(ProtocolTypeConstants.DME2, servers, "", MY_USERNAME, MY_PASSWD, true) {};
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapPublisherWrapper_Aaf_NullServers() {
        new DmaapAafPublisherWrapper(null, MY_TOPIC, MY_USERNAME, MY_PASSWD, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapPublisherWrapper_Aaf_NoServers() {
        new DmaapAafPublisherWrapper(Collections.emptyList(), MY_TOPIC, MY_USERNAME, MY_PASSWD, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapPublisherWrapper_InvalidProtocol() {
        new DmaapPublisherWrapper(ProtocolTypeConstants.HTTPNOAUTH, servers, MY_TOPIC, MY_USERNAME, MY_PASSWD, true) {};
    }

    @Test
    public void testDmaapPublisherWrapperClose() throws Exception {
        MRSimplerBatchPublisher pub = mock(MRSimplerBatchPublisher.class);
        DmaapPublisherWrapper dmaap = new DmaapAafPublisherWrapper(servers, MY_TOPIC, MY_USERNAME, MY_PASSWD, true);
        dmaap.publisher = pub;

        dmaap.close();
        verify(pub).close(anyLong(), any(TimeUnit.class));

        // close, but with exception from publisher
        doThrow(new IOException(EXPECTED)).when(pub).close(anyLong(), any(TimeUnit.class));
        dmaap.close();
    }

    @Test
    public void testDmaapPublisherWrapperSend() throws Exception {
        MRSimplerBatchPublisher pub = mock(MRSimplerBatchPublisher.class);
        DmaapPublisherWrapper dmaap = new DmaapAafPublisherWrapper(servers, MY_TOPIC, MY_USERNAME, MY_PASSWD, true);
        dmaap.publisher = pub;

        // null response
        assertTrue(dmaap.send(MY_PARTITION, MY_MESSAGE));
        verify(pub).setPubResponse(any(MRPublisherResponse.class));
        verify(pub).send(MY_PARTITION, MY_MESSAGE);

        // with response
        pub = mock(MRSimplerBatchPublisher.class);
        dmaap.publisher = pub;

        MRPublisherResponse resp = new MRPublisherResponse();
        when(pub.sendBatchWithResponse()).thenReturn(resp);
        assertTrue(dmaap.send(MY_PARTITION, MY_MESSAGE));
        verify(pub).setPubResponse(any(MRPublisherResponse.class));
        verify(pub).send(MY_PARTITION, MY_MESSAGE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapPublisherWrapperSend_NullMessage() throws Exception {
        MRSimplerBatchPublisher pub = mock(MRSimplerBatchPublisher.class);
        DmaapPublisherWrapper dmaap = new DmaapAafPublisherWrapper(servers, MY_TOPIC, MY_USERNAME, MY_PASSWD, true);
        dmaap.publisher = pub;

        dmaap.send(MY_PARTITION, null);
    }

    @Test
    public void testDmaapDmePublisherWrapper() {
        // verify with different parameters
        new DmaapDmePublisherWrapper(makeBuilder().build());
        new DmaapDmePublisherWrapper(makeBuilder().additionalProps(null).build());

        addProps.put(ROUTE_PROP, MY_ROUTE);
        new DmaapDmePublisherWrapper(makeBuilder().build());
        new DmaapDmePublisherWrapper(makeBuilder().partner(null).build());

        addProps.put("null-value", null);
        new DmaapDmePublisherWrapper(makeBuilder().build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmePublisherWrapper_InvalidEnv() {
        new DmaapDmePublisherWrapper(makeBuilder().environment(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmePublisherWrapper_InvalidAft() {
        new DmaapDmePublisherWrapper(makeBuilder().aftEnvironment(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmePublisherWrapper_InvalidLat() {
        new DmaapDmePublisherWrapper(makeBuilder().latitude(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmePublisherWrapper_InvalidLong() {
        new DmaapDmePublisherWrapper(makeBuilder().longitude(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmePublisherWrapper_InvalidPartner() {
        new DmaapDmePublisherWrapper(makeBuilder().partner(null).build());
    }
}
