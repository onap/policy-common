/*-
 * ============LICENSE_START=======================================================
 * site-manager
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.policy.common.sitemanager.utils;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.onap.policy.common.im.jmx.ComponentAdminMBean;
import org.onap.policy.common.im.jpa.ResourceRegistrationEntity;

public class JmxOpProcessor {

    private JmxOpProcessor() {
        super();
    }

    /**
     * Process a 'lock' or 'unlock' operation on a single 'ResourceRegistrationEntity'
     *
     * @param arg0 this is the string "lock" or "unlock"
     * @param resourceRegistrationEntity this is the ResourceRegistrationEntity to lock or unlock
     */
    public static void jmxOp(final String arg0, final ResourceRegistrationEntity resourceRegistrationEntity,
            final Printable printable) {
        final String resourceName = resourceRegistrationEntity.getResourceName();
        final String jmxUrl = resourceRegistrationEntity.getResourceUrl();
        if (jmxUrl == null) {
            printable.println(arg0 + ": no resource URL for '" + resourceName + "'");
            return;
        }

        try (final JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(jmxUrl))) {
            final ComponentAdminMBean admin = JMX.newMXBeanProxy(connector.getMBeanServerConnection(),
                    new ObjectName("ONAP_POLICY_COMP:name=" + resourceName), ComponentAdminMBean.class);

            if ("lock".equals(arg0)) {
                admin.lock();
            } else {
                admin.unlock();
            }
        } catch (final Exception exception) {
            printable.println(arg0 + " failed for '" + resourceName + "': " + exception);
        }
    }

}
