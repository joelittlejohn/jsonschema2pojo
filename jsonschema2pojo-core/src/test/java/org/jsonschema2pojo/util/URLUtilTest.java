/**
 * Copyright © 2010-2020 Nokia
 *
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
 */

package org.jsonschema2pojo.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

public class URLUtilTest {
    @Test
    public void testParseURLNoProto() throws MalformedURLException {
        String input = "/path/to/file";
        URL expected =  new File(input).toURI().toURL();
        URL result = URLUtil.parseURL(input);
        assertEquals(expected, result);
    }

    @Test
    public void testParseURLFileProto() throws MalformedURLException {
        String input = "file:///path/to/file";
        URL expected =  URI.create(input).toURL();
        URL result = URLUtil.parseURL(input);
        assertEquals(expected, result);
    }

    @Test
    public void testGetFileFromUrl() throws URISyntaxException {
        URL url = getClass().getResource("/schema/address.json");

        assert url != null;
        File expected = new File(url.toURI());

        File result = URLUtil.getFileFromURL(url);
        assertEquals(expected, result);
    }

    @Test
    public void testSymlinkGetFileFromUrl() throws URISyntaxException, IOException {
        URL symlinkUrl = getClass().getResource("/schema/address-symlink.json");
        URL targetUrl = getClass().getResource("/schema/address.json");

        assert symlinkUrl != null;
        assert targetUrl != null;

        File expected = new File(targetUrl.getFile());

        File result = URLUtil.getFileFromURL(symlinkUrl);
        assertEquals(expected, result);
    }

}
