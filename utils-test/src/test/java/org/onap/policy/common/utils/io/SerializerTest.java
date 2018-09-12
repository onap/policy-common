/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
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

package org.onap.policy.common.utils.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import org.junit.Test;

public class SerializerTest {

    @Test
    public void testSerialize() throws Exception {
        MyObject obj1 = new MyObject(3);
        byte[] data = Serializer.serialize(obj1);
        assertTrue(data.length > 0);

        byte[] data2 = Serializer.serialize(obj1);
        assertEquals(Arrays.toString(data), Arrays.toString(data2));

        MyObject obj2 = Serializer.deserialize(MyObject.class, data);
        assertEquals(obj1.value, obj2.value);
    }

    @Test(expected = java.io.NotSerializableException.class)
    public void testSerialize_Ex() throws Exception {
        Serializer.serialize(new NotSerializable());
    }

    @Test
    public void testDeserialize() throws Exception {
        MyObject obj1 = new MyObject(3);
        byte[] data = Serializer.serialize(obj1);

        MyObject obj2 = Serializer.deserialize(MyObject.class, data);
        assertEquals(obj1.value, obj2.value);
    }

    @Test
    public void testRoundTrip() throws Exception {
        MyObject obj1 = new MyObject(3);

        MyObject obj2 = Serializer.roundTrip(obj1);
        assertEquals(obj1.value, obj2.value);
    }

    @Test(expected = java.io.NotSerializableException.class)
    public void testRoundTrip_Ex() throws Exception {
        Serializer.roundTrip(new NotSerializable());
    }

    /**
     * Simple, serializable object.
     */
    public static class MyObject implements Serializable {
        private static final long serialVersionUID = 1L;

        private int value;

        public MyObject(int val) {
            value = val;
        }
    }

    /**
     * Object that is <i>not</i> serializable.
     */
    public static class NotSerializable {

        public NotSerializable() {
            super();
        }
    }

}
