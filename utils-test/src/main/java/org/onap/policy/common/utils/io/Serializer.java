/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Engine - Common Modules
 * ================================================================================
 * Copyright (C) 2018-2021 AT&T Intellectual Property. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utilities for testing serialization and de-serialization of objects.
 */
public class Serializer {

    /**
     * Factory to access various objects. May be overridden for junit tests.
     */
    private static Factory factory = new Factory();

    /**
     * The constructor.
     */
    private Serializer() {

    }

    /**
     * Serializes an object into a byte array.
     *
     * @param object the object to be serialized
     * @return the byte array containing the serialized object
     * @throws IOException if an error occurs
     */
    public static <T> byte[] serialize(T object) throws IOException {
        try (var out = factory.makeByteArrayOutputStream()) {
            try (var oos = factory.makeObjectOutputStream(out)) {
                /*
                 * writeObject() is final and mockito can't mock final methods. In
                 * addition, powermock seemed to be having difficulty with the junit test
                 * class as well, so we'll just do it with a factory method.
                 */
                factory.writeObject(object, oos);
            }

            return out.toByteArray();
        }
    }

    /**
     * De-serializes an object from a byte array.
     *
     * @param clazz class of object that is expected to be de-serialized
     * @param data the byte array containing the serialized object
     * @return the object that was de-serialized from the byte array
     * @throws IOException if an error occurs
     */
    private static <T> T deserialize(Class<T> clazz, byte[] data) throws IOException {

        try (var in = factory.makeByteArrayInputStream(data);
                        var ois = factory.makeObjectInputStream(in)) {
            /*
             * readObject() is final and mockito can't mock final methods. In addition,
             * powermock seemed to be having difficulty with the junit test class as well,
             * so we'll just do it with a factory method.
             */
            return clazz.cast(factory.readObject(ois));
        }
    }

    /**
     * Runs an object through a complete round trip, serializing and then de-serializing
     * it.
     *
     * @param object object to be serialized
     * @return the object that was de-serialized
     * @throws IOException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static <T> T roundTrip(T object) throws IOException {
        return (T) deserialize(object.getClass(), serialize(object));
    }

    /**
     * Factory to access various objects.
     */
    public static class Factory {

        public ByteArrayOutputStream makeByteArrayOutputStream() {
            return new ByteArrayOutputStream();
        }

        public ByteArrayInputStream makeByteArrayInputStream(byte[] data) {
            return new ByteArrayInputStream(data);
        }

        public ObjectOutputStream makeObjectOutputStream(ByteArrayOutputStream out) throws IOException {
            return new ObjectOutputStream(out);
        }

        public ObjectInputStream makeObjectInputStream(ByteArrayInputStream in) throws IOException {
            return new ObjectInputStream(in);
        }

        public void writeObject(Object object, ObjectOutputStream oos) throws IOException {
            oos.writeObject(object);
        }

        /**
         * Read the object.
         *
         * @param ois input stream
         * @return the object
         * @throws IOException throws IO exception if cannot read
         */
        public Object readObject(ObjectInputStream ois) throws IOException {
            try {
                /*
                 * This class is only used by junit tests. In addition, it is only used by
                 * deserialize(), which has been made "private", thus disabling sonar.
                 */
                return ois.readObject(); // NOSONAR

            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }

    }
}
