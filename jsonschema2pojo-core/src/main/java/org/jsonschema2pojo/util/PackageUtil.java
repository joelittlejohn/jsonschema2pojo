/**
 * Copyright © 2010-2014 Nokia
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class PackageUtil {

    private static final String COLON = ":";
    private static final String ESCAPED_DOT = "\\.";
    private static final String DOT = ".";
    private static final String SLASH = "/";
    private static final String END_CHARS = "#?&/";
    private static final String HASH = "#";
    private static final String SUBDIR = "../";
    private static final String EMPTY_STRING = "";

    private static final List<String> OMITTED_PREFIXES = new ArrayList<String>();

    static
    {
        OMITTED_PREFIXES.add("file");
        OMITTED_PREFIXES.add("http");
        OMITTED_PREFIXES.add("https");
        OMITTED_PREFIXES.add("java");
        OMITTED_PREFIXES.add("classpath");
        OMITTED_PREFIXES.add("resource");
    }

    private PackageUtil() {
        // this utility class shouln't be intantiated
    }
    public static String resolve(String packageBase, String refAsText) {
        StringBuilder sb = new StringBuilder(128);

        String relativePath = refToRelativePath(refAsText);

        if (packageBase != null) {
            String[] parts = packageBase.split(ESCAPED_DOT);

            int c = StringUtils.countMatches(relativePath, SUBDIR);

            // handle ("foo.bar", "../../any.json")
            if (parts.length > c) {
                sb.append(parts[0]);
                for (int ii = 1; ii < parts.length - c; ii++) {
                    sb.append(DOT).append(parts[ii]);
                }
            } else if (parts.length < c){
                return EMPTY_STRING;
            }

        }

        String prefix = EMPTY_STRING;

        int colonIndex = relativePath.indexOf(COLON);
        if (colonIndex > -1) {
            prefix = relativePath.substring(0, colonIndex);
        }

        if (relativePath.contains(SLASH) && !(OMITTED_PREFIXES.contains(prefix))) {
            relativePath = relativePath.replace(SUBDIR, EMPTY_STRING);
            int lastSlash = relativePath.lastIndexOf(SLASH);
            if (lastSlash > -1) {
                relativePath = relativePath.substring(0, lastSlash);
            }
            relativePath = relativePath.replace(SLASH, DOT);
            if (sb.length() > 0 && relativePath.length() > 0) {
                sb.append(DOT);
            }
            sb.append(relativePath);
        } 
        return sb.toString();
    }

    private static String refToRelativePath(String refAsText) {

        String result = EMPTY_STRING;

        // local ref - current path
        if (refAsText == null || refAsText.startsWith(HASH)) {
            return result;
        }

        result = StringUtils.stripEnd(refAsText, END_CHARS);

        // handle paths with hash inside like '/foo/bar.json/#/any'
        if (result.contains(HASH)) {
            result = StringUtils.stripEnd(result.substring(0, result.indexOf(HASH)), END_CHARS);
        }

        if (result.contains(SLASH)) {
            result = result.substring(0, result.lastIndexOf(SLASH) + 1);
        } else {
            result = EMPTY_STRING;
        }
        return result ;
    }
}
