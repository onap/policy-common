/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2018-2020 Ericsson, AT&T. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.logging.flexlogger.PropertyUtil.Listener;
import org.powermock.reflect.Whitebox;

public class PropertyUtilTest {

    private static final String TIMER_FIELD = "timer";
    private static final File FILE = new File("target/test.properties");
    private static Timer saveTimer;
    
    private TimerTask task;
    private Timer timer;
    private TestListener testListener;
    
    @BeforeAll
    public static void setUpBeforeClass() {
        saveTimer = Whitebox.getInternalState(PropertyUtil.LazyHolder.class, TIMER_FIELD);
        
    }
    
    @AfterAll
    public static void tearDownAfterClass() {
        Whitebox.setInternalState(PropertyUtil.LazyHolder.class, TIMER_FIELD, saveTimer);
        
    }

    /**
     * Perform test case set up.
     */
    @BeforeEach
    public void setUp() throws IOException {
        task = null;
        timer = mock(Timer.class);
        Whitebox.setInternalState(PropertyUtil.LazyHolder.class, TIMER_FIELD, timer);
        
        doAnswer(args -> {
            task = args.getArgument(0, TimerTask.class);
            return null;
        }).when(timer).schedule(any(TimerTask.class), anyLong(), anyLong());
        
        testListener = new TestListener();
        
        FileOutputStream fileOutputStream = new FileOutputStream(FILE);
        Properties properties = new Properties();
        properties.put("testProperty", "testValue");
        properties.store(fileOutputStream, "");
        fileOutputStream.close();
    }

    @AfterEach
    public void tearDown() throws IOException {
        PropertyUtil.stopListening(FILE, testListener);
        FILE.delete();
    }
    
    @Test
    public void testTimer() {
        assertNotNull(saveTimer);
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

        // fire task and verify that it notifies the listener
        task.run();
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

    /**
     * The {@link #propertiesChanged(Properties, Set)} method is invoked via a background
     * thread, thus we have to use a latch to wait for it to be invoked.
     */
    private class TestListener implements Listener {

        private CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void propertiesChanged(Properties properties, Set<String> changedKeys) {
            latch.countDown();
        }

        public boolean isPropertiesChangedInvoked() throws InterruptedException {
            return latch.await(5, TimeUnit.SECONDS);
        }
    }

}
