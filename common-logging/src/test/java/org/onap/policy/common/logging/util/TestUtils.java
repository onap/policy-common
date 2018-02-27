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
package org.onap.policy.common.logging.util;

import static org.junit.Assert.fail;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TestUtils {
    
    public static void overrideField(@SuppressWarnings("rawtypes") final Class clazz, final Object object, final String fieldName, final Object newValue) {
        try {
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            field.set(object, newValue);
        } catch (final Exception e) {
            fail(e.toString());
        }
    }
    
    public static void overrideStaticField(@SuppressWarnings("rawtypes") final Class clazz, final String fieldName, final Object newValue) {
        try {
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, newValue);
        } catch (final Exception e) {
            fail(e.toString());
        }
    }

}
