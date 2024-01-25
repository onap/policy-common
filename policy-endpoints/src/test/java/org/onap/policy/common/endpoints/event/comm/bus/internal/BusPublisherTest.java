/*
 * ============LICENSE_START=======================================================
 * policy-endpoints
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.TopicTestBase;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusPublisher.CambriaPublisherWrapper;


public class BusPublisherTest extends TopicTestBase {

    @Before
    @Override
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
        assertThatCode(() -> new CambriaPublisherWrapper(makeBuilder().userName(null).password(null).build()))
                        .doesNotThrowAnyException();
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
    public void testCambriaPublisherWrapperClose() {
        CambriaBatchingPublisher pub = mock(CambriaBatchingPublisher.class);
        CambriaPublisherWrapper cambria = new CambriaPublisherWrapper(makeBuilder().build());
        cambria.publisher = pub;

        cambria.close();
        verify(pub).close();

        // try again, this time with an exception
        doThrow(new RuntimeException(EXPECTED)).when(pub).close();
        cambria.close();
    }
}
