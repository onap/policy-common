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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.onap.policy.common.logging.util.TestUtils.overrideStaticField;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.onap.policy.common.logging.flexlogger.FlexLogger.PropertiesCallBack;

public class FlexLoggerTest {

    @Test
    public void testGetLoggerClassOfQEelf() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger((Class) null);
        assertSame(logger, FlexLogger.getLogger((Class) null));
        assertNotEquals(logger, FlexLogger.getLogger(String.class));
    }

    @Test
    public void testGetLoggerClassOfQLog4j() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.LOG4J);
        Logger logger = FlexLogger.getLogger(this.getClass());
        assertSame(logger, FlexLogger.getLogger(this.getClass()));
    }

    @Test
    public void testGetLoggerClassOfQSystemOut() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger(this.getClass());
        assertSame(logger, FlexLogger.getLogger(this.getClass()));
    }

    @Test
    public void testGetLoggerStringEelf() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger("str1");
        assertSame(logger, FlexLogger.getLogger("str1"));
    }

    @Test
    public void testGetLoggerStringLog4j() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.LOG4J);
        Logger logger = FlexLogger.getLogger("str1");
        assertSame(logger, FlexLogger.getLogger("str1"));
    }

    @Test
    public void testGetLoggerStringSystemOut() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger("str1");
        assertSame(logger, FlexLogger.getLogger("str1"));
    }

    @Test
    public void testGetLoggerClassOfQBooleanEelf() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger(this.getClass(), true);
        assertSame(logger, FlexLogger.getLogger(this.getClass(), true));
    }

    @Test
    public void testGetLoggerClassOfQBooleanLog4j() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.LOG4J);
        Logger logger = FlexLogger.getLogger(this.getClass(), true);
        assertSame(logger, FlexLogger.getLogger(this.getClass(), true));
    }

    @Test
    public void testGetLoggerClassOfQBooleanSystemOut() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger(this.getClass(), true);
        assertSame(logger, FlexLogger.getLogger(this.getClass(), true));
    }

    @Test
    public void testGetLoggerStringBooleanEelf() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.EELF);
        Logger logger = FlexLogger.getLogger("str1", true);
        assertSame(logger, FlexLogger.getLogger("str1", true));
    }

    @Test
    public void testGetLoggerStringBooleanLog4j() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.LOG4J);
        Logger logger = FlexLogger.getLogger("str1", true);
        assertSame(logger, FlexLogger.getLogger("str1", true));
    }

    @Test
    public void testGetLoggerStringBooleanSystemOut() {
        overrideStaticField(FlexLogger.class, "loggerType", LoggerType.SYSTEMOUT);
        Logger logger = FlexLogger.getLogger("str1", true);
        assertSame(logger, FlexLogger.getLogger("str1", true));
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
        propertiesCallBack.propertiesChanged(PropertyUtil.getProperties("config/policyLogger.properties"), changedKeys);
    }

}
