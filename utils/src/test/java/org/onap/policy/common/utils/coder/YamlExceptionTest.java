/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.common.utils.coder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import org.junit.Test;
import org.yaml.snakeyaml.error.Mark;

public class YamlExceptionTest {
    private static final char[] str = {'d', 'a', 't', 'a'};
    private static final String DATA = new String(str);
    private static final String MESSAGE = "hello";

    private static final Mark mark = new Mark("a file", 3, 5, 9, str, 0);

    @Test
    public void testYamlExceptionStringMark() {
        YamlException ex = new YamlException(MESSAGE, mark);

        String str = ex.getMessage();
        assertThat(str).contains(MESSAGE).contains(DATA);

        str = ex.toString();
        assertThat(str).contains(MESSAGE).contains(DATA);
    }

    @Test
    public void testYamlExceptionStringMarkThrowable() {
        Throwable thr = new IOException("expected exception");
        YamlException ex = new YamlException(MESSAGE, mark, thr);

        assertSame(thr, ex.getCause());

        String str = ex.getMessage();
        assertThat(str).contains(MESSAGE).contains(DATA);

        str = ex.toString();
        assertThat(str).contains(MESSAGE).contains(DATA);
    }
}
