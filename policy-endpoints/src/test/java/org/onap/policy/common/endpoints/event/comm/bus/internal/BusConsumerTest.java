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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.att.aft.dme2.internal.apache.commons.collections.IteratorUtils;
import com.att.nsa.cambria.client.CambriaConsumer;
import com.att.nsa.mr.client.impl.MRConsumerImpl;
import com.att.nsa.mr.client.response.MRConsumerResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer.CambriaConsumerWrapper;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer.DmaapAafConsumerWrapper;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer.DmaapConsumerWrapper;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusConsumer.DmaapDmeConsumerWrapper;
import org.powermock.reflect.Whitebox;

public class BusConsumerTest extends TopicTestBase {

    @Before
    public void setUp() {
        super.setUp();
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
        new CambriaConsumerWrapper(makeBuilder().userName(null).password(null).build());
    }

    @Test
    public void testCambriaConsumerWrapperFetch() throws Exception {
        CambriaConsumer inner = mock(CambriaConsumer.class);
        List<String> lst = Arrays.asList(MY_MESSAGE, MY_MESSAGE2);
        when(inner.fetch()).thenReturn(lst);

        CambriaConsumerWrapper cons = new CambriaConsumerWrapper(builder.build());
        Whitebox.setInternalState(cons, "consumer", inner);

        assertEquals(lst, IteratorUtils.toList(cons.fetch().iterator()));

        // arrange to throw exception next time fetch is called
        IOException ex = new IOException(EXPECTED);
        when(inner.fetch()).thenThrow(ex);

        cons.fetchTimeout = 10;

        try {
            cons.fetch();
            fail("missing exception");

        } catch (IOException | InterruptedException e) {
            assertEquals(ex, e);
        }
    }

    @Test
    public void testCambriaConsumerWrapperClose() throws Exception {
        CambriaConsumerWrapper cons = new CambriaConsumerWrapper(builder.build());

        // set filter several times to cause different branches of close() to be executed
        for (int count = 0; count < 3; ++count) {
            cons.close();
            cons.setFilter("close=" + count);
        }
    }

    @Test
    public void testCambriaConsumerWrapperSetFilter() {
        // set filter several times to cause different branches to be executed
        CambriaConsumerWrapper cons = new CambriaConsumerWrapper(builder.build());
        for (int count = 0; count < 3; ++count) {
            cons.setFilter("set-filter=" + count);
        }
    }

    @Test
    public void testCambriaConsumerWrapperToString() {
        assertNotNull(new CambriaConsumerWrapper(makeBuilder().build()).toString());
    }

    @Test
    public void testDmaapConsumerWrapper() throws Exception {
        // verify that different wrappers can be built
        new DmaapAafConsumerWrapper(makeBuilder().build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapConsumerWrapper_InvalidTopic() throws Exception {
        new DmaapAafConsumerWrapper(makeBuilder().topic(null).build());
    }

    @Test
    public void testDmaapConsumerWrapperFetch() throws Exception {
        DmaapAafConsumerWrapper dmaap = new DmaapAafConsumerWrapper(makeBuilder().build());
        MRConsumerImpl cons = mock(MRConsumerImpl.class);

        dmaap.fetchTimeout = 5;
        dmaap.consumer = cons;

        // null return
        when(cons.fetchWithReturnConsumerResponse()).thenReturn(null);
        assertFalse(dmaap.fetch().iterator().hasNext());

        // with messages, 200
        List<String> lst = Arrays.asList(MY_MESSAGE, MY_MESSAGE2);
        MRConsumerResponse resp = new MRConsumerResponse();
        resp.setResponseCode("200");
        resp.setActualMessages(lst);
        when(cons.fetchWithReturnConsumerResponse()).thenReturn(resp);

        assertEquals(lst, IteratorUtils.toList(dmaap.fetch().iterator()));

        // null messages
        resp.setActualMessages(null);
        when(cons.fetchWithReturnConsumerResponse()).thenReturn(resp);

        assertFalse(dmaap.fetch().iterator().hasNext());

        // with messages, NOT 200
        resp.setResponseCode("400");
        resp.setActualMessages(lst);
        when(cons.fetchWithReturnConsumerResponse()).thenReturn(resp);

        assertEquals(lst, IteratorUtils.toList(dmaap.fetch().iterator()));
    }

    @Test
    public void testDmaapConsumerWrapperClose() throws Exception {
        new DmaapAafConsumerWrapper(makeBuilder().build()).close();
    }

    @Test
    public void testDmaapConsumerWrapperToString() throws Exception {
        assertNotNull(new DmaapConsumerWrapper(makeBuilder().build()) {}.toString());
    }

    @Test
    public void testDmaapAafConsumerWrapper() throws Exception {
        // verify that different wrappers can be built
        new DmaapAafConsumerWrapper(makeBuilder().useHttps(true).build());
        new DmaapAafConsumerWrapper(makeBuilder().useHttps(false).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapAafConsumerWrapper_InvalidServers() throws Exception {
        /*
         * Unfortunately, the MR code intercepts this and throws an exception before the
         * wrapper gets a chance to check it, thus this test does not improve the coverage
         * for the constructor.
         */
        new DmaapAafConsumerWrapper(makeBuilder().servers(Collections.emptyList()).build());
    }

    @Test
    public void testDmaapAafConsumerWrapperToString() throws Exception {
        assertNotNull(new DmaapAafConsumerWrapper(makeBuilder().build()).toString());
    }

    @Test
    public void testDmaapDmeConsumerWrapper() throws Exception {
        // verify that different wrappers can be built
        new DmaapDmeConsumerWrapper(makeBuilder().build());
        new DmaapDmeConsumerWrapper(makeBuilder().useHttps(true).build());
        new DmaapDmeConsumerWrapper(makeBuilder().useHttps(false).build());
        new DmaapDmeConsumerWrapper(makeBuilder().additionalProps(null).build());

        addProps.put(ROUTE_PROP, MY_ROUTE);
        new DmaapDmeConsumerWrapper(makeBuilder().build());
        new DmaapDmeConsumerWrapper(makeBuilder().partner(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmeConsumerWrapper_InvalidEnvironment() throws Exception {
        new DmaapDmeConsumerWrapper(makeBuilder().environment(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmeConsumerWrapper_InvalidAft() throws Exception {
        new DmaapDmeConsumerWrapper(makeBuilder().aftEnvironment(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmeConsumerWrapper_InvalidLat() throws Exception {
        new DmaapDmeConsumerWrapper(makeBuilder().latitude(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmeConsumerWrapper_InvalidLong() throws Exception {
        new DmaapDmeConsumerWrapper(makeBuilder().longitude(null).build());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDmaapDmeConsumerWrapper_InvalidPartner() throws Exception {
        new DmaapDmeConsumerWrapper(makeBuilder().partner(null).build());
    }
}
