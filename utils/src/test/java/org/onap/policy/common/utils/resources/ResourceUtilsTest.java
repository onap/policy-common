/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Ericsson. All rights reserved.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020 Nordix Foundation.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class ResourceUtilsTest.
 *
 * @author Liam Fallon (liam.fallon@ericsson.com)
 */
public class ResourceUtilsTest {
    private File tmpDir = null;
    private File tmpEmptyFile = null;
    private File tmpUsedFile = null;

    private String jarDirResource = null;
    private String jarFileResource = null;

    private static final String RESOURCES_PATH = "src/test/resources/";
    private static final String PATH_DIR_RESOURCE = "testdir";
    private static final String PATH_FILE_RESOURCE = "testdir/testfile.xml";

    private static final String NON_EXISTENT_RESOURCE = "somewhere/over/the/rainbow";
    private static final String INVALID_RESOURCE = "@%%%\\\\_:::DESD";

    /**
     * Setup resource utils test.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Before
    public void setupResourceUtilsTest() throws IOException {
        tmpDir = new File(System.getProperty("java.io.tmpdir"));
        tmpEmptyFile = File.createTempFile(this.getClass().getName(), ".tmp");
        tmpUsedFile = File.createTempFile(this.getClass().getName(), ".tmp");

        jarDirResource = "META-INF";
        jarFileResource = "META-INF/MANIFEST.MF";

        try (final FileWriter fileWriter = new FileWriter(tmpUsedFile)) {
            fileWriter.write("Bluebirds fly over the rainbow");
        }
    }

    /**
     * Cleandown resource utils test.
     */
    @After
    public void cleandownResourceUtilsTest() {
        tmpEmptyFile.delete();
        tmpUsedFile.delete();
    }

    /**
     * Test get url resource.
     */
    @Test
    public void testgetUrlResource() {
        URL theUrl = ResourceUtils.getUrlResource(tmpDir.getAbsolutePath());
        assertNull(theUrl);

        theUrl = ResourceUtils.getUrlResource(tmpEmptyFile.getAbsolutePath());
        assertNull(theUrl);

        theUrl = ResourceUtils.getUrlResource(tmpUsedFile.getAbsolutePath());
        assertNull(theUrl);

        theUrl = ResourceUtils.getUrlResource(jarDirResource);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrlResource(jarFileResource);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrlResource(PATH_DIR_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrlResource(PATH_FILE_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrlResource("file:///" + PATH_DIR_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(RESOURCES_PATH + PATH_DIR_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(RESOURCES_PATH + PATH_FILE_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrlResource(NON_EXISTENT_RESOURCE);
        assertNull(theUrl);

        theUrl = ResourceUtils.getUrlResource(INVALID_RESOURCE);
        assertNull(theUrl);

        theUrl = ResourceUtils.getUrlResource(null);
        assertNull(theUrl);
    }

    /**
     * Test get local file.
     */
    @Test
    public void testGetLocalFile() {
        URL theUrl = ResourceUtils.getLocalFile(tmpDir.getAbsolutePath());
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(tmpEmptyFile.getAbsolutePath());
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(tmpUsedFile.getAbsolutePath());
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(jarDirResource);
        assertNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(jarFileResource);
        assertNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(PATH_DIR_RESOURCE);
        assertNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(PATH_FILE_RESOURCE);
        assertNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(RESOURCES_PATH + PATH_DIR_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(RESOURCES_PATH + PATH_FILE_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(NON_EXISTENT_RESOURCE);
        assertNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(INVALID_RESOURCE);
        assertNull(theUrl);

        theUrl = ResourceUtils.getLocalFile("file:///");
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getLocalFile("file:///testdir/testfile.xml");
        assertNull(theUrl);

        theUrl = ResourceUtils.getLocalFile(null);
        assertNull(theUrl);
    }

    /**
     * Test get resource as stream.
     */
    @Test
    public void testGetResourceAsStream() {
        InputStream theStream = ResourceUtils.getResourceAsStream(tmpDir.getAbsolutePath());
        assertNotNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(tmpEmptyFile.getAbsolutePath());
        assertNotNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(tmpUsedFile.getAbsolutePath());
        assertNotNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(jarDirResource);
        assertNotNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(jarFileResource);
        assertNotNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(PATH_DIR_RESOURCE);
        assertNotNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(PATH_FILE_RESOURCE);
        assertNotNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(RESOURCES_PATH + PATH_DIR_RESOURCE);
        assertNotNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(RESOURCES_PATH + PATH_FILE_RESOURCE);
        assertNotNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(NON_EXISTENT_RESOURCE);
        assertNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(INVALID_RESOURCE);
        assertNull(theStream);

        theStream = ResourceUtils.getResourceAsStream(null);
        assertNull(theStream);

        theStream = ResourceUtils.getResourceAsStream("");
        assertNotNull(theStream);
    }

    /**
     * Test get resource as string.
     */
    @Test
    public void testGetResourceAsString() {
        String theString = ResourceUtils.getResourceAsString(tmpDir.getAbsolutePath());
        assertNotNull(theString);

        theString = ResourceUtils.getResourceAsString(tmpEmptyFile.getAbsolutePath());
        assertEquals("", theString);

        theString = ResourceUtils.getResourceAsString(tmpUsedFile.getAbsolutePath());
        assertEquals("Bluebirds fly over the rainbow", theString);

        theString = ResourceUtils.getResourceAsString(jarFileResource);
        assertNotNull(theString);

        theString = ResourceUtils.getResourceAsString(PATH_DIR_RESOURCE);
        assertNotNull(theString);

        theString = ResourceUtils.getResourceAsString(PATH_FILE_RESOURCE);
        assertNotNull(theString);

        theString = ResourceUtils.getResourceAsString(RESOURCES_PATH + PATH_DIR_RESOURCE);
        assertNotNull(theString);

        theString = ResourceUtils.getResourceAsString(RESOURCES_PATH + PATH_FILE_RESOURCE);
        assertNotNull(theString);

        theString = ResourceUtils.getResourceAsString(NON_EXISTENT_RESOURCE);
        assertNull(theString);

        theString = ResourceUtils.getResourceAsString(INVALID_RESOURCE);
        assertNull(theString);

        theString = ResourceUtils.getResourceAsString(null);
        assertNull(theString);

        theString = ResourceUtils.getResourceAsString("");

        assertEquals("logback-test.xml\nMETA-INF\norg\ntestdir\n", theString);

    }

    @Test
    public void testgetUrl4Resource() {
        URL theUrl = ResourceUtils.getUrl4Resource(tmpDir.getAbsolutePath());
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(tmpEmptyFile.getAbsolutePath());
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(tmpUsedFile.getAbsolutePath());
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(jarDirResource);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(jarFileResource);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(PATH_DIR_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(PATH_FILE_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(RESOURCES_PATH + PATH_DIR_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(RESOURCES_PATH + PATH_FILE_RESOURCE);
        assertNotNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(NON_EXISTENT_RESOURCE);
        assertNull(theUrl);

        theUrl = ResourceUtils.getUrl4Resource(INVALID_RESOURCE);
        assertNull(theUrl);
    }

    @Test
    public void testGetFilePath4Resource() {
        assertNull(ResourceUtils.getFilePath4Resource(null));
        assertEquals("/something/else", ResourceUtils.getFilePath4Resource("/something/else"));
        assertTrue(ResourceUtils.getFilePath4Resource("xml/example.xml").endsWith("xml/example.xml"));
        assertTrue(ResourceUtils.getFilePath4Resource("com/google").endsWith("com/google"));
    }

    @Test
    public void testGetDirectoryContents() throws MalformedURLException {
        assertTrue(ResourceUtils.getDirectoryContents(null).isEmpty());
        assertTrue(ResourceUtils.getDirectoryContents("idontexist").isEmpty());
        assertTrue(ResourceUtils.getDirectoryContents("logback-test.xml").isEmpty());

        Set<String> resultD0 = ResourceUtils.getDirectoryContents("testdir");
        assertEquals(1, resultD0.size());
        assertEquals("testdir/testfile.xml", normalizePath(resultD0.iterator().next()));

        Set<String> resultD1 = ResourceUtils.getDirectoryContents("org/onap/policy/common");
        assertTrue(resultD1.size() > 0);
        assertEquals("org/onap/policy/common/utils/", normalizePath(resultD1.iterator().next()));

        Set<String> resultD2 = ResourceUtils.getDirectoryContents("org/onap/policy/common/utils/coder");
        assertTrue(resultD2.size() >= 15);
        assertEquals("org/onap/policy/common/utils/coder/CoderExceptionTest.class",
                        normalizePath(resultD2.iterator().next()));

        Set<String> resultJ0 = ResourceUtils.getDirectoryContents("com");
        assertEquals(189, resultJ0.size());
        assertEquals("com/google/", normalizePath(resultJ0.iterator().next()));

        Set<String> resultJ1 = ResourceUtils.getDirectoryContents("com/google/gson");
        assertTrue(resultJ1.size() > 1);
        assertTrue(resultJ1.contains("com/google/gson/JsonElement.class"));

        URL dummyUrl = new URL("http://even/worse");
        assertTrue(ResourceUtils.getDirectoryContentsJar(dummyUrl, "nonexistantdirectory").isEmpty());

    }

    /**
     * Normalizes a path name, replacing OS-specific separators with "/".
     * @param pathName path name to be normalized
     * @return the normalized path name
     */
    private String normalizePath(String pathName) {
        return pathName.replace(File.separator, "/");
    }
}
