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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;

import org.apache.commons.lang.StringUtils;
import org.jsonschema2pojo.URLProtocol;

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

        File file;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                String.format("URL contains an invalid URI syntax: %s", url), e
            );
        }

        // If symlink, then return the resolved File instead.
        if (Files.isSymbolicLink(file.toPath())) {
            try {
                file = file.toPath().toRealPath().toFile();
            } catch (IOException e) {
                throw new IllegalArgumentException(
                    String.format("Failed getting file from symlink URL: %s", url), e
                );
            }
        }

        return file;
    }
}
