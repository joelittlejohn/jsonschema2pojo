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

package org.jsonschema2pojo.cli;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import com.beust.jcommander.ParameterException;

public class UrlConverterTest {

    private UrlConverter converter = new UrlConverter("--source");

    @Test
    public void urlIsCreatedFromFilePath() {
        URL url = converter.convert("/path/to/something");

        // on *ux the path part of the URL is equal to the given path
        // on Windows C: is prepended, which is expected
        assertThat(url.getPath(), endsWith("/path/to/something"));
    }

    @Test
    public void urlIsCreatedFromFileUrl() {
        URL url = converter.convert("file:/path/to/something");

        assertThat(url.toString(), is("file:/path/to/something"));
    }

    @Test(expected = ParameterException.class)
    public void invalidUrlThrowsParameterException() {
        converter.convert("http:total nonsense");
    }

    @Test(expected = ParameterException.class)
    public void nullValueThrowsParameterException() {
        converter.convert(null);
    }

}
