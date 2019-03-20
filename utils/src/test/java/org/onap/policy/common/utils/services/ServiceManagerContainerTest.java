/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.capabilities.Startable;
import org.onap.policy.common.utils.services.ServiceManager.RunnableWithEx;

public class ServiceManagerContainerTest {
    private static final String MY_NAME = "my-name";
    private static final String MY_ACTION = "my-action";
    private static final String MY_OBJECT = "my-object";
    private RunnableWithEx starter;
    private RunnableWithEx stopper;
    private Startable startObj;
    private MyCont cont;

    /**
     * Set up.
     */
    @Before
    public void setUp() {
        starter = mock(RunnableWithEx.class);
        stopper = mock(RunnableWithEx.class);
        startObj = mock(Startable.class);

        cont = new MyCont(MY_NAME);
    }

    @Test
    public void testServiceManagerContainer() throws Exception {
        // use no-arg constructor
        cont = new MyCont();
        assertEquals("service manager", cont.getName());

        cont.start();
        verify(starter).run();
    }

    @Test
    public void test() throws Exception {
        assertEquals(MY_NAME, cont.getName());

        assertFalse(cont.isAlive());

        cont.start();
        assertTrue(cont.isAlive());
        verify(starter).run();
        verify(startObj).start();
        verify(stopper, never()).run();
        verify(startObj, never()).stop();
        verify(startObj, never()).shutdown();

        cont.stop();
        assertFalse(cont.isAlive());
        verify(starter).run();
        verify(startObj).start();
        verify(stopper).run();
        verify(startObj).stop();
        verify(startObj, never()).shutdown();
    }

    @Test
    public void testShutdown() throws Exception {
        cont.start();
        cont.shutdown();
        assertFalse(cont.isAlive());
        verify(stopper).run();
        verify(startObj).stop();
        verify(startObj, never()).shutdown();
    }

    private class MyCont extends ServiceManagerContainer {

        public MyCont() {
            addServices();
        }

        public MyCont(String name) {
            super(name);
            addServices();
        }

        private void addServices() {
            addAction(MY_ACTION, starter, stopper);
            addService(MY_OBJECT, startObj);
        }
    }
}
