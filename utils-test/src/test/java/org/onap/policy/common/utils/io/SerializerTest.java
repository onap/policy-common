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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.utils.io.Serializer.Factory;
import org.powermock.reflect.Whitebox;

public class SerializerTest {

    /**
     * Saved and restored when tests complete. Also restored at the start of each test.
     */
    private static Factory saveFactory;

    @BeforeClass
    public static void setUpBeforeClass() {
        saveFactory = Whitebox.getInternalState(Serializer.class, "factory");
    }

    @AfterClass
    public static void tearDownAfterClass() {
        Whitebox.setInternalState(Serializer.class, "factory", saveFactory);
    }

    @Before
    public void setUp() {
        setFactory(saveFactory);
    }

    @Test
    public void testFactory() {
        assertNotNull(saveFactory);
    }

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
    public void testSerialize_ArrayCloseEx() throws Exception {
        IOException ex = new IOException("testSerialize_ArrayCloseEx");

        ByteArrayOutputStream out = new ByteArrayOutputStream() {
            private int nclose = 0;

            @Override
            public void close() throws IOException {
                if (++nclose > 1) {
                    throw ex;
                }
            }
        };

        setFactory(new Factory() {
            @Override
            public ByteArrayOutputStream makeByteArrayOutputStream() {
                return out;
            }
        });

        assertEquals(ex, expectException(() -> Serializer.serialize(new MyObject(100))));
    }

    @Test
    public void testSerialize_ObjectWriteEx() throws Exception {
        IOException ex = new IOException("testSerialize_ObjectWriteEx");

        setFactory(new Factory() {
            @Override
            public void writeObject(Object object, ObjectOutputStream oos) throws IOException {
                throw ex;
            }
        });

        assertEquals(ex, expectException(() -> Serializer.serialize(new MyObject(110))));
    }

    @Test
    public void testSerialize_ObjectCloseEx() throws Exception {
        IOException ex = new IOException("testSerialize_ObjectCloseEx");
        ObjectOutputStream oos = mock(ObjectOutputStream.class);
        doThrow(ex).when(oos).close();

        setFactory(new Factory() {
            @Override
            public ObjectOutputStream makeObjectOutputStream(ByteArrayOutputStream out) throws IOException {
                return oos;
            }

            @Override
            public void writeObject(Object object, ObjectOutputStream oos) throws IOException {
                return;
            }
        });

        assertEquals(ex, expectException(() -> Serializer.serialize(new MyObject(120))));
    }

    @Test
    public void testDeserialize() throws Exception {
        MyObject obj1 = new MyObject(3);
        byte[] data = Serializer.serialize(obj1);

        MyObject obj2 = Serializer.deserialize(MyObject.class, data);
        assertEquals(obj1.value, obj2.value);
    }

    @Test
    public void testDeserialize_ArrayCloseEx() throws Exception {
        IOException ex = new IOException("testSerialize_ObjectWriteEx");

        setFactory(new Factory() {
            @Override
            public ByteArrayInputStream makeByteArrayInputStream(byte[] data) {
                return new ByteArrayInputStream(data) {
                    private int nclose = 0;

                    @Override
                    public void close() throws IOException {
                        if (++nclose > 1) {
                            throw ex;
                        }
                    }
                };
            }
        });

        byte[] data = Serializer.serialize(new MyObject(300));
        assertEquals(ex, expectException(() -> Serializer.deserialize(MyObject.class, data)));
    }

    @Test
    public void testDeserialize_ObjectReadClassEx() throws Exception {
        ClassNotFoundException ex = new ClassNotFoundException("testDeserialize_ObjectReadClassEx");

        setFactory(new Factory() {
            @Override
            public Object readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
                throw ex;
            }
        });

        byte[] data = Serializer.serialize(new MyObject(305));

        Exception exwrap = expectException(() -> Serializer.deserialize(MyObject.class, data));
        assertTrue(exwrap instanceof IOException);
        assertNotNull(exwrap.getCause());
        assertEquals(ex, exwrap.getCause());
    }

    @Test
    public void testDeserialize_ObjectReadEx() throws Exception {
        IOException ex = new IOException("testDeserialize_ObjectReadEx");

        setFactory(new Factory() {
            @Override
            public Object readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
                throw ex;
            }
        });

        byte[] data = Serializer.serialize(new MyObject(310));
        assertEquals(ex, expectException(() -> Serializer.deserialize(MyObject.class, data)));
    }

    @Test
    public void testDeserialize_ObjectCloseEx() throws Exception {
        IOException ex = new IOException("testDeserialize_ObjectCloseEx");

        setFactory(new Factory() {
            @Override
            public ObjectInputStream makeObjectInputStream(ByteArrayInputStream in) throws IOException {
                return new ObjectInputStream(in) {
                    @Override
                    public void close() throws IOException {
                        throw ex;
                    }
                };
            }
        });

        byte[] data = Serializer.serialize(new MyObject(320));
        assertEquals(ex, expectException(() -> Serializer.deserialize(MyObject.class, data)));
    }

    @Test
    public void testDeserialize_BothCloseEx() throws Exception {
        IOException ex = new IOException("testDeserialize_BothCloseEx");
        IOException ex2 = new IOException("testDeserialize_BothCloseEx_2");

        setFactory(new Factory() {
            @Override
            public ByteArrayInputStream makeByteArrayInputStream(byte[] data) {
                return new ByteArrayInputStream(data) {
                    @Override
                    public void close() throws IOException {
                        throw ex;
                    }
                };
            }

            @Override
            public ObjectInputStream makeObjectInputStream(ByteArrayInputStream in) throws IOException {
                return new ObjectInputStream(in) {
                    @Override
                    public void close() throws IOException {
                        throw ex2;
                    }
                };
            }
        });

        byte[] data = Serializer.serialize(new MyObject(330));
        assertEquals(ex2, expectException(() -> Serializer.deserialize(MyObject.class, data)));
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
     * Sets a new factory in the Serializer.
     *
     * @param factory new factory to be set
     */
    private void setFactory(Factory factory) {
        Whitebox.setInternalState(Serializer.class, "factory", factory);
    }

    /**
     * Applies a function, which is expected to throw an exception.
     *
     * @param func the function to apply
     * @return the exception thrown by the function, or {@code null} if it did not throw
     *         an exception
     */
    private Exception expectException(RunnerWithEx func) {
        try {
            func.apply();
            return null;

        } catch (Exception ex) {
            return ex;
        }
    }

    @FunctionalInterface
    private static interface RunnerWithEx {
        public void apply() throws Exception;
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
