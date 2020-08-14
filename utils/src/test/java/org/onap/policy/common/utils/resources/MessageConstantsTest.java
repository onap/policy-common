/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Bell Canada. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.common.utils.resources;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

/**
 * The Class MessageConstantsTest.
 */
public class MessageConstantsTest {

    @Test
    public void test() {
        // verify that constructor does not throw an exception
        Assertions.assertThatCode(() -> Whitebox.invokeConstructor(MessageConstants.class)).doesNotThrowAnyException();
    }
}
