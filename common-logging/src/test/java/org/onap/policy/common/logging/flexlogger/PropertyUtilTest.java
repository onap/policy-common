/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
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

package org.onap.policy.common.logging.flexlogger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.logging.flexlogger.PropertyUtil.Listener;

public class PropertyUtilTest {

    private static final File FILE = new File("target/test.properties");
    private TestListener testListener = new TestListener();

    /**
     * Perform test case set up.
     */
    @Before
    public void setUp() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(FILE);
        Properties properties = new Properties();
        properties.put("testProperty", "testValue");
        properties.store(fileOutputStream, "");
        fileOutputStream.close();
    }

    @After
    public void tearDown() throws IOException {
        PropertyUtil.stopListening(FILE, testListener);
        FILE.delete();
    }

    @Test
    public void testGetProperties() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(FILE);
        Properties properties = new Properties();
        properties.put("testProperty", "testValue");
        properties.store(fileOutputStream, "");
        fileOutputStream.close();

        Properties readProperties = PropertyUtil.getProperties(FILE, testListener);
        assertEquals("testValue", readProperties.getProperty("testProperty"));
    }

    @Test
    public void testPropertiesChanged() throws IOException, InterruptedException {
        PropertyUtil.getProperties(FILE, testListener);

        FileOutputStream fileOutputStream = new FileOutputStream(FILE);
        Properties newProperties = new Properties();
        newProperties.put("testProperty", "testValueNew");
        newProperties.store(fileOutputStream, "");

        assertTrue(testListener.isPropertiesChangedInvoked());

    }

    @Test
    public void testStopListening() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(FILE);
        Properties properties = new Properties();
        properties.put("testProperty", "testValue");
        properties.store(fileOutputStream, "");

        Properties readProperties = PropertyUtil.getProperties(FILE, testListener);
        assertEquals("testValue", readProperties.getProperty("testProperty"));

        PropertyUtil.stopListening(FILE, testListener);

        properties.put("testProperty", "testValueNew");
        properties.store(fileOutputStream, "");
        fileOutputStream.close();
        readProperties = PropertyUtil.getProperties(FILE, testListener);
        // If stopListening did not remove the listener, the properties file will not be re-read
        // until poll expires and
        // hence "testValue" will be returned here instead of "testNewValue"
        assertEquals("testValueNew", readProperties.getProperty("testProperty"));
    }

    private class TestListener implements Listener {

        boolean propertiesChangedInvoked = false;

        @Override
        public void propertiesChanged(Properties properties, Set<String> changedKeys) {
            propertiesChangedInvoked = true;
        }

        public boolean isPropertiesChangedInvoked() throws InterruptedException {
            for (int i = 0; i < 20; i++) {
                if (propertiesChangedInvoked) {
                    return true;
                }
                Thread.sleep(1000);
            }
            return false;
        }
    }

}
