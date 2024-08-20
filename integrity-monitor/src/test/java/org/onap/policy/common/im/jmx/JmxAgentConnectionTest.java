/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2024 Nordix Foundation.
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

package org.onap.policy.common.im.jmx;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import javax.management.remote.JMXConnector;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.onap.policy.common.im.IntegrityMonitorException;

class JmxAgentConnectionTest {

    @Mock
    private JMXConnector connector;

    private JmxAgentConnection jmxAgentConnection;
    private final String urlBad = "http://test-url.net";
    private final String urlGood = "service:jmx:rmi:///jndi/rmi://localhost:9000";

    @Test
    void testConstructors() {
        jmxAgentConnection = new JmxAgentConnection();
        assertNotNull(jmxAgentConnection);

        jmxAgentConnection = new JmxAgentConnection(urlBad);
        assertNotNull(jmxAgentConnection);
    }

    @Test
    void testGetConnection() throws IOException {
        connector = mock(JMXConnector.class);
        doNothing().when(connector).connect();

        jmxAgentConnection = new JmxAgentConnection(null);
        assertThatThrownBy(() -> jmxAgentConnection.getMBeanConnection())
            .isInstanceOf(IntegrityMonitorException.class);

        jmxAgentConnection = new JmxAgentConnection("service:jmx:rmi:///jndi/rmi://host.domain:9999/jmxAgent");

        assertThatThrownBy(() -> jmxAgentConnection.getMBeanConnection())
            .isInstanceOf(IntegrityMonitorException.class)
            .hasMessageContaining("Failed to retrieve RMIServer stub");

        assertThatCode(() -> jmxAgentConnection.disconnect())
            .doesNotThrowAnyException();
    }

}
