/*
 * ============LICENSE_START=======================================================
 * Integrity Monitor
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/*
 * All JUnits are designed to run in the local development environment
 * where they have write privileges and can execute time-sensitive
 * tasks.
 */
class StateTransitionTest {
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

    @Test
    void testBad() throws StateTransitionException {
        // bad test case
        assertEquals("coldstandby,locked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", COLDSTANDBY, "lock"));

    }

    @Test
    void test1() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", "null", "lock"));

    }

    @Test
    void test2() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", "null", UNLOCK));

    }

    @Test
    void test3() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,", makeString(UNLOCKED, ENABLED, "null", "null", DISABLE_FAILED));

    }

    @Test
    void test4() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test5() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, "null", "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test6() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test7() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", "null", PROMOTE));

    }

    @Test
    void test8() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", "null", DEMOTE));

    }

    @Test
    void test9() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", COLDSTANDBY, DEMOTE));

    }

    @Test
    void test10() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", COLDSTANDBY, UNLOCK));

    }

    @Test
    void test11() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, "null", COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test12() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test13() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, "null", COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test14() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test15() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", COLDSTANDBY, PROMOTE));

    }

    @Test
    void test16() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", COLDSTANDBY, DEMOTE));

    }

    @Test
    void test17() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", HOTSTANDBY, DEMOTE));

    }

    @Test
    void test18() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", HOTSTANDBY, UNLOCK));

    }

    @Test
    void test19() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, "null", HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test20() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test21() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, "null", HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test22() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test23() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", HOTSTANDBY, PROMOTE));

    }

    @Test
    void test24() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, "null", HOTSTANDBY, DEMOTE));

    }

    @Test
    void test25() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test26() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test27() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test28() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test29() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test30() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test31() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test32() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, "null", PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test33() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, "null", DEMOTE));

    }

    @Test
    void test34() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, "null", UNLOCK));

    }

    @Test
    void test35() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,", makeString(UNLOCKED, ENABLED, FAILED, "null", DISABLE_FAILED));

    }

    @Test
    void test36() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test37() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, FAILED, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test38() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test39() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, "null", PROMOTE));

    }

    @Test
    void test40() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, "null", DEMOTE));

    }

    @Test
    void test41() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test42() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test43() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test44() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test45() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test46() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test47() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test48() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test49() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test50() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test51() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test52() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test53() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test54() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test55() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test56() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test57() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test58() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test59() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test60() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test61() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test62() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test63() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test64() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test65() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, DEPENDENCY, "null", DEMOTE));

    }

    @Test
    void test66() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, DEPENDENCY, "null", UNLOCK));

    }

    @Test
    void test67() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, "null", DISABLE_FAILED));

    }

    @Test
    void test68() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test69() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test70() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test71() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, "null", PROMOTE));

    }

    @Test
    void test72() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, DEPENDENCY, "null", DEMOTE));

    }

    @Test
    void test73() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test74() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test75() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test76() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test77() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test78() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test79() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test80() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test81() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test82() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test83() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test84() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test85() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test86() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test87() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test88() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test89() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test90() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test91() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test92() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test93() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test94() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test95() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test96() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test97() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", DEMOTE));

    }

    @Test
    void test98() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", UNLOCK));

    }

    @Test
    void test99() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", DISABLE_FAILED));

    }

    @Test
    void test100() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test101() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test102() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test103() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", PROMOTE));

    }

    @Test
    void test104() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, "null", DEMOTE));

    }

    @Test
    void test105() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test106() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test107() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test108() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test109() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test110() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test111() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test112() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test113() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test114() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test115() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test116() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test117() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test118() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test119() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test120() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test121() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test122() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test123() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test124() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test125() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test126() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test127() throws StateTransitionException {
        assertEquals("providingservice,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test128() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test129() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(UNLOCKED, DISABLED, "null", "null", DEMOTE));

    }

    @Test
    void test130() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,null,", makeString(UNLOCKED, DISABLED, "null", "null", UNLOCK));

    }

    @Test
    void test131() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,", makeString(UNLOCKED, DISABLED, "null", "null", DISABLE_FAILED));

    }

    @Test
    void test132() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(UNLOCKED, DISABLED, "null", "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test133() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, "null", "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test134() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test135() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, "null", "null", PROMOTE));

    }

    @Test
    void test136() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(UNLOCKED, DISABLED, "null", "null", DEMOTE));

    }

    @Test
    void test137() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", COLDSTANDBY, DEMOTE));

    }

    @Test
    void test138() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", COLDSTANDBY, UNLOCK));

    }

    @Test
    void test139() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, "null", COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test140() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test141() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, "null", COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test142() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test143() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, "null", COLDSTANDBY, PROMOTE));

    }

    @Test
    void test144() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", COLDSTANDBY, DEMOTE));

    }

    @Test
    void test145() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(UNLOCKED, DISABLED, "null", HOTSTANDBY, DEMOTE));

    }

    @Test
    void test146() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(UNLOCKED, DISABLED, "null", HOTSTANDBY, UNLOCK));

    }

    @Test
    void test147() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, "null", HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test148() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test149() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, "null", HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test150() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test151() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, "null", HOTSTANDBY, PROMOTE));

    }

    @Test
    void test152() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(UNLOCKED, DISABLED, "null", HOTSTANDBY, DEMOTE));

    }

    @Test
    void test153() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test154() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test155() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test156() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test157() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test158() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test159() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test160() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,",
                        makeString(UNLOCKED, DISABLED, "null", PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test161() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(UNLOCKED, DISABLED, FAILED, "null", DEMOTE));

    }

    @Test
    void test162() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,", makeString(UNLOCKED, DISABLED, FAILED, "null", UNLOCK));

    }

    @Test
    void test163() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,", makeString(UNLOCKED, DISABLED, FAILED, "null", DISABLE_FAILED));

    }

    @Test
    void test164() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(UNLOCKED, DISABLED, FAILED, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test165() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test166() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test167() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, FAILED, "null", PROMOTE));

    }

    @Test
    void test168() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(UNLOCKED, DISABLED, FAILED, "null", DEMOTE));

    }

    @Test
    void test169() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test170() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test171() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test172() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test173() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test174() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test175() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test176() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test177() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test178() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test179() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test180() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test181() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test182() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test183() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test184() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test185() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test186() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test187() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test188() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test189() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test190() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test191() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test192() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test193() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, "null", DEMOTE));

    }

    @Test
    void test194() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,", makeString(UNLOCKED, DISABLED, DEPENDENCY, "null", UNLOCK));

    }

    @Test
    void test195() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, "null", DISABLE_FAILED));

    }

    @Test
    void test196() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test197() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test198() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test199() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, "null", PROMOTE));

    }

    @Test
    void test200() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, "null", DEMOTE));

    }

    @Test
    void test201() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test202() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test203() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test204() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test205() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test206() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test207() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test208() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test209() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test210() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test211() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test212() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test213() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test214() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test215() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test216() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test217() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test218() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test219() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test220() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test221() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test222() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test223() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test224() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test225() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", DEMOTE));

    }

    @Test
    void test226() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", UNLOCK));

    }

    @Test
    void test227() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", DISABLE_FAILED));

    }

    @Test
    void test228() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test229() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test230() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test231() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", PROMOTE));

    }

    @Test
    void test232() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, "null", DEMOTE));

    }

    @Test
    void test233() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test234() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test235() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test236() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test237() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test238() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test239() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test240() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test241() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test242() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test243() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test244() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test245() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test246() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test247() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test248() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test249() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test250() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test251() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test252() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test253() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test254() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test255() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,StandbyStatusException",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test256() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(UNLOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test257() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", "null", DEMOTE));

    }

    @Test
    void test258() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(LOCKED, ENABLED, "null", "null", UNLOCK));

    }

    @Test
    void test259() throws StateTransitionException {
        assertEquals("null,locked,disabled,failed,", makeString(LOCKED, ENABLED, "null", "null", DISABLE_FAILED));

    }

    @Test
    void test260() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test261() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, "null", "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test262() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test263() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, "null", "null", PROMOTE));

    }

    @Test
    void test264() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", "null", DEMOTE));

    }

    @Test
    void test265() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", COLDSTANDBY, DEMOTE));

    }

    @Test
    void test266() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(LOCKED, ENABLED, "null", COLDSTANDBY, UNLOCK));

    }

    @Test
    void test267() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, "null", COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test268() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, "null", COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test269() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, "null", COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test270() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, "null", COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test271() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, "null", COLDSTANDBY, PROMOTE));

    }

    @Test
    void test272() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", COLDSTANDBY, DEMOTE));

    }

    @Test
    void test273() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", HOTSTANDBY, DEMOTE));

    }

    @Test
    void test274() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(LOCKED, ENABLED, "null", HOTSTANDBY, UNLOCK));

    }

    @Test
    void test275() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, "null", HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test276() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, "null", HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test277() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, "null", HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test278() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, "null", HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test279() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, "null", HOTSTANDBY, PROMOTE));

    }

    @Test
    void test280() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", HOTSTANDBY, DEMOTE));

    }

    @Test
    void test281() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test282() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(LOCKED, ENABLED, "null", PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test283() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, "null", PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test284() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, "null", PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test285() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, "null", PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test286() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, "null", PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test287() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, "null", PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test288() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, "null", PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test289() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, "null", DEMOTE));

    }

    @Test
    void test290() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, "null", UNLOCK));

    }

    @Test
    void test291() throws StateTransitionException {
        assertEquals("null,locked,disabled,failed,", makeString(LOCKED, ENABLED, FAILED, "null", DISABLE_FAILED));

    }

    @Test
    void test292() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test293() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, FAILED, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test294() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test295() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, FAILED, "null", PROMOTE));

    }

    @Test
    void test296() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, "null", DEMOTE));

    }

    @Test
    void test297() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test298() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test299() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, FAILED, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test300() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, FAILED, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test301() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, FAILED, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test302() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test303() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, FAILED, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test304() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test305() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test306() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test307() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, FAILED, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test308() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, FAILED, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test309() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, FAILED, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test310() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test311() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, FAILED, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test312() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test313() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test314() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test315() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test316() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test317() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test318() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test319() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test320() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test321() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, "null", DEMOTE));

    }

    @Test
    void test322() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, "null", UNLOCK));

    }

    @Test
    void test323() throws StateTransitionException {
        assertEquals("null,locked,disabled,failed,", makeString(LOCKED, ENABLED, DEPENDENCY, "null", DISABLE_FAILED));

    }

    @Test
    void test324() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test325() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test326() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test327() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, DEPENDENCY, "null", PROMOTE));

    }

    @Test
    void test328() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, "null", DEMOTE));

    }

    @Test
    void test329() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test330() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test331() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test332() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test333() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test334() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test335() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test336() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test337() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test338() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test339() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test340() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test341() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test342() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test343() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test344() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test345() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test346() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test347() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test348() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test349() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test350() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test351() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test352() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test353() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", DEMOTE));

    }

    @Test
    void test354() throws StateTransitionException {
        assertEquals("null,unlocked,enabled,null,", makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", UNLOCK));

    }

    @Test
    void test355() throws StateTransitionException {
        assertEquals("null,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", DISABLE_FAILED));

    }

    @Test
    void test356() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test357() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test358() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test359() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", PROMOTE));

    }

    @Test
    void test360() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, "null", DEMOTE));

    }

    @Test
    void test361() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test362() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test363() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test364() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test365() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test366() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test367() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test368() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test369() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test370() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test371() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test372() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test373() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test374() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test375() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test376() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test377() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test378() throws StateTransitionException {
        assertEquals("hotstandby,unlocked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test379() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test380() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test381() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test382() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test383() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,StandbyStatusException",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test384() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, ENABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test385() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,", makeString(LOCKED, DISABLED, "null", "null", DEMOTE));

    }

    @Test
    void test386() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,null,", makeString(LOCKED, DISABLED, "null", "null", UNLOCK));

    }

    @Test
    void test387() throws StateTransitionException {
        assertEquals("null,locked,disabled,failed,", makeString(LOCKED, DISABLED, "null", "null", DISABLE_FAILED));

    }

    @Test
    void test388() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,", makeString(LOCKED, DISABLED, "null", "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test389() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, "null", "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test390() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,", makeString(LOCKED, DISABLED, "null", "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test391() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,StandbyStatusException",
                        makeString(LOCKED, DISABLED, "null", "null", PROMOTE));

    }

    @Test
    void test392() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,", makeString(LOCKED, DISABLED, "null", "null", DEMOTE));

    }

    @Test
    void test393() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,", makeString(LOCKED, DISABLED, "null", COLDSTANDBY, DEMOTE));

    }

    @Test
    void test394() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(LOCKED, DISABLED, "null", COLDSTANDBY, UNLOCK));

    }

    @Test
    void test395() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, "null", COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test396() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, "null", COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test397() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, "null", COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test398() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, "null", COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test399() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,StandbyStatusException",
                        makeString(LOCKED, DISABLED, "null", COLDSTANDBY, PROMOTE));

    }

    @Test
    void test400() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,", makeString(LOCKED, DISABLED, "null", COLDSTANDBY, DEMOTE));

    }

    @Test
    void test401() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,", makeString(LOCKED, DISABLED, "null", HOTSTANDBY, DEMOTE));

    }

    @Test
    void test402() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,", makeString(LOCKED, DISABLED, "null", HOTSTANDBY, UNLOCK));

    }

    @Test
    void test403() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, "null", HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test404() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, "null", HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test405() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, "null", HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test406() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, "null", HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test407() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,StandbyStatusException",
                        makeString(LOCKED, DISABLED, "null", HOTSTANDBY, PROMOTE));

    }

    @Test
    void test408() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,", makeString(LOCKED, DISABLED, "null", HOTSTANDBY, DEMOTE));

    }

    @Test
    void test409() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,",
                        makeString(LOCKED, DISABLED, "null", PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test410() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,null,",
                        makeString(LOCKED, DISABLED, "null", PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test411() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, "null", PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test412() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, "null", PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test413() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, "null", PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test414() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, "null", PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test415() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,StandbyStatusException",
                        makeString(LOCKED, DISABLED, "null", PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test416() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,null,",
                        makeString(LOCKED, DISABLED, "null", PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test417() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,", makeString(LOCKED, DISABLED, FAILED, "null", DEMOTE));

    }

    @Test
    void test418() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,failed,", makeString(LOCKED, DISABLED, FAILED, "null", UNLOCK));

    }

    @Test
    void test419() throws StateTransitionException {
        assertEquals("null,locked,disabled,failed,", makeString(LOCKED, DISABLED, FAILED, "null", DISABLE_FAILED));

    }

    @Test
    void test420() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,", makeString(LOCKED, DISABLED, FAILED, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test421() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, FAILED, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test422() throws StateTransitionException {
        assertEquals("null,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test423() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,StandbyStatusException",
                        makeString(LOCKED, DISABLED, FAILED, "null", PROMOTE));

    }

    @Test
    void test424() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,", makeString(LOCKED, DISABLED, FAILED, "null", DEMOTE));

    }

    @Test
    void test425() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,", makeString(LOCKED, DISABLED, FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test426() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test427() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test428() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, FAILED, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test429() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, FAILED, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test430() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test431() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,StandbyStatusException",
                        makeString(LOCKED, DISABLED, FAILED, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test432() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,", makeString(LOCKED, DISABLED, FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test433() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,", makeString(LOCKED, DISABLED, FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test434() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,", makeString(LOCKED, DISABLED, FAILED, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test435() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test436() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, FAILED, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test437() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, FAILED, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test438() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test439() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,StandbyStatusException",
                        makeString(LOCKED, DISABLED, FAILED, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test440() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,", makeString(LOCKED, DISABLED, FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test441() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test442() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test443() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test444() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test445() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test446() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test447() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,StandbyStatusException",
                        makeString(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test448() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test449() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, "null", DEMOTE));

    }

    @Test
    void test450() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,", makeString(LOCKED, DISABLED, DEPENDENCY, "null", UNLOCK));

    }

    @Test
    void test451() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, "null", DISABLE_FAILED));

    }

    @Test
    void test452() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test453() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test454() throws StateTransitionException {
        assertEquals("null,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test455() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,StandbyStatusException",
                        makeString(LOCKED, DISABLED, DEPENDENCY, "null", PROMOTE));

    }

    @Test
    void test456() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, "null", DEMOTE));

    }

    @Test
    void test457() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test458() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test459() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test460() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test461() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test462() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test463() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,StandbyStatusException",
                        makeString(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test464() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test465() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test466() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test467() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test468() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test469() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test470() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test471() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,StandbyStatusException",
                        makeString(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test472() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test473() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test474() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test475() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test476() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test477() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test478() throws StateTransitionException {
        assertEquals("coldstandby,locked,enabled,null,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test479() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,StandbyStatusException",
                        makeString(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test480() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test481() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", DEMOTE));

    }

    @Test
    void test482() throws StateTransitionException {
        assertEquals("null,unlocked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", UNLOCK));

    }

    @Test
    void test483() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", DISABLE_FAILED));

    }

    @Test
    void test484() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", ENABLE_NOT_FAILED));

    }

    @Test
    void test485() throws StateTransitionException {
        assertEquals("null,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", DISABLE_DEPENDENCY));

    }

    @Test
    void test486() throws StateTransitionException {
        assertEquals("null,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test487() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,StandbyStatusException",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", PROMOTE));

    }

    @Test
    void test488() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, "null", DEMOTE));

    }

    @Test
    void test489() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test490() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, UNLOCK));

    }

    @Test
    void test491() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test492() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test493() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test494() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test495() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,StandbyStatusException",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, PROMOTE));

    }

    @Test
    void test496() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, COLDSTANDBY, DEMOTE));

    }

    @Test
    void test497() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test498() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, UNLOCK));

    }

    @Test
    void test499() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_FAILED));

    }

    @Test
    void test500() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NOT_FAILED));

    }

    @Test
    void test501() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DISABLE_DEPENDENCY));

    }

    @Test
    void test502() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test503() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,StandbyStatusException",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, PROMOTE));

    }

    @Test
    void test504() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, HOTSTANDBY, DEMOTE));

    }

    @Test
    void test505() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE));

    }

    @Test
    void test506() throws StateTransitionException {
        assertEquals("coldstandby,unlocked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, UNLOCK));

    }

    @Test
    void test507() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_FAILED));

    }

    @Test
    void test508() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, ENABLE_NOT_FAILED));

    }

    @Test
    void test509() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DISABLE_DEPENDENCY));

    }

    @Test
    void test510() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, ENABLE_NO_DEPENDENCY));

    }

    @Test
    void test511() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,StandbyStatusException",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, PROMOTE));

    }

    @Test
    void test512() throws StateTransitionException {
        assertEquals("coldstandby,locked,disabled,dependency,failed,",
                        makeString(LOCKED, DISABLED, DEPENDENCY_FAILED, PROVIDINGSERVICE, DEMOTE));
    }

    /**
     * Converts a transition to a string.
     */
    private String makeString(String adminState, String opState, String availStatus, String standbyStatus,
                    String actionName) throws StateTransitionException {
        StateTransition st = new StateTransition();
        StateElement se = st.getEndingState(adminState, opState, availStatus, standbyStatus, actionName);
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
