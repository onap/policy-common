/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.Test;

/**
 * Test text file utilities.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class TextFileUtilsTest {

    private static final String FILE_CONTENT = "This is the contents of a text file";

    @Test
    public void testPutToFile() throws IOException {
        final File tempTextFile = File.createTempFile("Test", "txt");

        TextFileUtils.putStringAsTextFile(FILE_CONTENT, tempTextFile.getAbsolutePath());

        final String textFileString0 = TextFileUtils.getTextFileAsString(tempTextFile.getAbsolutePath());
        assertEquals(FILE_CONTENT, textFileString0);

        final FileInputStream fis = new FileInputStream(tempTextFile);
        final String textFileString1 = TextFileUtils.getStreamAsString(fis);
        assertEquals(textFileString0, textFileString1);
    }

    @Test
    public void testPutToFileWithNewPath() throws IOException {
        String tempDirAndFileName = System.getProperty("java.io.tmpdir") + "/non/existant/path/Test.txt";

        TextFileUtils.putStringAsTextFile(FILE_CONTENT, tempDirAndFileName);

        final String textFileString0 = TextFileUtils.getTextFileAsString(tempDirAndFileName);
        assertEquals(FILE_CONTENT, textFileString0);

        final FileInputStream fis = new FileInputStream(tempDirAndFileName);
        final String textFileString1 = TextFileUtils.getStreamAsString(fis);
        assertEquals(textFileString0, textFileString1);

        File tempDirAndFile = new File(tempDirAndFileName);
        tempDirAndFile.delete();
        tempDirAndFile.getParentFile().delete();
        tempDirAndFile.getParentFile().getParentFile().delete();
        tempDirAndFile.getParentFile().getParentFile().getParentFile().delete();
    }
}
