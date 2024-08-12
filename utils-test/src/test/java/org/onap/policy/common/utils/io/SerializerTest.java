/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
 * Modificaitons Copyright (C) 2023-2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.io.Serializer.Factory;
import org.springframework.test.util.ReflectionTestUtils;

class SerializerTest {
    private static final String FACTORY = "factory";

    /**
     * Saved and restored when tests complete. Also restored at the start of each test.
     */
    private static Factory saveFactory;

    @BeforeAll
    public static void setUpBeforeClass() {
        saveFactory = (Factory) ReflectionTestUtils.getField(Serializer.class, FACTORY);
    }

    @AfterAll
    public static void tearDownAfterClass() {
        ReflectionTestUtils.setField(Serializer.class, FACTORY, saveFactory);
    }

    @BeforeEach
    public void setUp() {
        setFactory(saveFactory);
    }

    @Test
    void testFactory() {
        assertNotNull(saveFactory);
    }

    @Test
    void testSerialize() throws Exception {
        MyObject obj1 = new MyObject(3);
        byte[] data = Serializer.serialize(obj1);
        assertTrue(data.length > 0);

        byte[] data2 = Serializer.serialize(obj1);
        assertEquals(Arrays.toString(data), Arrays.toString(data2));

        MyObject obj2 = Serializer.roundTrip(obj1);
        assertEquals(obj1.value, obj2.value);
    }

    @Test
    void testSerialize_Ex() {
        assertThatThrownBy(() -> Serializer.serialize(new NotSerializable()))
            .isInstanceOf(java.io.NotSerializableException.class);
    }

    @Test
    void testSerialize_ArrayCloseEx() {
        IOException ex = new IOException("testSerialize_ArrayCloseEx");

        /*
         * This stream will throw an exception when close() is invoked. However, close()
         * is called twice, once by the ObjectOutputStream and once by the code we want to
         * test. As a result, we'll have the first close() succeed and the second one
         * fail.
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream() {
            private int nclose = 0;

            @Override
            public void close() throws IOException {
                if (++nclose > 1) {
                    throw ex;
                }
            }
        };

        /*
         * Use a factory that returns our special stream.
         */
        setFactory(new Factory() {
            @Override
            public ByteArrayOutputStream makeByteArrayOutputStream() {
                return out;
            }
        });

        assertThatThrownBy(() -> Serializer.serialize(new MyObject(100))).isEqualTo(ex);
    }

    @Test
    void testSerialize_ObjectWriteEx() {
        IOException ex = new IOException("testSerialize_ObjectWriteEx");

        /*
         * Use a factory that throws an exception when writeObject() is invoked.
         */
        setFactory(new Factory() {
            @Override
            public void writeObject(Object object, ObjectOutputStream oos) throws IOException {
                throw ex;
            }
        });

        assertThatThrownBy(() -> Serializer.serialize(new MyObject(110))).isEqualTo(ex);
    }

    @Test
    void testSerialize_ObjectCloseEx() throws Exception {
        IOException ex = new IOException("testSerialize_ObjectCloseEx");
        ObjectOutputStream oos = mock(ObjectOutputStream.class);
        doThrow(ex).when(oos).close();

        /*
         * Use a factory that returns the mocked object which throws an exception when
         * close() is invoked. However, we also have to override writeObject() so that it
         * succeeds even through we're using a mock.
         */
        setFactory(new Factory() {
            @Override
            public ObjectOutputStream makeObjectOutputStream(ByteArrayOutputStream out) throws IOException {
                return oos;
            }

            @Override
            public void writeObject(Object object, ObjectOutputStream oos) throws IOException {
                // do nothing
            }
        });

        assertThatThrownBy(() -> Serializer.serialize(new MyObject(120))).isEqualTo(ex);
    }

    @Test
    void testSerialize_BothCloseEx() throws Exception {
        IOException ex = new IOException("testSerialize_BothCloseEx");
        IOException ex2 = new IOException("testSerialize_BothCloseEx_2");
        ObjectOutputStream oos = mock(ObjectOutputStream.class);
        doThrow(ex2).when(oos).close();

        /*
         * This stream will throw an exception when close() is invoked. However, close()
         * is called twice, once by the ObjectOutputStream and once by the code we want to
         * test. As a result, we'll have the first close() succeed and the second one
         * fail.
         */
        ByteArrayOutputStream out = new ByteArrayOutputStream() {
            private int nclose = 0;

            @Override
            public void close() throws IOException {
                if (++nclose > 1) {
                    throw ex;
                }
            }
        };

        /*
         * Use a factory that returns our special stream.
         */
        setFactory(new Factory() {
            @Override
            public ByteArrayOutputStream makeByteArrayOutputStream() {
                return out;
            }

            @Override
            public ObjectOutputStream makeObjectOutputStream(ByteArrayOutputStream out) throws IOException {
                return oos;
            }

            @Override
            public void writeObject(Object object, ObjectOutputStream oos) throws IOException {
                // do nothing
            }
        });

        assertThatThrownBy(() -> Serializer.roundTrip(new MyObject(130))).isEqualTo(ex2);

    }

    @Test
    void testDeserialize() throws Exception {
        MyObject obj1 = new MyObject(3);
        MyObject obj2 = Serializer.roundTrip(obj1);
        assertEquals(obj1.value, obj2.value);
    }

    @Test
    void testDeserialize_ArrayCloseEx() {
        IOException ex = new IOException("testSerialize_ObjectWriteEx");

        /*
         * Use a factory that returns a stream that will throw an exception when close()
         * is invoked. However, close() is called twice, once by the ObjectInputStream and
         * once by the code we want to test. As a result, we'll have the first close()
         * succeed and the second one fail.
         */
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

        assertThatThrownBy(() -> Serializer.roundTrip(new MyObject(300))).isEqualTo(ex);
    }

    @Test
    void testDeserialize_ObjectReadEx() {
        IOException ex = new IOException("testDeserialize_ObjectReadEx");

        /*
         * Use a factory that throws an IOException when readObject() is invoked.
         */
        setFactory(new Factory() {
            @Override
            public Object readObject(ObjectInputStream ois) throws IOException {
                throw ex;
            }
        });

        assertThatThrownBy(() -> Serializer.roundTrip(new MyObject(310))).isEqualTo(ex);
    }

    @Test
    void testDeserialize_ObjectRead_ClassEx() throws Exception {
        MyObject obj1 = new MyObject(200);

        // must use binary character set
        Charset binary = StandardCharsets.ISO_8859_1;

        // serialize the object
        String text = new String(Serializer.serialize(obj1), binary);

        /*
         * Replace the class name with a bogus class name, which should cause
         * ClassNotFoundException when we attempt to deserialize it.
         */
        text = text.replace("MyObject", "AnObject");

        byte[] data2 = text.getBytes(binary);

        /*
         * Use a factory that returns a byte array for "data2" instead of the real "data".
         */
        setFactory(new Factory() {
            @Override
            public ByteArrayInputStream makeByteArrayInputStream(byte[] data) {
                // read from "data2" instead of "data"
                return super.makeByteArrayInputStream(data2);
            }
        });

        assertThatThrownBy(() -> Serializer.roundTrip(obj1)).isInstanceOf(IOException.class)
                        .hasCauseInstanceOf(ClassNotFoundException.class);
    }

    @Test
    void testDeserialize_ObjectCloseEx() {
        IOException ex = new IOException("testDeserialize_ObjectCloseEx");

        /*
         * Use a factory that returns an ObjectInputStream that throws an exception when
         * close() is invoked.
         */
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

        assertThatThrownBy(() -> Serializer.roundTrip(new MyObject(320))).isEqualTo(ex);
    }

    @Test
    void testDeserialize_BothCloseEx() {
        IOException ex = new IOException("testDeserialize_BothCloseEx");
        IOException ex2 = new IOException("testDeserialize_BothCloseEx_2");

        /*
         * Use a factory that returns input streams, both of which throw exceptions when
         * close() is invoked.
         */
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

        assertThatThrownBy(() -> Serializer.roundTrip(new MyObject(330))).isEqualTo(ex2);
    }

    @Test
    void testRoundTrip() throws Exception {
        MyObject obj1 = new MyObject(4);

        MyObject obj2 = Serializer.roundTrip(obj1);
        assertEquals(obj1.value, obj2.value);
    }

    @Test
    void testRoundTrip_Ex() {
        assertThatThrownBy(() -> Serializer.roundTrip(new NotSerializable()))
            .isInstanceOf(java.io.NotSerializableException.class);
    }

    /**
     * Sets a new factory in the Serializer.
     *
     * @param factory new factory to be set
     */
    private void setFactory(Factory factory) {
        ReflectionTestUtils.setField(Serializer.class, FACTORY, factory);
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
