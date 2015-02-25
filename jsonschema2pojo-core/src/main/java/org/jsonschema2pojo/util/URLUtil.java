/**
 * Copyright ¬© 2010-2014 Nokia
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

import org.apache.commons.lang.StringUtils;
import org.jsonschema2pojo.URLProtocol;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URLUtil {

    public static URLProtocol parseProtocol(String input) {
        return URLProtocol.fromString(StringUtils.substringBefore(input, ":"));
    }

    public static URL parseURL(String input) {
        try {
            switch (parseProtocol(input)) {
                case NO_PROTOCOL:
                    return new File(input).toURI().toURL();
                default:
                    return URI.create(input).toURL();
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Unable to parse source: %s", input), e);
        }
    }

    public static File getFileFromURL(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(String.format("URL contains an invalid URI syntax: %s", url), e);
        }
    }
}
