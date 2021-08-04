/*-
 * ============LICENSE_START=======================================================
 * ONAP-Logging
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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
import org.powermock.reflect.Whitebox;

public class FlexLoggerTest {

    @Test
    public void testGetLoggerClassOfQEelf() {
        Whitebox.setInternalState(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger((Class<?>) null);
        assertSame(logger, FlexLogger.getLogger((Class<?>) null));
        assertNotEquals(logger, FlexLogger.getLogger(String.class));
    }

    @Test
    public void testGetLoggerClassOfQSystemOut() {
        Whitebox.setInternalState(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger(this.getClass());
        assertSame(logger, FlexLogger.getLogger(this.getClass()));
    }

    @Test
    public void testGetLoggerStringEelf() {
        Whitebox.setInternalState(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger();
        assertSame(logger, FlexLogger.getLogger());
    }

    @Test
    public void testGetLoggerStringSystemOut() {
        Whitebox.setInternalState(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger();
        assertSame(logger, FlexLogger.getLogger());
    }

    @Test
    public void testGetLoggerClassOfQBooleanEelf() {
        Whitebox.setInternalState(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger(this.getClass(), true);
        assertSame(logger, FlexLogger.getLogger(this.getClass(), true));
    }

    @Test
    public void testGetLoggerClassOfQBooleanSystemOut() {
        Whitebox.setInternalState(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger(this.getClass(), true);
        assertSame(logger, FlexLogger.getLogger(this.getClass(), true));
    }

    @Test
    public void testGetLoggerStringBooleanEelf() {
        Whitebox.setInternalState(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger(true);
        assertSame(logger, FlexLogger.getLogger(true));
    }

    @Test
    public void testGetLoggerStringBooleanSystemOut() {
        Whitebox.setInternalState(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger(true);
        assertSame(logger, FlexLogger.getLogger(true));
    }

    @Test
    public void testGetClassName() {
        assertNotEquals("FlexLogger", new FlexLogger().getClassName());
    }

    @Test
    public void testPropertiesCallBack() throws IOException {
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
