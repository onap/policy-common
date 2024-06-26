/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.logging.flexlogger.FlexLogger.PropertiesCallBack;
import org.springframework.test.util.ReflectionTestUtils;

class FlexLoggerTest {

    @Test
    void testGetLoggerClassOfQEelf() {
        ReflectionTestUtils.setField(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger((Class<?>) null);
        assertSame(logger, FlexLogger.getLogger((Class<?>) null));
        assertNotEquals(logger, FlexLogger.getLogger(String.class));
    }

    @Test
    void testGetLoggerClassOfQSystemOut() {
        ReflectionTestUtils.setField(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger(this.getClass());
        assertSame(logger, FlexLogger.getLogger(this.getClass()));
    }

    @Test
    void testGetLoggerStringEelf() {
        ReflectionTestUtils.setField(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger();
        assertSame(logger, FlexLogger.getLogger());
    }

    @Test
    void testGetLoggerStringSystemOut() {
        ReflectionTestUtils.setField(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger();
        assertSame(logger, FlexLogger.getLogger());
    }

    @Test
    void testGetLoggerClassOfQBooleanEelf() {
        ReflectionTestUtils.setField(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger(this.getClass(), true);
        assertSame(logger, FlexLogger.getLogger(this.getClass(), true));
    }

    @Test
    void testGetLoggerClassOfQBooleanSystemOut() {
        ReflectionTestUtils.setField(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger(this.getClass(), true);
        assertSame(logger, FlexLogger.getLogger(this.getClass(), true));
    }

    @Test
    void testGetLoggerStringBooleanEelf() {
        ReflectionTestUtils.setField(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger(true);
        assertSame(logger, FlexLogger.getLogger(true));
    }

    @Test
    void testGetLoggerStringBooleanSystemOut() {
        ReflectionTestUtils.setField(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger(true);
        assertSame(logger, FlexLogger.getLogger(true));
    }

    @Test
    void testGetClassName() {
        assertNotEquals("FlexLogger", new FlexLogger().getClassName());
    }

    @Test
    void testPropertiesCallBack() {
        Set<String> changedKeys = new HashSet<>();
        changedKeys.add("debugLogger.level");
        changedKeys.add("metricsLogger.level");
        changedKeys.add("error.level");
        changedKeys.add("audit.level");
        PropertiesCallBack propertiesCallBack = new PropertiesCallBack("name");
        assertThatCode(() -> propertiesCallBack
                        .propertiesChanged(PropertyUtil.getProperties("config/policyLogger.properties"), changedKeys))
                                        .doesNotThrowAnyException();
    }

}
