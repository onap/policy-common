/*
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.im;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
public class StateTransitionTest {
    private static final String HOTSTANDBY = "hotstandby";
    private static final String LOCKED = "locked";
    private static final String PROMOTE = "promote";
    private static final String PROVIDINGSERVICE = "providingservice";
    private static final String UNLOCK = "unlock";
    private static final String UNLOCKED = "unlocked";
    private static final String COLDSTANDBY = "coldstandby";
    private static final String DEMOTE = "demote";
    private static final String DEPENDENCY = "dependency";
    private static final String DEPENDENCY_FAILED = "dependency,failed";
    private static final String DISABLE_DEPENDENCY = "disableDependency";
    private static final String DISABLE_FAILED = "disableFailed";
    private static final String DISABLED = "disabled";
    private static final String ENABLE_NO_DEPENDENCY = "enableNoDependency";
    private static final String ENABLE_NOT_FAILED = "enableNotFailed";
    private static final String ENABLED = "enabled";
    private static final String FAILED = "failed";
    private static Logger logger = LoggerFactory.getLogger(StateTransitionTest.class);

    @Test
    public void test() throws StateTransitionException {
        logger.info("\n\nlogger.infor StateTransitionTest: Entering\n\n");
        logger.info("??? create a new StateTransition");
        StateTransition st = new StateTransition();

        StateElement se = null;

        // bad test case
        se = st.getEndingState(UNLOCKED, ENABLED, "null", COLDSTANDBY, "lock");
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 1");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", "null", "lock");
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 2");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", "null", UNLOCK);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 3");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", "null", DISABLE_FAILED);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 4");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", "null", ENABLE_NOT_FAILED);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 5");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", "null", DISABLE_DEPENDENCY);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 6");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 7");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", "null", PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 8");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", "null", DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 9");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", COLDSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 10");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", COLDSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 11");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 12");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 13");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 14");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 15");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", COLDSTANDBY, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 16");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", COLDSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 17");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", HOTSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 18");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", HOTSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 19");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 20");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 21");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 22");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 23");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", HOTSTANDBY, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 24");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", HOTSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 25");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 26");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, UNLOCK);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 27");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 28");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 29");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 30");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 31");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 32");
        se = st.getEndingState(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 33");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, "null", DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 34");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, "null", UNLOCK);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 35");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, "null", DISABLE_FAILED);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 36");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, "null", ENABLE_NOT_FAILED);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 37");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, "null", DISABLE_DEPENDENCY);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 38");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 39");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, "null", PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 40");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, "null", DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 41");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 42");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 43");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 44");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 45");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 46");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 47");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 48");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 49");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 50");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 51");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 52");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 53");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 54");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 55");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 56");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 57");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 58");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, UNLOCK);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 59");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 60");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 61");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 62");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 63");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 64");
        se = st.getEndingState(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 65");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, "null", DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 66");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, "null", UNLOCK);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 67");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, "null", DISABLE_FAILED);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 68");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, "null", ENABLE_NOT_FAILED);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 69");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, "null", DISABLE_DEPENDENCY);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 70");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 71");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, "null", PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 72");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, "null", DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 73");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 74");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 75");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 76");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 77");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 78");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 79");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 80");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 81");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 82");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 83");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 84");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 85");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("hotstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 86");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 87");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 88");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 89");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 90");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, UNLOCK);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 91");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 92");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 93");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 94");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 95");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 96");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 97");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 98");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", UNLOCK);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 99");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", DISABLE_FAILED);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 100");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", ENABLE_NOT_FAILED);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 101");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", DISABLE_DEPENDENCY);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 102");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 103");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 104");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 105");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 106");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 107");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 108");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 109");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 110");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 111");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 112");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 113");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 114");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 115");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 116");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 117");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 118");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 119");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 120");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 121");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 122");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, UNLOCK);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 123");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 124");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                ENABLE_NOT_FAILED);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 125");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 126");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                ENABLE_NO_DEPENDENCY);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 127");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, PROMOTE);
        assertEquals("providingservice,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 128");
        se = st.getEndingState(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 129");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", "null", DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 130");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", "null", UNLOCK);
        assertEquals("null,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 131");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", "null", DISABLE_FAILED);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 132");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", "null", ENABLE_NOT_FAILED);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 133");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", "null", DISABLE_DEPENDENCY);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 134");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 135");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", "null", PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 136");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", "null", DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 137");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 138");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", COLDSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 139");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 140");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 141");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 142");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 143");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 144");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 145");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 146");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", HOTSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 147");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 148");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 149");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 150");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 151");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 152");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 153");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 154");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 155");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 156");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 157");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 158");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 159");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 160");
        se = st.getEndingState(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 161");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, "null", DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 162");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, "null", UNLOCK);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 163");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, "null", DISABLE_FAILED);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 164");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, "null", ENABLE_NOT_FAILED);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 165");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, "null", DISABLE_DEPENDENCY);
        assertEquals("null,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 166");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 167");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, "null", PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 168");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, "null", DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 169");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 170");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 171");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 172");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 173");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 174");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 175");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 176");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 177");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 178");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 179");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 180");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 181");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 182");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 183");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 184");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 185");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 186");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 187");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 188");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 189");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 190");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 191");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 192");
        se = st.getEndingState(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 193");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, "null", DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 194");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, "null", UNLOCK);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 195");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, "null", DISABLE_FAILED);
        assertEquals("null,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 196");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, "null", ENABLE_NOT_FAILED);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 197");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, "null", DISABLE_DEPENDENCY);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 198");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 199");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, "null", PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 200");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, "null", DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 201");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 202");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 203");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 204");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 205");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 206");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 207");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 208");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 209");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 210");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 211");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 212");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 213");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 214");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 215");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 216");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 217");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 218");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 219");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 220");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 221");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 222");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 223");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 224");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 225");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 226");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", UNLOCK);
        assertEquals("null,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 227");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", DISABLE_FAILED);
        assertEquals("null,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 228");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", ENABLE_NOT_FAILED);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 229");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", DISABLE_DEPENDENCY);
        assertEquals("null,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 230");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 231");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 232");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 233");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 234");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 235");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 236");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 237");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 238");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY,
                ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 239");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 240");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 241");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 242");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 243");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 244");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 245");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 246");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 247");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 248");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 249");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 250");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 251");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                DISABLE_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 252");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                ENABLE_NOT_FAILED);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 253");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                DISABLE_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 254");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 255");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 256");
        se = st.getEndingState(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 257");
        se = st.getEndingState(LOCKED, ENABLED, "null", "null", DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 258");
        se = st.getEndingState(LOCKED, ENABLED, "null", "null", UNLOCK);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 259");
        se = st.getEndingState(LOCKED, ENABLED, "null", "null", DISABLE_FAILED);
        assertEquals("null,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 260");
        se = st.getEndingState(LOCKED, ENABLED, "null", "null", ENABLE_NOT_FAILED);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 261");
        se = st.getEndingState(LOCKED, ENABLED, "null", "null", DISABLE_DEPENDENCY);
        assertEquals("null,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 262");
        se = st.getEndingState(LOCKED, ENABLED, "null", "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 263");
        se = st.getEndingState(LOCKED, ENABLED, "null", "null", PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 264");
        se = st.getEndingState(LOCKED, ENABLED, "null", "null", DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 265");
        se = st.getEndingState(LOCKED, ENABLED, "null", COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 266");
        se = st.getEndingState(LOCKED, ENABLED, "null", COLDSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 267");
        se = st.getEndingState(LOCKED, ENABLED, "null", COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 268");
        se = st.getEndingState(LOCKED, ENABLED, "null", COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 269");
        se = st.getEndingState(LOCKED, ENABLED, "null", COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 270");
        se = st.getEndingState(LOCKED, ENABLED, "null", COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 271");
        se = st.getEndingState(LOCKED, ENABLED, "null", COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 272");
        se = st.getEndingState(LOCKED, ENABLED, "null", COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 273");
        se = st.getEndingState(LOCKED, ENABLED, "null", HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 274");
        se = st.getEndingState(LOCKED, ENABLED, "null", HOTSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 275");
        se = st.getEndingState(LOCKED, ENABLED, "null", HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 276");
        se = st.getEndingState(LOCKED, ENABLED, "null", HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 277");
        se = st.getEndingState(LOCKED, ENABLED, "null", HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 278");
        se = st.getEndingState(LOCKED, ENABLED, "null", HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 279");
        se = st.getEndingState(LOCKED, ENABLED, "null", HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 280");
        se = st.getEndingState(LOCKED, ENABLED, "null", HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 281");
        se = st.getEndingState(LOCKED, ENABLED, "null", PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 282");
        se = st.getEndingState(LOCKED, ENABLED, "null", PROVIDINGSERVICE, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 283");
        se = st.getEndingState(LOCKED, ENABLED, "null", PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 284");
        se = st.getEndingState(LOCKED, ENABLED, "null", PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 285");
        se = st.getEndingState(LOCKED, ENABLED, "null", PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 286");
        se = st.getEndingState(LOCKED, ENABLED, "null", PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 287");
        se = st.getEndingState(LOCKED, ENABLED, "null", PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 288");
        se = st.getEndingState(LOCKED, ENABLED, "null", PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 289");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, "null", DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 290");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, "null", UNLOCK);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 291");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, "null", DISABLE_FAILED);
        assertEquals("null,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 292");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, "null", ENABLE_NOT_FAILED);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 293");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, "null", DISABLE_DEPENDENCY);
        assertEquals("null,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 294");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 295");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, "null", PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 296");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, "null", DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 297");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 298");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, COLDSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 299");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 300");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 301");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 302");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 303");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 304");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 305");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 306");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, HOTSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 307");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 308");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 309");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 310");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 311");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 312");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 313");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 314");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 315");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 316");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 317");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 318");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 319");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 320");
        se = st.getEndingState(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 321");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, "null", DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 322");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, "null", UNLOCK);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 323");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, "null", DISABLE_FAILED);
        assertEquals("null,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 324");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, "null", ENABLE_NOT_FAILED);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 325");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, "null", DISABLE_DEPENDENCY);
        assertEquals("null,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 326");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 327");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, "null", PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 328");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, "null", DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 329");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 330");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 331");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 332");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 333");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 334");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 335");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 336");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 337");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 338");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 339");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 340");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 341");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 342");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 343");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 344");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 345");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 346");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 347");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 348");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 349");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 350");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 351");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 352");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 353");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 354");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", UNLOCK);
        assertEquals("null,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 355");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", DISABLE_FAILED);
        assertEquals("null,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 356");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", ENABLE_NOT_FAILED);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 357");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", DISABLE_DEPENDENCY);
        assertEquals("null,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 358");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 359");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 360");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 361");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 362");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 363");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 364");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 365");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 366");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 367");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 368");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 369");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 370");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 371");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 372");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 373");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 374");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 375");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 376");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 377");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 378");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, UNLOCK);
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 379");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 380");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 381");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 382");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 383");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 384");
        se = st.getEndingState(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 385");
        se = st.getEndingState(LOCKED, DISABLED, "null", "null", DEMOTE);
        assertEquals("coldstandby,locked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 386");
        se = st.getEndingState(LOCKED, DISABLED, "null", "null", UNLOCK);
        assertEquals("null,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 387");
        se = st.getEndingState(LOCKED, DISABLED, "null", "null", DISABLE_FAILED);
        assertEquals("null,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 388");
        se = st.getEndingState(LOCKED, DISABLED, "null", "null", ENABLE_NOT_FAILED);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 389");
        se = st.getEndingState(LOCKED, DISABLED, "null", "null", DISABLE_DEPENDENCY);
        assertEquals("null,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 390");
        se = st.getEndingState(LOCKED, DISABLED, "null", "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 391");
        se = st.getEndingState(LOCKED, DISABLED, "null", "null", PROMOTE);
        assertEquals("coldstandby,locked,disabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 392");
        se = st.getEndingState(LOCKED, DISABLED, "null", "null", DEMOTE);
        assertEquals("coldstandby,locked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 393");
        se = st.getEndingState(LOCKED, DISABLED, "null", COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 394");
        se = st.getEndingState(LOCKED, DISABLED, "null", COLDSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 395");
        se = st.getEndingState(LOCKED, DISABLED, "null", COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 396");
        se = st.getEndingState(LOCKED, DISABLED, "null", COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 397");
        se = st.getEndingState(LOCKED, DISABLED, "null", COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 398");
        se = st.getEndingState(LOCKED, DISABLED, "null", COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 399");
        se = st.getEndingState(LOCKED, DISABLED, "null", COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,disabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 400");
        se = st.getEndingState(LOCKED, DISABLED, "null", COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 401");
        se = st.getEndingState(LOCKED, DISABLED, "null", HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 402");
        se = st.getEndingState(LOCKED, DISABLED, "null", HOTSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 403");
        se = st.getEndingState(LOCKED, DISABLED, "null", HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 404");
        se = st.getEndingState(LOCKED, DISABLED, "null", HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 405");
        se = st.getEndingState(LOCKED, DISABLED, "null", HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 406");
        se = st.getEndingState(LOCKED, DISABLED, "null", HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 407");
        se = st.getEndingState(LOCKED, DISABLED, "null", HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,disabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 408");
        se = st.getEndingState(LOCKED, DISABLED, "null", HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 409");
        se = st.getEndingState(LOCKED, DISABLED, "null", PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 410");
        se = st.getEndingState(LOCKED, DISABLED, "null", PROVIDINGSERVICE, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 411");
        se = st.getEndingState(LOCKED, DISABLED, "null", PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 412");
        se = st.getEndingState(LOCKED, DISABLED, "null", PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 413");
        se = st.getEndingState(LOCKED, DISABLED, "null", PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 414");
        se = st.getEndingState(LOCKED, DISABLED, "null", PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 415");
        se = st.getEndingState(LOCKED, DISABLED, "null", PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,locked,disabled,null,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 416");
        se = st.getEndingState(LOCKED, DISABLED, "null", PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,disabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 417");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, "null", DEMOTE);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 418");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, "null", UNLOCK);
        assertEquals("null,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 419");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, "null", DISABLE_FAILED);
        assertEquals("null,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 420");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, "null", ENABLE_NOT_FAILED);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 421");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, "null", DISABLE_DEPENDENCY);
        assertEquals("null,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 422");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 423");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, "null", PROMOTE);
        assertEquals("coldstandby,locked,disabled,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 424");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, "null", DEMOTE);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 425");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 426");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, COLDSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 427");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 428");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 429");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 430");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 431");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,disabled,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 432");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 433");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 434");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, HOTSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 435");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 436");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 437");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 438");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 439");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,disabled,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 440");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 441");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 442");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 443");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 444");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 445");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 446");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 447");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,locked,disabled,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 448");
        se = st.getEndingState(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 449");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, "null", DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 450");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, "null", UNLOCK);
        assertEquals("null,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 451");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, "null", DISABLE_FAILED);
        assertEquals("null,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 452");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, "null", ENABLE_NOT_FAILED);
        assertEquals("null,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 453");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, "null", DISABLE_DEPENDENCY);
        assertEquals("null,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 454");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 455");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, "null", PROMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 456");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, "null", DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 457");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 458");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 459");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 460");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 461");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 462");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 463");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 464");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 465");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 466");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 467");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 468");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 469");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 470");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 471");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 472");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 473");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 474");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 475");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 476");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 477");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 478");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,enabled,null,", makeString(se));

        logger.info("??? StateTransition testcase 479");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 480");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 481");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 482");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", UNLOCK);
        assertEquals("null,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 483");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", DISABLE_FAILED);
        assertEquals("null,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 484");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", ENABLE_NOT_FAILED);
        assertEquals("null,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 485");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", DISABLE_DEPENDENCY);
        assertEquals("null,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 486");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", ENABLE_NO_DEPENDENCY);
        assertEquals("null,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 487");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", PROMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 488");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 489");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 490");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 491");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 492");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 493");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 494");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 495");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 496");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 497");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 498");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 499");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 500");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 501");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 502");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 503");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, PROMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 504");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 505");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 506");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, UNLOCK);
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 507");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 508");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                ENABLE_NOT_FAILED);
        assertEquals("coldstandby,locked,disabled,dependency,", makeString(se));

        logger.info("??? StateTransition testcase 509");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                DISABLE_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("??? StateTransition testcase 510");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE,
                ENABLE_NO_DEPENDENCY);
        assertEquals("coldstandby,locked,disabled,failed,", makeString(se));

        logger.info("??? StateTransition testcase 511");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, PROMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,StandbyStatusException", makeString(se));

        logger.info("??? StateTransition testcase 512");
        se = st.getEndingState(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE);
        assertEquals("coldstandby,locked,disabled,dependency,failed,", makeString(se));

        logger.info("\n\nStateTransitionTest: Exit\n\n");
    }

    /**
     * Converts a state element to a comma-separated string.
     *
     * @param se element to be converted
     * @return a string representing the element
     */
    private String makeString(StateElement se) {
        if (se == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        String endingStandbyStatus = se.getEndingStandbyStatus();
        if (endingStandbyStatus != null) {
            stringBuilder.append(endingStandbyStatus.replace(".", ","));
            stringBuilder.append(',');
        }

        stringBuilder.append(se.getEndingAdminState());
        stringBuilder.append(',');
        stringBuilder.append(se.getEndingOpState());
        stringBuilder.append(',');
        stringBuilder.append(se.getEndingAvailStatus());
        stringBuilder.append(',');
        stringBuilder.append(se.getException());

        return stringBuilder.toString();
    }
}
